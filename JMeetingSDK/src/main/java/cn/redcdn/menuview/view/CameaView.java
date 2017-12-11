/*===================================================================
 * 南京青牛通讯技术有限公司
 * 日期：2015-12-3 下午2:51:49
 * 作者：zl
 * 版本：1.0.0
 * 版权：All rights reserved.
 *===================================================================
 * 修订日期           修订人               描述
 * 2015-12-3     zl      创建
 */
package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public abstract class CameaView extends BaseView {

	public static enum CameraType {
		OwnNoSpeak, OwnSpeak, NoSpeake;
	}

	private final String TAG = "CameaView";
	private Context mContext;
	private String tag = CameaView.class.getName();
	private Button moive = null;
	private Button changeMoiveMode = null;
	private Button changeMicMode = null;
	private Button changeAudioSpeakerMode = null;
	private Button changeScale = null;
	private TextView live;
	// TextView tvMicAloneName;
	private TextView tvMic1Name;
	private TextView tvMic2Name;
	// private LinearLayout micNameLinear;
	private boolean isCloseMoive = false;
	private ButelOpenSDK mButelOpenSDK;
	private int currentModeStyle = 0;
	private boolean bShareDocStatus = false;
	private int currentMicStyle = SpeakerInfo.MIC_STATUS_ON; // 0普通状态，1静音状态
	private int currentAudioSpeakerStyle = cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN; //1开启状态；2关闭状态
	private String mAccountId;
	private int showType;
	private String zoomOutAccount;

	private OnClickListener btnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == MResource.getIdByName(mContext, MResource.ID,
					"camera_icon")) {
				CameaView.this.onClick(v);
			} else if (id == MResource.getIdByName(mContext, MResource.ID,
					"change_mode_icon")) {
				CustomLog.e(tag, "点击切换模式");
				CameaView.this.onClick(v);
			} else if (id == MResource.getIdByName(mContext, MResource.ID,
					"change_mic_icon")) {
				CustomLog.e(tag, "点击静音模式");
				CameaView.this.onClick(v);
			} else if(id == MResource.getIdByName(mContext, MResource.ID,
					"change_audio_speaker_icon")) {
				CustomLog.e(tag,"点击扬声器模式");
				CameaView.this.onClick(v);
			}else if(id == MResource.getIdByName(mContext, MResource.ID,"change_scale_icon")){
				CustomLog.e(tag,"点击缩小按钮");
				CameaView.this.onClick(v);
			}
		}
	};

	private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener = new ButelOpenSDKNotifyListener() {
		@Override
		public void onNotify(int arg0, Object arg1) {
			switch (arg0) {
			case NotifyType.CHANGE_MODE:
				CustomLog.d(TAG, "NotifyType.CHANGE_MODE=" + (Integer) arg1);
				// handleChangeMode((Integer) arg1);
				break;
			case NotifyType.START_SHARE_DOC:
				bShareDocStatus = true;
				CustomLog.d(TAG, "NotifyType.START_SHARE_DOC=");
				break;
			case NotifyType.STOP_SHARE_DOC:
				CustomLog.d(TAG, "NotifyType.STOP_SHARE_DOC=");
				bShareDocStatus = false;
				// handleStopShareDoc();
				break;

			case NotifyType.SHARE_NAME_CHANGE:
				CustomLog.d(TAG, "NotifyType.SHARE_NAME_CHANGE="
						+ mButelOpenSDK.getShareDocName());// + "1="
				// + mButelOpenSDK.getMic1UserName() + "2="
				// + mButelOpenSDK.getMic2UserName());
				// handleShareNameChange(currentModeStyle);
				break;
			case NotifyType.START_SPEAK:
				Cmd startRes = null;
				startRes = (Cmd) arg1;
				if (startRes == null)
					return;
				CustomLog.d(TAG,
						"NotifyType.START_SPEAK=" + startRes.getAccountId()
								+ " " + startRes.getUserName());
				handleIconShowType();
				// SpeakerInfo micInfo = mButelOpenSDK
				// .getSpeakerInfoById(mAccountId);
				// if (micInfo != null)
				// setMicModeState(true, micInfo.getMICStatus());
				break;
			case NotifyType.STOP_SPEAK:
				Cmd stopRes = null;
				stopRes = (Cmd) arg1;
				if (stopRes == null)
					return;
				CustomLog.d(TAG,
						"NotifyType.STOP_SPEAK=" + stopRes.getAccountId() + " "
								+ stopRes.getUserName());
				handleIconShowType();
				// setMicModeState(false, SpeakerInfo.MIC_STATUS_ON);
				break;
			case NotifyType.SPEAKER_ON_LINE:
				handleIconShowType();
				break;
			case NotifyType.SPEAKER_OFF_LINE:
				handleIconShowType();
				break;
			/*
			 * case NotifyType.SERVER_NOTICE_STREAM_PUBLISH: Cmd respModel =
			 * (Cmd) arg1; if (respModel == null) return; CustomLog.d(TAG,
			 * "SERVER_NOTICE_STREAM_PUBLISH getMediaType() " +
			 * respModel.getMediaType()); handleIconShowType(); break; case
			 * NotifyType.SERVER_NOTICE_STREAM_UNPUBLISH: Cmd resp = (Cmd) arg1;
			 * if (resp == null) return; CustomLog.d(TAG,
			 * "SERVER_NOTICE_STREAM_UNPUBLISH getMediaType() " +
			 * resp.getMediaType()); handleIconShowType(); break;
			 */
			default:
				break;
			}
		}
	};

	public void handleIconShowType() {
		CustomLog.d(TAG, "handleIconShowType:");
		if (mButelOpenSDK.getSpeakers() != null
				&& mButelOpenSDK.getSpeakers().size() > 0) {
			CustomLog.d(TAG, "handleIconShowType 1111111111111111");
			SpeakerInfo micInfo = mButelOpenSDK.getSpeakerInfoById(mAccountId);
			if (mAccountId != null && micInfo != null) {
				CustomLog.d(TAG, "handleIconShowType2222222222222222222");
				setViewPosition(CameraType.OwnSpeak, 0);
				setMicModeState(true, micInfo.getMICStatus());
				if(mButelOpenSDK.getSpeakers().size()==1){
					setAudioSpeakerModeState(true,mButelOpenSDK.getMyLoudspeakerStatus());
				}else{
					setAudioSpeakerModeState(true,mButelOpenSDK.getMyLoudspeakerStatus());
				}
				
			} else {
				CustomLog.d(TAG, "handleIconShowType33333333333333333333");
				setViewPosition(CameraType.OwnNoSpeak, 0);
				setMicModeState(false, SpeakerInfo.MIC_STATUS_ON);
				setAudioSpeakerModeState(true,mButelOpenSDK.getMyLoudspeakerStatus());
			}

		} else {
			CustomLog.d(TAG, "handleIconShowType444444444444444444");
			setViewPosition(CameraType.NoSpeake, 0);
			setMicModeState(false, SpeakerInfo.MIC_STATUS_ON);
			setAudioSpeakerModeState(false,cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN);
		}
	}

	public void setMicModeState(boolean canShow, int micStyle) {
		CustomLog.d(TAG, "setMicModeState:" + canShow + "," + micStyle);
		currentMicStyle = micStyle;
		if (canShow) {
			// 发言状态
			changeMicMode.setVisibility(View.VISIBLE);
			if (currentMicStyle == SpeakerInfo.MIC_STATUS_ON) {
				// 图标替换
				changeMicMode.setBackgroundResource(MResource.getIdByName(
						mContext, MResource.DRAWABLE,
						"jmeetingsdk_change_mode_mic_open_selector"));
			} else {
				changeMicMode.setBackgroundResource(MResource.getIdByName(
						mContext, MResource.DRAWABLE,
						"jmeetingsdk_change_mode_mic_close_selector"));
			}

		} else {
			// 取消发言状态
			changeMicMode.setVisibility(View.GONE);
		}

	}
	
	public void setAudioSpeakerModeState(boolean audioSpeakerCanShow, int audioSpeakerStyle) {
		CustomLog.d(TAG,"setAudioSpeakerModeState:" + audioSpeakerCanShow + "," + audioSpeakerStyle);
		currentAudioSpeakerStyle = audioSpeakerStyle;
		if(audioSpeakerCanShow) {
			// 发言状态
			changeAudioSpeakerMode.setVisibility(View.VISIBLE);
			if (currentAudioSpeakerStyle == cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN) {
				// 图标替换
				changeAudioSpeakerMode.setBackgroundResource(MResource.getIdByName(
						mContext, MResource.DRAWABLE,
						"jmeetingsdk_change_mode_audio_speaker_open_selector"));
			} else {
				changeAudioSpeakerMode.setBackgroundResource(MResource.getIdByName(
						mContext, MResource.DRAWABLE,
						"jmeetingsdk_change_mode_audio_speaker_close_selector"));		
			}
		} else {
	        // 取消发言状态
			changeAudioSpeakerMode.setVisibility(View.GONE);
		}
	}

	public CameaView(Context context, ButelOpenSDK butelOpenSDK,
                     String accountId) {

		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"meeting_view_camera"));
		mContext = context;
		mAccountId = accountId;
		moive = (Button) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "camera_icon"));
		changeMoiveMode = (Button) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "change_mode_icon"));
		changeMicMode = (Button) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "change_mic_icon"));
		changeAudioSpeakerMode = (Button) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "change_audio_speaker_icon"));
		changeScale  = (Button) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "change_scale_icon"));
		if (MeetingManager.getInstance().getShowMeetingFloat()){

		}else {
			changeScale.setVisibility(GONE);
		}
		live = (TextView) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "live_titile"));
		moive.setOnClickListener(btnOnClickListener);
		changeMoiveMode.setOnClickListener(btnOnClickListener);
		changeMicMode.setOnClickListener(btnOnClickListener);
		changeAudioSpeakerMode.setOnClickListener(btnOnClickListener);
		changeScale.setOnClickListener(btnOnClickListener);
		// tvMicAloneName = (TextView)
		// findViewById(MResource.getIdByName(mContext, MResource.
		// ID,"speak_name_titile" ));
		tvMic1Name = (TextView) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "mic1_name_titile"));
		tvMic2Name = (TextView) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "mic2_name_titile"));
		// micNameLinear = (LinearLayout)
		// findViewById(MResource.getIdByName(mContext, MResource.
		// ID,"micNameLinear" ));
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int mScreenWidth = 0;
		int mScreenHeight = 0;
		if (size.x > size.y) {
			mScreenWidth = size.x;
			mScreenHeight = size.y;
		} else {
			mScreenWidth = size.y;
			mScreenHeight = size.x;
		}
		int topMargin = (mScreenHeight - (mScreenWidth / 2) * 9 / 16) / 2;
		CustomLog.e(TAG, "gaodu :" + topMargin);
		// FrameLayout.LayoutParams lllp = (FrameLayout.LayoutParams)
		// micNameLinear
		// .getLayoutParams();
		// lllp.setMargins(0, topMargin, 0, 0);
		mButelOpenSDK = butelOpenSDK;
		mButelOpenSDK
				.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
	}

	/*
	 * private void handleShareNameChange(int currentModeStyle) { switch
	 * (currentModeStyle) { case ModeStatusCode.MODE_EQUAL_SIZE: if
	 * (mButelOpenSDK.getMic1UserId() != null && mButelOpenSDK.getMic1UserName()
	 * != null) { if (mButelOpenSDK.getMic1UserId()
	 * .equals(MeetingStatus.getInstance().accountId)) {
	 * tvMic1Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getShareDocName(), 6));
	 * tvMic1Name.setVisibility(View.VISIBLE); } else {
	 * tvMic1Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMic1Name.setVisibility(View.VISIBLE); } } if
	 * (mButelOpenSDK.getMic2UserId() != null && mButelOpenSDK.getMic2UserName()
	 * != null) { if (mButelOpenSDK.getMic2UserId()
	 * .equals(MeetingStatus.getInstance().accountId)) {
	 * tvMic2Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getShareDocName(), 6));
	 * tvMic2Name.setVisibility(View.VISIBLE); } else {
	 * tvMic2Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMic2Name.setVisibility(View.VISIBLE); } } break; case
	 * ModeStatusCode.MODE_MIC_1_ALONE: if (mButelOpenSDK.getMic1UserId() !=
	 * null && mButelOpenSDK.getMic1UserName() != null) { if
	 * (mButelOpenSDK.getMic1UserId()
	 * .equals(MeetingStatus.getInstance().accountId)) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getShareDocName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } else {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } } break; case
	 * ModeStatusCode.MODE_MIC_2_ALONE: currentModeStyle =
	 * ModeStatusCode.MODE_MIC_2_ALONE; if (mButelOpenSDK.getMic2UserId() !=
	 * null && mButelOpenSDK.getMic2UserName() != null) { if
	 * (mButelOpenSDK.getMic2UserId()
	 * .equals(MeetingStatus.getInstance().accountId)) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getShareDocName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } else {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } } break; case
	 * ModeStatusCode.MODE_MIC_1_ORDER_ALONE: if (mButelOpenSDK.getMic1UserId()
	 * != null && mButelOpenSDK.getMic1UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getShareDocName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_2_ORDER_ALONE: if (mButelOpenSDK.getMic2UserId()
	 * != null && mButelOpenSDK.getMic2UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getShareDocName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_NO_SPEAKER: break; default: break; } }
	 * 
	 * private void handleStopShareDoc() { switch (currentModeStyle) { case
	 * ModeStatusCode.MODE_EQUAL_SIZE: if (mButelOpenSDK.getMic1UserId() != null
	 * && mButelOpenSDK.getMic1UserName() != null) {
	 * tvMic1Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMic1Name.setVisibility(View.VISIBLE); } if
	 * (mButelOpenSDK.getMic2UserId() != null && mButelOpenSDK.getMic2UserName()
	 * != null) { tvMic2Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMic2Name.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_1_ALONE: if (mButelOpenSDK.getMic1UserId() !=
	 * null && mButelOpenSDK.getMic1UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_2_ALONE: currentModeStyle =
	 * ModeStatusCode.MODE_MIC_2_ALONE; if (mButelOpenSDK.getMic2UserId() !=
	 * null && mButelOpenSDK.getMic2UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_1_ORDER_ALONE: if (mButelOpenSDK.getMic1UserId()
	 * != null && mButelOpenSDK.getMic1UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_2_ORDER_ALONE: if (mButelOpenSDK.getMic2UserId()
	 * != null && mButelOpenSDK.getMic2UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_NO_SPEAKER: break; default: break; } }
	 */

	/*
	 * private void handleChangeMode(int mode) {
	 * tvMicAloneName.setVisibility(View.INVISIBLE);
	 * tvMic1Name.setVisibility(View.INVISIBLE);
	 * tvMic2Name.setVisibility(View.INVISIBLE); currentModeStyle = mode; if
	 * (bShareDocStatus) { handleShareNameChange(currentModeStyle); } else {
	 * switch (mode) { case ModeStatusCode.MODE_EQUAL_SIZE:
	 * 
	 * if (mButelOpenSDK.getMic1UserId() != null &&
	 * mButelOpenSDK.getMic1UserName() != null) {
	 * tvMic1Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMic1Name.setVisibility(View.VISIBLE); } if
	 * (mButelOpenSDK.getMic2UserId() != null && mButelOpenSDK.getMic2UserName()
	 * != null) { tvMic2Name.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMic2Name.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_1_ALONE: if (mButelOpenSDK.getMic1UserId() !=
	 * null && mButelOpenSDK.getMic1UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_2_ALONE: if (mButelOpenSDK.getMic2UserId() !=
	 * null && mButelOpenSDK.getMic2UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_1_ORDER_ALONE: if (mButelOpenSDK.getMic1UserId()
	 * != null && mButelOpenSDK.getMic1UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic1UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_MIC_2_ORDER_ALONE: if (mButelOpenSDK.getMic2UserId()
	 * != null && mButelOpenSDK.getMic2UserName() != null) {
	 * tvMicAloneName.setText(CommonUtil.getLimitSubstring(
	 * mButelOpenSDK.getMic2UserName(), 6));
	 * tvMicAloneName.setVisibility(View.VISIBLE); } break; case
	 * ModeStatusCode.MODE_NO_SPEAKER: break; default: break; } } }
	 */

	public void setLiveTitleShow(int isNeedShow) {
		CustomLog.e(tag, "setLiveTitleShow " + isNeedShow);
		if (isNeedShow == 1) {
			live.setVisibility(View.VISIBLE);
		} else {
			live.setVisibility(View.GONE);
		}
	}

	// 单屏，两个人发言时
	public void setView(CameraType t, float desity) {
		/*
		 * if (isCloseMoive) changeMoiveMode
		 * .setBackgroundResource(MResource.getIdByName(mContext,
		 * MResource.DRAWABLE
		 * ,"jmeetingsdk_change_mode_moive_video_close_selector" )); else
		 * changeMoiveMode
		 * .setBackgroundResource(MResource.getIdByName(mContext,
		 * MResource.DRAWABLE
		 * ,"jmeetingsdk_change_mode_moive_video_open_selector" ));
		 */
		setViewPosition(t, desity);
	}

	public void setShowType(int showType, String account) {
		CustomLog.d(TAG, "setShowType");
		this.showType = showType;
		this.zoomOutAccount = account;
		handleIconShowType();
	}

	private void setViewPosition(CameraType t, float desity) {

		LinearLayout rl = (LinearLayout) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "cameraLinear"));
		RelativeLayout.LayoutParams lllp = (RelativeLayout.LayoutParams) rl
				.getLayoutParams();
		switch (t) {
		case OwnSpeak:
			// 自己发言
			CustomLog.d("CameaView", "OwnSpeak");
			moive.setVisibility(View.VISIBLE);
			changeMicMode.setVisibility(View.VISIBLE);
			// changeMoiveMode.setVisibility(View.VISIBLE);
			setVideoIcon();
			setAudioSpeakerIcon();
			// lllp.setMargins(0, (int) (18 * desity), (int) (25 * desity), 0);
			// rl.setLayoutParams(lllp);
			break;

		case OwnNoSpeak:
			// 其他发言
			CustomLog.d("CameaView", "OwnNoSpeak");
			moive.setVisibility(View.GONE);
			changeMicMode.setVisibility(View.GONE);
			changeMoiveMode.setVisibility(View.VISIBLE);
			changeAudioSpeakerMode.setVisibility(View.VISIBLE);
			// lllp.setMargins(0, (int) (18 * desity), (int) (0 * desity), 0);
			// rl.setLayoutParams(lllp);
			break;
		case NoSpeake:
			// 无人发言
			CustomLog.d("CameaView", "NoSpeake");
			moive.setVisibility(View.GONE);
			changeMicMode.setVisibility(View.GONE);
			changeMoiveMode.setVisibility(View.GONE);
			changeAudioSpeakerMode.setVisibility(View.GONE);
			break;
		default:
			break;

		}
	}

	private void setVideoIcon() {
		CustomLog.d(TAG, "setVideoIcon " + showType);
		switch (showType) {
		case 0:
			if (mButelOpenSDK.getSpeakers() != null
					&& mButelOpenSDK.getSpeakers().size() > 1) {
				changeMoiveMode.setVisibility(View.VISIBLE);
			} else {
				changeMoiveMode.setVisibility(View.GONE);
			}
			break;
		case 1:
			// 分享
			SpeakerInfo micInfo = mButelOpenSDK.getSpeakerInfoById(mAccountId);
			if (micInfo != null
					&& micInfo.getScreenShareStatus() == SpeakerInfo.SCREEN_SHARING) {
				changeMoiveMode.setVisibility(View.GONE);
			} else {
				changeMoiveMode.setVisibility(View.VISIBLE);
			}
			break;
		case 2:
			// 放大
			if (mAccountId != null && mAccountId.equals(zoomOutAccount)) {
				changeMoiveMode.setVisibility(View.GONE);
			} else {
				changeMoiveMode.setVisibility(View.VISIBLE);
			}
			break;
		}
	}
	
	private void setAudioSpeakerIcon() {
		CustomLog.d(TAG,"setAudioSpeakerIcon" + showType);
		switch(showType) {
		case 0:
			if (mButelOpenSDK.getSpeakers() != null
					&& mButelOpenSDK.getSpeakers().size() > 1) {
				changeAudioSpeakerMode.setVisibility(View.VISIBLE);
			} else {
				changeAudioSpeakerMode.setVisibility(View.GONE);
			}
			break;
		case 1:
			// 分享
			SpeakerInfo micInfo = mButelOpenSDK.getSpeakerInfoById(mAccountId);
			if (micInfo != null
					&& micInfo.getScreenShareStatus() == SpeakerInfo.SCREEN_SHARING) {
				changeAudioSpeakerMode.setVisibility(View.GONE);
			} else {
				changeAudioSpeakerMode.setVisibility(View.VISIBLE);
			}
			break;
		case 2:
			// 放大
			if (mAccountId != null && mAccountId.equals(zoomOutAccount)) {
				changeAudioSpeakerMode.setVisibility(View.GONE);
			} else {
				changeAudioSpeakerMode.setVisibility(View.VISIBLE);
			}
			break;
		}
	}

	public boolean getisCloseMoive() {
		return isCloseMoive;
	}

	public void setisCloseMoive(boolean is) {
		CustomLog.e("setisCloseMoive", is + "=isCloseMoive");
		isCloseMoive = is;
		/*
		 * if (isCloseMoive) changeMoiveMode
		 * .setBackgroundResource(MResource.getIdByName(mContext,
		 * MResource.DRAWABLE
		 * ,"jmeetingsdk_change_mode_moive_video_close_selector" )); else
		 * changeMoiveMode
		 * .setBackgroundResource(MResource.getIdByName(mContext,
		 * MResource.DRAWABLE
		 * ,"jmeetingsdk_change_mode_moive_video_open_selector" ));
		 */
	}

}
