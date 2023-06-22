package com.xinlan.imageeditlibrary.picchooser;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import com.xinlan.imageeditlibrary.R;


class GalleryAdapter extends BaseAdapter {

    private final Context context;
    private final List<GridItem> items;
    private final LayoutInflater mInflater;
    private int galleryType = SelectPictureActivity.IMAGE_GALLERY;
    private static final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .cacheInMemory(true)
            .cacheOnDisk(false)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public GalleryAdapter(final Context context, final List<GridItem> buckets) {
        this.items = buckets;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public int getGalleryType() {
        return galleryType;
    }

    public void setGalleryType(int galleryType) {
        this.galleryType = galleryType;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (items.get(0) instanceof BucketItem) { // show buckets
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.bucketitem, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BucketItem bi = (BucketItem) items.get(position);
            holder.text.setText(bi.images > 1 ?
                    bi.name + " - " + context.getString(R.string.images, bi.images) :
                    bi.name);
            try {
                Glide.with(context)
                        .load(Uri.fromFile(new File(items.get(position).path)))
                        .thumbnail(0.1f)
                        .into(holder.icon);
            }catch (Exception e){
                e.printStackTrace();
            }

           /* Picasso.with(context)
                    .load(new File(items.get(position).path))
                    .centerCrop()
                    .into(holder.icon);*/

            //ImageLoader.getInstance().displayImage("file://" + bi.path, holder.icon);
            return convertView;
        } else { // show images in a bucket
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = (ImageView) mInflater.inflate(R.layout.imageitem, null);
            } else {
                imageView = (ImageView) convertView;
            }

            Glide.with(context)
                    .load(Uri.fromFile(new File(items.get(position).path)))
                    .thumbnail(0.1f)
                    .into(imageView);

            /*Picasso.with(context)
                    .load(new File(items.get(position).path))
                    .centerCrop()

                    .into(imageView);*/

            //ImageLoader.getInstance().displayImage("file://" + items.get(position).path, imageView, options);
            return imageView;
        }
    }

    private static class ViewHolder {
        private ImageView icon;
        private TextView text;
    }

}
