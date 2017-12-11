package cn.redcdn.incoming;

import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.content.LocalBroadcastManager;

import cn.redcdn.jmeetingsdk.IncomingItem;
import cn.redcdn.jmeetingsdk.JMeetingAgent;
//depot/研发六部/视频会议/5.MobileJustMeeting/src/JMeetingSDK/src/cn/redcdn/incoming/IncomingMessageManage.java#3
//depot/研发六部/视频会议/5.MobileJustMeeting/src/JMeetingSDK/src/cn/redcdn/incoming/IncomingMessageManage.java#5
//perforce-workspace/研发六部/视频会议/5.MobileJustMeeting/src/JMeetingSDK/src/cn/redcdn/incoming/IncomingMessageManage.java
import cn.redcdn.jmeetingsdk.JMeetingService;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk.MeetingRoomActivity;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.InviteMeetingInfo;

public abstract class IncomingMessageManage {
  private String tag = IncomingMessageManage.class.getName();

  public static boolean openSound = true;

//  public static final String JMEETING_START_INCOMINGCALL_INTENT = "cn.redcdn.jmeetingsdk.start.incomingactivity";
public static final String JMEETING_START_INCOMINGCALL_INTENT = "cn.redcdn.jmeetingsdk.start.incoming";

  //  public static final String INVITE_MESSAGE_BROADCAST = "cn.redcdn.jmeetingsdk.incoming.InviteMessage.comein";
public static final String INVITE_MESSAGE_BROADCAST = "cn.redcdn.incoming.InviteMessage.comein.medical";

  // 发送给meetingroom
  public static final String MEETING_INVITED_BROADCAST = "cn.redcdn.jmeetingsdk.incoming.incomingactivity.invited";

  private ArrayList<InviteMeetingInfo> inviteMeetingList = new ArrayList<InviteMeetingInfo>();


  enum MessageState {
    NONE, MEETING_NOW, INCOMING_RING
  }

  MessageState messageState = MessageState.NONE;

  enum State {
    NONE, INIT
  }

  State state = State.NONE;

  Context context;
  
 // SystemManger mSystemManger;

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      CustomLog.i(tag, "received broadcast:" + action);

      if (action.equals(INVITE_MESSAGE_BROADCAST)) {
        handleInviteMessageBroadcastFromServer(intent);
      } else if (action.equals(MeetingRoomActivity.START_MEETING_BROADCAST)) {
        handleStartMeetingBroadcastFromMeetingRoom(intent);
      } else if (action.equals(MeetingRoomActivity.END_MEETING_BROADCAST)) {
        handleEndMeetingBroadcastFromMeetingRoom(intent);
      } else if (action.equals(IncomingDialog.JOIN_MEETING_BROADCAST)) {
        handleJoinMeetingBroadcastFromIncomingActivity(intent);
      } else if (action.equals(IncomingDialog.IGNORE_MEETING_BROADCAST)) {
        handleIgnoreMeetingBroadcastFromIncomingActivity(intent);
      } else if (action.equals(IncomingDialog.TIMEOUT_MEETING_BROADCAST)){
        handleTimeoutMeetingBroadcastFromIncomingActivity(intent);
      }else {
        CustomLog.w(tag, "received other broadcast, ignore");
      }
    }
  };

  @SuppressWarnings("deprecation")
  private void acquireWakeLock() {
    // 电源管理
    PowerManager pm = (PowerManager) context
        .getSystemService(Context.POWER_SERVICE);
    WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
        | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
    wakeLock.acquire();
    wakeLock.release();
  }

  // 收到外呼邀请
  private void handleInviteMessageBroadcastFromServer(Intent intent) {
    int meetingID = intent.getIntExtra(HostAgent.MEETING_ID, 0);
    String inviterAccountID = intent
        .getStringExtra(HostAgent.INVITER_ACCOUNT_ID);
    String inviterAccountName = intent
        .getStringExtra(HostAgent.INVITER_ACCOUNT_NAME);
    String headUrl = intent.getStringExtra(HostAgent.INVITER_HEADURL);
   // acquireWakeLock();
//
//    // 停止音乐播放,获取音频服务焦点
//    mSystemManger.pauseMusic(true);

//
////    // 先解锁，再点亮屏幕
//    CustomLog.w(tag, "解锁屏幕disableKeyguard");
//    mSystemManger.disableKeyguard();
//    CustomLog.w(tag, "点亮屏幕acquireWakeLock");
//    acquireWakeLock();
    if (MessageState.NONE == messageState) {
      CustomLog.i(tag, "message state NONE, start incomingActivity");

      try {
        // 启动呼入界面
        Intent incomingIntent = new Intent();
        incomingIntent.setClass(context,IncomingDialog.class);
        incomingIntent.setPackage(MeetingManager.getInstance().getRootDirectory());
//        incomingIntent.setClassName("cn.redcdn.hvs",JMEETING_START_INCOMINGCALL_INTENT);
        incomingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        incomingIntent.putExtra(HostAgent.MEETING_ID, meetingID);
        incomingIntent.putExtra(HostAgent.INVITER_ACCOUNT_ID, inviterAccountID);
        incomingIntent.putExtra(HostAgent.INVITER_ACCOUNT_NAME,
            inviterAccountName);
        incomingIntent.putExtra(HostAgent.INVITER_HEADURL, headUrl);

        context.startActivity(incomingIntent);

        messageState = MessageState.INCOMING_RING;
        CustomLog.i(tag, "set message state to INCOMING_RING");
      } catch (ActivityNotFoundException e) {
        CustomLog.e(tag, "not find IncomingActivity activity");
      }

      startRing(meetingID, inviterAccountID, inviterAccountName);

    } else if (MessageState.INCOMING_RING == messageState) {
      CustomLog.i(tag, "message state INCOMING_RING, add invite info to list");

      InviteMeetingInfo info = new InviteMeetingInfo();
      info.setMeetingID(meetingID);
      info.setInviterAccountID(inviterAccountID);
      info.setInviterAccountName(inviterAccountName);
      info.setInviterHeadUrl(headUrl);
      inviteMeetingList.add(info);
    } else if (MessageState.MEETING_NOW == messageState) {
      CustomLog
          .i(tag,
              "message state MEETING_NOW, send invite broadcast to meetingroom activity");

      // send broadcast to meeting room activity
      InviteMeetingInfo info = new InviteMeetingInfo();
      info.setMeetingID(meetingID);
      info.setInviterAccountID(inviterAccountID);
      info.setInviterAccountName(inviterAccountName);

      ArrayList<InviteMeetingInfo> inviteInfoList = new ArrayList<InviteMeetingInfo>();
      inviteInfoList.add(info);

      Bundle bundle = new Bundle();
      bundle.putSerializable("INVITE_MESSAGE", inviteInfoList);

      Intent showMessageIntent = new Intent();
      showMessageIntent.setAction(MEETING_INVITED_BROADCAST);
      showMessageIntent.putExtras(bundle);
      context.sendBroadcast(showMessageIntent);
    } else {
      CustomLog
          .e(tag, "invalidate message state,current state:" + messageState);
    }
  }

  // 收到开始会议广播
  private void handleStartMeetingBroadcastFromMeetingRoom(Intent intent) {
    if (MessageState.NONE == messageState) {
      CustomLog.i(tag, "message state NONE, set state to MEETING_NOW");

      messageState = MessageState.MEETING_NOW;
    } else if (MessageState.MEETING_NOW == messageState) {
      CustomLog
          .w(tag,
              "message state already in MEETING_NOW, check invite list empty or not ?");

      if (inviteMeetingList.size() > 0) {
        CustomLog.i(tag, "send left invite list(" + inviteMeetingList.size()
            + ") broadcast to meetingroom activity");

        // send broadcast to meeting room activity
        Bundle bundle = new Bundle();
        bundle.putSerializable("INVITE_MESSAGE", inviteMeetingList);

        Intent showMessageIntent = new Intent();
        showMessageIntent.setAction(MEETING_INVITED_BROADCAST);
        showMessageIntent.putExtras(bundle);
        context.sendBroadcast(showMessageIntent);

        inviteMeetingList.clear();
      }
    } else {
      CustomLog.w(tag, "warning, current message state is INCOMING_RING");
    }
  }

  // 收到结束会议广播
  private void handleEndMeetingBroadcastFromMeetingRoom(Intent intent) {
    if (MessageState.MEETING_NOW == messageState) {
      CustomLog.i(tag,
          "message state MEETING_NOW, set state to NONE and clear invite list");
      messageState = MessageState.NONE;
      inviteMeetingList.clear();
    } else {
      CustomLog
          .w(tag,
              "current message state isn't MEETING_NOW, may be INCOMING_RING or NONE");
    }
  }

  // 点击加入按钮
  private void handleJoinMeetingBroadcastFromIncomingActivity(Intent intent) {
    if (MessageState.INCOMING_RING == messageState) {
      CustomLog.i(tag,
          "message state INCOMING_RING, set message state MEETING_NOW");
      messageState = MessageState.MEETING_NOW;
    } else {
      CustomLog
          .w(tag,
              "current message state isn't INCOMING_RING, maybe MEETING_NOW or NONE");
    }
    onJoinMeetingBtnClicked(intent.getIntExtra(HostAgent.MEETING_ID, 0),
        intent.getStringExtra(HostAgent.INVITER_ACCOUNT_NAME),
        intent.getStringExtra(HostAgent.INVITER_ACCOUNT_NAME));
    stopRing(JMeetingAgent.END_PHONE_RING_AS_JOIN_MEETING);
  }

  // 点击忽略按钮
  private void handleIgnoreMeetingBroadcastFromIncomingActivity(Intent intent) {
    if (MessageState.INCOMING_RING != messageState) {
      CustomLog
          .w(tag,
              "current message state isn't INCOMING_RING, maybe MEETING_NOW or NONE, ignore");
      return;
    }

    stopRing(JMeetingAgent.END_PHONE_RING_AS_IGNORE);

    if (inviteMeetingList.size() > 0) {
      CustomLog.i(tag, "invite list size:" + inviteMeetingList.size()
          + ", and start incomingActivity");

      try {
        // 启动呼入界面
        InviteMeetingInfo info = inviteMeetingList.remove(0);
        Intent incomingIntent = new Intent();
        incomingIntent.setClass(context,IncomingDialog.class);
        incomingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        incomingIntent.putExtra(HostAgent.MEETING_ID, info.getMeetingID());
        incomingIntent.putExtra(HostAgent.INVITER_ACCOUNT_ID,
            info.getInviterAccountID());
        incomingIntent.putExtra(HostAgent.INVITER_ACCOUNT_NAME,
            info.getInviterAccountName());
        incomingIntent.putExtra(HostAgent.INVITER_HEADURL,
            info.getInviterAccountName());
        incomingIntent.putExtra("openSound", openSound);
        context.startActivity(incomingIntent);
        startRing(info.getMeetingID(), info.getInviterAccountID(),
            info.getInviterAccountName());
      } catch (ActivityNotFoundException e) {
        CustomLog.e(tag, "not find IncomingActivity activity");
      }

    } else {
      CustomLog.i(tag, "inviteMeetingList is empty,set message state to NONE");
      messageState = MessageState.NONE;
    }
  }
  
  //振铃超时
  private void handleTimeoutMeetingBroadcastFromIncomingActivity(Intent intent){
	  if (MessageState.INCOMING_RING != messageState) {
	      CustomLog
	          .w(tag,
	              "current message state isn't INCOMING_RING, maybe MEETING_NOW or NONE, ignore");
	      return;
	    }

	    stopRing(JMeetingAgent.END_PHONE_RING_AS_TIMEOUT);
	    if (inviteMeetingList.size() > 0) {
	        CustomLog.i(tag, "invite list size:" + inviteMeetingList.size()
	            + ", and start incomingActivity");

	        try {
	          // 启动呼入界面
	          InviteMeetingInfo info = inviteMeetingList.remove(0);
	          Intent incomingIntent = new Intent();
              incomingIntent.setClass(context,IncomingDialog.class);
	          incomingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	          incomingIntent.putExtra(HostAgent.MEETING_ID, info.getMeetingID());
	          incomingIntent.putExtra(HostAgent.INVITER_ACCOUNT_ID,
	              info.getInviterAccountID());
	          incomingIntent.putExtra(HostAgent.INVITER_ACCOUNT_NAME,
	              info.getInviterAccountName());
	          incomingIntent.putExtra(HostAgent.INVITER_HEADURL,
	              info.getInviterAccountName());
	          incomingIntent.putExtra("openSound", openSound);
	          context.startActivity(incomingIntent);
	          startRing(info.getMeetingID(), info.getInviterAccountID(),
	              info.getInviterAccountName());
	        } catch (ActivityNotFoundException e) {
	          CustomLog.e(tag, "not find IncomingActivity activity");
	        }

	      } else {
	        CustomLog.i(tag, "inviteMeetingList is empty,set message state to NONE");
	        messageState = MessageState.NONE;
	      }
  }

  public int init(Context appContext) {
    CustomLog.i(tag, "init IncomingMessageManage");

    if (State.NONE != state) {
      CustomLog.e(tag, "IncomingMessageManage already init");
      return -1;
    }

    context = appContext;
    IntentFilter filter = new IntentFilter();
    filter.addAction(INVITE_MESSAGE_BROADCAST);
    filter.addAction(MeetingRoomActivity.START_MEETING_BROADCAST);
    filter.addAction(MeetingRoomActivity.END_MEETING_BROADCAST);
    filter.addAction(IncomingDialog.JOIN_MEETING_BROADCAST);
    filter.addAction(IncomingDialog.IGNORE_MEETING_BROADCAST);
    filter.addAction(IncomingDialog.TIMEOUT_MEETING_BROADCAST);
    context.registerReceiver(receiver, filter);

   // mSystemManger=new SystemManger(appContext);
    state = State.INIT;
    return 0;
  }

  public void setMeetingState(boolean state) {
    // 暂时不使用外部设置 meetingState，采取和手机版统一的方式，通过会议室内广播自己更新状态
  }

  public int inComingCall(String inviterId, String inviterName, int MeetingId,
      String headUrl) {
    CustomLog.i(tag, "inComingCall: meetingId " + MeetingId + " | inviterID: "
        + inviterId + " | inviterName: " + inviterId + " | headUrl: " + headUrl);
    if (State.NONE == state) {
      CustomLog.e(tag, "IncomingMessageManage is not init");
      return -1;
    }

    Intent startIncomingActivity = new Intent(INVITE_MESSAGE_BROADCAST);
    startIncomingActivity.setPackage(MeetingManager.getInstance().getRootDirectory());
    startIncomingActivity.putExtra(HostAgent.MEETING_ID, MeetingId);
    startIncomingActivity.putExtra(HostAgent.INVITER_ACCOUNT_ID, inviterId);
    startIncomingActivity.putExtra(HostAgent.INVITER_ACCOUNT_NAME, inviterName);
    startIncomingActivity.putExtra(HostAgent.INVITER_HEADURL, headUrl);
    context.sendBroadcast(startIncomingActivity);

    return 0;
  }

  private void startRing(int meetingId, String inviterID, String inviterName) {
    CustomLog.i(tag, "startRing: meetingId " + meetingId + " | inviterID: "
        + inviterID + " | inviterName: " + inviterName);
    IncomingItem item = new IncomingItem();
    item.inviterID = inviterID;
    item.inviterName = inviterName;
    item.meetingID = String.valueOf(meetingId);
    onEvent(JMeetingAgent.PHONE_RING, item);
  }

  private void stopRing(int valueCode) {
    onEvent(valueCode, JMeetingAgent.eventCodeDes(valueCode));
  }

  abstract protected void onJoinMeetingBtnClicked(int meetingID,
      String inviterID, String inviterName);

  abstract protected void onEvent(int eventCode, Object eventContent);

  public void release() {
    CustomLog.i(tag, "release IncomingMessageManage");

    if (State.NONE == state) {
      return;
    }

    try {
      context.unregisterReceiver(receiver);
    } catch (Exception ex) {

    }

    context = null;

    state = State.NONE;
    messageState = MessageState.NONE;

    inviteMeetingList.clear();

  }
}
