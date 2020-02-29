package com.sih.seizureglove.PatientHistory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sih.seizureglove.R;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PatientHistory extends AppCompatActivity {


    private DatabaseReference patientDatabase;
    private RecyclerView recyclerView;
    private PatientHistoryRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);
        patientDatabase = FirebaseDatabase.getInstance().getReference("Patients");



        recyclerView = findViewById(R.id.recyclerview);
        adapter = new PatientHistoryRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm a");
// you can get seconds by adding  "...:ss" to it
        date.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = df.format(currentLocalTime);

        String localTime = date.format(currentLocalTime);

        Log.i("popopo",formattedDate);




    }
}
