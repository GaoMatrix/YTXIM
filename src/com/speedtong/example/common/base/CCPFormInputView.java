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
package com.speedtong.example.common.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.speedtong.example.R;
import com.speedtong.example.common.utils.LogUtil;

/**
 * <p>Title: CCPFormInputView.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2014-8-16
 * @version 1.0
 */
public class CCPFormInputView extends LinearLayout {

	public static final String TAG = LogUtil.getLogUtilsTag(CCPFormInputView.class);
	
	private Context mContext;
	private TextView mTitleView;
	private EditText mContentEditText;
	private int mLayout = -1;
	private CharSequence mTitle;
	private CharSequence mHint;
	
	private View.OnFocusChangeListener mOnFocusChangeListener;
	
	/**
	 * @param context
	 */
	public CCPFormInputView(Context context) {
		super(context);
		mContext = null;
		mOnFocusChangeListener = null;
		mLayout = -1;
		mContext = context;
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public CCPFormInputView(Context context, AttributeSet attrs) {
		this(context, attrs , -1);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	@TargetApi(VERSION_CODES.HONEYCOMB)
	public CCPFormInputView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = null;
		mLayout = -1;
		mOnFocusChangeListener = null;
		
		TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.form_input, defStyle, 0);
		mTitle = obtainStyledAttributes.getString(R.styleable.form_input_form_title);
		mHint = obtainStyledAttributes.getString(R.styleable.form_input_form_hint);
		mLayout = obtainStyledAttributes.getResourceId(R.styleable.form_input_form_layout , mLayout);
		obtainStyledAttributes.recycle();
		inflate(context, mLayout, this);
		mContext = context;
	}
	
	/**
	 * 
	 * @param l
	 */
	public void setOnFormInputViewFocusChangeListener(View.OnFocusChangeListener l) {
		mOnFocusChangeListener = l;
	}
	
	/**
	 * Change the editor type integer associated with the text view, which
     * will be reported to an IME with {@link EditorInfo#imeOptions} when it
     * has focus.
	 */
	public void setFormInputViewImeOptions() {
		if(mContentEditText != null) {
			mContentEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
			return ;
		}
		LogUtil.e(TAG, "mContentEditText is null");
	}
	
	/**
	 * 
	 */
	public void setFormInputViewInputTypeForPhone() {
		if(mContentEditText != null) {
			mContentEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
			return ;
		}
		LogUtil.e(TAG, "mContentEditText is null");
	}
	
	public EditText getFormInputEditView() {
		return mContentEditText;
	}
	
	/**
	 * 
	 * @param textWatcher
	 */
	public void addTextChangedListener(TextWatcher textWatcher) {
	
		if(mContentEditText != null) {
			mContentEditText.addTextChangedListener(textWatcher);
			return ;
		}
		LogUtil.w(TAG, "watcher : " + textWatcher + " ,mContentEditText : " + mContentEditText);
	}
	
	/**
	 * 
	 * @return
	 */
	public Editable getText() {
		if(mContentEditText != null) {
			return mContentEditText.getText();
		}
		
		LogUtil.e(TAG, "mContentEditText is null");
		return null;
	}
	
	/**
	 * 
	 * @param text
	 */
	public void setText(CharSequence text) {
		if(mContentEditText != null) {
			mContentEditText.setText(text);
		}
		
		LogUtil.e(TAG, "mContentEditText is null");
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mTitleView = (TextView) findViewById(R.id.title);
		mContentEditText = (EditText) findViewById(R.id.edittext);
		
		if(mTitleView == null || mContentEditText == null) {
			LogUtil.w(TAG, "mTitleView: " + mTitleView + " ,mContentEditText: " + mContentEditText);
		} else {
			if (mContentEditText != null ) {
				mContentEditText.setOnFocusChangeListener(new  View.OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						
						if(v == mContentEditText) {
							if(hasFocus) {
								setBackgroundResource(R.drawable.input_bar_bg_active);
							} else {
								setBackgroundResource(R.drawable.input_bar_bg_normal);
							}
							
							if(mOnFocusChangeListener != null) {
								mOnFocusChangeListener.onFocusChange(v, hasFocus);
							}
						}
					}
				});
			} 
			
			if(mTitle != null) {
				mTitleView.setText(mTitle);
			}
			
			if(mHint != null) {
				mContentEditText.setHint(mHint);
			}
		}
		
		
	}
	
}
