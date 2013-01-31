// (c) Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

import com.catchoom.servicerecognition.CatchoomApplication;

public class ImageUtil {

	private static final String TMP_PICTURE_NAME = "catchoom_picture.tmp";
	
	/**
	 * Checks whether the external storage is mounted
	 * @return External storage is mounted
	 */
	public static boolean isExternalStorageMounted() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * Generates the output file for the used pictures
	 * @return The output file for the picture
	 */
	public static File getOutputPicture() {
	    if (!isExternalStorageMounted()) {
	    	Log.d(CatchoomApplication.APP_LOG_TAG, "External storage unmounted");
	    	return null;
	    }
		
	    // Create the directory for the app
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + CatchoomApplication.APP_DIR_PATH);

	    if (!mediaStorageDir.exists()){
	        if (!mediaStorageDir.mkdirs()){
	            Log.d(CatchoomApplication.APP_LOG_TAG, "Failed to create directory to store picture");
	            return null;
	        }
	    }

	    // Create the file to storage the picture
	    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + TMP_PICTURE_NAME);
	    return mediaFile;
	}
	
	/**
	 * Copies an image from a source to a destination
	 * @param srcImage The source image
	 * @param dstImage The destination image
	 * @return true if the operation has succeed
	 */
	public static boolean copyImage(File srcImage, File dstImage) {
		try {
			InputStream in = new FileInputStream(srcImage);
			OutputStream out = new FileOutputStream(dstImage);
			
			byte[] buffer = new byte[1024];
		    int read;
		    while ((read = in.read(buffer)) != -1) {
		      out.write(buffer, 0, read);
		    }
		    
		    in.close();
		    out.flush();
		    out.close();
		    
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
