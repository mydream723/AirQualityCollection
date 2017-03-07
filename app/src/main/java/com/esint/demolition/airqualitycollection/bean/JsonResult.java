package com.esint.demolition.airqualitycollection.bean;

/**
 * 网络返回值
 * 
 * @author
 *
 */
public class JsonResult {
	/**
	 * 访问状态码
	 */
	protected int code;
	/**
	 * 错误信息
	 */
	protected String error;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public JsonResult() {

	}
}
