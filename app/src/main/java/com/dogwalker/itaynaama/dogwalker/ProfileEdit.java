package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Address;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ProfileEdit extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_REQUEST = 0;
    private static final int REQUEST_ADDRESS = 1;
    protected EditText nameEdit,addressEdit,phoneEdit;
    protected Button resetPassword, saveChangesB, changePicB;
    protected ImageView curruserPic;
    protected ParseUser currentUser = ParseUser.getCurrentUser();
    protected UserAvailabilityAdapter availabilityAdapter;
    protected List<ParseObject> userAvailabilitiesList;
    protected ParseFile photoFile;
    protected byte[] picByteArray;
    protected Bitmap bmPic;
    protected Address address;
    protected Switch phoneShareEdit;
    protected Boolean shareSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        curruserPic = (ImageView)findViewById(R.id.view_curr_pic);

        //show profile picture in the imageView
        ParseFile p = (ParseFile) currentUser.get("Photo");
        if(p!=null) {
            try {
                Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
                curruserPic.setImageBitmap(b);
            } catch (ParseException e) {
                Log.d("My Loggggg", e.getMessage().toString());
            }
        }

        nameEdit = (EditText) findViewById(R.id.profile_name_edit);
        addressEdit = (EditText) findViewById(R.id.profile_address_edit);
        phoneEdit = (EditText)findViewById(R.id.profile_user_phone_edit);
        phoneShareEdit = (Switch)findViewById(R.id.phone_switch_edit);

        nameEdit.setHint(currentUser.get("Name").toString());
        addressEdit.setHint(Utils.addressToString(currentUser.getJSONArray("address")));
        phoneEdit.setHint(currentUser.get("Phone").toString());
        phoneShareEdit.setChecked((Boolean) currentUser.get("sharePhone"));

        phoneShareEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {
                    shareSwitch = true;
                }
                else
                {
                    shareSwitch = false;
                }
            }
        });

        resetPassword = (Button) findViewById(R.id.reset_password_button);
        saveChangesB = (Button) findViewById(R.id.save_changes_button);
        changePicB = (Button) findViewById(R.id.change_pic_button);

        resetPassword.setOnClickListener(this);
        saveChangesB.setOnClickListener(this);
        changePicB.setOnClickListener(this);

        ImageButton addressChangeButton = (ImageButton)findViewById(R.id.profile_address_change_edit);
        addressChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressSelectionIntent = new Intent(ProfileEdit.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);
            }
        });

        // user availability
        availabilityAdapter = new UserAvailabilityAdapter(this);
        final LinearLayout availabilityItems = (LinearLayout)findViewById(R.id.edit_profile_availability_items);

        // connect between adapter to component
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

        // take all data of availability of current user
        final ParseQuery<ParseObject> userAvailabilityQuery = new ParseQuery<>("UserAvailability");
        userAvailabilityQuery.whereEqualTo("user", currentUser);

        userAvailabilityQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> userAvailabilities, ParseException e) {
                if (e == null) {
                    userAvailabilitiesList = userAvailabilities;
                    int id = 0;
                    for (ParseObject userAvailability : userAvailabilities) {
                        AvailabilityRecord record = new AvailabilityRecord();
                        record.setTimeFrom(userAvailability.getInt("startTime"));
                        record.setTimeUntil(userAvailability.getInt("endTime"));
                        record.setTag(id++);
                        JSONArray days = userAvailability.getJSONArray("days");
                        for (int i = 0; i < days.length(); i++) {
                            try {
                                record.setDay(days.getInt(i)-1, true);
                            } catch (JSONException e1) {
                            }
                        }
                        availabilityAdapter.add(record);
                    }
                    availabilityAdapter.add(new AvailabilityRecord());
                    availabilityAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case (R.id.reset_password_button):
                final android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setHint("enter your email address here...");
                alertBuilder.setMessage("Do you want to reset your password \n(sending reset to your mail)")
                        .setPositiveButton("Send reset", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //send the reset to the mail, if not successful print exception.
                                ParseUser.requestPasswordResetInBackground(input.getText().toString().toLowerCase(), new RequestPasswordResetCallback()
                                {
                                    @Override
                                    public void done(ParseException e)
                                    {
                                        if (e == null)
                                        {
                                            Toast.makeText(getApplicationContext(), "reset sent successfully to your email", Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            Log.d("My Loggggg", e.getMessage());
                                            Toast.makeText(getApplicationContext(), "error" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("My Loggggg", "user canceled reset");
                            }
                        });
                alertBuilder.setView(input);
                alertBuilder.show();
                break;

            case (R.id.save_changes_button):
                if (!(nameEdit.getText().toString().isEmpty()))
                {
                    Log.d("My Loggggg", "name changed");
                    currentUser.put("Name", nameEdit.getText().toString());
                }

                if (address!=null)
                {
                    Log.d("My Loggggg", "address changed");
                    currentUser.put("address", Utils.addressToJSONArray(address));
                    currentUser.put("addressLocation",new ParseGeoPoint(address.getLatitude(),address.getLongitude()));
                }

                if (!(phoneEdit.getText().toString().isEmpty()))
                {
                    Log.d("My Loggggg", "phone changed");
                    currentUser.put("Phone", phoneEdit.getText().toString());
                }

                currentUser.put("sharePhone",shareSwitch);

                //if the new user took a profile picture - save it to parse data base
                if (picByteArray != null)
                {
                    Log.d("My Loggggg", "profile picture changed");
                    Toast.makeText(getApplicationContext(), "saving photo", Toast.LENGTH_LONG).show();
                    photoFile = new ParseFile(currentUser.getUsername().toString()+"profile_pic.jpg",picByteArray);

                    try
                    {
                        photoFile.save();
                    }
                    catch (ParseException e)
                    {
                        Log.d("My Loggggg", e.getMessage().toString());
                    }
                    currentUser.put("Photo", photoFile);
//                    currentUser.saveInBackground();
                }

                //save changes in user availability
                for(int i=0;i<availabilityAdapter.getCount()-1;i++){
                    Integer id = (Integer)availabilityAdapter.getItem(i).getTag();
                    AvailabilityRecord record = availabilityAdapter.getItem(i);
                    if(id != null){
                        // update if need old records
                        ParseObject userAvailability = userAvailabilitiesList.get(id);
                        userAvailabilitiesList.set(id,null);
                        record.toParseObject(userAvailability);
                        userAvailability.saveInBackground();
                    }else{
                        // else is a new record
                        ParseObject newUserAvailability = record.toParseObject();
                        newUserAvailability.put("user",currentUser);
                        newUserAvailability.saveInBackground();
                    }
                }
                // remove from parse all availabilities that user remove
                for(ParseObject availability: userAvailabilitiesList) {
                    if(availability!=null) {
                        availability.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e!=null){
                                    Log.e("DeleteParseObject", e.getMessage());
                                }
                            }
                        });
                    }
                }

                try
                {
                    currentUser.save();
                    Toast.makeText(getApplicationContext(), "changes saved successfully", Toast.LENGTH_LONG).show();
                }
                catch (ParseException e)
                {
                    Toast.makeText(getApplicationContext(), "error! changes didn't saved", Toast.LENGTH_LONG).show();
                    Log.d("My Loggggg", "error saving changes");
                    e.printStackTrace();
                }

                finish();

                break;

            case (R.id.change_pic_button):
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST)
        {
            //get the photo
            bmPic = (Bitmap) data.getExtras().get("data");
            //convert and scale the photo
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmPic.compress(Bitmap.CompressFormat.JPEG,100,stream);
            //put the photo in byte array
            picByteArray = stream.toByteArray();
            //present the photo that is going to be save in circle view
            curruserPic.setImageBitmap(getCircleBitmap(bmPic));
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_ADDRESS){

            Address address = data.getParcelableExtra("address");
            addressEdit.setText(Utils.addressToString(address));
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
