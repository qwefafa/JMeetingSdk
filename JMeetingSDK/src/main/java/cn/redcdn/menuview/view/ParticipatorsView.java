package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.MeetingStyleStatus;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.jmeetingsdk.MeetingRoomActivity;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.data.DataChangedListener;
import cn.redcdn.meeting.data.ParticipantorList;
import cn.redcdn.menuview.NotifyType;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;
import cn.redcdn.util.RoundImageView;

public abstract class ParticipatorsView extends BaseView {
  private final String TAG = ParticipatorsView.this.getClass().getName();
  private ListView mParticipatorListView;
  public static ParticipatorListViewAdapter mParticipatorListViewAdapter;
  public static List<Person> mDataList = new ArrayList<Person>();
  private static ParticipantorList mParticipantorList;
  private Button hideViewBtn;
  private String mAccountId;
  private Context mContext;
  private TextView mParticipantorCount;
  // private SearchAccount mSearchAccount = null; Datacenter 接口替换
  private MDSAppSearchUsers mdsAppSearchUsers = null;
  private String mToken;
  private ImageView lockButton;
  private static ButelOpenSDK mButelOpenSDK;

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (v.getId() == MResource.getIdByName(mContext, MResource.ID, "hide_view_btn")) {
        ParticipatorsView.this.onClick(v);
      }else if(v.getId()==MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_participators_view_lock_icon")){
    	 ParticipatorsView.this.onClick(v);
      }
    }
  };

  public void setLock(int lockInfo) {
	    if (lockInfo == 0) {
	      lockButton
	          .setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE, "meeting_room_menu_participators_view_lock"));
	    } else if (lockInfo == 1) {
	      lockButton
	          .setBackgroundResource(MResource.getIdByName(mContext, MResource.DRAWABLE, "meeting_room_menu_participators_view_unlock"));
	    }
	  }

	  public void setLockEnable(int userType) {
	    if (userType == MeetingStyleStatus.MASTER_MODE ) {//主持人模式
	      lockButton.setVisibility(View.INVISIBLE);
	    } else {
	      lockButton.setVisibility(View.INVISIBLE);
	    }
	  }

  public ParticipatorsView(Context context,ButelOpenSDK butelOpenSDK,
      ParticipantorList participantorList, String accountId, String token) {
    super(context,  MResource.getIdByName(context, MResource.LAYOUT, "meeting_room_menu_participators_view"));
    mToken=token;
    mAccountId = accountId;
    mContext = context;
    mButelOpenSDK = butelOpenSDK;

    mParticipantorCount = (TextView) findViewById( MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_participators_view_participator_count"));
    mParticipatorListView = (ListView) findViewById( MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_participators_view_listview"));

    mParticipantorList = participantorList;
    mParticipantorList.setDataChangedListener(new DataChangedListener() {
      @Override
      public void onDataChanged(List<Person> dataList,
          List<Person> participatorList) {
        CustomLog.d(TAG, "收到刷新列表通知。列表大小：" + dataList.size());
        ParticipatorsView.this.mDataList.clear();
        ParticipatorsView.this.mDataList.addAll(dataList);
        if (mParticipantorCount != null) {
          mParticipantorCount.setText(getContext().getString(R.string.join_meetting_people) + dataList.size());
        }
        mParticipatorListViewAdapter.notifyDataSetChanged();
      }
    });

    mParticipatorListViewAdapter = new ParticipatorListViewAdapter(context,
        mDataList, accountId);

    mParticipatorListView.setAdapter(mParticipatorListViewAdapter);
    mParticipatorListViewAdapter.notifyDataSetChanged();

    hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "hide_view_btn"));
    hideViewBtn.setOnClickListener(btnOnClickListener);

    lockButton = (ImageView) findViewById(MResource.getIdByName(mContext, MResource. ID,"meeting_room_menu_participators_view_lock_icon" ));
    lockButton.setOnClickListener(btnOnClickListener);

    //取消掉添加按钮功能
    // mParticipatorListView.setOnItemClickListener(new OnItemClickListener() {
    //   @Override
    //   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
    //       long arg3) {
    //     if (!mDataList.get(arg2).isInTXL()) {
    //     //  mDataList.get(arg2).setInTXL(true);
    //       inviteInputPerson(arg2);
    //     } else {
    //       if (mDataList.get(arg2).getAccountId().equals(mAccountId)) {
    //         // CustomToast.show(mContext, "请不要将自己添加至通讯录！", Toast.LENGTH_LONG);
    //       } else {
    //         CustomToast.show(mContext, "已添加至通讯录！", Toast.LENGTH_LONG);
    //       }
    //     }
    //     mParticipatorListViewAdapter.notifyDataSetChanged();
    //   }
    // });
  }

  private void inviteInputPerson(final int arg2) {

    mdsAppSearchUsers = new MDSAppSearchUsers() {
      @Override protected void onSuccess(List<MDSDetailInfo> responseContent) {
        super.onSuccess(responseContent);
      // protected void onSuccess(List<Users> responseContent) {
        if (responseContent.size() > 0) {
          // Users user = responseContent.get(0);
          MDSDetailInfo mMDSDetailInfo=responseContent.get(0);
          if((arg2+1)<=mDataList.size()){
        	  mDataList.get(arg2).setInTXL(true);
//            InfoReportManager.notifyAddOtherToAddrList();
              mParticipatorListViewAdapter.notifyDataSetChanged();
              ParticipatorsView.this.onNotify(NotifyType.ADD_TO_TXL, mMDSDetailInfo);
          } else {
        	  CustomToast.show(mContext, getContext().getString(R.string.get_nubinfo_error), CustomToast.LENGTH_SHORT);
          }

        } else {
          CustomToast.show(mContext, getContext().getString(R.string.get_nubinfo_error), CustomToast.LENGTH_SHORT);
        }
      }

      @Override
      protected void onFail(int statusCode, String statusInfo) {
        CustomToast.show(mContext, getContext().getString(R.string.get_nubinfo_error), CustomToast.LENGTH_SHORT);
      }
    };

    String[] str = new String[1];
    str[0] = mDataList.get(arg2).getAccountId();
    mdsAppSearchUsers.appSearchUsers(mToken, 0, str);
  }

  @Override
  public void show() {
    if (mParticipatorListViewAdapter != null) {
      mParticipatorListViewAdapter.notifyDataSetChanged();
    }
    super.show();
  }

  public static class ParticipatorListViewAdapter extends BaseAdapter {
    private final String TAG = ParticipatorListViewAdapter.this.getClass()
        .getName();
    private LayoutInflater mInflater;
    private List<Person> mParticipatorList;
    private String mAccountId;
    private Context mContext;
  //  private AsyncBitmapLoaderUtil mAsyncBitmapLoader;
    private MeetingDisplayImageListener mMeetingDisplayImageListener = null;

    public ParticipatorListViewAdapter(Context context,
        List<Person> participatorList, String accountId) {
      mInflater = LayoutInflater.from(context);
      mParticipatorList = participatorList;
      mAccountId = accountId;
      mContext = context;
  //    mAsyncBitmapLoader = new AsyncBitmapLoaderUtil(mContext);
      mMeetingDisplayImageListener=new MeetingDisplayImageListener(mContext);
    }

    @Override
    public int getCount() {
      return mParticipatorList.size();
    }

    @Override
    public Object getItem(int arg0) {
      return null;
    }

    @Override
    public long getItemId(int arg0) {
      return 0;
    }

    private void show(RoundImageView roundImageView, String imgUrl) {
//      mAsyncBitmapLoader.showImageAsyn(roundImageView, imgUrl,
//          R.drawable.meeting_room_menu_person_default_icon);
      ImageLoader imageLoader = ImageLoader.getInstance();
  	
  	DisplayImageOptions options = new DisplayImageOptions.Builder()
    .showStubImage(MResource.getIdByName(mContext, MResource.DRAWABLE, "jmeetingsdk_defaultheadimage"))//设置图片在下载期间显示的图片
    .showImageForEmptyUri(MResource.getIdByName(mContext, MResource.DRAWABLE, "jmeetingsdk_defaultheadimage"))//设置图片Uri为空或是错误的时候显示的图片
    .showImageOnFail(MResource.getIdByName(mContext, MResource.DRAWABLE, "jmeetingsdk_defaultheadimage"))//设置图片加载/解码过程中错误时候显示的图片
    .cacheInMemory(true)//是否緩存都內存中
    .cacheOnDisc(true)//是否緩存到sd卡上
    .displayer(new RoundedBitmapDisplayer(20))//设置图片的显示方式 : 设置圆角图片  int roundPixels
    .bitmapConfig(Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
        .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
    .build();
  	
  	imageLoader.displayImage(imgUrl, 
  			roundImageView,
  			options,
  			mMeetingDisplayImageListener);
    }

    @SuppressWarnings("unchecked")
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;
      // if (convertView == null) {
      convertView = mInflater.inflate(
          MResource.getIdByName(mContext, MResource.LAYOUT, "meeting_room_menu_participator_view_list_item"), null);
      holder = new ViewHolder();
      holder.participatorName = (TextView) convertView
          .findViewById(MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_participator_view_list_item_participator_name"));
      holder.participatorAccountId = (TextView) convertView
          .findViewById(MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_participator_view_list_item_participator_account_id"));
      holder.addToTxlIcon = (TextView) convertView
          .findViewById(MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_participator_view_list_item_add_to_txl_btn"));
      holder.participatorImg = (RoundImageView) convertView
          .findViewById(MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_participator_view_list_item_participator_img"));
      holder.audioSpeakerOff = (ImageView) convertView
    	  .findViewById(MResource.getIdByName(mContext, MResource.ID, "meeting_room_menu_audio_speaker_off_img"));	  
      convertView.setTag(holder);
      // } else {
      // holder = (ViewHolder) convertView.getTag();
      // }

      holder.participatorName.setText(mParticipatorList.get(position)
          .getAccountName());
      holder.participatorAccountId.setText(mParticipatorList.get(position)
          .getAccountId()); 
      CustomLog.d(TAG,"mParticipatorList.get(position).getAccountId():"+mParticipatorList.get(position).getAccountId());
      CustomLog.d(TAG,"mParticipatorList.get(position).getLoudSpeakerStatus():"+mParticipatorList.get(position).getLoudSpeakerStatus());
      if((mParticipatorList.get(position).getLoudSpeakerStatus()==2)){    	  
    	 holder.audioSpeakerOff.setVisibility(View.VISIBLE);
      }else{
     	 holder.audioSpeakerOff.setVisibility(View.GONE);
      }
      
      CustomLog.d(TAG,"MeetingRoomActivity.mLoudspeakerInfoList.size():"+String.valueOf(MeetingRoomActivity.mLoudspeakerInfoList.size()));      
  	  
          for (int a=0; a<MeetingRoomActivity.mLoudspeakerInfoList.size();a++){
        	  CustomLog.d(TAG, "MeetingRoomActivity.mLoudspeakerInfoList.get(a).getAccountId():"+MeetingRoomActivity.mLoudspeakerInfoList.get(a).getAccountId()
        			  +" MeetingRoomActivity.mLoudspeakerInfoList.get(a).getLoudspeakerStatus():"+String.valueOf(MeetingRoomActivity.mLoudspeakerInfoList.get(a).getLoudspeakerStatus()));
              if(mParticipatorList.get(position).getAccountId().equals(MeetingRoomActivity.mLoudspeakerInfoList.get(a).getAccountId())){
            	  if(MeetingRoomActivity.mLoudspeakerInfoList.get(a).getLoudspeakerStatus()==2){
            	    	 holder.audioSpeakerOff.setVisibility(View.VISIBLE);
            	  }
            	  else if(MeetingRoomActivity.mLoudspeakerInfoList.get(a).getLoudspeakerStatus()==1){
            	     	 holder.audioSpeakerOff.setVisibility(View.GONE);
            	  }
              }
          }
            
      if (holder.participatorName.getText().toString() == null
          || holder.participatorName.getText().toString().equals("")) {
        holder.participatorName.setText(R.string.unnamed);
      }

      boolean isSelected = mParticipatorList.get(position).isSelected();
      boolean isInTXL = mParticipatorList.get(position).isInTXL();
      if (isSelected && isInTXL) {
        holder.addToTxlIcon.setText(R.string.have_add);
        holder.addToTxlIcon.setTextColor(Color.GRAY);
      } else if (isSelected && !isInTXL) {
        holder.addToTxlIcon.setText(R.string.add);
        holder.addToTxlIcon.setTextColor(Color.argb(255, 51, 181, 229));
      } else if (!isSelected && isInTXL) {
        holder.addToTxlIcon.setText(R.string.have_add);
        holder.addToTxlIcon.setTextColor(Color.GRAY);
      } else {
        holder.addToTxlIcon.setText(R.string.add);
        holder.addToTxlIcon.setTextColor(Color.argb(255, 51, 181, 229));
      }

      if (mParticipatorList.get(position).getAccountId().equals(mAccountId)) {
        mParticipatorList.get(position).setInTXL(true);
        holder.addToTxlIcon.setVisibility(View.INVISIBLE);
      }

      //取消掉添加按钮
      holder.addToTxlIcon.setVisibility(View.GONE);

      // mAsyncBitmapLoader.showImageAsynByNube(holder.participatorImg,
      // mParticipatorList.get(position).getAccountId(),
      // R.drawable.meeting_room_menu_person_default_icon);
      show(holder.participatorImg, mParticipatorList.get(position).getPhoto());

      // holder.participatorImg
      // .setBackgroundResource(R.drawable.meeting_room_menu_person_default_icon);      
      return convertView;      
    }
        
    static class ViewHolder {
      TextView participatorName;
      TextView participatorAccountId;
      TextView addToTxlIcon;
      RoundImageView participatorImg;
      ImageView audioSpeakerOff;
    }

  }
}