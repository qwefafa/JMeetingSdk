package cn.redcdn.hnyd.im.agent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import cn.redcdn.authentication.server.AuthenticateInfo;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.contacts.contact.manager.ContactManager;
import cn.redcdn.hnyd.im.IMConstant;
import cn.redcdn.hnyd.im.UrlConstant;
import cn.redcdn.hnyd.im.agent.AppGroupManager.GroupInterfaceBean;
import cn.redcdn.hnyd.im.bean.FileTaskBean;
import cn.redcdn.hnyd.im.fileTask.FileTaskManager;
import cn.redcdn.hnyd.im.fileTask.FileTaskManager.SCIMBean;
import cn.redcdn.hnyd.im.interfaces.IFriendRelation;
import cn.redcdn.hnyd.im.interfaces.IMsgReceiveListener;
import cn.redcdn.hnyd.im.manager.FriendsManager;
import cn.redcdn.hnyd.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hnyd.im.preference.DaoPreference.PrefType;
import cn.redcdn.hnyd.im.receiver.NetWorkChangeReceiver;
import cn.redcdn.hnyd.im.work.BizConstant;
import cn.redcdn.hnyd.im.work.MessageGroupEventParse;
import cn.redcdn.hnyd.im.work.MessageReceiveAsyncTask;
import cn.redcdn.hnyd.im.work.MessageReceiveAsyncTask.MessageReceiverListener;
import cn.redcdn.hnyd.im.work.MessageReceiveAsyncTask.PrivateMessage;
import cn.redcdn.hnyd.im.work.MessageReceiveAsyncTask.SCIMRecBean;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.api.CommonButelConnSDKAPI_V2_4;
import com.butel.connectevent.api.ICommonButelConnCB_V2_4;
import com.butel.connectevent.api.ICommonButelConn_V2_4;
import com.butel.connectevent.api.IGroupButelConnCB_V2_4;
import com.butel.connectevent.api.IGroupButelConn_V2_4;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class AppP2PAgentManager implements ICommonButelConnCB_V2_4, IGroupButelConnCB_V2_4 {

    private static final String TAG = "AppP2PAgentManager";
    public final static int UPLOADFILE_TIMEOUT = 60 * 10; //文件上传的超时事件，单位秒
    private final static HashMap<String, GroupInterfaceBean> mGroupInterfaceBean
            = new HashMap<String, GroupInterfaceBean>();
    private MessageReceiverListener msgcheckListener = null;
    private final HashMap<String, MessageGroupEventParse> sid2eventParseMap
            = new HashMap<String, MessageGroupEventParse>();
    public final static int DEFAULT_ERROR_CODE = -22222;
    private FileTaskManager filetaskMgr = null;
    private Context mContext = MedicalApplication.getContext();

    private static ICommonButelConn_V2_4 innerclient = null;
    private IGroupButelConn_V2_4 groupclient = null;
    public static AppP2PAgentManager p2pAgentMgr = null;

    private final static HashMap<String, String> msgIdMap = new HashMap<String, String>();
    private boolean isOldVersionMsg = false;

    private final static int LOGIN_RETRY_TIME = 10;  //-99场景下，最多尝试login的次数
    private final static int AIDL_AGENT_NOT_INIT = -99;
    private static int login99_time = 0;
    private final static int MSG_LOGIN_RETRY = 10001;
    private final static int MSG_RESTART_INIT = 10002;
    private final static int MSG_CHECK_LOGINSTATUS = 10003; //检查当前登录状态，保护性措施【0021034】
    private final static long DELAY_TIME_CHECK = 3 * 60 * 1000; //检查当前登录状态的时间间隔【3分钟】

    private ImMsgResultInterface msgResultInterface = null;

    /*群组回掉事件消息*/
    public final static int GROUP_MESSAGE_EVENT_BASE = 6100;
    /*群事件-新建群*/
    public final static int GROUP_MESSAGE_EVENT_CREATE = GROUP_MESSAGE_EVENT_BASE + 1;
    /*群事件-更新群信息*/
    public final static int GROUP_MESSAGE_EVENT_UPDATE = GROUP_MESSAGE_EVENT_BASE + 2;
    /*群事件-增加群成员*/
    public final static int GROUP_MESSAGE_EVENT_ADDUSER = GROUP_MESSAGE_EVENT_BASE + 3;
    /*群事件-删除群成员*/
    public final static int GROUP_MESSAGE_EVENT_DELUSER = GROUP_MESSAGE_EVENT_BASE + 4;
    /*群事件-退出群*/
    public final static int GROUP_MESSAGE_EVENT_QUIT = GROUP_MESSAGE_EVENT_BASE + 5;
    /*群事件-解散群*/
    public final static int GROUP_MESSAGE_EVENT_DELETE = GROUP_MESSAGE_EVENT_BASE + 6;

    /*群组正调事件消息*/
    public final static int GROUP_MESSAGE_FUNCT_BASE = 6200;
    /*创建群回调*/
    public final static int GROUP_MESSAGE_FUNCT_CREATE = GROUP_MESSAGE_FUNCT_BASE + 1;
    /*修改群信息回调*/
    public final static int GROUP_MESSAGE_FUNCT_UPDATE = GROUP_MESSAGE_FUNCT_BASE + 2;
    /*增加群成员回调*/
    public final static int GROUP_MESSAGE_FUNCT_ADDUSER = GROUP_MESSAGE_FUNCT_BASE + 3;
    /*删除群成员回调*/
    public final static int GROUP_MESSAGE_FUNCT_DELUSER = GROUP_MESSAGE_FUNCT_BASE + 4;
    /*退出群回调*/
    public final static int GROUP_MESSAGE_FUNCT_QUIT = GROUP_MESSAGE_FUNCT_BASE + 5;
    /*解散群回调*/
    public final static int GROUP_MESSAGE_FUNCT_DELETE = GROUP_MESSAGE_FUNCT_BASE + 6;
    /*查询群详情回调*/
    public final static int GROUP_MESSAGE_FUNCT_QUERY = GROUP_MESSAGE_FUNCT_BASE + 7;
    /*获取与某人相关群的列表群回调*/
    public final static int GROUP_MESSAGE_FUNCT_GETALL = GROUP_MESSAGE_FUNCT_BASE + 8;

    private BroadcastReceiver netReceiver;
    private int imLoginStatus = 0; //0:初始化失败 1:login失败 2:login成功
    private AccountManager accountManager;
    public IFriendRelation friendRelationListener;
    public IMsgReceiveListener msgReceiveListener;
    private final Handler myHandler = new Handler(MedicalApplication.getContext().getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_LOGIN_RETRY:
                    CustomLog.d(TAG, "MSG_LOGIN_RETRY  login99_time " + login99_time);
                    if (login99_time < LOGIN_RETRY_TIME) {
                        login99_time++;
                        backgroundLogin();
                    } else {
                        CustomLog.d(TAG, "重复登录已到最大次数 retrytime is " + login99_time);
                        CustomToast.show(MedicalApplication.getContext(),mContext.getString(R.string.new_init_fail_close_app_try_again),2);
                        myHandler.removeMessages(MSG_LOGIN_RETRY);
                    }
                    break;
                default:
            }
        }
    };


    private AppP2PAgentManager() {
        CustomLog.d(TAG, "AppP2PAgentManager 构造函数调用");
        login99_time = 0;
        filetaskMgr = MedicalApplication.getFileTaskManager();
        accountManager = AccountManager.getInstance(MedicalApplication.getContext());
        innerclient = CommonButelConnSDKAPI_V2_4.CreateCommonButelConn(mContext, this);
        CustomLog.d(TAG, "curr threadId is " + android.os.Process.myTid());
        if(innerclient != null){
            groupclient = innerclient.getGroupConn(this);
        }
        if(netReceiver == null){
            initReceiver();
        }
        imLoginStatus = 0;
    }

    public void connectIMService(){
        CustomLog.d(TAG,"conncet im service");
        if (innerclient != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int initResult = innerclient.Init("");
                    if (initResult == 0) {
                        CustomLog.d(TAG, "init success");
                    } else if (initResult == -2) {
                        CustomLog.d(TAG, "已经初始化过了,直接登录");
                        Login();
                        return;
                    } else {
                        CustomLog.d(TAG, "init failed");
                        IMConstant.isP2PConnect = false;
                        imLoginStatus = 0;
                        sendUpdateSIPBroadcast();
                        return;
                    }
                }
            }).start();
        }
    }


    public static AppP2PAgentManager getInstance() {
        CustomLog.d(TAG, "getInstance");
        if (p2pAgentMgr == null || innerclient == null) {
            CustomLog.d(TAG, "p2pAgentMgr is null");
            p2pAgentMgr = null;
            innerclient = null;
            p2pAgentMgr = new AppP2PAgentManager();
        }
        return p2pAgentMgr;
    }


    public static void init() {
        CustomLog.i(TAG, "AppP2PAgentManager init");
        login99_time = 0;
        getInstance();
        CustomLog.d(TAG, "curr threadId is " + android.os.Process.myTid());
    }


    public int Login() {
        CustomLog.d(TAG, "IM Login ");
        if (innerclient != null) {
            AuthenticateInfo userInfo = AccountManager.getInstance(mContext).getmAuthInfo();
            String appkey = SettingData.getInstance().AppKey;//"88c508a39e9547cca29bd4bc9ce4589c";
            String token = userInfo.getAccessToken();
            String nubeNum = userInfo.getNubeNumber();
            String nickName = userInfo.getNickname();
            CustomLog.d(TAG, "P2P Login token:" + token + " nube:" + nubeNum + " nickName:" + nickName + " appkey" + appkey);
            if(TextUtils.isEmpty(appkey) || TextUtils.isEmpty(token) || TextUtils.isEmpty(nubeNum)
                    ){
                CustomLog.d(TAG,"IM 登录参数为空");
                sendUpdateSIPBroadcast();
                return DEFAULT_ERROR_CODE;
            }
            int code = innerclient.LoginWithToken(appkey, token, nubeNum, nickName, nubeNum);
            CustomLog.d(TAG, "curr threadId is " + android.os.Process.myTid());
            if (code == 0) {
                imLoginStatus = 2;
                CustomLog.d(TAG, "Login success");
            } else {
                CustomLog.d(TAG, "Login failed");
                imLoginStatus = 1;
                myHandler.removeMessages(MSG_LOGIN_RETRY);
                myHandler.sendEmptyMessageDelayed(MSG_LOGIN_RETRY, 1000);
            }
            CustomLog.d(TAG, "Login return Code = " + code);
            return code;
        }
        return DEFAULT_ERROR_CODE;

    }


    @Override
    public void OnInit(int nReason) {
        CustomLog.d(TAG, "OnInit nReason:" + nReason);
        if (0 == nReason) {
            backgroundLogin();
        } else {
            CustomLog.d(TAG, "onInit failed");
            IMConstant.isP2PConnect = false;
            imLoginStatus = 0;
            sendUpdateSIPBroadcast();
        }
    }

    public void backgroundLogin(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomLog.d(TAG, "AppP2PAgentManager 开启线程进行login()");
                CustomLog.d(TAG, "curr threadId is " + android.os.Process.myTid());
                Login();
            }
        }).start();
    }


    @Override
    public void OnUninit(int i) {

    }


    @Override
    public void OnRegister(int i, String s) {

    }


    @Override
    public void OnUnregister(int i) {

    }


    @Override
    public void OnLogin(int nReason) {
    }


    @Override
    public void OnLoginWithToken(int nReason, String token) {
        CustomLog.d(TAG, "OnLoginWithToken nReason is:" + nReason + " token:" + token);
        if (0 == nReason) {
            CustomLog.d(TAG, "OnLoginWithToken success,nReason:" + nReason + "token:" + token);
            IMConstant.isP2PConnect = true;
            login99_time = 0;
            MedicalApplication.getPreference().setKeyValue(
                    PrefType.KEY_FOR_SIP_REG_OK, "true");
            MessageReceiveAsyncTask.FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = true;
            sendUpdateSIPBroadcast();
        } else {
            CustomLog.d(TAG, "OnLoginWithToken failed");
            IMConstant.isP2PConnect = false;
            MessageReceiveAsyncTask.FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = true;
            sendUpdateSIPBroadcast();
            if (-2020 == nReason) {
                CustomLog.d(TAG, "OnLoginWithToken 收到被迫下线");
                myHandler.removeMessages(MSG_LOGIN_RETRY);
                if(accountManager.getLoginState() == AccountManager.LoginState.OFFLINE){
                    CustomLog.d(TAG,"应用不在登录中，不做IM回调处理");
                    return;
                }
                AccountManager.getInstance(mContext).showForceOfflineDialog();
                return;
            } else if (-2114 == nReason) {
                CustomLog.d(TAG, "token 无效");
                myHandler.removeMessages(MSG_LOGIN_RETRY);
                //-2114im token 无效 ,-907 MDS token 无效
                if(accountManager.getLoginState() == AccountManager.LoginState.OFFLINE){
                    CustomLog.d(TAG,"应用不在登录中，不做IM回调处理");
                    return;
                }
                AccountManager.getInstance(MedicalApplication.getContext()).tokenAuthFail(-907);
                return;
            } else {
                CustomLog.d(TAG, "登录时回调其他错误");
                String loginUserNube = AccountManager.getInstance(mContext).getmAuthInfo().getNubeNumber();
                if(loginUserNube.equals("")){
                    CustomLog.d(TAG,"nubeNumber is null，不重试登录");
                    return;
                }
                imLoginStatus = 1;
                myHandler.removeMessages(MSG_LOGIN_RETRY);
                myHandler.sendEmptyMessageDelayed(MSG_LOGIN_RETRY, 1000);
                return;
            }
        }
    }


    @Override
    public void OnLogout(int nReason) {
        CustomLog.d(TAG, "OnLogout reason = " + nReason);
        if (nReason == 0) {
            CustomLog.d(TAG,"im 退出登录成功");
//            innerclient = null;
            myHandler.removeMessages(MSG_LOGIN_RETRY);
        } else {
            CustomLog.d(TAG,"im 退出登录失败");
            IMConstant.isP2PConnect = false;
            sendUpdateSIPBroadcast();
            if(accountManager.getLoginState() == AccountManager.LoginState.ONLINE){
                CustomToast.show(MedicalApplication.getContext(),mContext.getString(R.string.new_init_fail_close_app_try_again),1);
            }
        }
        mContext.unregisterReceiver(netReceiver);
        netReceiver = null;
    }


    @Override
    public void OnRing(String s) {

    }


    @Override
    public void OnNewcall(String s, String s1, String s2, int i, String s3) {

    }


    @Override
    public void OnNewMonicall(String s, String s1, String s2, int i, String s3) {

    }


    @Override
    public void OnConnect(int i, String s) {

    }


    @Override
    public void OnDisconnect(int i, String s, String s1) {

    }


    @Override
    public void OnMakeCallQueuePos(String s, int i) {

    }


    @Override
    public void OnOccupyingAgentQueuePos(String s, int i) {

    }


    @Override
    public void OnAgentDisconnect(String s, int i, String s1) {

    }


    @Override
    public void OnOccupyingAgent(String s, int i, String s1) {

    }


    @Override
    public void OnEnableCamera(int i, boolean b) {

    }


    @Override
    public void OnRemoteCameraEnabled(boolean b) {

    }


    @Override
    public void OnStartCameraPreview(int i) {

    }


    @Override
    public void OnStopCameraPreview() {

    }


    @Override
    public void OnIM_SendMessage(String msgid, int result, long serverTime) {
        CustomLog.d(TAG,
                "OnIM_SendMessage msgid = " + msgid + " result:" + result + " serverTime:" +
                        serverTime);
        String uuid = msgIdMap.get(msgid);
        boolean succ = result == 0 ? true : false;
        if (!TextUtils.isEmpty(uuid)) {
            filetaskMgr.updateStatusAfterIM(uuid, succ);
            filetaskMgr.updateTime(uuid, serverTime);
            filetaskMgr.removeMap(uuid);
        }
        msgIdMap.remove(msgid);
    }


    @Override
    public void OnIM_SendMessageComb(String msgid, int result, long serverTime) {

        CustomLog.d(TAG,
                "OnIM_SendMessage msgid = " + msgid + " result:" + result + " serverTime:" +
                        serverTime);
        String uuid = msgIdMap.get(msgid);
        boolean succ = result == 0 ? true : false;
        if (!TextUtils.isEmpty(uuid)) {
            filetaskMgr.updateStatusAfterIM(uuid, succ);
            filetaskMgr.updateTime(uuid, serverTime);
            filetaskMgr.removeMap(uuid);
        }
        msgIdMap.remove(msgid);

    }


    @Override
    public void OnIM_NewMsgArrive(String msgType, String title, String sender, String msgId, String text, String thumUrl
            , String nikeName, String sendTime, int durationSec, long serverTime, String appExtendInfo) {

        CustomLog.d(TAG,
                "OnIM_NewMsgArrive,收到新消息,msgType:" + msgType + " title:" + title + " sender:" + sender
                        + " msgId:" + msgId + " text:" + text + " thumUrl:" + thumUrl + " nikeName:" +
                        nikeName + " sendTime:" + sendTime
                        + " durationSec:" + durationSec + " serverTime:" + serverTime + " appExtendInfo" +
                        appExtendInfo);

        if (msgcheckListener != null) {
            msgcheckListener.onFinished();
            msgcheckListener = null;
        }

        if(!isMsgValidMsg(sender,msgType,text)){
            return;
        }

        if(isOldVersionMsg){
            try {
                msgType = "common";
                JSONObject textObj = new JSONObject();
                textObj.put("nickname",nikeName);
                textObj.put("isReplayMsg",FileTaskManager.STRANGER_TYPE_REQUEST);
                textObj.put("text",mContext.getString(R.string.request_add_you_friend));
                textObj.put("subtype","stranger_msg");
                text = textObj.toString();
                CustomLog.d(TAG,"收到oldversion msg " + msgId + text);
            }catch (Exception e){
                CustomLog.d(TAG,"老版本消息，重组错误");
            }
        }
        isOldVersionMsg = false;


        SCIMRecBean bean = new SCIMRecBean();
        bean.msgType = msgType;
        bean.title = title;
        bean.sender = sender;
        bean.msgId = msgId;
        bean.text = text;
        bean.thumUrl = thumUrl;
        bean.nikeName = nikeName;
        bean.sendTime = sendTime;
        bean.groupId = "";
        bean.durationSec = durationSec;
        bean.serverTime = serverTime;
        bean.extJson = appExtendInfo;

        PrivateMessage prvMsg = MessageReceiveAsyncTask.convertSDIMMsg(bean);
        if (prvMsg != null) {
            List<PrivateMessage> msgs = new ArrayList<PrivateMessage>();
            msgs.add(prvMsg);
            MessageReceiveAsyncTask msgRec = new MessageReceiveAsyncTask();
            msgRec.saveSCImMessageThread(msgs);
        }
    }


    @Override
    public void OnIM_Upload(String seqId, String urlJson) {

        CustomLog.d(TAG, "OnIM_Upload seqId = " + seqId + " urlJson:" + urlJson);
        String uuid = msgIdMap.get(seqId);
        List<FileTaskBean> beanlist = filetaskMgr.findFileTasks(uuid);
        if (beanlist != null && beanlist.size() > 0) {
            FileTaskBean bean = beanlist.get(0);
            bean.convertSuccessStringToResultUrl(urlJson);
            filetaskMgr.updateBodybutTaskStatus(uuid);
        }
    }


    @Override
    public void OnSendOnlineNotify(int i, int i1) {

    }


    @Override
    public void OnNewOnlineNotify(String s, String s1) {

    }


    @Override
    public void OnNewPermitUserCall(String s, String s1, int i) {

    }


    @Override
    public void OnNewUnPermitUserCall(String s, String s1, int i) {

    }


    @Override
    public void OnCdrNotify(String s) {

    }


    @Override
    public void OnIM_UpLoadFileProcess(String msgid, int percent) {
        CustomLog.d(TAG, "OnIM_UpLoadFileProcess msgid = " + msgid + " percent:" + percent);
        String uuid = msgIdMap.get(msgid);
        List<FileTaskBean> beanlist = filetaskMgr.findFileTasks(uuid);
        if (beanlist != null && beanlist.size() > 0) {
            beanlist.get(0).setCurrentSCIM(percent);
        }
    }


    @Override
    public void OnRemoteRotate(int i) {

    }


    @Override
    public void OnSendShortMsg(int i, int i1) {

    }


    @Override
    public void OnNewShortMsgArrive(String s, String s1) {

    }


    @Override
    public void OnSDKDebugInfo(String s) {

    }


    @Override
    public void OnUploadLog(int i) {

    }


    @Override
    public void OnFirstIFrameArrive() {

    }


    @Override
    public void X1AlarmNotify(String s) {

    }


    @Override
    public void OnSetExclusiveQueue(int i) {

    }


    @Override
    public void OnRedirectCall(int i) {

    }


    @Override
    public void OnForceDetectBW(int i, int i1) {
        CustomLog.d(TAG, "force exit");
    }


    @Override
    public void OnRedirectCallProcessing(String s) {

    }


    @Override
    public void OnMakecallEnd() {

    }


    @Override
    public void OnNetQosNotify(int i) {

    }


    @Override
    public void OnDetectDevice(String s) {

    }


    @Override
    public void OnUpDownNetQosNotify(int i, int i1, String s) {

    }

    /**消息标记已读
     * @param reason    成功与否=0成功
     * @param seqno     异步消息队列号
     */
    @Override
    public void onMarkMsgRead(int reason, int seqno) {
        if(reason == 0){
            CustomLog.d(TAG,"标记消息已读成功,seqno:" + seqno);
        }else{
            CustomLog.d(TAG,"标记消息已读失败,seqno:" + seqno);
        }

    }

    @Override
    public void onGetHistoryMsg(int i, int i1, String s) {

    }

    @Override
    public void OnSDKAbnormal() {

    }


    @Override
    public void OnGroupOperateCallBack(int reason, int subOperateId, String cbJson, int seqId) {
        CustomLog.d(TAG, "OnGroupOperateCallBack begin");
        CustomLog.d(TAG, "OnGroupOperateCallBack return reason = " + reason + " subOperateId:" + subOperateId  +
                " cbJson:" + cbJson + " seqId:" +  seqId);
        String interfacename = "";
        switch (subOperateId) {
            case GROUP_MESSAGE_FUNCT_CREATE:
                interfacename = UrlConstant.METHOD_CREATE_GROUP;
                break;
            case GROUP_MESSAGE_FUNCT_UPDATE:
                interfacename = UrlConstant.METHOD_EDIT_GROUP;
                break;
            case GROUP_MESSAGE_FUNCT_ADDUSER:
                interfacename = UrlConstant.METHOD_ADD_USERS;
                break;
            case GROUP_MESSAGE_FUNCT_DELUSER:
                interfacename = UrlConstant.METHOD_DEL_USERS;
                break;
            case GROUP_MESSAGE_FUNCT_QUIT:
                interfacename = UrlConstant.METHOD_QUITE_GROUP;
                break;
            case GROUP_MESSAGE_FUNCT_DELETE:
                interfacename = UrlConstant.METHOD_DEL_GROUP;
                break;
            case GROUP_MESSAGE_FUNCT_QUERY:
                interfacename = UrlConstant.METHOD_QUERY_GROUP_DETAIL;
                break;
            case GROUP_MESSAGE_FUNCT_GETALL:
                interfacename = UrlConstant.METHOD_GET_ALL_GROUP;
                break;
            default:
                break;
        }

        if (!mGroupInterfaceBean.containsKey(seqId + "")) {
            CustomLog.d(TAG, "缓存中无seqId=" + seqId + "的记录");
        } else {
            CustomLog.d(TAG, "缓存中有seqId=" + seqId + "的记录,处理回调");
            GroupInterfaceBean bean = mGroupInterfaceBean.get(seqId + "");
            mGroupInterfaceBean.remove(seqId + "");
            new GroupChatInterfaceManager(mContext).resultParse(reason, cbJson, interfacename,
                    bean.getGroupId(), bean.getGroupName(), bean.getGroupListener(),
                    bean.getGroupQuitType());
        }

        if (!TextUtils.isEmpty(cbJson)) {
            MessageGroupEventParse parse = sid2eventParseMap.get(seqId + "");
            if (parse != null) {
                if (reason != 0) {
                    parse.createEmptyGroup();
                }
                parse.parseMessage();
            }
            sid2eventParseMap.remove(seqId + "");
        }

        CustomLog.d(TAG, "OnGroupOperateCallBack end ");
    }


    @Override
    public void OnNewGroupEventNotify(int subEventId, String eventJson, int seqId) {
        CustomLog.d(TAG, "OnNewGroupEventNotify begin");
        CustomLog.d(TAG, "subEventId : " + subEventId + " eventJson:" + eventJson + " seqId :" + seqId);
        if (msgcheckListener != null) {
            msgcheckListener.onFinished();
            msgcheckListener = null;
        }

        switch (subEventId) {
            case GROUP_MESSAGE_EVENT_CREATE:
            case GROUP_MESSAGE_EVENT_UPDATE:
            case GROUP_MESSAGE_EVENT_ADDUSER:
            case GROUP_MESSAGE_EVENT_DELUSER:
            case GROUP_MESSAGE_EVENT_QUIT:

                PrivateMessage prvMsg = MessageReceiveAsyncTask.convertSDIMMsg4GroupEvent(
                        eventJson);
                if (prvMsg != null) {
                    List<PrivateMessage> msgs = new ArrayList<PrivateMessage>();
                    msgs.add(prvMsg);
                    MessageReceiveAsyncTask msgRec = new MessageReceiveAsyncTask();
                    msgRec.saveSCImMessageThread(msgs);
                }
                break;
            case GROUP_MESSAGE_EVENT_DELETE:
                // do nothing
                break;
            default:
                // do nothing
                break;
        }
        CustomLog.d(TAG, "OnNewGroupEventNotify end");
    }


    @Override
    public void OnGroupSendMsg(String msgId, long serverTime, int reason) {
        CustomLog.d(TAG,
                "OnGroupSendMsg msgid = " + msgId + " reason:" + reason + " serverTime:" + serverTime);
        String uuid = msgIdMap.get(msgId);
        boolean succ = reason == 0 ? true : false;
        if (!TextUtils.isEmpty(uuid)) {
            filetaskMgr.updateStatusAfterIM(uuid, succ);
            filetaskMgr.updateTime(uuid, serverTime);
            filetaskMgr.removeMap(uuid);
            //更新会议室内信息
            if(msgResultInterface != null){
                msgResultInterface.onFinalResult(succ,uuid);
            }
        }
        msgIdMap.remove(msgId);
    }


    @Override
    public void OnGroupNewMsgArrive(String msgType, String sender,
                                    String msgId, String text, String thumUrl, String nikeName,
                                    String sendTime, int durationSec, String groupId, long serverTime, String appExtendInfo) {

        CustomLog.d(TAG, "OnGroupNewMsgArrive,收到群消息,msgType:" + msgType + " sender:" + sender
                + " msgId:" + msgId + " text:" + text + " thumUrl:" + thumUrl + " nikeName:" +
                nikeName + " sendTime:" + sendTime
                + " durationSec:" + durationSec + " groupId:" + groupId + "serverTime:"
                + serverTime + " appExtendInfo:" + appExtendInfo);
        String loginUserNuber = AccountManager.getInstance(mContext).getNube();
        if (loginUserNuber.equals(sender)) {
            CustomLog.d(TAG, "收到自己发送的消息");
            return;
        } else {
            CustomLog.d(TAG, "收到群中其他人发送的消息");
        }

        if (msgcheckListener != null) {
            msgcheckListener.onFinished();
            msgcheckListener = null;
        }

        SCIMRecBean bean = new SCIMRecBean();
        bean.msgType = msgType;
        bean.title = "";
        bean.sender = sender;
        bean.msgId = msgId;
        bean.text = text;
        bean.thumUrl = thumUrl;
        bean.nikeName = nikeName;
        bean.sendTime = sendTime;
        bean.groupId = groupId;
        bean.durationSec = durationSec;
        bean.serverTime = serverTime;
        bean.extJson = appExtendInfo;

        PrivateMessage prvMsg = MessageReceiveAsyncTask.convertSDIMMsg(bean);
        if (prvMsg != null) {
            List<PrivateMessage> msgs = new ArrayList<PrivateMessage>();
            msgs.add(prvMsg);
            MessageReceiveAsyncTask msgRec = new MessageReceiveAsyncTask();
            msgRec.saveSCImMessageThread(msgs);
        }

    }


    @Override
    public void OnGroupSendMsgComb(String msgId, long serverTime, int reason) {
        CustomLog.d(TAG,
                "OnGroupSendMsgComb msgid = " + msgId + " reason:" + reason + " serverTime:" +
                        serverTime);
        String uuid = msgIdMap.get(msgId);
        boolean succ = reason == 0 ? true : false;
        if (!TextUtils.isEmpty(uuid)) {
            filetaskMgr.updateStatusAfterIM(uuid, succ);
            filetaskMgr.updateTime(uuid, serverTime);
            filetaskMgr.removeMap(uuid);
        }
        msgIdMap.remove(msgId);
    }


    public IGroupButelConn_V2_4 getGroupButelP2PAgent() {
        CustomLog.d(TAG, "getGroupButelP2PAgent");
        if (groupclient != null) {
            return groupclient;
        } else if (innerclient != null) {
            groupclient = innerclient.getGroupConn(this);
            return groupclient;
        }
        return null;
    }


    public void setGroupInterfaceBean(int seqId, GroupInterfaceBean bean) {
        CustomLog.d(TAG,
                "seqId=" + seqId + "|gid=" + bean.getGroupId() + "|gname=" + bean.getGroupName() +
                        "|gQuitType=" + bean.getGroupQuitType());
        if (!mGroupInterfaceBean.containsKey(seqId + "")) {
            mGroupInterfaceBean.put(seqId + "", bean);
        } else {
            CustomLog.d(TAG, "重复的seqId=" + seqId);
        }
    }


    public void setGroupEventParse(String sid, MessageGroupEventParse eventParse) {
        sid2eventParseMap.put(sid, eventParse);
    }

//    private Handler repeatHandle = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            final SCIMBean bean = (SCIMBean)msg.obj;
//            if(msg.what == 1){
//                new Thread(){
//                    @Override
//                    public void run() {
//                        sendIMMessage(bean);
//                    }
//                }.start();
//            }
//        }
//    };

    /**
     * 发送消息（含点对点消息 和 群聊消息）
     *
     * @param bean 消息发送的内容，根据内容区分是点对点消息，还是群聊消息
     * @return true:成功调用SDK发送接口     false:没有调用 或 调用接口失败
     */
    public boolean sendIMMessage(SCIMBean bean) {
        if (!IMConstant.isP2PConnect){
            filetaskMgr.updateStatusAfterIM(bean.uuid, false);
            filetaskMgr.removeMap(bean.uuid);
            CustomLog.d(TAG,"im 未连接上");
            if(msgResultInterface != null){
                msgResultInterface.onFailed(bean.uuid);
            }
            return false;
        }

//        Message beanMsg = repeatHandle.obtainMessage();
//        beanMsg.obj = bean;
//        beanMsg.what = 1;
//        repeatHandle.sendMessageDelayed(beanMsg,100);

        if (bean != null && innerclient != null) {
            CustomLog.d(TAG,
                    "开始发送消息,msgType:" + bean.msgType + " title:" + bean.title + " recevie:" + Arrays.toString(bean.recvs) + " recvsLen:" + bean.recvsLen
                            + " text:" + bean.text + " thumUrl:" + bean.thumUrl + " filePath:" + bean.filePath
                            + " upLoadFileTImeOutSec:" + bean.upLoadFilTimeOutSec
                            + " durationSec:" + bean.durationSec + " extjson:" + bean.extJson);
            String result = "";
            if (!bean.isGroupMsg) {
                if (!TextUtils.isEmpty(bean.thumUrl)) {
                    result = innerclient.IM_SendMessage(bean.msgType,
                            bean.title, bean.recvs,
                            bean.recvsLen, bean.text,
                            bean.thumUrl, bean.durationSec, bean.extJson);
                } else {
                    if (!TextUtils.isEmpty(bean.filePath)) {
                        result = innerclient.IM_SendMessageComb(bean.msgType,
                                bean.title, bean.recvs, bean.recvsLen,
                                bean.text, bean.filePath,
                                bean.upLoadFilTimeOutSec,
                                bean.durationSec, bean.extJson);
                    } else {
                        result = innerclient.IM_SendMessage(bean.msgType,
                                bean.title, bean.recvs,
                                bean.recvsLen, bean.text,
                                bean.thumUrl, bean.durationSec, bean.extJson);
                    }
                }
            } else {
                if (!TextUtils.isEmpty(bean.thumUrl)) {
                    result = groupclient.GroupSendMsg(bean.msgType,
                            bean.groupId, bean.text, bean.thumUrl,
                            bean.durationSec, bean.extJson);
                } else {
                    if (!TextUtils.isEmpty(bean.filePath)) {
                        result = groupclient.GroupSendMsgComb(bean.msgType,
                                bean.groupId, bean.text, bean.filePath,
                                bean.upLoadFilTimeOutSec, bean.durationSec, bean.extJson);
                    } else {
                        result = groupclient.GroupSendMsg(bean.msgType,
                                bean.groupId, bean.text, bean.thumUrl,
                                bean.durationSec, bean.extJson);
                    }
                }
            }




            CustomLog.d(TAG, "SendIMMessage return result = " + result);

            if (!TextUtils.isEmpty(result)) {
                if (result.equals("3")) {
                    CustomLog.e(TAG, "SendIMMessage return failed,网络异常" + bean.text);
                    if (!TextUtils.isEmpty(bean.uuid)) {
                        filetaskMgr.updateStatusAfterIM(bean.uuid, false);
                        filetaskMgr.removeMap(bean.uuid);
                        CustomToast.show(MedicalApplication.getContext(),mContext.getString(R.string.net_error_check_internet),1);
                        return false;
                    }

                    if(msgResultInterface != null){
                        msgResultInterface.onFailed(bean.uuid);
                    }

                } else {
                    String msgId = result;
                    if (!TextUtils.isEmpty(bean.uuid)) {
                        msgIdMap.put(msgId, bean.uuid);
                    }
                    if(msgResultInterface != null){
                        msgResultInterface.onSuccess(bean.uuid);
                    }
                    return true;
                }
            } else {
                CustomLog.e(TAG,"发送消息的result为空");
                if (!TextUtils.isEmpty(bean.uuid)) {
                    filetaskMgr.updateStatusAfterIM(bean.uuid, false);
                    filetaskMgr.removeMap(bean.uuid);
                }

                if(msgResultInterface != null){
                    msgResultInterface.onFailed(bean.uuid);
                }
            }
        }
        return false;
    }

    //标记消息为已读
    public void markMsgRead(HashMap<String,String> unReadMsgMap){
        for(Map.Entry<String, String> entry : unReadMsgMap.entrySet()){
//            CustomLog.d(TAG,"un read msg map key:" + entry.getKey() + " value" + entry.getValue());
            String [] valueArray = entry.getValue().split(",");
            String result = innerclient.markMsgRead(valueArray,entry.getKey());
            CustomLog.d(TAG,"markMsgRead return:" + result);
        }
    }

    public void markMsgReadOne(String serviceId,String msgIds){
        String [] valueArray = msgIds.split(",");
        String result = innerclient.markMsgRead(valueArray,serviceId);
        CustomLog.d(TAG,"markMsgReadOne return:" + result);
    }


    /**
     * 更新sip状态的广播
     */
    public static final String updatesip = "UPDATESIP";


    private static void sendUpdateSIPBroadcast() {
        CustomLog.d(TAG, "sendUpdateSIPBroadcast bgein");
        Intent intent = new Intent(updatesip);
        MedicalApplication.getContext().sendBroadcast(intent);
        CustomLog.d(TAG, "sendUpdateSIPBroadcast end");
    }

    //不释放对象，用于在重新登录或忘记密码登录时，可以调用对象
    public static void destroyAgent(){
        CustomLog.d(TAG, "destroyAgent 被调用");
        if (innerclient == null) {
            CustomLog.d(TAG, "innerclient 为null");
            return;
        }
        //没有连接成功，不需要调用logout
        if(IMConstant.isP2PConnect) {
            int logoutResult = innerclient.Logout();
            CustomLog.d(TAG, "logoutResut:" + logoutResult);
        }
        p2pAgentMgr = null;
        IMConstant.isP2PConnect = false;
        MessageReceiveAsyncTask.FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = true;
        sendUpdateSIPBroadcast();
    }

    public void setIFriendRelationListener(IFriendRelation listener){
        this.friendRelationListener = listener;
    }

    public void removeFriendRelationListener(){
        this.friendRelationListener = null;
    }


    private boolean isFriend(String targetNubeNumber){
        int relationCode =  FriendsManager.getInstance().getFriendRelationByNubeNumber(targetNubeNumber);
            if(relationCode != FriendsManager.RELATION_TYPE_BOTH){
                return false;
            }
            return true;
    }

    private boolean isMsgValidMsg(String sender,String msgType,String msgContent){
        isOldVersionMsg = false;

        if(TextUtils.isEmpty(sender)){
            CustomLog.e(TAG,"收到异常消息，不做处理");
            return false;
        }

        if(sender.equals("10000") || sender.equals(SettingData.getInstance().adminNubeNum)){
            CustomLog.d(TAG,sender + "是系统账号，认为是好友");
            return true;
        }

        if(ContactManager.getInstance(mContext).checkNubeIsCustomService(sender)){
            CustomLog.d(TAG,sender + "是客服账号，可以直接发送消息");
            return true;
        }

        if(isFriend(sender)){
            if(msgType.equals("common")){
                try {
                    JSONObject commonObject = new JSONObject(msgContent);
                    //是好友关系，可以收到删除好友消息
                    if(commonObject.optString("subtype").equals(BizConstant.MSG_SUB_TYPE_STRANGER)
                            || commonObject.optString("subtype").equals(BizConstant.MSG_SUB_TYPE_ADDFRIEND)){
                        CustomLog.w(TAG,sender + "是好友,但发送了陌生人消息");
                        return false;
                    }else{
                        return true;
                    }
                }catch (Exception e){
                    CustomLog.e(TAG,"判断好友关系，解析text出错");
                    return false;
                }
            }else{
                return true;
            }
        }else{
            if(msgType.equals("common")){
                try {
                    JSONObject commonObject = new JSONObject(msgContent);
                    if(commonObject.optString("subtype").equals(BizConstant.MSG_SUB_TYPE_CHATRECORD)
                            || commonObject.optString("subtype").equals(BizConstant.MSG_SUB_TYPE_ARTICLE)
                    || commonObject.optString("subtype").equals(BizConstant.MSG_SUB_TYPE_MEETING)
                            ||commonObject.optString("subtype").equals(BizConstant.MSG_SUB_TYPE_MEETING_BOOK))
                    {
                        isOldVersionMsg = true;
                        CustomLog.w(TAG,sender + "commcon消息，不是好友,但发送了好友消息");
                        return true;
                    }else {
                        CustomLog.w(TAG,sender + "commcon消息，不是好友,收到陌生人消息");
                        return true;
                    }

                }catch (Exception e){
                    CustomLog.e(TAG,"判断好友关系，解析text出错");
                    return false;
                }
            }else {
                isOldVersionMsg = true;
                CustomLog.w(TAG,sender + "不是好友,但发送了好友消息");
                return true;
            }
        }
    }

    public void setImMsgResultListener(ImMsgResultInterface imMsgResultInterface){
        msgResultInterface = imMsgResultInterface;
    }

    public void removeImMsgResultListener(){
        msgResultInterface = null;
    }



    public interface ImMsgResultInterface{
        public void onSuccess(String uuid);
        public void onFailed(String uuid);
        public void onFinalResult(boolean isSuccess, String uuid);  //消息发送后，收到回调信息回调
    }

    public void setImMsgReceiveListener(IMsgReceiveListener listener){
        this.msgReceiveListener = listener;
    }

    public void removeImMsgReceiveListener(){
        this.msgReceiveListener = null;
    }

    private void initReceiver(){
        netReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                判断当前网络状态
                if(intent.getIntExtra(NetWorkChangeReceiver.NET_TYPE,0)==0){
                    CustomLog.d(TAG,"网络断开");
                }else {
                    CustomLog.d(TAG,"网络连接,IM当前状态:" + imLoginStatus);
                    if(imLoginStatus == 0){
                        connectIMService();
                    }else if(imLoginStatus == 1){
                        //正在登陆中，将重置次数归0  重试结束后，重新开始重新登录
                        if(login99_time < LOGIN_RETRY_TIME){
                            CustomLog.d(TAG,"登录正在重试，修改重试次数");
                            login99_time = 0;
                        }else{
                            CustomLog.d(TAG,"登录重试次数结束，重新尝试登录");
                            login99_time = 0;
                            backgroundLogin();
                        }
                    }else {
                        CustomLog.d(TAG,"已登录成功,不做处理");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(NetWorkChangeReceiver.NET_CHANGE);
        mContext.registerReceiver(netReceiver,filter);
    }

}
