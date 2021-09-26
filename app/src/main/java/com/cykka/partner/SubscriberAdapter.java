package com.cykka.partner;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SubscriberAdapter extends RecyclerView.Adapter<SubscriberAdapter.MyViewHolder> {

    private List<SubscriberListItem> subscriberList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, lastMsg, time, profileChar;
        public String imgUri;
        public ImageView ivProfileImg;

        public MyViewHolder(View view) {
            super(view);
            time = view.findViewById(R.id.tv_time);
            name = view.findViewById(R.id.tv_subcriber_name);
            lastMsg = view.findViewById(R.id.tv_subscriber_last_msg);
            ivProfileImg = view.findViewById(R.id.iv_subscriber_profile_img);
            profileChar = view.findViewById(R.id.tv_subscriber_profile_img);
        }
    }


    public SubscriberAdapter(List<SubscriberListItem> subscriberList, Context applicationContext) {
        this.subscriberList = subscriberList;
        this.context = applicationContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_chat_subscriber, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SubscriberListItem subscriber = subscriberList.get(position);
        String timeAgo = getTimeAgo(subscriber.getTimeStamp());

        int[] androidColors = context.getResources().getIntArray(R.array.rainbow);
        int randomColor = androidColors[new Random().nextInt(androidColors.length)];

        holder.name.setText(subscriber.getName());
        holder.lastMsg.setText(subscriber.getLastMsg());
        //holder.time.setText(timeAgo);

        holder.profileChar.setText(String.valueOf(subscriber.getName().charAt(0)));

        GradientDrawable bgShape = (GradientDrawable) holder.ivProfileImg.getBackground();
        bgShape.setColor(randomColor);

    }

    @Override
    public int getItemCount() {
        return subscriberList.size();
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(String timestamp) {

        SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date date = null;
        long time = 0;
        try {
            date = dt.parse(timestamp);
            time = Objects.requireNonNull(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
