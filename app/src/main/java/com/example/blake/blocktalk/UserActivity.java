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
//    @Bind(R.id.getEmail) EditText mUserEmail;
//    @Bind(R.id.getPassword) EditText mUserPassword;
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

        mLocationMessagesReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);
        final Context stuff = this;
        mLocationMessagesReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println("YO this is messages at first spot " + locationMessagesList.get(0).getMessages());
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



//        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, );
//        mMessagesView.setAdapter(adapter);


        ///PUT TIMER BACK ON!!!!!!!!!!!!!!!!
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

        Intent intent = getIntent();
        final String username = intent.getStringExtra("username");
        String newMessage = intent.getStringExtra("message");
        mGetUser.setText("Hey");

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
            locationManager.requestLocationUpdates(provider, 1000, 0, listener);
        }

        ///LOCAL MESSAGE SUBMIT                                                 Constants check is wrong, check that you radius and rounding is doing what its supposed to.
        mSubmitLocalMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == mSubmitLocalMessage) {
                    DatabaseReference locationMessagesRef = FirebaseDatabase
                            .getInstance()
                            .getReference(Constants.FIREBASE_CHILD_LOCATIONMESSAGES);

                    if (locationMessagesList.size() >= 1) {
                        for(int i = 0; i < locationMessagesList.size(); i++){
                            if (((locationMessagesList.get(i).getLatLng().latitude() + radius) > userLocation.latitude() && userLocation.latitude() > (locationMessagesList.get(i).getLatLng().latitude() - radius)) && ((locationMessagesList.get(i).getLatLng().longitude() + radius) > userLocation.longitude() && userLocation.longitude() > (locationMessagesList.get(i).getLatLng().longitude() - radius))) {
                                String newMessage = mUserMessage.getText().toString();
                                if(newMessage.length() > 0) {
                                    LocationMessages newLocationMessage = locationMessagesList.get(i);
                                    newLocationMessage.getMessages().add(newMessage);
                                    Map<String, Object> newCrap = new HashMap<String, Object>();
                                    newCrap.put("messages", newLocationMessage.getMessages());
                                    locationMessagesRef.child(keys.get(i)).updateChildren(newCrap);
                                    mUserMessage.setText("");
                                }
                            } else {
                                String newMessage = mUserMessage.getText().toString();
                                if(newMessage.length() > 0) {
                                    List<String> messages = new ArrayList<>();
                                    messages.add(newMessage);
                                    LocationMessages locationMessages = new LocationMessages(userLocation, messages);
                                    mUserMessage.setText("");
                                    locationMessagesRef.push().setValue(locationMessages);
                                    System.out.println("YO new location with message");
                                }
                            }
                        }
                    }

                    if (locationMessagesList.size() == 0) {
                        String newMessage = mUserMessage.getText().toString();
                        if(newMessage.length() > 0) {
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

    private final LocationListener listener = new LocationListener() {
        public void onLocationChanged(Location location) {
            userLong = location.getLongitude();
            userLat = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ///CREATING MARKER ON MAP OF USERS CURRENT LOCATION.
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