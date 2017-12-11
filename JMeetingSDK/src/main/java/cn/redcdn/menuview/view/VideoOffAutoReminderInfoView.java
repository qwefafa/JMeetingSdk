package cn.redcdn.menuview.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.RelativeLayout;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.util.MResource;

public abstract class VideoOffAutoReminderInfoView extends BaseView {
	private final String TAG = "VideoOffAutoReminderInfoView";
	private Context mContext;
	private String mAccountId;
	private ButelOpenSDK mButelOpenSDK;
	private MenuView mMenuView;
	private RelativeLayout layout;
	private SharedPreferences sharedPreferences;

	public VideoOffAutoReminderInfoView(Context context, ButelOpenSDK butelOpenSDK,
			MenuView menuView, String accountId) {
		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"meeting_room_menu_video_off_auto_reminder"));
		mContext = context;
		mButelOpenSDK = butelOpenSDK;
		mMenuView = menuView;
		mAccountId = accountId;
		layout = (RelativeLayout) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "video_tip_layout"));
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setSharedPreferenceInfo(true);
				VideoOffAutoReminderInfoView.this.dismiss();
			}
		});
	}

	@Override
	public void show() {
		super.show();
		if (getSharedPreferenceInfo()) {
			CustomLog.e(TAG, "VideoOffAutoReminderInfoView.this.setVisibility(View.GONE)");
			VideoOffAutoReminderInfoView.this.setVisibility(View.GONE);
		} else if (mButelOpenSDK != null
				&& mButelOpenSDK.getSpeakerInfoById(mAccountId) != null) {
			CustomLog.e(TAG, "VideoOffAutoReminderInfoView.this.setVisibility(View.VISIBLE)");
			VideoOffAutoReminderInfoView.this.setVisibility(View.VISIBLE);
			setSharedPreferenceInfo(true);
		}	
	}

	private boolean getSharedPreferenceInfo() {
		if (sharedPreferences == null){
			sharedPreferences = mContext.getSharedPreferences("videoOffAutoTip",
					Context.MODE_PRIVATE);
		}
		return sharedPreferences.getBoolean("isNeedShow", false);
	}

	private void setSharedPreferenceInfo(boolean isNeedShow) {
		if (sharedPreferences == null){
			sharedPreferences = mContext.getSharedPreferences("videoOffAutoTip",
					Context.MODE_PRIVATE);
		}
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("isNeedShow", isNeedShow);
		editor.commit();
	}
}

