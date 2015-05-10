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
package com.speedtong.example.ui.group;

import java.util.ArrayList;
import java.util.List;

import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.core.SDKCoreHelper;
import com.speedtong.example.storage.ContactSqlManager;
import com.speedtong.example.storage.GroupMemberSqlManager;
import com.speedtong.example.storage.GroupSqlManager;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECGroupManager;
import com.speedtong.sdk.ECGroupManager.OnGetAllPublicGroupsListener;
import com.speedtong.sdk.ECGroupManager.OnGetGroupDetailListener;
import com.speedtong.sdk.ECGroupManager.OnQueryOwnGroupsListener;
import com.speedtong.sdk.im.ECGroup;

/**
 * 群组同步
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-18
 * @version 4.0
 */
public class GroupService {
	private static final String TAG  = "ECSDK_Demo.GroupService";
	
	public static final String PRICATE_CHATROOM = "@priategroup.com";
	private static GroupService sInstance;
	private ECGroupManager mGroupManager;
	private List<String> mGroupIds;
	private Callback mCallback;
	private boolean isSync = false;
	
	private GroupService () {
		mGroupManager = SDKCoreHelper.getECGroupManager();
		countGroups();
	}
	
	private static GroupService getInstance() {
		if(sInstance == null) {
			sInstance = new GroupService();
		}
		return sInstance;
	}
	
	/**
	 * 同步所有的群组
	 */
	private void countGroups() {
		mGroupIds = GroupSqlManager.getAllGroupId();
	}
	
	/**
	 * 开始网络同步群组列表信息
	 */
	public static void syncGroup(Callback callbck) {
		getInstance().mGroupManager = SDKCoreHelper.getECGroupManager();
		if(getInstance().mGroupManager == null || getInstance().isSync) {
			LogUtil.e(TAG, "SDK not ready or isSync " + getInstance().isSync);
			return ;
		}
		getInstance().isSync = true;
		getInstance().mCallback = callbck;
		getInstance().mGroupManager.queryOwnGroups(new OnQueryOwnGroupsListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onQueryOwnGroupsComplete(ECError error, List<ECGroup> groups) {
				if(groups == null || groups.isEmpty()) {
					return ;
				}
				List<String> allGroupIdByJoin = GroupSqlManager.getAllGroupIdBy(true);
				ArrayList<String> ids = new ArrayList<String>();
				for(ECGroup group :groups) {
					ids.add(group.getGroupId());
				}
				
				// 查找不是我的群组
				if(!allGroupIdByJoin.isEmpty()) {
					for(String id : allGroupIdByJoin) {
						if(ids.contains(id)) {
							continue;
						}
						// 不是我的群组
						GroupSqlManager.updateUNJoin(id);
					}
				}
				GroupSqlManager.insertGroupInfos(groups, 1);
				
				// 更新公共所有群组
				syncPublicGroups();
			}
		});
	}
	
	/**
	 * 更新所有的群组信息
	 */
	private static void syncPublicGroups() {
		getInstance().mGroupManager.getAllPublicGroups(String.valueOf(System.currentTimeMillis()), new OnGetAllPublicGroupsListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onGetAllPublicGroupsComplete(ECError error, List<ECGroup> groups) {
				if(groups == null || groups.isEmpty()) {
					return ;
				}
				List<String> allGroupIdByUNJoin = GroupSqlManager.getAllGroupIdBy(false);
				ArrayList<String> ids = new ArrayList<String>();
				for(ECGroup group :groups) {
					ids.add(group.getGroupId());
				}
				
				// 查找不是我的群组
				if(!allGroupIdByUNJoin.isEmpty()) {
					for(String id : allGroupIdByUNJoin) {
						if(ids.contains(id)) {
							continue;
						}
						// 不是我的群组
						GroupSqlManager.delGroup(id);
					}
				}
				GroupSqlManager.insertGroupInfos(groups, -1);
				getInstance().isSync = false;
				
				if(getInstance().mCallback != null) {
					getInstance().mCallback.onSyncGroup();
				}
			}
		});
	}
	
	/**
	 * 同步群组信息
	 * @param groupId
	 */
	public static void syncGroupInfo(final String groupId) {
		ECGroupManager groupManager = SDKCoreHelper.getECGroupManager();
		if(groupManager == null) {
			return ;
		}
		groupManager.getGroupDetail(groupId, new OnGetGroupDetailListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onGetGroupDetailComplete(ECError error, ECGroup group) {
				if(group == null) {
					return ;
				}
				
				GroupSqlManager.updateGroup(group);
				if(getInstance().mCallback != null) {
					getInstance().mCallback.onSyncGroupInfo(groupId);
				}
			}
		});
	}
	
	/**
	 * 解散群组
	 * @param groupId
	 */
	public static void disGroup(String groupId) {
		getGroupManager();
		getInstance().mGroupManager.deleteGroup(groupId, new ECGroupManager.OnDeleteGroupListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onDeleteGroupComplete(ECError error, String groupId) {
				if(getInstance().isSuccess(error)) {
					GroupMemberSqlManager.delAllMember(groupId);
					GroupSqlManager.delGroup(groupId);
					if(getInstance().mCallback != null) {
						getInstance().mCallback.onGroupDel(groupId);
					}
				}
			}
		});
		
	}
	
	/**
	 * 退出群组
	 * @param groupId
	 */
	public static void quitGroup(String groupId) {
		getGroupManager();
		getInstance().mGroupManager.quitGroup(groupId, new ECGroupManager.OnQuitGroupListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onQuitGroupMembersComplete(ECError error, String groupId) {
				if(getInstance().isSuccess(error)) {
					GroupMemberSqlManager.delAllMember(groupId);
					GroupSqlManager.updateUNJoin(groupId);
					getInstance().mCallback.onGroupDel(groupId);
				}
			}
		});
	}
	
	/**
	 * 申请加入群组
	 * @param groupid
	 */
	public static void applyGroup(String groupId , String declare , final OnApplyGroupCallbackListener l) {
		getGroupManager();
		getInstance().mGroupManager.joinGroup(groupId, declare, new ECGroupManager.OnJoinGroupListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onJoinGroupComplete(ECError error, String groupId) {
				if(getInstance().isSuccess(error)) {
					GroupSqlManager.updateJoinStatus(groupId , true);
					
					if(l != null) {
						l.onApplyGroup(true);
					}
					return ;
				}
				if(l != null) {
					l.onApplyGroup(false);
				}
			}
		});
		
		
	}
	
	
	/**
	 * 创建私有群组
	 * @param member
	 * @param permission
	 */
	public static void doCreateGroup(final String[] member , final ECGroupManager.OnInviteJoinGroupListener l) {
		ECGroup group = new ECGroup();
		group.setName(CCPAppManager.getClientUser().getUserId() + PRICATE_CHATROOM);
		group.setDeclare("");
		// 临时群组（100人）
		group.setGroupType(0);
		// 群组验证权限，需要身份验证
		group.setPermission(2);
		group.setOwner(CCPAppManager.getClientUser().getUserId());
		
		getGroupManager();
		getInstance().mGroupManager.createGroup(group, new ECGroupManager.OnCreatGroupListener() {
			
			@Override
			public void onComplete(ECError error) {
				
			}
			
			@Override
			public void onCreatGroupComplete(ECError error, ECGroup group) {
				if(getInstance().isSuccess(error)) {
					if(group.getName() != null && group.getName().endsWith(PRICATE_CHATROOM)) {
						ArrayList<String> contactName = ContactSqlManager.getContactName(member);
						String chatroomName = DemoUtils.listToString(contactName, ",");
						group.setName(chatroomName);
					}
					GroupSqlManager.insertGroup(group, true, false);
					GroupMemberService.inviteMembers(group.getGroupId(), "", 1, member , l);
				}
			}
		});
	}
	
	/**
	 * 请求是否成功
	 * @param error
	 * @return
	 */
	private boolean isSuccess(ECError error) {
		if("000000".equals(error.errorCode))  {
			return true;
		}
		return false;
	}
	
	private static void getGroupManager() {
		getInstance().mGroupManager = SDKCoreHelper.getECGroupManager();
	}
	
	/**
	 * @param callback
	 */
	public static void addListener(Callback callback) {
		getInstance().mCallback = callback;
	}
	
	public interface Callback {
		void onSyncGroup();
		void onSyncGroupInfo(String groupId);
		void onGroupDel(String groupId);
	}
	
	public interface OnApplyGroupCallbackListener {
		void onApplyGroup(boolean success);
	}
}
