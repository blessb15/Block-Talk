package com.example.blake.blocktalk.UI;

import com.example.blake.blocktalk.Models.*;
import com.example.blake.blocktalk.*;
import com.example.blake.blocktalk.Services.*;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.example.blake.blocktalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public ArrayList<LocationInfo> mLocationInfos = new ArrayList<LocationInfo>();
    private LocationManager locationManager;
    public static Double userLong;
    public static Double userLat;
    public static LatLng userLocation;
    private Double radius = 0.0003;
    private DatabaseReference mLocationMessagesReference;
    final ArrayList<LocationMessages> locationMessagesList = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);

        ///REFRENCE TO MY DATABASE
        mLocationMessagesReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);
        final Context stuff = this;
        mLocationMessagesReference.addValueEventListener(new ValueEventListener() {

            ///DATABASE STUFF, GRAB LOCATION MESSAGES, GRAB UNIQUE KEYS TO COMPARE.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    keys.add(locationSnapshot.getKey());
                    locationMessagesList.add(locationSnapshot.getValue(LocationMessages.class));
                    for(int i = 0; i < locationMessagesList.size(); i++){
                        if (((locationMessagesList.get(i).getLatLng().latitude() + radius) > userLocation.latitude() && userLocation.latitude() > (locationMessagesList.get(i).getLatLng().latitude() - radius)) && ((locationMessagesList.get(i).getLatLng().longitude() + radius) > userLocation.longitude() && userLocation.longitude() > (locationMessagesList.get(i).getLatLng().longitude() - radius))) {
                            ArrayAdapter adapter = new ArrayAdapter(stuff, android.R.layout.simple_list_item_1, locationMessagesList.get(i).getMessages());
                            mMessagesView.setAdapter(adapter);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);


        ///TIMER TO REFRESH LOCATION INFO EVERY MINUTE
//        Timer timer = new Timer();
//        TimerTask myTask = new TimerTask() {
//            @Override
//            public void run() {
//                getLocationInfo();
//                System.out.println("location info refreshing...");
//            }
//        };
//
//        timer.schedule(myTask, 1*60*1000, 1*60*2000);

        ///GRAB STUFF FROM PREVIOUS PAGE SUBMIT
        Intent intent = getIntent();
        final String username = intent.getStringExtra("username");
        final String newMessage = intent.getStringExtra("message");
        mGetUser.setText("Hey, " + username);

        ///FIND USER LOCATION WITH PERMISSIONS
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
            Toast.makeText(UserActivity.this, "Cant get location, check connection", Toast.LENGTH_LONG).show();
            locationManager.requestLocationUpdates(provider, 1000, 0, listener);
        }

        ///LOCAL MESSAGE SUBMIT
        mSubmitLocalMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == mSubmitLocalMessage) {
                    DatabaseReference locationMessagesRef = FirebaseDatabase
                            .getInstance()
                            .getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);

                    String newMessage = mUserMessage.getText().toString();

                    if (newMessage.length() > 0){
                        if (locationMessagesList.size() >= 1) {
                            for (int i = 0; i < locationMessagesList.size(); i++) {
                                if (((locationMessagesList.get(i).getLatLng().latitude() + radius) > userLocation.latitude() && userLocation.latitude() > (locationMessagesList.get(i).getLatLng().latitude() - radius)) && ((locationMessagesList.get(i).getLatLng().longitude() + radius) > userLocation.longitude() && userLocation.longitude() > (locationMessagesList.get(i).getLatLng().longitude() - radius))) {
                                    LocationMessages newLocationMessage = locationMessagesList.get(i);
                                    newLocationMessage.getMessages().add(newMessage);
                                    Map<String, Object> newCrap = new HashMap<String, Object>();
                                    newCrap.put("messages", newLocationMessage.getMessages());
                                    locationMessagesRef.child(keys.get(i)).updateChildren(newCrap);
                                    mUserMessage.setText("");
                                } else {
                                    List<String> messages = new ArrayList<>();
                                    messages.add(newMessage);
                                    LocationMessages locationMessages = new LocationMessages(userLocation, messages);
                                    mUserMessage.setText("");
                                    locationMessagesRef.push().setValue(locationMessages);
                                    System.out.println("YO new location with message");
                                }
                            }
                        }

                        if (locationMessagesList.size() == 0) {
                            List<String> messages = new ArrayList<>();
                            messages.add(newMessage);
                            LocationMessages locationMessages = new LocationMessages(userLocation, messages);
                            mUserMessage.setText("");
                            locationMessagesRef.push().setValue(locationMessages);
                            System.out.println("YO first message");
                            System.out.println("YO list of lm's " + locationMessagesList);
                            System.out.println("YO list of lm keys " + keys);
                        }
                    }
                }
            }
        });
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_logout) {
//            logout();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void logout(){
//        FirebaseAuth.getInstance().signOut();
//        Intent intent = new Intent(UserActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }

    ///GRABING USERS LOCATION INFO FROM WEATHER UNDERGOROUND API
    private void getLocationInfo(){
        System.out.println("YO getLocationInfo");
        final LocationInfoService locationService = new LocationInfoService();
        locationService.getLocationInfo(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response){
                System.out.println("YO on response");
                mLocationInfos = locationService.processResults(response);
                UserActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("YO on ui thread");
                        for(int i = 0; i < mLocationInfos.size(); i++){
                            mLocationInfoText.setText("Your current LocationMessages: " + mLocationInfos.get(i).getFullName());
                        }
                    }
                });
            }
        });
    }

    ///LISTENER FROM DEVICES LOCATION. SETTING LAT AND LNG.
    private final LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location location) {

            userLong = location.getLongitude();
            userLat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ///REFRESHING LOCATION AND CURRENT MESSAGES VISIBLE.
                    for(int i = 0; i < locationMessagesList.size(); i++){
                        if (((locationMessagesList.get(i).getLatLng().latitude() + radius) > userLocation.latitude() && userLocation.latitude() > (locationMessagesList.get(i).getLatLng().latitude() - radius)) && ((locationMessagesList.get(i).getLatLng().longitude() + radius) > userLocation.longitude() && userLocation.longitude() > (locationMessagesList.get(i).getLatLng().longitude() - radius))) {
                            ArrayAdapter adapter = new ArrayAdapter(UserActivity.this, android.R.layout.simple_list_item_1, locationMessagesList.get(i).getMessages());
                            mMessagesView.setAdapter(adapter);
                        }
                    }
                    userLocation = new LatLng(userLat, userLong);
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
}