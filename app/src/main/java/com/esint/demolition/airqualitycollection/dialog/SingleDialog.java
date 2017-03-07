package com.esint.demolition.airqualitycollection.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;

public class SingleDialog extends BaseDialog implements OnClickListener {
	private Context mContext;
	private Button posButton;
	private TextView msgTextView;
	private SingleButtonClickListener mListener;
	private String message = null, posMessage = null;

	public SingleDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_single);
		initView();
		initEvent();
	}

	private void initView() {
		initDialogSize();
		setCanceledOnTouchOutside(false);
		posButton = (Button) findViewById(R.id.bt_singleDialog_pos);
		msgTextView = (TextView) findViewById(R.id.tv_singleDialog_msg);
		if (message != null) {
			msgTextView.setText(message);
		}
		if (posMessage != null) {
			posButton.setText(posMessage);
		}
	}

	public void initEvent() {
		super.initEvent();
		posButton.setOnClickListener(this);
	}

	public interface SingleButtonClickListener {
		public void clickPos(Dialog dialog);
	}

	public void setSingleListener(SingleButtonClickListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_singleDialog_pos:
			if (null != mListener) {
				mListener.clickPos(this);
			}
			break;
		}
	}

	/**
	 * 设置显示信息
	 * 
	 * @param msg
	 */
	public void setMessage(String msg) {
		message = msg;
	}

	/**
	 * 设置按键信息
	 * 
	 * @param pos
	 */
	public void setPosButtonText(String pos) {
		posMessage = pos;
	}

}
