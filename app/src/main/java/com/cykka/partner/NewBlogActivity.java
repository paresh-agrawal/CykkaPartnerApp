package com.cykka.partner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class NewBlogActivity extends AppCompatActivity {

    private TextInputEditText etBlogTitle, etBlogDescription, etBlogLink;
    private RelativeLayout rlBlogImage;
    private ImageView ivBlogImage;
    private Button btCancel, btSave;
    public static int REQUEST_IMAGE = 110;
    private Uri blogImageUri=null;
    private UploadTask upload_task, thumb_upload_task;
    private Bitmap compressedThumbImageBitmap,compressedImageBitmap;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String randomName;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_blog);

        ActionBar actionBar = getSupportActionBar();
        String title = getIntent().getStringExtra("Type");
        actionBar.setTitle(title);
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        /*rlBlogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectImageClick(v);
            }
        });*/

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerActivity.clearCache(NewBlogActivity.this);
                onBackPressed();
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title, description, link;
                title = etBlogTitle.getText().toString();
                description = etBlogDescription.getText().toString();
                link = etBlogLink.getText().toString();
                if(TextUtils.isEmpty(title)){
                    etBlogTitle.setError("Please enter blog title");
                }else if(TextUtils.isEmpty(description)){
                    etBlogDescription.setError("Please enter blog description");
                }else if(TextUtils.isEmpty(link)){
                    etBlogLink.setError("Please enter blog link");
                }else{
                    String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("BlogTitle", title);
                    dataMap.put("BlogDescription", description);
                    dataMap.put("BlogLink", link);
                    dataMap.put("TimeStamp", timeStamp);
                    dataMap.put("Active", "1");
                    hideKeyboard(getApplicationContext(), v);
                    uploadBlog(dataMap);

                }
            }
        });
    }

    /*private void uploadBlogImage( Map<String, Object> dataMap) {

        progressDialog = new ProgressDialog(NewBlogActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        if (blogImageUri!=null){
            File newImageFile = new File(Objects.requireNonNull(blogImageUri.getPath()));

            try {
                compressedImageBitmap = new Compressor(NewBlogActivity.this).setQuality(50).compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos_original = new ByteArrayOutputStream();
            compressedImageBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos_original);
            byte[] data_original = baos_original.toByteArray();

            final StorageReference storageRef = storageReference.child("Blogs")
                    .child(Objects.requireNonNull(mAuth.getCurrentUser().getUid()));
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
                        downloadUriMap.put("ImageUri", downloadUri);
                        downloadUriMap.put("ImageName", randomName);
                        uploadBlog(downloadUriMap);

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(NewBlogActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewBlogActivity.this, "Failed "+e.getMessage() , Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            uploadBlog(dataMap);
        }

    }*/

    private void uploadBlog(Map<String, Object> dataMap) {
        progressDialog = new ProgressDialog(NewBlogActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .collection("Blogs")
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

                                    progressDialog.dismiss();
                                    Toast.makeText(NewBlogActivity.this, "Blog Uploaded", Toast.LENGTH_SHORT).show();
                                    //Snackbar.make(getWindow().getDecorView(), "Blog Uploaded.", Snackbar.LENGTH_LONG).show();
                                    ImagePickerActivity.clearCache(NewBlogActivity.this);
                                    onBackPressed();
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
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(NewBlogActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
                }
            });
        }else {
            progressDialog.dismiss();
            Toast.makeText(NewBlogActivity.this, "Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }




    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void init() {
        etBlogTitle = findViewById(R.id.et_blog_title);
        etBlogDescription = findViewById(R.id.et_blog_description);
        etBlogLink = findViewById(R.id.et_blog_link);
        //rlBlogImage = findViewById(R.id.rl_blog_image);
        //ivBlogImage = findViewById(R.id.iv_blog_image);
        btCancel = findViewById(R.id.bt_cancel);
        btSave = findViewById(R.id.bt_save);

        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        randomName = getIntent().getStringExtra("Id");

        String title = getIntent().getStringExtra("Title");
        String description = getIntent().getStringExtra("Description");
        String link = getIntent().getStringExtra("Link");
        if (!TextUtils.isEmpty(title)){
            etBlogTitle.setText(title);
        }if (!TextUtils.isEmpty(description)){
            etBlogDescription.setText(description);
        }if (!TextUtils.isEmpty(link)){
            etBlogLink.setText(link);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ImagePickerActivity.clearCache(NewBlogActivity.this);
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


    /*public void onSelectImageClick(View view) {
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
        Intent intent = new Intent(NewBlogActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(NewBlogActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void showSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(NewBlogActivity.this);
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
                    blogImageUri = uri;
                    // loading profile image from local cache
                    loadProfile(uri.toString());


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    private void loadProfile(String url) {
        //Log.d(TAG, "Image cache path: " + url);
        if (ivBlogImage!=null){
            //Log.d("ImageView id : ",String.valueOf(imageView.getId()));

            Glide.with(this).load(url)
                    .into(ivBlogImage);
            ivBlogImage.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
        }

    }*/

}
