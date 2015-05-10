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
import java.util.List;

import android.database.Cursor;
import android.text.TextUtils;

import com.speedtong.example.core.ClientUser;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.example.ui.manager.CCPAppManager;

/**
 * 联系人数据库管理
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-12
 * @version 4.0
 */
public class ContactSqlManager extends AbstractSQLManager{

	private static ContactSqlManager sInstance;
	private static ContactSqlManager getInstance() {
		if(sInstance == null) {
			sInstance = new ContactSqlManager();
		}
		return sInstance;
	}
	
	/**
	 * 插入联系人到数据库
	 * @param contacts
	 * @return
	 */
	public static ArrayList<Long> insertContacts(List<ECContacts> contacts) {
	
		ArrayList<Long> rows = new ArrayList<Long>();
		try {
			
			getInstance().sqliteDB().beginTransaction();
			for(ECContacts c : contacts) {
				long rowId = insertContact(c);
				if(rowId != -1L) {
					rows.add(rowId);
				}
			}
			
			// 初始化系统联系人
			insertSystemNoticeContact();
			getInstance().sqliteDB().setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			getInstance().sqliteDB().endTransaction();
		}
		return rows;
	}
	
	/**
	 * 初始化联系人数据库
	 * @return
	 */
	private static long insertSystemNoticeContact() {
		ECContacts contacts = new ECContacts(GroupNoticeSqlManager.CONTACT_ID);
		contacts.setNickname("群组通知");
		contacts.setRemark("touxiang_notice.png");
		
		return insertContact(contacts);
	}
	
	/**
	 * 插入联系人到数据库
	 * @param contact
	 * @return
	 */
	public static long insertContact(ECContacts contact) {
		
		if(contact == null || TextUtils.isEmpty(contact.getContactid())) {
			return -1;
		}
		
		return getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_CONTACT, null, contact.buildContentValues());
		
	}
	
	/**
	 * 查询联系人名称
	 * @param contactId
	 * @return
	 */
	public static ArrayList<String> getContactName(String[] contactId) {
		ArrayList<String> contacts = null;
		try {
			String sql = "select username from contacts where contact_id in ";
			StringBuilder sb = new StringBuilder("(");
			for (int i = 0; i < contactId.length; i++) {
				sb.append("'").append(contactId[i]).append("'");
				if (i != contactId.length - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
			Cursor cursor = getInstance().sqliteDB().rawQuery(
					sql + sb.toString(), null);
			if (cursor != null && cursor.getCount() > 0) {
				contacts = new ArrayList<String>();
				ClientUser clientUser = CCPAppManager.getClientUser();
				while (cursor.moveToNext()) {
					if (clientUser != null
							&& clientUser.getUserId().equals(
									cursor.getString(0))) {
						continue;
					}

					contacts.add(cursor.getString(0));
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contacts;
	}
	
	/**
	 * 查询联系人名称
	 * @param contactId
	 * @return
	 */
	public static ArrayList<String> getContactRemark(String[] contactId) {
		ArrayList<String> contacts = null;
		try {
			String sql = "select remark from contacts where contact_id in ";
			StringBuilder sb = new StringBuilder("(");
			for (int i = 0; i < contactId.length; i++) {
				sb.append("'").append(contactId[i]).append("'");
				if (i != contactId.length - 1) {
					sb.append(",");
				}
			}
			sb.append(")");
			Cursor cursor = getInstance().sqliteDB().rawQuery(
					sql + sb.toString(), null);
			if (cursor != null && cursor.getCount() > 0) {
				contacts = new ArrayList<String>();
				while (cursor.moveToNext()) {
					contacts.add(cursor.getString(0));
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contacts;
	}
	
	/**
	 * 查询联系人
	 * @return
	 */
	public static ArrayList<ECContacts> getContacts() {
		ArrayList<ECContacts> contacts = null;
		try {
			Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, 
					new String[]{
					ContactsColumn.ID,
					ContactsColumn.USERNAME ,
					ContactsColumn.CONTACT_ID,
					ContactsColumn.REMARK},
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				contacts = new ArrayList<ECContacts>();
				ClientUser clientUser = CCPAppManager.getClientUser();
				while (cursor.moveToNext()) {
					if(GroupNoticeSqlManager.CONTACT_ID.equals(cursor.getString(2))) {
						continue;
					}
					ECContacts c = new ECContacts(cursor.getString(2));
					c.setNickname(cursor.getString(1));
					c.setRemark(cursor.getString(3));
					c.setId(cursor.getInt(0));
					if(clientUser != null && clientUser.getUserId().equals(c.getContactid())) {
						continue;
					}
					contacts.add(c);
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contacts;
		
	}
	
	
	/**
	 * 根据联系人ID查询联系人
	 * @param rawId
	 * @return
	 */
	public static ECContacts getContact(long rawId) {
		if(rawId == -1) {
			return null;
		}
		try {
			Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
					 "id=?", new String[]{String.valueOf(rawId)},null , null, null, null);
			ECContacts c = null;
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					c = new ECContacts(cursor.getString(2));
					c.setNickname(cursor.getString(1));
					c.setRemark(cursor.getString(3));
					c.setId(cursor.getInt(0));
				}
				cursor.close();
			}
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 根据联系人账号查询
	 * @param contactId
	 * @return
	 */
	public static ECContacts getContact(String contactId) {
		if(TextUtils.isEmpty(contactId)) {
			return null;
		}
		try {
			Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
					 "contact_id=?", new String[]{contactId},null , null, null, null);
			ECContacts c = null;
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					c = new ECContacts(cursor.getString(2));
					c.setNickname(cursor.getString(1));
					c.setRemark(cursor.getString(3));
					c.setId(cursor.getInt(0));
				}
				cursor.close();
			}
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param contactId
	 * @return
	 */
	public static ECContacts getContactLikeUsername(String nikeName) {
		if(TextUtils.isEmpty(nikeName)) {
			return null;
		}
		try {
			Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
					 "username LIKE '" + nikeName + "'" , null,null , null, null, null);
			ECContacts c = null;
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					c = new ECContacts(cursor.getString(2));
					c.setNickname(cursor.getString(1));
					c.setRemark(cursor.getString(3));
					c.setId(cursor.getInt(0));
				}
				cursor.close();
			}
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void reset() {
		getInstance().release();
		sInstance = null;
	}
}
