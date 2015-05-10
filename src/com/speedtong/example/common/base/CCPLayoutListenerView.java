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
package com.speedtong.example.common.base;

import com.speedtong.example.common.utils.LogUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

/**
 * 获取当前View页面变化状态，并返回改变的高度
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class CCPLayoutListenerView extends FrameLayout {


	private OnCCPViewLayoutListener mOnLayoutListener;
	private OnCCPViewSizeChangedListener mOnSizeChangedListener;
	/**
	 * @param context
	 */
	public CCPLayoutListenerView(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CCPLayoutListenerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(this.mOnLayoutListener != null ) {
			this.mOnLayoutListener.onViewLayout();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "jorstinchan onInitializeAccessibilityEvent");
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "jorstinchan onInitializeAccessibilityNodeInfo");
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
		super.onPopulateAccessibilityEvent(event);
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "jorstinchan onPopulateAccessibilityEvent");
	}
	
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if(this.mOnSizeChangedListener != null ) {
			this.mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
		}
	}
	
	public void setOnLayoutListener(OnCCPViewLayoutListener onLayoutListener) {
		this.mOnLayoutListener = onLayoutListener;
	}

	public void setOnSizeChangedListener(
			OnCCPViewSizeChangedListener onSizeChangedListener) {
		this.mOnSizeChangedListener = onSizeChangedListener;
	}


	public interface OnCCPViewLayoutListener {
		void onViewLayout();
	}
	
	public interface OnCCPViewSizeChangedListener {
		void onSizeChanged(int w, int h, int oldw, int oldh) ;
	}
}
