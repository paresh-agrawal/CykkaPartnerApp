package com.cykka.partner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private String mPhoneNumber;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private LinearLayout llSignIn;
    private LinearLayout llOTPVerification;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;

    private TextView tvPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_sign_in);

        Log.d("Activity", TAG);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        FirebaseApp.initializeApp(this);


        mPhoneNumberField = findViewById(R.id.et_phone_number);
        mVerificationField = findViewById(R.id.et_otp_code);

        Button mStartButton = findViewById(R.id.bt_get_otp);
        Button mVerifyButton = findViewById(R.id.bt_verify_otp);
        TextView mResendButton = findViewById(R.id.tv_resend_otp);
        ImageView mEditNumber = findViewById(R.id.iv_edit_number);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        LinearLayout tvSignUp = findViewById(R.id.ll_sign_up_text);

        llSignIn = findViewById(R.id.ll_sign_in);
        llOTPVerification = findViewById(R.id.ll_otp_verification);

        // Assign click listeners
        mStartButton.setOnClickListener(this);
        mVerifyButton.setOnClickListener(this);
        mResendButton.setOnClickListener(this);
        mEditNumber.setOnClickListener(this);
        tvSignUp.setOnClickListener(this);
        //mSignOutButton.setOnClickListener(this);


        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mPhoneNumberField.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }


    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        /*if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification("+91" + mPhoneNumberField.getText().toString());
        }*/
        if (mVerificationInProgress) {
            mVerificationInProgress = false;
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }


    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        updateUI(STATE_CODE_SENT,null,null);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        // [START_EXCLUDE]
                        updateUI(STATE_SIGNIN_SUCCESS, user);
                        // [END_EXCLUDE]
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // [START_EXCLUDE silent]
                            mVerificationField.setError("Invalid code.");
                            // [END_EXCLUDE]
                        }
                        // [START_EXCLUDE silent]
                        // Update UI
                        updateUI(STATE_SIGNIN_FAILED);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END sign_in_with_phone]


    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                llSignIn.setVisibility(View.VISIBLE);
                llOTPVerification.setVisibility(View.GONE);
                break;
            case STATE_CODE_SENT:
            case STATE_SIGNIN_SUCCESS:
                // Verification has failed, show all options
                // Code sent state, show the verification field, the
                llSignIn.setVisibility(View.GONE);
                llOTPVerification.setVisibility(View.VISIBLE);
                break;
            case STATE_VERIFY_FAILED:
                llSignIn.setVisibility(View.GONE);
                llOTPVerification.setVisibility(View.VISIBLE);
                Snackbar.make(findViewById(android.R.id.content), "Make sure you entered correct phone number.",
                        Snackbar.LENGTH_SHORT).show();
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                //start new activity
                llSignIn.setVisibility(View.GONE);
                llOTPVerification.setVisibility(View.VISIBLE);
                //mDetailText.setText("verification succeeded");

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mVerificationField.setText(cred.getSmsCode());
                    } else {
                        mVerificationField.setText("******");
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                llSignIn.setVisibility(View.GONE);
                llOTPVerification.setVisibility(View.VISIBLE);
                Snackbar.make(getWindow().getDecorView(), "Sign up failed. Incorrect OTP or phone number.", Snackbar.LENGTH_LONG).show();
                break;
        }

        if(user!=null){
            // Signed in
            llSignIn.setVisibility(View.GONE);
            llOTPVerification.setVisibility(View.VISIBLE);

            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.putExtra("PARENT_ACTIVITY_TAG", SignInActivity.TAG);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    private void validatePhoneNumber() {
        mPhoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(mPhoneNumber) || mPhoneNumberField.getText().toString().length()!=10) {
            mPhoneNumberField.setError("Invalid phone number.");
        }else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            DocumentReference docIdRef = db.collection("AdvisorsPhoneNumbers").document("Users");
            if (isNetworkAvailable(getApplicationContext())) {
                docIdRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            if (document.get(mPhoneNumber) != null) {
                                Log.d(TAG, "your field exist");
                                progressDialog.dismiss();
                                tvPhoneNumber.setText("+91 " + mPhoneNumber);
                                startPhoneNumberVerification("+91" + mPhoneNumber);

                            } else {
                                progressDialog.dismiss();
                                mPhoneNumberField.setError("User with this phone number do not exist");
                                Log.d(TAG, "your field does not exist");
                                //Create the filed
                            }
                        }
                    }
                });
            }else {
                progressDialog.dismiss();
                Toast.makeText(SignInActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return Objects.requireNonNull(connectivityManager).getActiveNetworkInfo() != null && Objects.requireNonNull(connectivityManager.getActiveNetworkInfo()).isConnected();
    }

    @Override
    public void onClick(View v) {
        hideKeyboard(getApplicationContext(),v);
        switch (v.getId()) {
            case R.id.bt_get_otp:
                /*if (!validatePhoneNumber()) {
                    return;
                }else{
                    startPhoneNumberVerification("+91"+mPhoneNumber);
                }*/
                validatePhoneNumber();
                //updateUI(STATE_CODE_SENT,null,null);

                break;
            case R.id.bt_verify_otp:
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }

                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.iv_edit_number:
                updateUI(STATE_INITIALIZED);
                break;
            case R.id.tv_resend_otp:
                resendVerificationCode("+91"+mPhoneNumber, mResendToken);
                break;
            case R.id.ll_sign_up_text:
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                intent.putExtra("PARENT_ACTIVITY_TAG", SignInActivity.TAG);
                startActivity(intent);
                finish();
                break;


        }
    }
    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SignInActivity.this, SplashScreenActivity.class);
        startActivity(intent);
        finish();
    }
}

