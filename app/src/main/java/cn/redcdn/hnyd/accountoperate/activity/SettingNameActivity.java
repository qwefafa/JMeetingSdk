package cn.redcdn.hnyd.accountoperate.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cn.redcdn.datacenter.usercenter.SetAccountAttr;
import cn.redcdn.datacenter.usercenter.data.BasicInfo;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.HomeActivity;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

public class SettingNameActivity extends BaseActivity {
    private EditText settingName;
    private Boolean isInput = false;
    private String urlMeetingId;//短信链接中的会议号
    private SetAccountAttr sa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_name);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.input_name));
        titleBar.enableBack();
        titleBar.enableRightBtn(getString(R.string.complete), 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInput) {
                    execSubmit(settingName.getText().toString().trim());
                }
            }
        });
        settingName = (EditText) findViewById(R.id.setting_name_edit);
        settingName.addTextChangedListener(settingNameWatcher);
    }

    private TextWatcher settingNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!TextUtils.isEmpty(settingName.getText().toString())) {
                isInput = true;
            } else {
                isInput = false;
            }
        }
    };

    private void execSubmit(final String nickName) {
        String token = AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getAccessToken();
        final SetAccountAttr setaccountatt = new SetAccountAttr() {
            @Override
            protected void onSuccess(BasicInfo responseContent) {
                super.onSuccess(responseContent);
//                 移除提示窗口
                SettingNameActivity.this.removeLoadingView();
                // 修改新的参会名称，返回到MyFileCardActivity后会自动刷新出来
                AccountManager.getInstance(getApplicationContext()).getmAuthInfo().setNickname(nickName);
//                login();
                Intent intent = new Intent(SettingNameActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
//          TODO      MeetingManager.getInstance().setAccountName(newmeetingname);
                // 提示窗口
                CustomToast.show(SettingNameActivity.this,
                        getString(R.string.change_name_success), Toast.LENGTH_LONG);
                CustomLog.d("ChangeMeetingName: ", "OnSuccess");
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                SettingNameActivity.this.removeLoadingView();
                CustomLog.e("ChangeMeetingName: ", statusInfo);

                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(SettingNameActivity.this, getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                    return;
                }
                if (statusCode == SettingData.getInstance().tokenUnExist || statusCode == SettingData.getInstance().tokenInvalid) {
                    AccountManager.getInstance(getApplicationContext()).tokenAuthFail(statusCode);
                }

                CustomToast.show(SettingNameActivity.this,
                        getString(R.string.change_name_fail), Toast.LENGTH_LONG);
            }
        };
        SettingNameActivity.this.showLoadingView(getString(R.string.changeing),
                new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        if (setaccountatt != null)
                            setaccountatt.cancel();
                        CustomToast.show(SettingNameActivity.this,
                                getString(R.string.cancel_change_name), Toast.LENGTH_LONG);
                    }
                }

        );

        setaccountatt.setAccountAttr(token,nickName, null);
    }

    private void login() {
        AccountManager.getInstance(MedicalApplication.shareInstance())
                .registerLoginCallback(new AccountManager.LoginListener() {

                    @Override
                    public void onLoginFailed(int errorCode, String msg) {
                        CustomLog.e(TAG, "自动登录出错!  errorMsg: " + msg);
                        Intent i = new Intent();
                        i.setClass(SettingNameActivity.this, LoginActivity.class);
                        if (urlMeetingId != null && urlMeetingId != "") {
                            i.putExtra("urlMeetingId", urlMeetingId);
                        }
                        startActivity(i);
                        finish();
                    }


                    @Override
                    public void onLoginSuccess() {
                        //此处增加声音检测，防止首次登录成功后立即2次返回，未进行检测
                        SharedPreferences sharedPreferences = getSharedPreferences("VDS", Activity.MODE_PRIVATE);
                        int hasVoiceDetect = sharedPreferences.getInt("hasVoiceDetect", 0);
                        System.out.println("hasVoiceDetect = " + hasVoiceDetect);
                        if (hasVoiceDetect == 1) {
                            CustomLog.i(TAG, "已经检测过，进入主页");
                            Intent i = new Intent(SettingNameActivity.this, HomeActivity.class);
                            if (urlMeetingId != null && urlMeetingId != "") {
                                i.putExtra("urlMeetingId", urlMeetingId);
                            }
                            startActivity(i);
                            finish();
                        } else {
                            System.out.println("未检测过,进行检测");
                            Intent i = new Intent(SettingNameActivity.this, HomeActivity.class);
                            if (urlMeetingId != null && urlMeetingId != "") {
                                i.putExtra("urlMeetingId", urlMeetingId);
                            }
                            startActivity(i);
                            finish();
                        }
                    }
                });
        AccountManager.getInstance(MedicalApplication.shareInstance()).login();
    }
}
