package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import cn.redcdn.util.MResource;

public abstract class AskForStopSpeakView extends BaseView {
  private Button mHideViewBtn;
  private Button mGiveMicBtn;
  private Button mStopSpeakBtn;
  private Context mContext;

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      int id = v.getId();
      if (id == MResource.getIdByName(mContext, MResource.ID,"hide_view_btn")) {
        AskForStopSpeakView.this.onClick(v);
      } else if (id == MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_ask_for_stop_speak_view_give_mic_btn")){
        AskForStopSpeakView.this.onClick(v);
      } else if (id == MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_ask_for_stop_speak_view_stop_speak_btn")){
    	  
        AskForStopSpeakView.this.onClick(v);
      }
    }
  };

  public AskForStopSpeakView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_ask_for_stop_speak_view"));
    mContext = context;
    mHideViewBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID,"hide_view_btn"));
    mHideViewBtn.setOnClickListener(btnOnClickListener);

    mGiveMicBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_ask_for_stop_speak_view_give_mic_btn"));
    mGiveMicBtn.setOnClickListener(btnOnClickListener);

    mStopSpeakBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_ask_for_stop_speak_view_stop_speak_btn"));
    mStopSpeakBtn.setOnClickListener(btnOnClickListener);

  }
  

  
}