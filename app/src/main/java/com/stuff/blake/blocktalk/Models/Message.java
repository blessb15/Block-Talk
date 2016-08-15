package com.stuff.blake.blocktalk.Models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Blake on 7/26/2016.
 */
public class Message {
    private ArrayList<String> likes = new ArrayList<String>();
    private String user;
    private String date;
    private String content;
    private Date now = new Date();
    private SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss aaa");

    public Message(String user, String content) {
        this.user = user;
        this.content = content;
        this.date = df.format(now);
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
}
