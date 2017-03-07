package com.esint.demolition.airqualitycollection.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;
import com.esint.demolition.airqualitycollection.bean.SurveillanceInfo;

import java.util.List;

/**
 * Created by Administrator on 2017-02-28.
 */

public class ProjectAdapter extends MyBaseAdapter {
    private List<SurveillanceInfo> infoList;

    public ProjectAdapter(Context context, List<SurveillanceInfo> infoList) {
        this.infoList = infoList;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public SurveillanceInfo getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.item_projects, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iv_projectItem_icon);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.tv_projectItem_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.nameTextView.setText(getItem(position).getName());
        if (getItem(position).getCamera() == 1) {
            //视频监控
            holder.iconImageView.setImageResource(R.drawable.icon_surveillance);
        } else if (getItem(position).getFly() == 1) {
            //无人机
            holder.iconImageView.setImageResource(R.drawable.icon_fly);
        } else {
            holder.iconImageView.setImageResource(R.drawable.icon_device_null);
        }


        return convertView;
    }

    class ViewHolder {
        TextView nameTextView;
        ImageView iconImageView;
    }
}
