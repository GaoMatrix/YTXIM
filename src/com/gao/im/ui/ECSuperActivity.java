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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.gao.im.common.utils.LogUtil;
import com.gao.im.common.view.TopBarView;
import com.gao.im.ui.manager.ActivityTaskUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author 容联•云通讯
 * @date 2014-12-5
 * @version 4.0
 */
public abstract class ECSuperActivity extends FragmentActivity {

	private static final String TAG = LogUtil.getLogUtilsTag(ECSuperActivity.class);
	
	/**
	 * 初始化应用ActionBar
	 */
	private CCPActivityBase mBaseActivity = new CCPActivityImpl(this);
	/**
	 * 初始化广播接收器
	 */
	private InternalReceiver internalReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBaseActivity.init(getBaseContext(), this);
		LogUtil.d(TAG, "checktask onCreate:" + super.getClass().getSimpleName() + "#0x" 
				+ super.hashCode() + ", taskid:" + getTaskId() + ", task:" + new ActivityTaskUtils(this));
	}
	
	protected final void registerReceiver(String[] actionArray) {
		if (actionArray == null) {
			return;
		}
		IntentFilter intentfilter = new IntentFilter();
		for (String action : actionArray) {
			intentfilter.addAction(action);
		}
		if (internalReceiver == null) {
			internalReceiver = new InternalReceiver();
		}
		registerReceiver(internalReceiver, intentfilter);
	}
	
	
	/**
	 * The sub Activity implement, set the Ui Layout
	 * @return
	 */
	protected abstract int getLayoutId();
	
	protected void onActivityInit() {
		
	}
	
	/**
	 * 如果子界面需要拦截处理注册的广播
	 * 需要实现该方法
	 * @param context
	 * @param intent
	 */
	protected void handleReceiver(Context context, Intent intent) {
		// 广播处理
	}
	
	public void onBaseContentViewAttach(View contentView) {
		setContentView(contentView);
	}
	
	public FragmentActivity getActionBarActivityContext() {
		return mBaseActivity.getFragmentActivity();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mBaseActivity.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onResume() {
		// HSCoreService
		super.onResume();
		mBaseActivity.onResume();
		MobclickAgent.onResume(this);
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		LogUtil.d(TAG, "checktask onCreate:" + super.getClass().getSimpleName() + "#0x" 
				+ super.hashCode() + ", taskid:" + getTaskId() + ", task:" + new ActivityTaskUtils(this));
		super.onDestroy();
		mBaseActivity.onDestroy();
		try {
			unregisterReceiver(internalReceiver);
		} catch (Exception e) {
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(mBaseActivity.onKeyDown(keyCode, event)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(mBaseActivity.onKeyUp(keyCode, event)) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	
	public void hideSoftKeyboard() {
		mBaseActivity.hideSoftKeyboard();
	}
	
	
	/**
	 * 跳转
	 * @param clazz
	 * @param intent
	 */
	protected void startCCPActivity(Class<? extends Activity> clazz , Intent intent) {
		intent.setClass(this, clazz);
	    startActivity(intent);
	}
	
	
	// Internal calss.
	private class InternalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null || intent.getAction() == null ) {
				return ;
			}
			handleReceiver(context, intent);
		}
	}
	
	public Activity getActivitContext() {
		if(getParent() != null) {
			return getParent();
		}
		return null;
	}
	
	public TopBarView getTopBarView() {
		return mBaseActivity.getTopBarView();
	}
	
	/**
	 * 设置ActionBar标题
	 * @param resid
	 */
	public void setActionBarTitle(int resid) {
		mBaseActivity.setActionBarTitle(getString(resid));
	}
	
	/**
	 * 设置ActionBar标题
	 * @param text
	 */
	public void setActionBarTitle(CharSequence text) {
		mBaseActivity.setActionBarTitle(text);
	}
	
	/**
	 * 返回ActionBar 标题
	 * @return
	 */
	public final CharSequence getActionBarTitle() {
		return mBaseActivity.getActionBarTitle();
	}
    
    
    /**
     * @see CASActivity#getLayoutId()
     */
    public View getActivityLayoutView() {
    	return mBaseActivity.getActivityLayoutView();
    }
    
    /**
     * 
     */
    public final void showTitleView() {
    	mBaseActivity.showTitleView();
    }
    
    /**
     * 
     */
    public final void hideTitleView() {
    	mBaseActivity.hideTitleView();
    }
}
