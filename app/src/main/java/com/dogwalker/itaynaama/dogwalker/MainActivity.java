package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.parse.ParseUser;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    Button theSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button setup
        theSearchButton = (Button) findViewById(R.id.searchButton);
        theSearchButton.setOnClickListener(this);
        //check if there is a current user logged in
        currentUserHandle();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.searchButton):
                // Acquire a reference to the system Location Manager
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                // check if the device's gps is turned on
                boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                // if gps isn't turned on - ask the user if he/she wants to activate it
                if (!gps_enabled)
                {
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setMessage("enable GPS?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(gpsOptionsIntent);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Log.d("My Loggggg", "user canceled");
                                }
                            });
                    alertBuilder.show();
                }
                else
                {
                    Intent i = new Intent(this, SearchActivity.class);
                    startActivity(i);
                }
        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handle action bar item clicks.
     * @param item - menu item.
     * @return true if item was clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch(item.getItemId())
        {
            case R.id.action_settings:
                return true;

            case R.id.edit_profile_action:
                editProfile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Check if there is a current user logged in.
     * yes - handle the current user and puts it's name in the action bar.
     * No - go to OpeningActivity for login or register.
     */
    private void currentUserHandle()
    {
        SharedPreferences anyUserExists = PreferenceManager.getDefaultSharedPreferences(this);
        boolean currentUserExists = anyUserExists.getBoolean("USEREXISTS", false);

        if (!currentUserExists)
        {
            Log.d("My Loggggg", "there is no current user, referring to login or register activity...");
            Intent i = new Intent(this, OpeningActivity.class);
            startActivity(i);
        }
        else
        {
            ParseUser currentUser = ParseUser.getCurrentUser();
            setTitle(currentUser.getUsername());//set the title in the action bar to be the username.
            Log.d("My Loggggg", "user exists and logged in");
            Log.d("My Loggggg", "the username that logged in is - " + currentUser.getUsername());
        }
    }

    /**
     * Edit user's profile.
     */
    private void editProfile()
    {

    }

}

