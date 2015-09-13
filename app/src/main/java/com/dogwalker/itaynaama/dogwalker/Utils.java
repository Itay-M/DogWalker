package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Address;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Utility class
 */
public class Utils {

    /**
     * The common date format used in the application
     */
    public static final java.text.DateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    /**
     * The common time format used in the application
     */
    public static final java.text.DateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("HH:mm");

    /**
     * Convert address object to string presentation using a default separator (comma followed by a new line).
     *
     * @param addr the address to convert
     * @return the converted address string presentation
     */
    static public String addressToString(Address addr){
        return addressToString(addr, ",\n");
    }

    /**
     * Convert address object to string presentation using the given separator between the address lines.
     * @param addr the address to convert
     * @param sep the separator to use between address lines
     * @return the converted address string presentation
     */
    static public String addressToString(Address addr,String sep){
        StringBuilder res = new StringBuilder();
        for(int i=0;i<=addr.getMaxAddressLineIndex();i++){
            res.append(addr.getAddressLine(i));
            if(i<addr.getMaxAddressLineIndex()){
                res.append(sep);
            }
        }

        return res.toString();
    }

    /**
     * Convert the address - given as JSONArray of lines - to a string presentation. The address
     * lines will be separated by a comma and a new line
     *
     * @param addr the address lines
     * @return the address as string
     */
    static public String addressToString(JSONArray addr){
        return addressToString(addr, ",\n");
    }

    /**
     * Convert the address - given as JSONArray of lines - to a string presentation. The address
     * lines will be separated by the given separator string.
     *
     * @param addr the address lines
     * @return the address as string
     */
    static public String addressToString(JSONArray addr,String sep){
        if(addr==null) return "";

        /*StringBuilder res = new StringBuilder();
        for(int i=0;i<addr.length();i++){
            try {
                res.append(addr.getString(i));
            } catch (JSONException e) {}
            if(i<(addr.length()-1)){
                res.append(sep);
            }
        }
        return res.toString();*/

        try {
            return addr.join(sep);
        } catch (JSONException e) {
            return "";
        }
    }

    /**
     * Convert an address object into a JSONArray of lines.
     *
     * @param address the address to convert
     * @return a JSONArray with the address lines
     */
    static public JSONArray addressToJSONArray(Address address){
        JSONArray addressLines = new JSONArray();
        if(address!=null) {
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressLines.put(address.getAddressLine(i));
            }
        }
        return addressLines;
    }

    /**
     * Show a cancelable message dialog with the given title and mesasge. The message will only
     * have one single "OK" button.
     *
     * @param context the context to be used in order to display the dialog
     * @param title the title of the message dialog
     * @param msg the content of the message dialog
     */
    static public void showMessageBox(Context context,String title, String msg){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(msg);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    /**
     * Convert the given time in minutes to a string presentation by the common format.
     *
     * @param minutes the time to convert in minutes
     * @return the time as string presentation
     */
    static public String formatMinutesAsTime(int minutes){
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, minutes / 60);
        time.set(Calendar.MINUTE, minutes % 60);
        return DISPLAY_TIME_FORMAT.format(time.getTime());
    }

    /**
     * Send a reset password mail to the user. The user will be asked to fill his email address for
     * identification.
     *
     * @param context the context used to show messages
     */
    static public void resetPassword(final Context context){
        final android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
        final EditText input = new EditText(context);

        alertBuilder
                .setMessage("Fill in you email address.\nA link to reset your password will be sent to you email address.")
                .setPositiveButton("Send Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetPassword(context, input.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("My Loggggg", "user canceled reset");
                    }
                });
        alertBuilder.setView(input);
        alertBuilder.show();
    }

    /**
     * Send a reset password mail to the user. If provided with an @email address it will search
     * for a user having this mail address and if found will send it a reset password mail. If no
     * @email is provided the user will be asked to enter one.
     *
     * @param context the context used to show messages
     * @param email the email address of the user (optional)
     */
    static public void resetPassword(final Context context, String email){
        //send the reset to the mail, if not successful print exception.
        ParseUser.requestPasswordResetInBackground(email.toLowerCase(), new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(context, "Reset password link sent successfully to your email", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("ResetPassword", "Reset password failed: " + e.getMessage());
                    Utils.showMessageBox(context, "Reset Password Failed", context.getString(R.string.unknown_error_occur));
                }
            }
        });
    }

    /**
     * convert a rectangle picture into a circle picture
     * @param bitmap - the picture
     * @return a rounded bitmap picture
     */
    static public Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
