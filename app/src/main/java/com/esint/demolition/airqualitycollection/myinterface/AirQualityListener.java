package com.esint.demolition.airqualitycollection.myinterface;

/**
 * 空气质量监听
 * 
 * @author mx
 *
 */
public interface AirQualityListener {
	/**
	 * 蓝牙设备连接断开
	 */
	public void BTDeviceDisconnect();

	/**
	 * 蓝牙设备成功
	 */
	public void BTDeviceConnected();
}
