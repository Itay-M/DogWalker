package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

//import com.parse.Parse;
//import com.parse.ParseException;
//import com.parse.ParseUser;
//import com.parse.SignUpCallback;


public class MainActivity extends ActionBarActivity implements View.OnClickListener
{

    Button mainLogin,notRegistered;
    boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.initialize(this, "e1dj65Ni0LxqLAMpkS6USfFn72rPAOoajEOARnX2", "IcJnYSSt3oVTJQOwPycUhONhQ5N8qdx40kuIbujP");



        ParseUser user = new ParseUser();
        user.setUsername("my name");
        user.setPassword("my pass");
        user.setEmail("email@example.com");
//
// other fields can be set just like with ParseObject
        user.put("phone", "650-555-0000");

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                }
            }
        });





//        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
//        loginPref.edit().putBoolean("ISLOGIN",isLogin).commit();

        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
        isLogin = loginPref.getBoolean("ISLOGIN",false);

        if(isLogin)
        {

        }
        else
        {
            Intent i = new Intent(this, OpeningActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();



    }

    @Override
    public void onClick(View v)
    {



    }
}






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