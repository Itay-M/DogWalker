package com.dogwalker.itaynaama.dogwalker;

import android.location.Address;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by naama on 31/08/2015.
 */
public class Utils {
    /**
     * Convert address object to string presentation using a default separator (comma followed by a new line).
     *
     * @param addr the address to convert
     * @return the converted address string presentation
     */
    static public String addressToString(Address addr){
        return addressToString(addr,",\n");
    }

    /**
     * Convert address object to string presentation using the given separator between the address lines.
     * @param addr the address to convert
     * @param sep the separator to use between address lines
     * @return the converted address string presentation
     */
    static public String addressToString(Address addr,String sep){
        StringBuilder res = new StringBuilder();
        for(int i=0;i<=addr.getMaxAddressLineIndex();i++){
            res.append(addr.getAddressLine(i));
            if(i<addr.getMaxAddressLineIndex()){
                res.append(sep);
            }
        }
        return res.toString();
    }

    static public String addressToString(JSONArray addr){
        return addressToString(addr,",\n");
    }
    static public String addressToString(JSONArray addr,String sep){
        if(addr==null) return "";

        StringBuilder res = new StringBuilder();
        for(int i=0;i<addr.length();i++){
            try {
                res.append(addr.getString(i));
            } catch (JSONException e) {}
            if(i<(addr.length()-1)){
                res.append(sep);
            }
        }
        return res.toString();
    }

    static public JSONArray addressToJSONArray(Address address){
        JSONArray addressLines = new JSONArray();
        for (int i=0; i<=address.getMaxAddressLineIndex();i++){
            addressLines.put(address.getAddressLine(i));
        }
        return addressLines;
    }

    static public Adapter getActualListAdapter(ListAdapter adapter){
        if(adapter instanceof WrapperListAdapter)
            return getActualListAdapter(((WrapperListAdapter)adapter).getWrappedAdapter());
        return adapter;
    }
}
