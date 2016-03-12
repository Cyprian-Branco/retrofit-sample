package com.atahani.retrofit_sample.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.atahani.retrofit_sample.R;
import com.atahani.retrofit_sample.models.TweetModel;

import java.util.Collections;
import java.util.List;

/**
 * Tweet Adapter show tweet in Recycler view
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<TweetModel> mData = Collections.emptyList();
    private TweetEventHandler mTweetEventHandler;

    public TweetAdapter(Context context, TweetEventHandler tweetEventHandler) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTweetEventHandler = tweetEventHandler;
    }

    public void updateAdapterData(List<TweetModel> data) {
        this.mData = data;
    }


    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.tweet_row, parent, false);
        return new TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        TweetModel currentModel = mData.get(position);
        holder.mTxTweetBody.setText(currentModel.body);
        //convert char string into Hex code point
        int currentModeCodePoint = currentModel.feel.codePointAt(0);
        //produce the name of emoji drawable
        String drawable_name = "emoji_" + Integer.toHexString(currentModeCodePoint);
        //get the drawable resource id
        int icon = mContext.getResources().getIdentifier(drawable_name, "drawable", mContext.getPackageName());
        holder.mImMode.setImageDrawable(ContextCompat.getDrawable(mContext, icon));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * view holder for tweet adapter we have one view as tweet_row.xml layout
     */
    public class TweetViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mTxTweetBody;
        private AppCompatImageButton mImEdit;
        private AppCompatImageButton mImDelete;
        private ImageView mImMode;

        public TweetViewHolder(View itemView) {
            super(itemView);
            mTxTweetBody = (AppCompatTextView) itemView.findViewById(R.id.tx_tweet_body);
            mImEdit = (AppCompatImageButton) itemView.findViewById(R.id.im_edit);
            mImDelete = (AppCompatImageButton) itemView.findViewById(R.id.im_delete);
            mImMode = (ImageView) itemView.findViewById(R.id.im_mode);
            mImDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTweetEventHandler != null) {
                        mTweetEventHandler.onDeleteTweet(mData.get(getAdapterPosition()).id, getAdapterPosition());
                    }
                }
            });

            mImEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTweetEventHandler != null) {
                        mTweetEventHandler.onEditTweet(mData.get(getAdapterPosition()).id, getAdapterPosition());
                    }
                }
            });
        }
    }


    /**
     * define interface to handle events
     */
    public interface TweetEventHandler {
        void onEditTweet(String tweetId, int position);

        void onDeleteTweet(String tweetId, int position);
    }
}
