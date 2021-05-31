package com.noid.powermeter.ui.bluetoothlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BluetoothlistViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BluetoothlistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is bluetooth fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}