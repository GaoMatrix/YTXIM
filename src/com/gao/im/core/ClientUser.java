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
package com.gao.im.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用户登录账号信息
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-12
 * @version 4.0
 */
public class ClientUser implements Parcelable{
	
	public static final Parcelable.Creator<ClientUser> CREATOR = new Parcelable.Creator<ClientUser>() {
		public ClientUser createFromParcel(Parcel in) {
			return new ClientUser(in);
		}

		public ClientUser[] newArray(int size) {
			return new ClientUser[size];
		}
	};
	 
	/**用户注册VoIp账号*/
	private String userId;
	/**用户注册VoIp账号Token*/
	private String userToken;
	/**用户注册子账号*/
	private String subSid;
	/**用户注册子账号Token*/
	private String subToken;
	/**用户昵称*/
	private String userName;
	
	private ClientUser(Parcel in) {
		this.userId = in.readString();
		this.userToken = in.readString();
		this.subSid = in.readString();
		this.subToken = in.readString();
		this.userName = in.readString();
	}
	
	public ClientUser(String userId) {
		this.userId = userId;
	}
	
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userToken
	 */
	public String getUserToken() {
		return userToken;
	}

	/**
	 * @param userToken the userToken to set
	 */
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	/**
	 * @return the subSid
	 */
	public String getSubSid() {
		return subSid;
	}

	/**
	 * @param subSid the subSid to set
	 */
	public void setSubSid(String subSid) {
		this.subSid = subSid;
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
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.userId);
		dest.writeString(this.userToken);
		dest.writeString(this.subSid);
		dest.writeString(this.subToken);
		dest.writeString(this.userName);
	}

}
