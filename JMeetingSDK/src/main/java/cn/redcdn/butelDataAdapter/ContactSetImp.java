package cn.redcdn.butelDataAdapter;

import android.database.Cursor;

import cn.redcdn.buteldataadapter.DataSet;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meeting.interfaces.Contact;

public class ContactSetImp implements DataSet {
  private final String TAG = this.getClass().getName();
  private Cursor mCursor;

  public void setSrcData(Cursor data) {
    mCursor = data;
  }

  @Override
  public <T> Object getItem(int index) {
    if (index < 0 || mCursor == null || mCursor.getCount() <= index) {
      CustomLog.e(TAG, "Illegal params, return null !");
      return null;
    }
    int pos = mCursor.getPosition();

    mCursor.moveToPosition(index);
    Contact contact = getDataFromCursor(mCursor);
    mCursor.moveToPosition(pos);

    return contact;
  }

  @Override
  public int getCount() {
    if (mCursor != null) {
      return mCursor.getCount();
    }

    return 0;
  }

  @Override
  public void release() {
    if (mCursor != null && !mCursor.isClosed()) {
      mCursor.close();
    }
  }
  private Contact getDataFromCursor(Cursor cursor) {
    Contact c = null;
    if (cursor != null) {
      c = new Contact();
      c.setContactId(trackValue(cursor.getString(0)));
      c.setName(trackValue(cursor.getString(1)));
      c.setNumber(trackValue(cursor.getString(2)));
      c.setNickname(trackValue(cursor.getString(3)));
      c.setHeadUrl(trackValue(cursor.getString(4)));
      c.setNubeNumber(trackValue(cursor
                    .getString(5)));
      c.setContactUserId(trackValue(cursor.getString(6)));
      c.setFullPym(trackValue(cursor.getString(7)));
//      c.setSex(trackValue(cursor.getString(8)));
//      c.setContactId(cursor.getString(0));
//      c.setName(cursor.getString(1));
//      c.setNickname(cursor.getString(2));
//      c.setNubeNumber(cursor.getString(3));
//      c.setNumber(cursor.getString(4));
//      c.setHeadUrl(cursor.getString(5));
//      c.setContactUserId(cursor.getString(6));
//      c.setFullPym(cursor.getString(7));
//      c.setSex(cursor.getInt(8));
      //TODO 设备类型请根据产品经理定义的视频号前缀来区分
//      c.setDeviceType(ContactManager.checkDeviceTypeByNube(CommonUtil.trackValue(cursor
//                    .getString(5)));
      CustomLog.d(TAG, "getDataFromCursor "+c.toString());



      CustomLog.d(TAG, "getDataFromCursor "+c.toString());
    }
    return c;
  }
  public static String trackValue(Object object) {
    return object != null && object.toString().trim().length() != 0 && !object.toString().trim().equals("null") && !object.toString().trim().equals("NULL")?object.toString():"";
  }
}
