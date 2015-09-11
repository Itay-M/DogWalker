package com.dogwalker.itaynaama.dogwalker;

import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.Arrays;

/**
 * A simple class holds data about a user availability. User availability combined from a start
 * time, end time and the days which are valid for this time period.
 * This class is used to store, display and edit ParseObject of class 'UserAvailability'.
 */
public class AvailabilityRecord {
    /**
     * start time of the availability period
     */
    private int timeFrom;
    /**
     * end time of the availability period
     */
    private int timeUntil;
    /**
     * boolean for each day of the week. Each day relevant for this availbility period is marked as
     * "true".
     */
    private boolean[] days = new boolean[7];
    /**
     * Object that include extra data on this record. For external use to identify records across
     * application use.
     */
    private Object tag;

    public int getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(int timeFrom) {
        this.timeFrom = timeFrom;
    }

    public int getTimeUntil() {
        return timeUntil;
    }

    public void setTimeUntil(int timeUntil) {
        this.timeUntil = timeUntil;
    }

    public void setDay(int dayOfWeek, boolean selected){
        days[dayOfWeek] = selected;
    }

    public boolean isDaySelected(int dayOfWeek){
        return days[dayOfWeek];
    }

    public boolean isValid(){
        return timeFrom<timeUntil && !Arrays.equals(days, new boolean[7]);
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    /**
     * Convert this object to UserAvailability ParseObject
     *
     * @return a new ParseObject from class 'UserAvailability'
     */
    public ParseObject toParseObject(){
        return toParseObject(new ParseObject("UserAvailability"));
    }

    /**
     * Fill the detailed of this record in the given ParseObject. The given ParseObject is assumed
     * to be type of 'UserAvailability' class.
     *
     * @param o the ParseObject to fill the data with
     * @return the given ParseObject - for chaining.
     */
    public ParseObject toParseObject(ParseObject o){
        if(!o.getClassName().equals("UserAvailability")){
            throw new RuntimeException("ParseObject argument should be from 'UserAvailability' class");
        }

        o.put("startTime", timeFrom);
        o.put("endTime", timeUntil);
        JSONArray arrDays = new JSONArray();
        for (int i=0;i<days.length;i++){
            if(days[i]) {
                arrDays.put(i+1);
            }
        }
        o.put("days", arrDays);

        return o;
    }
}