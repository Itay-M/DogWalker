package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseGeoPoint;

import java.util.ArrayList;

public class UserSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selction);

        ListView usersList = (ListView)findViewById(R.id.user_selection_list);
        ArrayList<WalkerSearchActivity.ParseUserInfo> users = (ArrayList<WalkerSearchActivity.ParseUserInfo>)getIntent().getSerializableExtra("users");

        ParseGeoPoint pickupLocation = new ParseGeoPoint(getIntent().getDoubleExtra("addressLocationLat",0),getIntent().getDoubleExtra("addressLocationLng",0));

        usersList.setAdapter(new UserSelectionListAdapter(this,users,pickupLocation));
        usersList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       WalkerSearchActivity.ParseUserInfo user = (WalkerSearchActivity.ParseUserInfo) parent.getAdapter().getItem(position);
        Intent i = new Intent();
        // set the user as the result
        i.putExtra("user", user);
        setResult(RESULT_OK, i);
        finish();
    }
}
