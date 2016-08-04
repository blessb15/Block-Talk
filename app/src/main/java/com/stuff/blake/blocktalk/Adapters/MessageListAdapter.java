package com.stuff.blake.blocktalk.Adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stuff.blake.blocktalk.Models.Message;
import com.stuff.blake.blocktalk.R;

import java.sql.Array;
import java.util.ArrayList;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Blake on 7/27/2016.
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>{
    private ArrayList<Message> mMessages = new ArrayList<>();
    private Context mContext;
    private final OnItemClickListener listener;

    public MessageListAdapter(Context context, ArrayList<Message> messages, OnItemClickListener listener){
        mContext = context;
        mMessages = messages;
        this.listener = listener;
    }

    @Override
    public MessageListAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
        MessageViewHolder viewHolder = new MessageViewHolder(view);
        return viewHolder;
    }

    public interface OnItemClickListener {
        void onItemClick(Message message);
    }

    @Override
    public void onBindViewHolder(MessageListAdapter.MessageViewHolder holder, int position){
        holder.bindMessage(mMessages.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        @Nullable @Bind(R.id.messageNameTextView) TextView mMessageName;
        @Nullable @Bind(R.id.messageContentTextView) TextView mMessageContent;
        @Nullable @Bind(R.id.messageDateTextView) TextView mMessageDate;
        private Context mContext;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
        }

        public void bindMessage(final Message message, final OnItemClickListener listener) {
            String messageLikeSize = String.valueOf(message.getLikes().size());
            mMessageName.setText(message.getUser());
            mMessageContent.setText(message.getContent());
            mMessageDate.setText(message.getDate());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view){
                    listener.onItemClick(message);
                    System.out.println("YO A CLICK");
                }
            });
        }
    }
}