package cn.redcdn.menuview.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.constconfig.MediaType;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.butelopensdk.vo.VideoParameter;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk.MeetingRoomActivity;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.rom.DefaultRomUtils;
import cn.redcdn.rom.HuaweiUtils;
import cn.redcdn.rom.MeizuUtils;
import cn.redcdn.rom.MiuiUtils;
import cn.redcdn.rom.QikuUtils;
import cn.redcdn.rom.RomUtils;
import cn.redcdn.util.CustomDialog;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

import static android.view.ViewConfiguration.getTapTimeout;

public abstract class MultiSpeakerView extends BaseView {

    private boolean DEFAULT_ROM_AND_SDK_VERSION_BELOW_MASHMALLOW = false;
    private final String TAG = "MultiSpeakerView";
    private float selfPreViewProportionX;
    private float selfPreViewProportionY;
    private FrameLayout surfaceViewLayout;
    private float mRawX = 0;
    private float mRawY = 0;
    private float mStartX = 0;
    private float mStartY = 0;
    private float mEndX = 0;
    private float mEndY = 0;
    private float moveDX = 0;
    private float moveDY = 0;
    private int zoonInViewTop = 0;
    private int zoonInVietLeft = 0;
    private int zoonInViewRight = 0;
    private int zoomInBottom = 0;
    private int x8 = (int) getResources().getDimension(R.dimen.x8);
    private MeetingRoomActivity.FloatViewParamsListener mListener;
    private long t1 = 0;
    private long t2 = 0;
    private int moveX = 0;
    private int moveY = 0;
    private WindowManager mWindowManager;
    private FrameLayout topLayout;
    private ImageView mImageView;
    private LinearLayout miniLayout;
    private LayoutParams topParams;

    private MenuView.MultiSpeakerViewListener mMultiSpeakerViewListener;
    //放大的位置标记
    private int currentPosition = 0;

    private Context mContext;

    private ButelOpenSDK mButelOpenSDK;

    private MenuView mMenuView;

    private boolean videoOffAuto = false;

    // 记录自己的视讯号
    private String mAccountId;
    private boolean isOwnSpeaking = false;

    // 页面标记

    public static final int DATA_PAGE = 0;

    public static final int NORMAL_PAGE = 8;

    //public static final int MULTI_PAGE = 2;

    public static final int SINGLE_BIG_PAGE = 6;

    // 放大状态
    public static final int ZOOM_OUT_TYPE = 3;

    public static final int NORMAL_TYPE = 4;

    //private String masterAccountId;

    private String shareDocAccountId;

    private String zoomOutAccount;

    private String zoomInAccount;

    //private int pageSize = 1;

    private int currentPage = SINGLE_BIG_PAGE;

    private int pagetype;

    private SpeakerItemView shareDocView;

    private int width;

    private int height;
    private int specialHeight;
    private ImageView spareImg;

    private DisplayMetrics density;

    private boolean isWindowManager = false;

    private boolean isMiniWindowManager = false;

    private final String shareDocZoomInOrOut = "999";

    public List<SpeakerItemView> multiViewList = new ArrayList<>();

    private LayoutParams mParams = new LayoutParams(1,
        1);
    private CustomDialog mDialog = null;
    private int defaultRomFloatWindowPermissionState;


    public MultiSpeakerView(ButelOpenSDK butelOpenSDK, Context context,
                            MenuView menuView, String accountId, DisplayMetrics density,
                            MenuView.MultiSpeakerViewListener MultiSpeakerViewListener,
                            MeetingRoomActivity.FloatViewParamsListener listener) {
        super(context, MResource.getIdByName(context, MResource.LAYOUT,
            "jmeetingsdk_multi_speaker_view"));
        mContext = context;
        mListener = listener;
        mButelOpenSDK = butelOpenSDK;
        mMenuView = menuView;
        mAccountId = accountId;
        zoomOutAccount = null;
        zoomInAccount = null;
        zoonInViewTop = x8;
        zoonInVietLeft = x8;
        zoonInViewRight = 0;
        zoomInBottom = 0;
        mMultiSpeakerViewListener = MultiSpeakerViewListener;
        mButelOpenSDK.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
        pagetype = NORMAL_TYPE;
        this.density = density;
        WindowManager wm = (WindowManager) context
            .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size.x > size.y) {
            width = size.x;
            height = size.y;
        } else {
            width = size.y;
            height = size.x;
        }
        specialHeight = (height * 9) / 16;
        spareImg = new ImageView(context);
        spareImg.setBackgroundResource(MResource.getIdByName(mContext,
            MResource.DRAWABLE, "meeting_room_wait_for_speak_bg"));
        spareImg.setLayoutParams(mParams);
        this.addView(spareImg);
    }


    private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener
        = new ButelOpenSDKNotifyListener() {
        @Override
        public void onNotify(int arg0, Object arg1) {
            Cmd respModel = null;
            switch (arg0) {
                case NotifyType.HANDLE_EXCEPTION_SUC:
                    if (mButelOpenSDK != null && mButelOpenSDK.getSpeakers() != null) {
                        for (int i = 0; i < mButelOpenSDK.getSpeakers().size(); i++) {
                            setImageIconView(mButelOpenSDK.getSpeakers().get(i).getAccountId());
                        }
                    }
                    break;
                case NotifyType.SPEAKER_ON_LINE:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "SPEAKER_ON_LINE "
                            + " respModel.getAccountId():" + respModel.getAccountId()
                            + " respModel.getUserName():" + respModel.getUserName());
                    addSpeakerView(respModel.getAccountId(), respModel.getUserName(), false);
                    break;
                case NotifyType.START_SPEAK:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "START_SPEAK " + respModel.getAccountId() + " "
                            + MeetingManager
                            .getInstance().getAccountName());
                    isOwnSpeaking = true;
                    addSpeakerView(respModel.getAccountId(), MeetingManager
                        .getInstance().getAccountName(), true);
                    if(isMiniWindowManager){
                        CustomLog.d("MultiSpeakerView","miniview to floatingview");
                        release();
                        isMiniWindowManager = false;
                        showFloatingView();
                        mListener.hideTitleBar(true);
                        isAnimatGoOn = false;
                        if (anim != null) {
                            anim.cancel();
                            anim = null;
                        }
                    }
                    break;
                case NotifyType.SPEAKER_OFF_LINE:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView", "SPEAKER_OFF_LINE "
                        + " respModel.getAccountId():" + respModel.getAccountId()
                        + " respModel.getUserName():" + respModel.getUserName());
                    removeSpeakerView(respModel.getAccountId(),
                        respModel.getUserName());
                    if (multiViewList.size() <= 0) {
                        CustomToast.show(mContext, "没有人发言了", Toast.LENGTH_LONG);
                    }

                    break;
                case NotifyType.STOP_SPEAK:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "STOP_SPEAK " + respModel.getAccountId());
                    isOwnSpeaking = false;
                    removeSpeakerView(respModel.getAccountId(), MeetingManager
                        .getInstance().getAccountName());
                    if (multiViewList.size() <= 0) {
                        CustomToast.show(mContext, "没有人发言了", Toast.LENGTH_LONG);
                    }
                    break;
                case NotifyType.START_SHARE_DOC:
                    CustomLog.d("MultiSpeakerView", "START_SHARE_DOC");
                    addShareDocView(mAccountId, MeetingManager.getInstance()
                        .getAccountName(), true);
                    handleOpenShareDoc(mAccountId);
                    break;
                case NotifyType.STOP_SHARE_DOC:
                    CustomLog.d("MultiSpeakerView", "STOP_SHARE_DOC");
                    removeShareDocView(mAccountId, MeetingManager.getInstance()
                        .getAccountName());
                    // 去掉视频窗口分享图标
                    handleCloseShareDoc(mAccountId);
                    if (multiViewList.size() <= 0) {
                        CustomToast.show(mContext, "没有人发言了", Toast.LENGTH_LONG);
                    }
                    break;
                case NotifyType.SHARE_NAME_CHANGE:
                    CustomLog.d("MultiSpeakerView", "SHARE_NAME_CHANGE");
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView", "SHARE_NAME_CHANGE cmd "
                        + respModel.toString());
                    break;
                case NotifyType.SERVER_NOTICE_START_SCREEN_SHAREING:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "SERVER_NOTICE_START_SCREEN_SHAREING"
                            + " respModel.getAccountId():" + respModel.getAccountId()
                            + " respModel.getUserName():" + respModel.getUserName());
                    addShareDocView(respModel.getAccountId(),
                        respModel.getUserName(), false);
                    handleOpenShareDoc(respModel.getAccountId());
                    break;
                case NotifyType.SERVER_NOTICE_STOP_SCREEN_SHAREING:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "SERVER_NOTICE_STOP_SCREEN_SHAREING"
                            + " respModel.getAccountId():" + respModel.getAccountId()
                            + " respModel.getUserName():" + respModel.getUserName());
                    removeShareDocView(respModel.getAccountId(),
                        respModel.getUserName());
                    // 去掉视频窗口分享图标
                    handleCloseShareDoc(respModel.getAccountId());
                    if (multiViewList.size() <= 0) {
                        CustomToast.show(mContext, "没有人发言了", Toast.LENGTH_LONG);
                    }
                    break;
                case NotifyType.SERVER_NOTICE_STREAM_PUBLISH:
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "SERVER_NOTICE_STREAM_PUBLISH getMediaType() "
                            + respModel.getMediaType());
                    SurfaceView surfaceView = null;

                    if (respModel.getMediaType() == MediaType.TYPE_DOC_VIDEO) {
                        if (shareDocView == null) {
                            return;
                        }
                        surfaceView = shareDocView.getSurfaceView();
                    } else if (respModel.getMediaType() == MediaType.TYPE_VIDEO) {
                        if (getItemView(respModel.getAccountId()) == null) {
                            return;
                        }
                        surfaceView = getItemView(respModel.getAccountId())
                            .getSurfaceView();
                    }
                    if (mAccountId != null
                        && mAccountId.equals(respModel.getAccountId())) {
                        if (surfaceView != null) {

                            //处理Mate9画面分离、位置错乱问题
                            if(isWindowManager) {
                                if (getItemView(mAccountId) != null) {
                                    surfaceViewLayout.removeView(getItemView(mAccountId));
                                    addNewViewToShow(getItemView(mAccountId));
                                }
                            }

                            mButelOpenSDK.startLocalVideo(respModel.getMediaType(),
                                surfaceView);
                        }
                    } else {
                        if (surfaceView != null) {

                            //处理Mate9画面分离、位置错乱问题
                            if(isWindowManager){
                                surfaceViewLayout.removeView(getItemView(respModel.getAccountId()));
                                addNewViewToShow(getItemView(respModel.getAccountId()));
                                if(getItemView(mAccountId)!=null){
                                    surfaceViewLayout.removeView(getItemView(mAccountId));
                                    addNewViewToShow(getItemView(mAccountId));
                                }
                            }

                            mButelOpenSDK.startRemoteVideo(
                                respModel.getAccountId(),
                                respModel.getMediaType(), surfaceView);
                        }
                    }
                    handleOpenMicOrCam(respModel.getAccountId());
                    if (respModel.getMediaType() == MediaType.TYPE_VIDEO) {
                        handleOpenVideo(respModel.getAccountId(), 0);
                        handleVideoForNormal(respModel.getAccountId(),
                            getItemView(respModel.getAccountId()));
                    }
                    break;
                // 通知有人静音了
                // 取消静音
                case NotifyType.SERVER_NOTICE_STREAM_UNPUBLISH:
                    //CustomToast.show(mContext,"SERVER_NOTICE_STREAM_UNPUBLISH",Toast.LENGTH_LONG);
                    respModel = (Cmd) arg1;
                    if (respModel == null) {
                        return;
                    }
                    CustomLog.d("MultiSpeakerView",
                        "SERVER_NOTICE_STREAM_UNPUBLISH getMediaType() "
                            + respModel.getMediaType());
                    handleCloseMicOrCam(respModel.getAccountId());
                    if (respModel.getMediaType() == MediaType.TYPE_VIDEO) {
                        handleCloseVideo(respModel.getAccountId(), 0);
                    }
                    break;
                case NotifyType.SPEAKER_VIDEO_PARAM_UPDATE:
                    CustomLog.d("MultiSpeakerView", "SPEAKER_VIDEO_PARAM_UPDATE ");
                    String id = (String) arg1;
                    if (id == null) {
                        return;
                    }
                    SpeakerItemView view = getItemView(id);
                    if (view != null) {
                        handleVideoForNormal(id, view);
                    }
                    break;
                case NotifyType.CLOSE_CAMERA:
                    CustomLog.d("MultiSpeakerView", mAccountId + " CLOSE_CAMERA ");
                    CustomToast.show(mContext, "摄像头打开失败", CustomToast.LENGTH_LONG);
                    handleCloseMicOrCam(mAccountId);
                    break;
                case NotifyType.CLOSE_MIC:
                    CustomLog.d("MultiSpeakerView", mAccountId + " CLOSE_MIC ");
                    CustomToast.show(mContext, "麦克风打开失败", CustomToast.LENGTH_LONG);
                    handleCloseMicOrCam(mAccountId);
                    break;
                default:
                    break;
            }
        }
    };


    @SuppressLint("NewApi")
    public void handleOpenVideo(String accountId, int type) {
        CustomLog.d("MultiSpeakerView", "handleOpenVideo " + accountId);
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        SpeakerItemView view;
        if (type == 2) {
            view = shareDocView;
        } else {
            view = getItemView(accountId);
        }
        if (view == null) {
            return;
        }
        switch (type) {
            case 0:
                CustomLog
                    .d("MultiSpeakerView",
                        "handleOpenVideo info.getCamStatus()"
                            + info.getCamStatus());
                if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_OPEN) {
                    view.getSurfaceView().setBackground(null);
                    setImageIconView(accountId);
                }
                break;
            case 1:
                CustomLog.d(
                    "MultiSpeakerView",
                    "handleOpenVideo info.getVideoStatus()"
                        + info.getVideoStatus());
                if (info.getVideoStatus() == SpeakerInfo.VIDEO_OPEN) {
                    view.getSurfaceView().setBackground(null);
                }
                break;
            case 2:
                CustomLog.d(
                    "MultiSpeakerView",
                    "handleCloseVideo info.getDocVideoStatus()"
                        + info.getDocVideoStatus());
                if (info.getDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OPEN) {
                    view.getSurfaceView().setBackground(null);
                }
                break;
        }

    }


    public void handleCloseVideo(String accountId, int type) {
        CustomLog.d("MultiSpeakerView", "handleCloseVideo " + accountId);
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        SpeakerItemView view;
        if (type == 2) {
            view = shareDocView;
        } else {
            view = getItemView(accountId);
        }
        if (view == null) {
            return;
        }
        switch (type) {
            case 0:
                CustomLog.d(
                    "MultiSpeakerView",
                    "handleCloseVideo info.getCamStatus()"
                        + info.getCamStatus());
                if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
                    view.getSurfaceView().setBackgroundResource(
                        MResource.getIdByName(mContext, MResource.DRAWABLE,
                            "meeting_room_close_camera"));
                }
                break;
            case 1:
                CustomLog.d(
                    "MultiSpeakerView",
                    "handleCloseVideo info.getVideoStatus()"
                        + info.getVideoStatus());
                if (info.getVideoStatus() == SpeakerInfo.VIDEO_OFF) {
                    view.getSurfaceView().setBackgroundResource(
                        MResource.getIdByName(mContext, MResource.DRAWABLE,
                            "close_this_video"));
                }
                break;
            case 2:
                CustomLog.d(
                    "MultiSpeakerView",
                    "handleCloseVideo info.getDocVideoStatus()"
                        + info.getDocVideoStatus());
                if (info.getDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OFF) {
                    view.getSurfaceView().setBackgroundResource(
                        MResource.getIdByName(mContext, MResource.DRAWABLE,
                            "close_this_video"));
                }
                break;
        }

    }


    @SuppressLint("NewApi")
    private void handleVideoForNormal(String accountId, SpeakerItemView view) {
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        CustomLog.d("MultiSpeakerView", accountId
            + " handleVideoForNormal getCamStatus()" + info.getCamStatus()
            + " getVideoStatus() " + info.getVideoStatus() + " getVideoOffReason " +
            info.getVideoOffReason());
        if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
            view.getSurfaceView().setBackgroundResource(
                MResource.getIdByName(mContext, MResource.DRAWABLE,
                    "meeting_room_close_camera"));
        } else if (info.getVideoStatus() == SpeakerInfo.VIDEO_OFF) {
            if (info.getVideoOffReason() == SpeakerInfo.VIDEO_OFF_REASON_AUTO) {
                view.getSurfaceView().setBackgroundResource(
                    MResource.getIdByName(mContext, MResource.DRAWABLE,
                        "meeting_room_close_camera_tip"));

                if (currentPage != DATA_PAGE) {
                    mMenuView.videoOffReminderInfoViewShow();
                    videoOffAuto = false;
                } else {
                    videoOffAuto = true;
                }

            } else {
                view.getSurfaceView().setBackgroundResource(
                    MResource.getIdByName(mContext, MResource.DRAWABLE,
                        "meeting_room_close_camera"));
            }
        } else {
            view.getSurfaceView().setBackground(null);
        }
    }


    @SuppressLint("NewApi")
    private void handleVideoForShare(String accountId, SpeakerItemView view) {
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        CustomLog.d(
            "MultiSpeakerView",
            "handleVideoForShare getDocVideoStatus()"
                + info.getDocVideoStatus());
        if (info.getDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OFF) {

            view.getSurfaceView().setBackgroundResource(
                MResource.getIdByName(mContext, MResource.DRAWABLE,
                    "close_this_video"));

        } else {
            view.getSurfaceView().setBackground(null);
        }
    }


    private void handleOpenShareDoc(String accountId) {
        CustomLog.d("MultiSpeakerView", "handleOpenShareDoc " + accountId);
        if (getItemView(accountId) == null) {
            return;
        }
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        setImageIconView(accountId);
    }


    private void handleCloseShareDoc(String accountId) {
        CustomLog.d("MultiSpeakerView", "handleCloseShareDoc " + accountId);
        if (getItemView(accountId) == null) {
            return;
        }
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        removeImageIconView(getItemView(accountId),
            info.getScreenShareStatus(), info.getMICStatus(),
            info.getCamStatus());
    }


    public void handleOpenMicOrCam(String accountId) {
        if (accountId == null) {
            return;
        }
        CustomLog.d("MultiSpeakerView", "handleOpenMicOrCam " + accountId);
        if (getItemView(accountId) == null) {
            return;
        }
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        CustomLog.d("MultiSpeakerView", "handleOpenMicOrCam getMICStatus "
            + info.getMICStatus());
        if (shareDocView != null && shareDocAccountId.equals(accountId)) {
            if (info.getMICStatus() == SpeakerInfo.MIC_STATUS_ON) {
                shareDocView.removeImageIcon(SpeakerItemView.MIC_TYPE);
            }
            if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_OPEN) {
                shareDocView.removeImageIcon(SpeakerItemView.CAMERA_TYPE);
            }
        }
        removeImageIconView(getItemView(accountId),
            info.getScreenShareStatus(), info.getMICStatus(),
            info.getCamStatus());
    }


    public void handleCloseMicOrCam(String accountId) {
        if (accountId == null) {
            return;
        }
        CustomLog.d("MultiSpeakerView", "handleCloseMicOrCam " + accountId);
        if (getItemView(accountId) == null) {
            return;
        }
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        setImageIconView(accountId);
        if (shareDocView != null && shareDocAccountId.equals(accountId)) {
            if (shareDocView.isExistImageIcon()) {
                shareDocView.removeAllImageIcon();
            }
            if (info.getMICStatus() == SpeakerInfo.MIC_STATUS_OFF) {
                shareDocView.addImageIcon(SpeakerItemView.MIC_TYPE);
            }
            if (info.getCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
                shareDocView.addImageIcon(SpeakerItemView.CAMERA_TYPE);
            }
        }
    }


    private void addNewViewToShow(SpeakerItemView view) {
        if (isWindowManager) {
            surfaceViewLayout.removeView(topLayout);
            surfaceViewLayout.removeView(imageViewBg);
            surfaceViewLayout.addView(view);
            surfaceViewLayout.addView(topLayout, topParams);
            surfaceViewLayout.addView(imageViewBg, imageViewP);
        } else {
            MultiSpeakerView.this.addView(view);
        }
    }


    private void addViewToLocation(SpeakerItemView view) {
        if (zoomInAccount != null) {
            if (isWindowManager) {
                if (surfaceViewLayout == null) {
                    return;
                }

                surfaceViewLayout.removeView(topLayout);
                surfaceViewLayout.removeView(imageViewBg);

                int n = surfaceViewLayout.getChildCount() - 1;
                if (n < 0) {
                    n = 0;
                }

                surfaceViewLayout.addView(view, n);
                surfaceViewLayout.addView(topLayout, topParams);
                surfaceViewLayout.addView(imageViewBg, imageViewP);
            } else {
                int n = MultiSpeakerView.this.getChildCount() - 1;
                if (n < 0) {
                    n = 0;
                }
                MultiSpeakerView.this.addView(view, n);
            }
        } else {
            if (isWindowManager) {

                if (surfaceViewLayout == null) {
                    return;
                }

                surfaceViewLayout.removeView(topLayout);
                surfaceViewLayout.removeView(imageViewBg);
                surfaceViewLayout.addView(view);
                surfaceViewLayout.addView(topLayout, topParams);
                surfaceViewLayout.addView(imageViewBg, imageViewP);
            } else {
                MultiSpeakerView.this.addView(view);
            }
        }
    }


    public void addSpeakerView(String accountId, String name,
                               boolean isNeedPublish) {
        CustomLog.d("MultiSpeakerView", "addSpeakerView " + accountId + " "
            + name);
        if (accountId == null || getItemView(accountId) != null) {
            return;
        }
        // 新发言人产生
        String mName;
        if (name == null || name.equals("")) {
            mName = "未命名";
        } else {
            mName = name;
        }

        SurfaceView view = new SurfaceView(mContext);

        SpeakerItemView speaker = new SpeakerItemView(mContext, width, height) {

            @Override
            public void viewOnClick(View view, int type, int viewType,
                                    String account, float x, float y) {
                CustomLog.d("MultiSpeakerView ", "multiViewList.size() "
                    + multiViewList.size() + " type " + type + " viewType "
                    + viewType);
                if (zoomInAccount != null && account.equals(zoomInAccount)) {
                    CustomLog.d("MultiSpeakerView ", "点击了小窗口" + zoomInAccount);
                    if (zoomOutAccount != null && zoomOutAccount.equals(shareDocZoomInOrOut)) {
                        //数据视图大屏
                        zoomInAccount = shareDocZoomInOrOut;
                        zoomOutAccount = account;
                        currentPage = SINGLE_BIG_PAGE;
                    } else {
                        //人像大屏
                        String tmp = "";
                        tmp = zoomOutAccount;
                        zoomOutAccount = zoomInAccount;
                        zoomInAccount = tmp;
                    }
                    //chage front view
                    if (!isWindowManager) {
                        if (zoomInAccount != null && zoomInAccount.equals(shareDocZoomInOrOut)) {
                            setSurfaceViewParams(getItemView(zoomOutAccount), false);
                            setSurfaceViewParams(shareDocView, true);
                        } else {
                            CustomLog.d("MultiSpeakerView ",
                                "小窗口变成 " + zoomInAccount + " zoomOutAccount " +
                                    getItemView(zoomOutAccount));
                            setSurfaceViewParams(getItemView(zoomOutAccount), false);
                            setSurfaceViewParams(getItemView(zoomInAccount), true);
                        }
                        setPageShow(currentPage);
                        updateDotsShow();
                    }

                }
            }


            @Override
            public void viewParam(String accountId, int top, int left, int right, int bottom) {
                if (zoomInAccount != null && zoomInAccount.equals(accountId)) {
                    //数据视图小窗
                    if (!isWindowManager) {
                        zoonInViewTop = top;
                        zoonInVietLeft = left;
                        zoonInViewRight = right;
                        zoomInBottom = bottom;
                    }
                }
            }


            @Override
            public void viewFlingPageRight() {

            }


            @Override
            public void viewFlingPageLeft() {

            }


            @Override
            public void viewFlingPageUp() {
                flingPageUp();
            }


            @Override
            public void viewFlingPageDown() {
                flingPageDown();
            }

        };
        speaker.setViewType(NORMAL_PAGE);
        if (isWindowManager) {
            speaker.createView(view, accountId, mName, 0, density, false);
        } else {
            speaker.createView(view, accountId, mName, 0, density, true);
        }

        multiViewList.add(speaker);
        addViewToLocation(speaker);
        if (isNeedPublish) {
            if (mAccountId != null && mAccountId.equals(accountId)) {
                mButelOpenSDK.startLocalVideo(MediaType.TYPE_VIDEO, view);
            } else {
                mButelOpenSDK.startRemoteVideo(accountId, MediaType.TYPE_VIDEO,
                    view);
            }
        }
        if (zoomInAccount == null) {
            if (zoomOutAccount == null) {
                zoomOutAccount = accountId;
            } else {
                if (zoomOutAccount.equals(mAccountId)) {
                    zoomInAccount = mAccountId;
                    zoomOutAccount = accountId;
                    setSurfaceViewParams(getItemView(zoomInAccount), true);
                } else if (accountId.equals(mAccountId)) {
                    zoomInAccount = mAccountId;
                    speaker.getSurfaceView().setZOrderOnTop(true);
                    speaker.getSurfaceView().setZOrderMediaOverlay(true);
                    speaker.getSurfaceView().getHolder().setFormat(PixelFormat.TRANSPARENT);
                    speaker.setIsMove(true);
                }
            }
        } else {
     /*if(zoomInAccount.equals(shareDocZoomInOrOut)){
                setSurfaceViewParams(shareDocView, true);
            }else{
                setSurfaceViewParams(getItemView(zoomInAccount), true);
            }*/
        }
        if (mButelOpenSDK.getSpeakerInfoById(accountId) != null) {
            setImageIconView(accountId);
        }
        if(mListener != null){
            CustomLog.d(TAG,"getSelftVideoParameter "+mListener.getSelftVideoParameter().getCapWidth()+","+mListener.getSelftVideoParameter().getCapHeight());
            selfPreViewProportionX = (float)mListener.getSelftVideoParameter().getCapWidth()/ (float)mListener.getSelftVideoParameter().getCapHeight();
            selfPreViewProportionY = (float)mListener.getSelftVideoParameter().getCapHeight()/ (float)mListener.getSelftVideoParameter().getCapWidth();
        }
        setPageShow(currentPage);
        mMenuView.hideSpeakerListInfoViewShow();
        mMenuView.speakerListInfoViewShow();
        updateDotsShow();
    }


    public void setImageIconView(String accountId) {
        SpeakerInfo info = mButelOpenSDK.getSpeakerInfoById(accountId);
        if (info == null) {
            return;
        }
        SpeakerItemView view = getItemView(accountId);
        int shareDocType = info.getScreenShareStatus();
        int micPhoneType = info.getMICStatus();
        int camType = info.getCamStatus();
        CustomLog.d("MultiSpeakerView", "setImageIconView shareDocType "
            + shareDocType + " micPhoneType " + micPhoneType + " camType "
            + camType);
        if (view != null) {
            if (view.isExistImageIcon()) {
                view.removeAllImageIcon();
            }
            if (shareDocType == SpeakerInfo.SCREEN_SHARING) {
                view.addImageIcon(SpeakerItemView.SHARE_DOC_TYPE);
            }
            if (micPhoneType == SpeakerInfo.MIC_STATUS_OFF) {
                view.addImageIcon(SpeakerItemView.MIC_TYPE);
            }
            if (camType == SpeakerInfo.CAM_STATUS_CLOSE) {
                view.addImageIcon(SpeakerItemView.CAMERA_TYPE);
            }
        }
    }


    private void removeImageIconView(SpeakerItemView view, int shareDocType,
                                     int micPhoneType, int camType) {
        CustomLog.d("MultiSpeakerView", "removeImageIconView " + shareDocType
            + " " + micPhoneType + " " + camType);
        if (view != null) {
            if (shareDocType == SpeakerInfo.SCREEN_NORMAL) {
                view.removeImageIcon(SpeakerItemView.SHARE_DOC_TYPE);
            }
            if (micPhoneType == SpeakerInfo.MIC_STATUS_ON) {
                view.removeImageIcon(SpeakerItemView.MIC_TYPE);
            }
            if (camType == SpeakerInfo.CAM_STATUS_OPEN) {
                view.removeImageIcon(SpeakerItemView.CAMERA_TYPE);
            }
        }
    }


    public void addShareDocView(final String accountId, String name,
                                boolean isNeedPublish) {
        CustomToast.show(mContext, name + "开始屏幕分享", CustomToast.LENGTH_SHORT);
        mMenuView.hideSwitchVideoTypeView();
        CustomLog.d("MultiSpeakerView", "addShareDocView " + accountId + " "
            + name);
        if (shareDocView != null) {
            removeShareDocView(accountId, name);
        }

        SurfaceView view = new SurfaceView(mContext);

        shareDocView = new SpeakerItemView(mContext, width, height) {
            @Override
            public void viewOnClick(View view, int type, int viewType,
                                    String account, float x, float y) {
                CustomLog.e("MultiSpeakerView ", "multiViewList.size() "
                    + multiViewList.size() + " type " + type + " viewType "
                    + viewType + " x " + x + " y " + y);
                //窗口切换
                if (zoomInAccount != null && zoomInAccount.equals(shareDocZoomInOrOut)) {
                    //数据视图小窗
                    if (!isWindowManager) {
                        String tmp = "";
                        tmp = zoomOutAccount;
                        zoomInAccount = tmp;
                        zoomOutAccount = shareDocZoomInOrOut;
                        setSurfaceViewParams(shareDocView, false);
                        setSurfaceViewParams(getItemView(zoomInAccount), true);
                        setPageShow(DATA_PAGE);
                        updateDotsShow();
                    }
                }

            }


            @Override
            public void viewParam(String accountId, int top, int left, int right, int bottom) {
                if (zoomInAccount != null && zoomInAccount.equals(shareDocZoomInOrOut)) {
                    //数据视图小窗
                    if (!isWindowManager) {
                        zoonInViewTop = top;
                        zoonInVietLeft = left;
                        zoonInViewRight = right;
                        zoomInBottom = bottom;
                    }
                }
            }


            @Override
            public void viewFlingPageRight() {

            }


            @Override
            public void viewFlingPageLeft() {

            }


            @Override
            public void viewFlingPageUp() {
                flingPageUp();
            }


            @Override
            public void viewFlingPageDown() {
                flingPageDown();
            }

        };
        shareDocView.setViewType(DATA_PAGE);
        shareDocView.createView(view, accountId, name, 0, density, false);
        if (mButelOpenSDK.getSpeakerInfoById(accountId) != null) {
            if (mButelOpenSDK.getSpeakerInfoById(accountId).getMICStatus() ==
                SpeakerInfo.MIC_STATUS_OFF) {
                shareDocView.addImageIcon(SpeakerItemView.MIC_TYPE);
            }
            if (mButelOpenSDK.getSpeakerInfoById(accountId).getCamStatus() ==
                SpeakerInfo.CAM_STATUS_CLOSE) {
                shareDocView.addImageIcon(SpeakerItemView.CAMERA_TYPE);
            }
        }
        // 调用sdk 订阅
        if (isNeedPublish) {
            if (mAccountId != null && mAccountId.equals(accountId)) {
                mButelOpenSDK.startLocalVideo(MediaType.TYPE_DOC_VIDEO, view);
            } else {
                mButelOpenSDK.startRemoteVideo(accountId,
                    MediaType.TYPE_DOC_VIDEO, view);
            }
        }
        addNewViewToShow(shareDocView);
        if (zoomInAccount == null) {
            if (zoomOutAccount.equals(mAccountId)) {
                zoomInAccount = mAccountId;
                setSurfaceViewParams(getItemView(mAccountId), true);
            }
        } else {
            if (!zoomInAccount.equals(mAccountId)) {
                setSurfaceViewParams(getItemView(zoomInAccount), false);
            }
            zoomInAccount = mAccountId;
            setSurfaceViewParams(getItemView(mAccountId), true);
        }
        zoomOutAccount = shareDocZoomInOrOut;
        shareDocAccountId = accountId;
        setPageShow(DATA_PAGE);
        updateDotsShow();
    }


    private void updateDotsShow() {
        int size = 0;
        int position = 0;
        boolean isShow = false;
        if (zoomInAccount == null) {
            if (shareDocView == null) {
                size = multiViewList.size();
            } else {
                size = multiViewList.size() + 1;
            }
            if (size > 1) {
                isShow = true;
            } else {
                isShow = false;
            }
            if (currentPage == DATA_PAGE) {
                position = size - 1;
                currentPosition = multiViewList.size();
            } else {
                for (int i = 0; i < multiViewList.size(); i++) {
                    if (multiViewList.get(i).getAccountId().equals(zoomOutAccount)) {
                        position = i;
                        currentPosition = position;
                        break;
                    }
                }
            }
        } else {
            if (shareDocView == null) {
                size = multiViewList.size() - 1;
            } else {
                size = multiViewList.size();
            }
            if (size > 1) {
                isShow = true;
            } else {
                isShow = false;
            }
            int positionIn = 0;
            int positionOut = 0;
            if (currentPage == DATA_PAGE) {
                currentPosition = multiViewList.size();
                position = size - 1;
            } else {
                if (zoomInAccount.equals(shareDocZoomInOrOut)) {
                    position = size - 1;
                    currentPosition = getViewPosition(zoomOutAccount);
                } else {
                    positionOut = getViewPosition(zoomOutAccount);
                    positionIn = getViewPosition(zoomInAccount);
                    if (zoomOutAccount != null && zoomOutAccount.equals(mAccountId)) {
                        position = positionIn;
                    } else {
                        position = positionOut;
                    }
                    int own = getViewPosition(mAccountId);
                    if (position > own) {
                        position--;
                    }
                    currentPosition = positionOut;
                }
            }
        }
        mMultiSpeakerViewListener.update(size, position, isShow);
    }


    private void setSurfaceViewParams(SpeakerItemView view, boolean isOnTop) {
        if (view == null) {
            CustomLog.e(TAG, "setSurfaceViewParams view == null");
            return;
        }
        CustomLog.d(TAG,
            "setSurfaceViewParams isWindowManager " + isWindowManager + ",isMiniWindowManager" +
                isMiniWindowManager);
        if (isWindowManager) {
            if(topLayout!=null){
                surfaceViewLayout.removeView(topLayout);
            }
            if(imageViewBg!=null){
                surfaceViewLayout.removeView(imageViewBg);
            }
            surfaceViewLayout.removeView(view);
            surfaceViewLayout.addView(view);
            surfaceViewLayout.addView(topLayout, topParams);
            surfaceViewLayout.addView(imageViewBg, imageViewP);
        } else {
            if (!isMiniWindowManager) {
                MultiSpeakerView.this.removeView(view);
                MultiSpeakerView.this.addView(view);
            }
        }
        view.getSurfaceView().setZOrderOnTop(isOnTop);
        view.getSurfaceView().setZOrderMediaOverlay(isOnTop);
        if (isOnTop) {
            view.getSurfaceView().getHolder().setFormat(PixelFormat.TRANSPARENT);
        } else {
            view.getSurfaceView().getHolder().setFormat(PixelFormat.RGBA_8888);
        }
        view.setIsMove(isOnTop);
    }


    private void setSurfaceViewTopOrNot(SpeakerItemView view, boolean isOnTop) {
        if (view == null) {
            CustomLog.e(TAG, "setSurfaceViewTop view == null");
            return;
        }
        CustomLog.d(TAG,
            "setSurfaceViewParams isWindowManager " + isWindowManager + ",isMiniWindowManager" +
                isMiniWindowManager);
        view.getSurfaceView().setZOrderOnTop(isOnTop);
        view.getSurfaceView().setZOrderMediaOverlay(isOnTop);
        if (isOnTop) {
            view.getSurfaceView().getHolder().setFormat(PixelFormat.TRANSPARENT);
        } else {
            view.getSurfaceView().getHolder().setFormat(PixelFormat.RGBA_8888);
        }
        view.setIsMove(isOnTop);
    }


    public void removeSpeakerView(String accountId, String name) {
        if (accountId == null) {
            return;
        }
        if (shareDocView != null && shareDocAccountId != null
            && accountId.equals(shareDocAccountId)) {
            removeShareDocView(accountId, name);
        }
        SpeakerItemView view = getItemView(accountId);
        if (view != null) {
            if (mAccountId != null && mAccountId.equals(accountId)) {
                mButelOpenSDK.stopLocalVideo(MediaType.TYPE_VIDEO);
            } else {
                mButelOpenSDK.stopRemoteVideo(accountId, MediaType.TYPE_VIDEO);
            }
            if (isWindowManager) {
                if (surfaceViewLayout != null) {
                    surfaceViewLayout.removeView(view);
                }
            } else {
                MultiSpeakerView.this.removeView(view);
            }
            int currentType = currentPage;
            switch (currentPage) {
                case DATA_PAGE:
                    if (zoomInAccount != null && zoomInAccount.equals(accountId)) {
                        zoomInAccount = null;
                        zoonInViewTop = x8;
                        zoonInVietLeft = x8;
                        zoonInViewRight = 0;
                        zoomInBottom = 0;
                        //setPageShow(currentType);
                        //updateDotsShow();
                    }
                    break;
                case SINGLE_BIG_PAGE:
                    //delete zoom out account
                    if (zoomOutAccount != null && zoomOutAccount.equals(accountId)) {
                        if (zoomInAccount != null) {
                            if (zoomOutAccount.equals(mAccountId)) {
                                //chage zoom in to zoom out
                                String tmp = "";
                                tmp = zoomInAccount;
                                zoomOutAccount = tmp;
                                zoomInAccount = null;
                                zoonInViewTop = x8;
                                zoonInVietLeft = x8;
                                zoonInViewRight = 0;
                                zoomInBottom = 0;
                                if (tmp.equals(shareDocZoomInOrOut)) {
                                    setSurfaceViewParams(shareDocView, false);
                                    currentPage = DATA_PAGE;
                                    //setPageShow(DATA_PAGE);
                                    //updateDotsShow();
                                } else {
                                    setSurfaceViewParams(getItemView(tmp), false);
                                    //setPageShow(SINGLE_BIG_PAGE);
                                    //updateDotsShow();
                                }
                            } else {
                                // chage zoom out account and  show next
                                removeAndPageUpByZoomOut();
                            }
                        } else {
                            // chage zoom out account and  show next
                            removeAndPageUpByZoomOut();
                        }
                    } else if (zoomInAccount != null && zoomInAccount.equals(accountId)) {
                        // remove zoom in view
                        if (zoomInAccount.equals(mAccountId)) {
                            zoomInAccount = null;
                            zoonInViewTop = x8;
                            zoonInVietLeft = x8;
                            zoonInViewRight = 0;
                            zoomInBottom = 0;
                            //setPageShow(currentType);
                            // updateDotsShow();
                        } else {
                            //chage zoom in to next
                            removeAndPageUpByZoomIn();
                        }
                    }
                    break;
            }
            multiViewList.remove(view);
            setPageShow(currentPage);
            updateDotsShow();
            onNotify(0, null);
        }
    }


    private int getViewPosition(String accountId) {
        int position = -1;
        for (int i = 0; i < multiViewList.size(); i++) {
            if (multiViewList.get(i).getAccountId().equals(accountId)) {
                position = i;
                break;
            }
        }
        return position;
    }


    public void removeShareDocView(String accountId, String name) {
        CustomLog.d("MultiSpeakerView", "removeShareDocView " + accountId);
        CustomToast.show(mContext, name + "结束屏幕分享", CustomToast.LENGTH_SHORT);
        mMenuView.hideSwitchVideoTypeView();
        if (accountId == null) {
            return;
        }
        if (shareDocView != null) {
            if (mAccountId != null && mAccountId.equals(accountId)) {
                mButelOpenSDK.stopLocalVideo(MediaType.TYPE_DOC_VIDEO);
            } else {
                mButelOpenSDK.stopRemoteVideo(accountId,
                    MediaType.TYPE_DOC_VIDEO);
            }
            if (isWindowManager) {
                surfaceViewLayout.removeView(shareDocView);
            } else {
                MultiSpeakerView.this.removeView(shareDocView);
            }
        }
        shareDocAccountId = null;
        shareDocView = null;
        if (currentPage == DATA_PAGE) {
            //排除zoomin
            if (zoomInAccount != null) {
                int currentPoing = 0;
                if (zoomInAccount.equals(multiViewList.get(currentPoing).getAccountId())) {
                    currentPoing++;
                }
                if (currentPoing > (multiViewList.size() - 1)) {
                    currentPoing = 0;
                }
                if (zoomInAccount.equals(multiViewList.get(currentPoing).getAccountId())) {
                    String tmp = "";
                    tmp = zoomInAccount;
                    zoomOutAccount = tmp;
                    zoomInAccount = null;
                    zoonInViewTop = x8;
                    zoonInVietLeft = x8;
                    zoonInViewRight = 0;
                    zoomInBottom = 0;
                    setSurfaceViewParams(getItemView(zoomOutAccount), false);
                } else {
                    zoomOutAccount = multiViewList.get(currentPoing).getAccountId();
                }
                setPageShow(SINGLE_BIG_PAGE);
                updateDotsShow();
            } else {
                zoomOutAccount = multiViewList.get(0).getAccountId();
                setPageShow(SINGLE_BIG_PAGE);
                updateDotsShow();
            }
        } else {
            // 多屏页面
            if (zoomInAccount != null && zoomInAccount.equals(shareDocZoomInOrOut)) {
                //chage zoom in to next
                int currentPoing = 0;
                if (zoomOutAccount.equals(multiViewList.get(currentPoing).getAccountId())) {
                    currentPoing++;
                }
                if (currentPoing > (multiViewList.size() - 1)) {
                    currentPoing = 0;
                }
                if (zoomOutAccount.equals(multiViewList.get(currentPoing).getAccountId())) {
                    zoomInAccount = null;
                    zoonInViewTop = x8;
                    zoonInVietLeft = x8;
                    zoonInViewRight = 0;
                    zoomInBottom = 0;
                } else {
                    zoomInAccount = multiViewList.get(currentPoing).getAccountId();
                    setSurfaceViewParams(getItemView(zoomInAccount), true);
                }
                setPageShow(SINGLE_BIG_PAGE);
            }
            updateDotsShow();
        }
        CustomLog.d(TAG, "removeShareDocView,mMultiSpeakerViewListener.remove,currentPosition:" +
            String.valueOf(currentPosition));
    }


    public SpeakerItemView getItemView(String accountId) {
        SpeakerItemView view = null;
        for (int i = 0; i < multiViewList.size(); i++) {
            if (multiViewList.get(i).getAccountId() != null
                && multiViewList.get(i).getAccountId().equals(accountId)) {
                view = multiViewList.get(i);
            }
        }
        return view;
    }


    private void onlyShowAudio() {
        switch (currentPage) {
            case DATA_PAGE:
                if (shareDocView != null) {
                    shareDocView.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                    mButelOpenSDK.resumeSubscribe(shareDocView.getAccountId(),
                        MediaType.TYPE_DOC_VIDEO);
                }
                for (int i = 0; i < multiViewList.size(); i++) {
                    if (multiViewList.get(i).getAccountId().equals(zoomInAccount)) {
                        multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
                            .getAccountId(), MediaType.TYPE_VIDEO);
                    } else {
                        multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.pauseSubscribe(multiViewList.get(i)
                            .getAccountId(), MediaType.TYPE_VIDEO);
                    }
                }
                break;
            case SINGLE_BIG_PAGE:
                if (shareDocView != null) {
                    if (zoomInAccount != null && zoomInAccount.equals(shareDocZoomInOrOut)) {
                        shareDocView.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.resumeSubscribe(shareDocView.getAccountId(),
                            MediaType.TYPE_DOC_VIDEO);
                    } else {
                        shareDocView.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.pauseSubscribe(shareDocView.getAccountId(),
                            MediaType.TYPE_DOC_VIDEO);
                    }
                }
                SpeakerItemView view = null;
                for (int i = 0; i < multiViewList.size(); i++) {
                    view = multiViewList.get(i);
                    CustomLog.d(TAG, "ssssssssssss " + view.getAccountId());
                    if (zoomOutAccount != null && view.getAccountId().equals(zoomOutAccount)) {
                        multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
                            .getAccountId(), MediaType.TYPE_VIDEO);
                    } else {
                        if (zoomInAccount != null && view.getAccountId().equals(zoomInAccount)) {
                            multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                            mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
                                .getAccountId(), MediaType.TYPE_VIDEO);
                        } else {
                            multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                            mButelOpenSDK.pauseSubscribe(multiViewList.get(i)
                                .getAccountId(), MediaType.TYPE_VIDEO);
                        }
                    }
                }
                break;
        }
    }


    private void setPageShow(int pageType) {
        CustomLog.d(TAG, "setPageShow,currentPage:" + String.valueOf(currentPage) + " pageType:" +
                String.valueOf(pageType) + " & selfPreViewProportionY：" + selfPreViewProportionY
                + ", selfPreViewProportionX：" + selfPreViewProportionX);
        switch (pageType) {
            case DATA_PAGE:
                // mMenuView 常显提示
                mMenuView.setPageType(1, null);
                mMenuView.hideVideoOffReminderInfoView();
                mMenuView.hideSpeakerListInfoViewShow();
                mMenuView.shareDocReminderInfoViewShow();
                if (shareDocView != null) {
                    if (!isWindowManager) {
                        if (isMiniWindowManager) {
                            shareDocView.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        } else {
                            shareDocView.setSpeakerItemViewParams(width, height, 0, 0, 0, 0);
                        }
                    } else {
                        shareDocView.setSpeakerItemViewParams(
                            height,
                            specialHeight,
                            0, 0, 0, 0);
                    }
                    mButelOpenSDK.resumeSubscribe(shareDocView.getAccountId(),
                        MediaType.TYPE_DOC_VIDEO);
                    handleVideoForShare(shareDocView.getAccountId(), shareDocView);
                }
                for (int i = 0; i < multiViewList.size(); i++) {
                    if (multiViewList.get(i).getAccountId().equals(zoomInAccount)) {
                        multiViewList.get(i).setLayoutVisibility(false);
                        if (!isWindowManager) {
                            if (isMiniWindowManager) {
                                multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                            } else {
                                multiViewList.get(i)
                                        .setSpeakerItemViewParams(
                                                (int) (getResources().getDimension(R.dimen.y240)*selfPreViewProportionX),
                                                (int) getResources().getDimension(R.dimen.y240),
                                                zoonInVietLeft, zoonInViewTop, zoonInViewRight, zoomInBottom);
                            }
                        } else {
                            if (multiViewList.get(i).getAccountId().equals(mAccountId)) {
                                multiViewList.get(i).setSpeakerItemViewParams(
                                        (int) (getResources().getDimension(R.dimen.y320) *selfPreViewProportionY),
                                        (int) getResources().getDimension(R.dimen.y320),
                                        0,
                                        0,0,0);
                            } else {
                                multiViewList.get(i).setSpeakerItemViewParams(
                                        (int) getResources().getDimension(R.dimen.x320),
                                        (int) getResources().getDimension(R.dimen.y180),
                                        0,
                                        0,0,0);
                            }
                        }
                        mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
                            .getAccountId(), MediaType.TYPE_VIDEO);
                    } else {
                        multiViewList.get(i).setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.pauseSubscribe(multiViewList.get(i)
                            .getAccountId(), MediaType.TYPE_VIDEO);
                    }
                }
                currentPage = DATA_PAGE;
                break;
            case SINGLE_BIG_PAGE:
                mMenuView.setPageType(2, zoomOutAccount);
                mMenuView.hideShareDocReminderInfoView();
                if (videoOffAuto) {
                    videoOffAuto = false;
                    mMenuView.videoOffReminderInfoViewShow();
                }
                if (multiViewList.size() > 0) {
                    mMenuView.speakerListInfoViewShow();
                } else {
                    mMenuView.hideSpeakerListInfoViewShow();
                }
                setViewHierarchy();
                if (shareDocView != null) {
                    if (zoomInAccount != null && zoomInAccount.equals(shareDocZoomInOrOut)) {
                        if (!isWindowManager) {
                            if(isMiniWindowManager){
                                shareDocView.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                            }else {
                                shareDocView.setSpeakerItemViewParams(
                                        (int) getResources().getDimension(R.dimen.x427),
                                        (int) getResources().getDimension(R.dimen.y240),
                                        zoonInVietLeft, zoonInViewTop, zoonInViewRight, zoomInBottom);
                            }
                        } else {
                            shareDocView.setSpeakerItemViewParams(
                                (int) getResources().getDimension(R.dimen.x320),
                                (int) getResources().getDimension(R.dimen.y180),
                                0,
                                0, 0, 0);
                        }
                        mButelOpenSDK.resumeSubscribe(shareDocView.getAccountId(),
                            MediaType.TYPE_DOC_VIDEO);
                    } else {
                        shareDocView.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                        mButelOpenSDK.pauseSubscribe(shareDocView.getAccountId(),
                            MediaType.TYPE_DOC_VIDEO);
                    }
                }
                SpeakerItemView view = null;
                setViewHierarchy();
                for (int i = 0; i < multiViewList.size(); i++) {
                    view = multiViewList.get(i);
                    CustomLog.d(TAG, "ssssssssssss " + view.getAccountId());
                    if (zoomOutAccount != null && view.getAccountId().equals(zoomOutAccount)) {
                        if (!isWindowManager) {
                            if(isMiniWindowManager){
                                view.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                            }else {
                                view.setLayoutVisibility(true);
                                if(zoomOutAccount.equals(mAccountId)){
                                    view.setSpeakerItemViewCenterParams((int) (height * selfPreViewProportionX), height, 0, 0, 0, 0);
                                }else {
                                    view.setSpeakerItemViewParams(width, height, 0, 0, 0, 0);
                                }
                            }
                        } else {
                            if (view.getAccountId().equals(mAccountId)) {
                                view.setSpeakerItemViewParams(
                                    (int) getResources().getDimension(R.dimen.x180),
                                    (int) getResources().getDimension(R.dimen.y320),
                                    0,
                                    0, 0, 0);
                                if (zoomInAccount != null) {
                                    if (zoomInAccount.equals(shareDocZoomInOrOut)) {
                                        shareDocView.setSpeakerItemViewParams(
                                            height,
                                            specialHeight,
                                            0, 0, 0, 0);
                                    } else {
                                        getItemView(zoomInAccount).setSpeakerItemViewParams(
                                            height,
                                            specialHeight,
                                            0, 0, 0, 0);
                                    }
                                }
                            } else {
                                view.setSpeakerItemViewParams(
                                    height,
                                    specialHeight,
                                    0, 0, 0, 0);
                            }
                        }
                        //multiViewList.get(i).setIsMove(false);
                        mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
                            .getAccountId(), MediaType.TYPE_VIDEO);
                    } else {
                        if (zoomInAccount != null && view.getAccountId().equals(zoomInAccount)) {
                            view.setLayoutVisibility(false);
                            if (!isWindowManager) {
                                //view.setLayoutVisibility(false);
                                //multiViewList.get(i).setIsMove(true);
                                if(isMiniWindowManager){
                                    view.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                                }else {
                                    if(zoomInAccount.equals(mAccountId)){
                                        view.setSpeakerItemViewParams(
                                                (int) (getResources().getDimension(R.dimen.y240)*selfPreViewProportionX),
                                                (int) getResources().getDimension(R.dimen.y240),
                                                zoonInVietLeft, zoonInViewTop, zoonInViewRight, zoomInBottom);
                                    }else {
                                        view.setSpeakerItemViewParams(
                                                (int) getResources().getDimension(R.dimen.x427),
                                                (int) getResources().getDimension(R.dimen.y240),
                                                zoonInVietLeft, zoonInViewTop, zoonInViewRight, zoomInBottom);
                                    }
                                }
                            } else {
                                if(view.getAccountId().equals(mAccountId)){
                                    view.setSpeakerItemViewParams(
                                            (int) (getResources().getDimension(R.dimen.y320)*selfPreViewProportionY),
                                            (int)getResources().getDimension(R.dimen.y320),
                                            0,
                                            0,0,0);
                                }else{
                                    if(zoomOutAccount!=null&&zoomOutAccount.equals(mAccountId)){
                                        getItemView(zoomOutAccount).setSpeakerItemViewParams(
                                                (int) (getResources().getDimension(R.dimen.y320)*selfPreViewProportionY),
                                                (int)getResources().getDimension(R.dimen.y320),
                                                0,0,0,0);
                                        view.setSpeakerItemViewParams(
                                                height,
                                                specialHeight,
                                                0,0,0,0);
                                    }else{
                                        view.setSpeakerItemViewParams(
                                                (int)getResources().getDimension(R.dimen.x320),
                                                (int)getResources().getDimension(R.dimen.y180),
                                                0, 0,0,0);
                                    }
                                }
                            }
                            mButelOpenSDK.resumeSubscribe(multiViewList.get(i)
                                .getAccountId(), MediaType.TYPE_VIDEO);
                        } else {
                            //multiViewList.get(i).setIsMove(false);
                            view.setSpeakerItemViewParams(1, 1, 0, 0, 0, 0);
                            mButelOpenSDK.pauseSubscribe(multiViewList.get(i)
                                .getAccountId(), MediaType.TYPE_VIDEO);
                        }

                    }
                }
                //setViewHierarchy();
                currentPage = SINGLE_BIG_PAGE;
                break;
        }
    }


    private void setViewHierarchy() {
        if (isWindowManager) {
            if (surfaceViewLayout == null) {
                CustomLog.e(TAG, "setViewHierarchy surfaceViewLayout == null ");
                return;
            }
            if (getItemView(mAccountId) == null) {
                //我没有发言，不错处理
                CustomLog.e(TAG, "setViewHierarchy getItemView(mAccountId)==null ");
                return;
            }
            if (isMiniWindowManager) {
                CustomLog.e(TAG, "setViewHierarchy 最小化不做处理 ");
                return;
            }
            if (zoomOutAccount != null && zoomInAccount != null &&
                zoomOutAccount.equals(mAccountId)) {
                surfaceViewLayout.removeAllViews();
                if (shareDocView != null) {
                    surfaceViewLayout.addView(shareDocView);
                    shareDocView.getSurfaceView().setZOrderOnTop(false);
                    shareDocView.getSurfaceView().setZOrderMediaOverlay(false);
                    shareDocView.getSurfaceView().getHolder().setFormat(PixelFormat.RGBA_8888);
                }

                for (int i = 0; i < multiViewList.size(); i++) {
                    if (!multiViewList.get(i).getAccountId().equals(mAccountId)) {
                        surfaceViewLayout.addView(multiViewList.get(i));
                        multiViewList.get(i).getSurfaceView().setZOrderOnTop(false);
                        multiViewList.get(i).getSurfaceView().setZOrderMediaOverlay(false);
                        multiViewList.get(i)
                            .getSurfaceView()
                            .getHolder()
                            .setFormat(PixelFormat.RGBA_8888);
                    }
                }

                if (getItemView(mAccountId) != null) {
                    surfaceViewLayout.addView(getItemView(mAccountId));
                    getItemView(mAccountId).getSurfaceView().setZOrderOnTop(true);
                    getItemView(mAccountId).getSurfaceView().setZOrderMediaOverlay(true);
                    getItemView(mAccountId).getSurfaceView()
                        .getHolder()
                        .setFormat(PixelFormat.TRANSPARENT);
                }
                surfaceViewLayout.addView(topLayout, topParams);
                surfaceViewLayout.addView(imageViewBg, imageViewP);
            }
        }
    }


    public void setMultiViewListBg() {
        if (multiViewList == null) {
            return;
        }
        for (int i = 0; i < multiViewList.size(); i++) {
            handleVideoForNormal(multiViewList.get(i).getAccountId(),
                multiViewList.get(i));
        }
    }


    // 全屏 up 翻页
    private void removeAndPageUpByZoomOut() {
        //当前全屏的位置
        int position = currentPosition;
        if (currentPosition == multiViewList.size() - 1) {
            if (shareDocView != null) {
                zoomOutAccount = shareDocZoomInOrOut;
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = 0;
            }
        } else if (currentPosition == multiViewList.size() && shareDocView != null) {
            position = 0;
        } else {
            position = position + 1;
        }
        if (zoomInAccount != null &&
            zoomInAccount.equals(multiViewList.get(position).getAccountId())) {
            position = position + 1;
        }
        if (position > (multiViewList.size() - 1)) {
            if (shareDocView != null) {
                zoomOutAccount = shareDocZoomInOrOut;
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = 0;
            }
        }

        if (multiViewList.get(position).getAccountId().equals(zoomOutAccount)) {
            if (zoomInAccount != null) {
                String tmp = "";
                tmp = zoomInAccount;
                zoomOutAccount = tmp;
                zoomInAccount = null;
                zoonInViewTop = x8;
                zoonInVietLeft = x8;
                zoonInViewRight = 0;
                zoomInBottom = 0;
                setSurfaceViewParams(getItemView(tmp), false);
            } else {
                zoomOutAccount = null;
            }
        } else {
            zoomOutAccount = multiViewList.get(position).getAccountId();
        }
        //setPageShow(currentPage);
        //updateDotsShow();
    }


    // 全屏 up 翻页
    private void flingPageUpByZoomOut() {
        //当前全屏的位置
        int position = currentPosition;
        if (currentPosition == multiViewList.size() - 1) {
            if (shareDocView != null) {
                zoomOutAccount = shareDocZoomInOrOut;
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = 0;
            }
        } else if (currentPosition == multiViewList.size() && shareDocView != null) {
            position = 0;
            currentPage = SINGLE_BIG_PAGE;
        } else {
            position = position + 1;
        }
        if (zoomInAccount != null &&
            zoomInAccount.equals(multiViewList.get(position).getAccountId())) {
            position = position + 1;
        }
        if (position > (multiViewList.size() - 1)) {
            if (shareDocView != null) {
                position = multiViewList.size();
                zoomOutAccount = shareDocZoomInOrOut;
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = 0;
            }
        }
        zoomOutAccount = multiViewList.get(position).getAccountId();
        setPageShow(currentPage);
        updateDotsShow();
        CustomLog.d("MultiSpeakerView",
            "flingPageUpByZoomOut " + zoomInAccount + "    " + zoomOutAccount);
    }


    //小窗翻页
    private void removeAndPageUpByZoomIn() {
        CustomLog.d("MultiSpeakerView", "removeAndPageUpByZoomIn: own zoom out ");
        int position = 0;
        if (zoomInAccount.equals(shareDocZoomInOrOut)) {
            position = 0;
        } else {
            position = getViewPosition(zoomInAccount);
            if (position == multiViewList.size() - 1) {
                if (shareDocView != null) {
                    String tmp = "";
                    tmp = zoomInAccount;
                    zoomInAccount = shareDocZoomInOrOut;
                    setSurfaceViewParams(getItemView(tmp), false);
                    setSurfaceViewParams(shareDocView, true);
                    currentPage = DATA_PAGE;
                    setPageShow(currentPage);
                    updateDotsShow();
                    return;
                } else {
                    position = 0;
                }
            } else {
                position++;
            }
        }

        if (multiViewList.get(position).getAccountId().equals(zoomOutAccount)) {
            position++;
        }
        if (position > (multiViewList.size() - 1)) {
            if (shareDocView != null) {
                String tmp = "";
                tmp = zoomInAccount;
                zoomInAccount = shareDocZoomInOrOut;
                setSurfaceViewParams(getItemView(tmp), false);
                setSurfaceViewParams(shareDocView, true);
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = 0;
            }
        }

        if (multiViewList.get(position).getAccountId().equals(zoomInAccount)) {
            zoomInAccount = null;
            zoonInViewTop = x8;
            zoonInVietLeft = x8;
            zoonInViewRight = 0;
            zoomInBottom = 0;
            setPageShow(currentPage);
            updateDotsShow();
        } else {
            String tmp = "";
            tmp = zoomInAccount;
            zoomInAccount = multiViewList.get(position).getAccountId();
            if (tmp.equals(shareDocZoomInOrOut)) {
                setSurfaceViewParams(shareDocView, false);
            } else {
                setSurfaceViewParams(getItemView(tmp), false);
            }
            setSurfaceViewParams(multiViewList.get(position), true);
            setPageShow(currentPage);
            updateDotsShow();
        }
        return;
    }


    //小窗翻页
    private void flingPageUpByZoomIn() {
        CustomLog.d("MultiSpeakerView", "flingPageUpByZoomIn: own zoom out ");
        int position = 0;
        if (zoomInAccount.equals(shareDocZoomInOrOut)) {
            position = 0;
        } else {
            position = getViewPosition(zoomInAccount);
            if (position == multiViewList.size() - 1) {
                if (shareDocView != null) {
                    String tmp = "";
                    tmp = zoomInAccount;
                    zoomInAccount = shareDocZoomInOrOut;
                    setSurfaceViewParams(getItemView(tmp), false);
                    setSurfaceViewParams(shareDocView, true);
                    //currentPage = DATA_PAGE;
                    setPageShow(currentPage);
                    updateDotsShow();
                    return;
                } else {
                    position = 0;
                }
            } else {
                position++;
            }
        }

        if (multiViewList.get(position).getAccountId().equals(zoomOutAccount)) {
            position++;
        }
        if (position > (multiViewList.size() - 1)) {
            if (shareDocView != null) {
                String tmp = "";
                tmp = zoomInAccount;
                zoomInAccount = shareDocZoomInOrOut;
                if (tmp.equals(shareDocZoomInOrOut)) {
                    setSurfaceViewParams(shareDocView, false);
                } else {
                    setSurfaceViewParams(getItemView(tmp), false);
                }
                setSurfaceViewParams(shareDocView, true);
                //currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = 0;
            }
        }

        if (multiViewList.get(position).getAccountId().equals(zoomInAccount)) {
            // do noting
        } else {
            String tmp = "";
            tmp = zoomInAccount;
            zoomInAccount = multiViewList.get(position).getAccountId();
            if (tmp.equals(shareDocZoomInOrOut)) {
                setSurfaceViewParams(shareDocView, false);
            } else {
                setSurfaceViewParams(getItemView(tmp), false);
            }
            setSurfaceViewParams(multiViewList.get(position), true);
            setPageShow(currentPage);
            updateDotsShow();
        }
        return;
    }


    public void flingPageUp() {
        CustomLog.d("MultiSpeakerView", "flingPageUp currentPage " + currentPage);
        CustomLog.d("MultiSpeakerView", "flingPageUp " + zoomInAccount + "    " + zoomOutAccount);
        /*if (multiViewList.size() <= 1) {
            CustomLog.d("MultiSpeakerView", "flingPageUp size() <= 1 ");
			return;
		}*/
        if (zoomOutAccount != null && zoomOutAccount.equals(mAccountId)) {
            CustomLog.d("MultiSpeakerView", "flingPageUp: own zoom out ");
            if (zoomInAccount != null) {
                flingPageUpByZoomIn();
                return;
            }
        }

        flingPageUpByZoomOut();
    }


    // 全屏 up 翻页
    private void flingPageDownByZoomOut() {
        int position = currentPosition;
        if (currentPosition == 0) {
            if (shareDocView != null) {
                zoomOutAccount = shareDocZoomInOrOut;
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = multiViewList.size() - 1;
            }
        } else if (currentPosition == multiViewList.size() && shareDocView != null) {
            position = multiViewList.size() - 1;
            currentPage = SINGLE_BIG_PAGE;
        } else {
            position--;
        }
        if (zoomInAccount != null &&
            multiViewList.get(position).getAccountId().equals(zoomInAccount)) {
            position--;
        }
        if (position < 0) {
            if (shareDocView != null) {
                zoomOutAccount = shareDocZoomInOrOut;
                currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = multiViewList.size() - 1;
            }
        }

        zoomOutAccount = multiViewList.get(position).getAccountId();
        setPageShow(currentPage);
        updateDotsShow();
        CustomLog.d("MultiSpeakerView",
            "flingPageDownByZoomOut " + zoomInAccount + "    " + zoomOutAccount);
    }


    private void flingPageDownByZoomIn() {
        int position = getViewPosition(zoomInAccount);
        if (zoomInAccount.equals(shareDocZoomInOrOut)) {
            position = multiViewList.size() - 1;
        } else {
            if (position == 0) {
                if (shareDocView != null) {
                    String tmp = "";
                    tmp = zoomInAccount;
                    zoomInAccount = shareDocZoomInOrOut;
                    setSurfaceViewParams(getItemView(tmp), false);
                    setSurfaceViewParams(shareDocView, true);
                    //currentPage = DATA_PAGE;
                    setPageShow(currentPage);
                    updateDotsShow();
                    return;
                } else {
                    position = multiViewList.size() - 1;
                }
            } else {
                position--;
            }
        }
        if (multiViewList.get(position).getAccountId().equals(zoomOutAccount)) {
            position--;
        }
        if (position < 0) {
            if (shareDocView != null) {
                String tmp = "";
                tmp = zoomInAccount;
                zoomInAccount = shareDocZoomInOrOut;
                setSurfaceViewParams(getItemView(tmp), false);
                setSurfaceViewParams(shareDocView, true);
                //currentPage = DATA_PAGE;
                setPageShow(currentPage);
                updateDotsShow();
                return;
            } else {
                position = multiViewList.size() - 1;
            }
        }

        if (multiViewList.get(position).getAccountId().equals(zoomInAccount)) {
            // do noting
        } else {
            String tmp = "";
            tmp = zoomInAccount;
            zoomInAccount = multiViewList.get(position).getAccountId();
            if (tmp.equals(shareDocZoomInOrOut)) {
                setSurfaceViewParams(shareDocView, false);
            } else {
                setSurfaceViewParams(getItemView(tmp), false);
            }
            setSurfaceViewParams(multiViewList.get(position), true);
            setPageShow(currentPage);
            updateDotsShow();
        }
        return;
    }


    public void flingPageDown() {
        CustomLog.d("MultiSpeakerView", "flingPageDown currentPage " + currentPage);
        CustomLog.d("MultiSpeakerView", "flingPageDown " + zoomInAccount + "    " + zoomOutAccount);
        /*if (multiViewList.size() <= 1) {
            CustomLog.d("MultiSpeakerView", "flingPageDown size() <= 1 ");
			return;
		}*/
        if (zoomOutAccount != null && zoomOutAccount.equals(mAccountId)) {
            CustomLog.d("MultiSpeakerView",
                "flingPageDown: own zoom out , not answer flingPageDown");
            if (zoomInAccount != null) {
                flingPageDownByZoomIn();
                return;
            }
        }
        flingPageDownByZoomOut();
        CustomLog.d(TAG, "flingPageDown,mMultiSpeakerViewListener.update,currentPosition:" +
            String.valueOf(currentPosition));
    }


    public void showWinM() {
        CustomLog.i(TAG, "showWinM()");

        if (checkPermission(mContext)) {
            preperFloatingWindow();
        } else if (DEFAULT_ROM_AND_SDK_VERSION_BELOW_MASHMALLOW) {
            DefaultRomUtils.showDefaultOpenPermissonHint(mContext);
        } else {
            showConfirmPermissionDialog();
        }
    }


    private void preperFloatingWindow() {
        if (mListener != null) {
            mListener.hideActivity();
        }
    }


    /**
     * 只有当 mDialog 为空且没有显示时才创建 dialog
     * 防止快速点击Dialog  造成 Dialog 无法取消
     */
    private void showConfirmPermissionDialog() {
        if ((mDialog == null) || !mDialog.isShowing()) {
            showOnceConfirmPermissionDialog();
        }
    }


    /**
     * 显示权限确认 dialog
     */
    private void showOnceConfirmPermissionDialog() {
        mDialog = new CustomDialog(mContext);
        mDialog.setTip(getResources().getString(R.string.no_floating_window_permission));
        mDialog.setCancelable(false);
        mDialog.setOkBtnText("现在开启");
        mDialog.setCancelBtnText("暂不开启");
        mDialog.setBlackTheme();
        mDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dismissDialog();
                CustomLog.i(TAG, "Click OK");

                // 引导用户至授权界面
                applyPermission(mContext);
            }
        });
        mDialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                CustomLog.i(TAG, "Click Cancel");
                dismissDialog();
            }
        });

        mDialog.show();
    }


    /**
     * 跳转至应用授权界面
     */
    private void applyPermission(Context context) {
        CustomLog.i(TAG, "applyPermission()");
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            } else {
                //如果手机系统版本小于 23，且不是 miui \ meizu \ huawei \ 360, eg. VIVO 会走这个分支
                defaultROMPermissionApply(context);
            }
        }
        commonROMPermissionApply(context);
    }


    private void defaultROMPermissionApply(Context context) {
        DefaultRomUtils.showDefaultOpenPermissonHint(context);
    }


    private void ROM360PermissionApply(final Context context) {
        CustomLog.i(TAG, "ROM360PermissionApply()");
        QikuUtils.goto360ApplyPermissionActivity(context);
    }


    private void huaweiROMPermissionApply(final Context context) {
        CustomLog.i(TAG, "huaweiROMPermissionApply()");
        HuaweiUtils.gotoHuaweiApplyPermissionActivity(context);
    }


    private void meizuROMPermissionApply(final Context context) {
        CustomLog.i(TAG, "meizuROMPermissionApply()");

        MeizuUtils.gotoMeizuApplyPermissionActivity(context);
    }


    private void applySpecialROMPermission(final Context context) {
        CustomLog.i(TAG, "applySpecialROMPermission()");

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Field field = clazz.getDeclaredField(
                    "ACTION_MANAGE_OVERLAY_PERMISSION");

                Intent intent = new Intent(field.get(null).toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);

            } catch (Exception e) {
                CustomLog.e(TAG, Log.getStackTraceString(e));
                CustomToast.show(context,
                    context.getResources().getString(R.string.default_no_floating_window_permission_hint),
                    CustomToast.LENGTH_LONG);
            }
        }
    }


    private void miuiROMPermissionApply(final Context context) {
        CustomLog.i(TAG, "miuiROMPermissionApply()");
        MiuiUtils.applyMiuiPermission(context);
    }


    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        CustomLog.i(TAG, "commonROMPermissionApply()");

        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            applySpecialROMPermission(context);
        }
    }


    private boolean checkPermission(Context mContext) {
        CustomLog.i(TAG, "checkPermission()");
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        CustomLog.i(TAG, "current Build.VERSION.SDK_INT is : " + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(mContext);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(mContext);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(mContext);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(mContext);
            } else {
                return defaultPermissionCheck(mContext);
            }
        }
        return commonROMPermissionCheck(mContext);
    }


    private boolean defaultPermissionCheck(Context mContext) {
        CustomLog.i(TAG, "defaultPermissionCheck()");
        DEFAULT_ROM_AND_SDK_VERSION_BELOW_MASHMALLOW = true;
        return DefaultRomUtils.checkDefaultRomFloatWindowPermission(mContext);
    }


    private boolean commonROMPermissionCheck(Context context) {
        CustomLog.i(TAG, "commonROMPermissionCheck()");
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays",
                        Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);

                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            CustomLog.d(TAG,
                "If this common rom open floatingWindow permission : " + result);
            return result;
        }
    }


    private boolean huaweiPermissionCheck(Context context) {
        CustomLog.i(TAG, "huaweiPermissionCheck()");
        return HuaweiUtils.checkFloatWindowPermission(context);
    }


    private boolean miuiPermissionCheck(Context context) {
        CustomLog.i(TAG, "miuiPermissionCheck()");
        return MiuiUtils.checkFloatWindowPermission(context);
    }


    private boolean meizuPermissionCheck(Context context) {
        CustomLog.i(TAG, "meizuPermissionCheck()");
        return MeizuUtils.checkFloatWindowPermission(context);
    }


    private boolean qikuPermissionCheck(Context context) {
        CustomLog.i(TAG, "qikuPermissionCheck()");
        return QikuUtils.checkFloatWindowPermission(context);
    }


    FrameLayout imageViewBg;
    LayoutParams imageViewP;


    public void showFloatingView() {
        CustomLog.i(TAG, "showFloatingView()");
        if (isWindowManager) {
            CustomLog.d(TAG, "showFloatingView  isWindowManager " + isWindowManager);
            return;
        }
        setFloatingViewShowType(true);
        isWindowManager = true;
        mWindowManager = (WindowManager) mContext.getApplicationContext()
            .getSystemService(mContext.WINDOW_SERVICE);
        surfaceViewLayout = new FrameLayout(mContext);
        surfaceViewLayout.setBackgroundDrawable(
            getResources().getDrawable(R.drawable.meeting_room_wait_for_speak_bg));
        WindowManager.LayoutParams mWindowParams = mListener.getLayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = 0;
        mWindowParams.width = height;
        mWindowParams.height = specialHeight;
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;

        MultiSpeakerView.this.removeAllViews();
        mWindowManager.addView(surfaceViewLayout, mWindowParams);
        if (shareDocView != null) {
            //shareDocView.setSpeakerItemViewParams(1,1,0,0,0,0);
            //MultiSpeakerView.this.removeView(shareDocView);
            surfaceViewLayout.addView(shareDocView);
        }
        for (int i = 0; i < multiViewList.size(); i++) {
            multiViewList.get(i).setLayoutVisibility(false);
            CustomLog.d(TAG, "showFloatingView   sssssssssssssssssssssssssss" +
                multiViewList.get(i).getAccountId());
            //multiViewList.get(i).setSpeakerItemViewParams(1,1,0,0,0,0);
            //MultiSpeakerView.this.removeView( multiViewList.get(i));
            if (zoomInAccount != null) {
                int n = surfaceViewLayout.getChildCount() - 1;
                if (n < 0) {
                    n = 0;
                }
                if (multiViewList.get(i).getAccountId().equals(zoomInAccount)) {
                    surfaceViewLayout.addView(multiViewList.get(i));
                } else {
                    surfaceViewLayout.addView(multiViewList.get(i), n);
                }
            } else {
                surfaceViewLayout.addView(multiViewList.get(i));
            }
        }

        topLayout = new FrameLayout(mContext);
        topLayout.setBackgroundDrawable(
            getResources().getDrawable(R.color.meeting_room_bg_transparent));
        topParams = new LayoutParams(1, 1);
        topParams.width = height;
        topParams.height = specialHeight;
        topParams.leftMargin = 0;
        topParams.topMargin = 0;
        imageViewBg = new FrameLayout(mContext);
        imageViewBg.setBackgroundDrawable(
            getResources().getDrawable(R.color.meeting_room_bg_transparent));
        imageViewP = new LayoutParams(1, 1);
        imageViewP.width = (int) getResources().getDimension(R.dimen.x100);
        imageViewP.height = (int) getResources().getDimension(R.dimen.y100);
        imageViewP.rightMargin = 0;
        imageViewP.topMargin = 0;
        imageViewP.gravity = Gravity.RIGHT | Gravity.TOP;

        mImageView = new ImageView(mContext);
        mImageView.setBackgroundDrawable(
            getResources().getDrawable(R.drawable.jmeetingsdk_minimum_selector));

        LayoutParams imageParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);
        imageParams.rightMargin = (int) getResources().getDimension(R.dimen.x28);
        imageParams.topMargin = (int) getResources().getDimension(R.dimen.x28);
        imageParams.gravity = Gravity.RIGHT | Gravity.TOP;

        imageViewBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                miniFloatingView();
            }
        });
        imageViewBg.addView(mImageView, imageParams);
        surfaceViewLayout.addView(topLayout, topParams);
        surfaceViewLayout.addView(imageViewBg, imageViewP);

        if (zoomInAccount != null) {
            if (zoomInAccount.equals(shareDocZoomInOrOut)) {
                setSurfaceViewParams(shareDocView, true);
            } else {
                setSurfaceViewParams(getItemView(zoomInAccount), true);
            }
        }
        topLayout.setFocusable(true);
        topLayout.setFocusableInTouchMode(true);

        topLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int titleHeight = 0;
                if (mListener != null) {
                    titleHeight = mListener.getTitleHeight();
                }

                mRawX = event.getRawX();
                mRawY = event.getRawY();

                final int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mStartX = event.getX();
                        mStartY = event.getY();
                        moveDX = event.getX();
                        moveDY = event.getY();
                        t1 = System.currentTimeMillis();
                        t2 = 0;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        moveX += Math.abs(event.getX() - moveDX);//X轴距离
                        moveY += Math.abs(event.getY() - moveDY);//y轴距离
                        moveDX = event.getX();
                        moveDY = event.getY();
                        updateWindowPosition((int) (mRawX - mStartX),
                            (int) (mRawY - mStartY - titleHeight));
                        break;

                    case MotionEvent.ACTION_UP:
                        mEndX = event.getX();
                        mEndY = event.getY();
                        t2 = System.currentTimeMillis();
                        mListener.setIsShowWindow(false);
                        if ((t2 - t1) > getTapTimeout() && (moveX > 20 || moveY > 20)) {
                            int wholeY = 0;
                            if (width > height) {
                                wholeY = width;
                            } else {
                                wholeY = height;
                            }
                            if ((mRawY - mEndY) <=
                                (wholeY / 2 - (int) getResources().getDimension(R.dimen.y202))) {
                                updateWindowPosition(0, 0);
                            } else {
                                updateWindowPosition(0, wholeY);
                            }
                        } else {
                            release();
                            MeetingManager.getInstance()
                                .joinMeeting(MeetingManager.getInstance().getToken(),
                                    mAccountId, MeetingManager.getInstance().getAccountName(),
                                    MeetingManager.getInstance().getMeetingId());
                        }
                        t1 = 0;
                        t2 = 0;
                        break;
                }
                return true;
            }
        });
        //mWindowManager.addView(surfaceViewLayout, mWindowParams);
        setPageShow(currentPage);
    }


    public void hideFloatingView() {
        CustomLog.d(TAG, "hideFloatingView");
        if (!isWindowManager && !isMiniWindowManager) {
            CustomLog.d(TAG, "hideFloatingView isWindowManager " + isWindowManager);
            return;
        }
        setFloatingViewShowType(false);
        isWindowManager = false;
        isMiniWindowManager = false;
        release();
        MultiSpeakerView.this.setLayoutParams(new LayoutParams(width, height));
        if (shareDocView != null) {
            MultiSpeakerView.this.addView(shareDocView);
        }
        for (int i = 0; i < multiViewList.size(); i++) {
            multiViewList.get(i).setLayoutVisibility(true);
            CustomLog.d(TAG, "hideFloatingView   sssssssssssssssssssssssssss" +
                multiViewList.get(i).getAccountId());
            if (zoomInAccount != null) {
                int n;
                if (surfaceViewLayout != null) {
                    n = surfaceViewLayout.getChildCount() - 1;
                    if (n < 0) {
                        n = 0;
                    }
                } else {
                    n = 0;
                }
                if (multiViewList.get(i).getAccountId().equals(zoomInAccount)) {
                    MultiSpeakerView.this.addView(multiViewList.get(i));
                } else {
                    MultiSpeakerView.this.addView(multiViewList.get(i), n);
                }
            } else {
                MultiSpeakerView.this.addView(multiViewList.get(i));
            }
        }
        if (zoomOutAccount != null) {
            if (zoomOutAccount.equals(shareDocZoomInOrOut)) {
                setSurfaceViewTopOrNot(shareDocView, false);
            } else {
                setSurfaceViewTopOrNot(getItemView(zoomOutAccount), false);
            }
        }
        if (zoomInAccount != null) {
            if (zoomInAccount.equals(shareDocZoomInOrOut)) {
                setSurfaceViewParams(shareDocView, true);
            } else {
                setSurfaceViewParams(getItemView(zoomInAccount), true);
            }
        }
        setPageShow(currentPage);
        updateDotsShow();
    }


    private ObjectAnimator anim;
    private boolean isAnimatGoOn = false;
    private TextView mTextView;


    public void miniFloatingView() {
        CustomLog.d(TAG, "miniFloatingView");
        int lineHigh = 0;
        if (mListener != null) {
            lineHigh = mListener.getTitleHeight();
            CustomLog.d(TAG, "miniFloatingView lineHigh " + lineHigh);
        }
        isWindowManager = false;
        isMiniWindowManager = true;
        if (surfaceViewLayout != null) {
            surfaceViewLayout.setBackgroundDrawable(null);
            onlyShowAudio();
            surfaceViewLayout.removeView(topLayout);
            surfaceViewLayout.removeView(imageViewBg);
        }
        if (mWindowManager != null) {
            mWindowManager.removeViewImmediate(surfaceViewLayout);
        }
        mTextView = new TextView(mContext);
        if (MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)){
            mTextView.setText(mContext.getString(R.string.touch_return_meeting_room_activity));

            CustomLog.d(TAG,"android.os.Build.MODEL:"+String.valueOf(Build.MODEL)
                    +" & lineHigh:"+String.valueOf(lineHigh));

        }else {
            mTextView.setText(mContext.getString(R.string.touch_return_meeting));
        }

        float value = density.scaledDensity;
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
            (int) getResources().getDimension(R.dimen.x27));
        mTextView.setTextColor(Color.WHITE);
        miniLayout = new LinearLayout(mContext);
        miniLayout.setBackgroundDrawable(getResources().getDrawable(R.color.meeting_room_minimum));
        //miniLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.jmeetingsdk_minimum_selector));
        miniLayout.setOrientation(LinearLayout.HORIZONTAL);

        LayoutParams lllp = new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT);
        lllp.gravity = Gravity.CENTER;
        //miniLayout.setLayoutParams(lllp);

        miniLayout.setGravity(Gravity.CENTER);
        miniLayout.addView(mTextView);

        LayoutParams miniParams = new LayoutParams(1, 1);
        miniParams.width = height;
        miniParams.height = lineHigh;
        miniLayout.setFocusable(true);
        miniLayout.setFocusableInTouchMode(true);
        miniLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                release();
                //MeetingManager.getInstance().joinMeeting(MeetingManager.getInstance().getToken(),
                //		mAccountId,MeetingManager.getInstance().getAccountName(),
                //		MeetingManager.getInstance().getMeetingId());
                isMiniWindowManager = false;
                showFloatingView();
                mListener.hideTitleBar(true);
                isAnimatGoOn = false;
                if (anim != null) {
                    anim.cancel();
                    anim = null;
                }
            }
        });
        surfaceViewLayout.addView(miniLayout, miniParams);
        WindowManager.LayoutParams mWindowParams = mListener.getLayoutParams();
        //TODO need test   not quite sure
        //CustomToast.show(mContext,"api :"+Build.VERSION.SDK_INT,CustomToast.LENGTH_LONG);
        // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
        mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //} else {
        // mWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        //TODO 8.0
        //}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        //	mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        //}
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = 0;
        mWindowParams.width = height;
        mWindowParams.height = lineHigh;

        mWindowManager.addView(surfaceViewLayout, mWindowParams);
        mListener.hideTitleBar(true);
        setTextViewAnimation();
        isAnimatGoOn = true;
    }


    private void setTextViewAnimation() {
        anim = ObjectAnimator.ofFloat(mTextView, "alpha", 0.0f, 1f);
        anim.setDuration(1000);// 动画持续时间
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }


            @Override
            public void onAnimationEnd(Animator animator) {
                CustomLog.d(TAG, " sssssssssssss onAnimationEnd  " + isAnimatGoOn);
                //CustomToast.show(mContext,"onAnimationEnd "+isAnimatGoOn, CustomToast.LENGTH_LONG);
                if (isAnimatGoOn) {
                    anim.cancel();
                    mTextView.clearAnimation();
                    anim = null;
                    setTextViewAnimation();
                }
            }


            @Override
            public void onAnimationCancel(Animator animator) {

            }


            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        anim.setRepeatCount(1);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.start();
    }


    private void updateWindowPosition(int x, int y) {
        if (mListener != null) {
            WindowManager.LayoutParams layoutParams = mListener.getLayoutParams();
            /*layoutParams.x = (int) (mRawX - mStartX);
            layoutParams.y = (int) (mRawY - mStartY-72);*/
            layoutParams.x = x;
            layoutParams.y = y;
            mWindowManager.updateViewLayout(surfaceViewLayout, layoutParams);
        }

    }


    public void release() {
        if (mWindowManager != null && surfaceViewLayout != null) {
            CustomLog.d(TAG, " sssssssssssss release " + isWindowManager);
            surfaceViewLayout.removeAllViews();
            mWindowManager.removeViewImmediate(surfaceViewLayout);
            mWindowManager = null;
            surfaceViewLayout = null;
        }
    }


    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }


    public void setFloatingViewShowType(boolean floatingViewShow) {
        SharedPreferences setting = mContext.getApplicationContext()
            .getSharedPreferences("floatingViewShowType", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean("type", floatingViewShow);
        editor.commit();
    }

    public void hanleChageSelfPreview(int cameraInfo, VideoParameter parameter){
        if(cameraInfo == mButelOpenSDK.NO_CAMERA){
            CustomLog.d(TAG, " hanleChageSelfPreview cameraInfo " + cameraInfo);
            return;
        }

        //处理Mate9画面分离、位置错乱问题
        if(isWindowManager) {
            if (getItemView(mAccountId) != null) {
                surfaceViewLayout.removeView(getItemView(mAccountId));
                addNewViewToShow(getItemView(mAccountId));
            }
        }

        if(getItemView(mAccountId)!=null){
            selfPreViewProportionX = (float) parameter.getCapWidth()/ (float)parameter.getCapHeight();
            selfPreViewProportionY= (float)parameter.getCapHeight()/ (float)parameter.getCapWidth();
            setPageShow(currentPage);
        }

    }
}
