package com.noid.powermeter.ui.textdisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;

import com.noid.powermeter.MainActivity;
import com.noid.powermeter.Model.BLEService;

import com.noid.powermeter.databinding.FragmentTextdisplayBinding;

public class TextdisplayFragment extends Fragment {

    private TextdisplayViewModel textdisplayViewModel;
    private FragmentTextdisplayBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        textdisplayViewModel =
                new ViewModelProvider(this).get(TextdisplayViewModel.class);

        binding = FragmentTextdisplayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        MainActivity main = (MainActivity) getActivity();
        main.textVoltage = binding.textVoltage;
        main.textCurrent = binding.textCurrent;
        main.textPower = binding.textPower;
        main.textFactor = binding.textFactor;
        main.textCumulative = binding.textCumulative;
        main.textAc = binding.textHeaderAc;
        main.textDc = binding.textHeaderDc;
        main.textUsb = binding.textHeaderUSB;
        main.textView2 = binding.textView2;
        main.textName = binding.textHeaderName;
        main.text5 = binding.text5;
        main.text6 = binding.text6;
        main.text7 = binding.text7;
        main.text8 = binding.text8;
        main.text9 = binding.text9;
        main.text11 = binding.text11;
        main.textAC = binding.textAC;
        main.textBill = binding.textBill;
        main.textcarbon = binding.textcarbon;
        main.textInternal = binding.textInternal;
        main.textElectricity = binding.textElectricity;
        main.textBLV = binding.textBLV;
        main.button = binding.button;
        main.button2 = binding.button2;
        main.button3 = binding.button3;
        main.layoutBL = binding.layoutBL;
        if (BLEService.mBluetoothGatt != null)
            main.textName.setText(BLEService.mBluetoothGatt.getDevice().getName());
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}