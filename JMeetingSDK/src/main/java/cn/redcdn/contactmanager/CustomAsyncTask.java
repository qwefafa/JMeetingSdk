package cn.redcdn.contactmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import cn.redcdn.butelDataAdapter.ContactSetImp;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.interfaces.Contact;
import cn.redcdn.meeting.interfaces.ContactCallback;
import cn.redcdn.meeting.interfaces.ResponseEntry;
import cn.redcdn.util.CommonUtil;
import java.util.ArrayList;
import java.util.List;

public class CustomAsyncTask extends AsyncTask<String, Integer, ResponseEntry> {
              private static final String TAG = CustomAsyncTask.class.getSimpleName();
              private Uri uri;
              private ContentValues contentValues;
              private List<ContactCallback> mCallback;
              private Context context;
              private int mOpertionStatus = -1;
              private boolean isNeedCustomerService = false;
              public static final int OPERATE_RAWQUERY = 0;
              public static final int OPERATE_INSERT = 1;
              public static final int OPERATE_QUERY_IS_EXIST = 2;
              //  public static final String[] contacTableColumn = new String[] { "contactId",
//    "name", "nickname", "nubeNumber", "number",
//    "headUrl", "contactUserId", "fullPym", "sex",
//    "userType"};
              public static final String[] contacTableColumn = DBConf.contacTableColumn;

              public void setContentValues(ContentValues contentValues) {
                            this.contentValues = contentValues;
              }

              public void setCallback(ContactCallback mCallback) {
                            if (this.mCallback == null) {
                                          this.mCallback = new ArrayList<ContactCallback>();
                            }
                            this.mCallback.add(mCallback);
              }

              public void setOpertionStatus(int mOpertionStatus) {
                            this.mOpertionStatus = mOpertionStatus;
              }

              public void setContext(Context context) {
                            this.context = context;
              }

              public void setUri(Uri uri) {
                            this.uri = uri;
              }

              public void setCustomerServiceType(boolean isNeed) {
                            this.isNeedCustomerService = isNeed;
              }

              @Override
              protected void onPostExecute(ResponseEntry result) {
                            super.onPostExecute(result);
                            CustomLog.d(TAG, "async task onPostExecute");
                            if (mCallback != null) {
                                          for (int i = 0; i < mCallback.size(); i++) {
                                                        if (mCallback.get(i) != null) {
                                                                      mCallback.get(i).onFinished(result);
                                                        }
                                          }
                            }
              }

              @Override
              protected ResponseEntry doInBackground(String... params) {
                            CustomLog.d(TAG, "async task background thread run");
                            String param = params[0];
                            ResponseEntry result = new ResponseEntry();
                            Cursor cursor = null;
                            switch (mOpertionStatus) {
                                          case OPERATE_RAWQUERY: // 查询
                                                        cursor = context.getContentResolver().query(uri, null, null, null, null);
                                                        if (cursor == null) {
                                                                      result.status = -1;
                                                        } else {
                                                                      result.status = 0;
                                                                      CustomLog.d(TAG, "getAllContacts cursor size " + cursor.getCount());
                                                        }
                                                        isNeedCustomerService = false;
                                                        if (isNeedCustomerService) {
                                                                      MatrixCursor mm = new MatrixCursor(contacTableColumn);


                                                                      mm.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.videoCall), context.getString(R.string.videoCall), "#", 0,
                                                                                    0, "", "", ContactManager.customerServiceNum1, "",
                                                                                    1, 1, 0, "", "",
                                                                                    "", "", "", "", "",
                                                                                    "", 0, 1, "", "",
                                                                                    "", "", ""});
                                                                      mm.addRow(new Object[]{CommonUtil.getUUID(), context.getString(R.string.videoCall), context.getString(R.string.videoCall), "#", 0,
                                                                                    0, "", "", ContactManager.customerServiceNum2, "",
                                                                                    1, 1, 0, "", "",
                                                                                    "", "", "", "", "",
                                                                                    "", 0, 1, "", "",
                                                                                    "", "", ""});

                                                                      Cursor[] a = new Cursor[]{cursor, mm};
                                                                      cursor = new MergeCursor(a);
                                                        }
                                                        ContactSetImp imp = new ContactSetImp();
                                                        imp.setSrcData(cursor);
                                                        result.content = imp;
                                                        break;

                                          case OPERATE_INSERT: // 插入
                                             CustomLog.i(TAG,"向通讯录content添加联系人");
                                                        Uri insertUri = context.getContentResolver().insert(uri, contentValues);
                                                        if (insertUri == null) {
                                                                      result.status = -1;
                                                        } else {
                                                                      result.status = 0;
                                                        }
                                                        break;
                                          case OPERATE_QUERY_IS_EXIST: // 是否存在
                                                        // TODO
                                                        break;
                                          default:
                                                        break;
                            }
                            return result;
              }

              @Override
              protected void onCancelled(ResponseEntry entry) {
                            CustomLog.d(TAG, "async task onCancelled");
              }

              private Contact getDataFromCursor(Cursor cursor) {
                            Contact c = new Contact();
                            c.setContactId(cursor.getString(cursor.getColumnIndex(DBConf.CONTACTID)));
                            c.setName(cursor.getString(cursor.getColumnIndex(DBConf.NAME)));
                            c.setNubeNumber(cursor.getString(cursor
                                          .getColumnIndex(DBConf.NUBENUMBER)));
                            c.setNickname(cursor.getString(cursor.getColumnIndex(DBConf.NICKNAME)));
                            c.setAppType(cursor.getString(cursor.getColumnIndex(DBConf.APPTYPE)));
                            c.setPicUrl(cursor.getString(cursor.getColumnIndex(DBConf.PICURL)));
                            c.setIsDeleted(cursor.getInt(cursor.getColumnIndex(DBConf.ISDELETED)));
                            c.setLastTime(cursor.getLong(cursor.getColumnIndex(DBConf.LASTTIME)));
                            c.setNumber(cursor.getString(cursor.getColumnIndex(DBConf.PHONENUMBER)));
                            c.setContactUserId(cursor.getString(cursor
                                          .getColumnIndex(DBConf.CONTACTUSERID)));
                            c.setUserType(cursor.getInt(cursor.getColumnIndex(DBConf.USERTYPE)));
                            c.setUserFrom(cursor.getInt(cursor.getColumnIndex(DBConf.USERFROM)));

                            return c;
              }
}
