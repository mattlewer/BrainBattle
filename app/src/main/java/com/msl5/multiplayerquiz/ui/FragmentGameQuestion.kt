package com.msl5.multiplayerquiz.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
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
import com.google.firebase.database.*
import com.msl5.multiplayerquiz.*
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.databinding.FragmentGameQuestionBinding
import com.msl5.multiplayerquiz.dataclass.Answer
import com.msl5.multiplayerquiz.dataclass.Quiz
import com.msl5.multiplayerquiz.dataclass.User
import com.msl5.multiplayerquiz.recyclers.AnswerRecycler

class FragmentGameQuestion : Fragment(), AnswerRecycler.OnItemClickListener {

    private var _binding: FragmentGameQuestionBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter : AnswerRecycler
    private var navController : NavController? = null
    private lateinit var hostListener: ValueEventListener
    private lateinit var answerListener: ValueEventListener
    private lateinit var quiz: Quiz
    private var questionNum = 0
    private var question = ""
    private lateinit var answers: MutableList<Answer>
    private lateinit var timer: CountDownTimer
    private lateinit var submitBtn: Button
    private var selectedAnswer = ""
    var host = "";

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameQuestionBinding.inflate(inflater, container, false)
        observeQuiz()
        listenToHost()
        listenForAllAnswers()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
        submitBtn = binding.submitAnswerBtn
        submitBtn.setOnClickListener { submitAnswer() }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

    fun observeQuiz(){
        FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("quiz").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    quiz = snapshot.getValue(Quiz::class.java)!!
                    setupQuiz()
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }

    fun listenForAllAnswers(){
        answerListener = FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).addValueEventListener(
                object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var length = snapshot.child("users").childrenCount.toInt()
                        if(snapshot.child("quiz").child("results").child(questionNum.toString()).child("user_answers").childrenCount.toInt() == length){
                            timer.onFinish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                }
        )
    }

    fun setupQuiz(){
        // get question from quiz
        question = quiz.results[questionNum].question!!

        // gather all answer texts, shuffle
        var answerTexts = quiz.results[questionNum].incorrect_answers
        answerTexts.add(quiz.results[questionNum].correct_answer!!)
        answerTexts.shuffle()

        // Create answer objects with answerers
        var tempAnswers = mutableListOf<Answer>()
        answerTexts.forEach {
            tempAnswers.add(Answer(it, mutableListOf()))
        }
        answers = tempAnswers
        binding.questionNumberText.text = (questionNum + 1).toString() + "/" + quiz.results.size.toString()
        binding.questionCategoryText.text = quiz.results[questionNum].category
        submitBtn.isEnabled = true
        submitBtn.setBackgroundResource(R.drawable.button_primary_bg)
        selectedAnswer = ""
        question = removeHTMLElementText(question)
        for(i in 0..answers.size-1){
            answers[i].answer = removeHTMLElementText(answers[i].answer)
        }
        binding.questionTitleText.text = question
        prepareRecycler()
        startSelectAnswerTimer()
    }

    fun removeHTMLElementText(text: String): String{
        var textFixed = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        }else {
            Html.fromHtml(text).toString()
        }
        return textFixed
    }

    fun prepareRecycler(){
        var recyclerView: RecyclerView = binding.answerRecycler
        adapter = AnswerRecycler(answers, this, binding.root.context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
        recyclerView.setHasFixedSize(true)
        recyclerView.scheduleLayoutAnimation()
    }

    fun startSelectAnswerTimer(){
        timer = object: CountDownTimer(15000, 1000){
            override fun onTick(p0: Long) {
                binding.timeLeftText.text = (p0 / 1000).toInt().toString()
            }
            override fun onFinish() {
                timer.cancel()
                FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("quiz").child("results").child(questionNum.toString()).child("user_answers").addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                snapshot.children.forEach{ e ->
                                    answers.find {
                                        it.answer == e.getValue(String::class.java)
                                    }!!.apply {
                                        if(e.key!! != username){
                                            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").child(e.key!!).addListenerForSingleValueEvent(
                                                    object: ValueEventListener{
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            this@apply.answered.add(snapshot.getValue(User::class.java)!!)
                                                            adapter.notifyDataSetChanged()
                                                        }
                                                        override fun onCancelled(error: DatabaseError) {
                                                            TODO("Not yet implemented")
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                adapter.isSubmitted = true
                adapter.notifyDataSetChanged()
                adapter.isReview = true
                adapter.correctAnswer = answers.indexOfFirst { it.answer ==  removeHTMLElementText(quiz.results[questionNum].correct_answer!!)}
                adapter.notifyDataSetChanged()
                startShowCorrectAnswerTimer()
            }
        }.start()
    }

    fun startShowCorrectAnswerTimer(){
        var timer = object: CountDownTimer(5000, 1000){
            override fun onTick(p0: Long) {
                // Do nothing
            }
            override fun onFinish() {
                nextQuestion()
            }
        }.start()
    }

    fun nextQuestion(){
        questionNum++
        if(questionNum == quiz.results.size){
            // GAME OVER
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").removeEventListener(hostListener)
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).removeEventListener(answerListener )
            navController!!.navigate(R.id.action_fragmentGameQuestion_to_fragmentGameOver)
        }else{
            setupQuiz()
        }
    }

    override fun onItemClick(answer: String) {
        selectedAnswer = answer
    }

    fun submitAnswer(){
        if(selectedAnswer != ""){
            if(selectedAnswer == quiz.results[questionNum].correct_answer){
                var points : Long = when(quiz.results[questionNum].difficulty){
                    "easy" -> 1
                    "medium" -> 2
                    "hard" -> 3
                    else -> 1
                }
                FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").child(username).child("score").setValue(ServerValue.increment(points));
            }
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("quiz").child("results").child(questionNum.toString()).child("user_answers").child(username).setValue(selectedAnswer)
            adapter.isSubmitted = true
            adapter.notifyDataSetChanged()
            submitBtn.isEnabled = false
            submitBtn.setBackgroundResource(R.drawable.button_disabled_bg)

        }else{
            Toast.makeText(binding.root.context, "Select an answer first, quick!", Toast.LENGTH_SHORT).show()
        }
    }

    fun listenToHost(){
        hostListener = FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                host = snapshot.value.toString()
                if(host == "null") {
                    Toast.makeText(binding.root.context, "Host left the game!", Toast.LENGTH_SHORT).show()
                    var intent = Intent(activity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").removeEventListener(hostListener)
                    FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).removeEventListener(answerListener )
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun onBackPressed() {
        if(host == username){
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").removeEventListener(hostListener)
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).removeValue()
            Toast.makeText(binding.root.context, "You have ended the game.", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }else{
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("host").removeEventListener(hostListener)
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).removeEventListener(answerListener )
            FirebaseDatabase.getInstance(URL).reference.child("rooms").child(roomCode).child("users").child(username).removeValue()
            Toast.makeText(binding.root.context, "Left Game!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }
}