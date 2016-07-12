package com.example.blake.blocktalk;

import com.example.blake.blocktalk.UserActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Blake on 7/11/2016.
 */
public class LocationInfoService {

    public static void getWeather(Callback callback) {

        OkHttpClient client = new OkHttpClient();

        RequestBody postData = new FormBody.Builder()
                .add("type", "json")
                .build();


        Request request = new Request.Builder()
                .url("http://api.wunderground.com/api/" + Constants.key + "/geolookup/q/" + UserActivity.userLat + "," + UserActivity.userLong + ".json")
                .post(postData)
                .build();

//        45.359552, -122.629517
//        " + UserActivity.userLocation + "
//        http://api.wunderground.com/api/7ef0e77df5b9d3c3/geolookup/q/37.776289,-122.395234.json
//        http://api.wunderground.com/api/" + Constants.key + "/conditions/q/" + UserActivity.userLocation + ".json
        System.out.println("YO" + Constants.key);
        System.out.println("http://api.wunderground.com/api/" + Constants.key + "/geolookup/q/" + UserActivity.userLat + "," + UserActivity.userLat + ".json");
        System.out.println("YO on getweather4");




        Call call = client.newCall(request);
        call.enqueue(callback);
    }


    public ArrayList<LocationInfo> processResults(Response response){
        ArrayList<LocationInfo> locationInfoObjects = new ArrayList<>();
        try {
            System.out.println("YO on getweather5");
            String jsonData = response.body().string();
            if (response.isSuccessful()) {
                System.out.println("YO on getweather6");
                System.out.println("YO response was successful");
                JSONObject apiJSON = new JSONObject(jsonData);
                JSONObject current_observation = apiJSON.getJSONObject("location");
//                for (int i = 0; i < current_observation.length(); i++) {
//                    JSONObject display_location = current_observation.getJSONObject("display_location");
                    String fullname = current_observation.getString("city");
                    LocationInfo locationInfo = new LocationInfo(fullname);
                    locationInfoObjects.add(locationInfo);
                }
        } catch (IOException e){
            e.printStackTrace();

        } catch (JSONException e){
            e.printStackTrace();
        }
        return locationInfoObjects;
    }
}
