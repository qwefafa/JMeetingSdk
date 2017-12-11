package cn.redcdn.menuview.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.RelativeLayout;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.util.MResource;

public abstract class ShareDocReminderInfoView extends BaseView {
	private final String TAG = "ShareDocReminderInfoView";
	private Context mContext;
	private String mAccountId;
	private ButelOpenSDK mButelOpenSDK;
	private MenuView mMenuView;
	private RelativeLayout layout;
	private SharedPreferences sharedPreferences;

	public ShareDocReminderInfoView(Context context, ButelOpenSDK butelOpenSDK,
			MenuView menuView, String accountId) {
		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"meeting_room_menu_share_doc_reminder"));
		mContext = context;
		mButelOpenSDK = butelOpenSDK;
		mMenuView = menuView;
		mAccountId = accountId;
		layout = (RelativeLayout) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "tip_layout"));
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				setSharedPreferenceInfo(true);
				ShareDocReminderInfoView.this.dismiss();
			}
		});
	}

	@Override
	public void show() {
		super.show();
		if (getSharedPreferenceInfo()) {			
			CustomLog.e(TAG, "aaaaaaaaaaaaaaaa");
			ShareDocReminderInfoView.this.setVisibility(View.GONE);
		} else if (mButelOpenSDK != null
				&& mButelOpenSDK.getSpeakerInfoById(mAccountId) != null) {			
			CustomLog.e(TAG, "bbbbbbbbbbbbbb");
			ShareDocReminderInfoView.this.setVisibility(View.VISIBLE);
			
			if(mButelOpenSDK.getSpeakerInfoById(mAccountId).getScreenShareStatus() == SpeakerInfo.SCREEN_SHARING){
								
				setSharedPreferenceScreenShare(true);
				
				setSharedPreferenceInfo(true);
				
			}else{
				
				if(!getSharedPreferenceScreenShare()){
					
				}else{
					setSharedPreferenceInfo(true);
				}
				
//				setSharedPreferenceInfo(true);
			}								
			
		}	
	}

	private boolean getSharedPreferenceInfo() {
		if (sharedPreferences == null){
			sharedPreferences = mContext.getSharedPreferences("shareDocTip",
					Context.MODE_PRIVATE);
		}
		return sharedPreferences.getBoolean("isNeedShow", false);
	}

	private void setSharedPreferenceInfo(boolean isNeedShow) {
		if (sharedPreferences == null){
			sharedPreferences = mContext.getSharedPreferences("shareDocTip",
					Context.MODE_PRIVATE);
		}
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("isNeedShow", isNeedShow);
		editor.commit();
	}
	
	private boolean getSharedPreferenceScreenShare() {
		if (sharedPreferences == null){
			sharedPreferences = mContext.getSharedPreferences("hasShownScreenShare",
					Context.MODE_PRIVATE);
		}
		return sharedPreferences.getBoolean("hasShown", false);
	}
	
	private void setSharedPreferenceScreenShare(boolean hasShown) {
		if (sharedPreferences == null){
			sharedPreferences = mContext.getSharedPreferences("hasShownScreenShare",
					Context.MODE_PRIVATE);
		}
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("hasShown", hasShown);
		editor.commit();
	}
		
}
