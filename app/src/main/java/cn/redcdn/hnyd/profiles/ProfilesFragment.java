package cn.redcdn.hnyd.profiles;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cn.redcdn.authentication.server.AuthenticateInfo;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseFragment;
import cn.redcdn.hnyd.config.SettingData;
import cn.redcdn.hnyd.im.view.RoundImageView;
import cn.redcdn.hnyd.im.view.Share2FriendsDialog;
import cn.redcdn.hnyd.profiles.activity.AboutActivity;
import cn.redcdn.hnyd.profiles.activity.CollectionActivity;
import cn.redcdn.hnyd.profiles.activity.MyFileCardActivity;
import cn.redcdn.hnyd.profiles.activity.SettingActivity;
import cn.redcdn.hnyd.profiles.listener.DisplayImageListener;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.ScannerActivity;
import cn.redcdn.hnyd.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hnyd.util.CommonUtil.GetNetype;

/**
 * Created by thinkpad on 2017/2/7.
 */

public class ProfilesFragment extends BaseFragment {
    protected final String TAG = getClass().getName();
    //微信邀请声明
    public static String APP_ID = "wx075e76791e3ec1a8"; //微信appid
    public static IWXAPI api;
    //qq分享使用
    private String AppId = "1105341562";

    // 视图
    private View contentView = null;
    public static final String HTTPS = "https://www.baidu.com/";
    public static final String PERSON_TYPE = "person";
    public static final String GROUP_TYPE = "group";
    public static final String WE_TYPE = "weChat";
    public static final String ARTICLE_PREVIEW = "articlePreview";
    private RelativeLayout gotoMyfilecard;
//    private RelativeLayout gotoMeeting;
    private RelativeLayout gotoScan;
    private RelativeLayout gotoCollection;
    private RelativeLayout gotoSetting;
    private RelativeLayout gotoAbout;
    private RelativeLayout Share2friendsRL;

    private DisplayImageListener mDisplayImageListener = null;
    private TextView name;
    private TextView acccountId;
    private RoundImageView headIv;
    private String CfgPath = Environment.getExternalStorageDirectory().getPath()
        + "/cn.redcdn.hvs/main/qqshare_image.png";
    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int SCAN_CODE = 222;
    private LayoutInflater mInflater;
    private Tencent mTencent;


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        contentView = inflater.inflate(R.layout.profiles_fragment,
            container, false);
        initWidget(contentView);
        mDisplayImageListener = new DisplayImageListener();
        //注册到微信
        api = WXAPIFactory.createWXAPI(getActivity(), APP_ID, true);
        api.registerApp(APP_ID);
        mTencent = Tencent.createInstance(AppId, getContext());
        copyImageToPath("qqshare_image.png",CfgPath);
        return contentView;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            if (contentView != null) {
                contentView.requestLayout();
            }
        } else {
            //相当于Fragment的onPause
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.me));
    }


    private void initWidget(View view) {
        gotoMyfilecard = (RelativeLayout) view.findViewById(R.id.gotomyfilecard_rl);
//        gotoMeeting = (RelativeLayout) view.findViewById(R.id.gotomeeting_rl);
        gotoScan = (RelativeLayout) view.findViewById(R.id.gotoscan_rl);
        gotoCollection = (RelativeLayout) view.findViewById(R.id.gotocollection_rl);
        gotoSetting = (RelativeLayout) view.findViewById(R.id.gotosetting_rl);
        gotoAbout = (RelativeLayout) view.findViewById(R.id.gotoabout_rl);
        Share2friendsRL = (RelativeLayout) view.findViewById(R.id.share2friends_rl);
        headIv = (RoundImageView) view.findViewById(R.id.head_iv);
        gotoMyfilecard.setOnClickListener(mbtnHandleEventListener);
//        gotoMeeting.setOnClickListener(mbtnHandleEventListener);
        gotoScan.setOnClickListener(mbtnHandleEventListener);
        gotoCollection.setOnClickListener(mbtnHandleEventListener);
        gotoSetting.setOnClickListener(mbtnHandleEventListener);
        gotoAbout.setOnClickListener(mbtnHandleEventListener);
        Share2friendsRL.setOnClickListener(mbtnHandleEventListener);
        name = (TextView) view.findViewById(R.id.nube_tv);
        acccountId = (TextView) view.findViewById(R.id.setattend_nube_tv);
    }


    @Override
    protected void setListener() {

    }


    @Override
    protected void initData() {

    }


    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.gotomyfilecard_rl:
                Intent intentMyfilecard = new Intent();
                intentMyfilecard.setClass(getActivity(), MyFileCardActivity.class);
                startActivity(intentMyfilecard);
                break;
//            case R.id.gotomeeting_rl:
//                Intent intentMeeting = new Intent();
//                intentMeeting.setClass(getActivity(), ConsultingRoomActivity.class);
//                startActivity(intentMeeting);
//                break;
            case R.id.gotoscan_rl:
                Intent intentScan = new Intent();
                intentScan.setClass(getActivity(), ScannerActivity.class);
                startActivityForResult(intentScan, SCAN_CODE);
                break;
            case R.id.gotocollection_rl:
                Intent intentCollection = new Intent();
                intentCollection.setClass(getActivity(), CollectionActivity.class);
                startActivity(intentCollection);
                break;
            case R.id.gotosetting_rl:
                Intent intentSetting = new Intent();
                intentSetting.setClass(getActivity(), SettingActivity.class);
                startActivity(intentSetting);
                break;
            case R.id.gotoabout_rl:
                Intent intentAbout = new Intent();
                intentAbout.setClass(getActivity(), AboutActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.share2friends_rl:
                //展示一个从底部弹出的Dialog
                Share2FriendsDialog share2FriendsDialog = new Share2FriendsDialog(getContext());
                share2FriendsDialog.setShare2weChatListener(new Share2FriendsDialog.MenuClickedListener() {
                    @Override public void onMenuClicked() {
                     if( GetNetype(getActivity())==-1){
                         CustomToast.show(getActivity(),getString(R.string.check_internet),CustomToast.LENGTH_SHORT);
                         return;
                     }

                        if (isWeixinAvilible()) {
                            shareByWx();
                        }
                    }
                });
                share2FriendsDialog.setShare2QQListener(new Share2FriendsDialog.MenuClickedListener() {
                    @Override public void onMenuClicked() {
                        if( GetNetype(getActivity())==-1){
                            CustomToast.show(getActivity(),getString(R.string.check_internet),CustomToast.LENGTH_SHORT);
                            return;
                        }
                        shareByQQ();
                    }
                });
                share2FriendsDialog.setShare2MsgListener(new Share2FriendsDialog.MenuClickedListener() {
                    @Override public void onMenuClicked() {
                        shareBySms(getString(R.string.share_by_wx)+SettingData.getInstance().ShareUrl);
                    }
                });
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        initstatus();
    }


    private void initstatus() {
        CustomLog.d(TAG, "initstatus");
        AuthenticateInfo info = AccountManager.getInstance(getActivity())
            .getmAuthInfo();
        if (info.getHeadUrl() != null && !info.getHeadUrl().equalsIgnoreCase("")) {
            CustomLog.d(TAG, "显示图片");
            show(info.getHeadUrl());
        } else {
            headIv.setImageResource(R.drawable.doctor_default);
        }
        String nickName = "";
        if (AccountManager.getInstance(getActivity())
            .getmAuthInfo() != null) {
            nickName = AccountManager.getInstance(getActivity())
                .getmAuthInfo().getNickname();
        }
        if (nickName != null && !nickName.equalsIgnoreCase("")) {
            name.setText(nickName);
        } else {
            name.setText(R.string.no_name);
        }
        if (AccountManager.getInstance(getActivity())
            .getmAuthInfo() != null) {
            acccountId.setText(getString(R.string.nub_num) + AccountManager.getInstance(getActivity())
                .getmAuthInfo().getNubeNumber());
        }
    }


    private void show(final String str) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, headIv, MedicalApplication.shareInstance().options, mDisplayImageListener);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_CODE) {
            parseBarCodeResult(data);
        }
        if (null != mTencent) {
            mTencent.onActivityResult(requestCode, resultCode, data);
        }
    }


    //微信分享
    private boolean isWeixinAvilible() {
        final PackageManager packageManager = getActivity().getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        CustomToast.show(getContext(), getString(R.string.weixin_version_low_havenot), CustomToast.LENGTH_LONG);
        return false;
    }


    private void shareByWx() {
        WXWebpageObject webpage = new WXWebpageObject();  //初始化WXTextObject对象，填写分享的文本内容
        webpage.webpageUrl = SettingData.getInstance().ShareUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);//用WXTextObject对象初始化一个WXMedicalMessage对象
        msg.title = getString(R.string.app_name);
        msg.description = getString(R.string.share_by_wx);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        msg.thumbData=bmpToByteArray(bitmap,true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();  //构造一个Req
        req.message = msg; //transaction字段用于唯一标识一个请求
        req.scene = SendMessageToWX.Req.WXSceneSession;//分享到好友会话
        api.sendReq(req);  //调用api接口发送数据到微信
    }


    private void shareBySms(String content) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", content);
        startActivity(intent);
    }

    /**
     * 分享到QQ好友
     */
    private void shareByQQ() {

        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, getString(R.string.share_by_qq_title));
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, getString(R.string.share_by_qq_content));
        // params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, SettingData.getInstance().ShareUrl);//推荐的下载地址
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, SettingData.getInstance().ShareUrl);//推荐的下载地址
        // params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,"http://testmedical.butel.com:8189/mws/images/down_logo_butel.png");
        // params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL,"http://testmedical.butel.com:8189/mws/images/down_logo_butel.png");
        String sharePath = MedicalApplication.getContext().getFilesDir().getAbsolutePath()+"/ic_launcher";
        CustomLog.i(TAG,"sharePath=="+sharePath);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, CfgPath);
        params.putString("appName", getString(R.string.app_name));
        mTencent.shareToQQ(getActivity(), params, new IUiListener() {
            @Override public void onComplete(Object o) {
                CustomToast.show(getContext(), "onComplete", CustomToast.LENGTH_SHORT);
                CustomLog.i(TAG, "onComplete");
            }


            @Override
            public void onError(UiError e) {
                CustomToast.show(getContext(), "onError code:" + e.errorCode
                    + ", msg:" + e.errorMessage + ", detail:"
                    + e.errorDetail, CustomToast.LENGTH_SHORT);
                CustomLog.i(TAG, "onError code:" + e.errorCode
                    + ", msg:" + e.errorMessage + ", detail:"
                    + e.errorDetail);
            }


            @Override
            public void onCancel() {
                CustomToast.show(getContext(), "onCancel", CustomToast.LENGTH_SHORT);
                CustomLog.i(TAG, "onCancel");
            }
        });
    }
    private void copyFile(String from, String to) {
        CustomLog.i(TAG,"copyFile");
        //例：from:890.salid;
        // to:/mnt/sdcard/to/890.salid
        // File f = new File(to);
        // if(f.exists())
        //     return;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(from);//from为assest中所要写到的sdcard中的名称
            if (!oldfile.exists()) {
                oldfile.mkdir();
            }
            if (oldfile.exists()) {
                InputStream inStream = getResources().getAssets().open(from);//将assets中的内容以流的形式展示出来
                OutputStream fs = new BufferedOutputStream(new FileOutputStream(to));//to为要写入sdcard中的文件名称
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    /**
     * 将拷贝  assets目录下面的  medical.db 到  data/data/cn.redcdn.hvs/files/
     *
     * @param context
     * @throws IOException
     */
    private  void copyImage(Context context)
        throws Exception {
        CustomLog.d(TAG, "copyImage begin,");
        FileOutputStream fos = null;
        try {
            String newPath = MedicalApplication.getContext().getFilesDir().getAbsolutePath();
            File filedb = new File(newPath);
            if (filedb.exists()) {
                CustomLog.d(TAG, "图片已经存在:" + newPath);
            } else {
                CustomLog.d(TAG, "拷贝图片:" + newPath);
                fos = context.openFileOutput("ic_launcher.png", Context.MODE_APPEND);
                byte[] b = new byte[context.getAssets()
                    .open("ic_launcher.png").available()];
                context.getAssets().open("ic_launcher.png")
                    .read(b);
                fos.write(b);
            }

        } catch (IOException e) {
            CustomLog.e(TAG, "ioexception" + e.toString());
            throw e;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    CustomLog.e(TAG, "copyImage" + "fos.close() ioexception" + e.toString());
                }
            }
        }
        CustomLog.d(TAG, "copyImage end,");
    }
    private void copyImageToPath(String fileName, String filePath) {
        CustomLog.i(this.getClass().getName(), " copyImageToPath fileName: "
            + fileName + " filePath" + filePath);
        try {
            InputStream logxmlInputStream = getContext().getResources().getAssets()
                .open(fileName);
            FileOutputStream fileOutputStream = null;
            File file = new File(filePath);
            if (file.exists() ) {
                CustomLog.i(TAG, "qq分享图片已存在，不copy!");
                return;
            }
            fileOutputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int ch = -1;
            while ((ch = logxmlInputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, ch);
            }
            file.setReadable(true, false);
            fileOutputStream.flush();
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

