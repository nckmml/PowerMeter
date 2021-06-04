package com.noid.powermeter.ui.textdisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.noid.powermeter.MainActivity;
import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentTextdisplayBinding;

import java.util.HashMap;

public class TextdisplayFragment extends Fragment {

    public TextView textVoltage;
    public TextView textCurrent;
    public TextView textPower;
    public TextView textFactor;
    public TextView textCumulative;
    public TextView textAc;
    public TextView textDc;
    public TextView textUsb;
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
    value receiver = new value();
    private TextdisplayViewModel textdisplayViewModel;
    private FragmentTextdisplayBinding binding;
    private int adu;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        textdisplayViewModel =
                new ViewModelProvider(this).get(TextdisplayViewModel.class);

        binding = FragmentTextdisplayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ALL_VALUE);
        intentFilter.addAction(BLEService.CONTENT_DEVICE);
        getActivity().registerReceiver(receiver, intentFilter);
        MainActivity main = (MainActivity) getActivity();
        textVoltage = binding.textVoltage;
        textCurrent = binding.textCurrent;
        textPower = binding.textPower;
        textFactor = binding.textFactor;
        textCumulative = binding.textCumulative;
        textAc = binding.textHeaderAc;
        textDc = binding.textHeaderDc;
        textUsb = binding.textHeaderUSB;
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
        if (BLEService.mBluetoothGatt != null)
            textName.setText(BLEService.mBluetoothGatt.getDevice().getName());
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
        binding = null;
    }

    private void updateText(HashMap<String, String> datamap) {
        Log.d("updateText()", "Received data to update");
        this.adu = Integer.parseInt(datamap.get("device"));
        MainActivity main = (MainActivity) getActivity();
        main.adu = this.adu;
        switch (this.adu) {
            case 1:
                Log.d("updateText()", "updating 1");
                this.button.setVisibility(View.GONE);
                this.button2.setVisibility(View.GONE);
                this.layoutBL.setVisibility(View.VISIBLE);
                this.textAc.setBackgroundResource(0);
                this.textDc.setBackgroundResource(R.drawable.text_bg);
                this.textUsb.setBackgroundResource(R.drawable.text_bg);
                this.text5.setText(getText(R.string.Power_Factor));
                this.text7.setText(getText(R.string.carbon_dioxide));
                this.text8.setText(getText(R.string.Cumulative_electricity_bill));
                this.text9.setText(getText(R.string.AC_frequency));
                this.text11.setText(getText(R.string.Electricity_price_setting));
                this.textVoltage.setText(datamap.get("voltage"));
                this.textCurrent.setText(datamap.get("current"));
                this.textPower.setText(datamap.get("power"));
                this.textFactor.setText(datamap.get("powerfactor"));
                this.textCumulative.setText(datamap.get("electricity"));
                this.textcarbon.setText(datamap.get("co2"));
                this.textBill.setText(datamap.get("echarges"));
                this.textAC.setText(datamap.get("acfreq"));
                this.textInternal.setText(datamap.get("temperature"));
                this.textElectricity.setText(datamap.get("eprice"));
                this.textBLV.setText(datamap.get("backlight"));
                break;
            case 2:
                Log.d("updateText()", "updating 2");
                this.button.setVisibility(View.VISIBLE);
                this.button2.setVisibility(View.VISIBLE);
                this.layoutBL.setVisibility(View.VISIBLE);
                this.textAc.setBackgroundResource(R.drawable.text_bg);
                this.textDc.setBackgroundResource(0);
                this.textUsb.setBackgroundResource(R.drawable.text_bg);
                this.text5.setText(getText(R.string.Cumulative_capacity));
                this.text7.setText(getText(R.string.carbon_dioxide));
                this.text8.setText(getText(R.string.Cumulative_electricity_bill));
                this.text9.setText(getText(R.string.time_record));
                this.text11.setText(getText(R.string.Electricity_price_setting));
                this.textVoltage.setText(datamap.get("voltage"));
                this.textCurrent.setText(datamap.get("current"));
                this.textPower.setText(datamap.get("power"));
                this.textFactor.setText(datamap.get("capacity"));
                this.textCumulative.setText(datamap.get("electricity"));
                this.textcarbon.setText(datamap.get("co2"));
                this.textBill.setText(datamap.get("echarges"));
                this.textAC.setText(datamap.get("time"));
                this.textInternal.setText(datamap.get("temperature"));
                this.textElectricity.setText(datamap.get("eprice"));
                this.textBLV.setText(datamap.get("backlight"));
                break;
            case 3:
                Log.d("updateText()", "updating 3");
                this.button.setVisibility(View.VISIBLE);
                this.button2.setVisibility(View.VISIBLE);
                this.layoutBL.setVisibility(View.GONE);
                this.textAc.setBackgroundResource(R.drawable.text_bg);
                this.textDc.setBackgroundResource(R.drawable.text_bg);
                this.textUsb.setBackgroundResource(0);
                this.text5.setText(getText(R.string.Cumulative_capacity));
                this.text7.setText("USB_D + :");
                this.text8.setText("USB_D - :");
                this.text9.setText(getText(R.string.time_record));
                this.text11.setText(getText(R.string.Backlight));
                this.textVoltage.setText(datamap.get("voltage"));
                this.textCurrent.setText(datamap.get("current"));
                this.textPower.setText(datamap.get("power"));
                this.textFactor.setText(datamap.get("capacity"));
                this.textCumulative.setText(datamap.get("electricity"));
                this.textcarbon.setText(datamap.get("dplus"));
                this.textBill.setText(datamap.get("dminus"));
                this.textAC.setText(datamap.get("time"));
                this.textInternal.setText(datamap.get("temperature"));
                this.textElectricity.setText(datamap.get("backlight"));
                break;
        }
    }

    public class value extends BroadcastReceiver {
        public value() {
        }

        public void onReceive(Context context, Intent intent) {
            char c = 65535;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode != -678816493) {
                if (hashCode == 513534204 && action.equals(BLEService.CONTENT_DEVICE)) {
                    c = 1;
                }
            } else if (action.equals(BLEService.ALL_VALUE)) {
                c = 0;
            }
            switch (c) {
                case 0:
                    HashMap<String, String> datamap = (HashMap<String, String>) intent.getSerializableExtra(BLEService.ALL_VALUE);
                    if (datamap.size() > 0)
                        updateText(datamap);
                    return;
                case 1:
                    if (intent.getExtras().getBoolean(BLEService.CONTENT_DEVICE)) {
                        textName.setText(BLEService.mBluetoothGatt.getDevice().getName());
                        return;
                    }
                    textName.setText("");
                    return;
                default:
                    return;
            }
        }
    }
}