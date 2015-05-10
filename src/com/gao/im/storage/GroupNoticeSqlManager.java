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
package com.gao.im.storage;

import android.content.ContentValues;
import android.database.Cursor;

import com.gao.im.common.utils.LogUtil;
import com.gao.im.ui.group.NoticeSystemMessage;
import com.speedtong.sdk.im.ECMessage;

/**
 * 群组通知消息数据库
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-31
 * @version 4.0
 */
public class GroupNoticeSqlManager extends AbstractSQLManager {

	public static final int NOTICE_MSG_TYPE = 1000;
	public static final String CONTACT_ID = "ec_group@yuntongxun.com";
	
	private static GroupNoticeSqlManager instance;

	private GroupNoticeSqlManager() {
		super();
	}

	private static GroupNoticeSqlManager getInstance() {
		if (instance == null) {
			instance = new GroupNoticeSqlManager();
		}
		return instance;
	}
	
	/**
	 * 更新群组通知消息
	 * @param notice
	 * @return
	 */
	public static long insertNoticeMsg(NoticeSystemMessage notice) {
		long ownThreadId = -1;
		if(notice != null) {
			// values.put("sid", "ec_group@yuntongxun.com");
			ContentValues buildContentValues = notice.buildContentValues();
			ownThreadId = ConversationSqlManager.querySessionIdForBySessionId(CONTACT_ID);
			if (ownThreadId == 0) {
				try {
					ECMessage message = ECMessage.createECMessage(ECMessage.Type.NONE);
					message.setForm(CONTACT_ID);
					message.setSessionId(CONTACT_ID);
					ownThreadId = ConversationSqlManager.insertSessionRecord(message);
				
				} catch (Exception e) {
					e.printStackTrace();
					LogUtil.e(TAG + " " + e.toString());
				}
			}
			if(ownThreadId > 0) {
				buildContentValues.put(SystemNoticeColumn.OWN_THREAD_ID, ownThreadId);
				long row = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, null, buildContentValues);
				if(row != -1) {
					getInstance().notifyChanged("ec_group@yuntongxun.com");
				}
				return row;
			}
		}
		return -1;
	}
	
	/**
	 * 查询通知
	 * @return
	 */
	public static Cursor getCursor() {
		String sql = "select notice_id , system_notice.declared , admin  , confirm , system_notice.groupId , member ,dateCreated , groups.name from system_notice ,groups where groups.groupid = system_notice.groupId order by dateCreated desc";
		return getInstance().sqliteDB().rawQuery(sql, null);
	}
	
	/**
	 * 设置会话已读
	 */
	public static void setAllSessionRead() {
		ContentValues values = new ContentValues();
		values.put(SystemNoticeColumn.NOTICE_READ_STATUS, IMessageSqlManager.IMESSENGER_TYPE_READ);
		String where = SystemNoticeColumn.NOTICE_READ_STATUS + " != " + IMessageSqlManager.IMESSENGER_TYPE_READ;
		getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, values, where, null);
	}
	
	/**
	 * 情况群组通知消息
	 */
	public static void delSessions() {
		getInstance().sqliteDB().delete(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, null, null);
	}
	
	public static void setSessionRead() {
		
	}
	
	public static void registerMsgObserver(OnMessageChange observer) {
		getInstance().registerObserver(observer);
	}
	
	public static void unregisterMsgObserver(OnMessageChange observer) {
		getInstance().unregisterObserver(observer);
	}
	
	public static void notifyMsgChanged(String session) {
		getInstance().notifyChanged(session);
	}
	
	public static void reset() {
		getInstance().release();
	}
	
	@Override
	protected void release() {
		super.release();
		instance = null;
	}
}
