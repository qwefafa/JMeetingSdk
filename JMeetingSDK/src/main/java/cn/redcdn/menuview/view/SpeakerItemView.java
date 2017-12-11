package cn.redcdn.menuview.view;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;

import static android.view.ViewConfiguration.getTapTimeout;

public abstract class SpeakerItemView extends FrameLayout {

	private final String TAG = "SpeakerItemView";

	private Context mContext;

	private SurfaceView mSurfaceView;

	private  LinearLayout mLinearLayout;

	private TextView mTextView;

	private ImageView shareDocType;

	private ImageView micType;

	private ImageView cameraType;

	private String accountId;

	private String name;

	private int speakerType;

	private int pagetype;//放大，普通

	private int viewType;//数据view，普通view
	private float mRawX = 0;
	private float mRawY = 0;
	private int width;
	private int height;
	private int windowWidth;
	private int windowHeight;
	public static final int SHARE_DOC_TYPE = 1;

	public static final int MIC_TYPE = 2;

	public static final int CAMERA_TYPE = 3;

	private int mLimitScrollWidth = 50;

	private int mLimitScrollHeight = 50;
	float downX = 0, downY = 0;
	float upX, upY;
	float x_limit = mLimitScrollWidth;
	float y_limit = mLimitScrollHeight;
	boolean isMove = false;
	private int x8 = (int) getResources().getDimension(R.dimen.x8);
	private LayoutParams mediaViewBgLP;
	// 放大状态
	private LayoutParams zeroParentLP = new LayoutParams(
			1, 1);

	private LayoutParams matchParentLP = new LayoutParams(
			LayoutParams.MATCH_PARENT,
			LayoutParams.MATCH_PARENT);

    private void setDynamicItemViewParams(){
		LayoutParams mParams = new LayoutParams(1, 1);
		mParams.width = width;
		mParams.height = height;
		if((mRawX-upX) <= (windowWidth/2-width/2)){
			if((mRawY-upY) <= (windowHeight/2-height/2)){
				//zuo shang
				mParams.leftMargin = x8;
				mParams.topMargin = x8;
				mParams.bottomMargin = 0;
				mParams.rightMargin =0;
			}else{
				//zuo xia
				mParams.leftMargin = x8;
				mParams.topMargin = windowHeight-height-x8;
				mParams.bottomMargin = x8;
				mParams.rightMargin =0;
			}
		}else{
			if((mRawY-upY) <= (windowHeight/2-height/2)){
				//you shang
				mParams.leftMargin = windowWidth-width-x8;
				mParams.topMargin = x8;
				mParams.rightMargin =x8;
				mParams.bottomMargin = 0;
			}else{
				//you xia
				mParams.leftMargin = windowWidth-width-x8;
				mParams.topMargin = windowHeight-height-x8;
				mParams.rightMargin =x8;
				mParams.bottomMargin = x8;
			}
		}
		SpeakerItemView.this.setLayoutParams(mParams);
		viewParam(accountId,mParams.topMargin,mParams.leftMargin,mParams.rightMargin,mParams.bottomMargin);
	}
	private long downTime = 0;
	private long upTime = 0;
	private int moveX = 0;
	private int moveY = 0;
	private float moveDX=0;
	private float moveDY=0;
	private OnTouchListener viewOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//CustomLog.e("SpeakerItemView", "onTouch" + mRawX+","+mRawY);
			mRawX = event.getRawX();
			mRawY = event.getRawY();

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				CustomLog.e("SpeakerItemView", "ACTION_DOWN" + mRawX+","+mRawY);
				downX = event.getX();
				downY = event.getY();
				moveDX= event.getX();
				moveDY=	event.getY();
				downTime = System.currentTimeMillis();
				return true;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				CustomLog.e("SpeakerItemView", "ACTION_UP" + mRawX+","+mRawY);
				upTime = System.currentTimeMillis();
				upX = event.getX(); // 取得松开时的坐标x;
				upY = event.getY();
				float x = upX - downX;
				float y = upY - downY;
				float x_abs = Math.abs(x);
				float y_abs = Math.abs(y);
				CustomLog.e("SpeakerItemView", "ACTION_UP downX " +
						+downX+",downY "+downY+" upX "+upX+" upY "+upY+" x "+x+" y "+y+" x_abs "+x_abs+" y_abs "+y_abs+" getTapTimeout "+getTapTimeout());
				if((upTime-downTime) > getTapTimeout() && (moveX > 20|| moveY > 20)){
					if (isMove){
						setDynamicItemViewParams();
					}else{
						if (y > y_limit || y < -y_limit) {
							if (y > 0) {
								viewFlingPageDown();
							} else {
								viewFlingPageUp();
							}
						}
					}
				}else{
					viewOnClick(SpeakerItemView.this, pagetype, viewType, accountId, upX, upY);
				}
				return true;
			}else if(event.getAction() == MotionEvent.ACTION_MOVE){
				moveX += Math.abs(event.getX() - moveDX);//X轴距离
				moveY += Math.abs(event.getY() - moveDY);//y轴距离
				moveDX= event.getX();
				moveDY=	event.getY();
				if(isMove){
					LayoutParams mParams = new LayoutParams(1, 1);
					mParams.width = width;
					mParams.height = height;
					int left=(int)(mRawX-downX);
					int top =(int)(mRawY-downY);
					if(left <= 0){
						left = 0;
					}else if(left > windowWidth){
						left = windowWidth-width;
					}
					if(top <= 0){
						top = 0;
					}else if(top > windowHeight){
						top = windowHeight-height;
					}
					mParams.leftMargin = left;
					mParams.topMargin = top;
					SpeakerItemView.this.setLayoutParams(mParams);
				}
				return true;
			}
			return false;
		}
	};
	public void setIsMove(boolean isneedmove){
		this.isMove = isneedmove;
	}
	public SpeakerItemView(Context context, int windowWidth, int windowHeight) {
		super(context);
		mContext = context;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		pagetype = MultiSpeakerView.NORMAL_TYPE;
		viewType = MultiSpeakerView.NORMAL_PAGE;
		this.setLayoutParams(zeroParentLP);
	}

	public void createView(SurfaceView surfaceView, String nube, String name,
						   int speakerType, DisplayMetrics metrics,boolean isShowName) {
		this.mSurfaceView = surfaceView;
		this.accountId = nube;
		this.name = name;
		this.speakerType = speakerType;
		// DisplayMetrics metric = new DisplayMetrics();
		// float
		// density=mContext.getWindowManager().getDefaultDisplay().getMetrics(metric).density;
		LayoutParams matchParentLP = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		//mSurfaceView.setBackgroundDrawable(getResources().getDrawable(R.drawable.meeting_room_wait_for_speak_bg));
		// CustomLog.e("SpeakerItemView", "mSurfaceView before" +
		// mSurfaceView.getHolder().isCreating());
		this.addView(mSurfaceView,matchParentLP);
		// CustomLog.e("SpeakerItemView", "mSurfaceView after" +
		// mSurfaceView.getHolder().isCreating());
		mLinearLayout = new LinearLayout(mContext);
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

		LayoutParams lllp = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lllp.gravity = Gravity.CENTER_VERTICAL;
		float d = metrics.density;
		//lllp.setMargins((int) (22 * d), (int) (36 * d), 0, 0);
		mLinearLayout.setLayoutParams(lllp);
		lllp.gravity = Gravity.CENTER_VERTICAL;
		mediaViewBgLP = new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mediaViewBgLP.gravity = Gravity.TOP|Gravity.CENTER_HORIZONTAL;
//		mediaViewBgLP.leftMargin = 10;
		mediaViewBgLP.topMargin = 15;
		this.addView(mLinearLayout, mediaViewBgLP);
		mTextView = new TextView(mContext);
		mTextView.setText(name);
		float value = metrics.scaledDensity;
		//mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)getResources().getDimension(R.dimen.x32));
		mTextView.setTextSize(32 / value);
		mLinearLayout.setBackgroundResource(MResource.getIdByName(mContext,
				MResource.DRAWABLE, "meeting_room_media_view_account_name_bg"));
		mLinearLayout.addView(mTextView);

		this.setOnTouchListener(viewOnTouchListener);
		/*if(isShowName){
			mLinearLayout.setVisibility(View.VISIBLE);
		}else{
			mLinearLayout.setVisibility(View.GONE);
		}*/
		setLayoutVisibility(isShowName);
		// mSurfaceView.setOnClickListener(viewOnClickListener);

	}

	public void setSpeakerItemViewParams(int width, int height, int leftMargin,
										 int topMargin,int rightMargin,int bottomMargin) {
		this.width = width;
		this.height = height;
		LayoutParams mParams = new LayoutParams(1, 1);
		mParams.width = width;
		mParams.height = height;
		mParams.leftMargin = leftMargin;
		mParams.topMargin = topMargin;
		mParams.bottomMargin =bottomMargin;
		mParams.rightMargin = rightMargin;
		// mParams.gravity = Gravity.CENTER_VERTICAL;
		CustomLog.e("SpeakerItemView", "setSpeakerItemViewParams " + accountId +",height "+height+" ,width"+width);
		SpeakerItemView.this.setLayoutParams(mParams);
	}

	public void setSpeakerItemViewCenterParams(int width, int height, int leftMargin,
											   int topMargin,int rightMargin,int bottomMargin) {
		this.width = width;
		this.height = height;
		LayoutParams mParams = new LayoutParams(1, 1);
		mParams.width = width;
		mParams.height = height;
		mParams.leftMargin = leftMargin;
		mParams.topMargin = topMargin;
		mParams.bottomMargin =bottomMargin;
		mParams.rightMargin = rightMargin;

		mParams.gravity = Gravity.CENTER;

		SpeakerItemView.this.setLayoutParams(mParams);
	}

	public void addImageIcon(int iconType) {
		switch (iconType) {
			case SHARE_DOC_TYPE:
				shareDocType = new ImageView(mContext);
				shareDocType.setBackgroundResource(MResource.getIdByName(mContext,
						MResource.DRAWABLE, "meeting_room_share_doc_img"));
				LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				mLayoutParams.leftMargin = 8;
				mLayoutParams.gravity = Gravity.CENTER;
				shareDocType.setLayoutParams(mLayoutParams);
				mLinearLayout.addView(shareDocType);
				break;
			case MIC_TYPE:
				micType = new ImageView(mContext);
				LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				mParams.leftMargin = 8;
				mParams.gravity = Gravity.CENTER;
				micType.setLayoutParams(mParams);
				micType.setBackgroundResource(MResource.getIdByName(mContext,
						MResource.DRAWABLE, "meeting_room_speaker_mic_off"));
				mLinearLayout.addView(micType);
				break;
			case CAMERA_TYPE:
				cameraType = new ImageView(mContext);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				params.leftMargin = 8;
				params.gravity = Gravity.CENTER;
				cameraType.setLayoutParams(params);
				cameraType.setBackgroundResource(MResource.getIdByName(mContext,
						MResource.DRAWABLE, "meeting_room_speaker_cam_off"));
				mLinearLayout.addView(cameraType);
				break;
			default:
				break;
		}
	}

	public boolean isExistImageIcon() {
		if (mLinearLayout != null && mLinearLayout.getChildCount() > 0) {
			return true;
		}
		return false;
	}

	public void removeAllImageIcon() {
		if (micType != null) {
			mLinearLayout.removeView(micType);
			micType = null;
		}
		if (shareDocType != null) {
			mLinearLayout.removeView(shareDocType);
			shareDocType = null;
		}
		if (cameraType != null) {
			mLinearLayout.removeView(cameraType);
			cameraType = null;
		}
	}

	public void removeImageIcon(int iconType) {
		switch (iconType) {
			case SHARE_DOC_TYPE:
				if (shareDocType != null) {
					mLinearLayout.removeView(shareDocType);
					shareDocType = null;
				}
				break;
			case MIC_TYPE:
				if (micType != null) {
					mLinearLayout.removeView(micType);
					micType = null;
				}
				break;
			case CAMERA_TYPE:
				if (cameraType != null) {
					mLinearLayout.removeView(cameraType);
					cameraType = null;
				}
				break;
			default:
				break;
		}
	}

	public void setName(String name) {
		this.name = name;
		mTextView.setText(name);
	}

	public String getAccountId() {
		return accountId;
	}

	public String getName() {
		return name;
	}

	public void setPageType(int type) {
		pagetype = type;
	}

	public void setViewType(int viewType) {
		this.viewType = viewType;
	}

	public SurfaceView getSurfaceView() {
		return mSurfaceView;
	}

	public abstract void viewOnClick(View view, int type,int viewType, String account,float x,float y);

	public abstract void viewFlingPageRight();

	public abstract void viewFlingPageLeft();

	public abstract void viewFlingPageDown();

	public abstract void viewFlingPageUp();

	public abstract void viewParam(String accountId,int top,int left,int right,int bottom);

	public  void setLayoutVisibility(boolean visibility){
		if(visibility){
			mLinearLayout.setVisibility(View.VISIBLE);
		}else{
			mLinearLayout.setVisibility(View.GONE);
		}

	}
}
