package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public abstract class ChooseSpeakWindowItemView extends FrameLayout {
  private TextView txt;

  private ImageView img;

  private Button btn;

  private Context mContext;

  private String accountId;

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      v.setTag(accountId);
      ChooseSpeakWindowItemView.this.onClick(v);   
    }
  };

  public abstract void onClick(View v);

  public abstract void onNotify(int notifyType, Object object);

  public ChooseSpeakWindowItemView(Context context, String id, String name, boolean isSpeaking) {
    super(context);
    LayoutInflater.from(context).inflate(
        MResource.getIdByName(context, MResource.LAYOUT, "meeting_room_menu_choose_window_item"), this, true);
    //super(context,MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_choose_window_item"));
    CustomLog.d("ChooseSpeakWindowItemView", "初始化");
    mContext = context;
    accountId = id;
    txt = (TextView) findViewById(MResource.getIdByName(mContext, MResource.ID, "speak_mic_name"));
    img = (ImageView) findViewById(MResource.getIdByName(mContext, MResource.ID, "speak_mic_img"));
    btn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "speak_mic_btn"));
    btn.setOnClickListener(btnOnClickListener);
    img.setVisibility(View.GONE);
    if (isSpeaking) {
      btn.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,
          "meeting_room_menu_choose_speak_view_speak_btn_selector"));
      img.setVisibility(View.VISIBLE);
    }
    else {
      btn.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,
          "meeting_room_menu_main_view_ask_for_speak_empty_btn_selector"));
      img.setVisibility(View.GONE);
    }
    txt.setText(name);
  }

  /* public void changeMainSpeakType(boolean isShowImg){
     if(isShowImg){
       img.setVisibility(View.VISIBLE);
     }else{
       img.setVisibility(View.GONE);
     }
   }*/
  public void changeSpeakerType(String id, String name, boolean isSpeaking) {
    accountId = id;
    if (isSpeaking) {
      btn.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,
          "meeting_room_menu_choose_speak_view_speak_btn_selector"));
    }
    else {
      btn.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,
          "meeting_room_menu_main_view_ask_for_speak_empty_btn_selector"));
    }
    txt.setText(name);
  }

  public String getAccountId() {
    return accountId;
  }
}
