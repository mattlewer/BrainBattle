package com.msl5.multiplayerquiz.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.msl5.multiplayerquiz.*
import com.msl5.multiplayerquiz.databinding.FragmentGameLobbyBinding
import com.msl5.multiplayerquiz.dataclass.User
import com.msl5.multiplayerquiz.recyclers.LobbyUserRecycler

class FragmentGameLobby : Fragment() {

    private var _binding: FragmentGameLobbyBinding? = null
    private val binding get() = _binding!!
    private var navController : NavController? = null
    private lateinit var hostListener: ValueEventListener
    private lateinit var userListener: ValueEventListener
    private lateinit var gameStartListener: ValueEventListener
    private lateinit var startGameBtn : Button
    private lateinit var adapter : LobbyUserRecycler
    private var users = mutableListOf<User>()

    var host = "";

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameLobbyBinding.inflate(inflater, container, false)
        binding.lobbyCodeText.text = roomCode
        startGameBtn = binding.startGameBtn
        listenToHost()
        listenForUsers()
        listenForGameStart()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

    fun listenToHost(){
        hostListener = FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                host = snapshot.value.toString()
                if(host == username) {
                    startGameBtn.visibility = View.VISIBLE
                    startGameBtn.setOnClickListener { startGame() }
                }else if(host == "null"){
                    Toast.makeText(binding.root.context, "Host left the game!", Toast.LENGTH_SHORT).show()
                    var intent = Intent(activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }else{
                    startGameBtn.visibility = View.GONE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    fun listenForUsers(){
        userListener = FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tempList = mutableListOf<User>()
                    for (x in snapshot.children) {
                        tempList.add(x.getValue(User::class.java)!!)
                    }
                    users = tempList
                    prepareRecycler()
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun prepareRecycler(){
        var recyclerView: RecyclerView = binding.usersRecycler
        adapter = LobbyUserRecycler(users, host, binding.root.context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        recyclerView.setHasFixedSize(true)
    }

    fun startGame(){
        FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("status").setValue("InProgress")
    }

    fun listenForGameStart(){
        gameStartListener = FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("status").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value.toString() == "InProgress"){
                        removeListeners()
                        binding.pulseCountDown.visibility = View.VISIBLE
                        object: CountDownTimer(6000, 1000){
                            override fun onTick(p0: Long) {
                                val figureToShow: String = (p0 / 1000).toString()
                                if(figureToShow == "0"){
                                    binding.pulseCountDown.text = "GO!"
                                }else{
                                    binding.pulseCountDown.text = figureToShow
                                }
                            }
                            override fun onFinish() {
                                navController!!.navigate(R.id.fragmentGameQuestion)
                                binding.pulseCountDown.visibility = View.GONE
                            }
                        }.start()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun onBackPressed() {
        if(host == username){
            removeListeners()
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).removeValue()
            Toast.makeText(binding.root.context, "You have ended the game.", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }else{
            removeListeners()
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").child(username).removeValue()
            Toast.makeText(binding.root.context, "Left Game!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    fun removeListeners(){
        FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").removeEventListener(hostListener)
        FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").removeEventListener(userListener)
        FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("status").removeEventListener(gameStartListener)
    }


}