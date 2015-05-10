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
package com.gao.im.ui.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gao.im.R;
import com.gao.im.common.utils.ToastUtil;
import com.gao.im.storage.ContactSqlManager;
import com.gao.im.ui.ECSuperActivity;
import com.gao.im.ui.chatting.ChattingActivity;

/**
 * 联系人详情界面
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-19
 * @version 4.0
 */
public class ContactDetailActivity extends ECSuperActivity implements View.OnClickListener{

	public final static String RAW_ID = "raw_id";
	 
	private ImageView mPhotoView;
	private TextView mUsername;
	private TextView mNumber;
	
	private ECContacts mContacts;
	
	private View.OnClickListener onClickListener
		= new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mContacts == null) {
					return ;
				}
				Intent intent = new Intent(ContactDetailActivity.this, ChattingActivity.class);
				intent.putExtra(ChattingActivity.RECIPIENTS, mContacts.getContactid());
				intent.putExtra(ChattingActivity.CONTACT_USER, mContacts.getNickname());
				startActivity(intent);
				setResult(RESULT_OK);
				finish();
			}
		};
	
	@Override
	protected int getLayoutId() {
		return R.layout.layout_contact_detail;
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
		initActivityState(savedInstanceState);
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, R.string.contact_contactDetail, this);
	}


	/**
	 * @param savedInstanceState
	 */
	private void initActivityState(Bundle savedInstanceState) {
		long rawId = getIntent().getLongExtra(RAW_ID, -1);
		if(rawId == -1) {
			ToastUtil.showMessage(R.string.contact_none);
			finish();
			return ;
		}
		mContacts = ContactSqlManager.getContact(rawId);
		
		if(mContacts == null) {
			return ;
		}
		
		mPhotoView.setImageBitmap(ContactLogic.getPhoto(mContacts.getRemark()));
		mUsername.setText(mContacts.getNickname());
		mNumber.setText(mContacts.getContactid());
	}


	/**
	 * 
	 */
	private void initView() {
		mPhotoView = (ImageView) findViewById(R.id.desc);
		mUsername = (TextView) findViewById(R.id.contact_nameTv);
		mNumber = (TextView) findViewById(R.id.contact_numer);
		findViewById(R.id.entrance_chat).setOnClickListener(onClickListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;

		default:
			break;
		}
	}
}
