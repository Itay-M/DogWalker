package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseGeoPoint;

import java.util.List;

/**
 * An adapter which holds a list of users. Each user is displayed with all its details (name,
 * phone, address etc).
 */
public class UserSelectionListAdapter extends ArrayAdapter<WalkerSearchActivity.ParseUserInfo>{

    /**
     * The location to which the distance from the user address should be calculated.
     * If not set, no distance will be presented.
     */
    private final ParseGeoPoint distanceLocation;

    public UserSelectionListAdapter(Context context, List<WalkerSearchActivity.ParseUserInfo> users,ParseGeoPoint distanceLocation) {
        super(context, R.layout.user_row, R.id.result_row_username, users);
        this.distanceLocation = distanceLocation;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        WalkerSearchActivity.ParseUserInfo user = getItem(position);

        // get UI components
        TextView username = (TextView)row.findViewById(R.id.result_row_username);
        TextView address = (TextView)row.findViewById(R.id.result_row_distance);
        ImageView photo = (ImageView)row.findViewById(R.id.result_row_photo);
        TextView age = (TextView)row.findViewById(R.id.result_row_age);
        TextView phone = (TextView)row.findViewById(R.id.result_row_phone);

        // set user name
        username.setText(user.getName());

        // show the profile picture if available
        byte[] p = user.getProfilePicture();
        if(p!=null) {
            Bitmap b = BitmapFactory.decodeByteArray(p, 0, p.length);
            photo.setImageBitmap(b);
        }

        // show the distance to user (if distanceLocation has been set)
        if(distanceLocation!=null) {
            double distance = distanceLocation.distanceInKilometersTo(user.getAddressLocation());
            address.setText((((int) (distance * 10)) / 10.0) + " km");
        }else{
            address.setText("");
        }

        // show the age of user
        age.setText((int)user.getAge()+" years old");

        // show the user phone (if he allow it)
        if(user.isSharePhone()){
            phone.setText(user.getPhone());
        }else{
            phone.setText("");
        }

        return row;
    }
}
