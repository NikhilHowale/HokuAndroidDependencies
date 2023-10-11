package com.hokuapps.loadnativefileupload.annotate.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.annotate.AnnotateActivity;
import com.hokuapps.loadnativefileupload.annotate.TurboImageView;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    public Bitmap bitmap;
    private final ArrayList<Image> imageList;
    private final Context activity;
    private LruCache<String, Bitmap> mContactImageMemoryCache;

    public ImageAdapter(ArrayList<Image> list, Context activity) {
        this.imageList = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        Image image = imageList.get(position);
        if (image.isLocal()) {
            if (image.isSelected()) {
                holder.card.setCardBackgroundColor(Color.WHITE);
                holder.image.setImageDrawable(imageList.get(position).getNewDrawable());
            } else {
                holder.card.setCardBackgroundColor(Color.GRAY);
                holder.image.setImageDrawable(imageList.get(position).getOldDrawable());
            }
        } else {
            if (image.isSelected()) {
                holder.card.setCardBackgroundColor(Color.WHITE);
                Picasso.with(activity).load(new File(image.getSelectedImageName()))
                        .fit()
                        .centerInside()
                        .into(holder.image, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                            }
                        });
            } else {
                holder.card.setCardBackgroundColor(Color.GRAY);
                Picasso.with(activity).load(new File(image.getImageName()))
                        .fit()
                        .centerInside()
                        .into(holder.image, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                            }
                        });
            }
        }

        holder.image.setOnClickListener(view -> {
            ((AnnotateActivity) activity).imageTitle.setVisibility(View.VISIBLE);
            ((AnnotateActivity) activity).imageTitle.setText(imageList.get(position).getImageTitle());
            for (int i = 0; i < imageList.size(); i++) {
                Image image1 = imageList.get(i);
                if (image1.isLocal()) {
                    image1.setOldDrawable(imageList.get(i).getOldDrawable());
                    image1.setNewDrawable(imageList.get(i).getNewDrawable());
                    image1.setImageTitle(imageList.get(i).getImageTitle());
                    if (i == position) {
                        image1.setSelected(true);
                        if (image1.isDrawLine()) {
                            TurboImageView.isDraw = "Line";
                            TurboImageView.isFreeDraw = true;
                        } else {
                            TurboImageView.isFreeDraw = false;
                        }
                    } else {
                        image1.setSelected(false);
                    }
                    imageList.set(i, image1);
                } else {
                    image1.setImageName(imageList.get(i).getImageName());
                    image1.setSelectedImageName(imageList.get(i).getSelectedImageName());
                    image1.setImageTitle(imageList.get(i).getImageTitle());
                    image1.setSelected(i == position);
                    imageList.set(i, image1);
                }
            }
            notifyDataSetChanged();
        });

        holder.image.setOnLongClickListener(v -> {
            if (!imageList.get(position).isLocal()) {
                Bitmap bmImg = getBitmapFromMemCache(imageList.get(position).getSelectedImageName());
                if (bmImg == null) {
                    try {
                        bmImg = Ion.with(activity)
                                .load(imageList.get(position).getSelectedImageName()).asBitmap().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (bmImg != null) {
                    addBitmapToMemoryCache(imageList.get(position).getSelectedImageName(), bmImg);
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(holder.image);
                    holder.image.startDragAndDrop(data, shadowBuilder, imageList.get(position).getSelectedImageName(), 0);

                    ((AnnotateActivity) activity).getDraggedBitMap(bmImg, imageList.get(position).getImageTitle(), imageList.get(position).getMetaData());
                }
            } else {
                if (!imageList.get(position).isDrawLine()) {
                    TurboImageView.isFreeDraw = false;
                    BitmapDrawable drawable = (BitmapDrawable) imageList.get(position).getNewDrawable();
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(holder.image);
                    holder.image.startDragAndDrop(data, shadowBuilder, imageList.get(position).getNewDrawable(), 0);
                    ((AnnotateActivity) activity).getDraggedBitMap(drawable.getBitmap(), imageList.get(position).getImageTitle(), imageList.get(position).getMetaData());
                }
            }
            return true;
        });
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key == null) return;
        if (getBitmapFromMemCache(key) == null) {
            mContactImageMemoryCache.put(key, bitmap);
        }
    }
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) return null;
        return mContactImageMemoryCache.get(key);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public CardView card;

        public MyViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
            card = view.findViewById(R.id.card);
        }
    }
}
