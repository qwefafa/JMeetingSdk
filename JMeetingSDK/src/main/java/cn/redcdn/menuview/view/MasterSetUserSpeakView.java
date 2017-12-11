package cn.redcdn.menuview.view;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.buteldataadapter.DataSet;
import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKOperationListener;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKOperationCode.MeetingStyleStatus;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKReturnCode;
import cn.redcdn.butelopensdk.constconfig.CmdKey;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.interfaces.Contact;
import cn.redcdn.meeting.interfaces.ContactCallback;
import cn.redcdn.meeting.interfaces.ResponseEntry;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

public abstract class MasterSetUserSpeakView extends BaseView {
  private int preIndex = -1;
  private int curIndex = 0;
  private final String TAG = "MasterSetUserSpeakView";
  private boolean hasSetAdapter = false;
  private String mAccountId;
  private ButelOpenSDK mButelOpenSDK;
  private MenuView mMenuView;
  private List<Person> mParticipatorsList = new ArrayList<Person>();
  private MasterSetUserSpeakListViewAdapter listAdapter;
  private ListView MasterSetUserSpeakListView;
  private Handler notifyWorkHandler;
  private Person masterPerson;
  private Person currPerson;
  private Context mContext;
  private boolean isDoOperate = false;
  private WorkHandlerThread mWorkHandlerThread = new WorkHandlerThread(
      "MasterSetUserSpeakViewWorkHandlerThreaed");
  private Handler mUiRreashHandler = new Handler() {
    @SuppressWarnings("unchecked")
    public void handleMessage(Message msg) {
      if (msg.what == 0) {
        mParticipatorsList.clear();
        mParticipatorsList.addAll((List<Person>) msg.obj);
        notifyDataSetChanged();
        if (!isShowing()) {
          CustomLog.e(TAG, "ParticipatorsView not show, need dismiss!");
          dismiss();
        }
      }
    }
  };

  @Override
  public void show() {
    if (listAdapter != null) {
      listAdapter.notifyDataSetChanged();
    }
    super.show();
  }

  private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener = new ButelOpenSDKNotifyListener() {
    @Override
    public void onNotify(int arg0, Object arg1) {
      switch (arg0) {
      case NotifyType.PERSON_JOIN_MEETING:
        handlePersonOperation(arg1, WorkHandlerThread.NOTIFY_PERSON_IN);
        break;
      case NotifyType.PERSON_EXIT_MEETING:
        handlePersonOperation(arg1, WorkHandlerThread.NOTIFY_PERSON_OUT);
        break;
      case NotifyType.PARTICIPANT_CHANGED:
        handleNotifyParticipantorChanged(arg1);
        break;
      case NotifyType.SPEAKER_ON_LINE:
        handlePersonOperation(arg1, WorkHandlerThread.NOTIFY_PERSON_START_SPEAK);
        break;
      case NotifyType.START_SPEAK:
        handlePersonOperation(arg1, WorkHandlerThread.NOTIFY_PERSON_START_SPEAK);
        break;
      case NotifyType.SPEAKER_OFF_LINE:
      case NotifyType.STOP_SPEAK:
        handlePersonOperation(arg1, WorkHandlerThread.NOTIFY_PERSON_STOP_SPEAK);
        break;
      case NotifyType.SERVER_NOTICE_USER_ASK_FOR_RAISE_HAND:
        handlePersonOperation(arg1, WorkHandlerThread.NOTIFY_PERSON_HANG_UP);
        break;
      default:
        break;
      }
    }
  };

  private void handlePersonOperation(Object object, int type) {
    CustomLog.d(TAG, "handlePersonOperation type "+type);
    Message msg = Message.obtain();
    Cmd respModel = (Cmd) object;
    if (respModel != null) {
      Person p = new Person();
      p.setAccountId(respModel.getAccountId());
      p.setName(respModel.getUserName());
      //CustomLog.d(TAG, "handlePersonOperation p "+p.getAccountId()+p.getName());
      msg.obj = p;
      msg.what = type;
      notifyWorkHandler.sendMessage(msg);
    }
    CustomLog.d(TAG, "handlePersonOperation结束  type： " + type);
  }

  public void handleModelChanged() {
    if (mButelOpenSDK.getMeetingMode() ==  MeetingStyleStatus.FREE_MODE ) {
      CustomLog.d(TAG, "handleModelChanged 变到自由模式啦");
      Message msg = Message.obtain();
      msg.what = WorkHandlerThread.NOTIFY_MODE_CHANGE;
      notifyWorkHandler.sendMessage(msg);
    } else {
      CustomLog.d(TAG, "handleModelChanged 变到主持模式啦");
    }
  }

  private void handleNotifyParticipantorChanged(Object object) {
    CustomLog.d(TAG, "handleNotifyParticipantorChanged");
    getParticipatorList(mButelOpenSDK.getParticipatorList());
    CustomLog.d(TAG, "handleNotifyParticipantorChanged结束");
  }

  private void getParticipatorList(JSONArray particitors) {
    if (particitors != null) {
      CustomLog.d(TAG, "getParticipatorList size = " + particitors.length());
      Message msg = Message.obtain();
      msg.obj = particitors;
      msg.what = WorkHandlerThread.MAKE_PARTICIPATOR_MAP;
      notifyWorkHandler.sendMessage(msg);
      CustomLog.d(TAG, "getParticipatorList结束  " + particitors.toString());
    }
  }

  public MasterSetUserSpeakView(ButelOpenSDK butelOpenSDK, Context context,
      MenuView menuView, String accountId) {
    super(context,MResource.getIdByName(context, MResource.LAYOUT,"jmeetingsdk_menu_master_set_user_speak_view" ));
 //   super(context, R.layout.jmeetingsdk_menu_master_set_user_speak_view);
    mContext = context;
    Button hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource. ID,"hide_view_btn" ));
    hideViewBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        MasterSetUserSpeakView.this.onClick(v);

      }
    });
 
    mButelOpenSDK = butelOpenSDK;
    mMenuView = menuView;
   
    mAccountId = accountId;
    initWorkHandlerThread();   
    masterPerson = new Person();
    masterPerson.setAccountId(accountId);
    masterPerson.setName(getContext().getString(R.string.me));
//    masterPerson
//        .setPhoto(AccountManager.getInstance(mContext).getAccountInfo().headUrl);
    //String mic1 = mButelOpenSDK.getMic1UserId();
   // String mic2 = mButelOpenSDK.getMic2UserId();
    if (mButelOpenSDK.getSpeakerInfoById(accountId)!=null) {
      masterPerson.setStatusType(UserType.USER_SPEAK);
    } else {
      masterPerson.setStatusType(UserType.USER_OTHERS);
    }
    getParticipatorList(mButelOpenSDK.getParticipatorList()); 
    mButelOpenSDK.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
    listAdapter = new MasterSetUserSpeakListViewAdapter(context,
        mParticipatorsList, accountId);
    initView();
  }

  protected void initView() {
    MasterSetUserSpeakListView = (ListView) findViewById(MResource.getIdByName(mContext, MResource. ID,"userListView" ));
    if (!hasSetAdapter) {
      hasSetAdapter = true;
      MasterSetUserSpeakListView
          .setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                int arg2, long arg3) {
              // delayHide();
              Person prePerson = null;
              if (preIndex >= 0 && mParticipatorsList.size() > 0) {
                if (preIndex + 1 > mParticipatorsList.size()) {
                  preIndex = 0;
                }
                prePerson = mParticipatorsList.get(preIndex);
              }
              if (prePerson != null) {
                CustomLog.d(TAG, "prePerson.setSelected(false)");
                prePerson.setSelected(false);
              } else {
                CustomLog.d(TAG, "prePerson=null");
              }
              curIndex = arg2;
              Person person = mParticipatorsList.get(arg2);
              person.setSelected(true);
             // notifyDataSetChanged();
              prePerson = person;
              preIndex = curIndex;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
              CustomLog.d(TAG, "onNothingSelected");
            }
          });

      MasterSetUserSpeakListView
          .setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
              CustomLog.d(TAG, "onItemClick");
              // delayHide();
              if (!isDoOperate) {
                mParticipatorsList.get(arg2).setWaitingResult(true);
                isDoOperate = true;
                currPerson = mParticipatorsList.get(arg2);
                if (currPerson.getStatusType() == UserType.USER_SPEAK) {
                  // 取消发言
                  CustomLog.d(TAG, "取消发言 " + currPerson.toString());
       //           MobclickAgent.onEvent(mContext, AnalysisConfig.CLICK_CANCEL_SPEAK_IN_ASSIGN_SPEAK_LIST);
                  if(mButelOpenSDK == null){
                	  CustomLog.d(TAG, "mButelOpenSDK == null ");
                	  return;
                  }
                  int result = mButelOpenSDK.masterSetUserStopSpeakOnMic(
                      currPerson.getAccountId(),
                      new ButelOpenSDKOperationListener() {

                        @Override
                        public void onOperationFail(Object arg0) {
                          isDoOperate = false;
                          currPerson.setWaitingResult(false);
                          CustomLog.d(TAG, "取消发言 取消发言失败 ");
                          notifyDataSetChanged();
                          if (arg0 != null && ((Cmd) arg0).getStatus() == -982) {
                            CustomToast.show(mContext, R.string.no_internet_check_internet,
                                Toast.LENGTH_LONG);
                          } else {
                            CustomToast.show(mContext, R.string.cancel_speak_fail,
                                CustomToast.LENGTH_SHORT);
                          }
                        }

                        @Override
                        public void onOperationSuc(Object arg0) {
                          isDoOperate = false;
                          //currPerson.setWaitingResult(false);
                          //notifyDataSetChanged();
                          CustomLog.d(TAG, "取消发言 onOperationSuc ");
                        }
                      });
                  if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                    isDoOperate = false;
                    currPerson.setWaitingResult(false);
                    CustomLog.d(TAG, "取消发言 取消发言失败 ");
                    CustomToast.show(mContext, getContext().getString(R.string.cancel_speak_fail),
                        CustomToast.LENGTH_SHORT);
                  }

                } else {
        //          MobclickAgent.onEvent(mContext, AnalysisConfig.CLICK_SPEAK_IN_ASSIGN_SPEAK_LIST);
                  if(mButelOpenSDK.getSpeakers()==null||mButelOpenSDK.getSpeakers().size()< 4){
                    CustomLog.d(TAG, "指定发言  " + currPerson.toString());
                    int result = mButelOpenSDK.masterSetUserStartSpeakOnMic(
                        currPerson.getAccountId(), currPerson.getName(),
                        "", new ButelOpenSDKOperationListener() {

                          @Override
                          public void onOperationFail(Object arg0) {
                            isDoOperate = false;
                            CustomLog.d(TAG, "指定发言失败 ");
                            currPerson.setWaitingResult(false);
                            notifyDataSetChanged();
                            if (arg0 != null
                                && ((Cmd) arg0).getStatus() == -982) {
                              CustomToast.show(mContext, R.string.no_internet_check_internet,
                                  Toast.LENGTH_LONG);
                            } else {
                              CustomToast.show(mContext, R.string.command_speak_fail,
                                  CustomToast.LENGTH_SHORT);
                            }
                          }

                          @Override
                          public void onOperationSuc(Object arg0) {
                            isDoOperate = false;
                            //currPerson.setWaitingResult(false);
                            //notifyDataSetChanged();
                            CustomLog.d(TAG, "指定发言 onOperationSuc ");
                          }
                        });
                    if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
                      CustomLog.d(TAG, "指定发言失败 ");
                      isDoOperate = false;
                      currPerson.setWaitingResult(false);
                      CustomToast.show(mContext, R.string.command_speak_fail,
                          CustomToast.LENGTH_SHORT);
                    }
                  } else {
                    // 去选择界面
                    CustomLog.d(TAG, "去选择窗口页面 ");
                    isDoOperate = false;
                    currPerson.setWaitingResult(false);
                    mMenuView.hideMasterSetUserSpeakView();
                    mMenuView.showAskForSpeakView(currPerson,AskForSpeakView.FROM_MASTER_SET_USER);
                  }
                }
                notifyDataSetChanged();
              } else {
                CustomToast.show(mContext, getContext().getString(R.string.request_much_wait_try),
                    CustomToast.LENGTH_SHORT);
              }
            }
          });

      MasterSetUserSpeakListView.setAdapter(listAdapter);
    }

    listAdapter.notifyDataSetChanged();
  }

  private void notifyDataSetChanged() {
    listAdapter.notifyDataSetChanged();
  }

 /* private int getUserMicId(String accountId) {
    int micId = 0;
    if (mButelOpenSDK.getMic1UserId() != null
        && mButelOpenSDK.getMic1UserId().equals(accountId)) {
      micId = 1;
    } else if (mButelOpenSDK.getMic2UserId() != null
        && mButelOpenSDK.getMic2UserId().equals(accountId)) {
      micId = 2;
    }
    return micId;
  }*/

  private void initWorkHandlerThread() {
    mWorkHandlerThread.start();
    notifyWorkHandler = new Handler(mWorkHandlerThread.getLooper(),
        mWorkHandlerThread);
  }

  public class UserType {
    public static final int USER_SPEAK = 0;
    public static final int USER_HANG_UP = 1;
    public static final int USER_OTHERS = 2;
  }

  private class WorkHandlerThread extends HandlerThread implements Callback {
    public static final int MAKE_PARTICIPATOR_MAP = 0;
    public static final int NOTIFY_PERSON_IN = 1;
    public static final int NOTIFY_PERSON_OUT = 2;
    public static final int NOTIFY_PERSON_START_SPEAK = 3;
    public static final int NOTIFY_PERSON_STOP_SPEAK = 4;
    public static final int NOTIFY_PERSON_HANG_UP = 5;
    public static final int NOTIFY_MODE_CHANGE = 6;
    private HashMap<String, Person> speakers = new HashMap<String, Person>();
    private HashMap<String, Person> hangUpUsers = new HashMap<String, Person>();
    private HashMap<String, Person> others = new HashMap<String, Person>();

    public WorkHandlerThread(String name) {
      super(name);
    }

    @Override
    public boolean handleMessage(Message msg) {
      CustomLog.d(TAG, "处理指定发言命令");
      switch (msg.what) {
      case MAKE_PARTICIPATOR_MAP:
        makeParticipatorMap((JSONArray) msg.obj);
        break;
      case NOTIFY_PERSON_IN:
        handlePersonIn((Person) msg.obj);
        break;
      case NOTIFY_PERSON_OUT:
        handlePersonOut((Person) msg.obj);
        break;
      case NOTIFY_PERSON_START_SPEAK:
        handlePersonSpeakOrHangUp((Person) msg.obj, UserType.USER_SPEAK);
        break;
      case NOTIFY_PERSON_STOP_SPEAK:
        handlePersonStopSpeak((Person) msg.obj, UserType.USER_OTHERS);
        break;
      case NOTIFY_PERSON_HANG_UP:
        handlePersonSpeakOrHangUp((Person) msg.obj, UserType.USER_HANG_UP);
        break;
      case NOTIFY_MODE_CHANGE:
        handleModelChanged();
        break;
      default:
        break;
      }
      CustomLog.d(TAG, "处理指定发言命令结束");
      return false;
    }

    private void handleModelChanged() {
      CustomLog.d(TAG, "handleModelChanged");
      for (Map.Entry<String, Person> entry : hangUpUsers.entrySet()) {
        Person p = new Person();
        p.setAccountId(entry.getValue().getAccountId());
        p.setName(entry.getValue().getName());
        p.setStatusType(UserType.USER_OTHERS);
        others.put(p.getAccountId(), p);
      }
      hangUpUsers.clear();
      notifyUpdate(refreshPersonList());
      CustomLog.d(TAG, "handleModelChangedEnd");
    }

    private void makeParticipatorMap(final JSONArray particitors) {
      CustomLog.d(TAG, "makeParticipatorMap  particitors "+particitors.length());
      speakers.clear();
      hangUpUsers.clear();
      others.clear();
      MeetingManager.getInstance().getContactOperationImp().getAllContacts(
          new ContactCallback() {
            @Override
            public void onFinished(ResponseEntry result) {
              HashMap<String, Person> partcipantorPersonList = new HashMap<String, Person>();
              if (result.status >= 0) {
                DataSet adapterDataSetImp = (DataSet) result.content;
                //ContactSetImp adapterDataSetImp = (ContactSetImp) result.content;
                if (adapterDataSetImp == null) {
                  CustomLog.d(TAG, "onContactLoaded 结果为空");
                } else {                
                  for (int i = 0; i < adapterDataSetImp.getCount(); i++) {
                    Contact contact = (Contact) adapterDataSetImp.getItem(i);
                    Person p1 = new Person();
                    p1.setAccountId(contact.getNubeNumber());
                    p1.setAccountName(contact.getNickname());
                    p1.setPhoto(contact.getHeadUrl());
                    partcipantorPersonList.put(contact.getNubeNumber(), p1);
                  }
                }
              }
                  if (particitors != null && particitors.length()>0) {
                    for (int i = 0; i < particitors.length(); i++) {
                      try {
                        JSONObject paticitorObj = particitors.getJSONObject(i);
                        if(paticitorObj != null&&paticitorObj.optString(CmdKey.ACOUNT_ID)!=null){                      
                        Person p = new Person();
                        p.setAccountId(paticitorObj.optString(CmdKey.ACOUNT_ID));
                        p.setName(paticitorObj.optString(CmdKey.USERNAME));
                        p.setStatusType(UserType.USER_OTHERS);
                        if (partcipantorPersonList
                            .containsKey(p.getAccountId())) {
                          p.setPhoto(partcipantorPersonList.get(
                              p.getAccountId()).getPhoto());
                          CustomLog.d(TAG, "dddddddddd" + p.getPhoto());
                        }
                        if (p.getAccountId().equals(mAccountId)) {
                          continue;
                        }
                        if (mButelOpenSDK!=null && mButelOpenSDK.getSpeakerInfoById(p.getAccountId())!=null) {
                          continue;
                        }
                        others.put(p.getAccountId(), p);
                        }
                      } catch (JSONException e) {
                        e.printStackTrace();
                      }
                      
                    }
                  }
                  if(mButelOpenSDK!=null&&mButelOpenSDK.getSpeakers()!=null
                   &&mButelOpenSDK.getSpeakers().size()>0){
                    for(int i=0;i<mButelOpenSDK.getSpeakers().size();i++){
                      
                      if(mAccountId.equals(mButelOpenSDK.getSpeakers().get(i).getAccountId())){
                        //
                        if (partcipantorPersonList
                            .containsKey(masterPerson.getAccountId())) {
                          masterPerson.setPhoto(partcipantorPersonList.get(
                              masterPerson.getAccountId()).getPhoto());
                          CustomLog.d(TAG, "dddddddddd" + masterPerson.getPhoto());
                        }
                        masterPerson.setWaitingResult(false);
                       
                        masterPerson.setStatusType(UserType.USER_SPEAK);                      
                        masterPerson.setAccountName(getContext().getString(R.string.me));
                         
                      }else{
                        Person p = new Person();
                        p.setAccountId(mButelOpenSDK.getSpeakers().get(i).getAccountId());
                        p.setName(mButelOpenSDK.getSpeakers().get(i).getAccountName());
                        p.setStatusType(UserType.USER_SPEAK);
                        p.setWaitingResult(false);
                        if (partcipantorPersonList
                            .containsKey(p.getAccountId())) {
                          p.setPhoto(partcipantorPersonList.get(
                              p.getAccountId()).getPhoto());
                          CustomLog.d(TAG, "dddddddddd" + p.getPhoto());
                        }             
                         p.setStatusType(UserType.USER_SPEAK);                      
                        speakers.put(p.getAccountId(), p);
                      }
                    }
                  }
                  partcipantorPersonList.clear();
                  partcipantorPersonList = null;
                  notifyUpdate(refreshPersonList());
                }
          },true);
    }

    private boolean isPersonInWorkList(Person p) {
      if (mAccountId.equals(p.getAccountId())) {
        return true;
      }
      if (speakers.containsKey(p.getAccountId())) {
        return true;
      }
      if (hangUpUsers.containsKey(p.getAccountId())) {
        return true;
      }
      if (others.containsKey(p.getAccountId())) {
        return true;
      }
      return false;
    }

    private Person setPersonPhoto(Person p) {
      if (speakers.containsKey(p.getAccountId())) {
        p.setPhoto(speakers.get(p.getAccountId()).getPhoto());
        CustomLog.e("setPersonPhoto", "dddddddd " + p.getPhoto());
        return p;
      }
      if (hangUpUsers.containsKey(p.getAccountId())) {
        p.setPhoto(hangUpUsers.get(p.getAccountId()).getPhoto());
        CustomLog.e("setPersonPhoto", "dddddddd " + p.getPhoto());
        return p;
      }
      if (others.containsKey(p.getAccountId())) {
        p.setPhoto(others.get(p.getAccountId()).getPhoto());
        CustomLog.e("setPersonPhoto", "dddddddd " + p.getPhoto());
        return p;
      }
      return p;
    }

    private void removePerson(Person p) {
      CustomLog.d(TAG, "removePerson");
      if (speakers.containsKey(p.getAccountId())) {
        speakers.remove(p.getAccountId());
        return;
      }
      if (hangUpUsers.containsKey(p.getAccountId())) {
        hangUpUsers.remove(p.getAccountId());
        return;
      }
      if (others.containsKey(p.getAccountId())) {
        others.remove(p.getAccountId());
        return;
      }
      CustomLog.d(TAG, "removePersoneND");
    }

    private List<Person> refreshPersonList() {
      CustomLog.d(TAG, "refreshPersonList");
      List<Person> tmpParticipatorList = new ArrayList<Person>();
      tmpParticipatorList.add(masterPerson);
      tmpParticipatorList.addAll(new ArrayList<Person>(speakers.values()));
      tmpParticipatorList.addAll(new ArrayList<Person>(hangUpUsers.values()));
      tmpParticipatorList.addAll(new ArrayList<Person>(others.values()));
      CustomLog.d(TAG, "refreshPersonListEDN");
      return tmpParticipatorList;
    }

    private void handlePersonIn(Person p) {
      CustomLog.d(TAG, "handlePersonIn");
      if (p == null || isPersonInWorkList(p)) {
        CustomLog
            .d(TAG, "handlePersonIn person==null or person aready in list");
        return;
      }
      Person person = new Person();
      person.setAccountId(p.getAccountId());
      person.setAccountName(p.getAccountName());
      person.setStatusType(UserType.USER_OTHERS);
      person.setPhoto(MeetingManager.getInstance().getContactOperationImp().getHeadUrlByNube(
          p.getAccountId()));
      person.setWaitingResult(false);
      CustomLog.e("ddddddddddddd", "dddddddd " + person.getPhoto());
      others.put(person.getAccountId(), person);
      notifyUpdate(refreshPersonList());
      CustomLog.d(TAG, "handlePersonIn结束");
    }

    private void handlePersonOut(Person p) {
      CustomLog.d(TAG, "handlePersonOut");
      if (p == null) {
        CustomLog.d(TAG, "handlePersonOut person==null");
        return;
      }
      removePerson(p);
      notifyUpdate(refreshPersonList());
      CustomLog.d(TAG, "handlePersonOut结束");
    }

    private void handlePersonStopSpeak(Person p, int type) {
      CustomLog.d(TAG, "handlePersonOut");
      if (p == null) {
        CustomLog.d(TAG, "handlePersonStopSpeak person==null");
        return;
      }
      if (p.getAccountId().equals(mAccountId)) {
        masterPerson.setStatusType(UserType.USER_OTHERS);
        masterPerson.setWaitingResult(false);
        notifyUpdate(refreshPersonList());
        // Message msg = Message.obtain();
        // msg.what = 1;
        // msg.arg1 = UserType.USER_OTHERS;
        // mUiRreashHandler.sendMessage(msg);
        return;
      }
      if (speakers.containsKey(p.getAccountId())) {
        p = setPersonPhoto(p);
        speakers.remove(p.getAccountId());
        p.setStatusType(UserType.USER_OTHERS);
        p.setWaitingResult(false);
        others.put(p.getAccountId(), p);
        notifyUpdate(refreshPersonList());
      }
      CustomLog.d(TAG, "handlePersonStopSpeak结束");
    }

    private void handlePersonSpeakOrHangUp(Person p, int type) {
      CustomLog.d(TAG, "handlePersonSpeakOrHangUp");
      if (p == null) {
        CustomLog.d(TAG, "handlePersonSpeakOrHangUp person==null");
        return;
      }
      CustomLog.e(TAG, "handlePersonSpeakOrHangUp "+p.getAccountId()+p.getAccountName());
      if (p.getAccountId().equals(mAccountId) && type == UserType.USER_SPEAK) {
        masterPerson.setStatusType(UserType.USER_SPEAK);
        masterPerson.setWaitingResult(false);
        // Message msg = Message.obtain();
        // msg.what = 1;
        // msg.arg1 = UserType.USER_SPEAK;
        // mUiRreashHandler.sendMessage(msg);
        notifyUpdate(refreshPersonList());
        return;
      }
      if (type == UserType.USER_HANG_UP) {
        if (hangUpUsers.containsKey(p.getAccountId())) {
          CustomLog.d(TAG, "handlePersonSpeakOrHangUp 已经在举手了 ");
          return;
        } else {
          p = setPersonPhoto(p);
          removePerson(p);
          p.setStatusType(UserType.USER_HANG_UP);
          p.setWaitingResult(false);
          hangUpUsers.put(p.getAccountId(), p);
        }
      }
      if (type == UserType.USER_SPEAK) {
        if (speakers.containsKey(p.getAccountId())) {
          CustomLog.d(TAG, "handlePersonSpeakOrHangUp 已经在发言了 ");
          return;
        } else {
          p = setPersonPhoto(p);
          removePerson(p);
          p.setStatusType(UserType.USER_SPEAK);
          p.setWaitingResult(false);
          speakers.put(p.getAccountId(), p);
          CustomLog.e(TAG, "handlePersonSpeakOrHangUp USER_SPEAK "+p.toString());
        }
      }
      notifyUpdate(refreshPersonList());
      CustomLog.d(TAG, "handlePersonSpeakOrHangUp结束");
    }

    private void notifyUpdate(List<Person> tmpParticipatorList) {
      Message msg = Message.obtain();
      msg.what = 0;
      msg.obj = tmpParticipatorList;
      mUiRreashHandler.sendMessage(msg);
    }
  }

  @Override
  public void dismiss() {
    super.dismiss();
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
    mButelOpenSDK.removeButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
    mButelOpenSDK = null;
  }
}
