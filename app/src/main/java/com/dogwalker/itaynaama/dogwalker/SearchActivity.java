package com.dogwalker.itaynaama.dogwalker;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class SearchActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


//        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
    public void onConnected(Bundle bundle)
    {
        // Connected to Google Play services!
        // The good stuff goes here.


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (mLastLocation != null)
//        {

        Log.d("My Loggggg", "latitude-" + mLastLocation.getLatitude());
            t = (TextView)findViewById(R.id.latitudeTextView);
            t.setText(String.valueOf(mLastLocation.getLatitude()));


//            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the 'Handle Connection Failures' section.
    }

}
