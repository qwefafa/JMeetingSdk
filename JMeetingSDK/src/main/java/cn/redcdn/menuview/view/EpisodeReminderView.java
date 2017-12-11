package cn.redcdn.menuview.view;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.constconfig.EpisodeData;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public abstract class EpisodeReminderView extends BaseView {

  private final String TAG = EpisodeReminderView.class.getSimpleName();

  private Cmd newUser;
  private Cmd removeUser;
  private Context mContext;
  // private GridView mViewList = null;
  private TextView txt;
  private String info;
  private List<EpisodeData> itemInfoList = new ArrayList<EpisodeData>();
  private int NAME_MAX_COUNT = 6;

  // private EpisodeReminderListAdapter adapter;

  public synchronized void setInfoList(List<EpisodeData> list) {
    if (list != null) {
      itemInfoList.clear();
      itemInfoList.addAll(list);
      String name = "";
      info = "";
      for (int i = 0; i < itemInfoList.size(); i++) {
        name = itemInfoList.get(i).getAccountName();
        CustomLog.d(TAG, "itemInfoList " + i + ": " + name);
        info = info + getLimitSubstring(name);
      }
      txt.setText(info);
    }
  }

  private String getLimitSubstring(String inputStr) {
    char[] ch = inputStr.toCharArray();
    int varlength = 0;
    for (int i = 0; i < ch.length; i++) {
      if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)
          || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {
        varlength = varlength + 2;
      } else {
        varlength++;
      }
      if (varlength > NAME_MAX_COUNT) {
        return inputStr.substring(0, i) + "...  ";
      }
    }
    return inputStr + "  ";
  }

  public EpisodeReminderView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_episode_reminder" ));
    mContext = context;
    txt = (TextView) findViewById(MResource.getIdByName(mContext, MResource. ID,"episode_text" ));
    // mViewList = (GridView) findViewById(R.id.episode_play_list);
    // itemInfoList = new ArrayList<String>();
    // adapter = new EpisodeReminderListAdapter(mContext, itemInfoList, 10);
    // mViewList.setAdapter(adapter);
    // mViewList.setNumColumns(10);
  }
}
