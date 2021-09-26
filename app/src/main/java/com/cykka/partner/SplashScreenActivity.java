package com.cykka.partner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_splash_screen);

        Log.d("Activity", TAG);

        mAuth = FirebaseAuth.getInstance();
        mProgressBar = findViewById(R.id.progressBar);
        Button mSignUpButton = findViewById(R.id.bt_sign_up);
        LinearLayout mSignInButton = findViewById(R.id.ll_sign_in_text);
        LinearLayout linearLayout = findViewById(R.id.ll1);
        Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in);
        linearLayout.startAnimation(aniSlide);

        //Open Sign Up Activity
        mSignUpButton.setOnClickListener(v -> {
            startActivity(new Intent(SplashScreenActivity.this, SignUpActivity.class));
            finish();
        });

        //Open Sign In Activity
        mSignInButton.setOnClickListener(v -> {
            startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
            finish();
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        //Check for user already logged in on app start
        //If yes divert to Main Activity

        mProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            removeProgressBar();
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();
        }else{
            removeProgressBar();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        removeProgressBar();
    }

    private void removeProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
