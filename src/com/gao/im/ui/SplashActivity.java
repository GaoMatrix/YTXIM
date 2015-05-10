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
package com.gao.im.ui;

import android.os.Bundle;
import android.os.Handler;

import com.gao.im.R;

/**
 * 启动页面
 * @author Jorstin Chan@容联•云通讯
 * @date 2015-1-8
 * @version 4.0
 */
public class SplashActivity extends ECSuperActivity {

	@Override
	protected int getLayoutId() {
		return R.layout.splash_activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				finish();
			}
		}, 1700);
	}
}