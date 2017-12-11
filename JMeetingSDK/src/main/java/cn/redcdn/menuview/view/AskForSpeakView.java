package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.butelopensdk.ButelOpenSDK;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKNotifyListener;
import cn.redcdn.butelopensdk.ButelOpenSDK.ButelOpenSDKOperationListener;
import cn.redcdn.butelopensdk.constconfig.ButelOpenSDKReturnCode;
import cn.redcdn.butelopensdk.constconfig.NotifyType;
import cn.redcdn.butelopensdk.constconfig.SpeakerInfo;
import cn.redcdn.butelopensdk.vo.Cmd;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.menuview.MenuView;
import cn.redcdn.menuview.vo.Person;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

public abstract class AskForSpeakView extends BaseView {
  private Button hideViewBtn;

  private Context mContext;

  private ButelOpenSDK mButelOpenSDK;

  private MenuView mMenuView;

  private Person mPerson;

  private LinearLayout layout;

  private static String TAG = "AskForSpeakView";

  private Map<String, ChooseSpeakWindowItemView> speakerMap;

  public static final int FROM_MENU_BTN = 0;

  public static final int FROM_MASTER_SET_USER = 1;

  private int from;

  private List<SpeakerInfo> mList = new ArrayList<SpeakerInfo>();

  private OnClickListener btnOnClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      int id = v.getId();
      if (id == MResource.getIdByName(mContext, MResource.ID, "hide_view_btn")) {
        AskForSpeakView.this.onClick(v);
      }/* else if (id == MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_ask_for_speak_mic_1_btn")){
       v.setTag(mPerson);
        AskForSpeakView.this.onClick(v);
       }else if (id == MResource.getIdByName(mContext, MResource.ID,"meeting_room_menu_ask_for_speak_mic_2_btn")){
       v.setTag(mPerson);
        AskForSpeakView.this.onClick(v);
       }*/

    }
  };

  private ButelOpenSDKNotifyListener mButelOpenSDKNotifyListener = new ButelOpenSDKNotifyListener() {
    @Override
    public void onNotify(int arg0, Object object) {
      Cmd resp = null;
      if (object instanceof Cmd) {
        resp = (Cmd) object;
      }
      if (resp != null) {
        switch (arg0) {
          case NotifyType.START_SPEAK:
            CustomLog.d("AskForSpeakView", "NotifyType.START_SPEAK ");
            if(AskForSpeakView.this.isShowing()){
              CustomLog.d("AskForSpeakView", "AskForSpeakView isShowing");
              AskForSpeakView.this.dismiss();
            }
            break;
          case NotifyType.SPEAKER_ON_LINE:  
        	  if(AskForSpeakView.this.isShowing()){
                  CustomLog.d("AskForSpeakView", "AskForSpeakView isShowing");
                  AskForSpeakView.this.dismiss();
                }
            //speakerOnLine(resp.getAccountId(), resp.getUserName());
            break;
          case NotifyType.SPEAKER_OFF_LINE:
          case NotifyType.STOP_SPEAK:
        	  if(AskForSpeakView.this.isShowing()){
                  CustomLog.d("AskForSpeakView", "AskForSpeakView isShowing");
                  AskForSpeakView.this.dismiss();
                }
            //speakerOffLine(resp.getAccountId());
            break;
          /* case NotifyType.SERVER_NOTICE_NEW_MAIN_SPEAKER:
             setMainSpeakOn(resp.getAccountId(),resp.getUserName());
             break;
           case NotifyType.SERVER_NOTICE_MAIN_SPEAKER_INVALID:
             setMainSpeakOff(resp.getAccountId(),resp.getUserName());
             break;*/
          default:
            break;
        }
      }
    }
  };

  public void setPerson(Person p) {

    mPerson = p;
  }

  public AskForSpeakView(Context context, ButelOpenSDK butelOpenSDK, MenuView menuView) {
    super(context, MResource.getIdByName(context, MResource.LAYOUT, "meeting_room_menu_ask_for_speak_view"));
    mContext = context;
    mButelOpenSDK = butelOpenSDK;
    mMenuView = menuView;
    mButelOpenSDK.addButelOpenSDKNotifyListener(mButelOpenSDKNotifyListener);
    speakerMap = new HashMap<String, ChooseSpeakWindowItemView>();
    hideViewBtn = (Button) findViewById(MResource.getIdByName(mContext, MResource.ID, "hide_view_btn"));
    hideViewBtn.setOnClickListener(btnOnClickListener);
    layout = (LinearLayout) findViewById(MResource.getIdByName(mContext, MResource.ID, "ask_for_speaker_layout"));

  }

  private void setStartSpeak(String accountId, Person p) {
    CustomLog.d("AskForSpeakView", "setStartSpeak " + accountId + " p " + p);
    //调用sdk接口，指定发言
    int result;
    if (from == FROM_MENU_BTN) {
      result = mButelOpenSDK.askForSpeak(new ButelOpenSDKOperationListener() {

        @Override
        public void onOperationSuc(Object object) {
          CustomLog.d(TAG, "发言成功 ");
          mMenuView.dismissAskForSpeakView();
        }

        @Override
        public void onOperationFail(Object object) {
          CustomLog.d(TAG, "发言失败 ");
          if (object != null && ((Cmd) object).getStatus() == -982) {
            CustomToast.show(mContext, mContext.getString(R.string.no_internet_check_internet), Toast.LENGTH_LONG);
          }
          else {
            CustomToast.show(mContext, mContext.getString(R.string.speak_fail), CustomToast.LENGTH_SHORT);
          }
        }
      }, accountId);
      if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
        CustomLog.d(TAG, "发言失败 ");
        CustomToast.show(mContext, mContext.getString(R.string.speak_fail), CustomToast.LENGTH_SHORT);
      }
    }
    else {
      result = mButelOpenSDK.masterSetUserStartSpeakOnMic(p.getAccountId(), p.getName(), accountId,
          new ButelOpenSDKOperationListener() {

            @Override
            public void onOperationFail(Object arg0) {
              CustomLog.d(TAG, "指定发言失败 ");
              if (arg0 != null && ((Cmd) arg0).getStatus() == -982) {
                CustomToast.show(mContext, mContext.getString(R.string.no_internet_check_internet), Toast.LENGTH_LONG);
              }
              else {
                CustomToast.show(mContext, mContext.getString(R.string.command_speak_fail), CustomToast.LENGTH_SHORT);
              }
            }

            @Override
            public void onOperationSuc(Object arg0) {
              CustomLog.d(TAG, "指定发言 onOperationSuc ");
              mMenuView.dismissAskForSpeakView();
            }
          });
      if (result < ButelOpenSDKReturnCode.RETURN_NEED_CALLBCAK_SUCCESS) {
        CustomLog.d(TAG, "指定发言失败 ");
        CustomToast.show(mContext, mContext.getString(R.string.command_speak_fail), CustomToast.LENGTH_SHORT);
      }
    }
  }

  public void setAskForSpeakViewShow(int from) {
    CustomLog.d("AskForSpeakView", "setAskForSpeakViewShow");
    this.from = from;
    if (mButelOpenSDK.getSpeakers() != null) {
      CustomLog.d("AskForSpeakView", "mButelOpenSDK.getSpeakers() size " + mButelOpenSDK.getSpeakers().size() + " "
          + mButelOpenSDK.getSpeakers().toString());
      mList.clear();
      speakerMap.clear();
      layout.removeAllViews();
      mList.addAll(mButelOpenSDK.getSpeakers());
      if (mList.size() > 0) {
        for (int i = 0; i < mList.size(); i++) {
          speakerOnLine(mList.get(i).getAccountId(), mList.get(i).getAccountName());
        }
      }
      else {
        AskForSpeakView.this.dismiss();
        /* //if(AskForSpeakView.this.isShowing()){
         CustomLog.d("AskForSpeakView", "setAskForSpeakViewShow size <=0");
           ChooseSpeakWindowItemView spareView = new ChooseSpeakWindowItemView(mContext,null,"空闲",false){

             @Override
             public void onClick(View v) {
               if(v.getTag()==null){
                 setStartSpeak("",mPerson);
               }else{
               setStartSpeak((String)v.getTag(),mPerson);
               }      
             }

             @Override
             public void onNotify(int notifyType, Object object) {
               
             }};
           layout.addView(spareView);
         }*/
      }
    }
  }

  /* private void setMainSpeakOff(String accountId,String accountName){
     if(speakerMap!=null&&speakerMap.get(accountId)!=null){
       speakerMap.get(accountId).changeMainSpeakType(false);
     }
   }
   private void setMainSpeakOn(String accountId,String accountName){
     if(speakerMap!=null&&speakerMap.get(accountId)!=null){
       speakerMap.get(accountId).changeMainSpeakType(true);
     } 
   }*/
  public void speakerOnLine(String accountId, String accountName) {
    CustomLog.d("AskForSpeakView", "speakerOnLine 发言者产生micId " + accountId + " accountName " + accountName
        + " speakerMap.size() " + speakerMap.size());
    if (speakerMap != null && speakerMap.get(accountId) != null) {
      CustomLog.d("AskForSpeakView", "speakerOnLine 发言者已经存在了！");
      return;
    }
    if (accountName == null || accountName.equals("")) {
      accountName = "未命名";
    }
    ChooseSpeakWindowItemView speakerView = null;
    ChooseSpeakWindowItemView spareView = null;
    switch (speakerMap.size()) {
      case 0:
        CustomLog.d("AskForSpeakView", "speakerOnLine case 0 ");
        if (layout.getChildCount() == 0) {
          CustomLog.d("AskForSpeakView", "layout.getChildCount()==0");
          speakerView = new ChooseSpeakWindowItemView(mContext, accountId,
              CommonUtil.getLimitSubstring(accountName, 6), true) {

            @Override
            public void onClick(View v) {
              if (v.getTag() == null) {
                setStartSpeak("", mPerson);
              }
              else {
                setStartSpeak((String) v.getTag(), mPerson);
              }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
          };
          spareView = new ChooseSpeakWindowItemView(mContext, null, "空闲", false) {

            @Override
            public void onClick(View v) {
              if (v.getTag() == null) {
                setStartSpeak("", mPerson);
              }
              else {
                setStartSpeak((String) v.getTag(), mPerson);
              }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
          };
          //speakerView.show();
          //spareView.show();
          layout.addView(speakerView);
          layout.addView(spareView);
          CustomLog.d("AskForSpeakView", "layout.getChildCount() " + layout.getChildCount());
          speakerMap.put(accountId, speakerView);
        }
        else if (speakerMap.size() < layout.getChildCount()) {
          speakerView = new ChooseSpeakWindowItemView(mContext, accountId, CommonUtil.getLimitSubstring(accountName, 6), true) {

            @Override
            public void onClick(View v) {
              if (v.getTag() == null) {
                setStartSpeak("", mPerson);
              }
              else {
                setStartSpeak((String) v.getTag(), mPerson);
              }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
          };
          //speakerView.show();
          layout.addView(speakerView, speakerMap.size());
          speakerMap.put(accountId, speakerView);
        }
        break;
      case 1:
      case 2:
        CustomLog.d("AskForSpeakView", "speakerOnLine case 1 \2 ");
        if (speakerMap.size() < layout.getChildCount()) {
          speakerView = new ChooseSpeakWindowItemView(mContext, accountId, CommonUtil.getLimitSubstring(accountName, 6), true) {

            @Override
            public void onClick(View v) {
              if (v.getTag() == null) {
                setStartSpeak("", mPerson);
              }
              else {
                setStartSpeak((String) v.getTag(), mPerson);
              }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
          };
          //speakerView.show();
          layout.addView(speakerView, speakerMap.size());
          speakerMap.put(accountId, speakerView);
        }
        break;
      case 3:
        CustomLog.d("AskForSpeakView", "speakerOnLine case 3 ");
        if (speakerMap.size() < layout.getChildCount()) {
          speakerView = new ChooseSpeakWindowItemView(mContext, accountId, CommonUtil.getLimitSubstring(accountName, 6), true) {

            @Override
            public void onClick(View v) {
              if (v.getTag() == null) {
                setStartSpeak("", mPerson);
              }
              else {
                setStartSpeak((String) v.getTag(), mPerson);
              }
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
          };
          //speakerView.show();
          layout.addView(speakerView, speakerMap.size());
          speakerMap.put(accountId, speakerView);
        }
        break;

    }

  }

  public void speakerOffLine(String accountId) {
    if (speakerMap != null && speakerMap.get(accountId) == null) {
      CustomLog.d("AskForSpeakView", "speakerOffLine 发言者不存在了！");
      return;
    }
    ChooseSpeakWindowItemView spareView = null;
    switch (speakerMap.size()) {
      case 1:
      case 2:
      case 3:
        ChooseSpeakWindowItemView view = speakerMap.get(accountId);
        if (view != null) {
          layout.removeView(view);
          speakerMap.remove(accountId);
        }
        break;
      case 4:
        ChooseSpeakWindowItemView view2 = speakerMap.get(accountId);
        if (view2 != null) {
          layout.removeView(view2);
          speakerMap.remove(accountId);
          spareView = new ChooseSpeakWindowItemView(mContext, null, "空闲", false) {

            @Override
            public void onClick(View v) {
              setStartSpeak((String) v.getTag(), mPerson);
            }

            @Override
            public void onNotify(int notifyType, Object object) {

            }
          };
          //spareView.show();
          layout.addView(spareView);
        }
        break;
    }

  }
}