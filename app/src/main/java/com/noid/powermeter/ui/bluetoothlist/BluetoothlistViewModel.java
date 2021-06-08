package com.noid.powermeter.ui.bluetoothlist;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.noid.powermeter.ui.Repository;

import java.util.ArrayList;

public class BluetoothlistViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BluetoothlistViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is bluetooth fragment");
    }

    public LiveData<ArrayList<BluetoothDevice>> getBluetoothDevices() {
        return Repository.instance().getBluetoothDevices();
    }

    public LiveData<String> getText() {
        return mText;
    }
}