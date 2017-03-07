package com.esint.demolition.airqualitycollection.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;

public class MultDialog extends BaseDialog implements OnClickListener {
	private Context mContext;
	private Button posButton, negButton;
	private TextView msgTextView;
	private MultButtonClickListener mListener;
	private String message = null, posMessage = null, negMessage = null;

	public MultDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	public interface MultButtonClickListener {
		public void posClick(Dialog dialog);

		public void negClick(Dialog dialog);
	}

	public void setMultClickListener(MultButtonClickListener listener) {
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_mult);
		initView();
		initEvent();
	}

	private void initView() {
		initDialogSize();
		msgTextView = (TextView) findViewById(R.id.tv_multDialog_msg);
		posButton = (Button) findViewById(R.id.bt_multDialog_pos);
		negButton = (Button) findViewById(R.id.bt_multDialog_neg);
		if (null != message) {
			msgTextView.setText(message);
		}
		if (null != posMessage) {
			posButton.setText(posMessage);
		}

		if (null != negMessage) {
			negButton.setText(negMessage);
		}
	}

	public void initEvent() {
		super.initEvent();
		posButton.setOnClickListener(this);
		negButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_multDialog_pos:
			// 确定
			if (null != mListener) {
				mListener.posClick(this);
			}
			break;

		case R.id.bt_multDialog_neg:
			// 取消
			if (null != mListener)
				mListener.negClick(this);
			break;
		}
	}

	public void setMessage(String msg) {
		message = msg;
	}

	public void setPosMessage(String msgPos) {
		posMessage = msgPos;
	}

	public void setNegMessage(String msgNeg) {
		negMessage = msgNeg;
	}

}
