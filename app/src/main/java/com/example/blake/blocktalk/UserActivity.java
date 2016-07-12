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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//import android.support.v4.app.FragmentActivity;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserActivity extends AppCompatActivity {
    @Bind(R.id.GetUser)
    TextView mGetUser;
    @Bind(R.id.MessagesView)
    ListView mMessagesView;
    @Bind(R.id.LocalMessage)
    EditText mUserMessage;
    @Bind(R.id.SubmitLocalMessage)
    Button mSubmitLocalMessage;
    @Bind(R.id.locationInfoText) TextView mLocationInfoText;
    private ArrayList<String> newMessages = new ArrayList<String>();
    private Map <LatLng, ArrayList<String>> mLocationMessages = new HashMap<LatLng, ArrayList<String>>();
//    private GoogleMap mMap;
    public ArrayList<LocationInfo> mLocationInfos = new ArrayList<LocationInfo>();
    private LocationManager locationManager;
    public static Double userLong;
    public static Double userLat;
    public static LatLng userLocation;
    private Double radius = 0.0003;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, newMessages);
        mMessagesView.setAdapter(adapter);

        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                getLocationInfo();
                System.out.println("location info refreshing...");
            }
        };
        timer.schedule(myTask, 1*60*1000, 1*60*2000);

        //Map fragment setup
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        final String username = intent.getStringExtra("username");
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
        ///LOCAL MESSAGE SUBMIT                                                 Constants check is wrong, check that you radius and rounding is doing what its supposed to.
        mSubmitLocalMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if there are hashes
                if (mLocationMessages.size() >= 1) {
                    for (Map.Entry<LatLng, ArrayList<String>> entry : mLocationMessages.entrySet()) {
                        //checks if there is already hash within radius location, if so it just adds a message to it.
                        if (((entry.getKey().latitude + radius) > userLocation.latitude && userLocation.latitude > (entry.getKey().latitude - radius)) && ((entry.getKey().longitude + radius) > userLocation.longitude && userLocation.longitude > (entry.getKey().longitude - radius))) {
                            String newMessage = username + ": " + mUserMessage.getText().toString();
                            entry.getValue().add(newMessage);
                            mUserMessage.setText("");
                            getLocationInfo();
                            //if users location is not within radius of a hash in hashmap, it creates new hash with message.
                        } else {
                            String newMessage = username + ": " + mUserMessage.getText().toString();
                            newMessages.add(newMessage);
                            mLocationMessages.put(userLocation, newMessages);
                            mUserMessage.setText("");
                            getLocationInfo();
                        }
                    }
                }

                //if there are no hashes
                if (mLocationMessages.size() == 0) {
                    String userMessage = username + ": " + mUserMessage.getText().toString();
                    newMessages.add(userMessage);
                    mLocationMessages.put(userLocation, newMessages);
                    mUserMessage.setText("");
                    getLocationInfo();
                }
            }
        });
    }

    private void getLocationInfo(){
        System.out.println("YO getWeather");
        final LocationInfoService weatherService = new LocationInfoService();
        weatherService.getWeather(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response){
                System.out.println("YO on response");
                mLocationInfos = weatherService.processResults(response);
                UserActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("YO on ui thread");
                        for(int i = 0; i < mLocationInfos.size(); i++){
                            mLocationInfoText.setText("Your current Location: " + mLocationInfos.get(i).getFullName());
                        }
                    }
                });
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
//                    mCurrentLatLongView.setText("These are the messages at your current " + userLocation);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
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


//    @Override
//    public void onMapReady(GoogleMap googleMap)
//    {
//        mMap = googleMap;
//    }
}