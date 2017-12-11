package cn.redcdn.hnyd.im.work;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.text.TextUtils;
import android.widget.Toast;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.contacts.NewFriendsActivity;
import cn.redcdn.hnyd.contacts.contact.interfaces.Contact;
import cn.redcdn.hnyd.contacts.contact.manager.ContactManager;
import cn.redcdn.hnyd.im.IMConstant;
import cn.redcdn.hnyd.im.activity.ChatActivity;
import cn.redcdn.hnyd.im.agent.AppGroupManager;
import cn.redcdn.hnyd.im.agent.AppP2PAgentManager;
import cn.redcdn.hnyd.im.asyncTask.NetPhoneAsyncTask;
import cn.redcdn.hnyd.im.bean.ContactFriendBean;
import cn.redcdn.hnyd.im.bean.FriendInfo;
import cn.redcdn.hnyd.im.bean.GroupMemberBean;
import cn.redcdn.hnyd.im.bean.NoticesBean;
import cn.redcdn.hnyd.im.bean.ShowNameUtil;
import cn.redcdn.hnyd.im.dao.GroupDao;
import cn.redcdn.hnyd.im.dao.MedicalDaoImpl;
import cn.redcdn.hnyd.im.dao.NewFriendDao;
import cn.redcdn.hnyd.im.dao.NoticesDao;
import cn.redcdn.hnyd.im.fileTask.FileTaskManager;
import cn.redcdn.hnyd.im.manager.FriendsManager;
import cn.redcdn.hnyd.im.preference.DaoPreference.PrefType;
import cn.redcdn.hnyd.im.util.IMCommonUtil;
import cn.redcdn.hnyd.im.util.smileUtil.Emojicon;
import cn.redcdn.hnyd.im.util.xutils.http.SyncResult;
import cn.redcdn.hnyd.im.work.MessageBaseParse.ExtInfo;
import cn.redcdn.hnyd.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hnyd.util.BadgeUtil;
import cn.redcdn.hnyd.util.DateUtil;
import cn.redcdn.hnyd.util.NotificationUtil;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Desc
 * Created by wangkai on 2017/2/27.
 */

public class MessageReceiveAsyncTask extends NetPhoneAsyncTask<String, SyncResult, Void> {

    private static final String TAG = "MessageReceiveAsyncTask";

    public static String DOWNLOAD_IDLIST = "MessageReceiveAsyncTask.download.idlist";

    private Context context = null;
    private GroupDao groupDao = null;
    private NoticesDao noticesDao = null;
    //    private AlarmMsgDao alarmDao = null;
    //    private DeviceDao devDao = null;
    private MedicalDaoImpl contactsDao = null;
    private NewFriendDao newFriendDao = null;

    private Intent meetingIntent = null;
    private String own = "";
    // 下面两个类变量，在跨进程使用中会出现不一致的现象；
    // 但目前就使用场景来看，到也不会出现大的问题。
    private static boolean isRunning = false;
    // 是否Toast提示异常信息
    private boolean bShowTip = false;
    // 单次消息接收过程回调监听
    private MessageReceiverListener listener = null;

    private Map<String, List<String>> folder_msgs_map = null;
    public static final String KEY_SERVICE_NUBE_INFO = "ServiceNubeInfo";
    // 记录当次消息接收中的发送者
    // 普通消息（视频、图片、声音、文字、名片）的发送者《nube----消息类型+内容简介》
    private Map<String, String> msgsender = new HashMap<String, String>();
    // 好友添加消息（添加好友、同意绘制）的发送者
    //    private Map<String, String> frisender = new HashMap<String, String>();
    // 报警消息
    private String alarmTxt = null;
    // 群组消息（视频、图片、声音、文字、名片）《gid----消息类型+发送者+内容简介》
    private Map<String, String> groupMsgSnippet = new HashMap<String, String>();

    public static boolean FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = true;
    // 下列定义为当前版本能识别的消息类型
    /***
     * private static String TYPE_PIC_1 = BizConstant.MSG_APP_N8_PHOTO + "_" +
     * BizConstant.MSG_BODY_TYPE_PIC; private static String TYPE_PIC_2 =
     * BizConstant.MSG_APP_N8_PHOTO + "_" + BizConstant.MSG_BODY_TYPE_PIC_2;
     * private static String TYPE_VIDEO_1 = BizConstant.MSG_APP_N8_SPHONE + "_"
     * + BizConstant.MSG_BODY_TYPE_VIDEO; private static String TYPE_VIDEO_2 =
     * BizConstant.MSG_APP_N8_SPHONE + "_" + BizConstant.MSG_BODY_TYPE_VIDEO_2;
     * private static String TYPE_AUDIO = BizConstant.MSG_APP_NAME + "_" +
     * BizConstant.MSG_BODY_TYPE_AUDIO; private static String TYPE_TXT =
     * BizConstant.MSG_APP_NAME + "_" + BizConstant.MSG_BODY_TYPE_TXT; private
     * static String TYPE_CARD = BizConstant.MSG_APP_NAME + "_" +
     * BizConstant.MSG_BODY_TYPE_POSTCARD; private static String TYPE_ADDFRI =
     * BizConstant.MSG_APP_N8_CONTACT + "_" + BizConstant.MSG_BODY_TYPE_VCARD;
     * private static String TYPE_FEEDBACK = BizConstant.MSG_APP_N8_CONTACT +
     * "_" + BizConstant.MSG_BODY_TYPE_MULTITRUST; private static String
     * TYPE_OKVISIT = BizConstant.MSG_APP_NAME + "_" +
     * BizConstant.MSG_BODY_TYPE_ONEKEYVISIT; private static String TYPE_MSGRP =
     * BizConstant.MSG_APP_NAME + "_" + BizConstant.MSG_BODY_TYPE_MSGRP; private
     * static String TYPE_HK_IMG = BizConstant.MSG_APP_NAME + "_" +
     * BizConstant.MSG_BODY_TYPE_HK_IMG; // private static String TYPE_IPCALL =
     * // BizConstant.MSG_APP_NAME+"_"+BizConstant.MSG_BODY_TYPE_IPCALL;
     **/
    private static String TYPE_PIC_1 = BizConstant.MSG_BODY_TYPE_PIC;
    private static String TYPE_PIC_2 = BizConstant.MSG_BODY_TYPE_PIC_2;
    private static String TYPE_VIDEO_1 = BizConstant.MSG_BODY_TYPE_VIDEO;
    private static String TYPE_VIDEO_2 = BizConstant.MSG_BODY_TYPE_VIDEO_2;
    private static String TYPE_AUDIO = BizConstant.MSG_BODY_TYPE_AUDIO;
    private static String TYPE_TXT = BizConstant.MSG_BODY_TYPE_TXT;
    private static String TYPE_COMMON = BizConstant.MSG_BODY_TYPE_COMMON;
    private static String TYPE_CARD = BizConstant.MSG_BODY_TYPE_POSTCARD;
    private static String TYPE_ADDFRI = BizConstant.MSG_BODY_TYPE_VCARD;
    private static String TYPE_FEEDBACK = BizConstant.MSG_BODY_TYPE_MULTITRUST;
    private static String TYPE_OKVISIT = BizConstant.MSG_BODY_TYPE_ONEKEYVISIT;
    private static String TYPE_MSGRP = BizConstant.MSG_BODY_TYPE_MSGRP;
    private static String TYPE_HK_IMG = BizConstant.MSG_BODY_TYPE_HK_IMG;
    // private static String TYPE_IPCALL = BizConstant.MSG_BODY_TYPE_IPCALL;

    public static int newMsgCount = 0;

    public static final List<String> MSGTYPES = new ArrayList<String>();

    private PrivateMessage item;
    private static List list = new ArrayList();


    static {
        MSGTYPES.add(TYPE_PIC_1);
        MSGTYPES.add(TYPE_PIC_2);
        MSGTYPES.add(TYPE_VIDEO_1);
        MSGTYPES.add(TYPE_VIDEO_2);
        MSGTYPES.add(TYPE_AUDIO);
        MSGTYPES.add(TYPE_TXT);
        MSGTYPES.add(TYPE_CARD);
        MSGTYPES.add(TYPE_ADDFRI);
        MSGTYPES.add(TYPE_FEEDBACK);
        MSGTYPES.add(TYPE_OKVISIT);
        MSGTYPES.add(TYPE_MSGRP);
        MSGTYPES.add(TYPE_HK_IMG);
        MSGTYPES.add(TYPE_COMMON);
        // MSGTYPES.add(TYPE_IPCALL);
    }


    public static boolean isRunning() {
        return isRunning;
    }


    public void setReceiverListener(MessageReceiverListener listener) {
        this.listener = listener;
    }


    public void setShowErrorTip(boolean show) {
        this.bShowTip = show;
    }


    public MessageReceiveAsyncTask() {
        this.context = MedicalApplication.getContext();
        this.bShowTip = false;
        this.own = AccountManager.getInstance(context).getmAuthInfo().getNubeNumber();
        this.groupDao = new GroupDao(context);
        this.noticesDao = new NoticesDao(context);
        //        this.alarmDao = new AlarmMsgDao(context);
        //        this.devDao = new DeviceDao(context);
        this.contactsDao = new MedicalDaoImpl(context);
        this.newFriendDao = new NewFriendDao(context);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null) {
            listener.onStarted();
        }
    }


    @Override
    protected Void doInBackground(String... arg0) {

        //        isRunning = true;
        //        // 获取消息的简单的索引
        //        String appname = "";
        //        String typename = "";
        //        SyncResult obj = doHttpGetAllMsgsIndex(appname, typename);
        //
        //        // 解析未读的消息索引，并按文件夹归类
        //        List<PrivateMessage> msgdetaillist = parseDetailMsgList(obj);
        //        if (msgdetaillist != null && msgdetaillist.size() > 0) {
        //
        //            // 下面按文件夹分组，方便置阅读消息状态
        //            folder_msgs_map = new HashMap<String, List<String>>();
        //            int indexLength = msgdetaillist.size();
        //            PrivateMessage item = null;
        //            List<String> msgidList = null;
        //            for (int i = 0; i < indexLength; i++) {
        //                item = msgdetaillist.get(i);
        //                if (folder_msgs_map.containsKey(item.folderId)) {
        //                    msgidList = folder_msgs_map.get(item.folderId);
        //                    msgidList.add(item.msgId);
        //                } else {
        //                    msgidList = new ArrayList<String>();
        //                    // TODO:在list的首位记录APP属性
        //                    msgidList.add(0, item.app);
        //                    msgidList.add(item.msgId);
        //                    folder_msgs_map.put(item.folderId, msgidList);
        //                }
        //            }
        //
        //            // 保存消息到本地
        //            doBatchSave(msgdetaillist);
        //            // 内存数据清理
        //            msgdetaillist.clear();
        //            msgdetaillist = null;
        //
        //            // 置消息已读状态
        //            Iterator<Map.Entry<String, List<String>>> it1 = folder_msgs_map
        //                    .entrySet().iterator();
        //            while (it1.hasNext()) {
        //                Map.Entry<String, List<String>> entry = it1.next();
        //                String folderid = entry.getKey();
        //                List<String> msgids = entry.getValue();
        //                /**
        //                 * 这个通知服务器该消息一经从服务端下载过了 1.成功了 就不会再次从服务端下载
        //                 * 2.有可能失败但是失败的时候我也继续讲该消息插入到 NoticeTable 表中 这样的话有可能会出现两条消息
        //                 * 是因为有不同的id TODO 针对动态有可能出现多个消息我应该根据消息id进行排重 TODO 注意将msgids
        //                 * list的首位移除（APP值）
        //                 */
        //                String app = msgids.get(0);
        //                msgids.remove(0);
        //                // 按文件夹和消息id, 置已读状态
        //                @SuppressWarnings("unused")
        //                Object reslut = doHttpSetStatus(app, folderid, msgids);
        //            }
        //            if (folder_msgs_map != null) {
        //                folder_msgs_map.clear();
        //                folder_msgs_map = null;
        //            }
        //
        //            // 推送通知栏消息
        //            sendNotifacation();
        //            msgsender.clear();
        //            frisender.clear();
        //            alarmTxt = null;
        //        }
        return null;
    }


    /**
     * 解决会议外呼弹屏和系统通知出现冲突问题 原因：外呼弹屏时页面拉起，此时客户端判断应用在前端故而不弹系统通知
     * 解决方法：用meetingIntent作为是否存在有会议弹屏消息的标记，在sendNotifacation调用之后，再做会议弹屏的逻辑
     */
    private void handleInviteMeetingCall() {
        if (meetingIntent != null) {
            context.sendBroadcast(meetingIntent);

            String data = meetingIntent.getStringExtra(IMCommonUtil.KEY_BROADCAST_INTENT_DATA);
            CustomLog.d(TAG, "收到邀请会议消息广播:" + data);
            String id = "";
            String name = "";
            String headurl = "";
            String room = "";
            boolean show = false;
            try {
                JSONObject object = new JSONObject(data);
                id = object.optString("inviterId");
                name = object.optString("inviterName");
                headurl = object.optString("inviterHeadUrl");
                room = object.optString("meetingRoom");
                show = object.optBoolean("showMeeting", false);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            MedicalMeetingManage.getInstance().incomingCall(id, name, room, headurl,
                new MedicalMeetingManage.OnIncommingCallListener() {
                    @Override
                    public void onIncommingCall(String arg0, int code) {
                        CustomLog.d(TAG, "onInCommingCall");
                    }
                });

        }
        meetingIntent = null;
    }


    @Override
    protected void onProgressUpdate(SyncResult... values) {
        super.onProgressUpdate(values);
        if (bShowTip) {
            if (values == null || values.length == 0) {
                return;
            }
            SyncResult result = (SyncResult) values[0];
            if (!result.isOK()) {
                String tip = result.getErrorMsg();
                CustomLog.d("", "Toast:" + tip);
                Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        isRunning = false;
        if (folder_msgs_map != null) {
            folder_msgs_map.clear();
            folder_msgs_map = null;
        }
        if (listener != null) {
            listener.onFinished();
        }
    }


    @SuppressWarnings("unused")
    private String getAppNameFromType(String msgtype) {
        if (!TextUtils.isEmpty(msgtype)) {
            int index = msgtype.indexOf('_');
            if (index != -1) {
                return msgtype.substring(0, index);
            } else {
                return msgtype;
            }
        }
        return "";
    }


    private void removeMsgId(String msgId) {

        if (folder_msgs_map != null) {
            Iterator<Map.Entry<String, List<String>>> it1 = folder_msgs_map
                .entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry<String, List<String>> entry = it1.next();
                List<String> msgIds = entry.getValue();
                if (msgIds != null) {
                    msgIds.remove(msgId);
                }
            }
        }

    }


    private void doBatchSave(List<PrivateMessage> msg) {

        if (msg == null || msg.size() == 0) {
            return;
        }

        int length = msg.size();
        item = null;
        NoticesBean repeatData = null;
        msgsender.clear();
        //        frisender.clear();
        alarmTxt = null;

        for (int i = 0; i < length; i++) {

            item = msg.get(i);

            String gid = item.gid;
            boolean isGroupMsg = false;
            String groupPN = MessageGroupEventParse.getGroupPublicNumber();
            // 校验gid的有效性
            if (!TextUtils.isEmpty(gid)) {
                isGroupMsg = true;
                // 是否已存在；若没有则通过接口查询出群组的详情并保存
                MessageGroupEventParse parse = new MessageGroupEventParse(item);
                boolean exit = parse.groupExist(gid);
                if (!exit) {
                    AppGroupManager.getInstance(context)
                        .groupQueryDetailBackgroud(gid, parse);
                    if (groupPN.equals(item.sender)) {
                        continue;
                    }
                } else {
                    // 是否是群事件的公众消息
                    if (groupPN.equals(item.sender)) {
                        parse.parseMessage();
                        continue;
                    }
                }
            }

            // @lihs 2013.12.31 根据消息ID 排重，防止更新服务端消息信息失败的场合多次插入表中
            String id = item.msgId;//.replace("-", "");
            String bodyType = splitBodyType(item.type);

            repeatData = noticesDao.getNoticeById(id);
            if (repeatData != null) {
                CustomLog.d(TAG, "该消息已经存在，不再往NoticesTable表中加入数据，重复消息下载消息ID=" + id);
                continue;
            }

            boolean succ = false;
            if (bodyType.equalsIgnoreCase(TYPE_VIDEO_1)
                || bodyType.equalsIgnoreCase(TYPE_VIDEO_2)) {
                // 分享的视频消息
                MessageVedioParse parse = new MessageVedioParse(item);
                succ = parse.parseMessage();
                if (succ) {
                    String txt = "";
                    ExtInfo extinfo = parse.getExtInfoAfterParse();
                    if (extinfo != null) {
                        txt = getNickName(item.sender) +
                            context.getResources().getString(R.string.sent_a_video);
                    }
                    if (isGroupMsg) {
                        groupMsgSnippet.put(gid, item.online
                            + getGroupMemberName(gid, item.sender)
                            + context.getResources().getString(R.string.sent_a_video));
                    } else {
                        if (!TextUtils.isEmpty(txt)) {
                            msgsender.put(item.sender, item.online + txt);
                        } else {
                            msgsender
                                .put(item.sender,
                                    item.online
                                        + context.getResources().getString(R.string.sent_a_video));
                        }
                    }
                }
            } else if (bodyType.equalsIgnoreCase(TYPE_CARD)) {
                // 分享名片的消息
                String ver = "";
                String tmpName = "";
                if (!TextUtils.isEmpty(item.extendedInfo)) {
                    try {
                        JSONObject obj = new JSONObject(item.extendedInfo);
                        ver = obj.optString("ver");
                        JSONArray bodyArray = new JSONArray(obj.optString("card"));
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            tmpName = bodyObj.optString("name");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        CustomLog.d(TAG, "消息扩展格式不正确");
                        CustomLog.d(TAG, "解析名片信息出错");
                    }
                } else {
                    CustomLog.d(TAG, "消息扩展字段为空");
                }
                MessageCardParse parse = new MessageCardParse(item);
                String txt = "";
                succ = parse.parseMessage();
                if (succ && !TextUtils.isEmpty(ver)) {

                    txt = getNickName(item.sender) + context.getResources().getString(R.string.sent_out) + tmpName + context.getResources().getString(R.string.of_the_business_card);

                    if (isGroupMsg) {
                        groupMsgSnippet.put(gid, item.online
                            + getGroupMemberName(gid, item.sender)
                            + context.getResources().getString(R.string.sent_out) + tmpName + context.getResources().getString(R.string.of_the_business_card));
                    } else {
                        msgsender
                            .put(item.sender,
                                item.online
                                    + txt);
                    }
                }
            } else if (bodyType.equalsIgnoreCase(TYPE_PIC_1)
                || bodyType.equalsIgnoreCase(TYPE_PIC_2)) {
                // 分享图片的消息
                MessagePicParse parse = new MessagePicParse(item);
                succ = parse.parseMessage();
                if (succ) {
                    String txt = "";
                    ExtInfo extinfo = parse.getExtInfoAfterParse();
                    if (extinfo != null) {
                        txt = getNickName(item.sender) +
                            context.getResources().getString(R.string.send_a_picture);
                    }
                    if (isGroupMsg) {
                        groupMsgSnippet.put(gid, item.online
                            + getGroupMemberName(gid, item.sender)
                            + context.getResources().getString(R.string.send_a_picture));
                    } else {
                        if (!TextUtils.isEmpty(txt)) {
                            msgsender.put(item.sender, item.online + txt);
                        } else {
                            msgsender
                                .put(item.sender,
                                    item.online
                                        +
                                        context.getResources().getString(R.string.send_a_picture));
                        }
                    }
                }
            } else if (bodyType.equalsIgnoreCase(TYPE_TXT)) {
                // 文字信息
                String txt = "";
                String subStr = "";
                MessageTextParse parse = new MessageTextParse(item);
                succ = parse.parseMessage();
                if (succ) {
                    ExtInfo extinfo = parse.getExtInfoAfterParse();
                    if (extinfo != null) {
                        txt = extinfo.text;
                        if (txt.length() > 43) {
                            subStr = txt.substring(0, 43) + "...";
                        } else {
                            subStr = txt;
                        }
                        txt = getNickName(item.sender) + subStr;

                    }
                    if (isGroupMsg) {
                        if (txt.contains(IMConstant.SPECIAL_CHAR + "")) {
                            ArrayList<String> result = new ArrayList<String>();
                            result = IMCommonUtil.getDispList(txt);
                            for (int j = 0; j < result.size(); j++) {
                                GroupMemberBean gbean = groupDao
                                    .queryGroupMember(gid, result.get(j));
                                if (gbean != null) {
                                    ShowNameUtil.NameElement element = ShowNameUtil
                                        .getNameElement(gbean.getName(),
                                            gbean.getNickName(),
                                            gbean.getPhoneNum(),
                                            gbean.getNubeNum());
                                    String MName = ShowNameUtil
                                        .getShowName(element);

                                    if (result.get(j).equals(this.own)) {
                                        subStr = context.getString(R.string.group_chat_aite_you);
                                    } else {
                                        subStr = subStr.replace("@" + result.get(j)
                                            + IMConstant.SPECIAL_CHAR, "@"
                                            + MName
                                            + IMConstant.SPECIAL_CHAR);
                                    }
                                    subStr = subStr + "#remind#";
                                }
                            }
                        }
                        groupMsgSnippet.put(gid, item.online
                            + getGroupMemberName(gid, item.sender) + "：" + subStr);
                    } else {
                        msgsender.put(item.sender, item.online +
                            getNickName(item.sender) + "：" + subStr);
                    }
                }
            } else if (bodyType.equalsIgnoreCase(TYPE_COMMON)) {

                ExtInfo extInfo = MessageBaseParse
                    .convertExtInfo(item.extendedInfo);

                String subtype = extInfo != null ? extInfo.subtype : "";

                if (BizConstant.MSG_SUB_TYPE_MEETING.equals(subtype)) {
                    // 会议邀请信息
                    String txt = "";
                    MessageMeetingParse parse = new MessageMeetingParse(item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        if (extinfo != null) {
                            txt = getNickName(item.sender) + context.getResources().getString(R.string.invite_you_to_video_consultation);
                        }
                        if (isGroupMsg) {
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender)
                                + context.getResources().getString(R.string.invite_you_to_video_consultation));
                        } else {
                            msgsender.put(item.sender, item.online + getNickName(item.sender)
                                + context.getResources().getString(R.string.invite_you_to_video_consultation));
                        }

                        // TODO:如果是即时消息，会发送广播调用会议的来电页面

                        Date sysDate = new Date();
                        long sysTime = sysDate.getTime() / 1000;
                        long itemTime = Long.parseLong(item.time) / 1000;

                        //退出应用后，在打开应用，时间间隔小于 30s ,才可以调出外呼
                        if ((sysTime - itemTime) < 30) {

                            CustomLog.d(TAG, "item time is " + item.time);
                            if (IMConstant.isP2PConnect) {
                                meetingIntent = new Intent();
                                meetingIntent
                                    .setAction(BizConstant.JMEETING_INVITE_ACTION);
                                meetingIntent
                                    .putExtra(
                                        IMCommonUtil.KEY_BROADCAST_INTENT_DATA,
                                        extinfo.meetingInfo);
                            }
                        }

                    }
                } else if (BizConstant.MSG_SUB_TYPE_MEETING_BOOK
                    .equals(subtype)) {
                    // 会议预约信息
                    String txt = "";
                    MessageBookMeetingParse parse = new MessageBookMeetingParse(
                        item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        if (extinfo != null) {
                            txt = getNickName(item.sender) + context.getResources().getString(R.string.sent_a_scheduled_appointment_invitation);
                        }
                        if (isGroupMsg) {
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender)
                                + context.getResources().getString(R.string.sent_a_scheduled_appointment_invitation));
                        } else {
                            msgsender.put(item.sender, item.online + getNickName(item.sender)
                                + context.getResources().getString(R.string.sent_a_scheduled_appointment_invitation));
                        }
                    }
                } else if (BizConstant.MSG_SUB_TYPE_FILE.equals(subtype)) {
                    String txt = "";
                    MessageFileParse parse = new MessageFileParse(item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        if (extinfo != null) {
                            txt = context.getString(R.string.file);
                        }
                        if (isGroupMsg) {
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender)
                                + txt);
                        } else {
                            msgsender.put(item.sender, item.online + txt);
                        }
                    }

                } else if (BizConstant.MSG_SUB_TYPE_CHATRECORD.equals(subtype)) {
                    String txt = "";
                    MessageChatRecordParse parse = new MessageChatRecordParse(item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        if (extinfo != null) {
                            txt = getNickName(item.sender) + context.getResources().getString(R.string.sent_a_chat_record);
                        }
                        if (isGroupMsg) {
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender)
                                + context.getString(R.string.send_a_chat_revery));
                        } else {
                            msgsender.put(item.sender, item.online + getNickName(item.sender)
                                + context.getResources().getString(R.string.sent_a_chat_record));
                        }
                    }

                } else if (BizConstant.MSG_SUB_TYPE_ARTICLE.equals(subtype)) {
                    String txt = "";
                    MessageAritcleParse parse = new MessageAritcleParse(item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        String articleTitle = "";
                        try {
                            JSONObject articleObj = new JSONObject(extinfo.articleInfo);
                            articleTitle = articleObj.optString("title");
                        } catch (Exception e) {

                        }

                        if (extinfo != null) {
                            txt = getNickName(item.sender) + context.getString(R.string.colon_article) + articleTitle;
                        }
                        if (isGroupMsg) {
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender)
                                + context.getString(R.string.colon_article) + articleTitle);
                        } else {
                            msgsender.put(item.sender, item.online + getNickName(item.sender)
                                + context.getString(R.string.colon_article) + articleTitle);
                        }
                    }
                } else if (BizConstant.MSG_SUB_TYPE_STRANGER.equals(subtype)) {
                    try {
                        JSONObject titleObj = new JSONObject(item.title);
                        JSONObject extentObj = new JSONObject(item.extendedInfo);
                        CustomLog.d(TAG, "收到" + titleObj.optString("sender") + "陌生人消息");
                        //收到好友验证消息要发送通知
                        if (extentObj.optInt("isReplayMsg") ==
                            FileTaskManager.STRANGER_TYPE_REQUEST) {
                            //兼容ios验证好友时 a,b是好友，a删除b,a再添加b,b出现a请求加你为好友的通知
                            Contact contact = ContactManager.getInstance(context)
                                .getContactInfoByNubeNumber(item.sender);

                            //证明通讯录中没有改人
                            if (contact != null && contact.getContactId() == null) {
                                sendRequestAddFriendNotification(item.sender,
                                    extentObj.optString("nickname"), item.online + context.getString(R.string.request_add_you_friend));
                            } else {
                                CustomLog.d(TAG, item.sender + "在通讯录中存在，不发送通知");
                            }

                        } else {
                            sendRequestAddFriendNotification(item.sender,
                                extentObj.optString("nickname"),
                                item.online + context.getString(R.string.reply) + extentObj.optString("text"));
                        }
                        if (AppP2PAgentManager.getInstance().friendRelationListener != null) {
                            AppP2PAgentManager.getInstance().friendRelationListener
                                .onMsgArrived(titleObj.optString("sender"),
                                    extentObj.optString("nickname"),
                                    extentObj.optString("headurl"),
                                    extentObj.optString("text"),
                                    String.valueOf(System.currentTimeMillis()),
                                    extentObj.optInt("isReplayMsg"));
                        }
                        setFriendAuthMsgRead(extInfo.serverId, item.msgId);
                    } catch (JSONException e) {
                        CustomLog.d(TAG, "MSG_SUB_TYPE_STRANGER" + e.toString());
                    }

                } else if (BizConstant.MSG_SUB_TYPE_ADDFRIEND.equals(subtype)) {
                    try {
                        JSONObject titleObj = new JSONObject(item.title);
                        String sender = titleObj.optString("sender");
                        CustomLog.d(TAG, "收到" + sender + "收到添加好友消息");
                        JSONObject extentObj = new JSONObject(item.extendedInfo);
                        sendAgreeAddFriendNotification(item.sender,
                            extentObj.optString("nickname"),
                            item.online + context.getString(R.string.approved_start_chat));
                        if (AppP2PAgentManager.getInstance().friendRelationListener != null) {
                            AppP2PAgentManager.getInstance().friendRelationListener
                                .onFriendAdded(sender, extentObj.optString("nickname")
                                    , extentObj.optString("headurl"));
                        }
                        FriendInfo friendInfo = FriendsManager.getInstance()
                            .getFriendByNubeNumber(sender);
                        noticesDao.createAddFriendMsgTip(friendInfo.getName(),
                            friendInfo.getNubeNumber());
                        setFriendAuthMsgRead(extInfo.serverId, item.msgId);
                    } catch (Exception e) {
                        CustomLog.d(TAG, "MSG_SUB_TYPE_ADDFRIEND" + e.toString());
                    }

                } else if (BizConstant.MSG_SUB_TYPE_DELETEFRIEND.equals(subtype)) {
                    try {
                        JSONObject titleObj = new JSONObject(item.title);
                        String sender = titleObj.optString("sender");
                        CustomLog.d(TAG, "收到" + sender + "收到删除好友消息");
                        if (AppP2PAgentManager.getInstance().friendRelationListener != null) {
                            AppP2PAgentManager.getInstance().friendRelationListener
                                .onFriendDeleted(sender);
                        }
                        setFriendAuthMsgRead(extInfo.serverId, item.msgId);
                    } catch (Exception e) {
                        CustomLog.d(TAG, "MSG_SUB_TYPE_DELETEFRIEND" + e.toString());
                    }
                } else if (BizConstant.MSG_SUB_TYPE_REMIND_NOTICE.equals(subtype)) {
                    String txt = "";
                    MessageRemindNoticeParse parse = new MessageRemindNoticeParse(item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        if (extinfo != null) {
                            txt = getNickName(item.sender) + context.getString(R.string.group_chat_aite_you);
                        }
                        if (isGroupMsg) {
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender)
                                + context.getString(R.string.group_chat_aite_you) + "#remind#");
                        } else {
                            CustomLog.d(TAG, "MessageAritcleParse：异常消息类型");
                            //                            msgsender.put(item.sender, item.online + getNickName(item.sender)
                            //                                    +"[公告]");
                        }
                    }
                } else if (BizConstant.MSG_SUB_TYPE_REMIND_ONE_NOTICE.equals(subtype)) {
                    String txt = "";
                    String subStr = "";
                    MessageRemindOneNoticeParse parse = new MessageRemindOneNoticeParse(item);
                    succ = parse.parseMessage();
                    if (succ) {
                        ExtInfo extinfo = parse.getExtInfoAfterParse();
                        if (extinfo != null) {
                            txt = extinfo.text;
                            if (txt.length() > 43) {
                                subStr = txt.substring(0, 43) + "...";
                            } else {
                                subStr = txt;
                            }
                            txt = getNickName(item.sender) + "：" + subStr;

                        }
                        if (isGroupMsg) {
                            if (txt.contains(IMConstant.SPECIAL_CHAR + "")) {
                                ArrayList<String> result = new ArrayList<String>();
                                result = IMCommonUtil.getDispList(txt);
                                for (int j = 0; j < result.size(); j++) {
                                    GroupMemberBean gbean = groupDao
                                        .queryGroupMember(gid, result.get(j));
                                    if (gbean != null) {
                                        ShowNameUtil.NameElement element = ShowNameUtil
                                            .getNameElement(gbean.getName(),
                                                gbean.getNickName(),
                                                gbean.getPhoneNum(),
                                                gbean.getNubeNum());
                                        String MName = ShowNameUtil
                                            .getShowName(element);

                                        if (result.get(j).equals(this.own)) {
                                            subStr = context.getString(R.string.group_chat_aite_you);
                                        } else {
                                            subStr = subStr.replace("@" + result.get(j)
                                                + IMConstant.SPECIAL_CHAR, "@"
                                                + MName
                                                + IMConstant.SPECIAL_CHAR);
                                        }
                                        subStr = subStr + "#remind#";
                                    }
                                }
                            }
                            groupMsgSnippet.put(gid, item.online
                                + getGroupMemberName(gid, item.sender) + subStr);
                        } else {
                            CustomLog.d(TAG, "MessageAritcleParse：异常消息类型");
                        }
                    } else {
                        // TODO:其他自定义的 sub type
                    }
                }
            } else if (bodyType.equalsIgnoreCase(TYPE_AUDIO)) {
                // 语音文件接收
                String txt = "";
                MessageAudioParse parse = new MessageAudioParse(item);
                succ = parse.parseMessage();
                if (succ) {

                    txt = getNickName(item.sender) + context.getResources().getString(R.string.sent_a_voice);

                    if (isGroupMsg) {
                        groupMsgSnippet.put(gid, item.online
                            + getGroupMemberName(gid, item.sender)
                            + context.getResources().getString(R.string.sent_a_voice));
                    } else {
                        msgsender
                            .put(item.sender,
                                item.online + getNickName(item.sender)
                                    + context.getResources().getString(R.string.sent_a_voice));
                    }
                }
            } else {
                continue;
            }

            if (succ) {
                // 成功消费该信息,需要置已读状态
            } else {
                // 消费失败，不需要置已读状态
                removeMsgId(item.msgId);
            }
        }
    }


    /**
     * 判断是否是 开启了面打扰设置
     */
    private boolean forbiddenNotify(String id) {
        String forbiddenList = MedicalApplication.getPreference().getKeyValue(
            PrefType.KEY_CHAT_DONT_DISTURB_LIST, "");
        if (forbiddenList.contains(id)) {
            return true;
        }
        return false;
    }


    private boolean appOnTheDesk() {
        boolean bkg1 = isApplicationBroughtToBackground(context);
        boolean scrOn = isScreenOn(context);
        boolean scrlocked = isScreenLocked(context);
        CustomLog.d(TAG, "normal msg BroughtToBackground:" + bkg1 + " | scrOn:"
            + scrOn + " | scrlocked:" + scrlocked);
        // 在消息聊天界面、正在使用引用（不在聊天界面）-->归纳为：应用在前台，仅震动提示
        // 在聊天界面锁屏，（应用仍处于前台），产品经理要求：状态栏通知+声音提醒，故排除
        return !(!bkg1 && scrOn && !scrlocked);
    }


    /**
     * 在线消息，需要响铃
     * 离线消息，只需要响铃一次
     */
    private void doSendNotifyMsg(boolean online, String titleString, String name,
                                 String notifyId, String txt, Intent pdintent) {
        String newTxt = null;
        CustomLog.d(TAG,
            "online=" + online + "|txt=" + txt + "|FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG=" +
                FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG);
        //        getUnicode(txt);
        if (filterEmoji(txt).length() != txt.length()) {
            for (int i = list.size() - 1; i >= 0; i--) {
                int index = (int) list.get(i);
                CustomLog.d(TAG, "index" + index);
                String y = substring(txt, index - 1, index);
                CustomLog.d(TAG, "Y" + y);
                if (!y.equals("")) {
                    String x = Emojicon.getHexResName(y);
                    CustomLog.d(TAG, "表情符名称" + x);
                    String xxx = emojiExchangeName(x);
                    CustomLog.d(TAG, "字符串转换后的名字" + xxx);
                    if (!xxx.equals("")) {
                        newTxt = txt.replace(y, emojiExchangeName(x));
                        CustomLog.d(TAG, "newTxt" + newTxt);
                        txt = newTxt;
                        CustomLog.d(TAG, "::" + txt);
                    }
                }
            }

            list.clear();
        } else {
            newTxt = txt;
        }
        if (online) {
            NotificationUtil.sendNotifacationForSmallIcoMSG(
                titleString, name, notifyId + "", newTxt, pdintent,
                NotificationUtil.NOTIFACATION_STYLE_MSG, true);
        } else {
            if (FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG) {
                NotificationUtil.sendNotifacationForSmallIcoMSG(
                    titleString, name, notifyId + "", newTxt, pdintent,
                    NotificationUtil.NOTIFACATION_STYLE_MSG, true);
                FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = false;
            } else {
                NotificationUtil.sendNotifacationForSmallIcoMSG(
                    titleString, name, notifyId + "", newTxt, pdintent,
                    NotificationUtil.NOTIFACATION_STYLE_MSG, false);
            }
        }
    }


    public static String substring(String source, int start, int end) {
        String result;
        try {
            result = source.substring(source.offsetByCodePoints(0, start),
                source.offsetByCodePoints(0, end));
        } catch (Exception e) {
            result = "";
        }
        return result;
    }


    private String emojiExchangeName(String x) {
        String name = null;
        if (x.equals("emoji_1f60a")) {
            name = context.getString(R.string.emoji_smile);
        } else if (x.equals("emoji_1f60b")) {
            name = context.getString(R.string.emoji_hunger);
        } else if (x.equals("emoji_1f60d")) {
            name = context.getString(R.string.emoji_color);
        } else if (x.equals("emoji_1f60c")) {
            name = context.getString(R.string.emoji_shy);
        } else if (x.equals("emoji_1f60e")) {
            name = context.getString(R.string.emoji_proud);
        } else if (x.equals("emoji_1f60f")) {
            name = context.getString(R.string.emoji_insidious);
        } else if (x.equals("emoji_1f61a")) {
            name = context.getString(R.string.emoji_kissing);
        } else if (x.equals("emoji_1f61d")) {
            name = context.getString(R.string.emoji_naughty);
        } else if (x.equals("emoji_1f61e")) {
            name = context.getString(R.string.emoji_almostcrying);
        } else if (x.equals("emoji_1f61f")) {
            name = context.getString(R.string.emoji_wronged);
        } else if (x.equals("emoji_1f62c")) {
            name = context.getString(R.string.emoji_curse);
        } else if (x.equals("emoji_1f62d")) {
            name = context.getString(R.string.emoji_tears);
        } else if (x.equals("emoji_1f44f")) {
            name = context.getString(R.string.emoji_applaud);
        } else if (x.equals("emoji_1f480")) {
            name = context.getString(R.string.emoji_skeleton);
        } else if (x.equals("emoji_1f528")) {
            name = context.getString(R.string.emoji_beat);
        } else if (x.equals("emoji_1f600")) {
            name = context.getString(R.string.emoji_teeth);
        } else if (x.equals("emoji_1f602")) {
            name = context.getString(R.string.emoji_crying);
        } else if (x.equals("emoji_1f604")) {
            name = context.getString(R.string.emoji_hanxiao);
        } else if (x.equals("emoji_1f605")) {
            name = context.getString(R.string.emoji_awkward);
        } else if (x.equals("emoji_1f612")) {
            name = context.getString(R.string.emoji_despise);
        } else if (x.equals("emoji_1f613")) {
            name = context.getString(R.string.emoji_sweat);
        } else if (x.equals("emoji_1f615")) {
            name = context.getString(R.string.emoji_piezui);
        } else if (x.equals("emoji_1f616")) {
            name = context.getString(R.string.emoji_crazy);
        } else if (x.equals("emoji_1f621")) {
            name = context.getString(R.string.emoji_angry);
        } else if (x.equals("emoji_1f622")) {
            name = context.getString(R.string.emoji_coldsweat);
        } else if (x.equals("emoji_1f625")) {
            name = context.getString(R.string.emoji_daze);
        } else if (x.equals("emoji_1f630")) {
            name = context.getString(R.string.emoji_itsashame);
        } else if (x.equals("emoji_1f632")) {
            name = context.getString(R.string.emoji_surprised);
        } else if (x.equals("emoji_1f633")) {
            name = context.getString(R.string.emoji_frightened);
        } else if (x.equals("emoji_1f634")) {
            name = context.getString(R.string.emoji_sleep);
        } else if (x.equals("emoji_1f635")) {
            name = context.getString(R.string.emoji_yawn);
        } else if (x.equals("emoji_1f637")) {
            name = context.getString(R.string.emoji_toshutup);
        } else if (x.equals("emoji_1f641")) {
            name = context.getString(R.string.emoji_sad);
        } else if (x.equals("emoji_1ff00")) {
            name = context.getString(R.string.emoji_cool);
        } else if (x.equals("emoji_1ff0a")) {
            name = context.getString(R.string.emoji_boo);
        } else if (x.equals("emoji_1ff0b")) {
            name = context.getString(R.string.emoji_gosh);
        } else if (x.equals("emoji_1ff0c")) {
            name = context.getString(R.string.emoji_torture);
        } else if (x.equals("emoji_1ff0d")) {
            name = context.getString(R.string.emoji_decline);
        } else if (x.equals("emoji_1ff0e")) {
            name = context.getString(R.string.emoji_goodbye);
        } else if (x.equals("emoji_1ff0f")) {
            name = context.getString(R.string.emoji_wipesweat);
        } else if (x.equals("emoji_1ff01")) {
            name = context.getString(R.string.emoji_vomit);
        } else if (x.equals("emoji_1ff02")) {
            name = context.getString(R.string.emoji_laughing);
        } else if (x.equals("emoji_1ff03")) {
            name = context.getString(R.string.emoji_lovely);
        } else if (x.equals("emoji_1ff04")) {
            name = context.getString(R.string.emoji_supercilious);
        } else if (x.equals("emoji_1ff05")) {
            name = context.getString(R.string.emoji_arrogant);
        } else if (x.equals("emoji_1ff06")) {
            name = context.getString(R.string.emoji_sleepy);
        } else if (x.equals("emoji_1ff07")) {
            name = context.getString(R.string.emoji_soldier);
        } else if (x.equals("emoji_1ff08")) {
            name = context.getString(R.string.emoji_struggle);
        } else if (x.equals("emoji_1ff09")) {
            name = context.getString(R.string.emoji_doubt);
        } else if (x.equals("emoji_1ff10")) {
            name = context.getString(R.string.emoji_pullthenose);
        } else if (x.equals("emoji_1ff11")) {
            name = context.getString(R.string.emoji_badlaugh);
        } else if (x.equals("emoji_1ff12")) {
            name = context.getString(R.string.emoji_lefthum);
        } else if (x.equals("emoji_1ff13")) {
            name = context.getString(R.string.emoji_righthumhum);
        } else if (x.equals("emoji_1ff14")) {
            name = context.getString(R.string.emoji_scare);
        } else if (x.equals("emoji_1ff15")) {
            name = context.getString(R.string.emoji_poor_pitiful);
        } else if (x.equals("emoji_1f3c0")) {
            name = context.getString(R.string.emoji_basketball);
        } else if (x.equals("emoji_1f3d3")) {
            name = context.getString(R.string.emoji_pingpong);
        } else if (x.equals("emoji_1f4a3")) {
            name = context.getString(R.string.emoji_bomb);
        } else if (x.equals("emoji_1f4a9")) {
            name = context.getString(R.string.emoji_pooh);
        } else if (x.equals("emoji_1f5e1")) {
            name = context.getString(R.string.emoji_knife);
        } else if (x.equals("emoji_1f31e")) {
            name = context.getString(R.string.emoji_sun);
        } else if (x.equals("emoji_1f35a")) {
            name = context.getString(R.string.emoji_rice);
        } else if (x.equals("emoji_1f35c")) {
            name = context.getString(R.string.emoji_noodles);
        } else if (x.equals("emoji_1f37a")) {
            name = context.getString(R.string.emoji_beer);
        } else if (x.equals("emoji_1f41e")) {
            name = context.getString(R.string.emoji_ladybug);
        } else if (x.equals("emoji_1f44c")) {
            name = "[OK]";
        } else if (x.equals("emoji_1f44d")) {
            name = context.getString(R.string.emoji_strong);
        } else if (x.equals("emoji_1f44e")) {
            name = context.getString(R.string.emoji_weak);
        } else if (x.equals("emoji_1f52a")) {
            name = context.getString(R.string.emoji_kitchenknife);
        } else if (x.equals("emoji_1f91d")) {
            name = context.getString(R.string.emoji_shakehands);
        } else if (x.equals("emoji_1f339")) {
            name = context.getString(R.string.emoji_rose);
        } else if (x.equals("emoji_1f349")) {
            name = context.getString(R.string.emoji_watermelon);
        } else if (x.equals("emoji_1f381")) {
            name = context.getString(R.string.emoji_gift);
        } else if (x.equals("emoji_1f382")) {
            name = context.getString(R.string.emoji_cake);
        } else if (x.equals("emoji_1f437")) {
            name = context.getString(R.string.emoji_pighead);
        } else if (x.equals("emoji_1f444")) {
            name = context.getString(R.string.emoji_showlove);
        } else if (x.equals("emoji_1f446")) {
            name = "[NO]";
        } else if (x.equals("emoji_1f494")) {
            name = context.getString(R.string.emoji_heartbreak);
        } else if (x.equals("emoji_1f918")) {
            name = context.getString(R.string.emoji_loveyou);
        } else if (x.equals("emoji_1f940")) {
            name = context.getString(R.string.emoji_withering);
        } else if (x.equals("emoji_1ff1a")) {
            name = context.getString(R.string.emoji_lefttaichi);
        } else if (x.equals("emoji_1ff1b")) {
            name = context.getString(R.string.emoji_righttaichi);
        } else if (x.equals("emoji_1ff1c")) {
            name = context.getString(R.string.emoji_kissing_fly);
        } else if (x.equals("emoji_1ff1d")) {
            name = context.getString(R.string.emoji_jumping);
        } else if (x.equals("emoji_1ff1e")) {
            name = context.getString(R.string.emoji_trembling);
        } else if (x.equals("emoji_1ff1f")) {
            name = context.getString(R.string.emoji_fire);
        } else if (x.equals("emoji_1ff16")) {
            name = context.getString(R.string.emoji_moon);
        } else if (x.equals("emoji_1ff17")) {
            name = context.getString(R.string.emoji_embrace);
        } else if (x.equals("emoji_1ff18")) {
            name = context.getString(R.string.emoji_hiphop);
        } else if (x.equals("emoji_1ff19")) {
            name = context.getString(R.string.emoji_kissed);
        } else if (x.equals("emoji_1ff20")) {
            name = context.getString(R.string.emoji_turnaround);
        } else if (x.equals("emoji_1ff21")) {
            name = context.getString(R.string.emoji_kowtow);
        } else if (x.equals("emoji_1ff22")) {
            name = context.getString(R.string.emoji_back);
        } else if (x.equals("emoji_1ff23")) {
            name = context.getString(R.string.emoji_ropeskipping);
        } else if (x.equals("emoji_1ff24")) {
            name = context.getString(R.string.emoji_waved);
        } else if (x.equals("emoji_1ff25")) {
            name = context.getString(R.string.emoji_excitement);
        } else if (x.equals("emoji_1ff26")) {
            name = context.getString(R.string.emoji_baoquan);
        } else if (x.equals("emoji_1ff27")) {
            name = context.getString(R.string.emoji_seduce);
        } else if (x.equals("emoji_1ff28")) {
            name = context.getString(R.string.emoji_poor);
        } else if (x.equals("emoji_1ff29")) {
            name = context.getString(R.string.emoji_love);
        } else if (x.equals("emoji_26a1")) {
            name = context.getString(R.string.emoji_lightning);
        } else if (x.equals("emoji_26bd")) {
            name = context.getString(R.string.emoji_football);
        } else if (x.equals("emoji_270a")) {
            name = context.getString(R.string.emoji_fist);
        } else if (x.equals("emoji_270c")) {
            name = context.getString(R.string.emoji_victory);
        } else if (x.equals("emoji_2615")) {
            name = context.getString(R.string.emoji_coffee);
        } else if (x.equals("emoji_2764")) {
            name = context.getString(R.string.emoji_love_hearta);
        } else if (x.equals("emoji_1f4a1")) {
            name = context.getString(R.string.emoji_lightbulb);
        } else if (x.equals("emoji_1f4bc")) {
            name = context.getString(R.string.emoji_towork);
        } else if (x.equals("emoji_1f4e7")) {
            name = context.getString(R.string.emoji_mail);
        } else if (x.equals("emoji_1f6cb")) {
            name = context.getString(R.string.emoji_sofa);
        } else if (x.equals("emoji_1f34c")) {
            name = context.getString(R.string.emoji_banana);
        } else if (x.equals("emoji_1f43c")) {
            name = context.getString(R.string.emoji_panda);
        } else if (x.equals("emoji_1f48d")) {
            name = context.getString(R.string.emoji_diamondring);
        } else if (x.equals("emoji_1f69d")) {
            name = context.getString(R.string.emoji_traintail);
        } else if (x.equals("emoji_1f327")) {
            name = context.getString(R.string.emoji_rain);
        } else if (x.equals("emoji_1f388")) {
            name = context.getString(R.string.emoji_balloon);
        } else if (x.equals("emoji_1f438")) {
            name = context.getString(R.string.emoji_frog);
        } else if (x.equals("emoji_1f570")) {
            name = context.getString(R.string.emoji_alarmclock);
        } else if (x.equals("emoji_1f684")) {
            name = context.getString(R.string.emoji_locomotive);
        } else if (x.equals("emoji_1f688")) {
            name = context.getString(R.string.emoji_carriage);
        } else if (x.equals("emoji_1f697")) {
            name = context.getString(R.string.emoji_car);
        } else if (x.equals("emoji_1ff2e")) {
            name = context.getString(R.string.emoji_luckycat);
        } else if (x.equals("emoji_1ff2f")) {
            name = context.getString(R.string.emoji_mahjong);
        } else if (x.equals("emoji_1ff30")) {
            name = context.getString(R.string.emoji_chess);
        } else if (x.equals("emoji_1ff31")) {
            name = context.getString(R.string.emoji_rollpaper);
        } else if (x.equals("emoji_2601")) {
            name = context.getString(R.string.emoji_cloudyday);
        } else if (x.equals("emoji_2602")) {
            name = context.getString(R.string.emoji_umbrella);
        } else if (x.equals("emoji_1f3a4")) {
            name = context.getString(R.string.emoji_ksong);
        } else if (x.equals("emoji_1f4a2")) {
            name = context.getString(R.string.emoji_bursttendons);
        } else if (x.equals("emoji_1f4b5")) {
            name = context.getString(R.string.emoji_banknote);
        } else if (x.equals("emoji_1f36d")) {
            name = context.getString(R.string.emoji_lollipop);
        } else if (x.equals("emoji_1f37c")) {
            name = context.getString(R.string.emoji_drinkmilk);
        } else if (x.equals("emoji_1f48a")) {
            name = context.getString(R.string.emoji_medicine);
        } else if (x.equals("emoji_1f52b")) {
            name = context.getString(R.string.emoji_pistol) ;
        } else if (x.equals("emoji_1f56f")) {
            name = context.getString(R.string.emoji_prayer);
        } else if (x.equals("emoji_1f389")) {
            name = context.getString(R.string.emoji_applauded);
        } else if (x.equals("emoji_1ff2a")) {
            name = context.getString(R.string.emoji_doublehappiness);
        } else if (x.equals("emoji_1ff2b")) {
            name = context.getString(R.string.emoji_firecracker);
        } else if (x.equals("emoji_1ff2c")) {
            name = context.getString(R.string.emoji_lantern);
        } else if (x.equals("emoji_1ff2d")) {
            name = context.getString(R.string.emoji_windmill);
        } else if (x.equals("emoji_2708")) {
            name = context.getString(R.string.emoji_aircraft);
        } else {
            name = "";
        }
        return name;
    }


    /**
     * 过滤emoji 或者 其他非文字类型的字符
     */
    public static String filterEmoji(String source) {

        if (!containsEmoji(source)) {
            CustomLog.d(TAG, "不包含表情");
            return source;//如果不包含，直接返回

        }
        //到这里铁定包含
        StringBuilder buf = null;

        int len = source.length();

        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);

            if (!isEmojiCharacter(codePoint)) {
                if (buf == null) {
                    buf = new StringBuilder(source.length());
                }
                buf.append(codePoint);
            } else {
                String xx = String.valueOf(codePoint);
                String aa = Emojicon.getHexResName(xx);
                CustomLog.d(TAG, "aa" + aa);
                if (aa.equals("emoji_2615") || aa.equals("emoji_1f4a3") ||
                    aa.equals("emoji_270a")) {
                    i++;

                }
                CustomLog.d(TAG, "" + i);
                list.add(i);

            }
        }

        if (buf == null) {
            return source;//如果没有找到 emoji表情，则返回源字符串
        } else {
            if (buf.length() == len) {//这里的意义在于尽可能少的toString，因为会重新生成字符串
                buf = null;
                return source;
            } else {
                return buf.toString();

            }
        }

    }


    public static boolean containsEmoji(String source) {
        //        if (StringUtils.isBlank(source)) {
        //            return false;
        //        }

        if (TextUtils.isEmpty(source)) {
            return false;
        }

        int len = source.length();

        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);

            if (isEmojiCharacter(codePoint)) {
                //do nothing，判断到了这里表明，确认有表情字符
                return true;
            }
        }

        return false;
    }


    private static boolean isEmojiCharacter(char codePoint) {
        String xx = String.valueOf(codePoint);
        String aa = Emojicon.getHexResName(xx);
        CustomLog.d(TAG, "aa" + aa);
        if (aa.equals("emoji_2615") || aa.equals("emoji_1f4a3") || aa.equals("emoji_270a")) {
            return true;
        }

        return !((codePoint == 0x0)
            || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD) ||
            ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
            ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
            ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
    }


    /**
     * 在线消息，需要震动
     * 离线消息，只需要震动一次
     */
    private void doVibratorMsg(boolean online) {
        CustomLog.d(TAG, "online=" + online + "|FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG=" +
            FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG);
        if (online) {
            //            OutCallUtil.vibratorWhenEndCall();
        } else {
            if (FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG) {
                //                OutCallUtil.vibratorWhenEndCall();
                FLAG_OF_NOTIFICATION_NOT_ON_LINE_MSG = false;
            }
        }
    }


    private void sendNotifacation() {
        if (groupMsgSnippet.size() > 0) {
            Iterator<Entry<String, String>> it = groupMsgSnippet.entrySet()
                .iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String gid = entry.getKey();
                String txt = entry.getValue();// 消息类型+发送者+内容简介
                boolean online = txt.startsWith(true + "");
                if (online) {
                    txt = txt.substring(4);
                } else {
                    txt = txt.substring(5);
                }
                boolean isRemindNotice = txt.endsWith("#remind#");
                if (isRemindNotice) {
                    txt = txt.substring(0, txt.length() - 8);
                }

                if (forbiddenNotify(gid)) {
                    if (!isRemindNotice) {
                        continue;
                    }
                }

                setSmartIcon();

                if (appOnTheDesk()) {
                    String name = groupDao.getGroupNameByGid(gid);
                    int notifyId = NotificationUtil.getGroupNotifyID(gid);
                    Intent pdintent = new Intent(context, ChatActivity.class);
                    pdintent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                        ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, gid);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_ID, gid);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME, name);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                        ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
                    String titleString = context.getString(R.string.notice_information);

                    doSendNotifyMsg(online, titleString, name, notifyId + "", txt, pdintent);

                    //                    MobclickAgent.onEvent(context,UmengEventConstant.EVENT_P2P_NOTIFICATION_COUNT);
                } else {
                    doVibratorMsg(online);
                }
            }
        }
        if (msgsender.size() > 0) {
            Iterator<Entry<String, String>> it = msgsender.entrySet()
                .iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String number = entry.getKey();

                String txt = entry.getValue();// 消息类型+内容简介
                boolean online = txt.startsWith(true + "");
                if (online) {
                    txt = txt.substring(4);
                } else {
                    txt = txt.substring(5);
                }

                boolean isRemindNotice = txt.endsWith("#remind#");
                if (isRemindNotice) {
                    txt = txt.substring(0, txt.length() - 8);
                }
                if (forbiddenNotify(number)) {
                    if (!isRemindNotice) {
                        continue;
                    }
                }

                setSmartIcon();

                if (appOnTheDesk()) {
                    int count = noticesDao.getNewNoticeCountByNumber(number);
                    String name = getNickName(number);
                    if (count > 0) {
                        if (count > 1) {
                            //有多条未读通知时显示未读个数
                            // txt = "[" + count + "]" + txt;

                        }
                        // if (txt.length() )
                        // else {
                        //     txt = getNickName(item.sender) + ": " + txt;
                        // }
                    }
                    Intent pdintent = new Intent(context, ChatActivity.class);
                    pdintent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                        ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
                        number);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
                        name);
                    pdintent.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                        ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE);
                    String titleString = context
                        .getString(R.string.notice_information);
                    doSendNotifyMsg(online, titleString, name, number, txt, pdintent);
                    //                    MobclickAgent.onEvent(context,
                    //                            UmengEventConstant.EVENT_P2P_NOTIFICATION_COUNT);
                } else {
                    doVibratorMsg(online);
                }
            }
        }
    }


    private String getGroupMemberName(String gid, String number) {
        GroupMemberBean bean = groupDao.queryGroupMember(gid, number);
        String name = bean != null ? bean.getDispName() : number;
        return name;
    }


    private String getNickName(String number) {
        String name = "";
        if (number.equals(SettingData.getInstance().adminNubeNum)) {
            SharedPreferences preferences = MedicalApplication.getContext()
                .getSharedPreferences(KEY_SERVICE_NUBE_INFO, context.MODE_PRIVATE);
            name = preferences.getString("USERNAME", SettingData.getInstance().adminNubeNum);
            return name;
        }

        ContactFriendBean bean = contactsDao.queryFriendInfoByNube(number);
        if (bean != null) {

            ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(number);
            name = ShowNameUtil.getShowName(element);

        } else {
            //			String butelPublicNo = NetPhoneApplication.getPreference()
            //					.getKeyValue(PrefType.KEY_BUTEL_PUBLIC_NO, "");
            //			if (number.equals(butelPublicNo)) {
            //				name = context.getString(R.string.str_butel_name);
            //			} else {
            ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(number);
            name = ShowNameUtil.getShowName(element);
            //			}
        }
        return name;
    }


    private String splitBodyType(String msg_type) {
        String type = "";
        if (!TextUtils.isEmpty(msg_type)) {
            int index = msg_type.indexOf('_');
            if (index != -1) {
                type = msg_type.substring(index + 1);
            } else {
                type = msg_type;
            }
        }
        return type;
    }


    public static class PrivateMessage {
        public String folderId = "";
        public String app = "";
        public String sender = "";
        public String receivers = "";
        public String msgId = "";
        public String type = "";
        public String body = "";
        public String title = "";
        public String time = "";
        public String readStatus = "";
        public String extendedInfo = "";
        public String gid = "";
        public boolean online = false;

    }


    public static interface MessageReceiverListener {
        public void onStarted();

        public void onFinished();
    }


    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context
            .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }


    private boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context
            .getSystemService(Context.POWER_SERVICE);

        boolean isScreenOn = pm.isScreenOn();// 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        return isScreenOn;
    }


    public final static boolean isScreenLocked(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context
            .getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }


    private static final Executor executor = Executors
        .newSingleThreadExecutor();


    public void saveSCImMessageThread(final List<PrivateMessage> privateMsgs) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (privateMsgs != null) {
                    // 分类保存
                    doBatchSave(privateMsgs);
                    // 推送通知栏消息
                    sendNotifacation();
                    handleInviteMeetingCall();
                    msgsender.clear();
                    //                    frisender.clear();
                    groupMsgSnippet.clear();
                    if (folder_msgs_map != null) {
                        folder_msgs_map.clear();
                        folder_msgs_map = null;
                    }
                }
            }
        });

    }

    // ==========END==========IM Connect集成===============================

    // ==========START =========SDK CONNECT 集成===========================


    public static class SCIMRecBean {
        public String msgType = "";
        public String title = "";
        public String sender = "";
        public String msgId = "";
        public String text = "";
        public String thumUrl = "";
        public String nikeName = "";
        public String sendTime = "";
        public String groupId = "";
        public int durationSec = 0;
        public long serverTime = 0;
        public boolean offline = false;
        public String extJson = "";
    }


    public static PrivateMessage convertSDIMMsg4GroupEvent(String eventJson) {
        PrivateMessage item = null;
        if (!TextUtils.isEmpty(eventJson)) {

            try {
                JSONObject object = new JSONObject(eventJson);
                item = new PrivateMessage();

                item.folderId = "";
                item.app = "";
                item.sender = object.optString("sender");
                item.receivers = "";
                item.msgId = object.optString("msgId");
                item.type = object.optString("type");
                item.body = object.optString("body");
                item.title = object.optString("title");
                item.time = object.optString("createTime");
                item.readStatus = "0";
                item.extendedInfo = object.optString("extendedInfo");
                item.gid = object.optString("gid");
                item.online = false;

                if (TextUtils.isEmpty(item.gid)) {
                    JSONObject bodyobject = new JSONObject(item.body);
                    item.gid = bodyobject.optString("gid");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return item;
    }


    public static PrivateMessage convertSDIMMsg(SCIMRecBean ImMsgs) {
        PrivateMessage item = null;
        if (ImMsgs != null) {
            item = new PrivateMessage();
            item.folderId = "";
            item.app = "";
            item.sender = ImMsgs.sender;
            item.receivers = "";
            item.msgId = ImMsgs.msgId;
            item.type = ImMsgs.msgType;

            item.title = ImMsgs.title;
            item.time = ImMsgs.serverTime + "";
            item.readStatus = "0";

            item.gid = ImMsgs.groupId;
            item.online = isOnlineMsg(ImMsgs.extJson) && checkOnlineBydiffTime(ImMsgs);

            String[] urls = null;
            if (!TextUtils.isEmpty(ImMsgs.thumUrl)) {
                urls = ImMsgs.thumUrl.split(",");
            }
            item.body = getBody(ImMsgs.msgType, urls);
            item.extendedInfo = getExtInfo(ImMsgs.msgType, urls, ImMsgs.text,
                ImMsgs.extJson, ImMsgs.durationSec);
        }
        return item;
    }


    private static boolean isOnlineMsg(String extJson) {
        CustomLog.d(TAG, "online extJson :" + extJson);
        JSONObject object = null;
        boolean online = false;
        try {
            object = new JSONObject(extJson);
            online = object.optBoolean("online", false);
        } catch (JSONException e) {
            CustomLog.d(TAG, "online json error:" + e.getLocalizedMessage());
        }
        return online;
    }


    private static boolean checkOnlineBydiffTime(SCIMRecBean ImMsgs) {
        if (ImMsgs != null) {
            long sendtime = DateUtil.getTimeInMillis(ImMsgs.sendTime, "yyyy-MM-dd HH-mm-ss");
            long recvtime = ImMsgs.serverTime;
            long diff = recvtime - sendtime;
            boolean online = diff < 60 * 1000 ? true : false;
            CustomLog.d(TAG,
                "check by diffTime: st_s:" + ImMsgs.sendTime + "st_l: " + sendtime + " rt_l:" +
                    recvtime + " online:" + online);
            return online;
        }
        return false;
    }


    private static String getBody(String type, String[] urls) {

        CustomLog.d(TAG, "getBody type :" + type + " url: " + urls != null ? "" : urls
            .toString());
        JSONArray array = new JSONArray();
        if (urls != null && urls.length > 0) {
            int length = urls.length;
            CustomLog.d(TAG, "getBody type :" + type + "url length=" + length);
            String romoteUrl = "";
            if (TYPE_PIC_2.equals(type)) {
                if (length == 3) {
                    romoteUrl = urls[1];
                    array.put(romoteUrl);
                } else if (length > 3) {
                    int mod = length / 3;
                    for (int i = 0; i < mod; i++) {
                        romoteUrl = urls[i * 2 + 1];
                        array.put(romoteUrl);
                    }
                } else {
                    romoteUrl = urls[0];
                    array.put(romoteUrl);
                }
            } else if (TYPE_VIDEO_2.equals(type)) {
                romoteUrl = urls[0];
                array.put(romoteUrl);
            } else if (TYPE_AUDIO.equals(type)) {
                romoteUrl = urls[0];
                array.put(romoteUrl);
            } else if (TYPE_CARD.equals(type)) {
                romoteUrl = urls[0];
                array.put(romoteUrl);
            } else {
                romoteUrl = urls[0];
                array.put(romoteUrl);
            }

        } else {
            // do nothing
        }
        return array.toString();
    }


    private static String getExtInfo(String type, String[] urls, String text,
                                     String extJson, int duration) {

        JSONArray array = new JSONArray();
        if (urls != null && urls.length > 0) {
            int length = urls.length;
            CustomLog.d(TAG, "getExtInfo type :" + type + "url length=" + length);
            String romoteUrl = "";
            if (TYPE_PIC_2.equals(type)) {
                if (length == 3) {
                    romoteUrl = urls[2];
                    array.put(romoteUrl);
                } else if (length > 3) {
                    int mod = length / 3;
                    for (int i = 0; i < mod; i++) {
                        romoteUrl = urls[mod * 2 + i];
                        array.put(romoteUrl);
                    }
                } else {
                    romoteUrl = urls[0];
                    array.put(romoteUrl);
                }
            } else if (TYPE_VIDEO_2.equals(type)) {

                if (length >= 2) {
                    // "thumUrl":"http:\/\/210.51.168.105\/group1\/M00\/37\/E1\/wKhlFldo826AfnPeAG267VLnhJA663.mp4,http:\/\/210.51.168.105\/group1\/M00\/37\/DF\/wKhlFVdo9ACAaYlnAAAe21QK6XM598.jpg"
                    // 第一个是视频url，第二个链接才是缩略图url
                    romoteUrl = urls[1];
                    array.put(romoteUrl);
                }
            }

        }

        JSONObject object = null;
        try {
            object = new JSONObject(extJson);

            if (duration > 0) {
                if (TYPE_VIDEO_2.equals(type)) {
                    object.put("vediolen", duration);
                }
                if (TYPE_AUDIO.equals(type)) {
                    object.put("audiolen", duration);
                }
            }

            // if(!TextUtils.isEmpty(thumb)){
            // JSONArray array = new JSONArray();
            // array.put(thumb);
            // object.put("thumbUrls", array);
            // }

            if (array != null && array.length() > 0) {
                object.put("thumbUrls", array);
            }

            if (TYPE_COMMON.equals(type)) {
                JSONObject commonObject = new JSONObject(text);
                object.put("text", commonObject.optString("text"));
                object.put("subtype", commonObject.optString("subtype"));
                if (BizConstant.MSG_SUB_TYPE_FILE.equals(commonObject.optString("subtype"))) {
                    object.put("fileInfo", commonObject.optString("fileInfo"));
                } else if (BizConstant.MSG_SUB_TYPE_CHATRECORD.equals(
                    commonObject.optString("subtype"))) {
                    object.put("chatrecordInfo", commonObject.optJSONArray("chatrecordInfo"));
                } else if (BizConstant.MSG_SUB_TYPE_ARTICLE.equals(
                    commonObject.optString("subtype"))) {
                    object.put("articleInfo", commonObject.optJSONObject("articleInfo"));
                } else if (BizConstant.MSG_SUB_TYPE_STRANGER.equals(
                    commonObject.optString("subtype"))) {
                    object.put("nickname", commonObject.optString("nickname"));
                    object.put("headurl", commonObject.optString("headurl"));
                    object.put("text", commonObject.optString("text"));
                    object.put("isReplayMsg", commonObject.optInt("isReplayMsg"));
                } else if (BizConstant.MSG_SUB_TYPE_ADDFRIEND.equals(
                    commonObject.optString("subtype"))) {
                    object.put("nickname", commonObject.optString("nickname"));
                    object.put("headurl", commonObject.optString("headurl"));
                } else if (
                    BizConstant.MSG_SUB_TYPE_MEETING.equals(commonObject.optString("subtype"))
                        || BizConstant.MSG_SUB_TYPE_MEETING_BOOK.equals(
                        commonObject.optString("subtype"))) {
                    object.put("meetingInfo", commonObject.optString("meetingInfo"));
                } else if (BizConstant.MSG_SUB_TYPE_REMIND_NOTICE.equals(
                    commonObject.optString("subtype"))) {
                    object.put("content", commonObject.optString("content"));
                }
            } else {
                object.put("text", text);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object == null ? "" : object.toString();
    }


    private void setFriendAuthMsgRead(String serverId, String msgId) {
        HashMap<String, String> serverIds = new HashMap<String, String>();
        serverIds.put(serverId, msgId);
        AppP2PAgentManager.getInstance().markMsgRead(serverIds);
    }


    private void setSmartIcon() {
        newMsgCount = newMsgCount + 1;
        BadgeUtil.setBadgeCount(context, newMsgCount);
    }


    private void sendRequestAddFriendNotification(String nubeNumber, String nickName, String content) {

        String txt = content;// 消息类型+内容简介
        boolean online = txt.startsWith(true + "");
        if (online) {
            txt = txt.substring(4);
        } else {
            txt = txt.substring(5);
        }

        setSmartIcon();

        if (appOnTheDesk()) {
            Intent pdintent = new Intent(context, NewFriendsActivity.class);
            String titleString = context
                .getString(R.string.notice_information);
            doSendNotifyMsg(online, titleString, nickName, nubeNumber, txt, pdintent);
        } else {
            doVibratorMsg(online);
        }
    }


    private void sendAgreeAddFriendNotification(String nubeNumber, String nickName, String content) {
        String txt = content;// 消息类型+内容简介
        boolean online = txt.startsWith(true + "");
        if (online) {
            txt = txt.substring(4);
        } else {
            txt = txt.substring(5);
        }

        setSmartIcon();

        if (appOnTheDesk()) {
            Intent pdintent = new Intent(context, ChatActivity.class);
            pdintent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
                nubeNumber);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
                nickName);
            pdintent.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE);
            String titleString = context
                .getString(R.string.notice_information);
            doSendNotifyMsg(online, titleString, nickName, nubeNumber, txt, pdintent);
        } else {
            doVibratorMsg(online);
        }
    }
}
