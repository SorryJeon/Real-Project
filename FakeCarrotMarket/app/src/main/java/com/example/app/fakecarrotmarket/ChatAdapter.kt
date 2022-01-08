package com.example.app.fakecarrotmarket

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.LinearLayout

public abstract class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    private var mDataset: List<ChatData>? = null
    private var myNickName: String? = null

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var TextView_NickName: TextView? = null
        var TextView_Msg: TextView? = null
        var rootView: View? = null

        public fun MyViewHolder(v: View) {
            TextView_NickName = v.findViewById(R.id.TextView_NickName)
            TextView_Msg = v.findViewById(R.id.TextView_Msg)
            rootView = v

            v.setClickable(true)
            v.setEnabled(true)

        }
    }

    public fun ChatAdapter(myDataset: List<ChatData>, context: Context, myNickname: String) {
        mDataset = myDataset
        this.myNickName = myNickname
    }

    @Override
    public fun onCreateViewHolder(parent: ViewGroup?, Viewtype: Int): ChatAdapter.MyViewHolder {
        val v: LinearLayout =
            LayoutInflater.from(parent?.getContext())
                .inflate(R.layout.activity_chat, parent, false) as LinearLayout

        val vh: MyViewHolder = MyViewHolder(v)
        return vh
    }

    @Override
    public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat: ChatData = mDataset!!.get(position)

        holder.TextView_NickName?.setText(chat.getNickName())
        holder.TextView_Msg?.setText(chat.getMsg())

        if (chat.getNickName().equals(this.myNickName)) {
            holder.TextView_Msg?.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END)
        } else {

        }
    }

    public override fun getItemCount(): Int {
        if (mDataset == null) {
            return 0
        } else {
            return mDataset!!.size
        }
    }

    public fun getChat(position: Int): ChatData? {
        return if (mDataset != null) mDataset!![position] else null
    }

    public fun addChat(chat: ChatData?) {
        mDataset?.plus(chat)
        notifyItemInserted(mDataset!!.size - 1)
    }
}
