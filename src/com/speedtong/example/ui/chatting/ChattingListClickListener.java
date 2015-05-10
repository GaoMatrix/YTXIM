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

import android.text.TextUtils;
import android.view.View;

import com.speedtong.example.common.utils.MediaPlayTools;
import com.speedtong.example.common.utils.MediaPlayTools.OnVoicePlayCompletionListener;
import com.speedtong.example.storage.ImgInfoSqlManager;
import com.speedtong.example.ui.chatting.mode.ImgInfo;
import com.speedtong.example.ui.chatting.mode.ViewHolderTag;
import com.speedtong.example.ui.chatting.mode.ViewHolderTag.TagType;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.im.ECVoiceMessageBody;
/**
 * 处理聊天消息点击事件响应
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-10
 * @version 4.0
 */
public class ChattingListClickListener implements View.OnClickListener{

	/**聊天界面*/
	private ChattingActivity mContext;
	
	public ChattingListClickListener(ChattingActivity activity , String userName) {
		mContext = activity;
	}
	
	@Override
	public void onClick(View v) {
		ViewHolderTag holder = (ViewHolderTag) v.getTag();
		ECMessage iMessage = holder.detail;
		
		switch (holder.type) {
		case ViewHolderTag.TagType.TAG_VIEW_FILE:
			ECFileMessageBody body = (ECFileMessageBody) holder.detail.getBody();
			CCPAppManager.doViewFilePrevieIntent(mContext, body.getLocalUrl());
			break;

		case ViewHolderTag.TagType.TAG_VOICE:
			if(iMessage == null) {
				return ;
			}
			/*if(iMessage.getReadStatus() != SQLiteManager.IMESSENGER_TYPE_READ) {
				SQLiteManager.getInstance().setIMessageRead(iMessage.getMessageId());
				iMessage.setReadStatus(SQLiteManager.IMESSENGER_TYPE_READ);
			}*/
			
			MediaPlayTools instance = MediaPlayTools.getInstance();
			final ChattingListAdapter adapterForce = mContext.getChattingAdapter();
			if(instance.isPlaying()) {
				instance.stop();
			}
			if(adapterForce.mVoicePosition == holder.position) {
				adapterForce.mVoicePosition = -1;
				adapterForce.notifyDataSetChanged();
				return ;
			}
			
			instance.setOnVoicePlayCompletionListener(new OnVoicePlayCompletionListener() {
				
				@Override
				public void OnVoicePlayCompletion() {
					adapterForce.mVoicePosition = -1;
					adapterForce.notifyDataSetChanged();
				}
			});
			ECVoiceMessageBody voiceBody = (ECVoiceMessageBody) holder.detail.getBody();
			String fileLocalPath = voiceBody.getLocalUrl();
			instance.playVoice(fileLocalPath, false);
			adapterForce.setVoicePosition(holder.position);
			adapterForce.notifyDataSetChanged();

			break;
			
		case TagType.TAG_VIEW_PICTURE:
			if(iMessage != null) {
				ImgInfo imgInfo = ImgInfoSqlManager.getInstance().getImgInfo(iMessage.getMsgId());
				if(imgInfo == null || TextUtils.isEmpty(imgInfo.getBigImgPath()) || TextUtils.isEmpty(imgInfo.getThumbImgPath())) {
					return ;
				}
				String remoteUrl = imgInfo.getBigImgPath();
				ImageMsgInfoEntry imageMsgInfoEntry = new ImageMsgInfoEntry(iMessage.getMsgId(), remoteUrl, imgInfo.getThumbImgPath() ,imgInfo.getBigImgPath());
				CCPAppManager.startChattingImageViewAction(mContext,imageMsgInfoEntry);
				
			}
			break;
			
		case ViewHolderTag.TagType.TAG_RESEND_MSG :
			
			mContext.doResendMsgRetryTips(iMessage, holder.position);
			break;
		default:
			break;
		}
	}



}
