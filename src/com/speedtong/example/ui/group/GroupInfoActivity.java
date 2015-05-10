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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.speedtong.example.R;
import com.speedtong.example.common.dialog.ECProgressDialog;
import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.common.utils.ToastUtil;
import com.speedtong.example.core.ClientUser;
import com.speedtong.example.storage.ContactSqlManager;
import com.speedtong.example.storage.GroupMemberSqlManager;
import com.speedtong.example.storage.GroupSqlManager;
import com.speedtong.example.ui.ECSuperActivity;
import com.speedtong.example.ui.contact.ContactDetailActivity;
import com.speedtong.example.ui.contact.ContactLogic;
import com.speedtong.example.ui.contact.ContactSelectListActivity;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.example.ui.manager.CCPAppManager;
import com.speedtong.sdk.im.ECGroup;
import com.speedtong.sdk.im.ECGroupMember;

/**
 * 群组详情界面
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-29
 * @version 4.0
 */
public class GroupInfoActivity extends ECSuperActivity implements
		View.OnClickListener , GroupMemberService.OnSynsGroupMemberListener , GroupService.Callback{

	private static final String TAG = "ECDemo.GroupInfoActivity";
	public final static String GROUP_ID = "group_id";
	
	/**群组ID*/
	private ECGroup mGroup;
	/**群组公告*/
	private EditText mNotice;
	/**群组成员列表*/
	private ListView mListView;
	/**群组成员适配器*/
	private GroupInfoAdapter mAdapter;
	private ECProgressDialog mPostingdialog;
	
	private final AdapterView.OnItemClickListener mItemClickListener
		= new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ECGroupMember item = mAdapter.getItem(position);
				if(item == null) {
					return ;
				}
				
				if("add@yuntongxun.com".equals(item.getVoipAccount())) {
					Intent intent = new Intent(GroupInfoActivity.this, ContactSelectListActivity.class);
					intent.putExtra("group_select_need_result", true);
					intent.putExtra("select_type", false);
					startActivityForResult(intent, 0x2a);
					return ;
				}
				
				ECContacts contact = ContactSqlManager.getContact(item.getVoipAccount());
				if(contact == null || contact.getId() == -1) {
					ToastUtil.showMessage(R.string.contact_none);
					return ;
				}
				Intent intent = new Intent(GroupInfoActivity.this, ContactDetailActivity.class);
				intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
				startActivity(intent);
			}
		};
		
	@Override
	protected int getLayoutId() {
		return R.layout.group_info_activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String groupId = getIntent().getStringExtra(GROUP_ID);
		mGroup = GroupSqlManager.getECGroup(groupId);
		if(mGroup == null) {
			finish();
			return ;
		}
		initView();
		refeshGroupInfo();
		
		GroupService.syncGroupInfo(mGroup.getGroupId());
		GroupMemberService.synsGroupMember(mGroup.getGroupId());
	}

	@Override
	protected void onResume() {
		super.onResume();
		GroupService.addListener(this);
		GroupMemberService.addListener(this);
	}
	/**
	 * 
	 */
	private void initView() {
		mNotice = (EditText) findViewById(R.id.group_notice);
		mListView = (ListView) findViewById(R.id.member_lv);
		mListView.setOnItemClickListener(mItemClickListener);
		mAdapter = new GroupInfoAdapter(this);
		mListView.setAdapter(mAdapter);
		
		findViewById(R.id.red_btn).setOnClickListener(this);
		
		mNotice.setEnabled(isOwner() ? true : false);
		TextView button = (TextView) findViewById(R.id.red_btn);
		button.setText(isOwner() ? R.string.str_group_dissolution : R.string.str_group_quit);
		onSynsGroupMember(mGroup.getGroupId());
	}
	
	/**
	 * 
	 */
	private void refeshGroupInfo() {
		mNotice.setText(mGroup.getDeclare());
		mNotice.setSelection(mNotice.getText().length());
		if(mGroup.getName() != null && mGroup.getName().endsWith("@priategroup.com")) {
			ArrayList<String> member = GroupMemberSqlManager.getGroupMemberID(mGroup.getGroupId());
			if(member != null) {
				ArrayList<String> contactName = ContactSqlManager.getContactName(member.toArray(new String[]{}));
				String chatroomName = DemoUtils.listToString(contactName, ",");
				mGroup.setName(chatroomName);
			}
		}
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, mGroup.getName(), this);
	}
	
	/**
	 * 是否是群组创建者
	 * @return
	 */
	private boolean isOwner() {
		return CCPAppManager.getClientUser().getUserId().equals(mGroup.getOwner());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		case R.id.red_btn:
			mPostingdialog = new ECProgressDialog(this, R.string.group_exit_posting);
			mPostingdialog.show();
			if(isOwner()) {
				GroupService.disGroup(mGroup.getGroupId());
				return ;
			}
			GroupService.quitGroup(mGroup.getGroupId());
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
			String reason = getString(R.string.group_invite_reason, CCPAppManager.getClientUser().getUserName() , mGroup.getName());
			GroupMemberService.inviteMembers(mGroup.getGroupId(), reason, 1, selectUser);
		}
	}

	@Override
	public void onSynsGroupMember(String groupId) {
		dismissPostingDialog();
		if(mGroup == null || !mGroup.getGroupId().equals(groupId)) {
			return ;
		}
		int count = mAdapter.getCount();
		ArrayList<ECGroupMember> members = GroupMemberSqlManager.getGroupMemberWithName(mGroup.getGroupId());
		if(members == null) {
			members = new ArrayList<ECGroupMember>();
		}
		boolean hasSelf = false;
		ClientUser clientUser = CCPAppManager.getClientUser();
		for(ECGroupMember member : members) {
			if(clientUser.getUserId().equals(member.getVoipAccount())) {
				hasSelf = true;
				break;
			}
		}
		if(!hasSelf) {
			ECContacts contact = ContactSqlManager.getContact(clientUser.getUserId());
			ECGroupMember member = new ECGroupMember();
			member.setVoipAccount(contact.getContactid());
			member.setRemark(contact.getRemark());
			member.setDisplayName(contact.getNickname());
			members.add(member);
		}
		
		mAdapter.setData(members);
		if(members != null && count <= members.size()) {
			setListViewHeightBasedOnChildren(mListView);
		}
	}
	
	/**
	 * 动态改变ListView 高度
	 * @param listView
	 */
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() + 2));
		((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
		listView.setLayoutParams(params);
		
		getActivityLayoutView().invalidate();
	}
	
	public class GroupInfoAdapter extends ArrayAdapter<ECGroupMember> {
		Context mContext;
		public GroupInfoAdapter(Context context) {
			super(context, 0);
			mContext = context;
		}

		public void setData(List<ECGroupMember> data) {
			clear();
			if (data != null) {
				for (ECGroupMember appEntry : data) {
					add(appEntry);
				}
			}
			
			if(isOwner()) {
				ECGroupMember add = new ECGroupMember();
				add.setVoipAccount("add@yuntongxun.com");
				add(add);
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = null;
			ViewHolder mViewHolder = null;
			if(convertView == null || convertView.getTag() == null) {
				view = View.inflate(mContext, R.layout.group_member_item, null);

				mViewHolder = new ViewHolder();
				mViewHolder.mAvatar = (ImageView) view.findViewById(R.id.group_card_item_avatar_iv);
				mViewHolder.name_tv = (TextView) view.findViewById(R.id.group_card_item_nick);
				mViewHolder.operation = (Button) view.findViewById(R.id.contact_operation_btn);
				
				view.setTag(mViewHolder);
			} else {
				view = convertView;
				mViewHolder = (ViewHolder) view.getTag();
			}
			
			final ECGroupMember item = getItem(position);
			if(item != null) {
				
				if(item.getVoipAccount().equals("add@yuntongxun.com") && position == getCount() - 1) {
					mViewHolder.mAvatar.setImageResource(R.drawable.add_contact_selector);
					mViewHolder.name_tv.setText(R.string.str_group_invite);
					mViewHolder.operation.setVisibility(View.INVISIBLE);
				} else {
					mViewHolder.mAvatar.setImageBitmap(ContactLogic.getPhoto(item.getRemark()));
					mViewHolder.name_tv.setText(item.getDisplayName());
					if(isOwner() && !CCPAppManager.getClientUser().getUserId().equals(item.getVoipAccount())) {
						mViewHolder.operation.setVisibility(View.VISIBLE);
						mViewHolder.operation.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								mPostingdialog = new ECProgressDialog(GroupInfoActivity.this, R.string.group_remove_member_posting);
								mPostingdialog.show();
								GroupMemberService.removerMember(mGroup.getGroupId(), new String[]{item.getVoipAccount()});
							}
						});
					} else {
						mViewHolder.operation.setVisibility(View.INVISIBLE);
					}
				}
				
			}
			
			return view;
		}
		
		class ViewHolder {
			/**头像*/
			ImageView mAvatar;
			/**名称*/
			TextView name_tv;
			/**踢出按钮*/
			Button operation;
			
		}
	}

	@Override
	public void onSyncGroup() {
		
	}

	@Override
	public void onSyncGroupInfo(String groupId) {
		if(mGroup == null && !mGroup.getGroupId().equals(groupId)) {
			return ;
		}
		mGroup = GroupSqlManager.getECGroup(groupId);
		refeshGroupInfo();
	}

	@Override
	public void onGroupDel(String groupId) {
		dismissPostingDialog();
		ECGroup ecGroup = GroupSqlManager.getECGroup(mGroup.getGroupId());
		setResult(RESULT_OK);
		if(ecGroup == null) {
			// 群组被解散
			finish();
			return ;
		}
		finish();
		// 更新群组界面 已经退出群组
	}
}
