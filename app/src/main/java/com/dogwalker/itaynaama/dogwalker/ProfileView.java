package com.dogwalker.itaynaama.dogwalker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseUser;

public class ProfileView extends BaseActivity implements View.OnClickListener {

    TextView name,userName,userCity;
    Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        editButton = (Button) findViewById(R.id.edit_profile_button);
        name = (TextView) findViewById(R.id.profile_name);
        userName = (TextView) findViewById(R.id.profile_user_name);
        userCity = (TextView) findViewById(R.id.profile_user_city);

        editButton.setOnClickListener(this);

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


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.edit_profile_button):

        }
    }
}
