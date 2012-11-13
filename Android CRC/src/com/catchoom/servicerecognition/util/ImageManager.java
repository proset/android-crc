// © Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import com.catchoom.servicerecognition.CatchoomApplication;

public class ImageManager extends Handler {

	private final SoftReference<HashMap<String, Drawable>> imagesMap;
	
	public ImageManager() {
        imagesMap = new SoftReference<HashMap<String, Drawable>>(new HashMap<String, Drawable>());
    }

    public Drawable getDrawableFromURL(String url) {
        if (imagesMap.get().containsKey(url)) {
            return imagesMap.get().get(url);
        }

        try {
            InputStream is = downloadImage(url);
            Drawable drawable = Drawable.createFromStream(is, url);

            if (drawable != null) {
            	imagesMap.get().put(url, drawable);
            } else {
            	Log.e(CatchoomApplication.APP_LOG_TAG, "Error downloading thumbnail from " + url);
            }

            return drawable;
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        return null;
    }

   public void loadImageInView(final String urlString, final ImageView imageView) {
        if (imagesMap.get().containsKey(urlString)) {
            imageView.setImageDrawable(imagesMap.get().get(urlString));
        } else {
	        Thread thread = new Thread() {
	            @Override
	            public void run() {
	                Drawable drawable = getDrawableFromURL(urlString);
	                Pair<ImageView, Drawable> imageData = new Pair<ImageView, Drawable>(imageView, drawable);
	                Message message = obtainMessage(1, imageData);
	                sendMessage(message);
	            }
	        };
	        thread.start();
        }
    }

    private InputStream downloadImage(String urlString) throws MalformedURLException, IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
    }
	
    @Override
	public void handleMessage(Message message) {
    	Pair<ImageView, Drawable> imageData = (Pair<ImageView, Drawable>) message.obj;
    	ImageView placeHolder = imageData.first;
    	placeHolder.setImageDrawable(imageData.second);
    }
}
