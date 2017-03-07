package com.esint.demolition.airqualitycollection.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.esint.demolition.airqualitycollection.R;
import com.esint.demolition.airqualitycollection.application.CollectionApplication;
import com.esint.demolition.airqualitycollection.dialog.LoadingDialog;
import com.esint.demolition.airqualitycollection.dialog.SingleDialog;

import org.w3c.dom.Text;

public class BaseActivity extends Activity implements View.OnClickListener {
    protected Context mContext;
    /**
     * 上下文
     */
    protected SharedPreferences mSharedPreferences;
    /**
     *
     */
    protected CollectionApplication mApplication;
    /**
     * 标题
     */
    protected TextView titleTextView;
    /**
     * 功能按键
     */
    protected TextView functionTextView;
    /**
     * 返回按键
     */
    protected  TextView backTextView;

    /**
     * 加载图标
     */
    protected ProgressBar loadingProgressBar;
    /**
     * 加载文字
     */
    protected TextView loadingMessageTextView;

    /**
     * 加载loading
     */
    protected LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

    }

    protected void initData() {
        mSharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_APPEND);
        mApplication = (CollectionApplication) getApplication();
    }

    protected void initEvent() {

    }

    /**
     * 初始化标题
     */
    protected void initTitle() {
        titleTextView = (TextView) findViewById(R.id.tv_title_title);
        functionTextView = (TextView) findViewById(R.id.tv_title_function);
    }

    protected void initSubTitle(){
        titleTextView = (TextView) findViewById(R.id.tv_subtitle_title);
        functionTextView = (TextView) findViewById(R.id.tv_subtitle_function);
        backTextView = (TextView)findViewById(R.id.tv_subtitle_back);
        backTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_subtitle_back:
                finish();
                break;
        }
    }

    /**
     * 显示dialog
     */
    protected void showLoadingDialog(Context context) {
        mLoadingDialog = new LoadingDialog(context);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.show();
    }

    /**
     * 取消dialog
     */
    protected void cancelLoadingDialog() {
        if (null != mLoadingDialog && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    /**
     * 提示dialog
     * @param context
     * @param message
     */
    protected void showAlertDialog(Context context, String message){
        SingleDialog dialog = new SingleDialog(context);
        dialog.setMessage(message);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setSingleListener(new SingleDialog.SingleButtonClickListener() {
            @Override
            public void clickPos(Dialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 显示正在加载
     *
     * @param contentView
     * @param loadingView
     */
    protected void showLoadingView(View contentView, View loadingView) {
        loadingProgressBar = (ProgressBar) loadingView.findViewById(R.id.pb_loaidng_icon);
        loadingMessageTextView = (TextView) loadingView.findViewById(R.id.tv_loading_message);
        contentView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
    }

    /**
     * 加载成功
     *
     * @param contentView
     * @param loadingView
     */
    protected void loadingSuc(View contentView, View loadingView) {
        contentView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
    }

    protected void loadingFail(View contentView, View loadingView, String message) {
        contentView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        loadingProgressBar = (ProgressBar) loadingView.findViewById(R.id.pb_loaidng_icon);
        loadingMessageTextView = (TextView) loadingView.findViewById(R.id.tv_loading_message);
        loadingProgressBar.setVisibility(View.GONE);
        if (null != message && !message.isEmpty())
            loadingMessageTextView.setText(message);
    }
}
