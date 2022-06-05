package com.msl5.multiplayerquiz.recyclers

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.msl5.multiplayerquiz.R
import com.msl5.multiplayerquiz.URL
import com.msl5.multiplayerquiz.dataclass.Answer
import com.msl5.multiplayerquiz.dataclass.Quiz
import com.msl5.multiplayerquiz.roomCode
import com.msl5.multiplayerquiz.util.GetImage

class AnswerRecycler(private val answers: MutableList<Answer>, private val listener: OnItemClickListener, val context: Context): RecyclerView.Adapter<AnswerRecycler.CardViewHolder>()  {

    var selected_position = -1
    var correctAnswer = -1
    var isSubmitted = false
    var isReview = false
    var getImage = GetImage()
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
            holder.answerText.setTextColor(if(selected_position == position) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.font_hint))
            holder.answerCard.setBackgroundResource(if(selected_position == position) R.drawable.button_disabled_bg else R.drawable.button_disabled_bg_hollow)
            holder.answerCard.isClickable = false
        }

        if(isReview){
            if(correctAnswer == position){
                holder.answerCard.setBackgroundResource(R.drawable.button_primary_bg)
                holder.answerText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            holder.answerCard.isClickable = false
            if(answers[position].answered.size != 0) {
                answers[position].answered.forEachIndexed { i, element ->
                    holder.answerTabOutline[i].visibility = View.VISIBLE
                    holder.answerTabOutline[i].strokeColor = Color.parseColor(element.color.toString())
                    holder.answerTabImages[i].setBackgroundResource(getImage.getImageFromAssets(element.image!!))
                }
            }
        }
    }

    // Always returns items.size
    override fun getItemCount() = answers.size

    // Always need to build custom builder class - take data - set to viewholder
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var answerCard = itemView.findViewById<ConstraintLayout>(R.id.answerCard)
        var answerText = itemView.findViewById<TextView>(R.id.answerText)

        var answeredCircle1 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle1)
        var answeredCircle2 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle2)
        var answeredCircle3 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle3)
        var answeredCircle4 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle4)
        var answeredCircle5 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle5)
        var answeredCircle6 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle6)
        var answeredCircle7 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle7)
        var answeredCircle8 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle8)
        var answeredCircle9 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle9)
        var answeredCircle10 = itemView.findViewById<MaterialCardView>(R.id.answeredCircle10)

        var answeredCircleImage1 = itemView.findViewById<ImageView>(R.id.answeredCircleImage1)
        var answeredCircleImage2 = itemView.findViewById<ImageView>(R.id.answeredCircleImage2)
        var answeredCircleImage3 = itemView.findViewById<ImageView>(R.id.answeredCircleImage3)
        var answeredCircleImage4 = itemView.findViewById<ImageView>(R.id.answeredCircleImage4)
        var answeredCircleImage5 = itemView.findViewById<ImageView>(R.id.answeredCircleImage5)
        var answeredCircleImage6 = itemView.findViewById<ImageView>(R.id.answeredCircleImage6)
        var answeredCircleImage7 = itemView.findViewById<ImageView>(R.id.answeredCircleImage7)
        var answeredCircleImage8 = itemView.findViewById<ImageView>(R.id.answeredCircleImage8)
        var answeredCircleImage9 = itemView.findViewById<ImageView>(R.id.answeredCircleImage9)
        var answeredCircleImage10 = itemView.findViewById<ImageView>(R.id.answeredCircleImage10)

        var answerTabOutline = listOf<MaterialCardView>(answeredCircle1,answeredCircle2,answeredCircle3,answeredCircle4,answeredCircle5,answeredCircle6,answeredCircle7,answeredCircle8,answeredCircle9,answeredCircle10)
        var answerTabImages = listOf<ImageView>(answeredCircleImage1,answeredCircleImage2,answeredCircleImage3,answeredCircleImage4,answeredCircleImage5,answeredCircleImage6,answeredCircleImage7,answeredCircleImage8,answeredCircleImage9,answeredCircleImage10)

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