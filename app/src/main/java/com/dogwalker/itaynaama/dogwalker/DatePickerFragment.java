package com.dogwalker.itaynaama.dogwalker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by naama on 28/08/2015.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DatePickerListener listener;
    private Calendar date;

    public DatePickerFragment(){
        date = Calendar.getInstance();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(!(activity instanceof DatePickerListener)){
            throw new RuntimeException("Activity must implement DatePickerListener interface");
        }

        listener = (DatePickerListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        date.set(year, month, day);
        listener.onDateSelected(date);
    }

    public Calendar getDate(){
        return date;
    }

    public interface DatePickerListener{
        void onDateSelected(Calendar date);
    }
}