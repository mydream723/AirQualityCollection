package com.esint.demolition.airqualitycollection.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;

/**
 * 加载dialog
 * 
 * @author mx
 *
 */
public class LoadingDialog extends AlertDialog {
	private Context mContext;
	private String message;
	private ProgressBar loadingProgressBar;
	private TextView msgTextView;

	public LoadingDialog(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param msg
	 *            显示内容
	 */
	public LoadingDialog(Context context, String msg) {
		super(context);
		this.mContext = context;
		message = msg;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_loading);
		initView();
		initDialogSize();
	}
	
	private void initData() {

	}

	private void initEvent() {

	}

	private void initView() {
		loadingProgressBar = (ProgressBar) findViewById(R.id.pb_loaidingDialog_icon);
		msgTextView = (TextView) findViewById(R.id.tv_loadingDialog_msg);
		// 显示加载内容
		if (null != message && !message.isEmpty()) {
			msgTextView.setText(message);
		} else {
			msgTextView.setText(mContext.getResources().getString(R.string.loading_message));
		}
	}
	
	/**
	 * 规定dialog大小
	 */
	protected void initDialogSize() {
		Window dialogWindow = getWindow();
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
		WindowManager.LayoutParams params = dialogWindow.getAttributes();
		params.width = (int) (display.getWidth() * 0.85);
		dialogWindow.setAttributes(params);
	}

}
