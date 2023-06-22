/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xinlan.imageeditlibrary.picchooser;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinlan.imageeditlibrary.R;


public class BucketsFragment extends Fragment implements View.OnClickListener {

	private TextView titleGallery;
	private ImageView customBack;
	private int galleryType = 0;
	private String title = "";
	private String toolbarColor = "#FFFFFF";
	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gallery, null);
		loadBundleData();
		initView(v);
		setupTitle(title);
		final List<GridItem> buckets = getGalleryList();
		if (buckets.isEmpty()) {
			Toast.makeText(getActivity(), R.string.no_images,
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		} else {
			GridView grid = (GridView) v.findViewById(R.id.grid);
			grid.setNumColumns(2);
			GalleryAdapter galleryAdapter = new  GalleryAdapter(getActivity(), buckets);
			galleryAdapter.setGalleryType(galleryType);
			grid.setAdapter(galleryAdapter);
			grid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					((SelectPictureActivity) getActivity())
							.showBucket(((BucketItem) buckets.get(position)).id, ((BucketItem) buckets.get(position)).name, galleryType, toolbarColor);
				}
			});
		}
		return v;
	}

	private List<GridItem> getGalleryList() {
		final List<GridItem> buckets = new ArrayList<GridItem>();
		try {
			Cursor cur = null;
			String[] projection = null;
			switch (galleryType) {
				case SelectPictureActivity.VIDEO_GALLERY:
					projection = new String[]{MediaStore.Video.Media.DATA,
							MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
							MediaStore.Video.Media.BUCKET_ID};

					cur = getActivity().getContentResolver().query(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							projection,
							null,
							null,

							MediaStore.Video.Media.DATE_MODIFIED + " DESC, " +
									MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC");
					break;

				case SelectPictureActivity.IMAGE_GALLERY:
				default:

					projection = new String[] { MediaStore.Images.Media.DATA,
							MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
							MediaStore.Images.Media.BUCKET_ID };
					cur = getActivity().getContentResolver().query(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection,
							null,
							null,
							MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, "
									+ MediaStore.Images.Media.DATE_MODIFIED + " DESC");

			}

			BucketItem lastBucket = null;

			if (cur != null) {
				if (cur.moveToFirst()) {
					while (!cur.isAfterLast()) {
						if (lastBucket == null
								||  !lastBucket.name.equals(cur.getString(1)) || lastBucket.id != cur.getInt(2)) {
							lastBucket = new BucketItem(cur.getString(1),
									cur.getString(0), "", cur.getInt(2));
							buckets.add(lastBucket);
						} else {
							lastBucket.images++;
						}

//						lastBucket = new BucketItem(cur.getString(1),
//								cur.getString(0), "", cur.getInt(2));
//						lastBucket.images = cur.getInt(3);
//						buckets.add(lastBucket);
						cur.moveToNext();
					}
				}
				cur.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

//		try {
//			Cursor cur = null;
//			String[] projection = null;
//
//
//					projection = new String[]{MediaStore.Video.Media.DATA,
//							MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
//							MediaStore.Video.Media.BUCKET_ID};
//
//					cur = getActivity().getContentResolver().query(
//							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//							projection,
//							null,
//							null,
//
//							MediaStore.Video.Media.DATE_MODIFIED + " DESC, " +
//									MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC");
//
//
//			BucketItem lastBucket = null;
//
//			if (cur != null) {
//				if (cur.moveToFirst()) {
//					while (!cur.isAfterLast()) {
//						if (lastBucket == null
//								||  !lastBucket.name.equals(cur.getString(1)) || lastBucket.id != cur.getInt(2)) {
//							lastBucket = new BucketItem(cur.getString(1),
//									cur.getString(0), "", cur.getInt(2));
//							buckets.add(lastBucket);
//						} else {
//							lastBucket.images++;
//						}
//
////						lastBucket = new BucketItem(cur.getString(1),
////								cur.getString(0), "", cur.getInt(2));
////						lastBucket.images = cur.getInt(3);
////						buckets.add(lastBucket);
//						cur.moveToNext();
//					}
//				}
//				cur.close();
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}

		return buckets;
	}

	private void loadBundleData() {
		if (getArguments() != null) {
			galleryType = getArguments().containsKey(SelectPictureActivity.EXTRA_GALLERY_TYPE)
					? getArguments().getInt(SelectPictureActivity.EXTRA_GALLERY_TYPE)
					: 0;
			title = getArguments().containsKey(SelectPictureActivity.EXTRA_TITLE)
					&& !TextUtils.isEmpty(getArguments().getString(SelectPictureActivity.EXTRA_TITLE))
					? /*String.format(getResources().getString(R.string.lbl_send_to),
							getArguments().getString(SelectPictureActivity.EXTRA_TITLE))*/
					getArguments().getString(SelectPictureActivity.EXTRA_TITLE)
					: getResources().getString(R.string.lbl_select_images);

			toolbarColor = getArguments().containsKey(SelectPictureActivity.EXTRA_TOOLBAR_COLOR)
					? getArguments().getString(SelectPictureActivity.EXTRA_TOOLBAR_COLOR)
					: "#FFFFFF";
		}
	}

	public void initView(View view) {
		titleGallery = (TextView) view.findViewById(R.id.titleGallery);
		customBack = (ImageView) view.findViewById(R.id.customBack);
		if (!TextUtils.isEmpty(toolbarColor) && !"#FFFFFF".equalsIgnoreCase(toolbarColor)) {
			try {
				view.findViewById(R.id.custom_toolbar).setBackgroundColor(Color.parseColor(toolbarColor));
			} catch (Exception ex) {}
		}
		customBack.setOnClickListener(this);
	}

	private void setupTitle(String title) {
			if (titleGallery != null) {
				titleGallery.setText(title);
			}

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.customBack) {
			getActivity().onBackPressed();
		}
	}

}
