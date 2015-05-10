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
package com.speedtong.example.common.utils;

import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.speedtong.example.R;

/**
 * Emoji表情和文本字符串处理工具类
 * @author 容联•云通讯
 * @date 2014-12-5
 * @version 4.0
 */
public class SpannableStringUtils {

	private static HashMap<String, SpannableString> hashMap = new HashMap<String, SpannableString>();
	
	/**
	 * The Emoji expressions into Unicode code
	 * @param context
	 * @param str
	 * @param textSize
	 * @param isReplaceLine
	 * @return
	 */
	public static SpannableString emoji2CharSequence(Context context , String str , int textSize , boolean isReplaceLine) {
		
		SpannableString object = null;
		String key = null;
		if(TextUtils.isEmpty(str)) {
			return new SpannableString("");
		}
		
		if(textSize == -1) {
			textSize = context.getResources().getDimensionPixelSize(R.dimen.ccp_button_text_size);
		} else if (textSize == -2) {
			textSize = context.getResources().getDimensionPixelSize(R.dimen.primary_text_size);
		}
		
		key = str + "@" + textSize;
		object = hashMap.get(key);
		if(object != null) {
			return object;
		}
		String source = str;
		if(isReplaceLine) {
			source = replaceLinebreak(str).toString();
		}
		
		source = matchEmojiUnicode(source);
		
		object = new SpannableString(source);
		
		boolean containsKeyEmoji = containsKeyEmoji(context, object, textSize);
		if(containsKeyEmoji) {
			hashMap.put(key, object);
		}
		
		return object;
	}
	
	/**
	 * 
	 */
	public static SpannableString getTextFormat(Context context , String format , int paddingSize) {
		if(TextUtils.isEmpty(format)) {
			return new SpannableString("");
		}
		String strValue = replaceLinebreak(format).toString();
		switch (paddingSize) {
		case -1:
			paddingSize = context.getResources().getDimensionPixelSize(R.dimen.ccp_button_text_size);
			break;
		case -2:
			paddingSize = context.getResources().getDimensionPixelSize(R.dimen.primary_text_size);
			break;
		default:
			break;
		}
		String key = strValue + "@" + paddingSize;
		String source = matchEmojiUnicode(strValue);
		SpannableString object = hashMap.get(key);
		if(object == null) {
			object = new SpannableString(source);
		}
		boolean containsKeyEmoji = containsKeyEmoji(context, object, paddingSize);
		if(containsKeyEmoji) {
			hashMap.put(key, object);
		}
		return object;
		
	}
	
	
	/**
	 * @param str
	 * @return
	 */
	private static CharSequence replaceLinebreak(CharSequence str) {
		if(TextUtils.isEmpty(str)) {
			return str;
		}
		
		if(str.toString().contains("\n")) {
			return str.toString().replace("\n", " ");
		}
		
		return str;
	}

	public static boolean containsKeyEmoji(Context context, SpannableString spannableString, int textSize) {
		
		if(TextUtils.isEmpty(spannableString)) {
			return false;
		}
		
		boolean isEmoji = false;
		char[] charArray = spannableString.toString().toCharArray();
		int i = 0;
		
		while(i < charArray.length) {
			
			int emojiId = getEmojiId(charArray[i]) ;
			
			if(emojiId != -1) {
				Drawable drawable = getEmoticonDrawable(context , emojiId);
				
				if(drawable != null) {
					drawable.setBounds(0, 0, (int)(1.3D * textSize), (int)(1.3D * textSize));
					spannableString.setSpan(new ImageSpan(drawable, 0), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					isEmoji = true;
				}
			}
			i ++;
		}
		
		return isEmoji;
	}

	/**
	 * @param context
	 * @param emojiId
	 * @return
	 */
	private static Drawable getEmoticonDrawable(Context context, int emojiId) {

		Drawable drawable = null;
		if(context == null || emojiId == -1) {
			return drawable;
		}
		
		int identifier = context.getResources().getIdentifier(
				"emoji_" + emojiId, "drawable", context.getPackageName());
		
		if(identifier != 0) {
			drawable = context.getResources().getDrawable(identifier);
		}
		return drawable;
	}

	/**
	 * Replace the not support emoji.
	 * @param str
	 * @return
	 */
	public static String matchEmojiUnicode(String str) {
		if(TextUtils.isEmpty(str)) {
			return str;
		}
		char[] charArray = str.toCharArray();
		try {
			for(int i = 0 ; i < charArray.length - 1 ; i ++) {
				int _index = charArray[i];
				int _index_inc = charArray[i + 1];
				
				if(_index == 55356) {
					 if ((_index_inc < 56324) || (_index_inc > 57320)) {
						 continue;
					 }
					 charArray[i] = '.';
					 charArray[(i + 1)] = '.';
				}
				
				if((_index != 55357) || (_index_inc < 56343) || (_index_inc > 57024)) {
					continue;
				}
				
				 charArray[i] = '.';
				 charArray[(i + 1)] = '.';
			}
		} catch (Exception e) {
		}
		
		return new String(charArray);
	}
	
	/**
	 * @param charStr
	 * @return
	 */
	private static int getEmojiId(char charStr) {
		int i = -1;
		if ((charStr < 57345) || (charStr > 58679)) {
			return i;
		}
		if ((charStr >= 57345) && (charStr <= 57434)) {
			i = charStr - 57345;
		} else if ((charStr >= 57601) && (charStr <= 57690)) {
			i = charStr + 'Z' - 57601;
		} else if ((charStr >= 57857) && (charStr <= 57939)) {
			i = charStr + '´' - 57857;
		} else if ((charStr >= 58113) && (charStr <= 58189)) {
			i = charStr + 'ć' - 58113;
		} else if ((charStr >= 58369) && (charStr <= 58444)) {
			i = charStr + 'Ŕ' - 58369;
		} else if ((charStr >= 58625) && (charStr <= 58679)) {
			i = charStr + 'Ơ' - 58625;
		}
		return i;
	}

}
