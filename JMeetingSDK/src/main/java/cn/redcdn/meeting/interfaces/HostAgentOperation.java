package cn.redcdn.meeting.interfaces;

import java.util.List;

public interface HostAgentOperation {
  public static final String MEETING_INVITED_BROADCAST = "cn.redcdn.jmeetingsdk.incoming.incomingactivity.invited";

//TODO 添加  invite 接口
  /**
   * 邀请参会
   * 
   * @param inviteListID
   *          被邀请人列表
   * @param meetingID
   *          会议ID
   * @param inviterAccountID
   *          邀请者视讯号
   * @param inviterAccountName
   *          邀请者参会名称
   */
  public int invite(List<String> inviteListID, int meetingID,
                    String inviterAccountID, String inviterAccountName);
//TODO 添加 设置 HostAgent端口接口
  public int shareAccountInfo(String localActivityIp, int localActivityPort,
                              String accountID, String accountName);
}
