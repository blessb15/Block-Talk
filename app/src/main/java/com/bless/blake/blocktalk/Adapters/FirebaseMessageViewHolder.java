//package com.bless.blake.blocktalk.Adapters;
//
//import android.content.Context;
//import android.content.Intent;
//import android.support.v7.view.menu.MenuView;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import android.widget.TextView;
//
//import com.bless.blake.blocktalk.Constants;
//import com.bless.blake.blocktalk.Models.Message;
//import com.bless.blake.blocktalk.R;
//import com.bless.blake.blocktalk.UI.MainActivity;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import org.parceler.Parcels;
//
//import java.util.ArrayList;
//
///**
// * Created by Blake on 7/27/2016.
// */
//public class FirebaseMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//    private static final int MAX_WIDTH = 200;
//    private static final int MAX_HEIGHT = 200;
//
//    View mView;
//    Context mContext;
//
//    public FirebaseMessageViewHolder(View itemView){
//        super(itemView);
//        mView = itemView;
//        mContext = itemView.getContext();
//        itemView.setOnClickListener(this);
//    }
//
//    public void bindMessage(Message message){
//        TextView mMessageUser = (TextView) mView.findViewById(R.id.messageNameTextView);
//        TextView mMessageContent = (TextView) mView.findViewById(R.id.messageContentTextView);
//        TextView mMessageDate = (TextView) mView.findViewById(R.id.messageDateTextView);
//
//        mMessageUser.setText(message.getUser());
//        mMessageContent.setText(message.getContent());
//        mMessageDate.setText(message.getDate());
//    }
//
//    @Override
//    public void onClick(View view){
//        final ArrayList<Message> messages = new ArrayList<>();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    for(int i = 0; i < MainActivity.keys.size(); i++ ){
//                        messages.add(snapshot.child(MainActivity.keys.get(i)).child("messages").getValue(Message.class));
//                    }
//                }
//                int itemPosition = getLayoutPosition();
//
//                Intent intent = new Intent(mContext, MessageDetailActivity.class);
//                intent.putExtra("position", itemPosition + "");
//                intent.putExtra("restaurants", Parcels.wrap(messages));
//
//                mContext.startActivity(intent);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//}
