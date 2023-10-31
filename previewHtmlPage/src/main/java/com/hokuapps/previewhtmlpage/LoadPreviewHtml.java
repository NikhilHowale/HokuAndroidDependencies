package com.hokuapps.previewhtmlpage;

import android.Manifest;
import android.content.Intent;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONObject;

import java.util.List;

public class LoadPreviewHtml {
    private static LoadPreviewHtml instance;

    public static LoadPreviewHtml getInstance(){
        if(instance == null){
            instance = new LoadPreviewHtml();
        }
        return instance;
    }

    /**
     * This method call activity and display preview of web page
     * @param activity activity
     * @param response json in string format
     */
    public void showPreview(AppCompatActivity activity, String response) {
        try {
            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    try {
                        JSONObject targetDataJsonObj = new JSONObject(response);
                        String htmlPageData = Utility.getStringObjectValue(targetDataJsonObj, "htmlPageData");
                        Intent intent = new Intent(activity, PreviewHtmlActivity.class);
                        intent.putExtra("Content", htmlPageData);
                        activity.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.READ_MEDIA_AUDIO
                        )
                        .check();
            } else {
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
