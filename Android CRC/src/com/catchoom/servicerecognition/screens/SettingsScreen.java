// © Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.catchoom.api.CatchoomErrorResponseItem;
import com.catchoom.api.CatchoomResponseHandler;
import com.catchoom.servicerecognition.CatchoomApplication;
import com.catchoom.servicerecognition.R;

public class SettingsScreen extends Activity implements OnClickListener, TextWatcher, CatchoomResponseHandler {

	private EditText collectionTokenEdit = null;
	private Button startButton = null;
	private ProgressDialog mProgressDialog = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings);
		
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(this);
		startButton.setTag("disabled");
		
		collectionTokenEdit = (EditText) findViewById(R.id.collectionToken);
		collectionTokenEdit.addTextChangedListener(this);
		
		// Set default values if any
		if (null != CatchoomApplication.preferences) {
			collectionTokenEdit.setText(CatchoomApplication.preferences.getString(CatchoomApplication.PREFS_COLLECTION_TOKEN_KEY, ""));
		}
		
		CatchoomApplication.catchoom.setResponseHandler(this);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.startButton:
			
			if (v.getTag().equals("enabled")) {
				String token = collectionTokenEdit.getText().toString();
				dismissProgressDialog();
				mProgressDialog = ProgressDialog.show(SettingsScreen.this, null, getString(R.string.checking_collection), true, false);
				CatchoomApplication.catchoom.connect(token);

			} else {
				Builder dialog = new AlertDialog.Builder(SettingsScreen.this);
				dialog.setTitle(getString(R.string.alert_dialog_title));
				dialog.setMessage(getString(R.string.warning_settings));
				dialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.setCancelable(true);
				dialog.show();
			}
			
			break;
		}
	}

	@Override
	public void requestCompletedResponse(int requestCode, Object responseData) {
		
		dismissProgressDialog();
		
		Log.d(CatchoomApplication.APP_LOG_TAG, "Collection valid with timestamp " + responseData.toString());
		
		CheckBox remember = (CheckBox) findViewById(R.id.remember);
		
		if (remember.isChecked() && null != CatchoomApplication.preferences) {
			// Save collection settings
			Editor editor = CatchoomApplication.preferences.edit();
			editor.putString(CatchoomApplication.PREFS_COLLECTION_TOKEN_KEY, collectionTokenEdit.getText().toString());
			editor.commit();
		}
		
		// Go to results screen
		Intent intent = new Intent(SettingsScreen.this, ResultsScreen.class);
		intent.putExtra("collectionToken", collectionTokenEdit.getText().toString());
		startActivity(intent);
	}

	@Override
	public void requestFailedResponse(CatchoomErrorResponseItem responseError) {
		
		dismissProgressDialog();
		
		// Collection invalid or request failed
		if (null == responseError) {
			Toast.makeText(SettingsScreen.this, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
		} else {
			Log.d(CatchoomApplication.APP_LOG_TAG, responseError.getErrorCode() + ": " + responseError.getErrorPhrase());
			
			switch (responseError.getErrorCode()) {
			case 401:
			case 403:
				Toast.makeText(SettingsScreen.this, getString(R.string.error_settings), Toast.LENGTH_SHORT).show();
				break;
			case 400:
			case 500:
			default:
				Toast.makeText(SettingsScreen.this, getString(R.string.error_request), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void dismissProgressDialog() {
		if (null != mProgressDialog) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	// TextWatcher listener
	@Override
	public void afterTextChanged(Editable s) {
		
		if (collectionTokenEdit.getText().toString().equals("")) {
			startButton.setBackgroundResource(R.drawable.bg_disabled_button);
			startButton.setTag("disabled");
		} else {
			startButton.setBackgroundResource(R.drawable.red_button_drawable);
			startButton.setTag("enabled");
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
