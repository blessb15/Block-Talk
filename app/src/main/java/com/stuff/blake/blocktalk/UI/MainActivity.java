package com.stuff.blake.blocktalk.UI;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.stuff.blake.blocktalk.Adapters.MessageListAdapter;
import com.stuff.blake.blocktalk.Models.*;
import com.stuff.blake.blocktalk.*;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.stuff.blake.blocktalk.Models.LatLng;
import com.stuff.blake.blocktalk.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    @Bind(R.id.newLocation)
    EditText mNewLocation;
    @Bind(R.id.LocalMessage)
    EditText mUserMessage;
    @Bind(R.id.SubmitLocalMessage)
    Button mSubmitLocalMessage;
    @Bind(R.id.GetUser)
    TextView mBlockMessage;
    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private MessageListAdapter mAdapter;
    private LocationManager locationManager;
    public static Double userLong;
    public static Double userLat;
    public static LatLng userLocation;
    private Double radius = 0.0010;
    private DatabaseReference mLocationMessagesReference;
    final public static ArrayList<LocationMessages> locationMessagesList = new ArrayList<>();
    public static ArrayList<String> lmKeys = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String username;
    private String newMessage;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ///SETTING VIEW AND BINDING ID'S
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ///SETS FONT
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Poppins-Regular.ttf");
        mUserMessage.setTypeface(font);
        mBlockMessage.setTypeface(font);
        mNewLocation.setTypeface(font);

        ///MAP INSTANTIATION
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ///GRAB STUFF FROM PREVIOUS PAGE SUBMIT
        Intent intent = getIntent();
        final String newMessage = intent.getStringExtra("message");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    username = user.getDisplayName();
                    getSupportActionBar().setTitle("Hey, " + user.getDisplayName() + "!");
                } else {

                }
            }
        };

        ///REFRENCE TO DATABASE
        mLocationMessagesReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);
        mLocationMessagesReference.addValueEventListener(new ValueEventListener() {

            ///DATABASE STUFF, GRAB LOCATION MESSAGES, GRAB UNIQUE lmKeys TO COMPARE.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    lmKeys.add(locationSnapshot.getKey());
                    locationMessagesList.add(locationSnapshot.getValue(LocationMessages.class));
                    for(int i = 0; i < locationMessagesList.size(); i++){
                    }
                }
                getMessagesNearby();
                markMessages();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ///FIND USER LOCATION WITH PERMISSIONS
        refreshLocation();

        mSubmitLocalMessage.setOnClickListener(this);
    }

    public void refreshLocation() {
        ///SETS CRITERIA FOR WANTED CONNECTION
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        ///SETS REFRESH ON USER LOCATION TO EVERY SECOND.
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                            , 10);
                }
                return;
            }
            locationManager.requestLocationUpdates(provider, 5000, 0, listener);
        }
    }

    public void onClick(View view) {

        if (view == mSubmitLocalMessage) {
            DatabaseReference locationMessagesRef = FirebaseDatabase
                    .getInstance()
                    .getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);

            String newLocationText = mNewLocation.getText().toString();
            newMessage = mUserMessage.getText().toString();

            ///LOCAL MESSAGE SUBMIT
            if (newMessage.length() > 0 && newLocationText.length() == 0 && userLocation != null) {
                if (checkForNearbyLMS(locationMessagesList, userLocation) == true) {
                    ArrayList<Message> messages = new ArrayList<>();
                    Message message = new Message(username, newMessage);
                    messages.add(message);
                    LocationMessages locationMessages = new LocationMessages(userLocation, messages);
                    mUserMessage.setText("");
                    locationMessagesRef.push().setValue(locationMessages);
                }
                if (locationMessagesList.size() >= 1) {
                    for (int i = 0; i < locationMessagesList.size(); i++) {
                        if (locationMessagesList.get(i).getLatLng().latitude() <= (userLocation.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (userLocation.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (userLocation.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (userLocation.longitude() - radius)) {
                            LocationMessages newLocationMessage = locationMessagesList.get(i);
                            Message message = new Message(username, newMessage);
                            newLocationMessage.getMessages().add(message);
                            Map<String, Object> messageMap = new ObjectMapper().convertValue(message, Map.class);
                            Map<String, Object> update = new HashMap<String, Object>();
                            int messagesize = (locationMessagesList.get(i).getMessages().size() - 1);
                            update.put(String.valueOf(messagesize), messageMap);
                            locationMessagesRef.child(lmKeys.get(i)).child("messages").updateChildren(update);
                            mUserMessage.setText("");
                        }
                    }
                }
            }

            ///SEND MESSAGE TO NEW LOCATION
            if (newLocationText.length() > 0 && newMessage.length() > 0) {
                LatLng newLatLng = getNewUserLocation(newLocationText);
                if (checkForNearbyLMS(locationMessagesList, newLatLng) == true) {
                    ArrayList<Message> messages = new ArrayList<>();
                    Message message = new Message(username, newMessage);
                    messages.add(message);
                    LocationMessages locationMessages = new LocationMessages(newLatLng, messages);
                    mUserMessage.setText("");
                    locationMessagesRef.push().setValue(locationMessages);
                    Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                }
                for (int i = 0; i < locationMessagesList.size(); i++) {
                    if (locationMessagesList.get(i).getLatLng().latitude() <= (newLatLng.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (newLatLng.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (newLatLng.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (newLatLng.longitude() - radius)) {
                        LocationMessages newLocationMessage = locationMessagesList.get(i);
                        Message message = new Message(username, newMessage);
                        newLocationMessage.getMessages().add(message);
                        Map<String, Object> messageMap = new ObjectMapper().convertValue(message, Map.class);
                        Map<String, Object> update = new HashMap<String, Object>();
                        int messagesize = (locationMessagesList.get(i).getMessages().size() - 1);
                        update.put(String.valueOf(messagesize), messageMap);
                        locationMessagesRef.child(lmKeys.get(i)).child("messages").updateChildren(update);
                        mUserMessage.setText("");
                        Toast.makeText(MainActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }



    ///CHECKS FOR LOCATION MESSAGES AROUND WHERE YOUR SENDING TO KEEP FROM DUPLICATION
    public boolean checkForNearbyLMS(ArrayList<LocationMessages> lms, LatLng loc) {
        boolean checkforlms = false;
        int num = 0;
        for (int i = 0; i < lms.size(); i++) {
            if (lms.get(i).getLatLng().latitude() <= (loc.latitude() + radius) && lms.get(i).getLatLng().latitude() >= (loc.latitude() - radius) && lms.get(i).getLatLng().longitude() <= (loc.longitude() + radius) && lms.get(i).getLatLng().longitude() >= (loc.longitude() - radius)) {
                num += 1;
            }
        }
        if (num == 0) {
            checkforlms = true;
        }
        return checkforlms;
    }

    public void getMessagesNearby() {
        if (locationMessagesList.size() > 0) {
            for (int i = 0; i < locationMessagesList.size(); i++) {
                if (userLocation != null) {
                    if (locationMessagesList.get(i).getLatLng().latitude() <= (userLocation.latitude() + radius) && locationMessagesList.get(i).getLatLng().latitude() >= (userLocation.latitude() - radius) && locationMessagesList.get(i).getLatLng().longitude() <= (userLocation.longitude() + radius) && locationMessagesList.get(i).getLatLng().longitude() >= (userLocation.longitude() - radius)) {
                        mAdapter = new MessageListAdapter(getApplicationContext(), locationMessagesList.get(i).getMessages());
                        mRecyclerView.setAdapter(mAdapter);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                        mRecyclerView.setLayoutManager(layoutManager);
                        mRecyclerView.setHasFixedSize(true);
                    }
                } else {

                }
            }
        }
    }

    public void markMessages() {
        mMap.clear();
        if (locationMessagesList.size() > 0) {
            for (LocationMessages lm : locationMessagesList) {
                int lmSize = lm.getMessages().size();
                com.google.android.gms.maps.model.LatLng newlatlng = new com.google.android.gms.maps.model.LatLng(lm.getLatLng().latitude(), lm.getLatLng().longitude());
                String newLoc = getLocationAddress(newlatlng.latitude, newlatlng.longitude);
                mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .position(newlatlng)
                        .title("There is " + lmSize + " message(s) at " + newLoc));
            }
        }
    }

    ///GRABS LOCATION THAT USER PROVIDES AND CONVERTS TO LATLNG
    public LatLng getNewUserLocation(String newLocation) {
        Geocoder coder = new Geocoder(MainActivity.this);
        List<Address> address;

        List<LatLng> stuff = new ArrayList<LatLng>();
        LatLng newLatLng;

        try {
            address = coder.getFromLocationName(newLocation, 5);
            if (address == null) {
                Toast.makeText(MainActivity.this, "Location not found.", Toast.LENGTH_SHORT).show();
            }
            Address location = address.get(0);
            newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            stuff.add(newLatLng);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Location not found.", Toast.LENGTH_SHORT).show();
        }
        return stuff.get(0);
    }

    ///RETURNS STRING OF CURRENT LOCATION
    public String getLocationAddress(double userLat, double userLong) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        ArrayList addressArray = new ArrayList();
        String location = "Not found ";
        try {
            List<Address> address = geocoder.getFromLocation(userLat, userLong, 5);
            Address userLocationInfo = address.get(0);
            addressArray.add(userLocationInfo.getAddressLine(0).toString());
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Location not found.", Toast.LENGTH_SHORT).show();
        }
        if (addressArray.size() > 0) {
            location = addressArray.get(0).toString();
        }
        return location;
    }

    ///LISTENING FOR CHANGE IN LOCATION, SETTING USER LOCATION.
    private final LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location location) {

            userLong = location.getLongitude();
            userLat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ///REFRESHING LOCATION AND CURRENT MESSAGES VISIBLE.
                    userLocation = new LatLng(userLat, userLong);
                    mNewLocation.setHint(getLocationAddress(userLat, userLong));
                    getMessagesNearby();
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

    ///CREATES LOG OUT OPTION
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    ///SELECT LOGOUT
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///LOGOUT METHOD
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}