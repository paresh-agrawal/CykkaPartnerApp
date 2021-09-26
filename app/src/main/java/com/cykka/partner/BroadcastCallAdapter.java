package com.cykka.partner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cykka.partner.ui.blog.BlogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BroadcastCallAdapter extends RecyclerView.Adapter<BroadcastCallAdapter.MyViewHolder> {

    private List<BroadcastCall> broadcastCallList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView scripName, buySell, date, entryPrice, targetPrice, cmp, stopLossOrHoldingPeriod, notes, stopLossType, edited;
        public View view1;
        public LinearLayout llBroadcastCall;

        public MyViewHolder(View view) {
            super(view);
            scripName = view.findViewById(R.id.tv_scrip_name);
            buySell = view.findViewById(R.id.tv_buy_sell);
            date = view.findViewById(R.id.tv_date);
            entryPrice = view.findViewById(R.id.tv_entry_price);
            targetPrice = view.findViewById(R.id.tv_target_price);
            cmp = view.findViewById(R.id.tv_cmp);
            stopLossOrHoldingPeriod = view.findViewById(R.id.tv_stop_loss);
            notes = view.findViewById(R.id.tv_notes);
            stopLossType = view.findViewById(R.id.tv_stop_loss_type);
            view1 = view.findViewById(R.id.view1);
            edited = view.findViewById(R.id.tv_edited);
            llBroadcastCall = view.findViewById(R.id.ll_broadcast_call);
        }
    }


    public BroadcastCallAdapter(List<BroadcastCall> broadcastCallList, Context applicationContext) {
        this.broadcastCallList = broadcastCallList;
        this.context = applicationContext;
    }

    @Override
    public BroadcastCallAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_broadcast_call_item, parent, false);

        return new BroadcastCallAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BroadcastCallAdapter.MyViewHolder holder, int position) {
        BroadcastCall broadcastCall = broadcastCallList.get(position);
        String timestamp = broadcastCall.getTimeStamp();
        String currency = getCurrency(broadcastCall.getCurrencyId());
        SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date callDate = null;
        try {
            callDate = dt.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dtNew = new SimpleDateFormat("MMM dd, yyyy HH:mm");

        assert callDate != null;
        holder.date.setText(dtNew.format(callDate));
        holder.scripName.setText(broadcastCall.getScripName());
        holder.entryPrice.setText(currency + broadcastCall.getEntryPrice());
        holder.targetPrice.setText(currency + broadcastCall.getTargetPrice());
        holder.cmp.setText(currency + broadcastCall.getCmp());

        if (broadcastCall.getSpecType().equals("1")){
            holder.stopLossType.setText("Stop Loss");
            holder.stopLossOrHoldingPeriod.setText(currency + broadcastCall.getStopLossOrHoldingPeriod());
        }else if (broadcastCall.getSpecType().equals("2")){
            holder.stopLossType.setText("Hold Period");
            holder.stopLossOrHoldingPeriod.setText(broadcastCall.getStopLossOrHoldingPeriod());
        }

        if (broadcastCall.getBuySell().equals("BUY")){
            holder.buySell.setText("BUY");
            holder.buySell.setBackgroundResource(R.color.done_green);
        }else{
            holder.buySell.setText("SELL");
            holder.buySell.setBackgroundResource(R.color.redColor);
        }

        if (!TextUtils.isEmpty(broadcastCall.getNotes())){
            holder.view1.setVisibility(View.VISIBLE);
            holder.notes.setVisibility(View.VISIBLE);
            holder.notes.setText(broadcastCall.getNotes());
        }else {
            holder.view1.setVisibility(View.INVISIBLE);
            holder.notes.setVisibility(View.GONE);
            holder.notes.setText("");
        }

        if (broadcastCall.getIsEdited().equals("1")){
            holder.edited.setVisibility(View.VISIBLE);
        }else{
            holder.edited.setVisibility(View.INVISIBLE);
        }

        holder.llBroadcastCall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showPopupMenu(v, broadcastCall);
                return false;
            }
        });
    }

    private String getCurrency(String currencyId) {
        String[] symbolArray = context.getResources().getStringArray(R.array.currency_symbol);
        return symbolArray[Integer.parseInt(currencyId)];
    }

    private void showPopupMenu(View view, BroadcastCall broadcastCall) {
        // inflate menu
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_edit_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        Intent intent = new Intent(context, NewBroadcastCall.class);
                        intent.putExtra("Type", "Edit Broadcast Call");
                        intent.putExtra("SpecType", Integer.parseInt(broadcastCall.getSpecType()));
                        intent.putExtra("Id", broadcastCall.getCallId());
                        intent.putExtra("ScripName", broadcastCall.getScripName());
                        intent.putExtra("EntryPrice", broadcastCall.getEntryPrice());
                        intent.putExtra("TargetPrice", broadcastCall.getTargetPrice());
                        intent.putExtra("CMP", broadcastCall.getCmp());
                        intent.putExtra("StopLoss", broadcastCall.getStopLossOrHoldingPeriod());
                        intent.putExtra("Notes", broadcastCall.getNotes());
                        intent.putExtra("CurrencyId", broadcastCall.getCurrencyId());
                        intent.putExtra("BuySell", broadcastCall.getBuySell());
                        view.getContext().startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        AlertDialog.Builder alertbox = new AlertDialog.Builder(view.getRootView().getContext());
                        alertbox.setMessage("\nAre you sure you want to delete the Call?");
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
                                        .collection("BroadcastCalls")
                                        .document(broadcastCall.getCallId());
                                Map<String, Object> map = new HashMap<>();
                                map.put("Active", "0");

                                if (isNetworkAvailable(view.getContext())){
                                    docRef.update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
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
        return broadcastCallList.size();
    }
}
