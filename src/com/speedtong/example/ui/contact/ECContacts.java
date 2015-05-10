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
package com.speedtong.example.ui.contact;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.speedtong.example.storage.AbstractSQLManager.ContactsColumn;

/**
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-12
 * @version 4.0
 */
public class ECContacts  implements Parcelable{
	
	public static final Parcelable.Creator<ECContacts> CREATOR = new Creator<ECContacts>() {
		@Override
		public ECContacts[] newArray(int size) {
			return new ECContacts[size];
		}

		@Override
		public ECContacts createFromParcel(Parcel in) {
			return new ECContacts(in);
		}
	};
	
	private long id;

	/**联系人账号*/
	private String contactid;
	/**联系人昵称*/
	private String nickname;
	/**联系人类型*/
	private int type;
	/**联系人账号Token*/
	private String token;
	/**联系人子账号*/
	private String subAccount;
	/**联系人子账号Token*/
	private String subToken;
	/**备注*/
	private String remark;
	
	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}
	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}
	/**
	 * @param remark the remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * @return the contactid
	 */
	public String getContactid() {
		return contactid;
	}
	/**
	 * @param contactid the contactid to set
	 */
	public void setContactid(String contactid) {
		this.contactid = contactid;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * @return the subAccount
	 */
	public String getSubAccount() {
		return subAccount;
	}
	/**
	 * @param subAccount the subAccount to set
	 */
	public void setSubAccount(String subAccount) {
		this.subAccount = subAccount;
	}
	/**
	 * @return the subToken
	 */
	public String getSubToken() {
		return subToken;
	}
	/**
	 * @param subToken the subToken to set
	 */
	public void setSubToken(String subToken) {
		this.subToken = subToken;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	private ECContacts (Parcel in) {
		this.id  = in.readLong();
		this.contactid = in.readString();
		this.type = in.readInt();
		this.nickname = in.readString();
		this.subAccount= in.readString();
		this.subToken= in.readString();
		this.token= in.readString();
		this.remark= in.readString();
	}
	
	
	
	
	public ContentValues buildContentValues() {
		ContentValues values = new ContentValues();
		values.put(ContactsColumn.CONTACT_ID, this.contactid);
		values.put(ContactsColumn.type, this.type);
		values.put(ContactsColumn.USERNAME, this.nickname );
		values.put(ContactsColumn.SUBACCOUNT, this.subAccount );
		values.put(ContactsColumn.SUBTOKEN, this.subToken );
		values.put(ContactsColumn.TOKEN, this.token );
		values.put(ContactsColumn.REMARK, this.remark);
		return values;
	}
	
	public ECContacts(String contactId) {
		this.contactid = contactId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(this.contactid);
		dest.writeInt(this.type);
		dest.writeString(this.nickname);
		dest.writeString(this.subAccount);
		dest.writeString(this.subToken);
		dest.writeString(this.token);
		dest.writeString(this.remark);
	}
}
