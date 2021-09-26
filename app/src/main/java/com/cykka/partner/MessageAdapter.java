package com.cykka.partner;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<Message> messageList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView senderId, receiverId, msg, timestamp;
        public LinearLayout llMessageParent, llMessage;

        public MyViewHolder(View view) {
            super(view);
            msg = (TextView) view.findViewById(R.id.tv_message);
            llMessageParent = view.findViewById(R.id.ll_message_parent);
            llMessage = view.findViewById(R.id.ll_message);
        }
    }


    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_msg, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        int type = message.getType();
        holder.msg.setText(message.getMsg());
        if (type==1){
            holder.llMessageParent.setGravity(Gravity.RIGHT);
            holder.llMessage.setGravity(Gravity.RIGHT);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(120, 0, 20, 20);
            holder.llMessage.setLayoutParams(layoutParams);
            holder.llMessage.setBackgroundResource(R.drawable.round_msg1);
        }else{
            holder.llMessageParent.setGravity(Gravity.LEFT);
            holder.llMessage.setGravity(Gravity.LEFT);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.setMargins(20, 0, 120, 20);
            holder.llMessage.setLayoutParams(layoutParams);
            holder.llMessage.setBackgroundResource(R.drawable.round_msg2);
        }

        Log.d("Message", message.getMsg()+" : "+message.getType());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
