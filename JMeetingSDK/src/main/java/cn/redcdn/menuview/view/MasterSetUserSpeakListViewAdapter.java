package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.List;

import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.MResource;
import cn.redcdn.util.RoundImageView;

public class MasterSetUserSpeakListViewAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Person> mParticipatorsList;
	private String mAccountId;
	private Context mContext;
	private MeetingDisplayImageListener mMeetingDisplayImageListener = null;
	private Animation hyperspaceJumpAnimation;

	public MasterSetUserSpeakListViewAdapter(Context context,
			List<Person> participatorsMap, String accountId) {
		mInflater = LayoutInflater.from(context);
		mParticipatorsList = participatorsMap;
		mAccountId = accountId;
		mContext = context;
		mMeetingDisplayImageListener = new MeetingDisplayImageListener(context);
		hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mContext,
				MResource.getIdByName(mContext, MResource.ANIM,"jmeetingsdk_loading_animation" ));
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

	private void show(RoundImageView roundImageView, String imgUrl) {
		CustomLog
				.e("MasterSetUserSpeakListViewAdapter", "ssssssssss " + imgUrl);
		ImageLoader imageLoader = ImageLoader.getInstance();

		imageLoader.displayImage(imgUrl, roundImageView,
				 displayImageOpt(),
				mMeetingDisplayImageListener);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(
					MResource.getIdByName(mContext, MResource.LAYOUT,"jmeetingsdk_menu_item_master_set_users" ), null);
			viewHolder.participatorAccountId = (TextView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_set_speaker_account_id" ));
			viewHolder.participatorName = (TextView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_set_speaker_name" ));
			viewHolder.loadingProgressBar = (ImageView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource. ID,"masterListLoadingProgressBar" ));
			viewHolder.setSpeaker = (TextView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_set_speaker_btn" ));
			viewHolder.participatorImg = (RoundImageView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_set_speaker_img" ));
			viewHolder.spreakerType = (ImageView) convertView
					.findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_set_speaker_type_img" ));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Person personItem = mParticipatorsList.get(position);
		CustomLog.e("dddddddddddddddd", personItem.getPhoto());
		if (personItem != null) {
			viewHolder.loadingProgressBar.setAnimation(hyperspaceJumpAnimation);
			viewHolder.participatorAccountId.setText(personItem.getAccountId());
			if (personItem.getName() == null
					|| personItem.getName()
							.equals("")) {
				viewHolder.participatorName.setText(personItem.getAccountId());
			} else {
				viewHolder.participatorName.setText(CommonUtil
						.getLimitSubstring(personItem.getName(), 10));
			}
			boolean isLoading = personItem.isWaitingResult();
			if (isLoading) {
				viewHolder.loadingProgressBar.setVisibility(View.VISIBLE);
				viewHolder.loadingProgressBar
						.startAnimation(hyperspaceJumpAnimation);
				viewHolder.setSpeaker.setVisibility(View.GONE);
			} else {
				viewHolder.loadingProgressBar.clearAnimation();
				viewHolder.loadingProgressBar.setVisibility(View.GONE);
				viewHolder.setSpeaker.setVisibility(View.VISIBLE);
			}
			if (personItem.getStatusType() == 0) {
				viewHolder.setSpeaker.setText(mContext.getString(R.string.cancel_speak));
				viewHolder.spreakerType
						.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"jmeetingsdk_menu_master_view_speak_img" ));
				viewHolder.spreakerType.setVisibility(View.VISIBLE);
			} else if (personItem.getStatusType() == 1) {
				
				if(MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_TV)
						&&MainView.isMasterRole==true){
					viewHolder.setSpeaker.setText(mContext.getString(R.string.command_speak));
				}else{
					viewHolder.setSpeaker.setText(mContext.getString(R.string.ask_speak));
				}
				
				viewHolder.spreakerType
						.setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE,"jmeetingsdk_menu_master_view_hang_up_img" ));
				viewHolder.spreakerType.setVisibility(View.VISIBLE);
			} else {
				
				if(MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_TV)
						&&MainView.isMasterRole==true){
					viewHolder.setSpeaker.setText(mContext.getString(R.string.command_speak));
				}else{
					viewHolder.setSpeaker.setText(mContext.getString(R.string.ask_speak));
				}
				
				viewHolder.spreakerType.setVisibility(View.INVISIBLE);
			}
		}
		show(viewHolder.participatorImg, personItem.getPhoto());
		return convertView;
	}

	private static class ViewHolder {
		TextView participatorName;
		TextView participatorAccountId;
		TextView setSpeaker;
		ImageView spreakerType;
		RoundImageView participatorImg;
		ImageView loadingProgressBar;
	}
	
	private DisplayImageOptions displayImageOpt() {

		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showStubImage(
						MResource.getIdByName(mContext,
								MResource.DRAWABLE,
								"jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片在下载期间显示的图片
				.showImageForEmptyUri(
						MResource.getIdByName(mContext,
								MResource.DRAWABLE,
								"jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(
						MResource.getIdByName(mContext,
								MResource.DRAWABLE,
								"jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片加载/解码过程中错误时候显示的图片
				.cacheInMemory(true)// 是否緩存都內存中
				.cacheOnDisc(true)// 是否緩存到sd卡上
				.displayer(new RoundedBitmapDisplayer(20))// 设置图片的显示方式 : 设置圆角图片
															// int //
															// // roundPixels
				.bitmapConfig(Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
				.delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
				.build();
		return options;
	}
}
