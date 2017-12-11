package cn.redcdn.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;

import java.util.Map;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;

/**
 * Desc
 * Created by wangkai on 2017/8/28.
 */

public class MyTextStuffer extends SpannedCacheStuffer {

    private int TEXT_SIZE; // 文字大小
    private int TEXT_LEFT_PADING;
    private int TEXT_TOP_PADDING;

    private int NICK_COLOR = 0xff3aabcb;//昵称
    private int TEXT_COLOR = 0xffeeeeee;  //文字内容  白色
    private int TEXT_HEIGHT;
    private BaseDanmaku danmaku;

    public MyTextStuffer(Context context) {
        // 初始化固定参数，这些参数可以根据自己需求自行设定
        TEXT_SIZE = dp2px(context, 17);
        TEXT_LEFT_PADING = dp2px(context, 12);
        TEXT_TOP_PADDING = dp2px(context,1);
        TEXT_HEIGHT = dp2px(context,19);
    }

    @Override
    public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
        this.danmaku = danmaku;
        // 初始化数据
        Map<String, Object> map = (Map<String, Object>) danmaku.tag;
        String name = (String)map.get("name") + ":";
        String content = (String) map.get("content");

        // 设置画笔
        paint.setTextSize(TEXT_SIZE);
        // 计算名字和内容的长度，取最大值
//        float nameWidth = paint.measureText(name);
//        float contentWidth = paint.measureText(content);

        Rect bounds = new Rect();
        String showText = name + content;
        paint.getTextBounds(showText, 0, showText.length(), bounds);

        // 设置弹幕区域的宽高
        danmaku.paintWidth = bounds.width() + TEXT_LEFT_PADING * 2;// 设置弹幕区域的宽度
        danmaku.paintHeight = bounds.height() + TEXT_TOP_PADDING * 4;// 设置弹幕区域的高度
    }


    @Override
    public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {

        Map<String, Object> map = (Map<String, Object>) danmaku.tag;
        String name = map.get("name") + ":";
        String content = (String) map.get("content");

        String allContent = name +  content;
        // 设置画笔
        Paint paint = new Paint();
        paint.setTextSize(TEXT_SIZE);

        // 绘制文字灰色背景
        Rect rect = new Rect();
        paint.getTextBounds(allContent, 0, allContent.length(), rect);
        paint.setAntiAlias(true);

        // 绘制名字
        paint.setColor(NICK_COLOR);
        float nameLeft = left;
        float nameBottom = rect.height();
        canvas.drawText(name, nameLeft, nameBottom, paint);
//        paint.setShadowLayer(10f,1,3,Color.BLACK);

        // 绘制弹幕内容
        paint.setColor(TEXT_COLOR);
        float contentLeft = nameLeft + paint.measureText(name) + TEXT_LEFT_PADING;
        canvas.drawText(content, contentLeft, nameBottom, paint);
//        paint.setShadowLayer(10f,1,3,Color.BLACK);
    }

    private int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

}
