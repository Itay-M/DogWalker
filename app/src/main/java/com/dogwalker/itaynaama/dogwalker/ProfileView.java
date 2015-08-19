package com.dogwalker.itaynaama.dogwalker;

import android.os.Bundle;
import android.widget.TextView;

import com.parse.ParseUser;

public class ProfileView extends BaseActivity
{

    TextView name,userName,userCity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        name = (TextView) findViewById(R.id.profile_name);
        userName = (TextView) findViewById(R.id.profile_user_name);
        userCity = (TextView) findViewById(R.id.profile_user_city);

        fetchDetails();
    }

    /**
     * present the user's details.
     */
    private void fetchDetails()
    {
        ParseUser currentUser = ParseUser.getCurrentUser();

        name.setText("Name: " + currentUser.get("Name").toString());
        userName.setText("UserName: " + currentUser.getUsername().toString());
        userCity.setText("City: " + currentUser.get("City").toString());

    }


}
