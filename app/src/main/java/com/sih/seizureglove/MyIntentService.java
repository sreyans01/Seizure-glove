package com.sih.seizureglove;

import android.animation.FloatArrayEvaluator;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MyIntentService extends IntentService {

    public static final String MODEL_NAME = "tFlite_sfb";
    FirebaseModelInterpreter interpreter ;

    public static final String TAG = "com.sih.seizureglove";
    private Context context;
    MediaPlayer mediaPlayer;
    private DatabaseReference mDatabase,patientDatabase;

    int count = 0;

    private String patient;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        patient = intent.getStringExtra("Patient");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        mDatabase = FirebaseDatabase.getInstance().getReference("Caretakers");
        patientDatabase = FirebaseDatabase.getInstance().getReference("Patients");

        //buildOnlyCustomModelFirebase(MODEL_NAME);


    }

    @Override
    public int onStartCommand(@Nullable final Intent intent, int flags, int startId) {

            mediaPlayer = MediaPlayer.create(this,R.raw.alarmbuzzer);

            Intent intent2 = new Intent(this, NotificationsActivity.class);
            intent2.putExtra("ShowFeedback","Yes");
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);


            String title = "Seizure Alert";
            String content = "Patient is in danger, and he needs your help.";
            final NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            String CHANNEL_ID = "Seizure_Glove";

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {

                String description = "Help "+patient+" immediately";
                //for android oreo and higher because it needs notification channel
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_HIGH);

                notificationChannel.setDescription(description);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);

                notificationManager.createNotificationChannel(notificationChannel);
            }


            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_active_blue_24dp)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Patient is in danger, he needs your help. Open the app to see immediate first aid instructions. "))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent);

            //shows the Notification

// notificationId is a unique int for each notification that you must define



            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        //Log.i("kokoko",snapshot.child("EMG").getValue().toString());
                        try {
                            float value1 = Float.parseFloat(snapshot.child("EMG").getValue().toString());
                            int value = (int)value1;
                            Log.i("lolk",snapshot.child("EMG").getValue().toString());

                            if (value == 1)
                                count++;
                            if (count > 0 && count % 3 == 0) {

                                //patientDatabase.child(patient);
                                notificationManager.notify(2, builder.build());

                                mediaPlayer.start();
                                startForeground(2, builder.getNotification());
                            }
                            Log.i("lolololo", String.valueOf(count));

                        }
                        catch (Exception e){}
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            return START_REDELIVER_INTENT;



        //return super.onStartCommand(intent, flags, startId);
    }

    public void startmaekyakiyetheh(){}

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        super.onDestroy();
    }

    public void fetchApi(){


        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

// Build a remote model source object by specifying the name you assigned the model
// when you uploaded it in the Firebase console.

        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder(MODEL_NAME).build();

        /*

        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("")
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);
        
         */


    }

    public void buildOnlyCustomModelFirebase(String model_name){

        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder(model_name).build();

        Log.i("7596845338","builded model");
        checkIfModelDownloaded(remoteModel);

    }

    public void checkIfModelDownloaded(final FirebaseCustomRemoteModel remoteModel){

        //will make interpreter null if not downloaded the model


        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        FirebaseModelInterpreterOptions options;
                        if (isDownloaded) {
                            options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                            try {
                                interpreter = FirebaseModelInterpreter.getInstance(options);
                                Log.i("7596845338","downloaded model");
                                configureModelInputOutput();
                            } catch (FirebaseMLException e) {
                                e.printStackTrace();
                                Log.i("7596845338","downloaded model error");
                            }
                        } else {
                            interpreter = null;

                            Log.i("7596845338","downloaded model else");
                            final FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                                    .build();
                            FirebaseModelManager.getInstance().download(remoteModel, conditions)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // Success.
                                            Log.i("7596845338","downloaded model interpreter");
                                            downloadModelInterpreter(conditions,remoteModel);

                                        }
                                    });

                            //options = new FirebaseModelInterpreterOptions.Builder(localModel).build();
                        }

                        // ...
                    }
                });

    }

    public void downloadModelInterpreter(FirebaseModelDownloadConditions conditions, final FirebaseCustomRemoteModel remoteModel){

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {

                        FirebaseModelInterpreterOptions options;
                        options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                        try {
                            interpreter = FirebaseModelInterpreter.getInstance(options);
                            Log.i("7596845338","downloaded model interpreter");
                            configureModelInputOutput();
                        } catch (FirebaseMLException e) {
                            e.printStackTrace();
                            Log.i("7596845338","downloaded model interpreter error");
                        }
                        // Download complete. Depending on your app, you could enable
                        // the ML feature, or switch from the local model to the remote
                        // model, etc.
                    }
                });

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
    public void configureModelInputOutput(){

        try {
            FirebaseModelInputOutputOptions inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1501,4})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1,1501})
                            .build();

            Log.i("7596845338","configure model done");
            performInference();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            Log.i("7596845338","configure model error");
            Log.i("gogogogo","Inference not coming.");
        }
    }

    public void performInference(){


        final int batchNum = 0;
        final float[][][] input = new float[1][1501][4];

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(int i=0;i<1501;i++){



                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            //Log.i("kokoko",snapshot.child("EMG").getValue().toString());
                            try {

                                float valueEMG = Float.parseFloat(snapshot.child("EMG").getValue().toString());
                                float valueAx = Float.parseFloat(snapshot.child("Ax").getValue().toString());
                                float valueAy = Float.parseFloat(snapshot.child("Ay").getValue().toString());
                                float valueAz = Float.parseFloat(snapshot.child("Az").getValue().toString());

                                Log.i("lolk",snapshot.child("EMG").getValue().toString());



                                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                                // model. For example, some models might require values to be normalized
                                // to the range [0.0, 1.0] instead.
                                input[batchNum][i][0] = valueEMG;
                                input[batchNum][i][1] = valueAx;
                                input[batchNum][i][2] = valueAy;
                                input[batchNum][i][3] = valueAz;


                                Log.i("7596845338","perform inference done "+ String.valueOf(input[0][0][0]));
                                getOutput(input);


                            }
                            catch (Exception e){}


                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void getOutput(float[][][] input){

        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();

            FirebaseModelInputOutputOptions inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1501,4})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1,1501})
                            .build();
            Log.i("7596845338","output model interpreter done");

            FirebaseModelInterpreter firebaseInterpreter ;
            interpreter.run(inputs, inputOutputOptions)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseModelOutputs>() {
                                @Override
                                public void onSuccess(FirebaseModelOutputs result) {
                                    // ...


                                    float[][] output = result.<float[][]>getOutput(0);
                                    //float[] probabilities = output[0];

                                    Log.i("MLKit", String.valueOf(output.length)+"pop");

                                    BufferedReader reader = null;
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("7596845338","downloaded model interpreter error22");
                                    // Task failed with an exception
                                    // ...
                                }
                            });


        } catch (FirebaseMLException e) {
            e.printStackTrace();
            Log.i("7596845338","output model interpreter error");
        }
    }


}
