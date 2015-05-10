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
package com.gao.im.ui;

import android.view.View;

/**
 * <p>Title: CCPActivityImpl.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author 容联•云通讯
 * @date 2014-12-5
 * @version 4.0
 */
public class CCPActivityImpl extends CCPActivityBase {

	final private ECSuperActivity mActivity;
	
	public CCPActivityImpl(ECSuperActivity activity) {
		mActivity  = activity;
	}
	
	@Override
	protected void onInit() {
		mActivity.onActivityInit();
	}

	@Override
	protected int getLayoutId() {
		return mActivity.getLayoutId();
	}

	@Override
	protected View getContentLayoutView() {
		return null;
	}

	@Override
	protected String getClassName() {
		return mActivity.getClass().getName();
	}

	@Override
	protected void onBaseContentViewAttach(View contentView) {
		mActivity.onBaseContentViewAttach(contentView);
	}

}
