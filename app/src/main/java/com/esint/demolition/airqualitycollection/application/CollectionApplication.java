package com.esint.demolition.airqualitycollection.application;

import android.app.Application;

/**
 * Created by Administrator on 2017-02-28.
 */

public class CollectionApplication extends Application {
    private boolean isBluetoothConnected = false;
    /**
     * 选择的工地id
     */
    private int selectedProjectId;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean isBluetoothConnected() {
        return isBluetoothConnected;
    }

    public void setBluetoothConnected(boolean isBluetoothConnected) {
        this.isBluetoothConnected = isBluetoothConnected;
    }

    public int getSelectedProjectId() {
        return selectedProjectId;
    }

    public void setSelectedProjectId(int selectedProjectId) {
        this.selectedProjectId = selectedProjectId;
    }
}
