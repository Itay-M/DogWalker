package com.dogwalker.itaynaama.dogwalker;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by naama on 19/08/2015.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handle action bar item clicks.
     * @param item - menu item.
     * @return true if item was clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch(item.getItemId())
        {
            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
