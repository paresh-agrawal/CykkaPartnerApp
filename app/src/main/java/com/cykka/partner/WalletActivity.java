package com.cykka.partner;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {

    private LinearLayout llWithdraw;
    private TextView tvWithdraw, tvFaq1;
    private ImageView ivFaq1;
    private RelativeLayout rlFaq1;
    private boolean expand1 = false;
    private ObjectAnimator animation;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private RecyclerView recyclerView;
    private FAQAdapter mAdapter;
    private List<FAQ> faqList = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private RelativeLayout rlBankDetails, rlBank, rlViewTransactions;
    private LinearLayout llBankDetails, llEditBankDetails;
    private ImageView ivBankDetails, ivViewTransactions;
    private TextView tvName, tvBankAccountNumber, tvIFSCCode, tvNoTransactions;
    private Button btEdit, btSave, btCancel;
    private EditText etName, etAccountNumber, etIFSCCode, etConfirmAccountNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        init();

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new FAQAdapter(faqList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Insufficient Balance", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        setAppBarOffset();
        loadFAQ();
        loadBankDetails();
        loadTransactions();

        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llBankDetails.setVisibility(View.GONE);
                llEditBankDetails.setVisibility(View.VISIBLE);
                String name = tvName.getText().toString();
                String accountNumber = tvBankAccountNumber.getText().toString();
                String ifscCode = tvIFSCCode.getText().toString();
                if (!TextUtils.isEmpty(name)){etName.setText(name);}
                if (!TextUtils.isEmpty(accountNumber)){etAccountNumber.setText(accountNumber);}
                if (!TextUtils.isEmpty(ifscCode)){etIFSCCode.setText(ifscCode);}
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llBankDetails.setVisibility(View.VISIBLE);
                llEditBankDetails.setVisibility(View.GONE);
                hideKeyboard(WalletActivity.this, v);
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String accountNumber = etAccountNumber.getText().toString();
                String ifscCode = etIFSCCode.getText().toString().toUpperCase();
                String confirmAccountNumber = etConfirmAccountNumber.getText().toString();

                if (TextUtils.isEmpty(name)){
                    etName.setError("Required");
                }else if(TextUtils.isEmpty(accountNumber)){
                    etAccountNumber.setError("Required");
                }else if(TextUtils.isEmpty(confirmAccountNumber)){
                    etConfirmAccountNumber.setError("Required");
                }else if(TextUtils.isEmpty(ifscCode)){
                    etIFSCCode.setError("Required");
                }else if(!confirmAccountNumber.equals(accountNumber)){
                    etConfirmAccountNumber.setError("Account Numbers do not match");
                }else{
                    Map<String, Object> bankDetailMap = new HashMap<>();
                    bankDetailMap.put("Name", name);
                    bankDetailMap.put("AccountNumber", accountNumber);
                    bankDetailMap.put("IFSCCode", ifscCode);
                    hideKeyboard(WalletActivity.this, v);
                    updateBankDetails(bankDetailMap);

                }
            }
        });

    }

    private void loadTransactions() {
        final boolean[] expand = {false};

        rlViewTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!expand[0]) {
                    expand[0] = true;
                    tvNoTransactions.setVisibility(View.VISIBLE);
                    ivViewTransactions.setRotation(270);
                } else {
                    expand[0] = false;
                    tvNoTransactions.setVisibility(View.GONE);
                    ivViewTransactions.setRotation(90);
                }
            }
        });
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateBankDetails(Map<String, Object> bankDetailMap) {
        final ProgressDialog progressDialog = new ProgressDialog(WalletActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Saving...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        final DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(mAuth.getCurrentUser().getUid())
                .collection("IdProofs").document("BankDetails");

        docRef.update(bankDetailMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                llBankDetails.setVisibility(View.VISIBLE);
                llEditBankDetails.setVisibility(View.GONE);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ImagePickerActivity.clearCache(WalletActivity.this);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadBankDetails() {
        final boolean[] expand = {false};

        rlBankDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!expand[0]) {
                    expand[0] = true;
                    rlBank.setVisibility(View.VISIBLE);
                    ivBankDetails.setRotation(270);
                } else {
                    expand[0] = false;
                    rlBank.setVisibility(View.GONE);
                    ivBankDetails.setRotation(90);
                }
            }
        });

        final DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(mAuth.getCurrentUser().getUid())
                .collection("IdProofs").document("BankDetails");

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }
                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Map<String, Object> snapdata = snapshot.getData();

                    getBankData(snapdata);
                    //Log.d("SnapData", source + " data: " + snapdata.get("PhotoId"));
                    //Log.d("TAG", source + " data: " + snapshot.getData());
                } else {
                    Log.d("TAG", source + " data: null");
                    btEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Snackbar.make(v, "Complete your identity verification to continue.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

    }

    private void getBankData(Map<String, Object> snapdata) {
        String name = String.valueOf(snapdata.get("Name"));
        String accountNumber = String.valueOf(snapdata.get("AccountNumber"));
        String ifscCode = String.valueOf(snapdata.get("IFSCCode"));

        if (!TextUtils.isEmpty(name)){tvName.setText(name);}
        if (!TextUtils.isEmpty(accountNumber)){tvBankAccountNumber.setText(accountNumber);}
        if (!TextUtils.isEmpty(ifscCode)){tvIFSCCode.setText(ifscCode);}
    }

    private void loadFAQ() {


        final CollectionReference docRef = firebaseFirestore.collection("FAQ");
        Query query = docRef.orderBy("Number", Query.Direction.ASCENDING);

        if (isNetworkAvailable(this)) {

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    faqList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                            FAQ faq = new FAQ();
                            faq.setQuestion(doc.getString("Question"));
                            faq.setAnswer(doc.getString("Answer"));
                            faqList.add(faq);
                    }
                    mAdapter.notifyDataSetChanged();
                    //swipeRefreshLayout.setRefreshing(false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //swipeRefreshLayout.setRefreshing(false);
                }
            });
        }

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void setAppBarOffset() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();

                }
                if (((float) (scrollRange + verticalOffset)/scrollRange) < 0.32) {
                    collapsingToolbarLayout.setTitle("My Wallet");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        /*appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case COLLAPSED:
                        fab.hide();
                        tvWithdraw.setVisibility(View.GONE);
                        break;
                    case EXPANDED:
                    case IDLE:
                        fab.show();
                        tvWithdraw.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });*/
    }

    private void init() {

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        //llWithdraw = findViewById(R.id.ll_withdraw);
        //tvWithdraw = findViewById(R.id.tv_withdraw);

        recyclerView = findViewById(R.id.rv_faq);

        rlBankDetails = findViewById(R.id.rl_update_bank_details);
        rlBank = findViewById(R.id.rl_bank);
        rlViewTransactions = findViewById(R.id.rl_view_transactions);

        llBankDetails = findViewById(R.id.ll_bank_details);
        llEditBankDetails = findViewById(R.id.ll_edit_bank_details);

        ivBankDetails = findViewById(R.id.iv_dropdown_bank_details);
        ivViewTransactions = findViewById(R.id.iv_dropdown_view_transactions);

        tvName = findViewById(R.id.tv_name);
        tvBankAccountNumber = findViewById(R.id.tv_account_number);
        tvIFSCCode = findViewById(R.id.tv_ifsc_code);
        tvNoTransactions = findViewById(R.id.tv_no_transactions);


        etName = findViewById(R.id.et_name);
        etAccountNumber = findViewById(R.id.et_account_number);
        etIFSCCode = findViewById(R.id.et_ifsc_code);
        etConfirmAccountNumber = findViewById(R.id.et_confirm_account_number);

        btEdit = findViewById(R.id.bt_edit);
        btCancel = findViewById(R.id.bt_cancel);
        btSave = findViewById(R.id.bt_save);

        /*rlFaq1 = findViewById(R.id.rl_faq1);
        tvFaq1 = findViewById(R.id.tv_faq1);
        ivFaq1 = findViewById(R.id.iv_faq1);*/

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        appBarLayout = findViewById(R.id.app_bar);
    }
}
