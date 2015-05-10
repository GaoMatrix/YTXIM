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
package com.gao.im.ui.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

import com.gao.im.common.utils.ECPreferenceSettings;
import com.gao.im.common.utils.ECPreferences;
import com.gao.im.common.utils.LogUtil;
import com.gao.im.common.utils.MimeTypesTools;
import com.gao.im.core.ClientUser;
import com.gao.im.ui.ECSuperActivity;
import com.gao.im.ui.chatting.ImageGalleryActivity;
import com.gao.im.ui.chatting.ImageMsgInfoEntry;
import com.gao.im.ui.chatting.view.ChatFooterPanel;

/**
 * APP管理类
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class CCPAppManager {

	/**Android 应用上下文*/
	private static Context mContext = null;
	/**包名*/
	public static String pkgName = "com.gao.im";
	/**SharedPreferences 存储名字前缀*/
	public static final String PREFIX = "com.gao.im_";
	public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x10000000;
	/**IM功能UserData字段默认文字*/
	public static final String USER_DATA = "Speedtong";
	/**IM聊天更多功能面板*/
	private static ChatFooterPanel mChatFooterPanel;
	private static ClientUser mClientUser;
	public static HashMap<String, Integer> mPhotoCache = new HashMap<String, Integer>();
	public static ArrayList<ECSuperActivity> activities = new ArrayList<ECSuperActivity>();
	
	public static String getPackageName() {
		return pkgName;
	}
	
	/**
	 * 返回SharePreference配置文件名称
	 * @return
	 */
	public static String getSharePreferenceName() {
		return pkgName + "_preferences";
	}
	
	/**
	 * 返回上下文对象
	 * @return
	 */
	public static Context getContext(){
		 return mContext;
	 }
	
	/**
	 * 设置上下文对象
	 * @param context
	 */
	public static void setContext(Context context) {
		mContext = context;
		pkgName = context.getPackageName();
		LogUtil.d(LogUtil.getLogUtilsTag(CCPAppManager.class),
				"setup application context for package: " + pkgName);
	}
	
	/**
	 * 缓存账号注册信息
	 * @param user
	 */
	public static void setClientUser(ClientUser user) {
		mClientUser = user;
	}
	
	/**
	 * 保存注册账号信息
	 * @return
	 */
	public static ClientUser getClientUser() {
		
		if(mClientUser == null) {
			String account = getAutoRegistAccount();
			if(TextUtils.isEmpty(account)) {
				throw new RuntimeException("account not ready");
			}
			String[] split = account.split(",");
			ClientUser user = new ClientUser(split[2]);
			user.setSubSid(split[0]);
			user.setSubToken(split[1]);
			user.setUserToken(split[3]);
			mClientUser = user;
		}
		return mClientUser;
	}
	
	private static String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(), (String)registAuto.getDefaultValue());
		return registAccount;
	}
	
	/**
	 * 获取应用程序版本名称
	 * @return 版本名称
	 */
	public static String getVersion() {
		String version = "0.0.0";
		if(mContext == null) {
			return version;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}
	
	/**
	 * 获取应用版本号
	 * @return 版本号
	 */
	public static int getVersionCode() {
		int code = 1;
		if(mContext == null) {
			return code;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
					getPackageName(), 0);
			code = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return code;
	}
	
	public static ChatFooterPanel getChatFooterPanel(Context context) {
		return mChatFooterPanel;
	}
	
	public static void addActivity(ECSuperActivity activity) {
		activities.add(activity);
	}
	
	public static void clearActivity() {
		for(ECSuperActivity activity : activities) {
			if(activity != null) {
				activity.finish();
				activity = null;
			}
			activities.clear();
		}
	}
	
	/**
	 * @param context
	 * @param path
	 */
	public static void doViewFilePrevieIntent(Context context ,String path) {
		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			String type = MimeTypesTools.getMimeType(context, path);
			File file = new File(path);
			intent.setDataAndType(Uri.fromFile(file), type);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(LogUtil.getLogUtilsTag(CCPAppManager.class), "do view file error " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * 
	 * @param cotnext 
	 * @param value
	 */
	public static void startChattingImageViewAction(Context cotnext, ImageMsgInfoEntry value) {
		Intent intent = new Intent(cotnext, ImageGalleryActivity.class);
		intent.putExtra(ImageGalleryActivity.CHATTING_MESSAGE, value);
		cotnext.startActivity(intent);
	}
	
	public static HashMap<String, Object> prefValues = new HashMap<String, Object>();
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public static void putPref(String key , Object value) {
		prefValues.put(key, value);
	}
	
	public static Object getPref(String key) {
		return prefValues.remove(key);
	}
	
	public static void removePref(String key) {
		prefValues.remove(key);
	}
}
