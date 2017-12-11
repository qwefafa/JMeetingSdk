package cn.redcdn.menuview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.redcdn.imservice.IMMessageBean;
import cn.redcdn.jmeetingsdk1.R;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;
import cn.redcdn.util.RoundImageView;

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
 * Created by chenghb on 2017/8/24.
 */

public class BarrageViewListAdapter extends BaseAdapter {
    private static final String TAG = "BarrageViewListAdapter";
    private List<IMMessageBean> list;
    private Context context;
    MeetingDisplayImageListener mMeetingDisplayImageListener = null;

    public BarrageViewListAdapter(Context mContext, ArrayList<IMMessageBean> msgList) {
        this.context = mContext;
        this.list = msgList;
        CustomLog.d(TAG, "list" + list);
        mMeetingDisplayImageListener = new MeetingDisplayImageListener(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.meeting_chat_receive, null);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.account_name);
            CustomLog.d(TAG, "name" + holder.name);
            holder.msg = (TextView) view.findViewById(R.id.receive_message);
            holder.time = (TextView) view.findViewById(R.id.receive_time);
            holder.headImag = (RoundImageView) view.findViewById(R.id.receive_imag);
            holder.msg_noSend = (ImageView) view.findViewById(R.id.msg_noSend);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        CustomLog.d(TAG, "是否发送成功:" + list.get(position).getMsgStatus() + "内容:" + list.get(position).getMsgContent());
        if (list.get(position).getMsgStatus() == 1) {
            holder.msg_noSend.setVisibility(View.VISIBLE);
        } else {
            holder.msg_noSend.setVisibility(View.INVISIBLE);
        }
//        timeUtils(holder, position);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String newTime = format.format(new Date(list.get(position).getTime()));
        holder.time.setText(newTime);
        holder.msg.setText(list.get(position).getMsgContent());
        String name = list.get(position).getNickName();
        if (TextUtils.isEmpty(name)) {
            holder.name.setText(list.get(position).getNubeNumber());
        } else {
            holder.name.setText(name + " " + list.get(position).getNubeNumber());
        }

        ImageLoader loader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(
                        MResource.getIdByName(context, MResource.DRAWABLE,
                                "jmeetingsdk_defaultheadimage"))// 设置图片在下载期间显示的图片
                .showImageForEmptyUri(
                        MResource.getIdByName(context, MResource.DRAWABLE,
                                "jmeetingsdk_defaultheadimage"))// 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(
                        MResource.getIdByName(context, MResource.DRAWABLE,
                                "jmeetingsdk_defaultheadimage"))// 设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)// 是否緩存都內存中
                .cacheOnDisc(true)// 是否緩存到sd卡上
                .displayer(new RoundedBitmapDisplayer(20))// 设置图片的显示方式 :
                // 设置圆角图片 int
                // roundPixels
                .bitmapConfig(Bitmap.Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        loader.displayImage(list.get(position).getHeadUrl(), holder.headImag, options, mMeetingDisplayImageListener);
        return view;
    }

    class ViewHolder {
        private TextView name;
        private TextView msg;
        private TextView time;
        private RoundImageView headImag;
        private ImageView msg_noSend;
    }

}
