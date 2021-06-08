package com.noid.powermeter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.noid.powermeter.ui.Repository;

import java.util.ArrayList;

public class MainActivityViewModel extends ViewModel {

    public LiveData<ArrayList<String>> getData() {
        return Repository.instance().getData();
    }

    public LiveData<ArrayList<Entry>> getVoltageData() {
        return Repository.instance().getVoltageData();
    }
    public LiveData<ArrayList<Entry>> getCurrentData() {
        return Repository.instance().getCurrentData();
    }
    public LiveData<ArrayList<Entry>> getPowerData() {
        return Repository.instance().getPowerData();
    }
    public LiveData<ArrayList<String>> getTimeRecordData() {
        return Repository.instance().getTimeRecordData();
    }
}
