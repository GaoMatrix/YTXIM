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
 */package com.gao.im.ui.account;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gao.im.R;
import com.gao.im.common.dialog.ECProgressDialog;
import com.gao.im.common.utils.DemoUtils;
import com.gao.im.common.utils.ECPreferenceSettings;
import com.gao.im.common.utils.ECPreferences;
import com.gao.im.core.ClientUser;
import com.gao.im.core.SDKCoreHelper;
import com.gao.im.storage.ContactSqlManager;
import com.gao.im.ui.ECLauncherUI;
import com.gao.im.ui.ECSuperActivity;
import com.gao.im.ui.contact.ContactLogic;
import com.gao.im.ui.contact.ECContacts;
import com.gao.im.ui.manager.CCPAppManager;

/**
 * 
 * @ClassName: AccountChooseActivity
 * @Description: TODO
 * @author Jorstin Chan
 * @date 2013-12-3
 * 
 */
public class AccountChooseActivity extends ECSuperActivity implements
		View.OnClickListener, OnItemClickListener {

	private Button mConfrim;

	private ListView mSubaccountListView;

	private ECContacts seletedAccountBean;

	private ArrayList<ECContacts> aList;

	private ArrayList<ECContacts> contacts;
	private ECProgressDialog mPostingdialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initLayoutResource();

		if (getIntent() != null
				&& getIntent().getParcelableArrayListExtra("arraylist") != null) {
			aList = getIntent().getParcelableArrayListExtra("arraylist");
			initListView(aList);
		}
		
		// 注册激活广播
		registerReceiver(new String[]{getPackageName() + ".inited"});
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, R.string.main_title_select_account, this);

	}
	
	@Override
	protected void handleReceiver(Context context, Intent intent) {
		super.handleReceiver(context, intent);
		
		if((getPackageName() + ".inited").equals(intent.getAction())) {
			try {
				StringBuilder sb = new StringBuilder(seletedAccountBean.getSubAccount()).append(",");
				sb.append(seletedAccountBean.getSubToken()).append(",");
				sb.append(seletedAccountBean.getContactid()).append(",");
				sb.append(seletedAccountBean.getToken()).append(",");
				sb.append(seletedAccountBean.getNickname());
				ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, sb.toString(), true);
				ContactSqlManager.insertContacts(contacts);
				dismissPostingDialog();
				startActivity(new Intent(this , ECLauncherUI.class));
				setResult(RESULT_OK);
				this.finish();
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
		}
	}

	private void initLayoutResource() {

		mConfrim = (Button) findViewById(R.id.account_confrim);
		mConfrim.setOnClickListener(this);

		mSubaccountListView = (ListView) findViewById(R.id.account_list);
		mSubaccountListView.setOnItemClickListener(this);
	}

	private void initListView(ArrayList<ECContacts> objects) {
		contacts = ContactLogic.converContacts(objects);
		SubaccountAdapter adapter = new SubaccountAdapter(this, contacts);
		mSubaccountListView.setAdapter(adapter);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_account_choose;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.account_confrim:

			if (seletedAccountBean != null) {
				mPostingdialog = new ECProgressDialog(this, R.string.login_posting);
				mPostingdialog.show();
				ClientUser clientUser = new ClientUser(seletedAccountBean.getContactid());
				clientUser.setSubSid(seletedAccountBean.getSubAccount());
				clientUser.setSubToken(seletedAccountBean.getSubToken());
				clientUser.setUserToken(seletedAccountBean.getToken());
				clientUser.setUserName(seletedAccountBean.getNickname());
				CCPAppManager.setClientUser(clientUser);
				SDKCoreHelper.init(getApplication());
			}
			break;
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
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

	class SubaccountAdapter extends ArrayAdapter<ECContacts> {

		LayoutInflater mInflater;

		HashMap<Integer, Boolean> isSelected;

		public SubaccountAdapter(Context context, List<ECContacts> objects) {
			super(context, 0, objects);

			mInflater = getLayoutInflater();

			init(objects.size());
		}

		// initialize all checkbox are not selected
		public void init(int size) {
			if (isSelected != null) {
				isSelected.clear();
			} else {
				isSelected = new HashMap<Integer, Boolean>();
			}
			for (int i = 0; i < size; i++) {
				isSelected.put(i, false);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SubaccountHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.subaccount_list_item, null);
				holder = new SubaccountHolder();

				holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
				holder.tv_voip = (TextView) convertView .findViewById(R.id.tv_voip);
				holder.name = (TextView) convertView .findViewById(R.id.voip_name);
				holder.checkBox = (CheckBox) convertView .findViewById(R.id.check_box);
			} else {
				holder = (SubaccountHolder) convertView.getTag();
			}

			try {
				ECContacts account = getItem(position);
				if (account != null) {

					if (!TextUtils.isEmpty(account.getContactid())) {
						holder.avatar.setImageBitmap(ContactLogic.getPhoto(account.getRemark()));
						holder.name.setText(account.getNickname());
						holder.tv_voip.setText(account.getContactid());
						holder.checkBox.setChecked(isSelected.get(position));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		class SubaccountHolder {
			ImageView avatar;
			TextView name;
			TextView tv_voip;
			CheckBox checkBox;
		}

		public HashMap<Integer, Boolean> getIsSelected() {
			return isSelected;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getAdapter() instanceof SubaccountAdapter) {
			SubaccountAdapter adapter = (SubaccountAdapter) parent.getAdapter();

			CheckBox cBox = (CheckBox) view.findViewById(R.id.check_box);
			if (!cBox.isChecked()) {
				adapter.init(adapter.getCount());
			}
			cBox.toggle();

			boolean isChoice = cBox.isChecked();
			adapter.getIsSelected().put(position, isChoice);
			initImageButtonUI(isChoice);
			adapter.notifyDataSetChanged();

			try {
				seletedAccountBean = aList.get(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void initImageButtonUI(boolean isChoice) {
		if (isChoice) {
			mConfrim.setBackgroundResource(R.drawable.login_button_selector);
			mConfrim.setText(R.string.str_start_ex);
			ColorStateList colors = (ColorStateList) getResources()
					.getColorStateList(android.R.color.white);
			mConfrim.setTextColor(colors);
		} else {
			mConfrim.setText(R.string.str_no_choice_account);
			ColorStateList colors = (ColorStateList) getResources()
					.getColorStateList(R.color.ccp_attentoin_color);
			mConfrim.setTextColor(colors);
			mConfrim.setBackgroundResource(R.drawable.select_account_bottom);
		}

		mConfrim.setEnabled(isChoice);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
