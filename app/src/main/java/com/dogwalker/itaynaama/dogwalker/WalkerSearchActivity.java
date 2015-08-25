package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WalkerSearchActivity extends BaseActivity {
    private static final int REQUEST_ADDRESS = 1;
    private static final int REQUEST_USER = 2;

    private static final java.text.DateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    protected Button locationButton;
    protected Button searchWalker;
    protected TextView dateText;
    protected TextView timeText;
    protected Date date;
    protected Time time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_search);

        locationButton = (Button)findViewById(R.id.walker_search_location_btn);
        dateText = (TextView)findViewById(R.id.walker_search_date);
        timeText = (TextView)findViewById(R.id.walker_search_time);
        searchWalker = (Button)findViewById(R.id.walker_search_search_btn);

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
        datePickerFragment.setDateView(dateText);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        //handle time choosing
        final TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTimeView(timeText);
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        //handle search walker button TODO add location to the search
        searchWalker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery availabilityQuery = new ParseQuery("UserAvailability");
                availabilityQuery.whereEqualTo("dayOfWeek", datePickerFragment.getDate().get(Calendar.DAY_OF_WEEK));
                availabilityQuery.whereLessThanOrEqualTo("startTime", timePickerFragment.getTime());
                availabilityQuery.whereGreaterThanOrEqualTo("endTime", timePickerFragment.getTime());
                availabilityQuery.selectKeys(Arrays.asList("user"));
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
                    locationButton.setText(address.getAddressLine(0));
                }
                break;
            case REQUEST_USER:
                if(resultCode==RESULT_OK){
                    ParseUserInfo user = (ParseUserInfo) data.getSerializableExtra("user");
                    ParseUser puser = new ParseUser();
                    puser.setObjectId(user.getObjectId());

                    ParseQuery<ParseInstallation> userInstallationQuery = new ParseQuery<>(ParseInstallation.class);
                    userInstallationQuery.whereEqualTo("user", puser);

                    ParsePush push = new ParsePush();
                    push.setQuery(userInstallationQuery);

                    JSONObject pushData = new JSONObject();
                    try {
                        pushData.put("action","com.dogwalker.itaynaama.dogwalker.WALKING_REQUEST");
                        pushData.put("reqUser",ParseUser.getCurrentUser().getObjectId());
                    } catch (JSONException e) {}
                    push.setData(pushData);
                    push.sendInBackground(new SendCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                // create message box
                                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(WalkerSearchActivity.this);
                                dlgAlert.setTitle("Request Sent");
                                dlgAlert.setMessage("A request has been sent to the selected Walker");
                                dlgAlert.setCancelable(true);
                                dlgAlert.create().show();
                            }else{
                                //TODO
                                Log.e("Push",e.getMessage());
                            }
                        }
                    });
                }
                break;
        }
    }

    // window to choose date TODO
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private TextView dateText;
        private Calendar date;

        public DatePickerFragment(){
            date = Calendar.getInstance();
        }

        public void setDateView(TextView date){
            this.dateText = date;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = date.get(Calendar.YEAR);
            int month = date.get(Calendar.MONTH);
            int day = date.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            date.set(year, month, day);
            dateText.setText(DISPLAY_DATE_FORMAT.format(date.getTime()));
        }

        public Calendar getDate(){
            return date;
        }
    }

    // window to choose time TODO
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private static java.text.DateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("HH:mm");
        private TextView timeText;
        private Calendar time;

        public TimePickerFragment(){
            time = Calendar.getInstance();
            time.set(Calendar.SECOND,0);
        }

        public void setTimeView(TextView time){
            this.timeText = time;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            int hour = time.get(Calendar.HOUR_OF_DAY);
            int minute = time.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            time.set(Calendar.HOUR_OF_DAY,hourOfDay);
            time.set(Calendar.MINUTE,minute);
            timeText.setText(DISPLAY_TIME_FORMAT.format(time.getTime()));
        }

        /**
         * Return the time in minutes.
         */
        public int getTime(){
            return time.get(Calendar.HOUR_OF_DAY)*60+time.get(Calendar.MINUTE);
        }
    }

    static public class ParseUserInfo implements Serializable{
        private String username;
        private String address;
        private String objectId;

        public ParseUserInfo(){}

        public ParseUserInfo(ParseUser user){
            username = user.getUsername();
            address = user.getString("address");
            objectId = user.getObjectId();
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
