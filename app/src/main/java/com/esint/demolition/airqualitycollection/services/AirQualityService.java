package com.esint.demolition.airqualitycollection.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.esint.demolition.airqualitycollection.application.CollectionApplication;
import com.esint.demolition.airqualitycollection.bean.AirqualityForCollection;
import com.esint.demolition.airqualitycollection.myinterface.AirQualityListener;
import com.esint.demolition.airqualitycollection.utils.Constants;

/**
 * 空气质量服务
 * 
 * @author mx
 *
 */
public class AirQualityService extends Service {
	private static final String TAG = "AirQualityService";
	/**
	 * 相邻两次上下浮动
	 */
	private static final float VALUE_UPANDDOWN = 1;
	/**
	 * 蓝牙连接成功
	 */
	public static final String ACTION_BLUETOOTH_CONNECT_SUC = "com.esint.supervision.bluetooth.connect.suc";
	/**
	 * 蓝牙连接失败
	 */
	public static final String ACTION_BLUETOOTH_CONNECT_FAIL = "com.esint.supervision.bluetooth.connect.fail";
	/**
	 * 判断内容的正则表达式
	 */
	private static final String regEx = "^AAA503[0-9A-Z]{20}06[0-9A-Z]{8}AB$";
	private CollectionApplication mApplication;
	/**
	 * 空气质量状态监听
	 */
	private AirQualityListener mListener;

	/**
	 * 蓝牙正在连接
	 */
	public static final int BLUETOOTH_CONNECTING = 0x3001;
	/**
	 * 蓝牙连接成功
	 */
	public static final int BLUETOOTH_CONNECT_SUC = 0x3002;
	/**
	 * 蓝牙连接失败
	 */
	public static final int BLUETOOTH_CONNECT_FAIL = 0x3003;
	/**
	 * 蓝牙断开连接
	 */
	public static final int BLUETOOTH_DISCONNECT = 0x3005;
	/**
	 * 获得蓝牙值成功
	 */
	public static final int BLUETOOTH_VALUE_SUC = 0x3004;
	/**
	 * 蓝牙连接状态
	 */
	public static int BLUETOOTH_CONNNECT_STATUS = 0;
	/**
	 * 蓝牙设备
	 */
	private BluetoothDevice device;
	private BluetoothAdapter mBluetoothAdapter;
	/**
	 * 设备地址
	 */
	private String bluetoothAddress;
	/**
	 * 连接蓝牙
	 */
	private ClientThread clientConnectThread;
	/**
	 * 读取数据
	 */
	private ReadThread mReadThread;
	private BluetoothSocket socket;
	// 正则判断
	private Pattern pattern;
	// pm值
	private String airqualityCollectionString;
	private AirqualityForCollection airqualityCollection;

	private AirQualityBinder mBinder = new AirQualityBinder();
	/**
	 * 判断是否需要在连接后自动读取数值，只在绑定服务成功后执行
	 */

	private BluetoothBroadcastReceiver btReceiver;
	/**
	 * 上次接收值
	 */
	private float lastPM25, lastPM10;
	private Handler bluetoothHandlerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BLUETOOTH_CONNECTING:
				// 正在连接
				Log.e(TAG, "connecting");
				BLUETOOTH_CONNNECT_STATUS = BLUETOOTH_CONNECTING;

				break;
			case BLUETOOTH_CONNECT_SUC:
				// 连接成功
				Log.e(TAG, "connect successful");
				mApplication.setBluetoothConnected(true);

				BLUETOOTH_CONNNECT_STATUS = BLUETOOTH_CONNECT_SUC;

				// 启动接受数据
				mReadThread = new ReadThread();
				mReadThread.start();
				// 设置蓝牙状态广播
				btReceiver = new BluetoothBroadcastReceiver();
				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
				registerReceiver(btReceiver, filter);
				if (null != mListener)
					mListener.BTDeviceConnected();
				// 发送连接成功广播
				Intent connectedIntent = new Intent();
				connectedIntent.setAction(ACTION_BLUETOOTH_CONNECT_SUC);
				sendBroadcast(connectedIntent);

				break;
			case BLUETOOTH_CONNECT_FAIL:
				// 连接失败
				Log.e(TAG, "connect fail");
				mApplication.setBluetoothConnected(false);

				BLUETOOTH_CONNNECT_STATUS = BLUETOOTH_CONNECT_FAIL;
				if (null != mListener)
					mListener.BTDeviceDisconnect();
				// 发送连接失败广播
				Intent disConnectIntent = new Intent();
				disConnectIntent.setAction(ACTION_BLUETOOTH_CONNECT_FAIL);
				sendBroadcast(disConnectIntent);

				break;
			case BLUETOOTH_VALUE_SUC:
				// 获得值成功
				if (null != msg.obj && null != msg.obj.toString()) {
					Log.e(TAG, "getVaule:" + msg.obj.toString());
					airqualityCollectionString = msg.obj.toString();
				} else {
					if (null != mListener)
						mListener.BTDeviceDisconnect();
				}

				break;
			case BLUETOOTH_DISCONNECT:
				// 蓝牙断开连接
				if (null != mListener)
					mListener.BTDeviceDisconnect();
				break;
			}
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "onBind");
		if (BLUETOOTH_CONNNECT_STATUS != BLUETOOTH_CONNECT_SUC) {
			// 当前未绑定设备
			Log.e(TAG, "to connect");
			bluetoothAddress = intent.getStringExtra(Constants.INTENT_BLUETOOTH_ADDRESS);
			if (null != bluetoothAddress && !bluetoothAddress.isEmpty() && !bluetoothAddress.equals("null")) {
				device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
				clientConnectThread = new ClientThread();
				clientConnectThread.start();
			} else {
				Log.e(TAG, "have not address");
				// 没有连接地址
				if (null != mListener) {
					mListener.BTDeviceDisconnect();
				}

			}
		}
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "onUnbind");
		disconnect();
		stopSelf();
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "onCreate");
		super.onCreate();
		initData();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "onStartCommand");
		if (null != intent)
			bluetoothAddress = intent.getStringExtra(Constants.INTENT_BLUETOOTH_ADDRESS);
		if (null != bluetoothAddress && !bluetoothAddress.isEmpty() && !bluetoothAddress.equals("null")) {
			Log.e(TAG, "mac is:" + bluetoothAddress);
			device = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
			clientConnectThread = new ClientThread();
			clientConnectThread.start();
		} else {
			// 没有连接地址
			// // 发送连接失败广播
			// Intent disConnectIntent = new Intent();
			// disConnectIntent.setAction(ACTION_BLUETOOTH_CONNECT_FAIL);
			// sendBroadcast(disConnectIntent);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		disconnect();
		try {
			if (null != btReceiver) {
				unregisterReceiver(btReceiver);
			}
		} catch (Exception e) {

		}

	}

	private void initData() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		pattern = Pattern.compile(regEx);
		mApplication = (CollectionApplication) getApplication();
	}

	/**
	 * 设置监听
	 * 
	 * @param listener
	 */
	public void setAirqualityListener(AirQualityListener listener) {
		this.mListener = listener;
	}

	// 开启客户端
	private class ClientThread extends Thread {
		public void run() {
			try {
				// 删除历史连接
				disconnect();
				// 创建一个Socket连接：只需要服务器在注册时的UUID号
				socket = device
						.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				// 正在连接
				Message connectingMsg = new Message();
				connectingMsg.what = BLUETOOTH_CONNECTING;
				bluetoothHandlerHandler.sendMessage(connectingMsg);
				socket.connect();
				// 连接成功
				Message connectSucMsg = new Message();
				connectSucMsg.what = BLUETOOTH_CONNECT_SUC;
				bluetoothHandlerHandler.sendMessage(connectSucMsg);
			} catch (IOException e) {
				Message msg = new Message();
				msg.what = BLUETOOTH_CONNECT_FAIL;
				bluetoothHandlerHandler.sendMessage(msg);
			}
		}
	};

	/**
	 * 断开蓝牙连接
	 */
	private void disconnect() {
		BLUETOOTH_CONNNECT_STATUS = BLUETOOTH_CONNECT_FAIL;
		if (clientConnectThread != null) {
			clientConnectThread.interrupt();
			clientConnectThread = null;
		}
		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {

			}
			socket = null;
		}
	}

	// 读取数据
	private class ReadThread extends Thread {
		public void run() {
			StringBuilder submitBuilder = new StringBuilder();
			byte[] buffer = new byte[1024];
			int bytes;
			InputStream mmInStream = null;
			try {
				mmInStream = socket.getInputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while (true) {
				if (socket.isConnected()) {
					try {
						// Read from the InputStream
						if ((bytes = mmInStream.read(buffer)) > 0) {
							byte[] buf_data = new byte[bytes];
							for (int i = 0; i < bytes; i++) {
								buf_data[i] = buffer[i];
							}
							String message = bytesToHexString(buf_data);
							if (message.equals("AA")) {
								submitBuilder = new StringBuilder();
							}
							submitBuilder.append(message);
							String getMessage = submitBuilder.toString();
							String submit = "";
							if (getMessage.length() >= 38) {
								Matcher matcher = pattern.matcher(getMessage);
								if (matcher.find()) {
									submit = matcher.group();
									String low25 = submit.substring(14, 16);
									String high25 = submit.substring(16, 18);
									float pm25 = calculatePM(low25, high25);
									String low10 = submit.substring(22, 24);
									String high10 = submit.substring(24, 26);
									float pm10 = calculatePM(low10, high10);
									matcher.reset();
									submit = pm25 + ";" + pm10;
									submitBuilder = new StringBuilder();
									Message msg = new Message();

									msg.what = BLUETOOTH_VALUE_SUC;
									if (Math.abs((pm25 - lastPM25)) < VALUE_UPANDDOWN
											|| Math.abs((pm10 - lastPM10)) < VALUE_UPANDDOWN) {
										Log.e(TAG, "sended");
										msg.obj = submit;

									} else {
										Log.e(TAG, "unSend");
										msg.obj = "";
									}
									bluetoothHandlerHandler.sendMessage(msg);
									lastPM25 = pm25;
									lastPM10 = pm10;
								} else {
									Log.e("not match", "not match");
								}
							}

						}
					} catch (IOException e) {
						try {
							mmInStream.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						// 数据不传输
						bluetoothHandlerHandler.sendEmptyMessage(BLUETOOTH_DISCONNECT);
						break;
					}
				} else {
					// 数据不传输
					bluetoothHandlerHandler.sendEmptyMessage(BLUETOOTH_DISCONNECT);
					break;
				}

			}
		}
	}

	/**
	 * 转换十六进制
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			String hexString = Integer.toHexString(bytes[i] & 0xFF);
			if (hexString.length() == 1) {
				hexString = '0' + hexString;
			}
			result += hexString.toUpperCase();
		}
		return result;
	}

	/**
	 * 计算pm值
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	private float calculatePM(String low, String high) {
		Integer lowInteger = Integer.valueOf(low, 16);
		Integer highInteger = Integer.valueOf(high, 16);
		float highFloat = highInteger.floatValue();
		float lowFloat = lowInteger.floatValue();
		return (highFloat * 256f + lowFloat) / 10f;
	}

	/**
	 * 空气质量检测
	 * 
	 * @author mx
	 *
	 */
	public class AirQualityBinder extends Binder {
		public AirQualityService getService() {
			return AirQualityService.this;
		}
	}

	/**
	 * 获得采集的空气质量
	 * 
	 * @return
	 */
	public AirqualityForCollection getAirQuality() {
		if (null != airqualityCollectionString && !airqualityCollectionString.isEmpty()
				&& airqualityCollectionString.contains(";")) {
			String[] pms = airqualityCollectionString.split(";");
			if (null != pms && pms.length == 2)
				return new AirqualityForCollection(pms[0], pms[1]);
			else
				return null;
		} else {
			return null;
		}

	}

	private class BluetoothBroadcastReceiver extends BroadcastReceiver {
		private String action;

		@Override
		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				// 蓝牙连接断开
				bluetoothHandlerHandler.sendEmptyMessage(BLUETOOTH_DISCONNECT);
			}
		}

	}

}
