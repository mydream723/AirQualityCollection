package com.esint.demolition.airqualitycollection.bean;

import java.util.List;

/**
 * 监控状态返回值
 * 
 * @author mx
 *
 */
public class JsonSureillance extends JsonResult {

	private List<SurveillanceInfo> content;

	public List<SurveillanceInfo> getContent() {
		return content;
	}

	public void setContent(List<SurveillanceInfo> content) {
		this.content = content;
	}

}
