package com.esint.demolition.airqualitycollection.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.esint.demolition.airqualitycollection.R;
import com.esint.demolition.airqualitycollection.adapter.ProjectAdapter;
import com.esint.demolition.airqualitycollection.bean.JsonSureillance;
import com.esint.demolition.airqualitycollection.bean.SurveillanceInfo;
import com.esint.demolition.airqualitycollection.utils.JsonUtils;
import com.esint.demolition.airqualitycollection.utils.WebConstants;
import com.esint.demolition.airqualitycollection.utils.WebOkHttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SelectionActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "SelectionActivity";
    /**
     * 加载样式
     */
    private View loadingView;
    /**
     * 拆迁工地列表
     */
    private ListView projectListView;
    /**
     * 拆迁办适配器
     */
    private ProjectAdapter projectAdapter;
    /**
     * 拆迁点列表
     */
    private List<SurveillanceInfo> projectList;

    /**
     * 选择工地id
     */
    private int projectId;
    /**
     * 选择的第几项
     */
    private int selectedProjectIndex;
    /**
     * 是否正在刷新
     */
    private boolean isRefreshing = false;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WebOkHttpUtils.WEBFLAG_ERROR:
                    //网络错误
                    isRefreshing = false;
                    loadingFail(projectListView, loadingView, getString(R.string.selection_alert_refresh));
                    break;
                case WebConstants.WEBFLAG_GETPROJECTS:
                    //获得拆迁工地列表
                    isRefreshing = false;
                    Log.e(TAG, msg.obj.toString());
                    try {
                        JsonSureillance sureillanceJson = JsonUtils.getInstance().getSureillance(msg.obj.toString());
                        if (null != sureillanceJson) {
                            int code = sureillanceJson.getCode();
                            switch (code) {
                                case WebConstants.RESULT_SUC:
                                    //返回成功
                                    loadingSuc(projectListView, loadingView);
                                    List<SurveillanceInfo> infos = sureillanceJson.getContent();
                                    if (infos.isEmpty()) {
                                        loadingFail(projectListView, loadingView, getString(R.string.selection_alert_nodata));
                                    } else {
                                        projectList.clear();
                                        projectList.addAll(infos);
                                        projectAdapter.notifyDataSetChanged();
                                    }

                                    break;
                                default:
                                    loadingFail(projectListView, loadingView, getString(R.string.selection_alert_refresh));
                                    break;

                            }
                        } else {
                            loadingFail(projectListView, loadingView, getString(R.string.selection_alert_refresh));
                        }

                    } catch (Exception e) {
                        loadingFail(projectListView, loadingView, getString(R.string.selection_alert_refresh));
                    }

                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        initData();
        initView();
        initEvent();
        projectListView.setAdapter(projectAdapter);

        showLoadingView(projectListView, loadingView);
        getProjects();
    }

    @Override
    protected void initData() {
        super.initData();
        mContext = SelectionActivity.this;
        projectList = new ArrayList<SurveillanceInfo>();
        projectAdapter = new ProjectAdapter(mContext, projectList);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        functionTextView.setOnClickListener(this);
        projectListView.setOnItemClickListener(this);
    }

    private void initView() {
        initTitle();
        titleTextView.setText(R.string.selection_title);
        functionTextView.setText(R.string.title_refresh);

        projectListView = (ListView) findViewById(R.id.lv_selectionActivity_projects);
        loadingView = findViewById(R.id.ic_selectionActivity_loading);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.tv_title_function:
                //刷新
                showLoadingView(projectListView, loadingView);
                getProjects();
                break;
        }
    }

    /**
     * 获得拆迁点信息
     */
    private void getProjects() {
        if(!isRefreshing){
            //正在刷新
            isRefreshing = true;
            Map<String, String> params = new HashMap<String, String>();
            WebOkHttpUtils.getInstance().getRequest(WebConstants.URL_BASE + WebConstants.URL_GETPROJECTS, params, mHandler, WebConstants.WEBFLAG_GETPROJECTS);
        }else{
            Toast.makeText(mContext, getString(R.string.selection_isRefreshing), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent btIntent = new Intent(mContext, BTConnectActivity.class);
        mApplication.setSelectedProjectId(projectList.get(position).getId());
        startActivity(btIntent);
    }
}

