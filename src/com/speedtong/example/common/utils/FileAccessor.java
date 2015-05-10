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
package com.speedtong.example.common.utils;

import java.io.File;

import android.os.Environment;
import android.text.TextUtils;

import com.speedtong.example.ECApplication;
import com.speedtong.example.R;

/**
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class FileAccessor {
	
	public static final String TAG = FileAccessor.class.getName();
	public static final String APPS_ROOT_DIR = getExternalStorePath() + "/ECSDK_Demo";
	public static final String CAMERA_PATH = getExternalStorePath() + "/DCIM/ECSDK_Demo";
	public static final String TACK_PIC_PATH = getExternalStorePath()+ "/ECSDK_Demo/.tempchat";
	public static final String IMESSAGE_VOICE = getExternalStorePath() + "/ECSDK_Demo/voice";
	public static final String IMESSAGE_IMAGE = getExternalStorePath() + "/ECSDK_Demo/image";
	public static final String IMESSAGE_AVATAR = getExternalStorePath() + "/ECSDK_Demo/avatar";
	public static final String IMESSAGE_FILE = getExternalStorePath() + "/ECSDK_Demo/file";

	
	/**
	 * 初始化应用文件夹目录
	 */
	public static void initFileAccess() {
		File rootDir = new File(APPS_ROOT_DIR);
		if (!rootDir.exists()) {
			rootDir.mkdir();
		}
		
		File imessageDir = new File(IMESSAGE_VOICE);
		if (!imessageDir.exists()) {
			imessageDir.mkdir();
		}
		
		File imageDir = new File(IMESSAGE_IMAGE);
		if (!imageDir.exists()) {
			imageDir.mkdir();
		}
		
		File fileDir = new File(IMESSAGE_FILE);
		if (!fileDir.exists()) {
			fileDir.mkdir();
		}
		File avatarDir = new File(IMESSAGE_AVATAR);
		if (!avatarDir.exists()) {
			avatarDir.mkdir();
		}
	}
	
	/**
	 * 获取语音文件存储目录
	 * @return
	 */
	public static File getVoicePathName() {
		if (!isExistExternalStore()) {
			ToastUtil.showMessage(R.string.media_ejected);
			return null;
		}
		
		File directory = new File(IMESSAGE_VOICE);
		if (!directory.exists() && !directory.mkdirs()) {
			ToastUtil.showMessage("Path to file could not be created");
			return null;
		}
		
		return directory;
	}
	
	/**
	 * 头像
	 * @return
	 */
	public static File getAvatarPathName() {
		if (!isExistExternalStore()) {
			ToastUtil.showMessage(R.string.media_ejected);
			return null;
		}
		
		File directory = new File(IMESSAGE_AVATAR);
		if (!directory.exists() && !directory.mkdirs()) {
			ToastUtil.showMessage("Path to file could not be created");
			return null;
		}
		
		return directory;
	}
	
	
	
	/**
	 * 获取文件目录
	 * @return
	 */
	public static File getFilePathName() {
		if (!isExistExternalStore()) {
			ToastUtil.showMessage(R.string.media_ejected);
			return null;
		}
		
		File directory = new File(IMESSAGE_FILE);
		if (!directory.exists() && !directory.mkdirs()) {
			ToastUtil.showMessage("Path to file could not be created");
			return null;
		}
		
		return directory;
	}
	
	/**
	 * 返回图片存放目录
	 * @return
	 */
	public static File getImagePathName() {
		if (!isExistExternalStore()) {
			ToastUtil.showMessage(R.string.media_ejected);
			return null;
		}
		
		File directory = new File(IMESSAGE_IMAGE);
		if (!directory.exists() && !directory.mkdirs()) {
			ToastUtil.showMessage("Path to file could not be created");
			return null;
		}
		
		return directory;
	}
	
	/**
	 * 获取文件名
	 * @param pathName
	 * @return
	 */
	public static String getFileName(String pathName) {

		int start = pathName.lastIndexOf("/");
		if (start != -1) {
			return pathName.substring(start + 1, pathName.length());
		}
		return pathName;

	}
	
	/**
	 * 外置存储卡的路径
	 * @return
	 */
	public static String getExternalStorePath() {
		if (isExistExternalStore()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * 是否有外存卡
	 * @return
	 */
	public static boolean isExistExternalStore() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * /data/data/com.ECSDK_Demo.bluetooth/files
	 *
	 * @return
	 */
	public static String getAppContextPath() {
		return ECApplication.getInstance().getFilesDir().getAbsolutePath();
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileUrlByFileName(String fileName) {
		return FileAccessor.IMESSAGE_IMAGE + File.separator + FileAccessor.getSecondLevelDirectory(fileName)+ File.separator + fileName;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getSecondLevelDirectory(String fileName) {
		if(TextUtils.isEmpty(fileName) || fileName.length() < 4) {
			return null;
		}
		
		String sub1 = fileName.substring(0, 2);
		String sub2 = fileName.substring(2, 4);
		return sub1 + File.separator + sub2;
	}
	
	/**
	 * 
	 * @param root
	 * @param srcName
	 * @param destName
	 */
	public static void renameTo(String root , String srcName , String destName) {
		if(TextUtils.isEmpty(root) || TextUtils.isEmpty(srcName) || TextUtils.isEmpty(destName)){
			return;
		}
		
		File srcFile = new File(root + srcName);
		File newPath = new File(root + destName);
		
		if(srcFile.exists()) {
			srcFile.renameTo(newPath);
		}
	}
	
	public static File getTackPicFilePath() {
		File localFile = new File(TACK_PIC_PATH , "temp.jpg");
		if ((!localFile.getParentFile().exists())
				&& (!localFile.getParentFile().mkdirs())) {
			LogUtil.e("hhe", "SD卡不存在");
			localFile = null;
		}
		return localFile;
	}
}
