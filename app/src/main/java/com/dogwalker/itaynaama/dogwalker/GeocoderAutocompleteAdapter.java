package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Autocomplete adapter which fetch suggestion from the Google geocoder service.
 */
public class GeocoderAutocompleteAdapter extends ArrayAdapter<GeocoderAutocompleteAdapter.AddressAutocompleteResult> implements Filterable, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    /**
     * Address autocomplete suggestions Top-Right (NE) limit bounds
     */
    static public final LatLng BOUNDS_TR = new LatLng(33.417551, 35.927429);
    /**
     * Address autocomplete suggestions Bottom-Left (SW) limit bounds
     */
    static public final LatLng BOUNDS_BL = new LatLng(29.527829, 34.021740);

    /**
     * Number of maximum results returned
     */
    static private final int AUTOCOMPLETE_MAX_RESULTS = 20;

    /**
     * The geocoder used to get suggestion based on user query / current location.
     */
    private final Geocoder geocoder;
    /**
     * The filter created by this adapter. Save it for future use instead of recreating over and
     * over.
     */
    private Filter filter;

    /**
     * The last addresses fetched by this adapter. Used for accessing them (mainly) outside of this
     * class.
     */
    private List<AddressAutocompleteResult> lastAddresses;
    /**
     * The google client used to access last known location.
     */
    private final GoogleApiClient googleClient;
    /**
     * The last search query used by this adapter
     */
    private CharSequence lastCconstraint;

    /**
     * Construct a new adapter bound to the given context
     * @param context the context this adapter is bound to
     */
    public GeocoderAutocompleteAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);

        geocoder = new Geocoder(context);
        lastAddresses = Collections.EMPTY_LIST;

        // initialize (and connect) a Google client in order to be able to get last user location
        googleClient = new GoogleApiClient.Builder(context)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleClient.connect();
    }

    public Address getAddress(int position){
       return getItem(position).getAddress();
    }

    @Override
    public int getCount() {
        return lastAddresses.size();
    }

    @Override
    public AddressAutocompleteResult getItem(int position) {
        return lastAddresses.get(position);
    }

    @Override
    public Filter getFilter() {
        if(filter==null) {
            filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    lastCconstraint = constraint;
                    try {
                        // Skip the autocomplete query if no constraints are given.
                        if (constraint != null && constraint.length() > 0) {
                            // search for addresses based on the user query
                            lastAddresses = AddressAutocompleteResult.wrap(
                                    geocoder.getFromLocationName(constraint.toString(), AUTOCOMPLETE_MAX_RESULTS, BOUNDS_BL.latitude,BOUNDS_BL.longitude,BOUNDS_TR.latitude,BOUNDS_TR.longitude));
                            // fill in the filter results
                            results.values = lastAddresses;
                            results.count = lastAddresses.size();
                        }else{
                            // get last known location
                            Location loc = LocationServices.FusedLocationApi.getLastLocation(googleClient);
                            if(loc!=null) {
                                // search for addressed near the location
                                lastAddresses = AddressAutocompleteResult.wrap(geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), AUTOCOMPLETE_MAX_RESULTS));
                                // fill in the filter results
                                results.values = lastAddresses;
                                results.count = lastAddresses.size();
                            }else{
                                Log.d("GeocodeAutocomplete", "Current location is null");
                            }
                        }
                    } catch (IOException e) {
                        Log.e("GeocodeAutocomplete", e.getMessage());
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        // The API returned at least one result, update the data.
                        notifyDataSetChanged();
                    } else {
                        // The API did not return any results, invalidate the data set.
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
        return filter;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("GeocoderAutocomplete","Google Client failed to connect: error #"+connectionResult.getErrorCode());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GeocoderAutocomplete","Google Client CONNECTED");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // filter using the last search query - if it's empty/too short it will use the
                // user location - which is now available
                getFilter().filter(lastCconstraint);
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("GeocoderAutocomplete", "Google client SUSPENDED: "+i);
    }

    /**
     * Simple wrapper class for the Address returned by the Geocoder to implement its toString.
     */
    public static class AddressAutocompleteResult{
        private final Address address;

        public AddressAutocompleteResult(Address a){
            this.address = a;
        }

        public Address getAddress() {
            return address;
        }

        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            for(int i=0;i<=address.getMaxAddressLineIndex();i++) {
                res.append(address.getAddressLine(i));
                if(i<address.getMaxAddressLineIndex())
                    res.append("\n");
            }
            return res.toString();
        }

        public static List<AddressAutocompleteResult> wrap(List<Address> addresses){
            ArrayList<AddressAutocompleteResult> result = new ArrayList<>(addresses.size());
            for(Address address : addresses) {
                if(address.getCountryCode()==null || "IL".equalsIgnoreCase(address.getCountryCode())) {
                    result.add(new AddressAutocompleteResult(address));
                }
            }
            return result;
        }
    }
}
