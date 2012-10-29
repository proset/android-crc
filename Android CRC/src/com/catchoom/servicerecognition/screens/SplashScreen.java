// © Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition.screens;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.catchoom.api.CatchoomErrorResponseItem;
import com.catchoom.api.CatchoomResponseHandler;
import com.catchoom.servicerecognition.CatchoomApplication;
import com.catchoom.servicerecognition.R;

public class SplashScreen extends Activity {

	private static final long MILLIS_TO_SKIP = 2000;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		// Set click listener to go to the Catchoom website when clicking the logo
		ImageView splashLogo = (ImageView) findViewById(R.id.splashLogo);
		splashLogo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent goToWeb = new Intent(Intent.ACTION_VIEW);
				goToWeb.setData(Uri.parse(CatchoomApplication.WEBSITE_URL));
				startActivity(goToWeb);
			}
		});
		
		new SplashTimer().execute();
		
	}
	
	private class SplashTimer extends AsyncTask<Void, Void, Void> implements CatchoomResponseHandler {
		
		private boolean collectionChecked = false;
		private boolean collectionValid = false;
		private boolean canSkip = false;
		
		@Override
		protected void onPreExecute() {
			CatchoomApplication.catchoom.setResponseHandler(this);
		}
		
		@Override
		protected Void doInBackground(Void... params) {

			// Check if collection settings has been ever set
			if (null != CatchoomApplication.preferences) {
				
				String token = CatchoomApplication.preferences.getString(CatchoomApplication.PREFS_COLLECTION_TOKEN_KEY, "");
				
				if (!token.equals("")) {
					// Check collection and token are valid
					CatchoomApplication.catchoom.connect(token);
				} else {
					collectionChecked = true;
				}
			}
			
			// Wait for Splash minimum timeout
			try {
				Thread.sleep(MILLIS_TO_SKIP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			canSkip = true;
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			dismissSplashScreen();
			
		}
		
		@Override
		public void requestCompletedResponse(int requestCode, Object responseData) {
			// Collection is set and valid
			collectionValid = collectionChecked = true;
			Log.d(CatchoomApplication.APP_LOG_TAG, "Collection valid with timestamp " + responseData.toString());
			dismissSplashScreen();	
		}

		@Override
		public void requestFailedResponse(CatchoomErrorResponseItem responseError) {
			// Collection is set but invalid or request failed
			if (null == responseError) {
				Toast.makeText(SplashScreen.this, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();
			} else {
				Log.d(CatchoomApplication.APP_LOG_TAG, responseError.getErrorCode() + ": " + responseError.getErrorPhrase());
				
				switch (responseError.getErrorCode()) {
				case 401:
				case 403:
					Toast.makeText(SplashScreen.this, getString(R.string.error_settings), Toast.LENGTH_SHORT).show();
					break;
				case 400:
				case 500:
				default:
					Toast.makeText(SplashScreen.this, getString(R.string.error_request), Toast.LENGTH_SHORT).show();
				}
			}
			
			collectionChecked = true;
			dismissSplashScreen();
		}
		
		private void dismissSplashScreen() {
			
			if (canSkip && collectionChecked) {
				// Go either to settings or results screen
				Class<? extends Activity> targetScreen = collectionValid ? ResultsScreen.class : SettingsScreen.class;
				Intent intent = new Intent(SplashScreen.this, targetScreen);
				startActivity(intent);
				
				// Unload the splash screen
				finish();
			}
		}
	}
}
