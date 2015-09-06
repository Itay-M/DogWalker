package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Calendar;


public class Register extends AppCompatActivity implements View.OnClickListener
{
    private static final int CAMERA_REQUEST = 0;
    private static final int REQUEST_ADDRESS = 1;
    private boolean pictureTaken = false;
    protected Button registerB,pictureB,addressB;
    protected ImageView newProfilePicIV;
    protected EditText nameET,usernameET,emailET,passwordET,phoneET,addressET;
    protected Address address;
    SharedPreferences userPref;
    ParseFile photoFile;
    byte[] picByteArray;
    Bitmap bmPic;
    private UserAvailabilityAdapter availabilityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        newProfilePicIV = (ImageView) findViewById(R.id.newProfilePicView);

        //Buttons
        registerB = (Button) findViewById(R.id.registerButton);
        pictureB = (Button) findViewById(R.id.pictureButton);

        //editTexts
        nameET = (EditText)findViewById(R.id.regNameEditText);
        usernameET = (EditText)findViewById(R.id.regUsernameEditText);
        emailET = (EditText)findViewById(R.id.regEmailEditText);
        addressB = (Button)findViewById(R.id.regAddressButton);
        addressET = (EditText)findViewById(R.id.regAddressEditText);
        passwordET = (EditText)findViewById(R.id.regPasswordEditText);
        phoneET = (EditText)findViewById(R.id.regPhoneEditText);

        // availability
        availabilityAdapter = new UserAvailabilityAdapter(this);
        final LinearLayout availabilityItems = (LinearLayout)findViewById(R.id.register_availability_items);
        availabilityAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                availabilityItems.removeAllViews();
                for (int i = 0; i < availabilityAdapter.getCount(); i++) {
                    View row = availabilityAdapter.getView(i, null, availabilityItems);
                    availabilityItems.addView(row);
                }
            }

            @Override
            public void onInvalidated() {
                availabilityItems.removeAllViews();
            }
        });
        availabilityAdapter.notifyDataSetChanged();

        //onClick listeners
        registerB.setOnClickListener(this);
        pictureB.setOnClickListener(this);
        addressB.setOnClickListener(this);

        userPref = PreferenceManager.getDefaultSharedPreferences(this);

        //set the "GO" button in the keyboard to hide the keyboard
        emailET.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    //hide the keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });
    }

    static private class UserAvailabilityAdapter extends ArrayAdapter<AvailabilityRecord> implements View.OnClickListener {
        private final TimePickerFragment timePicker = new TimePickerFragment();

        public UserAvailabilityAdapter(FragmentActivity context){
            super(context,R.layout.user_availablity_row,R.id.availability_row_from);
            add(new AvailabilityRecord());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);

            final AvailabilityRecord record = getItem(position);


            TextView fromTextView = (TextView)row.findViewById(R.id.availability_row_from);
            fromTextView.setText(Utils.formatMinutesAsTime(record.getTimeFrom()));
            fromTextView.setTag(record);
            fromTextView.setOnClickListener(this);

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

            for(int i=0;i<7;i++) {
                CheckBox chk = (CheckBox) row.findViewById(getContext().getResources().getIdentifier("availability_row_day"+(i+1),"id",getClass().getPackage().getName()));
                chk.setChecked(record.isDaySelected(i));
                chk.setTag(i);
                chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        record.setDay((Integer)buttonView.getTag(),isChecked);
                    }
                });
            }
            return row;
        }

        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.availability_row_remove:
                    remove((AvailabilityRecord)v.getTag());
                    notifyDataSetChanged();
                    break;
                case R.id.availability_row_add:
                    AvailabilityRecord record = (AvailabilityRecord)v.getTag();
                    if(record.isValid()) {
                        add(new AvailabilityRecord());
                        notifyDataSetChanged();
                    }else{
                        Utils.showMessageBox(v.getContext(),"Invalid period","Please select at least 1 day and make sure the start time is before the end time.");
                    }
                    break;
                case R.id.availability_row_from:
                    timePicker.setListener(new TimePickerFragment.TimePickerListener() {
                        @Override
                        public void onTimeSelected(Calendar time) {
                            AvailabilityRecord record = (AvailabilityRecord)v.getTag();
                            record.setTimeFrom(time.get(Calendar.HOUR_OF_DAY)*60+time.get(Calendar.MINUTE));
                            notifyDataSetChanged();
                        }
                    });
                    timePicker.show(((FragmentActivity)getContext()).getSupportFragmentManager(),"timePicker");
                    break;
                case R.id.availability_row_until:
                    timePicker.setListener(new TimePickerFragment.TimePickerListener() {
                        @Override
                        public void onTimeSelected(Calendar time) {
                            AvailabilityRecord record = (AvailabilityRecord) v.getTag();
                            record.setTimeUntil(time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE));
                            notifyDataSetChanged();
                        }
                    });
                    timePicker.show(((FragmentActivity)getContext()).getSupportFragmentManager(),"timePicker");
                    break;
            }
        }

    }

    private static class AvailabilityRecord{
        private int timeFrom;
        private int timeUntil;
        private boolean[] days = new boolean[7];

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
            return timeFrom<timeUntil && !Arrays.equals(days,new boolean[7]);
        }

        public ParseObject toParseObject(){
            ParseObject record = new ParseObject("UserAvailability");
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


    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case(R.id.registerButton):

                //new user creation.
                final ParseUser user = new ParseUser();

                //retrieve data from the EditText objects and place it as the user data.
                user.setUsername(usernameET.getText().toString());
                user.setPassword(passwordET.getText().toString());
                user.setEmail(emailET.getText().toString().toLowerCase());
                user.put("Name", nameET.getText().toString());
                user.put("address", Utils.addressToJSONArray(address));
                user.put("Phone", phoneET.getText().toString());
                user.put("addressLocation",new ParseGeoPoint(address.getLatitude(),address.getLongitude()));

                //if the new user took a profile picture - save it to parse data base
                Log.d("My Loggggg", String.valueOf(pictureTaken));
                if (pictureTaken)
                {
                    Toast.makeText(getApplicationContext(), "saving photo", Toast.LENGTH_LONG).show();
                    newProfilePicIV.setImageBitmap(bmPic);
                    photoFile = new ParseFile(usernameET.getText().toString()+"profile_pic.jpg",picByteArray);

                    try
                    {
                        photoFile.save();
                    }
                    catch (ParseException e)
                    {
                        Log.d("My Loggggg", e.getMessage().toString());
                    }
                    user.put("Photo", photoFile);
                    user.saveInBackground();
                }


                //register the new user in Parse database.
                user.signUpInBackground(new SignUpCallback()
                {
                    public void done(final ParseException e)
                    {
                        if (e == null)
                        {
                            // add user availability and place it in parse
                            for(int i=0;i<availabilityAdapter.getCount()-1;i++){
                                ParseObject availability = availabilityAdapter.getItem(i).toParseObject();
                                availability.put("user",user);
                                availability.saveInBackground();
                            }

                            // Hooray! the user created successfully.
                            // create and prompt alert for the successful user creation.
                            final AlertDialog alert = new AlertDialog.Builder(Register.this).create();
                            alert.setTitle("New User");//set the alert title.
                            alert.setMessage("User created successfully");//set the alert message.
                            alert.setCancelable(true);//set the back button to exit the alert.
                            alert.setButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alert.cancel();//exit the alert.
                                    //save the fact that there is a current user.
                                    SharedPreferences.Editor editor = userPref.edit();
                                    editor.putBoolean("USEREXISTS",true).commit();
                                    //go to main activity.
                                    Intent registerIntent = new Intent(Register.this, MainActivity.class);
                                    startActivity(registerIntent);
                                    finish();
                                }
                            });



                            alert.show();
                        }
                        else
                        {
                            // TODO how to know what the problem to show to user
                            // Sign up didn't succeed, Look at the ParseException to figure out what went wrong.
                            //in addition prompt alert for the unsuccessful registration.
                            Log.d("My Loggggg", e.getMessage().toString());
                            final AlertDialog alert = new AlertDialog.Builder(Register.this).create();
                            alert.setTitle("New User Error");//set the alert title.
                            alert.setMessage("User creation failed!");//set the alert message.
                            alert.setCancelable(true);//set the back button to exit the alert.
                            alert.setButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {

                                    alert.cancel();//exit the alert.
                                    finish();
                                    startActivity(getIntent());
                                }
                            });

                            alert.show();
                        }
                    }
                });

                break;

            case (R.id.pictureButton):
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);

                break;

            case (R.id.regAddressButton):
                Intent addressSelectionIntent = new Intent(Register.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST)
        {
            pictureTaken = true;

            //get the photo
            bmPic = (Bitmap) data.getExtras().get("data");

            //convert and scale the photo
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmPic.compress(Bitmap.CompressFormat.JPEG,100,stream);

            //put the photo in byte array
            picByteArray = stream.toByteArray();
            //present the photo that is going to be save in circle view
            newProfilePicIV.setImageBitmap(getCircleBitmap(bmPic));

        }else if(resultCode == RESULT_OK && requestCode == REQUEST_ADDRESS){

            Address address = data.getParcelableExtra("address");
            addressET.setText(Utils.addressToString(address));
            this.address = address;
        }


    }

    /**
     * convert a rectangle picture into a circle picture
     * @param bitmap - the picture
     * @return a rounded bitmap picture
     */
    private Bitmap getCircleBitmap(Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
