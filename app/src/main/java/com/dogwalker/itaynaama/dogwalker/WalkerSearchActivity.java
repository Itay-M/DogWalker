package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
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
import java.util.Date;
import java.util.List;

public class WalkerSearchActivity extends BaseActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {
    private static final int REQUEST_ADDRESS = 1;
    private static final int REQUEST_USER = 2;

    public static final java.text.DateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public static final java.text.DateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("HH:mm");

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
                availabilityQuery.whereEqualTo("dayOfWeek", datePickerFragment.getDate().get(Calendar.DAY_OF_WEEK));
                availabilityQuery.whereLessThanOrEqualTo("startTime", time);
                availabilityQuery.whereGreaterThanOrEqualTo("endTime", time);
                availabilityQuery.selectKeys(Arrays.asList("user"));
                availabilityQuery.whereMatchesKeyInQuery("user","objectId",usersQuery);
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
                                            showMessageBox("Request Sent","A request has been sent to the selected Walker");
                                        }else{
                                            showMessageBox("Send Request Failed","The message not send, try later");
                                            Log.e("Push",e.getMessage());
                                        }
                                    }
                                });
                            }else{
                                showMessageBox("Send Request Failed","The message not send, try later");
                                Log.e("SaveRequest", e.getMessage());
                            }
                        }
                    });
                }
                break;
        }
    }

    // create message box
    public void showMessageBox(String title, String msg){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(WalkerSearchActivity.this);
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(msg);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    @Override
    public void onDateSelected(Calendar date) {
        dateText.setText(DISPLAY_DATE_FORMAT.format(date.getTime()));
        this.date = date;
    }

    @Override
    public void onTimeSelected(Calendar time) {

        timeText.setText(DISPLAY_TIME_FORMAT.format(time.getTime()));
        this.time = time.get(Calendar.HOUR_OF_DAY)*60+time.get(Calendar.MINUTE);
    }

    /*
        this class create to pass ParseUser to intent (the object must to be serializable)
     */
    static public class ParseUserInfo implements Serializable{
        private String username;
        private String address;
        private String objectId;
        private double addressLocationLat;
        private double addressLocationLng;

        public ParseUserInfo(){}

        public ParseUserInfo(ParseUser user){
            username = user.getUsername();
            address = user.getString("address");
            objectId = user.getObjectId();
            ParseGeoPoint addressLocation = user.getParseGeoPoint("addressLocation");
            addressLocationLat = addressLocation.getLatitude();
            addressLocationLng = addressLocation.getLongitude();
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
    }
}
