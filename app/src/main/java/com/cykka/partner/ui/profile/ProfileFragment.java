package com.cykka.partner.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.cykka.partner.Compressor;
import com.cykka.partner.IdentityVerificationActivity;
import com.cykka.partner.ImagePickerActivity;
import com.cykka.partner.MainActivity;
import com.cykka.partner.NewBlogActivity;
import com.cykka.partner.R;
import com.cykka.partner.Review;
import com.cykka.partner.ReviewAdapter;
import com.cykka.partner.ReviewHolder;
import com.cykka.partner.SubscriberAdapter;
import com.cykka.partner.SubscriberListItem;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private TextView tvAboutMe;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private RecyclerView recyclerView;
    private ReviewAdapter mAdapter;
    private List<Review> reviewList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private TextView tvRatingAchievement, tvReviewCount, tvRatingMain, tvTotalReview, tv5StarCount, tv4StarCount,
            tv3StarCount, tv2StarCount, tv1StarCount;
    private ProgressBar pb5Star, pb4Star, pb3Star, pb2Star, pb1Star;
    private RatingBar rbRating;
    private CircleImageView ivProfileImage;
    private TextView tvName, tvPreferredLanguages, tvSpecialisation, tvExperience, tvClientCount, tvBlogCount, tvWriteSomething;
    //private CardView cvProfile, cvAboutMe;
    private ConstraintLayout clAboutMe, clName, clLanguages;
    private Uri uriImage;
    private ImageView ivEditProfileImage;
    public static int REQUEST_IMAGE = 0;
    private UploadTask upload_task, thumb_upload_task;
    private Bitmap compressedThumbImageBitmap,compressedImageBitmap;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
       // ((MainActivity) getActivity()).setActionBarTitle("Profile");
        init(root);

        mAdapter = new ReviewAdapter(reviewList, requireActivity().getApplicationContext());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (isAdded()) {
            getProfileImage();
            getReviews(null);
            getRating();
            getProfileInfo();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getProfileImage();
                getReviews(null);
                getRating();
                getProfileInfo();
            }
        });

        clAboutMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String aboutMe = tvAboutMe.getText().toString();
                if (TextUtils.isEmpty(aboutMe)){
                    aboutMe="";
                }
                ((MainActivity) getActivity()).editAboutMe(aboutMe, "About Me", "This description will be visible to the users.");
            }
        });

        clName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = tvName.getText().toString();
                if (TextUtils.isEmpty(name)){
                    name="";
                }
                ((MainActivity) getActivity()).editAboutMe(name, "Name", "This name will be visible to the users.");
            }
        });

        clLanguages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String languages = tvPreferredLanguages.getText().toString();
                if (TextUtils.isEmpty(languages)){
                    languages="";
                }
                ((MainActivity) getActivity()).languagePicker(languages);
            }
        });

        if (uriImage != null){
            loadProfile(uriImage.toString());
        }

        ivEditProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                REQUEST_IMAGE = 114;
                onSelectImageClick(v);
            }
        });

        return root;
    }

    private void loadProfile(String url) {
        //Log.d(TAG, "Image cache path: " + url);
        if (ivProfileImage!=null){
            //Log.d("ImageView id : ",String.valueOf(imageView.getId()));
            if (isAdded()) {
                Glide.with(this).load(url).placeholder(R.drawable.ic_profile)
                        .into(ivProfileImage);
                ivProfileImage.setColorFilter(ContextCompat.getColor(requireActivity(), android.R.color.transparent));
                ImagePickerActivity.clearCache(requireActivity().getApplicationContext());
            }
        }

    }

    public void onSelectImageClick(View view) {
        Dexter.withActivity(requireActivity())
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
        ImagePickerActivity.showImagePickerOptions(requireActivity(), new ImagePickerActivity.PickerOptionListener(){
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
        Intent intent = new Intent(requireActivity(), ImagePickerActivity.class);
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
        Intent intent = new Intent(requireActivity(), ImagePickerActivity.class);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);

                    // loading profile image from local cache
                    uriImage = uri;
                    uploadImage();
                    //loadProfile(uri.toString());
                    //loadURI(uri, requestCode);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(requireActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        if (uriImage!=null){
            File newImageFile = new File(Objects.requireNonNull(uriImage.getPath()));

            try {
                compressedImageBitmap = new Compressor(requireActivity()).setQuality(50).compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos_original = new ByteArrayOutputStream();
            compressedImageBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos_original);
            byte[] data_original = baos_original.toByteArray();
            final String randomName = getSaltString();
            final StorageReference storageRef = storageReference.child("IdentityVerification")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser().getUid())).child("FaceVerification");
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
                        downloadUriMap.put("ProfileImage", downloadUri);
                        downloadUriMap.put("ProfileImageName", randomName);
                        downloadUriMap.put(randomName, downloadUri);
                        uploadDetails(downloadUriMap);

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(requireActivity(), "Error : " + error, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(requireActivity(), "Failed "+e.getMessage() , Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(requireActivity(), "Failed to load image" , Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadDetails(Map<String, Object> downloadUriMap) {
        final DocumentReference imageNameRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("IdProofs").document("FaceVerification");

        imageNameRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()){
                    imageNameRef.update(downloadUriMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loadProfile(uriImage.toString());
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(requireActivity().getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    imageNameRef.set(downloadUriMap);
                    Map<String, Object> boolMap = new HashMap<>();
                    boolMap.put("FaceVerification", 0);
                    imageNameRef.getParent().document("PhotoBoolean").update(boolMap);
                    loadProfile(uriImage.toString());
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(requireActivity().getApplicationContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void getProfileInfo() {
        final DocumentReference profileNameRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("IdProofs").document("PersonalInfo");

        profileNameRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    tvName.setText(snapshot.getString("Name"));
                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });

        final DocumentReference preferredLanguageRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("IdProofs").document("OtherDetails");

        preferredLanguageRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    tvPreferredLanguages.setText(snapshot.getString("Languages"));
                    String yearsOfExperience = setExperiece(snapshot.get("YearsOfExperience"));
                    tvExperience.setText(yearsOfExperience);
                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });

        final DocumentReference specialisationRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("IdProofs").document("Specialisation");

        specialisationRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    tvSpecialisation.setText(getSpec(snapshot.getString("SpecialisationText")));
                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });

        final DocumentReference advisorRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid());

        advisorRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                tvWriteSomething.setVisibility(View.GONE);
                if (snapshot != null && snapshot.exists()) {
                    String aboutMe = snapshot.getString("AboutMe");
                    if (!TextUtils.isEmpty(aboutMe)) {
                        tvWriteSomething.setVisibility(View.GONE);
                        tvAboutMe.setVisibility(View.VISIBLE);
                        tvAboutMe.setText(aboutMe);
                        //ifReadMore();
                    }else{
                        tvWriteSomething.setVisibility(View.VISIBLE);
                        tvAboutMe.setVisibility(View.INVISIBLE);
                        tvAboutMe.setText("");
                        //tvReadMore.setVisibility(View.GONE);
                    }
                } else {
                    Log.d("TAG", source + " data: null");
                }
            }
        });


        final CollectionReference subsRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("Subscribed");

        if (isNetworkAvailable(requireActivity().getApplicationContext())) {
            subsRef.whereEqualTo("Active", "1").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    tvClientCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    //tvClientCount.setText("1k");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        final CollectionReference blogsRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("Blogs");

        if (isNetworkAvailable(requireActivity().getApplicationContext())) {
            blogsRef.whereEqualTo("Active", "1").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    tvBlogCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        /*tvAboutMe.post(new Runnable() {
            @Override
            public void run() {
                if (tvAboutMe.getLineCount()>1){
                    tvReadMore.setVisibility(View.VISIBLE);
                    Log.d("Kya ho rha hai", "bc");
                }else {
                    tvReadMore.setVisibility(View.GONE);
                    Log.d("pata nhi", "bc");
                }

            }
        });
        Log.d("Line count", String.valueOf(tvAboutMe.getLineCount()));

        tvReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAboutMe.setMaxLines(Integer.MAX_VALUE);
                tvReadMore.setVisibility(View.GONE);
            }
        });*/



    }

    private String getSpec(String str) {
        if(str.equals("StocksFundamental")){return "Stocks (Fundamental)";}
        else if(str.equals("StocksTechnical")){return "Stocks (Technical)";}
        else if(str.equals("BondsDebtMarket")){return "Bonds/Debt Market";}
        else if(str.equals("BitcoinCryptoTrading")){return "Crypto Trading";}
        else if(str.equals("SilverGoldBullion")){return "Silver/Gold (Bullion)";}
        else if(str.equals("CurrencyForexTrading")){return "Currency/Forex Trading";}
        else if(str.equals("Commodity")){return "Commodity";}
        else if(str.equals("Insurance")){return "Insurance";}
        else if(str.equals("RetirementPlanning")){return "Retirement Planning";}
        else if(str.equals("TaxPlanning")){return "Tax Planning";}
        else if(str.equals("LoanSpecialist")){return "Loan Specialist";}
        else if(str.equals("MutualFunds")){return "Mutual Funds";}
        return "";
    }

    /*private void ifReadMore() {
        tvAboutMe.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("Line Count", String.valueOf(tvAboutMe.getLineCount()));
                if (tvAboutMe.getLineCount()>3){
                    tvReadMore.setVisibility(View.VISIBLE);
                    tvAboutMe.setMaxLines(3);
                    tvAboutMe.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.d("Kya ho rha hai", "bc");
                }else {
                    tvReadMore.setVisibility(View.GONE);
                    Log.d("pata nhi", "bc");
                }
            }
        });

        tvReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAboutMe.setMaxLines(Integer.MAX_VALUE);
                tvReadMore.setVisibility(View.GONE);
            }
        });
    }*/

    private String setExperiece(Object yearsOfExperience) {
        int experience = Integer.parseInt(String.valueOf(yearsOfExperience));
        switch (experience){
            case 1:
                return "Less than 1 year";
            case 2:
                return "1-2 years";
            case 3:
                return "2-5 years";
            case 4:
                return "5-10 years";
            case 5:
                return "10-15 years";
            case 6:
                return "More than 15 years";
            default:
                return "NA";

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

    private void getProfileImage() {

        final DocumentReference imageNameRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("IdProofs").document("FaceVerification");

        if (isNetworkAvailable(requireActivity().getApplicationContext())) {
            imageNameRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String imageName = documentSnapshot.getString("ProfileImageName");

                    StorageReference thumbRef = storageReference.child("IdentityVerification")
                            .child(Objects.requireNonNull(mAuth.getCurrentUser().getUid())).child("FaceVerification")
                            .child("thumbnails").child(imageName + "_200x200.png");
                    thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            if (isAdded()) {
                                swipeRefreshLayout.setRefreshing(false);
                                loadProfile(downloadUrl.toString());
                            }
                            //do something with downloadurl
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            swipeRefreshLayout.setRefreshing(false);
                            /*if (isAdded()) {
                                Toast.makeText(requireActivity().getApplicationContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                            }*/
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void init(View root) {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        tvAboutMe = root.findViewById(R.id.tv_about_me);
        //tvReadMore = root.findViewById(R.id.tv_read_more);
        recyclerView = root.findViewById(R.id.rv_review_list);

        tvRatingAchievement = root.findViewById(R.id.tv_rating_achievement);
        tvReviewCount = root.findViewById(R.id.tv_review_count);
        tvRatingMain = root.findViewById(R.id.tv_rating_main);
        tvTotalReview = root.findViewById(R.id.tv_total_review);
        tv1StarCount = root.findViewById(R.id.tv_1_star_count);
        tv2StarCount = root.findViewById(R.id.tv_2_star_count);
        tv3StarCount = root.findViewById(R.id.tv_3_star_count);
        tv4StarCount = root.findViewById(R.id.tv_4_star_count);
        tv5StarCount = root.findViewById(R.id.tv_5_star_count);
        pb1Star = root.findViewById(R.id.pb_1_star);
        pb2Star = root.findViewById(R.id.pb_2_star);
        pb3Star = root.findViewById(R.id.pb_3_star);
        pb4Star = root.findViewById(R.id.pb_4_star);
        pb5Star = root.findViewById(R.id.pb_5_star);
        rbRating = root.findViewById(R.id.rating_bar);
        ivProfileImage = root.findViewById(R.id.iv_profile_image);
        tvName = root.findViewById(R.id.tv_profile_name);
        tvPreferredLanguages = root.findViewById(R.id.tv_preferred_languages);
        tvSpecialisation = root.findViewById(R.id.tv_specialisation);
        tvExperience = root.findViewById(R.id.tv_experience);
        tvClientCount = root.findViewById(R.id.tv_client_count);
        tvBlogCount = root.findViewById(R.id.tv_blog_count);
        tvWriteSomething = root.findViewById(R.id.tv_write_something);
        //cvProfile = root.findViewById(R.id.cv_profile);
        //cvAboutMe = root.findViewById(R.id.cv_about_me);
        clAboutMe = root.findViewById(R.id.cl_about_me);
        clName = root.findViewById(R.id.cl_name);
        clLanguages = root.findViewById(R.id.cl_languages);
        ivEditProfileImage = root.findViewById(R.id.iv_edit_profile_image);

        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
    }

    private void getRating() {
        final DocumentReference totalRatingRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid());
        if (isNetworkAvailable(getActivity().getApplicationContext())) {
            totalRatingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String totalRating = document.getString("TotalRating");
                        String totalReviews = document.getString("TotalReviews");
                        String total1StarCount = document.getString("Total1StarCount");
                        String total2StarCount = document.getString("Total2StarCount");
                        String total3StarCount = document.getString("Total3StarCount");
                        String total4StarCount = document.getString("Total4StarCount");
                        String total5StarCount = document.getString("Total5StarCount");

                        float rating = 0;
                        int ratio1Star = 0, ratio2Star = 0, ratio3Star = 0, ratio4Star = 0, ratio5Star = 0;

                        if (Integer.parseInt(totalReviews) != 0) {
                            rating = (float) ((Float.parseFloat(totalRating) / Float.parseFloat(totalReviews)) * 1.0);
                            ratio1Star = (int) ((Float.parseFloat(total1StarCount) / Float.parseFloat(totalReviews)) * 100);
                            ratio2Star = (int) ((Float.parseFloat(total2StarCount) / Float.parseFloat(totalReviews)) * 100);
                            ratio3Star = (int) ((Float.parseFloat(total3StarCount) / Float.parseFloat(totalReviews)) * 100);
                            ratio4Star = (int) ((Float.parseFloat(total4StarCount) / Float.parseFloat(totalReviews)) * 100);
                            ratio5Star = (int) ((Float.parseFloat(total5StarCount) / Float.parseFloat(totalReviews)) * 100);
                            tvRatingAchievement.setText(String.format("%.1f", rating));
                            tvRatingMain.setText(String.format("%.2f", rating));
                            tvTotalReview.setText(totalReviews + " Total");
                        } else {
                            tvRatingAchievement.setText("NA");
                            tvRatingMain.setText("NA");
                            tvTotalReview.setText("NA");
                        }
                        String reviewCount = "(" + format(Long.parseLong(totalReviews)) + " Reviews)";
                        tvReviewCount.setText(Html.fromHtml(reviewCount));
                        rbRating.setRating(rating);
                        tv1StarCount.setText(format(Long.parseLong(total1StarCount)));
                        tv2StarCount.setText(format(Long.parseLong(total2StarCount)));
                        tv3StarCount.setText(format(Long.parseLong(total3StarCount)));
                        tv4StarCount.setText(format(Long.parseLong(total4StarCount)));
                        tv5StarCount.setText(format(Long.parseLong(total5StarCount)));
                        pb1Star.setProgress(ratio1Star);
                        pb2Star.setProgress(ratio2Star);
                        pb3Star.setProgress(ratio3Star);
                        pb4Star.setProgress(ratio4Star);
                        pb5Star.setProgress(ratio5Star);

                    }
                }
            });
        }else {
            Toast.makeText(getActivity().getApplicationContext(), "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    private void getReviews(DocumentSnapshot lastDoc) {

        Log.d("Get Riview", "called");

        final CollectionReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("Reviews");
        Query query = null;

        if (lastDoc==null){
            query = docRef.orderBy("TimeStamp", Query.Direction.DESCENDING)
                    .limit(25);
            reviewList.clear();
        }else{
            query = docRef.orderBy("TimeStamp", Query.Direction.DESCENDING)
                    .startAfter(lastDoc)
                    .limit(25);
        }
        if (isNetworkAvailable(getActivity().getApplicationContext())) {
            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    DocumentSnapshot lastVisible = null;
                    if (queryDocumentSnapshots.size() > 0) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Review review = new Review();
                        review.setName(doc.getString("Name"));
                        review.setReviewText(doc.getString("Text"));
                        review.setTimestamp(doc.getString("TimeStamp"));
                        review.setRating(doc.getString("Rating"));
                        reviewList.add(review);
                        mAdapter.notifyDataSetChanged();
                    }
                    if (queryDocumentSnapshots.size() == 5) {
                        getReviews(lastVisible);
                    } else {
                        return;
                    }
                }
            });
        }
    }

    private String getTermsString() {
        StringBuilder termsString = new StringBuilder();
        termsString.append("");
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getActivity().getAssets().open("terms.txt")));

            String str;
            while ((str = reader.readLine()) != null) {
                termsString.append(str);
            }

            reader.close();
            termsString.append("]]>");
            return termsString.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
