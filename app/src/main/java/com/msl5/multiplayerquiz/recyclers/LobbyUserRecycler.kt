package com.msl5.multiplayerquiz.recyclers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.dataclass.User

class LobbyUserRecycler(private val users: MutableList<User>, private val host: String) : RecyclerView.Adapter<LobbyUserRecycler.CardViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_lobby_user, parent, false)
        return CardViewHolder(itemView)
    }

    // Bind data to items when the position currently displayed in the Recycler View
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        if(users[position].username == host){
            holder.hostTab.visibility = View.VISIBLE
            holder.hostText.visibility = View.VISIBLE
        }else{
            holder.hostTab.visibility = View.GONE
            holder.hostText.visibility = View.GONE
        }
        // Set static card information
        holder.usernameText.text = users[position].username

    }

    // Always returns items.size
    override fun getItemCount() = users.size

    // Always need to build custom builder class - take data - set to viewholder
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var usernameText = itemView.findViewById<TextView>(R.id.usernameText)
        var hostTab = itemView.findViewById<View>(R.id.hostTab)
        var hostText = itemView.findViewById<TextView>(R.id.hostText)

    }
}