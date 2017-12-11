package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public abstract class EpisodeButtonView extends BaseView {

  private Button episodeViewBtn;
  private Context mContext;
  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      int id = v.getId();
	if (id == MResource.getIdByName(mContext, MResource. ID,"menu_episode_btn" )) {
		CustomLog.e("episodeViewBtn", "插话按键点击.....");
		EpisodeButtonView.this.onClick(v);
	} else {
	}
    }
  };

  public EpisodeButtonView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_episode_button_view" ));
    mContext = context;
    episodeViewBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"menu_episode_btn" ));
    CustomLog.e("EpisodeButtonView", (episodeViewBtn == null) + "");
    episodeViewBtn.setOnClickListener(btnOnClickListener);
  }

  public void setEpisodeButtonBg(boolean isSpeak) {
    if (isSpeak) {
      CustomLog.d("EpisodeButtonView", "插话按钮置灰");     
      //episodeViewBtn.setEnabled(false);
      episodeViewBtn.setVisibility(View.INVISIBLE);
    } else {
      CustomLog.d("EpisodeButtonView", "插话按钮可用");
      //episodeViewBtn.setEnabled(true);
      //episodeViewBtn.setVisibility(View.VISIBLE);
      episodeViewBtn.setVisibility(View.INVISIBLE);
    }
  }
}
