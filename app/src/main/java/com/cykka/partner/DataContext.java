package com.cykka.partner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataContext extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Advisor.db";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String advisorId;

    //Subscriber Table
    public static final String SUBSCRIBER_TABLE = "subscriber_table";
    public static final String SUBSCRIBER_ID = "UserId";
    public static final String SUBSCRIBER_NAME = "Name";
    public static final String SUBSCRIBER_STATUS = "IsSubscribed";
    public static final String SUBSCRIBER_IMAGE = "ImgUri";

    //Message Table
    public static final String MESSAGES_TABLE = "messages_table";
    public static final String MSD_ID = "MsgId";
    public static final String MSG_UNIQUE_ID = "MsgUniqueId";
    public static final String MSG_FROM_ID = "FromId";
    public static final String MSG_TO_ID = "ToId";
    public static final String MSG_TEXT = "Text";
    public static final String MSG_TIMESTAMP = "Timestamp";

    //Broadcast Table
    public static final String BROADCAST_TABLE = "broadcast_table";
    public static final String CALL_ID = "CallId";
    public static final String IS_ACTIVE = "IsActive";
    public static final String BUY_SELL = "BuySell";
    public static final String CURRENCY_ID = "CurrencyId";
    public static final String CURRENT_MARKET_PRICE = "CurrentMarketPrice";
    public static final String ENTRY_PRICE = "EntryPrice";
    public static final String IS_EDITED = "IsEdited";
    public static final String NOTE = "Note";
    public static final String SCRIP_NAME = "ScripName";
    public static final String SPEC_TYPE = "SpecType";
    public static final String STOP_LOSS_OR_HOLDING_PERIOD = "StopLossOrHoldingPeriod";
    public static final String TARGET_PRICE = "TargetPrice";
    public static final String TIMESTAMP = "Timestamp";

    public DataContext(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tblSubscribers = "create table if not exists " + SUBSCRIBER_TABLE + "(" + SUBSCRIBER_ID + " TEXT PRIMARY KEY, " + SUBSCRIBER_NAME + " TEXT,"
                + SUBSCRIBER_STATUS + " TEXT, " + SUBSCRIBER_IMAGE + " TEXT);";

        String tblMessages = "create table if not exists " + MESSAGES_TABLE + "(" + MSD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MSG_FROM_ID + " TEXT,"
                + MSG_TO_ID + " TEXT, " + MSG_TEXT + " TEXT, " +MSG_UNIQUE_ID + " TEXT, "+ MSG_TIMESTAMP + " TEXT);";

        String tblBroadcast = "create table if not exists " + BROADCAST_TABLE + "(" + CALL_ID + " TEXT PRIMARY KEY, " + IS_ACTIVE + " TEXT,"
                + BUY_SELL + " TEXT, " + CURRENCY_ID + " TEXT, " +CURRENT_MARKET_PRICE + " TEXT, " + ENTRY_PRICE + " TEXT, "  + IS_EDITED + " TEXT, "
                + NOTE + " TEXT, " + SCRIP_NAME + " TEXT, " + SPEC_TYPE + " TEXT, " + STOP_LOSS_OR_HOLDING_PERIOD + " TEXT, " + TARGET_PRICE + " TEXT, " + TIMESTAMP + " TEXT);";

        db.execSQL(tblSubscribers);
        db.execSQL(tblMessages);
        db.execSQL(tblBroadcast);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*db.execSQL("DROP TABLE IF EXISTS "+ SUBSCRIBER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ MESSAGES_TABLE);*/
        onCreate(db);
    }

    public void addMessage(String msgFrom, String msgTo, String msgText, String msgUniqueId, String timestamp){

        if(!checkMsgAlreadyExist(msgUniqueId)) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MSG_FROM_ID, msgFrom);
            contentValues.put(MSG_TO_ID, msgTo);
            contentValues.put(MSG_TEXT, msgText);
            contentValues.put(MSG_UNIQUE_ID, msgUniqueId);
            contentValues.put(MSG_TIMESTAMP, timestamp);

            db.insert(MESSAGES_TABLE, null, contentValues);
        }
    }

    private boolean checkMsgAlreadyExist(String msgUniqueId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + MESSAGES_TABLE + " WHERE MsgUniqueId = '" + msgUniqueId + "';";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            Log.d("Cursor Count", String.valueOf(cursor.getCount()));
            cursor.close();
            return false;
        }


        Log.d("Msg already present","error ki mkc");
        cursor.close();
        return true;
    }

    public String getLastMessageTimestamp(String subscriberId, String advisorId){
        SQLiteDatabase db = getReadableDatabase();
        String query = "select rowid, * from " + MESSAGES_TABLE +
                        " where ("+ MSG_FROM_ID +" = '" + subscriberId + "' and "+ MSG_TO_ID + "='" + advisorId + "') " +
                        "or ("+ MSG_TO_ID +" = '" + subscriberId + "' and "+ MSG_FROM_ID +"='" + advisorId + "')  order by "+MSG_TIMESTAMP+" desc limit 1  ";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        try {
            //Log.d("last time"+ subscriberId, c.getString(c.getColumnIndex(MSG_TIMESTAMP)));
            //Log.d("last msg"+ subscriberId, c.getString(c.getColumnIndex(MSG_TEXT)));
            return c.getString(c.getColumnIndex(MSG_TIMESTAMP));
        } catch (Exception e) {
            return "0000.00.00.00.00.00";
        }
    }

    public String getLastMessage(String subscriberId, String advisorId){
        SQLiteDatabase db = getReadableDatabase();
        String query = "select rowid, * from " + MESSAGES_TABLE +
                " where ("+ MSG_FROM_ID +" = '" + subscriberId + "' and "+ MSG_TO_ID + "='" + advisorId + "') " +
                "or ("+ MSG_TO_ID +" = '" + subscriberId + "' and "+ MSG_FROM_ID +"='" + advisorId + "')  order by "+ MSG_TIMESTAMP +" desc limit 1  ";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        try {
            //Log.d("last time"+ subscriberId, c.getString(c.getColumnIndex(MSG_TIMESTAMP)));
            //Log.d("last msg"+ subscriberId, c.getString(c.getColumnIndex(MSG_TEXT)));
            return c.getString(c.getColumnIndex(MSG_TEXT));
        } catch (Exception e) {
            return "";
        }
    }

    public List<Message> getChat(String advisorId, String userId, int pageNo) {
        List<Message> messageList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try {
            int limit = (5 * pageNo) + 35;
            String whereCondition = "(("+ MSG_FROM_ID + " = '" + advisorId + "' and "+MSG_TO_ID+"='" + userId + "') or ("+MSG_TO_ID+" = '" + advisorId + "' and "+MSG_FROM_ID+"='" + userId + "'))";
            String query = "select * from ( select "+ MSG_TIMESTAMP +", * from "+MESSAGES_TABLE+" where " + whereCondition + " order by "+MSG_TIMESTAMP+" desc limit " + limit + ")  order by "+MSG_TIMESTAMP+"; ";
            //String query = "select * from ( select "+ MSG_TIMESTAMP +", * from "+MESSAGES_TABLE+" where " + whereCondition + " order by "+MSG_TIMESTAMP+" )  order by "+MSG_TIMESTAMP+"; ";
            Cursor c = db.rawQuery(query, null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Message mess = new Message();
                mess.setSenderId(c.getString(c.getColumnIndex(MSG_FROM_ID)));
                mess.setReceiverId(c.getString(c.getColumnIndex(MSG_TO_ID)));
                mess.setMsg(c.getString(c.getColumnIndex(MSG_TEXT)));
                mess.setTimestamp(c.getString(c.getColumnIndex(MSG_TIMESTAMP)));
                //Log.d("From id", c.getString(c.getColumnIndex(MSG_FROM_ID)));

                if (c.getString(c.getColumnIndex(MSG_FROM_ID)).equals(advisorId)){
                    mess.setType(1);
                }else {
                    mess.setType(2);
                }

                messageList.add(mess);
                c.moveToNext();
            }
            c.close();
            return messageList;
        } catch (Exception e) {
            e.printStackTrace();
            return messageList;
        }

    }


    public void addCall(BroadcastCall broadcastCall){

            if(!checkCallAlreadyExist(broadcastCall) ){
                SQLiteDatabase db = this.getWritableDatabase();

                ContentValues contentValues = new ContentValues();
                contentValues.put(CALL_ID, broadcastCall.getCallId());
                contentValues.put(IS_ACTIVE, broadcastCall.getIsActive());
                contentValues.put(BUY_SELL, broadcastCall.getBuySell());
                contentValues.put(CURRENCY_ID, broadcastCall.getCurrencyId());
                contentValues.put(CURRENT_MARKET_PRICE, broadcastCall.getCmp());
                contentValues.put(ENTRY_PRICE, broadcastCall.getEntryPrice());
                contentValues.put(IS_EDITED, broadcastCall.getIsEdited());
                contentValues.put(NOTE, broadcastCall.getNotes());
                contentValues.put(SCRIP_NAME, broadcastCall.getScripName());
                contentValues.put(SPEC_TYPE, broadcastCall.getSpecType());
                contentValues.put(STOP_LOSS_OR_HOLDING_PERIOD, broadcastCall.getStopLossOrHoldingPeriod());
                contentValues.put(TARGET_PRICE, broadcastCall.getTargetPrice());
                contentValues.put(TIMESTAMP, broadcastCall.getTimeStamp());

                //Log.d("Insert method", subscriber.getUserId());

                long res = db.insertOrThrow(BROADCAST_TABLE, null, contentValues);
                if (res == -1){
                    Log.d("Error", "inserting in database");
                }else {
                    Log.d("Id", String.valueOf(res));
                }
                Log.d("Call inserted", "true");
            }

    }

    public void updateCall(BroadcastCall broadcastCall){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CALL_ID, broadcastCall.getCallId());
        contentValues.put(IS_ACTIVE, "0");
        contentValues.put(BUY_SELL, broadcastCall.getBuySell());
        contentValues.put(CURRENCY_ID, broadcastCall.getCurrencyId());
        contentValues.put(CURRENT_MARKET_PRICE, broadcastCall.getCmp());
        contentValues.put(ENTRY_PRICE, broadcastCall.getEntryPrice());
        contentValues.put(IS_EDITED, broadcastCall.getIsEdited());
        contentValues.put(NOTE, broadcastCall.getNotes());
        contentValues.put(SCRIP_NAME, broadcastCall.getScripName());
        contentValues.put(SPEC_TYPE, broadcastCall.getSpecType());
        contentValues.put(STOP_LOSS_OR_HOLDING_PERIOD, broadcastCall.getStopLossOrHoldingPeriod());
        contentValues.put(TARGET_PRICE, broadcastCall.getTargetPrice());
        contentValues.put(TIMESTAMP, broadcastCall.getTimeStamp());
        db.update(BROADCAST_TABLE, contentValues, CALL_ID + "= ?", new String[]{broadcastCall.getCallId()});
    }

    private boolean checkCallAlreadyExist(BroadcastCall broadcastCall) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + BROADCAST_TABLE + " WHERE "+ CALL_ID + "= '" + broadcastCall.getCallId() + "';";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            Log.d("Cursor Count", String.valueOf(cursor.getCount()));
            cursor.close();
            return false;
        }
        if( cursor != null && cursor.moveToFirst() ){
            //String num = cursor.getString(cursor.getColumnIndex(SUBSCRIBER_STATUS));
            //Log.d("Cursor Count", cursor.getString(cursor.getColumnIndex(SUBSCRIBER_STATUS)));
            if (cursor.getString(cursor.getColumnIndex(IS_ACTIVE)).equals("0")){
                renewCall(broadcastCall);
            }
        }

        Log.d("Already present","Bhadhai ho");
        cursor.close();
        return true;
    }

    private void renewCall(BroadcastCall broadcastCall) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CALL_ID, broadcastCall.getCallId());
        contentValues.put(IS_ACTIVE, "1");
        contentValues.put(BUY_SELL, broadcastCall.getBuySell());
        contentValues.put(CURRENCY_ID, broadcastCall.getCurrencyId());
        contentValues.put(CURRENT_MARKET_PRICE, broadcastCall.getCmp());
        contentValues.put(ENTRY_PRICE, broadcastCall.getEntryPrice());
        contentValues.put(IS_EDITED, broadcastCall.getIsEdited());
        contentValues.put(NOTE, broadcastCall.getNotes());
        contentValues.put(SCRIP_NAME, broadcastCall.getScripName());
        contentValues.put(SPEC_TYPE, broadcastCall.getSpecType());
        contentValues.put(STOP_LOSS_OR_HOLDING_PERIOD, broadcastCall.getStopLossOrHoldingPeriod());
        contentValues.put(TARGET_PRICE, broadcastCall.getTargetPrice());
        contentValues.put(TIMESTAMP, broadcastCall.getTimeStamp());
        db.update(BROADCAST_TABLE, contentValues, CALL_ID + "= ?", new String[]{broadcastCall.getCallId()});
        Log.d("Renewed", "asfa");
    }

    public List<BroadcastCall> getBroadcastCallList() {

        List<BroadcastCall> broadcastCallList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + BROADCAST_TABLE + " WHERE " + IS_ACTIVE + "= '1'";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()){
            Log.d("Sub list id", cursor.getString(0));

            //String lastMsg = getLastMessage(cursor.getString(0), advisorId);
            //String timestamp = getLastMessageTimestamp(cursor.getString(0), advisorId);
            BroadcastCall broadcastCall = new BroadcastCall();
            broadcastCall.setCallId(cursor.getString(0));
            broadcastCall.setIsActive(cursor.getString(1));
            broadcastCall.setBuySell(cursor.getString(2));
            broadcastCall.setCurrencyId(cursor.getString(3));
            broadcastCall.setCmp(cursor.getString(4));
            broadcastCall.setEntryPrice(cursor.getString(5));
            broadcastCall.setIsEdited(cursor.getString(6));
            broadcastCall.setNotes(cursor.getString(7));
            broadcastCall.setScripName(cursor.getString(8));
            broadcastCall.setSpecType(cursor.getString(9));
            broadcastCall.setStopLossOrHoldingPeriod(cursor.getString(10));
            broadcastCall.setTargetPrice(cursor.getString(11));
            broadcastCall.setTimeStamp(cursor.getString(12));
            broadcastCallList.add(broadcastCall);

        }
        cursor.close();

        Collections.sort(broadcastCallList, new Comparator<BroadcastCall>() {
            @Override
            public int compare(BroadcastCall o1, BroadcastCall o2) {
                return o2.getTimeStamp().compareTo(o1.getTimeStamp());
            }
        });

        return broadcastCallList;

    }

    public Cursor viewAllSubs(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+SUBSCRIBER_TABLE, null);
        //res.moveToNext();
        return res;
    }

    public Cursor viewAllMessages(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+MESSAGES_TABLE, null);
        //res.moveToNext();
        return res;
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ MESSAGES_TABLE);
        SQLiteDatabase db1 = this.getWritableDatabase();
        db1.execSQL("delete from "+ SUBSCRIBER_TABLE);
    }

    public List<SubscriberListItem> getSubscriberList(String advisorId) {

        List<SubscriberListItem> subscriberListItemList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + SUBSCRIBER_TABLE + " WHERE " + SUBSCRIBER_STATUS + "= '1'";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()){
            Log.d("Sub list id", cursor.getString(0));

            String lastMsg = getLastMessage(cursor.getString(0), advisorId);
            String timestamp = getLastMessageTimestamp(cursor.getString(0), advisorId);
            SubscriberListItem subscriberListItem = new SubscriberListItem();
            subscriberListItem.setUserId(cursor.getString(0));
            subscriberListItem.setName(cursor.getString(1));
            subscriberListItem.setLastMsg(lastMsg);
            subscriberListItem.setImgUri("asfasf");
            subscriberListItem.setTimeStamp(timestamp);
            subscriberListItemList.add(subscriberListItem);
        }
        cursor.close();

        Collections.sort(subscriberListItemList, new Comparator<SubscriberListItem>() {
            @Override
            public int compare(SubscriberListItem o1, SubscriberListItem o2) {
                return o2.getTimeStamp().compareTo(o1.getTimeStamp());
            }
        });

        return subscriberListItemList;

    }

    public void addSubscriber(Subscriber subscriber){

        if(!checkSubscriberAlreadyExist(subscriber) ){
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(SUBSCRIBER_ID, subscriber.getUserId());
            contentValues.put(SUBSCRIBER_NAME, subscriber.getName());
            contentValues.put(SUBSCRIBER_STATUS, subscriber.getStatus());
            contentValues.put(SUBSCRIBER_IMAGE, subscriber.getImgUri());

            Log.d("Insert method", subscriber.getUserId());

            long res = db.insertOrThrow(SUBSCRIBER_TABLE, null, contentValues);
            if (res == -1){
                Log.d("Error", "inserting in database");
            }else {
                Log.d("Id", String.valueOf(res));
            }
            Log.d("Sub inserted", "true");
        }

    }

    public void updateSubscriber(Subscriber subscriber){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBSCRIBER_ID, subscriber.getUserId());
        contentValues.put(SUBSCRIBER_NAME, subscriber.getName());
        contentValues.put(SUBSCRIBER_STATUS, "0");
        contentValues.put(SUBSCRIBER_IMAGE, subscriber.getImgUri());
        db.update(SUBSCRIBER_TABLE, contentValues, SUBSCRIBER_ID + "= ?", new String[]{subscriber.getUserId()});
    }

    private boolean checkSubscriberAlreadyExist(Subscriber subscriber) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + SUBSCRIBER_TABLE + " WHERE UserId = '" + subscriber.getUserId() + "';";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            Log.d("Cursor Count", String.valueOf(cursor.getCount()));
            cursor.close();
            return false;
        }
        if( cursor != null && cursor.moveToFirst() ){
            //String num = cursor.getString(cursor.getColumnIndex(SUBSCRIBER_STATUS));
            //Log.d("Cursor Count", cursor.getString(cursor.getColumnIndex(SUBSCRIBER_STATUS)));
            if (cursor.getString(cursor.getColumnIndex(SUBSCRIBER_STATUS)).equals("0")){
                renewSubscriber(subscriber);
            }
        }

        Log.d("Already present","Bhadhai ho");
        cursor.close();
        return true;
    }

    private void renewSubscriber(Subscriber subscriber) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBSCRIBER_ID, subscriber.getUserId());
        contentValues.put(SUBSCRIBER_NAME, subscriber.getName());
        contentValues.put(SUBSCRIBER_STATUS, "1");
        contentValues.put(SUBSCRIBER_IMAGE, subscriber.getImgUri());
        db.update(SUBSCRIBER_TABLE, contentValues, SUBSCRIBER_ID + "= ?", new String[]{subscriber.getUserId()});
        Log.d("Renewed", "asfa");
    }


}
