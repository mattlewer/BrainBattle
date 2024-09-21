package com.msl5.multiplayerquiz

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.msl5.multiplayerquiz.dataclass.Room
import com.msl5.multiplayerquiz.dataclass.User
import com.msl5.multiplayerquiz.util.GetImage
import com.msl5.multiplayerquiz.util.GetQuiz

var URL = "FIREBASE_REALTIME_URL_HERE"
var username = "null"
var code = "null"
var codeFound = false
var roomCode : String = "null"
var color: String = "null"

class MainActivity : AppCompatActivity() {

    lateinit var appLogoImage: ImageView
    lateinit var appTitleText: TextView
    lateinit var codeHeaderText: TextView
    lateinit var nameHeaderText: TextView
    lateinit var editNameText : EditText
    lateinit var usernameTextCount: TextView
    lateinit var editCodeText : EditText
    lateinit var editCodeTextCount: TextView
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
        usernameTextCount = findViewById(R.id.usernameTextCount)
        editCodeText = findViewById(R.id.editCodeText)
        editCodeTextCount = findViewById(R.id.editRoomCodeTextCount)
        createBtn = findViewById(R.id.createBtn)
        joinBtn = findViewById(R.id.joinBtn)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        progressBarLoadingText = findViewById(R.id.progressBarLoadingText)

        editNameText.addTextChangedListener(editNameWatcher)
        editCodeText.addTextChangedListener(editCodeWatcher)
        createBtn.setOnClickListener {
            createRoom()
        }
        joinBtn.setOnClickListener {
            joinRoom()
        }
    }

    fun createRoom(){
        username = editNameText.text.toString()
        val regex = Regex("[^A-Za-z0-9]")
        username = regex.replace(username, "")
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
                            var getImage = GetImage()
                            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(code).child("users").child(username).setValue(User(username, 0, "#41b4a5", getImage.getImageFromPos(0)))
                            color = "#41b4a5"
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
                    println("--------------------------------")
                    println(error)
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
        val regex = Regex("[^A-Za-z0-9]")
        username = regex.replace(username, "")
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
                            if (isRoomFree(snapshot, code)) {
                                var numUsers = snapshot.child(code).child("users").childrenCount.toInt()
                                var colorsFirstRow = listOf("#ffabc3", "#fbd0da", "#9de0e9", "#90e3df", "#cceff1", "#ff9c96", "#feb4a7", "#ffdbcd", "#84addb", "#aec8e9")
                                var colorsSecondRow = listOf("#99dbf3", "#fed4d5", "#b0e5c7", "#feeae9", "#c2dcd9", "#c9def1","#8bc3a8", "#fdc7bd", "#a6dbc7", "#beecea")
                                var colorsThirdRow = listOf("#febe81", "#caeee0", "#fec49c", "#bde8f8", "#fecab4", "#ddecf3", "#f1bdff", "#eebcb3", "#edd9f4", "#f9dad5")
                                var allColors = listOf(colorsFirstRow, colorsSecondRow, colorsThirdRow)
                                var colour = allColors[(0..2).random()][numUsers - 1]
                                color = colour
                                var getImage = GetImage()
                                FirebaseDatabase.getInstance(URL).reference.child("rooms").child(code).child("users").child(username).setValue(User(username, 0, colour, getImage.getImageFromPos(numUsers)))
                                roomCode = code
                                accepted()
                            } else {
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

        // MAXIMUM 10 USERS PER GAME
        if(snapshot.child(code).child("users").childrenCount.toInt() >= 10){
            return false
        }
        // USERNAME CHECK
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
        // GAME HASNT STARTED
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
        editCodeTextCount.visibility = View.VISIBLE
        usernameTextCount.visibility = View.VISIBLE
        editNameText.visibility = View.VISIBLE
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
        editCodeTextCount.visibility = View.GONE
        usernameTextCount.visibility = View.GONE
        editNameText.visibility = View.GONE
        progressBarLoading.visibility = View.VISIBLE
        progressBarLoadingText.visibility = View.VISIBLE
    }

    private val editCodeWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            //This sets a textview to the current length
            editCodeTextCount.setText(s.length.toString()+ "/6")
        }
        override fun afterTextChanged(s: Editable) {}
    }
    private val editNameWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            usernameTextCount.setText(s.length.toString()+ "/12")
        }
        override fun afterTextChanged(s: Editable) {}
    }
}