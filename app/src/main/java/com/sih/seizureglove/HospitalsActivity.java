package com.sih.seizureglove;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class HospitalsActivity extends AppCompatActivity {

    ViewFlipper introviewflipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitals);
        introviewflipper = findViewById(R.id.introviewflipper);
    }



    public void showNextView(View v){

        int displayChild = introviewflipper.getDisplayedChild();
        int childcount = introviewflipper.getChildCount();
        if(displayChild == childcount-1){
            Toast.makeText(HospitalsActivity.this,"This is the last page.",Toast.LENGTH_LONG).show();
            return;
        }
        else
            introviewflipper.showNext();

    }

    public void showPreviousView(View v){

        int displayChild = introviewflipper.getDisplayedChild();

        if(displayChild == 0){
            Toast.makeText(HospitalsActivity.this,"This is the first page.",Toast.LENGTH_LONG).show();
            return;
        }
        introviewflipper.showPrevious();

    }
    public void allfindviewbyids(){

        introviewflipper = (ViewFlipper)findViewById(R.id.introviewflipper);
    }
}
