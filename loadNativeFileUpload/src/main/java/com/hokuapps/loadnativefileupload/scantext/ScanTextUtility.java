package com.hokuapps.loadnativefileupload.scantext;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScanTextUtility {

    public static final int SCAN_IMAGE_REQUEST_GALLERY = 9031;
    public static final int SCAN_IMAGE_REQUEST_CAMERA = 9032;
    private static Activity activity;
    private static ScanTextUtility scanTextUtility;
    private static ImageScanListener imageScanListener;
    private Uri imageUri;

    public static ImageScanListener getImageScanListener() {
        return imageScanListener;
    }

    public static ScanTextUtility setImageScanListener(ImageScanListener imageScanListener) {
        ScanTextUtility.imageScanListener = imageScanListener;
        return scanTextUtility;
    }

    public ScanTextUtility(Activity activity) {
        this.activity = activity;

    }

    public ScanTextUtility showPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select");
        builder.setItems(new CharSequence[]
                        {"Camera", "Gallery",},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                launchCamera();
                                break;
                            case 1:
                                launchGallery();
                                break;
                        }
                    }
                });
        builder.create().show();
        return scanTextUtility;
    }

    private void launchCamera() {
        String filename = System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, SCAN_IMAGE_REQUEST_CAMERA);
    }

    /**
     * Launch gallery to pick image.
     */
    private void launchGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, SCAN_IMAGE_REQUEST_GALLERY);
    }

    public static ScanTextUtility getInstance(Activity activity) {
//        ScanTextUtility.imageScanListener = imageScanListener;
        ScanTextUtility.activity = activity;
        if (scanTextUtility == null) {
            return scanTextUtility = new ScanTextUtility(activity);
        }
        return scanTextUtility;
    }

    public interface ImageScanListener {
        void onImageScan(String string, JSONArray jsonArrayScannedText);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case SCAN_IMAGE_REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    inspect(intent.getData());
                }
                break;
            case SCAN_IMAGE_REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        inspect(imageUri);
                    }
                }
                break;
        }
    }

    private void inspect(Uri uri) {
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = activity.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inScreenDensity = DisplayMetrics.DENSITY_LOW;
            bitmap = BitmapFactory.decodeStream(is, null, options);
            inspectFromBitmap(bitmap);
        } catch (FileNotFoundException e) {
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void inspectFromBitmap(Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(activity).build();
        try {
            if (!textRecognizer.isOperational()) {
                new AlertDialog.
                        Builder(activity).
                        setMessage("Text recognizer could not be set up on your device").show();
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> origTextBlocks = textRecognizer.detect(frame);
            List<TextBlock> textBlocks = new ArrayList<>();
            for (int i = 0; i < origTextBlocks.size(); i++) {
                TextBlock textBlock = origTextBlocks.valueAt(i);
                textBlocks.add(textBlock);
            }
            Collections.sort(textBlocks, new Comparator<TextBlock>() {
                @Override
                public int compare(TextBlock o1, TextBlock o2) {
                    int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                    int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                    if (diffOfTops != 0) {
                        return diffOfTops;
                    }
                    return diffOfLefts;
                }
            });

            StringBuilder detectedText = new StringBuilder();
            JSONArray jsonArrayScannedText = new JSONArray();
            for (TextBlock textBlock : textBlocks) {
                if (textBlock != null && textBlock.getValue() != null) {
                    System.out.println("SCANNED DATA :" + textBlock.getValue());
                    System.out.println("\n");
                    detectedText.append(textBlock.getValue());
                    jsonArrayScannedText.put(textBlock.getValue());
                    detectedText.append("\n");

                }
            }
            System.out.println("ALL SORT DATA :" + detectedText.toString());
            if (imageScanListener != null) {
                imageScanListener.onImageScan(detectedText.toString(),jsonArrayScannedText);
            }


        } finally {
            textRecognizer.release();
        }
    }
}
