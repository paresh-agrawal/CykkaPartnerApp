package com.cykka.partner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.cykka.partner.ui.blog.BlogViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BroadcastActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private LinearLayout llAddNewBroadcastCall;
    private RecyclerView recyclerView;
    private BroadcastCallAdapter mAdapter;
    private List<BroadcastCall> broadcastCallList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private DataContext db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Broadcast Calls");
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        mAdapter = new BroadcastCallAdapter(broadcastCallList, getApplicationContext());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(BroadcastActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences specialisationPrefs = getSharedPreferences("DocVerification", Context.MODE_PRIVATE);
                int specType = specialisationPrefs.getInt("specialisationType", 3);//"No name defined" is the default value.
                Intent intent = new Intent(BroadcastActivity.this, NewBroadcastCall.class);
                intent.putExtra("Type", "Add New Broadcast Call");
                //intent.putExtra("Id", getSaltString());
                intent.putExtra("SpecType", specType);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBroadcastCalls();
            }
        });

        getBroadcastCalls();

    }

    private void getBroadcastCalls() {
        final CollectionReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(mAuth.getCurrentUser().getUid())
                .collection("BroadcastCalls");
        Query query = docRef.whereEqualTo("Active", "1");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }
                int i=0;
                //List<Subscriber> newList = new ArrayList<>();
                BroadcastCall broadcastCall;
                for (DocumentChange dc : value.getDocumentChanges()) {
                    i++;
                    QueryDocumentSnapshot doc = dc.getDocument();
                    broadcastCall = new BroadcastCall();
                    broadcastCall.setCallId(doc.getId());
                    broadcastCall.setIsActive(doc.getString("Active"));
                    broadcastCall.setBuySell(doc.getString("BuySell"));
                    broadcastCall.setCurrencyId(doc.getString("CurrencyId"));
                    broadcastCall.setCmp(doc.getString("CurrentMarketPrice"));
                    broadcastCall.setEntryPrice(doc.getString("EntryPrice"));
                    broadcastCall.setIsEdited(doc.getString("IsEdited"));
                    broadcastCall.setNotes(doc.getString("Note"));
                    broadcastCall.setScripName(doc.getString("ScripName"));
                    broadcastCall.setSpecType(doc.getString("SpecType"));
                    broadcastCall.setStopLossOrHoldingPeriod(doc.getString("StopLossOrHoldingPeriod"));
                    broadcastCall.setTargetPrice(doc.getString("TargetPrice"));
                    broadcastCall.setTimeStamp(doc.getString("TimeStamp"));
                    Log.d("docid", doc.getId());

                    switch (dc.getType()) {
                        case ADDED:
                        case MODIFIED:
                            db.addCall(broadcastCall);
                            //newList.add(subscriber);
                            Log.d("Call", "Modified city: " + dc.getDocument().getId());
                            break;
                        case REMOVED:
                            db.updateCall(broadcastCall);
                            Log.d("Call", "Removed city: " + dc.getDocument().getId());
                            break;
                    }
                }

                prepareBroadcastCallList();
            }
        });
    }

    public void prepareBroadcastCallList() {

        List<BroadcastCall> dbBroadcastCallList = db.getBroadcastCallList();
        broadcastCallList.clear();
        broadcastCallList.addAll(dbBroadcastCallList);
        if (dbBroadcastCallList.size()>0){
            llAddNewBroadcastCall.setVisibility(View.GONE);
        }else{
            llAddNewBroadcastCall.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
        mAdapter.notifyDataSetChanged();
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

    @Override
    protected void onStart() {
        super.onStart();
        prepareBroadcastCallList();
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

    private void init() {
        db = new DataContext(BroadcastActivity.this);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.rv_broadcast_calls);
        llAddNewBroadcastCall = findViewById(R.id.ll_add_new_broadcast_call);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
    }

}
