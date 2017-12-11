package cn.redcdn.hnyd.im.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.util.CustomDialog;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.TitleBar;


public class GroupAnnouncementEditActivity extends BaseActivity {
    private EditText announcement_edit;
    private Button titlerightbtn;
    private RelativeLayout titleLayout;
    private TitleBar titleBar;
    private String mGroupId, announceContent;
    private final int resultcode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_announcement_edit);
        initWidget();
        initData();
//        setButtonState();

    }
    private void initWidget(){
        titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.group_notices));
        titleBar.setBackText(getString(R.string.btn_cancle));
        titleBar.enableRightBtn(getString(R.string.complete), 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitGroupAnnouncement();
            }
        });
        announcement_edit = (EditText) findViewById(R.id.group_announcement_edit);  //群公告内容
        titleLayout = (RelativeLayout) findViewById(R.id.title);
        titlerightbtn = (Button) titleLayout.findViewById(R.id.right_btn);
    }
    private void initData(){
        announcement_edit.addTextChangedListener(textWatcher);
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(GroupAnnouncementActivity.GROUP_ID);
        announceContent = intent.getStringExtra(GroupAnnouncementActivity.ANNOUNCEMENT);
        announcement_edit.setText(announceContent);
        announcement_edit.setSelection(announcement_edit.getText().length());

    }

    private void submitGroupAnnouncement() {
        String announcement = announcement_edit.getText().toString()  ;
//        InputFilter[] filters = {new InputFilter.LengthFilter(10)};
//        announcement_edit.setFilters(filters);
        byte[] bff= new byte[0];
        try {
            bff = announcement.getBytes("GB2312");//utf-8编码一个汉子占三个字节
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int count=bff.length;
        if (announcement.length() == 0) {
            CustomToast.show(GroupAnnouncementEditActivity.this, getString(R.string.group_announcement_not_null), CustomToast.LENGTH_SHORT);
            return;
        }
        if (count > 3000) {
            CustomToast.show(GroupAnnouncementEditActivity.this, getString(R.string.group_announcement_not_more_3000), CustomToast.LENGTH_SHORT);
            return;
        }
        titlerightbtn.setTextColor(Color.parseColor("#cc49afcc"));
        final CustomDialog tipDlg = new CustomDialog(GroupAnnouncementEditActivity.this);
        tipDlg.setTip(getString(R.string.group_announcement_is_posted));
        tipDlg.setOkBtnText(getString(R.string.posted));
        tipDlg.setCancelBtnText(getString(R.string.btn_cancle));
        tipDlg.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
            }
        });
        tipDlg.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                tipDlg.dismiss();
            }
        });
        tipDlg.show();

    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            setButtonState();
        }


    };

    private void setButtonState(){
        if (announcement_edit.getText().toString().equals("")) {
            titlerightbtn.setTextColor(Color.parseColor("#222625"));
        } else {
            titlerightbtn.setTextColor(Color.parseColor("#49afcc"));
        }
    }
}
