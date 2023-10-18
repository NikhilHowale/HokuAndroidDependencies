package com.cometchat.pro.uikit.ui_components.messages.media_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.uikit.R;

import java.util.ArrayList;
import java.util.List;

class MultipleImagesViewPager extends RecyclerView.Adapter<MultipleImagesViewPager.MultipleViewHolder> {

    private List<BaseMessage> messageList = new ArrayList<>();
    private Context context;

    private static final int SHARED_MEDIA_IMAGE = 1;

    private static final int SHARED_MEDIA_VIDEO = 2;

    private static final int SHARED_MEDIA_FILE = 3;

    public MultipleImagesViewPager(Context mContext, List<BaseMessage> messageList) {
        this.context = mContext;
        setMessageList(messageList);
    }

    @NonNull
    @Override
    public MultipleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {

           /*
            case SHARED_MEDIA_IMAGE:
                view = layoutInflater.inflate(R.layout.cometchat_shared_media_image_row, parent, false);
                return new ImageViewHolder(view);

           case SHARED_MEDIA_VIDEO:
                view = layoutInflater.inflate(R.layout.cometchat_shared_media_video_row, parent, false);
                return new VideoViewHolder(view);

            case SHARED_MEDIA_FILE:
                view = layoutInflater.inflate(R.layout.cometchat_shared_media_file_row, parent, false);
                return new FileViewHolder(view);*/

            default:
                view = layoutInflater.inflate(R.layout.item_image_holder, parent, false);
                return new MultipleViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleViewHolder holder, int position) {
        BaseMessage baseMessage = messageList.get(position);
        if (baseMessage.getType().equals(com.cometchat.pro.constants.CometChatConstants.MESSAGE_TYPE_IMAGE)) {
            setImageData(baseMessage,(MultipleViewHolder) holder, position);
        }
    }

    private  void setImageData(BaseMessage baseMessage,MultipleViewHolder holder, int position){
        try {
            if(((MediaMessage)baseMessage).getAttachment() != null){
                Glide.with(context).asBitmap()
                        .load(((MediaMessage)baseMessage).getAttachment().getFileUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.mSingleImageView);
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewTypes(position);
    }

    private int getItemViewTypes(int position) {
        BaseMessage baseMessage = messageList.get(position);
        if (baseMessage.getType().equals(com.cometchat.pro.constants.CometChatConstants.MESSAGE_TYPE_IMAGE)) {
            return SHARED_MEDIA_IMAGE;
        } /*else if (baseMessage.getType().equals(com.cometchat.pro.constants.CometChatConstants.MESSAGE_TYPE_VIDEO)) {
            return SHARED_MEDIA_VIDEO;
        } else if (baseMessage.getType().equals(com.cometchat.pro.constants.CometChatConstants.MESSAGE_TYPE_FILE)) {
            return SHARED_MEDIA_FILE;
        }*/

        return -1;

    }

    public void updateMessageList(List<BaseMessage> updateMessageList) {
        if(messageList.size() > 0){
            int sizeBeforeUpdate = messageList.size() - 1;
            messageList.addAll(updateMessageList);
            notifyItemRangeInserted(sizeBeforeUpdate,updateMessageList.size());
        }
    }

    private void setMessageList(List<BaseMessage> messageArrayList) {
        this.messageList.addAll(messageArrayList);
        notifyDataSetChanged();
    }

    public class MultipleViewHolder extends RecyclerView.ViewHolder {

        private ImageView mSingleImageView;

        public MultipleViewHolder(@NonNull View itemView) {
            super(itemView);
            mSingleImageView = itemView.findViewById(R.id.single_image_view);
        }
    }
}
