package com.example.quantacup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.quantacup.R;
import com.example.quantacup.bean.MessageUtil;

import java.util.List;

public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<MessageUtil> list;
    private LayoutInflater inflater;
    private int COME_MSG = 0;
    private int TO_MSG = 1;



    public MyAdapter(Context context, List<MessageUtil> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
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
        //缓存机制

        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();

            //判断是发送还是接收，从而设置风格样式靠左还是靠右
            if (list.get(position).isJudge()) {
                convertView = inflater.inflate(R.layout.list_item_right, null);
            } else {
                convertView = inflater.inflate(R.layout.list_item_left, null);
            }
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.time.setText(list.get(position).getTime());
        holder.content.setText(list.get(position).getMessage());
        return convertView;
    }



    static class ViewHolder {
        TextView time, content;
    }



    @Override

    public int getItemViewType(int position) {
        // 区别两种view的类型，标注两个不同的变量来分别表示各自的类型
        MessageUtil util = list.get(position);

        if (util.isJudge()) {
            return COME_MSG;
        } else {
            return TO_MSG;
        }
    }

    @Override
    public int getViewTypeCount() {
        // 这个方法默认返回1，如果希望listview的item都是一样的就返回1，我们这里有两种风格，返回2
        return 2;
    }

}
