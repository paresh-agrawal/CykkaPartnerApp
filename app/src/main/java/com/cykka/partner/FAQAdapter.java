package com.cykka.partner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.MyViewHolder> {

    private List<FAQ> faqList;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView question, answer;
        public ImageView ivFaq;
        public RelativeLayout rlFaq;
        //public ImageView ivBlog;

        public MyViewHolder(View view) {
            super(view);
            question = view.findViewById(R.id.tv_question);
            answer = view.findViewById(R.id.tv_answer);
            ivFaq = view.findViewById(R.id.iv_faq);
            rlFaq = view.findViewById(R.id.rl_faq);
        }
    }


    public FAQAdapter(List<FAQ> faqList) {
        this.faqList = faqList;
    }

    @Override
    public FAQAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_faq_item, parent, false);

        return new FAQAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FAQAdapter.MyViewHolder holder, int position) {

        FAQ faq = faqList.get(position);

        holder.question.setText(faq.getQuestion());
        holder.answer.setText(faq.getAnswer());
        final boolean[] expand = {false};

        holder.rlFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!expand[0]) {
                    expand[0] = true;
                    holder.answer.setVisibility(View.VISIBLE);
                    holder.ivFaq.setRotation(180);
                    /*animation = ObjectAnimator.ofInt(tvFaq1, "maxLines", 40);
                    animation.end();
                    animation.setDuration(500).start();*/
                    //btnSeeMore.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_collapse));
                } else {
                    expand[0] = false;
                    holder.answer.setVisibility(View.GONE);
                    holder.ivFaq.setRotation(0);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }
}