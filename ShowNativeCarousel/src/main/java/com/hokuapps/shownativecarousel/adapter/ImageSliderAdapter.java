package com.hokuapps.shownativecarousel.adapter;

import static com.hokuapps.shownativecarousel.constants.CarouselConstant.*;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.hokuapps.shownativecarousel.R;
import com.hokuapps.shownativecarousel.editimage.ImageViewTouch;
import com.hokuapps.shownativecarousel.progresswheel.ProgressWheel;
import com.hokuapps.shownativecarousel.utility.Utility;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.Objects;



public class ImageSliderAdapter extends PagerAdapter {
    // Declare Variables
    private final Context mContext;
    private final JSONArray imageSliderJsonArr;


    /**
     * constructor
     * @param mContext context
     * @param imageSliderJsonArr array of images
     */
    public ImageSliderAdapter(Context mContext, JSONArray imageSliderJsonArr) {
        this.mContext = mContext;
        this.imageSliderJsonArr = imageSliderJsonArr;
    }

    /**
     * Get the item counts from array
     */
    @Override
    public int getCount() {
        return imageSliderJsonArr != null ? imageSliderJsonArr.length() : 0;
    }


    /**
     *
     * @param view Page View to check for association with <code>object</code>
     * @param object Object to check for association with <code>view</code>
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    /**
     *
     * @param container The containing View in which the page will be shown.
     * @param position The page position to be instantiated.
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View itemView = null;
        try {

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.image_slider_item_layout, container,
                    false);

            JSONObject jsonObj = imageSliderJsonArr.getJSONObject(position);

            String fileUrl = Utility.getStringObjectValue(jsonObj, KeyConstants.S_3_FILE_PATH);
            String fileNm = Utility.getStringObjectValue(jsonObj, KeyConstants.FILE_NAME);
            String imageData = Utility.getStringObjectValue(jsonObj, KeyConstants.IMAGE_DATA);

            if (!TextUtils.isEmpty(imageData)) {
                initImagePreviewByBase64(itemView, imageData);
            } else {
                assert fileNm != null;
                initImagePreviewLayout(itemView, TextUtils.isEmpty(fileUrl)
                        ? fileNm.contains(Objects.requireNonNull(Utility.getHtmlDirFromSandbox(mContext)).getAbsolutePath()) ?
                        fileNm : Objects.requireNonNull(Utility.getHtmlDirFromSandbox(mContext)).getAbsolutePath() +
                        File.separator + fileNm
                        : fileUrl, TextUtils.isEmpty(fileUrl));
            }
            container.addView(itemView);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        assert itemView != null;
        return itemView;
    }


    /**
     * set image from bitmap to imageview
     * @param itemView view reference
     * @param imageData image details
     */
    private void initImagePreviewByBase64(View itemView, String imageData) {
        final ImageViewTouch imagePreview;
        imagePreview = itemView.findViewById(R.id.imagePreview);
        final ProgressWheel progressWheel = itemView.findViewById(R.id.loading_wheel);
        progressWheel.setVisibility(View.GONE);
        imagePreview.setDoubleTapEnabled(true);
        imagePreview.setScaleEnabled(true);
        imagePreview.setScrollEnabled(true);
        imagePreview.setDisplayType(ImageViewTouch.DisplayType.FIT_TO_SCREEN);
        imagePreview.setImageBitmap(Utility.getBitmap(imageData));
    }


    /**
     * initialize imageview and progress bar
     * @param itemView view reference
     * @param fileUrl image url
     * @param isFromLocal boolean value for loading image from local
     */
    private void initImagePreviewLayout(View itemView, String fileUrl, boolean isFromLocal) {
        final ImageViewTouch imagePreview;
        imagePreview = itemView.findViewById(R.id.imagePreview);
        final ProgressWheel progressWheel = itemView.findViewById(R.id.loading_wheel);
        imagePreview.setDoubleTapEnabled(true);
        imagePreview.setScaleEnabled(true);
        imagePreview.setScrollEnabled(true);
        imagePreview.setDisplayType(ImageViewTouch.DisplayType.FIT_TO_SCREEN);

        if (isFromLocal) {
            progressWheel.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(new File(fileUrl))
                    .fit()
                    .centerInside()
                    .into(imagePreview, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressWheel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressWheel.setVisibility(View.GONE);
                        }
                    });

        } else {
            progressWheel.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(fileUrl)
                    .fit()
                    .centerInside()
                    .into(imagePreview, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressWheel.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressWheel.setVisibility(View.GONE);
                        }
                    });
        }
    }


    /**
     *
     * @param container The containing View from which the page will be removed.
     * @param position The page position to be removed.
     * @param object The same object that was returned by
     * {@link #instantiateItem(View, int)}.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((FrameLayout) object);
    }

}