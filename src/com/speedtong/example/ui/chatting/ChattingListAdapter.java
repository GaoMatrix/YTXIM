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
package com.speedtong.example.ui.chatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.speedtong.example.R;
import com.speedtong.example.common.utils.DateUtil;
import com.speedtong.example.common.utils.MediaPlayTools;
import com.speedtong.example.ui.chatting.holder.BaseHolder;
import com.speedtong.example.ui.chatting.mode.BaseChattingRow;
import com.speedtong.example.ui.chatting.mode.ChattingRowType;
import com.speedtong.example.ui.chatting.mode.DescriptionRxRow;
import com.speedtong.example.ui.chatting.mode.DescriptionTxRow;
import com.speedtong.example.ui.chatting.mode.FileRxRow;
import com.speedtong.example.ui.chatting.mode.FileTxRow;
import com.speedtong.example.ui.chatting.mode.IChattingRow;
import com.speedtong.example.ui.chatting.mode.ImageRxRow;
import com.speedtong.example.ui.chatting.mode.ImageTxRow;
import com.speedtong.example.ui.chatting.mode.VoiceRxRow;
import com.speedtong.example.ui.chatting.mode.VoiceTxRow;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.im.ECMessage.Direction;

/**
 * @author 容联•云通讯
 * @date 2014-12-9
 * @version 4.0
 */
public class ChattingListAdapter extends BaseAdapter {

	private List<ECMessage> details;
	protected View.OnClickListener mOnClickListener;
	/**当前语音播放的Item*/
	public int mVoicePosition = -1;
	/**聊天所在的Activity*/
	private ChattingActivity mContext;
	/**需要显示时间的Item position*/
	private ArrayList<String> mShowTimePosition;
	/**初始化所有类型的聊天Item 集合*/
	private HashMap<Integer, IChattingRow> mRowItems ;
	/**时间显示控件的垂直Padding*/
	private int mVerticalPadding;
	/**时间显示控件的横向Padding*/
	private int mHorizontalPadding;
	/**消息联系人名称显示颜色*/
	private ColorStateList[] mChatNameColor;
	
	/**
	 * @param ctx
	 * @param t
	 */
	public ChattingListAdapter(ChattingActivity ctx) {
		mContext = ctx;
		mRowItems = new HashMap<Integer, IChattingRow>();
		mShowTimePosition = new ArrayList<String>();
		initRowItems();
		details = new ArrayList<ECMessage>();
		
		// 初始化聊天消息点击事件回调
		mOnClickListener = new ChattingListClickListener(mContext , null);
		mVerticalPadding = mContext.getResources().getDimensionPixelSize(R.dimen.SmallestPadding);
		mHorizontalPadding = mContext.getResources().getDimensionPixelSize(R.dimen.LittlePadding);
		mChatNameColor = new ColorStateList[]{
				mContext.getResources().getColorStateList(R.color.white),
				mContext.getResources().getColorStateList(R.color.chatroom_user_displayname_color)};
	
	}
	
	public void setData(List<ECMessage> data) {
		details.clear();
		if(data != null) {
			for(ECMessage iMessage : data){
				initRowType(iMessage);
			}
		} 
		notifyDataSetChanged();
		
	}
	
	/**
	 * 初始化不同的聊天Item View
	 */
	void initRowItems() {
		mRowItems.put(Integer.valueOf(1), new ImageRxRow(1));
		mRowItems.put(Integer.valueOf(2), new ImageTxRow(2));
		mRowItems.put(Integer.valueOf(3), new FileRxRow(3));
		mRowItems.put(Integer.valueOf(4), new FileTxRow(4));
		mRowItems.put(Integer.valueOf(5), new VoiceRxRow(5));
		mRowItems.put(Integer.valueOf(6), new VoiceTxRow(6));
		mRowItems.put(Integer.valueOf(7), new DescriptionRxRow(7));
		mRowItems.put(Integer.valueOf(8), new DescriptionTxRow(8));
	}
	
	
	@Override
	public int getCount() {
		if(details == null) {
			return 0;
		}
		return details.size();
	}
	
	@Override
	public ECMessage getItem(int position) {
		if(getCount() == 0) {
			return null;
		}
		return details.get(position);
	}

	@Override
	public long getItemId(int position) {
		if(getCount() == 0) {
			return 0L;
		}
		return details.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ECMessage item = getItem(position);
		if(item == null) {
			return null;
		}
		boolean showTimer = false;
		if(position == 0) {
			showTimer = true;
		}
		if(position != 0) {
			ECMessage previousItem = getItem(position - 1);
			if(mShowTimePosition.contains(item.getId())
					|| (item.getMsgTime() - previousItem.getMsgTime() >= 180000L)) {
				showTimer = true;
				
			}
		}
		
		int messageType = ChattingsRowUtils.getChattingMessageType(item.getType());
		BaseChattingRow chattingRow = getBaseChattingRow(messageType, item.getDirection() == Direction.SEND);
		View chatView = chattingRow.buildChatView(LayoutInflater.from(mContext), convertView);
		BaseHolder baseHolder = (BaseHolder) chatView.getTag();
		
		if(showTimer) {
			baseHolder.getChattingTime().setVisibility(View.VISIBLE);
			baseHolder.getChattingTime().setBackgroundResource(R.drawable.chat_tips_bg);
			baseHolder.getChattingTime().setText(DateUtil.getDateString(item.getMsgTime(), DateUtil.SHOW_TYPE_CALL_LOG).trim());
			baseHolder.getChattingTime().setTextColor(mChatNameColor[0]);
			baseHolder.getChattingTime().setPadding(mHorizontalPadding, mVerticalPadding, mHorizontalPadding, mVerticalPadding);
		} else {
			baseHolder.getChattingTime().setVisibility(View.GONE);
			baseHolder.getChattingTime().setShadowLayer(0.0F, 0.0F, 0.0F, 0);
			baseHolder.getChattingTime().setBackgroundResource(0);
		}
		
		chattingRow.buildChattingBaseData(mContext, baseHolder, item, position);
		
		if(baseHolder.getChattingUser() != null && baseHolder.getChattingUser().getVisibility() == View.VISIBLE) {
			baseHolder.getChattingUser().setTextColor(mChatNameColor[1]);
			baseHolder.getChattingUser().setShadowLayer(0.0F, 0.0F, 0.0F, 0);
		}
		return chatView;
	}
	
	/**
	 * 消息类型数
	 */
	@Override
	public int getViewTypeCount() {
		return ChattingRowType.values().length;
	}
	
	/**
	 * 返回消息的类型ID
	 */
	@Override
	public int getItemViewType(int position) {
		ECMessage message = getItem(position);
		return getBaseChattingRow(ChattingsRowUtils.getChattingMessageType(message.getType()),message.getDirection() == Direction.SEND).getChatViewType();
	}
	
	/**
	 * 根据消息类型返回相对应的消息Item
	 * @param rowType
	 * @param isSend
	 * @return
	 */
	public BaseChattingRow getBaseChattingRow(int rowType , boolean isSend) {
		StringBuilder builder = new StringBuilder("C").append(rowType);
		
		if(isSend) {
			builder.append("T");
		} else {
			builder.append("R");
		}
		
		ChattingRowType fromValue = ChattingRowType.fromValue(builder.toString());
		IChattingRow iChattingRow = mRowItems.get(fromValue.getId().intValue());
		return (BaseChattingRow) iChattingRow;
	}
	
	/**
	 * 下拉刷新
	 * @param data
	 */
	public void insertDataArrays(List<ECMessage> data) {
		if(data != null) {
			if(getCount() > 0) {
				ECMessage item = getItem(0);
				if(item != null) {
					mShowTimePosition.add(item.getMsgId());
				}
			}
			for(int i = data.size() -1 ; i >=0 ; i --) {
				insertData(data.get(i) , 0);
				
			}
		}
	}
	
	public void insertDataArraysAfter(List<ECMessage> data) {
		if(data != null) {
			for(int i = data.size() -1 ; i >=0 ; i --) {
				insertData(data.get(i) , getCount() - 1);
				
			}
		}
	}
	
	/**
	 * @param data
	 */
	public void insertData(ECMessage data) {
		if(getCount() > 0) {
			if(getCount() == 1) {
				insertData(data, getCount());
				return;
			}
			insertData(data, getCount() - 1);
			return;
		}
		insertData(data , 0);
	}
	
	private void insertData(ECMessage data , int index) {
		if(data != null) {
			
			if(index < 0) {
				index = 0;
			}
			
			initRowType(data ,index);
			notifyDataSetChanged();
		}
	}
	
	/**
	 * @param iMessage
	 */
	private void initRowType(ECMessage iMessage) {
		initRowType(iMessage, details.size());
	}

	/**
	 * Gets the message type row 
	 * @param iMessage
	 */
	private void initRowType(ECMessage iMessage , int index) {
		if(index != 0) {
			index = details.size();
		}
		details.add(index , iMessage);
		
	}
	
	/**
	 * @param detail
	 */
	public void removeMsg(ECMessage detail) {
		if(detail != null) {
			int removeIndex = -1;
			//details.
			for(int i = 0 ; details != null && i < details.size() ; i++) {
				ECMessage iMessageDetail = details.get(i);
				if(iMessageDetail.getId() != detail.getId()) {
					continue;
				}
				removeIndex = i;
				break;
			}
			
			if(removeIndex != -1) {
				details.remove(removeIndex);
				notifyDataSetChanged();
			}
		}
	}

	public Integer getMessageRowType(ECMessage iMessage) {
		return ChattingsRowUtils.getMessageRowType(iMessage);
	}

	/**
	 * 当前语音播放的位置
	 * @param position
	 */
	public void setVoicePosition(int position) {
		mVoicePosition = position;
	}

	/**
	 * @return the mOnClickListener
	 */
	public View.OnClickListener getOnClickListener() {
		return mOnClickListener;
	}
	
	/**
	 * 
	 */
	public void onPause() {
		mVoicePosition = -1;
		MediaPlayTools.getInstance().stop();
	}
	
	public void onResume() {
		notifyDataSetChanged();
	}
	
	
	/**
	 * 
	 */
	public void onDestory() {
		if(details != null) {
			details.clear();
			details = null;
		}
		
		mRowItems.clear();
		mRowItems = null;
		mOnClickListener = null;
	}

}
