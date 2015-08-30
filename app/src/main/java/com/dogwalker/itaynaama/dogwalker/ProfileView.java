package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class ProfileView extends BaseActivity implements View.OnClickListener
{
    ImageView viewPic;
    TextView name, userName, userCity, userPhone;
    Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        viewPic = (ImageView)findViewById(R.id.view_user_pic);
        editButton = (Button) findViewById(R.id.edit_profile_button);
        name = (TextView) findViewById(R.id.profile_name);
        userName = (TextView) findViewById(R.id.profile_user_name);
        userCity = (TextView) findViewById(R.id.profile_user_city);
        userPhone = (TextView)findViewById(R.id.profile_user_phone);

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
        userPhone.setText("Phone: " + currentUser.get("Phone").toString());

        ParseFile p = (ParseFile) currentUser.get("Photo");
        try
        {
            Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
            viewPic.setImageBitmap(b);
        }
        catch (ParseException e)
        {
            Log.d("My Loggggg", e.getMessage().toString());
        }

    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.edit_profile_button):
                Intent i = new Intent(this, ProfileEdit.class);
                startActivity(i);
        }
    }
}
