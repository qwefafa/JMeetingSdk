package cn.redcdn.contactmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import cn.redcdn.jmeetingsdk.MeetingManage;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.interfaces.Contact;
import cn.redcdn.meeting.interfaces.ContactCallback;
import cn.redcdn.meeting.interfaces.ContactOperation;
import cn.redcdn.util.CommonUtil;
import cn.redcdn.util.StringHelper;

public class ContactManager implements ContactOperation {
  private static final String TAG = ContactManager.class.getSimpleName();
  private static ContactManager mInstance = null;
  private Context mContext = null;
  public static final String customerServiceNum1 = "68000001";
  public static final String customerServiceNum2 = "68000002";
  public static final String customerServiceName = "视频客服";
  private String mContactProvider;

  public String ContactUrl = "content://" + mContactProvider +"/GET_APP_LINKMAN_DATA_NEW";
  public String ContactInsert = "content://" + mContactProvider +"/INSERT_LINKMAN_ITEM";
  public String ContactCheckRecord= "content://" + mContactProvider +"/QUERY_LINKMAN_BY_NUBENUMBER";

  public synchronized static ContactManager getInstance(Context context) {
    if (mInstance == null) {
      CustomLog.d(TAG, "ContactManager getInstance");
      mInstance = new ContactManager();
      mInstance.mContext = context;
    }
    return mInstance;
  }
  
  public void setSearchContactUrl(String ContactUrl){
	  this.ContactUrl = ContactUrl;
  }
  public void setContactProvider(String contactProvider) {
    mContactProvider = contactProvider;
    ContactUrl = "content://" + mContactProvider +"/GET_APP_LINKMAN_DATA_NEW";
    ContactInsert = "content://" + mContactProvider +"/INSERT_LINKMAN_ITEM";
    ContactCheckRecord= "content://" + mContactProvider +"/QUERY_LINKMAN_BY_NUBENUMBER";
    CustomLog.d(TAG,"ContactManager" + mContactProvider);
  }

  public void setInsertContactUrl(String ContactInsert){
	  this.ContactInsert = ContactInsert;
  }

  public boolean isContactExist(String nubeNumber) {
    boolean isExist = false;
    Uri uri = Uri
        .parse(ContactCheckRecord);
    uri = Uri.withAppendedPath(uri, nubeNumber);
    Cursor c = mContext.getContentResolver().query(uri, null, null, null, null);
    if (c != null && c.getCount() > 0) {
      isExist = true;
    }
    if (c != null) {
      c.close();
      c = null;
    }
    CustomLog.d(TAG, "isContactExist " + isExist);
    return isExist;
  }

  public void addContact(final Contact contact, ContactCallback callback) {
    if (contact != null) {
      if (TextUtils.isEmpty(contact.getNubeNumber())) {
        CustomLog.d(TAG, "NubeNumber is empty");
        if (callback != null) {
          callback.onFinished(null);
        }
      } else {
        CustomAsyncTask task = new CustomAsyncTask();
        Uri uri = Uri
            .parse(ContactInsert);
        task.setUri(uri);
        task.setCallback(callback);
        task.setContentValues(contactToContentValues(contact));
        task.setOpertionStatus(CustomAsyncTask.OPERATE_INSERT);
        task.setContext(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } else {
          task.execute("");
        }
      }
    }

  }
  
  public boolean checkNubeIsCustomService(String nubeNumber) {
    if (nubeNumber == null || nubeNumber.equals("")) {
      return false;
    }
    if (nubeNumber.equals(customerServiceNum1)) {
      return true;
    }
    if (nubeNumber.equals(customerServiceNum2)) {
      return true;
    }
    return false;
  }
  
  public String getHeadUrlByNube(String nubeNumber) {
    String url = "";
    Uri uri = Uri
        .parse(ContactUrl);
    uri = Uri.withAppendedPath(uri, nubeNumber);
    Cursor c = mContext.getContentResolver().query(uri, null, null, null, null);
    if (c != null && c.moveToNext()) {
      url = c.getString(c.getColumnIndex("headUrl"));
    }
    if (c != null) {
      c.close();
      c = null;
    }
    return url;
  }
  
  public void getAllContacts(ContactCallback callback,boolean isNeedCustomerService) {
    CustomAsyncTask task = new CustomAsyncTask();
    task.setCallback(callback);
    task.setCustomerServiceType(isNeedCustomerService);
    Uri uri = Uri
        .parse(ContactUrl);
    task.setUri(uri);
    task.setOpertionStatus(CustomAsyncTask.OPERATE_RAWQUERY);
    task.setContext(mContext);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    } else {
      task.execute("");
    }
  }

  private ContentValues contactToContentValues(Contact contact) {

    CustomLog.d(TAG, "contactToContentValues " + contact.toString());


    ContentValues values = new ContentValues();
    if (TextUtils.isEmpty(contact.getContactId())) {
      values.put(DBConf.CONTACTID, CommonUtil.getUUID());
    } else {
      values.put(DBConf.CONTACTID, contact.getContactId());
    }
    if (TextUtils.isEmpty(contact.getNickname())) {
      values.put(DBConf.NICKNAME, mContext.getString(R.string.unnamed));
      values.put(DBConf.FIRSTNAME, mContext.getString(R.string.unnamed));
      values.put(DBConf.PINYIN, mContext.getString(R.string.unnamed));
    } else {
      values.put(DBConf.NICKNAME, contact.getNickname());
      values.put(DBConf.FIRSTNAME, StringHelper.getHeadChar(contact.getNickname()));
      values.put(DBConf.PINYIN, StringHelper.getAllPingYin(contact.getNickname()));
    }
    if (TextUtils.isEmpty(contact.getName())) {
      values.put(DBConf.NAME, mContext.getString(R.string.unnamed));
    } else {
      values.put(DBConf.NAME, contact.getName());
    }
    values.put(DBConf.LASTNAME, String.valueOf(contact.getLastTime()));
    values.put(DBConf.ISDELETED, contact.getIsDeleted());
    values.put(DBConf.PHONENUMBER, checkIsNull(contact.getNumber()));
    values.put(DBConf.PICURL, checkIsNull(contact.getPicUrl()));
    values.put(DBConf.USERTYPE, contact.getUserType());
    values.put(DBConf.NUBENUMBER, checkIsNull(contact.getNubeNumber()));
    values.put(DBConf.USERFROM, contact.getUserFrom());
    values.put(DBConf.CONTACTUSERID, checkIsNull(contact.getContactUserId()));
    values.put(DBConf.APPTYPE, checkIsNull(contact.getAppType()));
    values.put(DBConf.SYNCSTAT, 0);

    //
    // values.put(DBConf.EMAIL, contact.getEmail());
    // values.put(DBConf.SAVE_TO_CONTACTS_TIME, contact.getSaveToContactsTime());
    //红云医疗添加字段
    values.put(DBConf.WORKUNIT_TYPE, contact.getWorkUnitType());
    values.put(DBConf.WORK_UNIT, contact.getWorkUnit());
    values.put(DBConf.DEPARTMENT, contact.getDepartment());
    values.put(DBConf.PROFESSIONAL, contact.getProfessional());
    values.put(DBConf.OFFICETEL, contact.getOfficTel());
    values.put(DBConf.PICURL,contact.getHeadUrl());//添加头像图片地址
    //获取服务器返回的数据(也可能是datacenter默认分会的数据AccountType==0表示通讯录  1表示添加到IM群聊)
    // values.put(DBConf.ACCOUNT_TYPE,Integer.parseInt(contact.getAccountType()));
    values.put(DBConf.ACCOUNT_TYPE, 0);

    //不加不行
    // values.put(DBConf.SAVE_TO_CONTACTS_TIME, "");
    // values.put(DBConf.EMAIL,"1194171358@qq.com");
    //TODO
    //插入假数据测试使用  后期不用可以删除
    // values.put(DBConf.EMAIL,"1194171358@qq.com");
    // values.put(DBConf.ACCOUNT_TYPE, 0);
    // values.put(DBConf.WORKUNIT_TYPE, "123");
    // values.put(DBConf.WORK_UNIT, "123");
    // values.put(DBConf.DEPARTMENT, "123");
    // values.put(DBConf.PROFESSIONAL,"123");
    // values.put(DBConf.OFFICETEL, "12311111");
    // values.put(DBConf.SAVE_TO_CONTACTS_TIME, "");


    // //原有的数据
    // // CustomLog.d(TAG, "contactToContentValues " + contact.toString());
    // ContentValues values = new ContentValues();
    // values.put("nickname", contact.getNickname());
    // values.put("headUrl", contact.getHeadUrl());
    // // values.put("number", contact.getNumber());
    // values.put("nubeNumber", contact.getNubeNumber());
    // values.put("sex", contact.getSex());
    // //  values.put("deviceType", contact.getDeviceType());
    // // CustomLog.d(TAG, "addContact ContentValues " + values.toString());
    return values;
  }

  public static int checkDeviceTypeByNube(String nubeNum) {
    if (nubeNum != null && !nubeNum.isEmpty()) {
      char c[] = nubeNum.toCharArray();
      if ("5".equals(c[0])) {
        return 3;
      }
      if ("7".equals(c[0])) {
        return 4;
      }
      if ("9".equals(c[0])) {
        return 5;
      }
      if ("6".equals(c[0])) {
        if ("0".equals(c[1]) || "1".equals(c[1])) {
          return 1;
        }
        if ("2".equals(c[1])) {
          return 2;
        }
        if ("3".equals(c[1])) {
          return 5;
        }
        if ("9".equals(c[1]) && "2".equals(c[2])) {
          return 2;
        }
      }
    }
    return 0;
  }
  private String checkIsNull(String str) {
    if (str != null) {
      return str;
    } else {
      return "";
    }
  }
}
