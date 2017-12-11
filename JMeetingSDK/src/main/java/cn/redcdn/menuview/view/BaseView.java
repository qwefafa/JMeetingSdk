package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public abstract class BaseView extends FrameLayout {
  private final String TAG = BaseView.this.getClass().getName();

  public abstract void onClick(View v);

  public abstract void onNotify(int notifyType, Object object);

  public BaseView(Context context, int layout) {
    super(context);
    LayoutInflater.from(context).inflate(layout, this, true);
    dismiss();
  }

  public void show() {
//    if (this.getVisibility() == View.INVISIBLE) {
      setVisibility(View.VISIBLE);
//    }
  }

  public void dismiss() {
//    if (this.getVisibility() == View.VISIBLE) {
      setVisibility(View.GONE);
//    }
  }
  
  public boolean isShowing() {
	    return View.VISIBLE == getVisibility();
	  }
  
  public void release() {
	     //TODO
	  }
}
