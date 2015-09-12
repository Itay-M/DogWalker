package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

/**
 * Activity which allow the user to select an address. The address can be auto complete by the
 * user location - if available - or by a search query. The user must select a single address from
 * the suggestion.
 * This activity will return the result of the selected address to its caller.
 */
public class AddressSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /**
     * The autocomplete adapter. The adapter will search for autocomplete suggestion from Google
     * geocoder and will store the results until the search query will be changed.
     */
    protected GeocoderAutocompleteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_selection);

        ListView addressesListView = (ListView)findViewById(R.id.lstAddresses);
        AutoCompleteTextView addressText = (AutoCompleteTextView)findViewById(R.id.txtAddress);

        // create the Google geocoder autocomplete adapter
        adapter = new GeocoderAutocompleteAdapter(this);
        // link the adapter to the AutocompleteTextView
        addressText.setAdapter(adapter);

        // list the adapter to the results list
        addressesListView.setAdapter(adapter);
        // register list listener to handle item click
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

                        }
                    });
            alertBuilder.show();
        }else {
            // initiate search by current location
            adapter.getFilter().filter(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // take the selected item's address, set it as a result and finish the activity
        Address address = adapter.getAddress(position);
        Intent i = new Intent();
        i.putExtra("address", address);
        setResult(RESULT_OK, i);
        finish();
    }
}
