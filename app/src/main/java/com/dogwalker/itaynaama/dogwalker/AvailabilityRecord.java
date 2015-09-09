package com.dogwalker.itaynaama.dogwalker;

import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.Arrays;

/**
 * Created by naama on 08/09/2015.
 */
public class AvailabilityRecord {
    private int timeFrom;
    private int timeUntil;
    private boolean[] days = new boolean[7];
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
    public ParseObject toParseObject(){
        return toParseObject(new ParseObject("UserAvailability"));
    }

    public ParseObject toParseObject(ParseObject record){
        record.put("startTime", timeFrom);
        record.put("endTime", timeUntil);
        JSONArray arrDays = new JSONArray();
        for (int i=0;i<days.length;i++){
            if(days[i]) {
                arrDays.put(i+1);
            }
        }
        record.put("days",arrDays);

        return record;
    }
}
