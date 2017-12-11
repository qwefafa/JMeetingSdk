package cn.redcdn.hnyd.base;

import android.support.v4.app.Fragment;
import cn.redcdn.hnyd.contacts.ContactsFragment;
import cn.redcdn.hnyd.im.MessageFragment;
import cn.redcdn.hnyd.meeting.MeetingFragment;
import cn.redcdn.hnyd.profiles.ProfilesFragment;
import cn.redcdn.log.CustomLog;

/**
 * Created by thinkpad on 2017/2/9.
 */

public class FragmentFactory {
    public static final String TAG = "FragmentFactory";
    /**
     * 根据不同的position生产对应的Fragment对象
     * */
  public static Fragment create(int position){
      Fragment fragment = null;
      switch (position){
          case 0:
              fragment = new MessageFragment();
              CustomLog.d(TAG,"fragment"+fragment);
              break;
          case 1:
              fragment = new MeetingFragment();
              CustomLog.d(TAG,"fragment"+fragment);
              break;

          case 2:
              fragment = new ContactsFragment();
              CustomLog.d(TAG,"fragment"+fragment);
              break;

          case 3:
              fragment = new ProfilesFragment();
              CustomLog.d(TAG,"fragment"+fragment);
              break;
      }
      return fragment;
  }
}
