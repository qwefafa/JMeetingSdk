package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.CurrentRoleStatus;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.MeetingStyleStatus;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.MResource;

public abstract class MainView extends BaseView {
  private final String TAG = MainView.this.getClass().getName();

  public static boolean isMasterRole = false;
  private Button hideViewBtn;
  // private Button moreBtn;
  private Button participatorBtn;
  private Button invitePersonBtn;
  private Button askForSpeakBtn;
  // private Button changeCameraBtn;
  private Button meetingInfoBtn;
  private Button shareMeetingBtn;
  private Button exitBtn;
  private TextView meetingRoomParticipatorCount;
  // private ImageView lockMeetingIcon;
  private LinearLayout moreViewBg;
  private LinearLayout playLayout;
  // private TextView mAccountIdTextView;
  private TextView mMeetingIdTextView;
  private RelativeLayout controlLayout;
  private TextView masterView;
  private TextView meetingTypeTextView;
  private ImageView controlBtn;
  private ImageView lockIcon;
  private ImageView keyIcon;
  private TextView keyContent;
  //private ImageView controlBtnL;
  private Button playBtn;
  private int mServiceType;
  private RelativeLayout layout;
  private LinearLayout line1;
  private LinearLayout line2;
  private LinearLayout line3;

  private Context mContext;
  // private boolean mIsLock;
  private Button viewBtnHide;
  private int masterType ;
  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      int id = v.getId();
	if (id == MResource.getIdByName(mContext, MResource. ID,"hide_view_btn" )) {
		MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"view_btn_hide" )) {
		CustomLog.e("view_btn_hide......", "按键点击.....");
		MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_participator_btn" )) {
		//     MobclickAgent.onEvent(mContext, AnalysisConfig.CLICK_PARTICIPANTS);
        MainView.this.onClick(v);
	} else if (id ==MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_invite_person_btn" )) {
		//     MobclickAgent.onEvent(mContext, AnalysisConfig.CLICK_INVITE);
        MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_ask_for_speak_btn" )) {
		MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_more_btn" )) {
		if (moreViewBg.getVisibility() == View.INVISIBLE) {
          moreViewBg.setVisibility(View.VISIBLE);
        } else {
          moreViewBg.setVisibility(View.INVISIBLE);
        }
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_change_camera_btn" )) {
		//      MobclickAgent.onEvent(mContext, AnalysisConfig.CLICK_SWITCH_CAMERA);
        MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_meeting_info_btn" )) {
		MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_share_btn" )) {
		/*
         * if (mIsLock) { MobclickAgent.onEvent(mContext,
         * AnalysisConfig.UNLOCK_MEETING); } else {
         * MobclickAgent.onEvent(mContext, AnalysisConfig.LOCK_MEETING); }
         */
        MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_exit_btn" )) {
		MainView.this.onClick(v);
	} else if (id ==MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_ask_for_play_btn" )) {
		MainView.this.onClick(v);
	} else if (id == MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_set_type_icon" )) {
		MainView.this.onClick(v);
	} else {
	}
    }
  };

  public MainView(Context context, String accountId, int meetingId) {
    // super(context, R.layout.meeting_room_menu_main_view);
    super(context,MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_test" ));
    mContext = context;
    hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"hide_view_btn" ));
    hideViewBtn.setOnClickListener(btnOnClickListener);
    viewBtnHide = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"view_btn_hide" ));
    viewBtnHide.setOnClickListener(btnOnClickListener);
    // moreBtn = (Button)
    // findViewById(R.id.meeting_room_menu_main_view_more_btn);
    // moreBtn.setOnClickListener(btnOnClickListener);
    //layout = (RelativeLayout) findViewById(R.id.main_view_layout);
    participatorBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_participator_btn" ));
    participatorBtn.setOnClickListener(btnOnClickListener);
    meetingRoomParticipatorCount = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_participatorcount" ));
    invitePersonBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_invite_person_btn" ));
    invitePersonBtn.setOnClickListener(btnOnClickListener);

    askForSpeakBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_ask_for_speak_btn" ));
    askForSpeakBtn.setOnClickListener(btnOnClickListener);

    // changeCameraBtn = (Button)
    // findViewById(R.id.meeting_room_menu_main_view_change_camera_btn);
    // changeCameraBtn.setOnClickListener(btnOnClickListener);

    meetingInfoBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_meeting_info_btn" ));
    meetingInfoBtn.setOnClickListener(btnOnClickListener);

    shareMeetingBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_share_btn" ));
    shareMeetingBtn.setOnClickListener(btnOnClickListener);

    exitBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_exit_btn" ));
    exitBtn.setOnClickListener(btnOnClickListener);
    line1 = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"main_view_linear1"));
    line2 = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"main_view_linear2"));
    line3 = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"main_view_linear3"));
    if (MeetingManager.getInstance().getmIsShare()){

    }else {
      line1.removeView(shareMeetingBtn);
      line2.removeView(meetingInfoBtn);
      line3.removeView(exitBtn);
      line1.addView(meetingInfoBtn);
      line2.addView(exitBtn);
    }
    // lockMeetingIcon = (ImageView)
    // findViewById(R.id.meeting_room_menu_main_view_lock_icon);

    moreViewBg = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_more_view_bg" ));

    // mAccountIdTextView = (TextView)
    // findViewById(R.id.meeting_room_menu_main_view_account_id);
    mMeetingIdTextView = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_meeting_id" ));

    // mAccountIdTextView.setText("视讯号：" + accountId);
    if(MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)){
      mMeetingIdTextView.setText(getContext().getString(R.string.consultation_room) + String.valueOf(meetingId));
    }else{
      mMeetingIdTextView.setText(getContext().getString(R.string.meeting_room) + String.valueOf(meetingId));
    }
    
    controlLayout = (RelativeLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_control_layout" ));
    masterView = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_master_name" ));
    meetingTypeTextView = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_type_name" ));
    controlBtn = (ImageView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_set_type_icon" ));
    //controlBtnL =(ImageView) findViewById(R.id.meeting_room_menu_main_view_set_type_icon_larger);
    playBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_ask_for_play_btn" ));
    playLayout = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_ask_for_play_layout" ));
    playBtn.setOnClickListener(btnOnClickListener);
    controlBtn.setOnClickListener(btnOnClickListener);
    lockIcon =(ImageView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_lock_icon" ));
    keyIcon =(ImageView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_key_icon" ));
    keyContent = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_main_view_key_content" ));
  }

  public void setMeetingModel(int isOpen) {
    if (isOpen == MeetingStyleStatus.MASTER_MODE) {
      controlBtn.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"jmeetingsdk_menu_main_view_master_model" ));
      meetingTypeTextView.setText(mContext.getString(R.string.host_model));
    } else {
      controlBtn.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"jmeetingsdk_menu_main_view_free_model" ));
      meetingTypeTextView.setText(mContext.getString(R.string.freedom_model));
    }
  }

  public void setMeetingRole(int isMaster, String masterName) {
    if (isMaster == CurrentRoleStatus.MASTER_ROLE) {
      //FrameLayout.LayoutParams linearParams =  (FrameLayout.LayoutParams)layout.getLayoutParams();  
      //linearParams.height = 502; 
      //linearParams.width=792;
      //layout.setLayoutParams(linearParams); 
      //controlBtnL.setVisibility(View.VISIBLE);
      
      if(MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_TV)){
    	  controlLayout.setVisibility(View.VISIBLE);
      }else{
    	  controlLayout.setVisibility(View.INVISIBLE);
      }
            
      meetingTypeTextView.setVisibility(View.GONE);
      masterView.setVisibility(View.GONE);
      playLayout.setVisibility(View.GONE);
      masterType = CurrentRoleStatus.MASTER_ROLE;
    } else {
      //FrameLayout.LayoutParams linearParams =  (FrameLayout.LayoutParams)layout.getLayoutParams();  
      //linearParams.height = 502;  
      //linearParams.width=612;
      //layout.setLayoutParams(linearParams); 
      //controlBtnL.setVisibility(View.GONE);
      controlLayout.setVisibility(View.GONE);
      meetingTypeTextView.setVisibility(View.VISIBLE);
      masterView.setVisibility(View.VISIBLE);
      playLayout.setVisibility(View.GONE);
      masterView.setText(CommonUtil.getLimitSubstring(masterName,6));
      masterType = CurrentRoleStatus.COMMON_ROLE;
    }
  }

  public void setSpeak(boolean isSpeak, int role, int Model) {
	  
	  if(role == CurrentRoleStatus.MASTER_ROLE){
		  isMasterRole = true;
	  }else{
		  isMasterRole = false;
	  }
	  
    if (role == CurrentRoleStatus.MASTER_ROLE) {
      askForSpeakBtn.setText(R.string.speak_manager);
      setDrawableTop(
          askForSpeakBtn,
          MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_main_view_set_speak_drawable_selector" ));
      return;
    }
    if (isSpeak) {
      if (Model == MeetingStyleStatus.MASTER_MODE) {
        askForSpeakBtn.setText(R.string.raise_hand);
        setDrawableTop(
            askForSpeakBtn,
            MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_main_view_hang_up_drawable_selector" ));
      } else {
        askForSpeakBtn.setText(R.string.cancel_speak);
        setDrawableTop(
            askForSpeakBtn,
            MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_main_view_ask_for_stop_speak_btn_drawable_selector" ));
      }
    } else {
      CustomLog.d(TAG, "设置为发言");
      if (Model == MeetingStyleStatus.MASTER_MODE) {
        askForSpeakBtn.setText(R.string.raise_hand);
        setDrawableTop(
            askForSpeakBtn,
            MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_main_view_hang_up_drawable_selector" ));
      } else {
        askForSpeakBtn.setText(R.string.ask_speak);
        setDrawableTop(
            askForSpeakBtn,
            MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_main_view_ask_for_speak_btn_drawable_selector" ));
      }
    }
  }

  private void setDrawableTop(Button btn, int drawable) {
    Drawable d = getResources().getDrawable(drawable);
    // / 这一步必须要做,否则不会显示.
    d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
    btn.setCompoundDrawables(null, d, null, null);
  }

  public void setParticipatorCount(int count) {
    // participatorBtn.setText(" 参会方 " + count);
    if (count > 0) {
      meetingRoomParticipatorCount.setText(count + "");
      meetingRoomParticipatorCount.setVisibility(View.VISIBLE);
    } else {
      meetingRoomParticipatorCount.setVisibility(View.INVISIBLE);
    }
  }

  public void setLock(int lockInfo) {
    if (lockInfo == 0) {
  //    MobclickAgent.onEvent(mContext, AnalysisConfig.LOCK_MEETING);
      if (mServiceType == 0) {
        invitePersonBtn.setEnabled(true);
        setDrawableTop(
            invitePersonBtn,
            MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_main_view_invite_person_btn_drawable_selector" ));
        lockIcon.setVisibility(View.GONE);
      }

    } else if (lockInfo == 1) {
 //     MobclickAgent.onEvent(mContext, AnalysisConfig.UNLOCK_MEETING);
      invitePersonBtn.setEnabled(false);
      setDrawableTop(invitePersonBtn, MResource.getIdByName(mContext, MResource.DRAWABLE,"lock_parter" ));
      lockIcon.setVisibility(View.VISIBLE);
    }
  }

  public void setKeyType(int keyType,String key){
	  if(keyType == 0){
		  //加密
		  if(masterType == CurrentRoleStatus.MASTER_ROLE){
			// 主持人
			  keyIcon.setVisibility(View.GONE);
			  keyContent.setVisibility(View.VISIBLE);
			  keyContent.setText(getContext().getString(R.string.password)+key);
		  }else{
			  keyIcon.setVisibility(View.VISIBLE);
			  keyContent.setVisibility(View.GONE); 
		  }
	  }else{
		  keyIcon.setVisibility(View.GONE);
		  keyContent.setVisibility(View.GONE); 
	  }
  }
  
  public void setServiceType(int serviceType) {
    mServiceType = serviceType;
    if (serviceType == 0) {
      // lockMeetingBtn.setEnabled(true);
      invitePersonBtn.setEnabled(true);
    } else if (serviceType == 1) {
      // lockMeetingBtn.setEnabled(false);
      invitePersonBtn.setEnabled(false);
    }
  }

  @Override
  public void dismiss() {
    if (moreViewBg != null && moreViewBg.getVisibility() == View.VISIBLE) {
      moreViewBg.setVisibility(View.INVISIBLE);
    }
    super.dismiss();
  }
}
