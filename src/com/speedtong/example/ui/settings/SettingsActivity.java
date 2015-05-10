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
package com.speedtong.example.ui.settings;

import java.io.InvalidClassException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedtong.example.R;
import com.speedtong.example.common.dialog.ECAlertDialog;
import com.speedtong.example.common.dialog.ECProgressDialog;
import com.speedtong.example.common.utils.ECPreferenceSettings;
import com.speedtong.example.common.utils.ECPreferences;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.common.view.SettingItem;
import com.speedtong.example.core.ClientUser;
import com.speedtong.example.core.SDKCoreHelper;
import com.speedtong.example.storage.ContactSqlManager;
import com.speedtong.example.ui.ECLauncherUI;
import com.speedtong.example.ui.ECSuperActivity;
import com.speedtong.example.ui.contact.ContactLogic;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;

/**
 * 设置界面/设置新消息提醒（声音或者振动）
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-27
 * @version 4.0
 */
public class SettingsActivity extends ECSuperActivity implements View.OnClickListener{

	private static final String TAG = "ECDemo.SettingsActivity";
	/**头像*/
	private ImageView mPhotoView;
	/**号码*/
	private TextView mUsername;
	/**昵称*/
	private TextView mNumber;
	private SettingItem mSettingSound;
	private SettingItem mSettingShake;
	private SettingItem mSettingExit;
	private SettingItem mSettingSwitch;
	private ECProgressDialog mPostingdialog;
	
	private int mExitType = 0;
	
	private final View.OnClickListener mSettingExitClickListener
		= new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mExitType = 1;
				handleLogout();
			}
		};
		
	private final View.OnClickListener mSettingSwitchClickListener
		= new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ECAlertDialog buildAlert = ECAlertDialog.buildAlert(SettingsActivity.this, R.string.settings_logout_warning_tip, null, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mExitType = 0;
						handleLogout();
					}

				});
				buildAlert.setTitle(R.string.settings_logout);
				buildAlert.show();
			}
		};
		
	/**
	 * 处理退出操作
	 */
	private void handleLogout() {
		mPostingdialog = new ECProgressDialog(this, R.string.posting_logout);
		mPostingdialog.show();
		SDKCoreHelper.logout();
	}
	
	@Override
	protected int getLayoutId() {
		return R.layout.settings_activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
		initActivityState();
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, R.string.app_set, this);
		
		registerReceiver(new String[]{SDKCoreHelper.ACTION_LOGOUT});
	}

	/**
	 * 加载页面布局
	 */
	private void initView() {
		mPhotoView = (ImageView) findViewById(R.id.desc);
		mUsername = (TextView) findViewById(R.id.contact_nameTv);
		mNumber = (TextView) findViewById(R.id.contact_numer);
		
		mSettingSound = (SettingItem) findViewById(R.id.settings_new_msg_sound);
		mSettingSound.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateNewMsgNotification(0);
			}
		});
		mSettingShake = (SettingItem) findViewById(R.id.settings_new_msg_shake);
		mSettingShake.getCheckedTextView().setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updateNewMsgNotification(1);
			}
		});
		mSettingExit = (SettingItem) findViewById(R.id.setting_exit);
		mSettingExit.setOnClickListener(mSettingExitClickListener);
		mSettingSwitch = (SettingItem) findViewById(R.id.setting_switch);
		mSettingSwitch.setOnClickListener(mSettingSwitchClickListener);
	}
	
	@Override
	protected void handleReceiver(Context context, Intent intent) {
		super.handleReceiver(context, intent);
		if(SDKCoreHelper.ACTION_LOGOUT.equals(intent.getAction())) {
			
			try {
				Intent outIntent = new Intent(SettingsActivity.this, ECLauncherUI.class);
				outIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if(mExitType == 1) {
					ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_FULLY_EXIT, true, true);
					startActivity(outIntent);
					finish();
					return ;
				}
				dismissPostingDialog();
				ECDevice.unInitial();
				ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, "", true);
			    startActivity(outIntent);
			    finish();
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initSettings();
	}
	
	/**
	 * 初始化
	 */
	private void initSettings() {
		initNewMsgNotificationSound();
		initNewMsgNotificationShake();
	}
	
	/**
	 * 初始化新消息声音设置参数
	 */
	private void initNewMsgNotificationSound() {
		if(mSettingSound == null) {
			return ;
		}
		mSettingSound.setVisibility(View.VISIBLE);
		boolean shakeSetting = ECPreferences.getSharedPreferences().getBoolean(ECPreferenceSettings.SETTINGS_NEW_MSG_SOUND.getId(),
				(Boolean) ECPreferenceSettings.SETTINGS_NEW_MSG_SOUND.getDefaultValue());
		mSettingSound.setChecked(shakeSetting);
	}
	
	/**
	 * 初始化新消息震动设置参数
	 */
	private void initNewMsgNotificationShake() {
		if(mSettingShake == null) {
			return ;
		}
		mSettingShake.setVisibility(View.VISIBLE);
		boolean shakeSetting = ECPreferences.getSharedPreferences().getBoolean(ECPreferenceSettings.SETTINGS_NEW_MSG_SHAKE.getId(),
				(Boolean) ECPreferenceSettings.SETTINGS_NEW_MSG_SHAKE.getDefaultValue());
		mSettingShake.setChecked(shakeSetting);
	}
	
	/**
	 * 更新状态设置
	 * @param i
	 */
	protected void updateNewMsgNotification(int type) {
		try {
			if(type == 0) {
				if(mSettingSound == null) {
					return ;
				}
				mSettingSound.toggle();
					ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_NEW_MSG_SOUND, mSettingSound.isChecked(), true);
				LogUtil.d(TAG, "com.speedtong.example_new_msg_sound " + mSettingSound.isChecked());
				return ;
			}
			if(type == 1) {
				if(mSettingShake == null) {
					return ;
				}
				mSettingShake.toggle();
					ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_NEW_MSG_SHAKE, mSettingShake.isChecked(), true);
				LogUtil.d(TAG, "com.speedtong.example_new_msg_sound " + mSettingSound.isChecked());
			}
		} catch (InvalidClassException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置页面参数
	 */
	private void initActivityState() {
		ClientUser clientUser = CCPAppManager.getClientUser();
		if(clientUser == null) {
			return ;
		}
		ECContacts contact = ContactSqlManager.getContact(clientUser.getUserId());
		if(contact == null) {
			return ;
		}
		
		mPhotoView.setImageBitmap(ContactLogic.getPhoto(contact.getRemark()));
		mUsername.setText(contact.getNickname());
		mNumber.setText(contact.getContactid());
		
	}
	
	/**
	 * 关闭对话框
	 */
	private void dismissPostingDialog() {
		if(mPostingdialog == null || !mPostingdialog.isShowing()) {
			return ;
		}
		mPostingdialog.dismiss();
		mPostingdialog = null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;

		default:
			break;
		}
	}

}
