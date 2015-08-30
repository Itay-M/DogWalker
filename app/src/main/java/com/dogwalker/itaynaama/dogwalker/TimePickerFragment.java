package com.dogwalker.itaynaama.dogwalker;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by naama on 28/08/2015.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private TimePickerListener listener;
    private Calendar time;

    public TimePickerFragment(){
        time = Calendar.getInstance();
        time.set(Calendar.SECOND,0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(!(activity instanceof TimePickerListener)){
            throw new RuntimeException("Activity must implement TimePickerListener interface");
        }

        listener = (TimePickerListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        time.set(Calendar.HOUR_OF_DAY,hourOfDay);
        time.set(Calendar.MINUTE, minute);
        listener.onTimeSelected(time);
    }

    public interface TimePickerListener{
        void onTimeSelected(Calendar time);
    }

}

