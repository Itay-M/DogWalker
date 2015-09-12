package com.dogwalker.itaynaama.dogwalker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.Date;

/**
 * Activity which display details about a single walking request. The request can be either from the
 * current user to another user, or from another user to the current user.
 * The details of the user which will be presented in the activity will be of the "other" user (not
 * the current user).
 */
public class WalkerRequestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_request);

        // get UI components
        TextView nameText = (TextView)findViewById(R.id.walker_request_name);
        TextView dateText = (TextView)findViewById(R.id.walker_requset_date);
        TextView timeText = (TextView)findViewById(R.id.walker_requset_time);
        TextView phoneText = (TextView)findViewById(R.id.walker_request_phone);
        TextView addressText = (TextView)findViewById(R.id.walker_request_address);
        ImageView profileImage = (ImageView)findViewById(R.id.walker_request_image);

        // get the intent used to open this activity
        Intent intent = getIntent();

        // get the request id (as given on the intent)
        String requestId = intent.getStringExtra("reqId");

        // cancel the notification - in case there is one for this request
        NotificationManager mNotifyMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(requestId.hashCode());

        // get the user which his details should be presented
        final ParseUser user = new ParseUser();
        user.setObjectId(intent.getStringExtra("user"));
        try {
            user.fetch();
        } catch (ParseException e) {
            Log.e("WalkerRequest","Failed fetching user details: "+e.getMessage());
        }

        // mark the request as read in case the request has been sent to the current user
        if(ParseUser.getCurrentUser().getObjectId().equals(intent.getStringExtra("to"))) {
            ParseObject request = ParseObject.createWithoutData("Requests",requestId);
            request.put("isRead", true);
            request.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("WalkerRequestActivity","Failed updating request read status: "+e.getMessage());
                    }
                }
            });
        }

        // get the user profile picture
        ParseFile userImage = user.getParseFile("photo");
        if(userImage!=null) {
            try {
                Bitmap b = BitmapFactory.decodeByteArray(userImage.getData(), 0, userImage.getData().length);
                profileImage.setImageBitmap(b);
            } catch (ParseException e) {
                Log.e("WalkerRequestActivity","Faield getting the user profile picture: "+e.getMessage());
            }
        }

        // set the user details
        nameText.setText((String) user.get("name"));

        nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewProfile = new Intent(WalkerRequestActivity.this,ProfileViewActivity.class);
                viewProfile.putExtra("userId",user.getObjectId());
                startActivity(viewProfile);
            }
        });

        phoneText.setText((String)user.get("phone"));

        // display the request pickup date
        Date date = (Date)intent.getSerializableExtra("date");
        dateText.setText(Utils.DISPLAY_DATE_FORMAT.format(date));

        // display the request pickup time
        timeText.setText(Utils.formatMinutesAsTime(intent.getIntExtra("time", 0)));

        // display the request pickup address
        ArrayList<String> addressLines = intent.getStringArrayListExtra("address");
        addressText.setText(Utils.addressToString(new JSONArray(addressLines)));
    }

    /**
     * prepare a new intent based on the given request which can be used to start this activity
     */
    static public Intent prepareIntent(Context context, ParseObject request, String userKey){
        ParseUser reqUser = request.getParseUser(userKey);
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
        resultIntent.putExtra("to", request.getParseUser("to").getObjectId());
        resultIntent.putExtra("user",reqUser.getObjectId());
        resultIntent.putStringArrayListExtra("address", pickupAddress);
        resultIntent.putExtra("addressLat", addressLocation.getLatitude());
        resultIntent.putExtra("addressLng",addressLocation.getLongitude());

        return resultIntent;
    }
}