package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.parse.FindCallback;

import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * The main screen activity - displaying the user his walking requests as well as requests he has
 * sent to other users and an option to search for a walker.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener
{
    /**
     * ListView displaying all the walking requests sent to the current user
     */
    protected ListView walkerRequestsList;
    /**
     * ListView dispaying all the requests sent by the current user to other users
     */
    protected ListView myRequestsList;
    /**
     * Loading indicator for the walking requests list
     */
    protected ProgressBar walkingReqsLoading;
    /**
     * Loading indicator for the current user requests list
     */
    protected ProgressBar myReqsLoading;
    /**
     * TextView displaying "not found" message when no walking requests has found
     */
    protected TextView walkerReqsNotFound;
    /**
     * TextView displaying "not found" message when no current user requests has found
     */
    protected TextView myReqsNotFound;

    public MainActivity(){
        super(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // search for a walker button
        Button theSearchButton = (Button) findViewById(R.id.searchButton);
        // requests lists loader indicators
        walkingReqsLoading = (ProgressBar)findViewById(R.id.main_walker_reqs_loading);
        myReqsLoading = (ProgressBar)findViewById(R.id.main_my_reqs_loading);
        // requests lists not found text views
        walkerReqsNotFound = (TextView)findViewById(R.id.main_walker_reqs_not_found);
        myReqsNotFound = (TextView)findViewById(R.id.main_my_reqs_not_found);
        // requests lists
        walkerRequestsList = (ListView)findViewById(R.id.main_walker_requests_list);
        myRequestsList =(ListView)findViewById(R.id.main_my_request_list);

        // handle request click
        walkerRequestsList.setOnItemClickListener(this);
        myRequestsList.setOnItemClickListener(this);

        // handle search button
        theSearchButton.setOnClickListener(this);

        //check if there is a current user logged in
        currentUserHandle();

        // retrieve walker requests and fill the list
        retrieveWalkerRequests();

        // retrieve my requests and fill the list
        retrieveMyRequests();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ParseObject request = (ParseObject)parent.getAdapter().getItem(position);
        Intent i = WalkerRequestActivity.prepareIntent(MainActivity.this, request);
        startActivity(i);
    }

    /**
     * take all request that send to user
     */
    protected void retrieveWalkerRequests(){
        // display loading indicator
        walkingReqsLoading.setVisibility(View.VISIBLE);
        walkerRequestsList.setVisibility(View.GONE);
        walkerReqsNotFound.setVisibility(View.GONE);

        // perform search in background
        ParseQuery<ParseObject> requestsQuery = new ParseQuery<>("Requests");
        requestsQuery.whereEqualTo("to",ParseUser.getCurrentUser());
        requestsQuery.include("from");
        requestsQuery.addDescendingOrder("datePickup");
        requestsQuery.addDescendingOrder("timePickup");
        requestsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                // update list items
                if (e == null) {
                    ListAdapter adapter = new WalkerRequestsListAdapter(MainActivity.this, requests, "from");
                    walkerRequestsList.setAdapter(adapter);
                } else {
                    Log.e("MainActivity", e.getMessage());
                }

                // display "no requests found" if needed
                if (e != null || requests.isEmpty()) {
                    walkerReqsNotFound.setVisibility(View.VISIBLE);
                    walkerRequestsList.setVisibility(View.GONE);
                } else {
                    walkerReqsNotFound.setVisibility(View.GONE);
                    walkerRequestsList.setVisibility(View.VISIBLE);
                }

                // hide loading indicator
                walkingReqsLoading.setVisibility(View.GONE);
            }
        });

    }

    /**
     * take all request that send from user
     */
    protected void retrieveMyRequests(){
        // display loading indicator
        myReqsLoading.setVisibility(View.VISIBLE);
        myRequestsList.setVisibility(View.GONE);
        myReqsNotFound.setVisibility(View.GONE);

        // perform search in background
        ParseQuery<ParseObject> requestsQuery = new ParseQuery<>("Requests");
        requestsQuery.whereEqualTo("from",ParseUser.getCurrentUser());
        requestsQuery.include("to");
        requestsQuery.addDescendingOrder("datePickup");
        requestsQuery.addDescendingOrder("timePickup");
        requestsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                // update list items
                if (e == null) {
                    ListAdapter adapter = new WalkerRequestsListAdapter(MainActivity.this, requests, "to");
                    myRequestsList.setAdapter(adapter);
                } else {
                    Log.e("MainActivity", e.getMessage());
                }

                // display "no requests found" if needed
                if (e != null || requests.isEmpty()) {
                    myReqsNotFound.setVisibility(View.VISIBLE);
                    myRequestsList.setVisibility(View.GONE);
                } else {
                    myReqsNotFound.setVisibility(View.GONE);
                    myRequestsList.setVisibility(View.VISIBLE);
                }

                // hide loading indicator
                myReqsLoading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentUserHandle()) {
            retrieveWalkerRequests();
            retrieveMyRequests();
        }
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

    /**
     * Check if there is a current user logged in.
     * No - go to LoginActivity for login or register.
     */
    private boolean currentUserHandle() {
        if (ParseUser.getCurrentUser() == null) {
            Log.d("MainActivity", "there is no current user, referring to login or register activity...");

            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);

            return false;
        }

        return true;
    }
}