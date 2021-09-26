package com.cykka.partner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ChatActivity extends AppCompatActivity {

    private List<Message> messageList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MessageAdapter mAdapter;
    private EditText etChatBox;
    private String userId;
    private String advisorId;
    private DataContext db;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private CollectionReference docRef;
    private View btChatSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(v);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = findViewById(R.id.rv_message_list);
        etChatBox = findViewById(R.id.et_chat_box);
        btChatSend = findViewById(R.id.button_chat_box_send);

        mAdapter = new MessageAdapter(messageList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        userId = getIntent().getStringExtra("UserId");
        advisorId = getIntent().getStringExtra("AdvisorId");
        db = new DataContext(this);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        docRef = firebaseFirestore.collection("Chats");
        //String time = db.getLastMessageTimestamp(userId,advisorId);
        /*Message message = messageList.get(messageList.size()-1);
        String time = message.getTimestamp();*/
        //Log.d("abc", "baar baar");


        btChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });




    }

    private void sendMessage() {

        String messageText = etChatBox.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            etChatBox.setError("Enter a message.");
        }else{
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            final Map<String, Object> mess = new HashMap<>();
            mess.put("From", advisorId);
            mess.put("To", userId);
            mess.put("Message", messageText);
            mess.put("TimeStamp", timeStamp);
            mess.put("AdvisorReceived", 1);
            mess.put("SenderReceived", 0);
            final DocumentReference chatRef = firebaseFirestore.collection("Chats").document(advisorId+userId).collection("Messages").document();
            chatRef.set(mess);
            String msgId = chatRef.getId();

            Message message = new Message();
            message.setSenderId(advisorId);
            message.setReceiverId(userId);
            message.setMsg(messageText);
            message.setTimestamp(timeStamp);
            message.setType(1);
            messageList.add(message);
            mAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(messageList.size()-1);

            db.addMessage(advisorId,userId, messageText, msgId, timeStamp);
        }



    }

    private void prepareChat() {

        List<Message> chatList = db.getChat(advisorId, userId, 1);
        messageList.clear();
        messageList.addAll(chatList);
        mAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messageList.size()-1);
        //Log.d("MessageList", messageList.toString());

        String time = messageList.get(messageList.size()-1).getTimestamp();

        docRef.document(advisorId+userId).collection("Messages").orderBy("TimeStamp", Query.Direction.DESCENDING).whereGreaterThan("TimeStamp", time)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e!=null){
                            Log.w("TAG", "Listen failed.", e);
                            return;
                        }
                        for (QueryDocumentSnapshot msgDoc : queryDocumentSnapshots){
                            //add message to database
                            String latestTime = msgDoc.getString("TimeStamp");
                            //String lastTime = db.getLastMessageTimestamp(userId,advisorId);
                            String lastTime = messageList.get(messageList.size()-1).getTimestamp();
                            if (latestTime.compareTo(lastTime)>0){
                                //db.addMessage(msgDoc.getString("From"),msgDoc.getString("To"), msgDoc.getString("Message"), msgDoc.getString("TimeStamp"));
                                Log.d("Msg Timestamp", Objects.requireNonNull(msgDoc.getString("TimeStamp")));
                                Log.d("new message", "dasdadadas");
                                //prepareSubscribersList();
                                Log.d("idhar", "tak aya");
                                Message mess = new Message();
                                mess.setSenderId(msgDoc.getString("From"));
                                mess.setReceiverId(msgDoc.getString("To"));
                                mess.setMsg(msgDoc.getString("Message"));
                                mess.setTimestamp(msgDoc.getString("TimeStamp"));
                                if (msgDoc.getString("From").equals(advisorId)) {
                                    // login user
                                    mess.setType(1);
                                } else {
                                    mess.setType(2);
                                }
                                messageList.add(mess);
                                mAdapter.notifyDataSetChanged();
                                recyclerView.scrollToPosition(messageList.size()-1);
                            }


                        }
                    }
                });
    }



    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ChatActivity.this, SplashScreenActivity.class));
            finish();
            //Toast.makeText(ChatActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareChat();
    }
}
