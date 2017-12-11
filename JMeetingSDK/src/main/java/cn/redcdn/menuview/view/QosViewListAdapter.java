package cn.redcdn.menuview.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.jmeetingsdk1.R;


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
 * Created by chenghb on 2017/11/13.
 */

public class QosViewListAdapter extends BaseAdapter {
    private static final String TAG = "QosViewListAdapter";
    private List<QosView.QosItem> itemList;
    private Context mContext;
    public QosViewListAdapter(Context context, ArrayList<QosView.QosItem> list) {
        this.mContext = context;
        this.itemList = list;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.meeting_room_menu_qos_item_view,null);
            holder = new ViewHolder();
            holder.content = (TextView) view.findViewById(R.id.qosInfoTitleTextView);
            holder.title = (TextView) view.findViewById(R.id.qosInfoTitle);
            view.setTag(holder);
        }else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        if (itemList.get(position).getTitle()!= null){
            holder.title.setText(itemList.get(position).getTitle());
            char[] titlechar = itemList.get(position).getTitle().toCharArray();
            for (int i =0 ;i<titlechar.length;i++){
                if (titlechar[i] == '信'&&titlechar[i+1] == '息'){
                    holder.content.setVisibility(View.GONE);
                }
            }
        }

        if (itemList.get(position).getContent()!= null){
            holder.content.setText(itemList.get(position).getContent());
        }

        return view;
    }

    class ViewHolder {
        private TextView content;
        private TextView title;
    }
}
