package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

/**
 * Login activity - allow the user to sign in into the application by providing his username
 * and password. In addition the user can reset his password or go to the register activity if he
 * isn't registered yet.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener
{
    /**
     * Username EditText field
     */
    protected EditText usernameET;
    /**
     * Password EditText field
     */
    protected EditText passwordET;

    public LoginActivity(){
        super(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //reference the login button from the activity to this button.
        final Button loginB = (Button) findViewById(R.id.loginButton);

        //reference the forgot password button from the activity to this text view.
        TextView forgotPassB = (TextView) findViewById(R.id.forgotPasswordButton);

        //reference the register button from the activity to this text view.
        TextView registerB = (TextView)findViewById(R.id.login_register_button);

        //reference the user button from the activity to this button.
        usernameET = (EditText)findViewById(R.id.usernameEditText);

        //reference the login button from the activity to this button.
        passwordET = (EditText)findViewById(R.id.passwordEditText);

        loginB.setOnClickListener(this);
        registerB.setOnClickListener(this);
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
                        if (e == null){
                            Toast.makeText(getApplicationContext(), "user logged in successfully", Toast.LENGTH_SHORT).show();

                            // connect between installation app to login user
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("user",ParseUser.getCurrentUser());
                            installation.saveEventually();

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }else{
                            Utils.showMessageBox(getApplicationContext(),"Login Failed","no such user exist");
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
                            ParseUser.requestPasswordResetInBackground(input.getText().toString().toLowerCase(), new RequestPasswordResetCallback()
                            {
                                @Override
                                public void done(ParseException e)
                                {
                                if (e == null) {
                                    Toast.makeText(getApplicationContext(), "reset sent successfully to your email", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.d("Login", "Reset password failed "+e.getMessage());
                                    Utils.showMessageBox(getApplicationContext(),"Reset Password Failed",getString(R.string.unknown_error_occur));
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

                break;

            case R.id.login_register_button:
                Intent regIntent = new Intent(this, RegisterActivity.class);
                startActivity(regIntent);

                break;
        }
    }
}