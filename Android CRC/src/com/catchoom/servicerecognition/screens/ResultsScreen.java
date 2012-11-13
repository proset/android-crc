// © Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition.screens;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.catchoom.api.CatchoomErrorResponseItem;
import com.catchoom.api.CatchoomResponseHandler;
import com.catchoom.api.CatchoomSearchResponseItem;
import com.catchoom.servicerecognition.CatchoomApplication;
import com.catchoom.servicerecognition.R;
import com.catchoom.servicerecognition.item.ItemsAdapter;
import com.catchoom.servicerecognition.util.ImageManager;
import com.catchoom.servicerecognition.util.ImageUtil;

public class ResultsScreen extends Activity implements OnClickListener, CatchoomResponseHandler {

	private static final int TAKE_PICTURE_REQUEST_CODE = 1;
	private static final int PICK_FROM_GALLERY_REQUEST_CODE = 2;
	
	private Uri pictureUri = null;
	private String token = null;
	
	private ImageManager mImageManager = null;
	private ProgressDialog mProgressDialog = null;
	private ArrayList<CatchoomSearchResponseItem> mItems = null;
	private ItemsAdapter mAdapter = null;
	
	public ResultsScreen() {
		
		// Set default values if any
		if (null != CatchoomApplication.preferences) {
			token = CatchoomApplication.preferences.getString(CatchoomApplication.PREFS_COLLECTION_TOKEN_KEY, "");
		}
		
		File outputPicture = ImageUtil.getOutputPicture();
		if (null != outputPicture) {
			pictureUri = Uri.fromFile(outputPicture);
		}
		
		CatchoomApplication.catchoom.setResponseHandler(this);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		
		mAdapter = new ItemsAdapter(ResultsScreen.this);
		
		// Check for the integrity of our UI objects
		final ArrayList<Object> savedData = (ArrayList<Object>) getLastNonConfigurationInstance();
		if (null != savedData) {
			mImageManager = (ImageManager) savedData.get(0);
			mItems = (ArrayList<CatchoomSearchResponseItem>) savedData.get(1);
			if (null != mItems) {
				Log.d(CatchoomApplication.APP_LOG_TAG, "Items not null");
				updateContent(mItems);
			}
		} else {
			mImageManager = new ImageManager();
		}
		
		mAdapter.setImageManager(mImageManager);
		
		// Check for the integrity of our collection settings
		if (null != savedInstanceState && savedInstanceState.containsKey("collectionToken")) {
			token = savedInstanceState.getString("collectionToken");
		} else if (null != getIntent()) {
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			
			if (null != extras && extras.containsKey("collectionToken")) {
				token = extras.getString("collectionToken");
			}
		}

		if (token.equals("")) {
			// If settings has not been set properly, go automatically to the settings screen
			Toast.makeText(ResultsScreen.this, getString(R.string.error_settings), Toast.LENGTH_SHORT).show();
			if (isTaskRoot()) {
				Intent backToSettings = new Intent(ResultsScreen.this, SettingsScreen.class);
				startActivity(backToSettings);
			}
			finish();
		}
		
		TextView collectionSelected = (TextView) findViewById(R.id.collectionSelected);
		collectionSelected.setText(token);
		
		
		ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);
		settingsButton.setOnClickListener(this);
		
		ImageButton cameraButton = (ImageButton) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(this);
		
		ImageButton galleryButton = (ImageButton) findViewById(R.id.galleryButton);
		galleryButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.settingsButton:
			// Go to the settings screen
			if (isTaskRoot()) {
				Intent intent = new Intent(ResultsScreen.this, SettingsScreen.class);
				startActivity(intent);
			}
			finish();
			break;
		case R.id.cameraButton:
			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
				// The device does not have a camera available
				Log.w(CatchoomApplication.APP_LOG_TAG, "Camera intent aborted due to camera unavailable");
				Toast.makeText(ResultsScreen.this, getString(R.string.error_no_camera), Toast.LENGTH_SHORT).show();
			} else {
				// Trigger the camera
				if (null != pictureUri) {
					Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
					startActivityForResult(cameraIntent, TAKE_PICTURE_REQUEST_CODE);
				} else {
					Toast.makeText(ResultsScreen.this, R.string.error_external_storage, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.galleryButton:
			
			// Trigger the gallery
			Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			galleryIntent.setType("image/*");
			startActivityForResult(galleryIntent, PICK_FROM_GALLERY_REQUEST_CODE);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedImage) {
		super.onActivityResult(requestCode, resultCode, returnedImage);
		
		File picture = null;
		
		switch (requestCode) {
		case TAKE_PICTURE_REQUEST_CODE:
			
			switch (resultCode) {
			case RESULT_OK:
				picture = new File(pictureUri.getPath());
				break;
			case RESULT_CANCELED:
				Log.d(CatchoomApplication.APP_LOG_TAG, "Camera capture cancelled");
				break;
			}
			
			break;
		case PICK_FROM_GALLERY_REQUEST_CODE:
			
			switch (resultCode) {
			case RESULT_OK:
				picture = new File(pictureUri.getPath());
				// Copy the image to prevent overwriting
				if (!ImageUtil.copyImage(new File(getPathFromUri(returnedImage.getData())), picture)) {
					picture = null;
				}
				break;
			case RESULT_CANCELED:
				Log.d(CatchoomApplication.APP_LOG_TAG, "Picture pick cancelled");
				break;
			}
			
			break;
		}
		
		if (null != picture) {
			dismissProgressDialog();
			mProgressDialog = ProgressDialog.show(ResultsScreen.this, null, getString(R.string.processing));
			
			CatchoomApplication.catchoom.search(token, picture);
		}
	}
	
	@Override
	public void requestCompletedResponse(int requestCode, Object responseData) {
		dismissProgressDialog();
		updateContent((ArrayList<CatchoomSearchResponseItem>) responseData);
	}

	@Override
	public void requestFailedResponse(CatchoomErrorResponseItem responseError) {
		
		dismissProgressDialog();
		
		if (null == responseError) {
			Toast.makeText(ResultsScreen.this, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
		} else {
			Log.d(CatchoomApplication.APP_LOG_TAG, responseError.getErrorCode() + ": " + responseError.getErrorPhrase());
			
			switch (responseError.getErrorCode()) {
			case 401:
			case 403:
				Toast.makeText(ResultsScreen.this, getString(R.string.error_settings), Toast.LENGTH_SHORT).show();
				break;
			case 400:
			case 500:
			default:
				Toast.makeText(ResultsScreen.this, getString(R.string.error_request), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void updateContent(ArrayList<CatchoomSearchResponseItem> items) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout newContent = (LinearLayout) inflater.inflate(R.layout.results_content, null);
		LinearLayout resultsContainer = (LinearLayout) findViewById(R.id.resultsContainer);
		
		if (null == newContent) {
			// Error inflating newView
			Log.e(CatchoomApplication.APP_LOG_TAG, "Error inflating results view");
			Toast.makeText(ResultsScreen.this, R.string.error_inflating_view, Toast.LENGTH_SHORT).show();
		} else {
			TextView resultsFound = (TextView) newContent.findViewById(R.id.resultsFound);
			resultsFound.setText(MessageFormat.format(getString(R.string.label_results_found), items.size()));
			
			mAdapter.setItems(items);
			
			ListView resultsList = (ListView) newContent.findViewById(R.id.resultsList);
			resultsList.setAdapter(mAdapter);
			resultsList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						Intent goToWeb = new Intent(Intent.ACTION_VIEW);
						String url = mAdapter.getItem(position).getMetadata().getString("url");
						
						if (null != url) {
							// Little hack to prevent Uri parser to crash with malformed URLs
							if (!url.matches("https?://")) url = "http://" + url;
							
							goToWeb.setData(Uri.parse(url));
							startActivity(goToWeb);
						}
					}
			});
			
			resultsContainer.removeAllViewsInLayout();
			resultsContainer.addView(newContent);
		}
		
		mItems = items;
	}
	
	private void dismissProgressDialog() {
		if (null != mProgressDialog && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	/**
	 * Get file path from content URI
	 * @param contentUri
	 * @return The file path
	 */
	private String getPathFromUri(Uri contentUri) {
		
		String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        
        return cursor.getString(column_index);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("collectionToken", token);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<Object> data = new ArrayList<Object>();
	    data.add(mImageManager);
	    data.add(mItems);
	    return data;
	}
}
