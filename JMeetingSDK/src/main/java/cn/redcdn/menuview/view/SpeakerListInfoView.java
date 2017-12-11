package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.util.MResource;

public abstract class SpeakerListInfoView extends BaseView {
	// private TextView master;
	private final String TAG = "SpeakerListInfoView";
	private List<TextView> textList;
	private Context mContext;
	private String mAccountId;
	private ButelOpenSDK mButelOpenSDK;
	private MenuView mMenuView;
	private List<SpeakerInfo> mList = new ArrayList<SpeakerInfo>();

	public SpeakerListInfoView(Context context, ButelOpenSDK butelOpenSDK,
			MenuView menuView, String accountId) {
		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"meeting_room_menu_speaker_info_reminder"));
		mContext = context;
		mButelOpenSDK = butelOpenSDK;
		mMenuView = menuView;
		mAccountId = accountId;
		mButelOpenSDK
				.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
		textList = new ArrayList<TextView>();
		TextView speaker1 = (TextView) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "master_view"));
		TextView speaker2 = (TextView) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "speaker_view_1"));
		TextView speaker3 = (TextView) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "speaker_view_2"));
		TextView speaker4 = (TextView) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "speaker_view_3"));
		textList.add(speaker1);
		textList.add(speaker2);
		textList.add(speaker3);
		textList.add(speaker4);
		setAllViewInvisiable();
	}

	private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener = new ButelOpenSDKNotifyListener() {
		@Override
		public void onNotify(int arg0, Object arg1) {
			Cmd cmd = null;
			switch (arg0) {
			case NotifyType.SPEAKER_OFF_LINE:
				cmd = (Cmd) arg1;
				if (cmd == null)
					return;
				handleRemoveSpeaker(cmd.getAccountId());
				setSpeakerListInfoViewShow();
				break;
			case NotifyType.STOP_SPEAK:
				handleRemoveSpeaker(mAccountId);
				setSpeakerListInfoViewShow();
				break;
			case NotifyType.SERVER_NOTICE_START_SCREEN_SHAREING:
			case NotifyType.SERVER_NOTICE_STOP_SCREEN_SHAREING:
			case NotifyType.SPEAKER_ON_LINE:
				cmd = (Cmd) arg1;
				if (cmd == null)
					return;
				handleRemoveSpeaker(cmd.getAccountId());
				handleAddSpeaker(cmd.getAccountId());
				setSpeakerListInfoViewShow();
				break;
			case NotifyType.START_SHARE_DOC:
			case NotifyType.STOP_SHARE_DOC:
			case NotifyType.START_SPEAK:
				handleRemoveSpeaker(mAccountId);
				handleAddSpeaker(mAccountId);
				setSpeakerListInfoViewShow();
				break;
			default:
				break;
			}
		}
	};

	private void setAllViewInvisiable() {
		if (textList != null) {
			for (int i = 0; i < textList.size(); i++) {
				textList.get(i).setVisibility(View.INVISIBLE);
				textList.get(i).setCompoundDrawables(null, null, null, null);
			}
		}
	}

	@Override
	public void show() {
		setSpeakerListInfoViewShow();
		;
		super.show();
	}

	public void setSpeakerList(List<SpeakerInfo> speakerList) {
		CustomLog.d(TAG, "setSpeakerList " + speakerList.size());
		this.mList.clear();
		this.mList.addAll(speakerList);
	}

	private void handleRemoveSpeaker(String account) {
		CustomLog.d(TAG, "handleRemoveSpeaker " + account);
		if (mList != null && mList.size() > 0) {
			for (int i = 0; i < mList.size(); i++) {
				if (account.equals(mList.get(i).getAccountId())) {
					CustomLog.d(TAG, "handleRemoveSpeaker remove ");
					mList.remove(i);
					return;
				}
			}
		}
	}

	private void handleAddSpeaker(String account) {
		CustomLog.d(TAG, "handleAddSpeaker " + account);
		if (account != null
				&& mButelOpenSDK.getSpeakerInfoById(account) != null) {
			CustomLog.d(TAG, "handleAddSpeaker add");
			mList.add(mButelOpenSDK.getSpeakerInfoById(account));
		}
	}

	// TODO 要监听刷新的！！！！
	public void setSpeakerListInfoViewShow() {
		// 显示隐藏等等操作
		setAllViewInvisiable();
		// mList.clear();
		// mList.addAll(mButelOpenSDK.getSpeakers());
		if (mList == null || mList.size() <= 0) {
			return;
		}
		for (int i = 0; i < mList.size(); i++) {
			if (i > 3) {
				CustomLog.e(TAG, "mList size " + mList.size());
				return;
			}
			CustomLog.i("SpeakerListInfoView", " SpeakerListInfo  "
					+ mList.get(i).toString());
			textList.get(i).setText(checkIsNull(mList.get(i).getAccountName()));
			textList.get(i).setVisibility(View.VISIBLE);
			/*
			 * if (mList.get(i).getSpeakerType() ==
			 * SpeakerInfo.SPEAKER_TYPE_MAIN) { Drawable drawable =
			 * getResources().getDrawable( MResource.getIdByName(mContext,
			 * MResource.DRAWABLE, "meeting_room_main_speaker_img"));
			 * drawable.setBounds(0, 0, drawable.getMinimumWidth(),
			 * drawable.getMinimumHeight());
			 * textList.get(i).setCompoundDrawables(null, null, drawable, null);
			 * textList.get(i).setCompoundDrawablePadding(8); } else
			 */if (mList.get(i).getScreenShareStatus() == SpeakerInfo.SCREEN_SHARING) {
				Drawable drawable = getResources().getDrawable(
						MResource.getIdByName(mContext, MResource.DRAWABLE,
								"meeting_room_share_doc_img"));
				drawable.setBounds(0, 0, drawable.getMinimumWidth(),
						drawable.getMinimumHeight());
				textList.get(i)
						.setCompoundDrawables(null, null, drawable, null);
				textList.get(i).setCompoundDrawablePadding(8);
			}
		}
	}

	private String checkIsNull(String str) {
		String result;
		if (str == null || str.equals("")) {
			result = getContext().getString(R.string.unnamed);
		} else {
			result = str;
		}
		return result;
	}
}
