package com.msl5.multiplayerquiz.util

import com.google.firebase.database.FirebaseDatabase
import com.msl5.multiplayerquiz.URL
import com.msl5.multiplayerquiz.code
import com.msl5.multiplayerquiz.dataclass.Quiz
import com.msl5.multiplayerquiz.util.RetrofitInstance.Companion.BASE_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class GetQuiz {

    var errorAccept = 3
    fun getData(){
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)

        val retrofitData = retrofitBuilder.getQuiz()
        retrofitData.enqueue(object : Callback<Quiz> {
            override fun onResponse(call: Call<Quiz>, response: Response<Quiz>) {
                try {
                    FirebaseDatabase.getInstance(URL).reference.child("rooms").child(code).child("quiz").setValue(response.body()!!)
                } catch (e: NullPointerException) {
                    // NO QUIZ
                }
            }
            override fun onFailure(call: Call<Quiz>, t: Throwable) {
                if(errorAccept > 0){
                    errorAccept--
                    getData()
                }

            }
        })

    }
}