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
package com.speedtong.example.storage;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;

import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.ui.chatting.mode.Conversation;
import com.speedtong.sdk.im.ECMessage;

/**
 * 会话消息数据库管理
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-11
 * @version 4.0
 */
public class ConversationSqlManager extends AbstractSQLManager {

	private static ConversationSqlManager instance;

	private ConversationSqlManager() {
		super();
	}

	public static ConversationSqlManager getInstance() {
		if (instance == null) {
			instance = new ConversationSqlManager();
		}
		return instance;
	}
	
	/**
	 * 查询会话列表
	 * @return
	 */
	public static ArrayList<Conversation> getConversations() {
		ArrayList<Conversation> conversations = null;
		try {
			String sql = "select unreadCount, im_thread.type, sendStatus, dateTime, sessionId, text, username from im_thread,contacts where im_thread.sessionId = contacts.contact_id order by dateTime desc";
			Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
			if(cursor != null && cursor.getCount() > 0) {
				conversations = new ArrayList<Conversation>();
				while (cursor.moveToNext()) {
					Conversation conversation = new Conversation();
					conversation.setCursor(cursor);
					conversations.add(conversation);
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return conversations;
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static Cursor getConversationCursor() {
		try {
			//String sql = "select unreadCount, im_thread.type, sendStatus, dateTime, sessionId, text, username from im_thread,contacts where im_thread.sessionId = contacts.contact_id order by dateTime desc";
			String sql = "select unreadCount, im_thread.type, sendStatus, dateTime, sessionId, text, username  from im_thread,contacts where im_thread.sessionId = contacts.contact_id"
				+ " union select unreadCount, im_thread.type, sendStatus, dateTime, sessionId, text, name  from im_thread,groups where im_thread.sessionId = groups.groupid  order by dateTime desc";
			return getInstance().sqliteDB().rawQuery(sql, null);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
		
	}
	
	
	/**
	 * 通过会话ID查找消息数据库主键
	 * @param sessionId 会话ID
	 * @return
	 */
	public static long querySessionIdForBySessionId(String sessionId) {
		Cursor cursor = null;
		long threadId = 0;
		if (sessionId != null) {
			String where = IThreadColumn.THREAD_ID + " = '" + sessionId + "' ";
			try {
				cursor = getInstance().sqliteDB().query(
						DatabaseHelper.TABLES_NAME_IM_SESSION, null, where,
						null, null, null, null);
				if (cursor != null && cursor.getCount() > 0) {
					if (cursor.moveToFirst()) {
						threadId = cursor.getLong(cursor
								.getColumnIndexOrThrow(IThreadColumn.ID));
					}
				}
			} catch (SQLException e) {
				LogUtil.e(TAG + " " + e.toString());
			} finally {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}
		}
		return threadId;
	}
	
	/**
	 * 生成一个新的会话消息
	 * @param msg
	 * @param threadId
	 * @return
	 */
	public static long insertSessionRecord(ECMessage msg) {
		if (msg == null || TextUtils.isEmpty(msg.getSessionId())) {
			throw new IllegalArgumentException("insert thread table "
					+ DatabaseHelper.TABLES_NAME_IM_SESSION
					+ "error , that Argument ECMessage:" + msg);
		}
		long row = -1;
		ContentValues values = new ContentValues();
		try {
			values.put(IThreadColumn.THREAD_ID, msg.getSessionId());
			values.put(IThreadColumn.DATE, System.currentTimeMillis());
			values.put(IThreadColumn.CONTACT_ID, msg.getForm());
			row = getInstance().sqliteDB().insertOrThrow(
					DatabaseHelper.TABLES_NAME_IM_SESSION, null, values);
		} catch (SQLException ex) {
			ex.printStackTrace();
			LogUtil.e(TAG + " " + ex.toString());
		} finally {
			if (values != null) {
				values.clear();
				values = null;
			}
		}
		return row;
	}
	
	public int qureySessionUnreadCount() {
		int count = 0;
		String[] columnsList = { "count(" + IThreadColumn.UNREAD_COUNT + ")" };
		String where = IThreadColumn.UNREAD_COUNT + " > 0";
		Cursor cursor = null;
		try {
			cursor = sqliteDB().query(DatabaseHelper.TABLES_NAME_IM_SESSION,
					columnsList, where, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				if (cursor.moveToFirst()) {
					count = cursor.getInt(cursor.getColumnIndex("count("
							+ IThreadColumn.UNREAD_COUNT + ")"));
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return count;
	}
	
	public int qureyAllSessionUnreadCount() {
		int count = 0;
		String[] columnsList = { "sum(" + IThreadColumn.UNREAD_COUNT + ")" };
		Cursor cursor = null;
		try {
			cursor = sqliteDB().query(DatabaseHelper.TABLES_NAME_IM_SESSION,
					columnsList, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				if (cursor.moveToFirst()) {
					count = cursor.getInt(cursor.getColumnIndex("sum("
							+ IThreadColumn.UNREAD_COUNT + ")"));
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return count;
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
