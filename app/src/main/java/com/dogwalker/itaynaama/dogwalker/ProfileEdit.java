package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ProfileEdit extends AppCompatActivity implements View.OnClickListener {
    EditText nameEdit,cityEdit,mailEdit;
    Button resetPassword,saveChangesB;
    ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        nameEdit = (EditText) findViewById(R.id.profile_name_edit);
        cityEdit = (EditText) findViewById(R.id.profile_user_city_edit);

        nameEdit.setHint(currentUser.get("Name").toString());
        cityEdit.setHint(currentUser.get("City").toString());

        resetPassword = (Button) findViewById(R.id.reset_password_button);
        saveChangesB = (Button) findViewById(R.id.save_changes_button);

        resetPassword.setOnClickListener(this);
        saveChangesB.setOnClickListener(this);


    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.reset_password_button):
                final android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setHint("enter your email address here...");
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
                break;

            case (R.id.save_changes_button):
                if (!(nameEdit.getText().toString().equals(currentUser.get("Name").toString())))
                {
                    Log.d("My Loggggg", "name changed");
                    currentUser.put("Name", nameEdit.getText().toString());
                }

                if (!(cityEdit.getText().toString().equals(currentUser.get("City").toString())))
                {
                    Log.d("My Loggggg", "city changed");
                    currentUser.put("City", cityEdit.getText().toString());
                }

                try
                {
                    currentUser.save();
                    Toast.makeText(getApplicationContext(), "changes saved successfully", Toast.LENGTH_LONG).show();
                }
                catch (ParseException e)
                {
                    Toast.makeText(getApplicationContext(), "error! changes didn't saved", Toast.LENGTH_LONG).show();
                    Log.d("My Loggggg", "error saving changes");
                    e.printStackTrace();
                }

                break;
        }
    }
}
