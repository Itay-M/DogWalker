package com.dogwalker.itaynaama.dogwalker;

import android.app.DatePickerDialog;
import android.app.Dialog;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;

public class WalkerSearchActivity extends AppCompatActivity {
    private static final int REQUEST_ADDRESS = 1;

    protected Button locationButton;
    protected Button searchWalker;
    protected TextView dateText;
    protected TextView timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_search);


        //handle location choosing button
        locationButton = (Button)findViewById(R.id.walker_search_location_btn);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressSelectionIntent = new Intent(WalkerSearchActivity.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);
            }
        });


        // handle date choosing
        dateText = (TextView)findViewById(R.id.walker_search_date);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setDateView(dateText);
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        //handle time choosing
        timeText = (TextView)findViewById(R.id.walker_search_time);
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setTimeView(timeText);
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        //handle search walker button
        searchWalker = (Button)findViewById(R.id.walker_search_search_btn);
        searchWalker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser users = new ParseUser();

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_ADDRESS:
                if(resultCode==RESULT_OK){
                    Address address = data.getParcelableExtra("address");
                    locationButton.setText(address.getAddressLine(0));
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_walker_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // window to choose date TODO
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private TextView dateText;

        public void setDateView(TextView date){
            this.dateText = date;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
           dateText.setText(day+"/"+(month+1)+"/"+year);
        }
    }

    // window to choose time TODO
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private TextView timeText;

        public void setTimeView(TextView time){
            this.timeText = time;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeText.setText(hourOfDay+":"+minute);
        }
    }
}
