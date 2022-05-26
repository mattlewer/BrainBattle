package com.msl5.multiplayerquiz

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.msl5.multiplayerquiz.dataclass.Quiz
import com.msl5.multiplayerquiz.dataclass.Room
import com.msl5.multiplayerquiz.dataclass.User
import com.msl5.multiplayerquiz.util.GetQuiz

var URL = "https://kotlin-multiplayer-quiz-default-rtdb.europe-west1.firebasedatabase.app/"
var username = "null"
var code = "null"
var codeFound = false
var roomCode : String = "null"

class MainActivity : AppCompatActivity() {

    lateinit var appLogoImage: ImageView
    lateinit var appTitleText: TextView
    lateinit var codeHeaderText: TextView
    lateinit var nameHeaderText: TextView
    lateinit var editNameText : EditText
    lateinit var editCodeText : EditText
    lateinit var createBtn : Button
    lateinit var joinBtn : Button
    lateinit var progressBarLoading : ProgressBar
    lateinit var progressBarLoadingText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLogoImage = findViewById(R.id.appLogoImage)
        appTitleText = findViewById(R.id.appTitleText)
        codeHeaderText = findViewById(R.id.codeHeaderText)
        nameHeaderText = findViewById(R.id.nameHeaderText)
        editNameText = findViewById(R.id.editNameText)
        editCodeText = findViewById(R.id.editCodeText)
        createBtn = findViewById(R.id.createBtn)
        joinBtn = findViewById(R.id.joinBtn)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        progressBarLoadingText = findViewById(R.id.progressBarLoadingText)

        createBtn.setOnClickListener {
            createRoom()
        }
        joinBtn.setOnClickListener {
            joinRoom()
        }
    }

    fun createRoom(){
        username = editNameText.text.toString()
        code = "null"
        codeFound = false
        roomCode = "null"
        code = editCodeText.text.toString()

        showProgress()

        if(code != "null" && code!="" && username!="null" && username!=""){
            FirebaseDatabase.getInstance(URL).reference.child("rooms").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var check = doesRoomExist(snapshot, code)
                    Handler().postDelayed({
                        if (check == true) {
                            hideProgress()
                            Toast.makeText(
                                this@MainActivity,
                                "Room already exists - ${code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            var empty = mutableListOf<User>()
                            var room = Room(code, empty, "Lobby", username)
                            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(code)
                                .setValue(
                                    room
                                )
                            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(code).child("users").child(username).setValue(User(username, 0))
                            var getQuiz = GetQuiz()
                            getQuiz.getData()
                            Handler().postDelayed({
                                roomCode = code
                                accepted()
                            }, 300)
                        }
                    }, 2000)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }else{
            hideProgress()
            Toast.makeText(this@MainActivity, "Please enter a valid code.", Toast.LENGTH_SHORT).show()
        }
    }

    fun joinRoom(){
        username = editNameText.text.toString()
        code = "null"
        codeFound = false
        roomCode = "null"
        code = editCodeText.text.toString()
        if(code!= "null" && code!="" && username!="null" && username!=""){
            showProgress()
            FirebaseDatabase.getInstance(URL).reference.child("rooms").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var data: Boolean = doesRoomExist(snapshot, code)
                    Handler().postDelayed({
                        if (data == true) {
                            codeFound = true
                            if(isRoomFree(snapshot,code)){
                                FirebaseDatabase.getInstance(URL).reference.child("rooms").child(code).child("users").child(username).setValue(User(username, 0))
                                roomCode = code
                                accepted()
                            }else{
                                hideProgress()
                            }
                        } else {
                            hideProgress()
                            Toast.makeText(
                                this@MainActivity,
                                "Please enter a valid code.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, 2000)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


        }else{
            Toast.makeText(this@MainActivity, "Please enter a code.", Toast.LENGTH_SHORT).show()
        }
    }

    fun accepted(){
        startActivity(Intent(this, GameActivity::class.java))
        hideProgress()
    }

    fun doesRoomExist(snapshot: DataSnapshot, code: String): Boolean{
        var data = snapshot.children
        data.forEach{
            val room = it.key
            if(room == code){
                roomCode = it.key.toString()
                return true
            }
        }
        return false
    }

    fun isRoomFree(snapshot: DataSnapshot, code: String) : Boolean{
        var data = snapshot.child(code).child("status").getValue(String::class.java)
        var users = snapshot.child(code).child("users")
        for (child in users.children) {
            var name = child.getValue(User::class.java)!!
            if(name.username == username){
                Toast.makeText(
                        this@MainActivity,
                        "Username already taken!",
                        Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        if(data == "Lobby"){

            return true
        }
        Toast.makeText(
                this@MainActivity,
                "Game already in progress!",
                Toast.LENGTH_SHORT
        ).show()
        return false
    }

    fun hideProgress(){
        appLogoImage.visibility = View.VISIBLE
        appTitleText.visibility = View.VISIBLE
        codeHeaderText.visibility = View.VISIBLE
        nameHeaderText.visibility = View.VISIBLE
        editNameText.visibility = View.VISIBLE
        createBtn.visibility = View.VISIBLE
        joinBtn.visibility = View.VISIBLE
        editCodeText.visibility = View.VISIBLE
        progressBarLoading.visibility = View.GONE
        progressBarLoadingText.visibility = View.GONE

    }

    fun showProgress(){
        appLogoImage.visibility = View.GONE
        appTitleText.visibility = View.GONE
        codeHeaderText.visibility = View.GONE
        nameHeaderText.visibility = View.GONE
        editNameText.visibility = View.GONE
        createBtn.visibility = View.GONE
        joinBtn.visibility = View.GONE
        editCodeText.visibility = View.GONE
        progressBarLoading.visibility = View.VISIBLE
        progressBarLoadingText.visibility = View.VISIBLE
    }


}