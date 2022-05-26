package com.msl5.multiplayerquiz.util

import com.msl5.multiplayerquiz.dataclass.Quiz
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {
    @GET("api.php?amount=5&type=multiple")
    @Headers("Accept-type: application/json")
    fun getQuiz(): Call<Quiz>
}