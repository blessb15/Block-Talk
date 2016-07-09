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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import android.support.v4.app.FragmentActivity;
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
    ArrayList<String> newMessages = new ArrayList<String>();
    Map <LatLng, ArrayList<String>> mLocationMessages = new HashMap<LatLng, ArrayList<String>>();
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

        ///FIND USER LOCATION WITH PERMISSIONS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        ///sets refresh of user location.
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

        ///LOCAL MESSAGE SUBMIT                                                 Location check is wrong, check that you radius and rounding is doing what its supposed to.
        mSubmitLocalMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMessage = mUserMessage.getText().toString();
                newMessages.add(userMessage);

                //if there are no hashes
                if (mLocationMessages.size() == 0) {
                    mLocationMessages.put(userLocation, newMessages);
                }

                //if there are hashes
                if (mLocationMessages.size() >= 1) {
                    for (Map.Entry<LatLng, ArrayList<String>> entry : mLocationMessages.entrySet()) {
                        //checks if there is already hash within radius location, if so it just adds a message to it.
                        System.out.println("this is entry lat = " + entry.getKey().latitude);
                        System.out.println("this is entry long = " + entry.getKey().longitude);
                        if (entry.getKey().latitude + radius > Math.abs(userLocation.latitude) && Math.abs(userLocation.latitude) < entry.getKey().latitude - radius && entry.getKey().longitude + radius > Math.abs(userLocation.longitude) && Math.abs(userLocation.longitude) < entry.getKey().longitude - radius) {
                            entry.getValue().add(userMessage);
                            //if users location is not within radius of a hash in hashmap, it creates new hash with message.
                        } else {
                            ArrayList<String> newMessageList = new ArrayList<String>();
                            String newMessage = mUserMessage.getText().toString();
                            newMessageList.add(newMessage);
                            mLocationMessages.put(userLocation, newMessageList);
                            System.out.println("this is messages in your location = " + entry.getValue());
                        }
                    }
                }
//              ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, entry.getValue());
//              mMessagesView.setAdapter(adapter);

                System.out.println("this is the HashMap = " + mLocationMessages);
                System.out.println("this is the users location = " + userLocation);
            }
        });
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
        mMap = googleMap;
    }
}