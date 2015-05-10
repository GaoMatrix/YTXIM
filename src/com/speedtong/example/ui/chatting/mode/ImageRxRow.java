/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.ui.chatting.mode;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.speedtong.example.R;
import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.storage.ImgInfoSqlManager;
import com.speedtong.example.ui.chatting.ChattingActivity;
import com.speedtong.example.ui.chatting.holder.BaseHolder;
import com.speedtong.example.ui.chatting.holder.ImageRowViewHolder;
import com.speedtong.example.ui.chatting.mode.ViewHolderTag.TagType;
import com.speedtong.example.ui.chatting.view.ChattingItemContainer;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;

/**
 * <p>Title: ImageRxRow.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2014-4-17
 * @version 1.0
 */
public class ImageRxRow extends BaseChattingRow {
	
	public ImageRxRow(int type){
		super(type);
	}
	
	
	@Override
	public View buildChatView(LayoutInflater inflater , View convertView) {
		
        //we have a don't have a converView so we'll have to create a new one
        if (convertView == null) {
        	convertView = new ChattingItemContainer(inflater, R.layout.chatting_item_from_picture);
        	
            //use the view holder pattern to save of already looked up subviews
        	ImageRowViewHolder holder = new ImageRowViewHolder(mRowType);
        	convertView.setTag(holder.initBaseHolder(convertView, true));

        }
        //actually setup the view
		return convertView;
	}
	
	@Override
	public void buildChattingData(Context context, BaseHolder baseHolder,
			ECMessage detail, int position) {

		ImageRowViewHolder holder = (ImageRowViewHolder) baseHolder;
		ECFileMessageBody body = (ECFileMessageBody) detail.getBody();
		String userData = detail.getUserData();
		int start = userData.indexOf("THUMBNAIL://");
		if(start != -1) {
			String thumbnail = userData.substring(start);
			holder.chattingContentIv.setImageBitmap(ImgInfoSqlManager.getInstance().getThumbBitmap(thumbnail, 2));
		} else {
			holder.chattingContentIv.setImageBitmap(null);
		}
		
		int startWidth = userData.indexOf("outWidth://");
		int startHeight = userData.indexOf(",outHeight://");
		if(startWidth != -1 && startHeight != -1 && start != -1) {
			int imageMinWidth = DemoUtils.getImageMinWidth(context);
			int width = DemoUtils.getInt(userData.substring(startWidth + "outWidth://".length(), startHeight) , imageMinWidth);
			int height = DemoUtils.getInt(userData.substring(startHeight + ",outHeight://".length(), start - 1) , imageMinWidth);
			holder.chattingContentIv.setMinimumWidth(imageMinWidth);
			if(width != 0) {
				holder.chattingContentIv.setMinimumHeight(height * imageMinWidth /width);
			} else {
				holder.chattingContentIv.setMinimumHeight(imageMinWidth);
			}
		}
		
		ViewHolderTag holderTag = ViewHolderTag.createTag(detail, TagType.TAG_VIEW_PICTURE ,position);
    	OnClickListener onClickListener = ((ChattingActivity) context).getChattingAdapter().getOnClickListener();
    	holder.chattingContentIv.setTag(holderTag);
    	holder.chattingContentIv.setOnClickListener(onClickListener);
	}
	
	@Override
	public int getChatViewType() {

		return ChattingRowType.IMAGE_ROW_RECEIVED.ordinal();
	}


	@Override
	public boolean onCreateRowContextMenu(ContextMenu contextMenu,
			View targetView, ECMessage detail) {
		// TODO Auto-generated method stub
		return false;
	}

}
