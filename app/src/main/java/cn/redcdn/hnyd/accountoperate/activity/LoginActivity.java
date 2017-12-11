package cn.redcdn.hnyd.accountoperate.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.AccountManager.LoginListener;
import cn.redcdn.hnyd.HomeActivity;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.im.preference.DaoPreference;
import cn.redcdn.hnyd.im.preference.DaoPreference.PrefType;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;


public class LoginActivity extends BaseActivity {
    private EditText userNameEdit = null;
    private EditText pwdEdit = null;
    private Button registerBtn = null;
    private Button loginBtn = null;
    private Button loginForgetBtn = null;
    private String tag = LoginActivity.class.getName();
    private String mToken;
    private TitleBar titleBar;
    private String meetingID;
    public static final String UNAGREE_REASON = "unagree";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        allowTwiceToExit();
        titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.login));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.enableRightBtn(getString(R.string.register), 0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent();
                        i.setClass(LoginActivity.this, PhoneRegisterActivity.class);
                        startActivity(i);
                        return;
                    }
                });
        titleBar.getSubRightBtn().setTextColor(Color.BLACK);
        initWidget();
        parseIntent();
    }


    private void parseIntent() {
        meetingID = getIntent().getStringExtra("urlMeetingId");
    }


    private void initWidget() {
        userNameEdit = (EditText) this.findViewById(R.id.Login_numdemo_edit);
        userNameEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (int i = 0; i < str.length; i++) {
                        str1 += str[i];
                    }
                    userNameEdit.setText(str1);

                    userNameEdit.setSelection(start);

                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        pwdEdit = (EditText) this.findViewById(R.id.Login_pwddemo_edit);
        loginBtn = (Button) this.findViewById(R.id.login_login_btn);
        loginBtn.setOnClickListener(mbtnHandleEventListener);
        loginForgetBtn = (Button) this.findViewById(R.id.login_forgetpwd_btn);
        loginForgetBtn.setOnClickListener(mbtnHandleEventListener);
        userNameEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((pwdEdit.getText() != null && !pwdEdit.getText().toString()
                        .equalsIgnoreCase(""))
                        && (userNameEdit.getText() != null && !userNameEdit
                        .getText().toString().equalsIgnoreCase(""))) {
                    loginBtn.setBackgroundResource(R.drawable.button_selector);
                    loginBtn.setClickable(true);
                } else {
                    loginBtn.setClickable(false);
                    loginBtn.setBackgroundResource(R.drawable.button_btn_notclick);
                }
            }
        });
        pwdEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((pwdEdit.getText() != null && !pwdEdit.getText().toString()
                        .equalsIgnoreCase(""))
                        && (userNameEdit.getText() != null && !userNameEdit
                        .getText().toString().equalsIgnoreCase(""))) {
                    loginBtn.setBackgroundResource(R.drawable.button_selector);
                    loginBtn.setClickable(true);
                } else {
                    loginBtn.setClickable(false);
                    loginBtn.setBackgroundResource(R.drawable.button_btn_notclick);
                }
            }
        });

        loginBtn.setBackgroundResource(R.drawable.button_btn_notclick);
        loginBtn.setClickable(false);
    }

    @Override
    public void todoClick(int i) {
        switch (i) {
            case R.id.login_forgetpwd_btn:
                loginForgetBtn.setTextColor(Color.parseColor("#1c8dad"));
                gotoResetPwd();
                break;
            case R.id.login_login_btn:

                AccountManager.getInstance(MedicalApplication.shareInstance())
                        .registerLoginCallback(new LoginListener() {

                            @Override
                            public void onLoginFailed(int errorCode, String msg) {
                                LoginActivity.this.removeLoadingView();
                                if (HttpErrorCode.checkNetworkError(errorCode)) {
                                    CustomToast.show(LoginActivity.this,
                                           getString( R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                                    return;
                                }

                                if (NetConnectHelper.getNetWorkType(MedicalApplication.shareInstance()) == NetConnectHelper.NETWORKTYPE_INVALID) {
                                    CustomToast.show(LoginActivity.this,
                                            getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                                    return;
                                }
                                CustomToast.show(LoginActivity.this, msg, Toast.LENGTH_LONG);
                                if (!msg.equals("")) {
                                    if (msg.contains("账号或密码有误")) {
                                        pwdEdit.setText("");
                                    }
                                }
                            }

                            @Override
                            public void onLoginSuccess() {

                                LoginActivity.this.removeLoadingView();
                                CustomLog.e(TAG, "登录成功");
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                if (meetingID == null) {
                                    intent.putExtra("urlMeetingId", meetingID);
                                }
                                intent.putExtra("", "");
                                startActivity(intent);
                                finish();
                                DaoPreference daoPre = MedicalApplication.getPreference();

                                daoPre.setKeyValue(PrefType.LOGIN_NUMBER_CHANGE, AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNubeNumber());
                                daoPre.setKeyValue(PrefType.BEFORE_LOGIN_NUMBER, AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNubeNumber());
                                daoPre.setKeyValue(PrefType.USER_NICKNAME, AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNickname());
                                daoPre.setKeyValue(PrefType.LOGIN_NUBENUMBER, AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNubeNumber());
                                daoPre.setKeyValue(PrefType.LOGIN_MOBILE, AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getMobile());
                                daoPre.setKeyValue(PrefType.LOGIN_ACCESSTOKENID, AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getAccessToken());

                            }

                        });
                LoginActivity.this.showLoadingView(getString(R.string.logining), new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        AccountManager.getInstance(
                                MedicalApplication.shareInstance()).cancelLogin();
                        CustomToast.show(LoginActivity.this, getString(R.string.login_cancel),
                                Toast.LENGTH_LONG);

                    }
                });
                AccountManager.getInstance(MedicalApplication.shareInstance())
                        .login(userNameEdit.getText().toString(),
                                pwdEdit.getText().toString());

                break;

        }

    }
    private void gotoResetPwd() {
        Intent iw = new Intent();
        iw.setClass(LoginActivity.this, SetNewPwdFirstActivity.class);
        startActivity(iw);
    }

}
