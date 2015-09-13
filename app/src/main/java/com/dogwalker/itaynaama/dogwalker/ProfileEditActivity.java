package com.dogwalker.itaynaama.dogwalker;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ProfileEditActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * Request code to identify response from the camera
     */
    private static final int CAMERA_REQUEST = 0;
    /**
     * Request code to identify response from the address selection activity
     */
    private static final int REQUEST_ADDRESS = 1;

    // UI components
    protected EditText nameEdit,addressEdit,phoneEdit;
    protected Button resetPassword, saveChangesB, changePicB;
    protected ImageView userPic;
    protected Switch phoneShareEdit;

    /**
     * The list of UserAvailabilities - used for updating/removing/inserting
     */
    protected List<ParseObject> userAvailabilitiesList;
    /**
     * The adapter used by the user availabilities list - use for checking which should be
     * removed/updated/inserted
     */
    protected UserAvailabilityAdapter availabilityAdapter;
    /**
     * The byte array of the picture selected by the user (null while not picture selected / changed)
     */
    protected byte[] picByteArray;
    /**
     * The address selected by ther user (null while not selected / changed)
     */
    protected Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        ParseUser currentUser = ParseUser.getCurrentUser();

        // get UI components
        userPic = (ImageView)findViewById(R.id.view_curr_pic);
        nameEdit = (EditText) findViewById(R.id.profile_name_edit);
        addressEdit = (EditText) findViewById(R.id.profile_address_edit);
        phoneEdit = (EditText)findViewById(R.id.profile_user_phone_edit);
        phoneShareEdit = (Switch)findViewById(R.id.phone_switch_edit);
        resetPassword = (Button) findViewById(R.id.reset_password_button);
        saveChangesB = (Button) findViewById(R.id.save_changes_button);
        changePicB = (Button) findViewById(R.id.change_pic_button);

        // init ui components with user details
        ParseFile p = (ParseFile) currentUser.get("photo");
        if(p!=null) {
            try {
                Bitmap b = BitmapFactory.decodeByteArray(p.getData(), 0, p.getData().length);
                userPic.setImageBitmap(b);
            } catch (ParseException e) {
                Log.d("ProfileEdit", e.getMessage());
            }
        }
        nameEdit.setHint(currentUser.get("name").toString());
        addressEdit.setHint(Utils.addressToString(currentUser.getJSONArray("address")));
        phoneEdit.setHint(currentUser.get("phone").toString());
        phoneShareEdit.setChecked(currentUser.getBoolean("sharePhone"));
        resetPassword.setOnClickListener(this);
        saveChangesB.setOnClickListener(this);
        changePicB.setOnClickListener(this);

        // handle address selection
        ImageButton addressChangeButton = (ImageButton)findViewById(R.id.profile_address_change_edit);
        addressChangeButton.setOnClickListener(this);

        // handle user availability changes
        final LinearLayout availabilityItems = (LinearLayout)findViewById(R.id.edit_profile_availability_items);
        availabilityAdapter = new UserAvailabilityAdapter(this);
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

        // query all availability of current user and fill the adapter
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
    public void onClick(View v) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        switch (v.getId()) {
            case (R.id.reset_password_button):
                Utils.resetPassword(ProfileEditActivity.this,currentUser.getEmail());
                break;

            case (R.id.save_changes_button):

                // check that all the user availabilities are valid
                for(int i=0;i<availabilityAdapter.getCount()-1;i++){
                    if(!availabilityAdapter.getItem(i).isValid()){
                        Utils.showMessageBox(v.getContext(),"Invalid period","At least one of the availability period are invalid");
                        return;
                    }
                }

                // handle name
                String name = nameEdit.getText().toString();
                if (!name.isEmpty()) {
                    currentUser.put("name", name);
                }

                // handle address
                if (address!=null) {
                    currentUser.put("address", Utils.addressToJSONArray(address));
                    currentUser.put("addressLocation",new ParseGeoPoint(address.getLatitude(),address.getLongitude()));
                }

                // handle phone number
                String phone = phoneEdit.getText().toString();
                if (!phone.isEmpty()) {
                    currentUser.put("phone", phone);
                }
                currentUser.put("sharePhone",phoneShareEdit.isChecked());

                // handle profile picture (save before proceeding
                if (picByteArray != null) {
                    Log.d("My Loggggg", "profile picture changed");
                    ParseFile photoFile = new ParseFile(currentUser.getUsername().toString()+"profile_pic.jpg",picByteArray);

                    try {
                        photoFile.save();
                    }catch (ParseException e) {
                        Log.d("ProfileEdit", "Saving picture failed: "+e.getMessage().toString());
                    }
                    currentUser.put("photo", photoFile);
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

                // save user details
                try {
                    currentUser.save();
                    Toast.makeText(getApplicationContext(), "Changes saved successfully", Toast.LENGTH_LONG).show();
                    finish();
                }catch (ParseException e) {
                    Log.e("ProfileEdit", "error saving changes: "+e.getMessage());
                    Utils.showMessageBox(ProfileEditActivity.this, "Failed saving changes", getString(R.string.unknown_error_occur));
                }

                break;

            case (R.id.change_pic_button):
                // create Intent to take a picture and return control to the calling application
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);

                break;

            case R.id.profile_address_change_edit:
                // move user to address selection activity
                Intent addressSelectionIntent = new Intent(ProfileEditActivity.this, AddressSelectionActivity.class);
                startActivityForResult(addressSelectionIntent, REQUEST_ADDRESS);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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
            userPic.setImageBitmap(bmPic);

        }else if(resultCode == RESULT_OK && requestCode == REQUEST_ADDRESS){

            // save the selected address
            Address address = data.getParcelableExtra("address");
            this.address = address;
            // upadte read-only component
            addressEdit.setText(Utils.addressToString(address));
        }
    }
}
