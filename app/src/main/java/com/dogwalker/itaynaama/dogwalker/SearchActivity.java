package com.dogwalker.itaynaama.dogwalker;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class SearchActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView t;
    private LocationRequest mlocationRequest;

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
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        t = (TextView)findViewById(R.id.latitudeTextView);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        // Connected to Google Play services!
        // The good stuff goes here.

        mlocationRequest = LocationRequest.create();
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setInterval(10000);//update location every 10 second.

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mlocationRequest,this);
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
        Log.d("My Loggggg", "faillllll");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        Log.d("My Loggggg", "latitude-" + location.toString());
        t.setText(String.valueOf(location.toString()));

    }
}
