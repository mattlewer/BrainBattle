package com.msl5.multiplayerquiz.util

import com.msl5.multiplayerquiz.R

class GetImage {

    fun getImageFromPos(userPos: Int): String{
        return when(userPos){
            0 -> "bear"
            1 -> "fox"
            2 -> "deer"
            3 -> "dog"
            4 -> "monkey"
            5 -> "monk"
            6 -> "samurai"
            7 -> "kabuki"
            8 -> "ninja"
            9 -> "cat"
            else -> "monkey"
        }
    }

    fun getImageFromAssets(image: String): Int{
        return when(image){
            "bear" -> R.drawable.bear
            "fox" -> R.drawable.fox
            "deer" -> R.drawable.deer
            "dog" -> R.drawable.dog
            "monkey" -> R.drawable.monkey
            "monk" -> R.drawable.monk
            "samurai" -> R.drawable.samurai
            "kabuki" -> R.drawable.kabuki
            "ninja" -> R.drawable.ninja
            "cat" -> R.drawable.cat
            "monkey" -> R.drawable.monkey
            else -> R.drawable.monkey
        }
    }
}