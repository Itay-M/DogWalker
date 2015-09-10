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
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

/**
 * Created by naama on 22/08/2015.
 */
public class UserSelectionListAdapter extends ArrayAdapter<WalkerSearchActivity.ParseUserInfo>{

    private ParseGeoPoint pickUpLocation;

    public UserSelectionListAdapter(Context context, List<WalkerSearchActivity.ParseUserInfo> users,ParseGeoPoint pickupLocation) {
        super(context, R.layout.result_walker_search_row, R.id.result_row_username, users);
        this.pickUpLocation = pickupLocation;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        WalkerSearchActivity.ParseUserInfo user = getItem(position);

        TextView username = (TextView)row.findViewById(R.id.result_row_username);
        TextView address = (TextView)row.findViewById(R.id.result_row_distance);
        ImageView photo = (ImageView)row.findViewById(R.id.result_row_photo);
        TextView age = (TextView)row.findViewById(R.id.result_row_age);
        TextView phone = (TextView)row.findViewById(R.id.result_row_phone);

        username.setText(user.getUsername());

        //show the profile picture of available user if not exist showed default photo
        byte[] p = user.getProfilePicture();
        if(p!=null) {
            Bitmap b = BitmapFactory.decodeByteArray(p, 0, p.length);
            photo.setImageBitmap(b);
        }

        // calculate distance from pickup address to available user
        double distance = pickUpLocation.distanceInKilometersTo(user.getAddressLocation());
        address.setText((((int) (distance * 10)) / 10.0) + " km");

        // show age of user
        age.setText((((int)(user.getAge()*10))/10.0)+" years old");

        //show phone of user if his want to expose it
        if(user.isSharePhone()){
            phone.setText(user.getPhone());
        }else{
            phone.setText("");
        }

        return row;


    }
}
