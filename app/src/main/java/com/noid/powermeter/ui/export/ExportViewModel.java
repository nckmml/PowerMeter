package com.noid.powermeter.ui.export;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.noid.powermeter.ui.Repository;

import java.util.ArrayList;

public class ExportViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ExportViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is export fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<ArrayList<String>> getData() {
        return Repository.instance().getData();
    }
}