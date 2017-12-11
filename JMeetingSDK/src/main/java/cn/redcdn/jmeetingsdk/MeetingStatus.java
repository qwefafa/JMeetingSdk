package cn.redcdn.jmeetingsdk;

import android.content.Context;
import cn.redcdn.log.CustomLog;

public class MeetingStatus {
	private final String TAG = getClass().getSimpleName().toString();
	private static MeetingStatus mInstance;
	private Context mContext;
	public String accountId = "";
	public String accountName = "";
	public int meetingId = 0;
	public String token = "";
	public int meetingInfo = 0;

	private MeetingStatus() {
		CustomLog.i(TAG, "MeetingStatus 初始化");
		// mContext = context;
	}

	/**
	 * 获取实例
	 * 
	 */
	public static MeetingStatus getInstance() {
		if (null == mInstance) {
			mInstance = new MeetingStatus();
		}

		return mInstance;
	}

	public void release() {
		accountId = "";
		accountName = "";
		meetingId = 0;
		token = "";
		meetingInfo = 0;
	}

}
