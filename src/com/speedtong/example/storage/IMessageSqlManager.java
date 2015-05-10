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

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;

import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.im.ECMessage.MessageStatus;
import com.speedtong.sdk.im.ECTextMessageBody;
import com.speedtong.sdk.im.ECVoiceMessageBody;

/**
 * 消息数据库管理
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-11
 * @version 4.0
 */
public class IMessageSqlManager extends AbstractSQLManager {

	/**消息未读状态--未读*/
	static final public int IMESSENGER_TYPE_UNREAD = 0;
	/**消息未读状态--已读*/
	static final public int IMESSENGER_TYPE_READ = 1;
	
	static final public int IMESSENGER_BOX_TYPE_ALL = 0;
	/**信箱类型--收件箱*/
	static final public int IMESSENGER_BOX_TYPE_INBOX = 1;
	static final public int IMESSENGER_BOX_TYPE_SENT = 2;
	/**信箱类型--草稿箱*/
	static final public int IMESSENGER_BOX_TYPE_DRAFT = 3;
	/**信箱类型--发件箱*/
	static final public int IMESSENGER_BOX_TYPE_OUTBOX = 4;
	static final public int IMESSENGER_BOX_TYPE_FAILED = 5;
	static final public int IMESSENGER_BOX_TYPE_QUEUED = 6;
	
	
	private static IMessageSqlManager instance;

	private IMessageSqlManager() {
		super();
	}

	private static IMessageSqlManager getInstance() {
		if (instance == null) {
			instance = new IMessageSqlManager();
		}
		return instance;
	}
	
	/**
	 * 更新消息到本地数据库
	 * @param message 消息
	 * @param boxType 消息保存的信箱类型
	 * @return 更新的消息ID
	 */
	public static long insertIMessage(ECMessage message, int boxType) {
		long ownThreadId = 0;
		long row = 0L;
		try {
			if (!TextUtils.isEmpty(message.getSessionId())) {
				String contactIds = message.getSessionId();
				ownThreadId = ConversationSqlManager.querySessionIdForBySessionId(contactIds);
				if (ownThreadId == 0) {
					try {
						ownThreadId = ConversationSqlManager.insertSessionRecord(message);
					} catch (Exception e) {
						LogUtil.e(TAG + " " + e.toString());
					}
				}
				if (ownThreadId > 0) {
					int isread = IMESSENGER_TYPE_UNREAD;
					if (boxType == IMESSENGER_BOX_TYPE_OUTBOX
							|| boxType == IMESSENGER_BOX_TYPE_DRAFT) {
						isread = IMESSENGER_TYPE_READ;
					}
					ContentValues values = new ContentValues();
					if (boxType == IMESSENGER_BOX_TYPE_DRAFT) {
						try { // 草稿箱只保存文本
							values.put(IMessageColumn.OWN_THREAD_ID,ownThreadId);
							values.put(IMessageColumn.sender,message.getForm());
							values.put(IMessageColumn.MESSAGE_ID,message.getMsgId());
							values.put(IMessageColumn.MESSAGE_TYPE,message.getType().ordinal());
							values.put(IMessageColumn.SEND_STATUS,message.getMsgStatus().ordinal());
							values.put(IMessageColumn.READ_STATUS, isread);
							values.put(IMessageColumn.BOX_TYPE, boxType);
							values.put(IMessageColumn.BODY, ((ECTextMessageBody)message.getBody()).getMessage());
							values.put(IMessageColumn.USER_DATA,message.getUserData());
							values.put(IMessageColumn.RECEIVE_DATE ,System.currentTimeMillis());
							values.put(IMessageColumn.CREATE_DATE,message.getMsgTime());

							row = getInstance().sqliteDB().insertOrThrow(DatabaseHelper.TABLES_NAME_IM_MESSAGE,null, values);
						} catch (SQLException e) {
							LogUtil.e(TAG + " " + e.toString());
						} finally {
							values.clear();
						}
					} else {
						try {
							values.put(IMessageColumn.OWN_THREAD_ID,ownThreadId);
							values.put(IMessageColumn.MESSAGE_ID,message.getMsgId());
							values.put(IMessageColumn.SEND_STATUS,message.getMsgStatus().ordinal());
							values.put(IMessageColumn.READ_STATUS, isread);
							values.put(IMessageColumn.BOX_TYPE, boxType);
							values.put(IMessageColumn.USER_DATA,message.getUserData());
							values.put(IMessageColumn.RECEIVE_DATE,System.currentTimeMillis());
							values.put(IMessageColumn.CREATE_DATE,message.getMsgTime());
							values.put(IMessageColumn.sender,message.getForm());
							values.put(IMessageColumn.MESSAGE_TYPE,message.getType().ordinal());
							putValues(message ,values);
							row = getInstance().sqliteDB().insertOrThrow(
									DatabaseHelper.TABLES_NAME_IM_MESSAGE,
									null, values);
						} catch (SQLException e) {
							e.printStackTrace();
							LogUtil.e(TAG + " " + e.toString());
						} finally {
							values.clear();
						}
					}
					getInstance().notifyChanged(contactIds);
				}
			}
		} catch (Exception e) {
			LogUtil.e(e.getMessage());
		}
		return row;
	}
	
	/**
	 * 更新消息的状态
	 * @param msgId
	 * @param sendStatu
	 * @return
	 */
	public static int setIMessageSendStatus(String msgId, int sendStatu) {
		return setIMessageSendStatus(msgId, sendStatu, 0);
	}
	
	/**
	 * 更新文件的下载状态
	 * @param msg
	 * @return
	 */
	public static int updateIMessageDownload(ECMessage msg) {
		if(msg == null || TextUtils.isEmpty(msg.getMsgId())) {
			return -1;
		}
		int row = -1;
		ContentValues values = new ContentValues();
		try {
			String where = IMessageColumn.MESSAGE_ID + " = '" + msg.getMsgId() + "'";
			ECFileMessageBody msgBody = (ECFileMessageBody) msg.getBody();
			values.put(IMessageColumn.FILE_PATH, msgBody.getLocalUrl());
			values.put(IMessageColumn.USER_DATA, msg.getUserData());
			if (msg.getType() == ECMessage.Type.VOICE) {
				int voiceTime = DemoUtils.calculateVoiceTime(msgBody.getLocalUrl());
				values.put(IMessageColumn.DURATION, voiceTime);
			}
			row = getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_IM_MESSAGE,
					values, where, null);
			//notifyChanged(msgId);
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
			e.getStackTrace();
		} finally {
			if (values != null) {
				values.clear();
				values = null;
			}
		}
		return row;
	}

	/**
	 * 设置Im消息发送状态
	 * @param msgId 消息ID
	 * @param sendStatu 发送状态
	 * @return
	 */
	public static int setIMessageSendStatus(String msgId, int sendStatu, int duration) {
		int row = 0;
		ContentValues values = new ContentValues();
		try {
			String where = IMessageColumn.MESSAGE_ID + " = '" + msgId
					+ "' and " + IMessageColumn.SEND_STATUS + "!=" + sendStatu;
			values.put(IMessageColumn.SEND_STATUS, sendStatu);
			if (duration > 0) {
				values.put(IMessageColumn.DURATION, duration);
			}
			row = getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_IM_MESSAGE,
					values, where, null);
			//notifyChanged(msgId);
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
			e.getStackTrace();
		} finally {
			if (values != null) {
				values.clear();
				values = null;
			}
		}
		return row;
	}	
	
	/**
	 * 根据不同的消息类型将数据保存到数据库
	 * @param message 
	 * @param values
	 */
	private static void putValues(ECMessage message, ContentValues values) {
		if(message.getType() == ECMessage.Type.TXT) {
			values.put(IMessageColumn.BODY, ((ECTextMessageBody)message.getBody()).getMessage());
		} else {
			ECFileMessageBody body = (ECFileMessageBody) message.getBody();
			values.put(IMessageColumn.FILE_PATH, body.getLocalUrl());
			values.put(IMessageColumn.FILE_URL, body.getRemoteUrl());
			if (message.getType() == ECMessage.Type.VOICE) {
				ECVoiceMessageBody Voicebody = (ECVoiceMessageBody) message.getBody();
				values.put(IMessageColumn.DURATION, Voicebody.getDuration());
			}
			
		}
	}
	
	/**
	 * 
	 * @param threadId
	 * @param lastTime
	 * @return
	 */
	public static  ArrayList<ECMessage> queryIMessageListAfter(long threadId,
			String lastTime) {
		ArrayList<ECMessage> al = null;
		Cursor cursor = null;
		StringBuffer sb = new StringBuffer();
		if (lastTime != null && !lastTime.equals("") && !lastTime.equals("0")) {
			sb.append(IMessageColumn.CREATE_DATE + " > ").append(lastTime);
		} else {
			sb.append("1=1");
		}
		sb.append(" and " + IMessageColumn.OWN_THREAD_ID + " = ").append(
				threadId);
		sb.append(" and  " + IMessageColumn.BOX_TYPE + " != 3");
		try {
			cursor = getInstance().sqliteDB().query(false,
					DatabaseHelper.TABLES_NAME_IM_MESSAGE, null, sb.toString(),
					null, null, null, IMessageColumn.RECEIVE_DATE + " asc",
					null);
			if (cursor != null) {
				if (cursor.getCount() == 0) {
					return null;
				}
				al = new ArrayList<ECMessage>();
				while (cursor.moveToNext()) {

					long id = cursor.getLong(cursor.getColumnIndex(IMessageColumn.ID));
					String sender = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.sender));
					String msgId = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.MESSAGE_ID));
					long ownThreadId = cursor.getLong(cursor.getColumnIndexOrThrow(IMessageColumn.OWN_THREAD_ID));
					long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(IMessageColumn.CREATE_DATE));
					long receiveDate = cursor.getLong(cursor.getColumnIndexOrThrow(IMessageColumn.RECEIVE_DATE));
					String userData = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.USER_DATA));
					int read = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.READ_STATUS));
					int boxType = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.BOX_TYPE));
					int msgType = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.MESSAGE_TYPE));
					int sendStatus = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.SEND_STATUS));
					
					ECMessage ecMessage = null; 
					if(msgType == ECMessage.Type.TXT.ordinal()) {
						String content = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.BODY));
						ecMessage = ECMessage.createECMessage(ECMessage.Type.TXT);
						ECTextMessageBody textBody = new ECTextMessageBody(content);
						ecMessage.setBody(textBody);
					} else {
						/*String fileUrl = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.FILE_URL));
						String fileLocalPath = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.FILE_PATH));
						
						if (msgType == ECMessage.Type.VOICE.ordinal()) {
							int duration = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.DURATION));
							ECVoiceMessageBody voiceBody = new ECVoiceMessageBody(new File(fileLocalPath), 0);
							voiceBody.setRemoteUrl(fileUrl);
							ecMessage.setBody(voiceBody);
							ecMessage = ECMessage.createECMessage(ECMessage.Type.VOICE);
						} else if (msgType == ECMessage.Type.IMAGE.ordinal() || msgType == ECMessage.Type.FILE.ordinal()) {
							ECFileMessageBody fileBody = new 
						} else {
							continue;
						}*/
					}
					ecMessage.setId(id);
					ecMessage.setForm(sender);
					ecMessage.setMsgId(msgId);
					ecMessage.setMsgTime(createDate);
					ecMessage.setUserData(userData);
					ecMessage.setDirection(getMessageDirect(boxType));
					al.add(0, ecMessage);
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return al;
	}

	/**
	 * IM分页查询
	 * @param num
	 * @param lastTime
	 * @return
	 */
	public static ArrayList<ECMessage> queryIMessageList(long threadId, int num,
			String lastTime) {
		ArrayList<ECMessage> al = null;
		Cursor cursor = null;
		StringBuffer sb = new StringBuffer();
		if (lastTime != null && !lastTime.equals("") && !lastTime.equals("0")) {
			sb.append(IMessageColumn.CREATE_DATE + " < ").append(lastTime);
		} else {
			sb.append("1=1");
		}
		// if (threadId != 0) {
		sb.append(" and " + IMessageColumn.OWN_THREAD_ID + " = ").append(
				threadId);
		// }
		sb.append(" and  " + IMessageColumn.BOX_TYPE + " != " + ECMessage.Direction.DRAFT.ordinal());
		try {
			cursor = getInstance().sqliteDB().query(false,
					DatabaseHelper.TABLES_NAME_IM_MESSAGE, null, sb.toString(),
					null, null, null, IMessageColumn.RECEIVE_DATE + " desc",
					num + "");
			if (cursor != null) {
				if (cursor.getCount() == 0) {
					return null;
				}
				al = new ArrayList<ECMessage>();
				while (cursor.moveToNext()) {

					long id = cursor.getLong(cursor.getColumnIndex(IMessageColumn.ID));
					String sender = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.sender));
					String msgId = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.MESSAGE_ID));
					//long ownThreadId = cursor.getLong(cursor.getColumnIndexOrThrow(IMessageColumn.OWN_THREAD_ID));
					long createDate = cursor.getLong(cursor.getColumnIndexOrThrow(IMessageColumn.CREATE_DATE));
					//long receiveDate = cursor.getLong(cursor.getColumnIndexOrThrow(IMessageColumn.RECEIVE_DATE));
					String userData = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.USER_DATA));
					int read = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.READ_STATUS));
					int boxType = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.BOX_TYPE));
					int msgType = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.MESSAGE_TYPE));
					int sendStatus = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.SEND_STATUS));
					
					ECMessage ecMessage = ECMessage.createECMessage(ECMessage.Type.NONE);; 
					if(msgType == ECMessage.Type.TXT.ordinal()) {
						String content = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.BODY));
						ecMessage.setType(ECMessage.Type.TXT);
						ECTextMessageBody textBody = new ECTextMessageBody(content);
						ecMessage.setBody(textBody);
					} else {
						String fileUrl = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.FILE_URL));
						String fileLocalPath = cursor.getString(cursor.getColumnIndexOrThrow(IMessageColumn.FILE_PATH));
						
						if (msgType == ECMessage.Type.VOICE.ordinal()) {
							ecMessage.setType(ECMessage.Type.VOICE);
							int duration = cursor.getInt(cursor.getColumnIndexOrThrow(IMessageColumn.DURATION));
							ECVoiceMessageBody voiceBody = new ECVoiceMessageBody(new File(fileLocalPath), 0);
							voiceBody.setRemoteUrl(fileUrl);
							ecMessage.setBody(voiceBody);
							voiceBody.setDuration(duration);
						} else if (msgType == ECMessage.Type.IMAGE.ordinal() || msgType == ECMessage.Type.FILE.ordinal()) {
							if(msgType == ECMessage.Type.FILE.ordinal()) {
								ecMessage.setType(ECMessage.Type.FILE);
							} else {
								ecMessage.setType(ECMessage.Type.IMAGE);
							}
							ECFileMessageBody fileBody = new ECFileMessageBody();
							fileBody.setLocalUrl(fileLocalPath);
							fileBody.setRemoteUrl(fileUrl);
							fileBody.setFileName(DemoUtils.getFileNameFormUserdata(userData));
							ecMessage.setBody(fileBody);
						} else {
							continue;
						}
					}
					ecMessage.setId(id);
					ecMessage.setForm(sender);
					ecMessage.setMsgId(msgId);
					ecMessage.setMsgTime(createDate);
					ecMessage.setUserData(userData);
					if(sendStatus == MessageStatus.SENDING.ordinal()) {
						ecMessage.setMsgStatus(MessageStatus.SENDING);
					} else if(sendStatus == MessageStatus.RECEIVE.ordinal()) {
						ecMessage.setMsgStatus(MessageStatus.RECEIVE);
					} else if(sendStatus == MessageStatus.SUCCESS.ordinal()) {
						ecMessage.setMsgStatus(MessageStatus.SUCCESS);
					} else if(sendStatus == MessageStatus.FAILED.ordinal()) {
						ecMessage.setMsgStatus(MessageStatus.FAILED);
					}
					ecMessage.setDirection(getMessageDirect(boxType));
					al.add(0, ecMessage);
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return al;

	}
	
	/**
	 * 返回消息的类型，发送、接收、草稿
	 * @param type 消息类型
	 * @return
	 */
	public static ECMessage.Direction getMessageDirect(int type) {
		if(type == ECMessage.Direction.SEND.ordinal()) {
			return ECMessage.Direction.SEND;
		} else if (type == ECMessage.Direction.RECEIVE.ordinal()) {
			return ECMessage.Direction.RECEIVE;
		} else {
			return ECMessage.Direction.DRAFT;
		}
	}
	
	/**
	 * 根据会话ID查询会话消息的数据量
	 * @param threadId 当前会话ID
	 * @return 会话总数
	 */
	public static int getTotalCount(long threadId) {
		String sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLES_NAME_IM_MESSAGE + " WHERE " + "sid" + "=" + threadId ;
		Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToLast()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}
	
	public static Cursor getNullCursor() {
		return getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_IM_MESSAGE, null, "msgid=?", new String[]{"-1"} ,null, null, null);
	}
	
	/**
	 * 分页加载查询数据
	 * @param threadId
	 * @param limit
	 * @return
	 */
	public static Cursor getCursor(long threadId , int limit) {
		String sql = "SELECT * FROM " + DatabaseHelper.TABLES_NAME_IM_MESSAGE
				+ " WHERE " + "sid" + "= " + threadId + " ORDER BY "
				+ "serverTime" + " ASC LIMIT " + limit + " offset "
				+ "(SELECT count(*) FROM "
				+ DatabaseHelper.TABLES_NAME_IM_MESSAGE + " WHERE " + "sid"
				+ "= " + threadId + " ) -" + limit;
	    LogUtil.d(TAG, "getCursor sid:" + threadId + " limit:" + limit + " [" + sql + "]");
	    return getInstance().sqliteDB().rawQuery(sql, null);
	}
	
	/**
	 * 设置会话所有的未读消息变成已读
	 * @param threadId
	 * @return
	 */
	public static int setIMessageNomalThreadRead(long threadId) {
		int row = 0;
		ContentValues values = new ContentValues();
		try {
			String where = IMessageColumn.OWN_THREAD_ID + " = " + threadId
					+ " and " + IMessageColumn.READ_STATUS + " = "
					+ IMESSENGER_TYPE_UNREAD ;
			values.put(IMessageColumn.READ_STATUS, IMESSENGER_TYPE_READ);
			row = getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_IM_MESSAGE,
					values, where, null);
		} catch (Exception e) {
			LogUtil.e(TAG + " " + e.toString());
			e.getStackTrace();
		} finally {
			if (values != null) {
				values.clear();
				values = null;
			}
		}
		LogUtil.d(TAG, " setIMessageNomalThreadRead rows :" + row);
		return row;
	}
	
	/**
	 * 查询所有未读数
	 * 
	 * @return
	 */
	public static int qureyAllSessionUnreadCount() {
		int count = 0;
		String[] columnsList = { "sum(" + IThreadColumn.UNREAD_COUNT + ")" };
		Cursor cursor = null;
		try {
			cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_IM_SESSION,
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
	
	/**
	 * 消息重发
	 * @param rowid
	 * @param detail
	 * @return
	 */
	public static int changeResendMsg(long rowid , ECMessage detail) {
		
		if(detail == null || TextUtils.isEmpty(detail.getMsgId()) || rowid == -1) {
			return -1;
		}

		String where = IMessageColumn.ID + "=" + rowid + " and " + IMessageColumn.SEND_STATUS + " = " + ECMessage.MessageStatus.FAILED.ordinal();
		ContentValues values = null;
		try {
			values = new ContentValues();
			values.put(IMessageColumn.MESSAGE_ID, detail.getMsgId());
			values.put(IMessageColumn.SEND_STATUS, detail.getMsgStatus().ordinal());
			values.put(IMessageColumn.USER_DATA, detail.getUserData());
			return getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_IM_MESSAGE, values, where,null);

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(e.getMessage());
		} finally {
			if (values != null) {
				values.clear();
				values = null;
			}
		}
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
