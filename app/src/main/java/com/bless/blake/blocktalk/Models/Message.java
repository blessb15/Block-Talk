package com.bless.blake.blocktalk.Models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Blake on 7/26/2016.
 */
public class Message {
    private String user;
    private String date;
    private boolean like;
    private boolean dislike;
    private String content;
    private Date now = new Date();
    private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm aaa");

    public Message(String user, String content){
        this.user = user;
        this.content = content;
        this.like = false;
        this.dislike = false;
        this.date = df.format(now);
    }

    public Message(){}

    public String getUser(){
        return this.user;
    }

    public String getDate(){
        return this.date;
    }

    public String getContent(){
        return this.content;
    }

    public boolean getLike(){
        return this.like;
    }

    public boolean getDislike(){
        return this.dislike;
    }

    public boolean likeIt(){
        return this.like = true;
    }

    public boolean dislikeIt(){
        return this.dislike = true;
    }
}
