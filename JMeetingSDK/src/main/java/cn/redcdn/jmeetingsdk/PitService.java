package cn.redcdn.jmeetingsdk;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;

/**
 * @author guoyx
 */

public class PitService extends Service {
    private static final String TAG = PitService.class.getSimpleName();
    private int JMEETINGSERVICE_NOTIfACTION_ID = 110;


    @Override
    public void onCreate() {
        super.onCreate();
        CustomLog.i(TAG, "onCreaet()");

        Intent nfIntent = new Intent(this, MeetingRoomActivity.class);
        Notification.Builder builder = new Notification.Builder(
            this.getApplicationContext());
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_launcher))
            .setContentTitle(getString(R.string.pit_service_notifaction_title))
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText(getString(R.string.pit_service_notifaction_content))
            .setWhen(System.currentTimeMillis());

        CustomLog.i(TAG, "升级前台服务");
        startForeground(JMEETINGSERVICE_NOTIfACTION_ID, builder.build());

    }


    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        CustomLog.i(TAG, "onDestroy()");
    }
}
