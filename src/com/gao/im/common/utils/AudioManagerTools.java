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
package com.gao.im.common.utils;

import com.gao.im.ui.manager.CCPAppManager;

import android.content.Context;
import android.media.AudioManager;

/**
 * AudioManager工具类
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class AudioManagerTools {
	/**AudioManager */
	private AudioManager mAudioManager = null;
	private static AudioManagerTools mInstance;
	private AudioManagerTools() {
		
	}
	
	/**单例方法*/
	public static AudioManagerTools getInstance() {
		
		if(mInstance == null) {
			mInstance = new AudioManagerTools();
		}
		
		return mInstance;
	}
	
	/**
	 * 返回当前所持有的AudioManager访问实例
	 * @return
	 */
	public final AudioManager getAudioManager() {
		if(mAudioManager == null) {
			mAudioManager = (AudioManager) CCPAppManager.getContext().getSystemService(Context.AUDIO_SERVICE);
		}
		
		return mAudioManager;
	}
}
