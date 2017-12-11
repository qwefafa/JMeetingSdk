package cn.redcdn.jmeetingsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.vo.VideoParameter;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.InviteeItem;
import cn.redcdn.meeting.data.MeetingEvent;
import cn.redcdn.meeting.interfaces.AccountManagerOperation;
import cn.redcdn.meeting.interfaces.ContactOperation;
import cn.redcdn.meeting.interfaces.HostAgentOperation;

public class MeetingManager {
	private Context context;

	private int meetingID = 0;

	private String groupID = "";

	private String accountId = "";

	private String accountName = "";

	private String token = "";

	private int meetingInfo = 0;

	private boolean isMeeting = false;

	private boolean isAllowMobileNet = false;

	private boolean selectSystemCamera = true;
	
	private boolean isAutoSpeak = true;

	private boolean isMeetingRoomRunning = false;

	private final String TAG = getClass().getSimpleName().toString();

	private final String JMEETING_BROADCAST_STARTMEETING = "cn.redcdn.jmeetingsdk.meetingroom.startmeeting";

	private final String JMEETING_BROADCAST_ENDMEETING = "cn.redcdn.jmeetingsdk.meetingroom.endmeeting";

	public static final int CAMERA_FACING_BACK = 0;

	public static final int CAMERA_FACING_FRONT = 1;
	/**
	 * 普通极会议项目
	 */
	public static final int PROJECT_MEETING = 1;

	/**
	 * 可视极会议项目
	 */
	public static final int PROJECT_KESHI = 2;

	/**
	 * 红云医疗项目
	 */
	public static final int PROJECT_HVS = 3;
	
	/**
	 * 微营销
	 */
	public static final int PROJECT_WYX = 4;

	/**
	 * 触频电视
	 */
	public static final int PROJECT_TV = 5;
	
	/**
	 * MEETING_APP_JIHY 极会议
	 */
	public static final String MEETING_APP_JIHY = "JHY_MOBILE";

	/**
	 * MEETING_APP_KESHI_JIHY 可视极会议
	 */
	public static final String MEETING_APP_KESHI_JIHY = "KESHI_JIHY";

	/**
	 * MEETING_APP_BUTEL_CONSULTATION 红云会诊
	 */
	public static final String MEETING_APP_BUTEL_CONSULTATION = "HVS";
	
	/**
	 * MEETING_APP_WYX 微营销
	 */
	public static final String MEETING_APP_WYX = "QNSOFT_WYX";
	
	/**
	 * MEETING_APP_TV 触频电视
	 */
	public static final String MEETING_APP_TV = "TV";

	public static final String MEETING_APP_ZGHY = "zghy";

	public static final String  HVS_CONTACTPROVIDER= "cn.redcdn.android.hvs.provider";

	public static final String  EJIANXIU_CONTACTPROVIDER= "cn.redcdn.android.zhiguang.provider";

	public static final String  MJM_CONTACTPROVIDER= "cn.redcdn.android.meeting.provider";

	private boolean isInit = false;

	private static MeetingManager mInstance;

	private String rcUrl; // rc路由地址

	private String cfgPath; // 配置文件地址
	
	private String phoneNumber; //手机号

	private String logPath; // 日志输出地址

	private List<MeetingListener> meetingListenerList;

	private ContactOperation mContactOperation;

	private HostAgentOperation mHostAgentOperation;
	private AccountManagerOperation mAccountManagerOperation;

	private int mProjectType = PROJECT_MEETING;
	
	private String mProjectName;

	private String mRootDirectory;

	private String mAppId;


	private Boolean mIsShare = false;


	private Boolean isShowMeetingFloat = true;

	private Boolean mIsMeetingAdapter = true;

	// private SharedPreferences meetingResolutionSetting = null;
	// public static final String MEETING_RESOLUTION_TYPE =
	// "meetingResolutionType";
	// private VideoParameter frontParameter;
	// private VideoParameter backParameter;

	private SharedPreferences frontSetting = null;
	private SharedPreferences backSetting = null;
	private SharedPreferences uvcSetting = null;
	private static final String VIDEO_CAP_WIDTH = "capWidth";
	private static final String VIDEO_CAP_HEIGHT = "capHeight";
	private static final String VIDEO_CAP_FPS = "capFps";
	private static final String VIDEO_ENC_BITRATE = "encBitrate";
	private int frontDefaultRW = 640;
	private int frontDefaultRH = 360;
	private int backDefaultRW = 640;
	private int backDefaultRH = 360;
	private int uvcDefaultRW = 640;
	private int uvcDefaultRH = 360;

	public static synchronized MeetingManager getInstance() {
		if (mInstance == null) {
			mInstance = new MeetingManager();
		}
		return mInstance;
	}

	private MeetingManager() {

	}

	public void init(Context context) {
		CustomLog.i(TAG, "MeetingManager::init()");
		if (isInit == true) {
			CustomLog.i(TAG, "MeetingManager已经初始化，不作处理");
			return;
		}
		initDefaultResolution();
		meetingID = 0;
		this.context = context;
		meetingListenerList = new ArrayList<MeetingListener>();
		registerBroadcast();
		isInit = true;

	}

	private int getCameraId(int type) {
		try {
			int numberOfCameras = Camera.getNumberOfCameras();
			CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == type) {
					return i;
				}
			}
		} catch (Exception e) {
			CustomLog.e(TAG, "getCameraId Exception " + e);
		}
		return -1;
	}

	private void getCameraResolution(int type) {
		try {
			int n = getCameraId(type);
			if (n < 0) {
				return;
			}
			List<Size> list = null;
			Camera mCamera = Camera.open(n);
			Parameters parameters = mCamera.getParameters();
			if (parameters != null) {
				list = parameters.getSupportedPreviewSizes();
				if (list != null) {
					synchronized (this) {
						if (CAMERA_FACING_BACK == type) {
							Size s = getNearResolution(list, 640, 360);
							backDefaultRW = s.width;
							backDefaultRH = s.height;
						} else {
							Size s = getNearResolution(list, 640, 360);
							frontDefaultRW = s.width;
							frontDefaultRH = s.height;
						}
					}
				}
			}
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			CustomLog.e(TAG, "getCameraResolution Exception " + e);
		}
	}

	private Size getNearResolution(List<Size> list, int w, int h) {
		if (list == null)
			return null;
		int dif = 9999;
		int index = 0;
		for (int i = 0; i < list.size(); i++) {
			int n = (Math.abs((list.get(i).width) - w) + Math
					.abs((list.get(i).height) - h));
			if (n < dif) {
				dif = n;
				index = i;
			}
		}
		CustomLog.e("SettingResolutionActivity",
				"最接近 640X360 is " + list.get(index).toString());

		return list.get(index);
	}

	private void initDefaultResolution() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				CustomLog.d(TAG, "initDefaultResolution run ");
				getCameraResolution(CAMERA_FACING_BACK);
				getCameraResolution(CAMERA_FACING_FRONT);
			}
		});
		thread.start();
	}

	public void release() {
		CustomLog.i(TAG, "MeetingManager::release()");
		if (!isInit) {
			return;
		}
		// frontParameter = null;
		// backParameter = null;
		quitMeeting();
		isMeeting = false;
		meetingID = 0;
		isInit = false;
		try{
            context.unregisterReceiver(MeetingManageReceiver);
        }catch (Exception e){
        }
	}
	
	public void setPhoneNumber(String phoneNumber){
		CustomLog.i(TAG,"MeetingManager::setPhoneNumber() 设置手机号"+ phoneNumber);
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setConfigPath(String confPath) {
		CustomLog.i(TAG, "MeetingManager::setConfigPath() 设置配置文件路径" + confPath);
		this.cfgPath = confPath;
	}

	public String getConfigPath() {
		return this.cfgPath;
	}

	public void setLogPath(String logPath) {
		CustomLog.i(TAG, "MeetingManager::setLogPath() 设置日志输出路径" + logPath);
		this.logPath = logPath;
	}

	public String getLogPath() {
		return this.logPath;
	}

	public synchronized void setRcAddress(String rcUrl) {
		CustomLog.i(TAG, "MeetingManager::setRcAddress() 设置rc地址" + rcUrl);
		this.rcUrl = rcUrl;
	}

	public String getRcAddress() {
		return this.rcUrl;
	}

	public int setAccountID(String accountID) {
		this.accountId = accountID;
		return 0;
	}

	public String getAccountID() {
		return accountId;

	}
	public String getToken() {
		return token;
	}
//	public String getAppType() {
//		if (mProjectType == PROJECT_MEETING) {
//			return MEETING_APP_JIHY;
//		} else if (mProjectType == PROJECT_KESHI) {
//			return MEETING_APP_KESHI_JIHY;
//		} else if(mProjectType == PROJECT_HVS) {
//			return MEETING_APP_BUTEL_CONSULTATION;
//		} else if(mProjectType == PROJECT_WYX){
//			return MEETING_APP_WYX;
//		}else{
//			return MEETING_APP_JIHY;
//		}
//	}
	
	public void setAppType(String appType) {
		mProjectName = appType;
	}

	public void setRootDirectory(String RootDirectory) {
		CustomLog.d(TAG,"MeetingMnanager setRootDirectory " + RootDirectory);
		mRootDirectory = RootDirectory;
	}

	public String getmAppId() {
		return mAppId;
	}

	public void setmAppId(String mAppId) {
		this.mAppId = mAppId;
	}


	public String getAppType() {

		return mProjectName;

	}
	public String getRootDirectory() {

		return mRootDirectory;

	}


	public int setAccountName(String accountName) {
		this.accountName = accountName;
		return 0;
	}
	public void setShowMeetingScreenSharing(Boolean isShare) {
		mIsShare = isShare;
	}


	public void setMeetingAdapter(Boolean isMeetingAdapter) {
		mIsMeetingAdapter = isMeetingAdapter;
	}

	public Boolean getmIsMeetingAdapter() {
		return mIsMeetingAdapter;
	}

	public String getAccountName() {
		return accountName;

	}

	public int setMeetingInfo(int meetingInfo) {
		this.meetingInfo = meetingInfo;
		return 0;
	}

	public int getMeetingInfo() {
		return meetingInfo;

	}

	public int getMeetingId() {
		return meetingID;
	}

	public String getGroupID() {
		return groupID;
	}

	/*
	 * public int setCameraCollectionMode(int mode) { CustomLog.i(TAG,
	 * "MeetingManager::setCameraCollectionMode() 设置摄像头采集模式 " + mode);
	 * cameraCollectionMode = mode; return 0; }
	 * 
	 * public int getCameraCollectionMode() { return cameraCollectionMode; }
	 */

	public int getActiveMeetingId() {
		return meetingID;
	}

	public void addListener(MeetingListener listener) {
		if (!meetingListenerList.contains(listener)) {
			meetingListenerList.add(listener);
		}
	}

	public void removeListener(MeetingListener listener) {
		if (meetingListenerList.contains(listener)) {
			meetingListenerList.remove(listener);
		}
	}

	public List<String> getSupportCameraCollection() {
		// TODO 获取camera支持的摄像头采集模式
		return new ArrayList<String>();
	}

	public void setContactOperationImp(ContactOperation imp) {
		mContactOperation = imp;
	}

	public ContactOperation getContactOperationImp() {
		return mContactOperation;
	}

	public void setHostAgentOperation(HostAgentOperation imp) {
		mHostAgentOperation = imp;
	}

	public HostAgentOperation getHostAgentOperation() {
		return mHostAgentOperation;
	}

	public AccountManagerOperation getAccountManagerOperation() {
		return mAccountManagerOperation;
	}

	public void setAccountManagerOperation(
			AccountManagerOperation mAccountManagerOperation) {
		this.mAccountManagerOperation = mAccountManagerOperation;
	}
	
	public void setProjectType(int type,String projectName) {
		CustomLog.i(TAG, "MeetingManager::setProjectType() 设置项目类型 ：" + type + " 项目名称简称:");
		mProjectType = type;
		mProjectName = projectName;
	}

	public int getProjectType() {
		return mProjectType;
	}

	public VideoParameter getVideoParameter(int id) {
		if (id == CAMERA_FACING_BACK) {
			if (backSetting == null) {
				backSetting = context.getSharedPreferences("backSetting",
						Context.MODE_PRIVATE);
			}
			VideoParameter p = new VideoParameter();
			p.setCapFps(backSetting.getInt(VIDEO_CAP_FPS, 15));
			p.setCapHeight(backSetting.getInt(VIDEO_CAP_HEIGHT, backDefaultRH));
			p.setCapWidth(backSetting.getInt(VIDEO_CAP_WIDTH, backDefaultRW));
			p.setEncBitrate(backSetting.getInt(VIDEO_ENC_BITRATE, 600));
			CustomLog.d(TAG,
					id + ":" + p.getCapWidth() + "," + p.getCapHeight() + ","
							+ p.getCapFps() + "," + p.getEncBitrate());
			return p;
		} else if (id == CAMERA_FACING_FRONT){
			if (frontSetting == null) {
				frontSetting = context.getSharedPreferences("frontSetting",
						Context.MODE_PRIVATE);
			}
			VideoParameter p = new VideoParameter();
			p.setCapFps(frontSetting.getInt(VIDEO_CAP_FPS, 15));
			p.setCapHeight(frontSetting
					.getInt(VIDEO_CAP_HEIGHT, frontDefaultRH));
			p.setCapWidth(frontSetting.getInt(VIDEO_CAP_WIDTH, frontDefaultRW));
			p.setEncBitrate(frontSetting.getInt(VIDEO_ENC_BITRATE, 600));
			CustomLog.d(TAG,
					id + ":" + p.getCapWidth() + "," + p.getCapHeight() + ","
							+ p.getCapFps() + "," + p.getEncBitrate());
			return p;
		}else {
			if (uvcSetting == null) {
				uvcSetting = context.getSharedPreferences("uvcSetting",
						Context.MODE_PRIVATE);
			}
			VideoParameter p = new VideoParameter();
			p.setCapFps(uvcSetting.getInt(VIDEO_CAP_FPS, 15));
			p.setCapHeight(uvcSetting
					.getInt(VIDEO_CAP_HEIGHT, uvcDefaultRH));
			p.setCapWidth(uvcSetting.getInt(VIDEO_CAP_WIDTH, uvcDefaultRW));
			p.setEncBitrate(uvcSetting.getInt(VIDEO_ENC_BITRATE, 600));
			CustomLog.d(TAG,
					id + ":" + p.getCapWidth() + "," + p.getCapHeight() + ","
							+ p.getCapFps() + "," + p.getEncBitrate());
			return p;
		}
	}

	public void setVideoParameter(int id, VideoParameter p) {
		if (p == null) {
			CustomLog.e(TAG, "setVideoParameter p is null");
			return;
		}
		if (id == CAMERA_FACING_BACK) {
			if (backSetting == null) {
				backSetting = context.getSharedPreferences("backSetting",
						Context.MODE_PRIVATE);
			}
			Editor editor = backSetting.edit();
			editor.putInt(VIDEO_CAP_WIDTH, p.getCapWidth());
			editor.putInt(VIDEO_CAP_HEIGHT, p.getCapHeight());
			editor.putInt(VIDEO_CAP_FPS, p.getCapFps());
			editor.putInt(VIDEO_ENC_BITRATE, p.getEncBitrate());
			editor.commit();
			// backParameter = p;
		} else if (id == CAMERA_FACING_FRONT){
			if (frontSetting == null) {
				frontSetting = context.getSharedPreferences("frontSetting",
						Context.MODE_PRIVATE);
			}
			Editor editor = frontSetting.edit();
			editor.putInt(VIDEO_CAP_WIDTH, p.getCapWidth());
			editor.putInt(VIDEO_CAP_HEIGHT, p.getCapHeight());
			editor.putInt(VIDEO_CAP_FPS, p.getCapFps());
			editor.putInt(VIDEO_ENC_BITRATE, p.getEncBitrate());
			editor.commit();
			// frontParameter = p;
		}else {
			if (uvcSetting == null) {
				uvcSetting = context.getSharedPreferences("uvcSetting",
						Context.MODE_PRIVATE);
			}
			Editor editor = uvcSetting.edit();
			editor.putInt(VIDEO_CAP_WIDTH, p.getCapWidth());
			editor.putInt(VIDEO_CAP_HEIGHT, p.getCapHeight());
			editor.putInt(VIDEO_CAP_FPS, p.getCapFps());
			editor.putInt(VIDEO_ENC_BITRATE, p.getEncBitrate());
			editor.commit();
			// frontParameter = p;
		}
	}

	private void callbackEvent(int code, Object content) {
		if (meetingListenerList != null) {
			for (MeetingListener listener : meetingListenerList) {
				CustomLog.i(TAG, "MeetingManager::callbackEvent() code: "
						+ code + " des:" + MeetingEvent.eventCodeDes(code));
				listener.onEvent(code, content);
			}
		}
	}

	private BroadcastReceiver MeetingManageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CustomLog.i(TAG, "MeetingManageonReceive " + intent.getAction());
			String action = intent.getAction();
			if (action.equals(JMEETING_BROADCAST_STARTMEETING)) {
				// int meetingId=intent.getExtras().getInt("meetingId");
				isMeeting = true;
				callbackEvent(MeetingEvent.JOIN_MEETING,
						String.valueOf(meetingID));

			} else if (action.equals(JMEETING_BROADCAST_ENDMEETING)) {
				isMeeting = false;
				meetingID = 0;
				groupID = "";
				int code = intent.getExtras().getInt("eventCode");
				callbackEvent(code, MeetingEvent.eventCodeDes(code));
			} else if (action
					.equals(MeetingRoomActivity.INVITE_MEETING_BROADCAST)) {
				String inviteeId = intent.getExtras().getString("inviteeId");
				String inviteeName = intent.getExtras()
						.getString("inviteeName");
				String meetingString = intent.getExtras()
						.getString("meetingId");
				InviteeItem item = new InviteeItem();
				item.inviteeId = inviteeId;
				item.inviteeName = inviteeName;
				item.meetingId = meetingString;
				callbackEvent(MeetingEvent.MEETING_INVITE, item);

			} else if (action
					.equals(MeetingRoomActivity.OPERATION_MEETING_BROADCAST)) {
				// int meetingId=intent.getExtras().getInt("meetingId");
				int code = intent.getExtras().getInt("eventCode");

				callbackEvent(code, MeetingEvent.eventCodeDes(code));
			} else {

			}
		}

	};

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(JMEETING_BROADCAST_STARTMEETING);
		filter.addAction(JMEETING_BROADCAST_ENDMEETING);
		filter.addAction(MeetingRoomActivity.INVITE_MEETING_BROADCAST);
		filter.addAction(MeetingRoomActivity.OPERATION_MEETING_BROADCAST);
		// filter.addAction(JMeetingService.JMEETING_ONEVENT_BROADCAST);
		context.registerReceiver(MeetingManageReceiver, filter);

	}

	public int setIsAllowMobileNet(boolean isAllowMobileNet) {
		CustomLog.i(TAG, "setIsAllowMobileNet " + isAllowMobileNet);
		this.isAllowMobileNet = isAllowMobileNet;
		return 0;
	}

	public int setSelectSystemCamera(boolean selectSystemCamera) {
		CustomLog.i(TAG, "setSelectSystemCamera " + selectSystemCamera);
		this.selectSystemCamera = selectSystemCamera;
		return 0;
	}
	
	public int setIsAutoSpeak(boolean isAutoSpeak) {
		CustomLog.i(TAG, "setIsAutoSpeak " + isAutoSpeak);
		this.isAutoSpeak = isAutoSpeak;
		return 0;
	}

	public boolean getMeetingState() {
		return isMeeting;
	}

	public int joinMeeting(String token, String accountID, String accountName,
						   int meetingID) {
		return joinMeeting(token,accountID,accountName,meetingID,"");
	}

	public int joinMeeting(String token, String accountID, String accountName,
			int meetingID, String groupID) {
		// CustomLog.v(TAG, "joinMeeting");
		CustomLog.i(TAG, "joinMeeting " + "token:" + token + " accountID"
				+ accountID + " accountName" + accountName + " meetingID"
				+ meetingID + " groupID"+groupID);
		setAccountName(accountName);
		Intent meetingItent = new Intent(context, MeetingRoomActivity.class);
		meetingItent.setPackage("cn.redcdn.hvs");
		meetingItent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		meetingItent.putExtra("accountId", accountID);
		meetingItent.putExtra("accountName", accountName);
		meetingItent.putExtra("meetingId", meetingID);
		meetingItent.putExtra("token", token);
		meetingItent.putExtra("isAllowMobileNet", isAllowMobileNet);
		meetingItent.putExtra("selectSystemCamera", selectSystemCamera);
		meetingItent.putExtra("isAutoSpeak", isAutoSpeak);
		meetingItent.putExtra("groupId", groupID);
		this.meetingID = meetingID;
		this.groupID = groupID;
		context.startActivity(meetingItent);

		// onEvent(JMeetingAgent.JOIN_MEETING, null);
		return 0;

	}

	public int quitMeeting() {
		CustomLog.i(TAG, "quitMeeting ");
		CustomLog.i(TAG, "检查是否正在会议中 isMeeting " + isMeeting);
		Intent intent = new Intent(
				MeetingRoomActivity.RUQUEST_QUIT_MEETING_BROADCAST);
		intent.setPackage(mRootDirectory);
		context.sendBroadcast(intent);
		if (isMeeting == true) {
			return meetingID;
		} else {
			return 0;
		}
		// /IncomingMessageManage.getInstance().setMeetingState(false);
		// onEvent(JMeetingAgent.QUIT_MEETING_INTERFACE,null);
		// return 0;
	}

	public int showInviteMessage() {
		CustomLog.i(TAG, "showInviteMessage ");
		Intent intent = new Intent();
		// intent.putExtra(INTENT_VALUE_CODE, 0);
		// intent.putExtra(INTENT_VALUE_DES, "PHONE_RING");
		context.sendBroadcast(intent);
		return 0;
	}

	/**
	 * 设置会议室页面是否正在运行
	 * 
	 * @param state
	 *            true: 会议室页面正在运行; false：会议室页面未运行
	 */
	public void setMeetingRoomRunningState(boolean state) {
		CustomLog.i(TAG, "setMeetingRoomRunningState :" + state);
		isMeetingRoomRunning = state;
	}

	/**
	 * 获取会议室页面运行状态
	 * 
	 * @return true: 会议室页面正在运行; false：会议室页面未运行
	 */
	public boolean getMeetingRoomRunningState() {
		CustomLog.i(TAG, "getMeetingRoomRunningState :" + isMeetingRoomRunning);

		return isMeetingRoomRunning;
	}

	public interface MeetingListener {
		public void onEvent(int eventCode, Object eventContent);
	}
	public Boolean getmIsShare() {
		return mIsShare;
	}
	public Context getContext() {
		return context;
	}
	public Boolean getShowMeetingFloat() {
		return isShowMeetingFloat;
	}

	public void setShowMeetingFloat(Boolean showMeetingFloat) {
		isShowMeetingFloat = showMeetingFloat;
	}

}
