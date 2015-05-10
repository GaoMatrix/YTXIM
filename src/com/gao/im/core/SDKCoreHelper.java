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
package com.gao.im.core;

import android.content.Context;
import android.content.Intent;

import com.gao.im.storage.ContactSqlManager;
import com.gao.im.storage.ConversationSqlManager;
import com.gao.im.storage.GroupMemberSqlManager;
import com.gao.im.storage.GroupNoticeSqlManager;
import com.gao.im.storage.GroupSqlManager;
import com.gao.im.storage.IMessageSqlManager;
import com.gao.im.storage.ImgInfoSqlManager;
import com.gao.im.ui.ECLauncherUI;
import com.gao.im.ui.chatting.IMChattingHelper;
import com.gao.im.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECInitialize;
import com.speedtong.sdk.ECChatManager;
import com.speedtong.sdk.ECGroupManager;

/**
 * @author 容联•云通讯
 * @date 2014-12-8
 * @version 4.0
 */
public class SDKCoreHelper implements ECDevice.InitListener,
		ECDevice.OnECDeviceConnectListener ,ECDevice.OnLogoutListener{

	public static final String ACTION_LOGOUT = "com.gao.im.ECDemo_logout";
	private static SDKCoreHelper sInstance;
	private Context mContext;
	private ECInitialize params;
	private Connect mConnect = Connect.ERROR;
	private SDKCoreHelper() {

	}
	
	public static Connect getConnectState() {
		return getInstance().mConnect;
	}

	private static SDKCoreHelper getInstance() {
		if (sInstance == null) {
			sInstance = new SDKCoreHelper();
		}
		return sInstance;
	}

	public static void init(Context ctx) {
		getInstance().mContext = ctx;
		if(!ECDevice.isInitialized()) {
			getInstance().mConnect = Connect.CONNECTING;
			ECDevice.initial(ctx, getInstance());
			postConnectNotify();
		}
	}

	@Override
	public void onInitialized() {
		if (params == null || params.getInitializeParams() == null || params.getInitializeParams().isEmpty()){
			ClientUser clientUser = CCPAppManager.getClientUser();
			params = new ECInitialize();
			params.setServerIP("sandboxapp.cloopen.com");
			params.setServerPort(8883);
			params.setSid(clientUser.getUserId());
			params.setSidToken(clientUser.getUserToken());
			params.setSubId(clientUser.getSubSid());
			params.setSubToken(clientUser.getSubToken());
			
			params.setOnChatReceiveListener(IMChattingHelper.getInstance());
		}
		
		// 设置SDK注册结果回调通知，当第一次初始化注册成功或者失败会通过该引用回调
		// 通知应用SDK注册状态
		
		// 当网络断开导致SDK断开连接或者重连成功也会通过该设置回调
		params.setOnECDeviceConnectListener(this);
		ECDevice.login(params);
	}

	@Override
	public void onError(Exception exception) {
		ECDevice.unInitial();
	}
	
	@Override
	public void onConnect() {
		// SDK与云通讯平台连接成功
		getInstance().mConnect = Connect.SUCCESS;
		Intent intent = new Intent();
		intent.setAction(mContext.getPackageName()+".inited");
		mContext.sendBroadcast(intent);
		postConnectNotify();
	}

	@Override
	public void onDisconnect(ECError error) {
		// SDK与云通讯平台断开连接
		getInstance().mConnect = Connect.ERROR;
		postConnectNotify();
	}
	
	public static void logout() {
		ECDevice.logout(getInstance());
		release();
	}
	
	/**
	 * IM聊天功能接口
	 * @return
	 */
	public static ECChatManager getECChatManager() {
		return ECDevice.getECChatManager();
	}
	
	/**
	 * 状态通知
	 */
	private static void postConnectNotify() {
		if(getInstance().mContext instanceof ECLauncherUI) {
			((ECLauncherUI) getInstance().mContext).onNetWorkNotify(getConnectState());
		}
	}
	
	/**
	 * 群组聊天接口
	 * @return
	 */
	public static ECGroupManager getECGroupManager() {
		return ECDevice.getECGroupManager();
	}

	@Override
	public void onLogout() {
		if(params != null && params.getInitializeParams() != null) {
			params.getInitializeParams().clear();
		}
		params = null;
		mContext.sendBroadcast(new Intent(ACTION_LOGOUT));
	}
	
	public enum Connect {
		ERROR , CONNECTING , SUCCESS
	}
	
	public static void release() {
		ContactSqlManager.reset();
		ConversationSqlManager.reset();
		GroupMemberSqlManager.reset();
		GroupNoticeSqlManager.reset();
		GroupSqlManager.reset();
		IMessageSqlManager.reset();
		ImgInfoSqlManager.reset();
	}
}
