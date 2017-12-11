package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.vo.Speaker;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.MResource;

public class SwitchVideoModeListViewAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Speaker> mParticipatorsList;
	private String mAccountId;
	private Context mContext;

	public SwitchVideoModeListViewAdapter(Context context,
			List<Speaker> participatorsMap, String accountId) {
		mInflater = LayoutInflater.from(context);
		mParticipatorsList = participatorsMap;
		mAccountId = accountId;
		mContext = context;
	}

	@Override
	public int getCount() {
		return mParticipatorsList.size();
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
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(MResource.getIdByName(mContext,
					MResource.LAYOUT, "switch_video_mode_item"), null);
			viewHolder.participatorResolution = (TextView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource.ID,
							"change_video_resolution"));
			viewHolder.participatorName = (TextView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource.ID,
							"change_video_name"));
			viewHolder.videolMode = (ImageView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource.ID,
							"image_change_video"));
			viewHolder.line = (LinearLayout) convertView.findViewById(MResource
					.getIdByName(mContext, MResource.ID, "change_video_line"));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Speaker personItem = mParticipatorsList.get(position);
		CustomLog
				.e("SwitchVideoModeListViewAdapter", personItem.toString());
		if (personItem != null) {
			int n = mParticipatorsList.size() - 1;
			if (n >= 0 && position == n) {
				viewHolder.line.setVisibility(View.GONE);
			} else {
				viewHolder.line.setVisibility(View.VISIBLE);
			}
			if (personItem.getSpeakType() == 1) {
				// 文档分享者
				CustomLog.e("SwitchVideoModeListViewAdapter",mAccountId+ " getScreenShareStatus() "+personItem.getScreenShareStatus());
				// viewHolder.line.setVisibility(View.GONE);
				viewHolder.participatorName.setText(R.string.screen_share);
				int[] r = personItem.getDocVideoResolution();
				if (r != null && r.length == 2)
					viewHolder.participatorResolution
							.setText(r[0] + "*" + r[1]);
				if (personItem.getmDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OPEN) {
					viewHolder.videolMode
							.setBackgroundResource(MResource.getIdByName(
									mContext, MResource.DRAWABLE,
									"jmeetingsdk_change_mode_video_open_selector"));
				} else {
					viewHolder.videolMode.setBackgroundResource(MResource
							.getIdByName(mContext, MResource.DRAWABLE,
									"jmeetingsdk_change_mode_video_close_selector"));
				}
			} else {
				// 普通发言者
				// viewHolder.line.setVisibility(View.VISIBLE);
				int[] r = personItem.getVideoResolution();
				if (r != null && r.length == 2)
					viewHolder.participatorResolution
							.setText(r[0] + "*" + r[1]);
				if(personItem.getAccountName()==null||personItem.getAccountName().equals("")){
					viewHolder.participatorName.setText(R.string.unnamed);
				}else{
				viewHolder.participatorName.setText(CommonUtil
						.getLimitSubstring(personItem.getAccountName(), 6));
				}
				if (mAccountId != null
						&& mAccountId.equals(personItem.getAccountId())) {
					//viewHolder.participatorName.setText("我");
					CustomLog.e("SwitchVideoModeListViewAdapter",mAccountId+ " getmCamStatus() "+personItem.getmCamStatus());
					if (personItem.getmCamStatus() == SpeakerInfo.CAM_STATUS_OPEN) {
						viewHolder.videolMode
								.setBackgroundResource(MResource.getIdByName(
										mContext, MResource.DRAWABLE,
										"jmeetingsdk_change_mode_video_open_selector"));
					} else {
						viewHolder.videolMode.setBackgroundResource(MResource
								.getIdByName(mContext, MResource.DRAWABLE,
										"jmeetingsdk_change_mode_video_close_selector"));
					}
				} else {					
					if (personItem.getmVideoStatus() == SpeakerInfo.VIDEO_OPEN&&personItem.getmCamStatus() == SpeakerInfo.CAM_STATUS_OPEN) {
						viewHolder.videolMode
								.setBackgroundResource(MResource.getIdByName(
										mContext, MResource.DRAWABLE,
										"jmeetingsdk_change_mode_video_open_selector"));
					} else {
						viewHolder.videolMode.setBackgroundResource(MResource
								.getIdByName(mContext, MResource.DRAWABLE,
										"jmeetingsdk_change_mode_video_close_selector"));
					}
				}
			}

		}
		return convertView;
	}

	private static class ViewHolder {
		TextView participatorName;
		TextView participatorResolution;
		ImageView videolMode;
		LinearLayout line;
	}
}
