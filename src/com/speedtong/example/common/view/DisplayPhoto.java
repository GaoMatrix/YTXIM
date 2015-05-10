/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedtong.example.R;
import com.speedtong.example.common.utils.DemoUtils;

/**
 * 头像
 * @author Jorstin Chan
 * @date 2015-1-22
 * @version 4.0
 */
public class DisplayPhoto extends FrameLayout {

	private Context mContext;
	private ImageView mDisplayPhoto;
	private TextView mNamePhoto;
	private ImageView mEditView;
	private Bitmap mPhotoDefaultMask;
	/**
	 * @param context
	 */
	public DisplayPhoto(Context context) {
		this(context , null);
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public DisplayPhoto(Context context, AttributeSet attrs) {
		this(context, attrs , -1);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DisplayPhoto(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initDisplayPhoto();
	}

	/**
	 * 
	 */
	private void initDisplayPhoto() {
		inflate(getContext(), R.layout.contact_display_photo_view, this);
		mDisplayPhoto = (ImageView) findViewById(R.id.display_photo);
		mNamePhoto = (TextView) findViewById(R.id.name_photo);
		mEditView = (ImageView) findViewById(R.id.edit_img);
		mPhotoDefaultMask = ((BitmapDrawable)getResources().getDrawable(R.drawable.bg_photo_default_mask)).getBitmap();
		
		if(isInEditMode()) {
			return ;
		}
		setDefaultPhoto();
	}
	
	
	public void setDefaultPhoto() {
		setDisplayPhoto(R.drawable.personal_center_default_avatar);
	}

	
	public void setDisplayPhoto(int resId) {
		mDisplayPhoto.setImageDrawable(DemoUtils.getDrawable(mContext, resId, mPhotoDefaultMask));
		mNamePhoto.setVisibility(View.INVISIBLE);
	}
	
	public void setDisplayPhoto(Bitmap bitmap) {
		mDisplayPhoto.setImageDrawable(DemoUtils.getDrawable(bitmap,mPhotoDefaultMask));
		mNamePhoto.setVisibility(View.INVISIBLE);
	}
	
	public void setEditImgVisibility(boolean visibility) {
		ImageView editView = mEditView;
		editView.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
	}
	
	public void setEditImg(int resId) {
		mEditView.setImageResource(resId);
		mEditView.setVisibility(View.VISIBLE);
	}

	public void setEditImg(Drawable editBitmap) {
		mEditView.setImageDrawable(editBitmap);
		mEditView.setVisibility(View.VISIBLE);
	}

	public void setText(String text) {
		mNamePhoto.setVisibility(View.VISIBLE);
		mNamePhoto.setText(text);
		mDisplayPhoto.setImageDrawable(DemoUtils.getDrawable(mContext, R.drawable.bg_aptitude_photo, mPhotoDefaultMask));
	}
}
