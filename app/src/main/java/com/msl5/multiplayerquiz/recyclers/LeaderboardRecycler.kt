package com.msl5.multiplayerquiz.recyclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.dataclass.User

class LeaderboardRecycler(private val users: MutableList<User>) : RecyclerView.Adapter<LeaderboardRecycler.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_leaderboard_entry, parent, false)
        return CardViewHolder(itemView)
    }

    // Bind data to items when the position currently displayed in the Recycler View
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        // Set static card information
        holder.positionText.text = "#" + (position + 1).toString()
        holder.usernameScoreboardText.text = users[position].username
        holder.userScoreText.text = "Score: " + users[position].score.toString()
    }

    // Always returns items.size
    override fun getItemCount() = users.size

    // Always need to build custom builder class - take data - set to viewholder
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var positionText = itemView.findViewById<TextView>(R.id.positionText)
        var usernameScoreboardText = itemView.findViewById<TextView>(R.id.usernameScoreboardText)
        var userScoreText = itemView.findViewById<TextView>(R.id.userScoreText)
    }
}