package com.cykka.partner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class NewBroadcastCall extends AppCompatActivity {

    private TextInputEditText etScripName, etEntryPrice, etTargetPrice, etCmp, etStopLoss, etNotes;
    private Button btCancel, btSave, btPreview;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String randomName;
    private ProgressDialog progressDialog;
    private Spinner spBuySell, spCurrency;
    private TextView tvStopLoss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_broadcast_call);

        ActionBar actionBar = getSupportActionBar();
        String title = getIntent().getStringExtra("Type");
        actionBar.setTitle(title);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(NewBroadcastCall.this, R.style.WideDialog);
                dialog.setContentView(R.layout.dialog_broadcast_call_preview);

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        String currency = getCurrencyString();

                        ImageView btCancel = dialog.findViewById(R.id.bt_cancel);
                        TextView tvScripName = dialog.findViewById(R.id.tv_scrip_name);
                        TextView tvEntryPrice = dialog.findViewById(R.id.tv_entry_price);
                        TextView tvTargetPrice = dialog.findViewById(R.id.tv_target_price);
                        TextView tvCmp = dialog.findViewById(R.id.tv_cmp);
                        TextView tvStopLossType = dialog.findViewById(R.id.tv_stop_loss_type);
                        TextView tvStopLoss = dialog.findViewById(R.id.tv_stop_loss);
                        TextView tvNotes = dialog.findViewById(R.id.tv_notes);
                        TextView tvDate = dialog.findViewById(R.id.tv_date);
                        TextView tvBuySell = dialog.findViewById(R.id.tv_buy_sell);
                        View view = dialog.findViewById(R.id.view1);

                        SimpleDateFormat dtNew = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                        String date = dtNew.format(new Date());

                        String scripName = etScripName.getText().toString();
                        String entryPrice = etEntryPrice.getText().toString();
                        String targetPrice = etTargetPrice.getText().toString();
                        String cmp = etCmp.getText().toString();
                        String stopLoss = etStopLoss.getText().toString();
                        int specType = getIntent().getIntExtra("SpecType",3);
                        if (specType==1){
                            tvStopLossType.setText("Stop Loss");
                        }else if(specType==2){
                            tvStopLossType.setText("Hold Period");
                        }

                        String notes = etNotes.getText().toString();
                        String buySell = spBuySell.getSelectedItem().toString();

                        tvDate.setText(date);
                        if (buySell.equals("BUY")){
                            tvBuySell.setText("BUY");
                            tvBuySell.setBackgroundResource(R.color.done_green);
                        }else{
                            tvBuySell.setText("SELL");
                            tvBuySell.setBackgroundResource(R.color.redColor);
                        }

                        if (!TextUtils.isEmpty(scripName)){
                            tvScripName.setText(scripName);
                        }if (!TextUtils.isEmpty(entryPrice)){
                            tvEntryPrice.setText(currency + entryPrice);
                        }if (!TextUtils.isEmpty(targetPrice)){
                            tvTargetPrice.setText(currency  + targetPrice);
                        }if (!TextUtils.isEmpty(cmp)){
                            tvCmp.setText(currency + cmp);
                        }if (!TextUtils.isEmpty(stopLoss) && specType==1){
                            tvStopLoss.setText(currency + stopLoss);
                        }if (!TextUtils.isEmpty(stopLoss) && specType==2){
                            tvStopLoss.setText(stopLoss);
                        }
                        if (!TextUtils.isEmpty(notes)){
                            view.setVisibility(View.VISIBLE);
                            tvNotes.setVisibility(View.VISIBLE);
                            tvNotes.setText(notes);
                        }else {
                            view.setVisibility(View.INVISIBLE);
                            tvNotes.setVisibility(View.GONE);
                            tvNotes.setText("");
                        }


                        btCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                });


                dialog.show();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String scripName, entryPrice, targetPrice, cmp, stopLoss, currencyId, buySell, note, isEdited, previousId;
                scripName = etScripName.getText().toString();
                entryPrice = etEntryPrice.getText().toString();
                targetPrice = etTargetPrice.getText().toString();
                cmp = etCmp.getText().toString();
                stopLoss = etStopLoss.getText().toString();
                currencyId = String.valueOf(spCurrency.getSelectedItemPosition());
                note = etNotes.getText().toString();
                buySell = spBuySell.getSelectedItem().toString();
                previousId = getIntent().getStringExtra("Id");



                if(TextUtils.isEmpty(scripName)){
                    etScripName.setError("Required");
                }else if(TextUtils.isEmpty(entryPrice)){
                    etEntryPrice.setError("Required");
                }else if(TextUtils.isEmpty(targetPrice)){
                    etTargetPrice.setError("Required");
                }else if(TextUtils.isEmpty(cmp)){
                    etCmp.setError("Required");
                }else if(TextUtils.isEmpty(stopLoss)){
                    etStopLoss.setError("Required");
                }else{
                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("ScripName", scripName);
                    dataMap.put("EntryPrice", entryPrice);
                    dataMap.put("TargetPrice", targetPrice);
                    dataMap.put("CurrentMarketPrice", cmp);
                    dataMap.put("StopLossOrHoldingPeriod", stopLoss);
                    dataMap.put("CurrencyId", currencyId);
                    dataMap.put("BuySell", buySell);
                    dataMap.put("TimeStamp", timeStamp);
                    dataMap.put("SpecType", String.valueOf(getIntent().getIntExtra("SpecType",3)));
                    if (!TextUtils.isEmpty(previousId)){
                        isEdited = "1";
                        dataMap.put("PreviousId", previousId);
                        setPrevInactive(previousId);
                    }else {
                        isEdited = "0";
                    }

                    if (!TextUtils.isEmpty(note)){
                        dataMap.put("Note", note);
                    }
                    dataMap.put("IsEdited", isEdited);
                    dataMap.put("Active", "1");
                    hideKeyboard(getApplicationContext(), v);
                    uploadBroadcastCall(dataMap);

                }
            }
        });
    }

    private void setPrevInactive(String previousId) {
        ProgressDialog progressDialog = new ProgressDialog(NewBroadcastCall.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("BroadcastCalls")
                .document(previousId);

        Map<String, Object> setInactive = new HashMap<>();
        setInactive.put("Active", "0");

        if (isNetworkAvailable(getApplicationContext())) {
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    docRef.update(setInactive);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewBroadcastCall.this, "Check your internet connection", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(NewBroadcastCall.this, "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadBroadcastCall(Map<String, Object> dataMap) {
        ProgressDialog progressDialog = new ProgressDialog(NewBroadcastCall.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("BroadcastCalls")
                .document(randomName);

        if (isNetworkAvailable(getApplicationContext())) {
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    docRef.set(dataMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    String isEdited = dataMap.get("IsEdited").toString();
                                    if (!TextUtils.isEmpty(isEdited)){
                                        if (isEdited.equals("1")){
                                            String prevId = dataMap.get("PreviousId").toString();
                                            if (!TextUtils.isEmpty(prevId)){
                                                DocumentReference prevDocRef = firebaseFirestore.collection("AdvisorsDatabase")
                                                        .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                                        .collection("BroadcastCalls")
                                                        .document(prevId);
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("Active", "0");
                                                prevDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        prevDocRef.update(map);
                                                        progressDialog.dismiss();
                                                        Toast.makeText(NewBroadcastCall.this, "Call Uploaded", Toast.LENGTH_SHORT).show();
                                                        //Snackbar.make(getWindow().getDecorView(), "Blog Uploaded.", Snackbar.LENGTH_LONG).show();
                                                        //ImagePickerActivity.clearCache(NewBlogActivity.this);
                                                        onBackPressed();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(NewBroadcastCall.this, "Call Uploaded", Toast.LENGTH_SHORT).show();
                                                        //Snackbar.make(getWindow().getDecorView(), "Blog Uploaded.", Snackbar.LENGTH_LONG).show();
                                                        //ImagePickerActivity.clearCache(NewBlogActivity.this);
                                                        onBackPressed();
                                                    }
                                                });
                                            }
                                        }else{
                                            progressDialog.dismiss();
                                            Toast.makeText(NewBroadcastCall.this, "Call Uploaded", Toast.LENGTH_SHORT).show();
                                            //Snackbar.make(getWindow().getDecorView(), "Blog Uploaded.", Snackbar.LENGTH_LONG).show();
                                            //ImagePickerActivity.clearCache(NewBlogActivity.this);
                                            onBackPressed();
                                        }
                                    }else{
                                        progressDialog.dismiss();
                                        Toast.makeText(NewBroadcastCall.this, "Call Uploaded", Toast.LENGTH_SHORT).show();
                                        //Snackbar.make(getWindow().getDecorView(), "Blog Uploaded.", Snackbar.LENGTH_LONG).show();
                                        //ImagePickerActivity.clearCache(NewBlogActivity.this);
                                        onBackPressed();
                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                    }


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
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewBroadcastCall.this, "Check your internet connection", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(NewBroadcastCall.this, "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private String getCurrencyString() {

        String[] symbolArray = getResources().getStringArray(R.array.currency_symbol);
        return symbolArray[spCurrency.getSelectedItemPosition()];

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void init() {
        etScripName = findViewById(R.id.et_scrip_name);
        etEntryPrice = findViewById(R.id.et_entry_price);
        etTargetPrice = findViewById(R.id.et_target_price);
        etCmp = findViewById(R.id.et_cmp);
        etStopLoss = findViewById(R.id.et_stop_loss);
        etNotes = findViewById(R.id.et_notes);
        tvStopLoss = findViewById(R.id.tv_new_call_stop_loss);
        //rlBlogImage = findViewById(R.id.rl_blog_image);
        //ivBlogImage = findViewById(R.id.iv_blog_image);
        btCancel = findViewById(R.id.bt_cancel);
        btSave = findViewById(R.id.bt_save);
        btPreview = findViewById(R.id.bt_preview);

        spCurrency = findViewById(R.id.sp_currency);
        spBuySell = findViewById(R.id.sp_buy_sell);

        randomName = getSaltString();


        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        String scripName = getIntent().getStringExtra("ScripName");
        String entryPrice = getIntent().getStringExtra("EntryPrice");
        String targetPrice = getIntent().getStringExtra("TargetPrice");
        String cmp = getIntent().getStringExtra("CMP");
        String stopLoss = getIntent().getStringExtra("StopLoss");
        int specType = getIntent().getIntExtra("SpecType", 3);
        String notes = getIntent().getStringExtra("Notes");
        String currencyId = getIntent().getStringExtra("CurrencyId");
        String buySell = getIntent().getStringExtra("BuySell");
        if (!TextUtils.isEmpty(scripName)){
            etScripName.setText(scripName);
        }if (!TextUtils.isEmpty(entryPrice)){
            etEntryPrice.setText(entryPrice);
        }if (!TextUtils.isEmpty(targetPrice)){
            etTargetPrice.setText(targetPrice);
        }if (!TextUtils.isEmpty(cmp)){
            etCmp.setText(cmp);
        }if (specType==1){
            tvStopLoss.setText("Stop Loss");
            etStopLoss.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            if (!TextUtils.isEmpty(stopLoss)) {
                etStopLoss.setText(stopLoss);
            }

        }if (specType==2){
            tvStopLoss.setText("Hold Period");
            etStopLoss.setInputType(InputType.TYPE_CLASS_TEXT);
            if (!TextUtils.isEmpty(stopLoss)) {
                etStopLoss.setText(stopLoss);
            }
        }if (!TextUtils.isEmpty(notes)){
            etNotes.setText(notes);
        }if (!TextUtils.isEmpty(currencyId)){
            spCurrency.setSelection(Integer.parseInt(currencyId));
        }if (!TextUtils.isEmpty(buySell)){
            if (buySell.equals("BUY")){
                spBuySell.setSelection(0);
            }else if(buySell.equals("SELL")){
                spBuySell.setSelection(1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //ImagePickerActivity.clearCache(NewBlogActivity.this);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}
