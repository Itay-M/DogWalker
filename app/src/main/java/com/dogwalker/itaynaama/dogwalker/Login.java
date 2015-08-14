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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


public class Login extends AppCompatActivity implements View.OnClickListener
{

    Button loginB;
    EditText usernameET;
    EditText passwordET;
    SharedPreferences userPref;

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




//                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//                    dialogBuilder.setMessage("invalid user");//the alert to be prompt.
//                    dialogBuilder.setCancelable(true);// back button can exit the alert.
