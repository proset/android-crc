// © Catchoom Technologies S.L.
// Licensed under the MIT license.
// https://raw.github.com/catchoom/android-crc/master/LICENSE
// All warranties and liabilities are disclaimed.
package com.catchoom.servicerecognition.item;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.catchoom.api.CatchoomSearchResponseItem;
import com.catchoom.servicerecognition.R;
import com.catchoom.servicerecognition.util.ImageManager;

public class ItemsAdapter extends BaseAdapter {

	private Context mContext = null;
	private ArrayList<CatchoomSearchResponseItem> mItems = null;
	private ImageManager mImageManager = null;
	
	public ItemsAdapter(Context context) {
		mContext = context;
	}
	
	public ItemsAdapter(Context context, ArrayList<CatchoomSearchResponseItem> items) {
		mContext = context;
		this.mItems = items;
	}
	
	public void setImageManager(ImageManager manager) {
		mImageManager = manager;
	}
	
	public void setItems(ArrayList<CatchoomSearchResponseItem> items) {
		mItems = items;
	}
	
	public boolean addItem(CatchoomSearchResponseItem item) {
		return mItems.add(item);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public CatchoomSearchResponseItem getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		
		if (null == convertView) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.result_row, null);
		} else {
			view = convertView;
		}
		
		final CatchoomSearchResponseItem item = (CatchoomSearchResponseItem) getItem(position);
		
		if (null != item) {
			Bundle metadata = item.getMetadata();
			
			TextView itemName = (TextView) view.findViewById(R.id.itemName);
			TextView itemScore = (TextView) view.findViewById(R.id.itemScore);
			ImageView viewport = (ImageView) view.findViewById(R.id.viewport);
			
			String name = metadata.getString("name");
			String thumbnailUrl = metadata.getString("thumbnail");
			
			if (null != thumbnailUrl && null != mImageManager) {
				mImageManager.loadImageInView(thumbnailUrl, viewport);
			}
			
			if (null == name) {
				name = item.getId();
			}
			
			itemName.setText(name);
			itemScore.setText(String.format(mContext.getString(R.string.score), item.getScore()));
			
		} else {
			return null;
		}
		
		return view;
	}
}
