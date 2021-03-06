package cn.redcdn.hnyd.meeting.meetingManage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.authentication.server.AuthenticateInfo;
import cn.redcdn.butelopensdk.vo.VideoParameter;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.im.bean.BookMeetingExInfo;
import cn.redcdn.hnyd.im.bean.ButelMeetingExInfo;
import cn.redcdn.hnyd.im.dao.NoticesDao;
import cn.redcdn.hnyd.im.fileTask.FileTaskManager;
import cn.redcdn.hnyd.meeting.util.CommonUtil;
import cn.redcdn.hnyd.meeting.util.LogUtil;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.StringUtil;
import cn.redcdn.jmeetingsdk.JMeetingAgent;
import cn.redcdn.jmeetingsdk.MeetingAgentContext;
import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.jmeetingsdk.MeetingItem;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.InviteeItem;

/**
 * Created by caiguo on 2017/2/27 0027.
 * <p> 单例结构 提供创建会议 加入会议 获取会议列表  创建预约会议  并提供回调接口   此外还提供设置4G开会  摄像头分辨率
 * <p> 代码流程 创建MedicalMeetingManage实例 调用初始化方法 调用接口
 */
//命名   MedicalMeetingManager
public class MedicalMeetingManage {
    public static final String JMEETING_INVITE_URL = SettingData.getInstance().DOWNLAOD_LINK;
    private String TAG = getClass().getName();
    public static final int NETWORKINVISIBLE = -9992;
    private static final int CANNOTINVITESELF = -9993;
    private static final int ISMEETIGNACTIVE = -9994;
    private static final int EXCEPTION_CODE = -9999;
    private String token;
    private String nubeNumber;//视讯号
    private String userName;//昵称
    private String masterNps = "http://103.25.23.83:8018/nps_x1/";//主Nps地址
    private String slaveNps = "http://103.25.23.83:8018/nps_x1/";//从Nps地址
    private static MedicalMeetingManage mInstance;
    private Context mContext = MedicalApplication.getContext();//外面传递
    private JMeetingAgent mAgent;
    private int meetingType;//会议类型  1、及时会议  2、预约会议
    private SharedPreferences frontSetting = null;//局部变量
    private SharedPreferences backSetting = null;
    private final String VIDEO_CAP_WIDTH = "capWidth";//sharepreference缓存
    private final String VIDEO_CAP_HEIGHT = "capHeight";
    private final String VIDEO_CAP_FPS = "capFps";
    private final String VIDEO_ENC_BITRATE = "encBitrate";
    private final int CAMERA_FACING_BACK = 0;//幻数说明
    private OnCreateMeetingListener mOnCreateMeetingListener = null;
    private OnJoinMeetingListener mOnJoinMeetingListener = null;
    private OnGetNowMeetignListener mOnGetNowMeetignListener = null;
    private OnReserveMeetingListener mOnReserveMeetingListener = null;
    private OnIncommingCallListener mOnIncommingCallListener = null;
    private OnInitListener mOnInitListener= null;
    private OnInitListener mMSGOnInitListener=null;
    private initState mInitState =initState.NONE;
    public enum initState {
        NONE, INITIALIZING, FAILED, SUCCESS
    }

    public static synchronized MedicalMeetingManage getInstance() {
        if (mInstance == null) {
            mInstance = new MedicalMeetingManage();
        }
        return mInstance;
    }


    /**
     * 构造方法中实例化JmeetingAgent
     */
    private MedicalMeetingManage() {
        CustomLog.i(TAG, "MedicalMeetingManage");
    }


    private void newJmeetingAgent() {
        if (mAgent == null) {
            //风险    TODO   JMeetingAgent 参数问题 建议放在init  参数合法性校验
            mAgent = new JMeetingAgent() {
                @Override
                protected void onInit(String valueDes, int valueCode) {//是否会调用第二次
                    CustomLog.i(TAG, "JMeetingAgent初始化回调  valueCode==" + valueCode + "  valueDes==" + valueDes);
                    if (mOnInitListener != null) {
                        mOnInitListener.onInit(valueDes, valueCode);
                        mOnInitListener = null;
                    }
                    if (mMSGOnInitListener!=null){
                        mMSGOnInitListener.onInit(valueDes, valueCode);
                        mMSGOnInitListener=null;
                    }
                    if (valueCode == 0) {
                        //TODO 让agent在重启的时候恢复状态
                        setInitState(initState.SUCCESS);
                        setAppType(MeetingManager.MEETING_APP_BUTEL_CONSULTATION);
                    }else{
                        setInitState(initState.FAILED);
                    }
                }


                @Override
                protected void onCreatMeeting(int valueCode, MeetingInfo meetingInfo, MeetingAgentContext agentContext) {
                    if (valueCode == -6) {
                        CustomToast.show(mContext, MedicalApplication.getContext().getString(R.string.net_error), CustomToast.LENGTH_SHORT);
                    }
                    if (valueCode != 0 && valueCode != -6) {
                        CustomToast.show(mContext, MedicalApplication.getContext().getString(R.string.creat_meeting_fail), CustomToast.LENGTH_SHORT);
                    }
                    if (meetingType == 1) {
                        if (mOnCreateMeetingListener != null) {
                            mOnCreateMeetingListener.onCreateMeeting(valueCode, meetingInfo);
                        }
                        mOnCreateMeetingListener = null;
                    } else if (meetingType == 2) {
                        if (mOnReserveMeetingListener != null) {
                            mOnReserveMeetingListener.OnReserveMeeting(valueCode, meetingInfo);
                        }
                        mOnReserveMeetingListener = null;
                    }
                }


                @Override
                protected void onIncomingCall(String valueDes, int valueCode) {
                    if (mOnIncommingCallListener != null) {
                        mOnIncommingCallListener.onIncommingCall(valueDes, valueCode);
                    }
                    mOnIncommingCallListener = null;
                }


                @Override
                protected void onJoinMeeting(String meetingId, int valueCode) {
                    CustomLog.i(TAG, " onJoinMeeting 返回 " + " " + valueCode);
                    if (mOnJoinMeetingListener != null) {
                        mOnJoinMeetingListener.onJoinMeeting(meetingId, valueCode);
                    }
                    mOnJoinMeetingListener = null;
                }


                @Override
                protected void onNowMeetings(List<MeetingItem> meetingInfos,
                                             int valueCode, MeetingAgentContext agentContextCreat) {
                    if (mOnGetNowMeetignListener != null) {
                        mOnGetNowMeetignListener.onGetNowMeeting(meetingInfos,
                            valueCode);
                    }
                    mOnGetNowMeetignListener = null;
                }


                @Override
                protected void onQuitMeeting(String valueDes, int valueCode) {
                    CustomLog.i(TAG, " onQuitMeeting 返回 " + valueDes + " " + valueCode);
                }


                @Override
                protected void onEvent(int eventCode, Object eventContent) {
                    if (eventContent != null) {
                        CustomLog.i(TAG, " onEvent 返回 " + eventCode + " "
                            + eventContent.toString());
                        switch (eventCode) {
                            case JOIN_MEETING:
                                String eventString = (String) eventContent;
                                CustomLog.i(TAG, "onEvent 返回 1100 加入会议" + " " + eventString);
                                break;
                            case PHONE_RING:
                                CustomLog.i(TAG, "onEvent 返回 1200 开始振铃");
                                break;
                            case MEETING_INVITE:
                                InviteeItem item = (InviteeItem) eventContent;
                                ArrayList<String> inviteList = new ArrayList<>();
                                inviteList.add(item.inviteeId);
                                inviteMeeting(inviteList, item.meetingId);
                                break;
                            case TOKEN_DISABLED:
                                String tokenString = (String) eventContent;
                                CustomLog.i(TAG,
                                    "onEvent 返回 1400 token失效  tokenString==" + tokenString);
                                AccountManager.getInstance(mContext).tokenAuthFail(1400);
                                break;
                            case MEETING_JOINMEETING:
                                String id = mAgent.getActiveMeetingId();
                                CustomLog.i(TAG, "onEvent 返回 1615" + "  getActiveMeetingId" + id);
                                break;
                            case QUIT_MEETING_SERVER_DESCONNECTED:
                                CommonUtil.showToast(MedicalApplication.getContext().getString(R.string.net_error_try));
                                break;
                            case QUIT_MEETING_LOCKED:
                                // CommonUtil.showToast("会诊已锁定！"); 重复提示
                                break;
                            case QUIT_MEETING_LIBS_ERROR:
                                CommonUtil.showToast(MedicalApplication.getContext().getString(R.string.server_connect_error_wait_try));
                                break;
                            case QUIT_MEETING_AS_MEETING_END:
                                CommonUtil.showToast(MedicalApplication.getContext().getString(R.string.consultation_has_ended));
                                break;
                            case QUIT_MEETING_OTHER_PROBLEM:
                                CommonUtil.showToast(MedicalApplication.getContext().getString(R.string.server_connect_error_wait_try));
                                break;
                            case MEETING_CRASH:
                                CustomLog.i(TAG, "会诊进程崩溃！");
                                break;
                            case RECOVERY_MEETING_CRASH:
                                CustomLog.i(TAG, "会诊进程从崩溃中恢复！");
                                setAppType(MeetingManager.MEETING_APP_BUTEL_CONSULTATION);
                                break;
                            default:
                                break;
                        }
                    }
                }
            };
        }
    }


    /**
     * MedicalMeetingManage初始化 和  release 配合使用
     * 初始化接口，异步接口，初始化结果onInit回调返回。
     *
     * @return 0成功   非0失败
     */
    public int init(OnInitListener listener) {
        CustomLog.i(TAG, "init()  ");
        //参数传递   由外界提供   参数不正确直接返回
        setAccoutInfo();
        newJmeetingAgent();
        int result = mAgent.init(mContext, token,   // 初始化meetingAgent
            nubeNumber, userName, masterNps, slaveNps);
        CustomLog.i(TAG, "初始化 meetingAgent result == " + result);
        if (result == 0) {
            this.mOnInitListener = listener;
            setInitState(initState.INITIALIZING);
        }else{
            setInitState(initState.FAILED);
        }
        return result;
    }


    /**
     * 1、设置Nps地址不调用使用默认地址  2、
     */
    public void setNps(String masterNps, String slaveNps) {
        this.masterNps = masterNps;
        this.slaveNps = slaveNps;
    }


    /**
     * 1、设置appType  2、同步接口
     *
     * @param appType aap名称
     */
    public int setAppType(String appType) {
        return mAgent.setAppType(appType);
    }


    /**
     * 1、设置账户信息用于登陆  2、同步接口
     */
    private void setAccoutInfo() {
        CustomLog.i(TAG, "getAccoutInfo()");
        AccountManager mAccountManager = AccountManager.getInstance(mContext);
        AuthenticateInfo mAccountInfo = mAccountManager.getmAuthInfo();
        token = mAccountInfo.getAccessToken();
        nubeNumber = mAccountInfo.getNubeNumber();
        userName = mAccountInfo.getNickname();
        if (token != null) {
            CustomLog.i(TAG, "token:" + token);
        } else {
            CustomLog.e(TAG, "toke == null");
        }
        if (userName != null) {
            CustomLog.i(TAG, "userName:" + userName);
        } else {
            CustomLog.e(TAG, "userName == null");
        }
        if (nubeNumber != null) {
            CustomLog.i(TAG, "nubeNumber:" + nubeNumber);
        } else {
            CustomLog.e(TAG, "nubeNumber == null");
        }
    }


    /**
     * 1、创建即时会议  2、异步接口 创建会议结果通过onCreateMeeting回调
     *
     * @param contextid 调用者类名
     * @param list 被邀请参会人员列表
     * @param listener 创建会议监听
     * @return -101 未初始化 -1 创建会议失败 0 成功
     */
    public int createMeeting(String contextid, ArrayList<String> list, OnCreateMeetingListener listener) {
        MeetingAgentContext agentContext = new MeetingAgentContext();
        agentContext.setContextId(contextid);
        if (checkNet()) return NETWORKINVISIBLE;
        return createMeeting(agentContext, list, listener);
    }


    private void reInit() {
        init(new OnInitListener() {
            @Override public void onInit(String valueDes, int valueCode) {
                if (valueCode == 0) {
                    setAppType(MeetingManager.MEETING_APP_BUTEL_CONSULTATION);
                }
            }
        });
    }


    /**
     * 创建会议
     *
     * @param context 用来保存调用者类名字符串
     * @param list 参会人员列表
     */
    private int createMeeting(MeetingAgentContext context, ArrayList<String> list, OnCreateMeetingListener listener) {
        int ret;
        CustomLog.i(TAG, "createMeeting::");
        ret = mAgent.createMeeting(list, AccountManager.getInstance(mContext).getName() + "会诊室", 1, "", context);
        meetingType = 1;
        switch (ret) {
            case -1:
                reInit();
                break;
            case 0:
                this.mOnCreateMeetingListener = listener;
        }
        return ret;
    }


    /**
     * 预约会议
     *
     * @param contextId 调用者类名
     * @param list 邀请人员列表
     * @param topic 会议主题
     * @param beginDateTime 开始时间
     */
    public int createBookMeeting(String contextId, ArrayList<String> list, String topic, String beginDateTime, OnReserveMeetingListener listener) {
        MeetingAgentContext agentContext = new MeetingAgentContext();
        agentContext.setContextId(contextId);
        int ret;
        ret = mAgent.createMeeting(list, topic, 2, beginDateTime, agentContext);
        meetingType = 2;
        switch (ret) {
            case -1:
                reInit();
            case 0:
                this.mOnReserveMeetingListener = listener;
        }
        return ret;
    }


    /**
     * 取消创建会议
     *
     * @param contextId 接口调用者类名
     * 取消创建会议并无回调方法
     */
    public int cancelCreateMeeting(String contextId) {
        LogUtil.testD_JMeetingManager("start  contextID: " + contextId);
        return mAgent.cancelCreatMeeting();
    }


    /**
     * @return 0：拉取操作成功，等待请求回调;
     * -3:级会议进程崩溃；
     * <0:接口调用失败；
     */
    public int getNowMeetings(OnGetNowMeetignListener listener) {
        // needFreshData = true; 如果能够对是否需要更新数据进行判断的话那么就不在需要重新获取数据了
        if (checkNet()) return -6;
        MeetingAgentContext agentContext = new MeetingAgentContext();
        int ret;
        if(mAgent==null){
            ret = -1;
        }else{
            ret = mAgent.getNowMeetings(agentContext);
        }
        CustomLog.i(TAG, "getNowMeetings 同步返回值 reseult==" + ret);
        switch (ret) {
            case 0:
                this.mOnGetNowMeetignListener = listener;
                return 0;
            case -2:
                return -2;
            //获取会议列表的异常处理：当返回-1时，说明当前agent尚未初始化或者正在初始化过程中，则重新init;
            //如果init返回-2.则说明正在初始化工程中，则不做任何处理，在onInit根据needFreshData标记来进行getNowmeeting的操作
            //返回0或者-1：说明初始化刚好完成，此时再次获取列表
            //其他值则说明当前程序异常，返回异常错误码
            case -1://检测到会议sdk没有没有初始化
                CustomLog.i(TAG, "检测会议sdk没有初始化");
                reInit();
                return -1;
            default:
                break;
        }
        return EXCEPTION_CODE;
    }


    private boolean checkNet() {
        if (NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID) {
            CustomLog.v(TAG, "createMeeting return: -6  网络异常   ");
            return true;
        }
        CustomLog.v(TAG, "createMeeting return: 网络正常  ");
        return false;
    }


    /**
     * 1. 获取会议列表取消接口 2. 同步接口
     *
     * @return =0 接口调用成功 -101 未初始化  -2 没有调用getNowMeeting接口  <0 接口调用失败
     */
    public int cancelGetNowMeetings(String className) {
        CustomLog.v(TAG, "cancelGetNowMeetings"+className);
        return mAgent.cancelGetNowMeetings();
    }


    /**
     * 1、加入会议   2.异步回调 加入会议结果通过onJoinMeeting返回
     *
     * @return -101未初始化  0成功 -1 失败
     */
    public int joinMeeting(String meetingId, OnJoinMeetingListener listener) {
        CustomLog.i(TAG, "joinMeeting");
        if (checkNet()) {
            return NETWORKINVISIBLE;
        }
        int ret = mAgent.joinMeeting(meetingId);
        if (ret == -1) {
            reInit();
        }
        if (ret == 0) {
            this.mOnJoinMeetingListener = listener;
        }
        return ret;
    }
    public int joinMeeting(String meetingId,String groupId,OnJoinMeetingListener listener) {
        CustomLog.i(TAG, "joinMeeting");
        if (checkNet()) {
            return NETWORKINVISIBLE;
        }
        int ret = mAgent.joinMeeting(meetingId,groupId);
        if (ret == -1) {
            reInit();
        }
        if (ret == 0) {
            this.mOnJoinMeetingListener = listener;
        }
        return ret;
    }


    /**
     * 1. 取消加入会议 2. 同步接口
     *
     * @return =0 接口调用成功 -101 未初始化  -<0 接口调用失败
     */
    public int cancelJoinMeeting(String ContextId) {
        CustomLog.v(TAG, "cancelJoinMeeting " + ContextId);
        try {
            mAgent.cancelJoinMeeting();
        } catch (Exception e) {
            CustomLog.v(TAG, "aldl接口调用异常：" + e.toString());
            e.printStackTrace();
            return -3;
        }
        return 0;
    }


    /**
     * 1.异步接口 2.当收到会议邀请时调用3.处理外呼邀请结果通过onIncomingCall返回；
     *
     * @param inviterId 邀请人视讯号ID
     * @param inviterName 邀请人名称
     * @param meetingId 邀请人头像地址
     * @param headUrl 邀请人头像地址
     * @return 0 接口调用成功 -1 未初始化 <0 接口调用失败
     */
    public int incomingCall(String inviterId, String inviterName, String meetingId, String headUrl, OnIncommingCallListener listener) {
        this.mOnIncommingCallListener = listener;
        CustomLog.i(TAG, "" + String.format("inviterId: %s, inviterName: %s,meetingId : %s,headUrl: %s", inviterId,
            inviterName, meetingId, headUrl));

        if (TextUtils.isEmpty(mAgent.getActiveMeetingId())) {
            if (!inviterId.equals(AccountManager.getInstance(mContext).getNube())) {//不让自己邀请自己
                int ret = mAgent.incomingCall(inviterId, inviterName, meetingId, headUrl);
                if (ret == -1) {
                    reInit();
                }
                return ret;
            } else {
                return CANNOTINVITESELF;
            }
        } else {
            return ISMEETIGNACTIVE;
        }
    }


    /**
     * 释放资源和init配合使用
     */
    public void release() {
        CustomLog.i(TAG, "release");
        mAgent.release();
        mAgent = null;
        mContext = null;
        mInstance = null;
    }


    /**
     * 1.设置前后摄像头参数接口; 2.同步接口
     *
     * @param id: 类型：（int） | 说明： （摄像头类型， 0：后摄像头   1：前摄像头）
     * capWidth: 类型：（int） | 说明：（采集分辨率-宽度）
     * capHeight: 类型：（int） | 说明： （采集分辨率-高度）
     * capFps: 类型： （int） | 说明： （采集帧率）
     * encBitrate: 类型： （int） | 说明：（编码码率）
     * @return 0: 成功
     * -2: 参数不合法
     */
    public int setVideoParameter(int id, VideoParameter p) {
        if (p == null) {
            CustomLog.e(TAG, "setVideoParameter p is null");
            return -2;
        }
        if (id == CAMERA_FACING_BACK) {
            if (backSetting == null) {
                backSetting = mContext.getSharedPreferences("backSetting",
                    Context.MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = backSetting.edit();
            editor.putInt(VIDEO_CAP_WIDTH, p.getCapWidth());
            editor.putInt(VIDEO_CAP_HEIGHT, p.getCapHeight());
            editor.putInt(VIDEO_CAP_FPS, p.getCapFps());
            editor.putInt(VIDEO_ENC_BITRATE, p.getEncBitrate());
            editor.apply();
            return mAgent.setVideoParameter(id, p.getCapWidth(), p.getCapHeight(), p.getCapFps(),
                p.getEncBitrate());
        } else {
            if (frontSetting == null) {
                frontSetting = mContext.getSharedPreferences("frontSetting",
                    Context.MODE_PRIVATE);
            }
            SharedPreferences.Editor editor = frontSetting.edit();
            editor.putInt(VIDEO_CAP_WIDTH, p.getCapWidth());
            editor.putInt(VIDEO_CAP_HEIGHT, p.getCapHeight());
            editor.putInt(VIDEO_CAP_FPS, p.getCapFps());
            editor.putInt(VIDEO_ENC_BITRATE, p.getEncBitrate());
            editor.apply();

            return mAgent.setVideoParameter(id, p.getCapWidth(), p.getCapHeight(), p.getCapFps(),
                p.getEncBitrate());

        }
    }


    /**
     * @param id 1、后置摄像头 2、前置摄像头
     * @return 摄像头参数类
     */
    public VideoParameter getVideoParameter(int id) {
        int frontDefaultRW = 640;
        int frontDefaultRH = 360;
        int backDefaultRW = 640;
        int backDefaultRH = 360;
        if (id == CAMERA_FACING_BACK) {
            if (backSetting == null) {
                backSetting = mContext.getSharedPreferences("medicalBackSetting",
                    Context.MODE_PRIVATE);
            }
            VideoParameter p = new VideoParameter();
            p.setCapFps(backSetting.getInt(VIDEO_CAP_FPS, 15));
            p.setCapHeight(backSetting.getInt(VIDEO_CAP_HEIGHT, 0));
            p.setCapWidth(backSetting.getInt(VIDEO_CAP_WIDTH, 0));
            p.setEncBitrate(backSetting.getInt(VIDEO_ENC_BITRATE, 300));
            CustomLog.d(TAG,
                id + ":" + p.getCapWidth() + "," + p.getCapHeight() + ","
                    + p.getCapFps() + "," + p.getEncBitrate());
            return p;
        } else {
            if (frontSetting == null) {
                frontSetting = mContext.getSharedPreferences("medicalFrontSetting",
                    Context.MODE_PRIVATE);
            }
            VideoParameter p = new VideoParameter();
            p.setCapFps(frontSetting.getInt(VIDEO_CAP_FPS, 15));
            p.setCapHeight(frontSetting
                .getInt(VIDEO_CAP_HEIGHT, 0));
            p.setCapWidth(frontSetting.getInt(VIDEO_CAP_WIDTH, 0));
            p.setEncBitrate(frontSetting.getInt(VIDEO_ENC_BITRATE, 300));
            CustomLog.d(TAG,
                id + ":" + p.getCapWidth() + "," + p.getCapHeight() + ","
                    + p.getCapFps() + "," + p.getEncBitrate());
            return p;
        }
    }


    /**
     * @param isAllow true|允许2、3、4G网络开会  false|不允许
     * @return 0 | 设置成功   非0 | 失败
     */
    public int setIsAllowNetJoinMeeting(boolean isAllow) {
        return mAgent.setisAllowMobileNet(isAllow);
    }


    public interface OnInitListener {
        /**
         * @param valueCode 0|成功  !0|失败
         */
        void onInit(String valueDes, int valueCode);
    }


    //CreateMeetingListener
    public interface OnCreateMeetingListener {

        void onCreateMeeting(int valuecode, MeetingInfo meetingInfo);
    }


    public interface OnJoinMeetingListener {
        /**
         * @param valueCode 0|成功  !0|失败
         */
        void onJoinMeeting(String valueDes, int valueCode);
    }


    public interface OnGetNowMeetignListener {
        /**
         * @param valueCode 0|成功  !0|失败
         */
        void onGetNowMeeting(List<MeetingItem> meetingInfos,
                             int valueCode);
    }


    public interface OnReserveMeetingListener {
        void OnReserveMeeting(int valueCode, MeetingInfo meetingInfo);
    }


    public interface OnIncommingCallListener {
        void onIncommingCall(String arg0, int code);
    }


    /**
     * 会议正在进行中点击home键响应问题
     */
    public void resumeMeeting() {
        CustomLog.i(TAG, "resumeMeeting");
        String meetId = mAgent.getActiveMeetingId();
        if (!TextUtils.isEmpty(meetId)) {
            mAgent.joinMeeting(meetId);
        }
    }


    public int inviteMeeting(ArrayList<String> nubeList, String meetid) {
        CustomLog.d("inviteMeeting", "nubelist: " + StringUtil.list2String(nubeList, ';') + "   meetid: " + meetid);
        if (nubeList != null && nubeList.size() > 0 && !TextUtils.isEmpty(meetid)) {
            AuthenticateInfo loginUserInfo = AccountManager.getInstance(MedicalApplication.getContext()).getmAuthInfo();
            NoticesDao noticesdao = new NoticesDao(mContext);
            String number = loginUserInfo.getNubeNumber();
            String nickname = loginUserInfo.getNickname();
            String headUrl = loginUserInfo.getHeadUrl();
            String meetingUrl = SettingData.getInstance().DOWNLAOD_LINK;
            ButelMeetingExInfo info = new ButelMeetingExInfo();
            info.setInviterId(number);
            info.setInviterName(nickname);
            info.setInviterHeadUrl(headUrl);
            info.setMeetingRoom(meetid);
            info.setMeetingUrl(meetingUrl);
            info.setShowMeeting(true);
            String uuid;
            if (nubeList.size() == 1 && nubeList.get(0).length() > 11) {
                //群组中刚发起会议
                uuid = noticesdao.createSendFileNotice(number,
                    nubeList.get(0), null, MedicalApplication.getContext().getString(R.string.messsge_title_meeting),
                    FileTaskManager.NOTICE_TYPE_MEETING_INVITE, "",
                    nubeList.get(0), info);
                MedicalApplication.getFileTaskManager()
                    .addTask(uuid, null);
            } else {
                //已有会议中邀请
                for (int i = 0; i < nubeList.size(); i++) {
                    if (nubeList.get(i).equals("68000001")
                        || nubeList.get(i).equals("68000002")) {
                        break;
                    }
                    uuid = noticesdao.createSendFileNotice(number,
                        nubeList.get(i), null, MedicalApplication.getContext().getString(R.string.messsge_title_meeting),
                        FileTaskManager.NOTICE_TYPE_MEETING_INVITE, "",
                        "", info);
                    MedicalApplication.getFileTaskManager()
                        .addTask(uuid, null);
                }
            }

        }
        return 0;
    }


    /**
     * 发送预约会议的消息，目前先放在这，以后不合适，可以移动
     * @param exInfo    预约会诊信息  （预约者视讯号  昵称 会诊主题等）
     * @param nubeList
     * @param gid
     */
    public void sendBookMeetingMsgs(BookMeetingExInfo exInfo, ArrayList<String> nubeList, String gid) {
        if (exInfo == null) {
            return;
        }
        NoticesDao noticesdao = new NoticesDao(mContext);
        String uuid;
        String sender = AccountManager.getInstance(MedicalApplication.getContext()).getNube();
        if (!TextUtils.isEmpty(gid)) {//群组中刚发起会议
            uuid = noticesdao.createSendFileNotice(sender,
                gid, null, mContext.getString(R.string.meeting_order),
                FileTaskManager.NOTICE_TYPE_MEETING_BOOK, "",
                gid, exInfo);
            MedicalApplication.getFileTaskManager().addTask(uuid, null);
        } else {
            //已有会议中邀请
            for (int i = 0; i < nubeList.size(); i++) {
                uuid = noticesdao.createSendFileNotice(sender,
                    nubeList.get(i), null, mContext.getString(R.string.meeting_order),
                    FileTaskManager.NOTICE_TYPE_MEETING_BOOK, "",
                    "", exInfo);
                MedicalApplication.getFileTaskManager().addTask(uuid, null);
            }
        }
    }
    public initState getInitState() {
        return mInitState;
    }


    private void setInitState(initState init) {
        mInitState = init;
    }

    public void addInitListener (OnInitListener onInitListener){
        this.mMSGOnInitListener=onInitListener;
    }
}

