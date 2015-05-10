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
package com.gao.im.ui.chatting;

import java.io.InvalidClassException;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.gao.im.R;
import com.gao.im.common.utils.DemoUtils;
import com.gao.im.common.utils.ECPreferenceSettings;
import com.gao.im.common.utils.ECPreferences;
import com.gao.im.common.view.TopBarView;
import com.gao.im.ui.ECSuperActivity;
import com.gao.im.ui.manager.CCPAppManager;

/**
 * 图片预览
 * @author Jorstin Chan@容联•云通讯
 * @date 2015-1-4
 * @version 4.0
 */
public class ImagePreviewActivity extends ECSuperActivity implements View.OnClickListener{

	private TopBarView mTopBarView;
	private ImageView mImageView;
	private Bitmap bitmap;
	
	@Override
	protected int getLayoutId() {
		return R.layout.image_preview_activity;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTopBarView = getTopBarView();
		mTopBarView.setTopBarToStatus(1, R.drawable.topbar_back_bt,
				R.drawable.btn_style_green, null,
				getString(R.string.dialog_ok_button),
				getString(R.string.app_title_image_preview), null, this);
		mTopBarView.setRightBtnEnable(false);
		
		initViewUI();
	}
	
	private void initViewUI() {
		final String path = ECPreferences.getSharedPreferences().
		getString(ECPreferenceSettings.SETTINGS_CROPIMAGE_OUTPUTPATH.getId(), 
				(String)ECPreferenceSettings.SETTINGS_CROPIMAGE_OUTPUTPATH.getDefaultValue());
		
		mImageView = (ImageView) findViewById(R.id.image);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				bitmap = DemoUtils.getSuitableBitmap(path);
				mImageView.post(new Runnable() {
					
					@Override
					public void run() {
						mImageView.setImageBitmap(bitmap);
						mImageView.invalidate();
						mTopBarView.setRightBtnEnable(true);
						
					}
				});
			}
		}).start();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		case R.id.text_right:
			CCPAppManager.putPref(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED.getId() , Boolean.TRUE);
			try {
				ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED, Boolean.TRUE, true);
				setResult(RESULT_OK);
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
			finish();
			break;
		default:
			break;
		}
	}

}
