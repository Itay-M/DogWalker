package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class Register extends ActionBarActivity implements View.OnClickListener
{
    Button registerB;
    EditText nameET,usernameET,ageET,cityET,passwordET;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerB = (Button) findViewById(R.id.registerButton);
        nameET = (EditText)findViewById(R.id.regNameEditText);
        usernameET = (EditText)findViewById(R.id.regUsernameEditText);
        ageET = (EditText)findViewById(R.id.regAgeEditText);
        cityET = (EditText)findViewById(R.id.regCityEditText);
        passwordET = (EditText)findViewById(R.id.regPasswordEditText);

        registerB.setOnClickListener(this);
    }


    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case(R.id.registerButton):
                Intent registerIntent = new Intent(this, MainActivity.class);
                startActivity(registerIntent);
                break;
        }

    }
}
