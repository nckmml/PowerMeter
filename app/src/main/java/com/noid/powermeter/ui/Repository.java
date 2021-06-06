package com.noid.powermeter.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;

public class Repository {
    private static final Repository INSTANCE = new Repository();

    private final MediatorLiveData<ArrayList<String>> mData = new MediatorLiveData<>();

    private Repository() {}

    public static Repository instance() {
        return INSTANCE;
    }

    public LiveData<ArrayList<String>> getData() {
        return mData;
    }

    public void addData(LiveData<ArrayList<String>> data) {
        mData.addSource(data, mData::setValue);
    }
}
