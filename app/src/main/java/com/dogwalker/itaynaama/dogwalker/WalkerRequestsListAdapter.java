package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by naama on 31/08/2015.
 */
public class WalkerRequestsListAdapter extends ArrayAdapter<ParseObject> {

    /**
     * the key of the user object which his details should be presented
     */
    protected String userKey;

    public WalkerRequestsListAdapter(Context context, List<ParseObject> requests, String userKey) {
        super(context, R.layout.walker_request_row, R.id.walker_request_row_name, requests);
        this.userKey = userKey;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        // get walker request
        ParseObject request = getItem(position);

        TextView nameText = (TextView)row.findViewById(R.id.walker_request_row_name);
        TextView addressText = (TextView)row.findViewById(R.id.walker_request_row_address);
        TextView dateText = (TextView)row.findViewById(R.id.walker_request_row_date);
        TextView timeText = (TextView)row.findViewById(R.id.walker_request_row_time);
        ImageView pictureView = (ImageView)row.findViewById(R.id.walker_request_row_image);

        // fill in row views
        nameText.setText(request.getParseUser(userKey).getString("name"));
        addressText.setText(Utils.addressToString(request.getJSONArray("address"), ", "));
        dateText.setText(Utils.DISPLAY_DATE_FORMAT.format(request.getDate("datePickup")));
        timeText.setText(Utils.formatMinutesAsTime(request.getInt("timePickup")));

        // show image profile of user
        ParseFile p = request.getParseUser(userKey).getParseFile("photo");
        try
        {
            if(p != null) {
                Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
                    pictureView.setImageBitmap(b);
            }
        }
        catch (ParseException e)
        {
            Log.d("My Loggggg", e.getMessage().toString());
        }

        // set background on requests that not read
        if(request.getBoolean("isRead")!=Boolean.TRUE){
            row.setBackgroundColor(getContext().getResources().getColor(R.color.request_not_read));
        }

        row.setTag(userKey);

        return row;
    }
}
