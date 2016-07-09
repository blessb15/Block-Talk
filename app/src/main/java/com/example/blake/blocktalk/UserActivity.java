package com.example.blake.blocktalk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends FragmentActivity implements OnMapReadyCallback {
    @Bind(R.id.GetUser)
    TextView mGetUser;
    @Bind(R.id.MessagesView)
    ListView mMessagesView;
    @Bind(R.id.LocalMessage)
    EditText mUserMessage;
    @Bind(R.id.SubmitLocalMessage)
    Button mSubmitLocalMessage;
    ///mMessages is what stores location and messages in that location.
    ArrayList<String> messages = new ArrayList<String>();
    Map <LatLng, ArrayList<String>> mMessages = new HashMap<LatLng, ArrayList<String>>();
    private GoogleMap mMap;
    LocationManager locationManager;
    Double userLong;
    Double userLat;
    LatLng userLocation;
    //Message visibility radius.
    Double radius = .0005;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String newMessage = intent.getStringExtra("message");
        mGetUser.setText("Hey, " + username + "!");
        //THIS IS WHERE YOU PUT THE MESSAGES TO BE SHOWN IN THE LIST.
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, getMessageLocation());
        mMessagesView.setAdapter(adapter);

        ////////FIND USER LOCATION WITH PERMISSIONS//////////
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                            , 10);
                }
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, listener);
        }
        //LOCAL MESSAGE SUBMIT
        mSubmitLocalMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMessage = mUserMessage.getText().toString();
                messages.add(userMessage);
                mMessages.put(userLocation, messages);
                System.out.println("this is the users location =" + userLocation);
                System.out.println("this is the users message =" + mUserMessage);
                System.out.println("this is the users messageArray =" + messages);
            }
        });
    }

//    //GRABBING LOCATION AND MESSAGE TO STICK IN HASH
//    private void sendLocalMessage(Double userLat, Double userLong, String message){
//        LatLng currentLatLng = new LatLng(userLat, userLong);
//        String[] messageArray = {};
//    }

    //GETTING MESSAGES THAT ARE IN USERS LOCATION
    private ArrayList<String> getMessageLocation() {
        ArrayList<String> messagesInUserLocation = new ArrayList<String>();
        for (Map.Entry<LatLng, ArrayList<String>> entry : mMessages.entrySet()) {
            ///THIS IS A CHECK TO SEE IF USER IS WITHIN A CERTAIN DISTANCE OF VIEWABLE MESSAGES
            if (entry.getKey().latitude + radius == userLocation.latitude && entry.getKey().longitude + radius == userLocation.longitude) {
                ///IF YOU'RE AT A LOCATION WITH MESSAGES IT GRABS ALL THE MESSAGES
                for (int i = 0; i < entry.getValue().size(); i++) {
                    String message = (entry.getValue().get(i));
                    messagesInUserLocation.add(message);
                }
            }
        }
        return messagesInUserLocation;
    }

    private final LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location location) {
            userLong = location.getLongitude();
            userLat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ///CREATING MARKER ON MAP OF USERS CURRENT LOCATION.
                    userLocation = new LatLng(userLat, userLong);
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Current Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {
            userLocation = new LatLng(userLat, userLong);
        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
//        Toast.makeText(UserActivity.this, "IN ONMAPREADY METHOD", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
    }
}