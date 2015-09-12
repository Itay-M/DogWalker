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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

/**
 * New user registration activity. The user should fill in all the required details and a new
 * user will be created and automatically logged in.
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * Request code to identify response from the camera
     */
    private static final int CAMERA_REQUEST = 0;
    /**
     * Request code to identify response from the address selection activity
     */
    private static final int REQUEST_ADDRESS = 1;

    // UI components
    protected Button registerB,pictureB,addressB;
    protected ImageView newProfilePicIV;
    protected EditText nameET,usernameET,emailET,passwordET,phoneET,addressET;
    protected DatePicker bornDateDP;
    protected Switch phoneSwitch;

    /**
     * The selected address
     */
    protected Address address;
    /**
     * The profile picture stored as a byte array
     */
    byte[] picByteArray;
    /**
     * Adapter that holds all the selected user availabilities
     */
    private UserAvailabilityAdapter availabilityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // get UI components
        newProfilePicIV = (ImageView) findViewById(R.id.newProfilePicView);
        registerB = (Button) findViewById(R.id.registerButton);
        pictureB = (Button) findViewById(R.id.pictureButton);
        nameET = (EditText)findViewById(R.id.regNameEditText);
        usernameET = (EditText)findViewById(R.id.regUsernameEditText);
        emailET = (EditText)findViewById(R.id.regEmailEditText);
        addressB = (Button)findViewById(R.id.regAddressButton);
        addressET = (EditText)findViewById(R.id.regAddressEditText);
        passwordET = (EditText)findViewById(R.id.regPasswordEditText);
        phoneET = (EditText)findViewById(R.id.regPhoneEditText);
        bornDateDP = (DatePicker)findViewById(R.id.regDatePicker);
        phoneSwitch = (Switch) findViewById(R.id.phone_switch);

        // handle buttons click
        registerB.setOnClickListener(this);
        pictureB.setOnClickListener(this);
        addressB.setOnClickListener(this);

        // user availability
        final LinearLayout availabilityItems = (LinearLayout) findViewById(R.id.register_availability_items);
        availabilityAdapter = new UserAvailabilityAdapter(this);
        availabilityAdapter.add(new AvailabilityRecord());
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

        //set the "GO" button in the keyboard to hide the keyboard
        emailET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //hide the keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case(R.id.registerButton):

                // check that all the user availabilities are valid
                for(int i=0;i<availabilityAdapter.getCount()-1;i++){
                    if(!availabilityAdapter.getItem(i).isValid()){
                        Utils.showMessageBox(v.getContext(),"Invalid period","At least one of the availability period are invalid");
                        return;
                    }
                }

                // create new user
                final ParseUser user = new ParseUser();

                // fill in details from the components
                user.setUsername(usernameET.getText().toString());
                user.setPassword(passwordET.getText().toString());
                user.setEmail(emailET.getText().toString().toLowerCase());
                user.put("name", nameET.getText().toString());
                user.put("address", Utils.addressToJSONArray(address));
                user.put("phone", phoneET.getText().toString());
                user.put("addressLocation",new ParseGeoPoint(address.getLatitude(),address.getLongitude()));
                user.put("sharePhone",phoneSwitch.isChecked());

                // save born date
                int day = bornDateDP.getDayOfMonth()+ 1;
                int month = bornDateDP.getMonth();
                int year = bornDateDP.getYear();
                Calendar c = Calendar.getInstance();
                c.set(year, month, day);
                user.put("bornDate", c.getTime());

                // handle profile picture
                if (picByteArray!=null) {
                    ParseFile photoFile = new ParseFile(usernameET.getText().toString()+"profile_pic.jpg",picByteArray);
                    try {
                        photoFile.save();
                    } catch (ParseException e){
                        Log.e("Register", "Saving profile picture failed: " + e.getMessage());
                    }
                    user.put("photo", photoFile);
                }

                //register the new user in Parse database.
                user.signUpInBackground(new SignUpCallback() {
                    public void done(final ParseException e) {
                        if (e == null) {
                            // add user availability and place it in parse
                            for(int i=0;i<availabilityAdapter.getCount()-1;i++){
                                ParseObject availability = availabilityAdapter.getItem(i).toParseObject();
                                availability.put("user",user);
                                availability.saveInBackground();
                            }

                            // since a login has been made - set the user on the installation object
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("user",ParseUser.getCurrentUser());
                            installation.saveEventually();

                            // move the user to the main activity
                            Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_LONG);
                            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Utils.showMessageBox(RegisterActivity.this,"Registration Failed",getString(R.string.unknown_error_occur));
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
                // move user to address selection activity
                Intent addressSelectionIntent = new Intent(RegisterActivity.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST) {

            //get the photo
            Bitmap bmPic = (Bitmap) data.getExtras().get("data");
            //convert and scale the photo
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmPic.compress(Bitmap.CompressFormat.JPEG,100,stream);
            //put the photo in byte array
            picByteArray = stream.toByteArray();
            //present the photo that is going to be save in circle view
            newProfilePicIV.setImageBitmap(Utils.getCircleBitmap(bmPic));

        }else if(resultCode == RESULT_OK && requestCode == REQUEST_ADDRESS){

            // save the address for later use
            Address address = data.getParcelableExtra("address");
            this.address = address;
            // update UI to display the selected address
            addressET.setText(Utils.addressToString(address));
        }
    }
}
