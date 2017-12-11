package cn.redcdn.menuview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.enterprisecenter.SearchAccount;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.DataChangedListener;
import cn.redcdn.meeting.data.InvitePersonList;
import cn.redcdn.menuview.NotifyType;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;
import cn.redcdn.util.RoundImageView;

//import cn.redcdn.contact.AsyncBitmapLoader;
//import cn.redcdn.contact.AsyncBitmapLoader.ImageCallBack;
//import cn.redcdn.contact.AsyncBitmapLoaderUtil;


@SuppressLint("ResourceAsColor")
public abstract class InvitePersonView extends BaseView {
    private final String TAG = InvitePersonView.this.getClass().getName();
    private ListView mInvitePersonListView;
    private InvitePersonListViewAdapter mInvitePersonListViewAdapter;
    private InvitePersonList mInvitePersonList;
    private List<Person> mDataList = new ArrayList<Person>();
    private List<Person> mParticipatorList = new ArrayList<Person>();
    private Button hideViewBtn;
    private EditText mEditText;
    private Button invitePersonBtn;
    private Context mContext;
    private String mAccountId;
    private View smsButton;
    private View weixingButton;
    private String mToken;

    private SearchAccount mSearchAccount = null;
    private MDSAppSearchUsers mdsAppSearchUsers = null;

    private OnClickListener btnOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == MResource.getIdByName(mContext, MResource.ID,
                "hide_view_btn")) {
                InvitePersonView.this.onClick(v);
            } else if (id == MResource.getIdByName(mContext, MResource.ID,
                "meeting_room_menu_invite_person_view_invite_person_btn")) {
                if (mEditText.getText().toString().equals("24682468")) {
                    mEditText.setText("");
                    hideInputKeyBoard();
                    View view = new View(mContext);
                    view.setId(24682468);
                    InvitePersonView.this.onClick(view);
                    return;
                }
                hideInputKeyBoard();
                setInvitePersonEnable(false);
                inviteInputPerson();
            } else if (id == MResource.getIdByName(mContext, MResource.ID,
                "meeting_room_menu_invite_person_view_sms_btn")) {
                invitePersonBySMS();
            } else if (id == MResource.getIdByName(mContext, MResource.ID,
                "meeting_room_menu_invite_person_view_weixing_btn")) {
                invitePersonByWeixing();

            }
        }
    };


    public InvitePersonView(Context context, InvitePersonList invitePersonList,
                            String accountId, String token) {
        super(context, MResource.getIdByName(context, MResource.LAYOUT,
            "meeting_room_menu_invite_person_view"));
        this.mToken = token;
        mContext = context;
        mAccountId = accountId;
        mInvitePersonListView = (ListView) findViewById(MResource.getIdByName(
            mContext, MResource.ID,
            "meeting_room_menu_invite_person_view_listview"));
        mInvitePersonList = invitePersonList;
        // for (int i = 0; i < 20; i++) {
        // Person p = new Person();
        // p.setAccountName("name-" + i);
        // p.setAccountId("id-" + i);
        // invitePersonList.add(p);
        // }
        //
        // mMeetingDisplayImageListener=new MeetingDisplayImageListener();

        smsButton = findViewById(MResource.getIdByName(mContext, MResource.ID,
            "meeting_room_menu_invite_person_view_sms_btn"));
        smsButton.setOnClickListener(btnOnClickListener);

        weixingButton = findViewById(MResource.getIdByName(mContext, MResource.ID,
            "meeting_room_menu_invite_person_view_weixing_btn"));

        if (MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_TV)) {
            weixingButton.setVisibility(View.INVISIBLE);
        }

        weixingButton.setOnClickListener(btnOnClickListener);

        mInvitePersonListViewAdapter = new InvitePersonListViewAdapter(context,
            mDataList);

        mInvitePersonList.setDataChangedListener(new DataChangedListener() {
            @Override
            public void onDataChanged(List<Person> dataList,
                                      List<Person> participatorList) {
                InvitePersonView.this.mParticipatorList = participatorList;
                CustomLog.d(TAG, "收到刷新列表通知。列表大小：" + dataList.size());
                InvitePersonView.this.mDataList.clear();
                InvitePersonView.this.mDataList.addAll(dataList);
                mInvitePersonListViewAdapter.notifyDataSetChanged();
            }
        });

        mInvitePersonListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (!mDataList.get(arg2).isInvited()) {
                    mDataList.get(arg2).setInvited(true);
                    mDataList.get(arg2).setInvitedFrom(1);
                    // InfoReportManager.notifyInviteOther();
                    InvitePersonView.this.onNotify(NotifyType.INVITE_PERSON,
                        mDataList.get(arg2));
                    // MobclickAgent.onEvent(mContext,
                    // AnalysisConfig.INVITE_BY_CONTACTS);
                } else {
                    CustomLog.d(TAG, "已经邀请过此人");
                }
                mInvitePersonListViewAdapter.notifyDataSetChanged();
            }
        });

        mInvitePersonListView.setAdapter(mInvitePersonListViewAdapter);
        mInvitePersonListViewAdapter.notifyDataSetChanged();

        hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext,
            MResource.ID, "hide_view_btn"));
        hideViewBtn.setOnClickListener(btnOnClickListener);

        invitePersonBtn = (Button) findViewById(MResource.getIdByName(mContext,
            MResource.ID,
            "meeting_room_menu_invite_person_view_invite_person_btn"));
        invitePersonBtn.setOnClickListener(btnOnClickListener);

        setInvitePersonEnable(false);

        mEditText = (EditText) findViewById(MResource.getIdByName(mContext,
            MResource.ID, "meeting_room_menu_invite_person_view_edittext"));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                if (arg0 != null && arg0.length() == 8) {
                    setInvitePersonEnable(true);
                } else {
                    setInvitePersonEnable(false);
                }
            }


            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
            }


            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }


    private void setInvitePersonEnable(boolean isEnable) {
        if (invitePersonBtn == null) {
            return;
        }
        invitePersonBtn.setEnabled(isEnable);
        if (isEnable) {
            invitePersonBtn.setTextColor(Color.argb(255, 51, 181, 229));
        } else {
            invitePersonBtn.setTextColor(Color.GRAY);
        }
    }


    private void inviteInputPerson() {
        mdsAppSearchUsers = new MDSAppSearchUsers() {
            @Override protected void onSuccess(List<MDSDetailInfo> responseContent) {
                super.onSuccess(responseContent);
                if (responseContent.size() > 0) {
                    MDSDetailInfo mdsDetailInfo = responseContent.get(0);
                    CustomLog.i(TAG,mdsDetailInfo.toString());
                    Person p = new Person();
                    p.setAccountId(mdsDetailInfo.nubeNumber);
                    p.setAccountName(mdsDetailInfo.nickName);
                    p.setPhoto(mdsDetailInfo.headThumUrl);
                    //红云医疗添加字段
                    p.setAccountType(mdsDetailInfo.accountType);
                    p.setWorkUnit(mdsDetailInfo.workUnit);
                    p.setWorkUnitType(mdsDetailInfo.workUnitType);
                    p.setDepartment(mdsDetailInfo.department);
                    p.setProfessional(mdsDetailInfo.professional);
                    p.setOfficTel(mdsDetailInfo.officTel);
                    //服务器没有返回相关字段，强制写成手机
                    //TODO
                    p.setAppType("mobile");
                    p.setUserType( Integer.parseInt(mdsDetailInfo.accountType));
                    CustomLog.i(TAG, " mdsDetailInfo.accountType == " + mdsDetailInfo.accountType);
                    p.setInvitedFrom(0);
                    if (mParticipatorList.contains(p)) {
                        CustomToast.show(mContext, mContext.getString(R.string.user_have_join),
                            CustomToast.LENGTH_SHORT);
                    } else {
                        //取消掉在列表中显示邀请人逻辑
                        // if (!mDataList.contains(p)) {
                        //     mDataList.add(0, p);
                        //     p.setInvited(true);
                        //     mInvitePersonListViewAdapter.notifyDataSetChanged();
                        // } else {
                        //     mDataList.remove(p);
                        //     mDataList.add(0, p);
                        //     p.setInvited(true);
                        //     mInvitePersonListViewAdapter.notifyDataSetChanged();
                        // }
                        // InfoReportManager.notifyInviteOther();
                        InvitePersonView.this.onNotify(
                            NotifyType.INVITE_PERSON, p);
                    }
                } else {
                    CustomToast.show(mContext, getContext().getString(R.string.nube_error),
                        CustomToast.LENGTH_SHORT);
                }

                mEditText.setText("");
            }


            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.i(TAG, "statusCode == " + statusCode + "statusInfo == " + statusInfo);
                mEditText.setText("");
                CustomToast.show(mContext,getContext().getString(R.string.invite_fail), CustomToast.LENGTH_SHORT);
//                CustomToast.show(mContext, getContext().getString(R.string.get_nubinfo_error)+ "statusCode == " + statusCode + "statusInfo == " + statusInfo,
//                    CustomToast.LENGTH_SHORT);
            }
        };
        // @Override
        // protected void onSuccess(List<MDSDepartmentInfoA> responseContent) {
        // 	super.onSuccess(responseContent);
        // 	departmentainfo = responseContent;
        // 	int count = responseContent.size();
        // 	for (int x = 0; x < count; x++) {
        // 		MDSDepartmentInfoA mdsDepartmentInfoA = responseContent.get(x);
        // 		Department dep = new Department();
        // 		dep.setChoose_Department(mdsDepartmentInfoA.class_a_departmentName);
        // 		//                    CustomLog.d(TAG,mdsDepartmentInfoA.class_a_departmentName
        // 		+"depatment");
        // 		listdepartmentA.add(dep);
        // 	}
        // 	adapter_Department = new DepartmentSelectAdapter(listdepartmentA,
        // 		DoctorActivity.this);
        // 	listView.setAdapter(adapter_Department);
        // 	title.setVisibility(View.GONE);
        // 	hospital_select.setVisibility(View.GONE);
        // 	department_select.setVisibility(View.VISIBLE);
        // 	doctor_linearlayout.setVisibility(View.GONE);
        // }
        //
        // @Override
        // protected void onFail(int statusCode, String statusInfo) {
        // 	super.onFail(statusCode, statusInfo);
        // 	CustomToast.show(DoctorActivity.this, "获取科室失败", Toast.LENGTH_LONG);
        //
        // }

        // mSearchAccount = new SearchAccount() {
        // 	@Override
        // 	protected void onSuccess(List<Users> responseContent) {
        // 		if (responseContent.size() > 0) {
        // 			Users user = responseContent.get(0);
        // 			Person p = new Person();
        // 			p.setAccountId(user.nubeNumber);
        // 			p.setAccountName(user.nickName);
        // 			p.setPhoto(user.headUrl);
        // 			p.setAppType(user.appType);
        // 			p.setUserType(user.serviceType);
        // 			p.setInvitedFrom(0);
        // 			if (mParticipatorList.contains(p)) {
        // 				CustomToast.show(mContext, "该用户已参会",
        // 						CustomToast.LENGTH_SHORT);
        // 			} else {
        // 				if (!mDataList.contains(p)) {
        // 					mDataList.add(0, p);
        // 					p.setInvited(true);
        // 					mInvitePersonListViewAdapter.notifyDataSetChanged();
        // 				} else {
        // 					mDataList.remove(p);
        // 					mDataList.add(0, p);
        // 					p.setInvited(true);
        // 					mInvitePersonListViewAdapter.notifyDataSetChanged();
        // 				}
        // 				// InfoReportManager.notifyInviteOther();
        // 				InvitePersonView.this.onNotify(
        // 						NotifyType.INVITE_PERSON, p);
        // 			}
        // 		} else {
        // 			CustomToast.show(mContext, "视讯号无效！",
        // 					CustomToast.LENGTH_SHORT);
        // 		}
        // 		mEditText.setText("");
        // 	}
        //
        // 	@Override
        // 	protected void onFail(int statusCode, String statusInfo) {
        // 		mEditText.setText("");
        // 		CustomToast.show(mContext, "网络不好，获取帐号信息失败！",
        // 				CustomToast.LENGTH_SHORT);
        // 	}
        // };

        if (mEditText.getText() != null
            && !mEditText.getText().toString().equals("")
            && mEditText.getText().toString().length() == 8) {
            // MobclickAgent.onEvent(mContext,
            // AnalysisConfig.INVITE_BY_VEDIONUMBER);
            Person p = new Person();
            p.setAccountId(mEditText.getText().toString());
            if (mEditText.getText().toString().equals(mAccountId)) {
                CustomToast.show(mContext, getContext().getString(R.string.not_invite_myself_join_meeting),
                    CustomToast.LENGTH_LONG);
                // edittextInviteLoadingProgressBar.setVisibility(View.INVISIBLE);
                mEditText.setText("");
            } else if (mParticipatorList.contains(p)) {
                CustomToast.show(mContext, mContext.getString(R.string.user_have_join), CustomToast.LENGTH_LONG);
                // edittextInviteLoadingProgressBar.setVisibility(View.INVISIBLE);
                mEditText.setText("");
            } else {
                String[] str = new String[1];
                str[0] = mEditText.getText().toString();
                CustomLog.i(TAG, "mToken == " + mToken + "str" + str.toString());
                mdsAppSearchUsers.appSearchUsers(mToken, 0, str);

            }
        } else {
            CustomToast.show(mContext, getContext().getString(R.string.input_num_no_exit), CustomToast.LENGTH_SHORT);
        }
    }


    @Override
    public void show() {
        clearInvite();
        super.show();
    }


    @Override
    public void dismiss() {
        hideInputKeyBoard();
        super.dismiss();
    }


    private void hideInputKeyBoard() {
        if (mEditText == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) mEditText.getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(),
                0);
        }
    }


    private void clearInvite() {
        for (int i = 0; i < mDataList.size(); i++) {
            mDataList.get(i).setInvited(false);
        }
        if (mInvitePersonListViewAdapter != null) {
            mInvitePersonListViewAdapter.notifyDataSetChanged();
        }
    }


    private void invitePersonBySMS() {
        InvitePersonView.this.onNotify(NotifyType.INVITE_PERSON_SMS, null);

    }


    private void invitePersonByWeixing() {
        InvitePersonView.this.onNotify(NotifyType.INVITE_PERSON_WEIXING, null);

    }


    private static class InvitePersonListViewAdapter extends BaseAdapter {
        private final String TAG = InvitePersonListViewAdapter.this.getClass()
            .getName();
        private LayoutInflater mInflater;
        private List<Person> mParticipatorList;
        private Context mContext;
        // private AsyncBitmapLoaderUtil mAsyncBitmapLoader;
        private MeetingDisplayImageListener mMeetingDisplayImageListener = null;


        public InvitePersonListViewAdapter(Context context,
                                           List<Person> participatorList) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mParticipatorList = participatorList;
            // mAsyncBitmapLoader = new AsyncBitmapLoaderUtil(mContext);
            mMeetingDisplayImageListener = new MeetingDisplayImageListener(
                mContext);
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


        private void show(RoundImageView roundImageView, String imgUrl) {
            // if (imgUrl == null || imgUrl.equals("")) {
            // roundImageView
            // .setBackgroundResource(R.drawable.meeting_room_menu_person_default_icon);
            // } else {
            // mAsyncBitmapLoader.showImageAsyn(roundImageView, imgUrl,
            // R.drawable.meeting_room_menu_person_default_icon);
            // }

            ImageLoader imageLoader = ImageLoader.getInstance();

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(
                    MResource.getIdByName(mContext, MResource.DRAWABLE,
                        "jmeetingsdk_defaultheadimage"))// 设置图片在下载期间显示的图片
                .showImageForEmptyUri(
                    MResource.getIdByName(mContext, MResource.DRAWABLE,
                        "jmeetingsdk_defaultheadimage"))// 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(
                    MResource.getIdByName(mContext, MResource.DRAWABLE,
                        "jmeetingsdk_defaultheadimage"))// 设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)// 是否緩存都內存中
                .cacheOnDisc(true)// 是否緩存到sd卡上
                .displayer(new RoundedBitmapDisplayer(20))// 设置图片的显示方式 :
                // 设置圆角图片 int
                // roundPixels
                .bitmapConfig(Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
                .build();

            imageLoader.displayImage(imgUrl, roundImageView, options,
                mMeetingDisplayImageListener);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            CustomLog.d(TAG, "position = " + position);
            CustomLog.d(TAG, "imgurl = "
                + mParticipatorList.get(position).getPhoto());
            // if (convertView == null) {
            convertView = mInflater.inflate(MResource.getIdByName(mContext,
                MResource.LAYOUT,
                "meeting_room_menu_invite_person_view_list_item"), null);
            holder = new ViewHolder();
            holder.participatorName = (TextView) convertView
                .findViewById(MResource
                    .getIdByName(mContext, MResource.ID,
                        "meeting_room_menu_invite_person_view_list_item_participator_name"));

            holder.participatorAccountId = (TextView) convertView
                .findViewById(MResource
                    .getIdByName(mContext, MResource.ID,
                        "meeting_room_menu_invite_person_view_list_item_participator_account_id"));

            holder.participatorImg = (RoundImageView) convertView
                .findViewById(MResource
                    .getIdByName(mContext, MResource.ID,
                        "meeting_room_menu_invite_person_view_list_item_participator_img"));

            holder.invitePersonIcon = (TextView) convertView
                .findViewById(MResource
                    .getIdByName(mContext, MResource.ID,
                        "meeting_room_menu_invite_person_view_list_item_invite_person_tip_icon"));

            holder.invitePersonListItem = (FrameLayout) convertView
                .findViewById(MResource
                    .getIdByName(mContext, MResource.ID,
                        "fl_invite_person_list_item"));

            convertView.setTag(holder);
            // } else {
            // holder = (ViewHolder) convertView.getTag();
            // }

            holder.participatorName.setText(mParticipatorList.get(position)
                .getAccountName());
            holder.participatorAccountId.setText(mParticipatorList
                .get(position).getAccountId());

            //防止出现重复的联系人
            if (position > 0 && mParticipatorList.get(position).getAccountId()
                .equals(mParticipatorList.get(position - 1).getAccountId())) {
                holder.invitePersonListItem.setVisibility(View.GONE);
            } else {
                holder.invitePersonListItem.setVisibility(View.VISIBLE);
            }

            if (holder.participatorName.getText().toString() == null
                || holder.participatorName.getText().toString().equals("")) {
                holder.participatorName.setText(mContext.getString(R.string.unnamed));
            }

            boolean isSelected = mParticipatorList.get(position).isSelected();
            boolean isInvited = mParticipatorList.get(position).isInvited();

            show(holder.participatorImg, mParticipatorList.get(position)
                .getPhoto());
            // holder.participatorImg
            // .setBackgroundResource(R.drawable.meeting_room_menu_person_default_icon);
            if (isSelected && isInvited) {
                holder.invitePersonIcon.setText(mContext.getString(R.string.have_invite));
                holder.invitePersonIcon.setTextColor(Color.GRAY);
            } else if (isSelected && !isInvited) {
                holder.invitePersonIcon.setText(mContext.getString(R.string.invite));
                holder.invitePersonIcon.setTextColor(Color.argb(255, 51, 181,
                    229));
            } else if (!isSelected && isInvited) {
                holder.invitePersonIcon.setText(mContext.getString(R.string.have_invite));
                holder.invitePersonIcon.setTextColor(Color.GRAY);
            } else {
                holder.invitePersonIcon.setText(mContext.getString(R.string.invite));
                holder.invitePersonIcon.setTextColor(Color.argb(255, 51, 181,
                    229));
            }
            return convertView;
        }


        static class ViewHolder {
            TextView participatorName;
            TextView participatorAccountId;
            TextView invitePersonIcon;
            RoundImageView participatorImg;
            FrameLayout invitePersonListItem;
        }
    }
}