package cn.redcdn.jmeetingsdk;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import cn.redcdn.butelopensdk.vo.VideoParameter;
import cn.redcdn.contactmanager.ContactManager;
import cn.redcdn.crash.Crash;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.datacenter.meetingmanage.AcquireParameter;
import cn.redcdn.datacenter.meetingmanage.CreateMeeting;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfo;
import cn.redcdn.datacenter.meetingmanage.GetNowMeetings;
import cn.redcdn.datacenter.meetingmanage.VerifyMeetingNo;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInfo;
import cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty;
import cn.redcdn.dep.MeetingHostAgentJNI;
import cn.redcdn.incoming.HostAgent;
import cn.redcdn.incoming.IncomingMessageManage;
import cn.redcdn.jmeetingsdk.MeetingManager.MeetingListener;
import cn.redcdn.jmeetingsdk.config.NpsParamConfig;
import cn.redcdn.jmeetingsdk.config.SettingData;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogcatFileManager;
import cn.redcdn.network.httprequest.HttpErrorCode;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

public class JMeetingService extends Service {
    private final String TAG = getClass().getSimpleName().toString();
    public final static String JMEETING_ONINIT_BROADCAST = "cn.redcdn.jmeetingsdk.oninit";
    public final static String JMEETING_ONCREATMEETING_BROADCAST
        = "cn.redcdn.jmeetingsdk.oncreatmeeting";
    public static final String JMEETING_ONJOINMEETING_BROADCAST
        = "cn.redcdn.jmeetingsdk.onjoinmeeting";
    public static final String JMEETING_ONGETNOWMEETINGS_BROADCAST
        = "cn.redcdn.jmeetingsdk.ongetnowmeetings";
    public static final String MEDICAL_JMEETING_ONINCOMINGCALL_BROADCASE
        = "cn.redcdn.jmeetingsdk.onincomingcall.medical";
    public static final String JMEETING_PHONERING_BROADCAST = "cn.redcdn.jmeetingsdk.phonering";
    public static final String JMEETING_ONQUITMEETING_BROADCAST
        = "cn.redcdn.jmeetingsdk.onquitmeeting";
    public static final String JMEETING_ONEVENT_BROADCAST = "cn.redcdn.jmeetingsdk.onevent";

    public static final String JMEETING_NOTIFICATION_BROADCAST
        = "cn.redcdn.jmeetingsdk.notification";

    private final static String INTENT_MEETING_ID = "meetingID";

    private final static String INTENT_VALUE_CODE = "valueCode";
    private final static String INTENT_VALUE_DES = "valueDes";

    private final static int BOOT_ERROR_NPS = -1; // NPS获取失败

    /***
     * 定义消息类型
     */
    private final int MSG_INIT = 0x66660000;
    private final int MSG_CREATMEETING = 0x66660001;
    private final int MSG_GETNOWMEETINGS = 0x66660002;
    private final int MSG_INCOMINGCALL = 0x66660003;
    private final int MSG_JOINMEETING = 0x66660004;
    private final int MSG_QUITMEETING = 0x66660005;
    private final int MSG_CANCELCREATMEETING = 0x66660006;
    private final int MSG_GETNOWTMEETINGS = 0x66660007;
    private final int MSG_RELEASE = 0x66660008;
    private final int MSG_UPDATETOKEN = 0x66660009;
    private final int MSG_SETCURRENTUSER = 0x66660010;
    private final int MSG_SETISALLOWEDMOBILENET = 0x66660011;
    private final int MSG_HEART = 0x66660012;
    private final int MSG_GETMEETINGSTATUS = 0x66660013;
    private final int MSG_CANCELJOINMEETING = 0x66660014;
    private final int MSG_SETINSERTCONTACTURL = 0x6660016;
    private final int MSG_GETSEARCHCONTACTURL = 0x6660017;
    private final int MSG_SETAPPTYPE = 0x6660018;
    private final int MSG_SETVIDEOPARAMETER = 0x8880019;
    private final int MSG_SHARE = 0x6660020;
    private final int MSG_SETCONTACTPROVIDER = 0x6660021;
    private final int MSG_SETSELECTSYSTEMCAMERA = 0x6660022;
    private boolean selectSystemCamera = true;
    private final int MSG_SETROOTDIRECTORY = 0x6660023;
    private final int MSG_SETWXAPPID = 0x6660024;
    private final int MSG_SETMEETINGADAPTER = 0x6660025;
    private final int MSG_SETSHOWMEETINGFLOAT = 0x6660026;

    private final int DELAY_UPDATE_SECOND = 2000;

    private int cameraId;
    private int capWidth;
    private int capHeight;
    private int capFps;
    private int encBitrate;
    private String appType;
    private String authorities;
    private String appId;
    private Boolean isShare;
    private Boolean isShowMeetingFloat;
    private Boolean isMeetingAdapter;
    private String InsertContactUrl;
    private String SearchContactUrl;
    private String userID;
    private String userName;
    private String token;
    private boolean isAllowMobileNet = false;
    private String masterNps;
    private String slaveNps;
    private String rootDirectory;
    private List<String> invitersId;
    private String topic;
    private int meetingType;
    private String beginDateTime;
    private int meetingID;
    private String groupID;
    private String inviterID;
    private String inviterName;
    private String headUrl;
    private int valueCode;
    private int eventCode;
    private int heartSeq = 0;


    private String creatMeetingToken = null;
    private String getNowMeetingsToken = null;
    private String joinMeetingToken = null;

    MeetingManage meetingManage = null;
    IncomingMessageManage incomingMessageManage = null;
    MeetingListener meetingListener = null;
    CreateMeeting create;
    GetNowMeetings getNMeetings;
    VerifyMeetingNo vm;
    private boolean isPkgChanged = false;// 标示是否存在应用升级的情况，如果有，则执行配置文件的拷贝操作
    boolean isRelease = false;

    private boolean isRegistBroadcast = false;
    private boolean isInit = false;

    public static final int MSG_BOOT_FAILED = 0x00000012; // 启动失败
    public static final int MSG_ACQUIRE_NPS_CFG = 0x00000006; // 获取nps配置信息
    public static final int MSG_SET_DATACENTER_DATA = 0x00000007; // 设置datacenter数据

    private final int JMEETINGSERVICE_NOTIfACTION_ID = 110;


//    private String RootDirectory = "cn.redcdn.hvs";
    private static JMeetingService mInstance;

    // private Handler mAgentHandler;
    //	private class MeetingStatus {
    //		public String accountId = "63000001";
    //		public String accountName = "63000001";
    //		public int meetingId = 90009043;
    //		public String token = "405da3d4-5bfb-4230-94b6-c8107a2badf8";
    //		public int meetingInfo;
    //
    //	}
    //
    //	MeetingStatus meetingStatus;
    //

    public NotificationManager mNotificationManager;
    DatagramSocket dataSocket;
    int port;

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            int heartseq = 0;
        }
    };


    class ServerThread extends Thread {

        @Override
        public void run() {
            // CustomLog.i(TAG, "ServerThread run ");
            // LocalServerSocket server = null;
            // BufferedReader mBufferedReader = null;
            // PrintWriter os = null;
            // LocalSocket connect = null;
            // String readString = null;
            // try {
            // server = new
            // LocalServerSocket("cn.redcdn.jmeetingsdk.localsocket");
            // CustomLog.i(TAG, "等待建立心跳连接");
            // connect = server.accept();
            // CustomLog.i(TAG, "建立心跳连接");
            // while (true) {
            //
            // mBufferedReader = new BufferedReader(new InputStreamReader(
            // connect.getInputStream()));
            // while ((readString = mBufferedReader.readLine()) != null) {
            // if (readString.equals("heart ok"))
            // break;
            // // Log.d(TAG,readString);
            // }
            // CustomLog.i(TAG, "收到心跳回应，重置心跳超时");
            // mHandler.removeMessages(MSG_HEART);
            // mHandler.sendEmptyMessageDelayed(MSG_HEART,
            // DELAY_UPDATE_SECOND);
            //
            // }
            // } catch (IOException e) {
            // e.printStackTrace();
            // } finally {
            // try {
            // CustomLog.i(TAG, "关闭心跳连接");
            // mBufferedReader.close();
            // os.close();
            // connect.close();
            // server.close();
            // } catch (IOException e) {

            // e.printStackTrace();
            // }
            // }
            // final int PORT = 5000;
            CustomLog.i(TAG, "ServerThread run ");
            DatagramPacket dataPacket;
            byte receiveByte[];
            String receiveStr;
            try {
                // dataSocket = new DatagramSocket();

                receiveByte = new byte[1024];
                dataPacket = new DatagramPacket(receiveByte, receiveByte.length);
                receiveStr = "";
                int i = 0;
                while (i == 0)// 无数据，则循环

                {
                    dataSocket.receive(dataPacket);
                    i = dataPacket.getLength();
                    // 接收数据

                    if (i > 0) {
                        // 指定接收到数据的长度,可使接收数据正常显示,开始时很容易忽略这一点

                        receiveStr = new String(receiveByte, 0,
                            dataPacket.getLength());
                        if (receiveStr.equals("redcdn_meeting")) {
                            CustomLog.i(TAG, "收到心跳回应，重置心跳超时");
                            mHandler.removeMessages(MSG_HEART);
                            mHandler.sendEmptyMessageDelayed(MSG_HEART,
                                DELAY_UPDATE_SECOND);
                        }
                        i = 0;// 循环接收

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    ServerThread thread;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    init(token, userID, userName, masterNps, slaveNps, rootDirectory);
                    break;
                case MSG_CREATMEETING:
                    creatMeeting(invitersId);
                    break;
                case MSG_GETNOWMEETINGS:
                    getNowMeetings();
                    break;
                case MSG_INCOMINGCALL:
                    inComingCall(inviterID, inviterName, meetingID, headUrl);
                    break;
                case MSG_JOINMEETING:
                    CustomLog.d(TAG, "MSG_JOINMEETING");
                    joinMeeting(meetingID, groupID);
                    break;
                case MSG_QUITMEETING:
                    quitMeeting();
                    break;
                case MSG_CANCELCREATMEETING:
                    cancelCreatMeeting();
                    break;
                case MSG_GETNOWTMEETINGS:
                    cancelGetNowMeetings();
                    break;
                case MSG_UPDATETOKEN:
                    updateToken(token);
                    break;
                case MSG_SETCURRENTUSER:
                    setCurrentUser(userID, userName, token);
                    break;
                case MSG_SETISALLOWEDMOBILENET:
                    setIsAllowedMobileNet(isAllowMobileNet);
                    break;
                case MSG_RELEASE:
                    release();
                    break;
                case MSG_HEART:
                    CustomLog.i(TAG, "心跳超时，释放资源");
                    //	release();
                    break;
                case MSG_GETMEETINGSTATUS:
                    break;
                case MSG_CANCELJOINMEETING:
                    cancelJoinMeeting();
                    break;
                case MSG_SETSELECTSYSTEMCAMERA:
                    setSelectSystemCamera(selectSystemCamera);
                    break;
                case MSG_BOOT_FAILED:
                    onBootFailed(msg.arg1, (String) msg.obj);
                    break;

                case MSG_ACQUIRE_NPS_CFG:
                    accquireNpsConfig(masterNps, slaveNps);
                    break;

                case MSG_SET_DATACENTER_DATA:
                    initHttpRequestConfig();
                    break;

                case MSG_SETINSERTCONTACTURL:
                    setInsertContactUrl(InsertContactUrl);
                    break;

                case MSG_GETSEARCHCONTACTURL:
                    setSearchContactUrl(SearchContactUrl);
                    break;

                case MSG_SETAPPTYPE:
                    setAppType(appType);
                    break;

                case MSG_SETROOTDIRECTORY:
                    setRootDirectory(rootDirectory);
                    CustomLog.d(TAG, "MSG_SETROOTDIRECTORY");
                    break;
                case MSG_SHARE:
                    setShowMeetingScreenSharing(isShare);
                    break;
                case MSG_SETCONTACTPROVIDER:
                    setContactProvider(authorities);
                    break;
                case MSG_SETWXAPPID:
                    setWXAppId(appId);
                    break;
                case MSG_SETMEETINGADAPTER:
                    setMeetingAdapter(isMeetingAdapter);
                    break;
                case MSG_SETSHOWMEETINGFLOAT:
                    setShowMeetingFloat(isShowMeetingFloat);
                    break;

                case MSG_SETVIDEOPARAMETER:
                    setVideoParameter(cameraId, capWidth, capHeight, capFps, encBitrate);
                    break;

            }
        }
    };

    private BroadcastReceiver JMeetingServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(JMeetingService.JMEETING_NOTIFICATION_BROADCAST)) {
                // onEvent(JMeetingAgent.JOIN_MEETING, null);
                CustomLog.i(TAG, "onReceiveJMeetingServiceBroadcast:" + action);
                String idString = getActiveMeetingId();
                if (idString.equals("")) {
                    idString = "0";
                }
                if (MeetingManager.getInstance().getMeetingId() == 0) {
                    CustomLog.i(TAG, "会议状态异常：meetingStatus.meetingId：" +
                        MeetingManager.getInstance().getMeetingId() +
                        " meetingId: " + idString);
                    mNotificationManager.cancel(100);
                    CustomToast.show(JMeetingService.this,
                        getString(R.string.unnormalStateofMeeting), CustomToast.LENGTH_LONG);
                } else if (MeetingManager.getInstance().getMeetingId() ==
                    Integer.valueOf(idString)) {
                    CustomLog.i(TAG, "会议状态一致：meetingStatus.meetingId：" +
                        MeetingManager.getInstance().getMeetingId() +
                        " meetingId: " + idString);
                    joinMeeting(MeetingManager.getInstance().getMeetingId(),
                        MeetingManager.getInstance().getGroupID());
                } else {
                    CustomLog.i(TAG, "会议状态异常：meetingStatus.meetingId：" +
                        MeetingManager.getInstance().getMeetingId() +
                        " meetingId: " + idString);
                    mNotificationManager.cancel(100);
                    CustomToast.show(JMeetingService.this,
                        getString(R.string.unnormalStateofMeeting), CustomToast.LENGTH_LONG);
                }

            } else {

            }
        }

    };


    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(JMeetingService.JMEETING_NOTIFICATION_BROADCAST);
        this.registerReceiver(JMeetingServiceReceiver, filter);
        isRegistBroadcast = true;
    }


    @Override
    public IBinder onBind(Intent intent) {

        String bindString = intent.getExtras().getString("bindString");
        CustomLog.i(TAG, "onBind " + bindString);
        if (bindString.equals("cn.redcdn.jmeetingsdk.meetingservice.bindstring")) {
            return mBinder;
        } else {
            return null;
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();

//        LogcatFileManager.getInstance().setLogDir(this.rootDirectory + "/jmeetingsdk");
//        LogcatFileManager.getInstance().start("cn.redcdn.jmeetingsdkservice");
//        Crash crash = new Crash();
//        crash.setDir(this.rootDirectory + "/jmeetingsdk");
//        crash.init(this, "cn.redcdn.jmeetingsdkservice");
//        CustomLog.i(TAG, "onCreate ");


        meetingListener = new MeetingListener() {
            public void onEvent(int eventCode, Object eventContent) {
                CustomLog.v(TAG, "meetingManage onEvent" + eventCode);
                switch (eventCode) {
                    case JMeetingAgent.JOIN_MEETING:
                        MeetingManager.getInstance().setMeetingInfo(1);
                        incomingMessageManage.setMeetingState(true);
                        MeetingManager.getInstance().setAccountID(userID);
                        MeetingManager.getInstance().setAccountName(userName);
                        // MeetingStatus.getInstance().token = token;

                        showNotify();
                        Intent intent = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent.setPackage(rootDirectory);
                        intent.putExtra("eventCode", JMeetingAgent.JOIN_MEETING);
                        intent.putExtra("eventContent", (Serializable) eventContent);
                        sendBroadcast(intent);
                        break;
                    case JMeetingAgent.MEETING_INVITE:
                        Intent intent2 = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent2.setPackage(rootDirectory);
                        intent2.putExtra("eventCode", JMeetingAgent.MEETING_INVITE);
                        intent2.putExtra("eventContent",
                            (Serializable) eventContent);
                        sendBroadcast(intent2);
                        break;
                    case JMeetingAgent.QUIT_TOKEN_DISABLED:
                        Intent intent3 = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent3.setPackage(rootDirectory);
                        intent3.putExtra("eventCode", JMeetingAgent.TOKEN_DISABLED);
                        intent3.putExtra("eventContent",
                            (Serializable) joinMeetingToken);
                        sendBroadcast(intent3);
                        // break;
                    case JMeetingAgent.QUIT_MEETING_INTERFACE:
                    case JMeetingAgent.QUIT_MEETING_BACK:
                    case JMeetingAgent.QUIT_MEETING_AS_MEETING_END:
                    case JMeetingAgent.QUIT_MEETING_AS_MESSAGE_INVITE:
                    case JMeetingAgent.QUIT_MEETING_LIBS_ERROR:
                    case JMeetingAgent.QUIT_MEETING_LOCKED:
                    case JMeetingAgent.QUIT_MEETING_NOTALLOW_USE_MOBILE_NET:
                    case JMeetingAgent.QUIT_MEETING_SERVER_DESCONNECTED:
                    case JMeetingAgent.QUIT_PHONE_CALL:
                    case JMeetingAgent.QUIT_MEETING_OTHER_PROBLEM:
                    case JMeetingAgent.QUIT_MEETING_CAMERA_FAILED:
                        MeetingManager.getInstance().setMeetingInfo(0);

                        // incomingMessageManage.setMeetingState(false);
                        Intent intent1 = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent1.setPackage(rootDirectory);
                        intent1.putExtra("eventCode", eventCode);
                        intent1.putExtra("eventContent",
                            (Serializable) eventContent);
                        sendBroadcast(intent1);
                        mNotificationManager.cancel(100);
                        break;
                    case JMeetingAgent.MEETING_MENU:
                    case JMeetingAgent.MEETING_SPEAK:
                    case JMeetingAgent.MEETING_STOP_SPEAK:
                    case JMeetingAgent.MEETING_INVITE_CLICK:
                    case JMeetingAgent.MEETING_INVITE_INVITELIST:
                    case JMeetingAgent.MEETING_INVITE_NUBE:
                    case JMeetingAgent.MEETING_PARTICIPATERS:
                    case JMeetingAgent.MEETING_GIVE_MIC:
                    case JMeetingAgent.MEETING_LOCK:
                    case JMeetingAgent.MEETING_UNLOCK:
                    case JMeetingAgent.MEETING_ADD_CONTACTS:
                    case JMeetingAgent.MEETING_SWITCHWINDOW:
                    case JMeetingAgent.MEETING_EXIT:
                    case JMeetingAgent.MEETING_JOINMEETING:
                        Intent intent4 = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent4.setPackage(rootDirectory);
                        intent4.putExtra("eventCode", eventCode);
                        intent4.putExtra("eventContent",
                            (Serializable) eventContent);
                        sendBroadcast(intent4);
                        break;
                }
            }
        };
        MeetingManager.getInstance().init(this);
        MeetingManager.getInstance().addListener(meetingListener);
        incomingMessageManage = new IncomingMessageManage() {
            @Override
            protected void onEvent(int eventCode, Object eventContent) {
                CustomLog.i(TAG, "incomingMessageManage onEvent " + eventCode);
                switch (eventCode) {
                    case JMeetingAgent.PHONE_RING:
                        Intent intent = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent.setPackage(rootDirectory);
                        intent.putExtra("eventCode", JMeetingAgent.PHONE_RING);
                        IncomingItem item = (IncomingItem) eventContent;
                        intent.putExtra("eventContent", (Serializable) item);
                        sendBroadcast(intent);
                        break;
                    // case JMeetingAgent.MEETING_INVITE:
                    // meetingManage.showInviteMessage();
                    // Intent intent1 = new Intent(JMEETING_ONEVENT_BROADCAST);
                    // intent1.putExtra("eventCode", JMeetingAgent.MEETING_INVITE);
                    // intent1.putExtra("eventContent",
                    // (Serializable) "MEETING_INVITE");
                    // sendBroadcast(intent1);
                    // break;
                    case JMeetingAgent.END_PHONE_RING_AS_JOIN_MEETING:
                    case JMeetingAgent.END_PHONE_RING_AS_TIMEOUT:
                    case JMeetingAgent.END_PHONE_RING_AS_IGNORE:
                        Intent intent2 = new Intent(JMEETING_ONEVENT_BROADCAST);
                        intent2.setPackage(rootDirectory);
                        intent2.putExtra("eventCode", eventCode);
                        intent2.putExtra("eventContent",
                            (Serializable) eventContent);
                        sendBroadcast(intent2);
                        break;
                }

            }


            @Override
            protected void onJoinMeetingBtnClicked(int meetingID,
                                                   String inviterID, String inviterName) {
                //废弃的类 meetingManage始终为空
                // if (meetingManage != null) {
                // 	meetingManage.joinMeeting(token, userID, userName,
                // 			meetingID);
                // }
                MeetingManager.getInstance().joinMeeting(token, userID,
                    userName, meetingID);

            }
        };

    }


    ;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CustomLog.i(TAG, "onStartCommand");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        CustomLog.i(TAG, "onDestroy ");
        if (!isRelease) {
            release();
        }
        isRelease = false;

        stopForeground(true);// 停止前台服务
        super.onDestroy();
    }


    private void onBootSuccess() {
        CustomLog.i(TAG, "onBootSuccess ");

        //port = dataSocket.getLocalPort();
        //CustomLog.i(TAG, "getLocalPort " + port);
        // thread.start();
        MeetingManager.getInstance().setAccountID(userID);
        MeetingManager.getInstance().setAccountName(userName);
        Intent intent = new Intent(JMEETING_ONINIT_BROADCAST);
        intent.setPackage(rootDirectory);
        intent.putExtra(INTENT_VALUE_CODE, 0);
        intent.putExtra(INTENT_VALUE_DES, "INIT SUCCESS");
        intent.putExtra("serverPort", port);
        intent.putExtra("npsWebDomain", ConstConfig.npsWebDomain);
        intent.putExtra("slaveNpsWebDomain", ConstConfig.slaveNpsWebDomain);
        intent.putExtra("masterBmsWebDomain", ConstConfig.masterBmsWebDomain);
        intent.putExtra("slaveBmsWebDomain", ConstConfig.slaveBmsWebDomain);
        intent.putExtra("enterPriseUserCenterWebDomain", ConstConfig.enterPriseUserCenterWebDomain);
        intent.putExtra("personalUserCenterWebDomain", ConstConfig.personalUserCenterWebDomain);
        intent.putExtra("personalContactWebDomain", ConstConfig.personalContactWebDomain);
        intent.putExtra("masterAppUpdateServerWebDomain",
            ConstConfig.masterAppUpdateServerWebDomain);
        intent.putExtra("slaveAppUpdateServerWebDomain", ConstConfig.slaveAppUpdateServerWebDomain);
        sendBroadcast(intent);

        CustomLog.i(TAG, "sendBroadcast INIT SUCCESS");
        MeetingManager.getInstance()
            .setContactOperationImp(ContactManager.getInstance(getApplicationContext()));
        MeetingManager.getInstance().setHostAgentOperation(HostAgent.getInstance());
        MeetingManager.getInstance().setConfigPath(SettingData.getInstance().CfgPath);
        MeetingManager.getInstance().setLogPath(SettingData.getInstance().LogFileOutPath);
        MeetingManager.getInstance().setRcAddress(SettingData.getInstance().RC_URL);
        MeetingManager.getInstance()
            .setProjectType(MeetingManager.PROJECT_KESHI, MeetingManager.MEETING_APP_KESHI_JIHY);
    }


    private void onBootFailed(int statusCode, String statusInfo) {
        Intent intent = new Intent(JMEETING_ONINIT_BROADCAST);
        intent.setPackage(rootDirectory);
        intent.putExtra(INTENT_VALUE_CODE, statusCode);
        intent.putExtra(INTENT_VALUE_DES, statusInfo);
        sendBroadcast(intent);
        CustomLog.i(TAG, "onBootFailed " + statusCode + statusInfo);
    }


    private int setVideoParameter(int cameraId, int capWidth, int capHeight, int capFps, int encBitrate) {

        CustomLog.i(TAG, "setVideoParameter: cameraId= " + cameraId + "capWidth= " + capWidth +
            "capHeight " + capHeight + "capFps " + capFps + "encBitrate " + encBitrate);

        VideoParameter p = new VideoParameter(capWidth, capHeight, capFps, encBitrate);

        MeetingManager.getInstance().setVideoParameter(cameraId, p);

        return 0;
    }


    private int setAppType(String appType) {

        CustomLog.i(TAG, "setAppType: appType= " + appType);

        MeetingManager.getInstance().setAppType(appType);

        return 0;
    }


    private int setRootDirectory(String RootDirectory) {

        CustomLog.i(TAG, "setRootDirectory: RootDirectory= " + RootDirectory);
        MeetingManager.getInstance().setRootDirectory(RootDirectory);

        return 0;
    }


    private int setShowMeetingScreenSharing(Boolean isShare) {

        CustomLog.i(TAG, "setShowMeetingScreenSharing: isShare= " + isShare);

        MeetingManager.getInstance().setShowMeetingScreenSharing(isShare);

        return 0;
    }

    private int setShowMeetingFloat(Boolean isShowMeetingFloat) {

        CustomLog.i(TAG, "setShowMeetingFloat: isShowMeetingFloat= " + isShowMeetingFloat);

        MeetingManager.getInstance().setShowMeetingFloat(isShowMeetingFloat);

        return 0;
    }


    private int setContactProvider(String authorities) {

        CustomLog.i(TAG, "setContactProvider: authorities= " + authorities);

        ContactManager.getInstance(getApplicationContext()).setContactProvider(authorities);

        return 0;
    }
    private int setWXAppId(String appId) {

        CustomLog.i(TAG, "setWXAppId: appId= " + authorities);
        MeetingManager.getInstance().setmAppId(appId);


        return 0;
    }

    private int setMeetingAdapter(Boolean isMeetingAdapter) {

        CustomLog.i(TAG, "setMeetingAdapter: isMeetingAdapter= " + isMeetingAdapter);

        MeetingManager.getInstance().setMeetingAdapter(isMeetingAdapter);

        return 0;
    }
    /**
     * 通讯录查询url接口
     */
    private int setSearchContactUrl(String ContactUrl) {

        CustomLog.i(TAG, "setSearchContactUrl: ContactUrl=" + ContactUrl);

        ContactManager.getInstance(getApplicationContext()).setSearchContactUrl(ContactUrl);

        return 0;

    }


    /**
     * 通讯录插入url接口
     *
     * @param ContactUrl 通讯录url
     */
    private int setInsertContactUrl(String ContactUrl) {

        CustomLog.i(TAG, "setInsertContactUrl: ContactUrl= " + ContactUrl);

        ContactManager.getInstance(getApplicationContext()).setInsertContactUrl(ContactUrl);

        return 0;

    }


    private int init(String token, String userID, String userName,
                     String masterNps, String slaveNps,String rootDirectory) {
        CustomLog.v(TAG, "IJMeetingService init" + token + " " + userID + " "
            + userName + " " + masterNps);
        SettingData.getInstance().init(JMeetingService.this,rootDirectory);
        CustomLog.v(TAG, String.valueOf(android.os.Process.myPid()));
        //	meetingManage.init(this);
        //		MeetingManager.getInstance().init(this);
        incomingMessageManage.init(this);
        mNotificationManager = (NotificationManager) this
            .getSystemService(this.NOTIFICATION_SERVICE);
        registerBroadcast();
        //		meetingStatus.meetingInfo=0;
        //		try {
        //			dataSocket = new DatagramSocket();
        //		} catch (SocketException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
        //		thread = new ServerThread();
        setMeetingSharedPreferences("");

        accquireNpsConfig(masterNps, slaveNps);

        imageLoaderConfig();
        displayImageOpt();
        isInit = true;
        LogcatFileManager.getInstance().setLogDir(rootDirectory + "/jmeetingsdk");
        LogcatFileManager.getInstance().start("cn.redcdn.jmeetingsdkservice");
        Crash crash = new Crash();
        crash.setDir(rootDirectory + "/jmeetingsdk");
        crash.init(this, "cn.redcdn.jmeetingsdkservice");
        CustomLog.i(TAG, "onCreate ");
        MeetingManager.getInstance().setRootDirectory(rootDirectory);
        return 0;
    }


    private void imageLoaderConfig() {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
            getApplicationContext())
            .threadPriority(Thread.NORM_PRIORITY - 2)// 设置线程的优先级，比ui稍低优先级
            .threadPoolSize(3) // 线程池内加载的数量，建议1~5
            // .denyCacheImageMultipleSizesInMemory()//当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
            .discCacheFileNameGenerator(new Md5FileNameGenerator())// 设置缓存文件的名字,通过Md5将url生产文件的唯一名字
            .memoryCache(
                new LruMemoryCache(5 * 1024 * 1024)) // 可以通过自己的内存缓存实现。LruMemoryCache：缓存只使用强引用.
            // (缓存大小超过指定值时，删除最近最少使用的bitmap)
            // --默认情况下使用
            // .memoryCacheSize(2 * 1024 * 1024) // 内存缓存的最大值
            .discCacheFileCount(1000)// 缓存文件的最大个数
            .tasksProcessingOrder(QueueProcessingType.FIFO)// 设置图片下载和显示的工作队列排序
            // .enableLogging() //是否打印日志用于检查错误
            .build();

        ImageLoader.getInstance().init(config);

    }


    private void displayImageOpt() {

        new DisplayImageOptions.Builder()
            .showStubImage(
                MResource.getIdByName(this, MResource.DRAWABLE,
                    "jmeetingsdk_defaultheadimage"))// 设置图片在下载期间显示的图片
            .showImageForEmptyUri(
                MResource.getIdByName(this, MResource.DRAWABLE,
                    "jmeetingsdk_defaultheadimage"))// 设置图片Uri为空或是错误的时候显示的图片
            .showImageOnFail(
                MResource.getIdByName(this, MResource.DRAWABLE,
                    "jmeetingsdk_defaultheadimage"))// 设置图片加载/解码过程中错误时候显示的图片
            .cacheInMemory(true)// 是否緩存都內存中
            .cacheOnDisc(true)// 是否緩存到sd卡上
            .displayer(new RoundedBitmapDisplayer(20))// 设置图片的显示方式 : 设置圆角图片
            // int roundPixels
            .bitmapConfig(Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
            .delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
            .build();

    }


    private int creatMeeting(final List<String> invitersId) {
        CustomLog.v(TAG, "CreateMeeting "
            + ",inviterUserList.toString()" + invitersId.toString());
        create = new CreateMeeting() {

            @Override
            protected void onSuccess(MeetingInfo responseContent) {
                int meetingId = Integer.parseInt(responseContent.meetingId);
                String adminId = responseContent.adminPhoneId;
                CustomLog.v(TAG, "CreateMeeting meetingId=" + meetingId
                    + ",adminId=" + adminId);
                HostAgent.getInstance().invite(invitersId, meetingId, userID,
                    userName);
                Intent intent = new Intent(JMEETING_ONCREATMEETING_BROADCAST);
                intent.setPackage(rootDirectory);
                intent.putExtra(INTENT_VALUE_CODE, 0);
                cn.redcdn.jmeetingsdk.MeetingInfo
                    meetingInfo = new cn.redcdn.jmeetingsdk.MeetingInfo();
                meetingInfo.creatorId = adminId;
                meetingInfo.meetingId = responseContent.meetingId;
                intent.putExtra("MeetingInfo", (Serializable) meetingInfo);
                sendBroadcast(intent);
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.v(TAG, "creatMeeting failed" + statusCode
                    + statusInfo);
                int returnCode = 0;
                if (statusCode == -65540 || statusCode == -65544
                    || statusCode == -65548 || statusCode == -65568) {
                    returnCode = -100;
                } else if (statusCode == -65664) {
                    returnCode = -101;
                } else {
                    returnCode = -102;
                }
                if (statusCode == -902 || statusCode == -903) {
                    returnCode = statusCode;
                    Intent intent1 = new Intent(JMEETING_ONEVENT_BROADCAST);
                    intent1.setPackage(rootDirectory);
                    intent1.putExtra("eventCode", JMeetingAgent.TOKEN_DISABLED);
                    intent1.putExtra("eventContent",
                        (Serializable) creatMeetingToken);
                    sendBroadcast(intent1);
                }

                Intent intent = new Intent(JMEETING_ONCREATMEETING_BROADCAST);
                intent.setPackage(rootDirectory);
                intent.putExtra(INTENT_VALUE_CODE, returnCode);
                // intent.putExtra("statusCode", -1);
                intent.putExtra("statusInfo", statusInfo);
                sendBroadcast(intent);

            }

        };
        // List<String> phoneId = new ArrayList<String>();
        invitersId.add(userID);

        String[] str = new String[0];
        creatMeetingToken = token;
        create.createMeeting(CreateMeeting.MEETING_APP_BUTEL_CONSULTATION, topic, invitersId, token,
            meetingType, beginDateTime);
        return 0;
    }


    private int joinMeeting(final int meetingID, final String groupID) {
        CustomLog.i(TAG, "joinMeeting" + meetingID + " groupID:" + groupID);
        joinMeetingToken = token;

        String meetingIdString = String.valueOf(meetingID);
        // if (meetingIdString.equals("null") || meetingIdString.length() < 8) {
        // valueCode = -1;
        // }

        vm = new VerifyMeetingNo() {

            @Override
            protected void onSuccess(ResponseEmpty responseContent) {
                CustomLog.v(TAG, "VerifyMeetingNo onSuccess ");
                MeetingManager.getInstance().joinMeeting(joinMeetingToken, userID, userName,
                    meetingID, groupID);
                Intent intent = new Intent(JMEETING_ONJOINMEETING_BROADCAST);
                intent.setPackage(rootDirectory);
                intent.putExtra(INTENT_VALUE_CODE, 0);
                intent.putExtra("meetingId", String.valueOf(meetingID));
                sendBroadcast(intent);
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {

                CustomLog.v(TAG, "VerifyMeetingNo onFail statusCode= "
                    + statusCode);

                if (statusCode == -906) {
                    valueCode = -1;
                    eventCode = JMeetingAgent.MEETINGID_DISABLED;
                }
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    valueCode = -2;
                    eventCode = JMeetingAgent.MEETINGID_NET_DISABLED;
                }
                if (statusCode == SettingData.getInstance().tokenUnExist
                    || statusCode == SettingData.getInstance().tokenInvalid) {
                    valueCode = statusCode;
                    Intent intent1 = new Intent(JMEETING_ONEVENT_BROADCAST);
                    intent1.setPackage(rootDirectory);
                    intent1.putExtra("eventCode", JMeetingAgent.TOKEN_DISABLED);
                    intent1.putExtra("eventContent",
                        (Serializable) joinMeetingToken);
                    sendBroadcast(intent1);
                }
                Intent intent = new Intent(JMEETING_ONJOINMEETING_BROADCAST);
                intent.setPackage(rootDirectory);
                intent.putExtra(INTENT_VALUE_CODE, valueCode);
                intent.putExtra("meetingId", String.valueOf(0));
                sendBroadcast(intent);

                Intent intent3 = new Intent(JMEETING_ONEVENT_BROADCAST);
                intent3.setPackage(rootDirectory);
                intent3.putExtra("eventCode", eventCode);
                intent3.putExtra("eventContent",
                    JMeetingAgent.eventCodeDes(eventCode));
                sendBroadcast(intent3);

                if (MeetingManager.getInstance().getMeetingState()) {
                    MeetingManager.getInstance().joinMeeting(joinMeetingToken, userID,
                        userName, meetingID, groupID);
                }

            }
        };

        vm.verifymeetingNo(joinMeetingToken, meetingID);

        return 0;
    }


    private int cancelJoinMeeting() {
        CustomLog.i(TAG, "cancelJoinMeeting ");
        vm.cancel();
        return 0;
    }


    private int getNowMeetings() {
        CustomLog.i(TAG, "getNowMeetings " + token);
        getNMeetings = new GetNowMeetings() {
            @Override
            protected void onSuccess(List<GetMeetingInfo> responseContent) {
                CustomLog.i(TAG, "getNowMeetings " + responseContent.size());
                Intent intent = new Intent(JMEETING_ONGETNOWMEETINGS_BROADCAST);
                intent.setPackage(rootDirectory);
                List<MeetingItem> meetingInfoList = new ArrayList<MeetingItem>();
                for (int i = 0; i < responseContent.size(); i++) {
                    MeetingItem meetingItem = new MeetingItem();
                    meetingItem.meetingId = String.valueOf(responseContent
                        .get(i).meetingId);
                    meetingItem.creatorId = responseContent.get(i).phoneId;
                    meetingItem.creatorName = responseContent.get(i).creatorName;
                    meetingItem.createTime = responseContent.get(i).createTime;
                    meetingItem.meetingType = responseContent.get(i).meetingType;
                    meetingItem.topic = responseContent.get(i).topic;
                    meetingItem.hasMeetingPwd = responseContent.get(i).hasMeetingPwd;
                    meetingInfoList.add(meetingItem);
                }
                intent.putExtra(INTENT_VALUE_CODE, 0);
                intent.putExtra("meetingInfoList",
                    (Serializable) meetingInfoList);
                sendBroadcast(intent);
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.i(TAG, "getNowMeetings failed" + statusCode
                    + statusInfo);

                int returnCode = 0;
                if (statusCode == -65540 || statusCode == -65544
                    || statusCode == -65548 || statusCode == -65568) {
                    returnCode = -100;
                } else if (statusCode == -65664) {
                    returnCode = -101;
                } else {
                    returnCode = -102;
                }
                if (statusCode == -923) {
                    returnCode = -923;
                }
                if (statusCode == -902 || statusCode == -903) {
                    returnCode = statusCode;
                    Intent intent1 = new Intent(JMEETING_ONEVENT_BROADCAST);
                    intent1.setPackage(rootDirectory);
                    intent1.putExtra("eventCode", JMeetingAgent.TOKEN_DISABLED);
                    intent1.putExtra("eventContent",
                        (Serializable) getNowMeetingsToken);
                    sendBroadcast(intent1);
                }
                Intent intent = new Intent(JMEETING_ONGETNOWMEETINGS_BROADCAST);
                intent.setPackage(rootDirectory);
                intent.putExtra(INTENT_VALUE_CODE, returnCode);
                // intent.putExtra("meetingInfoList",null);
                intent.putExtra("statusCode", returnCode);

                intent.putExtra("statusInfo", statusInfo);

                sendBroadcast(intent);
            }

        };
        getNowMeetingsToken = token;
        getNMeetings.getNowMeetings(getNowMeetingsToken);
        return 0;
    }


    private int inComingCall(String inviterId, String inviterName,
                             int MeetingId, String headUrl) {
        CustomLog.i(TAG, "inComingCalled" + inviterId + " " + inviterName + " "
            + meetingID + " " + headUrl);
        incomingMessageManage.inComingCall(inviterId, inviterName, MeetingId,
            headUrl);
        Intent intent = new Intent(MEDICAL_JMEETING_ONINCOMINGCALL_BROADCASE);
        intent.setPackage(rootDirectory);
        intent.putExtra(INTENT_VALUE_CODE, 0);
        intent.putExtra(INTENT_VALUE_DES, "inComingCall");
        sendBroadcast(intent);
        return 0;
    }


    private int quitMeeting() {
        CustomLog.i(TAG, "quitMeeting");
        int meetingIdres = MeetingManager.getInstance().quitMeeting();
        Intent intent = new Intent(JMEETING_ONQUITMEETING_BROADCAST);
        intent.setPackage(rootDirectory);
        intent.putExtra(INTENT_VALUE_CODE, 0);
        intent.putExtra("meetingId", String.valueOf(meetingIdres));
        sendBroadcast(intent);
        return 0;
    }


    private int cancelCreatMeeting() {
        CustomLog.i(TAG, "cancelCreatMeeting");
        create.cancel();
        return 0;
    }


    private int cancelGetNowMeetings() {
        CustomLog.i(TAG, "cancelGetNowMeetings");
        getNMeetings.cancel();
        return 0;
    }


    private int updateToken(String token) {
        CustomLog.i(TAG, "updateToken");
        //		BindToken bindToken = new BindToken() {
        //
        //			@Override
        //			protected void onSuccess(ResponseEmpty responseContent) {
        //				CustomLog.v(TAG, "bindToken success");
        //			}
        //
        //			@Override
        //			protected void onFail(int statusCode, String statusInfo) {
        //				CustomLog.v(TAG, "bindToken failed");
        //
        //			}
        //		};
        //
        //		bindToken.bindToken4jmeeting(userID, token, userName, "jmeetingsdk");

        HostAgent.getInstance().release();
        HostAgent.getInstance().init(JMeetingService.this, token,
            JMeetingService.this.userID, JMeetingService.this.userName);
        return 0;
    }


    private int setCurrentUser(String userID, String userName, String token) {
        CustomLog.i(TAG, "setCurrentUser");
        // HostAgent.getInstance().shareAccountInfo(userID, userName);
        //		BindToken bindToken = new BindToken() {
        //
        //			@Override
        //			protected void onSuccess(ResponseEmpty responseContent) {
        //				CustomLog.v(TAG, "bindToken success");
        //			}
        //
        //			@Override
        //			protected void onFail(int statusCode, String statusInfo) {
        //				CustomLog.v(TAG, "bindToken failed");
        //
        //			}
        //		};
        //
        //		bindToken.bindToken4jmeeting(userID, token, userName, "jmeetingsdk");
        MeetingManager.getInstance().setAccountID(userID);
        MeetingManager.getInstance().setAccountName(userName);
        HostAgent.getInstance().release();
        HostAgent.getInstance().init(JMeetingService.this, token,
            JMeetingService.this.userID, JMeetingService.this.userName);
        return 0;
    }


    private int setIsAllowedMobileNet(boolean isAllowMobileNet) {
        MeetingManager.getInstance().setIsAllowMobileNet(isAllowMobileNet);
        return 0;
    }


    private int setSelectSystemCamera(boolean selectSystemCamera) {
        MeetingManager.getInstance().setSelectSystemCamera(selectSystemCamera);
        return 0;
    }


    @SuppressWarnings("deprecation")
    private int release() {
        CustomLog.i(TAG, "release");
        if (!isInit) {
            return 0;
        }
        // thread.stop() ;
        LogcatFileManager.getInstance().stop();
        mNotificationManager.cancel(100);
        if (isRegistBroadcast) {
            this.unregisterReceiver(JMeetingServiceReceiver);
            isRegistBroadcast = false;
        }
        setMeetingSharedPreferences("");
        MeetingManager.getInstance().release();
        incomingMessageManage.release();
        HostAgent.getInstance().release();
        isRelease = true;
        //	meetingStatus.meetingInfo=0;
        MeetingManager.getInstance().release();
        isInit = false;
        return 0;
    }


    /**
     * 显示常驻通知栏
     */
    @SuppressWarnings("deprecation")
    public void showNotify() {
        CustomLog.i(TAG, "showNotify");

        //		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
        //				this);
        //		// //PendingIntent 跳转动作
        //		Intent meetingItent = new Intent(this,  MeetingRoomActivity.class);
        //		meetingItent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //		meetingItent.putExtra("accountId", meetingStatus.accountId);
        //		meetingItent.putExtra("accountName", meetingStatus.accountName);
        //		meetingItent.putExtra("meetingId", meetingStatus.meetingId);
        //		meetingItent.putExtra("token", meetingStatus.token);
        //		meetingItent.putExtra("isAllowMobileNet", isAllowMobileNet);
        //		//meetingItent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
        //				meetingItent, PendingIntent.FLAG_UPDATE_CURRENT);
        //		int ic=MResource.getIdByName(this, MResource.DRAWABLE, "jmeetingsdk_notification");
        //		String titleString=meetingStatus.accountName;
        //		if(titleString.equals("")){
        //			titleString=meetingStatus.accountId;
        //		}
        //		CustomLog.i(TAG, "showNotifyicon资源id: +"+ic);
        //		mBuilder.setSmallIcon(ic).setTicker("开始会议")
        //				.setContentTitle(titleString).setContentText("正在视频会议"+meetingStatus.meetingId)
        //				.setContentIntent(pendingIntent);
        //		Notification mNotification = mBuilder.build();
        //		mBuilder.setContentIntent(pendingIntent);
        //		// 设置通知 消息 图标
        //		mNotification.icon = ic;
        //
        //		// 在通知栏上点击此通知后自动清除此通知
        //		mNotification.flags = Notification. FLAG_ONGOING_EVENT;// FLAG_ONGOING_EVENT
        //																// 在顶部常驻，可以调用下面的清除方法去除
        //																// FLAG_AUTO_CANCEL
        //																// 点击和清理可以去调
        //		// 设置显示通知时的默认的发声、震动、Light效果
        //	//	mNotification.defaults = Notification.DEFAULT_VIBRATE;
        //		// 设置发出消息的内容
        //		mNotification.tickerText = "开始会议";
        //		// 设置发出通知的时间
        //		mNotification.when = System.currentTimeMillis();
        //
        //		mNotificationManager.notify(100, mNotification);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
            this);
        int ic = MResource.getIdByName(this, MResource.DRAWABLE, "jmeetingsdk_notification");
        //		int ic=R.id.jmeetingsdk_notification;
        String titleString = MeetingManager.getInstance().getAccountName();
        if (titleString.equals("")) {
            titleString = MeetingManager.getInstance().getAccountID();
        }
        CustomLog.i(TAG, "showNotifyicon资源id: +" + ic);
        mBuilder.setSmallIcon(ic)
            .setTicker(getString(R.string.startmeeting))
            .setContentTitle(titleString)
            .setContentText(
                getString(R.string.videoMeeting) + MeetingManager.getInstance().getMeetingId());

        Notification mNotification = mBuilder.build();

        // 设置通知 消息 图标
        mNotification.icon = ic;

        // 在通知栏上点击此通知后自动清除此通知
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;// FLAG_ONGOING_EVENT
        // 在顶部常驻，可以调用下面的清除方法去除
        // FLAG_AUTO_CANCEL
        // 点击和清理可以去调
        // 设置显示通知时的默认的发声、震动、Light效果
        //	mNotification.defaults = Notification.DEFAULT_VIBRATE;
        // 设置发出消息的内容
        mNotification.tickerText = getString(R.string.startmeeting);
        // 设置发出通知的时间
        mNotification.when = System.currentTimeMillis();

        Intent intent = new Intent(JMEETING_NOTIFICATION_BROADCAST);//这个意图的action为FILE_NOTIFICATION
        PendingIntent mPI = PendingIntent.getBroadcast(this, 0, intent, 0);
        //		mNotification.setLatestEventInfo(this, titleString, "正在视频会议"+ MeetingManager.getInstance().getMeetingId(), mPI);
        //		//第一个参数：上下文；第二个参数：标题；第三个参数内容；第四个参数：点击后将发送的PendingIntent对象。】
        //
        //		mNotificationManager.notify(100, mNotification);

    }


    private IJMeetingService.Stub mBinder = new IJMeetingService.Stub() {

        /**
         * 设置根目录
         */
        @Override
        public int setRootDirectory(String RootDirectory)
            throws RemoteException {
            CustomLog.i(TAG, "setRootDirectory" + RootDirectory);
            JMeetingService.this.rootDirectory = RootDirectory;
            mHandler.sendEmptyMessage(MSG_SETROOTDIRECTORY);

            return 0;
        }


        /**
         * 设置前后摄像头参数
         */
        @Override
        public int setVideoParameter(int cameraId, int capWidth, int capHeight,
                                     int capFps, int encBitrate) throws RemoteException {
            CustomLog.i(TAG, "setVideoParameter");
            JMeetingService.this.cameraId = cameraId;
            JMeetingService.this.capWidth = capWidth;
            JMeetingService.this.capHeight = capHeight;
            JMeetingService.this.capFps = capFps;
            JMeetingService.this.encBitrate = encBitrate;
            mHandler.sendEmptyMessage(MSG_SETVIDEOPARAMETER);
            return 0;
        }


        /**
         * 设置appType
         */
        @Override
        public int setAppType(String appType) throws RemoteException {
            CustomLog.i(TAG, "setAppType");
            JMeetingService.this.appType = appType;
            mHandler.sendEmptyMessage(MSG_SETAPPTYPE);
            return 0;
        }


        @Override
        public int setShowMeetingScreenSharing(boolean isShare) throws RemoteException {
            CustomLog.i(TAG, "setShowMeetingScreenSharing");
            JMeetingService.this.isShare = isShare;
            mHandler.sendEmptyMessage(MSG_SHARE);
            return 0;
        }
        @Override
        public int setShowMeetingFloat(boolean isShowMeetingFloat) throws RemoteException {
            CustomLog.i(TAG, "setShowMeetingFloat");
            JMeetingService.this.isShowMeetingFloat = isShowMeetingFloat;
            mHandler.sendEmptyMessage(MSG_SETSHOWMEETINGFLOAT);
            return 0;
        }


        @Override
        public int setContactProvider(String authorities) throws RemoteException {
            CustomLog.i(TAG, "setContactProvider");
            JMeetingService.this.authorities = authorities;
            mHandler.sendEmptyMessage(MSG_SETCONTACTPROVIDER);
            return 0;
        }

        @Override
        public int setWXAppId(String appId) throws RemoteException {
            CustomLog.i(TAG, "setContactProvider");
            JMeetingService.this.appId = appId;
            mHandler.sendEmptyMessage(MSG_SETWXAPPID);
            return 0;
        }

        @Override
        public int setMeetingAdapter(boolean isMeetingAdapter) throws RemoteException {
            CustomLog.i(TAG, "setMeetingAdapter");
            JMeetingService.this.isMeetingAdapter = isMeetingAdapter;
            mHandler.sendEmptyMessage(MSG_SETMEETINGADAPTER);
            return 0;
        }


        /**
         * 通讯录查询url
         */
        @Override
        public int setSearchContactUrl(String ContactUrl) throws RemoteException {
            CustomLog.i(TAG, "setSearchContactUrl");
            JMeetingService.this.SearchContactUrl = ContactUrl;
            mHandler.sendEmptyMessage(MSG_GETSEARCHCONTACTURL);
            return 0;
        }


        /**
         * 通讯录插入url
         */
        @Override
        public int setInsertContactUrl(String ContactUrl) throws RemoteException {
            CustomLog.i(TAG, "setInsertContactUrl");
            JMeetingService.this.InsertContactUrl = ContactUrl;
            mHandler.sendEmptyMessage(MSG_SETINSERTCONTACTURL);
            return 0;
        }


        /**
         * 1.通过NPS服务器获取极会议相关参数 2.初始化SettingData、DataCenter config 值 3.拷贝配置文件
         * 3.启动各个模块（hostAgent）
         */

        @Override
        public int init(String token, String userID, String userName,
                        String masterNps, String slaveNps, String rootDirectory) throws RemoteException {
            JMeetingService.this.userID = userID;
            JMeetingService.this.userName = userName;
            JMeetingService.this.token = token;
            JMeetingService.this.isAllowMobileNet = isAllowMobileNet;
            JMeetingService.this.masterNps = masterNps;
            JMeetingService.this.slaveNps = slaveNps;
            JMeetingService.this.rootDirectory = rootDirectory;
            mHandler.sendEmptyMessage(MSG_INIT);
            // onBootSuccess();
            return 0;
        }


        @Override
        public void release() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_RELEASE);
        }


        @Override
        public int creatMeeting(List<String> invitersId, String topic, int meetingType, String beginDateTime)
            throws RemoteException {
            CustomLog.v(TAG, "CreateMeeting "
                + ",inviterUserList.toString()" + invitersId.toString());
            JMeetingService.this.invitersId = invitersId;
            JMeetingService.this.topic = topic;
            JMeetingService.this.meetingType = meetingType;
            JMeetingService.this.beginDateTime = beginDateTime;
            mHandler.sendEmptyMessage(MSG_CREATMEETING);
            return 0;
        }


        @Override
        public int getNowMeetings() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_GETNOWMEETINGS);
            return 0;
        }


        @Override
        public int joinMeeting(int meetingID, String groupId) throws RemoteException {
            JMeetingService.this.meetingID = meetingID;
            JMeetingService.this.groupID = groupId;
            CustomLog.d(TAG, "JMeetingService joinMeeting");
            mHandler.sendEmptyMessage(MSG_JOINMEETING);
            return 0;
        }
        @Override
        public int resumeMeeting(int meetingID, String groupId) throws RemoteException {
            JMeetingService.this.meetingID = meetingID;
            JMeetingService.this.groupID = groupId;
            CustomLog.d(TAG,"JMeetingService resumeMeeting");
            joinMeetingToken = token;
            MeetingManager.getInstance().joinMeeting(joinMeetingToken, userID, userName,
                    meetingID, groupID);
            Intent intent = new Intent(JMEETING_ONJOINMEETING_BROADCAST);
            intent.setPackage(rootDirectory);
            intent.putExtra(INTENT_VALUE_CODE, 0);
            intent.putExtra("meetingId", String.valueOf(meetingID));
            sendBroadcast(intent);
            return 0;
        }


        @Override
        public int incomingCall(String inviterId, String inviterName,
                                int MeetingId, String headUrl) throws RemoteException {
            JMeetingService.this.inviterID = inviterId;
            JMeetingService.this.inviterName = inviterName;
            JMeetingService.this.meetingID = MeetingId;
            JMeetingService.this.headUrl = headUrl;
            mHandler.sendEmptyMessage(MSG_INCOMINGCALL);
            return 0;
        }


        @Override
        public int quitMeeting() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_QUITMEETING);
            return 0;
        }


        @Override
        public int updateToken(String token) throws RemoteException {
            JMeetingService.this.token = token;
            mHandler.sendEmptyMessage(MSG_UPDATETOKEN);
            return 0;
        }


        @Override
        public int setCurrentUser(String userID, String userName, String token)
            throws RemoteException {
            JMeetingService.this.userID = userID;
            JMeetingService.this.userName = userName;
            JMeetingService.this.token = token;
            mHandler.sendEmptyMessage(MSG_SETCURRENTUSER);
            return 0;
        }


        @Override
        public int cancelCreatMeeting() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_CANCELCREATMEETING);
            return 0;
        }


        @Override
        public int cancelGetNowMeetings() throws RemoteException {
            mHandler.sendEmptyMessage(MSG_GETNOWMEETINGS);
            return 0;
        }


        public int setisAllowMobileNet(boolean isAllowMobileNet)
            throws RemoteException {
            JMeetingService.this.isAllowMobileNet = isAllowMobileNet;
            mHandler.sendEmptyMessage(MSG_SETISALLOWEDMOBILENET);
            return 0;
        }


        public int setSelectSystemCamera(boolean selectSystemCamera)
            throws RemoteException {
            JMeetingService.this.selectSystemCamera = selectSystemCamera;
            mHandler.sendEmptyMessage(MSG_SETSELECTSYSTEMCAMERA);
            return 0;
        }


        public String getActiveMeetingId()
            throws RemoteException {
            //mHandler.sendEmptyMessage(MSG_SETISALLOWEDMOBILENET);
            SharedPreferences share = getSharedPreferences("meetingId",
                Context.MODE_WORLD_READABLE);
            String str = share.getString("meetingId", "");
            CustomLog.d(TAG, "读取meetingid" + str);
            return str;

        }


        public int cancelJoinMeeting()
            throws RemoteException {
            mHandler.sendEmptyMessage(MSG_CANCELJOINMEETING);
            return 0;
        }

    };


    private synchronized void setMeetingSharedPreferences(String meetingId) {
        CustomLog.d(TAG, "写入meetingid" + meetingId);
        SharedPreferences sharedPreferences = getSharedPreferences("meetingId",
            Context.MODE_PRIVATE); //私有数据
        Editor editor = sharedPreferences.edit();//获取编辑器
        editor.putString("meetingId", meetingId);
        //  editor.putInt("age", 4);
        editor.commit();//提交修改
    }


    public String getActiveMeetingId() {
        //mHandler.sendEmptyMessage(MSG_SETISALLOWEDMOBILENET);
        SharedPreferences share = getSharedPreferences("meetingId", Context.MODE_WORLD_READABLE);
        String str = share.getString("meetingId", "");
        CustomLog.d(TAG, "读取meetingid" + str);
        return str;

    }


    private void accquireNpsConfig(String masterNps, String slaveNps) {
        String serialNum = "GDDXX1144500045";
        CustomLog.i(TAG, "BootManager::aquireMeetingParameter() 获取NPS参数");

        SettingData.getInstance().NPS_URL = masterNps;
        ConstConfig.npsWebDomain = masterNps;
        ConstConfig.slaveNpsWebDomain = slaveNps;
        CustomLog.i(TAG, "BootManager::aquireMeetingParameter() NPS_URL: "
            + ConstConfig.npsWebDomain + " | serialNum: " + serialNum
            + " | type: " + SettingData.getInstance().DeviceType);
        AcquireParameter ap = new AcquireParameter() {

            @Override
            public void onSuccess(JSONObject bodyObject) {
                if (bodyObject == null) {
                    CustomLog
                        .e(TAG,
                            "SettingData::aquireMeetingParameter() 获取NPS参数失败，返回为空");
                    KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO + "_fail_"
                        + "Mobile_reAquireNps_return==null");
                    onBootFailed(BOOT_ERROR_NPS, "Nps获取失败1");
                    return;
                }

                try {
                    JSONObject paramList = (JSONObject) bodyObject
                        .get("paramList");
                    if (null != paramList) {
                        // 获取公共参数
                        JSONObject commonObj = new JSONObject(paramList.get(
                            NpsParamConfig.COMMON).toString());
                        if (null != commonObj) {
                            SettingData.getInstance().MASTER_MS_URL = commonObj
                                .getString(NpsParamConfig.COMMON_MASTER_MS_URL);
                            String slave_ms_url = commonObj
                                .optString(NpsParamConfig.COMMON_SLAVE_MS_URL);
                            if (!slave_ms_url.equals("")) {
                                SettingData.getInstance().SLAVE_MS_URL = commonObj
                                    .getString(NpsParamConfig.COMMON_SLAVE_MS_URL);
                            }
                            SettingData.getInstance().RC_URL = commonObj
                                .getString(NpsParamConfig.COMMON_RC_URL);
                            SettingData.getInstance().ENTERPRISE_CENTER_URL = commonObj
                                .getString(NpsParamConfig.COMMON_EUC_URL);
                            SettingData.getInstance().PERSONAL_CENTER_URL = commonObj
                                .getString(NpsParamConfig.COMMON_PUC_URL);
                            SettingData.getInstance().PERSION_CONTACT_URL = commonObj
                                .getString(NpsParamConfig.COMMON_Persion_Contact_URL);
                            SettingData
                                .getInstance()
                                .setUploadImageUrl(
                                    commonObj
                                        .getString(NpsParamConfig.COMMON_Persion_Head_Upload_URL));
                        }

                        // 获取APP升级参数
                        JSONObject appUpdateObj = new JSONObject(paramList.get(
                            NpsParamConfig.APP_UPDATE).toString());
                        if (null != appUpdateObj) {
                            SettingData.getInstance().AppUpdateConfig.Master_ServerUrl
                                = appUpdateObj
                                .getString(NpsParamConfig.APP_UPDATE_MASTER_ServerUrl);
                            String slave_serverUrl = appUpdateObj
                                .optString(NpsParamConfig.APP_UPDATE_SLAVE_ServerUrl);

                            if (!slave_serverUrl.equals("")) {
                                SettingData.getInstance().AppUpdateConfig.Slave_ServerUrl
                                    = slave_serverUrl;
                            }

                            SettingData.getInstance().AppUpdateConfig.ProjectName = appUpdateObj
                                .getString(NpsParamConfig.APP_UPDATE_ProjectName);
                            //							SettingData.getInstance().AppUpdateConfig.DeviceType = "MOBILE";
                            SettingData.getInstance().AppUpdateConfig.DeviceType
                                = SettingData.getInstance().DeviceType;
                            SettingData.getInstance().AppUpdateConfig.CheckInterval = appUpdateObj
                                .getString(NpsParamConfig.APP_UPDATE_CheckInterval);
                        }

                        // 获取流媒体参数
                        JSONObject mediaPlayObj = new JSONObject(paramList.get(
                            NpsParamConfig.MediaPlay).toString());
                        if (null != mediaPlayObj) {
                            SettingData.getInstance().MediaPlayConfig.Jfec_in = mediaPlayObj
                                .getInt(NpsParamConfig.MediaPlay_Jfec_in);
                            SettingData.getInstance().MediaPlayConfig.Jfec_out = mediaPlayObj
                                .getInt(NpsParamConfig.MediaPlay_Jfec_out);
                        }

                        // 获取帮助参数
                        JSONObject helpObj = new JSONObject(paramList.get(
                            NpsParamConfig.HELP).toString());
                        if (null != helpObj) {
                            SettingData.getInstance().HELP_URL = helpObj
                                .getString(NpsParamConfig.HELP_URL);

                            SettingData.getInstance().DOWNLAOD_LINK = helpObj
                                .getString(NpsParamConfig.HELP_DOWNLOAD_LINK);
                        }

                        // 获取日志上传参数
                        JSONObject logUploadObj = new JSONObject(paramList.get(
                            NpsParamConfig.LogUpload).toString());
                        if (null != logUploadObj) {
                            SettingData.getInstance().LogUploadConfig.ServerIP = logUploadObj
                                .getString(NpsParamConfig.LogUpload_serverIp);
                            SettingData.getInstance().LogUploadConfig.ServerPort = logUploadObj
                                .getInt(NpsParamConfig.LogUpload_serverPort);
                        }

                        SettingData.getInstance().LogConfig();
                        initHttpRequestConfig();
                        KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO
                            + "_ok_" + "Mobile_reAquireNps");
                    }
                } catch (JSONException e) {
                    CustomLog.e(TAG, e.getMessage());
                    KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO + "_fail_"
                        + "Mobile_reAquireNps_jsonError");
                    onBootFailed(BOOT_ERROR_NPS, "Nps获取失败2");
                } catch (ClassCastException e) {
                    CustomLog.e(TAG, e.getMessage());
                    KeyEventWrite.write(KeyEventConfig.GET_NPS_INFO + "_fail_"
                        + "Mobile_reAquireNps_jsonError");
                    //					onBootFailed(BOOT_ERROR_NPS, "Nps获取失败3");
                    onBootFailed(BOOT_ERROR_NPS,
                        "Nps获取失败3" + "&& e:" + e.getMessage() + "&& bodyObject:" +
                            bodyObject.toString());

                }
            }


            @Override
            public void onFail(int statusCode, String statusInfo) {
                CustomLog.e(TAG,
                    "SettingData::aquireMeetingParameter() 获取NPS参数失败 statusCode: "
                        + statusCode + " | statusInfo: " + statusInfo);
                KeyEventWrite
                    .write(KeyEventConfig.GET_NPS_INFO + "_fail_"
                        + "Mobile_reAquireNps_" + statusCode + "_"
                        + statusInfo);
                onBootFailed(BOOT_ERROR_NPS, "Nps获取失败4");
            }

        };

        ArrayList<String> requestList = new ArrayList<String>();

        requestList.add(NpsParamConfig.COMMON);
        requestList.add(NpsParamConfig.APP_UPDATE);
        requestList.add(NpsParamConfig.MediaPlay);
        requestList.add(NpsParamConfig.HELP);
        requestList.add(NpsParamConfig.LogUpload);

        ap.acquire(requestList, SettingData.getInstance().DeviceType, serialNum);
    }


    // 初始化 DataCenter 数据
    private void initHttpRequestConfig() {
        CustomLog.i(TAG,
            "SettingData::initHttpRequestConfig() 初始化 DataCenter 中配置");

        ConstConfig.masterBmsWebDomain = SettingData.getInstance().MASTER_MS_URL;
        ConstConfig.slaveBmsWebDomain = SettingData.getInstance().SLAVE_MS_URL;
        ConstConfig.enterPriseUserCenterWebDomain = SettingData.getInstance().ENTERPRISE_CENTER_URL;
        ConstConfig.personalUserCenterWebDomain = SettingData.getInstance().PERSION_CONTACT_URL;
        ConstConfig.personalContactWebDomain = SettingData.getInstance().PERSION_CONTACT_URL;
        ConstConfig.masterAppUpdateServerWebDomain
            = SettingData.getInstance().AppUpdateConfig.Master_ServerUrl;
        ConstConfig.slaveAppUpdateServerWebDomain
            = SettingData.getInstance().AppUpdateConfig.Slave_ServerUrl;
        copyConfigfile();
        //		BindToken bindToken = new BindToken() {
        //
        //			@Override
        //			protected void onSuccess(ResponseEmpty responseContent) {
        //				CustomLog.v(TAG, "bindToken success");
        //		//		copyConfigfile();
        //			}
        //
        //			@Override
        //			protected void onFail(int statusCode, String statusInfo) {
        //				CustomLog.v(TAG, "bindToken failed" + statusCode);
        //				onBootFailed(statusCode, statusInfo);
        //
        //			}
        //		};
        //
        //		bindToken.bindToken4jmeeting(userID, token, userName, "jmeetingsdk");
    }


    // 拷贝配置文件
    private void copyConfigfile() {

        CustomLog.i(TAG, "BootManager::copyConfigfile() 判断版本号，是否存在升级情况");
        PackageManager pm = getPackageManager();
        PackageInfo pi = null;
        boolean diff = false;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
            int currVersion = pi.versionCode;

            SharedPreferences sharedPreferences = getSharedPreferences("share",
                Context.MODE_PRIVATE);

            int oldVersion = sharedPreferences.getInt("versionCode", 0);

            if (oldVersion != currVersion) {
                diff = true;
                Editor editor = sharedPreferences.edit();
                editor.putInt("versionCode", currVersion);
                editor.commit();
            }
            CustomLog.i(TAG,
                "BootManager::copyConfigfile() 判断版本号，是否存在升级情况 oldVersion: "
                    + oldVersion + " | currVersion: " + currVersion
                    + " diff: " + diff);
        } catch (NameNotFoundException e) {
            diff = false;
            e.printStackTrace();
        }

        isPkgChanged = diff;

        CustomLog.i(TAG, "BootManager::copyConfigfile() 开始拷贝配置文件");
        File rootPath = new File(SettingData.getInstance().rootPath);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        File cfgFile = new File(SettingData.getInstance().CfgPath);
        if (!cfgFile.exists()) {
            cfgFile.mkdirs();
        }

        copyCfgFileToPath("Log.xml", SettingData.getInstance().CfgPath
            + "/Log.xml");
        copyCfgFileToPath("media_server_agent.xml",
            SettingData.getInstance().CfgPath + "/media_server_agent.xml");
        copyCfgFileToPath("n8config.txt", SettingData.getInstance().CfgPath
            + "/n8config.txt");
        copyCfgFileToPath("LogFileUpdateConfig.xml",
            SettingData.getInstance().CfgPath + "/LogFileUpdateConfig.xml");
        copyCfgFileToPath("ShortLinkConfig.xml",
            SettingData.getInstance().CfgPath + "/ShortLinkConfig.xml");
        MeetingHostAgentJNI.LoadBreakpad2(
            Environment.getExternalStorageDirectory().getPath() + "/" + rootDirectory +
                "/jmeetingsdk/meetingdump");
        CustomLog.i(this.getClass().getName(),
            "dumpDirectory: " + Environment.getExternalStorageDirectory().getPath() + "/" +
                rootDirectory + "/jmeetingsdk/meetingdump");

        HostAgent.getInstance().init(this, token, userID, userName);
        onBootSuccess();
    }


    private void copyCfgFileToPath(String fileName, String filePath) {
        CustomLog.i(this.getClass().getName(),
            " copyCfgFileToPath orgFileName: " + fileName
                + " desFilePath : " + filePath);

        try {
            InputStream logxmlInputStream = getResources().getAssets().open(
                fileName);

            FileOutputStream fileOutputStream = null;

            File file = new File(filePath);
            if (file.exists() && !isPkgChanged) {
                CustomLog.i(TAG, "配置文件已存在，且包没有更新，不copy!");
                return;
            }

            if (file.exists() && isPkgChanged) {
                CustomLog.i(TAG, "配置文件已存在，但包有更新，删除之前的配置文件，重新copy!");
                file.delete();
            }

            fileOutputStream = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int ch = -1;
            while ((ch = logxmlInputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, ch);
            }
            file.setReadable(true, false);

            fileOutputStream.flush();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }

        } catch (IOException e) {
            CustomLog.e(TAG, "copy error! " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static synchronized JMeetingService getInstance() {
        if (mInstance == null) {
            mInstance = new JMeetingService();
        }
        return mInstance;
    }
    public String getRootDirectory() {
        return rootDirectory;
    }
}
