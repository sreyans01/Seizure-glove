package com.sih.seizureglove;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.allyants.notifyme.NotifyMe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;

public class NotificationsActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "Seizure Alert";
    public static final int SEIZURE_ALERT_ID = 100;

    Switch notify_switch;
    SharedPreferences notifyalerts = null;
    private DatabaseReference mDatabase,patientDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String useremail_temp;
    NotifyMe.Builder notifyMe;
    private MediaPlayer mediaPlayer;
    private EditText e_name,e_extra_details;
    private Button save,edit_btn;
    Context context = NotificationsActivity.this;

    private TextView feedback;
    private RadioGroup feedbackgroup;
    private LinearLayout feedbacklayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mediaPlayer = MediaPlayer.create(this,R.raw.alarmbuzzer);
        notifyMe = new NotifyMe.Builder(NotificationsActivity.this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        useremail_temp = user.getEmail().replace("."," ");
        mDatabase = FirebaseDatabase.getInstance().getReference("Caretakers");
        patientDatabase = FirebaseDatabase.getInstance().getReference("Patients");
        notifyalerts = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        //buildNotification();
        buildNotification2();

        e_name = (EditText)findViewById(R.id.e_name);
        e_extra_details = (EditText)findViewById(R.id.e_extra_details);
        save = (Button)findViewById(R.id.save);
        edit_btn = (Button)findViewById(R.id.edit);

        feedbacklayout = (LinearLayout)findViewById(R.id.feedbacklayout);
        feedbackgroup = (RadioGroup)findViewById(R.id.feedbackgroup);

        HandleOnClicks();


        notify_switch = findViewById(R.id.notify_switch);
        notify_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyalerts.edit().putBoolean("Alerts",notify_switch.isChecked()).commit();
                if(notify_switch.isChecked()) {
                    mDatabase.child(useremail_temp).child("Alerts").setValue("ON");
                    setIntentService();
                }
                else {
                    mDatabase.child(useremail_temp).child("Alerts").setValue("OFF");
                    stopIntentService();
                }

                //mediaPlayer.start();
                //fullNotificationCode(1);
                //convertLocalJson();
                //buildNotification2();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(e_name.getText().toString().isEmpty())
                    Toast.makeText(context,"Please put a patient's name.",Toast.LENGTH_LONG).show();
                else{

                    String name = e_name.getText().toString();
                    notifyalerts.edit().putString("Patient",name).commit();
                    if(!e_extra_details.getText().toString().isEmpty()){
                        String extra_Details = e_extra_details.getText().toString();
                        setTextOnTextView(R.id.otherDetails_saved,extra_Details);
                        notifyalerts.edit().putString("ExtraDetails",extra_Details).commit();
                        patientDatabase.child("Patients").child(name).child("Name").setValue(useremail_temp);
                        patientDatabase.child("Patients").child(name).child("Details").setValue(extra_Details);

                    }
                    else {
                        //setting main caretaker email in patients name child
                        patientDatabase.child("Patients").child(name).setValue(useremail_temp);
                    }
                    setTextOnTextView(R.id.patient_saved,name);
                }

                visibilityLinearLayout(R.id.edit_Detailslayout,View.GONE);
                e_name.setText("");
                e_extra_details.setText("");
                visibilityLinearLayout(R.id.showPatientDetailslayout,View.VISIBLE);
            }
        });


        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e_name.setText(notifyalerts.getString("Patient",""));
                e_extra_details.setText(notifyalerts.getString("ExtraDetails","Nothing saved"));
                visibilityLinearLayout(R.id.edit_Detailslayout,View.VISIBLE);
                visibilityLinearLayout(R.id.showPatientDetailslayout,View.GONE);
            }
        });

    }

    public void addSavedDetails(){
        //e_name.setText(notifyalerts.getString("Patient",""));
        //e_extra_details.setText(notifyalerts.getString("ExtraDetails","Nothing saved"));
        setTextOnTextView(R.id.patient_saved,notifyalerts.getString("Patient",""));
        setTextOnTextView(R.id.otherDetails_saved,notifyalerts.getString("ExtraDetails","Nothing saved"));

        visibilityLinearLayout(R.id.edit_Detailslayout,View.GONE);
        visibilityLinearLayout(R.id.showPatientDetailslayout,View.VISIBLE);
    }

    public void setTextOnTextView(int id,String text){
        TextView textView = findViewById(id);
        textView.setText(text);
    }

    public void visibilityLinearLayout(int id,int visibility){
        LinearLayout linearLayout = (LinearLayout) findViewById(id);
        linearLayout.setVisibility(visibility);
    }
    public void fullNotificationCode(int requestCode){

        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        String title = "Seizure Alert";
        String content = "Your loved one is in danger..!!";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "Seizure_Glove";

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {

            String description = "My Life my rules..!!";
            //for android oreo and higher because it needs notification channel
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription(description);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[4]);
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_active_blue_24dp)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Much longer text that cannot fit one line...jfhjfvhjfvjhvjh\n\nhf"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);

            //shows the Notification

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(requestCode, builder.build());


    }

    public void setIntentService(){
        if(notifyalerts.getString("Patient","").compareTo("")==0){
            Toast.makeText(context,"Please specify a patient's name first",Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(NotificationsActivity.this,MyIntentService.class);
        String name = notifyalerts.getString("Patient","");
        intent.putExtra("Patient",name);
        startService(intent);
    }

    public void stopIntentService(){
        Intent intent = new Intent(NotificationsActivity.this,MyIntentService.class);
        stopService(intent);
    }



    public void buildNotification2(){

        int hourOfDay = 0,minute=0,second=0;
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY,hourOfDay);
        now.set(Calendar.MINUTE,minute);
        now.set(Calendar.SECOND,second);
        Intent intent = new Intent(getApplicationContext(), NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        notifyMe.title("Patient in Danger !");
        notifyMe.content("Your loved one is in danger, take appropriate measures to help him out.");
        notifyMe.color(255,0,0,255);//Color of notification header
        //notifyMe.led_color(255,255,255,255);//Color of LED when notification pops up
        notifyMe.time(now);//The time to popup notification
        notifyMe.delay(2000);//Delay in ms
        //notifyMe.large_icon(Int resource);//Icon resource by ID
        notifyMe.rrule("FREQ=MINUTELY;INTERVAL=5;COUNT=2");//RRULE for frequency of notification
        notifyMe.addAction(new Intent(),"Save your loved one fast.Y").build();
        //The action will call the intent when pressed


    }

    public void buildNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_active_blue_24dp)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_MAX);

        //shows the Notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(SEIZURE_ALERT_ID, builder.build());

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Seizure Detected";           //channel name
            String description = "Your loved is in danger.....please help !!";   //channel description
            int importance = NotificationManager.IMPORTANCE_MAX;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    public void fireIntent(){
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_active_blue_24dp)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

    }

    public void showNotification(){


    }

    @Override
    protected void onResume() {
        if(notifyalerts.getBoolean("Alerts",true)) {
            setIntentService();
            notify_switch.setChecked(true);
        }
        else {
            notify_switch.setChecked(false);
            stopIntentService();
        }

        String text = notifyalerts.getString("Patient","");
        if(text.compareTo("")==0) {

            visibilityLinearLayout(R.id.edit_Detailslayout,View.VISIBLE);
            visibilityLinearLayout(R.id.showPatientDetailslayout,View.GONE);

        }
        else {
            visibilityLinearLayout(R.id.edit_Detailslayout,View.GONE);
            visibilityLinearLayout(R.id.showPatientDetailslayout,View.VISIBLE);
            addSavedDetails();
        }
        super.onResume();
    }

    public void convertLocalJson(){
        InputStream is = getResources().openRawResource(R.raw.hospitals);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int n;
            try {
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        String jsonString = writer.toString();
        Log.d("jsonString",jsonString);


    }


    public void gotoGmail(String subject){
        final String packagegmail = "com.google.android.gm";
// return true if gmail is installed
        boolean isGmailInstalled = false;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"sreyans01@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        if (isGmailInstalled) {
            intent.setType("text/html");
            intent.setPackage(packagegmail);
            startActivity(intent);
        } else {  // allow user to choose a different app to send email with
            intent.setType("message/rfc822");
            startActivity(Intent.createChooser(intent, "Choose an email client"));
        }

    }

    public void HandleOnClicks(){
        RadioButton positivebutton = findViewById(R.id.positive);
        positivebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                gotoGmail("Yes, the seizure detected was 100% positive.");
                RadioButton button = findViewById(R.id.positive);
                button.setChecked(false);
            }
        });


        RadioButton negativebutton = findViewById(R.id.negative);
        negativebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gotoGmail("No, the seizure detected was not positive.");
                RadioButton button = findViewById(R.id.negative);
                button.setChecked(false);

            }
        });
    }

}
