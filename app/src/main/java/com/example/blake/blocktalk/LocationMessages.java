package com.example.blake.blocktalk;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guest on 7/13/16.
 */
public class LocationMessages {
    private List<String> mMessages = new ArrayList<>();
    private LatLng latlng;

    public LocationMessages(LatLng latlng, List<String> messages) {
        this.mMessages = messages;
        this.latlng = latlng;
    }

    public LatLng getLatLng(){
        return this.latlng;
    }

    public List<String> getMessages(){
        return this.mMessages;
    }


}
