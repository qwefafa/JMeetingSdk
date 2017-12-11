package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.menuview.NotifyType;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.MResource;

public abstract class GiveMicView extends BaseView {
  private Context mContext;
  private ListView mGiveMicListView;
  private GiveMicListViewAdapter mGiveMicListViewAdapter;
  private List<Person> mGiveMicList = new ArrayList<Person>();
  private Button hideViewBtn;

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v.getId() == MResource.getIdByName(mContext, MResource.ID,"hide_view_btn")){
        GiveMicView.this.onClick(v);
      }
    }
  };

  public GiveMicView(Context context) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT,"meeting_room_menu_give_mic_view"));
    mContext = context;

    hideViewBtn = (Button) findViewById(MResource.getIdByName(context, MResource.ID,"hide_view_btn"));
    hideViewBtn.setOnClickListener(btnOnClickListener);

    mGiveMicListViewAdapter = new GiveMicListViewAdapter(context, mGiveMicList);

    mGiveMicListView = (ListView) findViewById(MResource.getIdByName(context, MResource.ID,"meeting_room_menu_give_mic_view_listview"));

    mGiveMicListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
          long arg3) {
        if (!mGiveMicList.get(arg2).isInvited()) {
          mGiveMicList.get(arg2).setInvited(true);
          GiveMicView.this.onNotify(NotifyType.GIVE_MIC, mGiveMicList.get(arg2));
        } else {
        }
        mGiveMicListViewAdapter.notifyDataSetChanged();
      }
    });

    mGiveMicListView.setAdapter(mGiveMicListViewAdapter);
  }

  public void show(List<Person> giveMicList) {
    mGiveMicList.clear();
    mGiveMicList.addAll(giveMicList);
    mGiveMicListViewAdapter.notifyDataSetChanged();
    super.show();
  }

  public static class GiveMicListViewAdapter extends BaseAdapter {
    private final String TAG = GiveMicListViewAdapter.this.getClass().getName();
    private LayoutInflater mInflater;
    private List<Person> mParticipatorList;
    private Context mContext;

    public GiveMicListViewAdapter(Context context, List<Person> participatorList) {
      mContext = context;
      mInflater = LayoutInflater.from(context);
      mParticipatorList = participatorList;
    }

    @Override
    public int getCount() {
      return mParticipatorList.size();
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
      ViewHolder holder;
      if (convertView == null) {
        convertView = mInflater.inflate(
            MResource.getIdByName(mContext, MResource.LAYOUT,"meeting_room_menu_give_mic_view_list_item"), null);
        holder = new ViewHolder();
        holder.participatorName = (TextView) convertView
            .findViewById(MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_give_mic_view_list_item_participator_name"));

        holder.participatorAccountId = (TextView) convertView
            .findViewById(MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_give_mic_view_list_item_participator_account_id"));

        holder.participatorImg = (TextView) convertView
            .findViewById(MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_give_mic_view_list_item_participator_img"));

        holder.giveMicIcon = (TextView) convertView
            .findViewById(MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_give_mic_view_list_item_give_mic_icon"));
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      holder.participatorName.setText(mParticipatorList.get(position)
          .getAccountName());
      holder.participatorAccountId.setText(mParticipatorList.get(position)
          .getAccountId());
      if (holder.participatorName.getText().toString() == null
          || holder.participatorName.getText().toString().equals("")) {
        holder.participatorName.setText(mContext.getString(R.string.unnamed));
      }

      boolean isSelected = mParticipatorList.get(position).isSelected();
      boolean isInvited = mParticipatorList.get(position).isInvited();
      if (mParticipatorList.get(position).getPhoto() == null
          || mParticipatorList.get(position).getPhoto().equals("")) {
        holder.participatorImg
            .setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"meeting_room_menu_person_default_icon"));
      }
      if (isSelected && isInvited) {
        holder.giveMicIcon.setText(mContext.getString(R.string.give_mic));
        holder.giveMicIcon.setTextColor(Color.argb(255, 51, 181, 229));
      } else if (isSelected && !isInvited) {
        holder.giveMicIcon.setText(mContext.getString(R.string.give_mic));
        holder.giveMicIcon.setTextColor(Color.argb(255, 51, 181, 229));
      } else if (!isSelected && isInvited) {
        holder.giveMicIcon.setText(mContext.getString(R.string.give_mic));
        holder.giveMicIcon.setTextColor(Color.argb(255, 51, 181, 229));
      } else {
      }
      return convertView;
    }

    static class ViewHolder {
      TextView participatorName;
      TextView participatorAccountId;
      TextView giveMicIcon;
      TextView participatorImg;
    }
  }
}
