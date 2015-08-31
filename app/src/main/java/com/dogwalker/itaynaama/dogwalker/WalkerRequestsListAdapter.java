package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

/**
 * Created by naama on 31/08/2015.
 */
public class WalkerRequestsListAdapter extends ArrayAdapter<ParseObject> {

    public WalkerRequestsListAdapter(Context context, List<ParseObject> requests) {
        super(context, R.layout.walker_requests_row, R.id.walker_request_row_name, requests);
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

        // fill in row views
        nameText.setText(request.getParseUser("from").getString("Name"));
        addressText.setText(Utils.addressToString(request.getJSONArray("address"), ", "));
        dateText.setText(WalkerSearchActivity.DISPLAY_DATE_FORMAT.format(request.getDate("datePickup")));
        timeText.setText(WalkerSearchActivity.DISPLAY_TIME_FORMAT.format(new Date(request.getInt("timePickup")*60*1000)));

        // TODO Image

        return row;
    }
}
