package com.hokuapps.loadmapviewbyconfig.widgets;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;
import com.hokuapps.loadmapviewbyconfig.widgets.bottomsheetshare.AppAdapter;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetShare extends BottomSheetDialogFragment {
    private ListView listViewShare;
    private Context context;
    private String title, messageText;
    private boolean isNavigation = false;
    private AppAdapter.AppSelectedListener appSelectedListener;

    public void setAppSelectedListener(AppAdapter.AppSelectedListener appSelectedListener) {
        this.appSelectedListener = appSelectedListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_share, container,false);
        listViewShare = (ListView) view.findViewById(R.id.listView_share);
        context = getActivity();
        Intent intent = null;
        ArrayList<String> setInclude = new ArrayList<String>();

        List<AppAdapter.AppInfo> list;

//        Intent of share.
        if (isNavigation) {
            intent = new Intent(Intent.ACTION_VIEW);

            setInclude.add("com.google.android.apps.maps"); // Google
            setInclude.add("com.waze"); // Waze
        } else {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, Utility.getValidString(messageText));
            intent.putExtra(Intent.EXTRA_HTML_TEXT, Utility.fromHtml(messageText));

//        Array list app package name to be include.
            setInclude.add("com.google.android.gm"); // Gmail
            setInclude.add("com.google.android.apps.messaging"); // Defualt messaging
            setInclude.add("com.whatsapp"); // WhatsApp
            setInclude.add("com.facebook.katana"); // Facebook
            setInclude.add("com.instagram.android"); // Instagram
            setInclude.add("com.facebook.orca"); // Facebook messenger

        }

        list = getListOfActivitiesCanBeShared(intent, setInclude, null);
        if (list == null || list.isEmpty()) {
            Utility.showMessage(context, "No apps available to share");
            dismiss();

        }

//        Add data to listView.
        final AppAdapter appAdapter = new AppAdapter(context, list);
        listViewShare.setAdapter(appAdapter);

//        Set item click listener.
        listViewShare.setOnItemClickListener((parent, view1, position, id) -> {
            if (appSelectedListener != null) {
                appSelectedListener.onAppSelected(appAdapter.getItem(position));
            }
            dismiss();
        });

        return view;
    }

    /**
     * Get shareable app list.
     *
     * @param intent intent to open share dialog
     * @param appsFilter app list to filter install app
     * @param toExclude exclude app from list
     * @return return intent with filter app for share
     */
    public List<AppAdapter.AppInfo> getListOfActivitiesCanBeShared(Intent intent, ArrayList<String> appsFilter, @Nullable ArrayList<String> toExclude) {
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> apps = manager.queryIntentActivities(intent, 0);
        List<AppAdapter.AppInfo> appResources = new ArrayList<>(apps.size());

        if (!apps.isEmpty()) {
            boolean shouldCheckPackages = appsFilter != null && !appsFilter.isEmpty();

            for (ResolveInfo resolveInfo : apps) {
                String packageName = resolveInfo.activityInfo.packageName;

                if (shouldCheckPackages && !appsFilter.contains(packageName)) {
                    continue;
                }

                String title = resolveInfo.loadLabel(manager).toString();
                String name = resolveInfo.activityInfo.name;
                Drawable drawable = resolveInfo.loadIcon(manager);
                appResources.add(new AppAdapter.AppInfo(title, packageName, name, drawable));
            }

            if (toExclude != null && !toExclude.isEmpty()) {
                List<AppAdapter.AppInfo> toRemove = new ArrayList<>();

                for (AppAdapter.AppInfo appInfo : appResources) {
                    if (toExclude.contains(appInfo.packageName)) {
                        toRemove.add(appInfo);
                    }
                }

                if (!toRemove.isEmpty()) appResources.removeAll(toRemove);
            }

        }
        return appResources;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set's whether the user should see the navigation apps instead of sharing apps in
     * bottom sheet list.
     *
     * @param navigation <b>true</b> to show navigation apps, <b>false</b> otherwise.
     */
    public void setNavigation(boolean navigation) {
        isNavigation = navigation;
    }
}
