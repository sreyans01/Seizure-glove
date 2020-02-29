package com.sih.seizureglove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.sih.seizureglove.PatientHistory.PatientHistory;

import static com.sih.seizureglove.MyIntentService.MODEL_NAME;

public class MainActivity extends AppCompatActivity {

    Button firstaid_btn,patienthistory_btn,notifications_btn,ambulance_btn;
    Context context = MainActivity.this;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    SharedPreferences loggedInFirstTime = null;
    private DatabaseReference mDatabase;
    private String useremail_temp;
    private Toolbar toolbar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.logout){
            loggedInFirstTime.edit().putBoolean("firstrun", true).commit();
            FirebaseAuth.getInstance().signOut();
            //t.LogoutUser();

            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.mainactivity_menu, menu);
        return true;
    }

    public void visibilityLinearLayout(int id,int visibility){
        LinearLayout linearLayout = (LinearLayout) findViewById(id);
        linearLayout.setVisibility(visibility);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        loggedInFirstTime = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        useremail_temp = user.getEmail().replace("."," ");

        mDatabase = FirebaseDatabase.getInstance().getReference("Caretakers");
        firstaid_btn = findViewById(R.id.firstaid_btn);
        patienthistory_btn = findViewById(R.id.patienthistory_btn);
        notifications_btn = findViewById(R.id.notifications_btn);
        ambulance_btn = findViewById(R.id.ambulance_btn);
        //toolbar = findViewById(R.id.toolbar);

        //toolbar.setTitle("Seizure Glove");
        HandleOnClicks();

        buildAndDownloadCustomModelFirebase(MODEL_NAME);
    }

    public void HandleOnClicks(){
        firstaid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentTo(context,FirstAidActivity.class);
            }
        });

        patienthistory_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intentTo(context, PatientHistory.class);
            }
        });

        notifications_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intentTo(context,NotificationsActivity.class);
            }
        });

        ambulance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intentTo(context,HospitalsActivity.class);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loggedInFirstTime.getBoolean("firstrun", true)) {

            mDatabase.child("Main Caretaker").setValue(useremail_temp);
            // Do first run stuff here then set 'firstrun' as false

            // using the following line to edit/commit checkFirstRun

        }


        loggedInFirstTime.edit().putBoolean("firstrun", false).commit();
    }
    public void intentTo(Context context, Class activityClass){

        Intent i = new Intent((Activity)context,activityClass);
        startActivity(i);
    }

    public void buildAndDownloadCustomModelFirebase(String model_name){

        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder(model_name).build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Success.
                    }
                });

    }

    public void LogOut(View view){
        loggedInFirstTime.edit().putBoolean("firstrun", true).commit();
        FirebaseAuth.getInstance().signOut();
        //t.LogoutUser();

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

}
