package com.hokuapps.loadmapviewbyconfig.widgets.bottomsheetshare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;


import com.hokuapps.loadmapviewbyconfig.R;

import java.util.List;

/**
 * Adapter for share.
 */
public class AppAdapter extends BaseAdapter {
    List<AppInfo> mApps;

    private final LayoutInflater mInflater;

    private final int mTextColor;

    private final int mLayoutResource;

    public AppAdapter(Context context, List<AppInfo> apps) {
        mApps = apps;
        mInflater = LayoutInflater.from(context);
        mTextColor = ContextCompat.getColor(context, R.color.black);
        mLayoutResource = R.layout.item_share;
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfo appInfo = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder(convertView);
            holder.title.setTextColor(mTextColor);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setImageDrawable(appInfo.drawable);
        holder.title.setText(appInfo.title);
        return convertView;
    }

    public static class AppInfo {
        public String title;

        public String packageName;

        public String name;

        public Drawable drawable;

        public AppInfo(String title, String packageName, String name, Drawable drawable) {
            this.title = title;
            this.packageName = packageName;
            this.name = name;
            this.drawable = drawable;
        }
    }

    public static class ViewHolder {
        public TextView title;
        public ImageView icon;

        public ViewHolder(View view) {
            title = view.findViewById(R.id.title);
            icon = view.findViewById(R.id.icon);
            view.setTag(this);
        }
    }

    /**
     * Interface to for callback.
     */
    public interface AppSelectedListener {
        void onAppSelected(AppInfo appInfo);
    }

}