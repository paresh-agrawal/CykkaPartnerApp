package com.cykka.partner.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cykka.partner.BroadcastActivity;
import com.cykka.partner.ChatActivity;
import com.cykka.partner.DataContext;
import com.cykka.partner.IdentityVerificationActivity;
import com.cykka.partner.R;
import com.cykka.partner.RecyclerTouchListener;
import com.cykka.partner.Subscriber;
import com.cykka.partner.SubscriberAdapter;
import com.cykka.partner.SubscriberListItem;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatFragment extends Fragment {

    private CardView cvDocVerification;
    private ImageView ivDocVerification, ivCancel;
    private TextView tvDocVerificationTitle, tvDocVerificationSubtitle, tvNoSubs;
    private SharedPreferences.Editor editor;
    private List<SubscriberListItem> subscriberList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SubscriberAdapter mAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private DataContext db;
    private LinearLayout rlChat;
    private RelativeLayout rlBroadcast;
    private View view1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        init(root);

        rlBroadcast.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity().getApplicationContext(), BroadcastActivity.class);
            startActivity(intent);
        });




        mAdapter = new SubscriberAdapter(subscriberList, requireActivity().getApplicationContext());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(requireActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                SubscriberListItem movie = subscriberList.get(position);
                Intent intent = new Intent(requireActivity().getApplicationContext(), ChatActivity.class);
                intent.putExtra("UserId", movie.getUserId());
                intent.putExtra("AdvisorId", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        getSubscribers();
        getNewMessages();
        docVerificationBool();
        getSpecialisation();

        return root;
    }



    private void init(View root) {

        db = new DataContext(requireActivity().getApplicationContext());

        cvDocVerification = root.findViewById(R.id.cv_doc_verification);
        ivDocVerification = root.findViewById(R.id.iv_doc_verification);
        tvDocVerificationTitle = root.findViewById(R.id.tv_doc_verification_title);
        tvDocVerificationSubtitle = root.findViewById(R.id.tv_doc_verification_subtitle);
        ivCancel = root.findViewById(R.id.iv_cancel);
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = root.findViewById(R.id.rv_subscriber_list);
        rlChat = root.findViewById(R.id.rl_chat);
        rlBroadcast = root.findViewById(R.id.rl_broadcast);
        view1 = root.findViewById(R.id.view1);
        tvNoSubs = root.findViewById(R.id.tv_no_subs);


        //Get Doc verified status from shared preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("DocVerification", Context.MODE_PRIVATE);

        boolean isVerified = prefs.getBoolean("isVerified", false);//"No name defined" is the default value.
        if (isVerified){
            cvDocVerification.setVisibility(View.GONE);
        }else{
            cvDocVerification.setVisibility(View.VISIBLE);
        }

        //Get Specialisation type from shared preferences
        SharedPreferences specialisationPrefs = requireActivity().getSharedPreferences("DocVerification", Context.MODE_PRIVATE);

        int specType = specialisationPrefs.getInt("specialisationType", 3);//"No name defined" is the default value.
        updateBroadcastType(specType);
    }

    private void getNewMessages() {

        final CollectionReference docRef = firebaseFirestore.collection("Chats");
        Query query = docRef.whereEqualTo("AdvisorId", Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if(e!=null){
                Toast.makeText(requireActivity().getApplicationContext(), "Error : " + e.toString(), Toast.LENGTH_SHORT).show();
                return;
            }
            for (QueryDocumentSnapshot doc : Objects.requireNonNull(queryDocumentSnapshots)) {

                docRef.document(doc.getId()).collection("Messages")
                        .whereEqualTo("AdvisorReceived", 0)
                        .addSnapshotListener((queryDocumentSnapshots1, e1) -> {
                            if (e1 !=null){
                                Toast.makeText(requireActivity().getApplicationContext(), "Error : " + e1.toString(), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (QueryDocumentSnapshot msgDoc : Objects.requireNonNull(queryDocumentSnapshots1)){
                                //add message to local database and set message received to 1
                                db.addMessage(msgDoc.getString("From"),msgDoc.getString("To"),
                                        msgDoc.getString("Message"), msgDoc.getId(), msgDoc.getString("TimeStamp"));
                                Map<String, Object> receiveMap = new HashMap<>();
                                receiveMap.put("AdvisorReceived", 1);
                                DocumentReference msgDocRef = docRef.document(doc.getId()).collection("Messages").document(msgDoc.getId());
                                msgDocRef.update(receiveMap);
                                prepareSubscribersList();


                            }
                        });
            }

        });
    }

    /*private void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.show();

    }*/

    private void getSubscribers() {

        final CollectionReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("Subscribed");
        Query query = docRef.whereEqualTo("Active", "1");
        query.addSnapshotListener((value, e) -> {
            if (e != null) {
                Log.w("TAG", "Listen failed.", e);
                return;
            }
            Subscriber subscriber;
            for (DocumentChange dc : Objects.requireNonNull(value).getDocumentChanges()) {
                QueryDocumentSnapshot doc = dc.getDocument();
                subscriber = new Subscriber();
                subscriber.setUserId(doc.getId());
                subscriber.setName(doc.getString("UserName"));
                subscriber.setStatus(doc.getString("Active"));
                subscriber.setImgUri(doc.getString("UserImgUri"));

                switch (dc.getType()) {
                    case ADDED:
                    case MODIFIED:
                        db.addSubscriber(subscriber);
                        break;
                    case REMOVED:
                        db.updateSubscriber(subscriber);
                        break;
                }
            }

            prepareSubscribersList();
        });
    }

    private void prepareSubscribersList() {

        List<SubscriberListItem> subscriberListItems = db.getSubscriberList(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        subscriberList.clear();
        subscriberList.addAll(subscriberListItems);
        if (subscriberList.size()==0){
            tvNoSubs.setVisibility(View.VISIBLE);
        }else{
            tvNoSubs.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareSubscribersList();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isNetworkAvailable(requireActivity().getApplicationContext())) {
            DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            docRef.get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                if (Objects.requireNonNull(document).exists()) {
                    Map<String, Object> docData = document.getData();
                    updateDocVerificationBool(Objects.requireNonNull(docData));
                } else {
                    Toast.makeText(requireActivity().getApplicationContext(), "There was some error please check your connection or restart the app.", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            Toast.makeText(requireActivity().getApplicationContext(), "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return Objects.requireNonNull(connectivityManager).getActiveNetworkInfo() != null && Objects.requireNonNull(connectivityManager.getActiveNetworkInfo()).isConnected();
    }

    private void getSpecialisation() {
        final DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                            .collection("IdProofs").document("Specialisation");
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(requireActivity().getApplicationContext(), "Error : "+e.toString(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> snapdata = snapshot.getData();

                updateSpecialisation(Objects.requireNonNull(snapdata));
            }
        });
    }
    private void updateSpecialisation(Map<String, Object> snapdata) {

        int specialisationType = 3;
        String specialisationText = String.valueOf(snapdata.get("SpecialisationText"));
        if (!TextUtils.isEmpty(specialisationText)){
            specialisationType = getSpecialisationType(specialisationText);
        }
        updateBroadcastType(specialisationType);

    }

    private void updateBroadcastType(int specialisationType) {
        if(isAdded()) {
            editor = requireActivity().getSharedPreferences("DocVerification", Context.MODE_PRIVATE).edit();
            switch (specialisationType) {
                case 1:
                    rlBroadcast.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.VISIBLE);
                    editor.putInt("specialisationType", 1);
                    editor.apply();
                    break;
                case 2:
                    rlBroadcast.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.VISIBLE);
                    editor.putInt("specialisationType", 2);
                    editor.apply();
                    break;
                case 3:
                    rlBroadcast.setVisibility(View.GONE);
                    view1.setVisibility(View.GONE);
                    editor.putInt("specialisationType", 3);
                    editor.apply();
                    break;
            }
        }
    }

    private int getSpecialisationType(String specialisationText) {

        switch (specialisationText){
            case "StocksTechnical":
            case "CurrencyForexTrading":
            case "BitcoinCryptoTrading":
            case "Commodity":
                return 1;
            case "StocksFundamental":
                return 2;
            case "MutualFunds":
            case "Insurance":
            case "LoanSpecialist":
            case "TaxPlanning":
            case "RetirementPlanning":
            default:
                return 3;
        }
    }

    private void updateDocVerificationBool(Map<String, Object> snapdata) {

        if(String.valueOf(snapdata.get("DocumentsUploadedBool")).equals(String.valueOf(false))){
            updateCardView(1);
        }else if(String.valueOf(snapdata.get("DocumentsUploadedBool")).equals(String.valueOf(true))
                && String.valueOf(snapdata.get("ManualVerified")).equals(String.valueOf(false))){
            updateCardView(2);
        }else if(String.valueOf(snapdata.get("DocumentsUploadedBool")).equals(String.valueOf(true))
                && String.valueOf(snapdata.get("DocumentsUploadedBool")).equals(String.valueOf(true))){
            updateCardView(3);
        }else {
            updateCardView(1);
        }
    }

    private void docVerificationBool() {
        final DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(requireActivity().getApplicationContext(), "Error : "+e.toString(), Toast.LENGTH_SHORT).show();
                return;
            }



            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> snapdata = snapshot.getData();

                updateDocVerificationBool(Objects.requireNonNull(snapdata));
            }
        });
    }



    @SuppressLint("SetTextI18n")
    private void updateCardView(int i) {
        if(isAdded()) {
            editor = requireActivity().getSharedPreferences("DocVerification", Context.MODE_PRIVATE).edit();
            switch (i) {
                case 1:
                    cvDocVerification.setVisibility(View.VISIBLE);
                    rlChat.setVisibility(View.GONE);
                    editor.putBoolean("isVerified", false);
                    editor.apply();
                    ivDocVerification.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_doc_not_uploaded));
                    tvDocVerificationTitle.setText("Complete Your Profile");
                    tvDocVerificationSubtitle.setText("Please complete your profile to verify your account. Your will start receiving clients once your account is verified.");
                    ivCancel.setVisibility(View.GONE);
                    cvDocVerification.setOnClickListener(v -> {
                        startActivity(new Intent(requireActivity().getApplicationContext(), IdentityVerificationActivity.class));
                        requireActivity().finish();
                    });
                    break;
                case 2:
                    cvDocVerification.setVisibility(View.VISIBLE);
                    rlChat.setVisibility(View.GONE);
                    ivDocVerification.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_doc_under_verification));
                    tvDocVerificationTitle.setText("Documents Under Verification");
                    tvDocVerificationSubtitle.setText("Your documents are under verification by Cykka team. We will let you know as soon as your documents are verified.");
                    ivCancel.setVisibility(View.GONE);
                    editor.putBoolean("isVerified", false);
                    editor.apply();
                    cvDocVerification.setOnClickListener(v -> Snackbar.make(v, "Documents under verification", Snackbar.LENGTH_LONG).show());
                    break;
                case 3:
                    ivDocVerification.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_doc_verified));
                    tvDocVerificationTitle.setText("Documents Verified");
                    tvDocVerificationSubtitle.setText("Congratulations! Your documents are now verified and you are a certified Cykka advisor.");
                    ivCancel.setVisibility(View.VISIBLE);
                    SharedPreferences.Editor editor = requireActivity().getSharedPreferences("DocVerification", Context.MODE_PRIVATE).edit();
                    ivCancel.setOnClickListener(v -> {
                        editor.putBoolean("isVerified", true);
                        editor.apply();
                        cvDocVerification.setVisibility(View.GONE);
                        rlChat.setVisibility(View.VISIBLE);
                        Snackbar.make(v, "Documents verified.", Snackbar.LENGTH_LONG).show();
                    });
                    cvDocVerification.setOnClickListener(v -> {
                        editor.putBoolean("isVerified", true);
                        editor.apply();
                        cvDocVerification.setVisibility(View.GONE);
                        rlChat.setVisibility(View.VISIBLE);
                        Snackbar.make(v, "Documents verified.", Snackbar.LENGTH_LONG).show();
                    });
                    break;
            }
        }

    }
}
