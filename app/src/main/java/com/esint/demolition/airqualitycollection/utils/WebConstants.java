package com.esint.demolition.airqualitycollection.utils;

/**
 * Created by Administrator on 2017-02-28.
 * 网络请求
 */

public class WebConstants {
    /**
     * 访问基本地址
     */
    public static final String URL_BASE = "http://192.168.3.115:8081/";
    /**
     * 返回成功
     */
    public static final int RESULT_SUC = 1;

    //获得有监控的拆迁点列表
    /**
     * 获得有监控的拆迁点列表
     */
    public static final String URL_GETPROJECTS = "interface/GetCameraDemolition";
    /**
     * 获得有监控的拆迁点列表
     */
    public static final int WEBFLAG_GETPROJECTS = 0x2001;
    //实时上传pm值
    /**
     * 实时上传pm值
     */
    public static final String URL_UPLOAD_AIRQUALITY = "interface/UpdatePM";
    /**
     * 拆迁工地id
     */
    public static final String PARAMS_UPLOADAIRQUALITY_PROJECTID = "projectid";
    /**
     * 实时上传pm10
     */
    public static final String PARAMS_UPLOADAIRQUALITY_PM10 = "pm10";
    /**
     * 实时上传pm2.5
     */
    public static final String PARAMS_UPLOADAIRQUALITY_PM25 = "pm25";
    /**
     * 实时上传pm值
     */
    public static final int WEBFLAG_UPLOAD_AIRQUALITY = 0x2002;


}
