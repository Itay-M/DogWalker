package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;



public class MainActivity extends ActionBarActivity implements View.OnClickListener
{
    Button theSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        theSearchButton = (Button) findViewById(R.id.searchButton);
        theSearchButton.setOnClickListener(this);

        //initialize parse for using data control.
        Parse.initialize(this, "e1dj65Ni0LxqLAMpkS6USfFn72rPAOoajEOARnX2", "IcJnYSSt3oVTJQOwPycUhONhQ5N8qdx40kuIbujP");

        ParseUser currentUser = ParseUser.getCurrentUser();
        Resources res = getResources();

        setTitle((CharSequence) currentUser.getUsername());//set the title in the action bar to be the username.


//        ParseUser.logOut();//this loges out the user


        if (currentUser != null)
        {
            Log.d("My Loggggg", "user exists and logged in");
            Log.d("My Loggggg", "the username that logged in is - " + currentUser.getUsername().toString());
        }
        else
        {
            // show the register or login screen.
            Log.d("My Loggggg", "no user logged in...");
            Intent i = new Intent(this, OpeningActivity.class);
            startActivity(i);
        }

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.searchButton):
                Intent i = new Intent(this, SearchActivity.class);
                startActivity(i);
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





//    Button mainLogin,notRegistered;
//    boolean isLogin;
//        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
//        loginPref.edit().putBoolean("ISLOGIN",isLogin).commit();

//        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
//        isLogin = loginPref.getBoolean("ISLOGIN",false);







//
