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
package com.speedtong.example.ui.chatting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.common.utils.ECNotificationManager;
import com.speedtong.example.common.utils.ECPreferenceSettings;
import com.speedtong.example.common.utils.ECPreferences;
import com.speedtong.example.common.utils.FileAccessor;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.core.SDKCoreHelper;
import com.speedtong.example.storage.ContactSqlManager;
import com.speedtong.example.storage.GroupNoticeSqlManager;
import com.speedtong.example.storage.IMessageSqlManager;
import com.speedtong.example.storage.ImgInfoSqlManager;
import com.speedtong.example.ui.chatting.mode.ImgInfo;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.example.ui.group.GroupNoticeHelper;
import com.speedtong.example.ui.group.GroupNoticeHelper.OnPushGroupNoticeMessageListener;
import com.speedtong.example.ui.group.NoticeSystemMessage;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.example.ui.plugin.FileUtils;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.OnChatReceiveListener;
import com.speedtong.sdk.ECChatManager;
import com.speedtong.sdk.ECChatManager.OnDownloadMessageListener;
import com.speedtong.sdk.ECChatManager.OnSendMessageListener;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.im.ECReport;
import com.speedtong.sdk.im.ECTextMessageBody;
import com.speedtong.sdk.im.ECVoiceMessageBody;
import com.speedtong.sdk.im.ECMessage.Type;
import com.speedtong.sdk.im.group.ECGroupNotice;
import com.speedtong.sdk.platformtools.VoiceUtil;

/**
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-12
 * @version 4.0
 */
public class IMChattingHelper implements OnChatReceiveListener , OnDownloadMessageListener{

	private static final String TAG = "ECSDK_Demo.IMChattingHelper"; 
	private static HashMap<String, ECMessage> syncMessage = new HashMap<String, ECMessage>();
	private static IMChattingHelper sInstance;
	public static IMChattingHelper getInstance(){
		if(sInstance == null) {
			sInstance = new IMChattingHelper();
		}
		return sInstance;
	}
	
	/**云通讯SDK聊天功能接口*/
	private ECChatManager mChatManager;
	/**全局处理所有的IM消息发送回调*/
	private ChatManagerListener mListener;
	private IMChattingHelper() {
		mChatManager = SDKCoreHelper.getECChatManager();
		mListener = new ChatManagerListener();
	}
	
	/**
	 * 消息发送报告
	 */
	private OnMessageReportCallback mOnMessageReportCallback;
	
	/**
	 * 发送ECMessage 消息
	 * @param msg
	 */
	public static long sendECMessage(ECMessage msg) {
		// 获取一个聊天管理器
		ECChatManager manager = getInstance().mChatManager;
		if(manager != null) {
			// 调用接口发送IM消息
			manager.sendMessage(msg, getInstance().mListener);
			// 保存发送的消息到数据库
			return IMessageSqlManager.insertIMessage(msg, ECMessage.Direction.SEND.ordinal());
		}
		return -1;
	}
	
	/**
	 * 消息重发
	 * @param msg
	 * @return
	 */
	public static long reSendECMessage(ECMessage msg) {
		ECChatManager manager = getInstance().mChatManager;
		if(manager != null) {
			// 调用接口发送IM消息
			String oldMsgId = msg.getMsgId();
			manager.sendMessage(msg, getInstance().mListener);
			if(msg.getType() == ECMessage.Type.IMAGE) {
				ImgInfo imgInfo = ImgInfoSqlManager.getInstance().getImgInfo(oldMsgId);
				if(imgInfo == null) {
					return -1;
				}
				String bigImagePath = new File(FileAccessor.getImagePathName(), imgInfo.getBigImgPath()).getAbsolutePath();
				imgInfo.setMsglocalid(msg.getMsgId());
				ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
				body.setLocalUrl(bigImagePath);
				BitmapFactory.Options options = DemoUtils.getBitmapOptions(new File(FileAccessor.IMESSAGE_IMAGE, imgInfo.getThumbImgPath()).getAbsolutePath());
				msg.setUserData("outWidth://" + options.outWidth + ",outHeight://" + options.outHeight + ",THUMBNAIL://" + msg.getMsgId());
				ImgInfoSqlManager.getInstance().updateImageInfo(imgInfo);
			}
			// 保存发送的消息到数据库
			return IMessageSqlManager.changeResendMsg(msg.getId(), msg);
		}
		return -1;
	}
	
	public static long sendImageMessage(ImgInfo imgInfo , ECMessage message) {
		
		ECChatManager manager = getInstance().mChatManager;
		if(manager != null) {
			// 调用接口发送IM消息
			manager.sendMessage(message, getInstance().mListener);
			
			if(TextUtils.isEmpty(message.getMsgId())) {
				return -1;
			}
			imgInfo.setMsglocalid(message.getMsgId());
			BitmapFactory.Options options = DemoUtils.getBitmapOptions(new File(FileAccessor.IMESSAGE_IMAGE, imgInfo.getThumbImgPath()).getAbsolutePath());
			message.setUserData("outWidth://" + options.outWidth + ",outHeight://" + options.outHeight + ",THUMBNAIL://" + message.getMsgId());
			long row = IMessageSqlManager.insertIMessage(message, ECMessage.Direction.SEND.ordinal());
			if(row != -1) {
				return ImgInfoSqlManager.getInstance().insertImageInfo(imgInfo);
			}
		}
		return -1;
		
	}
	
	
	private class ChatManagerListener implements OnSendMessageListener {

		@Override
		public void onComplete(ECError error) {
			
		}

		@Override
		public void onSendMessageComplete(ECError error, ECMessage message) {
			if(message == null) {
				return ;
			}
			// 处理ECMessage的发送状态
			if(message != null) {
				IMessageSqlManager.setIMessageSendStatus(message.getMsgId(), message.getMsgStatus().ordinal());
				IMessageSqlManager.notifyMsgChanged(message.getSessionId());
				if(mOnMessageReportCallback != null) {
					mOnMessageReportCallback.onMessageReport(message);
				}
				return ;
			}
		}

		@Override
		public void onProgress(int total, int progress) {
			// 处理发送文件IM消息的时候进度回调
		}
		
	}
	
	
	public static void setOnMessageReportCallback(OnMessageReportCallback callback) {
		getInstance().mOnMessageReportCallback = callback;
	}
	
	
	public interface OnMessageReportCallback{
		void onMessageReport(ECMessage message);
		void onPushMessage(String sessionId ,List<ECMessage> msgs);
	}


	@Override
	public void OnReceivedReport(ECReport report) {
		// 接收到的消息的发送报告
	}


	/**
	 * 收到新的IM文本和附件消息
	 */
	@Override
	public void OnReceivedMessage(ECMessage msg) {
		// 接收到的IM消息，根据IM消息类型做不同的处理
	    // IM消息类型：ECMessage.Type
		if(msg.getType() != ECMessage.Type.TXT) {
			ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
			if(!TextUtils.isEmpty(body.getRemoteUrl())) {
				boolean thumbnail = false;
				if(body.getRemoteUrl().endsWith("amr")) {
					msg.setType(ECMessage.Type.VOICE);
					ECVoiceMessageBody messageBody = new ECVoiceMessageBody(body.getFileName(), body.getRemoteUrl(), body.getLength());
					messageBody.setChunk(body.isChunk());
					messageBody.setDownloaded(body.isDownloaded());
					if(!FileAccessor.getVoicePathName().exists()) {
						FileAccessor.getVoicePathName().mkdirs();
					}
					messageBody.setLocalUrl(new File(FileAccessor.getVoicePathName() , VoiceUtil.md5(String.valueOf(System.currentTimeMillis())) + ".amr").getAbsolutePath());
					msg.setBody(messageBody);
				} else {
					if(!FileAccessor.getFilePathName().exists()) {
						FileAccessor.getFilePathName().mkdirs();
					}
					ECFileMessageBody fileBody = (ECFileMessageBody) msg.getBody();
					String fileExt = DemoUtils.getExtensionName(fileBody.getRemoteUrl());
					if(FileUtils.isPic(fileBody.getRemoteUrl())) {
						msg.setType(ECMessage.Type.IMAGE);
						String url = fileBody.getThumbnailFileUrl();
						thumbnail = true;
						if(TextUtils.isEmpty(fileBody.getThumbnailFileUrl())) {
							url = fileBody.getRemoteUrl();
							thumbnail = false;
						}
						fileBody.setLocalUrl(new File(FileAccessor.getImagePathName() , VoiceUtil.md5(url) + "." + fileExt).getAbsolutePath());
					} else {
						msg.setType(ECMessage.Type.FILE);
						fileBody.setLocalUrl(new File(FileAccessor.getFilePathName() , VoiceUtil.md5(String.valueOf(System.currentTimeMillis())) + "." + fileExt).getAbsolutePath());
					}
					
				}
				if(syncMessage != null) {
					syncMessage.put(msg.getSessionId(), msg);
				}
				if(thumbnail) {
					mChatManager.downloadThumbnailMessage(msg, this);
				} else {
					mChatManager.downloadMediaMessage(msg, this);
				}
				if(IMessageSqlManager.insertIMessage(msg, msg.getDirection().ordinal()) > 0) {
					return ;
				}
			} else {
				LogUtil.e(TAG, "ECMessage fileUrl: null");
			}
		}
		if(IMessageSqlManager.insertIMessage(msg, msg.getDirection().ordinal()) <= 0) {
			return ;
		}
		
		if(mOnMessageReportCallback != null) {
			ArrayList<ECMessage> msgs = new ArrayList<ECMessage>();
			msgs.add(msg);
			mOnMessageReportCallback.onPushMessage(msg.getSessionId() ,msgs);
		}
		showNotification(msg);
		
	}
	
	private static void showNotification(ECMessage msg) {
		if(checkNeedNotification(msg.getSessionId())) {
			ECNotificationManager.getInstance().forceCancelNotification();
			String lastMsg = "";
			if(msg.getType() == Type.TXT) {
				lastMsg = ((ECTextMessageBody) msg.getBody()).getMessage();
			}
			ECContacts contact = ContactSqlManager.getContact(msg.getForm());
			ECNotificationManager.getInstance().showCustomNewMessageNotification(CCPAppManager.getContext(),
					lastMsg,
					contact.getNickname(),
					msg.getType().ordinal());
		}
	}
	
	/**
	 * 是否需要状态栏通知
	 * @param contactId
	 */
	public static boolean checkNeedNotification(String contactId) {
		String currentChattingContactId = ECPreferences.getSharedPreferences().getString(
				ECPreferenceSettings.SETTING_CHATTING_CONTACTID.getId(), 
				(String) ECPreferenceSettings.SETTING_CHATTING_CONTACTID.getDefaultValue());
		
		if(contactId.equals(currentChattingContactId)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 群组通知消息
	 */
	@Override
	public void OnReceiveGroupNoticeMessage(ECGroupNotice notice) {
		// 接收到的群组消息，根据群组消息类型做不同处理
	    // 群组消息类型：ECGroupMessageType
		if(notice == null) {
			return ;
		}
		GroupNoticeHelper.handleGroupNoticeMessage(notice , new OnPushGroupNoticeMessageListener() {
			
			@Override
			public void onPushGroupNoticeMessage(NoticeSystemMessage system) {
				IMessageSqlManager.notifyMsgChanged(GroupNoticeSqlManager.CONTACT_ID);
			}
		});
	}

	/**
	 * 下载
	 */
	@Override
	public void onDownloadMessageComplete(ECError e, ECMessage message) {
		if(message == null) {
			return ;
		}
		if(message.getType() == ECMessage.Type.VOICE) {
			ECVoiceMessageBody voiceBody = (ECVoiceMessageBody) message.getBody();
			voiceBody.setDuration(DemoUtils.calculateVoiceTime(voiceBody.getLocalUrl()));
		} else if (message.getType() == ECMessage.Type.IMAGE){
			ECFileMessageBody fileMessageBody =  (ECFileMessageBody) message.getBody();
			ImgInfo thumbImgInfo = ImgInfoSqlManager.getInstance().getThumbImgInfo(message);
			ImgInfoSqlManager.getInstance().insertImageInfo(thumbImgInfo);
			BitmapFactory.Options options = DemoUtils.getBitmapOptions(fileMessageBody.getLocalUrl());
			message.setUserData("outWidth://" + options.outWidth + ",outHeight://" + options.outHeight + ",THUMBNAIL://" + message.getMsgId());
		}
		if(IMessageSqlManager.updateIMessageDownload(message) <= 0) {
			return ;
		}
		if(mOnMessageReportCallback != null) {
			mOnMessageReportCallback.onMessageReport(message);
		}
		if(mOnMessageReportCallback != null ) {
			ECMessage remove = syncMessage.remove(message.getSessionId());
			if( remove!= null) {
				ArrayList<ECMessage> msgs = new ArrayList<ECMessage>();
				msgs.add(remove);
				mOnMessageReportCallback.onPushMessage(remove.getSessionId() ,msgs);
			}
		}
		showNotification(message);
	}

}
