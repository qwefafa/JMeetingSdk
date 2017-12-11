package cn.redcdn.menuview;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.CurrentRoleStatus;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.MeetingStyleStatus;
import cn.redcdn.butelopensdk.constconfig.EpisodeData;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.VideoParameter;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.imservice.IMMessageBean;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk.MeetingRoomActivity;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.InvitePersonList;
import cn.redcdn.meeting.data.MeetingEvent;
import cn.redcdn.meeting.data.ParticipantorList;
import cn.redcdn.menuview.view.AskForSpeakView;
import cn.redcdn.menuview.view.AskForStopSpeakView;
import cn.redcdn.menuview.view.AutoSuitInfoView;
import cn.redcdn.menuview.view.BarrageView;
import cn.redcdn.menuview.view.CameaView;
import cn.redcdn.menuview.view.CameaView.CameraType;
import cn.redcdn.menuview.view.EpisodeButtonView;
import cn.redcdn.menuview.view.EpisodeReminderView;
import cn.redcdn.menuview.view.EpisodeView;
import cn.redcdn.menuview.view.ExitView;
import cn.redcdn.menuview.view.GiveMicView;
import cn.redcdn.menuview.view.InvitePersonView;
import cn.redcdn.menuview.view.MainButtonView;
import cn.redcdn.menuview.view.MainView;
import cn.redcdn.menuview.view.MasterSetUserSpeakView;
import cn.redcdn.menuview.view.MultiSpeakerView;
import cn.redcdn.menuview.view.ParticipatorsView;
import cn.redcdn.menuview.view.QosView;
import cn.redcdn.menuview.view.ShareDocReminderInfoView;
import cn.redcdn.menuview.view.ShareScreenView;
import cn.redcdn.menuview.view.SpeakerListInfoView;
import cn.redcdn.menuview.view.SwitchVideoTypeView;
import cn.redcdn.menuview.view.VideoOffAutoReminderInfoView;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

import static cn.redcdn.util.CustomToast.show;

public class MenuView extends FrameLayout {

    public interface BarrageViewListener {

        public void onMessageArrive(IMMessageBean object);

        public void onBarrageControlClick(boolean barrageIsOpen);

    }

    public BarrageViewListener mBarrageViewListener = new BarrageViewListener() {
        @Override
        public void onMessageArrive(IMMessageBean object) {
            mMainButtonView.onMessageArrive(object);
        }

        @Override
        public void onBarrageControlClick(boolean barrageIsOpen) {
            mMainButtonView.onBarrageControlClick(barrageIsOpen);
        }
    };

    public interface MultiSpeakerViewListener {
        public void update(int size, int position, boolean isShow);
    }

    public MultiSpeakerViewListener mMultiSpeakerViewListener = new MultiSpeakerViewListener() {
        @Override
        public void update(int size, int position, boolean isShow) {
            mMainButtonView.updateDotTextView(size, position, isShow);
        }

    };

    public interface MenuViewListener {
        public void viewOnClick(View view);

        public void invitePerson(Person person);

        public void invitePersonBySMS();

        public void invitePersonByWeixing();

        // public void addTxl(Users user);
        public void addTxl(MDSDetailInfo mMDSDetailInfo);

        public void giveMic(Person person);

        public void changeCamera();

        public void stopEpisode();

        public void changeNoSpeakerTip();

        public void openOrCloseCamera(boolean open);

        public void closeShareDocView(String id);

    }

    private final String TAG = MenuView.this.getClass().getName();
    private MenuViewListener mMenuViewListener;
    private Context mContext;
    private String mAccountId;
    private int mServiceType = 1;
    private boolean mIsSpeak = false;
    // private String mLeftMicId = null;
    // private String mRightMicId = null;
    private List<Person> mGiveMicList = new ArrayList<Person>();
    private ButelOpenSDK mButelOpenSDK;
    private MainView mMainView;
    private BarrageView mBarrageView;
    private ExitView mExitView;
    private ParticipatorsView mParticipatorsView;
    private InvitePersonView mInvitePersonView;
    private AskForSpeakView mAskForSpeakView;
    private AskForStopSpeakView mAskForStopSpeakView;
    private GiveMicView mGiveMicView;
    private QosView mQosView;
    private AutoSuitInfoView mAutoSuitInfoView;
    private MainButtonView mMainButtonView;
    private EpisodeButtonView mEpisodeButtonView;
    private EpisodeView mEpisodeView;
    private EpisodeReminderView mEpisodeReminderView;
    private ShareScreenView mShareScreenView;
    private CameaView mCameraView;
    private MasterSetUserSpeakView mMasterSetUserSpeakView;
    private MultiSpeakerView mMultiSpeakerView;
    private SpeakerListInfoView mSpeakerListInfoView;
    private SwitchVideoTypeView mSwitchVideoTypeView;
    private DisplayMetrics density;
    private ShareDocReminderInfoView mShareDocReminderInfoView;
    private VideoOffAutoReminderInfoView mVideoOffReminderInfoView;
    private String mToken;
    private String mGroupId = "";
    //	private SurfaceView mSurfaceView;
    private MeetingRoomActivity.FloatViewParamsListener mListener;

    public MenuView(Context context) {
        this(context, null);
    }

    public MenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, "MenuView构造");
        mContext = context;
        LayoutInflater.from(context).inflate(
                MResource.getIdByName(mContext, MResource.LAYOUT,
                        "meeting_room_menu_menu_view"), this, true);
    }

    public void init(MenuViewListener menuViewListener,
                     InvitePersonList invitePersonList,
                     ParticipantorList participantorList, String accountName,
                     String accountId, int meetingId, ButelOpenSDK butelOpenSDK,
                     DisplayMetrics density, String token, String groupId,
                     MeetingRoomActivity.FloatViewParamsListener listener) {
        this.mGroupId = groupId;
        this.mListener = listener;
        this.mToken = token;
        this.density = density;
        mMenuViewListener = menuViewListener;
        mAccountId = accountId;
        mButelOpenSDK = butelOpenSDK;
        mSwitchVideoTypeView = new SwitchVideoTypeView(mButelOpenSDK, mContext,
                this, accountId, density) {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mSwitchVideoTypeView.dismiss();
            }

            @Override
            public void onNotify(int notifyType, Object object) {
                if (notifyType == SwitchVideoTypeView.OPEN_CLOSE_CAMARE) {
                    mMenuViewListener.openOrCloseCamera((Boolean) object);
                } else if (notifyType == SwitchVideoTypeView.CLOSE_SHARE_DOC_VIEW) {
                    mMenuViewListener.closeShareDocView((String) object);
                }

            }
        };

        mShareDocReminderInfoView = new ShareDocReminderInfoView(mContext, mButelOpenSDK,
                this, accountId) {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNotify(int notifyType, Object object) {
                // TODO Auto-generated method stub

            }

        };

        mVideoOffReminderInfoView = new VideoOffAutoReminderInfoView(mContext, mButelOpenSDK,
                this, accountId) {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNotify(int notifyType, Object object) {
                // TODO Auto-generated method stub

            }
        };

        mSpeakerListInfoView = new SpeakerListInfoView(mContext, mButelOpenSDK,
                this, accountId) {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNotify(int notifyType, Object object) {
                // TODO Auto-generated method stub

            }
        };

        mMultiSpeakerView = new MultiSpeakerView(mButelOpenSDK, mContext, this,
                accountId, density, mMultiSpeakerViewListener, mListener) {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNotify(int notifyType, Object object) {
                if (notifyType == 0) {
                    mMenuViewListener.changeNoSpeakerTip();
                }
            }

        };

        mMasterSetUserSpeakView = new MasterSetUserSpeakView(mButelOpenSDK,
                mContext, this, mAccountId) {

            @Override
            public void onClick(View v) {
                //
                mMasterSetUserSpeakView.dismiss();
            }

            @Override
            public void onNotify(int notifyType, Object object) {
                // TODO Auto-generated method stub

            }

        };
        mCameraView = new CameaView(mContext, mButelOpenSDK, mAccountId) {

            @Override
            public void onNotify(int notifyType, Object object) {

            }

            @Override
            public void onClick(View v) {

                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "camera_icon")) {
                    mMenuViewListener.viewOnClick(v);

                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "change_mode_icon")) {
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "change_mic_icon")) {
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "change_audio_speaker_icon")) {
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "change_scale_icon")) {
                    if (mMultiSpeakerView != null) {
                        mMultiSpeakerView.showWinM();
                    }
                }

            }
        };
        mMainButtonView = new MainButtonView(mContext, density, mGroupId, mAccountId) {

            @Override
            public void onNotify(int notifyType, Object object) {

            }

            @Override
            public void onClick(View v) {

                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "main_view_btn")) {
                    CustomLog.e("MainButtonView", "按键点击.....");
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_MENU);
                    mMainView.show();
                    // mMasterSetUserSpeakView.show();
                } else if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "chat_btn")) {
                    CustomLog.e("chatview", "聊天按键点击.....");
                    if (!TextUtils.isEmpty(mGroupId) && mBarrageView != null) {
                        mBarrageView.show();
                        mMainButtonView.hideBarrageLayout();
                    }
                } else {
                    mMenuViewListener.viewOnClick(v);
                    // showPopWindow();
                }

            }
        };
        mEpisodeButtonView = new EpisodeButtonView(mContext) {

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "menu_episode_btn")) {
                    CustomLog.e(TAG, "插话按键点击.....");
                    mMenuViewListener.viewOnClick(v);
                    // MobclickAgent.onEvent(mContext,
                    // AnalysisConfig.CLICK_INTERPOSE);
                    // mEpisodeView.show();
                    // mEpisodeView.askForEpisode();
                }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
        };

        mEpisodeView = new EpisodeView(mContext) {

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "close_layout")) {
                    CustomLog.e(TAG, "说完了按键点击.....");
                    mMenuViewListener.viewOnClick(v);
                    // MobclickAgent.onEvent(mContext,
                    // AnalysisConfig.CLICK_INTERPOSE_END);
                    // hideEpisodeView();
                }
            }

            @Override
            public void onNotify(int notifyType, Object object) {
                if (notifyType == NotifyType.ASK_FOR_STOP_EPISODE) {
                    mMenuViewListener.stopEpisode();
                }
            }

        };
        mEpisodeReminderView = new EpisodeReminderView(mContext) {

            @Override
            public void onClick(View v) {

            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }

        };

        mShareScreenView = new ShareScreenView(mContext) {

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "share_screen_close_btn")) {
                    CustomLog.e("ShareScreenView share_screen_close_btn",
                            "按键点击.....");
                    dismiss();
                }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
        };

        if (!TextUtils.isEmpty(mGroupId)) {
            mBarrageView = new BarrageView(mContext, mGroupId, mBarrageViewListener) {
                @Override
                public void onClick(View v) {
                    if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                            "hide_view_btn")) {
                        dismiss();
                        mMainButtonView.showBarrageLayout();
                        notifyMenuHide();
                    }
                }

                @Override
                public void onNotify(int notifyType, Object object) {

                }
            };
        }

        mMainView = new MainView(mContext, accountId, meetingId) {
            @Override
            public void onNotify(int notifyType, Object object) {
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                    notifyMenuHide();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "view_btn_hide")) {
                    CustomLog.e("MainButtonView main_view_btn", "按键点击.....");
                    // dismiss();
                    notifyMenuHide();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_participator_btn")) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_PARTICIPATERS);
                    dismiss();
                    mParticipatorsView.show();
                    notifyMenuHide();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_invite_person_btn")) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_INVITE_CLICK);
                    dismiss();
                    mInvitePersonView.show();
                    notifyMenuHide();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_ask_for_speak_btn")) {
                    // TODO
                    dismiss();
                    notifyMenuHide();
                    if (mButelOpenSDK.getCurrentRole() == CurrentRoleStatus.MASTER_ROLE) {
                        mMasterSetUserSpeakView.show();
                        // MobclickAgent.onEvent(mContext,
                        // AnalysisConfig.CLICK_ASSIGN_SPEAK);
                        return;
                    }
                    if (mIsSpeak) {
                        if (mButelOpenSDK.getMeetingMode() == MeetingStyleStatus.MASTER_MODE) {
                            CustomToast.show(mContext, "主持人已指定您发言",
                                    CustomToast.LENGTH_SHORT);
                        } else {
                            mAskForStopSpeakView.show();
                            notifyMenuShow();
                        }
                    } else {
                        if (mButelOpenSDK.getMeetingMode() == MeetingStyleStatus.MASTER_MODE) {
                            v.setTag(3);
                            mMenuViewListener.viewOnClick(v);
                            // MobclickAgent.onEvent(mContext,
                            // AnalysisConfig.CLICK_RAISE_HANDS);
                        } else {
                            sendMeetingOperationBroadCast(MeetingEvent.MEETING_SPEAK);
                            if (mButelOpenSDK.getSpeakers() == null
                                    || mButelOpenSDK.getSpeakers().size() < 4) {
                                v.setTag(1);
                                mMenuViewListener.viewOnClick(v);
                                // if (mButelOpenSDK.getMic1UserId() == null) {
                                // v.setTag(1);
                                // / mMenuViewListener.viewOnClick(v);
                                // } else if (mButelOpenSDK.getMic2UserId() ==
                                // null) {
                                // v.setTag(2);
                                // mMenuViewListener.viewOnClick(v);
                            } else {
                                // Person p=new Person();
                                // p.setAccountId(mAccountId);
                                // p.setAccountName(mButelOpenSDK.getSpeakerInfoById(mAccountId).getAccountName());
                                showAskForSpeakView(null,
                                        AskForSpeakView.FROM_MENU_BTN);
                                notifyMenuShow();
                            }
                        }
                    }
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_ask_for_play_btn")) {
                    dismiss();
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_set_type_icon")) {
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_change_camera_btn")) {
                    mMenuViewListener.changeCamera();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "meeting_room_menu_main_view_share_btn")) {
                    mShareScreenView.show();
                    dismiss();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID, "meeting_room_menu_main_view_exit_btn")) {
                    dismiss();
                    notifyMenuHide();
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_main_view_meeting_info_btn")) {
                    dismiss();
                    mQosView.show();
                    notifyMenuShow();
                }
            }
        };

        mParticipatorsView = new ParticipatorsView(mContext, mButelOpenSDK, participantorList,
                accountId, token) {

            @Override
            public void onNotify(int notifyType, Object object) {
                if (notifyType == NotifyType.ADD_TO_TXL) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_ADD_CONTACTS);
                    // Users u = (Users) object;
                    MDSDetailInfo mMDSDetailInfo = (MDSDetailInfo) object;
                    //屏蔽将被邀请人添加到通讯录中
                    // mMenuViewListener.addTxl(mMDSDetailInfo);
                }
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                    notifyMenuHide();
                } else if (v.getId() == MResource.getIdByName(mContext,
                        MResource.ID,
                        "meeting_room_menu_participators_view_lock_icon")) {
                    mMenuViewListener.viewOnClick(v);
                }
            }
        };

        mInvitePersonView = new InvitePersonView(mContext, invitePersonList,
                accountId, mToken) {
            @Override
            public void onNotify(int notifyType, Object object) {
                if (notifyType == NotifyType.INVITE_PERSON) {
                    Person p = (Person) object;
                    //打断点时候进入方法
                    mMenuViewListener.invitePerson(p);
                    if (p.isInvitedFrom() == 0) {
                        sendMeetingOperationBroadCast(MeetingEvent.MEETING_INVITE_NUBE);
                    } else if (p.isInvitedFrom() == 1) {
                        sendMeetingOperationBroadCast(MeetingEvent.MEETING_INVITE_INVITELIST);
                    }
                } else if (notifyType == NotifyType.INVITE_PERSON_SMS) {
                    mMenuViewListener.invitePersonBySMS();
                } else if (notifyType == NotifyType.INVITE_PERSON_WEIXING) {
                    mMenuViewListener.invitePersonByWeixing();
                }
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                    notifyMenuHide();
                }
//				else if (v.getId() == 24682468) {
//					dismiss();
//					//mAutoSuitInfoView.show();
//					//notifyMenuShow();
//				}
            }
        };
        mAskForSpeakView = new AskForSpeakView(mContext, mButelOpenSDK, this) {

            @Override
            public void onNotify(int notifyType, Object object) {
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                }
                notifyMenuHide();
            }
        };
        mAskForStopSpeakView = new AskForStopSpeakView(mContext) {
            @Override
            public void onNotify(int notifyType, Object object) {
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                } else if (v.getId() == MResource
                        .getIdByName(mContext, MResource.ID,
                                "meeting_room_menu_ask_for_stop_speak_view_give_mic_btn")) {
                    dismiss();
                    mMenuViewListener.viewOnClick(v);
                } else if (v.getId() == MResource
                        .getIdByName(mContext, MResource.ID,
                                "meeting_room_menu_ask_for_stop_speak_view_stop_speak_btn")) {
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_STOP_SPEAK);
                    dismiss();
                    mMenuViewListener.viewOnClick(v);
                }
                notifyMenuHide();
            }
        };
        mExitView = new ExitView(mContext) {
            @Override
            public void onNotify(int notifyType, Object object) {
            }

            @Override
            public void onClick(View v) {
                dismiss();
                mMainView.show();
                notifyMenuShow();
            }
        };

        mGiveMicView = new GiveMicView(mContext) {
            @Override
            public void onNotify(int notifyType, Object object) {
                if (notifyType == NotifyType.GIVE_MIC) {
                    Person person = (Person) object;
                    sendMeetingOperationBroadCast(MeetingEvent.MEETING_GIVE_MIC);
                    mMenuViewListener.giveMic(person);
                    dismiss();
                    notifyMenuHide();
                }
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                    notifyMenuHide();
                }
            }
        };

        mQosView = new QosView(mContext) {
            @Override
            public void onNotify(int notifyType, Object object) {
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                        "hide_view_btn")) {
                    dismiss();
                    notifyMenuHide();
                }
            }
        };

        mAutoSuitInfoView = new AutoSuitInfoView(mContext) {
            @Override
            public void onNotify(int notifyType, Object object) {
            }

            @Override
            public void onClick(View v) {
                dismiss();
                notifyMenuHide();
            }
        };


        addView(mMultiSpeakerView);
        addView(mMainButtonView);
        addView(mCameraView);
        addView(mParticipatorsView);
        addView(mInvitePersonView);
        addView(mAskForStopSpeakView);
        addView(mAskForSpeakView);
        addView(mGiveMicView);
        addView(mExitView);
        addView(mQosView);
        addView(mAutoSuitInfoView);
        // addView(mEpisodeButtonView);
        addView(mEpisodeReminderView);
        addView(mShareScreenView);
        addView(mMainView);
        addView(mEpisodeView);
        addView(mMasterSetUserSpeakView);
        addView(mSpeakerListInfoView);
        addView(mSwitchVideoTypeView);
        if (!TextUtils.isEmpty(mGroupId) && mBarrageView != null) {
            addView(mBarrageView);
        }
        addView(mShareDocReminderInfoView);
        addView(mVideoOffReminderInfoView);
        setServiceType();

        mMainButtonView.showBarrageLayout();
    }

    public void hanleChageSelfPreview(int cameraInfo, VideoParameter parameter){
        CustomLog.d(TAG, "hanleChageSelfPreview ");
        mMultiSpeakerView.hanleChageSelfPreview(cameraInfo,parameter);
    }


    public void handleCloseVideo(String accountId, int type) {
        CustomLog.d(TAG, "handleCloseVideo " + accountId + " " + type);
        mMultiSpeakerView.handleCloseVideo(accountId, type);
    }

    public void handleOpenVideo(String accountId, int type) {
        CustomLog.d(TAG, "handleOpenVideo " + accountId + " " + type);
        mMultiSpeakerView.handleOpenVideo(accountId, type);
    }

    public void handleCloseMicOrCam(String accountId) {
        CustomLog.d(TAG, "handleCloseMicOrCam " + accountId);
        mMultiSpeakerView.handleCloseMicOrCam(accountId);
        mCameraView.handleIconShowType();
    }

    //public void handleCloseCam(String accountId) {
    //	CustomLog.d(TAG, "handleCloseCam " + accountId);
    //	mMultiSpeakerView.setImageIconView(accountId);
    //}

    public void handleOpenMicOrCam(String accountId) {
        CustomLog.d(TAG, "handleOpenMicOrCam " + accountId);
        mMultiSpeakerView.handleOpenMicOrCam(accountId);
        mCameraView.handleIconShowType();
    }

    public void removeShareDocView(String accountId, String name) {
        CustomLog.d(TAG, "removeShareDocView " + accountId);
        mMultiSpeakerView.removeShareDocView(accountId, name);
    }

    public void setPageType(int type, String account) {
        CustomLog.d(TAG, "setPageType " + type);
        mSwitchVideoTypeView.setShowType(type, account);
        mCameraView.setShowType(type, account);
    }

    public void setSpeakerList(List<SpeakerInfo> speakerList) {
        CustomLog.d(TAG, "setSpeakerList " + speakerList.size());
        mSwitchVideoTypeView.setSpeakerList(speakerList);
        mSpeakerListInfoView.setSpeakerList(speakerList);

    }

    public void speakerListInfoViewShow() {
        CustomLog.d(TAG, "speakerListInfoViewShow");
        mSpeakerListInfoView.show();
        mSpeakerListInfoView.setSpeakerListInfoViewShow();
    }

    public void hideSpeakerListInfoViewShow() {
        CustomLog.d(TAG, "hideSpeakerListInfoViewShow");
        mSpeakerListInfoView.dismiss();
    }

    public void shareDocReminderInfoViewShow() {
        CustomLog.d(TAG, "shareDocReminderInfoViewShow");
        mShareDocReminderInfoView.show();
    }

    public void hideShareDocReminderInfoView() {
        CustomLog.d(TAG, "hideShareDocReminderInfoView");
        mShareDocReminderInfoView.dismiss();
    }

    public void videoOffReminderInfoViewShow() {
        CustomLog.d(TAG, "videoOffReminderInfoViewShow");
        mVideoOffReminderInfoView.show();
    }

    public void hideVideoOffReminderInfoView() {
        CustomLog.d(TAG, "hideVideoOffReminderInfoView");
        mVideoOffReminderInfoView.dismiss();
    }

    public void hideMasterSetUserSpeakView() {
        CustomLog.d(TAG, "hideMasterSetUserSpeakView");
        mMasterSetUserSpeakView.dismiss();
    }

    public void showSwitchVideoTypeView() {
        CustomLog.d(TAG, "showSwitchVideoTypeView");
        mSwitchVideoTypeView.show();
    }

    public void hideSwitchVideoTypeView() {
        CustomLog.d(TAG, "hideSwitchVideoTypeView");
        mSwitchVideoTypeView.dismiss();
    }

    public void showMultiSpeakView() {
        CustomLog.d(TAG, "showMultiSpeakView");
        mMultiSpeakerView.show();
    }

    public void hideMultiSpeakView() {
        CustomLog.d(TAG, "hideMultiSpeakView");
        mMultiSpeakerView.dismiss();
    }

    public void showEpisodeButtonView() {
        CustomLog.d(TAG, "showEpisodeButtonView");
        mEpisodeButtonView.show();
        notifyMenuShow();
    }

    public void showEpisodeView() {
        CustomLog.d(TAG, "showEpisodeView");
        if (mEpisodeView != null) {
            mEpisodeView.show();
            // notifyMenuShow();
            mEpisodeView.statRecord();
        }
    }

    public void hideEpisodeView() {
        CustomLog.d(TAG, "hideEpisodeView");
        if (mEpisodeView != null) {
            mEpisodeView.stopRecord();
            mEpisodeView.dismiss();
            // notifyMenuHide();
        }
    }

    public void showEpisodeReminderView() {
        CustomLog.d(TAG, "showEpisodeReminderView");
        if (mEpisodeReminderView != null) {
            mEpisodeReminderView.show();
            // notifyMenuShow();

        }
    }

    public void refreshEpisodeReminderView(List<EpisodeData> list) {
        if (mEpisodeReminderView != null) {
            mEpisodeReminderView.setInfoList(list);
        }
    }

    public void hideEpisodeReminderView() {
        CustomLog.d(TAG, "hideEpisodeReminderView");
        if (mEpisodeReminderView != null) {
            mEpisodeReminderView.dismiss();
            // notifyMenuHide();
        }
    }

    public void showMainView() {
        CustomLog.d(TAG, "showMainView");
        mMainView.show();
        notifyMenuShow();
    }

    public void showBarrageView() {
        CustomLog.d(TAG, "showBarrageView");
        if (!TextUtils.isEmpty(mGroupId) && mBarrageView != null) {
            mBarrageView.show();
        }
        notifyMenuShow();
    }

    public void showMainButtonView() {
        CustomLog.d(TAG, "showMainButtonView");
        mMainButtonView.show();
        notifyMenuShow();
    }

    public void showAskForSpeakView(Person person, int fromWhere) {
        CustomLog.d(TAG, "showAskForSpeakView");
        /*
		 * if(mButelOpenSDK.getMic1UserId()!=null){
		 * //mAskForSpeakView.speakerOnLine(1, mButelOpenSDK.getMic1UserName());
		 * } if(mButelOpenSDK.getMic2UserId()!=null){
		 * //mAskForSpeakView.speakerOnLine(2, mButelOpenSDK.getMic2UserName());
		 * }
		 */
        mAskForSpeakView.setAskForSpeakViewShow(fromWhere);
        mAskForSpeakView.setPerson(person);
        mAskForSpeakView.show();
        notifyMenuShow();
    }

    public void showCameraView(CameraType t, float merix) {
        CustomLog.d(TAG, "showCameraView");
        // mCameraView.setView(t, merix);
        mCameraView.show();
        notifyMenuShow();
    }

    public void setMicModeState(boolean canShow, int micStyle) {
        CustomLog.d(TAG, "setMicModeState:" + canShow + "," + micStyle);
        mCameraView.setMicModeState(canShow, micStyle);
        // mMultiSpeakerView.setImageIconView();
    }

    public void handleIconShowType() {
        CustomLog.d(TAG, "handleIconShowType:");
        mCameraView.handleIconShowType();
    }

    public void hideMainView() {
        CustomLog.d(TAG, "hideMainView");
        mMainView.dismiss();
        notifyMenuHide();
    }

    public void hideBarrageView() {
        CustomLog.d(TAG, "hideBarrageView");
        if (!TextUtils.isEmpty(mGroupId) && mBarrageView != null) {
            mBarrageView.dismiss();
        }
        notifyMenuHide();
    }

    public void hideCameraView() {
        CustomLog.d(TAG, "hideCameraView");
        mCameraView.dismiss();
        notifyMenuHide();
    }

    public void showOrHideQosView() {
    }

    public void speakerOnLine(String accountId, String accountName) {
        CustomLog.d(TAG, "MenuView 发言者产生 " + " accountId " + accountId
                + " accountName " + accountName);
        if (accountId.equals(mAccountId)) {
            CustomLog.d(TAG, "发言者产生并且为自身，修改为发言状态");
            mIsSpeak = true;
            mMainView.setSpeak(true, mButelOpenSDK.getCurrentRole(),
                    mButelOpenSDK.getMeetingMode());
            setEpisodeStatus(mButelOpenSDK.getCurrentRole(),
                    mButelOpenSDK.getMeetingMode(), mIsSpeak);
        }

		/*
		 * if (micId == 1) { if (mButelOpenSDK.getMic1UserId() != null &&
		 * mAccountId != null &&
		 * mButelOpenSDK.getMic1UserId().equals(mAccountId) &&
		 * !accountId.equals(mAccountId)) { CustomLog.d(TAG,
		 * "发现记录的mic1 id为自身，但实际传入id非自身，改变状态为不发言"); mIsSpeak = false;
		 * mMainView.setSpeak(false, mButelOpenSDK.getCurrentRole(),
		 * mButelOpenSDK.getMeetingMode());
		 * setEpisodeStatus(mButelOpenSDK.getCurrentRole
		 * (),mButelOpenSDK.getMeetingMode(),mIsSpeak); } // mLeftMicId =
		 * accountId; } else if (micId == 2) { if (mButelOpenSDK.getMic2UserId()
		 * != null && mAccountId != null &&
		 * mButelOpenSDK.getMic2UserId().equals(mAccountId) &&
		 * !accountId.equals(mAccountId)) { CustomLog.d(TAG,
		 * "发现记录的mic2 id为自身，但实际传入id非自身，改变状态为不发言"); mIsSpeak = false;
		 * mMainView.setSpeak(false, mButelOpenSDK.getCurrentRole(),
		 * mButelOpenSDK.getMeetingMode());
		 * setEpisodeStatus(mButelOpenSDK.getCurrentRole
		 * (),mButelOpenSDK.getMeetingMode(),mIsSpeak); } // mRightMicId =
		 * accountId; }
		 */

        // mAskForSpeakView.speakerOnLine(micId, accountName);
    }

    public void speakerOffLine(String accountId) {
        CustomLog.d(TAG, "发言者取消");
        if (accountId.equals(mAccountId)) {
            CustomLog.d(TAG, "发言者取消并且为自身，修改为未发言状态");
            mIsSpeak = false;
            mMainView.setSpeak(false, mButelOpenSDK.getCurrentRole(),
                    mButelOpenSDK.getMeetingMode());
            setEpisodeStatus(mButelOpenSDK.getCurrentRole(),
                    mButelOpenSDK.getMeetingMode(), mIsSpeak);
        }
		/*
		 * if (micId == 1 && mButelOpenSDK.getMic1UserId() != null &&
		 * mButelOpenSDK.getMic1UserId().equals(accountId)) { //mLeftMicId =
		 * null; //mAskForSpeakView.speakerOffLine(micId);
		 * setEpisodeStatus(mButelOpenSDK
		 * .getCurrentRole(),mButelOpenSDK.getMeetingMode(),mIsSpeak); } else if
		 * (micId == 2 && mButelOpenSDK.getMic2UserId() != null &&
		 * mButelOpenSDK.getMic2UserId().equals(accountId)) { //mRightMicId =
		 * null; //mAskForSpeakView.speakerOffLine(micId);
		 * setEpisodeStatus(mButelOpenSDK
		 * .getCurrentRole(),mButelOpenSDK.getMeetingMode(),mIsSpeak); }
		 */
    }

    public void setLock(int lockInfo) {
        mParticipatorsView.setLock(lockInfo);
        mMainView.setLock(lockInfo);

    }

    public void setKey(int keyInfo, String key) {
        mMainView.setKeyType(keyInfo, key);
    }

    public void setParticipatorCount(int count) {
        mMainView.setParticipatorCount(count);
    }

    public void showExitView() {
    }

    public void showGiveMicView(List<Person> participatorList) {
        mGiveMicList.clear();
        for (int i = 0; i < participatorList.size(); i++) {
            if (mButelOpenSDK.getSpeakerInfoById(participatorList.get(i)
                    .getAccountId()) != null) {
                continue;
            }
            Person p = new Person();
            p.setAccountId(participatorList.get(i).getAccountId());
            p.setAccountName(participatorList.get(i).getAccountName());

            mGiveMicList.add(p);
        }
		/*
		 * if (mButelOpenSDK.getMic1UserId() != null) { Person leftPerson = new
		 * Person(); leftPerson.setAccountId(mButelOpenSDK.getMic1UserId());
		 * mGiveMicList.remove(leftPerson); } if (mButelOpenSDK.getMic2UserId()
		 * != null) { Person rightPerson = new Person();
		 * rightPerson.setAccountId(mButelOpenSDK.getMic2UserId());
		 * mGiveMicList.remove(rightPerson); }
		 */
        if (mGiveMicList.size() == 0) {
            show(mContext, "暂无可传麦者", CustomToast.LENGTH_SHORT);
            return;
        }
        mGiveMicView.show(mGiveMicList);
        notifyMenuShow();
    }

    public void reportOperationResult(int operationResult) {
    }

    public void dismissInvitePersonView() {
        if (mInvitePersonView != null
                && mInvitePersonView.getVisibility() == View.VISIBLE) {
            mInvitePersonView.dismiss();
            notifyMenuHide();
        }
    }

    public void dismissAskForSpeakView() {
        if (mAskForSpeakView != null) {
            mAskForSpeakView.dismiss();
        }
        notifyMenuHide();
    }

    public void dismissAskForStopSpeakViewAndGiveMicView() {
        if (mAskForStopSpeakView != null) {
            mAskForStopSpeakView.dismiss();
        }
        if (mGiveMicView != null) {
            mGiveMicView.dismiss();
        }
        notifyMenuHide();
    }

    public void release() {
        if (mQosView != null) {
            mQosView.release();
        }
        if (mBarrageView != null) {
            mBarrageView.release();
        }
        mIsSpeak = false;
        // mLeftMicId = null;
        // mRightMicId = null;
        if (mMasterSetUserSpeakView != null) {
            mMasterSetUserSpeakView.release();
            mMasterSetUserSpeakView = null;
        }

        if (mMainButtonView != null) {
            mMainButtonView.release();
        }

        if (mMultiSpeakerView != null) {
            mMultiSpeakerView.release();
        }

        removeAllViews();
    }

    private void setServiceType() {
        // mServiceType =
        // AccountManager.getInstance(mContext).getAccountInfo().serviceType;
        mServiceType = 0;
        mMainView.setServiceType(mServiceType);
    }

    public static String MENU_SHOW = "cn.redcdn.menumanager.show";
    public static String MENU_HIDE = "cn.redcdn.menumanager.hide";

    private void notifyMenuShow() {
        CustomLog.d(TAG, "发送notifyMenuShow广播");

        try {
            Intent intent = new Intent();
            intent.setPackage(MeetingManager.getInstance().getRootDirectory());
            intent.setAction(MENU_SHOW);
            mContext.sendBroadcast(intent);
            CustomLog.d(TAG, "发送notifyMenuShow广播结束");
        } catch (Exception e) {
            CustomLog.e(TAG, "notifyMenuShow Exception:" + e.toString());
        }

    }

    private void notifyMenuHide() {
        CustomLog.d(TAG, "发送notifyMenuHide广播");

        try {
            Intent intent = new Intent();
            intent.setPackage(MeetingManager.getInstance().getRootDirectory());
            intent.setAction(MENU_HIDE);
            mContext.sendBroadcast(intent);
            CustomLog.d(TAG, "发送notifyMenuHide广播结束");
        } catch (Exception e) {
            CustomLog.e(TAG, "notifyMenuHide Exception:" + e.toString());
        }

    }

    public boolean getisCloseMoive() {
        return mCameraView.getisCloseMoive();
    }

    public void setisCloseMoive(boolean is) {
        mCameraView.setisCloseMoive(is);
    }

    public void setLiveTileShow(int isNeedShow) {
        mCameraView.setLiveTitleShow(isNeedShow);
    }

    public void setMeetingRole(int role) {
        CustomLog.d(TAG, "setMeetingRole role " + role);
        mMainView.setMeetingRole(role, mButelOpenSDK.getMasterName());
        mParticipatorsView.setLockEnable(role);
    }

    private boolean isSpeaking() {
        if (mButelOpenSDK.getSpeakerInfoById(mAccountId) != null) {
            return true;
        }
		/*
		 * if (mButelOpenSDK.getMic1UserId() != null &&
		 * mAccountId.equals(mButelOpenSDK.getMic1UserId())) { return true; } if
		 * (mButelOpenSDK.getMic2UserId() != null &&
		 * mAccountId.equals(mButelOpenSDK.getMic2UserId())) { return true; }
		 */
        return false;
    }

    public void setMeetingModel(int role, int meetingMode) {
        mMainView.setSpeak(isSpeaking(), mButelOpenSDK.getCurrentRole(),
                mButelOpenSDK.getMeetingMode());
        mMainView.setMeetingModel(meetingMode);
        // 参会方在主持模式下不可插花
        setEpisodeStatus(mButelOpenSDK.getCurrentRole(),
                mButelOpenSDK.getMeetingMode(), mIsSpeak);
    }

    public MultiSpeakerView getmMultiSpeakerView() {
        return mMultiSpeakerView;
    }

    private void setEpisodeStatus(int role, int meetingMode, boolean isSpeaking) {
        CustomLog.e(TAG, "setEpisodeStatus " + role + " " + meetingMode + " "
                + isSpeaking);
        if (isSpeaking) {
            mEpisodeButtonView.setEpisodeButtonBg(true);
        } else if (role == CurrentRoleStatus.COMMON_ROLE
                && meetingMode == MeetingStyleStatus.MASTER_MODE) {
            mEpisodeButtonView.setEpisodeButtonBg(true);
        } else {
            mEpisodeButtonView.setEpisodeButtonBg(false);
        }
    }

    private void sendMeetingOperationBroadCast(int code) {
        CustomLog.d(TAG, "发送会议操作广播事件");

        try {
            Intent intent = new Intent();
            intent.setPackage(MeetingManager.getInstance().getRootDirectory());
            intent.setAction(MeetingRoomActivity.OPERATION_MEETING_BROADCAST);
            intent.putExtra("eventCode", code);
            mContext.sendBroadcast(intent);
            CustomLog.d(TAG, "发送会议操作广播事件结束");
        } catch (Exception e) {
            CustomLog.e(TAG, "sendMeetingOperationBroadCast Exception:" + e.toString());
        }

    }

    public void setNetStatusView(int netStatus) {
        mMainButtonView.setNetStatusView(netStatus);
    }

//	public List<SurfaceView> getSurfaceViewList(){
//		List<SurfaceView> surfaceViewList = new ArrayList<SurfaceView>();
//		for(int i = 0; i < mMultiSpeakerView.multiViewList.size(); i ++){
//			surfaceViewList.add(mMultiSpeakerView.multiViewList.get(i).getSurfaceView());
//		}
//		return surfaceViewList;
//	}

    public void showFloatingView() {
        CustomLog.d(TAG, "showFloatingView");
        mMultiSpeakerView.showFloatingView();
    }

    public void hideFloatingView() {
        CustomLog.d(TAG, "hideFloatingView");
        mMultiSpeakerView.hideFloatingView();
    }

}
