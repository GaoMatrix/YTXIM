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
package com.gao.im.ui.chatting;

import java.io.File;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.gao.im.R;
import com.gao.im.common.dialog.ECAlertDialog;
import com.gao.im.common.utils.DemoUtils;
import com.gao.im.common.utils.ECNotificationManager;
import com.gao.im.common.utils.ECPreferenceSettings;
import com.gao.im.common.utils.ECPreferences;
import com.gao.im.common.utils.EmoticonUtil;
import com.gao.im.common.utils.FileAccessor;
import com.gao.im.common.utils.LogUtil;
import com.gao.im.common.utils.ToastUtil;
import com.gao.im.common.view.RefreshableView;
import com.gao.im.core.ECAsyncTask;
import com.gao.im.core.SDKCoreHelper;
import com.gao.im.storage.ContactSqlManager;
import com.gao.im.storage.ConversationSqlManager;
import com.gao.im.storage.IMessageSqlManager;
import com.gao.im.storage.ImgInfoSqlManager;
import com.gao.im.ui.ECSuperActivity;
import com.gao.im.ui.chatting.mode.ImgInfo;
import com.gao.im.ui.chatting.view.CCPChattingFooter2;
import com.gao.im.ui.chatting.view.CCPChattingFooter2.OnChattingFooterLinstener;
import com.gao.im.ui.chatting.view.SmileyPanel;
import com.gao.im.ui.contact.ContactDetailActivity;
import com.gao.im.ui.contact.ECContacts;
import com.gao.im.ui.group.GroupInfoActivity;
import com.gao.im.ui.manager.CCPAppManager;
import com.gao.im.ui.plugin.FileExplorerActivity;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECChatManager;
import com.speedtong.sdk.ECChatManager.OnRealTimeMessageListener;
import com.speedtong.sdk.ECChatManager.OnRecordTimeoutListener;
import com.speedtong.sdk.exception.ECRecordException;
import com.speedtong.sdk.im.ECFileMessageBody;
import com.speedtong.sdk.im.ECMessage;
import com.speedtong.sdk.im.ECMessage.Direction;
import com.speedtong.sdk.im.ECMessage.Type;
import com.speedtong.sdk.im.ECTextMessageBody;
import com.speedtong.sdk.im.ECVoiceMessageBody;
import com.speedtong.sdk.platformtools.ECHandlerHelper;
import com.speedtong.sdk.platformtools.VoiceUtil;

/**
 * @author 容联•云通讯
 * @date 2014-12-9
 * @version 4.0
 */
public class ChattingActivity extends ECSuperActivity implements View.OnClickListener,
		AbsListView.OnScrollListener , IMChattingHelper.OnMessageReportCallback{

	private static final String TAG = "ECSDK_Demo.ChattingActivity";
	private static final int WHAT_ON_COMPUTATION_TIME = 10000;
	/**request code for tack pic*/
	public static final int REQUEST_CODE_TAKE_PICTURE = 0x3;
	public static final int REQUEST_CODE_LOAD_IMAGE = 0x4;
	public static final int REQUEST_CODE_IMAGE_CROP = 0x5;
	/**查看名片*/
	public static final int REQUEST_VIEW_CARD = 0x6;
	
	/**会话ID，数据库主键*/
    public final static String THREAD_ID = "thread_id";
    /**联系人账号*/
    public final static String RECIPIENTS = "recipients";
    /**联系人名称*/
    public final static String CONTACT_USER = "contact_user";
    /**按键振动时长*/
    public static final int TONE_LENGTH_MS = 200;
    /**音量值*/
    private static final float TONE_RELATIVE_VOLUME = 100.0F;
    /**待发送的语音文件最短时长*/
    private static final int MIX_TIME = 1000; 
    /**聊天界面消息适配器*/
	private ChattingListAdapter mChattingAdapter;
	/**界面消息下拉刷新*/
	private RefreshableView mPullDownView;
	private long mPageCount;
	/**历史聊天纪录消息显示View*/
	private ListView mListView;
	private View mListViewHeadView;
	/**聊天界面附加聊天控件面板*/
	private CCPChattingFooter2 mChattingFooter;
	/**选择图片拍照路径*/
	private String mFilePath;
	/**会话ID*/
	private long mThread = -1;
	/**会话联系人账号*/
	private String mRecipients;
	/**联系人名称*/
	private String mUsername;
	/**计算当前录音时长*/
	private long computationTime = -1L;
	/**当前语言录制文件的时间长度*/
	private int mVoiceRecodeTime = 0; 
	/**是否使用边录制便传送模式发送语音*/
	private boolean isRecordAndSend = false;
	/**手机震动API*/
	private Vibrator mVibrator;
	private ToneGenerator mToneGenerator;
	/**录音剩余时间Toast提示*/
	private Toast mRecordTipsToast;
	private ECHandlerHelper mHandlerHelper = new ECHandlerHelper();
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private Handler mVoiceHandler;
	private Looper mChattingLooper;
	/**IM聊天管理工具*/
	private ECChatManager mChatManager;
	/**聊天底部导航控件通知回调*/
	private OnChattingFooterImpl mChattingFooterImpl = new OnChattingFooterImpl(this);
	/**聊天功能插件接口实现*/
	private OnOnChattingPanelImpl mChattingPanelImpl = new OnOnChattingPanelImpl();
	
	@Override
	protected int getLayoutId() {
		return R.layout.chatting_activity;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
        
        // 初始化界面资源
        initView();
        // 初始化联系人信息
        initActivityState(savedInstanceState);
        
		final ArrayList<ECMessage> list = IMessageSqlManager.queryIMessageList(mThread,20, getMessageAdapterLastMessageTime() + "");
			mListView.post(new Runnable() {
				
				@Override
				public void run() {
					mChattingAdapter.setData(list);
					if(mChattingAdapter.getCount() < 20) {
						//mPullDownView.setPullEnabled(false);
						//mPullDownView.setPullViewVisibed(false);
					}
					mListView.clearFocus();
					mChattingAdapter.notifyDataSetChanged();
					mListView.setSelection(mChattingAdapter.getCount());
				}
			});
        
        // 初始化IM聊天工具API
        mChatManager = SDKCoreHelper.getECChatManager();
        HandlerThread thread = new HandlerThread("ChattingVoiceRecord",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mChattingLooper = thread.getLooper();
		mVoiceHandler = new Handler(mChattingLooper);
		mVoiceHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doEmojiPanel();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mChattingLooper != null) {
			mChattingLooper.quit();
			mChattingLooper = null;
			mVoiceHandler = null;
		}
		mChattingAdapter.onDestory();
	}
	
	/**
	 * 初始化聊天界面资源
	 */
	private void initView() {
		mPullDownView = (RefreshableView) findViewById(R.id.chatting_pull_down_view);
		mPullDownView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
			
			@Override
			public void onRefresh() {
				long lastTime = System.currentTimeMillis();
				if(mChattingAdapter != null) {
					ECMessage item = mChattingAdapter.getItem(0);
					if(item != null) {
						lastTime = item.getMsgTime();
					}
				}
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				final ArrayList<ECMessage> queryIMessageList = IMessageSqlManager.queryIMessageList(mThread, 20, lastTime + "");
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mChattingAdapter.insertDataArrays(queryIMessageList);
						if(queryIMessageList !=null && (queryIMessageList.size()) > 0){
							mPageCount -= queryIMessageList.size();
							LogUtil.d(TAG, "onRefreshing history msg count " + queryIMessageList.size());
							mListView.setSelectionFromTop(queryIMessageList.size()+1, mListViewHeadView.getHeight() + mPullDownView.getTopViewHeight());
						}else{
							mListView.setSelectionFromTop(1, mListViewHeadView.getHeight() + mPullDownView.getTopViewHeight());
							mPageCount = 0;
							mPullDownView.setPullEnabled(false);
						}
						
						mPullDownView.finishRefreshing();
					}
				});
			}
		}, 0);
		mListView = (ListView) findViewById(R.id.chatting_history_lv);
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		mListView.setItemsCanFocus(false);
		mListView.setOnScrollListener(this);
		mListView.setKeepScreenOn(true);
		mListView.setStackFromBottom(false);
		mListView.setFocusable(false);
		mListView.setFocusableInTouchMode(false);
		
		mListViewHeadView = getLayoutInflater().inflate(R.layout.chatting_list_header, null);
		mListView.addHeaderView(mListViewHeadView);
		
		mPullDownView.setOnPullDownOnTouchListener(new RefreshableView.OnPullDownOnTouchListener() {
			
			@Override
			public void OnPullDownOnTouch() {
				// 隐藏键盘
				hideSoftKeyboard();
				// 隐藏更多的聊天功能面板
				mChattingFooter.hideBottomPanel();
			}
		});
		
		// 初始化聊天功能面板
		mChattingFooter = (CCPChattingFooter2) findViewById(R.id.nav_footer);
		// 注册一个聊天面板文本输入框改变监听
		mChattingFooter.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		// 注册聊天面板状态回调通知、包含录音按钮按钮下放开等录音操作
		mChattingFooter.setOnChattingFooterLinstener(mChattingFooterImpl);
		// 注册聊天面板附加功能（图片、拍照、文件）被点击回调通知
		mChattingFooter.setOnChattingPanelClickListener(mChattingPanelImpl);
		mChattingAdapter = new ChattingListAdapter(this);
		mListView.setAdapter(mChattingAdapter);
	}
	
	private void hideBottom() {
		// 隐藏键盘
		hideSoftKeyboard();
		if(mChattingFooter != null) {
			// 隐藏更多的聊天功能面板
			mChattingFooter.hideBottomPanel();
		}
	}
	
	/**
	 * 读取聊天界面联系人会话参数信息
	 * @param savedInstanceState
	 */
	private void initActivityState(Bundle savedInstanceState) {
		Intent intent = getIntent();
		mRecipients = intent.getStringExtra(RECIPIENTS);
		mUsername = intent.getStringExtra(CONTACT_USER);
		if(mUsername == null) {
			ECContacts contact = ContactSqlManager.getContact(mRecipients);
			if(contact != null) {
				mUsername = contact.getNickname();
			} else {
				mUsername = mRecipients;
			}
		}
		
		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, isPeerChat() ? R.drawable.actionbar_facefriend_icon :R.drawable.actionbar_particular_icon, mUsername, this);
		mThread = ConversationSqlManager.querySessionIdForBySessionId(mRecipients);
		
	}
	
	/**
	 * 是否群组
	 * @return
	 */
	public boolean isPeerChat() {
		return mRecipients.startsWith("g");
	}

	/**
	 * 返回聊天消息适配器
	 * @return the mChattingAdapter
	 */
	public ChattingListAdapter getChattingAdapter() {
		return mChattingAdapter;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			LogUtil.d(TAG, "keycode back , chatfooter mode: " +  mChattingFooter.getMode());
			if(!mChattingFooter.isButtomPanelNotVisibility()) {
				hideBottom();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if(mPullDownView != null) {
			if(mPageCount > 0) {
				mPullDownView.setPullEnabled(false);
			} else {
				mPullDownView.setPullEnabled(true);
			}
			LogUtil.d(TAG, "onScroll pageCount " + mPageCount);
		}
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		mChattingFooter.switchChattingPanel(SmileyPanel.APP_PANEL_NAME_DEFAULT);
		mChattingFooter.initSmileyPanel();
		IMChattingHelper.setOnMessageReportCallback(this);
		// 将所有的未读消息设置为已读
		setIMessageNomalThreadRead();
		mChattingAdapter.onResume();
		
		checkPreviewImage();
		setChattingContactId(mRecipients);
		ECNotificationManager.getInstance().forceCancelNotification();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mChattingAdapter.onPause();
		IMChattingHelper.setOnMessageReportCallback(null);
		setChattingContactId("");
	}
	
	/**
	 * 保存当前的聊天界面所对应的联系人、方便来消息屏蔽通知
	 */
	private void setChattingContactId(String contactid) {
		try {
			ECPreferences.savePreference(ECPreferenceSettings.SETTING_CHATTING_CONTACTID , contactid, true);
		} catch (InvalidClassException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 检查是否有预览带发送图片
	 */
	private void checkPreviewImage() {
		if(TextUtils.isEmpty(mFilePath)) {
			return ;
		}
		boolean previewImage = ECPreferences.getSharedPreferences().getBoolean(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED.getId()
				,(Boolean)ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED.getDefaultValue() );
		if(previewImage){
			try {
				ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED, Boolean.FALSE, true);
				new ChattingAsyncTask(this).execute(mFilePath);
				mFilePath = null;
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void doEmojiPanel() {
		if(EmoticonUtil.getEmojiSize() == 0) {
			EmoticonUtil.initEmoji();
		}
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
		
		if(data != null && 0x2a == requestCode) {
			handleAttachUrl(data.getStringExtra("choosed_file_path"));
			return ;
		}
		
		if(requestCode == REQUEST_CODE_TAKE_PICTURE
				|| requestCode == REQUEST_CODE_LOAD_IMAGE) {
			if(requestCode == REQUEST_CODE_LOAD_IMAGE) {
				mFilePath = DemoUtils.resolvePhotoFromIntent(ChattingActivity.this, data, FileAccessor.IMESSAGE_IMAGE);
			}
			
			File file = new File(mFilePath);
			if(file == null || !file.exists()) {
				return;
			}
			try {
				ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_CROPIMAGE_OUTPUTPATH, file.getAbsolutePath(), true);
				Intent intent = new Intent(ChattingActivity.this, ImagePreviewActivity.class);
				startActivityForResult(intent, REQUEST_CODE_IMAGE_CROP);
			} catch (InvalidClassException e1) {
				e1.printStackTrace();
			}
			return ;
		}
		if(requestCode == REQUEST_VIEW_CARD) {
			finish();
			return ;
		}

	}
	
	/**
	 * 处理附件
	 * @param path
	 */
	private void handleAttachUrl(final String path) {
		File file = new File(path);
		if(!file.exists()) {
			return ;
		}
		final long length = file.length();
		ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, getString(R.string.plugin_upload_attach_size_tip , length), new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				handleSendFileAttachMessage(length , path);
			}});
		
		buildAlert.setTitle(R.string.app_tip);
		buildAlert.show();
	}

	/**
	 * 处理文本发送方法事件通知
	 * @param text
	 */
	private void handleSendTextMessage(CharSequence text) {
		if(text == null) {
			return ;
		}
		// 组建一个待发送的ECMessage 
		ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
		//设置消息的属性：发出者，接受者，发送时间等
		msg.setForm(CCPAppManager.getClientUser().getUserId());
		msg.setMsgTime(System.currentTimeMillis());
		msg.setTo(mRecipients);
		msg.setSessionId(mRecipients);
		msg.setDirection(Direction.SEND);
		ECTextMessageBody msgBody = new ECTextMessageBody(text.toString());
		msg.setBody(msgBody);
		try {
			// 发送消息，该函数见上
			long rowId = IMChattingHelper.sendECMessage(msg);
			// 通知列表刷新
			msg.setId(rowId);
			notifyIMessageListView(msg);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 处理发送附件消息
	 * @param length
	 * @param pathName
	 */
	private void handleSendFileAttachMessage(long length, String pathName) {
		if(TextUtils.isEmpty(pathName)) {
			return ;
		}
		// 组建一个待发送的附件ECMessage 
		ECMessage msg = ECMessage.createECMessage(ECMessage.Type.FILE);
		// 设置接收者、发送者、会话ID等信息
		msg.setForm(CCPAppManager.getClientUser().getUserId());
		msg.setTo(mRecipients);
		msg.setSessionId(mRecipients);
		msg.setDirection(Direction.SEND);
		msg.setMsgTime(System.currentTimeMillis());
		// 创建附件消息体
		ECFileMessageBody msgBody  = new ECFileMessageBody();
		// 设置附件名
		msgBody.setFileName(DemoUtils.getFilename(pathName));
		// 设置附件扩展名
		msgBody.setFileExt(DemoUtils.getExtensionName(pathName));
		// 设置附件本地路径
		msgBody.setLocalUrl(pathName);
		// 设置附件长度
		msgBody.setLength(length);
		// 扩展附件名称、对方可以用此名称界面显示
		msg.setUserData("fileName=" + msgBody.getFileName());
		msg.setBody(msgBody);
		try {
			// 调用发送API
			long rowId = IMChattingHelper.sendECMessage(msg);
			// 通知列表刷新
			msg.setId(rowId);
			notifyIMessageListView(msg);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 处理发送图片消息
	 * @param imgInfo
	 */
	public void handleSendImageMessage(ImgInfo imgInfo) {
		String fileName = imgInfo.getBigImgPath();
		String fileUrl = FileAccessor.getImagePathName() + "/" + fileName;
		if(new File(fileUrl).exists()) {
			// 组建一个待发送的ECMessage 
			ECMessage msg = ECMessage.createECMessage(ECMessage.Type.IMAGE);
			// 设置接收者、发送者、会话ID等信息
			msg.setForm(CCPAppManager.getClientUser().getUserId());
			msg.setTo(mRecipients);
			msg.setSessionId(mRecipients);
			msg.setDirection(Direction.SEND);
			msg.setMsgTime(System.currentTimeMillis());
			// 设置附件包体（图片也是相当于附件）
			ECFileMessageBody msgBody  = new ECFileMessageBody();
			
			// 设置附件名
			msgBody.setFileName(fileName);
			// 设置附件扩展名
			msgBody.setFileExt(DemoUtils.getExtensionName(fileName));
			// 设置附件本地路径
			msgBody.setLocalUrl(fileUrl);
			msg.setBody(msgBody);
			
			try {
				long rowId = IMChattingHelper.sendImageMessage(imgInfo ,msg);
				// 通知列表刷新
				msg.setId(rowId);
				notifyIMessageListView(msg);
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 将发送的消息放入消息列表
	 * @param message
	 */
	public void notifyIMessageListView(ECMessage message) {
		
		ChattingListAdapter forceAdapter = mChattingAdapter;
		forceAdapter.insertData(message);
		mListView.setSelection(mListView.getCount() - 1);
	}
	
	/**
	 * 获得最后一条消息的时间
	 * @return
	 */
	private long getMessageAdapterLastMessageTime() {
		long lastTime = 0;
		if(mChattingAdapter != null && mChattingAdapter.getCount() >0) {
			ECMessage item = mChattingAdapter.getItem(mChattingAdapter.getCount() - 1);
			if(item != null) {
				lastTime = item.getMsgTime();
			}
		}
		return lastTime;
	}

	/**
	 * 消息发送报告
	 */
	@Override
	public void onMessageReport(ECMessage message) {
		if(mChattingAdapter != null) {
			mChattingAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 收到新的Push消息
	 */
	@Override
	public void onPushMessage(String sid ,List<ECMessage> msgs) {
		
		if(!mRecipients.equals(sid)) {
			return ;
		}
		mThread = ConversationSqlManager.querySessionIdForBySessionId(mRecipients);
		
		ChattingListAdapter forceAdapter = mChattingAdapter;
		forceAdapter.insertDataArraysAfter(msgs);
		mListView.setSelection(mListView.getCount() - 1);
		
		
		setIMessageNomalThreadRead();
	}
	
	/**
	 * 更新所有的未读消息
	 */
	private void setIMessageNomalThreadRead() {
		mVoiceHandler.post(new Runnable() {
			
			@Override
			public void run() {
				IMessageSqlManager.setIMessageNomalThreadRead(mThread);
			}
		});
	}
	
	/**
	 * 给予客户端震动提示
	 */
	protected void readyOperation() {
		computationTime = -1L;
		mRecordTipsToast = null;
		playTone(ToneGenerator.TONE_PROP_BEEP, TONE_LENGTH_MS);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				stopTone();
			}
		}, TONE_LENGTH_MS);
		vibrate(50L);
	}
	
	private Object mToneGeneratorLock = new Object();
	// 初始化
	private void initToneGenerator() {
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (mToneGenerator == null) {
			try {
				int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				int volume = (int) (TONE_RELATIVE_VOLUME * (streamVolume / streamMaxVolume));
				mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);

			} catch (RuntimeException e) {
				LogUtil.d("Exception caught while creating local tone generator: "
						+ e);
				mToneGenerator = null;
			}
		}
	}
	
	/**
	 * 停止播放声音
	 */
	public void stopTone() {
		if(mToneGenerator != null)
			mToneGenerator.stopTone();
	}
	
	/**
	 * 播放提示音
	 * @param tone
	 * @param durationMs
	 */
	public void playTone(int tone ,int durationMs) {
        synchronized(mToneGeneratorLock) {
        	initToneGenerator();
            if (mToneGenerator == null) {
                LogUtil.d("playTone: mToneGenerator == null, tone: "+tone);
                return;
            }

            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, durationMs);
        }
    }
	
	/**
	 * 手机震动
	 * @param milliseconds
	 */
	public synchronized void vibrate(long milliseconds) {
		Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null) {
           return ;
        }
        mVibrator.vibrate(milliseconds);
    }
	
	private void handleTackPicture() {
		if(!FileAccessor.isExistExternalStore()) {
			return;
		}
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = FileAccessor.getTackPicFilePath();
		if (file != null) {
			Uri uri = Uri.fromFile(file);
			if (uri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			}
			mFilePath = file.getAbsolutePath();
		}
		startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
	}
	
	private void handleSelectImageIntent() {
		Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_CODE_LOAD_IMAGE);
	}
	
	/**
	 * 消息重发
	 * @param msg
	 * @param position
	 */
	public void doResendMsgRetryTips(final ECMessage msg , final int position) {
		ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, R.string.chatting_resend_content, null, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				resendMsg(msg, position);
			}
		});
		buildAlert.setTitle(R.string.chatting_resend_title);
		buildAlert.show();
	}
	/**
	 * @param msg
	 * @param position
	 */
	protected void resendMsg(ECMessage msg, int position) {
		if(msg == null || position < 0 || mChattingAdapter.getItem(position) == null) {
			LogUtil.d(TAG, "ignore resend msg , msg " + msg + " , position " + position);
			return ;
		}
		ECMessage message = mChattingAdapter.getItem(position);
		message.setTo(mRecipients);
		long rowid = IMChattingHelper.reSendECMessage(message);
		if(rowid != -1) {
			mChattingAdapter.notifyDataSetChanged();
		}
	}
	/**
	 * 聊天插件功能实现
	 */
	private class OnOnChattingPanelImpl implements CCPChattingFooter2.OnChattingPanelClickListener {

		@Override
		public void OnTakingPictureRequest() {
			handleTackPicture();
			hideBottomPanel();
		}

		@Override
		public void OnSelectImageReuqest() {
			handleSelectImageIntent();
			hideBottomPanel();
		}

		@Override
		public void OnSelectFileRequest() {
			startActivityForResult(new Intent(ChattingActivity.this, FileExplorerActivity.class), 0x2a);
			hideBottomPanel();
		}
		
		private void hideBottomPanel() {
			mChattingFooter.hideBottomPanel();
		}
		
	}
	
	/**
	 * 聊天功能面板（发送、录音、切换输入选项）
	 */
	private class OnChattingFooterImpl implements OnChattingFooterLinstener {

		final ChattingActivity mActivity;
		protected String mAmrPathName;
		/**保存当前的录音状态*/
		public int mRecordState = RECORD_IDLE; 	
		/**语音录制空闲*/
		public static final int RECORD_IDLE = 0;
		/**语音录制中*/
		public static final int RECORD_ING = 1;
		/**语音录制结束*/
		public static final int RECORD_DONE = 2;
		/**待发的ECMessage消息*/
		private ECMessage mPreMessage;
		/**同步锁*/
		Object mLock = new Object();
		public OnChattingFooterImpl(ChattingActivity ctx) {
			mActivity = ctx;
		}
		
		@Override
		public void OnVoiceRcdInitReuqest() {
			mAmrPathName = VoiceUtil.md5(String.valueOf(System.currentTimeMillis())) + ".amr";
			if (FileAccessor.getVoicePathName() == null) {
				ToastUtil.showMessage("Path to file could not be created");
				mAmrPathName = null;
				return ;
			}
			
			if (getRecordState() != RECORD_ING) {
				setRecordState(RECORD_ING);
				
				// 手指按下按钮，按钮给予振动或者声音反馈
				readyOperation();
				// 显示录音提示框
				mChattingFooter.showVoiceRecordWindow(findViewById(R.id.chatting_bg_ll).getHeight() - mChattingFooter.getHeight());
				
				final ECChatManager chatManager = SDKCoreHelper.getECChatManager();
				mVoiceHandler.post(new Runnable() {
					
					@Override
					public void run() {
						try {
							ECMessage message = ECMessage.createECMessage(Type.VOICE);
							message.setForm(CCPAppManager.getClientUser().getUserId());
							message.setTo(mRecipients);
							message.setSessionId(mRecipients);
							message.setDirection(Direction.SEND);
							message.setUserData("ext=amr");
							ECVoiceMessageBody messageBody = new ECVoiceMessageBody(new File(FileAccessor.getVoicePathName() ,mAmrPathName ), 0);
							message.setBody(messageBody);
							mPreMessage = message;
							if(isRecordAndSend) {
								// 实时发送语音消息，录制完成后不需要再调用发送API接口
								chatManager.sendRealTimeMessage(message, new OnRealTimeMessageListener() {
									
									@Override
									public void onComplete(ECError error) {
										
									}
									
									@Override
									public void onSendRealTimeMessageComplete(ECError error, ECMessage message) {
										
									}
									
									@Override
									public void onRecordingTimeOut(long ms) {
										// 如果语音录制超过最大60s长度,则发送
										doProcesOperationRecordOver(false);
									}
									
									@Override
									public void onRecordingAmplitude(double amplitude) {
										// 显示声音振幅
										if(mChattingFooter != null && getRecordState()  == RECORD_ING) {
											mChattingFooter.showVoiceRecording();
											mChattingFooter.displayAmplitude(amplitude);
										}
									}
								});
								return;
							}
							// 仅录制语音消息，录制完成后需要调用发送接口发送消息
							chatManager.startVoiceRecording(message, new OnRecordTimeoutListener() {
								
								@Override
								public void onComplete(ECError error) {
									
								}

								@Override
								public void onRecordingTimeOut(long duration) {
									// 如果语音录制超过最大60s长度,则发送
									doProcesOperationRecordOver(false);
								}

								@Override
								public void onRecordingAmplitude(
										double amplitude) {
									// 显示声音振幅
									if(mChattingFooter != null && getRecordState()  == RECORD_ING) {
										mChattingFooter.showVoiceRecording();
										mChattingFooter.displayAmplitude(amplitude);
									}
								}
								
							});
						} catch (ECRecordException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}

		@Override
		public void OnVoiceRcdStartRequest() {
			// SDK完成初始化底层音频设备、开始采集音频数据
			mHandler.removeMessages(WHAT_ON_COMPUTATION_TIME);
			mHandler.sendEmptyMessageDelayed(WHAT_ON_COMPUTATION_TIME, TONE_LENGTH_MS);
		}

		@Override
		public void OnVoiceRcdCancelRequest() {
			handleMotionEventActionUp(true);
		}

		@Override
		public void OnVoiceRcdStopRequest() {
			handleMotionEventActionUp(false);
		}

		@Override
		public void OnSendTextMessageRequest(CharSequence text) {
			handleSendTextMessage(text);
		}

		@Override
		public void OnUpdateTextOutBoxRequest(CharSequence text) {
			
		}

		@Override
		public void OnSendCustomEmojiRequest(int emojiid, String emojiName) {
			
		}

		@Override
		public void OnEmojiDelRequest() {
			
		}

		@Override
		public void OnInEditMode() {
			
		}

		@Override
		public void onPause() {
			
		}

		@Override
		public void onResume() {
			
		}

		@Override
		public void release() {
			
		}
		
		/**
		 * 处理Button 按钮按下抬起事件
		 * @param doCancle 是否取消或者停止录制
		 */
		private void handleMotionEventActionUp(final boolean doCancle) {
			if(getRecordState()  == RECORD_ING) {
				doVoiceRecordAction(doCancle);
				doProcesOperationRecordOver(doCancle);
			}
		}
		
		/**
		 * 处理语音录制结束事件
		 * @param doCancle 是否取消或者停止录制
		 */
		private void doVoiceRecordAction(final boolean doCancle) {
			
			if(mChatManager != null) {
				mVoiceHandler.post(new Runnable() {
					
					@Override
					public void run() {
						if(isRecordAndSend) {
							// 停止或者取消实时语音
							if(doCancle) {
								LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "handleMotionEventActionUp cancle Real-Time record");
								mChatManager.cancelRealTimeMessage();
							} else {
								LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "handleMotionEventActionUp stop Real-Time record");
								mChatManager.stopRealTimeMessage(null);
							}
						} else {
							// 停止或者取消普通模式语音
							if(doCancle) {
								LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "handleMotionEventActionUp cancle normal record");
								mChatManager.cancelVoiceRecording();
							} else {
								LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "handleMotionEventActionUp stop normal record");
								mChatManager.stopVoiceRecording();
							}
						}
					}
				});
			}
		}
		
		/**
		 * 处理录音结束消息是否发送逻辑
		 * @param cancle 是否取消发送
		 */
		protected void doProcesOperationRecordOver(boolean cancle) {
			if(getRecordState() == RECORD_ING) {
				// 当前是否有正在录音的操作
				
				// 定义标志位判断当前所录制的语音文件是否符合发送条件
				// 只有当录制的语音文件的长度超过1s才进行发送语音
				boolean isVoiceToShort = false;
				File amrPathFile = new File(FileAccessor.getVoicePathName() ,mAmrPathName);
				if(amrPathFile.exists()) {
					mVoiceRecodeTime = DemoUtils.calculateVoiceTime(amrPathFile.getAbsolutePath());
					if(!isRecordAndSend) {
						if (mVoiceRecodeTime * 1000 < MIX_TIME) {
							isVoiceToShort = true;
						}
					}
				} else {
					isVoiceToShort = true;
				}
				// 设置录音空闲状态
				setRecordState(RECORD_IDLE);
				if(mChattingFooter != null ) {
					if (isVoiceToShort && !cancle) {
						// 提示语音文件长度太短
						mChattingFooter.tooShortPopuWindow();
						return;
					}
					// 关闭语音录制对话框
					mChattingFooter.dismissPopuWindow();
				}
				
				if(!cancle && mPreMessage != null) {
					if(!isRecordAndSend) {
						// 如果当前的录音模式为非Chunk模式
						try {
							ECVoiceMessageBody body = (ECVoiceMessageBody) mPreMessage.getBody();
							body.setDuration(mVoiceRecodeTime);
							long rowId = IMChattingHelper.sendECMessage(mPreMessage);
							mPreMessage.setId(rowId);
							notifyIMessageListView(mPreMessage);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return ;
				} 
				
				// 删除语音文件
				amrPathFile.deleteOnExit();
				// 重置语音时间长度统计
				mVoiceRecodeTime = 0;
			}
		}
		
		public int getRecordState() {
			synchronized (mLock) {
				return mRecordState;
			}
		}

		public void setRecordState(int state) {
			synchronized (mLock) {
				this.mRecordState = state;
			}
		}
		
	}
	
	
	public class ChattingAsyncTask extends ECAsyncTask {

		/**
		 * @param context
		 */
		public ChattingAsyncTask(Context context) {
			super(context);
		}

		@Override
		protected Object doInBackground(Object... params) {
			ImgInfo createImgInfo = ImgInfoSqlManager.getInstance().createImgInfo((String)params[0]);
			return createImgInfo;
		}

		@Override
		protected void onPostExecute(Object result) {
			if(result instanceof ImgInfo) {
				ImgInfo imgInfo = (ImgInfo) result;
				handleSendImageMessage(imgInfo);
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			hideSoftKeyboard();
			finish();
			break;
		case R.id.btn_right:
			if(!isPeerChat()) {
				// 如果是点对点聊天
				ECContacts contact = ContactSqlManager.getContact(mRecipients);
				Intent intent = new Intent(this, ContactDetailActivity.class);
				intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
				startActivityForResult(intent, REQUEST_VIEW_CARD);
				return ;
			}
			// 群组聊天室
			Intent intent = new Intent(this, GroupInfoActivity.class);
			intent.putExtra(GroupInfoActivity.GROUP_ID, mRecipients);
			startActivityForResult(intent, REQUEST_VIEW_CARD);
			break;
		default:
			break;
		}
	}

}
