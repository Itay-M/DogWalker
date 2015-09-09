package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;

public class BaseActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // show the actionBar's back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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

        // disconnect user from this installation
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.remove("user");
        installation.saveEventually();

        Intent i = new Intent(this, OpeningActivity.class);
        startActivity(i);
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
