package cn.redcdn.meeting.data;

public class MeetingEvent {
  public static final String FORCE_OFFLINE_MESSAGE_BROADCAST = "cn.redcdn.incoming.ForceOffline";
  public final static String LOGOUT_BROADCAST = "cn.redcdn.meeting.logout";
  /** 退出会议：可视端主动调用退出接口 */
  public static final int QUIT_MEETING_INTERFACE = 1001;
  /** 退出会议：会议过程中token失效 */
  public static final int QUIT_TOKEN_DISABLED = 1002;
  /** 退出会议：会议过程中收到用户来电 */
  public static final int QUIT_PHONE_CALL = 1003;
  /** 退出会议：会议室用户点击返回按钮 */
  public static final int QUIT_MEETING_BACK = 1004;
  /** 退出会议：出现网络异常 */
  public static final int QUIT_MEETING_SERVER_DESCONNECTED = 1005;
  /** 退出会议：出现网络异常 */
  public static final int QUIT_MEETING_LOCKED = 1006;
  /** 退出会议：基础库异常 */
  public static final int QUIT_MEETING_LIBS_ERROR = 1007;
  /** 退出会议：会议已结束 */
  public static final int QUIT_MEETING_AS_MEETING_END = 1008;
  /** 退出会议：不允许使用移动网络进行会议 */
  public static final int QUIT_MEETING_NOTALLOW_USE_MOBILE_NET = 1009;
  /** 退出会议：点击短信邀请退出 */
  public static final int QUIT_MEETING_AS_MESSAGE_INVITE = 1010;
  /** 退出会议：摄像头异常 */
  public static final int QUIT_MEETING_CAMERA_FAILED=1011; 
  /** 退出会议：会议过程中出现其它异常 */
  public static final int QUIT_MEETING_OTHER_PROBLEM = 1099;
  /** 加入会议 */
  public static final int JOIN_MEETING = 1100;
  /** 加入会议号无效 */
  public static final int MEETINGID_DISABLED = 1101;
  /** 网络超时，会议号查询失败 */
  public static final int MEETINGID_NET_DISABLED = 1102;
  
  /** 会议室内发起邀请 */
  public static final int MEETING_INVITE = 1300;

  /** 操作过程中出现token 失效 */
  public static final int TOKEN_DISABLED = 1400;
  
  /** 会控操作事件*/
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
  public static final int MEETING_CHANGECAMERA=1614;
  public static final int MEETING_JOINMEETING=1615;
  
  public static String eventCodeDes(int eventCode) {
    String eventContent = null;
    switch (eventCode) {
    case QUIT_MEETING_INTERFACE:
      eventContent = "主动调用退出接口退出会议";
      break;
    case QUIT_TOKEN_DISABLED:
      eventContent = "会议过程中token失效";
      break;
    case QUIT_PHONE_CALL:
      eventContent = "会议中收到用户来电";
      break;
    case QUIT_MEETING_BACK:
      eventContent = "会议中点击返回按钮";
      break;
    case QUIT_MEETING_SERVER_DESCONNECTED:
      eventContent = "会议中出现网络异常";
      break;
    case QUIT_MEETING_LOCKED:
      eventContent = "会议已锁定";
      break;
    case QUIT_MEETING_LIBS_ERROR:
      eventContent = "基础库异常";
      break;
    case QUIT_MEETING_AS_MEETING_END:
      eventContent = "会议已结束";
      break;
    case QUIT_MEETING_NOTALLOW_USE_MOBILE_NET:
      eventContent = "不允许使用移动网络进行会议";
      break;
    case QUIT_MEETING_AS_MESSAGE_INVITE:
      eventContent = "点击短信邀请退出会议";
      break;
    case QUIT_MEETING_OTHER_PROBLEM:
      eventContent = "会议中出现其他异常";
      break;
    case TOKEN_DISABLED:
      eventContent = "操作过程中token失效";
      break;
    case MEETING_MENU:
      eventContent = "会议菜单";
      break;
    case MEETING_SPEAK:
      eventContent = "发言";
      break;
    case MEETING_STOP_SPEAK:
      eventContent = "停止发言";
      break;
    case MEETING_INVITE_CLICK:
      eventContent = "会议邀请";
      break;
    case MEETING_ADD_CONTACTS:
      eventContent = "会议添加联系人";
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
      eventContent = "会议加锁";
      break;
    case MEETING_UNLOCK:
      eventContent = "会议解锁";
      break;
    case MEETING_PARTICIPATERS:
      eventContent = "参会列表";
      break;
    case MEETING_SWITCHWINDOW:
      eventContent = "切换窗口";
      break;
    case MEETING_EXIT:
      eventContent = "退出会议";
      break;
    case MEETINGID_DISABLED:
      eventContent = "会议号无效";
      break;
    case MEETINGID_NET_DISABLED:
      eventContent = "网络差，会议号查询失败";
      break;
    case MEETING_JOINMEETING:
      eventContent="加入会议";
    }
    return eventContent;
  }
}
