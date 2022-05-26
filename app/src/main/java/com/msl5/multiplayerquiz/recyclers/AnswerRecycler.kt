package com.msl5.multiplayerquiz.recyclers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.URL
import com.msl5.multiplayerquiz.dataclass.Answer
import com.msl5.multiplayerquiz.dataclass.Quiz
import com.msl5.multiplayerquiz.roomCode

class AnswerRecycler(private val answers: MutableList<Answer>, private val listener: OnItemClickListener, val context: Context): RecyclerView.Adapter<AnswerRecycler.CardViewHolder>()  {

    var selected_position = -1
    var correctAnswer = -1
    var isSubmitted = false
    var isReview = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_answer, parent, false)
        return CardViewHolder(itemView)
    }

    // Bind data to items when the position currently displayed in the Recycler View
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        // Set static card information
        holder.answerText.text = answers[position].answer
        holder.answerText.setTextColor(if(selected_position == position) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.default_font))
        holder.answerCard.setBackgroundResource(if(selected_position == position) R.drawable.button_primary_bg else R.drawable.button_secondary_bg)
        if(isSubmitted){
            holder.answerText.setTextColor(if(selected_position == position) ContextCompat.getColor(context, R.color.font_hint) else ContextCompat.getColor(context, R.color.white))
            holder.answerCard.setBackgroundResource(if(selected_position == position) R.drawable.button_disabled_bg_hollow else R.drawable.button_disabled_bg)
            holder.answerCard.isClickable = false
        }

        if(isReview){
            if(correctAnswer == position){
                holder.answerCard.setBackgroundResource(R.drawable.button_primary_bg)
                holder.answerText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            holder.answerCard.isClickable = false
            if(answers[position].answered.size != 0) {
                holder.whoAnsweredText.text = "^ " + answers[position].answered.joinToString(separator = ", ") + " answered!"
            }
        }
    }

    // Always returns items.size
    override fun getItemCount() = answers.size

    // Always need to build custom builder class - take data - set to viewholder
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var answerCard = itemView.findViewById<ConstraintLayout>(R.id.answerCard)
        var answerText = itemView.findViewById<TextView>(R.id.answerText)
        var whoAnsweredText = itemView.findViewById<TextView>(R.id.whoAnsweredtext)

        init{
            answerCard.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                notifyItemChanged(selected_position)
                selected_position = adapterPosition
                notifyItemChanged(selected_position)
                listener.onItemClick(answers[position].answer)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(answer: String)
    }


}