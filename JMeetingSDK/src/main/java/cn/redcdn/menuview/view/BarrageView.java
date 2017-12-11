package cn.redcdn.menuview.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.redcdn.imservice.IIMServe;
import cn.redcdn.imservice.IIMServeCB;
import cn.redcdn.imservice.IMMessageBean;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.util.MResource;

/**
 * Created by dell on 2017/8/23.
 */

public abstract class BarrageView extends BaseView {
    private static final String TAG = "BarrageView";
    private Context mContext;
    private Button hideBtn;
    private FrameLayout contentFl;
    private Button notice_send_btn;
    public ListView listView;
    private EditText et_content;
    private BarrageViewListAdapter viewListAdapter;
    private ServiceConnection meetingIM;
    public IIMServe iimServe;
    private String groupId = "";
    private String msg;
    private ArrayList<IMMessageBean> msgList;
    private static final int MSG_NOTIFY = 1;
    private static final int MSG_HISTORY = 2;
    private View headerLoadingView = null;
    private View headerRoot = null;
    private ExecutorService mSingleThreadExecutor = null;
    private MenuView.BarrageViewListener mBarrageViewListener;
    private Button barrageControlButton;
    private boolean barrageIsOpen = true;

    public BarrageView(final Context context, String groupId,MenuView.BarrageViewListener barrageViewListener) {
        super(context, MResource.getIdByName(context, MResource.LAYOUT,
                "meeting_room_barrage_view"));

        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        headerLoadingView = LayoutInflater.from(context).inflate(R.layout.page_load_header, null);
        headerRoot = headerLoadingView.findViewById(R.id.header_root);
        headerRoot.setPadding(0,
                0, 0, 0);
        headerRoot.setVisibility(View.INVISIBLE);
        mContext = context;
        this.groupId = groupId;
        this.mBarrageViewListener = barrageViewListener;
        willNotCacheDrawing();
        msgList = new ArrayList<>();
        //绑定服务
        initView();
        Intent intent = new Intent();
        intent.putExtra("gid", groupId);
        intent.setAction("cn.redcdn.butelmedical.ImConnectService");
        String packagename = MeetingManager.getInstance().getRootDirectory();
        intent.setPackage(packagename);
        meetingIM = new MeetingIM();
        boolean isbindSucc = mContext.bindService(intent, meetingIM, Context.BIND_AUTO_CREATE);
        CustomLog.d(TAG, "绑定服务 isbindSucc :" + isbindSucc);
        viewListAdapter = new BarrageViewListAdapter(mContext, msgList);
        CustomLog.d(TAG, "消息集合" + msgList.size());
        listView.addHeaderView(headerLoadingView);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                 /*
                scrollState有三种状态，分别是SCROLL_STATE_IDLE、SCROLL_STATE_TOUCH_SCROLL、SCROLL_STATE_FLING
                SCROLL_STATE_IDLE是当屏幕停止滚动时
                SCROLL_STATE_TOUCH_SCROLL是当用户在以触屏方式滚动屏幕并且手指仍然还在屏幕上时
                SCROLL_STATE_FLING是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                */
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    CustomLog.d(TAG, "列表正在滚动...");
                    // list列表滚动过程中，暂停图片上传下载
                } else {
                }
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 滚动停止的场合，加载更多数据
                    int firstVP = listView.getFirstVisiblePosition();
                    CustomLog.d(TAG, "列表停止滚动...FirstVisiblePosition:" + firstVP);
                    if (firstVP == 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    IMMessageBean tmpBean = msgList.get(0);
                                    CustomLog.d(TAG, "加载历史消息 time:" + tmpBean.getTime()
                                            + " content:" + tmpBean.getMsgContent());
                                    iimServe.queryHistoryMsg(tmpBean.getTime());

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
//                        isRequestSuccess = false;
                        headerRoot.setVisibility(VISIBLE);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        listView.setAdapter(viewListAdapter);
    }

    private void initView() {
        hideBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "hide_view_btn"));
        hideBtn.setOnClickListener(btnOnClickListener);
        contentFl = (FrameLayout) findViewById(MResource.getIdByName(mContext, MResource.ID, "barrage_content_fl"));
        contentFl.setOnClickListener(btnOnClickListener);
        notice_send_btn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "notice_send_btn"));
        notice_send_btn.setOnClickListener(btnOnClickListener);
        listView = (ListView) findViewById(MResource.getIdByName(mContext, MResource.ID, "listview"));
        et_content = (EditText) findViewById(MResource.getIdByName(mContext, MResource.ID, "et_content"));
        barrageControlButton = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "barrage_control_btn"));
        barrageControlButton.setOnClickListener(btnOnClickListener);
        barrageControlButton.setBackgroundDrawable(getResources().
                getDrawable(MResource.getIdByName(mContext, MResource.DRAWABLE, "barrage_on")));
    }

    public class MeetingIM implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iimServe = IIMServe.Stub.asInterface(service);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        iimServe.registerCallBack(callBack);
                        iimServe.queryHistoryMsg(0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                iimServe.unRegisterCallBack(callBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private IIMServeCB.Stub callBack = new IIMServeCB.Stub() {
        @Override
        public void onMsgUpdate(IMMessageBean item) throws RemoteException {

            Message message = handler.obtainMessage();
            message.what = MSG_NOTIFY;
            message.obj = item;
            handler.sendMessage(message);
            CustomLog.d(TAG, "content " + item.getMsgContent());

        }

        @Override
        public void onQueryHistoryMsg(List<IMMessageBean> historyMsg) throws RemoteException {
            CustomLog.d(TAG, "size is " + historyMsg.size());
            Message message = handler.obtainMessage();
            message.what = MSG_HISTORY;
            message.obj = historyMsg;
            handler.sendMessage(message);
            CustomLog.d(TAG, "size is " + msgList.size());
        }
    };


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_NOTIFY:
                    IMMessageBean item = (IMMessageBean) msg.obj;
                    //TODO 收到重复消息,添加msgId
                    CustomLog.d(TAG, "msgId:" + item.getMsgId());
                    if (item.getMsgStatus() == 1) {
                        if (msgList.size() == 0) {
                            CustomLog.d(TAG, "无数据");
                            msgList.add(item);
                            viewListAdapter.notifyDataSetChanged();
                            listView.setSelection(msgList.size());
                            return;
                        }
                        int trmi = msgList.size() > 30 ? msgList.size() - 30 : 0;
                        for (int i = msgList.size() - 1; i >= 0; i--) {
                            if (msgList.get(i).getMsgId().equals(item.getMsgId())) {
                                CustomLog.d(TAG, "找到匹配数据");
                                msgList.get(i).setMsgStatus(1);
                                viewListAdapter.notifyDataSetChanged();
                                listView.setSelection(msgList.size());
                                break;
                            }
                            if (i == trmi) {
                                CustomLog.d(TAG, "未找到匹配数据");
                                msgList.add(item);
                                viewListAdapter.notifyDataSetChanged();
                                listView.setSelection(msgList.size());
                                break;
                            }
                        }
                    } else {
                        CustomLog.d(TAG, "添加数据");
                        mBarrageViewListener.onMessageArrive(item);
                        msgList.add(item);
                        viewListAdapter.notifyDataSetChanged();
                        listView.setSelection(msgList.size());
                        CustomLog.d(TAG, "消息集合" + msgList.size());
                    }
                    headerRoot.setVisibility(INVISIBLE);
                    break;
                case MSG_HISTORY:
                    ArrayList<IMMessageBean> tmpList = (ArrayList<IMMessageBean>) msg.obj;
                    if (tmpList.size() != 0) {
                        msgList.addAll(0, tmpList);
                        viewListAdapter.notifyDataSetChanged();
                        listView.setSelection(tmpList.size());
                    }
                    headerRoot.setVisibility(INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void release() {
        super.release();
        mSingleThreadExecutor = null;
        mContext.unbindService(meetingIM);
    }

    private OnClickListener btnOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == MResource.getIdByName(mContext, MResource.ID, "hide_view_btn")) {
                BarrageView.this.onClick(view);
            } else if (id == MResource.getIdByName(mContext, MResource.ID, "barrage_content_fl")) {
//                CustomToast.show(mContext, "点击了BarrageView", Toast.LENGTH_LONG);
            } else if (id == MResource.getIdByName(mContext, MResource.ID, "notice_send_btn")) {
                msg = et_content.getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    return;
                } else {
                    et_content.setText("");
                    mSingleThreadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                iimServe.sendTextMsg(groupId, msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } else if(id == MResource.getIdByName(mContext, MResource.ID, "barrage_control_btn")){
                if(barrageIsOpen){
                    barrageIsOpen = false;
                    barrageControlButton.setBackgroundDrawable(getResources().
                            getDrawable(MResource.getIdByName(mContext, MResource.DRAWABLE, "barrage_off")));
                    mBarrageViewListener.onBarrageControlClick(false);
                }else{
                    barrageIsOpen = true;
                    barrageControlButton.setBackgroundDrawable(getResources().
                            getDrawable(MResource.getIdByName(mContext, MResource.DRAWABLE, "barrage_on")));
                    mBarrageViewListener.onBarrageControlClick(true);
                }

            }

        }
    };

    @Override
    public void show() {
        super.show();
        listView.setSelection(msgList.size());
    }
}
