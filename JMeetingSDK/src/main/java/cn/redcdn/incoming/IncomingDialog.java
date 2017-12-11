package cn.redcdn.incoming;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.redcdn.jmeetingsdk.JMeetingAgent;
import cn.redcdn.jmeetingsdk.MeetingManage;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;
import cn.redcdn.util.RoundImageView;
import cn.redcdn.util.SystemManger;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class IncomingDialog extends Activity implements View.OnClickListener {
	private String tag = IncomingDialog.class.getName();

	// incomingactivity页面发送的广播
	/** 点击加入按钮 */
	public static final String JOIN_MEETING_BROADCAST = "cn.redcdn.jmeetingsdk.incoming.incomingactivity.join";
	/** 点击忽略按钮 */
	public static final String IGNORE_MEETING_BROADCAST = "cn.redcdn.jmeetingsdk.incoming.incomingactivity.ignore";
	/** 超时，未做任何处理 */
	public static final String TIMEOUT_MEETING_BROADCAST = "cn.redcdn.jmeetingsdk.incoming.incomingactivity.timeout";

	private Button joinBtn;
	private Button ignoreBtn;
	private ImageView headPicBg;
	private ImageView headPicBgSec;
	private RoundImageView headPic;
	private String accountID;
	private String accountName;
	private int meetingID;
	private String headUrl;
	private boolean openSound = true;
	private boolean openTTS;

	private int ringTimelong = 70 * 1000; // 70秒
	private int ttsDelayTime = 3000; // 3秒
	Timer ignoreTimer;

	private MediaPlayer mp;
	private TextToSpeech tts;

	private int PLAY_ALARM_MESSAGE = 1;
	private int PLAY_TTS_MESSAGE = 2;
	private int PLAY_ALARM_FINISH_MESSAGE = 3;
	private int PLAY_TTS_FINISH_MESSAGE = 4;
	private int TIMEOUT_INCOMING_RING_MESSAGE = 5;
	private int TIMEOUT_STOP=6;
	
	private final int DELAY_UPDATE_SECOND =500;

	private String playText;
	
    private Ringtone ringtone = null;

    private Uri audioUri = null;
//    // 记录铃音的大小
//    private int ringVolume;
    // 记录播放模式
    private boolean isRing = false;
    private Vibrator vibrator = null;
	enum VoicePlayState {
		NONE, PLAYING
	};

	VoicePlayState state = VoicePlayState.NONE;
	
    SystemManger mSystemManager;

	AudioManager audio;
	  // 记录播放模式
    private int audioMode = -1;
    
    private MediaPlayer player;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (PLAY_ALARM_MESSAGE == msg.what) {
				if (!openSound) {
					CustomLog.w(tag, "来电铃声关闭. ignore play sound");
					handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
					return;
				}

				if (VoicePlayState.NONE == state) {
					CustomLog.i(tag, "state == VoicePlayState.NONE, return");
					return;
				}

				if (playAlarm() < 0) {
					handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
				}
			} else if (PLAY_ALARM_FINISH_MESSAGE == msg.what) {
				CustomLog.i(tag, "play alarm finish!");
				if (openTTS) {
					handler.sendEmptyMessage(PLAY_TTS_MESSAGE);
				} else if (!openTTS && openSound) {
					handler.sendEmptyMessage(PLAY_ALARM_MESSAGE);
				}
			}

			else if (PLAY_TTS_MESSAGE == msg.what) {
				if (!openTTS) {
					CustomLog.w(tag, "TTS语音播报关闭. ignore play tts");
					return;
				}

				CustomLog.i(tag, "tts text:" + playText);

				if (VoicePlayState.NONE == state) {
					return;
				}

				playTTS();
			} else if (PLAY_TTS_FINISH_MESSAGE == msg.what) {
				CustomLog.i(tag, "play tts finish and release tts");

				if (VoicePlayState.NONE == state) {
					return;
				}

				if (null != tts) {
					tts.shutdown();
					tts = null;
				}

				if (openSound) {
					handler.sendEmptyMessage(PLAY_ALARM_MESSAGE);
				} else {
					handler.sendEmptyMessageDelayed(PLAY_TTS_MESSAGE,
							ttsDelayTime);
				}

			} else if (TIMEOUT_INCOMING_RING_MESSAGE == msg.what) {
				CustomLog.i(tag,
						"release play resource and finish incoming activity");

				state = VoicePlayState.NONE;
				releasePlayResource();

				Intent timeoutIntent = new Intent();
				timeoutIntent.setAction(TIMEOUT_MEETING_BROADCAST);
				sendBroadcast(timeoutIntent);

				ignoreTimer = null;
				IncomingDialog.this.finish();
			}else if(TIMEOUT_STOP == msg.what){
				CustomLog.i(tag,
						"timeout tostop");

				stopIncomingDialog();
			}

			else {
				CustomLog.i(tag, "invalidate play type");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CustomLog.d(tag, "IncomingActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(MResource.getIdByName(this, MResource.LAYOUT,
				"jmeetingsdk_invite_dialog"));

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 保持视频时，窗口一直是高亮显示
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		meetingID = getIntent().getIntExtra(HostAgent.MEETING_ID, 0);
		accountID = getIntent().getStringExtra(HostAgent.INVITER_ACCOUNT_ID);
		accountName = getIntent()
				.getStringExtra(HostAgent.INVITER_ACCOUNT_NAME);
		headUrl = getIntent().getStringExtra(HostAgent.INVITER_HEADURL);

		CustomLog.i(tag, "IncomingDialog::onCreate() meetingID: " + meetingID
				+ " | accountID: " + accountID + " | accountName: "
				+ accountName + " | headUrl: " + headUrl);

		joinBtn = (Button) findViewById(MResource.getIdByName(this,
				MResource.ID, "qn_operae_dialog_right_button"));
		joinBtn.setOnClickListener(this);
		joinBtn.setFocusable(true);
		joinBtn.requestFocus();

		ignoreBtn = (Button) findViewById(MResource.getIdByName(this,
				MResource.ID, "qn_operae_dialog_left_button"));
		ignoreBtn.setOnClickListener(this);
		ignoreBtn.setFocusable(true);

		setFinishOnTouchOutside(false);
		TextView infoView = (TextView) findViewById(MResource.getIdByName(this,
				MResource.ID, "operate_dialog_title"));
		if (MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
			infoView.setText((accountName.equalsIgnoreCase("") ? accountID
					: accountName) + getString(R.string.invitedtoattendtheconsultation));
		}else {
			infoView.setText((accountName.equalsIgnoreCase("") ? accountID
					: accountName) + getString(R.string.invitedtoattendthemeeting));
		}

		TextView meetingText = (TextView) findViewById(MResource.getIdByName(
				this, MResource.ID, "operate_dialog_nube"));
		meetingText.setText(String.valueOf(meetingID));
		headPicBg = (ImageView) findViewById(MResource.getIdByName(this,
				MResource.ID, "operate_dialog_pic_bg"));
		headPicBgSec = (ImageView) findViewById(MResource.getIdByName(this,
				MResource.ID, "operate_dialog_pic_bg_sec"));
		headPic = (RoundImageView) findViewById(MResource.getIdByName(this,
				MResource.ID, "operate_dialog_pic"));
		AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.01f, 1.0f);
		alphaAnimation1.setDuration(1500);
		alphaAnimation1.setRepeatCount(Animation.INFINITE);
		alphaAnimation1.setRepeatMode(Animation.REVERSE);
		headPicBg.setAnimation(alphaAnimation1);

		AlphaAnimation alphaAnimation2 = new AlphaAnimation(1.0f, 0.01f);
		alphaAnimation2.setDuration(1500);
		alphaAnimation2.setRepeatCount(Animation.INFINITE);
		alphaAnimation2.setRepeatMode(Animation.REVERSE);
		headPicBgSec.setAnimation(alphaAnimation2);
		alphaAnimation1.start();
		alphaAnimation2.start();
		ImageLoader imageLoader = ImageLoader.getInstance();
		// 若options没有传递给ImageLoader.displayImage(…)方法，那么从配置默认显示选项(ImageLoaderConfiguration.defaultDisplayImageOptions(…))将被使用
		imageLoader.displayImage(headUrl, headPic, displayImageOpt(),
				new SimpleImageLoadingListener() {

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						CustomLog.i(tag, "displayImage onLoadingComplete ");
					}
				});

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		audio = (AudioManager) getSystemService(AUDIO_SERVICE);
		mSystemManager = new SystemManger(this);
		// 停止音乐播放,获取音频服务焦点
		mSystemManager.pauseMusic(true);
	
		// 释放键盘锁等
	  //  mSystemManager.reenableKeyguard();
		// 先解锁，再点亮屏幕
		mSystemManager.disableKeyguard();
	}

	private DisplayImageOptions displayImageOpt() {

		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showStubImage(
						MResource.getIdByName(IncomingDialog.this,
								MResource.DRAWABLE,
								"jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片在下载期间显示的图片
				.showImageForEmptyUri(
						MResource.getIdByName(IncomingDialog.this,
								MResource.DRAWABLE,
								"jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(
						MResource.getIdByName(IncomingDialog.this,
								MResource.DRAWABLE,
								"jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片加载/解码过程中错误时候显示的图片
				.cacheInMemory(true)// 是否緩存都內存中
				.cacheOnDisc(true)// 是否緩存到sd卡上
				.displayer(new RoundedBitmapDisplayer(20))// 设置图片的显示方式 : 设置圆角图片
															// int //
															// // roundPixels
				.bitmapConfig(Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
				.delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
				.build();
		return options;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(this, MResource.ID,
				"qn_operae_dialog_right_button")) {
			CustomLog
					.i(tag,
							"x1 user click join  button, shoud goto meetingroom activity");


			ignoreTimer.cancel();
			Intent joinIntent = new Intent();
			joinIntent.setPackage(MeetingManager.getInstance().getRootDirectory());
			joinIntent.setAction(JOIN_MEETING_BROADCAST);
			joinIntent.putExtra(HostAgent.MEETING_ID, meetingID);
			joinIntent.putExtra(HostAgent.INVITER_ACCOUNT_ID, accountID);
			joinIntent.putExtra(HostAgent.INVITER_ACCOUNT_NAME, accountName);
			sendBroadcast(joinIntent);

			if (VoicePlayState.NONE != state) {
				state = VoicePlayState.NONE;
				releasePlayResource();
			}


			IncomingDialog.this.finish();
		} else if (id == MResource.getIdByName(this, MResource.ID,
				"qn_operae_dialog_left_button")) {
			CustomLog
					.i(tag,
							"x1 user click ignore button, shoud finish IncomingActivity");

			ignoreTimer.cancel();
			Intent ignoreIntent = new Intent();
			ignoreIntent.setAction(IGNORE_MEETING_BROADCAST);
			sendBroadcast(ignoreIntent);
			if (VoicePlayState.NONE != state) {
				state = VoicePlayState.NONE;
				releasePlayResource();
			}

			IncomingDialog.this.finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		switch (keyCode) {
		// 音量减小
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			state = VoicePlayState.NONE;
			releasePlayResource();
			return true;
			// 音量增大
		case KeyEvent.KEYCODE_VOLUME_UP:
			state = VoicePlayState.NONE;
			releasePlayResource();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart() {
		CustomLog.d(tag, "incoming activity init");

		if (state == VoicePlayState.PLAYING) {
			CustomLog.d(tag, "current play state is PLAYING, ignore");
			super.onStart();
			return;
		}

		ignoreTimer = new Timer();
		ignoreTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				CustomLog.i(tag, "deadtime, finish IncomingActivity");

				handler.sendEmptyMessage(TIMEOUT_INCOMING_RING_MESSAGE);
			}
		}, ringTimelong);

		state = VoicePlayState.PLAYING;
		handler.sendEmptyMessage(PLAY_ALARM_MESSAGE);

		super.onStart();
	}

	@Override
	protected void onStop() {
		CustomLog.i(tag, "handle IncomingActivity onStop method");
	
//		CustomLog.i(tag, "ignore incoming meeting and release play resource");
//		if(ignoreTimer!=null){
//			ignoreTimer.cancel();
//		}
	
//		Intent ignoreIntent = new Intent();
//		ignoreIntent.setAction(IGNORE_MEETING_BROADCAST);
//		sendBroadcast(ignoreIntent);
//		
		

		
		handler.sendEmptyMessageDelayed(TIMEOUT_STOP,
				DELAY_UPDATE_SECOND);
		CustomLog.i(tag, "sendEmptyMessageDelayed TIMEOUT stop");
//		IncomingDialog.this.finish();
		super.onStop();
	}
	
	private void stopIncomingDialog(){
		CustomLog.i(tag, "hhandle IncomingActivity stopIncomingDialog method");
//		CustomLog.i(tag, "ignore incoming meeting and release play resource");
		if (VoicePlayState.PLAYING == state) {
			releasePlayResource();
			state = VoicePlayState.NONE;
		}
		if(ignoreTimer!=null){
			ignoreTimer.cancel();
		}
	
		Intent ignoreIntent = new Intent();
		ignoreIntent.setAction(IGNORE_MEETING_BROADCAST);
		sendBroadcast(ignoreIntent);
		
		


     	IncomingDialog.this.finish();
	}
	
	@Override
	protected void onDestroy(){
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		if (VoicePlayState.PLAYING == state) {
			releasePlayResource();
			state = VoicePlayState.NONE;
		}
		if (mSystemManager != null) {
			// 释放休眠锁等
			mSystemManager.releaseScreenOffWakeLock();
			mSystemManager.releaseWakeLock();
			// 释放键盘锁等
		    mSystemManager.reenableKeyguard();
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		CustomLog.i(tag, "ignore backpress key");
		return;
	}

	protected void onMenuBtnPressed() {
		CustomLog.i(tag, "ignore menu key");
		return;
	}

	private int playAlarm() {
		CustomLog.i(tag, "play alarm, init mediaplayer");
		if (Build.MODEL.equals("ZTE U930HD")) {
			startPlay(Uri.parse("android.resource://"
					+ this.getPackageName()
					+ "/"
					+ MResource.getIdByName(getApplicationContext(),
						MResource.RAW, "jmeetingsdk_incoming")));			
		}else{
			startRing(Uri.parse("android.resource://"
					+ this.getPackageName()
					+ "/"
					+ MResource.getIdByName(getApplicationContext(),
						MResource.RAW, "jmeetingsdk_incoming")));
		}
		
		return 0;
	}
	
	 public void startRing(Uri ringUri) {
	      
	            CustomLog.v(tag,"AudioPlayerService start ring");
	            if (ringUri == null) {
	                return;
	            }
	    
	            AudioManager audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	            ringtone = RingtoneManager.getRingtone(this, ringUri);
	            // 获取系统的振动模式
	            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    
	    //        ringVolume = audioMgr.getStreamVolume(AudioManager.STREAM_RING);
	    
	            int mode = audioMgr.getRingerMode();
	            int vibratorSetR = audioMgr
	                    .getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
	            boolean bVibrateRing = audioMgr
	                    .shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER);
	            CustomLog.v(tag,"ringerMode:" + mode + " | vibratorSetR:" + vibratorSetR
	                    + " | bVibrateRing:" + bVibrateRing);
	    
	            if (mode == AudioManager.RINGER_MODE_NORMAL) {
	                audioMode = audioMgr.getMode();
	                audioMgr.setMode(AudioManager.MODE_RINGTONE);
	                ringtone.play();
	            } else if (mode == AudioManager.RINGER_MODE_VIBRATE) {
	                vibrator.vibrate(new long[] { 0, 400, 100, 400, 100 }, 0);
	                bVibrateRing = false;
	            } else if (mode == AudioManager.RINGER_MODE_SILENT) {
	            }
	    
	            if (bVibrateRing) {
	                vibrator.vibrate(new long[] { 0, 400, 100, 400, 100 }, 0);
	            }

	            isRing = true;

	            CustomLog.v(tag,"AudioPlayerService end ring");
	      
	    }

	
	   public void stopPlay() {
	        
	            CustomLog.v(tag,"AudioPlayerService stop play");
	            if (isRing) {
	                // stop ring
	                if (ringtone != null && ringtone.isPlaying()) {
	                    ringtone.stop();
	                    ringtone = null;
	                }
	                if (vibrator != null) {
	                    vibrator.cancel();
	                    vibrator = null;
	                }
	                // 还原系统原来的模式
	                if (audioMode != -1) {
	                    CustomLog.v(tag,"ring,还原系统audioMode：" + audioMode);
	                    ((AudioManager) getSystemService(Context.AUDIO_SERVICE))
	                            .setMode(audioMode);
	                    audioMode = -1;
	                }
	                isRing = false;
	            }
	         
	           
	       
	    }
	   
	   public void startPlay(Uri ringUri) {
	        
	          CustomLog.v(tag,"Mediaplayer start play");
	  		  audioMode = audio.getMode();
	          CustomLog.v(tag,"system audio mode：" + audioMode);
	
	        if (!Build.MODEL.equalsIgnoreCase("ZTE U930HD")) {
	        //	  audioMgr.setMode(AudioManager.MODE_IN_CALL);
	        	audio.setMode(AudioManager.MODE_RINGTONE);        
	        } else {
	            // 中兴U930HD手机，设置MODE_IN_CALL无法播放出声音，故使用外放
	        	audio.setMode(AudioManager.MODE_NORMAL);
	        }
	
			mp = new MediaPlayer();
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (null != mp) {
						mp.release();
						mp = null;
					}
					CustomLog.i(tag, "play alarm finish and release mp");
	
					handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
				}
			});
	
			mp.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					if (null != mp) {
						mp.release();
						mp = null;
					}
	
					CustomLog.i(tag, "play alarm failed and release mp");
	
					handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
					return false;
				}
			});
	
			try {
				mp.setDataSource(
						this,
						ringUri);
				mp.prepare();
				mp.start();
			} catch (IllegalArgumentException e) {
				CustomLog.e(tag, "play alarm error:" + e.getMessage());
				e.printStackTrace();
			//	return -1;
			} catch (IllegalStateException e) {
				CustomLog.e(tag, "play alarm error:" + e.getMessage());
				e.printStackTrace();
			//	return -1;
			} catch (IOException e) {
				CustomLog.e(tag, "play alarm error:" + e.getMessage());
				e.printStackTrace();
			//	return -1;
			}
	    }


	private void playTTS() {
		CustomLog.i(tag, "begin init tts");

		// tts = new TextToSpeech(this, this, "com.iflytek.tts");// 科大讯飞tts
		// tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
		// @Override
		// public void onStart(String utteranceId) {
		// CustomLog.i(tag, "start speaking");
		// }
		//
		// @Override
		// public void onDone(String utteranceId) {
		// CustomLog.i(tag, "speak finish, utteranceId:" + utteranceId);
		//
		// handler.sendEmptyMessage(PLAY_TTS_FINISH_MESSAGE);
		// }
		//
		// @Override
		// public void onError(String utteranceId) {
		// CustomLog.e(tag, "speak error, utteranceId:" + utteranceId);
		//
		// handler.sendEmptyMessage(PLAY_TTS_FINISH_MESSAGE);
		// }
		// });
	}

	private void releasePlayResource() {
		CustomLog.i(tag, "release play resource");
		if (Build.MODEL.equals("ZTE U930HD")) {
		      // 还原系统原来的模式
	        if (audioMode != -1) {
	            CustomLog.v(tag,"ring,还原系统audioMode：" + audioMode);
	            ((AudioManager) getSystemService(Context.AUDIO_SERVICE))
	                    .setMode(audioMode);
	            audioMode = -1;
	        }
			if (null != mp) {
				try {
					mp.stop();
					CustomLog.v(tag,"释放mideaplayer");
				} catch (IllegalStateException e) {
					CustomLog.e(tag, "stop mediaplayer error:" + e.getMessage());
				}

				mp.release();
				mp = null;
			}
		}else{
			 
				
			stopPlay();
		}

//
//		if (null != tts) {
//			tts.stop();
//			tts.shutdown();
//			tts = null;
//		}
		
	}
	
	  @Override
	  protected void onResume() {
	    CustomLog.i(tag, "handle IncomingActivity onResume method");
		mSystemManager.releaseWakeLock();
		mSystemManager.releaseScreenOffWakeLock() ;
	    mSystemManager.acquireWakeLock();
//	    mHandler.sendEmptyMessageDelayed(MSG_HEART,
//				DELAY_UPDATE_SECOND);
	    handler.removeMessages(TIMEOUT_STOP);
	    CustomLog.i(tag, "remove TIMEOUT stop");
	    super.onResume();
	  }

	// @Override
	// public void onInit(int status) {
	// if (TextToSpeech.SUCCESS == status) {
	// CustomLog.i(tag, "init tts success, start speak");
	//
	// HashMap<String, String> map = new HashMap<String, String>();
	// map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
	// if (null != tts) {
	// tts.speak(playText, TextToSpeech.QUEUE_FLUSH, map);
	// }
	// } else {
	// CustomLog.e(tag, "init tts fail:" + status);
	// }
	// }
}
