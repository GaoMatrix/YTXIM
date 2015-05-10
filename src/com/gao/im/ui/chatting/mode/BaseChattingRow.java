/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.gao.im.ui.chatting.mode;

import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;

import com.gao.im.R;
import com.gao.im.common.utils.LogUtil;
import com.gao.im.storage.ContactSqlManager;
import com.gao.im.ui.chatting.ChattingActivity;
import com.gao.im.ui.chatting.holder.BaseHolder;
import com.gao.im.ui.contact.ContactLogic;
import com.gao.im.ui.contact.ECContacts;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.im.ECMessage.MessageStatus;


/**
 * <p>Title: BaseChattingRow.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2014-4-17
 * @version 1.0
 */
public abstract class BaseChattingRow implements IChattingRow {
	
	public static final String TAG = LogUtil.getLogUtilsTag(BaseChattingRow.class);
	private HashMap<String, String> hashMap = new HashMap<String, String>();
	int mRowType;
	
	public BaseChattingRow(int  type) {
		mRowType = type;
	}
	
	/**
	 * 处理消息的发送状态设置
	 * @param position 消息的列表所在位置
	 * @param holder 消息ViewHolder
	 * @param detail
	 * @param l
	 */
	protected static void getMsgStateResId(int position , BaseHolder holder , ECMessage msg , View.OnClickListener l){
		if(msg != null && msg.getDirection() == ECMessage.Direction.SEND) {
			MessageStatus msgStatus = msg.getMsgStatus();
			if(msgStatus == MessageStatus.FAILED) {
				holder.getUploadState().setImageResource(R.drawable.msg_state_failed_resend);
				holder.getUploadState().setVisibility(View.VISIBLE);
				if(holder.getUploadProgressBar() != null) {
					holder.getUploadProgressBar().setVisibility(View.GONE);
				}
			} else  if (msgStatus == MessageStatus.SUCCESS) {
				holder.getUploadState().setImageResource(0);
				holder.getUploadState().setVisibility(View.GONE);
				if(holder.getUploadProgressBar() != null) {
					holder.getUploadProgressBar().setVisibility(View.GONE);
				}
				
			} else  if (msgStatus == MessageStatus.SENDING) {
				holder.getUploadState().setImageResource(0);
				holder.getUploadState().setVisibility(View.GONE);
				if(holder.getUploadProgressBar() != null) {
					holder.getUploadProgressBar().setVisibility(View.VISIBLE);
				}
				
			} else {
				if(holder.getUploadProgressBar() != null) {
					holder.getUploadProgressBar().setVisibility(View.GONE);
				}
				LogUtil.d(TAG, "getMsgStateResId: not found this state");
			}
			
			
			ViewHolderTag holderTag = ViewHolderTag.createTag(msg, ViewHolderTag.TagType.TAG_RESEND_MSG , position);
			holder.getUploadState().setTag(holderTag);
			holder.getUploadState().setOnClickListener(l);
		}
	}
	
	/**
	 * 
	 * @param contextMenu
	 * @param targetView
	 * @param detail
	 * @return
	 */
	public abstract boolean onCreateRowContextMenu(ContextMenu contextMenu , View targetView , ECMessage detail);
	
	
	/**
	 * 
	 * @param baseHolder
	 * @param displayName
	 */
	public static void setDisplayName(BaseHolder baseHolder , String displayName) {
		if(baseHolder == null || baseHolder.getChattingUser() == null) {
			return ;
		}
		
		if(TextUtils.isEmpty(displayName)) {
			baseHolder.getChattingUser().setVisibility(View.GONE);
			return ;
		}
		baseHolder.getChattingUser().setText(displayName);
		baseHolder.getChattingUser().setVisibility(View.VISIBLE);
	}
	
	protected abstract void buildChattingData(Context context , BaseHolder baseHolder , ECMessage detail , int position);
	
	@Override
	public void buildChattingBaseData(Context context, BaseHolder baseHolder, ECMessage detail, int position) {
		
		// 处理其他使用逻辑
		buildChattingData(context, baseHolder, detail, position);
		setContactPhoto(baseHolder , detail);
		if(((ChattingActivity) context).isPeerChat() && detail.getDirection() == ECMessage.Direction.RECEIVE) {
			ECContacts contact = ContactSqlManager.getContact(detail.getForm());
			setDisplayName(baseHolder, contact.getNickname());
		}
	}

	/**
	 * 添加用户头像
	 * @param baseHolder
	 * @param detail
	 */
	private void setContactPhoto(BaseHolder baseHolder, ECMessage detail) {
		if(baseHolder.getChattingAvatar() != null) {
			try {
				if (TextUtils.isEmpty(detail.getForm())) {
					return;
				}
				String userUin = "";
				if (hashMap.containsKey(detail.getForm())) {
					userUin = hashMap.get(detail.getForm());
				} else {
					userUin = ContactSqlManager.getContact(detail.getForm())
							.getRemark();
				}
				baseHolder.getChattingAvatar().setImageBitmap(
						ContactLogic.getPhoto(userUin));
			} catch (Exception e) {
			}
		}
	}
	 
}
