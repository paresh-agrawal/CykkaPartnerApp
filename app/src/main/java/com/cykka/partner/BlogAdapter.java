package com.cykka.partner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cykka.partner.ui.blog.BlogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.MyViewHolder> {

private List<Blog> blogList;
private Context context;
private BlogFragment fragment;

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView title, description;
    public ImageView ivOptions;
    public ConstraintLayout layoutBlog;
    public RelativeLayout rlBlog;
    //public ImageView ivBlog;

    public MyViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.tv_blog_title);
        description = view.findViewById(R.id.tv_blog_description);
        ivOptions = view.findViewById(R.id.iv_options);
        layoutBlog = view.findViewById(R.id.layout_blog);
        rlBlog = view.findViewById(R.id.rl_blog);
        //ivBlog = view.findViewById(R.id.iv_blog);
    }
}


    public BlogAdapter(List<Blog> blogList, Context applicationContext, BlogFragment fragment) {
        this.blogList = blogList;
        this.context = applicationContext;
        this.fragment = fragment;
    }

    @Override
    public BlogAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_blog_list_item, parent, false);

        return new BlogAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BlogAdapter.MyViewHolder holder, int position) {

        Blog blog = blogList.get(position);

        holder.title.setText(blog.getTitle());
        holder.description.setText(blog.getDescription());

        holder.rlBlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadBlog(blog, v);
            }
        });

        holder.rlBlog.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopupMenu(v, blog);
                return false;
            }
        });

        holder.ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.ivOptions, blog);
            }
        });

    }

    private void loadBlog(Blog blog, View v) {
        Intent intent = new Intent(context, BlogView.class);
        intent.putExtra("Link", blog.getLink());
        intent.putExtra("Title", blog.getTitle());
        //getNewMessages(movie.getUserId());
        //getActivity().finish();
        v.getContext().startActivity(intent);
    }

    private void showPopupMenu(View view, Blog blog) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        Intent intent = new Intent(context, NewBlogActivity.class);
                        intent.putExtra("Type", "Edit Blog");
                        intent.putExtra("Id", blog.getId());
                        intent.putExtra("Title", blog.getTitle());
                        intent.putExtra("Description", blog.getDescription());
                        intent.putExtra("Link", blog.getLink());
                        view.getContext().startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        AlertDialog.Builder alertbox = new AlertDialog.Builder(view.getRootView().getContext());
                        alertbox.setMessage("\nAre you sure you want to delete the Blog?");
                        alertbox.setTitle("Delete Blog");
                        alertbox.setIcon(R.drawable.ic_delete);

                        alertbox.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ProgressDialog progressDialog = new ProgressDialog(view.getContext(),
                                        R.style.AppTheme_Dark_Dialog);
                                progressDialog.setIndeterminate(true);
                                progressDialog.setMessage("Deleting...");
                                progressDialog.show();
                                progressDialog.setCanceledOnTouchOutside(false);
                                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                DocumentReference docRef = firebaseFirestore.collection("AdvisorsDatabase")
                                        .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                        .collection("Blogs")
                                        .document(blog.getId());
                                Map<String, Object> map = new HashMap<>();
                                map.put("Active", "0");

                                if (isNetworkAvailable(view.getContext())){
                                    docRef.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            ((BlogFragment)fragment).getBlogs();
                                            progressDialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                        }
                                    });
                                    return;
                                }else {
                                    progressDialog.dismiss();
                                    Toast.makeText(view.getContext(), "Check your internet connection", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                        alertbox.show();
                        return true;
                    default:
                }
                return false;
            }
        });
        popup.show();
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }
}