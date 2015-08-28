package com.dogwalker.itaynaama.dogwalker;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by naama on 14/08/2015.
 */
public class GeocoderAutocompleteAdapter extends ArrayAdapter<GeocoderAutocompleteAdapter.AddressAutocompleteResult> implements Filterable {

    /**
     * Address autocomplete suggestions limit bounds
     */
    static private final double[] BOUNDS_TR = {33.417551, 35.927429};
    static private final double[] BOUNDS_BL = {29.527829, 34.021740};

    static private final int AUTOCOMPLETE_MAX_RESULTS = 20;

    private final Geocoder geocoder;
    private Filter filter;
    private List<AddressAutocompleteResult> lastAddresses;

    public GeocoderAutocompleteAdapter(Context context, int resource) {
        super(context, resource);

        geocoder = new Geocoder(context);
        lastAddresses = Collections.EMPTY_LIST;
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

                    // Skip the autocomplete query if no constraints are given.
                    if (constraint != null && constraint.length() > 0) {
                        try {
                            // TODO: limit bounds to Israel only
                            lastAddresses = AddressAutocompleteResult.wrap(
                                    geocoder.getFromLocationName(constraint.toString(), AUTOCOMPLETE_MAX_RESULTS, BOUNDS_BL[0],BOUNDS_BL[1],BOUNDS_TR[0],BOUNDS_TR[1]));
                            results.values = lastAddresses;
                            results.count = lastAddresses.size();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
