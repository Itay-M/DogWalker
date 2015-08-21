package com.dogwalker.itaynaama.dogwalker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseUser;

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
        mailEdit = (EditText) findViewById(R.id.profile_user_mail_edit);

        nameEdit.setHint(currentUser.get("Name").toString());
        cityEdit.setHint(currentUser.get("City").toString());

        resetPassword = (Button) findViewById(R.id.reset_password_button);
        saveChangesB = (Button) findViewById(R.id.save_changes_button);

        resetPassword.setOnClickListener(this);
        saveChangesB.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {

    }
}
