package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The activity allow the user to search for a walker based on an address and date and time which
 * he choose. The results will be displayed as a user list and the user should choose one of them.
 * The selected user will be notified by a push notification.
 */
public class WalkerSearchActivity extends BaseActivity implements DatePickerFragment.DatePickerListener, TimePickerFragment.TimePickerListener {
    /**
     * The radius (in kilometers) to search for users around the selected pickup address
     */
    static private final int USERS_SEARCH_RADIUS = 20;
    /**
     * A request code to identify result from address selection
     */
    static private final int REQUEST_ADDRESS = 1;
    /**
     * A request code to identify result from user selection
     */
    static private final int REQUEST_USER = 2;

    // UI components
    protected TextView dateText,timeText,addressText;

    /**
     * The pickup date selected
     */
    protected Calendar date;
    /**
     * The pickup time (as minutes) selected
     */
    protected int time;
    /**
     * The pickup address selected
     */
    protected Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_search);

        // initialize selected date and time to current time
        this.date = Calendar.getInstance();
        this.time = date.get(Calendar.HOUR_OF_DAY)*60+date.get(Calendar.MINUTE);

        // get UI components
        Button locationButton = (Button)findViewById(R.id.walker_search_location_btn);
        Button searchWalker = (Button)findViewById(R.id.walker_search_search_btn);
        dateText = (TextView)findViewById(R.id.walker_search_date);
        timeText = (TextView)findViewById(R.id.walker_search_time);
        addressText = (TextView)findViewById(R.id.walker_search_address);

        // init date and time UI components
        dateText.setText(Utils.DISPLAY_DATE_FORMAT.format(date.getTime()));
        timeText.setText(Utils.DISPLAY_TIME_FORMAT.format(date.getTime()));

        //handle address choosing button
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressSelectionIntent = new Intent(WalkerSearchActivity.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);
            }
        });

        // handle date choosing
        final DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setDefaultDate(date);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        //handle time choosing
        final TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setDefaultTime(date.get(Calendar.HOUR_OF_DAY)*60+date.get(Calendar.MINUTE));
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerFragment.show(getSupportFragmentManager(), "timePicker");

            }
        });

        //handle search walker button
        searchWalker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();

                // check that the chosen time is in the future (ignore seconds and milliseconds)
                date.set(Calendar.HOUR_OF_DAY,time/60);
                date.set(Calendar.MINUTE,time%60);
                date.set(Calendar.SECOND,currentTime.get(Calendar.SECOND));
                date.set(Calendar.MILLISECOND,currentTime.get(Calendar.MILLISECOND));
                if (date.compareTo(currentTime) < 0) {
                    Utils.showMessageBox(WalkerSearchActivity.this, "Action Failed", "The date and time you choose are in the past");
                    return;
                }

                // make sure address has been selected
                if(address==null){
                    Utils.showMessageBox(WalkerSearchActivity.this,"Action Failed", "Plase choose pickup address");
                    return;
                }

                // build geo point from the address location
                ParseGeoPoint addressGeoPoint = new ParseGeoPoint();
                addressGeoPoint.setLatitude(address.getLatitude());
                addressGeoPoint.setLongitude(address.getLongitude());

                // search for relevant users based on their address
                ParseQuery<ParseUser> usersQuery = new ParseQuery<ParseUser>(ParseUser.class)
                        .whereWithinKilometers("addressLocation", addressGeoPoint, USERS_SEARCH_RADIUS);

                // search for relevant users based on their availability
                ParseQuery<ParseObject> availabilityQuery = new ParseQuery("UserAvailability")
                        .whereContainsAll("days", Collections.singleton(date.get(Calendar.DAY_OF_WEEK)))
                        .whereLessThanOrEqualTo("startTime", time)
                        .whereGreaterThanOrEqualTo("endTime", time)
                        .selectKeys(Arrays.asList("user"))
                        .whereMatchesKeyInQuery("user", "objectId", usersQuery)
                        .include("user")
                        .whereNotEqualTo("user", ParseUser.getCurrentUser());

                // retrieval all users from DB
                availabilityQuery.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> usersAvailability, ParseException e) {
                        if (e == null) {
                            // build list of all users' info
                            ArrayList<ParseUserInfo> users = new ArrayList<>();
                            for (final ParseObject userAvailability : usersAvailability) {
                                ParseUser user = userAvailability.getParseUser("user");
                                users.add(new ParseUserInfo(user));
                            }

                            // create an intent for the user selection activity with all the
                            // returned users
                            Intent usersSelectionIntent = new Intent(WalkerSearchActivity.this, UserSelectionActivity.class);
                            usersSelectionIntent.putExtra("users", users);
                            // add the pickup address location to the intent (for the distance
                            // calculation)
                            usersSelectionIntent.putExtra("addressLocationLng", address.getLongitude());
                            usersSelectionIntent.putExtra("addressLocationLat", address.getLatitude());

                            // start the user selection activity
                            startActivityForResult(usersSelectionIntent, REQUEST_USER);
                        } else {
                            Log.e("WalkerSearchActivity", "Failed getting users: " + e.getMessage());
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
                    this.address = address;
                    addressText.setText(Utils.addressToString(address));
                }
                break;
            case REQUEST_USER:
                if(resultCode==RESULT_OK){
                    // get the selected user info
                    ParseUserInfo user = (ParseUserInfo) data.getSerializableExtra("user");

                    // save request details in db
                    final ParseObject request = new ParseObject("Requests");
                    request.put("from", ParseUser.getCurrentUser());
                    request.put("to", ParseObject.createWithoutData(ParseUser.class,user.getObjectId()));
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
                                userInstallationQuery.whereEqualTo("user", request.getParseUser("to"));

                                // create push to the user installations
                                ParsePush push = new ParsePush();
                                push.setQuery(userInstallationQuery);
                                // set push data
                                JSONObject pushData = new JSONObject();
                                try {
                                    pushData.put("action","com.dogwalker.itaynaama.dogwalker.WALKING_REQUEST");
                                    pushData.put("reqId",request.getObjectId());
                                } catch (JSONException e1) {}
                                push.setData(pushData);

                                // send push
                                push.sendInBackground(new SendCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            //Utils.showMessageBox(WalkerSearchActivity.this,"Request Sent", "A request has been sent to the selected Walker");
                                            Toast.makeText(getApplicationContext(),"A request has been sent to the selected Walker",Toast.LENGTH_LONG).show();
                                            finish();
                                        }else{
                                            Utils.showMessageBox(WalkerSearchActivity.this,"Send Request Failed", getString(R.string.unknown_error_occur));
                                            Log.e("Push","Failed sending push: "+e.getMessage());
                                        }
                                    }
                                });
                            }else{
                                Utils.showMessageBox(WalkerSearchActivity.this, "Send Request Failed", getString(R.string.unknown_error_occur));
                                Log.e("SaveRequest", "Saving request failed: "+e.getMessage());
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
    public void onTimeSelected(int time) {
        timeText.setText(Utils.formatMinutesAsTime(time));
        this.time = time;
    }

    /**
     * A class used to pass user information between one activity to another.
     */
    static public class ParseUserInfo implements Serializable{
        private String name;
        private String address;
        private String objectId;
        private String phone;
        private boolean sharePhone;
        private Date bornDate;
        private byte[] profilePicture;
        private double addressLocationLat;
        private double addressLocationLng;

        public ParseUserInfo(){}

        /**
         * Create a new UserInfo initialized from the given ParseUser object.
         *
         * @param user the ParseUser to be initialized from
         */
        public ParseUserInfo(ParseUser user){
            name = user.getString("name");
            address = user.getString("address");
            phone = user.getString("phone");
            sharePhone = user.getBoolean("sharePhone");
            objectId = user.getObjectId();
            bornDate = user.getDate("bornDate");
            ParseGeoPoint addressLocation = user.getParseGeoPoint("addressLocation");
            addressLocationLat = addressLocation.getLatitude();
            addressLocationLng = addressLocation.getLongitude();

            try{
                ParseFile p = user.getParseFile("photo");
                profilePicture = (p==null?null:p.getData());
            }catch (ParseException e){
                Log.e("UserInfo", "Failed get user profile picture: " + e.getMessage());
            }
        }

        public ParseGeoPoint getAddressLocation() {
            return new ParseGeoPoint(addressLocationLat,addressLocationLng);
        }

        public String getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        public String getObjectId() {
            return objectId;
        }

        public byte[] getProfilePicture() {
            return profilePicture;
        }

        /**
         * Calculate the age of the user (approximately).
         * @return
         */
        public double getAge() {
            Calendar today = Calendar.getInstance();
            return (today.getTimeInMillis()-bornDate.getTime())/1000.0/60/60/24/365;
        }

        public String getPhone() {
            return phone;
        }

        public boolean isSharePhone() {
            return sharePhone;
        }
    }
}
