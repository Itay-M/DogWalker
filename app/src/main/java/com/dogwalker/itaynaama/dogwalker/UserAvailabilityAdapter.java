package com.dogwalker.itaynaama.dogwalker;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter which holds a list of user availabilities. The adapter is responsible for generating the
 * user availability row which provide option for the user to define an hour range and a set of days
 * which together form an AvailabilityRecord record.
 */
public class UserAvailabilityAdapter extends ArrayAdapter<AvailabilityRecord> implements View.OnClickListener {
    /**
     * Time picker fragment used to select the start and end time of each record
     */
    private final TimePickerFragment timePicker = new TimePickerFragment();

    public UserAvailabilityAdapter(FragmentActivity context){
        super(context,R.layout.user_availablity_row,R.id.availability_row_from);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        final AvailabilityRecord record = getItem(position);

        // init start time
        TextView fromTextView = (TextView)row.findViewById(R.id.availability_row_from);
        fromTextView.setText(Utils.formatMinutesAsTime(record.getTimeFrom()));
        fromTextView.setTag(record);
        fromTextView.setOnClickListener(this);

        // init end time
        TextView untilTextView = (TextView)row.findViewById(R.id.availability_row_until);
        untilTextView.setText(Utils.formatMinutesAsTime(record.getTimeUntil()));
        untilTextView.setTag(record);
        untilTextView.setOnClickListener(this);

        // add record remove button in all records except the last
        ImageView removeButton = (ImageView)row.findViewById(R.id.availability_row_remove);
        removeButton.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
        removeButton.setTag(record);
        removeButton.setOnClickListener(this);

        // add record add button in the last record
        ImageView addButton = (ImageView)row.findViewById(R.id.availability_row_add);
        addButton.setVisibility(position == getCount() - 1 ? View.VISIBLE : View.GONE);
        addButton.setTag(record);
        addButton.setOnClickListener(this);

        // set days checksboxes
        for(int i=0;i<7;i++) {
            CheckBox chk = (CheckBox) row.findViewById(getContext().getResources().getIdentifier("availability_row_day"+(i+1),"id",getClass().getPackage().getName()));
            chk.setChecked(record.isDaySelected(i));
            chk.setTag(i);
            chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // mark days are checked
                    record.setDay((Integer)buttonView.getTag(),isChecked);
                }
            });
        }
        return row;
    }

    @Override
    public void onClick(final View v) {
        final AvailabilityRecord record = (AvailabilityRecord)v.getTag();

        switch(v.getId()){
            // remove button
            case R.id.availability_row_remove:
                remove(record);
                notifyDataSetChanged();
                break;
            // add button
            case R.id.availability_row_add:
                if(record.isValid()) {
                    add(new AvailabilityRecord());
                    notifyDataSetChanged();
                }else{
                    Utils.showMessageBox(v.getContext(),"Invalid period","Please select at least 1 day and make sure the start time is before the end time.");
                }
                break;
            // start time clicked - popup a time picker fragment
            case R.id.availability_row_from:
                timePicker.setDefaultTime(record.getTimeFrom());
                timePicker.setListener(new TimePickerFragment.TimePickerListener() {
                    @Override
                    public void onTimeSelected(int time) {
                        record.setTimeFrom(time);
                        notifyDataSetChanged();
                    }
                });
                timePicker.show(((FragmentActivity)getContext()).getSupportFragmentManager(),"timePicker");
                break;
            // end time clicked - popup a time picker fragment
            case R.id.availability_row_until:
                timePicker.setDefaultTime(record.getTimeUntil());
                timePicker.setListener(new TimePickerFragment.TimePickerListener() {
                    @Override
                    public void onTimeSelected(int time) {
                        record.setTimeUntil(time);
                        notifyDataSetChanged();
                    }
                });
                timePicker.show(((FragmentActivity)getContext()).getSupportFragmentManager(),"timePicker");
                break;
        }
    }
}