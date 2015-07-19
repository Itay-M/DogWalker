package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class Login extends ActionBarActivity implements View.OnClickListener
{

    Button loginB;
    EditText usernameET;
    EditText passwordET;
//    boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //reference the login button from the activity to this button.
        loginB = (Button) findViewById(R.id.loginButton);
        //reference the user button from the activity to this button.
        usernameET = (EditText)findViewById(R.id.usernameEditText);
        //reference the login button from the activity to this button.
        passwordET = (EditText)findViewById(R.id.passwordEditText);


        loginB.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case R.id.loginButton:
                if(userExists(usernameET.getText().toString(),passwordET.getText().toString()))
                {
                    saveLoginPref();

                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }
                else
                {
                    // create and prompt alert if the user wasn't found.
                    final AlertDialog alert = new AlertDialog.Builder(this).create();
                    alert.setTitle("ALERT");//set the alert title.
                    alert.setMessage("user invalid");//set the alert message.
                    alert.setCancelable(true);//set the back button to exit the alert.
                    alert.setButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            alert.cancel();//exit the alert.
                        }
                    });

                    alert.show();
                }
                break;
        }


    }

    private boolean userExists(String theUsername,String thePassword)
    {
        //////////

        return false;
    }

    private void saveLoginPref()
    {
        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = loginPref.edit();
        editor.putBoolean("USEREXISTS",true).commit();
    }
}




//                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//                    dialogBuilder.setMessage("invalid user");//the alert to be prompt.
//                    dialogBuilder.setCancelable(true);// back button can exit the alert.
