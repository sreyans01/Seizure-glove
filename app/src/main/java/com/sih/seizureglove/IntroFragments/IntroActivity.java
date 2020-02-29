package com.sih.seizureglove.IntroFragments;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.sih.seizureglove.LoginActivity;
import com.sih.seizureglove.R;

public class IntroActivity extends AppCompatActivity {
    ViewFlipper introviewflipper;
    SharedPreferences checkFirstRun = null;
    Context context = IntroActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        allfindviewbyids();
        checkFirstRun = getSharedPreferences(getPackageName(), MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkFirstRun.getBoolean("firstrun", true)) {

            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit checkFirstRun

        }
        else{
            Intent i = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
            checkFirstRun.edit().putBoolean("firstrun", false).commit();
    }

    public void showNextView(View v){

        int displayChild = introviewflipper.getDisplayedChild();
        int childcount = introviewflipper.getChildCount();
        if(displayChild == childcount-1){
            Intent i = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        else
            introviewflipper.showNext();

    }

    public void showPreviousView(View v){

        int displayChild = introviewflipper.getDisplayedChild();

        if(displayChild == 0){
            Toast.makeText(context,"This is the first page.",Toast.LENGTH_LONG).show();
            return;
        }
        introviewflipper.showPrevious();

    }
    public void allfindviewbyids(){

        introviewflipper = (ViewFlipper)findViewById(R.id.introviewflipper);
    }
}
