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
package com.speedtong.example.ui.account;

import java.util.ArrayList;

import jxl.demo.Demo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.speedtong.example.ECApplication;
import com.speedtong.example.R;
import com.speedtong.example.common.base.CCPFormInputView;
import com.speedtong.example.common.dialog.ECProgressDialog;
import com.speedtong.example.common.utils.DemoUtils;
import com.speedtong.example.common.utils.DensityUtil;
import com.speedtong.example.common.utils.ECPreferenceSettings;
import com.speedtong.example.common.utils.ECPreferences;
import com.speedtong.example.common.utils.LogUtil;
import com.speedtong.example.common.utils.OrgJosnUtils;
import com.speedtong.example.common.utils.ToastUtil;
import com.speedtong.example.ui.ECSuperActivity;
import com.speedtong.example.ui.contact.ECContacts;
import com.speedtong.sdk.core.CCPParameters;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.exception.CCPException;
import com.speedtong.sdk.net.AsyncECRequestRunner;
import com.speedtong.sdk.net.InnerRequestListener;

/**
 * ECSDK_Demo 登陆界面
 * 
 * @author 容联•云通讯
 * @date 2014-12-8
 * @version 4.0
 */
public class LoginActivity extends ECSuperActivity implements
		View.OnClickListener {
	
	private static final String TAG = "ECDemo.LoginActivity";
	private Button mExperLogin;
	private LinearLayout loginView;
	private LinearLayout registView;
	private Button registButton;
	private Button loginButton;

	private EditText mAccountEdt;
	private EditText mPasswordEdt;
	private TextView tv_regist_tip1;

	private CCPFormInputView mAccountInputView;
	private CCPFormInputView mPwdInputView;
	private ECProgressDialog mPostingdialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initResourceRefs();

		initExperienceView();

		initialize(savedInstanceState);
		
		// 自动补全登陆账号信息
		String yunAccount = DemoUtils.getLoginAccount();
		if(!TextUtils.isEmpty(yunAccount)) {
			mAccountEdt.setText(yunAccount);
			mPasswordEdt.requestFocus();
		}
		
		if(ECApplication.getInstance().getAlphaSwitch()) {
			getTopBarView().setTopBarToStatus(1, -1, R.drawable.btn_style_green, null, getString(R.string.app_login_auto), getString(R.string.app_name), null, this);
		}
	}

	private void initialize(Bundle savedInstanceState) {

	}

	private void initResourceRefs() {

		loginView = (LinearLayout) findViewById(R.id.experience_login_id);
		registView = (LinearLayout) findViewById(R.id.experience_regist_id);
		registButton = (Button) findViewById(R.id.switch_regist_view);
		registButton.setOnClickListener(this);
		loginButton = (Button) findViewById(R.id.switch_login_view);
		loginButton.setOnClickListener(this);
		mAccountInputView = (CCPFormInputView) findViewById(R.id.login_account_auto);
		mAccountEdt = mAccountInputView.getFormInputEditView();
		mPwdInputView = (CCPFormInputView) findViewById(R.id.login_password_et);
		mPasswordEdt = mPwdInputView.getFormInputEditView();
		tv_regist_tip1 = (TextView) findViewById(R.id.tv_regist_tip1);
		tv_regist_tip1.setText(Html.fromHtml(getString(R.string.tips_regist_1)));
	}

	private void initExperienceView() {
		mExperLogin = (Button) findViewById(R.id.experience_login_submit);
		mExperLogin.setOnClickListener(this);

	}

	@Override
	protected int getLayoutId() {
		return R.layout.login;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_regist_view:
			// HideSoftKeyboard();
			LayoutParams lp = registButton.getLayoutParams();
			lp.height=LayoutParams.MATCH_PARENT;
			registButton.setLayoutParams(lp);
			LayoutParams lp2 = loginButton.getLayoutParams();
			lp2.height=DensityUtil.dip2px(54);
			loginButton.setLayoutParams(lp2);
			
			loginView.setVisibility(View.GONE);
			registView.setVisibility(View.VISIBLE);
			registButton.setEnabled(false);
			loginButton.setEnabled(true);
			break;
		case R.id.switch_login_view:
			LayoutParams lp3 = loginButton.getLayoutParams();
			lp3.height=LayoutParams.MATCH_PARENT;
			loginButton.setLayoutParams(lp3);
			LayoutParams lp4 = registButton.getLayoutParams();
			lp4.height=DensityUtil.dip2px(54);
			registButton.setLayoutParams(lp4);
			
			registView.setVisibility(View.GONE);
			loginView.setVisibility(View.VISIBLE);
			registButton.setEnabled(true);
			loginButton.setEnabled(false);
			break;
		case R.id.experience_login_submit:
			
			
			hideSoftKeyboard();
			if(TextUtils.isEmpty(mAccountEdt.getText())||TextUtils.isEmpty(mPasswordEdt.getText())){
				ToastUtil.showMessage(R.string.tips_login_accountpwd_null);
				return;
			}
			mPostingdialog = new ECProgressDialog(this, R.string.login_posting);
			mPostingdialog.show();
			doLoginReuqest(mAccountEdt.getText().toString(),mPasswordEdt.getText().toString());
			break;
		case R.id.text_right:
			//doAutoLogin();
			
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
	
	private void doLoginReuqest(String user_name, String user_pwd) {
		CCPParameters ccpParameters = new CCPParameters();
		ccpParameters.setParamerTagKey("Request");
		ccpParameters.add("secret_key", "a04daaca96294836bef207594a0a4df8"); 
		ccpParameters.add("user_name", user_name);
		ccpParameters.add("user_pwd", user_pwd);
		AsyncECRequestRunner.requestAsyncForEncrypt("https://sandboxapp.cloopen.com:8883/2013-12-26/General/GetDemoAccounts", ccpParameters,
				"POST",true, new InnerRequestListener() {

					@Override
					public void onECRequestException(CCPException arg0) {
						ECLog4Util.e("TAG", "onECRequestException "+arg0.getMessage());
						ToastUtil.showMessage(arg0.getMessage());
						dismissPostingDialog();
					}


					@Override
					public void onComplete(String arg0) {
						ECLog4Util.e("TAG", "onComplete "+arg0);
						String json = OrgJosnUtils.xml2json(arg0);
						if(json!=null){
							try {
								JSONObject mJSONObject = (JSONObject) (new JSONObject(json)).get("Response");
								if("000000".equals(mJSONObject.get("statusCode"))){
									ECLog4Util.e("TAG", "statusCode "+mJSONObject.get("statusCode"));
									JSONObject app = (JSONObject) mJSONObject.get("Application");
									JSONArray jarray  =new JSONArray(app.get("SubAccount")+"");
									int length = jarray.length();
									ArrayList<ECContacts> arraylist =new ArrayList<ECContacts>();
									for(int i=0;i<length;i++){
										JSONObject object = (JSONObject) jarray.get(i);
										ECContacts bean =new ECContacts(object.getString("voip_account"));
										bean.setToken(object.getString("voip_token"));
										bean.setSubAccount(object.getString("sub_account"));
										bean.setSubToken(object.getString("sub_token"));
										arraylist.add((bean));
									}
									saveAutoLogin();
									Intent intent = new Intent(LoginActivity.this, AccountChooseActivity.class);
									intent.putParcelableArrayListExtra("arraylist", arraylist);
									intent.putParcelableArrayListExtra("arraylist", arraylist);
									startActivityForResult(intent, 0xa);
								}else{
									ECLog4Util.e("TAG", "statusCode "+mJSONObject.get("statusCode"));
									String errorMsg =mJSONObject.get("statusCode").toString();
									if(mJSONObject.has("statusMsg")){
										errorMsg = mJSONObject.get("statusMsg").toString();
									}
									ToastUtil.showMessage(errorMsg);
								}
							} catch (JSONException e) {
								e.printStackTrace();
								ToastUtil.showMessage(e.getMessage());
							}
						}
						
						dismissPostingDialog();
					}

				});
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
		finish();
		

	}
	
	/**
	 * 保存云通讯的获取账号登陆账号
	 */
	private void saveAutoLogin() {
		try {
			
			String account = mAccountEdt.getText().toString();
			ECPreferenceSettings settings = ECPreferenceSettings.SETTINGS_YUNTONGXUN_ACCOUNT;
			ECPreferences.savePreference(settings, account, true);
		} catch (Exception e) {
		}
	}

}
