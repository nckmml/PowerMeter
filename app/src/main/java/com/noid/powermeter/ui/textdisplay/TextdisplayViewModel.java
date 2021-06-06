package com.noid.powermeter.ui.textdisplay;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.noid.powermeter.ui.Repository;

import java.util.ArrayList;

public class TextdisplayViewModel extends ViewModel {

    public LiveData<ArrayList<String>> getData() {
        return Repository.instance().getData();
    }
}