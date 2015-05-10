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

import java.util.ArrayList;
import java.util.List;

import com.gao.im.core.SDKCoreHelper;
import com.gao.im.storage.GroupMemberSqlManager;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECGroupManager;
import com.speedtong.sdk.ECGroupManager.OnInviteJoinGroupListener;
import com.speedtong.sdk.ECGroupManager.OnQueryGroupMembersListener;
import com.speedtong.sdk.im.ECGroupMember;

/**
 * 群组成员同步接口
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-29
 * @version 4.0
 */
public class GroupMemberService {

	private static GroupMemberService sInstence;
	public static GroupMemberService getInstance() {
		if(sInstence == null) {
			sInstence = new GroupMemberService();
		}
		return sInstence;
	}
	
	/**SDK 访问接口*/
	private ECGroupManager mGroupManager;
	/**群组成员同步完成回调*/
	private OnSynsGroupMemberListener mGroupMemberListener;
	private GroupMemberService() {
		mGroupManager = SDKCoreHelper.getECGroupManager();
	}
	
	public static void synsGroupMember(final String groupId) {
		if(getInstance().mGroupManager == null) {
			return ;
		}
		ECGroupManager groupManager = getInstance().mGroupManager;
		groupManager.queryGroupMembers(groupId, new OnQueryGroupMembersListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onQueryGroupMembersComplete(ECError error,
					List<ECGroupMember> members) {
				if(members == null || members.isEmpty()) {
					return ;
				}
				
				ArrayList<String> accounts = GroupMemberSqlManager.getGroupMemberAccounts(groupId);
				ArrayList<String> ids = new ArrayList<String>();
				for(ECGroupMember member :members) {
					ids.add(member.getVoipAccount());
				}
				
				// 查找不是群组成员
				if(accounts != null && !accounts.isEmpty()) {
					for(String id : accounts) {
						if(ids.contains(id)) {
							continue;
						}
						// 不是群组成员、从数据库删除
						GroupMemberSqlManager.delMember(groupId, id);
					}
				}
				GroupMemberSqlManager.insertGroupMembers(members);
				
				getInstance().notify(groupId);
			}

		});
	}
	
	/**
	 * @param groupId
	 */
	private void notify(final String groupId) {
		if(getInstance().mGroupMemberListener != null) {
			getInstance().mGroupMemberListener.onSynsGroupMember(groupId);
		}
	}
	
	/**
	 * 邀请成员加入群组
	 * @param groupId 群组ID
	 * @param reason 邀请原因
	 * @param confirm 是否需要对方确认
	 * @param members 邀请的成员
	 */
	public static void inviteMembers(String groupId ,String reason ,final int confirm , String[] members) {
		getGroupManager();
		inviteMembers(groupId, reason,confirm, members, new OnInviteJoinGroupListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onInviteJoinGroupComplete(ECError error, String groupId,
					String[] members) {
				if("000000".equals(error.errorCode)) {
					if(confirm == 1) {
						GroupMemberSqlManager.insertGroupMembers(groupId, members);
					}
					getInstance().notify(groupId);
				}
			}
		});
	}
	
	public static void inviteMembers(String groupId ,String reason ,final int confirm , String[] members , OnInviteJoinGroupListener l) {
		getGroupManager();
		getInstance().mGroupManager.inviteJoinGroup(groupId, reason, members, confirm, l);
	}
	
	/**
	 * 将成员移除出群组
	 * @param groupid 群组ID
	 * @param member 移除出的群组成员
	 */
	public static void removerMember(String groupid , String[] member) {
		getGroupManager();
		getInstance().mGroupManager.deleteGroupMembers(groupid, member, new ECGroupManager.OnDeleteGroupMembersListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onDeleteGroupMembersComplete(ECError error, String groupId,
					String[] members) {
				if("000000".equals(error.errorCode)) {
					GroupMemberSqlManager.delMember(groupId, members);
					getInstance().notify(groupId);
				}
			}
		});
		
	}
	
	private static void getGroupManager() {
		getInstance().mGroupManager = SDKCoreHelper.getECGroupManager();
	}
	
	
	/**
	 * 注入SDK群组成员同步回调
	 * @param l
	 */
	public static void addListener(OnSynsGroupMemberListener l) {
		getInstance().mGroupMemberListener = l;
	}
	
	public interface OnSynsGroupMemberListener{
		void onSynsGroupMember(String groupId);
	}
}
 