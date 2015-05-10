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
package com.speedtong.example.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.speedtong.example.R;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.common.view.NetWarnBannerView;
import com.speedtong.example.core.SDKCoreHelper;
import com.speedtong.example.core.SDKCoreHelper.Connect;
import com.speedtong.example.storage.GroupNoticeSqlManager;
import com.speedtong.example.storage.IMessageSqlManager;
import com.speedtong.example.ui.CCPListAdapter.OnListAdapterCallBackListener;
import com.speedtong.example.ui.chatting.ChattingActivity;
import com.speedtong.example.ui.chatting.mode.Conversation;
import com.speedtong.example.ui.group.GroupNoticeActivity;
import com.speedtong.sdk.debug.ECLog4Util;

/**
 * 会话界面
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class ConversationListFragment extends TabFragment implements OnListAdapterCallBackListener{
	
	private static final String TAG = "ECSDK_Demo.ConversationListFragment";

	/**会话消息列表ListView*/
	private ListView mListView;
	private ConversationAdapter mAdapter;
	private NetWarnBannerView mBannerView;
	private OnUpdateMsgUnreadCountsListener mAttachListener;
	
	final private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			if(mAdapter != null) {
				int headerViewsCount = mListView.getHeaderViewsCount();
				if(position < headerViewsCount) {
					return;
				}
				int _position = position - headerViewsCount;
				
				if(mAdapter == null || mAdapter.getItem(_position) == null) {
					return ;
				}
				
				Conversation conversation = mAdapter.getItem(_position);
				if(GroupNoticeSqlManager.CONTACT_ID.equals(conversation.getSessionId())) {
					Intent intent = new Intent(getActivity() , GroupNoticeActivity.class);
					startActivity(intent);
					return ;
				}
				Intent intent = new Intent(getActivity() , ChattingActivity.class);
				intent.putExtra(ChattingActivity.RECIPIENTS, conversation.getSessionId());
				intent.putExtra(ChattingActivity.CONTACT_USER, conversation.getUsername());
				startActivity(intent);
			}
		}
	};
	
	@Override
	protected void onTabFragmentClick() {

	}

	@Override
	protected void onReleaseTabUI() {

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ECLog4Util.v(TAG, "onCreate");
		
		initView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateConnectState();
		IMessageSqlManager.registerMsgObserver(mAdapter);
		mAdapter.notifyChange();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mAttachListener = (OnUpdateMsgUnreadCountsListener) activity;
         } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUpdateMsgUnreadCountsListener");
        }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		IMessageSqlManager.unregisterMsgObserver(mAdapter);
	}

	/**
	 * 
	 */
	private void initView() {
		if(mListView != null) {
			mListView.setAdapter(null);
			
			if(mBannerView != null) {
				mListView.removeHeaderView(mBannerView);
			}
		}
		
		mListView = (ListView) findViewById(R.id.main_chatting_lv);
		View mEmptyView = findViewById(R.id.empty_conversation_tv);
		mListView.setEmptyView(mEmptyView);
		mListView.setDrawingCacheEnabled(false);
		mListView.setScrollingCacheEnabled(false);
		
		mListView.setOnItemClickListener(mItemClickListener);
		
		mBannerView = new NetWarnBannerView(getActivity());
		mListView.addHeaderView(mBannerView);
		mAdapter = new ConversationAdapter(getActivity() , this);
		mListView.setAdapter(mAdapter);
	}
	
	
	public void  updateConnectState() {
		Connect connect = SDKCoreHelper.getConnectState();
		if(connect == Connect.CONNECTING) {
			mBannerView.setNetWarnText(getString(R.string.connecting_server));
			mBannerView.reconnect(true);
		} else if (connect == Connect.ERROR) {
			mBannerView.setNetWarnText(getString(R.string.connect_server_error));
		} else if (connect == Connect.SUCCESS) {
			mBannerView.hideWarnBannerView();
		}
		LogUtil.d(TAG, "updateConnectState connect :" + connect.name());
	}

	@Override
	protected int getLayoutId() {
		return R.layout.conversation;
	}

	
	public interface OnUpdateMsgUnreadCountsListener {
		void OnUpdateMsgUnreadCounts();
	}


	@Override
	public void OnListAdapterCallBack() {
		if(mAttachListener != null) {
			mAttachListener.OnUpdateMsgUnreadCounts();
		}
	}
}
