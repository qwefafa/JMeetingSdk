package cn.redcdn.incoming;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.dep.MeetingHostAgentJNI;
import cn.redcdn.jmeetingsdk.config.SettingData;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.interfaces.HostAgentOperation;
import cn.redcdn.network.udp.UDPProcessor;
import cn.redcdn.network.udp.UDPReceiver;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HostAgent implements UDPReceiver, HostAgentOperation {
    private String tag = HostAgent.class.getName();

    public static final String INVITE_MESSAGE_BROADCAST = "cn.redcdn.incoming.InviteMessage.comein.medical";
    public static final String FORCE_OFFLINE_MESSAGE_BROADCAST = "cn.redcdn.incoming.ForceOffline";
    public static final String MEETING_ID = "meeingID";
    public static final String INVITER_ACCOUNT_ID = "inviterAccountID";
    public static final String INVITER_ACCOUNT_NAME = "inviterAccountName";
    public static final String INVITER_HEADURL = "headUrl";

    public static final String ACCOUNT_ID = "accountID";
    public static final String TOKEN = "token";
    public static final String PHONE = "phone";

    private final int SHAREINFO_CMD = 3000;
    private final int SHAREINFO_CMD_RESP = 3001;
    private final int INVITE_USER_CMD = 3004; // 邀请命令
    private final int INVITE_USER_CMD_RESP = 3005; // 邀请回应命令
    private final int BE_INVITED_CMD = 3006; // 被邀请命令
    private final int REGISTER_LOGIN_INFO_RESP = 3008; // 注册登录信息回应
    private final int FORCE_OFFLINE = 4005; // 被迫下线命令

    private final int GET_DATA = 1;

    private int currentNetworkType = -1;

    private Context context;
    private String token;
    private String accountId;
    private String accountName;
    private UDPProcessor udpProcessor;
    private String localIp = "";


    enum InitState {
        NONE, INIT
    }


    ;

    InitState initState = InitState.NONE;


    @SuppressWarnings("unused")
    private class CMDResponse {
        public int status;
        public int cmdID;
        public int meetingID;
        public String inviterAccountID;
        public String inviterAccountName;
        public String accountID;
        public String token;
        public String phone;
    }


    Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (GET_DATA != msg.what) {
                return;
            }

            if (initState == InitState.NONE) {
                CustomLog
                    .e(tag,
                        "HostAgentMessage handleMessage , check state == State.NONE, return! ");
                return;
            }

            CMDResponse resp = (CMDResponse) msg.obj;
            switch (resp.cmdID) {
                case SHAREINFO_CMD_RESP:
                case INVITE_USER_CMD_RESP:
                case REGISTER_LOGIN_INFO_RESP:
                    CustomLog.i(tag, "receive cmd response from hostAgent");
                    break;

                case BE_INVITED_CMD:
                    //取消掉使用agent唤醒外呼
                    CustomLog.i(tag,
                        "receive BE_INVITED_CMD cmd from hostAgent,inviterAccountID:"
                            + resp.inviterAccountID + ",meetingID:" + resp.meetingID
                            + ",name: " + resp.inviterAccountName);
                    if (InitState.INIT == initState) {
                        // send broadcast to IncomingMessageManage
                        Intent intent = new Intent(INVITE_MESSAGE_BROADCAST);
                        intent.putExtra(MEETING_ID, resp.meetingID);
                        intent.putExtra(INVITER_ACCOUNT_ID, resp.inviterAccountID);
                        intent.putExtra(INVITER_ACCOUNT_NAME, resp.inviterAccountName);
                        context.sendBroadcast(intent);
                    }
                    break;
                case FORCE_OFFLINE:
                    CustomLog
                        .i(tag, "receive FORCE_OFFLINE from hostAgent, accountID:"
                            + resp.accountID + ",token:" + resp.token + ",phone: "
                            + resp.phone);
                    Intent intent = new Intent(FORCE_OFFLINE_MESSAGE_BROADCAST);
                    intent.putExtra(ACCOUNT_ID, resp.accountID);
                    intent.putExtra(PHONE, resp.phone);
                    intent.putExtra(TOKEN, resp.token);
                    context.sendBroadcast(intent);
                    break;
            }
        }
    };


    private HostAgent() {
    }


    private static HostAgent mInstance;


    public static synchronized HostAgent getInstance() {
        if (mInstance == null) {
            mInstance = new HostAgent();
        }

        return mInstance;
    }


    public int init(Context appContext, String token, String accountId, String accountName) {
        CustomLog.i(tag, "init inviteMessage");

        if (InitState.INIT == initState) {
            CustomLog.e(tag, "inviteMessage already init");
            return -1;
        }

        context = appContext;
        this.token = token;
        this.accountId = accountId;
        this.accountName = accountName;

        registerNetWorkWatchReceiver();

        initState = InitState.INIT;

        ConnectivityManager connectivityManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null == networkInfo) {
            CustomLog
                .e(tag,
                    "no network default network is currently active and try again acquire parameter after 20s");
            return -1;
        }

        int nettype = networkInfo.getType();

        String activityIp = NetConnectHelper.getLocalIp(context);
        if (activityIp != null && !activityIp.equalsIgnoreCase("")) {
            return initHostAgent(activityIp, nettype);
        } else {
            CustomLog.e(tag, "get ip is null,don't initHostagent");
            return -1;
        }
    }


    private int initHostAgent(String ipaddress, int nettype) {
        udpProcessor = new UDPProcessor(this);
        int udplocalPort = -1;
        udplocalPort = udpProcessor.init(); // 返回监听端口，将监听这个端口的数据
        CustomLog.i(tag, "init inviteMessage. udplocalPort: " + udplocalPort);
        if (udplocalPort < 0) {
            CustomLog.e(tag, "init inviteMessage udp socket failed");
            return udplocalPort;
        }

        int agentServerlocalPort = SettingData.getInstance().HostAgentConfig.LocalPort;

        String agentReceiveUIIp = "127.0.0.1";
        int agentReceiveUIPort = 10001;

        String UIReceiveAgentIp = "127.0.0.1";
        int UIReceiveAgentPort = udplocalPort;

        // 返回目标端口，将往这个端口发送数据

        CustomLog.i(
            tag,
            "HostAgentMessage::Start2 ,启动hostagent。 account: " + accountId
                + " | agentServerlocalip: " + ipaddress
                + " | agentServerlocalPort: " + agentServerlocalPort
                + " | agentReceiveUIIp: " + agentReceiveUIIp
                + " | agentReceiveUIPort: " + agentReceiveUIPort
                + " | UIReceiveAgentIp: " + UIReceiveAgentIp
                + " | UIReceiveAgentPort: " + UIReceiveAgentPort + " | rc_url: "
                + SettingData.getInstance().RC_URL + " | cfgPath: "
                + SettingData.getInstance().CfgPath + " | logfileOutPaht: "
                + SettingData.getInstance().LogFileOutPath + " | token: " + token);

        int ret = MeetingHostAgentJNI.Start2(accountId, ipaddress,
            agentServerlocalPort, agentReceiveUIIp, agentReceiveUIPort,
            UIReceiveAgentIp, UIReceiveAgentPort, SettingData.getInstance().RC_URL,
            SettingData.getInstance().CfgPath,
            SettingData.getInstance().LogFileOutPath, token);

        CustomLog.e(tag, "start meeting hostagent result" + ret);

        if (0 > ret) {
            CustomLog.e(tag, "start meeting hostagent failed:" + ret);
            KeyEventWrite.write(KeyEventConfig.BOOT_HOSTAGENT + "_fail_" + accountId
                + "_startHostAgentFailed-" + ret);
            return -3;
        }
        CustomLog.e(tag, "set dest port :" + ret);
        udpProcessor.setDest(ret);

        localIp = ipaddress;
        currentNetworkType = nettype;
        CustomLog.i(tag, "init inviteMessage finished ");
        KeyEventWrite.write(KeyEventConfig.BOOT_HOSTAGENT + "_ok_" + accountId);
        shareAccountInfo(accountId, accountName);
        return udplocalPort;
    }


    public int shareAccountInfo(String accountID, String accountName) {
        if (accountID == null || accountName == null) {
            CustomLog.e(tag, "参数错误！accountID == null || accountName == null ");
            return -1;
        }

        String localActivityIp = localIp;
        int localActivityPort = SettingData.getInstance().ScreenSharingConfig.CmdPort;
        CustomLog.d(tag, "share account info to dht network,activityIp:"
            + localActivityIp + ",activityPort:" + localActivityPort
            + ",accountID:" + accountID + ",accountName:" + accountName);

        if (InitState.INIT != initState) {
            CustomLog.d(tag, "inviteMessage don't init");
            return -1;
        }

        if (!accountID.equalsIgnoreCase(this.accountId)) {
            CustomLog.e(tag, "视讯号发生变化，需要重新登录进行初始化！");
            return -1;
        }

        this.accountName = accountName;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmdid", SHAREINFO_CMD);
            jsonObject.put("ssip", localActivityIp);
            jsonObject.put("ssport", localActivityPort);
            jsonObject.put("accountid", accountID);
            jsonObject.put("name", accountName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return udpProcessor.send(jsonObject.toString());
    }


    private void registerNetWorkWatchReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mNetWorkWatchReceiver, mFilter);
    }


    private void unRegisterNetWorkWatchReceiver() {
        try {
            context.unregisterReceiver(mNetWorkWatchReceiver);
        } catch (Exception e) {
            CustomLog.e(tag, e.getMessage());
        }
    }


    public void release() {
        CustomLog.i(tag, "release inviteMessage");

        if (InitState.NONE == initState) {
            return;
        }

        unRegisterNetWorkWatchReceiver();
        MeetingHostAgentJNI.Stop();
        context = null;
        udpProcessor.destroy();
        udpProcessor = null;

        initState = InitState.NONE;
        mInstance = null;
    }


    /**
     * 邀请参会
     *
     * @param inviteListID 被邀请人列表
     * @param meetingID 会议ID
     * @param inviterAccountID 邀请者视讯号
     * @param inviterAccountName 邀请者参会名称
     */
    public int invite(List<String> inviteListID, int meetingID,
                      String inviterAccountID, String inviterAccountName) {
        //使用IM通道代替
        return 0;
        // CustomLog.d(tag, "invite other people join meeting, meetingId:" + meetingID
        //     + ", inviterAccountID:" + inviterAccountID + ", inviterAccountName:"
        //     + inviterAccountName);
        //
        // if (InitState.INIT != initState) {
        //     CustomLog.d(tag, "inviteMessage don't init");
        //     return -1;
        // }
        //
        // if (null == inviteListID || 0 == inviteListID.size()
        //     || null == inviterAccountID || null == inviterAccountName) {
        //     CustomLog.d(tag, "invalidate input parameter");
        //     return -2;
        // }
        // //从邀请人员列表中删除自己
        // for (int j = 0; j < inviteListID.size(); j++) {
        //     if (inviteListID.get(j).equals(inviterAccountID)) {
        //         inviteListID.remove(j);
        //         j--;
        //     }
        // }
        // JSONObject jsonObject = new JSONObject();
        // try {
        //     jsonObject.put("cmdid", INVITE_USER_CMD);
        //     jsonObject.put("name", inviterAccountName);
        //     jsonObject.put("accountid", inviterAccountID);
        //     jsonObject.put("meetingid", meetingID);
        //
        //     if (null != inviteListID) {
        //         JSONArray idArray = new JSONArray();
        //         int size = inviteListID.size();
        //         for (int i = 0; i < size; i++) {
        //             idArray.put(inviteListID.get(i));
        //         }
        //         jsonObject.put("accounted", idArray);
        //     }
        // } catch (JSONException e) {
        //     e.printStackTrace();
        // }
        //
        // return udpProcessor.send(jsonObject.toString());
    }


    @Override
    public void process(String buffer) {
        //使用IM通道代替
        return;

        // if (null == buffer || buffer.isEmpty()) {
        //     CustomLog.e(tag, "invalidate raw invite message");
        //     return;
        // }
        //
        // try {
        //
        //     CustomLog.i(tag, "invite message:" + buffer);
        //
        //     JSONObject jsonObj = new JSONObject(buffer);
        //
        //     CMDResponse resp = new CMDResponse();
        //     resp.status = jsonObj.optInt("status");
        //     resp.cmdID = jsonObj.optInt("cmdid");
        //     resp.meetingID = jsonObj.optInt("meetingid");
        //     resp.inviterAccountID = jsonObj.optString("inviteid");
        //     resp.inviterAccountName = jsonObj.optString("name");
        //
        //     // 添加被迫下线数据
        //     resp.accountID = jsonObj.optString("accountid");
        //     resp.phone = jsonObj.optString(PHONE);
        //     resp.token = jsonObj.optString(TOKEN);
        //
        //     Message msg = Message.obtain();
        //     msg.what = GET_DATA;
        //     msg.obj = resp;
        //     messageHandler.sendMessage(msg);
        //
        // } catch (JSONException e) {
        //     CustomLog.e(tag, "invalidate raw invite message! \n" + e.toString());
        //
        // }
    }


    private BroadcastReceiver mNetWorkWatchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            CustomLog.i(tag,
                "HostagentMessage receive broadcast:" + intent.getAction());

            if (initState == InitState.NONE) {
                CustomLog.e(tag, "hostagent don't init ok, ignore CONNECTIVITY_ACTION");
                return;
            }

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra("networkInfo");
                int networkType = networkInfo.getType();
                State netState = networkInfo.getState();
                CustomLog.i(tag, "network type:" + networkInfo.getTypeName()
                    + ",state:" + netState);

                if (State.CONNECTED == netState) {
                    String activityIp = NetConnectHelper.getLocalIp(context);//CommonUtil.getLocalIpAddress(context);
                    if (activityIp == null || activityIp.equalsIgnoreCase("")) {
                        CustomLog.e(tag, "HostAgentMessage getlocal ip is null,return!");
                        return;
                    }

                    CustomLog.i(tag, "old activity ip:" + localIp + ",activity ip:"
                        + activityIp);

                    // 网络类型不一致，需重新初始化hostagent
                    // 网络类型一致,但ip不相同，需重新初始化hostagent
                    if (networkType != currentNetworkType
                        || !activityIp.equalsIgnoreCase(localIp)) {
                        CustomLog.i(tag, "MeetingHostAgentJNI Stop");
                        MeetingHostAgentJNI.Stop();
                        initHostAgent(activityIp, networkType);
                    } else {
                        CustomLog.w(tag, "network setting don't change, ignore");
                    }
                } else {
                    CustomLog.w(tag, "network not connectted");
                }
            }
        }

    };


    @Override
    public int shareAccountInfo(String localActivityIp, int localActivityPort,
                                String accountID, String accountName) {
        // TODO Auto-generated method stub
        return 0;
    }
}
