package cn.redcdn.hnyd.contacts.contact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.authentication.server.AuthenticateInfo;
import cn.redcdn.datacenter.enterprisecenter.SearchAccount;
import cn.redcdn.datacenter.enterprisecenter.data.Users;
import cn.redcdn.hnyd.AccountManager;
import cn.redcdn.hnyd.R;
import cn.redcdn.hnyd.base.BaseActivity;
import cn.redcdn.hnyd.contacts.contact.interfaces.Contact;
import cn.redcdn.hnyd.contacts.contact.manager.RecommendManager;
import cn.redcdn.hnyd.im.activity.ChatActivity;
import cn.redcdn.hnyd.im.bean.GroupMemberBean;
import cn.redcdn.hnyd.im.dao.GroupDao;
import cn.redcdn.hnyd.util.CommonUtil;
import cn.redcdn.hnyd.util.CustomToast;
import cn.redcdn.hnyd.util.ScannerActivity;
import cn.redcdn.log.CustomLog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddContactActivity extends BaseActivity {

    private FrameLayout flAddContact;
    private LinearLayout addContactLayout;
    private LinearLayout contactSearchView;
    private EditText addContact = null;
    private Button addContactBtn = null;
    public static int recommendCount = 0;
    private TextView tvRightTop = null;
    private RelativeLayout rlScan = null;
    private Button btnAddContactBack = null;
    private SearchAccount searchUsers = null;
    private String[] addcontact = new String[1];
    public static final int SCAN_CODE = 222;
    private LinkedHashMap<String, GroupMemberBean> memberDateList = new LinkedHashMap<String, GroupMemberBean>();//显示数据
    private AuthenticateInfo loginUserInfo = null;
    private GroupDao mGroupDao;
    private String mGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_addcontact);
        recommendCount = RecommendManager.getInstance(AddContactActivity.this)
                .getNewRecommentCount();
        initAddContactPage();
    }

    private void initAddContactPage() {
        addContactLayout = (LinearLayout) findViewById(R.id.ll_add_contact);
        tvRightTop = (TextView) findViewById(R.id.tvrighttop);
        btnAddContactBack = (Button) findViewById(R.id.btnaddcontactback);
        addContact = (EditText) findViewById(R.id.contactadd_edit);
        addContactBtn = (Button) findViewById(R.id.addcontact_btn);
        rlScan = (RelativeLayout) findViewById(R.id.rl_scan);
        contactSearchView = (LinearLayout) findViewById(R.id.ll_contact_search);
        flAddContact = (FrameLayout) findViewById(R.id.fl_contact_addcontact);
        flAddContact.setVisibility(View.VISIBLE);
        addContactBtn.setClickable(false);
        addContactLayout.setOnClickListener(mbtnHandleEventListener);
        contactSearchView.setOnClickListener(mbtnHandleEventListener);
        btnAddContactBack.setOnClickListener(mbtnHandleEventListener);
        addContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (addContact.length() != 0) {
                    addContactBtn.setClickable(true);
                    addContactBtn.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.contact_search_btn));
                } else {
                    addContactBtn.setClickable(false);
                    addContactBtn.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.contact_search_btn));
                }
                addcontact[0] = addContact.getText().toString();
                CustomLog.d(TAG, "addContact.length" + addcontact.length);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addContact.setTextColor(Color.parseColor("#000000"));

            }
        });

        addContactBtn.setOnClickListener(mbtnHandleEventListener);
        rlScan.setOnClickListener(mbtnHandleEventListener);

        if (recommendCount != 0) {
            // 刷新
            if (tvRightTop == null) {
                tvRightTop = (TextView) findViewById(R.id.tvrighttop);
            }
            tvRightTop.setVisibility(View.VISIBLE);
            if(recommendCount>99){
                tvRightTop.setText("99+");
            }else{
                tvRightTop.setText(String.valueOf(recommendCount));
            }
        } else {
            if (tvRightTop == null) {
                tvRightTop = (TextView) findViewById(R.id.tvrighttop);
            }
            tvRightTop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.ll_add_contact:
                CustomLog.d(TAG, "ll_add_contact click");
                boolean result = CommonUtil.selfPermissionGranted(AddContactActivity.this, android.Manifest.permission.READ_CONTACTS);
                if(!result){
                    CustomToast.show(AddContactActivity.this,R.string.open_contact_premission,CustomToast.LENGTH_SHORT);
                }else {
                    Intent i = new Intent();
                    i.setClass(AddContactActivity.this, RecommendActivity.class);
                    startActivityForResult(i, 0);
                }
                break;
            case R.id.ll_contact_search:
                addContact.requestFocus();
                InputMethodManager imm = (InputMethodManager) addContact.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                break;
            case R.id.btnaddcontactback:
                if (searchUsers != null) {
                    searchUsers.cancel();
                }
                AddContactActivity.this.removeLoadingView();
                Intent intent = getIntent();
                setResult(ContactTransmitConfig.RESULT_ADD_CODE, intent);
                finish();
                break;
            case R.id.addcontact_btn:
                String[] arraylist = {addContact.getText().toString()};
                if (addContact.getText().toString() != null && !addContact.getText().toString().isEmpty()) {

                    if (isNubeNumber(arraylist[0]) == true) {
                        searchUser(3, arraylist); //通过视讯号搜索
                    } else if (isPhoneNumber(arraylist[0]) == true) {
                        searchUser(1, arraylist); //通过手机号搜索
                    } else if (isEmail(arraylist[0]) == true) {
                        searchUser(2, arraylist); //通过邮箱搜索
                    } else {
                        CustomToast.show(AddContactActivity.this, getString(R.string.login_numerror), 1);
                    }
                } else {
                    CustomToast.show(AddContactActivity.this, getString(R.string.input_search_content), 1);
                }
                break;

            case R.id.rl_scan:
                CustomLog.d(TAG, "rlScan click");
                //扫一扫
                Intent intentScan = new Intent();
                intentScan.setClass(AddContactActivity.this, ScannerActivity.class);
                startActivityForResult(intentScan, SCAN_CODE);
                break;
            default:
                break;
        }
    }


    private boolean isNubeNumber(String num) {
        boolean is = false;
        Pattern p = Pattern
                .compile("^([0-9])\\d{7}$");
        Matcher m = p.matcher(num);
        if (m.matches())
            is = true;
        return is;
    }

    private boolean isPhoneNumber(String num) {
        boolean is = false;
        Pattern p = Pattern
                .compile("^((1))\\d{10}$");
        Matcher m = p.matcher(num);
        if (m.matches())
            is = true;
        return is;
    }

    private boolean isEmail(String num) {
        boolean is = false;
        Pattern p = Pattern
                .compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");
        Matcher m = p.matcher(num);
        if (m.matches())
            is = true;
        return is;
    }


    private void searchUser(final int searchType, String[] content) {

            searchUsers = new SearchAccount(){
                @Override
                protected void onSuccess(List<Users> responseContent) {
                    super.onSuccess(responseContent);
                    AddContactActivity.this.removeLoadingView();
                List<Users> list = responseContent;
                Contact contact = new Contact();
                if (list != null && list.size() > 0) {
                    contact.setContactId(list.get(0).uid);
                    contact.setHeadUrl(list.get(0).headUrl);
                    contact.setNickname(list.get(0).nickName);
                    contact.setName(list.get(0).nickName);
                    contact.setNubeNumber(list.get(0).nubeNumber);
                    if (null != list.get(0).mobile && !list.get(0).mobile.isEmpty()) {//手机号
                        contact.setNumber(list.get(0).mobile);
                    } else if (null != list.get(0).mail && !list.get(0).mail.isEmpty()) {//邮箱号
                        contact.setEmail(list.get(0).mail);
                    }

                }

                if (list != null && list.size() > 0) {
                    if (null != contact.getNubeNumber() && contact.getNubeNumber()
                            .equals(AccountManager.getInstance(AddContactActivity.this).getNube())) {
                        CustomToast.show(AddContactActivity.this, getString(R.string.cannot_add_self_friend), 1);
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(AddContactActivity.this, ContactCardActivity.class);
                        intent.putExtra("searchType", String.valueOf(searchType));
                        intent.putExtra("contact", contact);
                        intent.putExtra("REQUEST_CODE", ContactTransmitConfig.REQUEST_CONTACT_CODE);
                        startActivity(intent);
                    }
                } else {
                    CustomToast.show(AddContactActivity.this, getString(R.string.the_user_not_exit), Toast.LENGTH_LONG);
                }
                }

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    AddContactActivity.this.removeLoadingView();
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);

                    CustomToast.show(AddContactActivity.this, statusInfo, Toast.LENGTH_LONG);

                }
            };
        AddContactActivity.this.showLoadingView(getString(R.string.loading_collection), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                CustomToast.show(AddContactActivity.this, getString(R.string.load_cancel),
                        Toast.LENGTH_LONG);
            }
        });
        searchUsers.searchAccount(content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addContact.setHint(R.string.mobile_num_nub_num_email);
        addContact.setText("");
        recommendCount = RecommendManager.getInstance(AddContactActivity.this)
                .getNewRecommentCount();
        CustomLog.d(TAG, "onResume recommendCount=" + recommendCount);
        if (recommendCount != 0) {
            // 刷新
            if (tvRightTop == null) {
                tvRightTop = (TextView) findViewById(R.id.tvrighttop);
            }
            tvRightTop.setVisibility(View.VISIBLE);
            if(recommendCount>99){
                tvRightTop.setText("99+");
            }else{
                tvRightTop.setText(String.valueOf(recommendCount));
            }
        } else {
            if (tvRightTop == null) {
                tvRightTop = (TextView) findViewById(R.id.tvrighttop);
            }
            tvRightTop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchUsers != null) {
            searchUsers.cancel();
        }
        AddContactActivity.this.removeLoadingView();
        Intent intent = new Intent();
        setResult(ContactTransmitConfig.RESULT_ADD_CODE, intent);
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomLog.d(TAG, "resultfrom" + resultCode);
        switch (resultCode) {
            case ContactTransmitConfig.RESULT_CARD_CODE:
                break;
            case ContactTransmitConfig.RESULT_RECOMMEND_CODE:
                break;
        }

        if(requestCode == SCAN_CODE){
            parseBarCodeResult(data);
        }
    }

    private void enterChatActivity() {
        Intent i = new Intent(AddContactActivity.this, ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
        i.putExtra(ChatActivity.KEY_CONVERSATION_ID, mGroupId);
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE, ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, mGroupId);
        startActivity(i);
        AddContactActivity.this.finish();
    }

}

