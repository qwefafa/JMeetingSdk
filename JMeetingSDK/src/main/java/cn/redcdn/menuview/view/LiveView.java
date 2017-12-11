package cn.redcdn.menuview.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.CustomToast;
import cn.redcdn.util.MResource;

public class LiveView extends Dialog {

  private EditText name;

  private Button openLiveButton;

  private Context mContext;

  private String liveNameString;

  private TextView txt;

  private BtnOnClickListener btnOnClickListener;

  private ImageView cancleImageView;

  private boolean isHandleEvent = false;

  private static final int IsHandleMsg = 99;

  Handler isHandleEventhandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == IsHandleMsg) {
        isHandleEvent = false;
        System.out.println("200ms到时，isHandleEvent = false");
      }
    }
  };

  public interface BtnOnClickListener {
    public void onClick(LiveView customDialog, int tag, String name);
  }

  /**
   * 构造方法，传入activity的上下文
   * 
   * @param context
   */
  public LiveView(Context context) {
    this(context, MResource.getIdByName(context, MResource.STYLE, "jmetingsdk_dialog"));
  }

  public LiveView(Context context, String name) {
    this(context, MResource.getIdByName(context, MResource.STYLE, "jmetingsdk_dialog"));
    liveNameString = name;
  }

  public LiveView(Context context, int theme) {
    super(context, theme);
    mContext = context;
  }

  public void setLiveName(String livename) {
    if (name != null) {
      name.setText(CommonUtil.getLimitSubstring(livename, 20));
      name.setSelection(name.length());
    }
  }

  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);

    setContentView(MResource.getIdByName(mContext, MResource.LAYOUT, "meeting_room_live_dialog"));
    name = (EditText) this.findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_edit_name"));
    txt = (TextView) this.findViewById(MResource.getIdByName(mContext, MResource.ID, "qn_operate_dialog_body"));
    if (MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
      txt.setText(mContext.getString(R.string.consultation_name));
    }
    else {
      txt.setText(mContext.getString(R.string.meeting_name));
    }
    name.addTextChangedListener(mTextWatcher);
    openLiveButton = (Button) this.findViewById(MResource.getIdByName(mContext, MResource.ID,
        "qn_operae_dialog_center_button"));
    openLiveButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (isHandleEvent == true) {
          System.out.println("触摸过快,返回");
          return;
        }
        isHandleEvent = true;
        Message msg = Message.obtain();
        msg.what = IsHandleMsg;
        msg.obj = v.getId();
        isHandleEventhandler.sendMessageDelayed(msg, 200);
        if (name.getText() == null || name.getText().toString().equals("")) {
          if (MeetingManager.getInstance().getAppType().equals(MeetingManager.MEETING_APP_BUTEL_CONSULTATION)) {
            CustomToast.show(mContext, mContext.getString(R.string.consultation_name_no_null), CustomToast.LENGTH_SHORT);
          }
          else {
            CustomToast.show(mContext, mContext.getString(R.string.meeting_name_not_null), CustomToast.LENGTH_SHORT);
          }

          return;
        }
        if (btnOnClickListener != null) {
          btnOnClickListener.onClick(LiveView.this, 0, name.getText().toString());
        }

      }
    });
    cancleImageView = (ImageView) this.findViewById(MResource.getIdByName(mContext, MResource.ID,
        "qn_operate_dialog_cancle"));
    cancleImageView.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (isHandleEvent == true) {
          System.out.println("触摸过快,返回");
          return;
        }
        isHandleEvent = true;
        Message msg = Message.obtain();
        msg.what = IsHandleMsg;
        msg.obj = v.getId();
        isHandleEventhandler.sendMessageDelayed(msg, 200);
        if (btnOnClickListener != null) {
          btnOnClickListener.onClick(LiveView.this, 1, name.getText().toString());
        }

      }
    });
    setLiveName(liveNameString);
  }

  public void setBtnOnClickListener(BtnOnClickListener btnOnClickListener) {
    this.btnOnClickListener = btnOnClickListener;
  }

  private TextWatcher mTextWatcher = new TextWatcher() {

    private int editStart;

    private int editEnd;

    private int MAX_COUNT = 20;

    public void afterTextChanged(Editable s) {
      editStart = name.getSelectionStart();
      editEnd = name.getSelectionEnd();
      if (editEnd != 0) {
        name.removeTextChangedListener(mTextWatcher);
        while (calculateLength(s.toString()) > MAX_COUNT) {
          s.delete(editStart - 1, editEnd);
          editStart--;
          editEnd--;
        }
        name.setSelection(editStart);
        name.addTextChangedListener(mTextWatcher);
      }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
  };

  private int calculateLength(String etstring) {
    char[] ch = etstring.toCharArray();

    int varlength = 0;
    for (int i = 0; i < ch.length; i++) {
      if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {
        varlength = varlength + 2;
      }
      else {
        varlength++;
      }
    }
    return varlength;
  }
}
