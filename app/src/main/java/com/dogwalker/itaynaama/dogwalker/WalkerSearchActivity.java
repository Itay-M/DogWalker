package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class WalkerSearchActivity extends BaseActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {
    private static final int REQUEST_ADDRESS = 1;
    private static final int REQUEST_USER = 2;

    protected Button locationButton;
    protected Button searchWalker;
    protected TextView dateText;
    protected TextView timeText;
    protected TextView addressText;
    protected Calendar date;
    protected int time;
    protected Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_search);

        locationButton = (Button)findViewById(R.id.walker_search_location_btn);
        dateText = (TextView)findViewById(R.id.walker_search_date);
        timeText = (TextView)findViewById(R.id.walker_search_time);
        searchWalker = (Button)findViewById(R.id.walker_search_search_btn);
        addressText = (TextView)findViewById(R.id.walker_search_address);

        dateText.setText(Utils.DISPLAY_DATE_FORMAT.format(new Date()));
        timeText.setText(Utils.DISPLAY_TIME_FORMAT.format(new Date()));

        //handle location choosing button
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressSelectionIntent = new Intent(WalkerSearchActivity.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);
            }
        });


        // handle date choosing
        final DatePickerFragment datePickerFragment = new DatePickerFragment();
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        //handle time choosing
        final TimePickerFragment timePickerFragment = new TimePickerFragment();
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PushReceiver.createPush(WalkerSearchActivity.this, ParseUser.getCurrentUser(),"01/01/01",
                //      "10:30","Jerusalem");
                timePickerFragment.show(getSupportFragmentManager(), "timePicker");

            }
        });

        //handle search walker button
        searchWalker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseGeoPoint addressGeoPoint = new ParseGeoPoint();
                addressGeoPoint.setLatitude(address.getLatitude());
                addressGeoPoint.setLongitude(address.getLongitude());

                ParseQuery<ParseUser> usersQuery = new ParseQuery<ParseUser>(ParseUser.class);
                usersQuery.whereWithinKilometers("addressLocation", addressGeoPoint, 20);

                // found walkers that can according to criterion
                ParseQuery<ParseObject> availabilityQuery = new ParseQuery("UserAvailability");
                //availabilityQuery.whereEqualTo("dayOfWeek", datePickerFragment.getDate().get(Calendar.DAY_OF_WEEK));
                availabilityQuery.whereContainsAll("days", Collections.singleton(datePickerFragment.getDate().get(Calendar.DAY_OF_WEEK)));
                availabilityQuery.whereLessThanOrEqualTo("startTime", time);
                availabilityQuery.whereGreaterThanOrEqualTo("endTime", time);
                availabilityQuery.selectKeys(Arrays.asList("user"));
                availabilityQuery.whereMatchesKeyInQuery("user", "objectId", usersQuery);
                availabilityQuery.include("user");

                // retrieval all users from DB
                availabilityQuery.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> usersAvailability, ParseException e) {
                        if (e == null) {
                            ArrayList<ParseUserInfo> users = new ArrayList<>();
                            for (final ParseObject userAvailability : usersAvailability) {
                                ParseUser user = userAvailability.getParseUser("user");
                                users.add(new ParseUserInfo(user));
                            }
                            Intent usersSelectionIntent = new Intent(WalkerSearchActivity.this, UserSelctionActivity.class);
                            usersSelectionIntent.putExtra("users", users);

                            // send details of pickup address
                            usersSelectionIntent.putExtra("addressLocationLng",address.getLongitude());
                            usersSelectionIntent.putExtra("addressLocationLat", address.getLatitude());

                            startActivityForResult(usersSelectionIntent, REQUEST_USER);
                        } else {
                            Log.d("users", "Error: " + e.getMessage());
                        }
                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_ADDRESS:
                // check that user choose option
                if(resultCode==RESULT_OK){
                    Address address = data.getParcelableExtra("address");
                    addressText.setText(Utils.addressToString(address));
                    this.address = address;
                }
                break;
            case REQUEST_USER:
                if(resultCode==RESULT_OK){
                    // push the objectId of selected walker
                    ParseUserInfo user = (ParseUserInfo) data.getSerializableExtra("user");
                    final ParseUser puser = new ParseUser();
                    puser.setObjectId(user.getObjectId());

                    // save request details in db
                    final ParseObject request = new ParseObject("Requests");
                    request.put("from", ParseUser.getCurrentUser());
                    request.put("to", puser);
                    request.put("datePickup", date.getTime());
                    request.put("timePickup", time);
                    request.put("address", Utils.addressToJSONArray(address));
                    request.put("addressLocation",new ParseGeoPoint(address.getLatitude(),address.getLongitude()));
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                // take all installation app of selected walker
                                ParseQuery<ParseInstallation> userInstallationQuery = new ParseQuery<>(ParseInstallation.class);
                                userInstallationQuery.whereEqualTo("user", puser);

                                // create push details
                                ParsePush push = new ParsePush();
                                push.setQuery(userInstallationQuery);

                                JSONObject pushData = new JSONObject();
                                try {
                                    pushData.put("action","com.dogwalker.itaynaama.dogwalker.WALKING_REQUEST");
                                    // save request id
                                    pushData.put("reqId",request.getObjectId());
                                } catch (JSONException e1) {}
                                push.setData(pushData);
                                // send push
                                push.sendInBackground(new SendCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            Utils.showMessageBox(WalkerSearchActivity.this,"Request Sent", "A request has been sent to the selected Walker");
                                        }else{
                                            Utils.showMessageBox(WalkerSearchActivity.this,"Send Request Failed", "The message not send, try later");
                                            Log.e("Push",e.getMessage());
                                        }
                                    }
                                });
                            }else{
                                Utils.showMessageBox(WalkerSearchActivity.this,"Send Request Failed", "The message not send, try later");
                                Log.e("SaveRequest", e.getMessage());
                            }
                        }
                    });
                }
                break;
        }
    }


    @Override
    public void onDateSelected(Calendar date) {
        dateText.setText(Utils.DISPLAY_DATE_FORMAT.format(date.getTime()));
        this.date = date;
    }

    @Override
    public void onTimeSelected(Calendar time) {

        timeText.setText(Utils.DISPLAY_TIME_FORMAT.format(time.getTime()));
        this.time = time.get(Calendar.HOUR_OF_DAY)*60+time.get(Calendar.MINUTE);
    }

    /*
        this class create to pass ParseUser to intent (the object must to be serializable)
     */
    static public class ParseUserInfo implements Serializable{
        private String username;
        private String address;
        private String objectId;
        private Date bornDate;
        private byte[] profilePicture;
        private double addressLocationLat;
        private double addressLocationLng;

        public ParseUserInfo(){}

        public ParseUserInfo(ParseUser user){
            username = user.getUsername();
            address = user.getString("address");
            objectId = user.getObjectId();
            bornDate = user.getDate("bornDate");
            ParseGeoPoint addressLocation = user.getParseGeoPoint("addressLocation");
            addressLocationLat = addressLocation.getLatitude();
            addressLocationLng = addressLocation.getLongitude();

            ParseFile p = user.getParseFile("Photo");
            try{
                if(p != null) {
                    profilePicture = p.getData();
                }else{
                    profilePicture = null;
                }
            }catch (ParseException e){
                Log.d("My Loggggg", e.getMessage().toString());
            }
        }

        public ParseGeoPoint getAddressLocation() {
            return new ParseGeoPoint(addressLocationLat,addressLocationLng);
        }

        public String getAddress() {
            return address;
        }

        public String getUsername() {
            return username;
        }

        public String getObjectId() {
            return objectId;
        }

        public byte[] getProfilePicture() {
            return profilePicture;
        }

        public double getAge() {
            double age=0;
            Calendar today = Calendar.getInstance();
            long todayInMillis = today.getTimeInMillis();
            long bornInMillis = bornDate.getTime();

            age = (todayInMillis-bornInMillis)/1000.0/60/60/24/365;

            return age;
        }
    }
}
