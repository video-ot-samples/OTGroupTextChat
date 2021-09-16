package com.example.myapplication.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.utilities.*
import com.tokbox.grouptextchat.ChatActivity
import com.tokbox.grouptextchat.R
import com.tokbox.grouptextchat.data.SignalMessage
import kotlinx.android.synthetic.main.remote_text_item.view.*

class MessageAdapter(var messageList: ArrayList<SignalMessage>, val context: ChatActivity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return messageList.get(position).type;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var viewHolder: RecyclerView.ViewHolder? = null

        when(viewType) {
            TYPE_REMOTE_TEXT -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.remote_text_item, parent, false)
                viewHolder = TextViewHolder(view)

            }
            TYPE_REMOTE_IMAGE -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.remote_image_item, parent, false)
                viewHolder = ImageViewHolder(view)
            }
            TYPE_SELF_TEXT -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.self_text_item, parent, false)
                viewHolder = TextViewHolder(view)
            }
            TYPE_SELF_IMAGE -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.self_image_item, parent, false)
                viewHolder = ImageViewHolder(view)
            }
            TYPE_JOIN -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.join_item, parent, false)
                viewHolder = JoinViewHolder(view)
            }
        }

        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.getItemViewType() == TYPE_REMOTE_IMAGE || holder.getItemViewType() == TYPE_SELF_IMAGE) {
            val vh1 = holder as ImageViewHolder
            configureImageViewHolder(vh1, position)
        } else if (holder.getItemViewType() == TYPE_SELF_TEXT || holder.getItemViewType() == TYPE_REMOTE_TEXT) {
            val vh1 = holder as TextViewHolder
            configureTextViewHolder(vh1, position)
            //configureTextViewHolder(vh1, position);
        } else if(holder.getItemViewType() == TYPE_JOIN) {
            val vh1 = holder as JoinViewHolder
            configureJoinTextViewHolder(vh1, position)
        }
    }

    private fun configureImageViewHolder(vh1: ImageViewHolder, position: Int) {
        val b: ByteArray =
            Base64.decode(messageList.get(position).data, Base64.DEFAULT)
        vh1.imageView.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.size))

    }

    private fun configureTextViewHolder(vh2: TextViewHolder, position: Int) {
        Log.d("MessageAdapter", "message is " + messageList.get(position).data)
        if (messageList.get(position).type === TYPE_REMOTE_TEXT) {
            vh2.name?.setText(messageList.get(position).name)
            vh2.name?.setTextColor(messageList.get(position).color)
        }
        vh2.messageText.setText(messageList.get(position).data)
    }

    private fun configureJoinTextViewHolder(vh2: JoinViewHolder, position: Int) {
        Log.d("MessageAdapter", "joined user is " + messageList.get(position).name)

        vh2.joinedUserName.setText(messageList.get(position).name)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class ImageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView

        init {
            imageView =
                itemView.findViewById<View>(R.id.imageView) as ImageView
        }
    }

    class TextViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var messageText: TextView
        lateinit var name: TextView

        init {
            messageText = itemView.findViewById(R.id.textView) as TextView

            if(itemView.remote_name != null) {
                name = itemView.findViewById(R.id.remote_name) as TextView
            }
        }
    }
    class JoinViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var joinedUserName: TextView

        init {
            joinedUserName = itemView.findViewById(R.id.join_text) as TextView
        }
    }
}