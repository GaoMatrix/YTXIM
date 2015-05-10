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
package com.speedtong.example.ui;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedtong.example.R;
import com.speedtong.example.common.base.CCPTextView;
import com.speedtong.example.common.utils.DateUtil;
import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.storage.ContactSqlManager;
import com.speedtong.example.storage.ConversationSqlManager;
import com.speedtong.example.storage.GroupMemberSqlManager;
import com.speedtong.example.storage.GroupNoticeSqlManager;
import com.speedtong.example.ui.chatting.mode.Conversation;
import com.speedtong.example.ui.contact.ContactLogic;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.example.ui.group.GroupNoticeHelper;
import com.speedtong.sdk.im.ECMessage;

/**
 * @author 容联•云通讯
 * @date 2014-12-8
 * @version 4.0
 */
public class ConversationAdapter extends CCPListAdapter <Conversation> {

	private OnListAdapterCallBackListener mCallBackListener;
	int padding;
	/**
	 * @param ctx
	 */
	public ConversationAdapter(Context ctx , OnListAdapterCallBackListener listener) {
		super(ctx, new Conversation());
		mCallBackListener = listener;
		padding = ctx.getResources().getDimensionPixelSize(R.dimen.OneDPPadding);
	}

	@Override
	protected void initCursor() {
		notifyChange();
	}

	@Override
	protected Conversation getItem(Conversation t, Cursor cursor) {
		Conversation conversation = new Conversation();
		conversation.setCursor(cursor);
		if(conversation.getUsername() != null && conversation.getUsername().endsWith("@priategroup.com")) {
			ArrayList<String> member = GroupMemberSqlManager.getGroupMemberID(conversation.getSessionId());
			if(member != null) {
				ArrayList<String> contactName = ContactSqlManager.getContactName(member.toArray(new String[]{}));
				String chatroomName = DemoUtils.listToString(contactName, ",");
				conversation.setUsername(chatroomName);
			}
		}
		return conversation;
	}

	/**
	 * 会话时间
	 * @param conversation
	 * @return
	 */
	protected final CharSequence getConversationTime(Conversation conversation) {
		return DateUtil.getDateString(conversation.getDateTime(),
				DateUtil.SHOW_TYPE_CALL_LOG).trim();
	}
	
	/**
	 * 根据消息类型返回相应的主题描述
	 * @param conversation
	 * @return
	 */
	protected final CharSequence getConversationSnippet(Conversation conversation) {
		if(conversation == null) {
			return "";
		}
		if(GroupNoticeSqlManager.CONTACT_ID.equals(conversation.getSessionId())) {
			return GroupNoticeHelper.getNoticeContent(conversation.getContent());
		}
		if(conversation.getMsgType() == ECMessage.Type.VOICE.ordinal()) {
			return mContext.getString(R.string.app_voice);
		} else if(conversation.getMsgType() == ECMessage.Type.FILE.ordinal()) {
			return mContext.getString(R.string.app_file);
		} else if(conversation.getMsgType() == ECMessage.Type.IMAGE.ordinal()) {
			return mContext.getString(R.string.app_pic);
		} else if(conversation.getMsgType() == ECMessage.Type.VIDEO.ordinal()) {
			return mContext.getString(R.string.app_video);
		}
		return conversation.getContent();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = null;
		ViewHolder mViewHolder = null;
		if(convertView == null || convertView.getTag() == null) {
			view = View.inflate(mContext , R.layout.conversation_item, null);

			mViewHolder = new ViewHolder();
			mViewHolder.user_avatar = (ImageView) view.findViewById(R.id.avatar_iv);
			mViewHolder.prospect_iv = (ImageView) view.findViewById(R.id.avatar_prospect_iv);
			mViewHolder.nickname_tv = (TextView) view.findViewById(R.id.nickname_tv);
			mViewHolder.tipcnt_tv = (TextView) view.findViewById(R.id.tipcnt_tv);
			mViewHolder.update_time_tv = (TextView) view.findViewById(R.id.update_time_tv);
			mViewHolder.last_msg_tv = (CCPTextView) view.findViewById(R.id.last_msg_tv);
			mViewHolder.image_input_text = (ImageView) view.findViewById(R.id.image_input_text);
			view.setTag(mViewHolder);
		} else {
			view = convertView;
			mViewHolder = (ViewHolder) view.getTag();
		}
		
		Conversation conversation = getItem(position);
		if(conversation != null) {
			mViewHolder.nickname_tv.setText(conversation.getUsername());
			mViewHolder.last_msg_tv.setEmojiText(getConversationSnippet(conversation));
			
			mViewHolder.tipcnt_tv.setText(conversation.getUnreadCount() + "");
			mViewHolder.tipcnt_tv.setVisibility(conversation.getUnreadCount() == 0 ? View.GONE : View.VISIBLE);
			mViewHolder.image_input_text.setVisibility(View.GONE);
			mViewHolder.update_time_tv.setText(getConversationTime(conversation));
			if(conversation.getSessionId().startsWith("g")) {
				Bitmap bitmap = ContactLogic.getChatroomPhoto(conversation.getSessionId());
				if(bitmap != null) {
					mViewHolder.user_avatar.setImageBitmap(bitmap);
					mViewHolder.user_avatar.setPadding(padding, padding, padding, padding);
				} else {
					mViewHolder.user_avatar.setImageResource(R.drawable.group_head);
					mViewHolder.user_avatar.setPadding(0, 0, 0, 0);
				}
			} else {
				ECContacts contact = ContactSqlManager.getContact(conversation.getSessionId());
				mViewHolder.user_avatar.setImageBitmap(ContactLogic.getPhoto(contact.getRemark()));
			}
		}
		
		return view;
	}
	
	static class ViewHolder {
		ImageView user_avatar;
		TextView tipcnt_tv;
		ImageView prospect_iv;
		TextView nickname_tv;
		TextView update_time_tv;
		CCPTextView last_msg_tv;
		ImageView image_input_text;
	}

	@Override
	protected void notifyChange() {
		if(mCallBackListener != null) {
			mCallBackListener.OnListAdapterCallBack();
		}
		Cursor cursor = ConversationSqlManager.getConversationCursor();
		setCursor(cursor);
		super.notifyDataSetChanged();
	}
	

}
