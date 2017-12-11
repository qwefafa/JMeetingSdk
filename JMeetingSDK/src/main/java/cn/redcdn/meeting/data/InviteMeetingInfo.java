package cn.redcdn.meeting.data;

import java.io.Serializable;

public class InviteMeetingInfo implements Serializable {

  private static final long serialVersionUID = -8553559854025570825L;

  private int meetingID;
  private String inviterAccountName;
  private String inviterAccountID;
  private String headUrl;
 
  public int getMeetingID() {
    return meetingID;
  }
  
  public void setMeetingID(int meetingID) {
    this.meetingID = meetingID;
  }
  
  public String getInviterAccountName() {
    return inviterAccountName;
  }
  
  public void setInviterAccountName(String inviterAccountName) {
    this.inviterAccountName = inviterAccountName;
  }
  
  public String getInviterAccountID() {
    return inviterAccountID;
  }
  
  public void setInviterAccountID(String inviterAccountID) {
    this.inviterAccountID = inviterAccountID;
  }
  
  public String getInviterHeadUrl() {
    return headUrl;
  }
  
  public void setInviterHeadUrl(String headUrl) {
    this.headUrl = headUrl;
  }
}
