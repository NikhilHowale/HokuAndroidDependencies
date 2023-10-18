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
package com.hoku.imageeditlibrary.picchooser;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.hoku.imageeditlibrary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BucketsFragment extends Fragment implements View.OnClickListener {

	private TextView titleGallery;
	private ImageView customBack;
	private int galleryType = 0;
	private String title = "";
	private String toolbarColor = "#FFFFFF";
	private static Map<Integer,ArrayList<FileData>> mFoldersListMap;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gallery, null);
		loadBundleData();
		initView(v);
		setupTitle(title);
		mFoldersListMap = new HashMap<>();
		final List<GridItem> buckets = getGalleryList(galleryType);


		if (buckets.isEmpty()) {
			Toast.makeText(getActivity(), R.string.no_images,Toast.LENGTH_SHORT).show();
			requireActivity().finish();
		} else {
			GridView grid = (GridView) v.findViewById(R.id.grid);
			grid.setNumColumns(2);
			final GalleryAdapter galleryAdapter = new  GalleryAdapter(getActivity(), buckets);
			galleryAdapter.setGalleryType(galleryType);
			grid.setAdapter(galleryAdapter);
			grid.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					((SelectPictureActivity) requireActivity())
							.showBucket(((BucketItem) buckets.get(position)).id, ((BucketItem) buckets.get(position)).name, galleryType, toolbarColor);

				}
			});
		}
		return v;
	}

	private List<GridItem> getGalleryList(int type) {
		List<GridItem> buckets = new ArrayList<GridItem>();
		try {
			BucketItem lastBucket = null;
			Cursor cur = null;
			String[] projection = null;

			switch (type) {
				case SelectPictureActivity.VIDEO_GALLERY: {

					projection = new String[]{MediaStore.Video.Media.DATA,
							MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
							MediaStore.Video.Media.BUCKET_ID};

					cur = requireActivity().getContentResolver().query(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							projection,
							null,
							null,

							MediaStore.Video.Media.DATE_MODIFIED + " DESC, " +
									MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC");
					if (cur != null) {
						if (cur.moveToFirst()) {
							while (!cur.isAfterLast()) {
									if (lastBucket == null || !lastBucket.name.equals(cur.getString(1)) || lastBucket.id != cur.getInt(2)) {
										lastBucket = new BucketItem(cur.getString(1), cur.getString(0), "", cur.getInt(2));
										buckets.add(lastBucket);
									} else {
										lastBucket.images++;
									}
								cur.moveToNext();
							}
						}
						cur.close();
						lastBucket = null;
					}
				}
				break;
				case SelectPictureActivity.MEDIA_GALLERY: {
					projection = new String[]{
							MediaStore.Video.Media.DATA,
							MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
							MediaStore.Video.Media.BUCKET_ID,
							MediaStore.Video.Media.MIME_TYPE,
					};

					cur = requireActivity().getContentResolver().query(
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							projection,
							null,
							null,

							MediaStore.Video.Media.DATE_MODIFIED + " DESC, " +
									MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC");
					if (cur != null) {
						if (cur.moveToFirst()) {
							while (!cur.isAfterLast()) {
							//	Log.e("PATH", "getGalleryList: " + cur.getString(0).replace("/"+cur.getString(0),"") );
								if (lastBucket == null	||  !lastBucket.name.equals(cur.getString(1)) || lastBucket.id != cur.getInt(2)) {
									lastBucket = new BucketItem(cur.getString(1),	cur.getString(0), "", cur.getInt(2));
									buckets.add(lastBucket);

								} else {
									lastBucket.images++;
								}
								cur.moveToNext();
							}
						}
						cur.close();
					}

					Cursor cur2 = null;
					String[] projection2 = null;

					projection2 = new String[]{MediaStore.Images.Media.DATA,
							MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
							MediaStore.Images.Media.BUCKET_ID,
							MediaStore.Images.Media.MIME_TYPE
					};

					cur2 = requireActivity().getContentResolver().query(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection2,
							null,
							null,
							MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, "
									+ MediaStore.Images.Media.DATE_MODIFIED + " DESC");

					if (cur2 != null) {
						if (cur2.moveToFirst()) {
							while (!cur2.isAfterLast()) {
								//Log.e("PATH", "getGalleryList: " + cur2.getString(0).replace("/"+cur2.getString(0),"") );
								if (lastBucket == null 	||  !lastBucket.name.equals(cur2.getString(1)) || lastBucket.id != cur2.getInt(2)) {
									lastBucket = new BucketItem(cur2.getString(1),cur2.getString(0), "", cur2.getInt(2));
									buckets.add(lastBucket);
								} else {
									lastBucket.images++;
								}
								cur2.moveToNext();
							}
						}
						cur2.close();
					}
					//buckets = getAllMediaFilesOnDevice(requireContext());
				}
				break;

				case SelectPictureActivity.IMAGE_GALLERY:
				default: {

					projection = new String[]{MediaStore.Images.Media.DATA,
							MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
							MediaStore.Images.Media.BUCKET_ID};

					cur = requireActivity().getContentResolver().query(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection,
							null,
							null,
							MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC, "
									+ MediaStore.Images.Media.DATE_MODIFIED + " DESC");

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
								cur.moveToNext();
							}
						}
						cur.close();
					}
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}



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
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		customBack.setOnClickListener(this);
	}

	private void setupTitle(String title) {
			if (titleGallery != null) {
				titleGallery.setText(title);

				if(galleryType == SelectPictureActivity.MEDIA_GALLERY)
					titleGallery.setTextColor(ContextCompat.getColor(requireContext(),R.color.materialcolorpicker__black));
			}

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.customBack) {
			requireActivity().onBackPressed();
		}
	}

	static class FileData{
		private int id;
		private String filepath;
		private String mimeType;
		private String bucketName;

		public FileData(int id, String filepath, String mimeType, String bucketName) {
			this.id = id;
			this.filepath = filepath;
			this.mimeType = mimeType;
			this.bucketName = bucketName;
		}

		public FileData(int id, String filepath, String mimeType) {
			this.id = id;
			this.filepath = filepath;
			this.mimeType = mimeType;
		}

		public FileData(String filepath, String mimeType) {
			this.filepath = filepath;
			this.mimeType = mimeType;
		}
	}

	public static List<GridItem> getAllMediaFilesOnDevice(Context context) {
		final List<GridItem> buckets = new ArrayList<GridItem>();
		final List<FileData> fileData = new ArrayList<>();
		try {

			final String[] columns = { MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.DATE_ADDED,
					MediaStore.Images.Media.BUCKET_ID,
					MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Images.Media.MIME_TYPE };

			final String[] videoColumn = { MediaStore.Video.Media.DATA,
					MediaStore.Video.Media.DATE_ADDED,
					MediaStore.Video.Media.BUCKET_ID,
					MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
					MediaStore.Video.Media.MIME_TYPE };

			String sortOrderImages = MediaStore.Images.Media.DATE_MODIFIED + " DESC, " +
					MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";

			String sortOrderVideo = MediaStore.Video.Media.DATE_MODIFIED + " DESC, " +
					MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " ASC";

			MergeCursor cursor = new MergeCursor(new Cursor[]{
					context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, sortOrderImages),
					context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoColumn, null, null, sortOrderVideo),
					context.getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null, null, sortOrderImages),
					context.getContentResolver().query(MediaStore.Video.Media.INTERNAL_CONTENT_URI, videoColumn, null, null, sortOrderVideo)
			});
			cursor.moveToFirst();

			while (!cursor.isAfterLast()){
				String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				String mime_type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
				int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
				String bucketName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

				int lastPoint = path.lastIndexOf(".");

				path = path.substring(0, lastPoint) + path.substring(lastPoint).toLowerCase();
				//fileData.add(new FileData(id,path,mime_type,bucketName));
				String[] mimType = mime_type.split("/");
				ArrayList<FileData> imageGalleryModels;
				if(mFoldersListMap.containsKey(id)){
					imageGalleryModels = mFoldersListMap.get(id);
				}else {
					imageGalleryModels = new ArrayList<>();
				}
				imageGalleryModels.add(new FileData(id,path,mime_type,bucketName));
				mFoldersListMap.put(id,imageGalleryModels);

				if(mimType != null && mimType.length > 1){
					if(mimType[0].equals("video")){
						fileData.add(new FileData(id,path,mime_type,bucketName));
					}
				}
				cursor.moveToNext();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Map.Entry<Integer, ArrayList<FileData>> entry : mFoldersListMap.entrySet()) {
			FileData data = entry.getValue().get(0);
			BucketItem bucketItem = new BucketItem(data.bucketName, data.filepath, "",data.id);
			bucketItem.images = entry.getValue().size();
			buckets.add(bucketItem);
		}

		if(fileData.size() > 0){
			BucketItem bucketItem = new BucketItem("All Video", fileData.get(0).filepath, "",fileData.get(0).id,true);
			bucketItem.images = fileData.size();
			buckets.add(2,bucketItem);
		}


		return buckets;
	}

}
