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


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int CAMERA_REQUEST = 0;
    private static final int REQUEST_ADDRESS = 1;
    private boolean pictureTaken = false;
    protected Button registerB,pictureB,addressB;
    protected ImageView newProfilePicIV;
    protected EditText nameET,usernameET,emailET,passwordET,phoneET,addressET;
    protected Address address;
    protected DatePicker bornDateDP;
    ParseFile photoFile;
    byte[] picByteArray;
    Bitmap bmPic;
    private UserAvailabilityAdapter availabilityAdapter;
    protected Switch phoneSwitch;
    protected Boolean sharePhone = false;

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
        bornDateDP = (DatePicker)findViewById(R.id.regDatePicker);

        //switch
        phoneSwitch = (Switch) findViewById(R.id.phone_switch);

        // availability
        availabilityAdapter = new UserAvailabilityAdapter(this);
        availabilityAdapter.add(new AvailabilityRecord());
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

        phoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                sharePhone = isChecked;
            }
        });

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
                user.put("sharePhone",sharePhone);

                // save born date
                int day = bornDateDP.getDayOfMonth()+ 1;
                int month = bornDateDP.getMonth();
                int year = bornDateDP.getYear();
                Calendar c = Calendar.getInstance();
                c.set(year, month, day);
                user.put("bornDate",c.getTime());

                //if the new user took a profile picture - save it to parse data base
                Log.d("My Loggggg", String.valueOf(pictureTaken));
                if (pictureTaken)
                {
                    Toast.makeText(getApplicationContext(), "saving photo", Toast.LENGTH_LONG).show();
                    //newProfilePicIV.setImageBitmap(bmPic);
                    photoFile = new ParseFile(usernameET.getText().toString()+"profile_pic.jpg",picByteArray);

                    try
                    {
                        photoFile.save();
                    } catch (ParseException e){
                        Log.d("My Loggggg", e.getMessage());
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

                            // since a login has been made - set the user on the installation object
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("user",ParseUser.getCurrentUser());
                            installation.saveEventually();

                            Toast.makeText(getApplicationContext(), "User created successfully", Toast.LENGTH_LONG);
                            Intent registerIntent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(registerIntent);
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
                Intent addressSelectionIntent = new Intent(RegisterActivity.this, AddressSelectionActivity.class);
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
