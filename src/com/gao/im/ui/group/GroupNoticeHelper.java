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
package com.gao.im.ui.group;

import com.gao.im.storage.ContactSqlManager;
import com.gao.im.storage.GroupNoticeSqlManager;
import com.gao.im.storage.GroupSqlManager;
import com.gao.im.storage.IMessageSqlManager;
import com.gao.im.ui.contact.ECContacts;
import com.speedtong.sdk.im.ECGroup;
import com.speedtong.sdk.im.group.ECGroupNotice;
import com.speedtong.sdk.im.group.ECGroupNotice.ECGroupMessageType;
import com.speedtong.sdk.im.group.IMGroupDismissMsg;
import com.speedtong.sdk.im.group.IMInviterJoinGroupReplyMsg;
import com.speedtong.sdk.im.group.IMInviterMsg;
import com.speedtong.sdk.im.group.IMProposerMsg;
import com.speedtong.sdk.im.group.IMQuitGroupMsg;
import com.speedtong.sdk.im.group.IMRemoveMemeberMsg;
import com.speedtong.sdk.im.group.IMReplyGroupApplyMsg;

/**
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-31
 * @version 4.0
 */
public class GroupNoticeHelper {

	/**群组通知接口*/
	private OnPushGroupNoticeMessageListener mListener;
	
	private static GroupNoticeHelper mHelper;
	private static GroupNoticeHelper getHelper() {
		if(mHelper == null) {
			mHelper = new GroupNoticeHelper();
		}
		return mHelper;
	} 
	
	/**
	 * 处理群组通知消息
	 * @param notice
	 */
	public static void handleGroupNoticeMessage(ECGroupNotice notice , OnPushGroupNoticeMessageListener l) {
		ECGroupMessageType type = notice.getType();
		NoticeSystemMessage message = null;
		if(type == ECGroupMessageType.APPLY_JOIN) {
			// 群组受到有人申请加入群组
			message = getHelper().onIMProposerMsg((IMProposerMsg) notice);
		} else if (type == ECGroupMessageType.REPLY_GROUP_APPLY) {
			// 群组管理员通过或拒绝用户加入群组申请
			message = getHelper().onIMReplyGroupApplyMsg((IMReplyGroupApplyMsg) notice);
		} else if (type == ECGroupMessageType.INVITE) {
			// 群组管理员邀请用户加入群组 - 
			message = getHelper().onIMInviterMsg((IMInviterMsg) notice);
		} else if (type == ECGroupMessageType.REMOVEMEMBER) {
			// 群组管理员删除成员
			message = getHelper().onIMRemoveMemeberMsg((IMRemoveMemeberMsg) notice);
		} else if (type == ECGroupMessageType.QUIT) {
			// 群组成员主动退出群组 
			message = getHelper().onIMQuitGroupMsg((IMQuitGroupMsg) notice);
		} else if (type == ECGroupMessageType.DISMISS) {
			// 删除群组（解散群组）
			message = getHelper().onIMGroupDismissMsg((IMGroupDismissMsg) notice);
		} else if (type == ECGroupMessageType.JOIN) {

		} else if (type == ECGroupMessageType.REPLY_INVITE) {
			// 用户通过或拒绝群组管理员邀请加入群组的申请
			message =getHelper().onIMInviterJoinGroupReplyMsg((IMInviterJoinGroupReplyMsg) notice);
		}
		
		if(message != null) {
			GroupNoticeSqlManager.insertNoticeMsg(message);
			getHelper().notify(message);
			if(l != null) {
				l.onPushGroupNoticeMessage(message);
			}
		}
	}

	/**
	 * 用户申请加入群组 - PUSH到群组管理员
	 * @param notice
	 */
	private NoticeSystemMessage onIMProposerMsg(IMProposerMsg notice) {
		NoticeSystemMessage systemMessage = createNoticeSystemMessage(notice);
		systemMessage.setMember(notice.getProposer());
		systemMessage.setContent("<member>"+notice.getProposer()+ "</member> 加入了群组");
		systemMessage.setDateCreated(notice.getDateCreated());
		
		GroupService.syncGroupInfo(notice.getGroupId());
		return systemMessage;
		
	}

	/**
	 * 群组管理员通过或拒绝用户加入群组申请
	 * @param notice
	 */
	private NoticeSystemMessage onIMReplyGroupApplyMsg(IMReplyGroupApplyMsg notice) {
		NoticeSystemMessage systemMessage = createNoticeSystemMessage(notice);
		systemMessage.setAdmin(notice.getAdmin());
		systemMessage.setConfirm(notice.getConfirm());
		if(notice.getConfirm() == 0) {
			systemMessage.setContent("群管理员<admin>"+notice.getAdmin()+ "</admin>拒绝<member>"+notice.getMember()+ "</member>加入群组申请");
		} else {
			systemMessage.setContent("群管理员<admin>"+notice.getAdmin()+ "</admin>通过<member>"+notice.getMember()+ "</member>加入群组申请");
		}
		systemMessage.setMember(notice.getMember());
		return systemMessage;
	}

	/**
	 * 群组管理员邀请用户加入群组 - 
	 * PUSH到被邀请的用户
	 * @param notice
	 */
	private NoticeSystemMessage onIMInviterMsg(IMInviterMsg notice) {
		NoticeSystemMessage systemMessage = createNoticeSystemMessage(notice);
		systemMessage.setAdmin(notice.getAdmin());
		systemMessage.setContent("群管理员<admin>"+notice.getAdmin()+ "</admin>邀请您加入群组");
		systemMessage.setConfirm(notice.getConfirm());
		return systemMessage;
	}

	/**
	 * 群组管理员删除成员
	 * @param notice
	 */
	private NoticeSystemMessage onIMRemoveMemeberMsg(IMRemoveMemeberMsg notice) {
		NoticeSystemMessage systemMessage = createNoticeSystemMessage(notice);
		systemMessage.setMember(notice.getMember());
		systemMessage.setContent("<member>"+notice.getMember()+ "</member>被移除出群组 " + "<groupId>"+notice.getGroupId()+"</groupId>");
		systemMessage.setGroupId(notice.getGroupId());
		return systemMessage;
	}

	/**
	 * 群组成员主动退出群组 
	 * - PUSH到所有用户
	 * @param notice
	 */
	private NoticeSystemMessage onIMQuitGroupMsg(IMQuitGroupMsg notice) {
		NoticeSystemMessage systemMessage = createNoticeSystemMessage(notice);
		systemMessage.setContent("群成员<member>"+notice.getMember()+ "</member>退出了群组 " + "<groupId>"+notice.getGroupId()+"</groupId>");
		systemMessage.setMember(notice.getMember());
		return systemMessage;
	}

	/**
	 * 删除群组（解散群组）
	 * - PUSH到群组的所有用户
	 * @param notice
	 */
	private NoticeSystemMessage onIMGroupDismissMsg(IMGroupDismissMsg notice) {
		NoticeSystemMessage systemMessage = createNoticeSystemMessage(notice);
		systemMessage.setContent("群组被解散");
		systemMessage.setGroupId(notice.getGroupId());
		return systemMessage;
	}

	/**
	 * 用户通过或拒绝群组管理员邀请加入群组的申请
	 *  – 通过PUSH到所有用户，拒绝PUSH到群组管理员
	 * @param notice
	 */
	private NoticeSystemMessage onIMInviterJoinGroupReplyMsg(
			IMInviterJoinGroupReplyMsg notice) {
		return null;
	}
	
	/**
	 * 生成群组通知消息
	 * @return
	 */
	private NoticeSystemMessage createNoticeSystemMessage(ECGroupNotice notice) {
		NoticeSystemMessage message = new NoticeSystemMessage(notice.getType());
		message.setGroupId(notice.getGroupId());
		message.setIsRead(IMessageSqlManager.IMESSENGER_TYPE_UNREAD);
		return message;
	}
	
	public static void addListener(OnPushGroupNoticeMessageListener listener) {
		getHelper().mListener = listener;
	}
	
	/**
	 * @param content
	 * @return
	 */
	public static CharSequence getNoticeContent(String content) {
		if(content == null) {
			return content;
		}
		if(content.indexOf("<admin>") != -1 && content.indexOf("</admin>") != -1) {
			int start = content.indexOf("<admin>");
			int end = content.indexOf("</admin>");
			String contactId = content.substring(start + "<admin>".length(), end);
			ECContacts contact = ContactSqlManager.getContact(contactId);
			String target = content.substring(start, end + "</admin>".length());
			content = content.replace(target, contact.getNickname());
		} 
		if(content.indexOf("<member>") != -1 && content.indexOf("</member>") != -1) {
			int start = content.indexOf("<member>");
			int end = content.indexOf("</member>");
			String member = content.substring(start + "<member>".length(), end);
			ECContacts contact = ContactSqlManager.getContact(member);
			String target = content.substring(start, end + "</member>".length());
			content = content.replace(target, contact.getNickname());
		}
		if(content.indexOf("<groupId>") != -1 && content.indexOf("</groupId>") != -1) {
			int start = content.indexOf("<groupId>");
			int end = content.indexOf("</groupId>");
			String groupId = content.substring(start + "<groupId>".length(), end);
			ECGroup ecGroup = GroupSqlManager.getECGroup(groupId);
			String target = content.substring(start, end + "</groupId>".length());
			if(ecGroup == null) {
				GroupService.syncGroupInfo(groupId);
			}
			content = content.replace(target, ecGroup!= null ? ecGroup.getName() : "");
		}
		return content;
	}
	
	/**
	 * 通知
	 * @param system
	 */
	private void notify(NoticeSystemMessage system) {
		if(getHelper().mListener != null) {
			getHelper().mListener.onPushGroupNoticeMessage(system);
		}
	}
	
	/**
	 * 群组通知
	 */
	public interface OnPushGroupNoticeMessageListener {
		void onPushGroupNoticeMessage(NoticeSystemMessage system);
	}
}
