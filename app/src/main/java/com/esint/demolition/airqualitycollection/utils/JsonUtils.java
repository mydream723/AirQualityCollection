package com.esint.demolition.airqualitycollection.utils;

import com.esint.demolition.airqualitycollection.bean.JsonResult;
import com.esint.demolition.airqualitycollection.bean.JsonSureillance;
import com.google.gson.Gson;

/**
 * Created by Administrator on 2017-02-28.
 */

public class JsonUtils {
    private static JsonUtils mJsonUtils;

    private JsonUtils(){

    }

    public static JsonUtils getInstance(){
        if(null == mJsonUtils){
            mJsonUtils = new JsonUtils();
        }
        return mJsonUtils;
    }

    /**
     * 解析拆迁点信息
     * @param json
     * @return
     * @throws Exception
     */
    public JsonSureillance getSureillance(String json) throws Exception{
        return new Gson().fromJson(json, JsonSureillance.class);
    }


    public JsonResult getJsonResult(String json) throws Exception{
        return new Gson().fromJson(json, JsonResult.class);
    }


}
