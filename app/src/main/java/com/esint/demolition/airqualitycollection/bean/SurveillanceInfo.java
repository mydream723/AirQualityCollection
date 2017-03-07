package com.esint.demolition.airqualitycollection.bean;

/**
 * 监控信息
 * 
 * @author mx
 *
 */
public class SurveillanceInfo {
	private int id;
	private int areaid;
	/**
	 * 区县名称
	 */
	private String areaname;
	/**
	 * 拆房工地名称
	 */
	private String name;
	private int prjtype;
	private String prjtypename;
	/**
	 * 1、有监控；0、没有监控
	 */
	private int camera;
	/**
	 * 1、有无人机。2、无无人机
	 */
	private int fly;
	private String pm10;
	private String pm25;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAreaid() {
		return areaid;
	}

	public void setAreaid(int areaid) {
		this.areaid = areaid;
	}

	public String getAreaname() {
		return areaname;
	}

	public void setAreaname(String areaname) {
		this.areaname = areaname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrjtype() {
		return prjtype;
	}

	public void setPrjtype(int prjtype) {
		this.prjtype = prjtype;
	}

	public String getPrjtypename() {
		return prjtypename;
	}

	public void setPrjtypename(String prjtypename) {
		this.prjtypename = prjtypename;
	}

	public int getCamera() {
		return camera;
	}

	public void setCamera(int camera) {
		this.camera = camera;
	}

	public int getFly() {
		return fly;
	}

	public void setFly(int fly) {
		this.fly = fly;
	}

	public String getPm10() {
		return pm10;
	}

	public void setPm10(String pm10) {
		this.pm10 = pm10;
	}

	public String getPm25() {
		return pm25;
	}

	public void setPm25(String pm25) {
		this.pm25 = pm25;
	}

}
