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

/**
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-10
 * @version 4.0
 */
public enum ECPreferenceSettings {

	/**坚持云通讯登陆账号*/
	SETTINGS_YUNTONGXUN_ACCOUNT("com.speedtong.example_yun_account" , ""),
	/**检查是否需要自动登录*/
	SETTINGS_REGIST_AUTO("com.speedtong.example_account" , ""),
	/**是否使用回车键发送消息*/
	SETTINGS_ENABLE_ENTER_KEY("com.speedtong.example_sendmessage_by_enterkey" , Boolean.TRUE),
	/**聊天键盘的高度*/
	SETTINGS_KEYBORD_HEIGHT("com.speedtong.example_keybord_height" , 0),
	/**新消息声音*/
	SETTINGS_NEW_MSG_SOUND("com.speedtong.example_new_msg_sound" , true),
	/**新消息震动*/
	SETTINGS_NEW_MSG_SHAKE("com.speedtong.example_new_msg_shake" , true),
	SETTING_CHATTING_CONTACTID("com.speedtong.example_chatting_contactid" , ""),
	/**图片缓存路径*/
	SETTINGS_CROPIMAGE_OUTPUTPATH("com.speedtong.example_CropImage_OutputPath" , ""),
	SETTINGS_ABSOLUTELY_EXIT("com.speedtong.example_absolutely_exit" , Boolean.FALSE),
    SETTINGS_FULLY_EXIT("com.speedtong.example_fully_exit" , Boolean.FALSE),
	SETTINGS_PREVIEW_SELECTED("com.speedtong.example_preview_selected" , Boolean.FALSE);
	    
	 
	private final String mId;
	private final Object mDefaultValue;

	/**
	 * Constructor of <code>CCPPreferenceSettings</code>.
	 * @param id
	 *            The unique identifier of the setting
	 * @param defaultValue
	 *            The default value of the setting
	 */
	private ECPreferenceSettings(String id, Object defaultValue) {
		this.mId = id;
		this.mDefaultValue = defaultValue;
	}

	/**
	 * Method that returns the unique identifier of the setting.
	 * @return the mId
	 */
	public String getId() {
		return this.mId;
	}

	/**
	 * Method that returns the default value of the setting.
	 * 
	 * @return Object The default value of the setting
	 */
	public Object getDefaultValue() {
		return this.mDefaultValue;
	}

	/**
	 * Method that returns an instance of {@link CCPPreferenceSettings} from
	 * its. unique identifier
	 * 
	 * @param id
	 *            The unique identifier
	 * @return CCPPreferenceSettings The navigation sort mode
	 */
	public static ECPreferenceSettings fromId(String id) {
		ECPreferenceSettings[] values = values();
		int cc = values.length;
		for (int i = 0; i < cc; i++) {
			if (values[i].mId == id) {
				return values[i];
			}
		}
		return null;
	}
}
