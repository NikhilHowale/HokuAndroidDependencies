package com.hokuapps.loadnativefileupload.annotate.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.annotate.FreeDrawingActivity;
import com.hokuapps.loadnativefileupload.annotate.TurboImageViewFree;

import java.util.ArrayList;

public class PencilAdapter extends RecyclerView.Adapter<PencilAdapter.MyViewHolder> {

    public Bitmap bitmap;
    private final ArrayList<Pencil> imageList;
    private final Context activity;

    public PencilAdapter(ArrayList<Pencil> list, Context activity) {
        this.imageList = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pencil_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        Pencil image = imageList.get(position);
        if (image.isSelected()) {
            holder.image.setImageDrawable(imageList.get(position).getDrawable());
        } else {
            holder.image.setImageDrawable(imageList.get(position).getDrawable());
        }

        holder.image.setOnClickListener(view -> {
            ((FreeDrawingActivity) activity).imageTitle.setVisibility(View.GONE);
            ((FreeDrawingActivity) activity).imageTitle.setText(imageList.get(position).getImageTitle());
            TurboImageViewFree.colorCode = imageList.get(position).getColorCode();
        });

    }

    @Override
    public int getItemCount() {
        return imageList.size();
        // return 10;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;

        public MyViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
        }
    }
}
