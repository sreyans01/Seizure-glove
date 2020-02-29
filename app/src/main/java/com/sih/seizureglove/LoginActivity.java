package com.sih.seizureglove;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/*

Copy and paste next1.onclicklistener to tutorx.
 */


public class LoginActivity extends AppCompatActivity {


    private Context context = LoginActivity.this;
    private CoordinatorLayout coordinatorLayout;
    private int requestCode;
    private LinearLayout emailverifylayout;
    private ConstraintLayout loginlayout,signuplayout;
    private EditText el,pl,es,ps,confirmps;
    private String email,pass;
    private Button login,next1;
    private TextView signup,forgotpass;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser user=null;

    SharedPreferences checkFirstRun = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        allfindviewbyids();
        HandleOnClicks();
        checkFirstRun = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        
        //emailverifylayout = (LinearLayout)findViewById(R.id.emailverifylayout);
        loginlayout.setVisibility(View.VISIBLE);
        signuplayout.setVisibility(View.INVISIBLE);


        progressBar.setVisibility(View.GONE);
        Intent i = getIntent();
        requestCode = i.getIntExtra("choice",2);

        try {
            checkcurrentlogin();
        }
        catch (Exception e){}


    }


    @Override
    public void onBackPressed() {

        if(loginlayout.getVisibility()== View.VISIBLE)
            super.onBackPressed();
        else if(signuplayout.getVisibility()==View.VISIBLE) {

            loginlayout.setVisibility(View.VISIBLE);
            signuplayout.setVisibility(View.GONE);
        }

    }



    public void intentme(){
        Intent i;
        String useremail = mAuth.getCurrentUser().getEmail();
        if(requestCode == 2)
            i = new Intent(LoginActivity.this, MainActivity.class);
        else
            i = new Intent(LoginActivity.this, MainActivity.class);

        i.putExtra("username",useremail);
        startActivity(i);
        finish();

    }




    public void checkcurrentlogin(){
        user = mAuth.getCurrentUser();
        if(user!=null){

            if(user.isEmailVerified())
                intentme();

//            else
//                showSnackBar(coordinatorLayout);
        }



    }

    public void putImageOnImageView(int id){
        ImageView imageView = (ImageView) findViewById(id);
    }

    public void showSnackBar(final View coordinator_home){
        Snackbar snackbar = Snackbar.make(coordinator_home, "Please verify your email address first.",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Send Verification Link", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification();
                loginlayout.setAlpha(1f);
                // ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, GET_LOCATIONREQUEST);

            }
        }).setActionTextColor(Color.RED);

        View snackview = snackbar.getView();

        TextView textView = snackview.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        textView.setLines(3);
        snackbar.show();

    }

    public void HandleOnClicks(){

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(el.getText()) || TextUtils.isEmpty(pl.getText())){
                    Toast.makeText(LoginActivity.this,"Fill up both the fields before clicking",Toast.LENGTH_LONG).show();
                }
                else {
                    loginlayout.setAlpha(0.4f);
                    loginlayout.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);

                    email = (el.getText().toString());
                    pass  = pl.getText().toString();

                    mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                user = mAuth.getCurrentUser();

                                progressBar.setVisibility(View.GONE);
                                if(user.isEmailVerified())
                                    intentme();
                                else
                                    showSnackBar(coordinatorLayout);
                            }

                            else {
                                loginlayout.setAlpha(1.0f);
                                loginlayout.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginlayout.setVisibility(View.GONE);
                signuplayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

            }
        });

        next1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next1.setText("Wait...");
                next1.setEnabled(false);

                RadioButton positive = (RadioButton)findViewById(R.id.positive);
                RadioButton negative = (RadioButton)findViewById(R.id.negative);

                if(TextUtils.isEmpty(es.getText()) || TextUtils.isEmpty(ps.getText()) || TextUtils.isEmpty(confirmps.getText())){
                    Toast.makeText(LoginActivity.this,"Fill up all the fields before clicking",Toast.LENGTH_LONG).show();
                    next1.setText("Next");
                    next1.setEnabled(true);
                }else if(!(negative.isChecked() || positive.isChecked()))
                    Toast.makeText(context,"Please select atleast one of Caretaker, Doctor or Patient.",Toast.LENGTH_SHORT).show();
                else {
                    String s_email = es.getText().toString();
                    String s_pass = ps.getText().toString();
                    String s_confirm = confirmps.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);


                    if(s_pass.compareTo(s_confirm)==0){
                        mAuth.createUserWithEmailAndPassword(s_email,s_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    user =mAuth.getCurrentUser();

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                    RadioButton positive = (RadioButton)findViewById(R.id.positive);
                                    RadioButton negative = (RadioButton)findViewById(R.id.negative);

                                    String email = (es.getText().toString().replace("."," "));
                                    if(positive.isChecked()){
                                        databaseReference.child("Caretaker").child(email).child("Alerts").setValue("ON");
                                    }
                                    else if(negative.isChecked()){
                                        databaseReference.child("SeizurePatients").child(email).child("Alerts").setValue("ON");
                                    }

                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this,"Account Created Successfully",Toast.LENGTH_LONG).show();
                                    es.setText("");
                                    ps.setText("");
                                    confirmps.setText("");
                                    loginlayout.setVisibility(View.VISIBLE);
                                    signuplayout.setVisibility(View.GONE);

                                }
                                else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                next1.setText("Next");
                                next1.setEnabled(true);
                            }
                        });
                    }
                    else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Password and Confirm Password does not match", Toast.LENGTH_LONG).show();
                    }

                }
                next1.setText("Next");
                next1.setEnabled(true);
            }
        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotpass.setEnabled(false);
                if(TextUtils.isEmpty(el.getText()))
                    Toast.makeText(LoginActivity.this,"Please enter the email whose password you need to reset",Toast.LENGTH_LONG).show();
                else {
                    mAuth.sendPasswordResetEmail(el.getText().toString()).addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this,"Please check your email now if you want to reset your password .",Toast.LENGTH_LONG).show();

                                    }
                                    else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(LoginActivity.this, "Error occured : "+ error, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                    );

                }
                forgotpass.setEnabled(true);
            }
        });

    }

    public void allfindviewbyids(){

        loginlayout = (ConstraintLayout)findViewById(R.id.login_layout);
        signuplayout = (ConstraintLayout)findViewById(R.id.signup_layout);
        el=(EditText)findViewById(R.id.email_teacher);
        pl=(EditText)findViewById(R.id.pass_teacher);
        es=(EditText)findViewById(R.id.s_email);
        ps=(EditText)findViewById(R.id.s_pass);
        confirmps=(EditText)findViewById(R.id.s_confirm);
        signup=(TextView)findViewById(R.id.signup_teacher);
        forgotpass=(TextView)findViewById(R.id.forgotpass_teacher);
        login = (Button)findViewById(R.id.login_teacher);
        next1 = (Button)findViewById(R.id.s_next1);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        //introviewFlipper = (ViewFlipper)findViewById(R.id.introviewflipper);
        //introlayout_relative = (RelativeLayout)findViewById(R.id.introlayout_relative);
        //showNext = (Button)findViewById(R.id.showNext);
        //showPrevious = (Button)findViewById(R.id.showPrevious);

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
    }

}
