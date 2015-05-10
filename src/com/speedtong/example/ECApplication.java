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
package com.speedtong.example;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.ui.manager.CCPAppManager;

/**
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class ECApplication extends Application {

	private static ECApplication instance;
	
	/**
	 * 单例，返回一个实例
	 * @return
	 */
	public static ECApplication getInstance() {
		if (instance == null) {
			LogUtil.w("[ECApplication] instance is null.");
		}
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		CCPAppManager.setContext(instance);
	}
	
	/**
	 * 返回配置文件的日志开关
	 * @return
	 */
	public boolean getLoggingSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("LOGGING");
			LogUtil.w("[ECApplication - getLogging] logging is: " + b);
			return b;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public boolean getAlphaSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("ALPHA");
			LogUtil.w("[ECApplication - getAlpha] Alpha is: " + b);
			return b;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}
}
