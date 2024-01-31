package com.devtanmay.wifichatapp

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val mList: MutableList<messageModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(){
    private val VIEW_TYPE_MESSAGE_SENT = 1
    private val VIEW_TYPE_MESSAGE_RECEIVED = 2


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val myMessageTextView: TextView = itemView.findViewById(R.id.text_gchat_message_me)
        val myMessageCard:CardView=itemView.findViewById(R.id.card_gchat_message_me)

        val otherMessageTextView: TextView = itemView.findViewById(R.id.text_gchat_message_other)
        val otherDeviceNameTextView: TextView = itemView.findViewById(R.id.text_gchat_user_other)
        val otherMessageCard:CardView=itemView.findViewById(R.id.card_gchat_message_other)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_other, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
     return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]


        // sets the text to the textview from our itemHolder class

        if(TextUtils.equals(item.sender ,"self")){
            holder.myMessageCard.visibility=View.VISIBLE
            holder.otherMessageCard.visibility=View.GONE
            holder.otherDeviceNameTextView.visibility=View.GONE
            holder.myMessageTextView.text=item.message

        }
        else{

            holder.myMessageCard.visibility=View.GONE
            holder.otherMessageCard.visibility=View.VISIBLE
            holder.otherDeviceNameTextView.visibility=View.VISIBLE
            holder.otherMessageTextView.text=item.message
            holder.otherDeviceNameTextView.text=item.sender
        }

    }
}