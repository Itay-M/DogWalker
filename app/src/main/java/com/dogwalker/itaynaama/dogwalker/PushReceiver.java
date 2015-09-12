package com.dogwalker.itaynaama.dogwalker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service class which handle push messages and creatign notifications if needed.
 */
public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent.getAction().equals("com.dogwalker.itaynaama.dogwalker.WALKING_REQUEST")) {
            try {
                JSONObject data = new JSONObject(intent.getStringExtra(ParsePushBroadcastReceiver.KEY_PUSH_DATA));

                // take ID of request
                final String reqId = data.getString("reqId");

                // take details of request and of requested user and create push
                ParseQuery<ParseObject> requestQuery = new ParseQuery<>("Requests");
                requestQuery.whereEqualTo("objectId", reqId);
                requestQuery.include("from");
                requestQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject request, ParseException e) {
                        if (e == null && request != null) {
                            createNotification(context, request);
                        } else {
                            Log.e("PushReceiver", "Failed getting request details: " + e.getMessage());
                        }
                    }
                });
            } catch (JSONException e) {
                Log.e("PushReceiver","Failed getting data from push: "+e.getMessage());
            }
        }
    }

    /**
     * Create a notification containing short description of the request. Clicking on the notification
     * will move the user to the @WalkerRequestActivity.
     *
     * This method is public mainly for testing purpose.
     *
     * @param context the context used for the notification
     * @param request the request object. the "from" user is assumed to be already populated.
     */
    static public void createNotification(Context context, ParseObject request){
        // get requesting user
        ParseUser user = request.getParseUser("from");

        // create intent which will be triggered on click and will open the request details activity
        Intent resultIntent = WalkerRequestActivity.prepareIntent(context, request,"from");
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // init notification builder
        android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("DogWalking request")
                .setContentText(user.getUsername() + " want you to take out his/her dog.")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(resultPendingIntent);

        // Sets an ID for the notification (use request objectId)
        int mNotificationId = request.getObjectId().hashCode();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}