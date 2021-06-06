package com.noid.powermeter.ui.textdisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentTextdisplayBinding;

import java.util.ArrayList;

public class TextdisplayFragment extends Fragment {

    public TextView textVoltage;
    public TextView textCurrent;
    public TextView textPower;
    public TextView textFactor;
    public TextView textCumulative;
    public Chip chipAc;
    public Chip chipDc;
    public Chip chipUsb;
    public TextView textView2;
    public TextView textName;
    public TextView text5;
    public TextView text6;
    public TextView text7;
    public TextView text8;
    public TextView text9;
    public TextView text11;
    public TextView textAC;
    public TextView textBill;
    public TextView textcarbon;
    public TextView textInternal;
    public TextView textElectricity;
    public TextView textBLV;
    public Button button;
    public Button button2;
    public Button button3;
    public ConstraintLayout layoutBL;
    private FragmentTextdisplayBinding binding;
    private int adu;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TextdisplayViewModel textdisplayViewModel = new ViewModelProvider(this).get(TextdisplayViewModel.class);
        binding = FragmentTextdisplayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        textVoltage = binding.textVoltage;
        textCurrent = binding.textCurrent;
        textPower = binding.textPower;
        textFactor = binding.textFactor;
        textCumulative = binding.textCumulative;
        chipAc = binding.chipHeaderAc;
        chipDc = binding.chipHeaderDc;
        chipUsb = binding.chipHeaderUSB;
        textView2 = binding.textView2;
        textName = binding.textHeaderName;
        text5 = binding.text5;
        text6 = binding.text6;
        text7 = binding.text7;
        text8 = binding.text8;
        text9 = binding.text9;
        text11 = binding.text11;
        textAC = binding.textAC;
        textBill = binding.textBill;
        textcarbon = binding.textcarbon;
        textInternal = binding.textInternal;
        textElectricity = binding.textElectricity;
        textBLV = binding.textBLV;
        button = binding.button;
        button2 = binding.button2;
        button3 = binding.button3;
        layoutBL = binding.layoutBL;
        final Observer<ArrayList<String >> dataObserver = newData -> {
            adu = Integer.parseInt(newData.get(0));
            switch (adu) {
                case 1:
                    button.setVisibility(View.GONE);
                    button2.setVisibility(View.GONE);
                    layoutBL.setVisibility(View.VISIBLE);
                    chipAc.setChecked(true);
                    chipDc.setChecked(false);
                    chipUsb.setChecked(false);
                    text5.setText(getText(R.string.Power_Factor));
                    text7.setText(getText(R.string.carbon_dioxide));
                    text8.setText(getText(R.string.Cumulative_electricity_bill));
                    text9.setText(getText(R.string.AC_frequency));
                    text11.setText(getText(R.string.Electricity_price_setting));
                    textBLV.setText(newData.get(11));
                    break;
                case 2:
                    button.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    layoutBL.setVisibility(View.VISIBLE);
                    chipAc.setChecked(false);
                    chipDc.setChecked(true);
                    chipUsb.setChecked(false);
                    text5.setText(getText(R.string.Cumulative_capacity));
                    text7.setText(getText(R.string.carbon_dioxide));
                    text8.setText(getText(R.string.Cumulative_electricity_bill));
                    text9.setText(getText(R.string.time_record));
                    text11.setText(getText(R.string.Electricity_price_setting));
                    textBLV.setText(newData.get(11));
                    break;
                case 3:
                    button.setVisibility(View.VISIBLE);
                    button2.setVisibility(View.VISIBLE);
                    layoutBL.setVisibility(View.GONE);
                    chipAc.setChecked(false);
                    chipDc.setChecked(false);
                    chipUsb.setChecked(true);
                    text5.setText(getText(R.string.Cumulative_capacity));
                    text7.setText(R.string.DataPlus);
                    text8.setText(R.string.DataMinus);
                    text9.setText(getText(R.string.time_record));
                    text11.setText(getText(R.string.Backlight));
                    break;
            }
            textVoltage.setText(newData.get(1));
            textCurrent.setText(newData.get(2));
            textPower.setText(newData.get(3));
            textFactor.setText(newData.get(4));
            textCumulative.setText(newData.get(5));
            textcarbon.setText(newData.get(6));
            textBill.setText(newData.get(7));
            textAC.setText(newData.get(8));
            textInternal.setText(newData.get(9));
            textElectricity.setText(newData.get(10));
        };
        textdisplayViewModel.getData().observe(getViewLifecycleOwner(), dataObserver);
        if (BLEService.mBluetoothGatt != null)
            textName.setText(BLEService.mBluetoothGatt.getDevice().getName());
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}