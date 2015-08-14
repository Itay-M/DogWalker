package com.dogwalker.itaynaama.dogwalker;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class OpeningActivity extends Activity implements View.OnClickListener
{
    Button registerB,signinB;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        registerB = (Button)findViewById(R.id.toRegisterButton);
        signinB = (Button)findViewById(R.id.toSignInButton);

        registerB.setOnClickListener(this);
        signinB.setOnClickListener(this);

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.toRegisterButton:
                Intent regIntent = new Intent(this, Register.class);
                startActivity(regIntent);
                break;


            case R.id.toSignInButton:
                Intent signIntent = new Intent(this, Login.class);
                startActivity(signIntent);

                break;

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_opening, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
