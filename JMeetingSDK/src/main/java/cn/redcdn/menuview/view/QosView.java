package cn.redcdn.menuview.view;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.channelsoft.sipsdk.SmSdkJNI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.menuview.QosInfoKey;
import cn.redcdn.util.MResource;

public abstract class QosView extends BaseView {
	private final String TAG = QosView.this.getClass().getName();
	private TextView accountNameTextView;
	private TextView accountIdTextView;
	private TextView uploadTextView;
	private TextView downloadTextView;
	protected Context mContext;
	private String mAccountName = "";
	private String mAccountId = "";
	private Button hideViewBtn;
	private Timer timer;
	private TimerTask timerTask ;
	private ArrayList<QosItem> list = new ArrayList<QosItem>();;
	private QosViewListAdapter adapter;
	private ListView listView;
	private Handler showQosInfoHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			showStreamQos(SmSdkJNI.GetStreamQosInfo());
			// showNetInfo(SmSdkJNI.GetNetQosInfo());
			// showLoadInfo(SmSdkJNI.GetEquipmentLoadInfo());
		};
	};

	private OnClickListener btnOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == MResource.getIdByName(mContext, MResource.ID,
					"hide_view_btn")) {
				QosView.this.onClick(v);
			}
		}
	};

	public QosView(Context context) {
		super(context, MResource.getIdByName(context, MResource.LAYOUT,
				"meeting_room_menu_qos_view"));
		mContext = context;
		hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext,
				MResource.ID, "hide_view_btn"));
		hideViewBtn.setOnClickListener(btnOnClickListener);

		uploadTextView = (TextView) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "qos_info_upload_speed"));
		downloadTextView = (TextView) findViewById(MResource.getIdByName(
				mContext, MResource.ID, "qos_info_download_speed"));
		listView = (ListView) findViewById(R.id.listview);
		adapter = new QosViewListAdapter(context,list);
		listView.setAdapter(adapter);

	}

	public void showStreamQos(String info) {

		list.clear();

		try {

			JSONObject jsonObject = new JSONObject(info);
			JSONArray baseInfo = new JSONArray();
			if (jsonObject.has(QosInfoKey.baseInfo))
				baseInfo = jsonObject.getJSONArray(QosInfoKey.baseInfo);
			JSONArray streamQosInfo = new JSONArray();
			if (jsonObject.has(QosInfoKey.streamQosInfo))
				streamQosInfo = jsonObject.getJSONArray(QosInfoKey.streamQosInfo);
			if (baseInfo != null) {
				JSONObject ob = new JSONObject();
				for (int i = 0; i < baseInfo.length(); i++) {
					QosItem item2 = new  QosItem();
					item2.setContent("");
					item2.setTitle("************基本信息************");
					list.add(item2);
					adapter.notifyDataSetChanged();
					ob = baseInfo.getJSONObject(i);
					Iterator<String> keys = ob.keys();
					while (keys != null && keys.hasNext()) {
						String title = keys.next();
						QosItem item3 = new  QosItem();
						item3.setContent(ob.getString(title));
						item3.setTitle(title);
						list.add(item3);
						adapter.notifyDataSetChanged();


					}
				}
			}
			if (streamQosInfo != null) {
				JSONObject ob = new JSONObject();
				for (int i = 0; i < streamQosInfo.length(); i++) {
					QosItem item4 = new  QosItem();
					item4.setContent("");
					item4.setTitle("**************第" + (i + 1) + "条流信息**************");
					list.add(item4);
					adapter.notifyDataSetChanged();
					ob = streamQosInfo.getJSONObject(i);
					Iterator<String> keys = ob.keys();
					while (keys != null && keys.hasNext()) {

						QosItem item5 = new  QosItem();
						String  title = keys.next();
						item5.setTitle(title);
						item5.setContent(ob.getString(title));
						list.add(item5);
						adapter.notifyDataSetChanged();
					}
				}
			}
//			for (int i = list.size()-1;i>=0;i--){
//				CustomLog.d(TAG,"标题:"+list.get(i).getTitle()+"内容::"+list.get(i).getContent());
//			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void showNetInfo(String info) {
		try {
			JSONObject jsonObject = new JSONObject(info);
			// CustomLog.d(TAG, "网络数据：" + jsonObject);
			int mic1UpNetSpeed = jsonObject.optInt(QosInfoKey.mic1UpNetSpeed);
			int mic1DownNetSpeed = jsonObject
					.optInt(QosInfoKey.mic1DownNetSpeed);
			int mic1UpLossRate = jsonObject.optInt(QosInfoKey.mic1UpLossRate);
			int mic1FecUpLossRate = jsonObject
					.optInt(QosInfoKey.mic1FecUpLossRate);
			int mic1DownLossRate = jsonObject
					.optInt(QosInfoKey.mic1DownLossRate);
			int mic1FecDownLossRate = jsonObject
					.optInt(QosInfoKey.mic1FecDownLossRate);

			int mic2UpNetSpeed = jsonObject.optInt(QosInfoKey.mic2UpNetSpeed);
			int mic2DownNetSpeed = jsonObject
					.optInt(QosInfoKey.mic2DownNetSpeed);
			int mic2UpLossRate = jsonObject.optInt(QosInfoKey.mic2UpLossRate);
			int mic2FecUpLossRate = jsonObject
					.optInt(QosInfoKey.mic2FecUpLossRate);
			int mic2DownLossRate = jsonObject
					.optInt(QosInfoKey.mic2DownLossRate);
			int mic2FecDownLossRate = jsonObject
					.optInt(QosInfoKey.mic2FecDownLossRate);

			// for information report
			// InfoReportManager.setUpNet(mic1UpNetSpeed + mic2UpNetSpeed);
			// InfoReportManager.setDownNet(mic1DownNetSpeed +
			// mic2DownNetSpeed);
			// InfoReportManager.setFrameLossNum(mic1DownLossRate);

			// CustomLog.d(TAG, "mic1UpNetSpeed = " + mic1UpNetSpeed);
			// CustomLog.d(TAG, "mic1DownNetSpeed = " + mic1DownNetSpeed);
			// CustomLog.d(TAG, "mic1UpLossRate = " + mic1UpLossRate);
			// CustomLog.d(TAG, "mic1FecUpLossRate = " + mic1FecUpLossRate);
			// CustomLog.d(TAG, "mic1DownLossRate = " + mic1DownLossRate);
			// CustomLog.d(TAG, "mic1FecDownLossRate = " + mic1FecDownLossRate);
			//
			// CustomLog.d(TAG, "mic2UpNetSpeed = " + mic2UpNetSpeed);
			// CustomLog.d(TAG, "mic2DownNetSpeed = " + mic2DownNetSpeed);
			// CustomLog.d(TAG, "mic2UpLossRate = " + mic2UpLossRate);
			// CustomLog.d(TAG, "mic2FecUpLossRate = " + mic2FecUpLossRate);
			// CustomLog.d(TAG, "mic2DownLossRate = " + mic2DownLossRate);
			// CustomLog.d(TAG, "mic2FecDownLossRate = " + mic2FecDownLossRate);

			TextView mic1UpNetSpeedTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic1UpNetSpeedTextView"));
			TextView mic1DownNetSpeedTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic1DownNetSpeedTextView"));
			TextView mic1UpLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic1UpLossRateTextView"));
			TextView mic1FecUpLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic1FecUpLossRateTextView"));
			TextView mic1DownLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic1DownLossRateTextView"));
			TextView mic1FecDownLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic1FecDownLossRateTextView"));

			TextView mic2UpNetSpeedTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic2UpNetSpeedTextView"));
			TextView mic2DownNetSpeedTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic2DownNetSpeedTextView"));
			TextView mic2UpLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic2UpLossRateTextView"));
			TextView mic2FecUpLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic2FecUpLossRateTextView"));
			TextView mic2DownLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic2DownLossRateTextView"));
			TextView mic2FecDownLossRateTextView = (TextView) findViewById(MResource
					.getIdByName(mContext, MResource.ID,
							"mic2FecDownLossRateTextView"));

			mic1UpNetSpeedTextView.setText(mic1UpNetSpeed / 1000 + "Kb/s");
			mic1DownNetSpeedTextView.setText(mic1DownNetSpeed / 1000 + "Kb/s");
			mic1UpLossRateTextView
					.setText((((double) mic1UpLossRate / 100.00) == 0 ? "0"
							: ((double) mic1UpLossRate / 100.00)) + "%");
			mic1FecUpLossRateTextView
					.setText((((double) mic1FecUpLossRate / 100.00) == 0 ? "0"
							: ((double) mic1FecUpLossRate / 100.00)) + "%");
			mic1DownLossRateTextView
					.setText((((double) mic1DownLossRate / 100.00) == 0 ? "0"
							: ((double) mic1DownLossRate / 100.00)) + "%");
			mic1FecDownLossRateTextView
					.setText((((double) mic1FecDownLossRate / 100.00) == 0 ? "0"
							: ((double) mic1FecDownLossRate / 100.00))
							+ "%");

			mic2UpNetSpeedTextView.setText(mic2UpNetSpeed / 1000 + "Kb/s");
			mic2DownNetSpeedTextView.setText(mic2DownNetSpeed / 1000 + "Kb/s");
			mic2UpLossRateTextView
					.setText((((double) mic2UpLossRate / 100.00) == 0 ? "0"
							: ((double) mic2UpLossRate / 100.00)) + "%");
			mic2FecUpLossRateTextView
					.setText((((double) mic2FecUpLossRate / 100.00) == 0 ? "0"
							: ((double) mic2FecUpLossRate / 100.00)) + "%");
			mic2DownLossRateTextView
					.setText((((double) mic2DownLossRate / 100.00) == 0 ? "0"
							: ((double) mic2DownLossRate / 100.00)) + "%");
			mic2FecDownLossRateTextView
					.setText((((double) mic2FecDownLossRate / 100.00) == 0 ? "0"
							: ((double) mic2FecDownLossRate / 100.00))
							+ "%");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void showLoadInfo(String info) {
		try {
			JSONObject jsonObject = new JSONObject(info);
			// CustomLog.d(TAG, "cpu以及内存数据：" + jsonObject);
			JSONObject cpuInfo = jsonObject.optJSONObject(QosInfoKey.cpuInfo);
			JSONObject memoryInfo = jsonObject
					.optJSONObject(QosInfoKey.memoryInfo);
			if (cpuInfo != null) {
				float cpuInfoUsed = (float) cpuInfo
						.optDouble(QosInfoKey.cpuInfoUsed);
				float cpuInfoSystem = (float) cpuInfo
						.optDouble(QosInfoKey.cpuInfoSystem);
				float cpuInfoIdle = (float) cpuInfo
						.optDouble(QosInfoKey.cpuInfoIdle);

				// CustomLog.d(TAG, "cpuInfoUsed = " + cpuInfoUsed);
				// CustomLog.d(TAG, "cpuInfoSystem = " + cpuInfoSystem);
				// CustomLog.d(TAG, "cpuInfoIdle = " + cpuInfoIdle);
			}
			if (memoryInfo != null) {
				int memoryInfoTotal = memoryInfo
						.optInt(QosInfoKey.memoryInfoTotal);
				int memoryInfoFree = memoryInfo
						.optInt(QosInfoKey.memoryInfoFree);
				int memoryInfoUsed = memoryInfo
						.optInt(QosInfoKey.memoryInfoUsed);
				float memoryInfoPercent = (float) memoryInfo
						.optDouble(QosInfoKey.memoryInfoPercent);

				// CustomLog.d(TAG, "memoryInfoTotal = " + memoryInfoTotal);
				// CustomLog.d(TAG, "memoryInfoFree = " + memoryInfoFree);
				// CustomLog.d(TAG, "memoryInfoUsed = " + memoryInfoUsed);
				// CustomLog.d(TAG, "memoryInfoPercent = " + memoryInfoPercent);
				TextView memoryInfoPercentTextView = (TextView) findViewById(MResource
						.getIdByName(mContext, MResource.ID,
								"memoryInfoPercentTextView"));
				memoryInfoPercentTextView.setText(memoryInfoPercent + "");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void release() {
		if (timer != null) {
			timer.cancel();
		}
		if (timerTask != null) {
			timerTask.cancel();
		}
	}

	@Override
	public void show() {
		super.show();
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				showQosInfoHandler.sendEmptyMessage(0);
			}
		};
		timer.schedule(timerTask, 1000, 10000);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if (timer != null) {
			timer.cancel();
		}
		if (timerTask != null) {
			timerTask.cancel();
		}
	}
	public class QosItem{
		String title;
		String content;
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
	}
}