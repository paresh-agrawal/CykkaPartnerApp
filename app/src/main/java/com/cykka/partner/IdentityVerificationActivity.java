package com.cykka.partner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentityVerificationActivity extends AppCompatActivity {

    private CheckBox cbTermsAndConditions;
    private CardView cvSpecialisation, cvPersonalInfo, cvPhotoId, cvAddressProof, cvFaceVerification, cvAwardsAndCertificate, cvBankDetails, cvOtherDetails;
    private ImageView ivSpecialisation, ivPersonalInfo, ivPhotoId, ivAddressProof, ivFaceVerification, ivAwardsAndCertificate, ivBankDetails, ivOtherDetails;
    private Button btSubmit;

    private int rbPhotoIdTypeId = -1 , rbAddressProofTypeId = -1, rbSebiNumber = -1;
    private String name = null, accountNumber = null, confirmAccountNumber = null, ifscCode = null;
    private int spinnerItemNumber = 0;
    private String languages = null, sebiNumber = null;
    private Uri uriPhotoIdFront = null, uriPhotoIdBack = null;
    private Uri uriAddressProofFront = null, uriAddressProofBack = null;
    private Uri uriFaceVerification = null;
    private Uri uriCancelledChequeImage = null;
    private Uri uriAwardsImage1 = null, uriAwardsImage2 = null, uriAwardsImage3 = null, uriAwardsImage4 = null, uriAwardsImage5 = null,
                    uriAwardsImage6 = null, uriAwardsImage7 = null, uriAwardsImage8 = null, uriAwardsImage9 = null;
    private ImageView imageView = null;
    private Uri uriImage = null;
    private int radioButtonBuffer=0;

    public static int REQUEST_IMAGE = 0;

    private TextView tvUploadCount, tvSkip;
    private int intUploadCount = 0;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private UploadTask upload_task, thumb_upload_task;
    private Bitmap compressedThumbImageBitmap,compressedImageBitmap;
    private Dialog dialog;
    private Handler handler;
    private Runnable runnable;
    private ImageView ivBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_identity_verification);

        init();

        String text = "I have read and accept Cykka's Terms of Use and Privacy Policy.";
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (cbTermsAndConditions.isChecked()){
                    cbTermsAndConditions.setChecked(false);
                }else {
                    cbTermsAndConditions.setChecked(true );
                }
                //Toast.makeText(IdentityVerificationActivity.this, "Terms and Conditions", Toast.LENGTH_SHORT).show();
                showTermsAndCondition();
            }
        };

        spannableString.setSpan(clickableSpan1, 31,62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cbTermsAndConditions.setText(spannableString);
        cbTermsAndConditions.setMovementMethod(LinkMovementMethod.getInstance());


        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cvSpecialisation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSpecialisation();
            }
        });

        cvPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPersonalInfo();
            }
        });

        cvPhotoId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhotoId();
            }
        });
        cvAddressProof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAddressProof();
            }
        });
        cvFaceVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFacePhoto();
            }
        });
        cvAwardsAndCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAwardsAndCertificatePhotos();
            }
        });
        cvBankDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBankDetails();
            }
        });
        cvOtherDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOtherDetails();
            }
        });

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerActivity.clearCache(IdentityVerificationActivity.this);
                startActivity(new Intent(IdentityVerificationActivity.this, MainActivity.class));
                finish();
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intUploadCount!=8){
                    Snackbar.make(getWindow().getDecorView(), "Upload all documents first.", Snackbar.LENGTH_LONG).show();
                } else if (!cbTermsAndConditions.isChecked()){
                    Snackbar.make(getWindow().getDecorView(), "Accept the terms and conditions to continue.", Snackbar.LENGTH_LONG).show();
                }else{
                    readyForManualVerification();
                }
            }
        });

        uploadedPhotoBool();

    }

    private void showTermsAndCondition() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_terms_and_condition);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                TextView tv = dialog.findViewById(R.id.tv_t_n_c);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                ImageView btFullScroll = dialog.findViewById(R.id.iv_scroll_full);
                Button btAccept = dialog.findViewById(R.id.bt_accept);
                ScrollView scrollView = dialog.findViewById(R.id.sv_tnc);

                Spanned sp = Html.fromHtml( getString(R.string.htmlsource1) + getString(R.string.htmlsource2)
                        + getString(R.string.htmlsource3) + getString(R.string.htmlsource4)
                        + getString(R.string.htmlsource5) + getString(R.string.htmlsource6));
                tv.setText(sp);

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cbTermsAndConditions.setChecked(true );
                        dialog.dismiss();
                    }
                });

                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        btFullScroll.setVisibility(View.GONE);
                    }
                };
                scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        handler.removeCallbacks(runnable);
                        btFullScroll.setVisibility(View.VISIBLE);
                        // DO SOMETHING WITH THE SCROLL COORDINATES

                        handler.postDelayed(runnable, 3000);
                        //btAccept.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
                    }

                });

                btFullScroll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        btFullScroll.setVisibility(View.GONE);
                    }
                });
            }
        });


        dialog.show();
    }

    private void readyForManualVerification() {
        final ProgressDialog progressDialog = new ProgressDialog(IdentityVerificationActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        if (isNetworkAvailable(getApplicationContext())){
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put("DocumentsUploadedBool", true);
                        docRef.update(dataMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        ImagePickerActivity.clearCache(IdentityVerificationActivity.this);
                                        Intent intent = new Intent(IdentityVerificationActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.w("TAG", "Error writing document", e);
                                    }
                                });
                    }else {

                    }
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(IdentityVerificationActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadedPhotoBool() {
        final DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(mAuth.getCurrentUser().getUid())
                .collection("IdProofs").document("PhotoBoolean");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> snapdata = snapshot.getData();

                    updateDoneIcon(snapdata);
                    Log.d("SnapData", source + " data: " + snapdata.get("PhotoId"));
                    Log.d("TAG", source + " data: " + snapshot.getData());
                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });
    }

    private void addSpecialisation() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_specialisation);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                //Button btSave = dialog.findViewById(R.id.bt_save);
                Button btNext = dialog.findViewById(R.id.bt_next);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                /*RadioGroup rgSpec1 = dialog.findViewById(R.id.radio_group_primary1);
                RadioGroup rgSpec2 = dialog.findViewById(R.id.radio_group_primary2);
                radioButtonBuffer = 0;*/
                int checkboxCount = 0;
                int specPrizeCount = 0;
                final boolean[] onSpecSelectScreen = {true};
                Map<String, List<String>> specialisationList = new HashMap<String, List<String>>();
                Iterator<Map.Entry<String, List<String>>> specListIterator;


                TextView tvError = dialog.findViewById(R.id.tv_error);
                TextView tvSpecDesc = dialog.findViewById(R.id.tv_spec_desc);
                TextView tvSubDesc = dialog.findViewById(R.id.tv_sub_desc);
                TextView tvSubSpec = dialog.findViewById(R.id.tv_sub_spec);

                LinearLayout llSubPlans = dialog.findViewById(R.id.ll_sub_plans);
                LinearLayout llSpecs = dialog.findViewById(R.id.ll_specs);

                CheckBox cbStocksTechnical = dialog.findViewById(R.id.checkboxStocksTechnical);
                CheckBox cbStocksFundamental  = dialog.findViewById(R.id.checkboxStocksFundamental);
                CheckBox cbBondsDebtMarket = dialog.findViewById(R.id.checkboxBondsDebtMarket);
                CheckBox cbBitcoinCryptoTrading  = dialog.findViewById(R.id.checkboxBitcoinCryptoTrading);
                CheckBox cbSilverGoldBullion = dialog.findViewById(R.id.checkboxSilverGoldBullion);
                CheckBox cbCurrencyForexTrading = dialog.findViewById(R.id.checkboxCurrencyForexTraining);
                CheckBox cbCommodity = dialog.findViewById(R.id.checkboxCommodity);
                CheckBox cbInsurance = dialog.findViewById(R.id.checkboxInsurance);
                CheckBox cbRetirementPlanning = dialog.findViewById(R.id.checkboxRetirementPlanning);
                CheckBox cbTaxPlanning = dialog.findViewById(R.id.checkboxTaxPlanning);
                CheckBox cbLoanSpecialist = dialog.findViewById(R.id.checkboxLoanSpecialist);
                CheckBox cbMutualFunds = dialog.findViewById(R.id.checkboxMutualFunds);

                TextInputEditText etSub1Month = dialog.findViewById(R.id.et_sub_1_month);
                TextInputEditText etSub3Month = dialog.findViewById(R.id.et_sub_3_month);
                TextInputEditText etSub6Month = dialog.findViewById(R.id.et_sub_6_month);
                TextInputEditText etSub12Month = dialog.findViewById(R.id.et_sub_12_month);

                tvSpecDesc.setVisibility(View.VISIBLE);
                tvSubDesc.setVisibility(View.GONE);
                tvSubSpec.setVisibility(View.GONE);
                llSubPlans.setVisibility(View.GONE);
                llSpecs.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvError.setVisibility(View.GONE);
                    }
                };



                /*rgSpec1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        if(radioButtonBuffer==0){
                            radioButtonBuffer=1;
                            return;
                        }else if(radioButtonBuffer==2){
                            radioButtonBuffer=3;
                            rgSpec2.clearCheck();
                            radioButtonBuffer=1;
                        }
                    }
                });


                rgSpec2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        if (radioButtonBuffer==0){
                            radioButtonBuffer=2;
                        }else if(radioButtonBuffer==1){
                            radioButtonBuffer=3;
                            rgSpec1.clearCheck();
                            radioButtonBuffer=2;
                        }
                    }
                });*/

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*if(radioButtonBuffer==1){
                            rgSpec1.clearCheck();
                            radioButtonBuffer=0;
                        }
                        else if(radioButtonBuffer==2){
                            rgSpec2.clearCheck();
                            radioButtonBuffer=0;
                        }*/
                        if (checkboxCount != 0 && onSpecSelectScreen[0]){
                            //remove all specs, clear list
                            specialisationList.clear();
                            cbStocksTechnical.setChecked(false);
                            cbStocksFundamental.setChecked(false);
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
                        }else if (!onSpecSelectScreen[0]){
                            etSub1Month.setText("");
                            etSub3Month.setText("");
                            etSub6Month.setText("");
                            etSub12Month.setText("");
                        }
                    }
                });

                btNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        List<String> subDefaultValues = new ArrayList<>();
                        subDefaultValues.add("0");
                        subDefaultValues.add("0");
                        subDefaultValues.add("0");
                        subDefaultValues.add("0");

                        if (onSpecSelectScreen[0]){
                            if (! (cbStocksTechnical.isChecked()|| cbStocksFundamental.isChecked() || cbBondsDebtMarket.isChecked() || cbBitcoinCryptoTrading.isChecked() ||
                                    cbSilverGoldBullion.isChecked() || cbCurrencyForexTrading.isChecked() || cbCommodity.isChecked() || cbInsurance.isChecked() ||
                                    cbRetirementPlanning.isChecked() || cbTaxPlanning.isChecked() || cbLoanSpecialist.isChecked() || cbMutualFunds.isChecked())){
                                tvError.setText("Please select one.");
                                tvError.setVisibility(View.VISIBLE);
                                handler.postDelayed(runnable, 4000);
                            }else {
                                if (cbStocksTechnical.isChecked()){
                                    String spec = getSpecialisationText(cbStocksTechnical.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbStocksFundamental.isChecked()){
                                    String spec = getSpecialisationText(cbStocksFundamental.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbBondsDebtMarket.isChecked()){
                                    String spec = getSpecialisationText(cbBondsDebtMarket.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbBitcoinCryptoTrading.isChecked()){
                                    String spec = getSpecialisationText(cbBitcoinCryptoTrading.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbSilverGoldBullion.isChecked()){
                                    String spec = getSpecialisationText(cbSilverGoldBullion.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbCurrencyForexTrading.isChecked()){
                                    String spec = getSpecialisationText(cbCurrencyForexTrading.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbCommodity.isChecked()){
                                    String spec = getSpecialisationText(cbCommodity.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbInsurance.isChecked()){
                                    String spec = getSpecialisationText(cbInsurance.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbRetirementPlanning.isChecked()){
                                    String spec = getSpecialisationText(cbRetirementPlanning.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbTaxPlanning.isChecked()){
                                    String spec = getSpecialisationText(cbTaxPlanning.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbLoanSpecialist.isChecked()){
                                    String spec = getSpecialisationText(cbLoanSpecialist.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }if (cbMutualFunds.isChecked()){
                                    String spec = getSpecialisationText(cbMutualFunds.getText().toString());
                                    specialisationList.put(spec, subDefaultValues);
                                }
                                tvSpecDesc.setVisibility(View.GONE);
                                tvSubDesc.setVisibility(View.VISIBLE);
                                tvSubSpec.setVisibility(View.VISIBLE);
                                llSubPlans.setVisibility(View.VISIBLE);
                                llSpecs.setVisibility(View.GONE);
                                onSpecSelectScreen[0] = false;
                                //String str = specialisationList.keySet().stream().findFirst().get();
                                //Map.Entry<String,List<Integer>> entry = specialisationList.entrySet().iterator().next();

                                //specialisationList.
                                Map.Entry<String, List<String>> entry = specListIterator.next();
                                //String key = specialisationList.get(specialisationList.keySet().toArray()[0]);
                                Log.d("Keys ", String.valueOf(specialisationList.size()));
                                Log.d("All", String.valueOf(specialisationList));
                                tvSubSpec.setText(entry.getKey());
                            }
                        }if (!onSpecSelectScreen[0]){
                            String price1month = etSub1Month.getText().toString();
                            String price3month = etSub3Month.getText().toString();
                            String price6month = etSub6Month.getText().toString();
                            String price12month = etSub12Month.getText().toString();
                            if (TextUtils.isEmpty(price1month) || TextUtils.isEmpty(price3month) || TextUtils.isEmpty(price6month) || TextUtils.isEmpty(price12month)){
                                tvError.setText("Please fill all details.");
                                tvError.setVisibility(View.VISIBLE);
                                handler.postDelayed(runnable, 4000);
                            } else {
                                if(specPrizeCount==specialisationList.size()-1){
                                    btNext.setText("Save");
                                    //save prices and upload
                                }else{
                                    List<String> prices = new ArrayList<>();
                                    prices.set(0, etSub1Month.getText().toString());
                                    prices.set(1, etSub3Month.getText().toString());
                                    prices.set(2, etSub6Month.getText().toString());
                                    prices.set(3, etSub12Month.getText().toString());
                                    specialisationList.put(tvSubSpec.getText().toString(), prices);

                                    tvSubDesc.setText(specListIterator.next().getKey());
                                    etSub1Month.setText("");
                                    etSub3Month.setText("");
                                    etSub6Month.setText("");
                                    etSub12Month.setText("");
                                }
                            }



                        }
                        /*if (radioButtonBuffer==0){
                            tvError.setText("Please select one.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please select one.", Snackbar.LENGTH_LONG).show();
                        }else{
                            Map<String, Object> dataMap = new HashMap<>();

                            int specId = -1;
                            *//*if(radioButtonBuffer==1){specId = rgSpec1.getCheckedRadioButtonId();}
                            else if(radioButtonBuffer==2){specId = rgSpec2.getCheckedRadioButtonId();}*//*
                            String specialisationText = getSpecText(specId);

                            dataMap.put("SpecialisationText", specialisationText);

                            uploadDetails("Specialisation", dataMap);

                        }*/

                    }
                });
            }
        });
        dialog.show();
    }

    private String getSpecialisationText(String str) {
        if(str.equals("Stocks (Fundamental)")){return "StocksFundamental";}
        else if(str.equals("Stocks (Technical)")){return "StocksTechnical";}
        else if(str.equals("Bonds/Debt Market")){return "BondsDebtMarket";}
        else if(str.equals("Crypto Trading")){return "CryptoTrading";}
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


    private void addPersonalInfo() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_personal_info);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                TextInputEditText etName = dialog.findViewById(R.id.et_name);
                TextInputEditText etEmailId = dialog.findViewById(R.id.et_email_id);
                TextInputEditText etZipCode = dialog.findViewById(R.id.et_zipcode);
                TextView tvError = dialog.findViewById(R.id.tv_error);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvError.setVisibility(View.GONE);
                    }
                };

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        etName.getText().clear();
                        etEmailId.getText().clear();
                        etZipCode.getText().clear();
                    }
                });

                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String name, emailId, zipCode;
                        name = etName.getText().toString();
                        emailId = etEmailId.getText().toString();
                        zipCode = etZipCode.getText().toString();
                        if(TextUtils.isEmpty(name)){
                            etName.setError("Please enter your name");
                        }else if(TextUtils.isEmpty(emailId)){
                            etEmailId.setError("Please enter your email-id");
                        }else if(!isEmailValid(emailId)){
                            etEmailId.setError("Enter a valid emailId.");
                        }else if(TextUtils.isEmpty(zipCode)){
                            etZipCode.setError("Please enter your zipcode.");
                        }else if(zipCode.length()!=6){
                            etZipCode.setError("Please enter a valid zipcode");
                        } else{

                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("Name", name);
                            dataMap.put("EmailId", emailId);
                            dataMap.put("Zipcode", zipCode);

                            uploadDetails("PersonalInfo", dataMap);

                        }

                    }
                });
            }
        });
        dialog.show();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void updateDoneIcon(@NonNull Map<String, Object> snapdata) {

        int count = 0;

        if(String.valueOf(snapdata.get("Specialisation")).equals(String.valueOf(1))){
            ivSpecialisation.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivSpecialisation.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivSpecialisation.setImageDrawable(getDrawable(R.drawable.ic_primary_specialisation));
            ivSpecialisation.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("PersonalInfo")).equals(String.valueOf(1))){
            ivPersonalInfo.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivPersonalInfo.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivPersonalInfo.setImageDrawable(getDrawable(R.drawable.ic_personal_info));
            ivPersonalInfo.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("PhotoId")).equals(String.valueOf(1))){
            ivPhotoId.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivPhotoId.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivPhotoId.setImageDrawable(getDrawable(R.drawable.ic_id_card));
            ivPhotoId.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("AddressProof")).equals(String.valueOf(1))){
            ivAddressProof.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivAddressProof.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivAddressProof.setImageDrawable(getDrawable(R.drawable.ic_address));
            ivAddressProof.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("FaceVerification")).equals(String.valueOf(1))){
            ivFaceVerification.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivFaceVerification.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivFaceVerification.setImageDrawable(getDrawable(R.drawable.ic_profile_image));
            ivFaceVerification.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("AwardsAndCertificate")).equals(String.valueOf(1))){
            ivAwardsAndCertificate.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivAwardsAndCertificate.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivAwardsAndCertificate.setImageDrawable(getDrawable(R.drawable.ic_awards));
            ivAwardsAndCertificate.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("BankDetails")).equals(String.valueOf(1))){
            ivBankDetails.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivBankDetails.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivBankDetails.setImageDrawable(getDrawable(R.drawable.ic_bank_details));
            ivBankDetails.setBackgroundColor(Color.TRANSPARENT);
        }

        if(String.valueOf(snapdata.get("OtherDetails")).equals(String.valueOf(1))){
            ivOtherDetails.setImageDrawable(getDrawable(R.drawable.ic_done));
            ivOtherDetails.setBackgroundColor(Color.parseColor("#2ECB70"));
            count++;
        }else{
            ivOtherDetails.setImageDrawable(getDrawable(R.drawable.ic_other_details));
            ivOtherDetails.setBackgroundColor(Color.TRANSPARENT);
        }

        tvUploadCount.setText(String.valueOf(count)+"/8 Uploaded");
        intUploadCount = count;
        if (count==8){
            btSubmit.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));

        }else {
            btSubmit.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.inactive_grey)));
        }


    }

    private void addOtherDetails() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_other_details);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                Spinner spYearsOfExperience = dialog.findViewById(R.id.sp_years_of_experience);
                //TextInputEditText etLanguages = dialog.findViewById(R.id.et_languages);
                RadioGroup rgSebiNumber = dialog.findViewById(R.id.rg_sebi_number);
                LinearLayout llSebiNumber = dialog.findViewById(R.id.ll_sebi_number);
                TextInputEditText etSebiNumber = dialog.findViewById(R.id.et_sebi_number);
                TextView tvError = dialog.findViewById(R.id.tv_error);
                CheckBox cbHindi = dialog.findViewById(R.id.lang_hindi);
                CheckBox cbEnglish = dialog.findViewById(R.id.lang_english);
                CheckBox cbAssamese = dialog.findViewById(R.id.lang_assamese);
                CheckBox cbBengali = dialog.findViewById(R.id.lang_bengali);
                CheckBox cbGerman = dialog.findViewById(R.id.lang_german);
                CheckBox cbGujarati = dialog.findViewById(R.id.lang_gujarati);
                CheckBox cbHaryanvi = dialog.findViewById(R.id.lang_haryanvi);
                CheckBox cbKannada = dialog.findViewById(R.id.lang_kannada);
                CheckBox cbKashmiri = dialog.findViewById(R.id.lang_kashmiri);
                CheckBox cbKonkani = dialog.findViewById(R.id.lang_konkani);
                CheckBox cbMaithili = dialog.findViewById(R.id.lang_maithili);
                CheckBox cbMalayalam = dialog.findViewById(R.id.lang_malayalam);
                CheckBox cbManipuri = dialog.findViewById(R.id.lang_manipuri);
                CheckBox cbMarathi = dialog.findViewById(R.id.lang_marathi);
                CheckBox cbMarwari = dialog.findViewById(R.id.lang_marwari);
                CheckBox cbNepali = dialog.findViewById(R.id.lang_nepali);
                CheckBox cbOdia = dialog.findViewById(R.id.lang_odia);
                CheckBox cbPunjabi = dialog.findViewById(R.id.lang_punjabi);
                CheckBox cbSanskrit = dialog.findViewById(R.id.lang_sanskrit);
                CheckBox cbSindhi = dialog.findViewById(R.id.lang_sindhi);
                CheckBox cbSpanish = dialog.findViewById(R.id.lang_spanish);
                CheckBox cbTamil = dialog.findViewById(R.id.lang_tamil);
                CheckBox cbTelugu = dialog.findViewById(R.id.lang_telugu);
                CheckBox cbUrdu = dialog.findViewById(R.id.lang_urdu);
                String textLang = "";

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvError.setVisibility(View.GONE);
                    }
                };
                rgSebiNumber.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch(checkedId) {
                            case R.id.rb_yes:
                                llSebiNumber.setVisibility(View.VISIBLE);
                                break;
                            case R.id.rb_no:
                                llSebiNumber.setVisibility(View.GONE);
                                etSebiNumber.getText().clear();
                                break;
                            default:
                                // code block
                        }
                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spYearsOfExperience.setSelection(0);
                        //etLanguages.getText().clear();
                        rgSebiNumber.clearCheck();
                        cbHindi.setChecked(false);
                        cbEnglish.setChecked(false);
                        cbAssamese.setChecked(false);
                        cbBengali.setChecked(false);
                        cbGerman.setChecked(false);
                        cbGujarati.setChecked(false);
                        cbHaryanvi.setChecked(false);
                        cbKannada.setChecked(false);
                        cbKashmiri.setChecked(false);
                        cbKonkani.setChecked(false);
                        cbMaithili.setChecked(false);
                        cbMalayalam.setChecked(false);
                        cbManipuri.setChecked(false);
                        cbMarathi.setChecked(false);
                        cbMarwari.setChecked(false);
                        cbNepali.setChecked(false);
                        cbOdia.setChecked(false);
                        cbPunjabi.setChecked(false);
                        cbSanskrit.setChecked(false);
                        cbSindhi.setChecked(false);
                        cbSpanish.setChecked(false);
                        cbTamil.setChecked(false);
                        cbTelugu.setChecked(false);
                        cbUrdu.setChecked(false);
                    }
                });

                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        spinnerItemNumber = spYearsOfExperience.getSelectedItemPosition();
                        //languages = etLanguages.getText().toString();
                        sebiNumber = etSebiNumber.getText().toString();
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        if (spinnerItemNumber==0){
                            tvError.setText("Please select years of experience.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please select years of experience.", Snackbar.LENGTH_LONG).show();
                        }else if ( !(cbHindi.isChecked() || cbEnglish.isChecked() || cbAssamese.isChecked() ||
                                cbBengali.isChecked() || cbGerman.isChecked() || cbGujarati.isChecked() ||
                                cbHaryanvi.isChecked() || cbKannada.isChecked() || cbKashmiri.isChecked() ||
                                cbKonkani.isChecked() || cbMaithili.isChecked() || cbMalayalam.isChecked() ||
                                cbManipuri.isChecked() || cbMarathi.isChecked() || cbMarwari.isChecked() ||
                                cbNepali.isChecked() || cbOdia.isChecked() || cbPunjabi.isChecked() ||
                                cbSanskrit.isChecked() || cbSindhi.isChecked() || cbSpanish.isChecked() ||
                                cbTamil.isChecked() || cbTelugu.isChecked() || cbUrdu.isChecked())){
                            tvError.setText("Please at least one language.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                        }else if(rgSebiNumber.getCheckedRadioButtonId()==-1){
                            tvError.setText("Please select yes/no for SEBI number.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please select yes/no.", Snackbar.LENGTH_LONG).show();
                        }else if(rgSebiNumber.getCheckedRadioButtonId() == R.id.rb_yes && TextUtils.isEmpty(sebiNumber)){
                            etSebiNumber.setError("Please enter your SEBI number.");
                        }else{
                            Map<String, Object> dataMap = new HashMap<>();
                            int yearsOfExperience = spinnerItemNumber;
                            String languages = "";
                            if (cbHindi.isChecked()){languages = languages + " Hindi,";}
                            if (cbEnglish.isChecked()){languages = languages + " English,";}
                            if (cbAssamese.isChecked()){languages = languages + " Assamese,";}
                            if (cbBengali.isChecked()){languages = languages + " Bengali,";}
                            if (cbGerman.isChecked()){languages = languages + " German,";}
                            if (cbGujarati.isChecked()){languages = languages + " Gujarati,";}
                            if (cbHaryanvi.isChecked()){languages = languages + " Haryanvi,";}
                            if (cbKannada.isChecked()){languages = languages + " Kannada,";}
                            if (cbKashmiri.isChecked()){languages = languages + " Kashmiri,";}
                            if (cbKonkani.isChecked()){languages = languages + " Konkani,";}
                            if (cbMaithili.isChecked()){languages = languages + " Maithili,";}
                            if (cbMalayalam.isChecked()){languages = languages + " Malayalam,";}
                            if (cbManipuri.isChecked()){languages = languages + " Manipuri,";}
                            if (cbMarathi.isChecked()){languages = languages + " Marathi,";}
                            if (cbMarwari.isChecked()){languages = languages + " Marwari,";}
                            if (cbNepali.isChecked()){languages = languages + " Nepali,";}
                            if (cbOdia.isChecked()){languages = languages + " Odia,";}
                            if (cbPunjabi.isChecked()){languages = languages + " Punjabi,";}
                            if (cbSanskrit.isChecked()){languages = languages + " Sanskrit,";}
                            if (cbSindhi.isChecked()){languages = languages + " Sindhi,";}
                            if (cbSpanish.isChecked()){languages = languages + " Spanish,";}
                            if (cbTamil.isChecked()){languages = languages + " Tamil,";}
                            if (cbTelugu.isChecked()){languages = languages + " Telugu,";}
                            if (cbUrdu.isChecked()){languages = languages + " Urdu,";}
                            String finalLang = languages.substring(1, languages.length()-1);
                            //float yearsOfExperience = getYearsOfExperience(spinnerItemNumber);
                            dataMap.put("YearsOfExperience", yearsOfExperience);
                            dataMap.put("Languages", finalLang);
                            switch(rgSebiNumber.getCheckedRadioButtonId()) {
                                case R.id.rb_yes:
                                    dataMap.put("isSebiNumber", true);
                                    dataMap.put("SebiNumber", sebiNumber);
                                    break;
                                default:
                                    dataMap.put("isSebiNumber", false);
                            }
                            uploadDetails("OtherDetails", dataMap);

                        }

                    }
                });
            }
        });
        dialog.show();
    }

    private void uploadDetails(String folder, Map<String, Object> dataMap) {
        final ProgressDialog progressDialog = new ProgressDialog(IdentityVerificationActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("IdProofs")
                .document(folder);
        if (isNetworkAvailable(getApplicationContext())) {
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        docRef.update(dataMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String, Object> photoBoolMap = new HashMap<>();
                                        photoBoolMap.put(folder, 1);
                                        docRef.getParent().document("PhotoBoolean").update(photoBoolMap);
                                        progressDialog.dismiss();
                                        dialog.dismiss();
                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.w("TAG", "Error writing document", e);
                                    }
                                });
                    } else {
                        docRef.set(dataMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String, Object> photoBoolMap = new HashMap<>();
                                        photoBoolMap.put(folder, 1);
                                        docRef.getParent().document("PhotoBoolean").update(photoBoolMap);
                                        //Log.d()
                                        progressDialog.dismiss();
                                        dialog.dismiss();
                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Log.w("TAG", "Error writing document", e);
                                    }
                                });
                    }
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(IdentityVerificationActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Contract(pure = true)
    private float getYearsOfExperience(int itemNumber) {
        if(itemNumber==1){
            return (float) 0.5;
        }else if(itemNumber==2){
            return (float) 1.5;
        }else if(itemNumber==3){
            return (float) 3.5;
        }else if(itemNumber==4){
            return (float) 7.5;
        }else if(itemNumber==5){
            return (float) 12.5;
        }else{
            return 15;
        }
    }

    private void addBankDetails() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_bank_details);
        TextView tvError = dialog.findViewById(R.id.tv_error);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tvError.setVisibility(View.GONE);
            }
        };

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                TextInputEditText etName = dialog.findViewById(R.id.et_name);
                TextInputEditText etAccountNumber = dialog.findViewById(R.id.et_account_number);
                TextInputEditText etConfirmAccountNumber = dialog.findViewById(R.id.et_confirm_account_number);
                TextInputEditText etIFSCCode = dialog.findViewById(R.id.et_ifsc_code);
                ImageView ivCancelledCheque = dialog.findViewById(R.id.iv_cancelled_cheque_image);
                RelativeLayout rlCancelledCheque = dialog.findViewById(R.id.rl_cancelled_cheque_image);

                if (name != null){
                    etName.setText(name);
                }

                if (accountNumber != null){
                    etName.setText(accountNumber);
                }

                if (ifscCode != null){
                    etName.setText(ifscCode);
                }

                if (uriCancelledChequeImage != null){
                    imageView = ivCancelledCheque;
                    loadProfile(uriCancelledChequeImage.toString());
                }

                rlCancelledCheque.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivCancelledCheque;
                        REQUEST_IMAGE = 124;
                        onSelectImageClick(v);
                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uriCancelledChequeImage=null;
                        ivCancelledCheque.setImageResource(android.R.color.transparent);
                        etName.getText().clear();
                        etAccountNumber.getText().clear();
                        etConfirmAccountNumber.getText().clear();
                        etIFSCCode.getText().clear();
                    }
                });

                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        name = etName.getText().toString();
                        accountNumber = etAccountNumber.getText().toString();
                        confirmAccountNumber = etConfirmAccountNumber.getText().toString();
                        ifscCode = etIFSCCode.getText().toString().toUpperCase();
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        if(TextUtils.isEmpty(name)){
                            etName.setError("Please enter your name");
                        }else if(TextUtils.isEmpty(accountNumber)){
                            etAccountNumber.setError("Please enter your Bank account number");
                        }else if(TextUtils.isEmpty(confirmAccountNumber)){
                            etConfirmAccountNumber.setError("Please confirm your account number.");
                        }else if(!confirmAccountNumber.equals(accountNumber)){
                            etConfirmAccountNumber.setError("Bank account number does not match.");
                        }else if(TextUtils.isEmpty(ifscCode)){
                            etIFSCCode.setError("Please enter your IFSC code");
                        }else if(uriCancelledChequeImage==null){
                            tvError.setText("Please upload a photo of your cancelled cheque.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please upload a photo of your cancelled cheque.", Snackbar.LENGTH_LONG).show();
                        }else{
                            List<Uri> uriPhotoIdList = new ArrayList<>();
                            uriPhotoIdList.add(uriCancelledChequeImage);

                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("Name", name);
                            dataMap.put("AccountNumber", accountNumber);
                            dataMap.put("IFSCCode", ifscCode);

                            uploadImages(uriPhotoIdList, "BankDetails", dataMap);

                        }

                    }
                });
            }
        });
        dialog.show();
    }

    private void addAwardsAndCertificatePhotos() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_awards_and_certificate);
        TextView tvError = dialog.findViewById(R.id.tv_error);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tvError.setVisibility(View.GONE);
            }
        };

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                ImageView ivAwardsImage1 = dialog.findViewById(R.id.iv_awards_image1);
                ImageView ivAwardsImage2 = dialog.findViewById(R.id.iv_awards_image2);
                ImageView ivAwardsImage3 = dialog.findViewById(R.id.iv_awards_image3);
                ImageView ivAwardsImage4 = dialog.findViewById(R.id.iv_awards_image4);
                ImageView ivAwardsImage5 = dialog.findViewById(R.id.iv_awards_image5);
                ImageView ivAwardsImage6 = dialog.findViewById(R.id.iv_awards_image6);
                ImageView ivAwardsImage7 = dialog.findViewById(R.id.iv_awards_image7);
                ImageView ivAwardsImage8 = dialog.findViewById(R.id.iv_awards_image8);
                ImageView ivAwardsImage9 = dialog.findViewById(R.id.iv_awards_image9);
                RelativeLayout rlAwardsImage1 = dialog.findViewById(R.id.rl_awards_image_1);
                RelativeLayout rlAwardsImage2 = dialog.findViewById(R.id.rl_awards_image_2);
                RelativeLayout rlAwardsImage3 = dialog.findViewById(R.id.rl_awards_image_3);
                RelativeLayout rlAwardsImage4 = dialog.findViewById(R.id.rl_awards_image_4);
                RelativeLayout rlAwardsImage5 = dialog.findViewById(R.id.rl_awards_image_5);
                RelativeLayout rlAwardsImage6 = dialog.findViewById(R.id.rl_awards_image_6);
                RelativeLayout rlAwardsImage7 = dialog.findViewById(R.id.rl_awards_image_7);
                RelativeLayout rlAwardsImage8 = dialog.findViewById(R.id.rl_awards_image_8);
                RelativeLayout rlAwardsImage9 = dialog.findViewById(R.id.rl_awards_image_9);

                if (uriAwardsImage1 != null){
                    imageView = ivAwardsImage1;
                    loadProfile(uriAwardsImage1.toString());
                }if (uriAwardsImage2 != null){
                    imageView = ivAwardsImage2;
                    loadProfile(uriAwardsImage2.toString());
                }if (uriAwardsImage3 != null){
                    imageView = ivAwardsImage3;
                    loadProfile(uriAwardsImage3.toString());
                }if (uriAwardsImage4 != null){
                    imageView = ivAwardsImage4;
                    loadProfile(uriAwardsImage4.toString());
                }if (uriAwardsImage5 != null){
                    imageView = ivAwardsImage5;
                    loadProfile(uriAwardsImage5.toString());
                }if (uriAwardsImage6 != null){
                    imageView = ivAwardsImage6;
                    loadProfile(uriAwardsImage6.toString());
                }if (uriAwardsImage7 != null){
                    imageView = ivAwardsImage7;
                    loadProfile(uriAwardsImage7.toString());
                }if (uriAwardsImage8 != null){
                    imageView = ivAwardsImage8;
                    loadProfile(uriAwardsImage8.toString());
                }if (uriAwardsImage9 != null){
                    imageView = ivAwardsImage9;
                    loadProfile(uriAwardsImage9.toString());
                }

                rlAwardsImage1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage1;
                        REQUEST_IMAGE = 115;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage2;
                        REQUEST_IMAGE = 116;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage3;
                        REQUEST_IMAGE = 117;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage4;
                        REQUEST_IMAGE = 118;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage5;
                        REQUEST_IMAGE = 119;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage6;
                        REQUEST_IMAGE = 120;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage7.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage7;
                        REQUEST_IMAGE = 121;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage8;
                        REQUEST_IMAGE = 122;
                        onSelectImageClick(v);
                    }
                });

                rlAwardsImage9.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAwardsImage9;
                        REQUEST_IMAGE = 123;
                        onSelectImageClick(v);
                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uriAwardsImage1=null;
                        uriAwardsImage2=null;
                        uriAwardsImage3=null;
                        uriAwardsImage4=null;
                        uriAwardsImage5=null;
                        uriAwardsImage6=null;
                        uriAwardsImage7=null;
                        uriAwardsImage8=null;
                        uriAwardsImage9=null;
                        ivAwardsImage1.setImageResource(android.R.color.transparent);
                        ivAwardsImage2.setImageResource(android.R.color.transparent);
                        ivAwardsImage3.setImageResource(android.R.color.transparent);
                        ivAwardsImage4.setImageResource(android.R.color.transparent);
                        ivAwardsImage5.setImageResource(android.R.color.transparent);
                        ivAwardsImage6.setImageResource(android.R.color.transparent);
                        ivAwardsImage7.setImageResource(android.R.color.transparent);
                        ivAwardsImage8.setImageResource(android.R.color.transparent);
                        ivAwardsImage9.setImageResource(android.R.color.transparent);
                    }
                });

                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        if(uriAwardsImage1==null && uriAwardsImage2==null && uriAwardsImage3==null && uriAwardsImage4==null &&
                                    uriAwardsImage5==null && uriAwardsImage6==null && uriAwardsImage7==null &&
                                    uriAwardsImage8==null && uriAwardsImage9==null){
                            tvError.setText("Please upload at least one photo.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please upload at least one photo.", Snackbar.LENGTH_LONG).show();
                        }else{
                            List<Uri> uriPhotoIdList = new ArrayList<>();
                            if (uriAwardsImage1 != null){
                                uriPhotoIdList.add(uriAwardsImage1);
                            }if (uriAwardsImage2 != null){
                                uriPhotoIdList.add(uriAwardsImage2);
                            }if (uriAwardsImage3 != null){
                                uriPhotoIdList.add(uriAwardsImage3);
                            }if (uriAwardsImage4 != null){
                                uriPhotoIdList.add(uriAwardsImage4);
                            }if (uriAwardsImage5 != null){
                                uriPhotoIdList.add(uriAwardsImage5);
                            }if (uriAwardsImage6 != null){
                                uriPhotoIdList.add(uriAwardsImage6);
                            }if (uriAwardsImage7 != null){
                                uriPhotoIdList.add(uriAwardsImage7);
                            }if (uriAwardsImage8 != null){
                                uriPhotoIdList.add(uriAwardsImage8);
                            }if (uriAwardsImage9 != null){
                                uriPhotoIdList.add(uriAwardsImage9);
                            }


                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("PhotoIdType", "Awards and Certificate");

                            uploadImages(uriPhotoIdList, "AwardsAndCertificate", dataMap);

                        }

                    }
                });
            }
        });
        dialog.show();
    }

    private void addFacePhoto() {
        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_face_verification);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                ImageView ivFaceVerification = dialog.findViewById(R.id.iv_face_verification_image);
                RelativeLayout rlFaceVerification = dialog.findViewById(R.id.rl_face_verification_image);
                TextView tvError = dialog.findViewById(R.id.tv_error);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvError.setVisibility(View.GONE);
                    }
                };

                if (uriFaceVerification != null){
                    imageView = ivFaceVerification;
                    loadProfile(uriFaceVerification.toString());
                }

                rlFaceVerification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivFaceVerification;
                        REQUEST_IMAGE = 114;
                        onSelectImageClick(v);
                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uriFaceVerification=null;
                        ivFaceVerification.setImageResource(android.R.color.transparent);
                    }
                });

                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        if(uriFaceVerification==null){
                            tvError.setText("Please upload the photo.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please upload the photo.", Snackbar.LENGTH_LONG).show();
                        }else{
                            List<Uri> uriPhotoIdList = new ArrayList<>();
                            uriPhotoIdList.add(uriFaceVerification);

                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("PhotoIdType", "Profile Photo");

                            uploadImages(uriPhotoIdList, "FaceVerification", dataMap);

                        }

                    }
                });
            }
        });
        dialog.show();
    }

    private void addAddressProof() {

        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_address_proof);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                RadioGroup rgAddressProof = dialog.findViewById(R.id.rg_address_proof);
                ImageView ivAddressProofFront = dialog.findViewById(R.id.iv_address_proof_front_image);
                ImageView ivAddressProofBack = dialog.findViewById(R.id.iv_address_proof_back_image);
                RelativeLayout rlAddressProofFront = dialog.findViewById(R.id.rl_address_proof_front_image);
                RelativeLayout rlAddressProofBack = dialog.findViewById(R.id.rl_address_proof_back_image);
                TextView tvError = dialog.findViewById(R.id.tv_error);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvError.setVisibility(View.GONE);
                    }
                };

                if (uriAddressProofFront != null){
                    imageView = ivAddressProofFront;
                    loadProfile(uriAddressProofFront.toString());
                }

                if (uriAddressProofBack != null){
                    imageView = ivAddressProofBack;
                    loadProfile(uriAddressProofBack.toString());
                }

                if(rbAddressProofTypeId!=-1){
                    rgAddressProof.check(rbAddressProofTypeId);
                }

                rgAddressProof.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        rbAddressProofTypeId = checkedId;
                    }
                });

                rlAddressProofFront.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAddressProofFront;
                        REQUEST_IMAGE = 112;
                        onSelectImageClick(v);
                    }
                });

                rlAddressProofBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivAddressProofBack;
                        REQUEST_IMAGE = 113;
                        onSelectImageClick(v);
                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rgAddressProof.clearCheck();
                        uriAddressProofFront=null;
                        uriAddressProofBack =null;
                        ivAddressProofFront.setImageResource(android.R.color.transparent);
                        ivAddressProofBack.setImageResource(android.R.color.transparent);
                    }
                });

                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        if(rgAddressProof.getCheckedRadioButtonId()==-1){
                            tvError.setText("Please select a photo id type.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please select a photo id type.", Snackbar.LENGTH_LONG).show();
                        }else if(uriAddressProofFront==null || uriAddressProofBack == null){
                            tvError.setText("Please upload the photos.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please upload the photos.", Snackbar.LENGTH_LONG).show();
                        }else{
                            List<Uri> uriPhotoIdList = new ArrayList<>();
                            uriPhotoIdList.add(uriAddressProofFront);
                            uriPhotoIdList.add(uriAddressProofBack);

                            Map<String, Object> dataMap = new HashMap<>();
                            RadioButton radioButton = dialog.findViewById(rgAddressProof.getCheckedRadioButtonId());

                            dataMap.put("PhotoIdType", radioButton.getText());

                            uploadImages(uriPhotoIdList, "AddressProof", dataMap);

                        }

                    }
                });
            }
        });
        dialog.show();

    }

    private void addPhotoId() {

        dialog = new Dialog(this, R.style.WideDialog);
        dialog.setContentView(R.layout.dialog_photo_id);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btSave = dialog.findViewById(R.id.bt_save);
                Button btReset = dialog.findViewById(R.id.bt_reset);
                ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                RadioGroup rgPhotoIdType = dialog.findViewById(R.id.rg_photo_id);
                ImageView ivPhotoIdFront = dialog.findViewById(R.id.iv_photo_id_front_image);
                ImageView ivPhotoIdBack = dialog.findViewById(R.id.iv_photo_id_back_image);
                RelativeLayout rlPhotoIdFront = dialog.findViewById(R.id.rl_photo_id_front_image);
                RelativeLayout rlPhotoIdBack = dialog.findViewById(R.id.rl_photo_id_back_image);
                TextView tvError = dialog.findViewById(R.id.tv_error);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        tvError.setVisibility(View.GONE);
                    }
                };

                if (uriPhotoIdFront != null){
                    imageView = ivPhotoIdFront;
                    loadProfile(uriPhotoIdFront.toString());
                }

                if (uriPhotoIdBack != null){
                    imageView = ivPhotoIdBack;
                    loadProfile(uriPhotoIdBack.toString());
                }

                if(rbAddressProofTypeId!=-1){
                    rgPhotoIdType.check(rbPhotoIdTypeId);
                }

                rgPhotoIdType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        rbPhotoIdTypeId = checkedId;
                    }
                });

                rlPhotoIdFront.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivPhotoIdFront;
                        REQUEST_IMAGE = 110;
                        onSelectImageClick(v);
                    }
                });

                rlPhotoIdBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageView = ivPhotoIdBack;
                        REQUEST_IMAGE = 111;
                        onSelectImageClick(v);
                    }
                });


                btSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handler.removeCallbacks(runnable);
                        tvError.setVisibility(View.GONE);
                        if(rgPhotoIdType.getCheckedRadioButtonId()==-1){
                            tvError.setText("Please select a photo id type.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please select a photo id type.", Snackbar.LENGTH_LONG).show();
                        }else if(uriPhotoIdFront==null || uriPhotoIdBack == null){
                            tvError.setText("Please upload the photos.");
                            tvError.setVisibility(View.VISIBLE);
                            handler.postDelayed(runnable, 4000);
                            //Snackbar.make(getWindow().getDecorView(), "Please upload the photos.", Snackbar.LENGTH_LONG).show();
                        }else{
                            List<Uri> uriPhotoIdList = new ArrayList<>();
                            uriPhotoIdList.add(uriPhotoIdFront);
                            uriPhotoIdList.add(uriPhotoIdBack);

                            Map<String, Object> dataMap = new HashMap<>();
                            RadioButton radioButton = dialog.findViewById(rgPhotoIdType.getCheckedRadioButtonId());

                            dataMap.put("PhotoIdType", radioButton.getText());

                            uploadImages(uriPhotoIdList, "PhotoId", dataMap);

                        }

                    }
                });

                btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                btReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rgPhotoIdType.clearCheck();
                        uriPhotoIdFront=null;
                        uriPhotoIdBack=null;
                        ivPhotoIdFront.setImageResource(android.R.color.transparent);
                        ivPhotoIdBack.setImageResource(android.R.color.transparent);
                    }
                });
            }
        });
        dialog.show();
    }


    private void uploadImages(@NotNull List<Uri> uriList, String folder, Map<String, Object> dataMap) {

        for (int i=0; i<uriList.size(); i++) {
            File newImageFile = new File(uriList.get(i).getPath());
            final ProgressDialog progressDialog = new ProgressDialog(IdentityVerificationActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            final String randomName = UUID.randomUUID().toString();

            try {
                compressedImageBitmap = new Compressor(IdentityVerificationActivity.this).setQuality(50).compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos_original = new ByteArrayOutputStream();
            compressedImageBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos_original);
            byte[] data_original = baos_original.toByteArray();

            final StorageReference storageRef = storageReference.child("IdentityVerification")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser().getUid())).child(folder);
            final StorageReference filePath = storageRef.child(randomName + ".png");
            upload_task = filePath.putBytes(data_original);

            Task<Uri> urlTask = upload_task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        final String downloadUri = task.getResult().toString();
                        Map<String, Object> downloadUriMap = new HashMap<>();
                        if (!dataMap.isEmpty()) {
                            downloadUriMap.putAll(dataMap);
                        }
                        downloadUriMap.put(randomName, downloadUri);
                        if (folder.equals("FaceVerification" )){
                            downloadUriMap.put("ProfileImage", downloadUri);
                            downloadUriMap.put("ProfileImageName", randomName);
                        }




                        DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                .collection("IdProofs")
                                .document(folder);
                        if (isNetworkAvailable(getApplicationContext())) {
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        docRef.update(downloadUriMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Map<String, Object> photoBoolMap = new HashMap<>();
                                                        photoBoolMap.put(folder, 1);
                                                        docRef.getParent().document("PhotoBoolean").update(photoBoolMap);
                                                        progressDialog.dismiss();
                                                        dialog.dismiss();
                                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Log.w("TAG", "Error writing document", e);
                                                    }
                                                });
                                    } else {
                                        docRef.set(downloadUriMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Map<String, Object> photoBoolMap = new HashMap<>();
                                                        photoBoolMap.put(folder, 1);
                                                        docRef.getParent().document("PhotoBoolean").update(photoBoolMap);
                                                        //Log.d()
                                                        progressDialog.dismiss();
                                                        dialog.dismiss();
                                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Log.w("TAG", "Error writing document", e);
                                                    }
                                                });
                                    }
                                }
                            });
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(IdentityVerificationActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
                        }




                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(IdentityVerificationActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(IdentityVerificationActivity.this, "Failed "+e.getMessage() , Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

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
        Intent intent = new Intent(IdentityVerificationActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        if (REQUEST_IMAGE == 114){
            intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
            intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
            intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        }

        // setting maximum bitmap width and height
        /*intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);*/
        //intent.putExtra(ImagePickerActivity.INTENT_IMAGE_COMPRESSION_QUALITY, 50);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(IdentityVerificationActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        if (REQUEST_IMAGE == 114){
            intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
            intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
            intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        }

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
                    loadProfile(uri.toString());
                    loadURI(uri, requestCode);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void loadURI(Uri uri, int requestCode) {

        if (requestCode == 110){
            uriPhotoIdFront = uri;
        }else if(requestCode == 111){
            uriPhotoIdBack = uri;
        }else if(requestCode == 112){
            uriAddressProofFront = uri;
        }else if(requestCode == 113){
            uriAddressProofBack = uri;
        }else if(requestCode == 114){
            uriFaceVerification = uri;
        }else if(requestCode == 115){
            uriAwardsImage1 = uri;
        }else if(requestCode == 116){
            uriAwardsImage2 = uri;
        }else if(requestCode == 117){
            uriAwardsImage3 = uri;
        }else if(requestCode == 118){
            uriAwardsImage4 = uri;
        }else if(requestCode == 119){
            uriAwardsImage5 = uri;
        }else if(requestCode == 120){
            uriAwardsImage6 = uri;
        }else if(requestCode == 121){
            uriAwardsImage7 = uri;
        }else if(requestCode == 122){
            uriAwardsImage8 = uri;
        }else if(requestCode == 123){
            uriAwardsImage9 = uri;
        }else if(requestCode == 124){
            uriCancelledChequeImage = uri;
        }
    }

    private void loadProfile(String url) {
        //Log.d(TAG, "Image cache path: " + url);
        if (imageView!=null){
            Log.d("ImageView id : ",String.valueOf(imageView.getId()));
            Glide.with(this).load(url)
                    .into(imageView);
            imageView.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
        }

    }

    private void init() {
        cbTermsAndConditions = findViewById(R.id.cb_t_n_c);

        ivBack = findViewById(R.id.iv_back);

        cvSpecialisation = findViewById(R.id.cv_specialisation);
        cvPersonalInfo = findViewById(R.id.cv_personal_info);
        cvPhotoId = findViewById(R.id.cv_photo_id);
        cvAddressProof = findViewById(R.id.cv_address_proof);
        cvFaceVerification = findViewById(R.id.cv_face_verification);
        cvAwardsAndCertificate = findViewById(R.id.cv_awards_and_certificate);
        cvBankDetails = findViewById(R.id.cv_bank_details);
        cvOtherDetails = findViewById(R.id.cv_other_details);

        ivSpecialisation = findViewById(R.id.iv_specialisation);
        ivPersonalInfo = findViewById(R.id.iv_personal_info);
        ivPhotoId = findViewById(R.id.iv_photo_id);
        ivAddressProof = findViewById(R.id.iv_address_proof);
        ivFaceVerification = findViewById(R.id.iv_face_verification);
        ivAwardsAndCertificate = findViewById(R.id.iv_awards_and_certificate);
        ivBankDetails = findViewById(R.id.iv_bank_details);
        ivOtherDetails = findViewById(R.id.iv_other_details);

        tvUploadCount = findViewById(R.id.tv_upload_count);
        tvSkip = findViewById(R.id.tv_skip);

        btSubmit = findViewById(R.id.bt_submit);

        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


    }

    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(IdentityVerificationActivity.this);
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

    private String getSpecText(int specId) {
        String primarySpecResult = "";
        switch(specId) {
            case R.id.radioStocksFundamental:
                primarySpecResult = "StocksFundamental";
                break;
            case R.id.radioStocksTechnical:
                primarySpecResult = "StocksTechnical";
                break;
            case R.id.radioBonds:
                primarySpecResult = "BondsDebtMarket";
                break;
            case R.id.radioBitcoin:
                primarySpecResult = "BitcoinCryptoTrading";
                break;
            case R.id.radioSilverGold:
                primarySpecResult = "SilverGoldBullion";
                break;
            case R.id.radioCurrencyForex:
                primarySpecResult = "CurrencyForexTrading";
                break;
            case R.id.radioCommodity:
                primarySpecResult = "Commodity";
                break;
            case R.id.radioInsurance:
                primarySpecResult = "Insurance";
                break;
            case R.id.radioRetirement:
                primarySpecResult = "RetirementPlanning";
                break;
            case R.id.radioTax:
                primarySpecResult = "TaxPlanning";
                break;
            case R.id.radioLoanSpecialist:
                primarySpecResult = "LoanSpecialist";
                break;
            case R.id.radioMutualFunds:
                primarySpecResult = "MutualFunds";
                break;

        }

        return primarySpecResult;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Map<String, Object> photoBoolMap = new HashMap<>();
        photoBoolMap.put("Specialisation", 0);
        photoBoolMap.put("PersonalInfo", 0);
        photoBoolMap.put("PhotoId", 0);
        photoBoolMap.put("AddressProof", 0);
        photoBoolMap.put("FaceVerification",0);
        photoBoolMap.put("AwardsAndCertificate",0);
        photoBoolMap.put("BankDetails",0);
        photoBoolMap.put("OtherDetails",0);

        if (isNetworkAvailable(getApplicationContext())) {
            DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(mAuth.getCurrentUser().getUid()).collection("IdProofs").document("PhotoBoolean");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> docData = document.getData();
                        updateDoneIcon(docData);
                    } else {
                        docRef.set(photoBoolMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                }
            });
        }else {
            Toast.makeText(IdentityVerificationActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ImagePickerActivity.clearCache(IdentityVerificationActivity.this);
        Intent intent = new Intent(IdentityVerificationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
