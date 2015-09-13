package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormatSymbols;
import java.util.List;

/**
 * Activity that display all the user details.
 */
public class ProfileViewActivity extends BaseActivity implements View.OnClickListener
{
    // UI components
    protected ImageView viewPic;
    protected TextView name, userName, userCity, userPhone, userSharePhone;
    protected Button editButton;
    protected ParseUser user;

    /**
     * Adapter to hold the user availabilities
     */
    protected UserAvailabilityAdapter availabilityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        Intent intent = getIntent();

        if(intent.getStringExtra("userId")==null) {
            user = ParseUser.getCurrentUser();
        }else{
            user = new ParseUser();
            user.setObjectId(intent.getStringExtra("userId"));
            try {
                user.fetch();
            } catch (ParseException e) {
                Log.e("ProfileViewActivity",e.getMessage());
                finish();
            }
        }

        // get UI components
        viewPic = (ImageView)findViewById(R.id.view_user_pic);
        editButton = (Button) findViewById(R.id.edit_profile_button);
        name = (TextView) findViewById(R.id.profile_name);
        userName = (TextView) findViewById(R.id.profile_user_name);
        userCity = (TextView) findViewById(R.id.profile_user_city);
        userPhone = (TextView)findViewById(R.id.profile_user_phone);
        userSharePhone = (TextView)findViewById(R.id.profile_user_sharePhone);

        // handle edit button
        if(!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            editButton.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            userCity.setVisibility(View.GONE);
        }else {
            editButton.setOnClickListener(this);
        }

        // refresh user details
        fetchDetails();
    }

    /**
     * Update the UI components with the user details
     */
    private void fetchDetails(){

        name.setText("Name: " + user.get("name").toString());
        if(user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            userName.setText("UserName: " + user.getUsername().toString());
            userCity.setText("Address: " + Utils.addressToString(user.getJSONArray("address"), ",\n"));
        }

        boolean isShared = (boolean) user.get("sharePhone");
        if (!isShared && !user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) ) {
            userPhone.setText("Phone: Hidden");
        } else {
            userPhone.setText("Phone: " + user.get("phone").toString());
        }

        // user availability
        final GridLayout availabilityItems = (GridLayout)findViewById(R.id.view_availability_items);
        availabilityItems.removeAllViews();
        final ParseQuery<ParseObject> userAvailabilityQuery = new ParseQuery<>("UserAvailability");
        userAvailabilityQuery.whereEqualTo("user", user);
        userAvailabilityQuery.orderByAscending("startTime");
        userAvailabilityQuery.findInBackground(new FindCallback<ParseObject>() {
            // get list of weekdays names
            private final String[] weekDays = new DateFormatSymbols().getWeekdays();

            @Override
            public void done(List<ParseObject> userAvailabilities, ParseException e) {
                if (e == null) {

                    // build TextView for each day and fill with hours ranges from results
                    TextView[] hours = new TextView[7];
                    for (ParseObject userAvailability : userAvailabilities) {
                        JSONArray days = userAvailability.getJSONArray("days");
                        for (int i = 0; i < days.length(); i++) {
                            try {
                                int d = days.getInt(i);
                                if(hours[d-1]==null){
                                    hours[d-1] = new TextView(ProfileViewActivity.this);
                                    hours[d-1].setText(Utils.formatMinutesAsTime(userAvailability.getInt("startTime"))+"-" +Utils.formatMinutesAsTime(userAvailability.getInt("endTime")));
                                }else{
                                    hours[d-1].setText(hours[d-1].getText()+", "+Utils.formatMinutesAsTime(userAvailability.getInt("startTime"))+"-" +Utils.formatMinutesAsTime(userAvailability.getInt("endTime")));
                                }
                            } catch (JSONException e1) {}
                        }
                    }

                    // for each day the user is available create a row in the table with the day
                    // name and the hours ranges
                    for (int i=0;i<hours.length;i++){
                        if(hours[i]!=null){
                            TextView day = new TextView(ProfileViewActivity.this);
                            day.setText(weekDays[i+1]);
                            availabilityItems.addView(day);
                            availabilityItems.addView(hours[i]);
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

                    // refresh the availabilities list items
                    availabilityItems.requestLayout();
                }else{
                    Log.e("ProfileView","Failed getting user availabilities periods: "+e.getMessage());
                }
            }
        });

        // user profile picture
        try {
            ParseFile p = user.getParseFile("photo");
            if(p != null) {
                Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
                viewPic.setImageBitmap(b);
            }
        } catch (ParseException e) {
            Log.e("ProfileView", "Failed getting profile picture: "+e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // refresh user details (in case updated from the profile edit activity)
        fetchDetails();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.edit_profile_button):
                // move to edit profile activity
                Intent i = new Intent(this, ProfileEditActivity.class);
                startActivity(i);

                break;
        }
    }
}
