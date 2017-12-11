package cn.redcdn.menuview.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public class LiveShareView extends Dialog {

  private TextView wxBtn;

  private TextView friendBtn;

  private TextView smsBtn;

  private TextView copyBtn;

  private TextView txt;

  private Context mContext;

  private ImageView cancleImageView;

  private ShareBtnOnClickListener btnOnClickListener;

  private boolean isHandleEvent = false;

  private static final int IsHandleMsg = 99;

  private Handler isHandleEventhandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == IsHandleMsg) {
        isHandleEvent = false;
        CustomLog.e("LiveShareView", "200ms到时，isHandleEvent = false");
      }
    }
  };

  public interface ShareBtnOnClickListener {
    public void onClick(LiveShareView customDialog, int tag);
  }

  /**
   * 构造方法，传入activity的上下文
   * 
   * @param context
   */
  public LiveShareView(Context context) {
    this(context, MResource.getIdByName(context, MResource.STYLE, "jmetingsdk_dialog"));
  }

  public LiveShareView(Context context, int theme) {
    super(context, theme);
    mContext = context;
  }

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(MResource.getIdByName(mContext, MResource.LAYOUT, "meeting_room_live_share_dialog"));
    wxBtn = (TextView) this.findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_share_wx"));
    friendBtn = (TextView) this.findViewById(MResource
        .getIdByName(mContext, MResource.ID, "qn_operate_dialog_share_wf"));
    smsBtn = (TextView) this.findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_share_sms"));
    copyBtn = (TextView) this.findViewById(MResource
        .getIdByName(mContext, MResource.ID, "qn_operate_dialog_share_copy"));
    cancleImageView = (ImageView) this.findViewById(MResource.getIdByName(mContext, MResource.ID,
        "qn_live_share_dialog_cancle"));
    txt = (TextView) this.findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_share_body"));
    if (MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
      txt.setText(mContext.getString(R.string.share));
    }
    else {
      txt.setText(mContext.getString(R.string.share));
    }
    wxBtn.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        handleBtnClick(1);

      }
    });
    friendBtn.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        handleBtnClick(2);

      }
    });
    smsBtn.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        handleBtnClick(3);

      }
    });
    copyBtn.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        handleBtnClick(4);

      }
    });
    cancleImageView.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        handleBtnClick(5);
      }
    });
  }

  private void handleBtnClick(int tag) {
    if (isHandleEvent == true) {
      CustomLog.e("LiveShareView", "触摸过快,返回");
      return;
    }
    if (btnOnClickListener != null) {
      btnOnClickListener.onClick(LiveShareView.this, tag);
      isHandleEvent = true;
      Message msg = Message.obtain();
      msg.what = IsHandleMsg;
      isHandleEventhandler.sendMessageDelayed(msg, 200);
    }
  }

  public void setBtnOnClickListener(ShareBtnOnClickListener btnOnClickListener) {
    this.btnOnClickListener = btnOnClickListener;
  }

}
