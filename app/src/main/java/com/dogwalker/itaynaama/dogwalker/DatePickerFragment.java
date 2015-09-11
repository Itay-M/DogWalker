package com.dogwalker.itaynaama.dogwalker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

/**
 * A dialog fragment which let the user to select a date.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    /**
     * A listener which will be notified when the user selected a date.
     */
    private DatePickerListener listener;

    public DatePickerFragment(){
        super();

        Bundle arguments = new Bundle();
        arguments.putSerializable("date",Calendar.getInstance());
        setArguments(arguments);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // backward compatibility - check if the activity implement the lisener interface and use it
        // as a listener
        if(activity instanceof DatePickerListener && listener==null)
            listener = (DatePickerListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // get current date
        Calendar date = (Calendar)getArguments().getSerializable("date");

        // Use the current date as the default date in the picker
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // build the selected date
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        // update fragment date
        getArguments().putSerializable("date",date);
        // notify listener (if any)
        if(listener!=null) {
            listener.onDateSelected(date);
        }
    }

    /**
     * Simple interface to allow initiator of @DatePickerFragment to be notified when a date is
     * being selected.
     */
    public interface DatePickerListener{
        /**
         * Being called when a date is being selected.
         *
         * @param date the selected date
         */
        void onDateSelected(Calendar date);
    }
}