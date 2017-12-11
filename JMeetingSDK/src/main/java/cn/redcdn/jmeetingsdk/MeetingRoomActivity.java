package cn.redcdn.jmeetingsdk;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import com.serenegiant.usb.DeviceFilter;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.redcdn.buteldataadapter.DataSet;
import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKOperationListener;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.LiveStatus;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.MeetingStyleStatus;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKReturnCode;
import cn.redcdn.butelopensdk.constconfig.CmdKey;
import cn.redcdn.butelopensdk.constconfig.EpisodeData;
import cn.redcdn.butelopensdk.constconfig.MediaType;
import cn.redcdn.butelopensdk.constconfig.MeetingStyleCode;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.constconfig.RespModelStatusCode;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.butelopensdk.vo.VideoParameter;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.datacenter.meetingmanage.CheckHasMeetingPwd;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInvitationSMS;
import cn.redcdn.datacenter.meetingmanage.GetMeetingPwd;
import cn.redcdn.datacenter.meetingmanage.GetMeetingZBInvitationContent;
import cn.redcdn.datacenter.meetingmanage.ModifyMeetingInviters;
import cn.redcdn.datacenter.meetingmanage.SendSMS;
import cn.redcdn.datacenter.meetingmanage.data.CheckHasMeetingPwdInfo;
import cn.redcdn.datacenter.meetingmanage.data.GetMeetingPwdInfo;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInvitationSMSInfo;
import cn.redcdn.datacenter.meetingmanage.data.MeetingZBInvitationInfo;
import cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.InviteMeetingInfo;
import cn.redcdn.meeting.data.InvitePersonList;
import cn.redcdn.meeting.data.LoudspeakerInfo;
import cn.redcdn.meeting.data.MeetingEvent;
import cn.redcdn.meeting.data.ParticipantorList;
import cn.redcdn.meeting.data.Participantors;
import cn.redcdn.meeting.interfaces.Contact;
import cn.redcdn.meeting.interfaces.ContactCallback;
import cn.redcdn.meeting.interfaces.HostAgentOperation;
import cn.redcdn.meeting.interfaces.ResponseEntry;
import cn.redcdn.meetinginforeport.InfoReportManager;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.menuview.MenuView.MenuViewListener;
import cn.redcdn.menuview.view.CameaView.CameraType;
import cn.redcdn.menuview.view.LiveShareView;
import cn.redcdn.menuview.view.LiveShareView.ShareBtnOnClickListener;
import cn.redcdn.menuview.view.LiveView;
import cn.redcdn.menuview.view.LiveView.BtnOnClickListener;
import cn.redcdn.menuview.view.ParticipatorsView;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.messagereminder.MessageInfo;
import cn.redcdn.messagereminder.MessageReminderManage;
import cn.redcdn.messagereminder.MessageReminderView;
import cn.redcdn.network.httprequest.HttpErrorCode;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.CustomDialog;
import cn.redcdn.util.CustomDialog.CancelBtnOnClickListener;
import cn.redcdn.util.CustomDialog.OKBtnOnClickListener;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

//import cn.sharesdk.framework.Platform;
//import cn.sharesdk.framework.ShareSDK;
//import cn.sharesdk.onekeyshare.OnekeyShare;
//import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;
//import cn.sharesdk.wechat.friends.Wechat;
//import cn.sharesdk.wechat.moments.WechatMoments;
//import PhoneStateMonitor.SignalStrengthChangerListener;
//import com.iflytek.cloud.util.ContactManager;

//import cn.redcdn.butelopensdk.media.MediaView;


public class MeetingRoomActivity extends BaseActivity /*
                                                     * implements
													 * SensorEventListener
													 */ {

    private OrientationEventListener mScreenOrientationEventListener;
    private int mScreenExifOrientation = 0;
    private int mFloatingViewOrientation =0;
    private int mNoFloatingViewOrientation =0;
    private boolean isNeedShowWindow = false;
    private FloatViewParamsListener mListener;
    private WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
    private static final long FLOATING_WINDOW_DELAY = 300;


    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }


    public class FloatingViewListener implements FloatViewParamsListener {
        @Override
        public void hideTitleBar(boolean hide) {
            full(hide);
        }


        @Override
        public int getTitleHeight() {

            int statusBarHeight = -1;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            //CustomLog.e(TAG, "状态栏 " + statusBarHeight);
            return statusBarHeight;
        }


        @Override
        public WindowManager.LayoutParams getLayoutParams() {

            return mWindowParams;
        }


        @Override
        public void setIsShowWindow(boolean isshow) {
            isNeedShowWindow = isshow;
        }


        @Override
        public void hideActivity() {
            CustomLog.i(TAG, "hideActivity()  mMenuView "+mMenuView);
            //CustomToast.show(MeetingRoomActivity.this,"hideActivity",CustomToast.LENGTH_LONG);
            isNeedShowWindow = true;
            if (mMenuView != null) {
                mMenuView.postDelayed(new Runnable() {
                    @Override public void run() {
                        CustomLog.i(TAG, "hideActivity()  showFloatingView ");
                        moveTaskToBack(true);

                    }
                }, FLOATING_WINDOW_DELAY);
            }
            mMenuView.showFloatingView();
            setFloatingViewShowType(true);

            //moveTaskToBack(true);

        }


        @Override
        public VideoParameter getSelftVideoParameter() {
            return MeetingManager.getInstance().getVideoParameter(mButelOpenSDK.getCurrentCameraIndex());
        }

    }

    public void setFloatingViewShowType(boolean floatingViewShow) {
        SharedPreferences setting = getApplicationContext()
                .getSharedPreferences("floatingViewShowType", Context.MODE_MULTI_PROCESS);
        Editor editor = setting.edit();
        editor.putBoolean("type", floatingViewShow);
        editor.commit();
    }

    //    private SurfaceView mSurfaceView;

    private String MDSURL = "http://175.102.21.35:10080";
    private final String TAG = MeetingRoomActivity.this.getClass().getName();

    private String GroupId = "";

    private boolean masterOperateUserMic = false;

    private boolean masterOperateUserCamera = false;

    private boolean handleCpuOverloadEvent = false;

    private boolean handleVideoCloseAdaptEvent = false;

    private static final int handleCpuOverloadMsg = 999;

    private static final int handleVideoCloseAdaptMsg = 998;

    private boolean menuViewisInit = false;

    private PopupWindow popupWindow;

    private PopupWindow popupWindowMode;

    public final String START_CAMERA_FAILED = "start_camera_failed";

    private Map<String, EpisodeData> episodeList = new HashMap<String, EpisodeData>();

    private ImageView mNoSpeakerImg;

    private TextView mNoSpeakerTextView;

    private int episodeState;

    private boolean isExistEpisode = false;

    private AudioManager mAudioManager;

//    private int cameraInfo = 1; //0 后置，1前置，2外置

    private boolean isOpenCamera = false;

    // private SensorManager mSensorManager;
    // private Sensor mSensor;
    private TextView open;

    private LinearLayout openLayout;

    private TextView change;

    private LinearLayout changeLayout;

    private Button changeMode;

    private ImageView imageMode;

    private ImageView imageCamera;

    private ImageView closeCamera;

    private boolean mIsHeadSetPlugged = false;

    private boolean mIsbluetooth = false;

    private String localIp = "";

    private CustomDialog mSwitchAudioModeDialog; // 当收到底层抛出的需要切换到语音模式时显示

    private CustomDialog mSwitchAudioAlertDialog;

    private CustomDialog mSwitchVideoAlertDialog;

    private CustomDialog mSwitchPlayAlertDialog;

    private String shareString;

    private String liveShareString;

    private String liveZbUrl;

    private String liveNameString;

    private LiveView liveDiaglog;

    private LiveShareView shareDialog;

    private boolean isMicClose = false;

    private boolean isAudioSpeakerClose = false;

    public final static String INTENT_EXTRA_TOKEN = "token";

    public final static String INTENT_EXTRA_ACCOUNT_ID = "accountId";

    public final static String INTENT_EXTRA_ACCOUNT_NAME = "accountName";

    public final static String INTENT_EXTRA_MEETING_ID = "meetingId";

    public final static String INTENT_EXTRA_IS_ALLOW_MOBILE_NET = "isAllowMobileNet";

    public final static String INTENT_EXTRA_SELECT_SYSTEM_CAMERA = "selectSystemCamera";

    public final static String INTENT_EXTRA_IS_AUTO_SPEAK = "isAutoSpeak";

    /** 请求退出会议广播，MeetingRoomActivity 接收该广播后将退出会议 */
    public static final String RUQUEST_QUIT_MEETING_BROADCAST
            = "cn.redcdn.jmeetingsdk.meetingroom.requestquitmeeting";

    /** 开始会议广播，MeetingRoomActivity发送该广播 */
    public static final String START_MEETING_BROADCAST
            = "cn.redcdn.jmeetingsdk.meetingroom.startmeeting";

    /** 结束会议广播，MeetingRoomActivity发送该广播 */
    public static final String END_MEETING_BROADCAST
            = "cn.redcdn.jmeetingsdk.meetingroom.endmeeting";

    /** 发起会议邀请广播 */
    public static final String INVITE_MEETING_BROADCAST
            = "cn.redcdn.jmeetingsdk.meetingroom.invite";

    /** 发起会议操作广播 */
    public static final String OPERATION_MEETING_BROADCAST
            = "cn.redcdn.jmeetingsdk.meetingroom.operation";

    //微信邀请生命
    public static String APP_ID; //微信appid
    public static IWXAPI api;
    private AudioManager audioManager;
    private BroadcastReceiver bluetoothReceiver;

    enum ownSpeak {
        noSpeak, SpeakOnMic
    }


    private class MeetingInfo {
        public String accountId = "63000001";

        public String accountName = "63000001";

        public int meetingId = 0;

        public String token = "405da3d4-5bfb-4230-94b6-c8107a2badf8";

        public int lockInfo = 0;

        public boolean isJoinMeeting = false;

        public int netType = NetType.NO_NET;

        public boolean userTempNetChoose = false;

        public boolean isInBackground = false;

        public boolean mIsDealWith981 = false;

        public boolean isAutoSpeak = true;

        public boolean selectSystemCamera = true;
    }


    private class NetType {
        public static final int NO_NET = -1;

        public static final int WIFI = 1;

        public static final int CMWAP = 2;

        public static final int CMNET = 3;
    }


    private class ShowingDialog {
        public static final int NO_DIALOG = 0;

        public static final int INVITE_DIALOG = 1;

        public static final int NET_CHANGE_DIALOG = 2;

        public static final int EXIT_DIALOG = 3;
    }


    private int mShowingDialog = ShowingDialog.NO_DIALOG;

    private GestureDetector mGestureDetector;

    public static final int GESTURE_UP = 0;

    public static final int GESTURE_DOWN = 1;

    public static final int GESTURE_LEFT = 2;

    public static final int GESTURE_RIGHT = 3;

    private int mLimitScrollWidth = 50;

    private int mLimitScrollHeight = 50;

    private CameraType currentMode = CameraType.OwnNoSpeak;

    private CustomDialog mDialog;

    private MessageReminderManage mMessageReminderManage;

	/*
     * private SignalStrengthChangerListener mListener = new
	 * SignalStrengthChangerListener() {
	 *
	 * @Override public void onStrengthChanged() { // TODO Auto-generated method
	 * stub } };
	 */

    private InvitePersonList mInvitePersonList = new InvitePersonList();

    private ParticipantorList mParticipantorList;

    public static List<LoudspeakerInfo> mLoudspeakerInfoList = new ArrayList<LoudspeakerInfo>();

    private Participantors mParticipantorsData = new Participantors() {
        @Override
        public void onPartcipantorChanged(Person person,
                                          DataChangeType dataChangeType) {
            CustomLog.d(TAG, "参会者数据变化，重新获取通讯录列表  " + person.getAccountId()
                    + " " + dataChangeType);
            final DataChangeType tmpDataChangeType = dataChangeType;
            final Person tmpPerson = person;
            MeetingManager.getInstance().getContactOperationImp()
                    .getAllContacts(new ContactCallback() {
                        @Override
                        public void onFinished(ResponseEntry result) {
                            if (result.status >= 0) {
                                DataSet adapterDataSetImp = (DataSet) result.content;
                                if (adapterDataSetImp == null) {
                                    CustomLog.d(TAG, "onContactLoaded 结果为空");
                                } else {
                                    List<Person> invitePersonTxlPersonList = new ArrayList<Person>();
                                    List<Person> partcipantorTxlPersonList = new ArrayList<Person>();
                                    for (int i = 0; i < adapterDataSetImp
                                            .getCount(); i++) {
                                        Contact contact = (Contact) adapterDataSetImp
                                                .getItem(i);
                                        Person p1 = new Person();
                                        p1.setAccountId(contact.getNubeNumber());
                                        p1.setAccountName(contact.getNickname());
                                        p1.setPhoto(contact.getHeadUrl());

                                        Person p2 = new Person();
                                        p2.setAccountId(contact.getNubeNumber());
                                        p2.setAccountName(contact.getNickname());
                                        p2.setPhoto(contact.getHeadUrl());

                                        invitePersonTxlPersonList.add(p1);
                                        partcipantorTxlPersonList.add(p2);
                                    }
                                    adapterDataSetImp.release();
                                    mInvitePersonList
                                            .notifyTxlChanged(invitePersonTxlPersonList);
                                    mParticipantorList
                                            .notifyTxlChanged(partcipantorTxlPersonList);
                                }
                            }
                            if (tmpDataChangeType == DataChangeType.INC) {
                                CustomLog.d(TAG, "添加参会方");
                                mInvitePersonList.participantorInc(tmpPerson);
                                mParticipantorList.participantorInc(tmpPerson);
                            } else {
                                CustomLog.d(TAG, "移除参会方");
                                mInvitePersonList.participantorDec(tmpPerson);
                                mParticipantorList.participantorDec(tmpPerson);
                            }

                        }

                    }, true);
        }


        @Override
        public void onPartcipantorChanged(List<Person> list) {
            CustomLog.d(TAG, "参会者数据变化，重新获取通讯录列表 " + list);
            final List<Person> tmpList = list;
            MeetingManager.getInstance().getContactOperationImp()
                    .getAllContacts(new ContactCallback() {
                        @Override
                        public void onFinished(ResponseEntry result) {
                            CustomLog.d(TAG, "参会者数据变化，重新获取通讯录列表onFinished "
                                    + result.status);
                            if (result.status >= 0) {
                                DataSet adapterDataSetImp = (DataSet) result.content;
                                if (adapterDataSetImp == null) {
                                    CustomLog.d(TAG, "onContactLoaded 结果为空");
                                } else {
                                    List<Person> invitePersonTxlPersonList = new ArrayList<Person>();
                                    List<Person> partcipantorTxlPersonList = new ArrayList<Person>();
                                    for (int i = 0; i < adapterDataSetImp
                                            .getCount(); i++) {
                                        Contact contact = (Contact) adapterDataSetImp
                                                .getItem(i);
                                        Person p1 = new Person();
                                        p1.setAccountId(contact.getNubeNumber());
                                        p1.setAccountName(contact.getNickname());
                                        p1.setPhoto(contact.getHeadUrl());

                                        Person p2 = new Person();
                                        p2.setAccountId(contact.getNubeNumber());
                                        p2.setAccountName(contact.getNickname());
                                        p2.setPhoto(contact.getHeadUrl());

                                        JSONArray paticitors = null;
                                        try {
                                            paticitors = mButelOpenSDK.getParticipatorList();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        if (paticitors != null) {
                                            for (int b = 0; b < paticitors.length(); b++) {
                                                try {
                                                    JSONObject paticitorObj = paticitors.getJSONObject(
                                                            b);
                                                    if (paticitorObj.optString(CmdKey.ACOUNT_ID)
                                                            .equals(p2.getAccountId())) {
                                                        if (paticitorObj.optInt("loudSpeakerStatus") ==
                                                                1) {
                                                            CustomLog.d(TAG, "NO.2");
                                                            p2.setLoudSpeakerStatus(1);

                                                            boolean matchFlag3 = false;
                                                            for (int a = 0;
                                                                 a < mLoudspeakerInfoList.size();
                                                                 a++) {
                                                                if (mLoudspeakerInfoList.get(a)
                                                                        .getAccountId()
                                                                        .equals(p2.getAccountId())) {
                                                                    mLoudspeakerInfoList.get(a)
                                                                            .setLoudspeakerStatus(1);
                                                                    matchFlag3 = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (!matchFlag3) {
                                                                LoudspeakerInfo loudspeakerInfo
                                                                        = new LoudspeakerInfo();
                                                                loudspeakerInfo.setAccountId(
                                                                        p2.getAccountId());
                                                                loudspeakerInfo.setLoudspeakerStatus(1);
                                                                mLoudspeakerInfoList.add(
                                                                        loudspeakerInfo);
                                                            }

                                                        } else if (
                                                                paticitorObj.optInt("loudSpeakerStatus") ==
                                                                        2) {
                                                            p2.setLoudSpeakerStatus(2);

                                                            boolean matchFlag4 = false;
                                                            for (int a = 0;
                                                                 a < mLoudspeakerInfoList.size();
                                                                 a++) {
                                                                if (mLoudspeakerInfoList.get(a)
                                                                        .getAccountId()
                                                                        .equals(p2.getAccountId())) {
                                                                    mLoudspeakerInfoList.get(a)
                                                                            .setLoudspeakerStatus(2);
                                                                    matchFlag4 = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (!matchFlag4) {
                                                                LoudspeakerInfo loudspeakerInfo
                                                                        = new LoudspeakerInfo();
                                                                loudspeakerInfo.setAccountId(
                                                                        p2.getAccountId());
                                                                loudspeakerInfo.setLoudspeakerStatus(2);
                                                                mLoudspeakerInfoList.add(
                                                                        loudspeakerInfo);
                                                            }

                                                        }
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        invitePersonTxlPersonList.add(p1);
                                        partcipantorTxlPersonList.add(p2);
                                    }
                                    adapterDataSetImp.release();
                                    mInvitePersonList
                                            .notifyTxlChanged(invitePersonTxlPersonList);
                                    mParticipantorList
                                            .notifyTxlChanged(partcipantorTxlPersonList);
                                }
                            }
                            mInvitePersonList.participantorsAdd(tmpList);
                            mParticipantorList.participantorsAdd(tmpList);

                        }
                    }, true);
        }
    };

    private ButelOpenSDK mButelOpenSDK;
    private boolean mIsBeKicked = false;
    private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener
            = new ButelOpenSDKNotifyListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onNotify(int notifyType, Object object) {
            String accoundid = mMeetingInfo.accountId;
            switch (notifyType) {
                case NotifyType.START_SPEAK:
                case NotifyType.SPEAKER_ON_LINE:
                    CustomLog.e(TAG, "SPEAKER_ON_LINE");
                    if (mButelOpenSDK.getSpeakers() != null
                            && mButelOpenSDK.getSpeakers().size() > 0) {
                        hideNoSpeakerTip();
                    } else {
                        showNoSpeakerTip();
                    }
                    Cmd speakerOnLineCmd = (Cmd) object;
                    String accountName;
                    accountName = mMeetingInfo.accountName;
                    CustomLog.e(TAG, "speakerOnLineCmd.getAccountId()="
                            + speakerOnLineCmd.getAccountId());
                    CustomLog.e(TAG, "accountidgetUserId()="
                            + mMeetingInfo.accountName);
                    if (speakerOnLineCmd.getAccountId().toString().trim()
                            .equalsIgnoreCase(accoundid.trim())) {
                        CustomLog.e(TAG, "本人被发言了mMenuView.getisCloseMoive()="
                                + mMenuView.getisCloseMoive());
                        if (!mMenuView.getisCloseMoive()) {
                            isOpenCamera = true;
                        }
                        CustomLog.d(TAG, "本人被发言");
                        // changemode(CameraType.OwnSpeak);
                    } else {
                        CustomLog.d(TAG, "不是本人发言");
                        // changemode(CameraType.OwnNoSpeak);
                    }
                    speakerOnLine(speakerOnLineCmd.getAccountId(), accountName);

                    if (speakerOnLineCmd.getCmdFlag() == 0) {
                        if (!accoundid.equals(speakerOnLineCmd.getAccountId())) {
                            String newSpeakerMsg = speakerOnLineCmd.getUserName()
                                    + "开始发言";
                            CustomToast.show(MeetingRoomActivity.this,
                                    newSpeakerMsg, Toast.LENGTH_LONG);
                        } else {
                            CustomToast.show(MeetingRoomActivity.this, "开始发言",
                                    Toast.LENGTH_LONG);
                        }
                        // if
                        // (!mMeetingInfo.accountId.equals(speakerOnLineCmd.getAccountId()))
                        // {
                        // String newSpeakerMsg = speakerOnLineCmd.getUserName() +
                        // "开始发言";
                        // MessageInfo newSpeakerMsgInfo = new MessageInfo();
                        // newSpeakerMsgInfo.type =
                        // MessageReminderManage.BEGIN_SPEAK;
                        // newSpeakerMsgInfo.msg = newSpeakerMsg;
                        // if (mMessageReminderManage != null) {
                        // mMessageReminderManage.sendMessage(newSpeakerMsgInfo);
                        // }
                        // } else {
                        // MessageInfo beginSpeakMsgInfo = new MessageInfo();
                        // beginSpeakMsgInfo.type =
                        // MessageReminderManage.BEGIN_SPEAK;
                        // beginSpeakMsgInfo.msg = "请求成功，开始发言吧";
                        // if (mMessageReminderManage != null) {
                        // mMessageReminderManage.sendMessage(beginSpeakMsgInfo);
                        // }
                        mMenuView.dismissAskForSpeakView();
                        // }
                    } else if (speakerOnLineCmd.getCmdFlag() == 1) {
                        if (speakerOnLineCmd.getAccountId().equalsIgnoreCase(
                                accoundid)) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    mMeetingInfo.accountName + "开始发言",
                                    Toast.LENGTH_LONG);
                        }
                    } else {
                        if (!accoundid.equalsIgnoreCase(speakerOnLineCmd
                                .getAccountId())) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    speakerOnLineCmd.getUserName() + "开始发言",
                                    Toast.LENGTH_LONG);
                        } else {
                            if (speakerOnLineCmd.getAccountId().equalsIgnoreCase(
                                    mButelOpenSDK.getMasterAccountId())) {
                                CustomToast.show(MeetingRoomActivity.this,
                                        mMeetingInfo.accountName + "开始发言",
                                        Toast.LENGTH_LONG);
                            } else {
                                CustomToast.show(MeetingRoomActivity.this, "主持人"
                                                + mButelOpenSDK.getMasterName() + "指定您发言",
                                        Toast.LENGTH_LONG);
                            }
                        }
                    }
                    // String newSpeakerMsg = speakerOnLineCmd.getOrigUserName() +
                    // "传麦给"
                    // + speakerOnLineCmd.getUserName() + "，"
                    // + speakerOnLineCmd.getUserName() + "开始发言";
                    // MessageInfo newSpeakerMsgInfo = new MessageInfo();
                    // newSpeakerMsgInfo.type = MessageReminderManage.BEGIN_SPEAK;
                    // newSpeakerMsgInfo.msg = newSpeakerMsg;
                    // if (mMessageReminderManage != null) {
                    // mMessageReminderManage.sendMessage(newSpeakerMsgInfo);
                    // }
                    // if
                    // (mMeetingInfo.accountId.equals(speakerOnLineCmd.getAccountId()))
                    // {
                    // mMenuView.dismissAskForSpeakView();
                    // }
                    // }

                    break;
                case NotifyType.SPEAKER_OFF_LINE:
                case NotifyType.STOP_SPEAK:
                    // mMenuView.hideCameraView();
                    Cmd speakerOffLineCmd = (Cmd) object;
                    speakerOffLine(speakerOffLineCmd.getAccountId());
                    if (mMeetingInfo.accountId.equals(speakerOffLineCmd
                            .getAccountId())) {
                        mMenuView.dismissAskForStopSpeakViewAndGiveMicView();
                    }
                    Cmd stopSpeakerResp = (Cmd) object;
                    CustomLog.e(TAG, stopSpeakerResp.getCmdFlag()
                            + "...............SPEAKER_OFF_LINE");
                    if (speakerOffLineCmd.getAccountId().toString().trim()
                            .equalsIgnoreCase(accoundid.trim())) {
                        CustomLog.d(TAG, "本人取消发言");
                        // changemode(CameraType.OwnNoSpeak);
                        if (popupWindow != null && popupWindow.isShowing()) {
                            CustomLog.d(TAG, "本人取消发言,popupWindow.dismiss()");
                            popupWindow.dismiss();
                        }
                    }
                    if (stopSpeakerResp.getCmdFlag() == 2) {
                        if (!accoundid.equals(stopSpeakerResp.getAccountId())) {
                            if (accoundid.equalsIgnoreCase(mButelOpenSDK
                                    .getMasterAccountId()))

                            {
                                CustomToast.show(MeetingRoomActivity.this, "已取消"
                                                + stopSpeakerResp.getUserName() + "发言",
                                        Toast.LENGTH_LONG);
                            }
                        } else {
                            if (!stopSpeakerResp.getAccountId().equalsIgnoreCase(
                                    mButelOpenSDK.getMasterAccountId())) {
                                CustomToast.show(MeetingRoomActivity.this, "主持人"
                                                + mButelOpenSDK.getMasterName() + "已取消您发言",
                                        Toast.LENGTH_LONG);

                            }
                        }
                    }/*
                 * else if(stopSpeakerResp.getCmdFlag() == 0){ if
				 * (accoundid.equals(stopSpeakerResp.getAccountId())) {
				 * CustomToast.show(MeetingRoomActivity.this, "取消发言成功",
				 * Toast.LENGTH_LONG); } }
				 */
                    break;
                case NotifyType.NOTIFY_LOCK_INFO:
                    Cmd notifyLockInfoCmd = (Cmd) object;
                    mMeetingInfo.lockInfo = notifyLockInfoCmd.getLockInfo();
                    mMenuView.setLock(mMeetingInfo.lockInfo);

                    MessageInfo notifyLockInfo = new MessageInfo();
                    if (mMeetingInfo.lockInfo == 0) {
                        notifyLockInfo.type = MessageReminderManage.LOCK_MEETING_OR_NOT;
                        notifyLockInfo.msg = "会议已解锁";
                        // mMessageReminderManage.sendMessage(notifyLockInfo);
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(MeetingRoomActivity.this, "会诊已解锁",
                                    Toast.LENGTH_LONG);
                        } else {
                            CustomToast.show(MeetingRoomActivity.this, "会议已解锁",
                                    Toast.LENGTH_LONG);
                        }
                    } else {
                        notifyLockInfo.type = MessageReminderManage.LOCK_MEETING_OR_NOT;
                        notifyLockInfo.msg = "会议已加锁";
                        // mMessageReminderManage.sendMessage(notifyLockInfo);
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(MeetingRoomActivity.this, "会诊已加锁",
                                    Toast.LENGTH_LONG);
                        } else {
                            CustomToast.show(MeetingRoomActivity.this, "会议已加锁",
                                    Toast.LENGTH_LONG);
                        }
                        // mMenuView.dismissInvitePersonView();
                        if (mMenuView != null) {
                            mMenuView.dismissInvitePersonView();
                        }
                    }
                    break;
                case NotifyType.PERSON_JOIN_MEETING:
                    Cmd personJoinMeetingCmd = (Cmd) object;
                    Person p = new Person();
                    p.setAccountId(personJoinMeetingCmd.getAccountId());
                    p.setAccountName(personJoinMeetingCmd.getUserName());

                    boolean matchFlag15 = false;
                    for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
                        if (mLoudspeakerInfoList.get(a).getAccountId().equals(p.getAccountId())) {
                            mLoudspeakerInfoList.get(a).setLoudspeakerStatus(1);
                            matchFlag15 = true;
                            break;
                        }
                    }

                    if (!matchFlag15) {
                        LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
                        loudspeakerInfo.setAccountId(p.getAccountId());
                        loudspeakerInfo.setLoudspeakerStatus(1);
                        mLoudspeakerInfoList.add(loudspeakerInfo);
                    }

                    if (mParticipantorsData.getList().contains(p)) {
                        CustomLog.d(TAG, "收到已参会用户的参会命令，不处理");
                        return;
                    }
                    mParticipantorsData.addParticipantor(p);
                    mMenuView.setParticipatorCount(mParticipantorsData.getSize());

                    // MessageInfo joinMeetingMsgInfo = new MessageInfo();
                    // joinMeetingMsgInfo.type = MessageReminderManage.JOIN_MEETING;
                    // joinMeetingMsgInfo.msg = personJoinMeetingCmd.getUserName() +
                    // "加入会议";
                    // mMessageReminderManage.sendMessage(joinMeetingMsgInfo);
                    if (!personJoinMeetingCmd.getAccountId().equalsIgnoreCase(
                            accoundid)) {

                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    personJoinMeetingCmd.getUserName() + "加入会诊",
                                    Toast.LENGTH_LONG);

                        } else {

                            CustomToast.show(MeetingRoomActivity.this,
                                    personJoinMeetingCmd.getUserName() + "加入会议",
                                    Toast.LENGTH_LONG);

                        }
                        // for information report
                        InfoReportManager.setJoinNum(mParticipantorsData.getSize());
                    }
                    break;
                case NotifyType.PERSON_EXIT_MEETING:
                    Cmd personExitMeetingCmd = (Cmd) object;
                    Person exitMeetingPerson = new Person();
                    exitMeetingPerson.setAccountId(personExitMeetingCmd
                            .getAccountId());
                    exitMeetingPerson.setAccountName(personExitMeetingCmd
                            .getUserName());
                    mParticipantorsData.removeParticipantor(exitMeetingPerson);
                    mMenuView.setParticipatorCount(mParticipantorsData.getSize());

                    // MessageInfo exitMeetingMsgInfo = new MessageInfo();
                    // exitMeetingMsgInfo.type = MessageReminderManage.EXIT_MEETING;
                    // exitMeetingMsgInfo.msg = exitMeetingPerson.getAccountName() +
                    // "退出会议";
                    // mMessageReminderManage.sendMessage(exitMeetingMsgInfo);
                    if (!exitMeetingPerson.getAccountId().equalsIgnoreCase(
                            accoundid)) {
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    exitMeetingPerson.getAccountName() + "退出会诊",
                                    Toast.LENGTH_LONG);
                            mMenuView.hideSwitchVideoTypeView();
                        } else {
                            CustomToast.show(MeetingRoomActivity.this,
                                    exitMeetingPerson.getAccountName() + "退出会议",
                                    Toast.LENGTH_LONG);
                            mMenuView.hideSwitchVideoTypeView();
                        }
                    }
                    break;
            /*
             * case NotifyType.CHANGE_MODE: CustomLog.d(TAG,
			 * "NotifyType.CHANGE_MODE"); int mode = (Integer) object;
			 * changemode(mode); if (mode == ModeStatusCode.MODE_NO_SPEAKER) {
			 * showNoSpeakerTip(); } else { hideNoSpeakerTip(); } break;
			 */
                case NotifyType.SERVER_NOTICE_MEETING_MODE_CHANGE:
                    if (mButelOpenSDK.getMeetingMode() == MeetingStyleStatus.MASTER_MODE) {
                        CustomToast.show(MeetingRoomActivity.this, "主持人切换到主持模式",
                                CustomToast.LENGTH_LONG);
                    } else {
                        CustomToast.show(MeetingRoomActivity.this, "主持人切换到自由模式",
                                CustomToast.LENGTH_LONG);
                    }
                    mMenuView.setMeetingModel(mButelOpenSDK.getCurrentRole(),
                            mButelOpenSDK.getMeetingMode());

                    break;
                case NotifyType.ACCOUNT_CONTEXT_NOT_EXIST:
                    if (mIsBeKicked) {
                        return;
                    }
                    CustomToast.show(MeetingRoomActivity.this,
                            "服务器连接失败，请稍后再试（-900）！", CustomToast.LENGTH_SHORT);
                    exitMeeting(MeetingEvent.QUIT_MEETING_LIBS_ERROR);
                    break;
                case NotifyType.MANAGER_NOT_EXIST:
                    CustomToast.show(MeetingRoomActivity.this,
                            "服务器连接失败，请稍后再试（-900）！", CustomToast.LENGTH_SHORT);
                    exitMeeting(MeetingEvent.QUIT_MEETING_LIBS_ERROR);
                    break;
                case NotifyType.MEETING_CONTEXT_INIT_FAILED:
                    CustomToast.show(MeetingRoomActivity.this,
                            "服务器连接失败，请稍后再试（-900）！", CustomToast.LENGTH_SHORT);
                    exitMeeting(MeetingEvent.QUIT_MEETING_LIBS_ERROR);
                    break;
                case NotifyType.OTHER_EXCEPTION:
                    CustomToast.show(MeetingRoomActivity.this,
                            "服务器连接失败，请稍后再试（-900）！", CustomToast.LENGTH_SHORT);
                    exitMeeting(MeetingEvent.QUIT_MEETING_LIBS_ERROR);
                    break;
                case NotifyType.TOKEN_TIME_OUT:
                    CustomToast.show(MeetingRoomActivity.this,
                            "服务器连接失败，请稍后再试（-900）！", CustomToast.LENGTH_SHORT);
                    exitMeetingForTokenTimeOut(MeetingEvent.QUIT_TOKEN_DISABLED);
                    break;
                case NotifyType.EXCEPTION:
                    // showLoading("网络差重试中");
                    mMeetingInfo.mIsDealWith981 = true;
                    break;
                case NotifyType.SERVICE_NOTICE_981:
                    CustomLog.d(TAG, "SERVICE_NOTICE_981");
                    showLoading("网络差重试中");
                    mMeetingInfo.mIsDealWith981 = true;
                    break;
                case NotifyType.HANDLE_EXCEPTION_SUC:
                    boolean cmd = (Boolean) object;
                    CustomLog.d(TAG, "HANDLE_EXCEPTION_SUC " + cmd);
                    if (cmd) {
                        // TODO 重新获取数据
                        releaseView();
                        if (mMediaViewBg != null) {
                            mMediaViewBg.removeAllViews();
                            // mMediaView = null;
                        }
                        if (mMenuView != null) {
                            mMenuView.release();
                            // mMenuView = null;
                        }
                        initMenuView();
                        handleJoinMeetingSuc();
                    }
                    dismissLoading();
                    mMeetingInfo.mIsDealWith981 = false;

                    break;
                case NotifyType.HANDLE_EXCEPTION_FAIL:
                    CustomLog.d(TAG, "HANDLE_EXCEPTION_FAIL");
                    dismissLoading();
                    if (MeetingManager.getInstance().getAppType()
                            .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，您已退出会诊，请尝试重新参会", Toast.LENGTH_LONG);
                    } else {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，您已退出会议，请尝试重新参会", Toast.LENGTH_LONG);
                    }
                    CustomLog.d(TAG, "981处理失败，退出会议");
                    exitMeeting(MeetingEvent.QUIT_MEETING_SERVER_DESCONNECTED);
                    break;
                case NotifyType.BE_GRABED:
                    CustomToast.show(MeetingRoomActivity.this, "异地登录",
                            CustomToast.LENGTH_LONG);
                    exitMeeting(MeetingEvent.TOKEN_DISABLED);
                    break;
                case NotifyType.START_MEETING_FAIL:

                    if (object != null) {
                        String info = (String) object;
                        if (info.equals(START_CAMERA_FAILED)) {
                            mDialog = new CustomDialog(MeetingRoomActivity.this);
                            mDialog.setTitle("退出");
                            // mDialog.setCancelable(false);
                            mDialog.removeCancelBtn();
                            mDialog.setTip("无法获取摄像头数据，您可通过在手机“设置-安全-权限管理器”中打开权限。");
                            mDialog.setOkBtnText("确定");
                            // mDialog.setCancelBtnText("取消");
                            mDialog.setBlackTheme();
                            mDialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {
                                @Override
                                public void onClick(CustomDialog customDialog) {
                                    exitMeeting(MeetingEvent.QUIT_MEETING_CAMERA_FAILED);
                                    // dismissDialog();
                                }
                            });
                            mDialog.show();

                        }
                    } else {
                        dismissLoading();
                        exitMeeting(MeetingEvent.QUIT_MEETING_CAMERA_FAILED);
                    }

                    break;
                case NotifyType.CHANGE_TO_VGA:
                    CustomToast.show(MeetingRoomActivity.this, "网络良好,切换到高画质",
                            CustomToast.LENGTH_LONG);
                    break;
                case NotifyType.CHANGE_TO_QVGA:
                    CustomToast.show(MeetingRoomActivity.this, "网络差,切换到低画质",
                            CustomToast.LENGTH_LONG);
                    break;
                case NotifyType.MEDIAPLAY_REMOTE_NETWORK_POOR:
                    CustomToast.show(MeetingRoomActivity.this, "对方网络差",
                            CustomToast.LENGTH_LONG);
                    break;
                case NotifyType.MEDIAPLAY_LOCAL_NETWORK_POOR:
                    CustomToast.show(MeetingRoomActivity.this, "本地网络差",
                            CustomToast.LENGTH_LONG);
                    break;
                case NotifyType.MEDIAPLAY_ALL_NETWORK_POOR:
                    CustomToast.show(MeetingRoomActivity.this, "双方网络差",
                            CustomToast.LENGTH_LONG);
                    break;
                case NotifyType.EPISODE_ADD:
                    EpisodeData addData = new EpisodeData();
                    addData.setAccountId((((EpisodeData) object).getAccountId()));
                    addData.setAccountName(((EpisodeData) object).getAccountName());
                    CustomLog.d(TAG, "NotifyType.EPISODE_ADD "
                            + ((EpisodeData) object).getAccountId());
                    synchronized (episodeList) {
                        episodeList.put(addData.getAccountId(), addData);
                        if (episodeList.size() > 0) {
                            mMenuView.showEpisodeReminderView();
                            mMenuView
                                    .refreshEpisodeReminderView(new ArrayList<EpisodeData>(
                                            episodeList.values()));
                        }
                    }
                    break;
                case NotifyType.EPISODE_REMOVE:
                    CustomLog.d(TAG, "NotifyType.EPISODE_ADD "
                            + ((EpisodeData) object).getAccountId());
                    EpisodeData removeData = new EpisodeData();
                    removeData
                            .setAccountId((((EpisodeData) object).getAccountId()));
                    removeData.setAccountName(((EpisodeData) object)
                            .getAccountName());
                    synchronized (episodeList) {
                        if (episodeList.containsKey(removeData.getAccountId())) {
                            episodeList.remove(removeData.getAccountId());
                        }
                        if (episodeList.size() > 0) {
                            mMenuView.showEpisodeReminderView();
                            mMenuView
                                    .refreshEpisodeReminderView(new ArrayList<EpisodeData>(
                                            episodeList.values()));
                        } else {
                            mMenuView.hideEpisodeReminderView();
                        }
                    }
                    removeData = null;
                    break;
                case NotifyType.EPISODE_STOP:
                    if (episodeState == 1) {
                        dismissLoading();
                        mMenuView.hideEpisodeView();
                        mMenuView.hideEpisodeReminderView();
                        episodeState = 0;
                    }
                    break;
                case NotifyType.REMOTE_OPEN_MIC_CAMERA:
                    Cmd remoteOpenCmd = (Cmd) object;
                    CustomLog.d(TAG, remoteOpenCmd.getAccountId() + "打开了摄像头");
                    // CustomToast.show(MeetingRoomActivity.this,
                    // remoteOpenCmd.getUserName() + "打开了摄像头",
                    // Toast.LENGTH_LONG);
                    break;
                case NotifyType.REMOTE_CLOSE_MIC_CAMERA:
                    Cmd remoteCloseCmd = (Cmd) object;
                    // CustomToast.show(MeetingRoomActivity.this,
                    // remoteCloseCmd.getUserName() + "关闭了摄像头",
                    // Toast.LENGTH_LONG);
                    CustomLog.d(TAG, remoteCloseCmd.getAccountId() + "关闭了摄像头");
                    break;
            /*
             * case NotifyType.GET_NET_INFO: CustomLog.d( TAG,
			 * "GET_NET_INFO: Rssi = " + PhoneStateMonitor.getInstance(
			 * MeetingRoomActivity.this).getRSSI() + " Speed = " +
			 * PhoneStateMonitor.getInstance( MeetingRoomActivity.this)
			 * .getLinkSpeed());
			 *
			 * mButelOpenSDK.setNetInfo(CommonUtil.getLocalIpAddress(),
			 * getNetType(),
			 * PhoneStateMonitor.getInstance(MeetingRoomActivity.this)
			 * .getLinkSpeed(),
			 * PhoneStateMonitor.getInstance(MeetingRoomActivity.this)
			 * .getRSSI()); break;
			 */
                case NotifyType.AUDIO_IN_DEVICE_INIT_FAILED:
                    if (mSwitchAudioAlertDialog != null
                            && mSwitchAudioAlertDialog.isShowing()) {
                        CustomLog.d(TAG, "提示音频采集设备初始化失败弹框已显示，不再显示");
                        return;
                    } else {
                        mSwitchAudioAlertDialog = new CustomDialog(
                                MeetingRoomActivity.this);
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            mSwitchAudioAlertDialog
                                    .setTip("音频采集出现异常，请检查麦克是否正常或退出会诊重试！");
                        } else {
                            mSwitchAudioAlertDialog
                                    .setTip("音频采集出现异常，请检查麦克是否正常或退出会议重试！");
                        }
                        mSwitchAudioAlertDialog.setOkBtnText("知道了");
                        mSwitchAudioAlertDialog.removeCancelBtn();
                        mSwitchAudioAlertDialog.setBlackTheme();
                        mSwitchAudioAlertDialog
                                .setOkBtnOnClickListener(new OKBtnOnClickListener() {

                                    @Override
                                    public void onClick(CustomDialog customDialog) {
                                        customDialog.dismiss();
                                        mSwitchAudioAlertDialog = null;
                                    }

                                });
                        mSwitchAudioAlertDialog.show();
                    }
                    break;
                case NotifyType.AUDIO_OUT_DEVICE_INIT_FAILED:
                    if (mSwitchPlayAlertDialog != null
                            && mSwitchPlayAlertDialog.isShowing()) {
                        CustomLog.d(TAG, "提示音频播放出现异常弹框已显示，不再显示");
                        return;
                    } else {
                        mSwitchPlayAlertDialog = new CustomDialog(
                                MeetingRoomActivity.this);
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            mSwitchPlayAlertDialog
                                    .setTip("音频播放出现异常，请检查扬声器是否正常或退出会诊重试！");
                        } else {
                            mSwitchPlayAlertDialog
                                    .setTip("音频播放出现异常，请检查扬声器是否正常或退出会议重试！");
                        }
                        mSwitchPlayAlertDialog.setOkBtnText("知道了");
                        mSwitchPlayAlertDialog.removeCancelBtn();
                        mSwitchPlayAlertDialog.setBlackTheme();
                        mSwitchPlayAlertDialog
                                .setOkBtnOnClickListener(new OKBtnOnClickListener() {

                                    @Override
                                    public void onClick(CustomDialog customDialog) {
                                        customDialog.dismiss();
                                        mSwitchPlayAlertDialog = null;
                                    }

                                });
                        mSwitchPlayAlertDialog.show();
                    }
                    break;
                case NotifyType.CAMERA_OPEN_FAILED:
                    if (mSwitchVideoAlertDialog != null
                            && mSwitchVideoAlertDialog.isShowing()) {
                        CustomLog.d(TAG, "提示视频采集出现异常弹框已显示，不再显示");
                        return;
                    } else {
                        mSwitchVideoAlertDialog = new CustomDialog(
                                MeetingRoomActivity.this);
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            mSwitchVideoAlertDialog
                                    .setTip("视频采集出现异常，请检查摄像头是否正常或退出会诊重试！");
                        } else {
                            mSwitchVideoAlertDialog
                                    .setTip("视频采集出现异常，请检查摄像头是否正常或退出会议重试！");
                        }
                        mSwitchVideoAlertDialog.setOkBtnText("知道了");
                        mSwitchVideoAlertDialog.removeCancelBtn();
                        mSwitchVideoAlertDialog.setBlackTheme();
                        mSwitchVideoAlertDialog
                                .setOkBtnOnClickListener(new OKBtnOnClickListener() {

                                    @Override
                                    public void onClick(CustomDialog customDialog) {
                                        customDialog.dismiss();
                                        mSwitchVideoAlertDialog = null;
                                    }

                                });
                        mSwitchVideoAlertDialog.show();
                    }
                    break;
                case NotifyType.AUTO_SWITCH_TO_AUDIO:
                    CustomLog.d(TAG, "底层提示切换到语音模式！");

                    if (mMenuView.getisCloseMoive()) {
                        CustomLog.d(TAG, "已经是语音模式，不做处理！");
                        return;
                    }

                    if (mSwitchAudioModeDialog != null
                            && mSwitchAudioModeDialog.isShowing()) {
                        CustomLog.d(TAG, "提示关闭摄像头弹框已显示，不再显示");
                        return;
                    } else {
                        mSwitchAudioModeDialog = new CustomDialog(
                                MeetingRoomActivity.this);
                        // mDialog.setCancelable(false);
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            mSwitchAudioModeDialog
                                    .setTip("当前网络不给力，建议您关闭视频画面，切换为语音会诊");
                        } else {
                            mSwitchAudioModeDialog
                                    .setTip("当前网络不给力，建议您关闭视频画面，切换为语音会议");
                        }
                        mSwitchAudioModeDialog.setOkBtnText("立即切换");
                        mSwitchAudioModeDialog.setCancelBtnText("取消");
                        mSwitchAudioModeDialog.setBlackTheme();

                        mSwitchAudioModeDialog
                                .setOkBtnOnClickListener(new OKBtnOnClickListener() {
                                    @Override
                                    public void onClick(CustomDialog customDialog) {

                                        CustomLog.d(TAG, "底层提示切换到语音模式，点击“立即切换”");

                                        int result = mButelOpenSDK
                                                .switchVideoOrAudioMode(1,
                                                        mAskForswitchVideoModeListener);
                                        if (result ==
                                                ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                                            CustomLog.e(TAG, "切到语音模式");
                                            MeetingRoomActivity.this
                                                    .showLoadingView("请求中...");

                                        } else {
                                            if (result ==
                                                    ButelOpenSDKReturnCode.RETURN_NONEED_CALLBACK_SUCCESS) {
                                                CustomToast.show(
                                                        getApplicationContext(),
                                                        "切换到语音模式成功",
                                                        Toast.LENGTH_SHORT);
                                                mMenuView.setisCloseMoive(true);
                                                ownSpeak oSpeak = isOwnSpeak();
                                                if (oSpeak == ownSpeak.SpeakOnMic) {
                                                    isOpenCamera = false;
                                                }
                                                setPositionOnVideoMode();
                                            } else {
                                                CustomToast.show(
                                                        getApplicationContext(),
                                                        "切换到语音模式失败",
                                                        Toast.LENGTH_SHORT);
                                            }
                                        }
                                        customDialog.dismiss();
                                    }
                                });

                        mSwitchAudioModeDialog
                                .setCancelBtnOnClickListener(new CancelBtnOnClickListener() {
                                    @Override
                                    public void onClick(CustomDialog customDialog) {
                                        customDialog.dismiss();
                                        CustomLog.d(TAG, "提示切换到语音模式，点击取消");
                                    }
                                });
                        mSwitchAudioModeDialog.show();
                    }
                    break;
                case NotifyType.SERVER_NOTICE_USER_ASK_FOR_RAISE_HAND:
                    Cmd newSpeakerResp1 = (Cmd) object;
                    if (newSpeakerResp1 != null
                            && newSpeakerResp1.getUserName() != null) {
                        CustomToast.show(MeetingRoomActivity.this,
                                newSpeakerResp1.getUserName() + "申请发言",
                                Toast.LENGTH_LONG);
                    }
                    break;
                case NotifyType.SERVER_NOTICE_LIVE:
                    mMenuView.setLiveTileShow(1);
                    break;
                case NotifyType.BE_KICKED:
                    CustomLog.e(TAG, "MeetingRoomActivity BE_KICKED");
                    Cmd respModel = (Cmd) object;
                    if (respModel != null
                            && respModel.getAccountId() != null
                            && respModel.getAccountId().equals(
                            mMeetingInfo.accountId)) {
                        CustomLog.e(TAG, "MeetingRoomActivity BE_KICKED "
                                + respModel);

                        mIsBeKicked = true;

                        // 退出会议
                        exitMeeting(MeetingEvent.QUIT_MEETING_BACK);

                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(getApplicationContext(),
                                    "您已被主持人请出会诊室", CustomToast.LENGTH_LONG);
                        } else {
                            CustomToast.show(getApplicationContext(),
                                    "您已被主持人请出会议室", CustomToast.LENGTH_LONG);
                        }

                    }
                    break;
                case NotifyType.MASTER_OPERATE_USER_LOUDSPEAKER:
                    CustomLog.e(TAG, "主持人控制参会方切换扬声器模式 ");
                    isAudioSpeakerClose = false;
                    if (getMyAudioSpeakerState() > 0) {
                        int res = 0;
                        if (getMyAudioSpeakerState() ==
                                cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN
                                && ((Cmd) object).getNewStreamOperatorType() == 2) {
                            // TODO
                            mButelOpenSDK.masterCloseUserLoudSpeaker();
                            isAudioSpeakerClose = true;
                        } else if (getMyAudioSpeakerState() ==
                                cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_CLOSE
                                && ((Cmd) object).getNewStreamOperatorType() == 1) {
                            mButelOpenSDK.masterOpenUserLoudSpeaker();
                        }

                    }
                    break;
                case NotifyType.MASTER_OPEN_USERLOUDSPEAKER_SUCCESS:
                    CustomLog.e(TAG, "主持人控制打开参会者扬声器成功");
                    masterOpenUserLoudspeakerSuccess();
                    break;
                case NotifyType.MASTER_OPEN_USERLOUDSPEAKER_FAIL:
                    CustomLog.e(TAG, "主持人控制打开参会者扬声器失败");
                    masterOpenUserLoudspeakerFail();
                    break;
                case NotifyType.MASTER_CLOSE_USERLOUDSPEAKER_SUCCESS:
                    CustomLog.e(TAG, "主持人控制关闭参会者扬声器成功");
                    masterCloseUserLoudspeakerSuccess();
                    break;
                case NotifyType.MASTER_CLOSE_USERLOUDSPEAKER_FAIL:
                    CustomLog.e(TAG, "主持人控制关闭参会者扬声器失败");
                    masterCloseUserLoudspeakerFail();
                    break;
                case NotifyType.USER_OPEN_LOUNDSPEAKER:
                    Cmd personOpenLoudspeaker = (Cmd) object;
                    CustomLog.d(TAG, personOpenLoudspeaker.getAccountId()
                            + "打开了扬声器");

                    boolean matchFlag5 = false;
                    for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
                        if (mLoudspeakerInfoList.get(a)
                                .getAccountId()
                                .equals(personOpenLoudspeaker.getAccountId())) {
                            mLoudspeakerInfoList.get(a).setLoudspeakerStatus(1);
                            matchFlag5 = true;
                            break;
                        }
                    }

                    if (!matchFlag5) {
                        LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
                        loudspeakerInfo.setAccountId(personOpenLoudspeaker.getAccountId());
                        loudspeakerInfo.setLoudspeakerStatus(1);
                        mLoudspeakerInfoList.add(loudspeakerInfo);
                    }

                    for (int i = 0; i < ParticipatorsView.mDataList.size(); i++) {
                        if (ParticipatorsView.mDataList.get(i).getAccountId()
                                .equals(personOpenLoudspeaker.getAccountId())) {
                            ParticipatorsView.mDataList.get(i)
                                    .setLoudSpeakerStatus(1);
                            CustomLog.d(TAG,
                                    "打开了扬声器后ParticipatorsView.mDataList.get(i)的扬声器状态："
                                            + ParticipatorsView.mDataList.get(i)
                                            .getLoudSpeakerStatus());
                        }
                    }

                    ParticipatorsView.mParticipatorListViewAdapter
                            .notifyDataSetChanged();
                    break;
                case NotifyType.USER_CLOSE_LUNDSPEAKER:
                    Cmd personCloseLoudspeaker = (Cmd) object;
                    CustomLog.d(TAG, personCloseLoudspeaker.getAccountId()
                            + "关闭了扬声器");

                    boolean matchFlag6 = false;
                    for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
                        if (mLoudspeakerInfoList.get(a)
                                .getAccountId()
                                .equals(personCloseLoudspeaker.getAccountId())) {
                            mLoudspeakerInfoList.get(a).setLoudspeakerStatus(2);
                            matchFlag6 = true;
                            break;
                        }
                    }

                    if (!matchFlag6) {
                        LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
                        loudspeakerInfo.setAccountId(personCloseLoudspeaker.getAccountId());
                        loudspeakerInfo.setLoudspeakerStatus(2);
                        mLoudspeakerInfoList.add(loudspeakerInfo);
                    }

                    for (int i = 0; i < ParticipatorsView.mDataList.size(); i++) {
                        if (ParticipatorsView.mDataList.get(i).getAccountId()
                                .equals(personCloseLoudspeaker.getAccountId())) {
                            ParticipatorsView.mDataList.get(i)
                                    .setLoudSpeakerStatus(2);
                            CustomLog.d(TAG,
                                    "关闭了扬声器后ParticipatorsView.mDataList.get(i)的扬声器状态："
                                            + ParticipatorsView.mDataList.get(i)
                                            .getLoudSpeakerStatus());
                        }
                    }

                    ParticipatorsView.mParticipatorListViewAdapter
                            .notifyDataSetChanged();
                    break;

                case NotifyType.MASTER_OPEN_USER_CAMERA:
                    isOpenCamera = false;
                    masterOperateUserCamera = true;
                    handleCamare();
                    break;

                case NotifyType.MASTER_CLOSE_USER_CAMERA:
                    isOpenCamera = true;
                    masterOperateUserCamera = true;
                    handleCamare();
                    break;

                case NotifyType.MASTER_OPEN_USER_MIC:
                    if (getMyMicphoneState() > 0 &&
                            getMyMicphoneState() == SpeakerInfo.MIC_STATUS_OFF) {
                        masterOperateUserMic = true;
                        int res = 0;
                        res = mButelOpenSDK
                                .openMicrophone(mAskForSwitchMicModeListener);
                        isMicClose = false;
                        if (res == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                            showLoading("请求中...");
                        } else {
                            CustomToast.show(getApplicationContext(),
                                    "切换静音模式失败", Toast.LENGTH_LONG);
                            masterOperateUserMic = false;
                        }
                    }
                    break;

                case NotifyType.MASTER_CLOSE_USER_MIC:
                    if (getMyMicphoneState() > 0 &&
                            getMyMicphoneState() == SpeakerInfo.MIC_STATUS_ON) {
                        masterOperateUserMic = true;
                        int res = 0;
                        res = mButelOpenSDK
                                .closeMicrophone(mAskForSwitchMicModeListener);
                        isMicClose = true;
                        if (res == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                            showLoading("请求中...");
                        } else {
                            CustomToast.show(getApplicationContext(),
                                    "切换静音模式失败", Toast.LENGTH_LONG);
                            masterOperateUserMic = false;
                        }
                    }
                    break;

                case NotifyType.SERVER_NOTICE_STREAM_PUBLISH:
                    respModel = (Cmd) object;
                    if (respModel == null) {
                        return;
                    }

                    if (respModel.getMediaType() == MediaType.TYPE_AUDIO) {
                        if (getMyAudioSpeakerState() ==
                                cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_CLOSE) {
                            mButelOpenSDK.personJoinMeetingCloseUserLoudSpeaker();
                            isAudioSpeakerClose = true;
                        }
                    }
                    break;

                case NotifyType.NOTIFY_CPU_OVERLOAD:
                    CustomLog.d(TAG, "NOTIFY_CPU_OVERLOAD");
                    handleCpuOverload();
                    break;

                case NotifyType.NOTIFY_NET_STATUS:
                    CustomLog.d(TAG, "NOTIFY_NET_STATUS");
                    int netStatus = (Integer) object;
                    if (menuViewisInit) {
                        if (mMenuView != null) {
                            mMenuView.setNetStatusView(netStatus);
                        }
                    } else {

                    }
                    break;

                case NotifyType.NOTIFY_VIDEO_CLOSE_ADAPT:
                    CustomLog.d(TAG, "NOTIFY_VIDEO_CLOSE_ADAPT");
                    handleVideoCloseAdapt();
                    break;

                default:
                    break;
            }
        }
    };


    private void handleCpuOverload() {
        if (handleCpuOverloadEvent == true) {
            CustomLog.d(TAG, "handleCpuOverload,handleEvent == true");
            return;
        } else {
            CustomLog.d(TAG, "handleCpuOverload,handleEvent == false");
            CustomToast.show(MeetingRoomActivity.this, "CPU已过载，建议关闭摄像头", Toast.LENGTH_LONG);
            handleCpuOverloadEvent = true;
            Message msg = Message.obtain();
            msg.what = handleCpuOverloadMsg;
            handleEventHandler.sendMessageDelayed(msg, 120000);
        }
    }


    private void handleVideoCloseAdapt() {
        if (handleVideoCloseAdaptEvent == true) {
            CustomLog.d(TAG, "handleVideoCloseAdapt,handleVideoCloseAdaptEvent == true");
            return;
        } else {
            CustomLog.d(TAG, "handleVideoCloseAdapt,handleVideoCloseAdaptEvent == false");
            CustomToast.show(MeetingRoomActivity.this, "带宽不足，已关闭视频", Toast.LENGTH_LONG);
            handleVideoCloseAdaptEvent = true;
            Message msg = Message.obtain();
            msg.what = handleVideoCloseAdaptMsg;
            handleEventHandler.sendMessageDelayed(msg, 120000);
        }
    }


    Handler handleEventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case handleCpuOverloadMsg:
                    handleCpuOverloadEvent = false;
                    break;
                case handleVideoCloseAdaptMsg:
                    handleVideoCloseAdaptEvent = false;
                    break;
            }
        }
    };

	/*
     * private void changemode(CameraType code) { DisplayMetrics metric = new
	 * DisplayMetrics();
	 * getWindowManager().getDefaultDisplay().getMetrics(metric); float density
	 * = metric.density; CustomLog.e(TAG, code + "=moshi"); // boolean isMicNull
	 * = isMisExistNull(); // ownSpeak oSpeak = isOwnSpeak(); currentMode =
	 * code; mMenuView.showCameraView(code, density); }
	 */


    private ownSpeak isOwnSpeak() {

        if (!mMeetingInfo.accountId.equalsIgnoreCase("")
                && mButelOpenSDK.getSpeakerInfoById(mMeetingInfo.accountId) != null) {
            return ownSpeak.SpeakOnMic;
        }

		/*
         * if (mButelOpenSDK.getMic1UserId() != null &&
		 * !mButelOpenSDK.getMic1UserId().equalsIgnoreCase("")) {
		 * CustomLog.e(TAG, mButelOpenSDK.getMic1UserId() +
		 * "=mButelOpenSDK.getMic1UserId()"); CustomLog .e(TAG,
		 * mMeetingInfo.accountId + "=mMeetingInfo.accountId"); if
		 * (!mMeetingInfo.accountId.equalsIgnoreCase("") &&
		 * mMeetingInfo.accountId
		 * .equalsIgnoreCase(mButelOpenSDK.getMic1UserId())) { return
		 * ownSpeak.SpeakOneMic; } } if (mButelOpenSDK.getMic2UserId() != null
		 * && !mButelOpenSDK.getMic2UserId().equalsIgnoreCase("")) {
		 * CustomLog.e(TAG, mButelOpenSDK.getMic2UserId() +
		 * "=mButelOpenSDK.getMic2UserId()"); CustomLog .e(TAG,
		 * mMeetingInfo.accountId + "=	mMeetingInfo.accountId"); if (!
		 * mMeetingInfo.accountId.equalsIgnoreCase("") && mMeetingInfo.accountId
		 * .equalsIgnoreCase(mButelOpenSDK.getMic2UserId())) return
		 * ownSpeak.SpeakTwoMic; }
		 */
        return ownSpeak.noSpeak;

    }


    private boolean isMisExistNull() {
        if (mButelOpenSDK.getSpeakers() == null
                || mButelOpenSDK.getSpeakers().size() == 0) {
            return true;
        }
        /*
         * if (mButelOpenSDK.getMic1UserId() == null ||
		 * mButelOpenSDK.getMic1UserId().equalsIgnoreCase("") ||
		 * mButelOpenSDK.getMic2UserId() == null ||
		 * mButelOpenSDK.getMic2UserId().equalsIgnoreCase("")) return true;
		 */
        else {
            return false;
        }
    }


    private GetMeetingPwd getPwd;
    private CheckHasMeetingPwd checkPwd;


    private void handleJoinMeetingSuc() {
        isBluetoothConnectState();
        bluetooth();
        handleUVCCamera();
        getOrientation();
        mMeetingInfo.lockInfo = mButelOpenSDK.getLockInfo();
        CustomLog.d(TAG, "mMeetingInfo.lockInfo：" + mMeetingInfo.lockInfo);
        mMenuView.setLock(mMeetingInfo.lockInfo);
        mMenuView.setMeetingRole(mButelOpenSDK.getCurrentRole());
        mMenuView.setMeetingModel(mButelOpenSDK.getCurrentRole(),
                mButelOpenSDK.getMeetingMode());
        mMenuView.setLiveTileShow(mButelOpenSDK.getLiveStatus());
        // showLoading("获取参会列表");
        // mButelOpenSDK.getParticipatorList(mGetParticipatorListListener);
        List<Person> mMeetingControlParticipatorsList = new ArrayList<Person>();
        JSONArray paticitors = mButelOpenSDK.getParticipatorList();
        CustomLog.d(TAG, "参会者列表：" + paticitors);
        if (paticitors != null) {

            for (int i = 0; i < paticitors.length(); i++) {
                try {
                    JSONObject paticitorObj = paticitors.getJSONObject(i);
                    Person p = new Person();
                    p.setAccountId(paticitorObj.optString(CmdKey.ACOUNT_ID));
                    p.setAccountName(paticitorObj.optString(CmdKey.USERNAME));

                    if (p.getAccountId().equals(mMeetingInfo.accountId)) {
                        isAudioSpeakerClose = false;
                        if (getMyAudioSpeakerState() > 0) {
                            int res = 0;
                            if (getMyAudioSpeakerState() ==
                                    cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN) {
                                p.setLoudSpeakerStatus(1);
                            } else {
                                p.setLoudSpeakerStatus(1);
                                res = mButelOpenSDK
                                        .askForOpenLoudspeaker(mAskForSwitchAudioSpeakerModeListener);
                                if (res == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                                    showLoading("请求中...");

                                } else {
                                    CustomToast.show(getApplicationContext(), "切换扬声器模式失败",
                                            Toast.LENGTH_LONG);
                                }
                            }
                        }
                    } else {
                        p.setLoudSpeakerStatus(paticitorObj
                                .optInt("loudSpeakerStatus"));
                    }

                    CustomLog.d(TAG, "参会者姓名：" + p.getAccountName());
                    CustomLog.d(TAG, "参会者帐号：" + p.getAccountId());
                    CustomLog.d(TAG, "参会者扬声器初始状态" + p.getLoudSpeakerStatus());
                    mMeetingControlParticipatorsList.add(p);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        mParticipantorsData.clear();
        mParticipantorsData.addParticipantors(mMeetingControlParticipatorsList);
        mMenuView.setParticipatorCount(mParticipantorsData.getSize());

        CustomLog.d(TAG,
                "mLoudspeakerInfoList.size():" + String.valueOf(mLoudspeakerInfoList.size()));
        boolean matchFlag16 = false;
        for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
            if (mLoudspeakerInfoList.get(a).getAccountId().equals(mMeetingInfo.accountId)) {
                mLoudspeakerInfoList.get(a).setLoudspeakerStatus(1);
                matchFlag16 = true;
                break;
            }
        }

        if (!matchFlag16) {
            LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
            loudspeakerInfo.setAccountId(mMeetingInfo.accountId);
            loudspeakerInfo.setLoudspeakerStatus(1);
            mLoudspeakerInfoList.add(loudspeakerInfo);
        }

        // TODO 指定发言列表处理数据
        // TODO 获取发言者消息
        List<SpeakerInfo> speakInfoList = new ArrayList<SpeakerInfo>();
        speakInfoList.addAll(mButelOpenSDK.getSpeakers());
        // List<SpeakerInfo> speakInfoList = mButelOpenSDK.getSpeakers();
        CustomLog.d(TAG, "四方列表speakInfoList：" + speakInfoList);
        // 解析数据，初始化四方页面
        if (speakInfoList != null && speakInfoList.size() > 0) {
            SpeakerInfo speakInfo = null;
            boolean isShowShare = false;
            mMenuView.setSpeakerList(speakInfoList);
            for (int i = 0; i < speakInfoList.size(); i++) {
                speakInfo = speakInfoList.get(i);
                CustomLog.d(TAG, "四方列表：" + speakInfo);
                speakerOnLine(speakInfo.getAccountId(),
                        speakInfo.getAccountName());
                /*
                 * if (speakInfo.getSpeakerType() ==
				 * SpeakerInfo.SPEAKER_TYPE_MAIN) { //
				 * mMenuView.getmMultiSpeakerView
				 * ().addSpeakerView(speakInfo.getAccountId(), //
				 * speakInfo.getAccountName()); //
				 * mMenuView.getmMultiSpeakerView
				 * ().setMaster(speakInfo.getAccountId(), //
				 * speakInfo.getAccountName()); } else
				 */
                if (speakInfo.getScreenShareStatus() == SpeakerInfo.SCREEN_SHARING) {
                    isShowShare = true;
                    mMenuView.getmMultiSpeakerView().addSpeakerView(
                            speakInfo.getAccountId(),
                            speakInfo.getAccountName(), true);
                    mMenuView.getmMultiSpeakerView().addShareDocView(
                            speakInfo.getAccountId(),
                            speakInfo.getAccountName(), true);
                } else {
                    mMenuView.getmMultiSpeakerView().addSpeakerView(
                            speakInfo.getAccountId(),
                            speakInfo.getAccountName(), true);
                }
            }

            mMenuView.getmMultiSpeakerView().setMultiViewListBg();
            //            mMenuView.setSpeakerList(speakInfoList);
            /*
			 * SpeakerInfo micInfo = mButelOpenSDK
			 * .getSpeakerInfoById(mMeetingInfo.accountId); if (micInfo != null)
			 * { CustomLog.d(TAG,
			 * "???????????????????????   "+mButelOpenSDK.getSpeakers().size());
			 * mMenuView.setMicModeState(true, micInfo.getMICStatus()); } else {
			 * CustomLog.d(TAG,
			 * ",,,,,,,,,,,,,,,,,,,,,,,,,,,,   "+mButelOpenSDK.getSpeakers
			 * ().size()); mMenuView.setMicModeState(false,
			 * SpeakerInfo.MIC_STATUS_ON); }
			 */
        }
        if (mButelOpenSDK.getSpeakers() != null
                && mButelOpenSDK.getSpeakers().size() > 0) {
            CustomLog.d(TAG, "tttttttttttttttttt   "
                    + mButelOpenSDK.getSpeakers().size());
            hideNoSpeakerTip();
        } else {
            showNoSpeakerTip();
        }
        mMenuView.showMultiSpeakView();
        mMenuView.showCameraView(CameraType.OwnSpeak, 0);
        mMenuView.handleIconShowType();
        // mMenuView.setKey(0, "443322");
        // dismissLoading();
        mMeetingInfo.isJoinMeeting = true;
        InfoReportManager.setJoinNum(mParticipantorsData.getSize());
        getPwd = new GetMeetingPwd() {

            @Override
            protected void onSuccess(GetMeetingPwdInfo responseContent) {
                dismissLoading();
                if (responseContent != null) {
                    String p = responseContent.meetingPwd;
                    CustomLog.e(TAG, "GetMeetingPwd onSuccess " + p);
                    if (p != null && !p.equals("")) {
                        mMenuView.setKey(0, p);
                    }
                }
                super.onSuccess(responseContent);
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                dismissLoading();
                CustomLog.e(TAG, "GetMeetingPwd onFail " + statusCode + ","
                        + statusInfo);
                super.onFail(statusCode, statusInfo);
            }

        };
        checkPwd = new CheckHasMeetingPwd() {

            @Override
            protected void onSuccess(CheckHasMeetingPwdInfo responseContent) {
                CustomLog.e(TAG, "CheckHasMeetingPwd onSuccess ");
                if (responseContent != null
                        && responseContent.hasMeetingPwd == 1) {
                    CustomLog.e(TAG,
                            "CheckHasMeetingPwd onSuccess hasMeetingPwd "
                                    + responseContent.hasMeetingPwd);
                    if (mButelOpenSDK != null
                            && mButelOpenSDK.getMasterAccountId().equals(
                            mMeetingInfo.accountId)) {
                        getPwd.getMeetingPwd(mMeetingInfo.token,
                                mMeetingInfo.meetingId);
                    } else {
                        dismissLoading();
                        mMenuView.setKey(0, "");
                    }
                } else {
                    dismissLoading();
                }
                super.onSuccess(responseContent);
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.e(TAG, "CheckHasMeetingPwd onFail " + statusCode
                        + "," + statusInfo);
                dismissLoading();
                super.onFail(statusCode, statusInfo);
            }

        };
        checkPwd.checkHasMeetingPwd(mMeetingInfo.meetingId);

    }


    private ButelOpenSDKOperationListener mJoinMeetingListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object object) {
            // initMenuView();
            if (MeetingManager.getInstance().getAppType()
                    .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                CustomToast.show(MeetingRoomActivity.this, "加入会诊成功",
                        CustomToast.LENGTH_SHORT);
            } else {
                CustomToast.show(MeetingRoomActivity.this, "加入会议成功",
                        CustomToast.LENGTH_SHORT);
            }

            sendMeetingOperationBroadCast(MeetingEvent.MEETING_JOINMEETING);
            // mButelOpenSDK.setVideoCaptureRate(MeetingManager.getInstance()
            // .getCameraCollectionMode());
            Cmd cmd = (Cmd) object;
			/*
			 * if (cmd.getMic1UserId() != null &&
			 * !cmd.getMic1UserId().equals("")) { speakerOnLine(1,
			 * cmd.getMic1UserId(), cmd.getMic1UserName()); } if
			 * (cmd.getMic2UserId() != null && !cmd.getMic2UserId().equals(""))
			 * { speakerOnLine(2, cmd.getMic2UserId(), cmd.getMic2UserName()); }
			 */
            handleJoinMeetingSuc();
        }


        @Override
        public void onOperationFail(Object object) {
            int code = MeetingEvent.QUIT_MEETING_OTHER_PROBLEM;
            Cmd cmd = (Cmd) object;
            if (mMeetingInfo.mIsDealWith981) {
                if (MeetingManager.getInstance().getAppType()
                        .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                    CustomToast.show(MeetingRoomActivity.this,
                            "网络不给力，您已退出会诊，请尝试重新参会", Toast.LENGTH_LONG);
                } else {
                    CustomToast.show(MeetingRoomActivity.this,
                            "网络不给力，您已退出会议，请尝试重新参会", Toast.LENGTH_LONG);
                }
                String str = KeyEventConfig.DEAL_981_EVENT + "_fail_"
                        + mMeetingInfo.accountId + "_处理981失败";
                KeyEventWrite.write(str);
                CustomLog.d(TAG, "981处理失败，退出会议");
                code = MeetingEvent.QUIT_MEETING_SERVER_DESCONNECTED;
            } else {
                switch (cmd.getStatus()) {
                    case RespModelStatusCode.MEETING_FINISHED:
                        // if (MeetingManager
                        //     .getInstance()
                        //     .getAppType()
                        //     .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                        //     CustomToast.show(MeetingRoomActivity.this, "会诊已结束",
                        //         CustomToast.LENGTH_SHORT);
                        // } else {
                        //     CustomToast.show(MeetingRoomActivity.this, "会议已结束",
                        //         CustomToast.LENGTH_SHORT);
                        // }
                        code = MeetingEvent.QUIT_MEETING_AS_MEETING_END;
                        break;
                    case RespModelStatusCode.MEETING_LOCKED:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (MeetingManager
                                        .getInstance()
                                        .getAppType()
                                        .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                                    CustomToast.show(getApplicationContext(),
                                            "会诊已加锁，暂无法加入", CustomToast.LENGTH_LONG);
                                } else {
                                    CustomToast.show(getApplicationContext(),
                                            "会议已加锁，暂无法加入", CustomToast.LENGTH_LONG);
                                }
                            }

                        }, 2000);
                        code = MeetingEvent.QUIT_MEETING_LOCKED;
                        break;
                    case RespModelStatusCode.TIME_OUT:
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(MeetingRoomActivity.this, "加入会诊超时！",
                                    CustomToast.LENGTH_SHORT);
                        } else {
                            CustomToast.show(MeetingRoomActivity.this, "加入会议超时！",
                                    CustomToast.LENGTH_SHORT);
                        }
                        code = MeetingEvent.QUIT_MEETING_SERVER_DESCONNECTED;
                        break;
                    default:
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            CustomToast.show(MeetingRoomActivity.this, "加入会诊失败",
                                    CustomToast.LENGTH_SHORT);
                        } else {
                            CustomToast.show(MeetingRoomActivity.this, "加入会议失败",
                                    CustomToast.LENGTH_SHORT);
                        }
                        break;
                }
            }
            dismissLoading();
            exitMeeting(code);
        }
    };

	/*
	 * private ButelOpenSDKOperationListener mGetParticipatorListListener = new
	 * ButelOpenSDKOperationListener() {
	 *
	 * @Override public void onOperationSuc(Object object) {
	 * CustomToast.show(MeetingRoomActivity.this, "获取参会者列表成功",
	 * CustomToast.LENGTH_SHORT); List<Person> mMeetingControlParticipatorsList
	 * = new ArrayList<Person>(); Cmd cmd = (Cmd) object; CustomLog.d(TAG,
	 * "参会者列表：" + cmd.getParticipators()); JSONArray paticitors =
	 * cmd.getParticipators(); if (paticitors != null) { for (int i = 0; i <
	 * paticitors.length(); i++) { try { JSONObject paticitorObj =
	 * paticitors.getJSONObject(i); Person p = new Person();
	 * p.setAccountId(paticitorObj.optString(CmdKey.ACOUNT_ID));
	 * p.setAccountName(paticitorObj .optString(CmdKey.USERNAME));
	 * CustomLog.d(TAG, "参会者姓名：" + p.getAccountName()); CustomLog.d(TAG,
	 * "参会者帐号：" + p.getAccountId()); mMeetingControlParticipatorsList.add(p); }
	 * catch (JSONException e) { e.printStackTrace(); } } }
	 * mParticipantorsData.clear(); mParticipantorsData
	 * .addParticipantors(mMeetingControlParticipatorsList);
	 * mMenuView.setParticipatorCount(mParticipantorsData.getSize()); // TODO
	 * 指定发言列表处理数据
	 *
	 * dismissLoading(); mMeetingInfo.isJoinMeeting = true;
	 *
	 * InfoReportManager.setJoinNum(mParticipantorsData.getSize()); }
	 *
	 * @Override public void onOperationFail(Object object) {
	 * CustomToast.show(MeetingRoomActivity.this, "获取参会者列表失败",
	 * CustomToast.LENGTH_SHORT); dismissLoading(); exitMeeting(); } };
	 */

    private ButelOpenSDKOperationListener mAskForSpeakListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object object) {
            CustomToast.show(MeetingRoomActivity.this, "申请发言成功",
                    CustomToast.LENGTH_SHORT);
            Cmd speakerOnLineCmd = (Cmd) object;
            if (speakerOnLineCmd != null
                    && speakerOnLineCmd.getAccountId().equals(
                    mMeetingInfo.accountId)) {
                String accountName;
                accountName = mMeetingInfo.accountName;
                speakerOnLine(speakerOnLineCmd.getAccountId(), accountName);
            }
            mMenuView.dismissAskForSpeakView();
            dismissLoading();
        }


        @Override
        public void onOperationFail(Object object) {
            dismissLoading();
            if (object != null) {
                Cmd cmd = (Cmd) object;
                if (cmd.getStatus() == -923) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }
            if (mMeetingInfo.mIsDealWith981) {
                showFailToast();
            } else {
                if (object != null) {
                    CustomToast.show(MeetingRoomActivity.this, "网络不给力，请重试！",
                            CustomToast.LENGTH_SHORT);
                } else {
                    CustomToast.show(MeetingRoomActivity.this, "申请发言失败！",
                            CustomToast.LENGTH_SHORT);
                }
            }

        }
    };

    private ButelOpenSDKOperationListener mAskForOpenCameraListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationFail(Object arg0) {
            isOpenCamera = false;
            CustomToast.show(getApplicationContext(), "打开摄像头失败",
                    Toast.LENGTH_SHORT);
        }


        @Override
        public void onOperationSuc(Object arg0) {
            CustomLog.e(TAG, "打开摄像头成功");
            isOpenCamera = true;
            // CustomToast.show(MeetingRoomActivity.this, "打开摄像头成功",
            // Toast.LENGTH_SHORT);
        }
    };

    private ButelOpenSDKOperationListener mAskForCloseCameraListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationFail(Object arg0) {
            CustomLog.e(TAG, "失败关闭摄像头");
            isOpenCamera = true;
            CustomToast.show(MeetingRoomActivity.this, "关闭摄像头失败",
                    Toast.LENGTH_SHORT);
        }


        @Override
        public void onOperationSuc(Object arg0) {
            CustomLog.e(TAG, "成功关闭摄像头");
            // CustomToast.show(getApplicationContext(), "关闭摄像头成功",
            // Toast.LENGTH_SHORT);
            isOpenCamera = false;
        }
    };

    private ButelOpenSDKOperationListener mAskForswitchVideoModeListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationFail(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomLog.e(TAG, "切换到语音模式，失败");
            CustomToast.show(getApplicationContext(), "切换到语音模式失败",
                    Toast.LENGTH_SHORT);
        }


        @Override
        public void onOperationSuc(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomLog.e(TAG, "切换到语音模式，成功");
            CustomToast.show(getApplicationContext(), "切换到语音模式成功",
                    Toast.LENGTH_SHORT);

            mMenuView.setisCloseMoive(true);
            ownSpeak oSpeak = isOwnSpeak();
            if (oSpeak == ownSpeak.SpeakOnMic) {
                isOpenCamera = false;
            }
            setPositionOnVideoMode();

        }
    };


    private void masterCloseUserLoudspeakerSuccess() {
        CustomToast.show(MeetingRoomActivity.this, "主持人已关闭您的扬声器",
                CustomToast.LENGTH_SHORT);
        Button changeAudioSpeakerMode = (Button) findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "change_audio_speaker_icon"));
        changeAudioSpeakerMode.setBackgroundResource(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.DRAWABLE,
                "jmeetingsdk_change_mode_audio_speaker_close_selector"));
        for (int i = 0; i < ParticipatorsView.mDataList.size(); i++) {
            if (ParticipatorsView.mDataList.get(i).getAccountId()
                    .equals(mMeetingInfo.accountId)) {
                ParticipatorsView.mDataList.get(i).setLoudSpeakerStatus(2);
            }
        }

        boolean matchFlag7 = false;
        for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
            if (mLoudspeakerInfoList.get(a).getAccountId().equals(mMeetingInfo.accountId)) {
                mLoudspeakerInfoList.get(a).setLoudspeakerStatus(2);
                matchFlag7 = true;
                break;
            }
        }

        if (!matchFlag7) {
            LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
            loudspeakerInfo.setAccountId(mMeetingInfo.accountId);
            loudspeakerInfo.setLoudspeakerStatus(2);
            mLoudspeakerInfoList.add(loudspeakerInfo);
        }

        ParticipatorsView.mParticipatorListViewAdapter.notifyDataSetChanged();
    }


    private void masterCloseUserLoudspeakerFail() {
        CustomToast.show(MeetingRoomActivity.this, "主持人关闭您的扬声器失败",
                CustomToast.LENGTH_SHORT);
    }


    private void masterOpenUserLoudspeakerSuccess() {
        CustomToast.show(MeetingRoomActivity.this, "主持人已打开您的扬声器",
                CustomToast.LENGTH_SHORT);
        Button changeAudioSpeakerMode = (Button) findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "change_audio_speaker_icon"));
        changeAudioSpeakerMode.setBackgroundResource(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.DRAWABLE,
                "jmeetingsdk_change_mode_audio_speaker_open_selector"));
        for (int i = 0; i < ParticipatorsView.mDataList.size(); i++) {
            if (ParticipatorsView.mDataList.get(i).getAccountId()
                    .equals(mMeetingInfo.accountId)) {
                ParticipatorsView.mDataList.get(i).setLoudSpeakerStatus(1);
            }
        }

        boolean matchFlag8 = false;
        for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
            if (mLoudspeakerInfoList.get(a).getAccountId().equals(mMeetingInfo.accountId)) {
                mLoudspeakerInfoList.get(a).setLoudspeakerStatus(1);
                matchFlag8 = true;
                break;
            }
        }

        if (!matchFlag8) {
            LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
            loudspeakerInfo.setAccountId(mMeetingInfo.accountId);
            loudspeakerInfo.setLoudspeakerStatus(1);
            mLoudspeakerInfoList.add(loudspeakerInfo);
        }

        ParticipatorsView.mParticipatorListViewAdapter.notifyDataSetChanged();
    }


    private void masterOpenUserLoudspeakerFail() {
        CustomToast.show(MeetingRoomActivity.this, "主持人打开您的扬声器失败",
                CustomToast.LENGTH_SHORT);
    }


    private ButelOpenSDKOperationListener mAskForSwitchMicModeListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationFail(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomLog.e(TAG, "切换静音模式，失败");
            CustomToast.show(getApplicationContext(), "切换静音模式失败",
                    Toast.LENGTH_SHORT);
            masterOperateUserMic = false;
        }


        @Override
        public void onOperationSuc(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            // 刷新多平页面
            if (isMicClose) {
                mMenuView.handleCloseMicOrCam(mMeetingInfo.accountId);
                if (masterOperateUserMic) {
                    masterOperateUserMic = false;
                    CustomToast.show(getApplicationContext(), "主持人已关闭您的麦克风",
                            Toast.LENGTH_SHORT);
                }
            } else {
                mMenuView.handleOpenMicOrCam(mMeetingInfo.accountId);
                if (masterOperateUserMic) {
                    masterOperateUserMic = false;
                    CustomToast.show(getApplicationContext(), "主持人已打开您的麦克风",
                            Toast.LENGTH_SHORT);
                }
            }
            CustomLog.e(TAG, "切换静音模式，成功");
        }
    };

    private ButelOpenSDKOperationListener mAskForSwitchAudioSpeakerModeListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationFail(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomLog.e(TAG, "切换扬声器模式，失败");
            CustomToast.show(MeetingRoomActivity.this, "网络不给力，请重试！",
                    Toast.LENGTH_SHORT);
        }


        @Override
        public void onOperationSuc(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomLog.e(TAG, "切换扬声器模式，成功");
            Button changeAudioSpeakerMode = (Button) findViewById(MResource
                    .getIdByName(MeetingRoomActivity.this, MResource.ID,
                            "change_audio_speaker_icon"));
            if (getMyAudioSpeakerState() == cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN) {
                //				CustomToast.show(MeetingRoomActivity.this, "扬声器已开启",
                //						CustomToast.LENGTH_SHORT);
                changeAudioSpeakerMode
                        .setBackgroundResource(MResource
                                .getIdByName(MeetingRoomActivity.this,
                                        MResource.DRAWABLE,
                                        "jmeetingsdk_change_mode_audio_speaker_open_selector"));

                boolean matchFlag9 = false;
                for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
                    if (mLoudspeakerInfoList.get(a).getAccountId().equals(mMeetingInfo.accountId)) {
                        mLoudspeakerInfoList.get(a).setLoudspeakerStatus(1);
                        matchFlag9 = true;
                        break;
                    }
                }

                if (!matchFlag9) {
                    LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
                    loudspeakerInfo.setAccountId(mMeetingInfo.accountId);
                    loudspeakerInfo.setLoudspeakerStatus(1);
                    mLoudspeakerInfoList.add(loudspeakerInfo);
                }

                for (int i = 0; i < ParticipatorsView.mDataList.size(); i++) {
                    if (ParticipatorsView.mDataList.get(i).getAccountId()
                            .equals(mMeetingInfo.accountId)) {
                        ParticipatorsView.mDataList.get(i)
                                .setLoudSpeakerStatus(1);
                    }
                }
                ParticipatorsView.mParticipatorListViewAdapter
                        .notifyDataSetChanged();
            } else if (getMyAudioSpeakerState() ==
                    cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_CLOSE) {
                //				CustomToast.show(MeetingRoomActivity.this, "扬声器已关闭",
                //						CustomToast.LENGTH_SHORT);
                changeAudioSpeakerMode
                        .setBackgroundResource(MResource
                                .getIdByName(MeetingRoomActivity.this,
                                        MResource.DRAWABLE,
                                        "jmeetingsdk_change_mode_audio_speaker_close_selector"));

                boolean matchFlag10 = false;
                for (int a = 0; a < mLoudspeakerInfoList.size(); a++) {
                    if (mLoudspeakerInfoList.get(a).getAccountId().equals(mMeetingInfo.accountId)) {
                        mLoudspeakerInfoList.get(a).setLoudspeakerStatus(2);
                        matchFlag10 = true;
                        break;
                    }
                }

                if (!matchFlag10) {
                    LoudspeakerInfo loudspeakerInfo = new LoudspeakerInfo();
                    loudspeakerInfo.setAccountId(mMeetingInfo.accountId);
                    loudspeakerInfo.setLoudspeakerStatus(2);
                    mLoudspeakerInfoList.add(loudspeakerInfo);
                }

                for (int i = 0; i < ParticipatorsView.mDataList.size(); i++) {
                    if (ParticipatorsView.mDataList.get(i).getAccountId()
                            .equals(mMeetingInfo.accountId)) {
                        ParticipatorsView.mDataList.get(i)
                                .setLoudSpeakerStatus(2);
                    }
                }
                ParticipatorsView.mParticipatorListViewAdapter
                        .notifyDataSetChanged();
            }

        }
    };

    private ButelOpenSDKOperationListener askForRaiseHandlistener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomToast.show(MeetingRoomActivity.this, "发言申请已提交",
                    Toast.LENGTH_SHORT);
        }


        @Override
        public void onOperationFail(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomToast.show(MeetingRoomActivity.this, "申请发言失败",
                    Toast.LENGTH_SHORT);
        }
    };


    private void setPositionOnVideoMode() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        // boolean isMicNull = isMisExistNull();
        // ownSpeak oSpeak = isOwnSpeak();
        mMenuView.showCameraView(currentMode, density);
    }


    private int getMyMicphoneState() {
        String accoundid = mMeetingInfo.accountId;
        int res = -1;
        if (mButelOpenSDK != null
                && mButelOpenSDK.getSpeakerInfoById(accoundid) != null) {
            res = mButelOpenSDK.getSpeakerInfoById(accoundid).getMICStatus();
        }
        return res;
    }


    private int getMyAudioSpeakerState() {
        String accountid = mMeetingInfo.accountId;
        int res = -1;
        if (mButelOpenSDK != null) {
            res = mButelOpenSDK.getMyLoudspeakerStatus();
        }
        return res;
    }


    private ButelOpenSDKOperationListener mAskForswitchAudioModeListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationFail(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomToast.show(getApplicationContext(), "切换到视频模式失败",
                    Toast.LENGTH_SHORT);
            CustomLog.e(TAG, "切换到视频模式，失败");
        }


        @Override
        public void onOperationSuc(Object arg0) {
            MeetingRoomActivity.this.removeLoadingView();
            CustomLog.e(TAG, "切换到视频模式，成功");
            CustomToast.show(getApplicationContext(), "切换到视频模式成功",
                    Toast.LENGTH_SHORT);
            mMenuView.setisCloseMoive(false);
            setPositionOnVideoMode();
            ownSpeak oSpeak = isOwnSpeak();
            if (oSpeak == ownSpeak.SpeakOnMic) {
                isOpenCamera = true;
            }
        }
    };

    private ButelOpenSDKOperationListener mAskForStartEpisodeListener
            = new ButelOpenSDKOperationListener() {

        @Override
        public void onOperationFail(Object arg0) {
            if (arg0 != null) {
                Cmd cmd = (Cmd) arg0;
                if (cmd.getStatus() == -923) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }

            CustomToast.show(MeetingRoomActivity.this, "网络不给力，请稍后重试！",
                    CustomToast.LENGTH_SHORT);
            dismissLoading();
            episodeState = 0;
        }


        @Override
        public void onOperationSuc(Object arg0) {

            dismissLoading();
            mMenuView.showEpisodeView();
            isExistEpisode = false;
        }
    };

    private ButelOpenSDKOperationListener mAskForStopEpisodeListener
            = new ButelOpenSDKOperationListener() {

        @Override
        public void onOperationFail(Object arg0) {
            // TODO Auto-generated method stub

        }


        @Override
        public void onOperationSuc(Object arg0) {
            // TODO Auto-generated method stub

        }

    };

    private ButelOpenSDKOperationListener mAskForStopSpeakListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object object) {
            CustomToast.show(MeetingRoomActivity.this, "申请停止发言成功",
                    CustomToast.LENGTH_SHORT);
            Cmd speakerOffLineCmd = (Cmd) object;
            if (speakerOffLineCmd.getAccountId().equals(mMeetingInfo.accountId)) {
                speakerOffLine(speakerOffLineCmd.getAccountId());
            }
            mMenuView.dismissAskForStopSpeakViewAndGiveMicView();
            dismissLoading();
        }


        @Override
        public void onOperationFail(Object object) {
            dismissLoading();
            if (object != null) {
                Cmd cmd = (Cmd) object;
                if (cmd.getStatus() == -923) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }
            if (mMeetingInfo.mIsDealWith981) {
                showFailToast();
            } else {

                if (object != null) {
                    Cmd cmd = (Cmd) object;
                    int id = cmd.getCmdId();
                    if (id == -923) {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，请重试！", CustomToast.LENGTH_SHORT);
                    }
                    CustomToast.show(MeetingRoomActivity.this, "网络不给力，请重试！",
                            CustomToast.LENGTH_SHORT);
                } else {
                    CustomToast.show(MeetingRoomActivity.this, "申请停止发言失败！",
                            CustomToast.LENGTH_SHORT);
                }
            }

        }
    };

    private ButelOpenSDKOperationListener mLockMeetingListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object object) {
            CustomLog.e(TAG, "mLockMeetingListener，成功");
            if (mMeetingInfo.lockInfo == 0) {
                sendMeetingOperationBroadCast(MeetingEvent.MEETING_LOCK);
                CustomToast.show(MeetingRoomActivity.this, "加锁成功",
                        CustomToast.LENGTH_SHORT);
                mMenuView.dismissInvitePersonView();
            } else if (mMeetingInfo.lockInfo == 1) {
                sendMeetingOperationBroadCast(MeetingEvent.MEETING_UNLOCK);
                CustomToast.show(MeetingRoomActivity.this, "解锁成功",
                        CustomToast.LENGTH_SHORT);
            }
            Cmd cmd = (Cmd) object;
            mMeetingInfo.lockInfo = cmd.getLockInfo();
            mMenuView.setLock(mMeetingInfo.lockInfo);
            dismissLoading();
        }


        @Override
        public void onOperationFail(Object object) {
            CustomLog.e(TAG, "mLockMeetingListener，失败");
            dismissLoading();
            Cmd cmdc = (Cmd) object;
            if (cmdc.getStatus() == -923) {
                if (mMeetingInfo.lockInfo == 0) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_UNLOCK);
                    CustomToast.show(MeetingRoomActivity.this, "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止解锁！",
                            CustomToast.LENGTH_SHORT);
                } else if (mMeetingInfo.lockInfo == 1) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_LOCK);
                    CustomToast.show(MeetingRoomActivity.this, "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止加锁！",
                            CustomToast.LENGTH_SHORT);
                }
                return;
            }
            if (mMeetingInfo.mIsDealWith981) {
                showFailToast();
            } else {
                Cmd cmd = (Cmd) object;
                if (object != null) {

                    if (cmd.getStatus() == RespModelStatusCode.LOCK_OR_UNLOCK_MEETING_FAILED) {
                        if (mMeetingInfo.lockInfo == 0) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "申请加锁失败！", CustomToast.LENGTH_SHORT);
                        } else if (mMeetingInfo.lockInfo == 1) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "申请解锁失败！", CustomToast.LENGTH_SHORT);
                        }
                    } else {
                        if (mMeetingInfo.lockInfo == 0) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "网络不给力，请重新加锁！", CustomToast.LENGTH_SHORT);
                        } else if (mMeetingInfo.lockInfo == 1) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "网络不给力，请重新解锁！", CustomToast.LENGTH_SHORT);
                        }
                    }
                } else {
                    if (mMeetingInfo.lockInfo == 0) {
                        CustomToast.show(MeetingRoomActivity.this, "申请加锁失败！",
                                CustomToast.LENGTH_SHORT);
                    } else if (mMeetingInfo.lockInfo == 1) {
                        CustomToast.show(MeetingRoomActivity.this, "申请解锁失败！",
                                CustomToast.LENGTH_SHORT);
                    }
                }
            }

        }
    };

    private ButelOpenSDKOperationListener mGiveMicListener = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object object) {
            CustomToast.show(MeetingRoomActivity.this, "传麦成功",
                    CustomToast.LENGTH_SHORT);
            Cmd cmd = (Cmd) object;
            if (cmd.getAccountId().equals(mMeetingInfo.accountId)) {
                speakerOffLine(cmd.getAccountId());
            }
            dismissLoading();
        }


        @Override
        public void onOperationFail(Object object) {
            dismissLoading();
            if (object != null) {
                Cmd cmd = (Cmd) object;
                if (cmd.getStatus() == -923) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }
            if (mMeetingInfo.mIsDealWith981) {
                showFailToast();
            } else {
                Cmd cmd = (Cmd) object;
                if (object != null) {
                    if (cmd.getStatus() == RespModelStatusCode.GIVE_MIC_FAILED) {
                        CustomToast.show(MeetingRoomActivity.this, "申请传麦失败！",
                                CustomToast.LENGTH_SHORT);
                    } else {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，请重试！", CustomToast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.show(MeetingRoomActivity.this, "申请传麦失败！",
                            CustomToast.LENGTH_SHORT);
                }
            }

        }
    };

    private ButelOpenSDKOperationListener masterChangeMeetingModeListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object arg0) {
            dismissLoading();
            // MobclickAgent.onEvent(MeetingRoomActivity.this,
            // AnalysisConfig.SWITCH_COMPEREMODE);
            CustomToast.show(MeetingRoomActivity.this, "切换到主持模式成功",
                    Toast.LENGTH_LONG);
            mMenuView.setMeetingModel(mButelOpenSDK.getCurrentRole(),
                    mButelOpenSDK.getMeetingMode());
        }


        @Override
        public void onOperationFail(Object arg0) {
            dismissLoading();
            Cmd cmd = (Cmd) arg0;
            if (cmd != null && cmd.getStatus() == -982) {
                CustomToast.show(MeetingRoomActivity.this, "网络不给力，请检查网络！",
                        CustomToast.LENGTH_LONG);
            } else

            {
                CustomToast.show(MeetingRoomActivity.this, "切换到主持模式失败",
                        Toast.LENGTH_LONG);
            }

        }
    };

    private ButelOpenSDKOperationListener FreeChangeMeetingModeListener
            = new ButelOpenSDKOperationListener() {
        @Override
        public void onOperationSuc(Object arg0) {
            dismissLoading();
            // MobclickAgent.onEvent(MeetingRoomActivity.this,
            // AnalysisConfig.SWITCH_FREEMODE);
            mMenuView.setMeetingModel(mButelOpenSDK.getCurrentRole(),
                    mButelOpenSDK.getMeetingMode());
            CustomToast.show(MeetingRoomActivity.this, "切换到自由模式成功",
                    Toast.LENGTH_LONG);
        }


        @Override
        public void onOperationFail(Object arg0) {
            dismissLoading();
            Cmd cmd = (Cmd) arg0;
            if (cmd != null && cmd.getStatus() == -982) {
                CustomToast.show(MeetingRoomActivity.this, "网络不给力，请检查网络！",
                        CustomToast.LENGTH_LONG);
            } else {
                CustomToast.show(MeetingRoomActivity.this, "切换到自由模式失败",
                        Toast.LENGTH_LONG);
            }

        }
    };

    private MeetingInfo mMeetingInfo = new MeetingInfo();

    private MenuView mMenuView;

    private MenuViewListener mMenuViewListener = new MenuViewListener() {
        @Override
        public void viewOnClick(View view) {
            // CustomLog.d(TAG, "MenuViewListener viewOnClick" + view.getId());
            String accoundid = mMeetingInfo.accountId;
            if (view.getId() == MResource.getIdByName(MeetingRoomActivity.this,
                    MResource.ID,
                    "meeting_room_menu_ask_for_stop_speak_view_stop_speak_btn")) {
                if (mButelOpenSDK.getMeetingMode() == MeetingStyleStatus.MASTER_MODE) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                } else {
                    int result = mButelOpenSDK
                            .askForStopSpeak(mAskForStopSpeakListener);
                    if (result >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        showLoading("申请停止发言");
                    } else if (result == ButelOpenSDKReturnCode.RETURN_NOAUTHORITY_ERROR) {
                        CustomToast.show(getApplicationContext(), "主持人"
                                        + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                                Toast.LENGTH_SHORT);
                    }
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_menu_main_view_ask_for_speak_btn")) {
				/*
				 * if (mButelOpenSDK.getMeetingMode() == 0) {
				 * CustomToast.show(getApplicationContext(), "主持人" +
				 * mButelOpenSDK.getMasterName() + "已禁止自由发言",
				 * Toast.LENGTH_SHORT); return; } else {
				 */

                showLoading("申请发言");

                int micId = (Integer) view.getTag();
                if (micId == 3) {
                    int result = mButelOpenSDK
                            .userAskForRaiseHand(askForRaiseHandlistener);
                    if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        // showLoading("申请发言");
                        dismissLoading();
                    } else {
                        CustomToast.show(getApplicationContext(), "申请发言失败",
                                Toast.LENGTH_SHORT);
                    }
                    return;
                }
                // CustomLog.e(TAG, "申请发言...micId = " + micId);
                int result = mButelOpenSDK
                        .askForSpeak(mAskForSpeakListener, "");
                if (result >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    // showLoading("申请发言");// + micId + "发言");
                    dismissLoading();
                } else if (result == ButelOpenSDKReturnCode.RETURN_NOAUTHORITY_ERROR) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                }
                // }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_menu_participators_view_lock_icon")) {

                if (mMeetingInfo.lockInfo == 0) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_LOCK);
                    int result = mButelOpenSDK.lockMeeting(
                            mLockMeetingListener, 1);
                    CustomLog.d(TAG, "加/解锁 result= " + result);
                    if (result >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            showLoading("加锁会诊");
                        } else {
                            showLoading("加锁会议");
                        }

                    } else if (result == ButelOpenSDKReturnCode.RETURN_NOAUTHORITY_ERROR) {
                        CustomToast.show(MeetingRoomActivity.this, "主持人"
                                        + mButelOpenSDK.getMasterName() + "已禁止加锁！",
                                CustomToast.LENGTH_SHORT);
                    } else {
                        CustomToast.show(MeetingRoomActivity.this, "申请加锁失败！",
                                CustomToast.LENGTH_SHORT);
                    }

                } else if (mMeetingInfo.lockInfo == 1) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_UNLOCK);
                    int result = mButelOpenSDK.lockMeeting(
                            mLockMeetingListener, 0);
                    CustomLog.d(TAG, "加/解锁 result= " + result);
                    if (result >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        if (MeetingManager
                                .getInstance()
                                .getAppType()
                                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                            showLoading("解锁会诊");
                        } else {
                            showLoading("解锁会议");
                        }

                    } else if (result == ButelOpenSDKReturnCode.RETURN_NOAUTHORITY_ERROR) {
                        CustomToast.show(MeetingRoomActivity.this, "主持人"
                                        + mButelOpenSDK.getMasterName() + "已禁止解锁！",
                                CustomToast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.show(MeetingRoomActivity.this, "申请解锁失败！",
                            CustomToast.LENGTH_SHORT);
                }

            } /*
			 * else if (view.getId() ==
			 * MResource.getIdByName(MeetingRoomActivity.this, MResource.ID,
			 * "meeting_room_menu_ask_for_speak_mic_1_btn")) { if (view.getTag()
			 * != null) { Person person = (Person) view.getTag(); //
			 * CustomLog.e("ssssssssss", //
			 * "sssss person "+person.getAccountId()); int result =
			 * mButelOpenSDK.masterSetUserStartSpeakOnMic(
			 * person.getAccountId(), person.getAccountName(), 1, setUserSpeak);
			 * if (result >=
			 * ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
			 * showLoading("指定发言"); } else {
			 * CustomToast.show(MeetingRoomActivity.this, "指定发言失败",
			 * CustomToast.LENGTH_SHORT); } } else { int result =
			 * mButelOpenSDK.askForSpeak( mAskForSpeakListener, 1); if (result
			 * >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
			 * showLoading("申请麦1发言"); } else if (result ==
			 * ButelOpenSDKReturnCode.RETURN_NOAUTHORITY_ERROR) {
			 * CustomToast.show(getApplicationContext(), "主持人" +
			 * mButelOpenSDK.getMasterName() + "已禁止自由发言", Toast.LENGTH_SHORT); }
			 * } } else if (view.getId() ==
			 * MResource.getIdByName(MeetingRoomActivity.this, MResource.ID,
			 * "meeting_room_menu_ask_for_speak_mic_2_btn")) { if (view.getTag()
			 * != null) { Person person = (Person) view.getTag(); //
			 * CustomLog.e("ssssssssss", // "sssss person "+person.toString());
			 * int result = mButelOpenSDK.masterSetUserStartSpeakOnMic(
			 * person.getAccountId(), person.getAccountName(), 2, setUserSpeak);
			 * if (result >=
			 * ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
			 * showLoading("指定发言"); } else {
			 * CustomToast.show(MeetingRoomActivity.this, "指定发言失败",
			 * CustomToast.LENGTH_SHORT); } } else { int result =
			 * mButelOpenSDK.askForSpeak( mAskForSpeakListener, 2); if (result
			 * >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
			 * showLoading("申请麦2发言"); } else if (result ==
			 * ButelOpenSDKReturnCode.RETURN_NOAUTHORITY_ERROR) {
			 * CustomToast.show(getApplicationContext(), "主持人" +
			 * mButelOpenSDK.getMasterName() + "已禁止自由发言", Toast.LENGTH_SHORT); }
			 * } }
			 */ else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_menu_ask_for_stop_speak_view_give_mic_btn")) {
                if (mButelOpenSDK.getMeetingMode() == MeetingStyleStatus.MASTER_MODE) {
                    CustomToast.show(getApplicationContext(), "主持人"
                                    + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                            Toast.LENGTH_SHORT);
                    return;
                } else

                {
                    mMenuView.showGiveMicView(mParticipantorsData.getList());
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_menu_main_view_exit_btn")) {
                sendMeetingOperationBroadCast(MeetingEvent.MEETING_EXIT);
                showExitMeetingDialog();
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "menu_episode_btn")) {
                CustomLog.d(TAG, "处理插话点击...");
                // if (mButelOpenSDK.getMeetingMode() == 0) {
                // CustomToast.show(getApplicationContext(),
                // "主持人" + mButelOpenSDK.getMasterName() + "已禁止自由发言",
                // Toast.LENGTH_SHORT);
                // return;
                // } else {
				/*
				 * if (!mButelOpenSDK.isSpeaking() &&
				 * !mButelOpenSDK.isEpisode()) { askForEpisodeStart(); } else {
				 * CustomLog.d( TAG, "mButelOpenSDK.isSpeaking() " +
				 * mButelOpenSDK.isSpeaking() + " mButelOpenSDK.isEpisode() " +
				 * mButelOpenSDK.isEpisode()); //
				 * CustomToast.show(MeetingRoomActivity.this, //
				 * "正在发言中，无法申请插话！", // CustomToast.LENGTH_SHORT); // } }
				 */
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "close_layout")) {
                askForEpisodeStop();
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "open_camera")) {
                int result = mButelOpenSDK
                        .askForOpenCamera(mAskForOpenCameraListener);
                if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    CustomLog.e(TAG, "打开摄像头失败");
                    isOpenCamera = false;
                    CustomToast.show(getApplicationContext(), "打开摄像头失败",
                            Toast.LENGTH_SHORT);
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "close_camera")) {
                int result = mButelOpenSDK
                        .askForCloseCamera(mAskForCloseCameraListener);
                if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    CustomLog.e(TAG, "关闭摄像头失败");
                    isOpenCamera = true;
                    CustomToast.show(MeetingRoomActivity.this, "关闭摄像头失败",
                            Toast.LENGTH_SHORT);
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "change_camera")) {
                handleUVCCamera();
                int result = mButelOpenSDK.changeCamera();

                if(result == 0){
                    if(mMenuView != null){
                        mMenuView.hanleChageSelfPreview(mButelOpenSDK.getCurrentCameraIndex(),MeetingManager.getInstance().getVideoParameter(mButelOpenSDK.getCurrentCameraIndex()));
                    }
                }

            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "change_to_moive"))// 切到视频模式
            {
                int result = mButelOpenSDK.switchVideoOrAudioMode(0,
                        mAskForswitchAudioModeListener);
                if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    CustomLog.e(TAG, "切到视频模式");
                    MeetingRoomActivity.this.showLoadingView("请求中...");
                } else {
                    if (result == ButelOpenSDKReturnCode.RETURN_NONEED_CALLBACK_SUCCESS) {
                        CustomToast.show(getApplicationContext(), "切换到视频模式成功",
                                Toast.LENGTH_SHORT);
                        mMenuView.setisCloseMoive(false);
                        setPositionOnVideoMode();
                        ownSpeak oSpeak = isOwnSpeak();
                        if (oSpeak == ownSpeak.SpeakOnMic) {
                            isOpenCamera = true;
                        }
                    } else {
                        CustomToast.show(getApplicationContext(), "切换到视频模式失败",
                                Toast.LENGTH_SHORT);
                    }
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "change_to_video"))// 切到语音模式
            {
                CustomLog.d(TAG, "切到语音模式");
                int result = mButelOpenSDK.switchVideoOrAudioMode(1,
                        mAskForswitchVideoModeListener);
                if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    CustomLog.e(TAG, "切到语音模式");
                    MeetingRoomActivity.this.showLoadingView("请求中...");
                } else {
                    if (result == ButelOpenSDKReturnCode.RETURN_NONEED_CALLBACK_SUCCESS) {
                        CustomToast.show(getApplicationContext(), "切换到语音模式成功",
                                Toast.LENGTH_SHORT);
                        mMenuView.setisCloseMoive(true);
                        ownSpeak oSpeak = isOwnSpeak();
                        if (oSpeak == ownSpeak.SpeakOnMic) {
                            isOpenCamera = false;
                        }
                        setPositionOnVideoMode();
                    } else {
                        CustomToast.show(getApplicationContext(), "切换到语音模式失败",
                                Toast.LENGTH_SHORT);
                    }
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "camera_icon")) {
                showPopWindow(view);
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "change_mode_icon")) {
                // CustomLog.e(TAG, "切到语音或者视频模式=" +
                // mMenuView.getisCloseMoive());
                // showPopWindowMode(view);
                mMenuView.showSwitchVideoTypeView();
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "change_audio_speaker_icon")) {
                CustomLog.e(TAG, "切换扬声器模式 ");
                isAudioSpeakerClose = false;
                if (getMyAudioSpeakerState() > 0) {
                    int res = 0;
                    if (getMyAudioSpeakerState() ==
                            cn.redcdn.butelopensdk.MeetingInfo.LOUNDSPEAKER_OPEN) {
                        // TODO
                        res = mButelOpenSDK
                                .askForCloseLoudspeaker(mAskForSwitchAudioSpeakerModeListener);
                        isAudioSpeakerClose = true;
                    } else {
                        res = mButelOpenSDK
                                .askForOpenLoudspeaker(mAskForSwitchAudioSpeakerModeListener);
                    }
                    if (res == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        showLoading("请求中...");

                    } else {
                        CustomToast.show(getApplicationContext(), "切换扬声器模式失败",
                                Toast.LENGTH_LONG);
                    }

                }

            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID, "change_mic_icon")) {
                CustomLog.e(TAG, "切换静音模式 ");
                isMicClose = false;
                if (getMyMicphoneState() > 0) {
                    int res = 0;
                    if (getMyMicphoneState() == SpeakerInfo.MIC_STATUS_ON) {
                        // TODO
                        res = mButelOpenSDK
                                .closeMicrophone(mAskForSwitchMicModeListener);
                        isMicClose = true;
                        // 通知视频窗口增加静音图标
                        // mMenuView.setMicModeState(true,
                        // SpeakerInfo.MIC_STATUS_OFF);
                    } else {
                        res = mButelOpenSDK
                                .openMicrophone(mAskForSwitchMicModeListener);
                        // 通知视频窗口去掉静音图标
                        // mMenuView.setMicModeState(true,
                        // SpeakerInfo.MIC_STATUS_ON);
                    }
                    if (res == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
						/*
						 * if(isMicClose){
						 * mMenuView.handleCloseMicPhone(mMeetingInfo
						 * .accountId); }else{
						 * mMenuView.handleOpenMicPhone(mMeetingInfo.accountId);
						 * }
						 */
                        // mMenuView.setMicModeState(true,
                        // getMyMicphoneState());
                        showLoading("请求中...");

                    } else {
                        CustomToast.show(getApplicationContext(), "切换静音模式失败",
                                Toast.LENGTH_LONG);
                    }

                }

            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_menu_main_view_set_type_icon")) {
                if (mButelOpenSDK.getMeetingMode() == MeetingStyleStatus.MASTER_MODE) {
                    int result = mButelOpenSDK.masterChangeMeetingMode(
                            MeetingStyleCode.FREE_MODE,
                            FreeChangeMeetingModeListener);
                    if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        showLoading("切换模式");
                    } else {
                        CustomToast.show(getApplicationContext(), "切换到自由模式失败",
                                Toast.LENGTH_LONG);
                    }
                } else {
                    int result = mButelOpenSDK.masterChangeMeetingMode(
                            MeetingStyleCode.MASTER_MODE,
                            masterChangeMeetingModeListener);
                    if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        showLoading("切换模式");
                    } else {
                        CustomToast.show(getApplicationContext(), "切换到主持模式失败",
                                Toast.LENGTH_LONG);
                    }
                }
            } else if (view.getId() == MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_menu_main_view_ask_for_play_btn")) {
                if (mButelOpenSDK.getLiveStatus() == LiveStatus.LIVING_NOW) {
                    showShareLiveDialog();
                } else {
                    if (MeetingManager
                            .getInstance()
                            .getAppType()
                            .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                        liveDiaglog = new LiveView(MeetingRoomActivity.this,
                                mButelOpenSDK.getMasterName() + "的会诊直播");
                    } else {
                        liveDiaglog = new LiveView(MeetingRoomActivity.this,
                                mButelOpenSDK.getMasterName() + "的会议直播");
                    }

                    liveDiaglog.setBtnOnClickListener(new BtnOnClickListener() {

                        @Override
                        public void onClick(LiveView customDialog, int tag,
                                            String name) {
                            if (tag == 0) {
                                // TODO调接口获取短信内容
                                // MobclickAgent
                                // .onEvent(
                                // MeetingRoomActivity.this,
                                // AnalysisConfig.CLICK_START_LIVE_TELECAST);
                                liveNameString = name;
                                if (liveNameString != null) {
                                    int result = mButelOpenSDK.askForStartLive(
                                            liveNameString, askForLive);
                                    if (result ==
                                            ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                                        showLoading("正在开启直播");
                                    } else {
                                        CustomToast.show(
                                                MeetingRoomActivity.this,
                                                "开启直播失败",
                                                CustomToast.LENGTH_LONG);
                                    }
                                } else {
                                    CustomToast.show(MeetingRoomActivity.this,
                                            "开启直播失败", CustomToast.LENGTH_LONG);
                                }
                            } else if (tag == 1) {
                                if (liveDiaglog != null) {
                                    liveDiaglog.dismiss();
                                    liveDiaglog = null;
                                }
                            }

                        }
                    });
                    liveDiaglog.show();

                }
            }
        }


        private ButelOpenSDKOperationListener setUserSpeak = new ButelOpenSDKOperationListener() {

            @Override
            public void onOperationFail(Object arg0) {
                dismissLoading();
                if (arg0 != null && ((Cmd) arg0).getStatus() == -982) {
                    CustomToast.show(MeetingRoomActivity.this, "网络不给力，请检查网络！",
                            Toast.LENGTH_LONG);
                } else {
                    CustomToast.show(MeetingRoomActivity.this, "指定发言失败",
                            CustomToast.LENGTH_SHORT);
                }

            }


            @Override
            public void onOperationSuc(Object arg0) {
                dismissLoading();
                mMenuView.dismissAskForSpeakView();
            }

        };

        private ButelOpenSDKOperationListener askForLive = new ButelOpenSDKOperationListener() {

            @Override
            public void onOperationFail(Object arg0) {
                dismissLoading();
                CustomToast.show(MeetingRoomActivity.this, "开启直播失败",
                        CustomToast.LENGTH_LONG);
            }


            @Override
            public void onOperationSuc(Object arg0) {
                mMenuView.setLiveTileShow(mButelOpenSDK.getLiveStatus());
                dismissLoading();
                if (liveDiaglog != null) {
                    liveDiaglog.dismiss();
                    liveDiaglog = null;
                }
                showShareLiveDialog();
            }
        };


        private void showShareLiveDialog() {
            shareDialog = new LiveShareView(MeetingRoomActivity.this);
            shareDialog.setBtnOnClickListener(new ShareBtnOnClickListener() {

                @Override
                public void onClick(LiveShareView customDialog, int tag) {
                    getLiveShareString(mButelOpenSDK.getMasterName(), tag);
                }

            });
            shareDialog.show();
        }


        private void askForEpisodeStart() {
			/*
			 * CustomLog.d(TAG, "申请插话中..."); int result = mButelOpenSDK
			 * .askForEpisodeStart(mAskForStartEpisodeListener); if (result >=
			 * ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
			 * episodeState = 1; showLoading("申请插话中..."); } else {
			 * CustomToast.show(MeetingRoomActivity.this, "申请插话失败，请重试！",
			 * CustomToast.LENGTH_SHORT); episodeState = 0; }
			 */
        }


        public void askForEpisodeStop() {
			/*
			 * if (mMenuView != null && mButelOpenSDK != null) {
			 * mMenuView.hideEpisodeView();
			 * mButelOpenSDK.askForEpisodeStop(mAskForStopEpisodeListener);
			 * episodeState = 0; }
			 */
        }


        @Override
        public void invitePerson(Person person) {

            Contact newContact = new Contact();
            newContact.setNubeNumber(person.getAccountId());
            newContact.setNickname(person.getAccountName());
            newContact.setName(person.getAccountName());

            newContact.setContactId(CommonUtil.getUUID());
            // newContact.setFirstName(StringHelper.getHeadChar(person
            // .getAccountName()));
            // newContact.setDeviceType(person.getAppType());
            newContact.setHeadUrl(person.getPhoto());
            newContact.setUserType(person.getUserType());
            // newContact.setNumber(userInfo.mobile);

            //红云医疗添加字段
            newContact.setAccountType(person.accountType);
            newContact.setWorkUnit(person.workUnit);
            newContact.setWorkUnitType(person.workUnitType);
            newContact.setDepartment(person.department);
            newContact.setProfessional(person.professional);
            newContact.setOfficTel(person.officTel);
            //去掉邀请参会的时候将邀请人添加到通讯录中
            // if (!MeetingManager.getInstance().getContactOperationImp().isContactExist(person.getAccountId())) {
            //     if (!MeetingManager.getInstance().getContactOperationImp()
            //         .checkNubeIsCustomService(newContact.getNubeNumber())) {
            //         MeetingManager.getInstance().getContactOperationImp()
            //             .addContact(newContact, new ContactCallback() {
            //                 @Override
            //                 public void onFinished(ResponseEntry result) {
            //                     if (result.status >= 0) {
            //                         MeetingManager
            //                             .getInstance()
            //                             .getContactOperationImp()
            //                             .getAllContacts(
            //                                 new ContactCallback() {
            //                                     @Override
            //                                     public void onFinished(
            //                                         ResponseEntry result) {
            //                                         if (result.status >= 0) {
            //                                             CustomToast
            //                                                 .show(MeetingRoomActivity.this,
            //                                                     "邀请成功,已添加至通讯录！",
            //                                                     Toast.LENGTH_LONG);
            //
            //                                             DataSet adapterDataSetImp
            //                                                 = (DataSet) result.content;
            //                                             if (adapterDataSetImp == null) {
            //                                                 CustomLog
            //                                                     .d(TAG,
            //                                                         "onContactLoaded 结果为空");
            //                                             } else {
            //                                                 List<Person> invitePersonTxlPersonList
            //                                                     = new ArrayList<Person>();
            //                                                 List<Person> partcipantorTxlPersonList
            //                                                     = new ArrayList<Person>();
            //                                                 for (int i = 0; i < adapterDataSetImp
            //                                                     .getCount(); i++) {
            //                                                     Contact contact
            //                                                         = (Contact) adapterDataSetImp
            //                                                         .getItem(i);
            //                                                     Person p1 = new Person();
            //                                                     p1.setAccountId(contact
            //                                                         .getNubeNumber());
            //                                                     p1.setAccountName(contact
            //                                                         .getNickname());
            //                                                     p1.setPhoto(contact
            //                                                         .getHeadUrl());
            //
            //                                                     Person p2 = new Person();
            //                                                     p2.setAccountId(contact
            //                                                         .getNubeNumber());
            //                                                     p2.setAccountName(contact
            //                                                         .getNickname());
            //                                                     p2.setPhoto(contact
            //                                                         .getHeadUrl());
            //
            //                                                     JSONArray paticitors = null;
            //                                                     try {
            //                                                         paticitors
            //                                                             = mButelOpenSDK.getParticipatorList();
            //                                                     } catch (Exception e) {
            //                                                         e.printStackTrace();
            //                                                     }
            //                                                     if (paticitors != null) {
            //                                                         for (int b = 0;
            //                                                              b < paticitors.length();
            //                                                              b++) {
            //                                                             try {
            //                                                                 JSONObject paticitorObj
            //                                                                     = paticitors.getJSONObject(
            //                                                                     b);
            //                                                                 if (paticitorObj.optString(
            //                                                                     CmdKey.ACOUNT_ID)
            //                                                                     .equals(
            //                                                                         p2.getAccountId())) {
            //                                                                     if (paticitorObj.optInt(
            //                                                                         "loudSpeakerStatus") ==
            //                                                                         1) {
            //                                                                         CustomLog.d(TAG,
            //                                                                             "NO.3");
            //                                                                         p2.setLoudSpeakerStatus(
            //                                                                             1);
            //
            //                                                                         boolean matchFlag11
            //                                                                             = false;
            //                                                                         for (int a = 0;
            //                                                                              a <
            //                                                                                  mLoudspeakerInfoList
            //                                                                                      .size();
            //                                                                              a++) {
            //                                                                             if (mLoudspeakerInfoList
            //                                                                                 .get(a)
            //                                                                                 .getAccountId()
            //                                                                                 .equals(
            //                                                                                     p2.getAccountId())) {
            //                                                                                 mLoudspeakerInfoList
            //                                                                                     .get(a)
            //                                                                                     .setLoudspeakerStatus(
            //                                                                                         1);
            //                                                                                 matchFlag11
            //                                                                                     = true;
            //                                                                                 break;
            //                                                                             }
            //                                                                         }
            //
            //                                                                         if (!matchFlag11) {
            //                                                                             LoudspeakerInfo
            //                                                                                 loudspeakerInfo
            //                                                                                 = new LoudspeakerInfo();
            //                                                                             loudspeakerInfo.setAccountId(
            //                                                                                 p2.getAccountId());
            //                                                                             loudspeakerInfo.setLoudspeakerStatus(
            //                                                                                 1);
            //                                                                             mLoudspeakerInfoList
            //                                                                                 .add(
            //                                                                                     loudspeakerInfo);
            //                                                                         }
            //
            //                                                                     } else if (
            //                                                                         paticitorObj.optInt(
            //                                                                             "loudSpeakerStatus") ==
            //                                                                             2) {
            //                                                                         p2.setLoudSpeakerStatus(
            //                                                                             2);
            //
            //                                                                         boolean matchFlag12
            //                                                                             = false;
            //                                                                         for (int a = 0;
            //                                                                              a <
            //                                                                                  mLoudspeakerInfoList
            //                                                                                      .size();
            //                                                                              a++) {
            //                                                                             if (mLoudspeakerInfoList
            //                                                                                 .get(a)
            //                                                                                 .getAccountId()
            //                                                                                 .equals(
            //                                                                                     p2.getAccountId())) {
            //                                                                                 mLoudspeakerInfoList
            //                                                                                     .get(a)
            //                                                                                     .setLoudspeakerStatus(
            //                                                                                         2);
            //                                                                                 matchFlag12
            //                                                                                     = true;
            //                                                                                 break;
            //                                                                             }
            //                                                                         }
            //
            //                                                                         if (!matchFlag12) {
            //                                                                             LoudspeakerInfo
            //                                                                                 loudspeakerInfo
            //                                                                                 = new LoudspeakerInfo();
            //                                                                             loudspeakerInfo.setAccountId(
            //                                                                                 p2.getAccountId());
            //                                                                             loudspeakerInfo.setLoudspeakerStatus(
            //                                                                                 2);
            //                                                                             mLoudspeakerInfoList
            //                                                                                 .add(
            //                                                                                     loudspeakerInfo);
            //                                                                         }
            //
            //                                                                     }
            //                                                                 }
            //                                                             } catch (JSONException e) {
            //                                                                 e.printStackTrace();
            //                                                             }
            //                                                         }
            //                                                     }
            //
            //                                                     invitePersonTxlPersonList
            //                                                         .add(p1);
            //                                                     partcipantorTxlPersonList
            //                                                         .add(p2);
            //                                                 }
            //                                                 adapterDataSetImp
            //                                                     .release();
            //                                                 mInvitePersonList
            //                                                     .notifyTxlChanged(
            //                                                         invitePersonTxlPersonList);
            //                                                 mParticipantorList
            //                                                     .notifyTxlChanged(
            //                                                         partcipantorTxlPersonList);
            //                                             }
            //                                         }
            //                                     }
            //                                 }, true);
            //                     } else {
            //                         CustomLog.d(TAG, "添加通讯录失败");
            //                     }
            //                 }
            //             });
            //     }
            // }

            CustomLog
                    .d(TAG,
                            "通过 MeetingManager.getInstance().getHostAgentOperation()新增此邀请人");
            List<String> inviteListID = new ArrayList<String>();
            inviteListID.add(person.getAccountId());
            MeetingManager
                    .getInstance()
                    .getHostAgentOperation()
                    .invite(inviteListID, mMeetingInfo.meetingId,
                            mMeetingInfo.accountId, mMeetingInfo.accountName);

            final ModifyMeetingInviters mModifyMeetingInviters = new ModifyMeetingInviters() {
                @Override
                protected void onSuccess(ResponseEmpty responseContent) {
                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                }
            };

            //通过发送邀请广播通过IM渠道通知对方发起会议邀请
            sendInviteBroadcast(person.getAccountId(), person.getName());
            // CustomLog.d(TAG, "通过后台新增此邀请人");
            List<String> invotedUsers = new ArrayList<String>();
            invotedUsers.add(person.getAccountId());

            List<String> invotedPhones = new ArrayList<String>();

            int resultCode = mModifyMeetingInviters.modifymeetingInviter(
                    MeetingManager.getInstance().getAppType(),

                    mMeetingInfo.token, mMeetingInfo.meetingId, invotedUsers,

                    invotedPhones);

            if (resultCode >= 0) {
                String str = KeyEventConfig.INVITE_IN_MEETINGROOM + "_ok_"
                        + mMeetingInfo.accountId + "_邀请成功";
                CustomToast.show(MeetingRoomActivity.this, "邀请成功！",
                        Toast.LENGTH_LONG);
                KeyEventWrite.write(str);
            } else {
                String str = KeyEventConfig.INVITE_IN_MEETINGROOM + "_fail_"
                        + mMeetingInfo.accountId + "_邀请失败";
                CustomToast.show(MeetingRoomActivity.this, "邀请失败！",
                        Toast.LENGTH_LONG);
                KeyEventWrite.write(str);
            }
        }


        @Override
        public void invitePersonBySMS() {

            if (MeetingManager.getInstance().getAppType()
                    .equals(MeetingManager.MEETING_APP_TV)) {

                ReserveShareMessage();

            } else {

                if (shareString != null) {

                    CustomLog.d(TAG,
                            "invite shareString:" + shareString.toString());

                    Uri smsToUri = Uri.parse("smsto:");

                    Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);

                    intent.putExtra("sms_body", shareString);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                } else {
                    GetMeetingInvitationSMS getSMS = new GetMeetingInvitationSMS() {
                        @Override
                        protected void onSuccess(
                                MeetingInvitationSMSInfo responseContent) {

                            Uri smsToUri = Uri.parse("smsto:");

                            Intent intent = new Intent(Intent.ACTION_SENDTO,
                                    smsToUri);

                            intent.putExtra("sms_body",
                                    responseContent.invitationSMS);

                            startActivity(intent);
                        }


                        @Override
                        protected void onFail(int statusCode, String statusInfo) {
                            if (HttpErrorCode.checkNetworkError(statusCode)) {
                                CustomToast.show(MeetingRoomActivity.this,
                                        "网络不给力，请检查网络！", Toast.LENGTH_LONG);
                                return;
                            } else {
                                CustomLog.d(TAG, "短信邀请失败 错误码=" + statusInfo);
                                if (CommonUtil
                                        .getNetWorkType(getApplicationContext()) == -1) {
                                    CustomToast.show(MeetingRoomActivity.this,
                                            "网络不给力，请检查网络！", Toast.LENGTH_LONG);
                                    return;
                                }
                            }
                        }
                    };

                    String app;

                    if (MeetingManager.getInstance().getAppType()
                            .equals(MeetingManager.MEETING_APP_TV)) {
                        app = MeetingManager.MEETING_APP_BUTEL_CONSULTATION;
                    } else {
                        app = MeetingManager.getInstance().getAppType();
                    }

                    getSMS.getMeetingInvitationSMS(app, "",
                            mMeetingInfo.meetingId,

                            mMeetingInfo.accountId, mMeetingInfo.accountName);
                }

            }

        }


        private void ReserveShareMessage() {
            MeetingRoomActivity.this.showLoadingView("发送短信中");
            if (shareString == null) {
                GetMeetingInvitationSMS gs = new GetMeetingInvitationSMS() {

                    @Override
                    protected void onSuccess(
                            MeetingInvitationSMSInfo responseContent) {

                        super.onSuccess(responseContent);
                        CustomLog.e(TAG, "成功获取链接");
                        if (responseContent.invitationSMS != null) {
                            shareString = responseContent.invitationSMS;
                            sendSMs();
                        } else {
                            CustomLog.e(TAG,
                                    "responseContent.invitationSMS==null");
                        }
                    }


                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        MeetingRoomActivity.this.removeLoadingView();
                        CustomLog.e(TAG, "失败获取链接");
                        CustomToast.show(MeetingRoomActivity.this, "短信发送失败",
                                Toast.LENGTH_SHORT);
                    }

                };

                String app;

                if (MeetingManager.getInstance().getAppType()
                        .equals(MeetingManager.MEETING_APP_TV)) {
                    app = MeetingManager.MEETING_APP_BUTEL_CONSULTATION;
                } else {
                    app = MeetingManager.getInstance().getAppType();
                }

                gs.getMeetingInvitationSMS(app, "", mMeetingInfo.meetingId,
                        mMeetingInfo.accountId, mMeetingInfo.accountName);
            } else {
                sendSMs();
            }
        }


        private void sendSMs() {

            SendSMS sms = new SendSMS() {

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    MeetingRoomActivity.this.removeLoadingView();
                    if (statusCode == -982) {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络异常，短信发送失败", Toast.LENGTH_SHORT);
                    } else

                    {
                        CustomToast.show(MeetingRoomActivity.this, "短信发送失败",
                                Toast.LENGTH_SHORT);
                    }
                }


                @Override
                protected void onSuccess(
                        cn.redcdn.datacenter.meetingmanage.ResponseEmpty responseContent) {
                    // TODO Auto-generated method stub
                    super.onSuccess(responseContent);
                    MeetingRoomActivity.this.removeLoadingView();
                    CustomToast.show(MeetingRoomActivity.this, "短信已成功发送",
                            Toast.LENGTH_SHORT);
                }

            };

            sms.sendSMS(mMeetingInfo.token, MeetingManager.getInstance()
                    .getPhoneNumber(), shareString);

        }


        @Override
        public void invitePersonByWeixing() {
            //			showShare(true, Wechat.NAME);
            //微信分享
            if (shareString == null) {
                getMeetingInvitationSMS();
            } else {
                if (isWeixinAvilible()) {
                    shareLiveByWx(shareString);
                }
            }
        }


        @Override
        public void giveMic(Person person) {
            if (mButelOpenSDK != null) {
                int result = mButelOpenSDK.giveMic(mGiveMicListener,
                        person.getAccountId());
                if (result >= ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    showLoading("正在传麦");
                }
            }
        }


        @Override
        public void addTxl(MDSDetailInfo mMDSDetailInfo) {
            CustomToast.show(MeetingRoomActivity.this, "成功添加至通讯录！",
                    Toast.LENGTH_LONG);

            Contact newContact = new Contact();

            // newContact.setNubeNumber(userInfo.nubeNumber);
            // newContact.setNickname(userInfo.nickName);
            // newContact.setName(userInfo.nickName);
            // newContact.setNubeNumber(userInfo.nubeNumber);
            // newContact.setContactId(CommonUtil.getUUID());
            // // newContact
            // // .setFirstName(StringHelper.getHeadChar(userInfo.nickName));
            // // newContact.setDeviceType(userInfo.appType);
            // newContact.setHeadUrl(userInfo.headUrl);
            // newContact.setUserType(userInfo.serviceType);
            // // newContact.setNumber(userInfo.mobile);

            newContact.setNubeNumber(mMDSDetailInfo.nubeNumber);
            newContact.setNickname(mMDSDetailInfo.nickName);
            newContact.setName(mMDSDetailInfo.nickName);
            newContact.setNubeNumber(mMDSDetailInfo.nubeNumber);
            newContact.setContactId(CommonUtil.getUUID());
            // newContact
            // .setFirstName(StringHelper.getHeadChar(userInfo.nickName));
            // newContact.setDeviceType(userInfo.appType);
            newContact.setHeadUrl(mMDSDetailInfo.headThumUrl);
            // newContact.setUserType(mMDSDetailInfo.serviceType);修改新接口的时候没有serviceType参数 并且不知道该参数意义
            // newContact.setNumber(userInfo.mobile);

            //红云医疗添加字段
            newContact.setAccountType(mMDSDetailInfo.accountType);
            newContact.setWorkUnit(mMDSDetailInfo.workUnit);
            newContact.setWorkUnitType(mMDSDetailInfo.workUnitType);
            newContact.setDepartment(mMDSDetailInfo.department);
            newContact.setProfessional(mMDSDetailInfo.professional);
            newContact.setOfficTel(mMDSDetailInfo.officTel);

            MeetingManager.getInstance().getContactOperationImp()
                    .addContact(newContact, new ContactCallback() {
                        @Override
                        public void onFinished(ResponseEntry result) {
                            if (result.status >= 0) {
                                // MobclickAgent
                                // .onEvent(
                                // MeetingRoomActivity.this,
                                // AnalysisConfig.ADD_PARTICIPANTS_TO_CONTACT);
                                MeetingManager.getInstance()
                                        .getContactOperationImp()
                                        .getAllContacts(new ContactCallback() {
                                            @Override
                                            public void onFinished(
                                                    ResponseEntry result) {
                                                if (result.status >= 0) {
                                                    DataSet adapterDataSetImp = (DataSet) result.content;
                                                    if (adapterDataSetImp == null) {
                                                        CustomLog
                                                                .d(TAG,
                                                                        "onContactLoaded 结果为空");
                                                    } else {
                                                        List<Person> invitePersonTxlPersonList
                                                                = new ArrayList<Person>();
                                                        List<Person> partcipantorTxlPersonList
                                                                = new ArrayList<Person>();
                                                        for (int i = 0; i < adapterDataSetImp
                                                                .getCount(); i++) {
                                                            Contact contact = (Contact) adapterDataSetImp
                                                                    .getItem(i);
                                                            Person p1 = new Person();
                                                            p1.setAccountId(contact
                                                                    .getNubeNumber());
                                                            p1.setAccountName(contact
                                                                    .getNickname());
                                                            p1.setPhoto(contact
                                                                    .getHeadUrl());

                                                            Person p2 = new Person();
                                                            p2.setAccountId(contact
                                                                    .getNubeNumber());
                                                            p2.setAccountName(contact
                                                                    .getNickname());
                                                            p2.setPhoto(contact
                                                                    .getHeadUrl());

                                                            JSONArray paticitors = null;
                                                            try {
                                                                paticitors
                                                                        = mButelOpenSDK.getParticipatorList();
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }

                                                            if (paticitors != null) {
                                                                for (int b = 0;
                                                                     b < paticitors.length();
                                                                     b++) {
                                                                    try {
                                                                        JSONObject paticitorObj = paticitors
                                                                                .getJSONObject(b);
                                                                        if (paticitorObj.optString(
                                                                                CmdKey.ACOUNT_ID)
                                                                                .equals(p2.getAccountId())) {
                                                                            if (paticitorObj.optInt(
                                                                                    "loudSpeakerStatus") == 1) {
                                                                                CustomLog.d(TAG, "NO.4");
                                                                                p2.setLoudSpeakerStatus(1);

                                                                                boolean matchFlag13 = false;
                                                                                for (int a = 0;
                                                                                     a <
                                                                                             mLoudspeakerInfoList
                                                                                                     .size();
                                                                                     a++) {
                                                                                    if (mLoudspeakerInfoList
                                                                                            .get(a)
                                                                                            .getAccountId()
                                                                                            .equals(
                                                                                                    p2.getAccountId())) {
                                                                                        mLoudspeakerInfoList
                                                                                                .get(a)
                                                                                                .setLoudspeakerStatus(
                                                                                                        1);
                                                                                        matchFlag13 = true;
                                                                                        break;
                                                                                    }
                                                                                }

                                                                                if (!matchFlag13) {
                                                                                    LoudspeakerInfo
                                                                                            loudspeakerInfo
                                                                                            = new LoudspeakerInfo();
                                                                                    loudspeakerInfo.setAccountId(
                                                                                            p2.getAccountId());
                                                                                    loudspeakerInfo.setLoudspeakerStatus(
                                                                                            1);
                                                                                    mLoudspeakerInfoList.add(
                                                                                            loudspeakerInfo);
                                                                                }

                                                                            } else if (paticitorObj.optInt(
                                                                                    "loudSpeakerStatus") == 2) {
                                                                                p2.setLoudSpeakerStatus(2);

                                                                                boolean matchFlag14 = false;
                                                                                for (int a = 0;
                                                                                     a <
                                                                                             mLoudspeakerInfoList
                                                                                                     .size();
                                                                                     a++) {
                                                                                    if (mLoudspeakerInfoList
                                                                                            .get(a)
                                                                                            .getAccountId()
                                                                                            .equals(
                                                                                                    p2.getAccountId())) {
                                                                                        mLoudspeakerInfoList
                                                                                                .get(a)
                                                                                                .setLoudspeakerStatus(
                                                                                                        2);
                                                                                        matchFlag14 = true;
                                                                                        break;
                                                                                    }
                                                                                }

                                                                                if (!matchFlag14) {
                                                                                    LoudspeakerInfo
                                                                                            loudspeakerInfo
                                                                                            = new LoudspeakerInfo();
                                                                                    loudspeakerInfo.setAccountId(
                                                                                            p2.getAccountId());
                                                                                    loudspeakerInfo.setLoudspeakerStatus(
                                                                                            2);
                                                                                    mLoudspeakerInfoList.add(
                                                                                            loudspeakerInfo);
                                                                                }

                                                                            }
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }

                                                            invitePersonTxlPersonList
                                                                    .add(p1);
                                                            partcipantorTxlPersonList
                                                                    .add(p2);
                                                        }
                                                        adapterDataSetImp
                                                                .release();
                                                        mInvitePersonList
                                                                .notifyTxlChanged(invitePersonTxlPersonList);
                                                        mParticipantorList
                                                                .notifyTxlChanged(partcipantorTxlPersonList);
                                                    }
                                                }
                                            }
                                        }, true);
                            } else {
                                CustomLog.d(TAG, "添加通讯录失败");
                            }
                        }
                    });
        }


        @Override
        public void changeCamera() {
            handleUVCCamera();
            int result = mButelOpenSDK.changeCamera();
            if(result == 0){
                if(mMenuView != null){
                    mMenuView.hanleChageSelfPreview(mButelOpenSDK.getCurrentCameraIndex(),MeetingManager.getInstance().getVideoParameter(mButelOpenSDK.getCurrentCameraIndex()));
                }
            }
        }


        @Override
        public void stopEpisode() {
            askForEpisodeStop();
        }


        @Override
        public void changeNoSpeakerTip() {
            if (mButelOpenSDK.getSpeakers() != null
                    && mButelOpenSDK.getSpeakers().size() > 0) {
                CustomLog.d(TAG, mButelOpenSDK.getSpeakers().toString());
                hideNoSpeakerTip();
            } else {
                showNoSpeakerTip();
            }
        }


        @Override
        public void openOrCloseCamera(boolean open) {
            CustomLog.e(TAG, "openOrCloseCamera " + open);
            handleCamare();
            // 刷新打开关闭视频列表
            List<SpeakerInfo> speakInfoList = new ArrayList<SpeakerInfo>();
            speakInfoList.addAll(mButelOpenSDK.getSpeakers());
            mMenuView.setSpeakerList(speakInfoList);
        }


        @Override
        public void closeShareDocView(String id) {
            CustomLog.e(TAG, "closeShareDocView " + id);
            if (mButelOpenSDK.getSpeakerInfoById(id) != null) {
                mMenuView.removeShareDocView(id, mButelOpenSDK
                        .getSpeakerInfoById(id).getAccountName());
            }
        }
    };


    private void handleCamare() {

        if (isOpenCamera) {
            CustomLog.e(TAG, "关闭摄像头");
            int result = mButelOpenSDK
                    .askForCloseCamera(new ButelOpenSDKOperationListener() {
                        @Override
                        public void onOperationFail(Object arg0) {
                            MeetingRoomActivity.this.removeLoadingView();
                            CustomLog.e(TAG, "失败关闭摄像头");
                            CustomToast.show(MeetingRoomActivity.this,
                                    "关闭摄像头失败", Toast.LENGTH_SHORT);
                            masterOperateUserCamera = false;
                        }


                        @Override
                        public void onOperationSuc(Object arg0) {
                            MeetingRoomActivity.this.removeLoadingView();
                            CustomLog.e(TAG, "成功关闭摄像头");
                            // CustomToast.show(getApplicationContext(),
                            // "关闭摄像头成功", Toast.LENGTH_SHORT);
                            isOpenCamera = false;
                            mMenuView
                                    .handleCloseMicOrCam(mMeetingInfo.accountId);
                            if (masterOperateUserCamera) {
                                CustomToast.show(getApplicationContext(), "主持人已关闭您的摄像头",
                                        Toast.LENGTH_SHORT);
                                masterOperateUserCamera = false;
                            }
                        }
                    });
            if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                MeetingRoomActivity.this.showLoadingView("请求中...");

            } else {
                isOpenCamera = true;
                masterOperateUserCamera = false;
                CustomToast.show(getApplicationContext(), "关闭摄像头失败",
                        Toast.LENGTH_SHORT);
            }
        } else {
            CustomLog.e(TAG, "开启摄像头");
            handleUVCCamera();
            ButelOpenSDKOperationListener openlistenr = new ButelOpenSDKOperationListener() {
                @Override
                public void onOperationFail(Object arg0) {
                    MeetingRoomActivity.this.removeLoadingView();
                    CustomToast.show(getApplicationContext(), "开启摄像头失败",
                            Toast.LENGTH_SHORT);
                    masterOperateUserCamera = false;
                }


                @Override
                public void onOperationSuc(Object arg0) {
                    MeetingRoomActivity.this.removeLoadingView();
                    CustomLog.e(TAG, "开启摄像头成功");
                    isOpenCamera = true;
                    mMenuView.handleOpenMicOrCam(mMeetingInfo.accountId);
                    // CustomToast.show(getApplicationContext(),
                    // "打开摄像头成功", Toast.LENGTH_SHORT);
                    if (masterOperateUserCamera) {
                        CustomToast.show(getApplicationContext(), "主持人已打开您的摄像头",
                                Toast.LENGTH_SHORT);
                        masterOperateUserCamera = false;
                    }
                    if(mMenuView != null){
                        mMenuView.hanleChageSelfPreview(mButelOpenSDK.getCurrentCameraIndex(),MeetingManager.getInstance().getVideoParameter(mButelOpenSDK.getCurrentCameraIndex()));
                    }
                }
            };
            CustomLog.e(TAG, "打开摄像头成功====" + openlistenr.toString());
            int result = mButelOpenSDK.askForOpenCamera(openlistenr);
            if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                MeetingRoomActivity.this.showLoadingView("请求中...");
            } else {
                CustomToast.show(getApplicationContext(), "开启摄像头失败",
                        Toast.LENGTH_SHORT);
                masterOperateUserCamera = false;
            }
        }
    }


    private void getLiveShareString(String shareName, final int tag) {
        if (liveShareString != null && liveZbUrl != null) {
            shareLiveThings(tag, liveShareString, liveZbUrl);
        } else {
            MeetingRoomActivity.this.showLoading("请稍后...");
            GetMeetingZBInvitationContent content = new GetMeetingZBInvitationContent() {

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    MeetingRoomActivity.this.dismissLoading();
                    if (HttpErrorCode.checkNetworkError(statusCode)) {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，请检查网络！", Toast.LENGTH_LONG);
                        return;
                    } else {
                        CustomToast.show(MeetingRoomActivity.this, "获取分享内容失败！",
                                Toast.LENGTH_LONG);
                    }
                }


                @Override
                protected void onSuccess(MeetingZBInvitationInfo responseContent) {
                    dismissLoading();
                    if (responseContent != null) {
                        liveZbUrl = responseContent.zbURL;
                        liveShareString = responseContent.invitationSMS;
                        CustomLog.d(TAG, "liveZbUrl:" + liveZbUrl
                                + "liveShareString:" + liveShareString);
                        shareLiveThings(tag, liveShareString, liveZbUrl);
                    }
                }

            };
            content.getMeetingZBInvitationContent(MeetingManager.getInstance()
                            .getAppType(), mMeetingInfo.meetingId,
                    mMeetingInfo.accountId, shareName);
        }
    }


    private void shareLiveThings(int tag, String content, String url) {
        switch (tag) {
            case 1:
                // MobclickAgent.onEvent(MeetingRoomActivity.this,
                // AnalysisConfig.CLICK_WECHAT_IN_LIVE_TELECAST);
                //			shareLiveByWx(Wechat.NAME, content, url);
                break;
            case 2:
                // MobclickAgent.onEvent(MeetingRoomActivity.this,
                // AnalysisConfig.CLICK_WECHATMOMENTS_IN_LIVE_TELECAST);
                //			shareLiveByFriend(WechatMoments.NAME, content, url);
                break;
            case 3:
                // MobclickAgent.onEvent(MeetingRoomActivity.this,
                // AnalysisConfig.CLICK_SMS_IN_LIVE_TELECAST);
                shareLiveBySms(content);
                break;
            case 4:
                // MobclickAgent.onEvent(MeetingRoomActivity.this,
                // AnalysisConfig.CLICK_COPYLINK_IN_LIVE_TELECAST);
                shareLiveByCopy(MeetingRoomActivity.this, content);
                break;
            case 5:
                if (shareDialog != null) {
                    shareDialog.dismiss();
                    shareDialog = null;
                }
                break;
        }

    }


    private void shareLiveByWx(String content) {
        //微信分享
        WXTextObject textObject = new WXTextObject();  //初始化WXTextObject对象，填写分享的文本内容
        textObject.text = content;
        WXMediaMessage msg = new WXMediaMessage();//用WXTextObject对象初始化一个WXMedicalMessage对象
        msg.mediaObject = textObject;
        msg.description = content;
        SendMessageToWX.Req req = new SendMessageToWX.Req();  //构造一个Req
        req.message = msg; //transaction字段用于唯一标识一个请求
        req.scene = SendMessageToWX.Req.WXSceneSession;//分享到好友会话
        api.sendReq(req);  //调用api接口发送数据到微信
        // OnekeyShare oks = new OnekeyShare();
        // oks.setText(content);
        // oks.setTitle("分享到微信");
        // oks.setDialogMode();
        // oks.disableSSOWhenAuthorize();
        // if (platform != null) {
        //     oks.setPlatform(platform);
        // }
        // oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
        //     @Override
        //     public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
        //
        //     }
        //
        // });
        // oks.show(ReserveSuccessActivity.this);
    }


    private void shareLiveByWx(String platform, final String content, String url) {
        CustomLog.d(TAG, "shareLiveByWx:" + url + "liveShareString:"
                + liveShareString);
        //		OnekeyShare oks = new OnekeyShare();
        //		oks.setUrl(url);
        //		oks.disableSSOWhenAuthorize();
        //		if (platform != null) {
        //			oks.setPlatform(platform);
        //		}
        //		oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
        //
        //			@Override
        //			public void onShare(Platform platform,
        //					cn.sharesdk.framework.Platform.ShareParams paramsToShare) {
        //				if ("Wechat".equals(platform.getName())) {
        //					paramsToShare.setText(content);
        //					paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
        //					Bitmap logo = BitmapFactory.decodeResource(getResources(),
        //							MResource
        //									.getIdByName(MeetingRoomActivity.this,
        //											MResource.DRAWABLE,
        //											"jmeetingsdk_share_img"));
        //					paramsToShare.setImageData(logo);
        //				}
        //			}
        //		});
        //		oks.show(MeetingRoomActivity.this);
    }


    private void shareLiveByFriend(String platform, final String content,
                                   String url) {
        //		OnekeyShare oks = new OnekeyShare();
        //		oks.setUrl(url);
        //		oks.disableSSOWhenAuthorize();
        //		if (platform != null) {
        //			oks.setPlatform(platform);
        //		}
        //		oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
        //
        //			@Override
        //			public void onShare(Platform platform,
        //					cn.sharesdk.framework.Platform.ShareParams paramsToShare) {
        //				if ("WechatMoments".equals(platform.getName())) {
        //					paramsToShare.setTitle(content);
        //					paramsToShare.setShareType(Platform.SHARE_WEBPAGE);
        //					Bitmap logo = BitmapFactory.decodeResource(getResources(),
        //							MResource
        //									.getIdByName(MeetingRoomActivity.this,
        //											MResource.DRAWABLE,
        //											"jmeetingsdk_share_img"));
        //					paramsToShare.setImageData(logo);
        //				}
        //			}
        //		});
        //		oks.show(MeetingRoomActivity.this);
    }


    private void shareLiveBySms(String content) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", content);
        startActivity(intent);
    }


    @SuppressWarnings("deprecation")
    private void shareLiveByCopy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
        CustomToast.show(context, "已复制", CustomToast.LENGTH_SHORT);
    }


    private void showShare(boolean silent, final String platform) {
        //		final OnekeyShare oks = new OnekeyShare();
        //
        //		// final String text = new String();
        //		if (shareString != null) {
        //			oks.setText(shareString);
        //			oks.setTitle("share title");
        //
        //			oks.setDialogMode();
        //			oks.disableSSOWhenAuthorize();
        //			if (platform != null) {
        //				oks.setPlatform(platform);
        //			}
        //
        //			oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
        //
        //				@Override
        //				public void onShare(Platform platform,
        //						cn.sharesdk.framework.Platform.ShareParams paramsToShare) {
        //
        //					String text = "1234";
        //
        //					if ("WechatMoments".equals(platform.getName())) {
        //
        //						// 改写twitter分享内容中的text字段，否则会超长，
        //
        //						// 因为twitter会将图片地址当作文本的一部分去计算长度
        //
        //						text += "http://link.fobshanghai.com/rmb.htm";
        //
        //						paramsToShare.setText(text);
        //
        //					}
        //
        //				}
        //
        //			});
        //			oks.show(MeetingRoomActivity.this);
        //		} else {
        //			GetMeetingInvitationSMS getSMS = new GetMeetingInvitationSMS() {
        //				@Override
        //				protected void onSuccess(
        //						MeetingInvitationSMSInfo responseContent) {
        //					oks.setText(responseContent.invitationSMS);
        //					oks.setTitle("share title");
        //
        //					oks.setDialogMode();
        //					oks.disableSSOWhenAuthorize();
        //					if (platform != null) {
        //						oks.setPlatform(platform);
        //					}
        //
        //					oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
        //
        //						@Override
        //						public void onShare(
        //								Platform platform,
        //								cn.sharesdk.framework.Platform.ShareParams paramsToShare) {
        //
        //							String text = "1234";
        //
        //							if ("WechatMoments".equals(platform.getName())) {
        //
        //								// 改写twitter分享内容中的text字段，否则会超长，
        //
        //								// 因为twitter会将图片地址当作文本的一部分去计算长度
        //
        //								text += "http://link.fobshanghai.com/rmb.htm";
        //
        //								paramsToShare.setText(text);
        //
        //							}
        //
        //						}
        //
        //					});
        //					oks.show(MeetingRoomActivity.this);
        //				}
        //
        //				@Override
        //				protected void onFail(int statusCode, String statusInfo) {
        //					if (HttpErrorCode.checkNetworkError(statusCode)) {
        //						CustomToast.show(MeetingRoomActivity.this,
        //								"网络不给力，请检查网络！", Toast.LENGTH_LONG);
        //						return;
        //					} else {
        //						CustomLog.d(TAG, "短信邀请失败 错误码=" + statusInfo);
        //						if (CommonUtil.getNetWorkType(getApplicationContext()) == -1) {
        //							CustomToast.show(MeetingRoomActivity.this,
        //									"网络不给力，请检查网络！", Toast.LENGTH_LONG);
        //							return;
        //						}
        //					}
        //				}
        //
        //			};
        //
        //			String app;
        //
        //			if (MeetingManager.getInstance().getAppType()
        //					.equals(MeetingManager.MEETING_APP_TV)) {
        //				app = MeetingManager.MEETING_APP_BUTEL_CONSULTATION;
        //			} else {
        //				app = MeetingManager.getInstance().getAppType();
        //			}
        //
        //			getSMS.getMeetingInvitationSMS(app, "", mMeetingInfo.meetingId,
        //
        //			mMeetingInfo.accountId, mMeetingInfo.accountName);
        //		}

    }


    private BroadcastReceiver mNetWorkWatchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                int state = getCurNetType();
                String activityIp = CommonUtil.getLocalIpAddress(context);
                CustomLog
                        .d(TAG,
                                "MeetingRoomingActivity::ConnectivityManager.CONNECTIVITY_ACTION. getCurNetType:"
                                        + getCurNetType()
                                        + " | old Type:"
                                        + mMeetingInfo.netType
                                        + " | currIp:"
                                        + activityIp + " | old ip:" + localIp);
                // if (mMeetingInfo.netType != state) {
                // CustomToast.show(MeetingRoomActivity.this, "网络发生变化",
                // CustomToast.LENGTH_SHORT);
                // mMeetingInfo.netType = state;
                // if (getCurNetType() == NetType.WIFI) {
                // CustomToast.show(MeetingRoomActivity.this, "当前网络：WIFI",
                // CustomToast.LENGTH_SHORT);
                // if (mShowingDialog == ShowingDialog.NET_CHANGE_DIALOG) {
                // dismissDialog();
                // }
                // reJoinMeetingByNetWorkChange();
                // } else if (getCurNetType() == NetType.NO_NET) {
                // CustomToast.show(MeetingRoomActivity.this, "当前无网络",
                // CustomToast.LENGTH_SHORT);
                // } else {
                // if (mMeetingInfo.userTempNetChoose) {
                // CustomToast.show(MeetingRoomActivity.this, "当前网络：GPRS",
                // CustomToast.LENGTH_SHORT);
                // reJoinMeetingByNetWorkChange();
                // } else {
                // handleNetWorkChange();
                // }
                // }
                //
                // }

                // ip 发生变化需要重新加入会议
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra("networkInfo");
                State netState = networkInfo.getState();
                CustomLog.i(TAG, "network type:" + networkInfo.getTypeName()
                        + ",state:" + netState);

                if (State.CONNECTED == netState) {
                    if (activityIp == null || activityIp.equalsIgnoreCase("")) {
                        CustomLog
                                .e(TAG,
                                        "MeetingRoomActivity getlocal ip is null,return!");
                        return;
                    }

                    CustomLog.i(TAG, "old activity ip:" + localIp
                            + ",activity ip:" + activityIp);

                    // 网络类型不一致或者ip不相同需重新加入会议
                    if (mMeetingInfo.netType != state
                            || !activityIp.equalsIgnoreCase(localIp)) {

                        mMeetingInfo.netType = state;
                        localIp = activityIp;
                        if (getCurNetType() == NetType.WIFI) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "当前网络：WIFI", CustomToast.LENGTH_SHORT);
                            if (mShowingDialog == ShowingDialog.NET_CHANGE_DIALOG) {
                                dismissDialog();
                            }
                            reJoinMeetingByNetWorkChange();
                        } else if (getCurNetType() == NetType.NO_NET) {
                            CustomToast.show(MeetingRoomActivity.this, "当前无网络",
                                    CustomToast.LENGTH_SHORT);
                        } else {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "当前网络：GPRS", CustomToast.LENGTH_SHORT);
                            if (mMeetingInfo.userTempNetChoose) {
                                reJoinMeetingByNetWorkChange();
                            } else {
                                handleNetWorkChange();
                            }
                        }
                    } else {
                        CustomLog.w(TAG, "network not connectted");
                    }

                }
            }
        }
    };

    private BroadcastReceiver mPhoneStateBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mPhoneStateBroadCastReceiver");
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                Log.e(TAG, "用户主动打电话，退出会议室");
                exitMeeting(MeetingEvent.QUIT_PHONE_CALL);
            }
        }
    };

    private BroadcastReceiver mHeadSetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (mIsbluetooth) {
                            CustomLog.d(TAG,"有线耳机拔掉，存在蓝牙连接");
//                                    chooseBluetooth();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    chooseBluetooth();
                                }
                            }, 1000);
                        } else {
                            CustomLog.d(TAG,"有线耳机拔掉，不存在蓝牙连接，使用外放");
                            mIsHeadSetPlugged = false;
                            useNormalMode();
                        }
                        break;
                    case 1:
                        if (mIsbluetooth) {
                            CustomLog.d(TAG,"有线耳机连接上，存在蓝牙连接,使用有线耳机");
                            chooseheadset();
                        } else {
                            CustomLog.d(TAG,"有线耳机连接上，不存在蓝牙连接，使用有线耳机");
                            mIsHeadSetPlugged = true;
                            useInCallMode();
                        }
                        break;
                    default:
                        CustomLog.d(TAG, "未知状态");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mOrderOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    MeetingEvent.FORCE_OFFLINE_MESSAGE_BROADCAST)
                    || intent.getAction().equals(MeetingEvent.LOGOUT_BROADCAST)) {
                exitMeetingByOrderOff(MeetingEvent.QUIT_TOKEN_DISABLED);
            }
        }
    };

    private DialogInterface.OnCancelListener mCancelLoadingListener
            = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            showExitMeetingDialog();
        }
    };

    private Handler doInitHandler = new Handler() {
        public void handleMessage(Message msg) {
            CustomLog.d(TAG, "doInitHandler");
            MeetingManager.getInstance().setMeetingRoomRunningState(true);
            // MeetingApplication.shareInstance().setMeetingRoomRunningState(true);
            initSensor();
            // registerSensorManagerListener();
            useNormalMode();
            setFullScreen();
            mMeetingInfo.netType = getCurNetType();
            // mMeetingInfo.userTempNetChoose = getUserNetConfig();
            registerRequestQuitReceiver();
            registerNetWorkWatchReceiver();
            registerPhoneStateBroadCastReceiver();
            registerHeadSetReceiver();
            registerOrderOffReceiver();
            keepScreenOn();

            mNoSpeakerImg = (ImageView) findViewById(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_no_speaker_img"));
            mNoSpeakerTextView = (TextView) findViewById(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.ID,
                    "meeting_room_no_speaker_textview"));
            mGestureDetector = new GestureDetector(MeetingRoomActivity.this,
                    new GestureDetector.OnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            CustomLog.d(TAG, "onSingleTapUp");
                            // TODO
                            // mMenuView.showMainView();
                            // mMenuView.showMainButtonView();
                            return false;
                        }


                        @Override
                        public void onShowPress(MotionEvent e) {
                        }


                        @Override
                        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                                float distanceX, float distanceY) {
                            return false;
                        }


                        @Override
                        public void onLongPress(MotionEvent e) {
                        }


                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2,
                                               float velocityX, float velocityY) {

                            CustomLog.d(TAG, "onFling");
                            if (e1 == null || e2 == null) {
                                return false;
                            }
                            float x = e2.getX() - e1.getX();
                            float y = e2.getY() - e1.getY();
                            // 限制必须得划过屏幕的1/4才能算划过
                            float x_limit = mLimitScrollWidth;
                            float y_limit = mLimitScrollHeight;
                            float x_abs = Math.abs(x);
                            float y_abs = Math.abs(y);
                            if (x_abs >= y_abs) {
                                // gesture left or right
                                if (x > x_limit || x < -x_limit) {
                                    if (x > 0) {
                                        // right
                                        doResult(GESTURE_RIGHT);
                                    } else if (x <= 0) {
                                        // left
                                        doResult(GESTURE_LEFT);
                                    }
                                }
                            } else {
                                // gesture down or up
                                if (y > y_limit || y < -y_limit) {
                                    if (y > 0) {
                                        // down
                                        doResult(GESTURE_DOWN);
                                    } else if (y <= 0) {
                                        // up
                                        doResult(GESTURE_UP);
                                    }
                                }
                            }
                            return false;
                        }


                        @Override
                        public boolean onDown(MotionEvent e) {
                            return false;
                        }
                    });

            // useLargeVoice();

            getMeetingInfoFromFile();

            // ShareSDK.initSDK(this);

            GetMeetingInvitationSMS getSMS = new GetMeetingInvitationSMS() {
                @Override
                protected void onSuccess(
                        MeetingInvitationSMSInfo responseContent) {
                    shareString = responseContent.invitationSMS;
                    if (shareString != null) {
                        CustomLog.d(
                                TAG,
                                "onSuccess shareString:"
                                        + shareString.toString());
                    } else {
                        CustomLog.d(TAG, "onSuccess shareString == null");
                    }
                    super.onSuccess(responseContent);
                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    if (HttpErrorCode.checkNetworkError(statusCode)) {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，请检查网络！", Toast.LENGTH_LONG);
                        return;
                    } else {
                        CustomLog.d(TAG, "短信邀请失败 错误码=" + statusInfo);
                        if (CommonUtil.getNetWorkType(getApplicationContext()) == -1) {
                            CustomToast.show(MeetingRoomActivity.this,
                                    "网络不给力，请检查网络！", Toast.LENGTH_LONG);
                            return;
                        }
                    }
                }

            };

            String app;

            if (MeetingManager.getInstance().getAppType()
                    .equals(MeetingManager.MEETING_APP_TV)) {
                app = MeetingManager.MEETING_APP_BUTEL_CONSULTATION;
            } else {
                app = MeetingManager.getInstance().getAppType();
            }

            getSMS.getMeetingInvitationSMS(app, "", mMeetingInfo.meetingId,

                    mMeetingInfo.accountId, mMeetingInfo.accountName);

            // sendJoinMeetingBroadcast();

            createMsgWindow();

            mParticipantorList = new ParticipantorList(mMeetingInfo.accountId);
            //			mLoudspeakerInfoList = new ArrayList<LoudspeakerInfo>();
            initMeeting();
            setMeetingSharedPreferences(String.valueOf(mMeetingInfo.meetingId));
        }


        ;
    };

    private LinearLayout mMediaViewBg;

    // private MediaView mMediaView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(MResource.getIdByName(MeetingRoomActivity.this,
                MResource.LAYOUT, "jmeetingsdk_activity_meeting_room"));
        //		ShareSDK.initSDK(this);
        //注册到微信
        APP_ID = MeetingManager.getInstance().getmAppId();
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        api.registerApp(APP_ID);
        doInitHandler.sendEmptyMessageDelayed(0, 100);
        isNeedShowWindow = false;
    }


    @Override
    protected void onResume() {
        if (mMeetingInfo != null) {
            mMeetingInfo.isInBackground = false;
        }
        if (mButelOpenSDK != null) {
            CustomLog.d(TAG, "重新开启摄像头监控");
            mButelOpenSDK.startWatchCamera();
        }
        // registerSensorManagerListener();
		/*
		 * if (mMediaView != null) { mMediaView.refreshViewSize(); }
		 */
        sendJoinMeetingBroadcast();
        if (mMenuView != null) {
            mMenuView.postDelayed(new Runnable() {
                @Override public void run() {
                    mMenuView.hideFloatingView();
                    isNeedShowWindow = false;
                }
            }, FLOATING_WINDOW_DELAY);
        }

        setFloatingViewShowType(false);

        stopPitService();

        super.onResume();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        boolean isFromMessage = false;
        int meetingId = getIntent().getIntExtra(ConstConfig.MEETING_ID, 0);
        if (meetingId != 0
                && (mMeetingInfo.meetingId != 0 && mMeetingInfo.meetingId != meetingId)) {
            isFromMessage = true;
        }
        CustomLog.d(TAG, "onNewIntent currentMeetingId: "
                + mMeetingInfo.meetingId + " requestMeetingId: " + meetingId
                + " isFromMessage " + isFromMessage);
        if (isFromMessage) {
            showExitMeetingByMessageDialog();
        }
        /*if(isNeedShowWindow) {
            moveTaskToBack(true);
            return;
        }*/
        super.onNewIntent(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        // CustomToast.show(MeetingRoomActivity.this,"onStop",CustomToast.LENGTH_LONG);
        if (mMeetingInfo != null) {
            mMeetingInfo.isInBackground = true;
        }
        if (mButelOpenSDK != null) {
            mButelOpenSDK.stopWatchCamera();
        }
        // unRegisterSensorManagerListener();
        startPitService();

        super.onStop();
        if (mMenuView != null) {
            //if(isAppOnForeground()) {
            //  if(isNeedShowWindow) {
            //    mMenuView.showFloatingView();
            //  isNeedShowWindow = false;
            // }
            // }else{
            //     isNeedShowWindow = true;
            // }
        }
    }

    /**
     * 停止坑位服务
     */
    private void stopPitService() {
        CustomLog.i(TAG,"stopPitService()");

        Intent stopIntent = new Intent(this, PitService.class);
        stopService(stopIntent);
    }


    /**
     * 开启坑位服务，此服务为前台服务，用以提高 JMeetingService 进程的优先级，减少被系统回收的概率
     */
    private void startPitService() {
        CustomLog.i(TAG,"startPitService()");
        Intent startIntent = new Intent(this, PitService.class);
        startService(startIntent);
    }


    public boolean isAppOnForeground() {
        ActivityManager mActivityManager = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE));
        String mPackageName = getPackageName();
        if (mActivityManager.getRunningTasks(1).size() > 0) {
            // 应用程序位于堆栈的顶层
            if (mPackageName.equals(mActivityManager.getRunningTasks(1).get(0).topActivity
                    .getPackageName())) {
                //CustomToast.show(MeetingRoomActivity.this,"isAppOnForeground true",CustomToast.LENGTH_LONG);
                return true;
            }
        }
        return false;
    }


    private void release() {
        setMeetingSharedPreferences("");
        if(mScreenOrientationEventListener!=null){
            mScreenOrientationEventListener.disable();
        }

        if (mButelOpenSDK != null) {
            mButelOpenSDK.exitMeeting();
            mButelOpenSDK.release();
            mButelOpenSDK = null;
        }
        removeFloatingViewParamsListener();
        cancelKeepScreenOn();
        cancelFullScreen();
        // sendExitMeetingBroadcast();
        unRegisterNetWorkWatchReceiver();
        unRegisterPhoneStateBroadCastReceiver();
        unRegisterHeadSetReceiver();
        unRegisterOrderOffReceiver();
        unRegisterRequestQuitReceiver();
        try {
            chooseSpeaker();
            unregisterReceiver(bluetoothReceiver);
        }catch (Exception ex){
            CustomLog.d(TAG,"unregisterReceiver(bluetoothReceiver)  ex: " + ex);
        }
        releaseMeeting();
        dismissLoading();
        // MeetingApplication.shareInstance().setMeetingRoomRunningState(false);
        MeetingManager.getInstance().setMeetingRoomRunningState(false);
        InfoReportManager.stopMeetingAndReport();
        releaseWorkThread();
        releaseMsgWindow();
        finish();
    }


    @Override
    protected void onDestroy() {
        if (mButelOpenSDK != null) {
            release();
        }
        isNeedShowWindow = false;
        setMeetingSharedPreferences("");

        stopPitService();

        super.onDestroy();
    }


    private void getMeetingInfoFromFile() {
        mMeetingInfo.meetingId = getIntent().getIntExtra(
                ConstConfig.MEETING_ID, mMeetingInfo.meetingId);
        mMeetingInfo.token = getIntent().getStringExtra(INTENT_EXTRA_TOKEN);
        mMeetingInfo.accountId = getIntent().getStringExtra(
                INTENT_EXTRA_ACCOUNT_ID);
        mMeetingInfo.accountName = getIntent().getStringExtra(
                INTENT_EXTRA_ACCOUNT_NAME);
        mMeetingInfo.userTempNetChoose = getIntent().getBooleanExtra(
                INTENT_EXTRA_IS_ALLOW_MOBILE_NET, false);

        mMeetingInfo.selectSystemCamera = getIntent().getBooleanExtra(
                INTENT_EXTRA_SELECT_SYSTEM_CAMERA, true);

        GroupId = getIntent().getStringExtra("groupId");

        // String encoding = "UTF-8";
        // File file = new
        // File(Environment.getExternalStorageDirectory().getPath()
        // + "/meeting/config/meetingInfo.txt");
        // InputStreamReader inputReader = null;
        // BufferedReader bufferReader = null;
        // try {
        // InputStream inputStream = new FileInputStream(file);
        // inputReader = new InputStreamReader(inputStream);
        // bufferReader = new BufferedReader(inputReader);
        //
        // // 读取一行
        // String line = null;
        // StringBuffer strBuffer = new StringBuffer();
        //
        // while ((line = bufferReader.readLine()) != null) {
        // strBuffer.append(line);
        // }
        // CustomLog.e(TAG, "jsonStr = " + strBuffer.toString());
        // JSONObject jsonObject = new JSONObject(strBuffer.toString().trim());
        // // mMeetingInfo.meetingId = jsonObject.getInt("meetingId");
        // // mMeetingInfo.token = jsonObject.getString("token");
        // // mMeetingInfo.accountId = jsonObject.getString("accountId");
        // // mMeetingInfo.accountName = jsonObject.getString("accountName");
        //
        // mMeetingInfo.meetingId =
        // getIntent().getIntExtra(ConstConfig.MEETING_ID,
        // mMeetingInfo.meetingId);
        // mMeetingInfo.token = AccountManager.getInstance(this).getToken();
        // mMeetingInfo.accountId = AccountManager.getInstance(this)
        // .getAccountInfo().nubeNumber;
        // mMeetingInfo.accountName = AccountManager.getInstance(this)
        // .getAccountInfo().nickName;
        // CustomLog.e(TAG, "meetingId = " + mMeetingInfo.meetingId);
        // CustomLog.e(TAG, "token = " + mMeetingInfo.token);
        // CustomLog.e(TAG, "accountId = " + mMeetingInfo.accountId);
        // CustomLog.e(TAG, "accountName = " + mMeetingInfo.accountName);
        // bufferReader.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // } catch (JSONException e) {
        // e.printStackTrace();
        // }
    }

	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) { if
	 * (!mMeetingInfo.isJoinMeeting) { return true; } return
	 * mGestureDetector.onTouchEvent(event); }
	 */


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			/*
			 * if (episodeState == 1) { if (!isExistEpisode) { isExistEpisode =
			 * true; CustomToast.show(MeetingRoomActivity.this, "再按一次，退出插话！",
			 * CustomToast.LENGTH_SHORT); } else { isExistEpisode = false;
			 * dismissLoading(); mMenuView.hideEpisodeView();
			 * mButelOpenSDK.askForEpisodeStop(mAskForStopEpisodeListener);
			 * episodeState = 0; } } else {
			 */
            sendMeetingOperationBroadCast(MeetingEvent.MEETING_EXIT);
            showExitMeetingDialog();
            // if(mMenuView!=null){
            //    mMenuView.showFloatingView();
            //}
            //moveTaskToBack(true);
            // }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            CustomLog.d(TAG, "音量加");
            setCallVolume(this, true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            CustomLog.d(TAG, "音量减");
            setCallVolume(this, false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void doResult(int result) {
        switch (result) {
            case GESTURE_UP:
                CustomLog.d(TAG, "GESTURE_UP");
                break;
            case GESTURE_DOWN:
                CustomLog.d(TAG, "GESTURE_DOWN");
                break;
            case GESTURE_LEFT:
                CustomLog.d(TAG, "GESTURE_LEFT");
                if (mButelOpenSDK != null) {
                    // MobclickAgent.onEvent(MeetingRoomActivity.this,
                    // AnalysisConfig.SWITCH_WINDOW);
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_SWITCHWINDOW);
                    // mButelOpenSDK.changeMode(0, 1);
                    //mMenuView.getmMultiSpeakerView().flingPageLeft();
                }
                break;
            case GESTURE_RIGHT:
                CustomLog.d(TAG, "GESTURE_RIGHT");
                if (mButelOpenSDK != null) {
                    // MobclickAgent.onEvent(MeetingRoomActivity.this,
                    // AnalysisConfig.SWITCH_WINDOW);
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_SWITCHWINDOW);
                    // mButelOpenSDK.changeMode(0, 2);
                    //mMenuView.getmMultiSpeakerView().flingPageRight();
                }
                break;
            default:
                break;
        }
    }


    private void createMsgWindow() {
        mMessageReminderManage = new MessageReminderManage();
        MessageReminderView messageReminderView = (MessageReminderView) findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "meeting_room_message_reminder_view"));
        mMessageReminderManage.init(this, messageReminderView);
    }


    private void releaseMsgWindow() {
        if (mMessageReminderManage != null) {
            mMessageReminderManage.release();
        }
    }


    private void showLoading(String msg) {
        removeLoadingView();
        showLoadingView(msg, mCancelLoadingListener, false);
    }


    private void dismissLoading() {
        removeLoadingView();
    }


    private void speakerOnLine(String accountId, String accountName) {
        mMenuView.speakerOnLine(accountId, accountName);
    }


    private void speakerOffLine(String accountId) {
        mMenuView.speakerOffLine(accountId);
    }


    private void showNoSpeakerTip() {
        CustomLog.d(TAG, "showNoSpeakerTip");
        mNoSpeakerImg.setVisibility(View.VISIBLE);
        mNoSpeakerTextView.setVisibility(View.VISIBLE);
    }


    private void hideNoSpeakerTip() {
        CustomLog.d(TAG, "hideNoSpeakerTip");
        mNoSpeakerImg.setVisibility(View.INVISIBLE);
        mNoSpeakerTextView.setVisibility(View.INVISIBLE);
    }


    private BroadcastReceiver inviteMeetingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            CustomLog.d(TAG, "收到邀请参会广播");
            ArrayList<InviteMeetingInfo> inviteInfoList = (ArrayList<InviteMeetingInfo>) arg1
                    .getSerializableExtra("INVITE_MESSAGE");
            if (inviteInfoList != null) {
                for (int i = 0; i < inviteInfoList.size(); i++) {
                    MessageInfo inviteMeetingMsgInfo = new MessageInfo();
                    inviteMeetingMsgInfo.type = MessageReminderManage.MEETING_INVITE;
                    if (MeetingManager
                            .getInstance()
                            .getAppType()
                            .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                        inviteMeetingMsgInfo.msg = inviteInfoList.get(i)
                                .getInviterAccountName() + "邀请您加入会诊";
                    } else {
                        inviteMeetingMsgInfo.msg = inviteInfoList.get(i)
                                .getInviterAccountName() + "邀请您加入会议";
                    }

                    if (mMessageReminderManage != null) {
                        mMessageReminderManage
                                .sendMessage(inviteMeetingMsgInfo);
                    }
                }
            }
            // if (mMeetingInfo.isInBackground) {
            // showExitMeetingByMessageDialog();
            // }
        }
    };


    private void sendJoinMeetingBroadcast() {
        CustomLog.d(TAG, "发送开始会议广播");
        Intent intent = new Intent();
        intent.setPackage(MeetingManager.getInstance().getRootDirectory());
        intent.setAction(START_MEETING_BROADCAST);
        sendBroadcast(intent);

        IntentFilter intentFilter = new IntentFilter(
                HostAgentOperation.MEETING_INVITED_BROADCAST);
        try {
            this.registerReceiver(inviteMeetingBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            CustomLog.i(TAG, e.getMessage());
        }

        CustomLog.d(TAG, "发送开始会议广播结束");
    }


    private void sendExitMeetingBroadcast(int code) {
        CustomLog.d(TAG, "发送退出会议广播");
        Intent intent = new Intent();
        intent.setPackage(MeetingManager.getInstance().getRootDirectory());
        intent.setAction(END_MEETING_BROADCAST);
        intent.putExtra("eventCode", code);
        sendBroadcast(intent);
        try {
            MeetingRoomActivity.this
                    .unregisterReceiver(inviteMeetingBroadcastReceiver);
        } catch (Exception e) {
            CustomLog.i(TAG, e.getMessage());
        }
        CustomLog.d(TAG, "发送退出会议广播结束");
    }


    private void sendInviteBroadcast(String inviteeId, String inviteeName) {
        CustomLog.d(TAG, "发送会议邀请广播,inviteeId: " + inviteeId + " | inviteeName:"
                + inviteeName);
        Intent intent = new Intent();
        intent.setPackage(MeetingManager.getInstance().getRootDirectory());
        intent.setAction(INVITE_MEETING_BROADCAST);
        intent.putExtra("inviteeId", inviteeId);
        intent.putExtra("inviteeName", inviteeName);
        intent.putExtra("meetingId", String.valueOf(mMeetingInfo.meetingId));
        sendBroadcast(intent);

        CustomLog.d(TAG, "发送会议邀请广播结束");
    }


    private void showExitMeetingDialog() {
        if (mShowingDialog == ShowingDialog.INVITE_DIALOG
                || mShowingDialog == ShowingDialog.NET_CHANGE_DIALOG) {
            return;
        }
        dismissDialog();
        mDialog = new CustomDialog(this);
        mDialog.setTitle("退出");
        mDialog.setCancelable(false);
        if (MeetingManager.getInstance().getAppType()
                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
            mDialog.setTip("您确定要退出会诊吗？");
        } else {
            mDialog.setTip("您确定要退出会议吗？");
        }
        mDialog.setOkBtnText("确定");
        mDialog.setCancelBtnText("取消");
        mDialog.setBlackTheme();
        mDialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                exitMeeting(MeetingEvent.QUIT_MEETING_BACK);
                dismissDialog();
            }
        });
        mDialog.setCancelBtnOnClickListener(new CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dismissDialog();
            }
        });
        mShowingDialog = ShowingDialog.EXIT_DIALOG;
        mDialog.show();

    }


    private void showExitMeetingByMessageDialog() {
        dismissDialog();
        mDialog = new CustomDialog(this);
        if (MeetingManager.getInstance().getAppType()
                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
            mDialog.setTitle("会诊邀请");
            mDialog.setTip("收到会诊邀请，是否进入会诊列表查看？");
        } else {
            mDialog.setTitle("会议邀请");
            mDialog.setTip("收到会议邀请，是否进入会议列表查看？");
        }

        mDialog.setCancelable(false);
        mDialog.setOkBtnText("确定");
        mDialog.setCancelBtnText("取消");
        mDialog.setBlackTheme();
        mDialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dismissDialog();
                exitMeetingToMeetingList(MeetingEvent.QUIT_MEETING_AS_MESSAGE_INVITE);
            }
        });
        mDialog.setCancelBtnOnClickListener(new CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dismissDialog();
            }
        });
        mShowingDialog = ShowingDialog.INVITE_DIALOG;
        mDialog.show();
    }


    private void showNetWorkChangeDialog() {
        CustomLog.d(TAG, "showNetWorkChangeDialog");
        if (mShowingDialog == ShowingDialog.INVITE_DIALOG) {
            return;
        }
        dismissDialog();
        mDialog = new CustomDialog(this);
        mDialog.setCancelable(false);
        mDialog.setTip("您当前正在使用移动网络，继续使用将产生流量费用，是否继续");
        mDialog.setOkBtnText("继续");
        mDialog.setCancelBtnText("取消");
        mDialog.setBlackTheme();
        mDialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                CustomLog.d(TAG,
                        "showNetWorkChangeDialog----OKBtnOnClickListener");
                dismissDialog();

                mMeetingInfo.userTempNetChoose = true;
                initMeeting();
            }
        });
        mDialog.setCancelBtnOnClickListener(new CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                CustomLog.d(TAG,
                        "showNetWorkChangeDialog----CancelBtnOnClickListener");
                exitMeeting(MeetingEvent.QUIT_MEETING_NOTALLOW_USE_MOBILE_NET);
                dismissDialog();
            }
        });
        mShowingDialog = ShowingDialog.NET_CHANGE_DIALOG;
        mDialog.show();
    }


    private void initMenuView() {
        addFloatViewParamsListener(new FloatingViewListener());
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        // float density = metric.density;
        mMenuView.init(mMenuViewListener, mInvitePersonList,
                mParticipantorList, mMeetingInfo.accountName,
                mMeetingInfo.accountId, mMeetingInfo.meetingId, mButelOpenSDK,
                metric, mMeetingInfo.token, GroupId, mListener);
        mMenuView.showMainButtonView();
        mMenuView.showEpisodeButtonView();
        // MediaView mediaView = (MediaView)
        // findViewById(R.id.meeting_room_media_view);
        LinearLayout.LayoutParams mediaViewLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mMediaViewBg.removeAllViews();
        // mMediaViewBg.addView(mMediaView, mediaViewLP);
        menuViewisInit = true;
    }


    private void initMeeting() {

        if (getCurNetType() == NetType.WIFI) {
            CustomToast.show(this, "当前网络：WIFI", CustomToast.LENGTH_SHORT);
        } else if (getCurNetType() == NetType.NO_NET) {
            CustomToast.show(this, "当前无网络", CustomToast.LENGTH_SHORT);
        } else {
            if (!mMeetingInfo.userTempNetChoose) {
                handleNetWorkChange();
                return;
            }
        }
        mMenuView = (MenuView) findViewById(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.ID,
                "meeting_room_menu_view"));
        mMediaViewBg = (LinearLayout) findViewById(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.ID,
                "meeting_room_media_view_bg"));
        // mMediaView = new MediaView(this);
        isOpenCamera = true;
        if (MeetingManager.getInstance().getAppType()
                .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
            showLoading("加入会诊");
        } else {
            showLoading("加入会议");
        }

        mButelOpenSDK = new ButelOpenSDK(MeetingRoomActivity.this);
        if (mButelOpenSDK.init(mMeetingInfo.accountId,
                mMeetingInfo.accountName, mMeetingInfo.meetingId,

                1, MeetingManager.getInstance().getConfigPath(),

                MeetingManager.getInstance().getLogPath(),

                MeetingManager.getInstance().getRcAddress(),

                "phone", MResource.getIdByName(MeetingRoomActivity.this,
                        MResource.DRAWABLE, "meeting_room_close_camera_vertical"),mMeetingInfo.selectSystemCamera) < 0) {
            CustomToast.show(MeetingRoomActivity.this, "初始化失败",
                    CustomToast.LENGTH_LONG);
            exitMeeting(MeetingEvent.QUIT_MEETING_LIBS_ERROR);
        } else {
            mButelOpenSDK
                    .addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
            MeetingManager
                    .getInstance()
                    .getHostAgentOperation()
                    .shareAccountInfo(
                            NetConnectHelper
                                    .getLocalIp(MeetingRoomActivity.this),
                            mButelOpenSDK.getScreenSharingPort(),
                            mMeetingInfo.accountId, mMeetingInfo.accountName);
            initMenuView();
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (null != networkInfo) {
                int netType = networkInfo.getType();
                if (ConnectivityManager.TYPE_WIFI == netType) {
                    netType = 1;
                } else if (ConnectivityManager.TYPE_MOBILE == netType) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(
                            Context.TELEPHONY_SERVICE);
                    Log.e(TAG, "getNetworkType " + tm.getNetworkType());
                    // if (tm.getNetworkType() ==
                    // TelephonyManager.NETWORK_TYPE_HSPAP) {
                    // netType = 3;
                    // } else if (tm.getNetworkType() ==
                    // TelephonyManager.NETWORK_TYPE_LTE) {
                    // netType = 2;
                    // } else {
                    // netType = -1;
                    // }
                    if (getNetworkClass(tm.getNetworkType()) == NETWORK_CLASS_2_G) {
                        netType = 1;
                    } else if (getNetworkClass(tm.getNetworkType()) == NETWORK_CLASS_3_G) {
                        netType = 3;
                    } else if (getNetworkClass(tm.getNetworkType()) == NETWORK_CLASS_4_G) {
                        netType = 2;
                    } else {
                        netType = -1;
                    }
                    String operator = tm.getSimOperator();
                    if (operator != null) {
                        if (operator.equals("46000")
                                || operator.equals("46002")
                                || operator.equals("46007")) {
                            // 中国移动
                            if (netType == 3) {
                                netType = 4;
                            } else if (netType == 2) {
                                netType = 5;
                            } else if (netType == 1) {
                                netType = 10;
                            }
                        } else if (operator.equals("46001")) {
                            // 中国联通
                            if (netType == 3) {
                                netType = 6;
                            } else if (netType == 2) {
                                netType = 7;
                            } else if (netType == 1) {
                                netType = 11;
                            }
                        } else if (operator.equals("46003")) {
                            // 中国电信
                            if (netType == 3) {
                                netType = 8;
                            } else if (netType == 2) {
                                netType = 9;
                            } else if (netType == 1) {
                                netType = 12;
                            }
                        }
                    }
                } else {
                    netType = -1;
                }
                localIp = CommonUtil.getLocalIpAddress(this);
                // mButelOpenSDK.setNetInfo(localIp, netType);
                // PhoneStateMonitor.getInstance(MeetingRoomActivity.this)
                // .addListener(mListener);
                // mButelOpenSDK.setNetInfo(localIp, netType, PhoneStateMonitor
                // .getInstance(MeetingRoomActivity.this).getLinkSpeed(),
                // PhoneStateMonitor.getInstance(this).getRSSI());
            }
            try {
                if (MeetingManager.getInstance().getVideoParameter(0) != null) {
                    CustomLog.d(TAG, "setVideoParam 0 ");
                    mButelOpenSDK.setVideoParam(0, MeetingManager.getInstance()
                            .getVideoParameter(0));
                }
                if (MeetingManager.getInstance().getVideoParameter(1) != null) {
                    CustomLog.d(TAG, "setVideoParam 1 ");
                    mButelOpenSDK.setVideoParam(1, MeetingManager.getInstance()
                            .getVideoParameter(1));
                }
                if (MeetingManager.getInstance().getVideoParameter(2) != null) {
                    CustomLog.d(TAG, "setVideoParam 2 ");
                    mButelOpenSDK.setVideoParam(2, MeetingManager.getInstance()
                            .getVideoParameter(2));
                }
            } catch (Exception ex) {
                CustomLog.d(TAG, "没有相机权限");
                CustomToast.show(MeetingRoomActivity.this,
                        getResources().getString(R.string.please_turn_on_camera_permissions),
                        CustomToast.LENGTH_LONG);
            }

            // mButelOpenSDK.setVideoCaptureRate(MeetingManager.getInstance()
            // .getCameraCollectionMode());

            mMeetingInfo.isAutoSpeak = getIntent().getBooleanExtra(
                    INTENT_EXTRA_IS_AUTO_SPEAK, true);
            mButelOpenSDK.setAutoSpeak(mMeetingInfo.isAutoSpeak);
            CustomLog.d(TAG,"MeetingRoomActivity 是否开启自适应" + MeetingManager.getInstance(). getmIsMeetingAdapter());
            int setAdaptState = mButelOpenSDK.setAdaptState(MeetingManager.getInstance().getmIsMeetingAdapter());
            CustomLog.d(TAG,"自适应同步返回： " + setAdaptState);
            int result = mButelOpenSDK.joinMeeting(mJoinMeetingListener,
                    mMeetingInfo.token, "");
            CustomLog.i(TAG, "token == " + mMeetingInfo.token);
            if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                if (mMeetingInfo.mIsDealWith981) {
                    if (MeetingManager
                            .getInstance()
                            .getAppType()
                            .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，您已退出会诊，请尝试重新参会", Toast.LENGTH_LONG);
                    } else {
                        CustomToast.show(MeetingRoomActivity.this,
                                "网络不给力，您已退出会议，请尝试重新参会", Toast.LENGTH_LONG);
                    }

                    String str = KeyEventConfig.DEAL_981_EVENT + "_fail_"
                            + mMeetingInfo.accountId + "_处理981失败";
                    KeyEventWrite.write(str);
                    CustomLog.d(TAG, "981处理失败，退出会议");
                }
                dismissLoading();
                exitMeeting(MeetingEvent.QUIT_MEETING_SERVER_DESCONNECTED);
            }
        }
    }


    private int getNetType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        int netType = 0;
        if (null != networkInfo) {
            netType = networkInfo.getType();
            if (ConnectivityManager.TYPE_WIFI == netType) {
                netType = 1;
            } else if (ConnectivityManager.TYPE_MOBILE == netType) {
                TelephonyManager tm = (TelephonyManager) getSystemService(
                        Context.TELEPHONY_SERVICE);
                Log.e(TAG, "getNetType " + tm.getNetworkType());
                if (getNetworkClass(tm.getNetworkType()) == NETWORK_CLASS_2_G) {
                    netType = 1;
                } else if (getNetworkClass(tm.getNetworkType()) == NETWORK_CLASS_3_G) {
                    netType = 3;
                } else if (getNetworkClass(tm.getNetworkType()) == NETWORK_CLASS_4_G) {
                    netType = 2;
                } else {
                    netType = -1;
                }
                String operator = tm.getSimOperator();
                if (operator != null) {
                    if (operator.equals("46000") || operator.equals("46002")
                            || operator.equals("46007")) {
                        // 中国移动
                        if (netType == 3) {
                            netType = 4;
                        } else if (netType == 2) {
                            netType = 5;
                        } else if (netType == 1) {
                            netType = 10;
                        }
                    } else if (operator.equals("46001")) {
                        // 中国联通
                        if (netType == 3) {
                            netType = 6;
                        } else if (netType == 2) {
                            netType = 7;
                        } else if (netType == 1) {
                            netType = 11;
                        }
                    } else if (operator.equals("46003")) {
                        // 中国电信
                        if (netType == 3) {
                            netType = 8;
                        } else if (netType == 2) {
                            netType = 9;
                        } else if (netType == 1) {
                            netType = 12;
                        }
                    }
                }
            } else {
                netType = -1;
            }
        }
        return netType;

    }


    /** Unknown network class. {@hide} */
    public static final int NETWORK_CLASS_UNKNOWN = 0;

    /** Class of broadly defined "2G" networks. {@hide} */
    public static final int NETWORK_CLASS_2_G = 1;

    /** Class of broadly defined "3G" networks. {@hide} */
    public static final int NETWORK_CLASS_3_G = 2;

    /** Class of broadly defined "4G" networks. {@hide} */
    public static final int NETWORK_CLASS_4_G = 3;


    private int getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }


    private void releaseMeeting() {
        CustomLog.d(TAG, "MeetingRoomActivity::releaseMeeting()");
        mMeetingInfo.isJoinMeeting = false;
        releaseView();
        if (mButelOpenSDK != null) {
            mButelOpenSDK.release();
            mButelOpenSDK = null;
        }

        if (mMediaViewBg != null) {
            mMediaViewBg.removeAllViews();
            // mMediaView = null;
        }
        if (mMenuView != null) {
            mMenuView.release();
            mMenuView = null;
        }
        // PhoneStateMonitor.getInstance(MeetingRoomActivity.this).removeListener(
        // mListener);
    }


    private void releaseView() {
        // 所有对话框若显示则关闭
        if (popupWindowMode != null && popupWindowMode.isShowing()) {
            popupWindowMode.dismiss();
            popupWindowMode = null;
        }

        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }

        if (mSwitchAudioModeDialog != null
                && mSwitchAudioModeDialog.isShowing()) {
            mSwitchAudioModeDialog.dismiss();
            mSwitchAudioModeDialog = null;
        }

        if (mSwitchAudioAlertDialog != null
                && mSwitchAudioAlertDialog.isShowing()) {
            mSwitchAudioAlertDialog.dismiss();
            mSwitchAudioAlertDialog = null;
        }
        if (mSwitchVideoAlertDialog != null
                && mSwitchVideoAlertDialog.isShowing()) {
            mSwitchVideoAlertDialog.dismiss();
            mSwitchVideoAlertDialog = null;
        }
        if (mSwitchPlayAlertDialog != null
                && mSwitchPlayAlertDialog.isShowing()) {
            mSwitchPlayAlertDialog.dismiss();
            mSwitchPlayAlertDialog = null;
        }
        if (liveDiaglog != null && liveDiaglog.isShowing()) {
            liveDiaglog.dismiss();
            liveDiaglog = null;
        }
        if (shareDialog != null && shareDialog.isShowing()) {
            shareDialog.dismiss();
            shareDialog = null;
        }
    }


    private void reJoinMeetingByNetWorkChange() {
        CustomLog.d(TAG, "MeetingRoomActivity::reJoinMeetingByNetWorkChange() "+getFloatingViewShowType());
        if(getFloatingViewShowType()) {
            if (mMenuView != null) {
                mMenuView.hideFloatingView();
                releaseMeeting();
                initMeeting();
                MeetingManager.getInstance()
                        .joinMeeting(MeetingManager.getInstance().getToken(),
                                MeetingManager.getInstance().getAccountID(), MeetingManager.getInstance().getAccountName(),
                                MeetingManager.getInstance().getMeetingId());
                CustomLog.d(TAG, "MeetingRoomActivity::reJoinMeetingByNetWorkChange() 小窗情况");
            }
        }else {
            releaseMeeting();
            initMeeting();
        }
    }


    private void handleNetWorkChange() {
        CustomLog.d(TAG, "MeetingRoomActivity::handleNetWorkChange()");
        if(getFloatingViewShowType()) {
            if (mMenuView != null) {
                mMenuView.hideFloatingView();
                releaseMeeting();
                initMeeting();
                MeetingManager.getInstance()
                        .joinMeeting(MeetingManager.getInstance().getToken(),
                                MeetingManager.getInstance().getAccountID(), MeetingManager.getInstance().getAccountName(),
                                MeetingManager.getInstance().getMeetingId());
                CustomLog.d(TAG, "MeetingRoomActivity::handleNetWorkChange() 小窗情况");
            }
        }else {
            releaseMeeting();
            showNetWorkChangeDialog();
        }
    }


    private void exitMeeting(int code) {
        MeetingManager.getInstance().setMeetingRoomRunningState(false);
        if (mButelOpenSDK != null && mButelOpenSDK.getSpeakers() != null) {
            for (int i = 0; i < mButelOpenSDK.getSpeakers().size(); i++) {
                if (mButelOpenSDK.getSpeakers().get(i).getAccountId()
                        .equals(mMeetingInfo.accountId)) {
                    mButelOpenSDK.stopLocalVideo(MediaType.TYPE_VIDEO);
                } else {
                    mButelOpenSDK.stopRemoteVideo(mButelOpenSDK.getSpeakers()
                            .get(i).getAccountId(), MediaType.TYPE_VIDEO);
                }
            }
        }
        sendExitMeetingBroadcast(code);
        release();
        // if (MeetingManager.getInstance().getAppType()
        //     .equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
        //     // TODO 普通极会议未处理
        //     Intent intent = new Intent("android.intent.action.MainActivity");
        //     String pkg = "cn.redcdn.hvs";
        //     String cls = "cn.redcdn.meeting.MainActivity";
        //     intent.setComponent(new ComponentName(pkg, cls));
        //     startActivity(intent);
        // }
    }


    private void exitMeetingByOrderOff(int code) {
        // MeetingApplication.shareInstance().setMeetingRoomRunningState(false);
        MeetingManager.getInstance().setMeetingRoomRunningState(false);
        sendExitMeetingBroadcast(code);
        release();
    }


    private void exitMeetingToMeetingList(int code) {
        // MeetingApplication.shareInstance().setMeetingRoomRunningState(false);
        MeetingManager.getInstance().setMeetingRoomRunningState(false);
        sendExitMeetingBroadcast(code);
        release();
    }


    private void exitMeetingForTokenTimeOut(int Code) {
        MeetingManager.getInstance().setMeetingRoomRunningState(false);
        // MeetingApplication.shareInstance().setMeetingRoomRunningState(false);
        if (MeetingManager.getInstance().getAccountManagerOperation() != null) {
            MeetingManager.getInstance().getAccountManagerOperation()
                    .tokenAuthFail(Code);
        }
        sendExitMeetingBroadcast(Code);
        release();
    }


    private void registerNetWorkWatchReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetWorkWatchReceiver, mFilter);
    }


    private void unRegisterNetWorkWatchReceiver() {
        try {
            unregisterReceiver(mNetWorkWatchReceiver);
        } catch (Exception e) {
            CustomLog.d(TAG, "unRegisterNetWorkWatchReceiver:" + e.toString());
        }
    }


    /**
     * 获取当前的网络状态 -1：没有网络 1：WIFI网络2：wap网络3：net网络
     */
    private int getCurNetType() {
        int netType = NetType.NO_NET;
        // ConnectivityManager connMgr = (ConnectivityManager)
        // getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // if (networkInfo == null) {
        // return netType;
        // }
        // int nType = networkInfo.getType();
        // if (nType == ConnectivityManager.TYPE_MOBILE) {
        // Log.e("networkInfo.getExtraInfo()", "networkInfo.getExtraInfo() is "
        // + networkInfo.getExtraInfo());
        // if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
        // netType = NetType.CMNET;
        // } else {
        // netType = NetType.CMWAP;
        // }
        // } else if (nType == ConnectivityManager.TYPE_WIFI) {
        // netType = NetType.WIFI;
        // }
        // return netType;

        int networkType = NetConnectHelper.getNetWorkType(this);
        CustomLog.d(TAG, "getCurNetType 获取当前的网络状态:" + networkType);
        switch (networkType) {
            case NetConnectHelper.NETWORKTYPE_INVALID:
                break;
            case NetConnectHelper.NETWORKTYPE_WIFI:
                netType = NetType.WIFI;
                break;
            case NetConnectHelper.NETWORKTYPE_2G:
            case NetConnectHelper.NETWORKTYPE_3G:
            case NetConnectHelper.NETWORKTYPE_WAP:
                netType = NetType.CMNET;
                break;
            case NetConnectHelper.NETWORKTYPE_ETHERNET:
                netType = NetType.WIFI;
                break;
        }
        CustomLog.d(TAG, "getCurNetType 当前的网络状态:" + netType);
        return netType;
    }


    private PhoneStateListener callListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                CustomLog.d(TAG, "收到来电，退出会议室");
                exitMeeting(MeetingEvent.QUIT_PHONE_CALL);
            }
        }
    };


    private void registerPhoneStateBroadCastReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(mPhoneStateBroadCastReceiver, mFilter);

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    private void unRegisterPhoneStateBroadCastReceiver() {
        unregisterReceiver(mPhoneStateBroadCastReceiver);

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(callListener, PhoneStateListener.LISTEN_NONE);
    }


    private void registerHeadSetReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadSetReceiver, filter);
    }


    private void unRegisterHeadSetReceiver() {
        unregisterReceiver(mHeadSetReceiver);
    }


    private void registerOrderOffReceiver() {
        CustomLog.i(TAG,
                "MeetingRoomActivity::registerOrderOffReceiver() 注册退出登录广播");
        IntentFilter filter = new IntentFilter();
        filter.addAction(MeetingEvent.FORCE_OFFLINE_MESSAGE_BROADCAST);
        filter.addAction(MeetingEvent.LOGOUT_BROADCAST);
        registerReceiver(mOrderOffReceiver, filter);
    }


    private void unRegisterOrderOffReceiver() {
        CustomLog.i(TAG,
                "MeetingRoomActivity::unRegisterOrderOffReceiver() 取消注册退出登录广播");
        unregisterReceiver(mOrderOffReceiver);
    }


    private void registerRequestQuitReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RUQUEST_QUIT_MEETING_BROADCAST);
        registerReceiver(mRequestQuitReceiver, filter);
    }


    private void unRegisterRequestQuitReceiver() {
        try {
            unregisterReceiver(mRequestQuitReceiver);
            unregisterReceiver(inviteMeetingBroadcastReceiver);
        } catch (Exception ex) {
            CustomLog.e(TAG, ex.getMessage());
        }
    }


    private BroadcastReceiver mRequestQuitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RUQUEST_QUIT_MEETING_BROADCAST)) {
                CustomLog.i(TAG, "收到退出会议请求");
                exitMeeting(MeetingEvent.QUIT_MEETING_INTERFACE);
            }
        }


        ;
    };


    private void keepScreenOn() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }


    private void cancelKeepScreenOn() {
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }


    private boolean getUserNetConfig() {
        // return MeetingApplication.shareInstance().getWebSetting();
        return true;
    }


    private void setFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    private void cancelFullScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    private void initSensor() {
        mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // mSensorManager = (SensorManager)
        // getSystemService(Context.SENSOR_SERVICE);
        // mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }


    /***
     * @param @param context
     * @param @param updown true增加音量 false减小音量
     * @return void 返回类型
     * @throws
     * @Title: setCallVolume
     * @Description: 设置通话的音量
     */
    private void setCallVolume(Context context, boolean updown) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (updown) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            } else {
                audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }

            int volSize = audioManager
                    .getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            int adjustValue = volSize;
            int max = audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            CustomLog.d(TAG, "max = " + max);
            CustomLog.d(TAG, "volSize = " + volSize);

            // if (updown) {
            // adjustValue = volSize + 1;
            // if (adjustValue >= max) {
            // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, max,
            // AudioManager.STREAM_VOICE_CALL);
            // } else {
            // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
            // adjustValue, AudioManager.STREAM_VOICE_CALL);
            // }
            // } else {
            // adjustValue = volSize - 1;
            // if (adjustValue <= 0) {
            // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0,
            // AudioManager.STREAM_VOICE_CALL);
            // } else {
            // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
            // adjustValue, AudioManager.STREAM_VOICE_CALL);
            // }
            // }
        }
    }


    private void useNormalMode() {
        if (mAudioManager != null) {
            CustomLog.d(TAG, "正常模式");
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(true);
        }
    }


    private void useInCallMode() {
        if (mAudioManager != null) {
            CustomLog.d(TAG, "听筒模式");
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            mAudioManager.setSpeakerphoneOn(false);
        }
    }

    // private void registerSensorManagerListener() {
    // if (mSensorManager != null) {
    // try {
    // mSensorManager.registerListener(this, mSensor,
    // SensorManager.SENSOR_DELAY_NORMAL);
    // } catch (Exception e) {
    // CustomLog.e(TAG, "注册距离感应失败");
    // }
    // } else {
    // CustomLog.e(TAG, "mSensorManager == null");
    // }
    // }
    //
    // private void unRegisterSensorManagerListener() {
    // if (mSensorManager != null) {
    // try {
    // mSensorManager.unregisterListener(this);
    // } catch (Exception e) {
    // }
    // }
    // }


    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mShowingDialog = ShowingDialog.NO_DIALOG;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        CustomLog.e(TAG, "onConfigurationChanged");
		/*
		 * if (mMediaView != null) { CustomLog.d(TAG,
		 * "onConfigurationChanged refresh mediaview"); mMediaView
		 * .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		 * mMediaView.requestLayout(); mMediaView.refreshDrawableState();
		 * mMediaView.forceLayout(); mMediaView.invalidate();
		 * mMediaView.refreshViewSize(); }
		 */
        super.onConfigurationChanged(newConfig);
    }

    // @Override
    // public void onSensorChanged(SensorEvent event) {
    // float range = event.values[0];
    // if (range == mSensor.getMaximumRange() && !mIsHeadSetPlugged) {
    // useNormalMode();
    // } else {
    // useInCallMode();
    // }
    // }

    // @Override
    // public void onAccuracyChanged(Sensor sensor, int accuracy) {
    //
    // }


    private void showFailToast() {
        CustomToast.show(this, "网络异常，正在重新连接中，请稍候", CustomToast.LENGTH_LONG);
    }


    private void releaseWorkThread() {
        mInvitePersonList.release();
        mParticipantorList.release();
    }


    public void showPopWindow(View parent) {

        View contentView = LayoutInflater.from(this).inflate(
                MResource.getIdByName(MeetingRoomActivity.this,
                        MResource.LAYOUT, "camera_popwindow"), null);
        // 设置按钮的点击事件

        // 一个自定义的布局，作为显示的内容

        change = (TextView) contentView.findViewById(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.ID, "change_camera"));

        changeLayout = (LinearLayout) contentView.findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "ll_change_camera"));

        if (MeetingManager.getInstance().getAppType()
                .equals(MeetingManager.MEETING_APP_TV)) {
            changeLayout.setVisibility(View.GONE);
        }

        changeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handleUVCCamera();
                int result = mButelOpenSDK.changeCamera();
                if(result == 0){
                    if(mMenuView != null){
                        mMenuView.hanleChageSelfPreview(mButelOpenSDK.getCurrentCameraIndex(),MeetingManager.getInstance().getVideoParameter(mButelOpenSDK.getCurrentCameraIndex()));
                    }
                }
                popupWindow.dismiss();

            }
        });
        imageCamera = (ImageView) contentView.findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "image_change_camera"));

        closeCamera = (ImageView) contentView.findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "close_camera"));
        open = (TextView) contentView.findViewById(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.ID, "operation_camera"));

        openLayout = (LinearLayout) contentView.findViewById(MResource
                .getIdByName(MeetingRoomActivity.this, MResource.ID,
                        "ll_operation_camera"));

        openLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handleCamare();
                popupWindow.dismiss();
            }
        });
        popupWindow = null;
        popupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框n bvc自行车，。
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        if (isOpenCamera) {
            imageCamera.setBackgroundResource(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.DRAWABLE,
                    "jmeetingsdk_change_camera_nl"));
            change.setTextColor(Color.parseColor("#ffffff"));
            changeLayout.setClickable(true);
            open.setText("关闭摄像头");
            closeCamera.setBackgroundResource(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.DRAWABLE,
                    "jmeetingsdk_open_camera"));
        } else {
            imageCamera.setBackgroundResource(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.DRAWABLE,
                    "jmeetingsdk_camera_main"));
            change.setTextColor(Color.parseColor("#4b4d4e"));
            changeLayout.setClickable(false);
            open.setText("开启摄像头");
            closeCamera.setBackgroundResource(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.DRAWABLE,
                    "jmeetingsdk_close_camera"));
        }

        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        popupWindow.showAtLocation(contentView, Gravity.NO_GRAVITY, location[0]
                - (int) (106 * density), location[1] - popupWindow.getHeight()
                + (int) (35 * density));
        // 设置好参数之后再show
        // popupWindow.showAsDropDown(contentView);

    }


    public void showPopWindowMode(View parent) {
        View contentView = LayoutInflater.from(this).inflate(
                MResource.getIdByName(MeetingRoomActivity.this,
                        MResource.LAYOUT, "popwindowmode"), null);
        // 设置按钮的点击事件

        // 一个自定义的布局，作为显示的内容
        imageMode = (ImageView) contentView.findViewById(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.ID, "image_change_camera"));
        changeMode = (Button) contentView.findViewById(MResource.getIdByName(
                MeetingRoomActivity.this, MResource.ID, "changemode"));
        changeMode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMenuView != null && mMenuView.getisCloseMoive()) {
                    // 切到视频模式

                    int result = mButelOpenSDK.switchVideoOrAudioMode(0,
                            mAskForswitchAudioModeListener);
                    if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        CustomLog.e(TAG, "切到视频模式");
                        MeetingRoomActivity.this.showLoadingView("请求中...");
                    } else {
                        if (result == ButelOpenSDKReturnCode.RETURN_NONEED_CALLBACK_SUCCESS) {
                            CustomToast.show(getApplicationContext(),
                                    "切换到视频模式成功", Toast.LENGTH_SHORT);
                            mMenuView.setisCloseMoive(false);
                            setPositionOnVideoMode();
                            ownSpeak oSpeak = isOwnSpeak();
                            if (oSpeak == ownSpeak.SpeakOnMic) {
                                isOpenCamera = true;
                            }
                        } else {
                            CustomToast.show(getApplicationContext(),
                                    "切换到视频模式失败", Toast.LENGTH_SHORT);
                        }
                    }
                } else {
                    // 切到语音模式

                    int result = mButelOpenSDK.switchVideoOrAudioMode(1,
                            mAskForswitchVideoModeListener);
                    if (result == ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                        CustomLog.e(TAG, "切到语音模式");
                        MeetingRoomActivity.this.showLoadingView("请求中...");

                    } else {
                        if (result == ButelOpenSDKReturnCode.RETURN_NONEED_CALLBACK_SUCCESS) {
                            CustomToast.show(getApplicationContext(),
                                    "切换到语音模式成功", Toast.LENGTH_SHORT);
                            mMenuView.setisCloseMoive(true);
                            ownSpeak oSpeak = isOwnSpeak();
                            if (oSpeak == ownSpeak.SpeakOnMic) {
                                isOpenCamera = false;
                            }
                            setPositionOnVideoMode();
                        } else {
                            CustomToast.show(getApplicationContext(),
                                    "切换到语音模式失败", Toast.LENGTH_SHORT);
                        }
                    }
                }
                popupWindowMode.dismiss();
            }
        });
        popupWindowMode = null;
        popupWindowMode = new PopupWindow(contentView,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

        popupWindowMode.setTouchable(true);

        popupWindowMode.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindowMode.setBackgroundDrawable(new BitmapDrawable());
        if (mMenuView.getisCloseMoive()) {
            imageMode.setBackgroundResource(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.DRAWABLE,
                    "jmeetingsdk_change_video_mode_icon"));
            changeMode.setText("开启视频");

        } else {
            imageMode.setBackgroundResource(MResource.getIdByName(
                    MeetingRoomActivity.this, MResource.DRAWABLE,
                    "jmeetingsdk_change_audio_mode_icon"));
            changeMode.setText("关闭视频");
        }

        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        popupWindowMode.showAtLocation(contentView, Gravity.NO_GRAVITY,
                location[0] - (int) (90 * density), location[1]
                        - popupWindowMode.getHeight() + (int) (35 * density));
        // 设置好参数之后再show
        // popupWindow.showAsDropDown(contentView);

    }


    private void sendMeetingOperationBroadCast(int code) {
        CustomLog.d(TAG, "发送会议操作广播事件" + code);
        Intent intent = new Intent();
        intent.setPackage(MeetingManager.getInstance().getRootDirectory());
        intent.setAction(OPERATION_MEETING_BROADCAST);
        intent.putExtra("eventCode", code);
        sendBroadcast(intent);

        CustomLog.d(TAG, "发送会议操作广播事件结束");
    }


    private void setMeetingSharedPreferences(String meetingId) {
        CustomLog.d(TAG, "写入meetingid: " + meetingId);
        SharedPreferences sharedPreferences = getSharedPreferences("meetingId",
                Context.MODE_PRIVATE); // 私有数据
        ;
        if (sharedPreferences.getString("meetingId", "").equals(meetingId)) {
            CustomLog.d(TAG, "写入meetingid: 不做修改");
            return;
        }
        Editor editor = sharedPreferences.edit();// 获取编辑器
        editor.putString("meetingId", meetingId);
        // editor.putInt("age", 4);
        editor.commit();// 提交修改
    }


    private void getMeetingInvitationSMS() {
        if (null == shareString) {
            GetMeetingInvitationSMS ss = new GetMeetingInvitationSMS() {
                @Override
                protected void onSuccess(
                        MeetingInvitationSMSInfo responseContent) {
                    super.onSuccess(responseContent);
                    CustomLog.i(TAG, " 分享信息成功返回 responseContent == " + responseContent);
                    removeLoadingView();
                    shareString = responseContent.invitationSMS;
                    shareLiveByWx(shareString);
                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    removeLoadingView();
                    CustomLog.i(TAG, "getMeetingInvitationSMS statusCode : " + statusCode);
                    // if (HttpErrorCode
                    //     .checkNetworkError(statusCode)) {
                    //     CustomToast.show(ReserveSuccessActivity.this,
                    //         "网络不给力，请重试！", Toast.LENGTH_LONG);
                    //     return;
                    // } else {
                    //     CustomToast.show(ReserveSuccessActivity.this,
                    //         "获取分享内容失败！", Toast.LENGTH_LONG);
                    // }
                }
            };

            if (mMeetingInfo != null) {
                int ret;
                String app;
                if (MeetingManager.getInstance().getAppType()
                        .equals(MeetingManager.MEETING_APP_TV)) {
                    app = MeetingManager.MEETING_APP_BUTEL_CONSULTATION;
                } else {
                    app = MeetingManager.getInstance().getAppType();
                }
                ret = ss.getMeetingInvitationSMS(app, "", mMeetingInfo.meetingId,
                        mMeetingInfo.accountId, mMeetingInfo.accountName);
                if (ret == 0) {
                    showLoadingView("正在获取分享信息");
                } else {
                    CustomToast.show(MeetingRoomActivity.this,
                            "获取分享内容失败！", Toast.LENGTH_LONG);
                    CustomLog.i(TAG, "getMeetingInvitationSMS ret : " + ret);
                }
            }
        }
    }


    //微信分享
    private boolean isWeixinAvilible() {
        final PackageManager packageManager = this.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        CustomToast.show(MeetingRoomActivity.this, "目前您的微信版本过低或未安装微信，需要安装微信才能使用",
                CustomToast.LENGTH_LONG);
        return false;
    }


    public interface FloatViewParamsListener {

        public int getTitleHeight();

        public WindowManager.LayoutParams getLayoutParams();

        public void setIsShowWindow(boolean isshow);

        public void hideActivity();

        public void hideTitleBar(boolean hide);

        public VideoParameter getSelftVideoParameter();

    }


    private void addFloatViewParamsListener(FloatViewParamsListener listener) {
        if (mListener == null) {
            mListener = listener;
        }
    }


    private void removeFloatingViewParamsListener() {
        mListener = null;
    }

    private void bluetooth(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
// TODO Auto-generated method stub
                String action = intent.getAction();
                CustomLog.d(TAG,"蓝牙" + "action: " + action );

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                            == BluetoothAdapter.STATE_OFF){
                        mIsbluetooth = false;
                        if (audioManager.isWiredHeadsetOn()) {
                            CustomLog.d(TAG, "蓝牙" + "BluetoothAdapter.STATE_OFF  关闭蓝牙的开关   使用有线耳机输出");
                            chooseheadset();
                        } else {
                            CustomLog.d(TAG, "蓝牙" + "BluetoothAdapter.STATE_OFF  关闭蓝牙的开关  使用外放输出");
                            chooseSpeaker();
                        }
                    }
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {		//蓝牙连接状态
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                    if (state == BluetoothAdapter.STATE_CONNECTED ) {
                        //连接或失联，切换音频输出（到蓝牙、或者强制仍然扬声器外放）
                        CustomLog.d(TAG, "蓝牙" + "BluetoothAdapter.STATE_CONNECTED   蓝牙连接上");
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                chooseBluetooth();
                            }
                        }, 1000);
                    }else {
                        CustomLog.d(TAG, "蓝牙" + "BluetoothAdapter.STATE_DISCONNECTED   蓝牙没有连接");
                        mIsbluetooth = false;
                        if (audioManager.isWiredHeadsetOn()) {
                            CustomLog.d(TAG,"蓝牙" + "BluetoothAdapter.STATE_DISCONNECTED   蓝牙没有连接  使用有线耳机输出" );
                            chooseheadset();
                        } else {
                            CustomLog.d(TAG,"蓝牙" + "BluetoothAdapter.STATE_DISCONNECTED   蓝牙没有连接  使用外放通道输出" );
                            chooseSpeaker();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, intentFilter);


    }
    private void chooseSpeaker()
    {
        CustomLog.d(TAG, "chooseSpeaker   外放模式");
        mIsbluetooth = false;
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(true);
    }

    private void chooseBluetooth()
    {
        CustomLog.d(TAG, "chooseBluetooth   蓝牙模式");
        mIsbluetooth = true;
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.startBluetoothSco();
        audioManager.setBluetoothScoOn(true);
        audioManager.setSpeakerphoneOn(false);
    }
    private void chooseheadset() {
        CustomLog.d(TAG, "chooseheadset  有线耳机模式");
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(false);
    }

    private void isBluetoothConnectState(){
        CustomLog.d(TAG,"MeetingRoomActivity::isBluetoothConnectState()");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            CustomLog.d(TAG,"MeetingRoomActivity state" + state);
            if(state == BluetoothAdapter.STATE_CONNECTED){
                CustomLog.i(TAG,"BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                CustomLog.i(TAG,"devices:"+devices.size());

                for(BluetoothDevice device : devices){
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if(isConnected){
                        CustomLog.i(TAG,"connected:"+device.getName());
                        new Handler().postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                chooseBluetooth();
                            }
                        }, 1000);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void handleUVCCamera(){
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(MeetingRoomActivity.this, R.xml.device_filter);
        mButelOpenSDK.setUseUVCCamera(filter);
    }

    private boolean getFloatingViewShowType() {
        SharedPreferences setting = getApplicationContext().getSharedPreferences("floatingViewShowType", Context.MODE_MULTI_PROCESS);
        return setting.getBoolean("type",false);
    }

    private void getOrientation() {
        mScreenOrientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {

                // i的范围是0～359
                // 屏幕左边在顶部的时候 i = 90;
                // 屏幕顶部在底部的时候 i = 180;
                // 屏幕右边在底部的时候 i = 270;
                // 正常情况默认i = 0;

                if (45 <= i && i < 135) {
                    mScreenExifOrientation = 180;
                } else if (135 <= i && i < 225) {
                    mScreenExifOrientation = 270;
                } else if (225 <= i && i < 315) {
                    mScreenExifOrientation = 0;
                } else {
                    mScreenExifOrientation = 90;
                }

                int currentOrientation = 0;

                if(getFloatingViewShowType()){

                    currentOrientation = mScreenExifOrientation % 360;

                    if(mFloatingViewOrientation!=currentOrientation){
                        mFloatingViewOrientation = currentOrientation;
                        CustomLog.d(TAG,"setRotation,FloatingView,orientation:"+String.valueOf(currentOrientation));
                        if(mButelOpenSDK!=null){
                            mButelOpenSDK.setRotation(currentOrientation);
                        }
                    }

                }else{

                    currentOrientation =mScreenExifOrientation % 360;

                    if(mNoFloatingViewOrientation!=currentOrientation){
                        mNoFloatingViewOrientation = currentOrientation;
                        CustomLog.d(TAG,"setRotation,Not FloatingView,orientation:"+String.valueOf(currentOrientation));
                        if(mButelOpenSDK!=null){
                            mButelOpenSDK.setRotation(currentOrientation);
                        }
                    }

                }



            }
        };
        mScreenOrientationEventListener.enable();

    }

}
