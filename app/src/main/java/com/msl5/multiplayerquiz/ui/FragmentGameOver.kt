package com.msl5.multiplayerquiz.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.URL
import com.msl5.multiplayerquiz.databinding.FragmentGameOverBinding
import com.msl5.multiplayerquiz.databinding.LayoutLeaderboardFirstBinding
import com.msl5.multiplayerquiz.databinding.LayoutLeaderboardSecondThirdBinding
import com.msl5.multiplayerquiz.dataclass.User
import com.msl5.multiplayerquiz.recyclers.LeaderboardRecycler
import com.msl5.multiplayerquiz.recyclers.LobbyUserRecycler
import com.msl5.multiplayerquiz.roomCode
import com.msl5.multiplayerquiz.username

class FragmentGameOver : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!
    private var navController : NavController? = null

    private lateinit var returnToHomeBtn : Button
    private lateinit var adapter : LeaderboardRecycler
    private var users = mutableListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentGameOverBinding.inflate(inflater, container, false)

        returnToHomeBtn = binding.returnToHomeBtn
        returnToHomeBtn.setOnClickListener { returnToHome() }
        listenForUsers()
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

    fun listenForUsers(){
        FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var tempList = mutableListOf<User>()
                    for (x in snapshot.children) {
                        var tempUser = x.getValue(User::class.java)!!
                        tempUser.score = tempUser.score * 100
                        tempList.add(tempUser)
                    }
                    users = tempList
                    users.sortByDescending { it.score }
                    prepareRecycler()
                    prepareTopThree()
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    fun prepareRecycler(){
        var recyclerView: RecyclerView = binding.gameOverRecycler
        adapter = LeaderboardRecycler(users)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        recyclerView.setHasFixedSize(true)
    }

    fun prepareTopThree(){
        println("GOT TO PREPARE")
        var cards = listOf(binding.firstPlaceCard, binding.secondPlaceCard, binding.thirdPlaceCard)
        cards.forEachIndexed { index, cardView ->
            if(users.getOrNull(index) != null){
                var topThreePointsPosition = cardView.root.findViewById<TextView>(R.id.topThreePointsPosition)
                var topThreeCardUserCardView = cardView.root.findViewById<MaterialCardView>(R.id.topThreeCardUserCardView)
                var topThreeCardUserInitials = cardView.root.findViewById<TextView>(R.id.topThreeCardUserInitials)
                var topThreePointsCard = cardView.root.findViewById<TextView>(R.id.topThreePointsText)

                topThreePointsPosition.text = (index + 1).toString()
                var cardRingColour = 0
                when(index){
                    0 -> cardRingColour = ContextCompat.getColor(binding.root.context, R.color.gold)
                    1 -> cardRingColour = ContextCompat.getColor(binding.root.context, R.color.silver)
                    2 -> cardRingColour = ContextCompat.getColor(binding.root.context, R.color.bronze)
                    else -> cardRingColour = ContextCompat.getColor(binding.root.context, R.color.gold)
                }
                topThreeCardUserCardView.strokeColor = cardRingColour
                topThreeCardUserInitials.text = users[index].username
                topThreePointsCard.text = users[index].score.toString()
                topThreePointsCard.setTextColor(cardRingColour)
                topThreePointsPosition.setTextColor(cardRingColour)

            }else{
                cardView.root.visibility = View.GONE
            }
        }
    }

    fun returnToHome(){
        if(host == username){
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).removeValue()
            Toast.makeText(binding.root.context, "You have ended the game.", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }else{
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").child(
                username
            ).removeValue()
            Toast.makeText(binding.root.context, "Thank you for playing!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

}