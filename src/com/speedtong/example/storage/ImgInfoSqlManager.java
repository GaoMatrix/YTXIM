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
package com.speedtong.example.storage;

import java.io.File;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;

import com.speedtong.example.common.utils.DateUtil;
import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.common.utils.FileAccessor;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.ui.chatting.mode.ImgInfo;
import com.speedtong.example.ui.plugin.FileUtils;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.platformtools.VoiceUtil;

/**
 * 图片保存
 * @author Jorstin Chan@容联•云通讯
 * @date 2015-1-4
 * @version 4.0
 */
public class ImgInfoSqlManager extends AbstractSQLManager {

	public HashMap<String, Bitmap> imgThumbCache = new HashMap<String, Bitmap>(20);
	private static int column_index = 1;
	
	public static ImgInfoSqlManager mInstance;
	public static ImgInfoSqlManager getInstance() {
		if(mInstance == null) {
			mInstance = new ImgInfoSqlManager();
		}
		return mInstance;
	}
	
	static final String TABLES_NAME_IMGINFO = "imginfo";
	
	public class ImgInfoColumn extends BaseColumn{
		
		public static final String MSGSVR_ID = "msgSvrId";
		public static final String OFFSET = "offset";
		public static final String TOTALLEN ="totalLen";
		public static final String BIG_IMGPATH = "bigImgPath";
		public static final String THUMBIMG_PATH = "thumbImgPath";
		public static final String CREATE_TIME = "createtime";
		public static final String STATUS = "status";
		public static final String MSG_LOCAL_ID = "msglocalid";
		public static final String NET_TIMES = "nettimes";

	}
	
	private ImgInfoSqlManager() {
		Cursor cursor = sqliteDB().query(TABLES_NAME_IMGINFO, null, null, null, null, null, ImgInfoColumn.ID + " ASC ");
		if ((cursor.getCount() > 0) && (cursor.moveToLast())) {
			column_index = 1 + cursor.getInt(cursor.getColumnIndex(ImgInfoColumn.ID));
		}
		cursor.close();
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()),  "loading new img id:" + column_index);
	}
	
	public long insertImageInfo(ImgInfo imgInfo) {
		if(imgInfo == null) {
			return -1;
		}
		ContentValues buildContentValues = imgInfo.buildContentValues();
		if(buildContentValues.size() == 0) {
			return -1;
		}
		try {
			return sqliteDB().insert(TABLES_NAME_IMGINFO, null, buildContentValues);
		} catch (Exception e) {
			LogUtil.e(TAG, "insert imgInfo error = " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * 
	 * @param imgInfo
	 * @return
	 */
	public long updateImageInfo(ImgInfo imgInfo) {
		if(imgInfo == null) {
			return -1;
		}
		ContentValues buildContentValues = imgInfo.buildContentValues();
		if(buildContentValues.size() == 0) {
			return -1;
		}
		try {
			String where = ImgInfoColumn.ID + " = " + imgInfo.getId();
			return sqliteDB().update(TABLES_NAME_IMGINFO, buildContentValues, where, null);
		} catch (Exception e) {
			LogUtil.e(TAG, "insert imgInfo error = " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public ImgInfo createImgInfo(String filePath) {
		
		if(!FileUtils.checkFile(filePath)) {
			return null;
		}
		
		int bitmapDegrees = DemoUtils.getBitmapDegrees(filePath);
		String fileNameMD5 = VoiceUtil.md5(filePath);
		String bigFileFullName = fileNameMD5 + ".jpg";
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "original img path = " + filePath);
		
		Options bitmapOptions = DemoUtils.getBitmapOptions(filePath);
		String authorityDir = FileAccessor.getImagePathName().getAbsolutePath();
		if((FileUtils.decodeFileLength(filePath) > 204800) 
				|| (bitmapOptions != null && (((bitmapOptions.outHeight > 960) || (bitmapOptions.outWidth > 960))))) {
			File file = new File(authorityDir);
			if(!file.exists()) {
				file.mkdirs();
			}
			
			if(!DemoUtils.createThumbnailFromOrig(filePath, 960, 960, Bitmap.CompressFormat.JPEG, 70, FileAccessor.getImagePathName().getAbsolutePath(), fileNameMD5)) {
				return null;
			}
			FileAccessor.renameTo(authorityDir+File.separator, fileNameMD5, bigFileFullName);
		} else {
			// file size small.
			FileUtils.copyFile(authorityDir, fileNameMD5 , ".jpg", FileUtils.readFlieToByte(filePath, 0, FileUtils.decodeFileLength(filePath)));
		}
		if(bitmapDegrees != 0 && !DemoUtils.rotateCreateBitmap(authorityDir +File.separator+ bigFileFullName, bitmapDegrees, Bitmap.CompressFormat.JPEG, authorityDir, bigFileFullName)) {
			return null;
		}
		LogUtil.d(TAG, "insert: compressed bigImgPath = " + bigFileFullName);
		String thumbName = VoiceUtil.md5(fileNameMD5 + System.currentTimeMillis());
		File file = new File(authorityDir);
		if(!file.exists()) {
			file.mkdirs();
		}
		if(!DemoUtils.createThumbnailFromOrig(authorityDir +File.separator+ bigFileFullName, 100, 100, Bitmap.CompressFormat.JPEG, 60, authorityDir, thumbName)) {
			return null;
		}
		LogUtil.d(TAG, "insert: thumbName = " + thumbName);
		ImgInfo imgInfo = new ImgInfo();
		column_index += 1;
		imgInfo.setId(column_index);
		imgInfo.setBigImgPath(bigFileFullName);
		imgInfo.setThumbImgPath(thumbName);
		imgInfo.setCreatetime((int)DateUtil.getCurrentTime());
		imgInfo.setTotalLen(FileUtils.decodeFileLength(filePath));
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "insert: compress img size = " + imgInfo.getTotalLen());
		return imgInfo;
	}
	
	/**
	 * 接收图片生成缩略图
	 * @param filePath
	 * @return
	 */
	public ImgInfo getThumbImgInfo(ECMessage msg) {
		ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
		if(TextUtils.isEmpty(body.getLocalUrl()) || !new File(body.getLocalUrl()).exists()) {
			return null;
		}
		LogUtil.d(TAG, "insert: thumbName = " + body.getFileName());
		ImgInfo imgInfo = new ImgInfo();
		column_index += 1;
		imgInfo.setId(column_index);
		if(!TextUtils.isEmpty(body.getThumbnailFileUrl())) {
			imgInfo.setBigImgPath(body.getRemoteUrl());
		} else {
			imgInfo.setBigImgPath(null);
		}
		/*if(body.getRemoteUrl().contains("_thumbnail")) {
			imgInfo.setBigImgPath(body.getRemoteUrl().replace("_thumbnail", ""));
		} else {
			imgInfo.setBigImgPath(null);
		}*/
		imgInfo.setThumbImgPath(new File(body.getLocalUrl()).getName());
		imgInfo.setMsglocalid(msg.getMsgId());
		imgInfo.setCreatetime((int)DateUtil.getCurrentTime());
		imgInfo.setTotalLen(FileUtils.decodeFileLength(body.getLocalUrl()));
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "insert: compress img size = " + imgInfo.getTotalLen());
		return imgInfo;
	}
	
	public Bitmap getThumbBitmap(String fileName , float scale) {
		if(TextUtils.isEmpty(fileName)) {
			return null;
		}
		if(fileName.trim().startsWith("THUMBNAIL://")) {
			String fileId = fileName.substring("THUMBNAIL://".length());
			String imgName = getImgInfo(fileId).getThumbImgPath();
			if(imgName == null) {
				return null;
			}
			String fileUrlByFileName = FileAccessor.getImagePathName() + "/" + imgName;;
			Bitmap bitmap = imgThumbCache.get(fileUrlByFileName);
			if(bitmap == null || bitmap.isRecycled()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
			    float density = 160.0F * scale;
			    options.inDensity = (int)density;
			    bitmap = BitmapFactory.decodeFile(fileUrlByFileName, options);
			    if (bitmap != null){
			    	bitmap.setDensity((int)density);
			    	bitmap = Bitmap.createScaledBitmap(bitmap, (int)(scale * bitmap.getWidth()), (int)(scale * bitmap.getHeight()), true);
			    	imgThumbCache.put(fileUrlByFileName, bitmap);
			    	LogUtil.d(TAG, "cached file " + fileName);
			    }
			}
			
			if(bitmap != null) {
				return DemoUtils.processBitmap(bitmap, bitmap.getWidth() / 15);
			}
			
		}
		return null;
	}
	
	/**
	 * 
	 * @param msgId
	 * @return
	 */
	public ImgInfo getImgInfo(String msgId) {
		ImgInfo imgInfo = new ImgInfo();
		String where = ImgInfoColumn.MSG_LOCAL_ID + "='" + msgId + "'";
		Cursor cursor = sqliteDB().query(TABLES_NAME_IMGINFO, null, where, null, null, null, null);
		if(cursor.getCount() != 0) {
			cursor.moveToFirst();
			imgInfo.setCursor(cursor);
		}
		cursor.close();
		return imgInfo;
	}
	
	public static void reset() {
		getInstance().release();
	}
	
	@Override
	protected void release() {
		super.release();
		mInstance = null;
	}
}
