package com.dogwalker.itaynaama.dogwalker;

import android.app.Application;

import com.parse.Parse;


public class DogWalkerApp extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        //initialize parse for using data control.
        Parse.initialize(this,getString(R.string.ApplicationID),getString(R.string.ClientKey));
    }
}
