// (c) Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition;

import org.apache.http.HttpVersion;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.catchoom.api.Catchoom;
import com.catchoom.servicerecognition.util.ImageManager;

public class CatchoomApplication extends Application {

	public static final String APP_LOG_TAG = "Catchoom app";
	
	public static final String PREFS_EDITOR_NAME = "catchoomPrefs";
	public static final String PREFS_COLLECTION_TOKEN_KEY = "collectionToken";
	
	public static final String WEBSITE_URL = "http://catchoom.com";
	public static final String APP_DIR_PATH = ".catchoom";
	
	public static SharedPreferences preferences = null;
	public static Catchoom catchoom = null;
	public static ImageManager imageManager = null;
	public static Context context = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Set up
		CatchoomApplication.preferences = getSharedPreferences(CatchoomApplication.PREFS_EDITOR_NAME, Context.MODE_PRIVATE);
		HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		CatchoomApplication.catchoom = new Catchoom();
		imageManager = new ImageManager();
		context = getApplicationContext();
	}
}
