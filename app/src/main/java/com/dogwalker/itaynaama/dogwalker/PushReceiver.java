package com.dogwalker.itaynaama.dogwalker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.provider.ContactsContract;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by naama on 25/08/2015.
 */
public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction().equals("com.dogwalker.itaynaama.dogwalker.WALKING_REQUEST")) {
            try {
                // TODO take request objectId from JSONObject and push other data from Requests parse class
                JSONObject data = new JSONObject(intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA));

                // take id of request
                final String reqId = data.getString("reqId");

                ParseQuery<ParseObject> requestQuery = new ParseQuery<>("Requests");
                requestQuery.whereEqualTo("objectId", reqId);
                requestQuery.include("from");
                requestQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject request, ParseException e) {
                        if (e == null && request != null) {
                            ParseUser reqUser = request.getParseUser("from");
                            Date pickupDate = request.getDate("datePickup");
                            int pickupTime = request.getInt("timePickup");
                            JSONArray pickupAddressLines = request.getJSONArray("address");
                            ArrayList<String> pickupAddress = new ArrayList<>(pickupAddressLines.length());
                            for (int i = 0; i < pickupAddressLines.length(); i++) {
                                try {
                                    pickupAddress.add(pickupAddressLines.getString(i));
                                } catch (JSONException e1) {
                                    Log.d("PushReceiver",e1.getMessage());
                                }
                            }
                            ParseGeoPoint addressLocation = request.getParseGeoPoint("addressLocation");

                            createPush(context, reqUser, pickupDate, pickupTime, pickupAddress, addressLocation.getLatitude(), addressLocation.getLongitude());
                        } else {
                            Log.d("PushReceiver", e.getMessage());
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    static public void createPush(Context context,ParseUser user,Date date,int time,ArrayList<String> address,double addressLat, double addressLng){
        android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("DogWalking request")
                        .setContentText(user.getUsername() + " want you to take out his/her dog.");

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        Intent resultIntent = new Intent(context, WalkerRequestActivity.class);
        resultIntent.setAction(context.getString(R.string.walking_request_intent_action));
        resultIntent.putExtra("date", date);
        resultIntent.putExtra("time", time);
        resultIntent.putExtra("user",user.getObjectId());
        resultIntent.putStringArrayListExtra("address", address);
        resultIntent.putExtra("addressLat", addressLat);
        resultIntent.putExtra("addressLng",addressLng);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
