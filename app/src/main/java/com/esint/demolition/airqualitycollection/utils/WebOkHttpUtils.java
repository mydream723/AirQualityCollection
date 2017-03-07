package com.esint.demolition.airqualitycollection.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * OkHttp工具封装
 * 
 * @author mx
 *
 */
public class WebOkHttpUtils {
	private static final String TAG = "WebOkHttpUtils";
	/**
	 * 连接超时时间
	 */
	private final static int TIMEOUT_CONNECT = 30;
	/**
	 * 读超时时间
	 */
	private final static int TIMEOUT_READ = 60;
	/**
	 * 写超时时间
	 */
	private final static int TIMEOUT_WRITE = 60;

	public static final int WEBFLAG_ERROR = 0x9000;
	private static WebOkHttpUtils mWebOkHttpUtils;
	private OkHttpClient mOkHttpClient;
	/**
	 * 上传进度
	 */
	public static final int WEBFLAG_UPLOAD_PROGRESS = 0x9001;
	/**
	 * png上传文件
	 */
	public static final String TYPE_UPLOAD_PNG = "image/png";
	/**
	 * jpg上传文件
	 */
	public static final String TYPE_UPLOAD_JPG = "image/jpeg";

	private WebOkHttpUtils() {
		mOkHttpClient = new OkHttpClient.Builder().readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
				.writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS).connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
				.build();

	}

	public static WebOkHttpUtils getInstance() {
		synchronized (WebOkHttpUtils.class) {
			if (null == mWebOkHttpUtils) {
				mWebOkHttpUtils = new WebOkHttpUtils();
			}
		}
		return mWebOkHttpUtils;
	}

	/**
	 * get网络请求
	 * 
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param handler
	 *            请求返回线程
	 * @param requestCode
	 *            网络请求码
	 * @param requestErrorCode
	 *            请求错误码
	 */
	public void getRequest(String url, Map<String, String> params, final Handler handler, final int requestCode) {
		Log.d(TAG, "url:" + url);
		String requestUrl = createGetUrl(url, params);
		Builder builder = new Request.Builder();
		builder.url(requestUrl);
		Request request = builder.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				// 请求正确返回数据
				String resp = response.body().string();
				Message msg = new Message();
				msg.what = requestCode;
				msg.obj = resp;
				handler.sendMessage(msg);
				Log.d(TAG, resp);
			}

			@Override
			public void onFailure(Call call, IOException exception) {
				if (call.isExecuted()) {
					call.cancel();
				}
				String error = (exception != null && exception.getMessage() != null ? exception.getMessage()
						: "web request have a unkown error");
				Message msg = new Message();
				msg.what = WEBFLAG_ERROR;
				msg.obj = error;
				handler.sendMessage(msg);
				Log.d(TAG, error);
			}
		});
	}

	/**
	 * get网络请求
	 * 
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param handler
	 *            请求返回线程
	 * @param requestCode
	 *            网络请求码
	 * @param requestErrorCode
	 *            请求错误码
	 */
	public void getRequest(String url, Map<String, String> params, final RequestCallBack callback,
			final int requestCode) {
		String requestUrl = createGetUrl(url, params);
		Builder builder = new Request.Builder();
		builder.url(requestUrl);
		Request request = builder.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				// 请求正确返回数据
				String resp = response.body().string();
				Message msg = new Message();
				msg.what = requestCode;
				msg.obj = resp;
				callback.requestSuccess(msg);
				Log.d(TAG, resp);
			}

			@Override
			public void onFailure(Call call, IOException exception) {
				if (call.isExecuted()) {
					call.cancel();
				}
				String error = (exception != null && exception.getMessage() != null ? exception.getMessage()
						: "web request have a unkown error");
				Message msg = new Message();
				msg.what = WEBFLAG_ERROR;
				msg.obj = error;
				callback.requesetFail(msg);
				Log.d(TAG, error);
			}
		});
	}

	/**
	 * 构造get请求url 参数
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	private String createGetUrl(String url, Map<String, String> params) {
		int paramsPos = 0;
		StringBuilder sBuilder = null;
		if (null != params && params.size() > 0) {
			sBuilder = new StringBuilder();
			Set<String> keysSet = params.keySet();
			Iterator<String> keyIterator = keysSet.iterator();
			while (keyIterator.hasNext()) {
				try {
					if (paramsPos != 0) {
						sBuilder.append("&");
					}
					String keyName = keyIterator.next();
					String keyValue = params.get(keyName);
					sBuilder.append(String.format("%s=%s", keyName, URLEncoder.encode(keyValue, "utf-8")));
					Log.d(TAG, keyName + ":" + keyValue);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				paramsPos++;
			}
		}
		String requestUrl = url;
		if (null != sBuilder)
			requestUrl = String.format("%s?%s", url, sBuilder.toString());
		return requestUrl;
	}

	/**
	 * post网络请求
	 * 
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param handler
	 *            请求返回线程
	 * @param requestCode
	 *            网络请求码
	 */
	public void postRequest(String url, Map<String, String> params, final Handler handler, final int requestCode) {
		okhttp3.FormBody.Builder paramsBuilder = setParamsToRequest(params);
		Builder builder = new Request.Builder();
		builder.url(url);
		if (null != paramsBuilder) {
			builder.post(paramsBuilder.build());
		}
		Request request = builder.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				// 请求正确返回数据
				Message msg = new Message();
				msg.what = requestCode;
				msg.obj = response.body().string();
				handler.sendMessage(msg);

				Log.d(TAG, "result: " + null != msg.obj ? msg.obj.toString() : "web error");
			}

			@Override
			public void onFailure(Call call, IOException exception) {
				if (call.isExecuted()) {
					call.cancel();
				}
				String error = exception != null ? exception.getMessage() : "web request have a unkown error";
				Message msg = new Message();
				msg.what = WEBFLAG_ERROR;
				msg.obj = error;
				handler.sendMessage(msg);
				Log.d(TAG, "error:" + error);
			}
		});
	}

	/**
	 * post网络请求
	 * 
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param handler
	 *            请求返回线程
	 * @param requestCode
	 *            网络请求码
	 * @param requestErrorCode
	 *            请求错误码
	 */
	public void postRequest(String url, Map<String, String> params, final Handler handler, final int requestCode,
			final int requestErrorCode) {
		okhttp3.FormBody.Builder paramsBuilder = setParamsToRequest(params);
		Builder builder = new Request.Builder();
		builder.url(url);
		if (null != paramsBuilder) {
			builder.post(paramsBuilder.build());
		}
		Request request = builder.build();
		mOkHttpClient.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				// 请求正确返回数据
				String resp = response.body().string();
				Message msg = new Message();
				msg.what = requestCode;
				msg.obj = resp;
				handler.sendMessage(msg);
				Log.d(TAG, resp);
			}

			@Override
			public void onFailure(Call call, IOException exception) {
				if (call.isExecuted()) {
					call.cancel();
				}
				String error = (exception != null && exception.getMessage() != null ? exception.getMessage()
						: "web request have a unkown error");
				Message msg = new Message();
				msg.what = requestErrorCode;
				msg.obj = error;
				handler.sendMessage(msg);
				Log.d(TAG, error);
			}
		});
	}

	/**
	 * 将请求参数赋值到request参数中
	 * 
	 * @param params
	 * @return 如果没有参数返回null
	 */
	private okhttp3.FormBody.Builder setParamsToRequest(Map<String, String> params) {
		okhttp3.FormBody.Builder builder = null;
		if (null != params && params.size() > 0) {
			builder = new FormBody.Builder();
			Set<String> keysSet = params.keySet();
			Iterator<String> keyIterator = keysSet.iterator();
			while (keyIterator.hasNext()) {
				String keyName = keyIterator.next();
				builder.add(keyName, params.get(keyName));
				Log.d(TAG, keyName + ":" + params.get(keyName));
			}
		}
		return builder;
	}

	/**
	 * 上传文件(不带参数 )
	 * 
	 * @param actionUrl
	 *            接口地址
	 * @param filePath
	 *            本地文件地址
	 * @param handler
	 *            处理线程
	 * @param requestCode
	 *            请求编码
	 */
	public void upLoadFile(String actionUrl, String filePath, String fileName, final Handler handler,
			final int requestCode) {
		// 补全请求地址
		String requestUrl = actionUrl;
		// 创建File
		File file = new File(filePath);
		// 创建RequestBody
		RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
		MultipartBody.Builder builder = new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);
		// builder.addPart(
		// Headers.of("Content-Disposition", "form-data;
		// name=\"mFile\";filename=\"" + file.getName() + "\""),
		// fileBody);
		builder.addPart(Headers.of("Content-Disposition", "form-data; filename=\"" + fileName + "\""), fileBody);
		RequestBody requestBody = builder.build();
		// 创建Request
		final Request request = new Request.Builder().url(requestUrl).post(requestBody).build();
		final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
		call.enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException exception) {
				String error = exception != null ? exception.getMessage() : "web request have a unkown error";
				Message msg = new Message();
				msg.what = WEBFLAG_ERROR;
				msg.obj = error;
				handler.sendMessage(msg);
				Log.d(TAG, error == null ? "web request have a unkown error" : error);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (response.isSuccessful()) {
					Message msg = new Message();
					msg.what = requestCode;
					msg.obj = response.body().string();
					handler.sendMessage(msg);
					Log.d(TAG, msg.obj.toString() == null ? "responese null" : msg.obj.toString());
				} else {
					Message msg = new Message();
					msg.what = WEBFLAG_ERROR;
					msg.obj = "upload unkown error";
					handler.sendMessage(msg);
					Log.d(TAG, msg.obj.toString());
				}
			}
		});
	}

	/**
	 * 上传文件
	 * 
	 * @param requestUrl
	 *            接口地址
	 * @param paramsMap
	 *            参数
	 * @param callBack
	 *            回调
	 * @param <T>
	 */
	public void upLoadFile(String requestUrl, Map<String, Object> params, final Handler handler,
			final int requestCode) {
		try {
			MultipartBody.Builder builder = new MultipartBody.Builder();
			// 设置类型
			builder.setType(MultipartBody.FORM);
			// 追加参数
			for (String key : params.keySet()) {
				Object object = params.get(key);
				if (!(object instanceof File)) {
					builder.addFormDataPart(key, object.toString());
				} else {
					File file = (File) object;
					String ext = file.getPath().substring(file.getPath().lastIndexOf("."), file.getPath().length());
					String mediaType;
					if (ext.toLowerCase().equals("png")) {
						mediaType = TYPE_UPLOAD_PNG;
					} else {
						mediaType = TYPE_UPLOAD_JPG;
					}
					RequestBody body = RequestBody.create(MediaType.parse(mediaType), file);
					builder.addFormDataPart(key, file.getName(), body);
				}
			}
			// 创建RequestBody
			RequestBody requsetbody = builder.build();
			// 创建Request
			final Request request = new Request.Builder().url(requestUrl).post(requsetbody).build();
			// 单独设置参数 比如读取超时时间
			final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
			call.enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException exception) {
					String error = exception != null ? exception.getMessage() : "web request have a unkown error";
					Message msg = new Message();
					msg.what = WEBFLAG_ERROR;
					msg.obj = error;
					handler.sendMessage(msg);
					Log.d(TAG, msg.obj.toString());
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (response.isSuccessful()) {
						Message msg = new Message();
						msg.what = requestCode;
						msg.obj = response.body().string();
						handler.sendMessage(msg);
						Log.d(TAG, msg.obj.toString());
					} else {
						Message msg = new Message();
						msg.what = WEBFLAG_ERROR;
						msg.obj = "upload unkown error";
						handler.sendMessage(msg);
						Log.d(TAG, msg.obj.toString());
					}
				}
			});
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * 上传文件
	 * 
	 * @param actionUrl
	 *            接口地址
	 * @param paramsMap
	 *            参数
	 * @param callBack
	 *            回调
	 * @param <T>
	 */
	public <T> void upLoadFileWithProgress(String actionUrl, Map<String, Object> paramsMap, final Handler handler,
			final int requestCode) {
		try {
			// 补全请求地址
			String requestUrl = actionUrl;
			MultipartBody.Builder builder = new MultipartBody.Builder();
			// 设置类型
			builder.setType(MultipartBody.FORM);
			// 追加参数
			for (String key : paramsMap.keySet()) {
				Object object = paramsMap.get(key);
				if (!(object instanceof File)) {
					builder.addFormDataPart(key, object.toString());
				} else {
					File file = (File) object;
					builder.addFormDataPart(key, file.getName(),
							createProgressRequestBody(MediaType.parse("application/octet-stream"), file, handler));
				}
			}
			// 创建RequestBody
			RequestBody body = builder.build();
			// 创建Request
			final Request request = new Request.Builder().url(requestUrl).post(body).build();
			final Call call = mOkHttpClient.newBuilder().writeTimeout(50, TimeUnit.SECONDS).build().newCall(request);
			call.enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException exception) {
					Log.e(TAG, exception.toString());
					String error = exception != null ? exception.getMessage() : "web request have a unkown error";
					Message msg = new Message();
					msg.what = WEBFLAG_ERROR;
					msg.obj = error;
					handler.sendMessage(msg);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					if (response.isSuccessful()) {
						Message msg = new Message();
						msg.what = requestCode;
						msg.obj = response.body().string();
						handler.sendMessage(msg);
					} else {
						Message msg = new Message();
						msg.what = WEBFLAG_ERROR;
						msg.obj = "upload unkown error";
						handler.sendMessage(msg);
					}
				}
			});
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * 创建带进度的RequestBody
	 * 
	 * @param contentType
	 *            MediaType
	 * @param file
	 *            准备上传的文件
	 * @param callBack
	 *            回调
	 * @param <T>
	 * @return
	 */
	public RequestBody createProgressRequestBody(final MediaType contentType, final File file, final Handler handler) {
		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return contentType;
			}

			@Override
			public long contentLength() {
				return file.length();
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				Source source;
				try {
					source = Okio.source(file);
					Buffer buf = new Buffer();
					long remaining = contentLength();
					long current = 0;
					for (long readCount; (readCount = source.read(buf, 2048)) != -1;) {
						sink.write(buf, readCount);
						current += readCount;
						Message msg = new Message();
						msg.what = WEBFLAG_UPLOAD_PROGRESS;
						msg.arg1 = (int) remaining;
						msg.arg2 = (int) current;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * 网络请求回调
	 * 
	 * @author mx
	 *
	 */
	public interface RequestCallBack {
		/**
		 * 访问成功
		 */
		public void requestSuccess(Message msg);

		/**
		 * 访问失败
		 */
		public void requesetFail(Message msg);
	}

}
