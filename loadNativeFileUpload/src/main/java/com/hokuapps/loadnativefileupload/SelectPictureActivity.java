package com.hokuapps.loadnativefileupload;

import static android.app.Activity.RESULT_CANCELED;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.picchooser.BucketsFragment;
import com.xinlan.imageeditlibrary.picchooser.ImagesFragment;


public class SelectPictureActivity extends BaseActivity {
    public static final int SELECT_GALLERY_IMAGE_CODE = 7000;
    public static final int SELECT_GALLERY_VIDEO_CODE = 7001;

    public static final int IMAGE_GALLERY = 0;
    public static final int VIDEO_GALLERY = 1;
    public static final String EXTRA_GALLERY_TYPE = "extra_gallery_type";
    public static final String EXTRA_TITLE= "extra_title";
    public static final String EXTRA_TOOLBAR_COLOR= "extra_toolbar_color";
    @Override
    protected void onCreate(final Bundle b) {
        super.onCreate(b);

        checkInitImageLoader();
        setResult(RESULT_CANCELED);

        setStatusBarColor();
        // Create new fragment and transaction
        Fragment newFragment = new BucketsFragment();
        newFragment.setArguments(getIntent().getExtras());
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack
        transaction.replace(android.R.id.content, newFragment);

        // Commit the transaction
        transaction.commit();
    }

    private void setStatusBarColor() {
        try {
            String color = getIntent().getExtras().containsKey(SelectPictureActivity.EXTRA_TOOLBAR_COLOR)
                    ? getIntent().getExtras().getString(SelectPictureActivity.EXTRA_TOOLBAR_COLOR)
                    : "#FFFFFF";
            if (!TextUtils.isEmpty(color) && !"#FFFFFF".equalsIgnoreCase(color)) {
                setStatusBarColor(BitmapUtils.changeColorToPrimaryHSB(color));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void showBucket(final int bucketId, String title, int galleryType, String toolbarColor) {
        Bundle b = new Bundle();
        b.putInt("bucket", bucketId);
        b.putString(EXTRA_TITLE, title);
        b.putInt(EXTRA_GALLERY_TYPE, galleryType);
        b.putString(EXTRA_TOOLBAR_COLOR, toolbarColor);
        Fragment f = new ImagesFragment();
        f.setArguments(b);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, f).addToBackStack(null).commit();
    }

    void imageSelected(final String imgPath, final String imgTaken, final long imageSize) {
        returnResult(imgPath, imgTaken, imageSize);
    }

    private void returnResult(final String imgPath, final String imageTaken, final long imageSize) {
        Intent result = new Intent();
        result.putExtra("imgPath", imgPath);
        result.putExtra("dateTaken", imageTaken);
        result.putExtra("imageSize", imageSize);
        setResult(RESULT_OK, result);
        finish();
    }

    public static void startActivityForResultImage(Fragment fragment, int type, String title, String toolbarColor, int requestCode) {
        Intent intent = new Intent(
                fragment.getActivity(), SelectPictureActivity.class);
        intent.putExtra(EXTRA_GALLERY_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        fragment.startActivityForResult(intent,
                requestCode);
    }

    public static void startActivityForResultImage(Activity activity, int type, String title, String toolbarColor, int requestCode) {
        Intent intent = new Intent(
                activity, SelectPictureActivity.class);
        intent.putExtra(EXTRA_GALLERY_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        activity.startActivityForResult(intent,
                requestCode);
    }

    public static void startActivityForResultVideo(Fragment fragment, int type, String title, String toolbarColor, int requestCode) {
        Intent intent = new Intent(
                fragment.getActivity(), SelectPictureActivity.class);
        intent.putExtra(EXTRA_GALLERY_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        fragment.startActivityForResult(intent,
                requestCode);
    }

    public static void startActivityForResultVideo(Activity activity, int type, String title, String toolbarColor, int requestCode) {
        Intent intent = new Intent(
                activity, SelectPictureActivity.class);
        intent.putExtra(EXTRA_GALLERY_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        activity.startActivityForResult(intent,
                requestCode);
    }

    public static void startActivityForResultVideo(android.app.Fragment fragment, int type, String title, String toolbarColor) {
        Intent intent = new Intent(
                fragment.getActivity(), SelectPictureActivity.class);
        intent.putExtra(EXTRA_GALLERY_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);
        fragment.startActivityForResult(intent,
                SELECT_GALLERY_VIDEO_CODE);
    }

    public static void startActivityForResultImage(android.app.Fragment fragment, int type, String title, String toolbarColor) {
        Intent intent = new Intent(
                fragment.getActivity(), SelectPictureActivity.class);
        intent.putExtra(EXTRA_GALLERY_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TOOLBAR_COLOR, toolbarColor);

        fragment.startActivityForResult(intent,
                SELECT_GALLERY_IMAGE_CODE);
    }
}
