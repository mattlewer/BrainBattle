package com.msl5.multiplayerquiz.dataclass

data class Room(
    var code: String,
    var users: MutableList<User>,
    var status: String,
    var host: String,
)

data class User(
    var username: String? = "",
    var score: Int = 0
)

data class Quiz(
    var results : List<Question> = mutableListOf()
)

data class Question(
    var category: String? = "",
    var type : String? = "",
    var difficulty : String? = "",
    var question: String? = "",
    var correct_answer: String? = "",
    var incorrect_answers: MutableList<String> = mutableListOf()
)

data class Answer(
        var answer: String = "",
        var answered: MutableList<String> = mutableListOf()
)

data class UserAnswer(
        var username: String? = "",
        var answer: String? = ""
)