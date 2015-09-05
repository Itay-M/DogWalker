package com.dogwalker.itaynaama.dogwalker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class WalkerRequestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_request);

        TextView nameText = (TextView)findViewById(R.id.walker_request_name);
        TextView dateText = (TextView)findViewById(R.id.walker_requset_date);
        TextView timeText = (TextView)findViewById(R.id.walker_requset_time);
        TextView phoneText = (TextView)findViewById(R.id.walker_request_phone);
        TextView addressText = (TextView)findViewById(R.id.walker_request_address);
        ImageView profileImage = (ImageView)findViewById(R.id.walker_request_image);

        Intent intent = getIntent();
        String requestId = intent.getStringExtra("reqId");

        NotificationManager mNotifyMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(requestId.hashCode());

        ParseUser userRequested = new ParseUser();
        userRequested.setObjectId(intent.getStringExtra("user"));
        try {
            userRequested.fetch();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // update request that is open
        ParseObject request = ParseObject.createWithoutData("Requests",requestId);

        if(ParseUser.getCurrentUser().equals(userRequested.getObjectId())) {
            request.put("isRead", true);
            request.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e == null) {
                        // Saved successfully.
                    } else {
                        // The save failed.
                    }
                }
            });
        }

        ParseFile userImage = userRequested.getParseFile("Photo");
        if(userImage!=null) {
            try {
                Bitmap b = BitmapFactory.decodeByteArray(userImage.getData(), 0, userImage.getData().length);
                profileImage.setImageBitmap(b);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        nameText.setText((String) userRequested.get("Name"));
        nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewProfile = new Intent(WalkerRequestActivity.this,ProfileView.class);
                startActivity(viewProfile);
            }
        });

        phoneText.setText((String)userRequested.get("Phone"));

        Date date = (Date)intent.getSerializableExtra("date");
        dateText.setText(WalkerSearchActivity.DISPLAY_DATE_FORMAT.format(date));

        int puTime = intent.getIntExtra("time", 0);
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

    /**
     * prepare a new intent based on the given request which can be used to start this activity
     */
    static public Intent prepareIntent(Context context, ParseObject request){
        ParseUser reqUser = request.getParseUser("from");
        Date pickupDate = request.getDate("datePickup");
        int pickupTime = request.getInt("timePickup");
        JSONArray pickupAddressLines = request.getJSONArray("address");
        ArrayList<String> pickupAddress = new ArrayList<>(pickupAddressLines.length());
        for (int i = 0; i < pickupAddressLines.length(); i++) {
            try {
                pickupAddress.add(pickupAddressLines.getString(i));
            } catch (JSONException e1) {
                Log.d("PushReceiver", e1.getMessage());
            }
        }
        ParseGeoPoint addressLocation = request.getParseGeoPoint("addressLocation");

        Intent resultIntent = new Intent(context, WalkerRequestActivity.class);
        resultIntent.setAction(context.getString(R.string.walking_request_intent_action));
        resultIntent.putExtra("reqId", request.getObjectId());
        resultIntent.putExtra("date", pickupDate);
        resultIntent.putExtra("time", pickupTime);
        resultIntent.putExtra("user",reqUser.getObjectId());
        resultIntent.putStringArrayListExtra("address", pickupAddress);
        resultIntent.putExtra("addressLat", addressLocation.getLatitude());
        resultIntent.putExtra("addressLng",addressLocation.getLongitude());

        return resultIntent;
    }

}
