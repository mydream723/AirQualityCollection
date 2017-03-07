package com.esint.demolition.airqualitycollection.adapter;

import java.util.List;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;

/**
 * 蓝牙适配器
 * 
 * @author mx
 *
 */
public class MyBluetoothAdapter extends MyBaseAdapter {
	private List<BluetoothDevice> deviceList;

	public MyBluetoothAdapter(Context context, List<BluetoothDevice> deviceList) {
		mContext = context;
		this.deviceList = deviceList;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return deviceList.size();
	}

	@Override
	public BluetoothDevice getItem(int position) {
		return deviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			convertView = mInflater.inflate(R.layout.item_bluetooth, null);
			holder = new ViewHolder();
			holder.nameTextView = (TextView) convertView.findViewById(R.id.tv_bluetoothItem_name);
			holder.numTextView = (TextView) convertView.findViewById(R.id.tv_bluetoothItem_mac);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.nameTextView.setText(getItem(position).getName());
		holder.numTextView.setText(getItem(position).getAddress());
		return convertView;
	}

	class ViewHolder {
		TextView nameTextView, numTextView;
	}

}
