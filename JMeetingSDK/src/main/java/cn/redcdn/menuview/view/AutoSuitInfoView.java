package cn.redcdn.menuview.view;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

public abstract class AutoSuitInfoView extends BaseView {
  private final String TAG = AutoSuitInfoView.this.getClass().getName();
  private Button hideViewBtn;
  private Context mContext;

  private class MyComparator implements Comparator {
    public int compare(Object obj1, Object obj2) {
      AutoSuitInfo u1 = (AutoSuitInfo) obj1;
      AutoSuitInfo u2 = (AutoSuitInfo) obj2;
      if (u1.key.compareTo(u2.key) > 0) {
        return 1;
      } else if (u1.key.compareTo(u2.key) < 0) {
        return -1;
      } else {
        // 利用String自身的排序方法。
        // 如果年龄相同就按名字进行排序
        return 0;
      }
    }
  }

  public class AutoSuitInfo {
    public String key;
    public String value;
  }

  private ListView mAutoSuitInfoListView;
  private AutoSuitInfoListViewAdapter mAutoSuitInfoListViewAdapter;

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
          "hide_view_btn")) {
        AutoSuitInfoView.this.onClick(v);
      }

    }
  };

  private Timer mTimer = null;
  private TimerTask mTimerTask = null;

  private Handler refreshHandler = new Handler() {
    @SuppressWarnings("unchecked")
    public void handleMessage(android.os.Message msg) {

      // String autoAdapter = SmSdkJNI.GetAdaptQosInfo();
      String autoAdapter = null;
      CustomLog.d(TAG, "调用自适应接口结果：" + autoAdapter);
      if (autoAdapter == null || autoAdapter.trim().equals("")) {
        CustomLog.d(TAG, "获取到的字符串为空");
        return;
      }

      List<AutoSuitInfo> infoList = new ArrayList<AutoSuitInfo>();
      try {
        JSONObject jsonObject = new JSONObject(autoAdapter);
        Iterator<?> it = jsonObject.keys();
        while (it.hasNext()) {
          AutoSuitInfo autoSuitInfo = new AutoSuitInfo();
          autoSuitInfo.key = it.next().toString(); // TODO
          autoSuitInfo.value = jsonObject.optString(autoSuitInfo.key);// TODO
          infoList.add(autoSuitInfo);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      Collections.sort(infoList, new MyComparator());

      mAutoSuitInfoListViewAdapter.setSource(infoList);
      mAutoSuitInfoListViewAdapter.notifyDataSetChanged();
    };
  };

  public AutoSuitInfoView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,
        "jmeetingsdk_menu_auto_suit_info_view"));
    mContext = context;
    mAutoSuitInfoListViewAdapter = new AutoSuitInfoListViewAdapter(context);

    initView();
  }

  private void initView() {
    hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext,
        MResource.ID, "hide_view_btn"));
    hideViewBtn.setOnClickListener(btnOnClickListener);
    mAutoSuitInfoListView = (ListView) findViewById(MResource.getIdByName(
        mContext, MResource.ID,
        "meeting_room_menu_auto_suit_info_view_list_view"));
    mAutoSuitInfoListView.setAdapter(mAutoSuitInfoListViewAdapter);
  }

  @Override
  public void show() {
    startTimer();
    super.show();
  }

  @Override
  public void dismiss() {
    stopTimer();
    super.dismiss();
  }

  private void startTimer() {
    mTimer = new Timer();
    mTimerTask = new TimerTask() {
      @Override
      public void run() {
        refreshHandler.sendEmptyMessage(0);
      }
    };
    mTimer.schedule(mTimerTask, 1, 5000);
  }

  private void stopTimer() {
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
    if (mTimerTask != null) {
      mTimerTask.cancel();
      mTimerTask = null;
    }
  }

  public void release() {
    stopTimer();
    refreshHandler.removeMessages(0);
  }

  private static class ViewHolder {
    TextView accountName;
    TextView accountId;
  }

  private class AutoSuitInfoListViewAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<AutoSuitInfo> mAutoSuitInfoListData;

    public AutoSuitInfoListViewAdapter(Context context) {
      mInflater = LayoutInflater.from(context);
    }

    public void setSource(List<AutoSuitInfo> autoSuitInfoListData) {
      mAutoSuitInfoListData = autoSuitInfoListData;
    }

    @Override
    public int getCount() {
      return mAutoSuitInfoListData == null ? 0 : mAutoSuitInfoListData.size();
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder viewHolder = null;
      if (convertView == null) {
        convertView = mInflater.inflate(MResource.getIdByName(mContext,
            MResource.LAYOUT, "jmeetingsdk_menu_item_auto_suit_info"), null);

        viewHolder = new ViewHolder();
        viewHolder.accountName = (TextView) convertView.findViewById(MResource
            .getIdByName(mContext, MResource.ID, "participatorName"));
        viewHolder.accountId = (TextView) convertView.findViewById(MResource
            .getIdByName(mContext, MResource.ID, "participatorAccountId"));
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }

      viewHolder.accountName.setText(mAutoSuitInfoListData.get(position).key);
      viewHolder.accountId.setText(mAutoSuitInfoListData.get(position).value);
      return convertView;
    }
  }
}