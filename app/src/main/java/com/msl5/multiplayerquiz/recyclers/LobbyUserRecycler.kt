package com.msl5.multiplayerquiz.recyclers

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.dataclass.User
import com.msl5.multiplayerquiz.util.GetImage

class LobbyUserRecycler(private val users: MutableList<User>, private val host: String, private val context : Context) : RecyclerView.Adapter<LobbyUserRecycler.CardViewHolder>()  {

    var getImage = GetImage()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_lobby_user, parent, false)
        return CardViewHolder(itemView)
    }

    // Bind data to items when the position currently displayed in the Recycler View
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        if(users[position].username == host){
            holder.hostText.visibility = View.VISIBLE
        }else{
            holder.hostTab.layoutParams.width = dpToPx(15)
            holder.hostTab.requestLayout()
            holder.hostText.visibility = View.GONE
        }
        // Set static card information
        holder.usernameText.text = users[position].username
        holder.hostTab.setBackgroundColor(Color.parseColor(users[position].color!!))
        holder.userImg.setImageResource(getImage.getImageFromAssets(users[position].image!!))

    }


    fun dpToPx(dp: Int): Int {
        val density: Float = context.getResources()
                .getDisplayMetrics().density
        return Math.round(dp.toFloat() * density)
    }

    // Always returns items.size
    override fun getItemCount() = users.size

    // Always need to build custom builder class - take data - set to viewholder
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var usernameText = itemView.findViewById<TextView>(R.id.usernameText)
        var hostTab = itemView.findViewById<View>(R.id.userColorTab)
        var hostText = itemView.findViewById<TextView>(R.id.hostText)
        var userImg = itemView.findViewById<ImageView>(R.id.userImg)

    }
}