package com.example.app.fakecarrotmarket

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    var mDataset: List<ChatData>? = null
    var myNickName: String? = null

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var TextView_nickname: TextView
        var TextView_msg: TextView
        var rootView: View

        init {
            TextView_nickname = v.findViewById(R.id.TextView_NickName)
            TextView_msg = v.findViewById(R.id.TextView_Msg)
            rootView = v
        }
    }

    fun ChatAdapter(myDataset: List<ChatData>, chatActivity: ChatActivity, nick: String) {
        mDataset = myDataset
        this.myNickName = myNickName
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_chat, parent, false) as LinearLayout
        return MyViewHolder(v)
    }

    @Override
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat: ChatData = mDataset!![position]

        holder.TextView_nickname.setText(chat.getNickName())
        holder.TextView_msg.setText(chat.getMsg())

        if (chat.getNickName().equals(this.myNickName)) {
            holder.TextView_msg.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            holder.TextView_nickname.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        } else {
            holder.TextView_msg.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.TextView_nickname.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        }
    }

    override fun getItemCount(): Int {
        return if (mDataset == null) 0 else mDataset!!.size
    }

    fun getChat(position: Int): ChatData? {
        return if (mDataset != null) mDataset!![position] else null
    }

    fun addChat(chat: ChatData) {
        mDataset?.plus(chat)
        notifyItemInserted(mDataset!!.count() - 1)
    }
}
