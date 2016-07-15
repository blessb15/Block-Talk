package com.example.blake.blocktalk.Models;

/**
 * Created by Blake on 7/14/2016.
 */
public class LatLng {
    public Double latitude;
    public Double longitude;

    public LatLng(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng(){};

    public Double latitude(){
        return this.latitude;
    }

    public Double longitude(){
        return this.longitude;
    }

}
