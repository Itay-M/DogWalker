package com.dogwalker.itaynaama.dogwalker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import java.io.ByteArrayOutputStream;


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


    @Override
    public void onClick(View v)
    {

        switch (v.getId())
        {
            case(R.id.registerButton):

                //new user creation.
                ParseUser user = new ParseUser();

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
