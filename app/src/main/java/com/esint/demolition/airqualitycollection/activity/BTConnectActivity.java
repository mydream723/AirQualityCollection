package com.esint.demolition.airqualitycollection.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;
import com.esint.demolition.airqualitycollection.adapter.MyBluetoothAdapter;
import com.esint.demolition.airqualitycollection.dialog.MultDialog;
import com.esint.demolition.airqualitycollection.dialog.SingleDialog;
import com.esint.demolition.airqualitycollection.services.AirQualityService;
import com.esint.demolition.airqualitycollection.utils.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017-02-28.
 * 蓝牙连接页面
 */

public class BTConnectActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "BTConnectActivity";

    /**
     * 搜索按键
     */
    private Button searchButton;
    /**
     * 取得默认的蓝牙适配器
     */
    private BluetoothAdapter mBtAdapter;
    /**
     * 已绑定的设备列表
     */
    private TextView bondedTextView;
    private List<BluetoothDevice> bondedDeviceList;
    private ListView bondedListView;
    private MyBluetoothAdapter bondedAdapter;

    /**
     * 为配对设备
     */
    private TextView unbondedTextView;
    private List<BluetoothDevice> unBondDeviceList;
    private ListView unBondListView;
    private MyBluetoothAdapter unBondAdapter;
    /**
     * 蓝牙设备mac地址
     */
    private String btDeviceMac;
    /**
     * 是否为修复蓝牙连接
     */
    private boolean isRepairConnect = false;
    private BTDeviceBroadcastReceiver BTReceiver;
    /**
     * 可连接蓝牙设备 显示内容
     */
    private TextView unBondedEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initData();
        initView();
        initEvent();

        bondedListView.setAdapter(bondedAdapter);
        unBondListView.setAdapter(unBondAdapter);

        mBtAdapter.startDiscovery();
        searchButton.setText(R.string.btconnect_button_search_stop);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // 如果蓝牙未开启， 开启蓝牙
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        try {
            this.unregisterReceiver(mReceiver);
        } catch (Exception e) {

        }
        try {
            if (null != BTReceiver) {
                unregisterReceiver(BTReceiver);
            }
        } catch (Exception e) {

        }

    }


    @Override
    protected void initData() {
        super.initData();
        mContext = BTConnectActivity.this;
        isRepairConnect = getIntent().getBooleanExtra(Constants.INTENT_BLUETOOTH_RECONNECT, false);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        bondedDeviceList = new ArrayList<BluetoothDevice>();
        unBondDeviceList = new ArrayList<BluetoothDevice>();
        // 注册查找设备列表
        IntentFilter discoveryFilter = new IntentFilter();
        discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, discoveryFilter);

        // 注册查找设备结束
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, foundFilter);

        // 已绑定的设备列表
//		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
//		for (BluetoothDevice bluetoothDevice : pairedDevices) {
//			bondedDeviceList.add(bluetoothDevice);
//		}

        bondedAdapter = new MyBluetoothAdapter(mContext, bondedDeviceList);
        unBondAdapter = new MyBluetoothAdapter(mContext, unBondDeviceList);


    }

    @Override
    protected void initEvent() {
        super.initEvent();
        searchButton.setOnClickListener(this);
        bondedListView.setOnItemClickListener(this);
        unBondListView.setOnItemClickListener(this);

    }

    private void initView(){

        initSubTitle();
        titleTextView.setText(R.string.btconnect_title);
        functionTextView.setVisibility(View.INVISIBLE);

        searchButton = (Button) findViewById(R.id.bt_bluetoothActivity_search);
        bondedTextView = (TextView) findViewById(R.id.tv_bluetoothActivity_bonded);
        bondedListView = (ListView) findViewById(R.id.lv_bluetoothActivity_bonded);
        unbondedTextView = (TextView) findViewById(R.id.tv_bluetoothActivity_unbonded);
        unBondListView = (ListView) findViewById(R.id.lv_bluetoothActivity_unBonded);

        TextView bondedEmptyTextView = (TextView) findViewById(R.id.tv_bluetoothActivity_bondedEmpty);
        bondedListView.setEmptyView(bondedEmptyTextView);

        unBondedEmptyTextView = (TextView) findViewById(R.id.tv_bluetoothActivity_unbondedEmpty);
        unBondListView.setEmptyView(unBondedEmptyTextView);
        // if (null != bondedDeviceList && bondedDeviceList.size() > 0) {
        // bondedTextView.setVisibility(View.VISIBLE);
        // } else {
        // bondedTextView.setVisibility(View.GONE);
        // }

    }

    @Override
    public void onClick(View v) {

        super.onClick(v);
        switch (v.getId()) {
            case R.id.bt_bluetoothActivity_search:
                // 搜素蓝牙
                if (mBtAdapter.isDiscovering()) {
                    // 停止搜索
                    mBtAdapter.cancelDiscovery();
                    searchButton.setText(R.string.btconnect_button_search_again);
                    unBondedEmptyTextView.setText(R.string.btconnect_alert_noUnBondedDevices);
                } else {
                    // 重新搜索
                    unBondDeviceList.clear();
                    unBondAdapter.notifyDataSetChanged();
                    unBondedEmptyTextView.setText(R.string.btconnect_alert_connecting_message);
                    mBtAdapter.startDiscovery();
                    searchButton.setText(R.string.btconnect_button_search_stop);
                }

                break;

            default:
                break;
        }

    }

    /**
     * 搜索蓝牙设备
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 没有绑定的设备
                    if(unBondDeviceList.indexOf(device) == -1){
                        unBondDeviceList.add(device);
                        unBondAdapter.notifyDataSetChanged();
                    }

                }else{
                    if(bondedDeviceList.indexOf(device) == -1){
                        bondedDeviceList.add(device);
                        bondedAdapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // 搜索完成
                setProgressBarIndeterminateVisibility(false);
                searchButton.setText(R.string.btconnect_button_search_again);
                unBondedEmptyTextView.setText(R.string.btconnect_alert_noUnBondedDevices);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 蓝牙连接状态改变
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        // 完成配对
                        cancelLoadingDialog();
                        bondedDeviceList.add(device);
                        unBondDeviceList.remove(device);
                        bondedAdapter.notifyDataSetChanged();
                        unBondAdapter.notifyDataSetChanged();

                        if (null != bondedDeviceList && bondedDeviceList.size() > 0) {
                            bondedTextView.setVisibility(View.VISIBLE);
                        } else {
                            bondedTextView.setVisibility(View.GONE);
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                        // 取消配对
                        cancelLoadingDialog();

                        if (null != bondedDeviceList && bondedDeviceList.size() > 0) {
                            bondedTextView.setVisibility(View.VISIBLE);
                        } else {
                            bondedTextView.setVisibility(View.GONE);
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        switch (parent.getId()) {
            case R.id.lv_bluetoothActivity_bonded:
                // 已绑定
                MultDialog connectDialog = new MultDialog(mContext);
                connectDialog.setMessage(
                        getString(R.string.btconnect_alert_connect_message_part1) + bondedDeviceList.get(position).getAddress()
                                + getString(R.string.btconnect_alert_connect_message_part2));
                connectDialog.setPosMessage(getString(R.string.btconnect_alert_connect_message_sure));
                connectDialog.setNegMessage(getString(R.string.btconnect_alert_connect_message_cancel));
                connectDialog.setMultClickListener(new MultDialog.MultButtonClickListener() {

                    @Override
                    public void posClick(Dialog dialog) {
                        dialog.dismiss();
                        mBtAdapter.cancelDiscovery();
                        searchButton.setText(R.string.btconnect_button_search_again);

                        unBondedEmptyTextView.setText(R.string.btconnect_alert_noUnBondedDevices);
                        showLoadingDialog(mContext);
                        Intent connectIntent = new Intent(mContext, AirQualityService.class);
                        connectIntent.putExtra(Constants.INTENT_BLUETOOTH_ADDRESS, bondedDeviceList.get(position).getAddress());
                        startService(connectIntent);

                        BTReceiver = new BTDeviceBroadcastReceiver();
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(AirQualityService.ACTION_BLUETOOTH_CONNECT_SUC);
                        filter.addAction(AirQualityService.ACTION_BLUETOOTH_CONNECT_FAIL);
                        registerReceiver(BTReceiver, filter);
                        btDeviceMac = bondedDeviceList.get(position).getAddress();

                    }

                    @Override
                    public void negClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                connectDialog.setCanceledOnTouchOutside(false);
                connectDialog.show();
                break;

            case R.id.lv_bluetoothActivity_unBonded:
                // 未绑定
                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                    searchButton.setText(R.string.btconnect_button_search_again);
                    unBondedEmptyTextView.setText(R.string.btconnect_alert_noUnBondedDevices);
                }
                String address = unBondDeviceList.get(position).getAddress();
                BluetoothDevice btDev = mBtAdapter.getRemoteDevice(address);
                try {
                    Boolean returnValue = false;
                    if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
                        // 利用反射方法调用BluetoothDevice.createBond(BluetoothDevice
                        // remoteDevice);
                        showLoadingDialog(mContext);
                        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                        returnValue = (Boolean) createBondMethod.invoke(btDev);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    /**
     * 蓝牙设备广播接收
     *
     * @author mx
     *
     */
    private class BTDeviceBroadcastReceiver extends BroadcastReceiver {
        private String action;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (action.equals(AirQualityService.ACTION_BLUETOOTH_CONNECT_SUC)) {
                // 连接成功
                cancelLoadingDialog();
                SingleDialog dialog = new SingleDialog(mContext);
                dialog.setMessage(getString(R.string.btconnect_connect_suc));
                dialog.setSingleListener(new SingleDialog.SingleButtonClickListener() {
                    @Override
                    public void clickPos(Dialog dialog) {
                        dialog.dismiss();
                        Log.e(TAG,"!!!!!!!!!!");

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString(Constants.SHARED_AIRQUALITY_DEVICE_MAC, btDeviceMac);
                        editor.commit();
                        if (isRepairConnect) {
                            repairSuc();
                        }else{
                            Intent intent = new Intent(mContext, CollectionActivity.class);
                            startActivity(intent);
                        }



                    }
                });
                dialog.show();


            } else if (action.equals(AirQualityService.ACTION_BLUETOOTH_CONNECT_FAIL)) {
                // 连接失败
                cancelLoadingDialog();

                showAlertDialog(mContext, getString(R.string.btconnect_connect_fail));

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(Constants.SHARED_AIRQUALITY_DEVICE_MAC, "");
                editor.commit();

            }

        }

    }
    private void repairSuc() {
        setResult(RESULT_OK);
        finish();
    }

}
