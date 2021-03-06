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
package com.gao.im.ui.chatting.mode;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;

import com.gao.im.R;
import com.gao.im.ui.chatting.ChattingActivity;
import com.gao.im.ui.chatting.holder.BaseHolder;
import com.gao.im.ui.chatting.holder.FileRowViewHolder;
import com.gao.im.ui.chatting.mode.ViewHolderTag.TagType;
import com.gao.im.ui.chatting.view.ChattingItemContainer;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;

/**
 * <p>Title: FileRxRow.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2014-4-17
 * @version 1.0
 */
public class FileRxRow extends BaseChattingRow{

	public FileRxRow(int type) {
		super(type);
	}
	

	@Override
	public View buildChatView(LayoutInflater inflater, View convertView) {
        //we have a don't have a converView so we'll have to create a new one
        if (convertView == null || convertView.getTag() == null) {
            convertView = new ChattingItemContainer(inflater, R.layout.chatting_item_file_from);

            //use the view holder pattern to save of already looked up subviews
            FileRowViewHolder holder = new FileRowViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, true));
        }
		return convertView;
	}

	@Override
	public void buildChattingData(final Context context, BaseHolder baseHolder,
			ECMessage detail, int position) {
		FileRowViewHolder holder = (FileRowViewHolder) baseHolder;
		if(detail != null) {

			ECMessage msg = detail;
			ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
			holder.contentTv.setText(body.getFileName());
			ViewHolderTag holderTag = ViewHolderTag.createTag(detail, TagType.TAG_VIEW_FILE, position);
        	holder.contentTv.setTag(holderTag);
        	holder.contentTv.setOnClickListener(((ChattingActivity) context).getChattingAdapter().getOnClickListener());
        	
        }
	}
	
	@Override
	public int getChatViewType() {
		return ChattingRowType.FILE_ROW_RECEIVED.ordinal();
	}

	@Override
	public boolean onCreateRowContextMenu(ContextMenu contextMenu,
			View targetView, ECMessage detail) {
		// TODO Auto-generated method stub
		return false;
	}

}
