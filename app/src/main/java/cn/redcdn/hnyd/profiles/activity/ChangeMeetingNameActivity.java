package cn.redcdn.hnyd.profiles.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


import cn.redcdn.datacenter.usercenter.SetAccountAttr;
import cn.redcdn.datacenter.usercenter.data.BasicInfo;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

/**
 * Created by Administrator on 2017/2/24.
 */

public class ChangeMeetingNameActivity extends BaseActivity {
    private EditText meetingNameEdit = null;
    private int resultCode = 0;
    private RelativeLayout titleRlt;
    private Button titlerightbtn;

    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setContentView(R.layout.activity_changemeetingname);
        meetingNameEdit = (EditText) findViewById(R.id.attendmeeting_name_edit);
        if (AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNickname().equals("")) {
            AccountManager.getInstance(getApplicationContext()).getmAuthInfo().setNickname(getString(R.string.no_name));
        }
        meetingNameEdit.setText(AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNickname());
        Editable etext = meetingNameEdit.getText();
        Selection.setSelection(etext, etext.length());
        meetingNameEdit.addTextChangedListener(mTextWatcher);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.input_name));
        titleBar.enableBack();
        titleBar.enableRightBtn(getString(R.string.complete), 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changename_sure_func();
            }
        });
        titleRlt = (RelativeLayout) findViewById(R.id.title);
        titlerightbtn = (Button) titleRlt.findViewById(R.id.right_btn);
    }

    private void changename_sure_func() {

        final String newmeetingname = meetingNameEdit.getText().toString().trim();
        String token = AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getAccessToken();
        final SetAccountAttr setaccountatt = new SetAccountAttr() {
            @Override
            protected void onSuccess(BasicInfo responseContent) {
                super.onSuccess(responseContent);
                ChangeMeetingNameActivity.this.removeLoadingView();
                // 修改新的参会名称，返回到MyFileCardActivity后会自动刷新出来
                AccountManager.getInstance(getApplicationContext()).getmAuthInfo().setNickname(newmeetingname);
//          TODO      MeetingManager.getInstance().setAccountName(newmeetingname);
                Intent intent =new Intent();
                ChangeMeetingNameActivity.this.setResult(resultCode,intent);
                ChangeMeetingNameActivity.this.finish();
                // 提示窗口
                CustomToast.show(ChangeMeetingNameActivity.this,
                        getString(R.string.change_name_success), Toast.LENGTH_LONG);
                CustomLog.d("ChangeMeetingName: ", "OnSuccess");
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                ChangeMeetingNameActivity.this.removeLoadingView();
                CustomLog.e("ChangeMeetingName: ",statusInfo);
                CustomLog.e("ChangeMeetingName: ",statusCode + "");

                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(ChangeMeetingNameActivity.this, getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                    return;
                }
                if(statusCode== SettingData.getInstance().tokenUnExist||statusCode==SettingData.getInstance().tokenInvalid)
                {
                    AccountManager.getInstance(getApplicationContext()).tokenAuthFail(statusCode);
                }

                CustomToast.show(ChangeMeetingNameActivity.this,
                        getString(R.string.change_name_fail), Toast.LENGTH_LONG);
            }
        };
        ChangeMeetingNameActivity.this.showLoadingView(getString(R.string.changeing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (setaccountatt != null)
                    setaccountatt.cancel();
                CustomToast.show(ChangeMeetingNameActivity.this,
                        getString(R.string.cancel_change_name), Toast.LENGTH_LONG);
            }
        });

        setaccountatt.setAccountAttr(token, newmeetingname,null);

    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        private int editStart;
        private int editEnd;
        private int MAX_COUNT = 16;

        public void afterTextChanged(Editable s) {
            if (meetingNameEdit.getText().toString().isEmpty()) {
                titlerightbtn.setTextColor(Color.parseColor("#235164"));
                titlerightbtn.setClickable(false);
            } else {
                titlerightbtn.setTextColor(Color.parseColor("#4ec4dd"));
                titlerightbtn.setClickable(true);
            }
            editStart = meetingNameEdit.getSelectionStart();
            editEnd = meetingNameEdit.getSelectionEnd();
            meetingNameEdit.removeTextChangedListener(mTextWatcher);
            while (calculateLength(s.toString()) > MAX_COUNT) {
                s.delete(editStart - 1, editEnd);
                editStart--;
                editEnd--;
            }
            meetingNameEdit.setSelection(editStart);
            meetingNameEdit.addTextChangedListener(mTextWatcher);
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

    };

    private int calculateLength(String etstring) {
        char[] ch = etstring.toCharArray();

        int varlength = 0;
        for (int i = 0; i < ch.length; i++) {
            if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)
                    || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {
                varlength = varlength + 2;
            } else {
                varlength++;
            }
        }
        return varlength;
    }

}
