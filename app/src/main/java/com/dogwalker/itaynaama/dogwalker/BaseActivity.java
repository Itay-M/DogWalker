package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseUser;

public class BaseActivity extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // show the actionBar's back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch(item.getItemId())
        {
            case R.id.logout_action:
                logout();
                return true;

            case R.id.action_settings:
                launchSettings();
                return true;

            case R.id.view_profile_action:
                viewProfile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * terminate the current activity and move to
     * @return
     */
    @Override
    public boolean onSupportNavigateUp()
    {
        finish();
        return true;
    }

    /**
     * Log out the current user.
     */
    private void logout()
    {
        Toast.makeText(getApplicationContext(), "Logging out...", Toast.LENGTH_LONG).show();
        ParseUser.logOut();
        Intent i = new Intent(this, OpeningActivity.class);
        startActivity(i);
    }

    private void launchSettings()
    {

    }

    /**
     * Edit user's profile.
     */
    private void viewProfile()
    {
        Intent i = new Intent(this, ProfileView.class);
        startActivity(i);
    }



}
