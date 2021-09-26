package com.cykka.partner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileDetailsActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE = 100;


    private static final String TAG = "ProfileDetailsActivity";
    private RelativeLayout rlPrimarySpecialisation, rlSecondarySpecialisation, rlPersonalInfo;
    private View view1, view2;
    private ImageView ivCirclePrimary, ivCircleSecondary, ivCirclePersonalInfo;
    private Button btReset, btContinue, btBack;
    private int layoutInt;
    private RadioGroup radioGroup1, radioGroup2;
    private CheckBox cbStocksFundamental, cbStocksTechnical, cbBondsDebtMarket, cbBitcoinCryptoTrading,
                        cbSilverGoldBullion, cbCurrencyForexTrading, cbCommodity, cbInsurance,
                        cbRetirementPlanning, cbTaxPlanning, cbLoanSpecialist, cbMutualFunds;
    private int radioButtonBuffer=0;
    //private RelativeLayout rlAddImage;
    //private CircleImageView ivProfileImage;
    private TextInputEditText etName, etWatsappNo, etEmailId, etZipCode;
    private String name, number, emailId, zipCode;
    private boolean imageSet;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String primarySpecialisation;
    private String parentActivity;
    //private String[] secondarySpecialisation;
    private List<String> secondarySpecialisation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_profile_details);

        init();
        parentActivity = getIntent().getStringExtra("PARENT_ACTIVITY_TAG");
        layoutInt=1;
        setLayout(layoutInt);


        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(radioButtonBuffer==0){
                    radioButtonBuffer=1;
                    return;
                }else if(radioButtonBuffer==2){
                    radioButtonBuffer=3;
                    radioGroup2.clearCheck();
                    radioButtonBuffer=1;
                }
            }
        });


        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (radioButtonBuffer==0){
                    radioButtonBuffer=2;
                }else if(radioButtonBuffer==1){
                    radioButtonBuffer=3;
                    radioGroup1.clearCheck();
                    radioButtonBuffer=2;
                }
            }
        });

        btContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getApplicationContext(),v);
                if(radioButtonBuffer==0 && layoutInt==1){
                    Snackbar.make(v, "Please select one", Snackbar.LENGTH_LONG).show();
                }else if(layoutInt==3){
                    name = etName.getText().toString();
                    number = etWatsappNo.getText().toString();
                    emailId = etEmailId.getText().toString();
                    zipCode = etZipCode.getText().toString();
                    if(checkProfileInfoInput()){
                        //Snackbar.make(getWindow().getDecorView(), "Profile updated.", Snackbar.LENGTH_LONG).show();
                        //startActivity(new Intent(ProfileDetailsActivity.this, IdentityVerificationActivity.class));
                        uploadProfileDetails();
                    }else{
                        Snackbar.make(getWindow().getDecorView(), "Please fill all the details.", Snackbar.LENGTH_LONG).show();
                    }

                }else{
                    //Log.d("bufferValue", String.valueOf(radioButtonBuffer));
                    if(layoutInt==1){
                        primarySpecialisation = getPrimarySpecialisationString(radioButtonBuffer);
                    }else if(layoutInt==2){
                        secondarySpecialisation = getSecondarySpecialisationArray();
                    }
                    setLayout(++layoutInt);
                }

            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(getApplicationContext(),v);
                setLayout(--layoutInt);
            }
        });

        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layoutInt==1 && radioButtonBuffer!=0){
                    radioButtonReset();
                }else if(layoutInt==2){
                    checkBoxReset();
                    return;
                }else if(layoutInt==3){
                    personalInfoReset();
                }
            }
        });

        /*rlAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(getWindow().getDecorView());
            }
        });*/


    }

    private void uploadProfileDetails() {

        Map<String, Object> userProfileDetails = new HashMap<>();
        userProfileDetails.put("PrimarySpecialisation", primarySpecialisation);
        userProfileDetails.put("SecondarySpecialisation", secondarySpecialisation);
        userProfileDetails.put("Name", name);
        userProfileDetails.put("WhatsAppNumber", number);
        userProfileDetails.put("EmailId", emailId);
        userProfileDetails.put("ZipCode", zipCode);
        userProfileDetails.put("ProfileDetailBool", true);

        Log.d("secondary", String.valueOf(secondarySpecialisation));

        final ProgressDialog progressDialog = new ProgressDialog(ProfileDetailsActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        db.collection("AdvisorsDatabase").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .update(userProfileDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        progressDialog.dismiss();
                        if (parentActivity.equals("SignUpActivity")) {
                            startActivity(new Intent(ProfileDetailsActivity.this, IdentityVerificationActivity.class));
                            finish();
                        }else {
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    private List<String> getSecondarySpecialisationArray() {
        List<String> secondarySpecResult = new ArrayList<>();
        //String[] secondarySpecResult = new String[];
        String str;
        if (cbStocksFundamental.isChecked()){
            str = getSpecialisationText(cbStocksFundamental.getText().toString());
            Log.d("str", str + " + prim - " + primarySpecialisation);
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbStocksTechnical.isChecked()){
            str = getSpecialisationText(cbStocksTechnical.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbBondsDebtMarket.isChecked()){
            str = getSpecialisationText(cbBondsDebtMarket.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbBitcoinCryptoTrading.isChecked()){
            str = getSpecialisationText(cbBitcoinCryptoTrading.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbSilverGoldBullion.isChecked()){
            str = getSpecialisationText(cbSilverGoldBullion.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbCurrencyForexTrading.isChecked()){
            str = getSpecialisationText(cbCurrencyForexTrading.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbCommodity.isChecked()){
            str = getSpecialisationText(cbCommodity.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbInsurance.isChecked()){
            str = getSpecialisationText(cbInsurance.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbRetirementPlanning.isChecked()){
            str = getSpecialisationText(cbRetirementPlanning.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbTaxPlanning.isChecked()){
            str = getSpecialisationText(cbTaxPlanning.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbLoanSpecialist.isChecked()){
            str = getSpecialisationText(cbLoanSpecialist.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (cbMutualFunds.isChecked()){
            str = getSpecialisationText(cbMutualFunds.getText().toString());
            if(!str.equals(primarySpecialisation)){
                secondarySpecResult.add(str);
            }
        }if (secondarySpecResult.isEmpty()){
            secondarySpecResult.add("NoSecondarySpecialisation");
        }

        return secondarySpecResult;
    }

    private String getPrimarySpecialisationString(int grpValue) {
        String primarySpecResult = "";
        int selectedId = -1;
        RadioButton rb;
        if(grpValue == 1){
            selectedId = radioGroup1.getCheckedRadioButtonId();
        }else if(grpValue ==2){
            selectedId = radioGroup2.getCheckedRadioButtonId();
        }

        if(selectedId!=-1){
            rb = findViewById(selectedId);
            primarySpecResult = getSpecialisationText(rb.getText().toString());
        }

        return primarySpecResult;
    }

    private String getSpecialisationText(String str) {
        if(str.equals("Stocks (Fundamental)")){return "StocksFundamental";}
        else if(str.equals("Stocks (Technical)")){return "StocksTechnical";}
        else if(str.equals("Bonds/Debt Market")){return "BondsDebtMarket";}
        else if(str.equals("Bitcoin/Crypto Trading")){return "BitcoinCryptoTrading";}
        else if(str.equals("Silver/Gold (Bullion)")){return "SilverGoldBullion";}
        else if(str.equals("Currency/Forex Trading")){return "CurrencyForexTrading";}
        else if(str.equals("Commodity")){return "Commodity";}
        else if(str.equals("Insurance")){return "Insurance";}
        else if(str.equals("Retirement Planning")){return "RetirementPlanning";}
        else if(str.equals("Tax Planning")){return "TaxPlanning";}
        else if(str.equals("Loan Specialist")){return "LoanSpecialist";}
        else if(str.equals("Mutual Funds")){return "MutualFunds";}
        return "";
    }

    private void personalInfoReset() {
        imageSet=false;
        /*ivProfileImage.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
        ivProfileImage.setPadding(24,24,24,24);*/
        etName.getText().clear();
        etWatsappNo.getText().clear();
        etEmailId.getText().clear();
        etZipCode.getText().clear();
    }

    private boolean checkProfileInfoInput() {
        int errorCount = 0;
        /*if (!imageSet){
            Snackbar.make(getWindow().getDecorView(), "Please select a photo.", Snackbar.LENGTH_LONG).show();
            errorCount++;
        }*/
        if(TextUtils.isEmpty(name)){
            etName.setError("Please enter your name");
            errorCount++;
        }

        if(TextUtils.isEmpty(number)){
            etWatsappNo.setError("Please enter your whatsapp number");
            errorCount++;
        }else if(number.length()!=10){
            etWatsappNo.setError("Please enter a valid number.");
            errorCount++;
        }

        if(TextUtils.isEmpty(emailId)){
            etEmailId.setError("Please enter your email id");
            errorCount++;
        }else if(!isEmailValid(emailId)){
            etEmailId.setError("Enter a valid emailId.");
            errorCount++;
        }

        if(TextUtils.isEmpty(zipCode)){
            etZipCode.setError("Enter a zip code");
            errorCount++;
        }else if(zipCode.length()!=6){
            etZipCode.setError("Please enter a valid zip code");
            errorCount++;
        }
        if(errorCount>0){
            return false;
        }else{
            return true;
        }

    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkBoxReset() {
        cbStocksFundamental.setChecked(false);
        cbStocksTechnical.setChecked(false);
        cbBondsDebtMarket.setChecked(false);
        cbBitcoinCryptoTrading.setChecked(false);
        cbSilverGoldBullion.setChecked(false);
        cbCurrencyForexTrading.setChecked(false);
        cbCommodity.setChecked(false);
        cbInsurance.setChecked(false);
        cbRetirementPlanning.setChecked(false);
        cbTaxPlanning.setChecked(false);
        cbLoanSpecialist.setChecked(false);
        cbMutualFunds.setChecked(false);
    }

    private void radioButtonReset() {
        if(radioButtonBuffer==1){
            radioGroup1.clearCheck();
            radioButtonBuffer=0;
        }
        else if(radioButtonBuffer==2){
            radioGroup2.clearCheck();
            radioButtonBuffer=0;
        }
    }

    /*private void loadProfile(String url) {
        Log.d(TAG, "Image cache path: " + url);

        Glide.with(this).load(url)
                .into(ivProfileImage);
        ivProfileImage.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
    }*/

    public void onSelectImageClick(View view) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener(){
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(ProfileDetailsActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(ProfileDetailsActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    // loading profile image from local cache
                    //loadProfile(uri.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /*@Override
    protected void onStart() {
        super.onStart();
    }*/

    private void setLayout(int i) {
        switch (i){
            case 1:
                rlPrimarySpecialisation.setVisibility(View.VISIBLE);
                rlSecondarySpecialisation.setVisibility(View.GONE);
                rlPersonalInfo.setVisibility(View.GONE);

                view1.setBackgroundColor(getColor(R.color.inactive_grey));
                view2.setBackgroundColor(getColor(R.color.inactive_grey));

                ivCirclePrimary.setBackground(getDrawable(R.drawable.ic_circle_active));
                ivCircleSecondary.setBackground(getDrawable(R.drawable.ic_circle));
                ivCirclePersonalInfo.setBackground(getDrawable(R.drawable.ic_circle));

                btBack.setVisibility(View.GONE);
                btContinue.setText("Continue");
                break;

            case 2:
                rlPrimarySpecialisation.setVisibility(View.GONE);
                rlSecondarySpecialisation.setVisibility(View.VISIBLE);
                rlPersonalInfo.setVisibility(View.GONE);

                view1.setBackgroundColor(getColor(R.color.active_blue));
                view2.setBackgroundColor(getColor(R.color.inactive_grey));

                ivCirclePrimary.setBackground(getDrawable(R.drawable.ic_circle_done));
                ivCircleSecondary.setBackground(getDrawable(R.drawable.ic_circle_active));
                ivCirclePersonalInfo.setBackground(getDrawable(R.drawable.ic_circle));

                btBack.setVisibility(View.VISIBLE);
                btContinue.setText("Continue");
                break;

            case 3:
                rlPrimarySpecialisation.setVisibility(View.GONE);
                rlSecondarySpecialisation.setVisibility(View.GONE);
                rlPersonalInfo.setVisibility(View.VISIBLE);

                view1.setBackgroundColor(getColor(R.color.done_green));
                view2.setBackgroundColor(getColor(R.color.active_blue));

                ivCirclePrimary.setBackground(getDrawable(R.drawable.ic_circle_done));
                ivCircleSecondary.setBackground(getDrawable(R.drawable.ic_circle_done));
                ivCirclePersonalInfo.setBackground(getDrawable(R.drawable.ic_circle_active));

                btBack.setVisibility(View.VISIBLE);
                btContinue.setText("Submit");
                break;
        }
    }

    private void init() {
        rlPersonalInfo = findViewById(R.id.rl_details);
        rlPrimarySpecialisation = findViewById(R.id.rl_primary);
        rlSecondarySpecialisation = findViewById(R.id.rl_secondary);

        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);

        ivCirclePrimary = findViewById(R.id.circle_primary);
        ivCircleSecondary = findViewById(R.id.circle_secondary);
        ivCirclePersonalInfo = findViewById(R.id.circle_details);

        btReset = findViewById(R.id.bt_reset);
        btContinue = findViewById(R.id.bt_continue);
        btBack = findViewById(R.id.bt_back);

        radioGroup1 = findViewById(R.id.radio_group_primary1);
        radioGroup2 = findViewById(R.id.radio_group_primary2);

        cbStocksFundamental = findViewById(R.id.checkboxStocksFundamental);
        cbStocksTechnical = findViewById(R.id.checkboxStocksTechnical);
        cbBondsDebtMarket = findViewById(R.id.checkboxBondsDebtMarket);
        cbBitcoinCryptoTrading = findViewById(R.id.checkboxBitcoinCryptoTrading);
        cbSilverGoldBullion = findViewById(R.id.checkboxSilverGoldBullion);
        cbCurrencyForexTrading = findViewById(R.id.checkboxCurrencyForexTraining);
        cbCommodity = findViewById(R.id.checkboxCommodity);
        cbInsurance = findViewById(R.id.checkboxInsurance);
        cbRetirementPlanning = findViewById(R.id.checkboxRetirementPlanning);
        cbTaxPlanning = findViewById(R.id.checkboxTaxPlanning);
        cbLoanSpecialist = findViewById(R.id.checkboxLoanSpecialist);
        cbMutualFunds = findViewById(R.id.checkboxMutualFunds);

        //ivProfileImage = findViewById(R.id.profile_image);
        //rlAddImage = findViewById(R.id.rl_add_image);

        etName = findViewById(R.id.et_name);
        etWatsappNo = findViewById(R.id.et_whatsapp);
        etEmailId = findViewById(R.id.et_email_id);
        etZipCode = findViewById(R.id.et_zipcode);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileDetailsActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}
