package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;


public class Login extends AppCompatActivity implements View.OnClickListener
{

    Button loginB;
    Button forgotPassB;
    EditText usernameET;
    EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //reference the login button from the activity to this button.
        loginB = (Button) findViewById(R.id.loginButton);
        //reference the forgot password button from the activity to this button.
        forgotPassB = (Button) findViewById(R.id.forgotPasswordButton);
        //reference the user button from the activity to this button.
        usernameET = (EditText)findViewById(R.id.usernameEditText);
        //reference the login button from the activity to this button.
        passwordET = (EditText)findViewById(R.id.passwordEditText);

        loginB.setOnClickListener(this);
        forgotPassB.setOnClickListener(this);

        //set the "GO" button in the keyboard to alternatively press the register button
        passwordET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    loginB.performClick();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.loginButton:
                //perform login using the username and password typed in the EditTexts, matching it to the Parse database.
                ParseUser.logInInBackground(usernameET.getText().toString(), passwordET.getText().toString(), new LogInCallback()
                {
                    @Override
                    public void done(ParseUser parseUser, ParseException e)
                    {
                        if (e == null)
                        {
                            saveLoginPref();
                            Toast.makeText(getApplicationContext(), "user logged in successfully", Toast.LENGTH_LONG).show();

                            // connect between installation app to login user
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("user",ParseUser.getCurrentUser());
                            installation.saveEventually();

                            Intent i = new Intent(Login.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "no such user exist", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                break;

            case R.id.forgotPasswordButton:
                //perform user password reset by sending a password reset request using provided email address.
                final android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                final EditText input = new EditText(this);

                alertBuilder.setMessage("Do you want to reset your password \n(sending reset to your mail)")
                        .setPositiveButton("Send reset", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //send the reset to the mail, if not successful print exception.
                                ParseUser.requestPasswordResetInBackground(input.getText().toString(), new RequestPasswordResetCallback()
                                {
                                    @Override
                                    public void done(ParseException e)
                                    {
                                        if (e == null)
                                        {
                                            Toast.makeText(getApplicationContext(), "reset sent successfully to your email", Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            Log.d("My Loggggg", e.getMessage());
                                            Toast.makeText(getApplicationContext(), "error" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("My Loggggg", "user canceled reset");
                            }
                        });
                alertBuilder.setView(input);
                alertBuilder.show();
        }
    }


    /**
     * save the status of current user exists to SharedPreferences.
     */
    private void saveLoginPref()
    {
        SharedPreferences loginPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = loginPref.edit();
        editor.putBoolean("USEREXISTS",true).commit();
    }

}