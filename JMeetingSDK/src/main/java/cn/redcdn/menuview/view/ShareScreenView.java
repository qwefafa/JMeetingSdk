package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public abstract class ShareScreenView extends BaseView {
  private Button closeButton;
  private FrameLayout framLayout;
  private TextView txt;
  private Context mContext;

  public ShareScreenView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_share_screen_view" ));
    mContext=context;
    framLayout = (FrameLayout) findViewById(MResource.getIdByName(mContext, MResource. ID,"share_screen_frameLayout" ));
    closeButton = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"share_screen_close_btn" ));
    txt = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"share_screen_txt" ));
    closeButton.setOnClickListener(btnOnClickListener);
    initView(context);
  }

  private void initView(Context context) {
    int height = 0;
    int width = 0;
    float height_multiple = 0.413f;
    float width_multiple = 0.66875f;
    WindowManager wm = (WindowManager) getContext().getSystemService(
        Context.WINDOW_SERVICE);
    int win_width = wm.getDefaultDisplay().getWidth();
    int win_height = wm.getDefaultDisplay().getHeight();
    if (framLayout != null) {
      height = (int) (win_height * height_multiple);
      width = (int) (win_width * width_multiple);
      CustomLog.d("ShareScreenView", height + " " + width);
    }
    LayoutParams params = new LayoutParams(
        txt.getLayoutParams());
    params.leftMargin = width;
    params.topMargin = height;
    if (MeetingManager.getInstance() != null) {
      txt.setText(MeetingManager.getInstance().getAccountID());
   }
    txt.setLayoutParams(params);
  }

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v.getId() == MResource.getIdByName(mContext, MResource. ID,"share_screen_close_btn" )) {
        CustomLog.e("ShareScreenView ", "share_screen_close_btn click");
        ShareScreenView.this.onClick(v);
      }
    }
  };
}
