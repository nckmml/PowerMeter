package com.noid.powermeter.ui.textdisplay;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TextdisplayViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TextdisplayViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Text fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}