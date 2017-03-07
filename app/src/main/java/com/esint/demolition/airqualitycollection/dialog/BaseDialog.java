package com.esint.demolition.airqualitycollection.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class BaseDialog extends AlertDialog {
	protected Context mContext;

	protected BaseDialog(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected void initData() {

	}

	protected void initEvent() {

	}

	/**
	 * 规定dialog大小
	 */
	protected void initDialogSize() {
		Window dialogWindow = getWindow();
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();
		WindowManager.LayoutParams params = dialogWindow.getAttributes();
		params.height = (int) (display.getHeight() * 0.35);
		params.width = (int) (display.getWidth() * 0.85);
		dialogWindow.setAttributes(params);
	}

	/**
	 * 弹出提示
	 * 
	 * @param context
	 * @param msg
	 *            提示内容
	 */
	public void showToast(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}


}
