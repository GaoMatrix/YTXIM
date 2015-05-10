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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.speedtong.example.R;
import com.speedtong.example.common.dialog.ECProgressDialog;
import com.speedtong.example.storage.ContactSqlManager;
import com.speedtong.example.storage.GroupSqlManager;
import com.speedtong.example.ui.ECSuperActivity;
import com.speedtong.example.ui.chatting.ChattingActivity;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.sdk.im.ECGroup;

/**
 * 申请加入群组界面
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-31
 * @version 4.0
 */
public class ApplyWithGroupPermissionActivity extends ECSuperActivity implements
		GroupService.Callback ,View.OnClickListener , GroupService.OnApplyGroupCallbackListener{

	/**群组ID*/
	private ECGroup mGroup;
	/**群组公告*/
	private EditText mNotice;
	/**
	 * 群组基本信息
	 */
	private GroupProfileView mGroupProfileView;
	private ECProgressDialog mPostingdialog;
	
	@Override
	protected int getLayoutId() {
		return R.layout.apply_group_activity;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String groupId = getIntent().getStringExtra(GroupInfoActivity.GROUP_ID);
		mGroup = GroupSqlManager.getECGroup(groupId);
		if(mGroup == null) {
			finish();
			return ;
		}
		
		initView();
		syncGroupInf(mGroup.getGroupId());
		GroupService.syncGroupInfo(mGroup.getGroupId());
	}

	/**
	 * 初始化
	 */
	private void initView() {
		mGroupProfileView = (GroupProfileView) findViewById(R.id.group_file);
		mNotice = (EditText) findViewById(R.id.group_notice);
		mNotice.setEnabled(false);
		
		TextView view = (TextView) findViewById(R.id.red_btn);
		view.setBackgroundResource(R.drawable.registration_btn);
		view.setOnClickListener(this);
		view.setText(R.string.group_apply_btn);
	}
	
	private void syncGroupInf(String groupId) {
		mGroup = GroupSqlManager.getECGroup(groupId);
		if(mGroup == null) {
			return ;
		}
		mGroupProfileView.setNameText(mGroup.getName());
		ECContacts contact = ContactSqlManager.getContact(mGroup.getOwner());
		if(contact != null) {
			mGroupProfileView.setOwnerText(contact.getNickname());
		}
		mGroupProfileView.setGroupIdText(mGroup.getGroupId());
		
		
		mNotice.setText(mGroup.getDeclare());
		mNotice.setSelection(mNotice.getText().length());
		
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, mGroup.getName(), this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		GroupService.addListener(this);
	}

	@Override
	public void onSyncGroup() {
		
	}

	@Override
	public void onSyncGroupInfo(String groupId) {
		syncGroupInf(groupId);
	}

	@Override
	public void onGroupDel(String groupId) {
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		case R.id.red_btn:
			mPostingdialog = new ECProgressDialog(this, R.string.loading_press);
			mPostingdialog.show();
			String userName = CCPAppManager.getClientUser().getUserName();
			GroupService.applyGroup(mGroup.getGroupId(), getString(R.string.group_apply_reason, userName), this);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 关闭对话框
	 */
	private void dismissPostingDialog() {
		if(mPostingdialog == null || !mPostingdialog.isShowing()) {
			return ;
		}
		mPostingdialog.dismiss();
		mPostingdialog = null;
	}

	@Override
	public void onApplyGroup(boolean success) {
		dismissPostingDialog();
		if(!success) {
			return ;
		}
		Intent intent = new Intent(ApplyWithGroupPermissionActivity.this , ChattingActivity.class);
		intent.putExtra(ChattingActivity.RECIPIENTS, mGroup.getGroupId());
		intent.putExtra(ChattingActivity.CONTACT_USER, mGroup.getName());
		startActivity(intent);
		finish();
	}
	
}
