package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

/**
 * Created by naama on 22/08/2015.
 */
public class UserSelectionListAdapter extends ArrayAdapter<WalkerSearchActivity.ParseUserInfo>{


    public UserSelectionListAdapter(Context context, List<WalkerSearchActivity.ParseUserInfo> users) {
        super(context, R.layout.result_walker_search_row,R.id.result_row_username, users);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        WalkerSearchActivity.ParseUserInfo user = getItem(position);

        TextView username = (TextView)row.findViewById(R.id.result_row_username);
        TextView address = (TextView)row.findViewById(R.id.result_row_address);

        username.setText(user.getUsername());
        address.setText(user.getAddress());
        return row;
    }
}
