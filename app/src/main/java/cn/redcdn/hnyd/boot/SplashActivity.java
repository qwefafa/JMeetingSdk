package cn.redcdn.hnyd.boot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.AccountManager.LoginListener;
import cn.redcdn.hnyd.AccountManager.LoginState;
import cn.redcdn.hnyd.HomeActivity;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.accountoperate.activity.LoginActivity;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.util.CommonUtil;
import cn.redcdn.hnyd.util.CustomDialog;
import cn.redcdn.hnyd.util.CustomDialog.OKBtnOnClickListener;
import cn.redcdn.log.CustomLog;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashActivity extends BaseActivity {
    private BootManager mBootManager;
    private String urlMeetingId;//短信链接中的会议号


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN) @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        allowTwiceToExit();
        parseIntent();
        Boolean resultWrite = CommonUtil.selfPermissionGranted(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Boolean resultRead = CommonUtil.selfPermissionGranted(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (!(resultWrite && resultRead)) {
            showDialog();
        } else {
            if (!MedicalApplication.shareInstance().getInitStatus()) {
                boot(); // 如果程序未启动，执行启动逻辑
            } else {
                SplashActivity.this.onBootSuccess();
            }
        }
    }


    private void parseIntent() {
        // 判断是否通过短信链接启动应用
        CustomLog.i(TAG,"parseIntent::");
        Intent intent = getIntent();
        if (intent != null) {
            String url = intent.getDataString();
            CustomLog.i(TAG, "判断是否是通过短信链接启动应用: " + url);
            if (url != null && (url.startsWith("http") || url.startsWith("cn.redcdn.hvs"))) {
                try {
                    //短信链接内容中获取的meetingId
                    urlMeetingId = url.substring("cn.redcdn.hvs://".length(), url.length());
                    if (isNumeric(urlMeetingId)){
                        MedicalApplication.shareInstance().setIsFromMessageLink(true);
                    }else{
                        urlMeetingId="";
                    }
                    CustomLog.i(TAG, "通过短信链接获取到的 meetingId:" + urlMeetingId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            CustomLog.i(TAG, "SplashActivity::parseIntent 不是通过短信链接启动");
        }
    }
    public boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        parseIntent();
        if (MedicalApplication.shareInstance().getInitStatus()) {
            onBootSuccess();
        }
    }


    /**
     * 启动成功 获取登录状态，已登录的情况下检查是否是通过短信链接启动，如果是，则跳转到会诊列表页面
     */
    private void onBootSuccess() {
        CustomLog.i(TAG, "SplashActivity::onBootSuccess 启动成功");
        if (mBootManager != null) {
            mBootManager.release();
            mBootManager = null;
        }
        MedicalApplication.shareInstance().setInit(true);
        LoginState loginState = AccountManager.getInstance(getApplicationContext())
            .getLoginState();
        CustomLog.i(TAG, "SplashActivity::onBootSuccess 登录状态:  " + loginState);
        if (loginState == LoginState.ONLINE) {
            Intent i = new Intent(SplashActivity.this, HomeActivity.class);
            if(urlMeetingId!=null){
                i.putExtra("urlMeetingId", urlMeetingId);
            }
            startActivity(i);
            finish();
        } else if (loginState == LoginState.OFFLINE) {
            login();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBootManager != null && mBootManager.getCurrentStep() == BootManager.MSG_CHECK_APP_VERSION) {
            CustomLog.d(TAG, "SplashActivity::onResume 继续执行检测应用版本");
            mBootManager.retry(mBootManager.getCurrentStep());
        }
    }


    private void onBootFailed(final int step, final int errorLevel,
                              final String errorMsg) {
        CustomLog.e(TAG, "启动出错!  errorMsg: " + errorMsg + "  errorLevel: " + errorLevel + " step: " + step);
        if (MedicalApplication.shareInstance().getInitStatus()) {
            CustomLog.e(TAG, "启动已完成，不处理启动出错信息!");
        }
        CustomDialog dialog = new CustomDialog(this);
        dialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {

            @Override
            public void onClick(CustomDialog customDialog) {
                MedicalApplication.shareInstance().exit();
            }
        });
        dialog.setTip(getString(R.string.networkAbnormalPleaseCheck));
        dialog.removeCancelBtn();
        dialog.setCancelable(false);
        dialog.setOkBtnText(getString(R.string.btn_ok));
        dialog.show();
    }


    private void login() {
        AccountManager.getInstance(MedicalApplication.shareInstance())
            .registerLoginCallback(new LoginListener() {

                @Override
                public void onLoginFailed(int errorCode, String msg) {
                    CustomLog.e(TAG, "自动登录出错!  errorMsg: " + msg);
                    Intent i = new Intent();
                    i.setClass(SplashActivity.this, LoginActivity.class);
                    if(urlMeetingId!=null&&urlMeetingId!=""){
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
                        Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                        if(urlMeetingId!=null&&urlMeetingId!=""){
                            i.putExtra("urlMeetingId", urlMeetingId);
                        }
                        startActivity(i);
                        finish();
                    } else {
                        System.out.println("未检测过,进行检测");
                        Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                        if(urlMeetingId!=null&&urlMeetingId!=""){
                            i.putExtra("urlMeetingId", urlMeetingId);
                        }
                        startActivity(i);
                        finish();
                        // switchToMainActivity();
                    }
                }
            });
        AccountManager.getInstance(MedicalApplication.shareInstance()).login();
    }


    /**
     * 执行应用启动逻辑
     */
    private void boot() {
        mBootManager = new BootManager(this) {
            @Override
            public void onBootSuccess() {
                SplashActivity.this.onBootSuccess();
            }


            @Override
            public void onBootFailed(int step, int errorCode, String errorMsg) {
                SplashActivity.this.onBootFailed(step, errorCode, errorMsg);
            }
        };
        mBootManager.start();
    }


    /**
     * 展示设置权限Dialog
     */
    private void showDialog() {
        CustomDialog cd = new CustomDialog(SplashActivity.this);
        cd.setTip(getString(R.string.giveStoragePermission));
        cd.removeCancelBtn();
        cd.setOkBtnText(getString(R.string.iknow));
        cd.setOkBtnOnClickListener(new OKBtnOnClickListener() {

            @Override
            public void onClick(CustomDialog customDialog) {
                MedicalApplication.shareInstance().exit();
            }
        });
        cd.show();
    }
}
