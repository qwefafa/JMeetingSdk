package cn.redcdn.menuview.vo;

import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;

public class Speaker {
	private String accountId;// 视讯号
	private String accountName;// 名称
	private int screenShareStatus = SpeakerInfo.SCREEN_NORMAL;// 屏幕分享状态，1屏幕分享者，2普通发言者
	private int mDocVideoStatus = SpeakerInfo.DOC_VIDEO_OFF;
	private int mVideoStatus = SpeakerInfo.VIDEO_OPEN; // 视频画面状态
	private int mCamStatus = SpeakerInfo.CAM_STATUS_OPEN;
	private int[] videoResolution;
	private int[] docVideoResolution;
	private int speakType;//0普通 1分享

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public int getmVideoStatus() {
		return mVideoStatus;
	}

	public void setmVideoStatus(int mVideoStatus) {
		this.mVideoStatus = mVideoStatus;
	}

	public int getScreenShareStatus() {
		return screenShareStatus;
	}

	public void setScreenShareStatus(int screenShareStatus) {
		this.screenShareStatus = screenShareStatus;
	}

	public int[] getVideoResolution() {
		return videoResolution;
	}

	public void setVideoResolution(int[] videoResolution) {
		this.videoResolution = videoResolution;
	}

	public int getmDocVideoStatus() {
		return mDocVideoStatus;
	}

	public void setmDocVideoStatus(int mDocVideoStatus) {
		this.mDocVideoStatus = mDocVideoStatus;
	}

	public int[] getDocVideoResolution() {
		return docVideoResolution;
	}

	public void setDocVideoResolution(int[] docVideoResolution) {
		this.docVideoResolution = docVideoResolution;
	}

	public int getmCamStatus() {
		return mCamStatus;
	}

	public void setmCamStatus(int mCamStatus) {
		this.mCamStatus = mCamStatus;
	}

	public int getSpeakType() {
		return speakType;
	}

	public void setSpeakType(int speakType) {
		this.speakType = speakType;
	}

	@Override
	public String toString() {
		String s="Speaker "+"accountId," + accountId + " accountName,"
				+ accountName + " screenShareStatus," + screenShareStatus
				+ " mDocVideoStatus," + mDocVideoStatus+ " mVideoStatus," + mVideoStatus;
		return s;
	}

}
