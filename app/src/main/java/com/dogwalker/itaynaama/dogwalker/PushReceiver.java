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
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.Date;

/**
 * Created by naama on 25/08/2015.
 */
public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction().equals("com.dogwalker.itaynaama.dogwalker.WALKING_REQUEST")) {
            try {
                JSONObject data = new JSONObject(intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA));
                ParseUser reqUser = new ParseUser();
                final String pickupDate = (String)data.get("pickupDate");
                final String pickupTime = (String) data.get("pickupTime");
                final Address pickupAddress = (Address)data.get("pickupAddress");

                reqUser.setObjectId(data.getString("reqUser"));
                reqUser.fetchInBackground(new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if(e==null){
                            createPush(context,parseUser,pickupDate,pickupTime,pickupAddress.getAddressLine(0));
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    static public void createPush(Context context,ParseUser user,String date,String time,String address){
        android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_template_icon_bg)
                        .setContentTitle("DogWalking request")
                        .setContentText(user.getUsername() + " want you to take out his/her dog." +
                                " Pickup details:" +
                                " Address:" + address +
                                " Date:" + date +
                                " Time:" + time +
                                " If you interested his phone for contact: " + user.get("Phone"));

        Intent resultIntent = new Intent(context, WalkerSearchActivity.class);
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
