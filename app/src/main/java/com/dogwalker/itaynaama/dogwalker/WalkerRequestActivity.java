package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.location.Address;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class WalkerRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_request);

        TextView nameText = (TextView)findViewById(R.id.walker_request_name);
        TextView dateText = (TextView)findViewById(R.id.walker_requset_date);
        TextView timeText = (TextView)findViewById(R.id.walker_requset_time);
        TextView phoneText = (TextView)findViewById(R.id.walker_request_phone);
        TextView addressText = (TextView)findViewById(R.id.walker_request_address);

        Intent intent = getIntent();
        if(intent.getAction().equals(getString(R.string.walking_request_intent_action))){

            ParseUser userRequested = new ParseUser();
            userRequested.setObjectId(intent.getStringExtra("user"));
            try {
                userRequested.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            nameText.setText((String) userRequested.get("Name"));
            phoneText.setText((String)userRequested.get("Phone"));

            long puDate = intent.getLongExtra("date", 0);
            int puTime = intent.getIntExtra("time", 0);

            Date date = new Date(puDate);
            dateText.setText(WalkerSearchActivity.DISPLAY_DATE_FORMAT.format(date.getTime()));

            Calendar time = Calendar.getInstance();
            time.set(Calendar.HOUR_OF_DAY,puTime/60);
            time.set(Calendar.MINUTE,puTime%60);
            timeText.setText(WalkerSearchActivity.DISPLAY_TIME_FORMAT.format(time.getTime()));

            ArrayList<String> addressLines = intent.getStringArrayListExtra("address");
            StringBuilder addressTextValue = new StringBuilder();
            for(int i=0;i<addressLines.size();i++){
                addressTextValue.append(addressLines.get(i));
                if(i<(addressLines.size()-1))
                    addressTextValue.append(",\n");
            }
            addressText.setText(addressTextValue);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_walker_request, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
