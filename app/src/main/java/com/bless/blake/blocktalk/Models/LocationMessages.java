package com.bless.blake.blocktalk.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guest on 7/13/16.
 */
public class LocationMessages {
    public ArrayList<Message> messages = new ArrayList<>();
    public LatLng latLng;

    public LocationMessages(LatLng latLng, ArrayList<Message> messages) {
        this.messages = messages;
        this.latLng = latLng;
    }

    public LocationMessages(){};

    public LatLng getLatLng(){
        return this.latLng;
    }

    public ArrayList<Message> getMessages(){
        return this.messages;
    }

}
