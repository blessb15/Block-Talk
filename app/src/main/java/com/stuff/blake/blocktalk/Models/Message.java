package com.stuff.blake.blocktalk.Models;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stuff.blake.blocktalk.Constants;
import com.stuff.blake.blocktalk.UI.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Blake on 7/26/2016.
 */
public class Message {
    private String user;
    private String date;
    public static ArrayList<String> likes;
    private String content;
    private Date now = new Date();
    private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");

    public Message(String user, String content) {
        this.user = user;
        this.content = content;
        this.date = df.format(now);
        this.likes = new ArrayList<String>();
        likes.add(user);
    }

    public Message() {
    }

    public String getUser() {
        return this.user;
    }

    public String getDate() {
        return this.date;
    }

    public String getContent() {
        return this.content;
    }

    public ArrayList<String> getLikes() {
        return this.likes;
    }

    public void likeIt(String user, Message message) {
        DatabaseReference messagesRef = FirebaseDatabase
                .getInstance()
                .getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);

        ///USE THESE AS REFRENCE, TOP PULLS FROM DB, BOTTOM PULLS FROM CLASS.
        System.out.println("YO " + messagesRef.child(MainActivity.lmKeys.get(0)).child("messages").child("0").child("date").removeValue().toString());
//        System.out.println("YO " + MainActivity.locationMessagesList.get(0).getMessages().get(0).getLikes());
        message.getLikes().add(user);
        for(int i = 0; i < MainActivity.locationMessagesList.size(); i++){
            if (messagesRef.child(MainActivity.lmKeys.get(i)).child("messages").child(String.valueOf(i)).child("date").toString().contains(message.getDate())){
                System.out.println("YO AWWW YEEE");
            }
        }



    }
}
