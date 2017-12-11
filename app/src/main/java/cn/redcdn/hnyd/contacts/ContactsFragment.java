package cn.redcdn.hnyd.contacts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseFragment;
import cn.redcdn.hnyd.contacts.contact.AddContactActivity;
import cn.redcdn.hnyd.contacts.contact.ContactCardActivity;
import cn.redcdn.hnyd.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hnyd.contacts.contact.ListViewAdapter;
import cn.redcdn.hnyd.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hnyd.contacts.contact.interfaces.Contact;
import cn.redcdn.hnyd.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hnyd.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hnyd.contacts.contact.manager.ContactManager;
import cn.redcdn.hnyd.contacts.contact.manager.IContactListChanged;
import cn.redcdn.hnyd.im.manager.FriendsManager;
import cn.redcdn.hnyd.im.provider.ProviderConstant;
import cn.redcdn.hnyd.util.CommonUtil;
import cn.redcdn.hnyd.util.PopDialogActivity;
import cn.redcdn.hnyd.util.ScannerActivity;
import cn.redcdn.hnyd.util.SideBar;
import cn.redcdn.hnyd.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static android.content.ContentValues.TAG;


/**
 * Created by thinkpad on 2017/2/7.
 * Created by thinkpad on 2017/2/7.
 *
 */

public class ContactsFragment extends BaseFragment {

    private ListView lvContact;
    public static TextView tvSelect = null;
    private ContactSetImp mContactSetImp=null;
    private ListViewAdapter contactAdapter;
    /*** 定义消息类型 */
    private final int MSG_UPDATAUI = 0x66660000;
    private final int MSG_RESUMEDATA = 0x66660005;
    //用于右上角下拉菜单
    private List<PopDialogActivity.MenuInfo> moreInfo;
    public static final int SCAN_CODE = 222;
    public static SideBar mSideBar;
    private List<LetterInfo> letterInfoList= null;
    private boolean isFirstResume = true;
    private StrangerMessageObserver observeStrangeRelation;
    private final int MSG_MESSAGE_NUMBER_CHANGED = 701;
    private IContactListChanged ic = null;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View l1 = View.inflate(getActivity(), R.layout.contacts_fragment, null);
        mSideBar = (SideBar)l1.findViewById(R.id.sidebar_contact_fragment);
        tvSelect = (TextView) l1.findViewById(R.id.fragment_tvselect);
        tvSelect.setVisibility(View.INVISIBLE);
        lvContact = (ListView) l1.findViewById(R.id.fragment_listView);
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                    if(position==0){
//                        Intent intent = new Intent();
//                        intent.setClass(getActivity(), VerificationActivity.class);
//                        startActivityForResult(intent, 0);
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), NewFriendsActivity.class);
                        startActivityForResult(intent, 0);
                    }else if(position==1){
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), ContactsGroupChatActivity.class);
                        startActivityForResult(intent, 0);
                    }else if(position==2){

                    }else{
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), ContactCardActivity.class);
                        intent.putExtra("contact", (Contact)mContactSetImp.getItem(position));
                        intent.putExtra("contactFragment","contactFragment");
                        intent.putExtra("REQUEST_CODE", ContactTransmitConfig.REQUEST_CONTACT_CODE);
                        startActivityForResult(intent, 0);
                    }
            }
        });
        return l1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getResources().getString(R.string.titlebar_middle_contact));
        titleBar.enableRightBtn("", R.drawable.btn_meetingfragment_addmeet,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtil.isFastDoubleClick()) {
                            return;
                        }
                        showMoreTitle();
                    }
                });

        mHandler.sendEmptyMessage(MSG_RESUMEDATA);

    }

    private void showMoreTitle() {
        if (moreInfo == null) {
            moreInfo = new ArrayList<PopDialogActivity.MenuInfo>();
            moreInfo.add(new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_addfriend, getString(R.string.add_friend),
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), AddContactActivity.class);
                            startActivityForResult(intent, 0);
                        }
                    }));
            moreInfo.add(new PopDialogActivity.MenuInfo(R.drawable.temp_pop_dialog_scan, getString(R.string.my_scan),
                    new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            //扫一扫
                            Intent intentScan = new Intent();
                            intentScan.setClass(getActivity(), ScannerActivity.class);
                            startActivityForResult(intentScan, SCAN_CODE);
                        }
                    }));
        }
        PopDialogActivity.setMenuInfo(moreInfo);
        startActivity(new Intent(getActivity(), PopDialogActivity.class));
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESUMEDATA:
                    CustomLog.d(TAG, "mHandler  MSG_RESUMEDATA 更新聯繫人數據");

                    registerListener();

                    initContactsInfo();
                    break;
                case MSG_UPDATAUI:
                    if(isFirstResume){
                        isFirstResume = false;
                        initContactAdapter();
                    }else{
                        if (contactAdapter != null) {
                            contactAdapter.updateDataSet(0, mContactSetImp);
                        }
                    }
                    switchLayout();
                    break;
                case MSG_MESSAGE_NUMBER_CHANGED:
                    initContactAdapter();
                    break;
                default:
                    break;
            }
        }
    };

    private void registerListener(){
        if (observeStrangeRelation == null) {
            observeStrangeRelation = new StrangerMessageObserver();
            getActivity().getContentResolver().registerContentObserver(
                    ProviderConstant.Strange_Message_URI, true,
                    observeStrangeRelation);
        }

        ic = new IContactListChanged() {
            @Override
            public void onListChange(ContactSetImp set) {
                CustomLog.d(TAG, " IContactListChanged change");
                initContactsInfo();
            }
        };
        ContactManager.getInstance(getActivity())
                .registerUpdateListener(ic);
    }


    private void initContactAdapter() {
        contactAdapter = new ListViewAdapter(getActivity(), FriendsManager.getInstance().getNotReadMsgSize());
        if(null!=mContactSetImp){
            contactAdapter.addDataSet(mContactSetImp);
            lvContact.setAdapter(contactAdapter);
        }else{
            CustomLog.e(TAG, "mContactSetImp is null");
        }
        CustomLog.i(TAG, "initContactAdapter");
    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
            switch (id) {
                default:
                    break;
            }
    }

    private void switchLayout() {
        CustomLog.i(TAG, "switchLayout");
        // 设置需要显示的提示框
        mSideBar.setTextView(tvSelect);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = CommonUtil.getLetterPosition(letterInfoList, s);
                if (position != -1) {
                    lvContact.setSelection(position);
                }
                mSideBar.setBackgroundColor(Color.parseColor("#e3e4e5"));
            }
        });
        if (mContactSetImp != null) {
            if (mContactSetImp.getCount() == 0) {
                lvContact.setVisibility(View.INVISIBLE);
                mSideBar.setVisibility(View.INVISIBLE);
            } else {
                lvContact.setVisibility(View.VISIBLE);
                mSideBar.setVisibility(View.VISIBLE);
            }
        }else{
            lvContact.setVisibility(View.INVISIBLE);
            mSideBar.setVisibility(View.INVISIBLE);
        }
        if (contactAdapter != null) {
            contactAdapter.notifyDataSetChanged();
        }
    }

    private void initContactsInfo() {
        CustomLog.i(TAG, "initContactsInfo");
        ContactManager.getInstance(getActivity()).getAllContacts(
                new ContactCallback() {

                    @Override
                    public void onFinished(ResponseEntry result) {
                        CustomLog.i(TAG, "onFinish! status: " + result.status
                                + " | content: " + result.content);
                        if (result.status >= 0) {
                            letterInfoList= new ArrayList<LetterInfo>();
                            mContactSetImp = (ContactSetImp) result.content;
                            if(null!=mContactSetImp&&mContactSetImp.getCount()>0){
                                for(int i=0;i<mContactSetImp.getCount();i++){
                                    Contact tContact = (Contact)mContactSetImp.getItem(i);
                                    if(null!=tContact.getFirstName()){
                                        LetterInfo letterInfo = new LetterInfo(){};
                                        letterInfo.setLetter(tContact.getFirstName());
                                        letterInfoList.add(letterInfo);
                                    }
                                }
                            }

                            mHandler.sendEmptyMessage(MSG_UPDATAUI);
                        }
                    }
                },true);
    }

    @Override
    public void onResume() {
        CustomLog.d(TAG, "onresume");
        super.onResume();

    }

    @Override
    public void onDestroy() {
        CustomLog.i(TAG, "onDestroy");
        super.onDestroy();
        if (observeStrangeRelation != null) {
            getActivity().getContentResolver().unregisterContentObserver(observeStrangeRelation);
            observeStrangeRelation = null;
        }
        if (ic != null) {
            ContactManager.getInstance(getActivity())
                    .unRegisterUpdateListener(ic);
            CustomLog.d(TAG, "onStop ic" + (ic == null));
        }
    }

    @Override
    public void onStop() {
        CustomLog.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        CustomLog.d(TAG, "resultfrom"+resultCode);
        if(requestCode == SCAN_CODE){
            parseBarCodeResult(data);   
        }
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    /**
     * 监听陌生人消息表
     */
    private class StrangerMessageObserver extends ContentObserver {

        public StrangerMessageObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"陌生人消息数据库数据发生变更");
            mHandler.sendEmptyMessage(MSG_MESSAGE_NUMBER_CHANGED);
        }

    }

}

