package cn.redcdn.hnyd.profiles.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;


import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.MedicalApplication;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.im.view.RoundImageView;
import cn.redcdn.hnyd.profiles.listener.DisplayImageListener;
import cn.redcdn.hnyd.util.CustomDialog;
import cn.redcdn.hnyd.util.TitleBar;
import cn.redcdn.log.CustomLog;
import static cn.redcdn.hnyd.AccountManager.getInstance;
import static cn.redcdn.hnyd.R.id.modify_password_rl;

/**
 * Created by Administrator on 2017/2/24.
 */

public class MyFileCardActivity extends BaseActivity {
    protected final String TAG = getClass().getName();
    public static final String KEY_FILE_ABSOLUTELY = "key_file_absolutely";
    public static final String KEY_FILE_CROPPEDICON_PATH = "key_file_croppedicon_path";
    public static final String NAME = "name";
    private DisplayImageListener mDisplayImageListener = null;
    private TextView myName;
    private Button backButton;
    private TextView accountId;
    private TextView phoneNum;
    private TextView mobile;
    private RelativeLayout nameRl;
    private RelativeLayout accountnumRl;
    private RelativeLayout modifyPasswordRl;
    private Button mycardExitButton;
    private String croppedIconFilepath = null;// 压缩后图片位置
    private int request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfilecard);
        initWidget();
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.personal_data));
        mDisplayImageListener = new DisplayImageListener();
    }

    private void initWidget() {
        mobile = (TextView) findViewById(R.id.mobile);
        myName = (TextView) findViewById(R.id.my_name);
        backButton = (Button) findViewById(R.id.back_btn);
        accountId = (TextView) findViewById(R.id.account_id);
        phoneNum = (TextView) findViewById(R.id.phone_num);

        nameRl = (RelativeLayout) findViewById(R.id.name_rl);
        accountnumRl = (RelativeLayout) findViewById(R.id.accountnum_rl);
        modifyPasswordRl = (RelativeLayout) findViewById(modify_password_rl);

        mycardExitButton = (Button) findViewById(R.id.mycard_exit_btn);


        nameRl.setOnClickListener(mbtnHandleEventListener);
        accountnumRl.setOnClickListener(mbtnHandleEventListener);
        modifyPasswordRl.setOnClickListener(mbtnHandleEventListener);
        mycardExitButton.setOnClickListener(mbtnHandleEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void todoClick(int id) {
        // TODO Auto-generated method stub
        super.todoClick(id);
        switch (id) {
            case R.id.name_rl:
                request = 1;
                Intent tosetattenddata = new Intent();
                tosetattenddata.setClass(MyFileCardActivity.this,
                        ChangeMeetingNameActivity.class);
                startActivityForResult(tosetattenddata,request);
                break;
            case R.id.accountnum_rl:
                Intent pd = new Intent();
                pd.setClass(MyFileCardActivity.this, PersonDataActivity.class);
                startActivity(pd);
                break;
            case R.id.modify_password_rl:
                Intent gochange = new Intent();
                gochange.setClass(MyFileCardActivity.this, ChangePwdActivity.class);
                startActivity(gochange);
                break;
            case R.id.mycard_exit_btn:
                CustomLog.d(TAG, "注销");
                final CustomDialog dialog = new CustomDialog(this);

                dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        dialog.cancel();
                        getInstance(getApplicationContext()).logout();
                    }
                });
                dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                });
                dialog.setTip(getString(R.string.are_you_sure));
                dialog.setOkBtnText(getString(R.string.quit));
                dialog.setCancelBtnText(getString(R.string.cancel));
                dialog.show();
                break;


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                String nickName = AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNickname();
                if (nickName != null && !nickName.equalsIgnoreCase(""))
                    myName.setText(nickName);
                else
                    myName.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initstatus();
    }

    private void initstatus() {

        if (!TextUtils.isEmpty(getInstance(getApplicationContext()).getmAuthInfo().getMail())) {
            mobile.setText(getString(R.string.email));
            phoneNum.setText(getInstance(getApplicationContext()).getmAuthInfo().getMail());
        }

            String nickName = AccountManager.getInstance(getApplicationContext()).getmAuthInfo().getNickname();
            if (nickName != null && !nickName.equalsIgnoreCase(""))
                myName.setText(nickName);
            else
                myName.setText("");

            if (getInstance(getApplicationContext()).getmAuthInfo() != null
                    && getInstance(getApplicationContext()).getmAuthInfo().getNubeNumber() != null) {
                accountId.setText(getInstance(getApplicationContext())
                        .getmAuthInfo().getNubeNumber());
            }
            if (getInstance(getApplicationContext()).getmAuthInfo() != null
                    && !getInstance(getApplicationContext()).getmAuthInfo().getMobile().equals("")) {
                phoneNum.setText(
                        getInstance(getApplicationContext()).getmAuthInfo().getMobile());
            }

        }



    @Override
    protected void onPause() {
        super.onPause();
    }
}
