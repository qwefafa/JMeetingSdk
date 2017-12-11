package cn.redcdn.hnyd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import cn.redcdn.authentication.agent.AuthenticationAgent;
import cn.redcdn.authentication.server.AuthenticateInfo;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hnyd.accountoperate.activity.LoginActivity;
import cn.redcdn.hnyd.boot.SplashActivity;
import cn.redcdn.hnyd.cdnmanager.UploadManager;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.contacts.contact.manager.ContactManager;
import cn.redcdn.hnyd.database.DatabaseManager;
import cn.redcdn.hnyd.forceoffline.ForceOfflineDialog;
import cn.redcdn.hnyd.im.agent.AppP2PAgentManager;
import cn.redcdn.hnyd.im.manager.FriendsManager;
import cn.redcdn.hnyd.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hnyd.util.CommonUtil;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogMonitor;

import static cn.redcdn.hnyd.AccountManager.TouristState.MEMBER_STATE;
import static cn.redcdn.hnyd.AccountManager.TouristState.TOURIST_STATE;

//import cn.redcdn.datacenter.enterprisecenter.data.AccountInfo;

//import cn.redcdn.contact.manager.ContactManager;
//import cn.redcdn.forceoffline.OfflineMessageManage;
//import cn.redcdn.incoming.HostAgentClient;
//import cn.redcdn.incoming.IncomingMessageManage;
//import cn.redcdn.jmeetingsdk.MeetingManager;
//import cn.redcdn.meeting.interfaces.AccountManagerOperation;
//import cn.redcdn.meetinginforeport.InfoReportManager;

/**
 * token缓存实现：
 * <p>
 * 1.登录成功后缓存用户名、密码、token、头像、服务类型信息，并设置登录时间
 * <p>
 * 2.下次启动直接使用缓存数据，不再进行登录
 * <p>
 * 3.监测上次登录时间与当前时间进行比对，当发现登录已超过27天，自动 重新登录
 * <p>
 * 4.启动 -> 无缓存登录数据 -> 登录 -> 保存登录数据 启动 -> 有缓存登录数据 -> 登录数据27天以内 -> 使用缓存数据 ->
 * 通过SearchAccount接口更新权限启动 -> 有缓存登录数据 -> 登录数据已达到27天 -> 登录 -> 保存登录数据
 */

public class AccountManager {
    private final String TAG = getClass().getName();
    private String accessToken;
    private String nube;
    private String name;
    private String passwd;
    private static AccountManager mInstance;
    private Context mContext;
    private String imei;
    private LoginListener callback;

    private AuthenticateInfo mAuthInfo;

    // 登录成功广播，携带LoginType参数，区分是普通登录还是切换账号登录
    public final static String LOGIN_BROADCAST = "cn.redcdn.meeting.login";
    // 注销广播
    public final static String LOGOUT_BROADCAST = "cn.redcdn.meeting.logout";

    private LoginState loginState = LoginState.OFFLINE;
    private TouristState touristState = TouristState.MEMBER_STATE;

    private AuthenticationAgent authAgent; //统一认证鉴权agent

    private final static String LOGIN_NUBE = "loginNube";
    private final static String LOGIN_PASSWD = "loginPasswd";
    private final static String CACHE_TIME = "cacheTime";

    private boolean USE_PASSWD_LOGIN = false; // 标记是否需要使用用户名、密码登录而不使用缓存数据
    private String token;

    public enum LoginState {
        ONLINE, // 在线状态
        OFFLINE // 离线状态
    }

    public enum TouristState {
        TOURIST_STATE,
        MEMBER_STATE
    }

    private AccountManager(Context context) {
        mContext = context;
    }

    /**
     * 获取实例
     *
     * @param appContext
     * @return
     */
    public static AccountManager getInstance(Context appContext) {
        if (null == mInstance) {
            mInstance = new AccountManager(appContext);
        }

        return mInstance;
    }


    public AuthenticateInfo getmAuthInfo(){
        if (mAuthInfo == null) {
            CustomLog.e(TAG, "AccoutManager::getAccountInfo == null,需要从新登陆");
            exitLoginState();
            if (!MedicalApplication.shareInstance().getInitStatus()) {
                Intent intent = new Intent();
                intent.setClass(mContext, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
            mAuthInfo = new AuthenticateInfo();
            return mAuthInfo;
        }
        return mAuthInfo;
    }



    /**
     * token验证失败后调用，将触发退出登录，并跳转到登录界面
     */
    public void tokenAuthFail(int errorCode) {
        if (getLoginState() == LoginState.OFFLINE) {
            CustomLog
                    .e(TAG,
                            "AccountManager::tokenAuthFail, the state has been set to LoginState.OFFLINE, return!");
            return;
        }
        CustomLog.d(TAG, "AccountManager::tokenAuthFail() errorCode:" + errorCode);
        logout();
        CustomToast.show(mContext, R.string.token_lapse_login, Toast.LENGTH_LONG);
    }

    /**
     * 获取token
     *
     * @return token
     */
    public String getToken() {
        String token = "";
        if (mAuthInfo != null) {
            token = mAuthInfo.getAccessToken();
        }
        return token;
    }

    public String getNube() {
        String nube = "";
        if (mAuthInfo != null) {
            nube = mAuthInfo.getNubeNumber();
        }
        return nube;
    }

    public String getName() {
        String name = "";
        if (mAuthInfo != null) {
            name = mAuthInfo.getNickname();
        }
        return name;
    }

    /**
     * 注册登录回调
     *
     * @param callback {@link LoginListener}
     */
    public void registerLoginCallback(LoginListener callback) {
        CustomLog.i(TAG, "AccountManager::registerLoginCallback");
        this.callback = callback;
    }

    /**
     * 删除登录回调
     *
     * @param callback {@link LoginListener}
     */
    public void unregisterLoginCallback(LoginListener callback) {
        CustomLog.i(TAG, "AccountManager::unregisterLoginCallback");
        if (this.callback != null && this.callback.equals(callback)) {
            this.callback = null;
        }
    }

    /**
     * 统一认证登录 -> MDS登录
     */
    public void login() {
        CustomLog.i(TAG, "login");
        if (nube == null || passwd == null) {
            nube = getLoginSharePre().getString("nube", "");
            passwd = getLoginSharePre().getString("passwd", "");
        }

        if (nube.equals("") || passwd.equals("")) {
            onLoginFailed(-1, "无保存的登录数据");
            return;
        }

        if (!USE_PASSWD_LOGIN && readLoginCache()) {
            CustomLog.i(TAG, "AccountManager::login() 存在缓存登录数据，使用缓存数据");
            USE_PASSWD_LOGIN = false;
            if (getLoginState() == LoginState.OFFLINE) {
                onLoginSuccess();
            }
            return;
        }

        auth();
    }

    /**
     * 恢复登录信息。
     */
    public void recoverLoginInfo() {
        nube = getLoginSharePre().getString("nube", "");
        passwd = getLoginSharePre().getString("passwd", "");
        onLoginSuccess();
    }

    private void onLoginSuccess() {
        KeyEventWrite.write(KeyEventConfig.LOGIN_INFO + "_ok_"
                + mAuthInfo.getNubeNumber());
        touristState=MEMBER_STATE;
        loginState = LoginState.ONLINE;
            setLoginCache();


        if (callback != null) {
            callback.onLoginSuccess();
            unregisterLoginCallback(callback);
        }

        LogMonitor.getInstance().setAccount(getNube());
        DatabaseManager.getInstance().init(mContext, getNube());
        UploadManager.getInstance().init(SettingData.getInstance().CDN_AppId);
        MedicalMeetingManage.getInstance().setNps(SettingData.getInstance().Meeting_Url, SettingData.getInstance().Slave_Meeting_Url);
        MedicalMeetingManage.getInstance().init(new MedicalMeetingManage.OnInitListener() {
            @Override
            public void onInit(String valueDes, int valueCode) {
                CustomLog.d(TAG, "MedicalMeetingManage::init() code: " + valueCode + " des: " + valueDes);

            }
        });
        //好友关系管理类初始化
        FriendsManager.getInstance().init(mContext);
        sendLoginBroadcast();
        ContactManager.getInstance(MedicalApplication.shareInstance()).initData(
                mAuthInfo.getNubeNumber());
        authAgent = null;
    }

    private void onLoginFailed(int statusCode, String statusInfo) {
        CustomLog.e(TAG, "AccountManager::onLoginFailed 登录失败. status: "
                + statusCode + " |info: " + statusInfo);
        if (callback != null) {
            callback.onLoginFailed(statusCode, statusInfo);
            unregisterLoginCallback(callback);
        }
        authAgent = null;
    }

    /**
     * 读取缓存数据
     *
     * @return </p> true: 读到缓存数据
     * </p> false: 未读到缓存数据
     */
    public boolean readLoginCache() {
        boolean result = false;
        final SharedPreferences loginSharePre = getLoginSharePre();

        long cacheTime = loginSharePre.getLong("cacheTime", 0);
        long offsetTime = System.currentTimeMillis() / 1000 - cacheTime;
        if (offsetTime > 27 * 24 * 60 * 60) { // 27天
            CustomLog.i(TAG, "缓存数据超过27天，不再使用缓存数据，进行登录！");
            return result;
        }

        String token = loginSharePre.getString("token", "");
        CustomLog.d(TAG, "readLoginCache() token: " + token);
        if (!TextUtils.isEmpty(token)) {
            mAuthInfo = new AuthenticateInfo();
            mAuthInfo.setAccessToken(token);

            nube = loginSharePre.getString("nube", "");
            passwd = loginSharePre.getString("passwd", "");

            mAuthInfo.setNubeNumber(nube);
            mAuthInfo.setNickname(loginSharePre.getString("nickname", ""));
            mAuthInfo.setMail(loginSharePre.getString("mail", ""));
            mAuthInfo.setMobile(loginSharePre.getString("mobile", ""));

            mAuthInfo.setHeadUrl(loginSharePre.getString("headThumUrl", ""));
            result = true;
        }
        return result;
    }

    // 登录成功且状态为审核通过状态时缓存登录信息
    private void setLoginCache() {
        if (null == mAuthInfo) {
            return;
        }
        CustomLog.i(TAG, "AccountManager::setLoginCache() 缓存登录成功数据");
        Editor edit = getLoginSharePre().edit();

        String token = getLoginSharePre().getString("token", "");
        if (!token.equals(accessToken)) {
            edit.putLong("cacheTime", System.currentTimeMillis() / 1000);
        }

        edit.putString("nube", nube);
        edit.putString("passwd", passwd); //md5加密后的密码
        edit.putString("nickname", mAuthInfo.getNickname());
        edit.putString("token", mAuthInfo.getAccessToken());
        edit.putString("mobile", mAuthInfo.getMobile());
        edit.putString("mail", mAuthInfo.getMail());

        edit.putString("headThumUrl", mAuthInfo.getHeadUrl());
        edit.commit();
    }

    private void clearLoginCache() {
        CustomLog.i(TAG, "AccountManager::clearLoginCache() 清除登录成功数据");
        getLoginSharePre().edit().clear().commit();
    }

    /**
     * 登录
     *
     * @param account 登录账号：视讯号、手机号
     * @param passwd  密码，明文
     */
    public void login(String account, String passwd) {
        CustomLog.i(TAG, "login");
        if (getLoginState() == LoginState.ONLINE) {
            CustomLog.i(TAG, "Account当前处于登录状态，先退出登录!");
            exitLoginState();
        }
        this.nube = account;
        getmAuthInfo().setMobile(account);
        CustomLog.i("AccountManager ", "account ==" + account);
        CustomLog.i("AccountManager ", "passwd =" + passwd);
        this.passwd = CommonUtil.string2MD5(passwd);
        CustomLog.i("AccountManager ", "MD5加密后密码 =" + this.passwd);

        USE_PASSWD_LOGIN = true;
        login();
    }

    public TouristState getTouristState() {
        CustomLog.d(TAG, "AccountManager::getTouristState() " + touristState);
        return touristState;
    }


    /**
     * 取消登录
     */
    public void cancelLogin() {
        if (authAgent != null) {
            authAgent.release();
            authAgent = null;
        }
    }

    public void showForceOfflineDialog() {
        exitLoginState();
        //清除当前缓存的token
        Editor edit = getLoginSharePre().edit();

        edit.putString("token", "");
        edit.commit();
        Intent intent = new Intent();
        ComponentName cn = new ComponentName(mContext.getPackageName(), ForceOfflineDialog.class.getName());
        intent.setComponent(cn);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MedicalApplication.shareInstance().startActivity(intent);
    }

    /**
     * 注销,切换到登录页面
     */
    public void logout() {
        CustomLog.d(TAG, "AccountManager::logout");
        exitLoginState();

        clearLoginCache();
        nube = null;
        passwd = null;

        MedicalApplication.shareInstance().clearTaskStack();
        Intent intent = new Intent();
        intent.setClass(mContext, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 退出登录状态
     */
    public void exitLoginState() {
        CustomLog.d(TAG, "AccountManager::exitLoginState");
        if (getLoginState() == LoginState.OFFLINE) {
            CustomLog
                    .e(TAG,
                            "AccountManager::exitLoginState, the state has been set to LoginState.OFFLINE, return!");
            return;
        }
        USE_PASSWD_LOGIN = true; // 一旦退出登录状态，下次需要使用保存的用户名和密码重新登录，不能再使用缓存数据进行免登录，因为密码有可能已经发生改变
        loginState = LoginState.OFFLINE;
        sendLogoutBroadcast();
        MedicalMeetingManage.getInstance().release();
        FriendsManager.getInstance().release();
        AppP2PAgentManager.destroyAgent();
        ContactManager.getInstance(MedicalApplication.shareInstance()).clearInfos();
        DatabaseManager.getInstance().release();
    }

    /**
     * 获取登录状态
     *
     * @return {@link LoginState}
     */
    public LoginState getLoginState() {
        CustomLog.d(TAG, "AccountManager::getLoginState() " + loginState);
        return loginState;
    }

    private void sendLoginBroadcast() {
        Intent loginIntent = new Intent();
        loginIntent.setAction(LOGIN_BROADCAST);
        mContext.sendBroadcast(loginIntent);
    }

    private void sendLogoutBroadcast() {
        Intent logoutIntent = new Intent();
        logoutIntent.setAction(LOGOUT_BROADCAST);
        mContext.sendBroadcast(logoutIntent);
    }

    private void auth() {
        authAgent = new AuthenticationAgent() {
            @Override
            public void onInit(int i) {
                CustomLog.i(TAG, "鉴权模块:初始化回调  onInit");
                if (i == -1) {
                    onLoginFailed(i, "auth 初始化失败");
                    release();
                }
                String userCenterUrl = SettingData.getInstance().PERSONAL_CENTER_URL;
                String imei = SettingData.AUTH_IMEI;
                String appKey = SettingData.getInstance().AppKey;
                String productId = SettingData.AUTH_PRODUCT_ID;
                String appType = SettingData.AUTH_APPTYPE;
                String deviceType = SettingData.AUTH_DEVICETYPE;
                setUserInfo(userCenterUrl, imei, appKey, nube, passwd, productId,
                        appType, deviceType);
                authenticate(getAuthenticateInfo() == null ? "" : getAuthenticateInfo().getAccessToken());
            }

            @Override
            public void onAuthenticate(int valueCode, AuthenticateInfo authInfo) {
                CustomLog.i(TAG, "鉴权模块:鉴权回调 onAuthenticate");
                mAuthInfo = authInfo;
                if (valueCode == 0) {
                    nube = authInfo.getNubeNumber();
                    name = authInfo.getNickname();
                    accessToken = authInfo.getAccessToken();
                    getmAuthInfo().setMail(authInfo.getMail());
                    getmAuthInfo().setMobile(authInfo.getMobile());


                    getmAuthInfo().setNubeNumber(nube);
                    getmAuthInfo().setNickname(name);
                    getmAuthInfo().setAccessToken(accessToken);
                    CustomLog.d(TAG, "鉴权成功。onAuthenticate,nubeNumber: " + getmAuthInfo().getNubeNumber() + " | name: " + getmAuthInfo().getNickname() + " |token:" + getmAuthInfo().getAccessToken());
                    onLoginSuccess();
                    release();
                } else {
                    CustomLog.e(TAG, "AccountManager::auth()失败 valueCode: " + valueCode);
                    String errorMsg = "鉴权失败";
                    if (valueCode == -2) {
                        errorMsg = "账号密码错误";
                    } else if (valueCode == -62) {
                        errorMsg = "帐号格式有误";
                    } else if (valueCode == -93) {
                        errorMsg = "账户不存在";
                    } else if (NetConnectHelper.getNetWorkType(mContext) == NetConnectHelper.NETWORKTYPE_INVALID) {
                        errorMsg = "网络异常，请连接网络后重试";
                    }
                    onLoginFailed(valueCode, errorMsg);
                    release();
                }
            }
        };

        authAgent.setUserCenterUrl(SettingData.getInstance().PERSONAL_CENTER_URL);

        authAgent.init(mContext);
    }

    public SharedPreferences getLoginSharePre() {
        SharedPreferences sharedPreferences = MedicalApplication.shareInstance()
                .getSharedPreferences("LoginCache", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    /**
     * 获取缓存中的账号信息，如果本地没有缓存（如第一次启动还未进行登录）
     *
     * @return 账号信息
     */
    public String getCacheNube() {
        return getLoginSharePre().getString("nube", "");
    }

    public interface LoginListener {

        /**
         * @param errorCode -1000:账号未提交，没有账号信息； -1001: 审核未通过； -1002:待审核状态
         * @param msg
         */
        void onLoginFailed(int errorCode, String msg);

        void onLoginSuccess();
    }

}
