package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.NotifyType;
import cn.redcdn.util.MResource;

public abstract class EpisodeView extends BaseView {
  private RelativeLayout waittingView;
  private RelativeLayout recordingView;
  private ImageView waittingImg;
  private TextView recordTxt;
  private ImageView recordImg;
  private ImageView recordBtn;
  private Context mContext;
  private TimeCount time;
  private AnimationDrawable animation;
  private LinearLayout closeLayout;

  public EpisodeView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_episode_view" ));

    mContext = context;
    // waittingView = (RelativeLayout) findViewById(R.id.waitting_layout);
    recordingView = (RelativeLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"recording_layout" ));
    // waittingImg = (ImageView) findViewById(R.id.waitting_img);
    recordTxt = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"recording_txt" ));
    recordImg = (ImageView) findViewById(MResource.getIdByName(mContext, MResource. ID,"recording_img" ));
    recordBtn = (ImageView) findViewById(MResource.getIdByName(mContext, MResource. ID,"recording_btn" ));
    closeLayout = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"close_layout" ));
    closeLayout.setOnClickListener(btnOnClickListener);

    recordImg.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"jmeetingsdk_episode_record_btn_animation" ));
    animation = (AnimationDrawable) recordImg.getBackground();
  }

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v.getId() == MResource.getIdByName(mContext, MResource. ID,"close_layout" )) {
        CustomLog.e("EpisodeView ", "recording_btn click");
        EpisodeView.this.onClick(v);
      }
    }
  };

  /*
   * public void askForEpisode() { waittingView.setVisibility(View.VISIBLE);
   * recordingView.setVisibility(View.INVISIBLE); Animation animation =
   * AnimationUtils.loadAnimation(mContext, R.anim.loading_animation);
   * waittingImg.startAnimation(animation); }
   */

  public void statRecord() {
    // waittingView.setVisibility(View.INVISIBLE);
    // recordingView.setVisibility(View.VISIBLE);
    //
    time = new TimeCount(30000, 1000);// 构造CountDownTimer对象
    time.start();// 开始计时
    animation.start();
    // 3个图片切换动画
  }

  public synchronized void stopRecord() {
    if (time != null) {
      time.cancel();
      time = null;
    }
    if (animation != null && animation.isRunning()) {
      animation.stop();
    }
  }

  /* 定义一个倒计时的内部类 */
  class TimeCount extends CountDownTimer {
    public TimeCount(long millisInFuture, long countDownInterval) {
      super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
    }

    @Override
    public void onFinish() {// 计时完毕时触发
      onNotify(NotifyType.ASK_FOR_STOP_EPISODE, null);
    }

    @Override
    public void onTick(long millisUntilFinished) {// 计时过程显示
      if (recordTxt != null)
        if (millisUntilFinished < 10000) {
          recordTxt
              .setText(mContext.getString(R.string.getting_in_word) + " " + millisUntilFinished / 1000 + mContext.getString(R.string.second));
        } else {
          recordTxt.setText(mContext.getString(R.string.getting_in_word)+ "   ");
        }
    }
  }
}
