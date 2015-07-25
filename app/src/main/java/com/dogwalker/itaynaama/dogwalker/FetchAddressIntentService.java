package com.dogwalker.itaynaama.dogwalker;

import android.app.IntentService;
import android.content.Intent;
import android.location.Geocoder;

import java.util.Locale;

/**
 * Created by iTay on 25-Jul-15.
 */
public class FetchAddressIntentService extends IntentService
{
    public FetchAddressIntentService()
    {
        super("name");
    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //passing a Locale in the Geocoder constructor is to adjust the location presentation for the user region.
        //geocoder is used for transforming gps locations to addresses or vise versa.
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
    }
}
