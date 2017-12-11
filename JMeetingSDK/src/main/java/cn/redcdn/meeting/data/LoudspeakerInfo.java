package cn.redcdn.meeting.data;

import cn.redcdn.log.CustomLog;

public class LoudspeakerInfo {
	
	private final String TAG = LoudspeakerInfo.class.getName();
	
	private String accountId; 
	
	private int mLoundspeakerStatus = LOUNDSPEAKER_OPEN; //扬声器状态
	
	public final static int LOUNDSPEAKER_OPEN = 1;
	
	public final static int LOUNDSPEAKER_CLOSE = 2;
	
	  public void setAccountId(String accountId) {
		  CustomLog.d(TAG, "LoudspeakerInfo::accountId()" + accountId);
		  this.accountId = accountId;
	  }
	  
	  public String getAccountId() {
		  return accountId;
	  }
	
	  public void setLoudspeakerStatus(int loudspeakerStatus) {
		  CustomLog.d(TAG, "LoudspeakerInfo::setLoudspeakerStatus()" + loudspeakerStatus);		  
		  this.mLoundspeakerStatus = loudspeakerStatus;
	  }
	  
	  public int getLoudspeakerStatus() {
		  return mLoundspeakerStatus;
	  }
	
}
