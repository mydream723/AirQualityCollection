package com.esint.demolition.airqualitycollection.activity;

import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.esint.demolition.airqualitycollection.R;
import com.esint.demolition.airqualitycollection.bean.AirqualityForCollection;
import com.esint.demolition.airqualitycollection.bean.JsonResult;
import com.esint.demolition.airqualitycollection.dialog.SingleDialog;
import com.esint.demolition.airqualitycollection.myinterface.AirQualityListener;
import com.esint.demolition.airqualitycollection.services.AirQualityService;
import com.esint.demolition.airqualitycollection.utils.Constants;
import com.esint.demolition.airqualitycollection.utils.JsonUtils;
import com.esint.demolition.airqualitycollection.utils.WebConstants;
import com.esint.demolition.airqualitycollection.utils.WebOkHttpUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017-03-01.
 * 空气采集上传页面
 */

public class CollectionActivity extends BaseActivity implements AirQualityListener {
    private static final String TAG = "CollectionActivity";
    private static final long TIME_UPLOAD = 1000 * 30;
    /**
     * 获得空气质量
     */
    private static final int FLAG_AIRQUALITY_GETVALUE = 0x3001;
    /**
     * 连接蓝牙设备
     */
    private static final int REQUEST_BLUETOOTH_TOCONNECT = 0x1001;
    /**
     * 拆迁点id
     */
    private int projectId;
    /**
     * 蓝牙断开连接dialog
     */
    private SingleDialog connectDialog;
    /**
     * 判断activity是否正在运行
     */
    private boolean isActivityRun = false;
    /**
     * 空气质量服务
     */
    private AirQualityService mAirQualityService;
    /**
     * 空气质量服务绑定
     */
    private AirqualityServiceConnection mAirqualityServiceConnection;
    /**
     * 空气质量服务是否绑定
     */
    private boolean isAirqualityServiceBinded = false;
    /**
     * 蓝牙状态接收广播
     */
    private BTStatusBroadcastReceiver btStatusBroadcastReceiver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WebOkHttpUtils.WEBFLAG_ERROR:
                    //网络问题
                    Toast.makeText(mContext, "网络连接错误", Toast.LENGTH_SHORT).show();
                    break;
                case WebConstants.WEBFLAG_UPLOAD_AIRQUALITY:
                    //上传
                    Log.e(TAG, "result:" + msg.obj.toString());
                    try {
                        JsonResult resultJson = JsonUtils.getInstance().getJsonResult(msg.obj.toString());
                        if(null != resultJson){
                            int code = resultJson.getCode();
                            switch (code){
                                case WebConstants.RESULT_SUC:
                                    //上传成功

                                    break;
                                default:
                                    Toast.makeText(mContext, "上传返回值错误", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }else{
                            Toast.makeText(mContext, "上传返回值错误", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "上传返回值错误", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        initData();
        initView();
        initEvent();

        isActivityRun = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(AirQualityService.ACTION_BLUETOOTH_CONNECT_FAIL);
        registerReceiver(btStatusBroadcastReceiver, filter);
        // 绑定空气质量服务
        Intent airqualityIntent = new Intent(mContext, AirQualityService.class);
        airqualityIntent.putExtra(Constants.INTENT_BLUETOOTH_ADDRESS,
                mSharedPreferences.getString(Constants.SHARED_AIRQUALITY_DEVICE_MAC, ""));
        mAirqualityServiceConnection = new AirqualityServiceConnection();
        bindService(airqualityIntent, mAirqualityServiceConnection, Service.BIND_AUTO_CREATE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                collectAirquality();
            }
        }, 0, TIME_UPLOAD);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityRun = false;
        // 解绑空气质量服务
        if (isAirqualityServiceBinded && null != mAirqualityServiceConnection && null != mAirQualityService
                && mAirQualityService.isRestricted())
            unbindService(mAirqualityServiceConnection);
        mAirQualityService = null;
    }

    @Override
    protected void initData() {
        super.initData();
        mContext = CollectionActivity.this;
        projectId = mApplication.getSelectedProjectId();
    }

    @Override
    protected void initEvent() {
        super.initEvent();
    }

    private void initView() {
        initSubTitle();
        titleTextView.setText(R.string.collection_title);
        functionTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public void BTDeviceDisconnect() {
        // 蓝牙设备断开连接
        if (isActivityRun) {
            Log.e("AirQualityService", "BTDeviceDisconnect");
            BTDisconnectDialog();
        }

    }

    @Override
    public void BTDeviceConnected() {
        // 蓝牙设备连接成功
        if (null != connectDialog && connectDialog.isShowing())
            connectDialog.dismiss();

    }

    /**
     * 蓝牙连接状态接收广播
     *
     * @author mx
     */
    private class BTStatusBroadcastReceiver extends BroadcastReceiver {
        private String action;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (action.equals(AirQualityService.ACTION_BLUETOOTH_CONNECT_FAIL)) {
                if (isActivityRun) {
                    Log.e("AirQualityService", "BTDeviceDisconnect");
                    BTDisconnectDialog();
                }
            }
        }

    }

    /**
     * 蓝牙断开连接dialog
     */
    private void BTDisconnectDialog() {
        if (null != connectDialog && !connectDialog.isShowing() || null == connectDialog) {
            connectDialog = new SingleDialog(mContext);
            connectDialog.setMessage(getString(R.string.collection_alert_bluetooth_disconnect_message));
            connectDialog.setPosButtonText(getString(R.string.collection_alert_bluetooth_disconnect_sure));
            connectDialog.setSingleListener(new SingleDialog.SingleButtonClickListener() {

                @Override
                public void clickPos(Dialog dialog) {
                    dialog.dismiss();
                    // 解绑空气质量服务
                    if (isAirqualityServiceBinded && null != mAirqualityServiceConnection && null != mAirQualityService
                            && mAirQualityService.isRestricted())
                        unbindService(mAirqualityServiceConnection);
                    mAirQualityService = null;

                    Intent btSettingIntent = new Intent(mContext, BTConnectActivity.class);
                    btSettingIntent.putExtra(Constants.INTENT_BLUETOOTH_RECONNECT, true);
                    startActivityForResult(btSettingIntent, REQUEST_BLUETOOTH_TOCONNECT);
                }
            });
            connectDialog.setCanceledOnTouchOutside(false);
            connectDialog.show();
        }
    }

    /**
     * 空气质量服务绑定连接
     *
     * @author mx
     */
    private class AirqualityServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("AirQualityService", "onServiceConnected");
            AirQualityService.AirQualityBinder mBinder = (AirQualityService.AirQualityBinder) service;
            mAirQualityService = mBinder.getService();
            mAirQualityService.setAirqualityListener(CollectionActivity.this);
            isAirqualityServiceBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isAirqualityServiceBinded = false;
        }

    }


    /**
     * 采集空气质量
     */
    private void collectAirquality() {
        Log.e(TAG, "collectAirquality");
        if (null != mAirQualityService) {

            AirqualityForCollection airquality = mAirQualityService.getAirQuality();
            if (null != airquality && null != airquality.getPm25() && !airquality.getPm25().isEmpty() && null != airquality.getPm10() && !airquality.getPm10().isEmpty()) {
                sendAirquality(airquality.getPm25(), airquality.getPm10());
            } else {
                Log.e(TAG, "get null");
            }
        }
    }

    private void sendAirquality(String pm25, String pm10) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(WebConstants.PARAMS_UPLOADAIRQUALITY_PROJECTID, projectId + "");
        params.put(WebConstants.PARAMS_UPLOADAIRQUALITY_PM25, pm25);
        params.put(WebConstants.PARAMS_UPLOADAIRQUALITY_PM10, pm10);
        WebOkHttpUtils.getInstance().getRequest(WebConstants.URL_BASE + WebConstants.URL_UPLOAD_AIRQUALITY, params, mHandler, WebConstants.WEBFLAG_UPLOAD_AIRQUALITY);
    }
}
