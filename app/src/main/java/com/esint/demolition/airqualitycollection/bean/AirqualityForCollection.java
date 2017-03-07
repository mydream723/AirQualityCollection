package com.esint.demolition.airqualitycollection.bean;

/**
 * 采集空气质量数值
 * 
 * @author mx
 *
 */
public class AirqualityForCollection {
	/**
	 * pm2.5
	 */
	private String pm25;
	/**
	 * pm10
	 */
	private String pm10;
	/**
	 * 采集时间
	 */
	private long collectionTime;

	public String getPm25() {
		return pm25;
	}

	public void setPm25(String pm25) {
		this.pm25 = pm25;
	}

	public String getPm10() {
		return pm10;
	}

	public void setPm10(String pm10) {
		this.pm10 = pm10;
	}

	public long getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(long collectionTime) {
		this.collectionTime = collectionTime;
	}

	public AirqualityForCollection(String pm25, String pm10, long collectionTime) {
		super();
		this.pm25 = pm25;
		this.pm10 = pm10;
		this.collectionTime = collectionTime;
	}

	public AirqualityForCollection(String pm25, String pm10) {
		super();
		this.collectionTime = System.currentTimeMillis();
		this.pm25 = pm25;
		this.pm10 = pm10;
	}

	public AirqualityForCollection() {
		super();
	}

}
