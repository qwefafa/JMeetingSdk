package cn.redcdn.util;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import cn.redcdn.log.CustomLog;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

/**
 * 　　　　　　　　┏┓　　　┏┓+ +
 * 　　　　　　　┏┛┻━━━┛┻┓ + +
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃ ++ + + +
 * 　　　　　　 ████━████ ┃+
 * 　　　　　　　┃　　　　　　　┃ +
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　　┃ + +
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃ + + + +
 * 　　　　　　　　　┃　　　┃　　　　Code is far away from bug with the animal protecting
 * 　　　　　　　　　┃　　　┃ + 　　　　神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　+
 * 　　　　　　　　　┃　 　　┗━━━┓ + +
 * 　　　　　　　　　┃ 　　　　　　　┣┓
 * 　　　　　　　　　┃ 　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛+ + + +
 * Created by chenghb on 2017/8/27.
 */

public class DanmuControl {
    private static final String TAG = "DanmuControl";
    private Context mContext;
    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;

    public DanmuControl(Context context, IDanmakuView danmakuView) {
        this.mContext = context;
        this.mDanmakuView = danmakuView;
        initDanmuConfig();
    }

    /**
     * 初始化配置
     */
    private void initDanmuConfig() {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 2); // 滚动弹幕最大显示2行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext
                .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(2.5f)//越大速度越慢
                .setScaleTextSize(1.2f)
                .setCacheStuffer(new MyTextStuffer(mContext), mCacheStufferAdapter)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);


        if (mDanmakuView != null) {
            mDanmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {
                    mDanmakuView.start();
                }

                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                }

                @Override
                public void drawingFinished() {
                }
            });
        }

        mDanmakuView.prepare(new BaseDanmakuParser() {

            @Override
            protected Danmakus parse() {
                return new Danmakus();
            }
        }, mDanmakuContext);
        mDanmakuView.enableDanmakuDrawingCache(true);
    }

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // tag包含bitmap，一定要清空
            danmaku.tag = null;
        }
    };

    public void addDanmu(final String avatorUrl, final String name, final String content) {
        final BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);

//        Glide.with(mContext).load(avatorUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
//            @Override
//            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//
//                Map<String, Object> map = new HashMap<String, Object>();
//                Bitmap bitmap = makeRoundCorner(resource);
//                map.put("name", name);
//                map.put("content", content);
//                map.put("bitmap", bitmap);
//                danmaku.tag = map;
//                danmaku.text = "";
//                danmaku.padding = 0;
//                danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
//                danmaku.isLive = true;
//                danmaku.setTime(mDanmakuView.getCurrentTime());
//                danmaku.textSize = 0;
//                mDanmakuView.addDanmaku(danmaku);
//            }
//        });
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", name);
            map.put("content", content);
            danmaku.tag = map;
            danmaku.text = "";
            danmaku.padding = 0;
            danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
            danmaku.isLive = true;
            danmaku.setTime(mDanmakuView.getCurrentTime());
            danmaku.textSize = 0;
            mDanmakuView.addDanmaku(danmaku);
        } catch (Exception e) {
            CustomLog.d(TAG, "danmu为空:" + danmaku);

        }


//        addDanmaku(content,true);
    }

    //    private void addDanmaku(String content, boolean withBorder) {
//        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
//        danmaku.text = content;
//        danmaku.padding = 5;
//        danmaku.textSize = sp2px(20);
//        danmaku.textColor = Color.WHITE;
//        danmaku.setTime(mDanmakuView.getCurrentTime());
//        if (withBorder) {
//            danmaku.borderColor = Color.GREEN;
//        }
//        mDanmakuView.addDanmaku(danmaku);
//    }
    public int sp2px(float spValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    /**
     * 将图片变成圆形
     *
     * @param bitmap
     * @return
     */
//    private static Bitmap makeRoundCorner(Bitmap bitmap) {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int left = 0, top = 0, right = width, bottom = height;
//        float roundPx = height / 2;
//        if (width > height) {
//            left = (width - height) / 2;
//            top = 0;
//            right = left + height;
//            bottom = height;
//        } else if (height > width) {
//            left = 0;
//            top = (height - width) / 2;
//            right = width;
//            bottom = top + width;
//            roundPx = width / 2;
//        }
//        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(output);
//        int color = 0xff424242;
//        Paint paint = new Paint();
//        Rect rect = new Rect(left, top, right, bottom);
//        RectF rectF = new RectF(rect);
//        paint.setAntiAlias(true);
//        canvas.drawARGB(0, 0, 0, 0);
//        paint.setColor(color);
//        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(bitmap, rect, rect, paint);
//        return output;
//    }
}
