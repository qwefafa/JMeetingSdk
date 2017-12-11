package cn.redcdn.menuview.view;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

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
import cn.redcdn.menuview.vo.Speaker;
import cn.redcdn.util.CustomDialog;
import cn.redcdn.util.CustomDialog.CancelBtnOnClickListener;
import cn.redcdn.util.CustomDialog.OKBtnOnClickListener;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

public abstract class SwitchVideoTypeView extends BaseView {
	private final String TAG = "SwitchVideoTypeView";
	private Context mContext;
	private ButelOpenSDK mButelOpenSDK;
	private MenuView mMenuView;
	private String mAccountId;
	private Handler notifyWorkHandler;
	private SwitchVideoModeListViewAdapter listAdapter;
	private ListView listView;
	private int showType;
	private String zoomOutAccount;
	private List<SpeakerInfo> speakerList = new ArrayList<SpeakerInfo>();// 缓存skd数据
	private List<Speaker> mParticipatorsList = new ArrayList<Speaker>();// adapter数据
	public static final int OPEN_CLOSE_CAMARE = 19;
	public static final int CLOSE_SHARE_DOC_VIEW = 20;
	private CustomDialog mSwitchMicAlertDialog;
	private WorkHandlerThread mWorkHandlerThread = new WorkHandlerThread(
			"SwitchVideoTypeViewWorkHandlerThreaed");
	private Handler mUiRreashHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				mParticipatorsList.clear();
				mParticipatorsList.addAll((List<Speaker>) msg.obj);
				CustomLog.d(TAG,
						"handleMessage mParticipatorsList size " + mParticipatorsList.size());
				listAdapter.notifyDataSetChanged();
				if (!isShowing()) {
					CustomLog.e(TAG,
							"ParticipatorsView not show, need dismiss!");
					dismiss();
				}
			}
		}
	};

	public void setSpeakerList(List<SpeakerInfo> speakerList) {
		CustomLog.d(TAG, "setSpeakerList " + speakerList.size());
		this.speakerList.clear();
		this.speakerList.addAll(speakerList);
	}

	public SwitchVideoTypeView(ButelOpenSDK butelOpenSDK, Context context,
			MenuView menuView, String accountId, DisplayMetrics density) {
		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"switch_video_mode"));
		mContext = context;
		mButelOpenSDK = butelOpenSDK;
		mMenuView = menuView;
		mAccountId = accountId;
		initWorkHandlerThread();
		mButelOpenSDK
				.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
		listAdapter = new SwitchVideoModeListViewAdapter(context,
				mParticipatorsList, accountId);
		initView();
	}

	public void setShowType(int showType, String account) {
		this.showType = showType;
		this.zoomOutAccount = account;
	}

	protected void initView() {
		Button hideViewBtn = (Button) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "hide_view_btn"));
		hideViewBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SwitchVideoTypeView.this.onClick(v);

			}
		});
		listView = (ListView) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "videoTypeListView"));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Speaker currSpeaker = mParticipatorsList.get(arg2);
				// CustomLog.d(TAG, "setOnItemClickListener ");
				if (currSpeaker.getSpeakType() == 0) {
					// 普通
					if (currSpeaker.getmVideoStatus() == SpeakerInfo.VIDEO_OPEN) {
						if (mAccountId != null
								&& mAccountId.equals(currSpeaker.getAccountId())) {
							// 关闭摄像头
							CustomLog.d(TAG, "setOnItemClickListener 关闭摄像头 "
									+ currSpeaker.getAccountId());
							onNotify(OPEN_CLOSE_CAMARE, true);
							dismiss();
						} else {
							// 关闭视频
							CustomLog.d(TAG, "setOnItemClickListener 关闭视频 "
									+ currSpeaker.getAccountId());
							if (currSpeaker.getmCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
								CustomToast.show(mContext, getContext().getString(R.string.other_close_camera),
										CustomToast.LENGTH_SHORT);
								return;
							}
							int result = mButelOpenSDK
									.closeRemoteVideo(currSpeaker
											.getAccountId());
							if (result == 0) {
								CustomLog.d(TAG,
										"setOnItemClickListener 关闭视频 result "
												+ result);
								mMenuView.handleCloseVideo(
										currSpeaker.getAccountId(), 1);
							} else {
								CustomToast.show(mContext, getContext().getString(R.string.net_error_wait_try),
										CustomToast.LENGTH_SHORT);
							}
							dismiss();
						}
					} else {
						if (mAccountId != null
								&& mAccountId.equals(currSpeaker.getAccountId())) {
							// 打开摄像头
							CustomLog.d(TAG, "setOnItemClickListener 打开摄像头 "
									+ currSpeaker.getAccountId());
							onNotify(OPEN_CLOSE_CAMARE, false);
							dismiss();
						} else {
							// 打开视频
							// >640
							CustomLog.d(TAG, "setOnItemClickListener 打开摄像头 "
									+ currSpeaker.getAccountId()
									+ " currSpeaker.getmCamStatus() "
									+ currSpeaker.getmCamStatus());
							if (currSpeaker.getmCamStatus() == SpeakerInfo.CAM_STATUS_CLOSE) {
								CustomToast.show(mContext, getContext().getString(R.string.other_close_camera),
										CustomToast.LENGTH_SHORT);
								return;
							}
							boolean isShowDialog = false;
							int r[] = currSpeaker.getVideoResolution();
							if (r != null && r.length == 2) {
								if (r[0] > 640 || r[1] > 480) {
									isShowDialog = true;
								}
							}
							if (isShowDialog) {
								dismiss();
								showDialog(currSpeaker.getAccountId(),
										currSpeaker.getAccountName(), r[0]
												+ "*" + r[1]);
							} else {
								CustomLog.d(TAG, "setOnItemClickListener 打开视频 "
										+ currSpeaker.getAccountId());
								int result = mButelOpenSDK
										.openRemoteVideo(currSpeaker
												.getAccountId());
								if (result == 0) {
									CustomLog.d(TAG,
											"setOnItemClickListener 打开视频  result"
													+ result);
									mMenuView.handleOpenVideo(
											currSpeaker.getAccountId(), 1);
								} else {
									CustomToast.show(mContext,getContext().getString(R.string.net_error_wait_try),
											CustomToast.LENGTH_SHORT);
								}
								dismiss();
							}
						}
					}
				} else {
					// 共享
					if (currSpeaker.getmDocVideoStatus() == SpeakerInfo.DOC_VIDEO_OPEN) {
						if (mAccountId != null
								&& mAccountId.equals(currSpeaker.getAccountId())) {
							// 关闭屏幕分享页面
							CustomLog.d(TAG, "setOnItemClickListener 关闭屏幕分享页面 "
									+ currSpeaker.getAccountId());
							onNotify(CLOSE_SHARE_DOC_VIEW,
									currSpeaker.getAccountId());
						} else {
							// 关闭其他屏幕分享视频画面
							CustomLog.d(TAG,
									"setOnItemClickListener 关闭其他屏幕分享视频画面 "
											+ currSpeaker.getAccountId());
							int result = mButelOpenSDK
									.closeRemoteDocVideo(currSpeaker
											.getAccountId());
							if (result == 0) {
								CustomLog.d(TAG,
										"setOnItemClickListener 关闭其他屏幕分享视频画面  result"
												+ result);
								mMenuView.handleCloseVideo(
										currSpeaker.getAccountId(), 2);
							} else {
								CustomToast.show(mContext, getContext().getString(R.string.net_error_wait_try),
										CustomToast.LENGTH_SHORT);
							}
						}
					} else {
						if (mAccountId != null
								&& mAccountId.equals(currSpeaker.getAccountId())) {
							// 没有此场景
						} else {
							// 打开其他屏幕分享视频画面
							CustomLog.d(TAG,
									"setOnItemClickListener 打开其他屏幕分享视频画面 "
											+ currSpeaker.getAccountId());
							int result = mButelOpenSDK
									.openRemoteDocVideo(currSpeaker
											.getAccountId());
							if (result == 0) {
								CustomLog.d(TAG,
										"setOnItemClickListener 打开其他屏幕分享视频画面 result "
												+ result);
								mMenuView.handleOpenVideo(
										currSpeaker.getAccountId(), 2);
							} else {
								CustomToast.show(mContext, getContext().getString(R.string.net_error_wait_try),
										CustomToast.LENGTH_SHORT);
							}
						}
					}
					dismiss();
				}

			}
		});
		listView.setAdapter(listAdapter);
	}

	private void showDialog(final String id, String name, String resolution) {
		if (mSwitchMicAlertDialog != null && mSwitchMicAlertDialog.isShowing()) {
			CustomLog.d(TAG, "提示弹框已显示，不再显示");
			return;
		} else {
			mSwitchMicAlertDialog = new CustomDialog(mContext);
			mSwitchMicAlertDialog.setTip(name + getContext().getString(R.string.resolution_more_vga)
					+ getContext().getString(R.string.is_open_video));
			mSwitchMicAlertDialog.setOkBtnText(getContext().getString(R.string.open_now));
			mSwitchMicAlertDialog.setCancelBtnText(getContext().getString(R.string.cancel));
			mSwitchMicAlertDialog.setBlackTheme();
			mSwitchMicAlertDialog
					.setOkBtnOnClickListener(new OKBtnOnClickListener() {

						@Override
						public void onClick(CustomDialog customDialog) {
							int result = mButelOpenSDK.openRemoteVideo(id);
							if (result == 0) {
								CustomLog.d(TAG,
										"setOnItemClickListener 打开视频  result"
												+ result);
								mMenuView.handleOpenVideo(id, 1);
							} else {
								CustomToast.show(mContext, getContext().getString(R.string.net_error_wait_try),
										CustomToast.LENGTH_SHORT);
							}
							customDialog.dismiss();
							mSwitchMicAlertDialog = null;
							SwitchVideoTypeView.this.dismiss();
						}

					});
			mSwitchMicAlertDialog
					.setCancelBtnOnClickListener(new CancelBtnOnClickListener() {

						@Override
						public void onClick(CustomDialog customDialog) {
							customDialog.dismiss();
							mSwitchMicAlertDialog = null;

						}
					});
			mSwitchMicAlertDialog.show();
		}
	}

	public void handleCamareRefresh() {

	}

	private void initWorkHandlerThread() {
		mWorkHandlerThread.start();
		notifyWorkHandler = new Handler(mWorkHandlerThread.getLooper(),
				mWorkHandlerThread);
	}

	@Override
	public void show() {
		handleRefreshData(showType);
		super.show();
	}

	private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener = new ButelOpenSDKNotifyListener() {
		@Override
		public void onNotify(int arg0, Object arg1) {
			Cmd cmd = null;
			switch (arg0) {
			case NotifyType.START_SHARE_DOC:
				CustomLog.d(TAG, "START_SHARE_DOC");
				handleRemoveSpeaker(mAccountId);
				handleAddSpeaker(mAccountId);
				handleRefreshData(showType);
				break;
			case NotifyType.STOP_SHARE_DOC:
				CustomLog.d(TAG, "STOP_SHARE_DOC");
				handleRemoveSpeaker(mAccountId);
				handleAddSpeaker(mAccountId);
				handleRefreshData(showType);
				break;
			case NotifyType.START_SPEAK:
				CustomLog.d(TAG, "START_SPEAK");
				handleRemoveSpeaker(mAccountId);
				handleAddSpeaker(mAccountId);
				handleRefreshData(showType);
				break;
			case NotifyType.STOP_SPEAK:
				CustomLog.d(TAG, "STOP_SPEAK");
				handleRemoveSpeaker(mAccountId);
				handleRefreshData(showType);
				break;
			case NotifyType.SPEAKER_ON_LINE:
				CustomLog.d(TAG, "SPEAKER_ON_LINE");
				cmd = (Cmd) arg1;
				if (cmd == null)
					return;
				handleRemoveSpeaker(cmd.getAccountId());
				handleAddSpeaker(cmd.getAccountId());
				handleRefreshData(showType);
				break;
			case NotifyType.SPEAKER_OFF_LINE:
				CustomLog.d(TAG, "SPEAKER_OFF_LINE");
				cmd = (Cmd) arg1;
				if (cmd == null)
					return;
				handleRemoveSpeaker(cmd.getAccountId());
				handleRefreshData(showType);
				break;
			case NotifyType.SERVER_NOTICE_START_SCREEN_SHAREING:
				CustomLog.d(TAG, "SERVER_NOTICE_START_SCREEN_SHAREING");
				cmd = (Cmd) arg1;
				if (cmd == null)
					return;
				handleRemoveSpeaker(cmd.getAccountId());
				handleAddSpeaker(cmd.getAccountId());
				handleRefreshData(showType);
				break;
			case NotifyType.SERVER_NOTICE_STOP_SCREEN_SHAREING:
				CustomLog.d(TAG, "SERVER_NOTICE_STOP_SCREEN_SHAREING");
				cmd = (Cmd) arg1;
				if (cmd == null)
					return;
				handleRemoveSpeaker(cmd.getAccountId());
				handleAddSpeaker(cmd.getAccountId());
				handleRefreshData(showType);
				break;
			case NotifyType.SPEAKER_VIDEO_PARAM_UPDATE:
				CustomLog.d(TAG, "SPEAKER_VIDEO_PARAM_UPDATE");
				String id = (String) arg1;
				if (id == null)
					return;
				handleRemoveSpeaker(id);
				handleAddSpeaker(id);
				handleRefreshData(showType);
				break;
			case NotifyType.SERVER_NOTICE_STREAM_PUBLISH:
				Cmd respModel = (Cmd) arg1;
				if (respModel == null)
					return;
				handleRemoveSpeaker(respModel.getAccountId());
				handleAddSpeaker(respModel.getAccountId());
				handleRefreshData(showType);
				break;
			case NotifyType.SERVER_NOTICE_STREAM_UNPUBLISH:
				Cmd resp = (Cmd) arg1;
				if (resp == null)
					return;
				handleRemoveSpeaker(resp.getAccountId());
				handleAddSpeaker(resp.getAccountId());
				handleRefreshData(showType);
				break;
			default:
				break;
			}
		}
	};

	private void handleRemoveSpeaker(String account) {
		CustomLog.d(TAG, "handleRemoveSpeaker " + account);
		if (speakerList != null && speakerList.size() > 0) {
			for (int i = 0; i < speakerList.size(); i++) {
				if (account.equals(speakerList.get(i).getAccountId())) {
					CustomLog.d(TAG, "handleRemoveSpeaker remove ");
					speakerList.remove(i);
					CustomLog.d(TAG, "handleRemoveSpeaker speakerList "
							+ speakerList.size());
					return;
				}
			}
		}
	}

	private void handleAddSpeaker(String account) {
		CustomLog.d(TAG, "handleAddSpeaker " + account + " size "
				+ mButelOpenSDK.getSpeakers().size());
		if (account != null
				&& mButelOpenSDK.getSpeakerInfoById(account) != null) {
			CustomLog.d(TAG, "handleAddSpeaker add");
			speakerList.add(mButelOpenSDK.getSpeakerInfoById(account));
			CustomLog.d(TAG,
					"handleAddSpeaker speakerList " + speakerList.size());
		}
	}

	private void handleRefreshData(int showType) {
		// showType 0 普通，1共享
		CustomLog.d(TAG, "handleRefreshData " + showType);
		Message msg = Message.obtain();
		msg.obj = speakerList;
		msg.what = WorkHandlerThread.MAKE_VIDEO_TYPE_LIST;
		msg.arg1 = showType;
		notifyWorkHandler.sendMessage(msg);
	}

	private class WorkHandlerThread extends HandlerThread implements Callback {
		public static final int MAKE_VIDEO_TYPE_LIST = 0;
		private List<SpeakerInfo> list;
		private Speaker shareDoc;
		private Speaker mySpeaker;
		private List<Speaker> otherList = new ArrayList<Speaker>();

		public WorkHandlerThread(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean handleMessage(Message msg) {

			if (msg.what == MAKE_VIDEO_TYPE_LIST) {
				CustomLog.d(TAG, "WorkHandlerThread 处理命令  " + showType);
				shareDoc = null;
				mySpeaker = null;
				otherList.clear();
				list = (List<SpeakerInfo>) msg.obj;
				if (list != null) {
					if (showType < 2) {
						for (int i = 0; i < list.size(); i++) {
							SpeakerInfo speakInfo = list.get(i);
							CustomLog.d(TAG, "SpeakerInfo：" + speakInfo);
							if (speakInfo.getScreenShareStatus() == SpeakerInfo.SCREEN_SHARING) {
								addShareDocSpeaker(speakInfo);
								addNormalSpeaker(speakInfo);
							} else {
								addNormalSpeaker(speakInfo);
							}

						}
					}
					notifyUpdate();
				}
			}
			return false;
		}

		private void notifyUpdate() {
			CustomLog.d(TAG, "处理命令  notifyUpdate ");
			List<Speaker> tmpParticipatorList = new ArrayList<Speaker>();
			switch (showType) {
			case 0:
				// if (mySpeaker != null) {
				// tmpParticipatorList.add(mySpeaker);
				// }
				tmpParticipatorList.addAll(otherList);
				break;
			case 1:
				if (shareDoc != null
						&& !shareDoc.getAccountId().equals(mAccountId)) {
					tmpParticipatorList.add(shareDoc);
				}
				break;
			case 2:
				if (zoomOutAccount != null
						&& mButelOpenSDK.getSpeakerInfoById(zoomOutAccount) != null) {
					SpeakerInfo speakInfo = mButelOpenSDK
							.getSpeakerInfoById(zoomOutAccount);
					Speaker speaker = new Speaker();
					speaker.setAccountId(speakInfo.getAccountId());
					if (mAccountId != null
							&& !mAccountId.equals(speakInfo.getAccountId())) {
						// speaker.setAccountName("我");
						// } else {
						speaker.setAccountName(speakInfo.getAccountName());
						// }
						speaker.setmVideoStatus(speakInfo.getVideoStatus());
						speaker.setVideoResolution(speakInfo
								.getVideoResolution());
						speaker.setScreenShareStatus(speakInfo
								.getScreenShareStatus());
						speaker.setmCamStatus(speakInfo.getCamStatus());
						speaker.setSpeakType(0);
						tmpParticipatorList.add(speaker);
					}
				}
				break;
			}
			Message message = Message.obtain();
			message.what = 0;
			message.obj = tmpParticipatorList;
			mUiRreashHandler.sendMessage(message);
		}

		private void addShareDocSpeaker(SpeakerInfo speakInfo) {
			CustomLog.d(TAG, "处理命令  addShareDocSpeaker ");
			shareDoc = new Speaker();
			shareDoc.setAccountId(speakInfo.getAccountId());
			shareDoc.setAccountName(speakInfo.getAccountName());
			shareDoc.setDocVideoResolution(speakInfo.getDocVideoResolution());
			shareDoc.setmDocVideoStatus(speakInfo.getDocVideoStatus());
			shareDoc.setScreenShareStatus(speakInfo.getScreenShareStatus());
			shareDoc.setmCamStatus(speakInfo.getCamStatus());
			shareDoc.setSpeakType(1);
		}

		private void addNormalSpeaker(SpeakerInfo speakInfo) {
			CustomLog.d(TAG, "处理命令  addNormalSpeaker ");
			if (mAccountId != null
					&& mAccountId.equals(speakInfo.getAccountId())) {
				mySpeaker = new Speaker();
				mySpeaker.setAccountId(speakInfo.getAccountId());
				mySpeaker.setAccountName(getContext().getString(R.string.me));
				mySpeaker.setmVideoStatus(speakInfo.getVideoStatus());
				mySpeaker.setVideoResolution(speakInfo.getVideoResolution());
				mySpeaker
						.setScreenShareStatus(speakInfo.getScreenShareStatus());
				mySpeaker.setmCamStatus(speakInfo.getCamStatus());
				mySpeaker.setSpeakType(0);
			} else {
				Speaker speaker = new Speaker();
				speaker.setAccountId(speakInfo.getAccountId());
				speaker.setAccountName(speakInfo.getAccountName());
				speaker.setmVideoStatus(speakInfo.getVideoStatus());
				speaker.setVideoResolution(speakInfo.getVideoResolution());
				speaker.setScreenShareStatus(speakInfo.getScreenShareStatus());
				speaker.setmCamStatus(speakInfo.getCamStatus());
				speaker.setSpeakType(0);
				otherList.add(speaker);
			}
		}
	}

	@Override
	public void release() {
		super.release();
		notifyWorkHandler.removeCallbacksAndMessages(null);
		mUiRreashHandler.removeCallbacksAndMessages(null);
		try {
			notifyWorkHandler.getLooper().quit();
			mWorkHandlerThread.getLooper().quit();
		} catch (Exception e) {
			CustomLog.d(TAG, "线程退出异常：" + e.toString());
		}
		mMenuView = null;
		mButelOpenSDK
				.removeButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
		mButelOpenSDK = null;
	}
}
