package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseQuery;

import java.util.ArrayList;

public class UserSelctionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selction);

        ListView usersList = (ListView)findViewById(R.id.user_selection_list);
        ArrayList<WalkerSearchActivity.ParseUserInfo> users = (ArrayList)getIntent().getSerializableExtra("users");
        usersList.setAdapter(new UserSelectionListAdapter(this,users));
        usersList.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_selction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
