package com.dogwalker.itaynaama.dogwalker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseInstallation;
import com.parse.ParseUser;

/**
 * A base class to use by all our main activities in order to maintain similar behavior across all
 * the application activities (such as options menu, action bar etc).
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Indicates whenever this activity is at the most upper level - no back option should be
     * presented and going back will trigger application exit.
     */
    private final boolean isTopActivity;

    public BaseActivity(){
        this.isTopActivity = false;
    }

    public BaseActivity(boolean isTopActivity){
        this.isTopActivity = isTopActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // show/hide the actionBar's back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(!this.isTopActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(ParseUser.getCurrentUser()==null){
            return false;
        }

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout_action:
                logout();
                return true;
            case R.id.view_profile_action:
                viewProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * terminate the current activity and move to
     * @return
     */
    @Override
    public boolean onSupportNavigateUp()
    {
        finish();
        return true;
    }

    /**
     * Log out the current user.
     */
    private void logout() {
        // logout the user from parse
        Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
        ParseUser.logOut();

        // disconnect user from this installation
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.remove("user");
        installation.saveEventually();

        // take the user back to the Login activity
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    /**
     * View the user's profile.
     */
    private void viewProfile(){
        Intent i = new Intent(this, ProfileView.class);
        startActivity(i);
    }

    /**
     * when the back button pressed, the user asked if he wants to exit the app.
     */
    @Override
    public void onBackPressed() {
        if(this.isTopActivity) {
            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Do you want to exit the app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("My Loggggg", "user canceled");

                        }
                    });
            alertBuilder.show();
        }else{
            super.onBackPressed();
        }
    }
}