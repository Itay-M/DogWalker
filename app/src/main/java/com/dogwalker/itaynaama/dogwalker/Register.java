package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class Register extends AppCompatActivity implements View.OnClickListener
{
    Button registerB;
    EditText nameET,usernameET,emailET,cityET,passwordET;
    SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerB = (Button) findViewById(R.id.registerButton);
        nameET = (EditText)findViewById(R.id.regNameEditText);
        usernameET = (EditText)findViewById(R.id.regUsernameEditText);
        emailET = (EditText)findViewById(R.id.regEmailEditText);
        cityET = (EditText)findViewById(R.id.regCityEditText);
        passwordET = (EditText)findViewById(R.id.regPasswordEditText);

        registerB.setOnClickListener(this);

        userPref = PreferenceManager.getDefaultSharedPreferences(this);
    }


    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case(R.id.registerButton):

                //new user creation.
                ParseUser user = new ParseUser();
                //retrieve data from the EditText objects and place it as the user data.
                user.setUsername(usernameET.getText().toString());
                user.setPassword(passwordET.getText().toString());
                user.setEmail(emailET.getText().toString());
                user.put("Name", nameET.getText().toString());
                user.put("City", cityET.getText().toString());

                //register the new user in Parse database.
                user.signUpInBackground(new SignUpCallback()
                {
                    public void done(final ParseException e)
                    {
                        if (e == null)
                        {
                            // Hooray! the user created successfully.
                            // create and prompt alert for the successful user creation.
                            final AlertDialog alert = new AlertDialog.Builder(Register.this).create();
                            alert.setTitle("New User");//set the alert title.
                            alert.setMessage("User created successfully");//set the alert message.
                            alert.setCancelable(true);//set the back button to exit the alert.
                            alert.setButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alert.cancel();//exit the alert.
                                    //save the fact that there is a current user.
                                    SharedPreferences.Editor editor = userPref.edit();
                                    editor.putBoolean("USEREXISTS",true).commit();
                                    //go to main activity.
                                    Intent registerIntent = new Intent(Register.this, MainActivity.class);
                                    startActivity(registerIntent);
                                }
                            });

                            alert.show();
                        }
                        else
                        {
                            // Sign up didn't succeed, Look at the ParseException to figure out what went wrong.
                            //in addition prompt alert for the unsuccessful registration.
                            final AlertDialog alert = new AlertDialog.Builder(Register.this).create();
                            alert.setTitle("New User Error");//set the alert title.
                            alert.setMessage("User creation failed!");//set the alert message.
                            alert.setCancelable(true);//set the back button to exit the alert.
                            alert.setButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {

                                    alert.cancel();//exit the alert.
                                    finish();
                                    startActivity(getIntent());
                                }
                            });

                            alert.show();
                        }
                    }
                });

                break;
        }
    }
}
