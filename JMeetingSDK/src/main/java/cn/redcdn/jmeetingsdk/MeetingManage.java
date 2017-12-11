package cn.redcdn.jmeetingsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.InviteeItem;

public abstract class MeetingManage {
	private Context context;
	private int meetingID;
	private boolean isMeeting = false;
	private boolean isAllowMobileNet = false;
	private final String TAG = getClass().getSimpleName().toString();
	private final String JMEETING_BROADCAST_STARTMEETING = "cn.redcdn.jmeetingsdk.meetingroom.startmeeting";
	private final String JMEETING_BROADCAST_ENDMEETING = "cn.redcdn.jmeetingsdk.meetingroom.endmeeting";
	private boolean isInit=false;
	

	public void init(Context context) {
		meetingID = 0;
		this.context = context;
		isAllowMobileNet = false;
		// TODO Auto-generated method stub
		registerBroadcast();
		isInit=true;
	}

	private BroadcastReceiver MeetingManageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CustomLog.i(TAG, "MeetingManageonReceive " + intent.getAction());
			String action = intent.getAction();
			if (action.equals(JMEETING_BROADCAST_STARTMEETING)) {
				// int meetingId=intent.getExtras().getInt("meetingId");
				isMeeting = true;
				onEvent(JMeetingAgent.JOIN_MEETING, String.valueOf(meetingID));

			} else if (action.equals(JMEETING_BROADCAST_ENDMEETING)) {
				isMeeting = false;
				int code = intent.getExtras().getInt("eventCode");
				onEvent(code, JMeetingAgent.eventCodeDes(code));
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
				onEvent(JMeetingAgent.MEETING_INVITE, item);

			} else if (action
					.equals(MeetingRoomActivity.OPERATION_MEETING_BROADCAST)) {
				// int meetingId=intent.getExtras().getInt("meetingId");
				int code = intent.getExtras().getInt("eventCode");
			
				onEvent(code, JMeetingAgent.eventCodeDes(code));
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
	
	public boolean getMeetingState(){
		return isMeeting;
	}

	public int joinMeeting(String token, String accountID, String accountName,
			int meetingID) {
		// CustomLog.v(TAG, "joinMeeting");
		CustomLog.i(TAG, "joinMeeting " + "token:" + token + " accountID"
				+ accountID + " accountName" + accountName + " meetingID"
				+ meetingID+"pid ");
		Intent meetingItent = new Intent(context, MeetingRoomActivity.class);
		meetingItent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		meetingItent.putExtra("accountId", accountID);
		meetingItent.putExtra("accountName", accountName);
		meetingItent.putExtra("meetingId", meetingID);
		meetingItent.putExtra("token", token);
		meetingItent.putExtra("isAllowMobileNet", isAllowMobileNet);
		this.meetingID = meetingID;
		context.startActivity(meetingItent);

		// onEvent(JMeetingAgent.JOIN_MEETING, null);
		return 0;

	}

	public int quitMeeting() {
		CustomLog.i(TAG, "quitMeeting ");
		CustomLog.i(TAG, "检查是否正在会议中 isMeeting " + isMeeting);
		Intent intent = new Intent(
				MeetingRoomActivity.RUQUEST_QUIT_MEETING_BROADCAST);
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

	public void release() {
		CustomLog.i(TAG, "release ");
		if(!isInit){
			return;
		}
		quitMeeting();
		isMeeting = false;
		meetingID = 0;
		isInit=false;
		context.unregisterReceiver(MeetingManageReceiver);
	}

	abstract protected void onEvent(int eventCode, Object eventContent);
}
