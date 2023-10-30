package com.hokuapps.shownativecarousel;


import static com.hokuapps.shownativecarousel.constants.CarouselConstant.*;
import static com.hokuapps.shownativecarousel.utility.Utility.getResString;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.shownativecarousel.adapter.ImageSliderAdapter;
import com.hokuapps.shownativecarousel.backgroundtask.DownloadFile;
import com.hokuapps.shownativecarousel.pref.CarouselPref;
import com.hokuapps.shownativecarousel.utility.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class ImageSliderActivity extends AppCompatActivity {
    private JSONArray imageSlidingJsonArr;
    private TextView headerTitle;
    private int index = 0;
    private boolean isShowDownload = false;
    private ProgressDialog downloadProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slider);
        loadBundleData();
        initHeaderLayout();
        setupViewPager();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    /**
     * load the data into variables got from bundle
     */
    private void loadBundleData() {
        Bundle bundle = getIntent().getExtras();
        imageSlidingJsonArr = new JSONArray();
        try {
            if (bundle != null) {
                String imageSlidingArrStr = new CarouselPref(ImageSliderActivity.this).getValue(KeyConstants.IMAGE_LIST);
                index = bundle.containsKey(KeyConstants.INDEX) ? bundle.getInt(KeyConstants.INDEX, 0) : 0;
                isShowDownload = bundle.containsKey(KeyConstants.IS_SHOW_DOWNLOAD) && bundle.getBoolean(KeyConstants.IS_SHOW_DOWNLOAD, false);

                if (!TextUtils.isEmpty(imageSlidingArrStr)) {
                    imageSlidingJsonArr = new JSONArray(imageSlidingArrStr);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * initialize the views(header title,close button,download button)
     * set click listener on download and close button
     */
    private void initHeaderLayout() {
        try {
            headerTitle = findViewById(R.id.headerTitle);
            setHeaderTitle(0);
            ImageView btnClose = findViewById(R.id.btn_close);
            ImageView btnDownload = findViewById(R.id.btn_download);
            if (isShowDownload) {
                btnDownload.setVisibility(View.VISIBLE);
            } else {
                btnDownload.setVisibility(View.GONE);
            }

            btnClose.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_clear_white_24dp));

            btnClose.setOnClickListener(view -> ImageSliderActivity.this.finish());

            btnDownload.setOnClickListener(view -> {
                try {
                    if (imageSlidingJsonArr != null && imageSlidingJsonArr.length() > index) {
                        JSONObject jsonObject = imageSlidingJsonArr.getJSONObject(index);
                        String downloadUrl = Utility.getStringObjectValue(jsonObject, KeyConstants.S_3_FILE_PATH);
                        String fileName = Utility.getStringObjectValue(jsonObject, KeyConstants.FILE_NAME);
                        downloadFileIfRequired(downloadUrl, fileName);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * get the title string from jason object using the given index position
     * set the title to header
     * @param position index position of the object
     */
    private void setHeaderTitle(int position) {
        try {
            if (imageSlidingJsonArr != null && imageSlidingJsonArr.length() > position) {
                JSONObject jsonObject = imageSlidingJsonArr.getJSONObject(position);
                String caption = Utility.getStringObjectValue(jsonObject, KeyConstants.CAPTION);
                headerTitle.setText(caption);
                Utility.changedToolbarTextColorByTheme(headerTitle);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set up the view pager
     * set adapter to view pager
     */
    public void setupViewPager() {
        ViewPager imageSlidingViewPager = (ViewPager) findViewById(R.id.imageSlidingViewPager);
        ImageSliderAdapter adapterSlider = new ImageSliderAdapter(this, imageSlidingJsonArr);
        imageSlidingViewPager.setAdapter(adapterSlider);

        imageSlidingViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                index = position;
                setHeaderTitle(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        imageSlidingViewPager.setOffscreenPageLimit(1);
        imageSlidingViewPager.setCurrentItem(index);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_from_back_in, R.anim.anim_from_back_out);
    }


    /**
     * start activity from calling activity
     * @param activity calling activity reference
     * @param jsonArray object containing details
     * @param index index position
     * @param isShowDownload boolean value suggesting to download or not the file
     */
    public static void startActivity(Activity activity, JSONArray jsonArray, int index, boolean isShowDownload) {

        Intent intent = new Intent(activity, ImageSliderActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(KeyConstants.IMAGE_SLIDING_ARR, jsonArray == null ? new JSONArray().toString() : jsonArray.toString());
        intent.putExtra(KeyConstants.INDEX, index);
        intent.putExtra(KeyConstants.IS_SHOW_DOWNLOAD, isShowDownload);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.anim_from_right, R.anim.anim_from_left);
    }


    /**
     * download the image file in local storage
     * @param mUrl url to download file
     * @param fileName name of the file
     */
    private void downloadFileIfRequired(final String mUrl, final String fileName) {


        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                final DownloadFile downloadFile = new DownloadFile();
                downloadFile.setDownloadFolderPath(Utility.getDownloadFileParentDir(KeyConstants.IMAGES, false, ImageSliderActivity.this));
                downloadFile.setOriginalFileName(fileName);
                downloadFile.setDownloadUrl(mUrl);
                downloadFile.setForceDownload(false);
                downloadFile.setDownloadCallback(new DownloadFile.DownloadCallback() {
                    @Override
                    public void onDownloadStatus(boolean isDownloaded, String filePath) {
                        //dismissProgressBar
                        if (downloadProgressDialog != null)
                            downloadProgressDialog.dismiss();
                        if (isDownloaded) {
                            Utility.showMessage(ImageSliderActivity.this, R.string.lbl_download_complete);
                        } else {
                            Utility.showMessage(ImageSliderActivity.this, R.string.lbl_download_failed);
                        }

                    }

                    @Override
                    public void onDownloadProgressUpdate(String status) {
                        try {
                            downloadProgressDialog.setProgress(Integer.parseInt(status));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onDownloadStarted(String filePath) {
                        //show progress bar
                        downloadProgressDialog = new ProgressDialog(ImageSliderActivity.this);
                        downloadProgressDialog.setMessage(getResString(R.string.lbl_donwloading_file, ImageSliderActivity.this));
                        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        downloadProgressDialog.setIndeterminate(false);

                        downloadProgressDialog.setOnDismissListener(dialog -> {
                            try {
                                downloadFile.cancel(true);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        downloadProgressDialog.show();
                    }
                });
                downloadFile.execute();
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
                            Manifest.permission.READ_MEDIA_VIDEO

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

    }
}
