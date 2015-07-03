package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

//        ParseUser.logOut();//this loges out the user

        theSearchButton = (Button) findViewById(R.id.searchButton);
        theSearchButton.setOnClickListener(this);

        //initialize parse for using data control.
        Parse.initialize(this, "e1dj65Ni0LxqLAMpkS6USfFn72rPAOoajEOARnX2", "IcJnYSSt3oVTJQOwPycUhONhQ5N8qdx40kuIbujP");

        ParseUser currentUser = ParseUser.getCurrentUser();
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

}





//    Button mainLogin,notRegistered;
//    boolean isLogin;
//        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
//        loginPref.edit().putBoolean("ISLOGIN",isLogin).commit();

//        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
//        isLogin = loginPref.getBoolean("ISLOGIN",false);






//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }