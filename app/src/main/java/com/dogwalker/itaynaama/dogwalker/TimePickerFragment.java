package com.dogwalker.itaynaama.dogwalker;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * A fragment showing a time selection dialog (hour & minutes). The fragment provide a way for it's initiator to receive
 * the selected time.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    /**
     * The listener which will be notified when a time was selected.
     */
    private TimePickerListener listener;

    /**
     * Set the time that will be shown when the dialog is created. By default the first time that
     * being used is the current time, and later the last time selected is used.
     *
     * @param time the default time in minutes
     */
    public void setDefaultTime(int time){
        if(getArguments()==null){
            setArguments(new Bundle());
        }
        getArguments().putInt("time", time);
    }

    /**
     * Get the default time (in minutes) to show when the dialog created.
     *
     * @return the default time (in minutes)
     */
    protected int getDefaultTime(){
        int d = (getArguments()==null?-1:getArguments().getInt("time",-1));
        if(d<0){
            Calendar now = Calendar.getInstance();
            d = now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE);
        }
        return d;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // backward compatibility - check if the activity implement the listener interface and use it
        // as a listener
        if(activity instanceof TimePickerListener && listener==null)
            listener = (TimePickerListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // get current selected time (in minutes)
        int time = getDefaultTime();

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, time/60, time%60, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int time = hourOfDay*60+minute;
        getArguments().putInt("time",hourOfDay*60+minute);
        if(listener!=null) {
            listener.onTimeSelected(time);
        }
    }

    public void setListener(TimePickerListener listener) {
        this.listener = listener;
    }

    public interface TimePickerListener{
        void onTimeSelected(int time);
    }

}