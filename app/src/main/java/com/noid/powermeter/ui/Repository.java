package com.noid.powermeter.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class Repository {
    private static final Repository INSTANCE = new Repository();

    private final MediatorLiveData<ArrayList<String>> mData = new MediatorLiveData<>();
    private final MediatorLiveData<ArrayList<Entry>> mVoltageData = new MediatorLiveData<>();
    private final MediatorLiveData<ArrayList<Entry>> mCurrentData = new MediatorLiveData<>();
    private final MediatorLiveData<ArrayList<Entry>> mPowerData = new MediatorLiveData<>();
    private final MediatorLiveData<ArrayList<String>> mTimeRecordData = new MediatorLiveData<>();


    private Repository() {}

    public static Repository instance() {
        return INSTANCE;
    }

    public LiveData<ArrayList<String>> getData() {
        return mData;
    }
    public LiveData<ArrayList<Entry>> getVoltageData(){
        return mVoltageData;
    }
    public LiveData<ArrayList<Entry>> getCurrentData(){
        return mCurrentData;
    }
    public LiveData<ArrayList<Entry>> getPowerData(){
        return mPowerData;
    }
    public LiveData<ArrayList<String>> getTimeRecordData(){
        return mTimeRecordData;
    }

    public void addData(LiveData<ArrayList<String>> data) {
        mData.addSource(data, mData::setValue);
    }

    public void addVoltageData(LiveData<ArrayList<Entry>> voltageData) {
        mVoltageData.addSource(voltageData, mVoltageData::setValue);
    }

    public void addCurrentData(LiveData<ArrayList<Entry>> currentData) {
        mCurrentData.addSource(currentData, mCurrentData::setValue);
    }

    public void addPowerData(LiveData<ArrayList<Entry>> powerData) {
        mPowerData.addSource(powerData, mPowerData::setValue);
    }

    public void addTimeRecordData(LiveData<ArrayList<String>> timeRecordData) {
        mPowerData.addSource(timeRecordData, mTimeRecordData::setValue);
    }
}
