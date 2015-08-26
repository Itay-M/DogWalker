package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import java.io.ByteArrayOutputStream;

public class ProfileEdit extends AppCompatActivity implements View.OnClickListener {
    private static final int CAMERA_REQUEST = 0;
    EditText nameEdit,cityEdit;
    Button resetPassword, saveChangesB, changePicB;
    ImageView curruserPic;
    ParseUser currentUser = ParseUser.getCurrentUser();

    ParseFile photoFile;
    byte[] picByteArray;
    Bitmap bmPic;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        curruserPic = (ImageView)findViewById(R.id.view_curr_pic);

        //show profile picture in the imageView
        ParseFile p = (ParseFile) currentUser.get("Photo");
        try
        {
            Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
            curruserPic.setImageBitmap(b);
        }
        catch (ParseException e)
        {
            Log.d("My Loggggg", e.getMessage().toString());
        }

        nameEdit = (EditText) findViewById(R.id.profile_name_edit);
        cityEdit = (EditText) findViewById(R.id.profile_user_city_edit);

        nameEdit.setHint(currentUser.get("Name").toString());
        cityEdit.setHint(currentUser.get("City").toString());

        resetPassword = (Button) findViewById(R.id.reset_password_button);
        saveChangesB = (Button) findViewById(R.id.save_changes_button);
        changePicB = (Button) findViewById(R.id.change_pic_button);

        resetPassword.setOnClickListener(this);
        saveChangesB.setOnClickListener(this);
        changePicB.setOnClickListener(this);


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
                                ParseUser.requestPasswordResetInBackground(input.getText().toString(), new RequestPasswordResetCallback()
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
                if (!(nameEdit.getText().toString().equals(currentUser.get("Name").toString())))
                {
                    Log.d("My Loggggg", "name changed");
                    currentUser.put("Name", nameEdit.getText().toString());
                }

                if (!(cityEdit.getText().toString().equals(currentUser.get("City").toString())))
                {
                    Log.d("My Loggggg", "city changed");
                    currentUser.put("City", cityEdit.getText().toString());
                }

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
