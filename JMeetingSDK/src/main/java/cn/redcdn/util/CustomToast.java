package cn.redcdn.util;import android.content.Context;import android.os.Handler;import android.view.LayoutInflater;import android.view.View;import android.widget.TextView;import android.widget.Toast;public abstract class CustomToast {  public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;  public static final int LENGTH_LONG = Toast.LENGTH_LONG;  private static Toast toast;  private static Handler handler = new Handler();  private static Runnable run = new Runnable() {    public void run() {      toast.cancel();    }  };  private static void toast(Context context, CharSequence msg, int duration) {    Context tmpContext = context.getApplicationContext();    handler.removeCallbacks(run);    switch (duration) {    case LENGTH_SHORT:      duration = 1000;      break;    case LENGTH_LONG:      duration = 3000;      break;    default:      break;    }    LayoutInflater inflater = LayoutInflater.from(tmpContext);    View convertView = inflater.inflate(MResource.getIdByName(tmpContext, MResource.LAYOUT, "jmeetingsdk_toast_layout"), null);    TextView view = (TextView) convertView.findViewById(MResource.getIdByName(tmpContext, MResource.ID,"toast_txt"));    view.setText(msg);    if (null == toast) {      toast = Toast.makeText(tmpContext, msg, duration);    }    // toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 50);    toast.setView(convertView);    handler.postDelayed(run, duration);    toast.show();  }  public static void show() {  }  public static void show(Context context, CharSequence msg, int duration)      throws NullPointerException {    if (null == context) {      throw new NullPointerException("The context is null!");    }    if (0 > duration) {      duration = LENGTH_SHORT;    }    toast(context, msg, duration);  }  public static void show(Context context, int resId, int duration)      throws NullPointerException {    if (null == context) {      throw new NullPointerException("The context is null!");    }    if (0 > duration) {      duration = LENGTH_SHORT;    }    toast(context, context.getResources().getString(resId), duration);  }}