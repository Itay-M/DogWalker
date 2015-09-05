package com.dogwalker.itaynaama.dogwalker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class AddressSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static private final int LOCATION_REQUEST = 1000;

    protected AutoCompleteTextView addressText;
    protected ListView addressesListView;
    protected GeocoderAutocompleteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_selection);

        addressesListView = (ListView)findViewById(R.id.lstAddresses);
        addressText = (AutoCompleteTextView)findViewById(R.id.txtAddress);

        adapter = new GeocoderAutocompleteAdapter(this,android.R.layout.simple_list_item_1);
        addressText.setAdapter(adapter);

        addressesListView.setAdapter(adapter);
        addressesListView.setOnItemClickListener(this);


        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // check if the device's gps is turned on
        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // if gps isn't turned on - ask the user if he/she wants to activate it
        if (!gps_enabled) {
            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("enable GPS?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Log.d("My Loggggg", "user canceled");
                        }
                    });
            alertBuilder.show();
        }else {
            // initiate search by current location
            adapter.getFilter().filter(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_address_selection, menu);
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
        Address address = adapter.getAddress(position);
        Intent i = new Intent();
        // set the address as the result
        i.putExtra("address", address);
        setResult(RESULT_OK, i);
        finish();
    }
}
