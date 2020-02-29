package com.sih.seizureglove;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

public class FirstAidActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_aid);

        ImageView firstaid_imageview = findViewById(R.id.firstaid_imageview);
        PhotoViewAttacher pAttacher;
        pAttacher = new PhotoViewAttacher(firstaid_imageview);
        pAttacher.update();
    }
}
