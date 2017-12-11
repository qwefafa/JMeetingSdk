package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.imservice.IMMessageBean;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.DanmuControl;
import cn.redcdn.util.MResource;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;

public abstract class MainButtonView extends BaseView {

    private boolean isGood = false;
    private ImageView netStatusImage;
    private Button netStatusBtn;
    private DisplayMetrics mDensity;
    private Button mainViewBtn;
    private Button chatBtn;
    private Context mContext;
    private LinearLayout mDotsLinearLayout;
    private List<FrameLayout> tv_dots_list = new ArrayList<FrameLayout>();
    private LayoutParams params;
    private FrameLayout barrageLayout;
    private boolean barrageLayoutVisibility = true;
    private DanmakuSurfaceView danmakuView;
    private DanmuControl danmuControl;
    private String mAccountId;
    private OnClickListener btnOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                    "main_view_btn")) {
                CustomLog.e("MainButtonView", "按键点击.....");
                MainButtonView.this.onClick(v);
            }
            if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
                    "chat_btn")) {
                MainButtonView.this.onClick(v);
            }
        }
    };

    public MainButtonView(Context context, DisplayMetrics density, String groupId, String accountId) {

        super(context, MResource.getIdByName(context, MResource.LAYOUT,
                "meeting_room_menu_main_button_view"));
        mContext = context;
        mDensity = density;
        mainViewBtn = (Button) findViewById(MResource.getIdByName(mContext,
                MResource.ID, "main_view_btn"));
        netStatusImage = (ImageView) findViewById(MResource.getIdByName(mContext,
                MResource.ID, "iv_net_status"));
        netStatusBtn = (Button) findViewById(MResource.getIdByName(mContext,
                MResource.ID, "iv_net_status_text"));
        chatBtn = (Button) findViewById(MResource.getIdByName(mContext,
                MResource.ID, "chat_btn"));
        barrageLayout = (FrameLayout) findViewById(MResource.getIdByName(mContext,
                MResource.ID, "fl_barrage"));
        mAccountId = accountId;
        Log.e("1111111", (mainViewBtn == null) + "");
        mainViewBtn.setOnClickListener(btnOnClickListener);
        chatBtn.setOnClickListener(btnOnClickListener);
        danmakuView = (DanmakuSurfaceView) findViewById(MResource.getIdByName(mContext, MResource.ID, "sv_danmaku"));
        danmakuView.setZOrderOnTop(true);
        danmakuView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        if (TextUtils.isEmpty(groupId)) {
            chatBtn.setVisibility(View.GONE);
            danmakuView.setVisibility(View.GONE);
        } else {
            danmuControl = new DanmuControl(mContext, danmakuView);
            chatBtn.setVisibility(View.VISIBLE);
            danmakuView.setVisibility(View.VISIBLE);
        }
        mDotsLinearLayout = (LinearLayout) findViewById(MResource.getIdByName(mContext,
                MResource.ID, "ll_dots"));
        params = new LayoutParams((int) (7 * density.density), (int) (14 * density.density));
        // layout子view的间距
//    params.setMargins(0, (int)(5 * density.density), 0, (int)(5 * density.density));
    }

    public void showBarrageLayout() {
        if (barrageLayoutVisibility) {
//            CustomToast.show(mContext,"1111,danmakuView.isPrepared():"+String.valueOf(danmakuView.isPrepared())+"danmakuView.isPaused():"+String.valueOf(danmakuView.isPaused()), Toast.LENGTH_LONG);
            barrageLayout.setVisibility(View.VISIBLE);

            if (danmakuView != null && danmakuView.isPrepared() /*&& danmakuView.isPaused()*/) {

//                danmakuView.show();
                danmakuView.resume();
            }

        } else {
//            CustomToast.show(mContext,"2222", Toast.LENGTH_LONG);
            barrageLayout.setVisibility(View.GONE);
//
//            if (danmakuView != null && danmakuView.isPrepared()) {
//                danmakuView.hide();
//                danmakuView.pause();
//                danmakuView.clear();
//            }

        }
    }

    public void hideBarrageLayout() {
        barrageLayout.setVisibility(View.GONE);

        if (danmakuView != null && danmakuView.isPrepared()) {
//            CustomToast.show(mContext,"3333", Toast.LENGTH_LONG);
//            danmakuView.hide();
            danmakuView.clear();

            danmakuView.pause();


        }

    }

    public void onMessageArrive(final IMMessageBean object) {
        CustomLog.d("MainButtonView", "onMessageArrive,msgContent:" + object.getMsgContent() + "mAccountId:" + mAccountId);
        //TODO:这里获取到了BarrageView中的消息，需要添加展示消息部分
        if (mAccountId.equals(object.getNubeNumber())) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isBarragLayoutVisible(object);
                }
            }, 2000);
        } else {
            isBarragLayoutVisible(object);
        }
    }

    private void isBarragLayoutVisible(IMMessageBean object) {
        if (barrageLayout.getVisibility() == VISIBLE&&danmuControl!=null) {
            if (TextUtils.isEmpty(object.getNickName())) {
                danmuControl.addDanmu(object.getHeadUrl(), object.getNubeNumber(), object.getMsgContent());
            } else {
                danmuControl.addDanmu(object.getHeadUrl(), object.getNickName(), object.getMsgContent());
            }
        }
    }

    public void onBarrageControlClick(boolean barrageIsOpen) {
        CustomLog.d("MainButtonView", "onBarrageControlClick barrageIsOpen:" + String.valueOf(barrageIsOpen));
        barrageLayoutVisibility = barrageIsOpen;
    }

    public void addDotTextView() {
        CustomLog.d("MainButtonView", "addDotTextView");
        ImageView tv_dot = new ImageView(mContext);
        tv_dot.setBackgroundResource(MResource.getIdByName(
                mContext, MResource.DRAWABLE, "main_button_view_dot_selector"));

        FrameLayout ll_dot = new FrameLayout(mContext);
        LayoutParams llParams = new LayoutParams((int) (7 * mDensity.density), (int) (7 * mDensity.density));
        ll_dot.setForegroundGravity(Gravity.CENTER_VERTICAL);
        ll_dot.addView(tv_dot, llParams);

        mDotsLinearLayout.addView(ll_dot, params);
        mDotsLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
        tv_dots_list.add(ll_dot);
    }

    public void removeDocTextView() {
        CustomLog.d("MainButtonView", "removeDocTextView");
        if (tv_dots_list.size() > 0) {
            FrameLayout view = tv_dots_list.get(0);
            tv_dots_list.remove(tv_dots_list.get(0));
            mDotsLinearLayout.removeView(view);
        }
    }
    private void addDotTextViews(int addSize){
        for(int i=0; i<addSize; i++){
            addDotTextView();
        }
    }
    private void removeDotTextViews(int removeSize){
        for(int i=0; i<removeSize; i++){
            removeDocTextView();
        }
    }
    public void updateDotTextView(int size,int position,boolean isShow) {
        CustomLog.d("MainButtonView", "updateDotTextView,position:" + String.valueOf(position));
        if(!isShow){
            mDotsLinearLayout.setVisibility(View.GONE);
            return;
        }else{
            mDotsLinearLayout.setVisibility(View.VISIBLE);
        }
        if(tv_dots_list.size() == size){
            //do nothing
        }else if(tv_dots_list.size() < size){
            int s = size-tv_dots_list.size();
            addDotTextViews(s);
        }else{
            int s = tv_dots_list.size()- size;
            removeDotTextViews(s);
        }
        for (int i = 0; i < tv_dots_list.size(); i++) {
            if (i == position) {
                tv_dots_list.get(i).setSelected(true);
            } else {
                tv_dots_list.get(i).setSelected(false);
            }
        }
    }

    public void setNetStatusView(int netStatus) {
        if (netStatus == 0) {
            netStatusImage.setBackgroundResource(MResource.getIdByName(mContext,
                    MResource.DRAWABLE, "signal_good_icon"));
            netStatusBtn.setBackgroundResource(MResource.getIdByName(mContext,
                    MResource.DRAWABLE, "signal_good_bg"));
            netStatusBtn.setText("  网络状态正常");
            if (isGood) {
                netStatusBtn.setVisibility(View.INVISIBLE);
            } else {
                netStatusBtn.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        netStatusBtn.setVisibility(View.INVISIBLE);
                        isGood = true;
                    }

                }, 3000);
            }
        } else if (netStatus == 1) {
            netStatusBtn.setVisibility(View.VISIBLE);
            netStatusImage.setBackgroundResource(MResource.getIdByName(mContext,
                    MResource.DRAWABLE, "signal_weak_icon"));
            netStatusBtn.setBackgroundResource(MResource.getIdByName(mContext,
                    MResource.DRAWABLE, "signal_weak_bg"));
            netStatusBtn.setText("  网络极差，已关闭视频");
            isGood = false;
        } else if (netStatus == 2) {
            netStatusBtn.setVisibility(View.VISIBLE);
            netStatusImage.setBackgroundResource(MResource.getIdByName(mContext,
                    MResource.DRAWABLE, "signal_bad_icon"));
            netStatusBtn.setBackgroundResource(MResource.getIdByName(mContext,
                    MResource.DRAWABLE, "signal_bad_bg"));
            netStatusBtn.setText("  网络极差，已关闭视频");
            isGood = false;
        } else {
//			netStatusImage.setVisibility(View.INVISIBLE);
            netStatusBtn.setVisibility(View.INVISIBLE);
            isGood = false;
        }
    }

    public void release() {
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }

}
