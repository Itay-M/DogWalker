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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.parse.Parse;
import com.parse.ParseUser;



public class MainActivity extends ActionBarActivity implements View.OnClickListener
{
    Button theSearchButton;
//    GoogleApiClient mGooleApiClient;
//    Location theLastLocation;

//    boolean thereIsAUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //button setup.
        theSearchButton = (Button) findViewById(R.id.searchButton);
        theSearchButton.setOnClickListener(this);

        //initialize parse for using data control.
        Parse.initialize(this, "e1dj65Ni0LxqLAMpkS6USfFn72rPAOoajEOARnX2", "IcJnYSSt3oVTJQOwPycUhONhQ5N8qdx40kuIbujP");
        SharedPreferences anyUserExists = PreferenceManager.getDefaultSharedPreferences(this);

//        SharedPreferences.Editor editor = anyUserExists.edit();
//        editor.putBoolean("USEREXISTS",thereIsAUser).commit();


        boolean b = anyUserExists.getBoolean("USEREXISTS", false);

        if (!b)
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
            Log.d("My Loggggg", "the username that logged in is - " + currentUser.getUsername().toString());


        }

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.searchButton):
//                Intent i = new Intent(this, SearchActivity.class);
//                startActivity(i);

                // Acquire a reference to the system Location Manager
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

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
     * Edit user's profile.
     */
    private void editProfile()
    {

    }

}

