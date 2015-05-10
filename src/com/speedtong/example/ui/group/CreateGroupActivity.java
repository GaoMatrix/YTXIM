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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.speedtong.example.R;
import com.speedtong.example.common.dialog.ECProgressDialog;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.core.SDKCoreHelper;
import com.speedtong.example.storage.GroupMemberSqlManager;
import com.speedtong.example.storage.GroupSqlManager;
import com.speedtong.example.ui.ECSuperActivity;
import com.speedtong.example.ui.chatting.ChattingActivity;
import com.speedtong.example.ui.contact.ContactSelectListActivity;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECGroupManager;
import com.speedtong.sdk.im.ECGroup;

/**
 * 群组创建功能
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-27
 * @version 4.0
 */
public class CreateGroupActivity extends ECSuperActivity implements
		View.OnClickListener , ECGroupManager.OnCreatGroupListener , GroupMemberService.OnSynsGroupMemberListener{

	private static final String TAG = "ECDemo.CreateGroupActivity";
	/**群组名称*/
	private EditText mNameEdit;
	/**群组公告*/
	private EditText mNoticeEdit;
	/**创建按钮*/
	private Button mCreateBtn;
	/**创建的群组*/
	private ECGroup group;
	private ECProgressDialog mPostingdialog;
	@Override
	protected int getLayoutId() {
		return R.layout.new_group;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, R.string.app_title_create_new_group, this);
		
		initView();
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

	/**
	 * 
	 */
	private void initView() {
		mNameEdit = (EditText) findViewById(R.id.group_name);
		mNoticeEdit = (EditText) findViewById(R.id.group_notice);
		mCreateBtn = (Button) findViewById(R.id.create);
		mCreateBtn.setOnClickListener(this);
		mCreateBtn.setEnabled(false);
		mNameEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(checkNameEmpty()) {
					mCreateBtn.setEnabled(true);
					return ;
				} 
				mCreateBtn.setEnabled(false);
			}
		});
		
		
	}
	
	/**
	 * @return
	 */
	private boolean checkNameEmpty() {
		return mNameEdit != null && mNameEdit.getText().toString().trim().length() > 0;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		case R.id.create:
			hideSoftKeyboard();
			mPostingdialog = new ECProgressDialog(this, R.string.create_group_posting);
			mPostingdialog.show();
			ECGroupManager ecGroupManager = SDKCoreHelper.getECGroupManager();
			if(!checkNameEmpty() || ecGroupManager == null) {
				return ;
			}
			// 调用API创建群组、处理创建群组接口回调
			ecGroupManager.createGroup(getGroup(), this);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 创建群组参数
	 * @return
	 */
	private ECGroup getGroup() {
		ECGroup group =  new ECGroup();
		// 设置群组名称
		group.setName(mNameEdit.getText().toString().trim());
		// 设置群组公告
		group.setDeclare(mNoticeEdit.getText().toString().trim());
		// 临时群组（100人）
		group.setGroupType(0);
		// 群组验证权限，需要身份验证
		group.setPermission(1);
		// 设置群组创建者
		group.setOwner(CCPAppManager.getClientUser().getUserId());
		return group;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GroupMemberService.addListener(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtil.d(TAG ,"onActivityResult: requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data);
		
		// If there's no data (because the user didn't select a picture and
	    // just hit BACK, for example), there's nothing to do.
		if (requestCode == 0x2a) {
			if (data == null) {
				return;
			}
		} else if (resultCode != RESULT_OK) {
			LogUtil.d("onActivityResult: bail due to resultCode=" + resultCode);
			return;
		}
		
		String[] selectUser = data.getStringArrayExtra("Select_Conv_User");
		if(selectUser != null && selectUser.length > 0) {
			mPostingdialog = new ECProgressDialog(this, R.string.invite_join_group_posting);
			mPostingdialog.show();
			GroupMemberService.inviteMembers(group.getGroupId(), "", 1, selectUser);
		}
		
	}
	

	@Override
	public void onComplete(ECError error) {
		
	}

	@Override
	public void onCreatGroupComplete(ECError error, ECGroup group) {
		if("000000".equals(error.errorCode)) {
			// 创建的群组实例化到数据库
			// 其他的页面跳转逻辑
			GroupSqlManager.insertGroup(group, true, false);
			this.group = group;
			Intent intent = new Intent(this, ContactSelectListActivity.class);
			intent.putExtra("group_select_need_result", true);
			startActivityForResult(intent, 0x2a);
		}
		dismissPostingDialog();
	}

	@Override
	public void onSynsGroupMember(String groupId) {
		dismissPostingDialog();
		Intent intent = new Intent(CreateGroupActivity.this , ChattingActivity.class);
		intent.putExtra(ChattingActivity.RECIPIENTS, groupId);
		intent.putExtra(ChattingActivity.CONTACT_USER, group.getName());
		startActivity(intent);
		finish();
	}
}
