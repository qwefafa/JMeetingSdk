package cn.redcdn.jmeetingsdk;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.jmeetingsdk.config.SettingData;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogcatFileManager;


//import cn.redcdn.datacenter.meetingmanage.data.MeetingInfo;

public abstract class JMeetingAgent {
    private final String TAG = "cn.redcdn.jmeetingsdk.jmeetingagent";
    private final String VERSION = "v2.2.8"; //记录当前版本号，每次版本发布需要进行更新

    /**
     * 退出会议：可视端主动调用退出接口
     */
    public static final int QUIT_MEETING_INTERFACE = 1001;
    /**
     * 退出会议：会议过程中token失效
     */
    public static final int QUIT_TOKEN_DISABLED = 1002;
    /**
     * 退出会议：会议过程中收到用户来电
     */
    public static final int QUIT_PHONE_CALL = 1003;
    /**
     * 退出会议：会议室用户点击返回按钮
     */
    public static final int QUIT_MEETING_BACK = 1004;
    /**
     * 退出会议：出现网络异常
     */
    public static final int QUIT_MEETING_SERVER_DESCONNECTED = 1005;
    /**
     * 退出会议：出现网络异常
     */
    public static final int QUIT_MEETING_LOCKED = 1006;
    /**
     * 退出会议：基础库异常
     */
    public static final int QUIT_MEETING_LIBS_ERROR = 1007;
    /**
     * 退出会议：会议已结束
     */
    public static final int QUIT_MEETING_AS_MEETING_END = 1008;
    /**
     * 退出会议：不允许使用移动网络进行会议
     */
    public static final int QUIT_MEETING_NOTALLOW_USE_MOBILE_NET = 1009;
    /**
     * 退出会议：点击短信邀请退出
     */
    public static final int QUIT_MEETING_AS_MESSAGE_INVITE = 1010;
    /**
     * 退出会议：摄像头异常
     */
    public static final int QUIT_MEETING_CAMERA_FAILED = 1011;
    /**
     * 退出会议：会议过程中出现其它异常
     */
    public static final int QUIT_MEETING_OTHER_PROBLEM = 1099;
    /**
     * 加入会议
     */
    public static final int JOIN_MEETING = 1100;
    /**
     * 加入会议号无效
     */
    public static final int MEETINGID_DISABLED = 1101;
    /**
     * 网络超时，会议号查询失败
     */
    public static final int MEETINGID_NET_DISABLED = 1102;
    /**
     * 开始振铃
     */
    public static final int PHONE_RING = 1200;
    /**
     * 点击忽略按钮结束振铃
     */
    public static final int END_PHONE_RING_AS_IGNORE = 1201;
    /**
     * 未执行任何操作振铃结束
     */
    public static final int END_PHONE_RING_AS_TIMEOUT = 1202;
    /**
     * 点击加入会议按钮结束振铃
     */
    public static final int END_PHONE_RING_AS_JOIN_MEETING = 1203;

    /**
     * 会议室内发起邀请
     */
    public static final int MEETING_INVITE = 1300;

    /**
     * 操作过程中出现token 失效
     */
    public static final int TOKEN_DISABLED = 1400;

    /**
     * 极会议进程崩溃
     */
    public static final int MEETING_CRASH = 1500;
    /**
     * 极会议进程从崩溃中恢复
     */
    public static final int RECOVERY_MEETING_CRASH = 1501;

    /**
     * 会控操作事件
     */
    public static final int MEETING_MENU = 1601;
    public static final int MEETING_SPEAK = 1602;
    public static final int MEETING_STOP_SPEAK = 1603;
    public static final int MEETING_PARTICIPATERS = 1604;
    public static final int MEETING_INVITE_CLICK = 1605;
    public static final int MEETING_GIVE_MIC = 1606;
    public static final int MEETING_ADD_CONTACTS = 1607;
    public static final int MEETING_INVITE_NUBE = 1608;
    public static final int MEETING_INVITE_INVITELIST = 1609;
    public static final int MEETING_LOCK = 1610;
    public static final int MEETING_UNLOCK = 1611;
    public static final int MEETING_SWITCHWINDOW = 1612;
    public static final int MEETING_EXIT = 1613;
    public static final int MEETING_CHANGECAMERA = 1614;
    public static final int MEETING_JOINMEETING = 1615;

    // private static final String NOTIFICATION_SERVICE = null;


    IJMeetingService iJMeetingService;
    private boolean initState = false;

    private boolean isCreatMeeting = false;
    private boolean isGetNowMeetings = false;
    private boolean isQuitMeeting = false;
    private boolean isInit = false;
    private boolean isRelease = false;
    private boolean isJoinMeeting = false;

    private boolean isCrashed = false;
    private boolean isJMeetingService = false;
    private boolean isRegistBroadcast = false;
    private boolean isAllowMobileNet = false;

    private String meetingID = null;
    private Context context;
    private String userID;
    private String userName;
    private String token;
    private String masterNps;
    private String slaveNps;

    MeetingAgentContext agentContextCreat;
    MeetingAgentContext agentContextGetMeetings;

    public static String  rootDirectory = "cn.redcdn.hnyd";

    public enum MeetingStatus {
        NONE, MEETING
    }

    MeetingStatus meetingStatus;

    public NotificationManager mNotificationManager;

    private static String bindString = "cn.redcdn.jmeetingsdk.meetingservice.bindstring";


    AliveService aliveService;

    int i;

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            clientConnect.send("heart ok");
        }
    };

    class ClientConnect {
        private static final String TAG = "ClientConnect";
        private static final String name = "cn.redcdn.jmeetingsdk.localsocket";
        private LocalSocket Client = null;
        private PrintWriter os = null;
        private BufferedReader is = null;
        private int timeout = 30000;

        private int PORT = 4561;
        private int serverPort;
        private DatagramSocket dataSocket;
        private DatagramPacket dataPacket;
        private byte sendDataByte[];
        private String sendStr;

        public void connect(int serverPort) {
            // try {
            // Client = new LocalSocket();
            // Client.connect(new LocalSocketAddress(name));
            // Client.setSoTimeout(timeout);
            // CustomLog.v(TAG, "建立心跳连接");
            // } catch (IOException e) {
            // CustomLog.v(TAG, "建立心跳连接失败");
            // e.printStackTrace();
            // }
            try {
                // 指定端口号，避免与其他应用程序发生冲突
                CustomLog.v(TAG, "建立心跳连接,server端口号：" + serverPort);
                dataSocket = new DatagramSocket();
                sendDataByte = new byte[1024];
                sendStr = "heart ok";
                sendDataByte = sendStr.getBytes();
                dataPacket = new DatagramPacket(sendDataByte,
                        sendDataByte.length,
                        InetAddress.getByName("localhost"), serverPort);

            } catch (SocketException se) {
                CustomLog.v(TAG, "建立心跳连接异常SocketException：" + se.toString());
                se.printStackTrace();
            } catch (IOException ie) {
                CustomLog.v(TAG, "建立心跳连接异常IOException：" + ie.toString());
                ie.printStackTrace();
            }
        }

        public void send(String data) {

            try {
                // os = new PrintWriter(Client.getOutputStream());
                // os.println(data);
                // os.flush();

                dataSocket.send(dataPacket);
                CustomLog.v(TAG, "agent发送心跳包 heart ok");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {

            dataSocket.close();
            // CustomLog.v(TAG, "agent发送心跳包 heart ok");

        }
    }

    ClientConnect clientConnect;

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CustomLog.v(TAG, "IJMeetingService connected");
            // isCrashed=true;
            isJMeetingService = true;
            iJMeetingService = IJMeetingService.Stub.asInterface(service);

            if (isCrashed) {
                CustomLog.v(TAG, "service处于崩溃状态,释放原service状态");
                try {
                    iJMeetingService.release();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    CustomLog.v(TAG, e1.toString());
                    e1.printStackTrace();
                }
            }

            try {
                iJMeetingService.init(token, userID, userName, masterNps,
                        slaveNps,rootDirectory);
            } catch (Exception e) {

                CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // isCrashed=true;
            CustomLog.v(TAG, "IJMeetingService Disconnected ");
            isJMeetingService = false;
            initState = false;
            isInit = false;

            //返回和清空agent的状态，如果已经处于崩溃状态，则不做处理
            if (isRelease != true) {
                if (!isCrashed) {
                    CustomLog.v(TAG, "service进程崩溃");
                    CustomLog.v(TAG, "onEvent " + MEETING_CRASH);
                    onEvent(MEETING_CRASH, eventCodeDes(MEETING_CRASH));
                    if (isCreatMeeting) {
                        isCreatMeeting = false;
                        CustomLog.v(TAG, "onCreatMeeting " + -3);
                        onCreatMeeting(-3, null, agentContextCreat);
                    }
                    if (isGetNowMeetings) {
                        isGetNowMeetings = false;
                        CustomLog.v(TAG, "onNowMeetings " + -3);
                        onNowMeetings(null, -3, agentContextGetMeetings);
                    }
                    if (isJoinMeeting) {
                        isJoinMeeting = false;
                        CustomLog.v(TAG, "onJoinMeeting " + -3);
                        onJoinMeeting(null, -3);
                    }
                    if (isQuitMeeting) {
                        isQuitMeeting = false;
                        CustomLog.v(TAG, "onQuitMeeting " + -3);
                        onQuitMeeting(null, -3);
                    }
                    isCrashed = true;
                }
                mNotificationManager.cancel(100);
                Intent intent = new Intent(
                        "cn.redcdn.jmeetingsdk.IJMeetingService");
                intent.setPackage(context.getPackageName());
                intent.putExtra("bindString", bindString);
                CustomLog.v(TAG, "重新绑定service进程");
                context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
            } else {
                isRelease = false;
            }

        }
    };

    private BroadcastReceiver jMeetingOnInitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int valueCode = intent.getExtras().getInt("valueCode");
            String valueDes = intent.getExtras().getString("valueDes");
            int serverPort = intent.getExtras().getInt("serverPort");
            CustomLog.v(TAG, "接收到agent初始化广播 " + valueCode + valueDes);
            // isInit=false;
            if (valueCode == 0) {
                CustomLog.v(TAG, "agent初始化 成功");
                initState = true;
                //	clientConnect.connect(serverPort);
                //	timer.schedule(task, 1000, 1000); // 执行task,经过1s再次执行
                //	aliveService.init(serverPort);
//				ConstConfig.npsWebDomain=intent.getExtras().getString("npsWebDomain");
//				ConstConfig.slaveNpsWebDomain=intent.getExtras().getString("slaveNpsWebDomain");
                ConstConfig.masterBmsWebDomain = intent.getExtras().getString("masterBmsWebDomain");
                ConstConfig.slaveBmsWebDomain = intent.getExtras().getString("slaveBmsWebDomain");
//				ConstConfig.enterPriseUserCenterWebDomain=intent.getExtras().getString("enterPriseUserCenterWebDomain");
//				ConstConfig.personalUserCenterWebDomain=intent.getExtras().getString("personalUserCenterWebDomain");
//				ConstConfig.personalContactWebDomain=intent.getExtras().getString("personalContactWebDomain");
//				ConstConfig.masterAppUpdateServerWebDomain=intent.getExtras().getString("masterAppUpdateServerWebDomain");
//				ConstConfig.slaveAppUpdateServerWebDomain=intent.getExtras().getString("slaveAppUpdateServerWebDomain");
                if (isInit != true) {

                    onEvent(RECOVERY_MEETING_CRASH,
                            eventCodeDes(RECOVERY_MEETING_CRASH));
                    CustomLog.v(TAG, "service从崩溃中恢复");
                    CustomLog.v(TAG, "onEvent " + RECOVERY_MEETING_CRASH);
                    isCrashed = false;
                    setisAllowMobileNet(isAllowMobileNet);
                } else {
                    onInit(valueDes, valueCode);
                }

            } else if (valueCode < 0) {
                initState = false;
                CustomLog.v(TAG, "agent初始化 失败" + valueCode);
                onInit(valueDes, valueCode);
                // context.unbindService(conn);
            }

            isInit = false;

        }

    };

    private BroadcastReceiver jMeetingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            CustomLog.v(TAG, "接收到JMeeting广播: " + action);
            if (action
                    .equals(JMeetingService.JMEETING_ONCREATMEETING_BROADCAST)) {
                handlerOnCreatMeetingBroadcast(intent);
            } else if (action
                    .equals(JMeetingService.JMEETING_ONJOINMEETING_BROADCAST)) {
                handlerOnJoinMeetingBroadcast(intent);

            } else if (action
                    .equals(JMeetingService.JMEETING_ONGETNOWMEETINGS_BROADCAST)) {
                handlerOnGetNowMeetingsBroadcast(intent);

            } else if (action
                    .equals(JMeetingService.MEDICAL_JMEETING_ONINCOMINGCALL_BROADCASE)) {
                handlerOnIncomingCallBroadcast(intent);

            } else if (action
                    .equals(JMeetingService.JMEETING_ONQUITMEETING_BROADCAST)) {
                handlerOnQuitMeetingBroadcast(intent);

            } else if (action
                    .equals(JMeetingService.JMEETING_ONEVENT_BROADCAST)) {
                handlerOnEventBroadcast(intent);

            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra("networkInfo");
                int networkType = networkInfo.getType();
                State netState = networkInfo.getState();
                CustomLog.i(TAG, "network type:" + networkInfo.getTypeName()
                        + ",state:" + netState);

                if (State.CONNECTED == netState) {
                    if (isJMeetingService && isCrashed && !checkInitState()) {
                        CustomLog.v(TAG, "检测到崩溃后重新初始化未成功,重新初始化");
                        try {
                            iJMeetingService.init(token, userID, userName,
                                    masterNps, slaveNps, rootDirectory);
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                } else {
                    CustomLog.w(TAG, "network not connectted");
                }
            } else {
            }

        }

    };

    private void handlerOnCreatMeetingBroadcast(Intent intent) {
        if (isCreatMeeting == false) {
            CustomLog.v(TAG, "创建会议状态为false，onCreatMeeting不做回调 ");
            return;
        }
        int valueCode = intent.getExtras().getInt("valueCode");
        if (valueCode == 0) {
            MeetingInfo meetingInfo = (MeetingInfo) intent
                    .getSerializableExtra("MeetingInfo");
            CustomLog.v(TAG, "onCreatMeeting " + valueCode + "会议id: "
                    + meetingInfo.meetingId);
            onCreatMeeting(valueCode, meetingInfo, agentContextCreat);
        } else {
            CustomLog.v(TAG, "onCreatMeeting " + valueCode);
            onCreatMeeting(valueCode, null, agentContextCreat);
        }
        // String valueDes = intent.getExtras().getString("valueDes");

        isCreatMeeting = false;
    }

    private void handlerOnJoinMeetingBroadcast(Intent intent) {

        int valueCode = intent.getExtras().getInt("valueCode");
        String valueDes = intent.getExtras().getString("meetingId");
        CustomLog.v(TAG, "onJoinMeeting " + valueCode + " " + valueDes);
        if (valueCode != 0) {
            isJoinMeeting = false;
        }
        onJoinMeeting(valueDes, valueCode);

    }

    private void handlerOnGetNowMeetingsBroadcast(Intent intent) {
        if (isGetNowMeetings == false) {
            CustomLog.v(TAG, "GetNowMeetings状态为false，onGetNowMeetings不做回调 ");
            return;
        }
        int valueCode = intent.getExtras().getInt("valueCode");

        if (valueCode == 0) {
            List<MeetingItem> meetingInfoList = (List<MeetingItem>) intent
                    .getSerializableExtra("meetingInfoList");
            CustomLog.v(TAG, "onNowMeetings " + meetingInfoList.size());
            for (int i = 0; i < meetingInfoList.size(); i++) {
                CustomLog.v(TAG, "onNowMeetings "
                        + meetingInfoList.get(i).meetingType + " "
                        + meetingInfoList.get(i).createTime + " "
                        + meetingInfoList.get(i).creatorId + " "
                        + meetingInfoList.get(i).creatorName + " "
                        + meetingInfoList.get(i).topic + " "
                        + meetingInfoList.get(i).meetingId + " "
                        + meetingInfoList.get(i).hasMeetingPwd);
            }
            onNowMeetings(meetingInfoList, valueCode, agentContextGetMeetings);
        } else {
            CustomLog.v(TAG, "onNowMeetings " + valueCode);
            onNowMeetings(null, valueCode, agentContextGetMeetings);
        }

        isGetNowMeetings = false;
    }

    private void handlerOnIncomingCallBroadcast(Intent intent) {
        int valueCode = intent.getExtras().getInt("valueCode");
        String valueDes = intent.getExtras().getString("valueDes");
        CustomLog.v(TAG, "onIncomingCall " + valueCode + valueDes);
        onIncomingCall(valueDes, valueCode);
    }

    private void handlerOnQuitMeetingBroadcast(Intent intent) {
        isQuitMeeting = false;
        int valueCode = intent.getExtras().getInt("valueCode");
        String valueDes = intent.getExtras().getString("meetingId");
        if (valueDes.equals("0")) {
            valueDes = "null";
        }
        CustomLog.v(TAG, "onQuitMeeting " + valueCode + valueDes);
        onQuitMeeting(valueDes, valueCode);
    }

    private void handlerOnEventBroadcast(Intent intent) {
        int eventCode = intent.getExtras().getInt("eventCode");
        Object eventContent = (Object) intent
                .getSerializableExtra("eventContent");
        CustomLog.v(TAG, "onEvent " + eventCode);
        switch (eventCode) {
            case JOIN_MEETING:
                isJoinMeeting = false;

                break;
            case PHONE_RING:
                break;
            case JMeetingAgent.QUIT_TOKEN_DISABLED:
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
            case JMeetingAgent.QUIT_MEETING_INTERFACE:
                isQuitMeeting = false;

        }
        if (!isCrashed) {
            onEvent(eventCode, eventContent);
        }
    }

    /**
     * 不设置RootDirectory,使用默认根目录
     */
    public JMeetingAgent() {
        LogcatFileManager.getInstance().setLogDir(this.rootDirectory + "/jmeetingsdk");
        LogcatFileManager.getInstance().start("cn.redcdn.jmeetingsdkagent");
        CustomLog.v(TAG, "JMeetingAgent construct 进程id:" + android.os.Process.myPid());
        CustomLog.v(TAG, "JMeetingSDK版本：" + VERSION);
    }

    /**
     * 设置RootDirectory
     *
     * @param RootDirectory
     */
    public JMeetingAgent(String RootDirectory) {
        LogcatFileManager.getInstance().setLogDir(RootDirectory + "/jmeetingsdk");
        LogcatFileManager.getInstance().start("cn.redcdn.jmeetingsdkagent");
        CustomLog.v(TAG, "JMeetingAgent construct 进程id:" + android.os.Process.myPid());
        CustomLog.v(TAG, "JMeetingSDK版本：" + VERSION);
        JMeetingAgent.this.rootDirectory = RootDirectory;
    }

    /**
     * 设置根目录接口
     *
     * @param RootDirectory
     * @return
     */
    public int setRootDirectory(String RootDirectory) {

        if (RootDirectory == null || RootDirectory.equals("")) {
            CustomLog.v(TAG, "setRootDirectory return: -2参数为空");
            return -2;
        }

        try {
            iJMeetingService.setRootDirectory(RootDirectory);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;

    }

    /**
     * 1.设置前后摄像头参数接口; 2.同步接口
     *
     * @param cameraId: 类型：（int） | 说明： （摄像头类型， 0：后摄像头   1：前摄像头）
     *                  capWidth: 类型：（int） | 说明：（采集分辨率-宽度）
     *                  capHeight: 类型：（int） | 说明： （采集分辨率-高度）
     *                  capFps: 类型： （int） | 说明： （采集帧率）
     *                  encBitrate: 类型： （int） | 说明：（编码码率）
     * @return 0: 成功
     * -2: 参数不合法
     * -3: aldl接口调用异常
     */
    public int setVideoParameter(int cameraId, int capWidth, int capHeight, int capFps, int encBitrate) {

        if (capWidth <= 0 || capHeight <= 0 || capFps <= 0 || encBitrate <= 0) {
            CustomLog.v(TAG, "setResolution return: -2参数不合法");
            return -2;
        }

        try {
            CustomLog.d(TAG,"setVideoParameter:" +cameraId + capWidth + capHeight + capFps +  encBitrate);
            iJMeetingService.setVideoParameter(cameraId, capWidth, capHeight, capFps, encBitrate);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.设置appType接口; 2.同步接口
     *
     * @param appType 应用类型
     * @return
     */
    public int setAppType(String appType) {
        if (appType == null || appType.equals("")) {
            CustomLog.v(TAG, "setAppType return: -2参数为空");
            return -2;
        }

        try {
            iJMeetingService.setAppType(appType);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;

    }

    /**
     * 1.设置是否需要屏幕分享接口; 2.同步接口
     *
     * @param isShare 为true的时候显示屏幕分享按钮，为false的时候，不显示屏幕分享按钮
     * @return
     */
    public int setShowMeetingScreenSharing(Boolean isShare) {
        if (isShare == null) {
            CustomLog.v(TAG, "isShareMeeting return: -2参数为空");
            return -2;
        }
        try {
            iJMeetingService.setShowMeetingScreenSharing(isShare);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.设置是否需要小窗按钮接口; 2.同步接口
     * @param isShowMeetingFloat
     * @return
     */
    public int setShowMeetingFloat(Boolean isShowMeetingFloat) {
        if (isShowMeetingFloat == null) {
            CustomLog.v(TAG, "setShowMeetingFloat return: -2参数为空");
            return -2;
        }
        try {
            iJMeetingService.setShowMeetingFloat(isShowMeetingFloat);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.设置通讯录内容提供器授权者; 2.同步接口
     *
     * @param authorities 讯录内容提供器授权者
     * @return
     */

    public int setContactProvider(String authorities) {
        if (authorities == null) {
            CustomLog.v(TAG, "setProvider return: -2参数为空");
            return -2;
        }
        try {
            iJMeetingService.setContactProvider(authorities);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.设置微信appid; 2.同步接口
     * @param appId
     * @return
     */
    public int setWXAppId(String appId) {
        if (appId == null) {
            CustomLog.v(TAG, "setWXAppId return: -2参数为空");
            return -2;
        }
        try {
            iJMeetingService.setWXAppId(appId);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.设置会议是否自适应; 2.同步接口
     * @param isMeetingAdapter
     * @return
     */

    public int setMeetingAdapter(Boolean isMeetingAdapter) {
        if (isMeetingAdapter == null) {
            CustomLog.v(TAG, "setMeetingAdapter return: -2参数为空");
            return -2;
        }
        try {
            iJMeetingService.setMeetingAdapter(isMeetingAdapter);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }


    /**
     * 1.设置通讯录插入url接口; 2.同步接口
     *
     * @param ContactUrl 联系人url
     * @return
     */
    public int setInsertContactUrl(String ContactUrl) {
        if (ContactUrl == null || ContactUrl.equals("")) {
            CustomLog.v(TAG, "setInsertContactUrl return: -2参数为空");
            return -2;
        }

        try {
            iJMeetingService.setInsertContactUrl(ContactUrl);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;

    }

    /**
     * 1.设置通讯录查询url接口; 2.同步接口
     *
     * @param ContactUrl 联系人url
     * @return
     */
    public int setSearchContactUrl(String ContactUrl) {
        if (ContactUrl == null || ContactUrl.equals("")) {
            CustomLog.v(TAG, "setSearchContactUrl return: -2参数为空");
            return -2;
        }

        try {
            iJMeetingService.setSearchContactUrl(ContactUrl);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常: " + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;

    }

    /**
     * 初始化接口，异步接口，初始化结果onInit回调返回。
     *
     * @param context   应用程序上下文
     * @param userID    用户视讯号
     * @param userName  用户昵称
     * @param masterNps 主Nps服务器地址
     * @param slaveNps  备份nps服务器地址
     * @return 备份nps服务器地址
     */
    public int init(Context context, String token, String userID,
                    String userName, String masterNps, String slaveNps) {
        CustomLog.v(TAG, "JMeetingAgent 初始化 " + "token:" + token + " userID:" + userID
                + " userName:" + userName + " masterNps:" + masterNps
                + " slaveNps:" + slaveNps);
        if (isInit == true) {
            CustomLog.v(TAG, "init return：-2 正在调用初始化接口");
            return -2;
        }
        if (checkInitState()) {
            CustomLog.v(TAG, "init return：-1 已经初始化");
            return -1;
        }
        if (isCrashed) {
            CustomLog.v(TAG, "init return：-3 service进程已崩溃");
            return -3;
        }
        if (token == null || token.equals("") || userID == null
                || userID.equals("")) {
            CustomLog.v(TAG, "init return：-5  参数为空");
            return -5;
        }
        if (userName == null) {
            userName = "";
        }
        if (isJMeetingService == true) {
            try {
                CustomLog.v(TAG, "JMeetingService进程已连接，直接初始化service");
                iJMeetingService.init(token, userID, userName, masterNps,
                        slaveNps, rootDirectory);
            } catch (Exception e) {
                CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isInit = true;
            initState = false;
            return 0;
        }


        // CustomLog.v(TAG, String.valueOf(android.os.Process.myPid()));
        this.context = context;
        this.userID = userID;
        this.userName = userName;
        this.token = token;
        // this.isAllowMobileNet=isAllowMobileNet;
        this.masterNps = masterNps;
        this.slaveNps = slaveNps;

        //clientConnect = new ClientConnect();
        //aliveService=AliveService.getInstance();

        IntentFilter mInitFilter = new IntentFilter();
        mInitFilter.addAction(JMeetingService.JMEETING_ONINIT_BROADCAST);
        context.registerReceiver(jMeetingOnInitReceiver, mInitFilter);

        Intent intent = new Intent("cn.redcdn.jmeetingsdk.IJMeetingService");
        intent.setPackage(context.getPackageName());
        intent.putExtra("bindString", bindString);
        boolean boo = context.bindService(intent, conn,
                Context.BIND_AUTO_CREATE);
        CustomLog.v(TAG, "bindService " + boo);
        if (!boo) {
            CustomLog.v(TAG, "init -4");
            return -4;
        }

        mNotificationManager = (NotificationManager) context
                .getSystemService(context.NOTIFICATION_SERVICE);
        isInit = true;
        registerBroadcast();
        isRegistBroadcast = true;
        initState = false;
        isCreatMeeting = false;
        isGetNowMeetings = false;
        isAllowMobileNet = false;
        // isCrashed=false;
        agentContextCreat = null;
        agentContextGetMeetings = null;
        // mHandler.sendEmptyMessageDelayed(MSG_INIT, DELAY_UPDATE_SECOND);
        return 0;
    }

    private void registerBroadcast() {
        CustomLog.v(TAG, "registerBroadcast");
        IntentFilter filter = new IntentFilter();
        filter.addAction(JMeetingService.JMEETING_ONCREATMEETING_BROADCAST);
        filter.addAction(JMeetingService.JMEETING_ONJOINMEETING_BROADCAST);
        filter.addAction(JMeetingService.JMEETING_ONGETNOWMEETINGS_BROADCAST);
        filter.addAction(JMeetingService.MEDICAL_JMEETING_ONINCOMINGCALL_BROADCASE);
        filter.addAction(JMeetingService.JMEETING_ONQUITMEETING_BROADCAST);
        filter.addAction(JMeetingService.JMEETING_ONEVENT_BROADCAST);
        filter.addAction("NOTIFICATION");
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(jMeetingReceiver, filter);
    }

    /**
     * 初始化接口，异步接口，初始化结果onInit回调返回。
     *
     * @param context  应用程序上下文
     * @param userID   用户视讯号
     * @param userName 用户昵称
     * @return
     */
    public int init(Context context, String token, String userID,
                    String userName) {
        SettingData.getInstance().init(context,"");
        String masterNps = SettingData.getInstance().readNpsUrlFromLocal();
        return init(context, token, userID, userName, masterNps, masterNps);
        // return 0;
    }

    /**
     * 检查是否初始化接口，同步接口。
     *
     * @return 初始化状态
     */
    public boolean checkInitState() {
        CustomLog.v(TAG, "检查初始化状态: " + initState);
        return initState;
    }

    /**
     * 释放接口，同步接口。
     *
     * @return
     */
    public void release() {
        CustomLog.v(TAG, "MeetingAgent release");

        if (isJMeetingService) {
            CustomLog.v(TAG, "JMeetingService进程已连接，释放service");
            try {
                iJMeetingService.release();
            } catch (Exception e) {
                CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            context.unbindService(conn);
        }

        isJMeetingService = false;
        initState = false;
        isCreatMeeting = false;
        isGetNowMeetings = false;
        isInit = false;
        isRelease = false;
        isJoinMeeting = false;
        isCrashed = false;
        if (isRegistBroadcast) {
            context.unregisterReceiver(jMeetingOnInitReceiver);
            context.unregisterReceiver(jMeetingReceiver);
        }
        isRegistBroadcast = false;
        // clientConnect.close();
        //LogcatFileManager.getInstance().stop();
    }

    /**
     * 1. 异步接口 2. 创建结果通过 onCreateMeeting 异步返回 3. 如果出现网络异常，最多有30s超时
     *
     * @param inviterUserList
     * @return =0 接口调用成功 -1 未初始化 -2 createMeeting调用过程中
     */
    public int createMeeting(List<String> inviterUserList, String topic, int meetingType, String beginDateTime,
                             MeetingAgentContext context) {

        CustomLog.v(TAG, "createMeeting");
        CustomLog.v(TAG, inviterUserList.toString()
                + "context: " + context == null ? null
                : context.getContextId());
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "createMeeting return: -6  网络异常   ");
            return -6;
        }
        CustomLog.v(TAG, "检查是否正在创建会议: " + isCreatMeeting);
        if (isCreatMeeting == true) {
            CustomLog.v(TAG, "createMeeting return: -2   正在创建会议");
            return -2;
        }
        agentContextCreat = context;
        isCreatMeeting = true;
        try {
            iJMeetingService.creatMeeting(inviterUserList, topic, meetingType, beginDateTime);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        // mHandler.sendEmptyMessageDelayed(MSG_CREATMEETING,
        // DELAY_UPDATE_SECOND);
        CustomLog.v(TAG, "createMeeting return: 0 ");
        return 0;
    }

    /**
     * 1. 创建会议取消接口 2. 同步接口
     *
     * @return =0 接口调用成功 -1 未初始化<0 接口调用失败
     */
    public int cancelCreatMeeting() {
        CustomLog.v(TAG, "cancelCreatMeeting");
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        CustomLog.v(TAG, "检查是否正在创建会议： " + isCreatMeeting);
        if (isCreatMeeting == false) {
            CustomLog.v(TAG, "cancelCreatMeeting return：" + " -2 没有调用创建会议接口 ");
            return -2;
        }
        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "cancelCreatMeeting return：-6 网络异常  ");
            return -6;
        }
        isCreatMeeting = false;
        try {
            iJMeetingService.cancelCreatMeeting();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        CustomLog.v(TAG, "cancelCreatMeeting return： 0 ");
        return 0;
    }

    /**
     * 1.获取当前正在召开并且邀请当前用户的会议列表； 2.异步接口 3.会议列表结果通过onGetNowMeetings回调接口返回；
     *
     * @param context
     * @return 0 接口调用成功 -1 未初始化 -2 getNowMeetings调用中 <0 接口调用失败
     */
    public int getNowMeetings(MeetingAgentContext context) {
        CustomLog.v(TAG, "getNowMeetings");

        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }


        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "getNowMeetings return: -6 网络异常");
            return -6;
        }
        CustomLog.v(TAG, "检查是否调用nowMeetings接口: " + isGetNowMeetings);
        if (isGetNowMeetings == true) {
            CustomLog.v(TAG, "return -2 正在调用nowMeetings接口");
            return -2;
        }
        isGetNowMeetings = true;
        agentContextGetMeetings = context;
        // mHandler.sendEmptyMessageDelayed(MSG_GETNOEMEETINGS,
        // DELAY_UPDATE_SECOND);
        try {
            iJMeetingService.getNowMeetings();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1. 获取会议列表取消接口 2. 同步接口
     *
     * @return =0 接口调用成功 -1 未初始化<0 接口调用失败
     */
    public int cancelGetNowMeetings() {
        CustomLog.v(TAG, "cancelGetNowMeetings");
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "cancelGetNowMeetings return: -6 网络异常");
            return -6;
        }
        CustomLog.v(TAG, "检查是否调用nowMeetings接口: "
                + isGetNowMeetings);
        if (isGetNowMeetings == false) {
            CustomLog.v(TAG, "cancelGetNowMeetings return: -2 没有调用nowMeetings接口");
            return -2;
        }
        isGetNowMeetings = false;
        try {
            iJMeetingService.cancelGetNowMeetings();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.异步接口 2.处理外呼邀请结果通过onIncomingCall返回；
     *
     * @param inviterId   邀请人视讯号ID
     * @param inviterName 邀请人名称
     * @param MeetingId   会议id
     * @param headUrl     邀请人头像地址
     * @return 0 接口调用成功 -1 未初始化 <0 接口调用失败
     */
    public int incomingCall(String inviterId, String inviterName,
                            String MeetingId, String headUrl) {
        CustomLog.v(TAG, "incomingCall " + "邀请者：" + inviterId + " " + inviterName
                + " " + "meetingId: " + MeetingId + " headurl: " + headUrl);
        if (inviterId == null || inviterId.equals("") || MeetingId == null
                || MeetingId.equals("")) {
            CustomLog.v(TAG, "incomingCall return: -4 参数为空");
            return -4;
        }

        if (inviterName == null) {
            inviterName = "";
        }
        if (headUrl == null) {
            headUrl = "";
        }
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "incomingCall return: -6 网络异常 ");
            return -6;
        }
        try {
            iJMeetingService.incomingCall(inviterId, inviterName,
                    Integer.parseInt(MeetingId), headUrl);
        } catch (NumberFormatException e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        // mHandler.sendEmptyMessageDelayed(MSG_INCOMINGCALL,
        // DELAY_UPDATE_SECOND);
        return 0;
    }

    public int joinMeeting(String meetingId) {
        return joinMeeting(meetingId, "");
    }

    public int resumeMeeting(String meetingId) {
        return resumeMeeting(meetingId, "");
    }

    /**
     * 1.加入会议，异步接口; 2.加入会议结果通过onJoinMeeting返回； 3. joinMeeting调用过程中则同步返回-2；
     * 4.正在会议过程中则通过onJoinMeeting异步返回加入会议失败；
     *
     * @param meetingID 需要加入的会议ID
     * @return =0 接口调用成功 -1 未初始化 -2 joinMeeting调用过程中 <0 接口调用失败
     */
    public int joinMeeting(String meetingID, String groupId) {
        // mHandler.sendEmptyMessageDelayed(MSG_JOINMEETING,
        // DELAY_UPDATE_SECOND);
        if (groupId == null) {
            groupId = "";
        }
        CustomLog.v(TAG, "joinMeeting meetingID: " + meetingID + " groupId:" + groupId);
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "joinMeeting return: -6 网络异常 ");
            return -6;
        }
        CustomLog.v(TAG, "检查是否调用正在加入会议: " + isJoinMeeting);
        if (isJoinMeeting == true) {
            CustomLog.v(TAG, "joinMeeting return: -2 正在调用加入会议接口");
//			return -2;
        }
        if (meetingID.equals("") || meetingID == null) {
            CustomLog.v(TAG, "joinMeeting return: -4 参数为空");
            return -4;
        }
        try {
            iJMeetingService.joinMeeting(Integer.parseInt(meetingID), groupId);
        } catch (NumberFormatException e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        isJoinMeeting = true;
        return 0;
    }
    public int resumeMeeting(String meetingID, String groupId) {
        // mHandler.sendEmptyMessageDelayed(MSG_JOINMEETING,
        // DELAY_UPDATE_SECOND);
        if (groupId == null) {
            groupId = "";
        }
        CustomLog.v(TAG, "resumeMeeting meetingID: " + meetingID + " groupId:" + groupId);
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "resumeMeeting return: -6 网络异常 ");
            return -6;
        }
        CustomLog.v(TAG, "检查是否调用正在加入会议: " + isJoinMeeting);
        if (isJoinMeeting == true) {
            CustomLog.v(TAG, "resumeMeeting return: -2 正在调用加入会议接口");
//			return -2;
        }
        if (meetingID.equals("") || meetingID == null) {
            CustomLog.v(TAG, "resumeMeeting return: -4 参数为空");
            return -4;
        }
        try {
            iJMeetingService.resumeMeeting(Integer.parseInt(meetingID), groupId);
        } catch (NumberFormatException e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        isJoinMeeting = true;
        return 0;
    }

    public int cancelJoinMeeting() {
        CustomLog.v(TAG, "cancelJoinMeeting ");
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "cancelJoinMeeting return -6 网络异常 ");
            return -6;
        }
        CustomLog.v(TAG, "检查是否调用正在加入会议: " + isJoinMeeting);
        if (isJoinMeeting == false) {
            CustomLog.v(TAG, "cancelJoinMeeting return: -2 当前没有调用加入会议接口");
            return -2;
        }

        try {
            iJMeetingService.cancelJoinMeeting();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        isJoinMeeting = false;
        return 0;
    }

    /**
     * 1.当用户信息视讯号、昵称发生变化时调用； 2.同步接口；
     *
     * @param userName 用户昵称
     * @param userID   用户ID
     */
    public int setCurrentUser(String userID, String userName, String token) {
        CustomLog.v(TAG, "setCurrentUser: " + userID + " " + userName + " " + token);
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }

        if (!checkInitState()) {
            return -1;
        }

        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "setCurrentUser return: -6 网络异常");
            return -6;
        }
        if (userID == null || userID.equals("") || token == null
                || token.equals("")) {
            CustomLog.v(TAG, "setCurrentUser return: -2 参数为空");
            return -2;
        }
        this.userID = userID;
        this.userName = userName;
        this.token = token;

        try {
            iJMeetingService.setCurrentUser(userID, userName, token);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1. 同步接口 2.触发切换到当前正在召开的会议室界面中
     *
     * @return 0 接口调用成功 -1 未初始化 -2 不存在当前正在召开的会议室界面 <0 接口调用失败
     */
    public int switchToMeetingRoom() {
        return 0;
    }

    /**
     * 1. 当token发生变化时调用; 2. 同步接口
     *
     * @param token 用户token
     * @return 0 更新成功 -1 未初始化 <0 更新失败
     */
    public int updateToken(String token) {
        CustomLog.v(TAG, "updateToken: " + token);
        if (!checkInitState()) {
            return -1;
        }
        if (isCrashed) {
            CustomLog.v(TAG, "isCrashed " + isCrashed);
            return -3;
        }
        if (NetConnectHelper.getNetWorkType(this.context) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "网络异常  接口返回-6");
            return -6;
        }
        if (token == null) {
            CustomLog.v(TAG, "return -2");
            return -2;
        }
        if (this.token.equals(token)) {
            return 0;
        }
        this.token = token;
        try {
            iJMeetingService.updateToken(token);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    public int setisAllowMobileNet(boolean isAllowMobileNet) {
        CustomLog.v(TAG, "setisAllowMobileNet: " + isAllowMobileNet);
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }
        if (!checkInitState()) {
            return -1;
        }

        try {
            iJMeetingService.setisAllowMobileNet(isAllowMobileNet);
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -3;
        }
        return 0;
    }

    /**
     * 1.退出会议，异步接口； 2.退出会议结果通过onQuitMeeting异步回调接口返回
     *
     * @return =0 接口调用成功 -1 未初始化 <0 接口调用失败
     */
    public int quitMeeting() {
        CustomLog.v(TAG, "quitMeeting");
        if (isCrashed) {
            CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
            return -3;
        }
        if (!checkInitState()) {
            return -1;
        }


        if (getActiveMeetingId().equals("")) {
            CustomLog.v(TAG, "quitMeeting return:  -7   当前没有正在进行的会议 ");
            return -7;
        }


        CustomLog.v(TAG, "检查是否调用退出会议: " + isQuitMeeting);
        if (isQuitMeeting == true) {
            CustomLog.v(TAG, "quitMeeting return: -2 正在调用退出会议接口");
            return -2;
        }
        // mHandler.sendEmptyMessageDelayed(MSG_QUITMEETING,
        // DELAY_UPDATE_SECOND);
        isQuitMeeting=true;
        return 0;
    }


	public int setSelectSystemCamera(boolean selectSystemCamera) {
		CustomLog.v(TAG, "setSelectSystemCamera: " + selectSystemCamera);
		if (isCrashed) {
			CustomLog.v(TAG, "service进程崩溃： isCrashed " + isCrashed);
			return -3;
		}
		if (!checkInitState()) {
			return -1;
		}
        try {
            iJMeetingService.quitMeeting();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            e.printStackTrace();
            return -3;
        }

		try {
			iJMeetingService.setSelectSystemCamera(selectSystemCamera);
		} catch (Exception e) {
			CustomLog.v(TAG, "aldl接口调用异常："+e.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -3;
		}
		return 0;
	}
    public String getActiveMeetingId() {
        CustomLog.v(TAG, "getActiveMeetingId");
        String returnInfo = "";
        if (isCrashed) {
            CustomLog.v(TAG, "isCrashed " + isCrashed);
            returnInfo = "";
            CustomLog.v(TAG, "getActiveMeetingId return：" + returnInfo);
            return returnInfo;
        }


        if (!checkInitState()) {
            returnInfo = "";
            CustomLog.v(TAG, "getActiveMeetingId return：" + returnInfo);
            return returnInfo;
        }


        try {
            returnInfo = iJMeetingService.getActiveMeetingId();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            e.printStackTrace();
            returnInfo = "";
            //return -3;
        }
        CustomLog.v(TAG, "getActiveMeetingId return：" + returnInfo);
        return returnInfo;
    }

    public static String eventCodeDes(int eventCode) {
        String eventContent = null;
        switch (eventCode) {
            case QUIT_MEETING_INTERFACE:
                eventContent = "主动调用退出接口退出会诊";
                break;
            case QUIT_TOKEN_DISABLED:
                eventContent = "会诊过程中token失效";
                break;
            case QUIT_PHONE_CALL:
                eventContent = "会诊中收到用户来电";
                break;
            case QUIT_MEETING_BACK:
                eventContent = "会诊中点击返回按钮";
                break;
            case QUIT_MEETING_SERVER_DESCONNECTED:
                eventContent = "会诊中出现网络异常";
                break;
            case QUIT_MEETING_LOCKED:
                eventContent = "会诊已锁定";
                break;
            case QUIT_MEETING_LIBS_ERROR:
                eventContent = "基础库异常";
                break;
            case QUIT_MEETING_AS_MEETING_END:
                eventContent = "会诊已结束";
                break;
            case QUIT_MEETING_NOTALLOW_USE_MOBILE_NET:
                eventContent = "不允许使用移动网络进行会诊";
                break;
            case QUIT_MEETING_AS_MESSAGE_INVITE:
                eventContent = "点击短信邀请退出会诊";
                break;
            case QUIT_MEETING_OTHER_PROBLEM:
                eventContent = "会诊中出现其他异常";
                break;
            case END_PHONE_RING_AS_IGNORE:
                eventContent = "点击忽略按钮结束振铃";
                break;
            case END_PHONE_RING_AS_JOIN_MEETING:
                eventContent = "点击加入会诊按钮结束振铃";
                break;
            case END_PHONE_RING_AS_TIMEOUT:
                eventContent = "未执行任何操作振铃结束";
                break;
            case TOKEN_DISABLED:
                eventContent = "操作过程中token失效";
                break;
            case MEETING_CRASH:
                eventContent = "极会诊进程崩溃";
                break;
            case RECOVERY_MEETING_CRASH:
                eventContent = "极会诊进程从崩溃中恢复";
                break;
            case MEETING_MENU:
                eventContent = "会诊菜单";
                break;
            case MEETING_SPEAK:
                eventContent = "发言";
                break;
            case MEETING_STOP_SPEAK:
                eventContent = "停止发言";
                break;
            case MEETING_INVITE_CLICK:
                eventContent = "会诊邀请";
                break;
            case MEETING_ADD_CONTACTS:
                eventContent = "会诊添加联系人";
                break;
            case MEETING_INVITE_INVITELIST:
                eventContent = "邀请列表添加联系人";
                break;
            case MEETING_INVITE_NUBE:
                eventContent = "视讯号添加联系人";
                break;
            case MEETING_GIVE_MIC:
                eventContent = "传麦";
                break;
            case MEETING_LOCK:
                eventContent = "会诊加锁";
                break;
            case MEETING_UNLOCK:
                eventContent = "会诊解锁";
                break;
            case MEETING_PARTICIPATERS:
                eventContent = "参会列表";
                break;
            case MEETING_SWITCHWINDOW:
                eventContent = "切换窗口";
                break;
            case MEETING_EXIT:
                eventContent = "退出会诊";
                break;
            case MEETINGID_DISABLED:
                eventContent = "会诊号无效";
                break;
            case MEETINGID_NET_DISABLED:
                eventContent = "网络差，会诊号查询失败";
                break;
            case MEETING_JOINMEETING:
                eventContent = "加入会诊";
        }
        return eventContent;
    }

    /**
     * 初始化异步回调接口
     *
     * @param valueCode =0, 初始化成功 <0, 初始化失败
     * @param valueDes
     */
    protected abstract void onInit(String valueDes, int valueCode);

    /**
     * 创建会议结果异步回调接口
     *
     * @param valueCode   =0, 创建会议成功，会议信息通过meetingInfo获取; <0, 创建会议失败,meetingInfo = null
     * @param meetingInfo
     */
    protected abstract void onCreatMeeting(int valueCode,
                                           MeetingInfo meetingInfo, MeetingAgentContext agentContext);

    /**
     * 外呼邀请异步回调接口
     *
     * @param valueDes
     * @param valueCode =0,处理成功； <0,处理失败
     */
    abstract protected void onIncomingCall(String valueDes, int valueCode);

    /**
     * 加入会议异步回调接口
     *
     * @param meetingId
     * @param valueCode =0, 加入会议成功; =-1，已经在会议过程中； <0, 加入会议失败
     */
    abstract protected void onJoinMeeting(String meetingId, int valueCode);

    /**
     * 获取会议列表异步回调接口
     *
     * @param meetingInfos
     * @param valueCode    =0 获取成功，会议列表通过解析meetingList获取; <0 获取失败，meetingList =null
     */
    abstract protected void onNowMeetings(List<MeetingItem> meetingInfos,
                                          int valueCode, MeetingAgentContext agentContext);

    /**
     * 退出会议异步回调接口
     *
     * @param valueCode =0, 退出会议成功; <0, 退出会议失败
     */
    abstract protected void onQuitMeeting(String meetingId, int valueCode);

    /**
     * 事件回调接口
     *
     * @param eventContent
     * @param eventCode    =0, 退出会议成功; <0, 退出会议失败
     */
    abstract protected void onEvent(int eventCode, Object eventContent);


}
