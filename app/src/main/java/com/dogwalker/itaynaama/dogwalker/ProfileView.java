package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

public class ProfileView extends BaseActivity implements View.OnClickListener
{
    protected ImageView viewPic;
    protected TextView name, userName, userCity, userPhone;
    protected Button editButton;
    protected UserAvailabilityAdapter availabilityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        viewPic = (ImageView)findViewById(R.id.view_user_pic);
        editButton = (Button) findViewById(R.id.edit_profile_button);
        name = (TextView) findViewById(R.id.profile_name);
        userName = (TextView) findViewById(R.id.profile_user_name);
        userCity = (TextView) findViewById(R.id.profile_user_city);
        userPhone = (TextView)findViewById(R.id.profile_user_phone);

        editButton.setOnClickListener(this);

        fetchDetails();
    }

    /**
     * present the user's details.
     */
    private void fetchDetails()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();

        name.setText("Name: " + currentUser.get("Name").toString());
        userName.setText("UserName: " + currentUser.getUsername().toString());
        userCity.setText("Address: " + Utils.addressToString(currentUser.getJSONArray("address"), ",\n"));
        userPhone.setText("Phone: " + currentUser.get("Phone").toString());

        // user availability
        final GridLayout availbilityItems = (GridLayout)findViewById(R.id.view_availability_items);

//
//        availabilityAdapter = new UserAvailabilityAdapter(this);
//        final LinearLayout availabilityItems = (LinearLayout)findViewById(R.id.view_profile_availability_items);
//
//        // connect between adapter to component
//        availabilityAdapter.registerDataSetObserver(new DataSetObserver() {
//            @Override
//            public void onChanged() {
//                availabilityItems.removeAllViews();
//                for (int i = 0; i < availabilityAdapter.getCount(); i++) {
//                    View row = availabilityAdapter.getView(i, null, availabilityItems);
//                    availabilityItems.addView(row);
//                }
//            }
//
//            @Override
//            public void onInvalidated() {
//                availabilityItems.removeAllViews();
//            }
//        });

        // take all data of availability of current user
        final ParseQuery<ParseObject> userAvailabilityQuery = new ParseQuery<>("UserAvailability");
        userAvailabilityQuery.whereEqualTo("user", currentUser);
        userAvailabilityQuery.orderByAscending("startTime");

        userAvailabilityQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> userAvailabilities, ParseException e) {
                if (e == null) {
                   TextView[] hours = new TextView[7];
                   for (ParseObject userAvailability : userAvailabilities) {
                       JSONArray days = userAvailability.getJSONArray("days");
                       for (int i = 0; i < days.length(); i++) {
                           try {
                               int d = days.getInt(i);
                               if(hours[d-1]==null){
                                   hours[d-1] = new TextView(ProfileView.this);
                                   hours[d-1].setText(Utils.formatMinutesAsTime(userAvailability.getInt("startTime"))+"-" +Utils.formatMinutesAsTime(userAvailability.getInt("endTime")));
                               }else{
                                   hours[d-1].setText(hours[d-1].getText()+", "+Utils.formatMinutesAsTime(userAvailability.getInt("startTime"))+"-" +Utils.formatMinutesAsTime(userAvailability.getInt("endTime")));
                               }
                           } catch (JSONException e1) {}
                       }
                   }
                   // get array with day of week days
                   String[] weekDays = new DateFormatSymbols().getWeekdays();
                   for (int i=0;i<hours.length;i++){
                        if(hours[i]!=null){
                            TextView day = new TextView(ProfileView.this);
                            day.setText(weekDays[i+1]);
                            availbilityItems.addView(day);
                            availbilityItems.addView(hours[i]);
                            GridLayout.LayoutParams dayParams = (GridLayout.LayoutParams)day.getLayoutParams();
                            GridLayout.LayoutParams hoursParams = (GridLayout.LayoutParams)hours[i].getLayoutParams();
                            dayParams.columnSpec = GridLayout.spec(0);
                            hoursParams.columnSpec = GridLayout.spec(1);
                            dayParams.rowSpec = GridLayout.spec(i+1);
                            hoursParams.rowSpec = GridLayout.spec(i+1);
                            day.setPadding(0,10,50,10);
                            day.setLayoutParams(dayParams);
                            hours[i].setLayoutParams(hoursParams);

                        }
                   }

                    availbilityItems.requestLayout();


//                        AvailabilityRecord record = new AvailabilityRecord();
//                        record.setTimeFrom(userAvailability.getInt("startTime"));
//                        record.setTimeUntil(userAvailability.getInt("endTime"));
//                        JSONArray days = userAvailability.getJSONArray("days");
//                        for (int i = 0; i < days.length(); i++) {
//                            try {
//                                record.setDay(days.getInt(i) - 1, true);
//                            } catch (JSONException e1) {
//                            }
//                        }
//                        availabilityAdapter.add(record);
//                    }
//                    availabilityAdapter.notifyDataSetChanged();
                }
            }
        });

        ParseFile p = currentUser.getParseFile("Photo");
        try
        {
            if(p != null) {
                Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
                viewPic.setImageBitmap(b);
            }
        }
        catch (ParseException e)
        {
            Log.d("My Loggggg", e.getMessage().toString());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDetails();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.edit_profile_button):
                Intent i = new Intent(this, ProfileEdit.class);
                startActivity(i);
        }
    }
}
