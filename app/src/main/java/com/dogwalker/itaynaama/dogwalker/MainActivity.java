package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.ParseInstallation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener
{
    protected Button theSearchButton;
    protected ImageView profilePicFromParse;
    protected ListView walkerRequestsList;
    protected ListView myRequestsList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //imageView
        profilePicFromParse = (ImageView)findViewById(R.id.profilePic);

        //button setup
        theSearchButton = (Button) findViewById(R.id.searchButton);

        // walker requests list
        walkerRequestsList = (ListView)findViewById(R.id.main_walker_requests_list);

        walkerRequestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject request = (ParseObject)parent.getAdapter().getItem(position);
                Intent i = WalkerRequestActivity.prepareIntent(MainActivity.this, request);
                startActivity(i);
            }
        });

        // my request list
        myRequestsList =(ListView)findViewById(R.id.main_my_request_list);

        myRequestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject myReq = (ParseObject)parent.getAdapter().getItem(position);
                Intent i = WalkerRequestActivity.prepareIntent(MainActivity.this, myReq);
                startActivity(i);
            }
        });

        //hide the actionBar's back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        theSearchButton.setOnClickListener(this);

        //check if there is a current user logged in
        currentUserHandle();

        // retrieve walker requests and fill the list
        retrieveWalkerRequests();

        // retrieve my requests and fill the list
        retrieveMyRequests();
    }

    /**
     * take all request that send to user
     */
    protected void retrieveWalkerRequests(){
        ParseQuery<ParseObject> requestsQuery = new ParseQuery<>("Requests");
        requestsQuery.whereEqualTo("to",ParseUser.getCurrentUser());
        requestsQuery.include("from");
        requestsQuery.addDescendingOrder("datePickup");
        requestsQuery.addDescendingOrder("timePickup");
        requestsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                if(e==null) {
                    ListAdapter adapter = new WalkerRequestsListAdapter(MainActivity.this, requests, "from");
                    walkerRequestsList.setAdapter(adapter);
                }else{
                    Log.e("MainActivity",e.getMessage());
                }
            }
        });
    }

    /**
     * take all request that send from user
     */
    protected void retrieveMyRequests(){
        ParseQuery<ParseObject> requestsQuery = new ParseQuery<>("Requests");
        requestsQuery.whereEqualTo("from",ParseUser.getCurrentUser());
        requestsQuery.include("to");
        requestsQuery.addDescendingOrder("datePickup");
        requestsQuery.addDescendingOrder("timePickup");
        requestsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                if(e==null) {
                    ListAdapter adapter = new WalkerRequestsListAdapter(MainActivity.this, requests, "to");
                    myRequestsList.setAdapter(adapter);
                }else{
                    Log.e("MainActivity",e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        currentUserHandle();

        retrieveWalkerRequests();
        retrieveMyRequests();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.searchButton):
                    Intent i = new Intent(this, WalkerSearchActivity.class);
                    startActivity(i);
                break;
        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();
    }

    /**
     * Check if there is a current user logged in.
     * yes - handle the current user and puts it's name in the action bar.
     * No - go to OpeningActivity for login or register.
     */

    private void currentUserHandle()
    {
        Log.d("My Loggggg","currentUserHandle");
        SharedPreferences anyUserExists = PreferenceManager.getDefaultSharedPreferences(this);
        boolean currentUserExists = anyUserExists.getBoolean("USEREXISTS", false);

        if (!currentUserExists || (ParseUser.getCurrentUser() == null))
        {
            Log.d("My Loggggg", "there is no current user, referring to login or register activity...");
            Intent i = new Intent(this, OpeningActivity.class);
            startActivity(i);
        }
        else
        {
            // connect between installation app to login user
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            installation.put("user",ParseUser.getCurrentUser());
            installation.saveEventually();

            ParseUser currentUser = ParseUser.getCurrentUser();

            // setTitle(currentUser.getUsername());//set the title in the action bar to be the username.
            Log.d("My Loggggg", "user exists and logged in");
            Log.d("My Loggggg", "the username that logged in is - " + currentUser.getUsername());

            //show profile picture in the imageView

            try
            {
                Log.d("My Loggggg", "in");
                ParseFile p = currentUser.getParseFile("Photo");
                if(p != null) {
                    Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
                    profilePicFromParse.setImageBitmap(b);
                }else{
                    profilePicFromParse.setImageResource(R.drawable.logo);
                }
            }
            catch (ParseException e)
            {
                Log.d("My Loggggg", e.getMessage().toString());
                Log.d("My Loggggg", currentUser.getUsername());
                            }
        }
    }

    /**
     * when the back button pressed, the user asked if he wants to exit the app.
     */

    @Override
    public void onBackPressed()
    {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
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
}