package com.cykka.partner.ui.blog;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.cykka.partner.Blog;
import com.cykka.partner.BlogAdapter;
import com.cykka.partner.BlogView;
import com.cykka.partner.ChatActivity;
import com.cykka.partner.MainActivity;
import com.cykka.partner.NewBlogActivity;
import com.cykka.partner.R;
import com.cykka.partner.RecyclerTouchListener;
import com.cykka.partner.Review;
import com.cykka.partner.ReviewAdapter;
import com.cykka.partner.SubscriberListItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Objects;
import java.util.Random;

public class BlogFragment extends Fragment {

    private BlogViewModel blogViewModel;
    private FloatingActionButton fab;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private LinearLayout llAddNewBlog;
    private RecyclerView recyclerView;
    private BlogAdapter mAdapter;
    private List<Blog> blogList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        blogViewModel =
                ViewModelProviders.of(this).get(BlogViewModel.class);
        View root = inflater.inflate(R.layout.fragment_blog, container, false);

        init(root);

        mAdapter = new BlogAdapter(blogList, requireActivity().getApplicationContext(), BlogFragment.this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /*recyclerView.addOnItemTouchListener(new RecyclerTouchListener(requireActivity().getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Blog blog = blogList.get(position);
                Intent intent = new Intent(requireActivity().getApplicationContext(), BlogView.class);
                intent.putExtra("Link", blog.getLink());
                intent.putExtra("Title", blog.getTitle());
                //getNewMessages(movie.getUserId());
                //getActivity().finish();
                startActivity(intent);
                //getActivity().finish();
                //Toast.makeText(requireActivity().getApplicationContext(), movie.getUserId() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));*/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity().getApplicationContext(), NewBlogActivity.class);
                intent.putExtra("Type", "Add New Blog");
                intent.putExtra("Id", getSaltString());
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBlogs();
            }
        });

        getBlogs();

        return root;
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

    private void init(View root) {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fab = root.findViewById(R.id.fab);
        recyclerView = root.findViewById(R.id.rv_blog_list);
        llAddNewBlog = root.findViewById(R.id.ll_add_new_blog);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout);
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void getBlogs() {


        final CollectionReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                .document(mAuth.getCurrentUser().getUid()).collection("Blogs");
        Query query = docRef.orderBy("TimeStamp", Query.Direction.DESCENDING);

        if (isNetworkAvailable(requireActivity().getApplicationContext())) {
            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    blogList.clear();
                    DocumentSnapshot lastVisible = null;
                    if (queryDocumentSnapshots.size() != 0) {
                        llAddNewBlog.setVisibility(View.GONE);
                    } else {
                        llAddNewBlog.setVisibility(View.VISIBLE);
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        if (doc.getString("Active").equals("1")) {
                            Blog blog = new Blog();
                            blog.setTitle(doc.getString("BlogTitle"));
                            blog.setDescription(doc.getString("BlogDescription"));
                            blog.setLink(doc.getString("BlogLink"));
                            blog.setId(doc.getId());
                            blogList.add(blog);
                        } else {
                            continue;
                        }

                    /*String imageName = doc.getString("ImageName");
                    if (TextUtils.isEmpty(imageName)){
                        blog.setUrl("");
                        blogList.add(blog);
                        mAdapter.notifyDataSetChanged();
                    }else {
                        StorageReference thumbRef = storageReference.child("Blogs")
                                .child(Objects.requireNonNull(mAuth.getCurrentUser().getUid()))
                                .child("thumbnails").child(imageName+"_200x200.png");
                        thumbRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri downloadUrl)
                            {
                                blog.setUrl(downloadUrl.toString());
                                blogList.add(blog);
                                mAdapter.notifyDataSetChanged();
                                //do something with downloadurl
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }*/

                    }
                    if (blogList.isEmpty()) {
                        llAddNewBlog.setVisibility(View.VISIBLE);
                    }
                    mAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getBlogs();
    }
}
