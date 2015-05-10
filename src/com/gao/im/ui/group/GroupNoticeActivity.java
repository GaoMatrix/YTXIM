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
package com.gao.im.ui.group;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gao.im.R;
import com.gao.im.storage.GroupNoticeSqlManager;
import com.gao.im.ui.CCPListAdapter;
import com.gao.im.ui.ECSuperActivity;
import com.speedtong.sdk.im.group.ECGroupNotice.ECGroupMessageType;

/**
 * 群组通知列表接口
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-31
 * @version 4.0
 */
public class GroupNoticeActivity extends ECSuperActivity implements
		View.OnClickListener {

	/**会话消息列表ListView*/
	private ListView mListView;
	private GroupNoticeAdapter mAdapter;
	
	@Override
	protected int getLayoutId() {
		return R.layout.group_notice_activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
		
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, getString(R.string.app_clear), getString(R.string.app_title_notice), this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		GroupNoticeSqlManager.setAllSessionRead();
		GroupNoticeSqlManager.registerMsgObserver(mAdapter);
		mAdapter.notifyChange();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		GroupNoticeSqlManager.unregisterMsgObserver(mAdapter);
	}
	
	
	/**
	 * 
	 */
	private void initView() {
		if(mListView != null) {
			mListView.setAdapter(null);
		}
		
		mListView = (ListView) findViewById(R.id.group_notice_lv);
		View mCallEmptyView = findViewById(R.id.empty_conversation_tv);
		mListView.setEmptyView(mCallEmptyView);
		mListView.setDrawingCacheEnabled(false);
		mListView.setScrollingCacheEnabled(false);
		
		mListView.setOnItemClickListener(null);
		
		mAdapter = new GroupNoticeAdapter(this);
		mListView.setAdapter(mAdapter);
	}


	public class GroupNoticeAdapter extends CCPListAdapter<NoticeSystemMessage> {

		/**
		 * @param ctx
		 */
		public GroupNoticeAdapter(Context ctx) {
			super(ctx, new NoticeSystemMessage(ECGroupMessageType.APPLY_JOIN));
		}

		@Override
		protected void initCursor() {
			notifyChange();
		}

		@Override
		protected NoticeSystemMessage getItem(NoticeSystemMessage t,
				Cursor cursor) {
			NoticeSystemMessage message = new NoticeSystemMessage(null);
			message.setCursor(cursor);
			return message;
		}

		public final CharSequence getContent(NoticeSystemMessage message) {
			if(message.getType() == ECGroupMessageType.QUIT) {
				
			}
			return message.getContent();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = null;
			ViewHolder mViewHolder = null;
			if (convertView == null || convertView.getTag() == null) {
				view = View.inflate(mContext, R.layout.group_notice_item, null);

				mViewHolder = new ViewHolder();
				mViewHolder.avatar = (ImageView) view .findViewById(R.id.avatar_iv);
				mViewHolder.nickname = (TextView) view .findViewById(R.id.name_tv);
				mViewHolder.content = (TextView) view .findViewById(R.id.content);
				view.setTag(mViewHolder);
			} else {
				view = convertView;
				mViewHolder = (ViewHolder) view.getTag();
			}
			
			NoticeSystemMessage item = getItem(position);
			mViewHolder.avatar.setImageResource(R.drawable.group_head);
			mViewHolder.nickname.setText(item.getGroupName());
			mViewHolder.content.setText(GroupNoticeHelper.getNoticeContent(item.getContent()));
			return view;
		}

		@Override
		protected void notifyChange() {
			Cursor cursor = GroupNoticeSqlManager.getCursor();
			setCursor(cursor);
			super.notifyDataSetChanged();
		}

	}
	

	static class ViewHolder {
		ImageView avatar;
		TextView nickname;
		TextView content;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		case R.id.text_right:
			GroupNoticeSqlManager.delSessions();
			mAdapter.notifyChange();
			break;
		default:
			break;
		}
	}

}
