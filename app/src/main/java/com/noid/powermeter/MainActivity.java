package com.noid.powermeter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.Model.UUIDs;
import com.noid.powermeter.databinding.ActivityMainBinding;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private byte[] mValue;

    private int adu;

    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

    private List<Float> list0 = new ArrayList();
    private List<Float> list1 = new ArrayList();
    private List<Float> list2 = new ArrayList();
    private List<Float> listData = new ArrayList();
    private List<String> timeList = new ArrayList();
    private ArrayList<ArrayList<String>> recordList;

    TextView textVoltage;
    TextView textCurrent;
    TextView textPower;
    TextView textFactor;
    TextView textCumulative;
    TextView textAc;
    TextView textDc;
    TextView textUsb;
    TextView textView2;
    TextView textName;
    TextView text5;
    TextView text6;
    TextView text7;
    TextView text8;
    TextView text9;
    TextView text11;
    TextView textAC;
    TextView textBill;
    TextView textcarbon;
    TextView textInternal;
    TextView textElectricity;
    TextView textBLV;

    Button button;
    Button button2;
    Button button3;

    LinearLayout layout1;
    LinearLayout layout2;
    LinearLayout layout3;
    LinearLayout layoutBL;

    private static final int WRITE_REQUEST_CODE = 43;

    private int f0 = 0;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_textdisplay, R.id.navigation_bluetoothlist, R.id.navigation_export)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        initBluetooth();
        initView();
        textVoltage = (TextView)findViewById(R.id.textVoltage);
        textCurrent = (TextView)findViewById(R.id.textCurrent);
        textPower = (TextView)findViewById(R.id.textPower);
        textFactor = (TextView)findViewById(R.id.textFactor);
        textCumulative = (TextView)findViewById(R.id.textCumulative);
        textAc = (TextView)findViewById(R.id.text_ac);
        textDc = (TextView)findViewById(R.id.text_dc);
        textUsb = (TextView)findViewById(R.id.text_usb);
        textView2 = (TextView)findViewById(R.id.textView2);
        textName = (TextView)findViewById(R.id.text_name);
        text5 = (TextView)findViewById(R.id.text5);
        text6 = (TextView)findViewById(R.id.text6);
        text7 = (TextView)findViewById(R.id.text7);
        text8 = (TextView)findViewById(R.id.text8);
        text9 = (TextView)findViewById(R.id.text9);
        text11 = (TextView)findViewById(R.id.text11);
        textAC = (TextView)findViewById(R.id.textAC);
        textBill = (TextView)findViewById(R.id.textBill);
        textcarbon = (TextView)findViewById(R.id.textcarbon);
        textInternal = (TextView)findViewById(R.id.textInternal);
        textElectricity = (TextView)findViewById(R.id.textElectricity);
        textBLV = (TextView)findViewById(R.id.textBLV);
        button = (Button)findViewById(R.id.button);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        layout1 = (LinearLayout)findViewById(R.id.layout1);
        layout2 = (LinearLayout)findViewById(R.id.layout2);
        layout3 = (LinearLayout)findViewById(R.id.layout3);
        layoutBL = (LinearLayout)findViewById(R.id.layoutBL);

        Intent intent = new Intent(MainActivity.this, NotificationService.class);
        intent.setAction(NotificationService.ACTION_START_NOTIFICATION_SERVICE);

        startForegroundService(intent);

    }

    private void initView() {
        Log.i("DEBUG", "startService(BLEService)");
        startService(new Intent(this, BLEService.class));
        value receiver = new value();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ALL_VALUE);
        intentFilter.addAction(BLEService.CONTENT_DEVICE);
        registerReceiver(receiver, intentFilter);
    }

        public void initBluetooth() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            System.exit(1);
        }
        if (permissionCheck == PackageManager.PERMISSION_DENIED){
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
            assert bluetoothAdapter != null;
            if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1);
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    System.exit(1);
                }
            });

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WRITE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null
                            && data.getData() != null) {
                        OutputStream outputStream;
                        try {
                            outputStream = getContentResolver().openOutputStream(data.getData());
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                            bw.write("Time,Voltage,Current,Power\n");
                            for (int i=0; i<getRecordData().size(); i++){
                                bw.write(getRecordData().get(i).get(0).toString()+","+getRecordData().get(i).get(1).toString()+","+getRecordData().get(i).get(2).toString()+","+getRecordData().get(i).get(3).toString()+"\n");
                            }
                            bw.flush();
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    private void writeInFile(@NonNull Uri uri, @NonNull String text) {
        OutputStream outputStream;
        try {
            outputStream = getContentResolver().openOutputStream(uri);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            bw.write(text);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void exportExcel(View view) {
        /*
        this.file = new File(getSDPath() + "/Etest");
        makeDir(this.file);
        ExcelUtils.initExcel(this.file.toString() + "/Etest.xls", new String[]{"时间", "电压", "电流", "功率"});
        this.fileName = getSDPath() + "/Etest/Etest.xls";
        ExcelUtils.writeObjListToExcel(getRecordData(), this.fileName, this);
         */
        createFile("text/csv", "export.csv");
    }

    private ArrayList<ArrayList<String>> getRecordData() {
        this.recordList = new ArrayList<>();
        for (int i = 0; i < this.timeList.size(); i++) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(this.timeList.get(i));
            arrayList.add(String.valueOf(this.list0.get(i)));
            arrayList.add(String.valueOf(this.list1.get(i)));
            arrayList.add(String.valueOf(this.list2.get(i)));
            this.recordList.add(arrayList);
        }
        return this.recordList;
    }

    private void textValue(byte[] bArr) {
        Float f;
        Float f2;
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        DecimalFormat decimalFormat2 = new DecimalFormat("0.00");
        DecimalFormat decimalFormat3 = new DecimalFormat("0.000");
        DecimalFormat decimalFormat4 = new DecimalFormat("0.000000");
        DecimalFormat decimalFormat5 = new DecimalFormat("00.00");
        new DecimalFormat("00.0000");
        DecimalFormat decimalFormat6 = new DecimalFormat("000.0");
        DecimalFormat decimalFormat7 = new DecimalFormat("000.00");
        new DecimalFormat("000.000");
        DecimalFormat decimalFormat8 = new DecimalFormat("000.000000");
        DecimalFormat decimalFormat9 = new DecimalFormat("0000.0");
        DecimalFormat decimalFormat10 = new DecimalFormat("0000.00");
        new DecimalFormat("0000.000");
        DecimalFormat decimalFormat11 = new DecimalFormat("0000.000000");
        new DecimalFormat("00000.0");
        Float valueOf = Float.valueOf(0.0f);
        Float valueOf2 = Float.valueOf(0.0f);
        Float valueOf3 = Float.valueOf(0.0f);
        this.adu = bArr[3];
        switch (bArr[3]) {
            case 1:
                this.button.setVisibility(View.GONE);
                this.button2.setVisibility(View.GONE);
                this.layout1.setBackgroundColor(getResources().getColor(R.color.colorlayoutbg));
                this.layout2.setBackgroundColor(getResources().getColor(R.color.colorlayoutbg));
                this.layout3.setBackgroundColor(getResources().getColor(R.color.colorlayoutbg));
                this.layoutBL.setVisibility(View.VISIBLE);
                this.textAc.setBackgroundResource(0);
                this.textDc.setBackgroundResource(R.drawable.text_bg);
                this.textUsb.setBackgroundResource(R.drawable.text_bg);
                this.text5.setText(getText(R.string.Power_Factor));
                this.text7.setText(getText(R.string.carbon_dioxide));
                this.text8.setText(getText(R.string.Cumulative_electricity_bill));
                this.text9.setText(getText(R.string.AC_frequency));
                this.text11.setText(getText(R.string.Electricity_price_setting));
                f = Float.valueOf((float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 10.0d));
                f2 = Float.valueOf((float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 1000.0d));
                valueOf3 = Float.valueOf((float) (((double) ((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256)) + (bArr[12] & 255))) / 10.0d));
                this.textVoltage.setText(decimalFormat6.format(f) + "V");
                this.textCurrent.setText(decimalFormat3.format(f2) + "A");
                this.textPower.setText(decimalFormat9.format(valueOf3) + "W");
                this.textFactor.setText(decimalFormat2.format(((double) (((bArr[22] & 255) * 256) + (bArr[23] & 255))) / 1000.0d) + "PF");
                this.textCumulative.setText(decimalFormat7.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + getString(R.string.KWH));
                String format = decimalFormat8.format((((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) * 0.997d);
                String substring = format.substring(0, format.length() + -4);
                this.textcarbon.setText(substring + "kg");
                String format2 = decimalFormat4.format((((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d) * (((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d));
                String substring2 = format2.substring(0, format2.length() + -4);
                this.textBill.setText(substring2);
                this.textAC.setText((((double) (((bArr[20] & 255) * 256) + (bArr[21] & 255))) / 10.0d) + "Hz");
                int i = ((bArr[24] & 255) * 256) + (bArr[25] & 255);
                this.textInternal.setText(i + "℃/" + decimalFormat.format((((double) i) * 1.8d) + 32.0d) + "℉");
                TextView textView4 = this.textElectricity;
                StringBuilder sb = new StringBuilder();
                sb.append(decimalFormat2.format(((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d));
                textView4.setText(sb.toString());
                if (bArr[30] == 0) {
                    this.textBLV.setText(getString(R.string.Long_black));
                } else if (bArr[30] == 60) {
                    this.textBLV.setText(getString(R.string.Long_bright));
                } else {
                    this.textBLV.setText(((int) bArr[30]) + getString(R.string.second));
                }
                break;
            case 2:
                this.f0++;
                this.button.setVisibility(View.VISIBLE);
                this.button2.setVisibility(View.VISIBLE);
                this.layoutBL.setVisibility(View.VISIBLE);
                this.layout1.setBackgroundColor(getResources().getColor(R.color.colorlayoutbg));
                this.layout2.setBackgroundColor(getResources().getColor(R.color.colorlayoutbg));
                this.layout3.setBackgroundColor(getResources().getColor(R.color.colorlayoutbg));
                this.textAc.setBackgroundResource(R.drawable.text_bg);
                this.textDc.setBackgroundResource(0);
                this.textUsb.setBackgroundResource(R.drawable.text_bg);
                this.text5.setText(getText(R.string.Cumulative_capacity));
                this.text7.setText(getText(R.string.carbon_dioxide));
                this.text8.setText(getText(R.string.Cumulative_electricity_bill));
                this.text9.setText(getText(R.string.time_record));
                this.text11.setText(getText(R.string.Electricity_price_setting));
                f = Float.valueOf((float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 10.0d));
                f2 = Float.valueOf((float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 1000.0d));
                valueOf3 = Float.valueOf(f.floatValue() * f2.floatValue());
                this.textVoltage.setText(decimalFormat6.format(f) + "V");
                this.textCurrent.setText(decimalFormat3.format(f2) + "A");
                String format3 = decimalFormat11.format(valueOf3);
                String substring3 = format3.substring(0, format3.length() + -5);
                this.textPower.setText(substring3 + "W");
                this.textFactor.setText(decimalFormat7.format(((double) ((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256)) + (bArr[12] & 255))) / 100.0d) + "Ah");
                this.textCumulative.setText(decimalFormat7.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + getString(R.string.KWH));
                String format4 = decimalFormat8.format((((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) * 0.997d);
                String substring4 = format4.substring(0, format4.length() + -4);
                this.textcarbon.setText(substring4 + "kg");
                String format5 = decimalFormat4.format((((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d) * (((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d));
                String substring5 = format5.substring(0, format5.length() - 4);
                this.textBill.setText(substring5);
                if (this.f0 / 3600 < 10) {
                    str = "00" + (this.f0 / 3600);
                } else if (this.f0 / 3600 < 100) {
                    str = "0" + (this.f0 / 3600);
                } else {
                    str = "" + (this.f0 / 3600);
                }
                if (this.f0 / 60 < 10) {
                    str2 = "0" + (this.f0 / 60);
                } else {
                    str2 = "" + (this.f0 / 60);
                }
                if (this.f0 % 60 < 10) {
                    str3 = "0" + (this.f0 % 60);
                } else {
                    str3 = "" + (this.f0 % 60);
                }
                this.textAC.setText(str + ":" + str2 + ":" + str3);
                int i2 = ((bArr[24] & 255) * 256) + (bArr[25] & 255);
                this.textInternal.setText(i2 + "℃/" + decimalFormat.format((((double) i2) * 1.8d) + 32.0d) + "℉");
                TextView textView5 = this.textElectricity;
                StringBuilder sb2 = new StringBuilder();
                sb2.append(decimalFormat2.format(((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d));
                textView5.setText(sb2.toString());
                if (bArr[30] == 0) {
                    this.textBLV.setText(getString(R.string.Long_black));
                } else if (bArr[30] == 60) {
                    this.textBLV.setText(getString(R.string.Long_bright));
                } else {
                    this.textBLV.setText(((int) bArr[30]) + getString(R.string.second));
                }
                break;
            case 3:
                this.button.setVisibility(View.VISIBLE);
                this.button2.setVisibility(View.VISIBLE);
                this.layoutBL.setVisibility(View.GONE);
                this.layout1.setBackgroundColor(getResources().getColor(R.color.colorlayoutbgclear));
                this.layout2.setBackgroundColor(getResources().getColor(R.color.colorlayoutbgclear));
                this.layout3.setBackgroundColor(getResources().getColor(R.color.colorlayoutbgclear));
                this.textAc.setBackgroundResource(R.drawable.text_bg);
                this.textDc.setBackgroundResource(R.drawable.text_bg);
                this.textUsb.setBackgroundResource(0);
                this.text5.setText(getText(R.string.Cumulative_capacity));
                this.text7.setText("USB_D + :");
                this.text8.setText("USB_D - :");
                this.text9.setText(getText(R.string.time_record));
                this.text11.setText(getText(R.string.Backlight));
                f = Float.valueOf((float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 100.0d));
                Float valueOf4 = Float.valueOf((float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 100.0d));
                valueOf3 = Float.valueOf(f.floatValue() * valueOf4.floatValue());
                this.textVoltage.setText(decimalFormat5.format(f) + "V");
                this.textCurrent.setText(decimalFormat5.format(valueOf4) + "A");
                String format6 = decimalFormat11.format(valueOf3);
                String substring6 = format6.substring(0, format6.length() + -4);
                this.textPower.setText(substring6 + "W");
                TextView textView6 = this.textFactor;
                StringBuilder sb3 = new StringBuilder();
                sb3.append(ReservedInt(5, (((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256) + (bArr[12] & 255)) + ""));
                sb3.append("mAh");
                textView6.setText(sb3.toString());
                this.textCumulative.setText(decimalFormat10.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "Wh");
                this.textcarbon.setText(decimalFormat2.format(((double) (((bArr[19] & 255) * 256) + (bArr[20] & 255))) / 100.0d) + "V");
                this.textBill.setText(decimalFormat2.format(((double) (((bArr[17] & 255) * 256) + (bArr[18] & 255))) / 100.0d) + "V");
                if (((bArr[23] & 255) * 256) + (bArr[24] & 255) < 10) {
                    str4 = "00" + (((bArr[23] & 255) * 256) + (bArr[24] & 255));
                } else if (((bArr[23] & 255) * 256) + (bArr[24] & 255) < 100) {
                    str4 = "0" + (((bArr[23] & 255) * 256) + (bArr[24] & 255));
                } else {
                    str4 = "" + (((bArr[23] & 255) * 256) + (bArr[24] & 255));
                }
                if (bArr[25] < 10) {
                    str5 = "0" + ((int) bArr[25]);
                } else {
                    str5 = "" + ((int) bArr[25]);
                }
                if (bArr[26] < 10) {
                    str6 = "0" + ((int) bArr[26]);
                } else {
                    str6 = "" + ((int) bArr[26]);
                }
                this.textAC.setText(str4 + ":" + str5 + ":" + str6);
                int i3 = ((bArr[21] & 255) * 256) + (bArr[22] & 255);
                this.textInternal.setText(i3 + "℃/" + decimalFormat.format((((double) i3) * 1.8d) + 32.0d) + "℉");
                if (bArr[27] == 0) {
                    this.textElectricity.setText(getString(R.string.Long_black));
                } else if (bArr[27] == 60) {
                    this.textElectricity.setText(getString(R.string.Long_bright));
                } else {
                    this.textElectricity.setText(((int) bArr[27]) + getString(R.string.second));
                }
                f2 = valueOf4;
                break;
            default:
                f = valueOf;
                f2 = valueOf2;
                break;
        }
        this.list0.add(f);
        this.list1.add(f2);
        this.list2.add(valueOf3);
        this.listData.add(f);
        this.listData.add(Float.valueOf(f2.floatValue() * 5.0f));
        this.listData.add(Float.valueOf(valueOf3.floatValue() / 6.0f));
        this.timeList.add(this.df.format(Long.valueOf(System.currentTimeMillis())));
        this.listData.clear();
    }

    private void DialogClear(String str, final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage(str);
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            private int anonVar;
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.send(MainActivity.this.adu, anonVar, 0, 0, 0);
            }
            private DialogInterface.OnClickListener init(int var){
                anonVar = var;
                return this;
            }
        }.init(i)).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }

    private void send(int i, int i2, int i3, int i4, int i5) {
        byte[] bArr = new byte[10];
        bArr[0] = -1;
        bArr[1] = 85;
        bArr[2] = 17;
        bArr[3] = (byte) i;
        Log.i("DEBUG","bArr[3] = "+bArr[3]);
        bArr[4] = (byte) i2;
        Log.i("DEBUG","bArr[4] = "+bArr[4]);
        bArr[6] = (byte) i3;
        Log.i("DEBUG","bArr[6] = "+bArr[6]);
        bArr[7] = (byte) i4;
        Log.i("DEBUG","bArr[7] = "+bArr[7]);
        bArr[8] = (byte) i5;
        Log.i("DEBUG","bArr[8] = "+bArr[8]);
        bArr[9] = (byte) ((((((((bArr[2] & 255) + (bArr[3] & 255)) + (bArr[4] & 255)) + (bArr[5] & 255)) + (bArr[6] & 255)) + (bArr[7] & 255)) + (bArr[8] & 255)) ^ 68);
        Log.i("校验码", ((bArr[2] & 255) + (bArr[3] & 255) + (bArr[4] & 255) + (bArr[5] & 255) + (bArr[6] & 255) + (bArr[7] & 255) + (bArr[8] & 255)) + "");
        BLEService.send(bArr);
    }

    public void reset1(View view){
        DialogClear(getString(R.string.Clear1), 2);
    }

    public void reset2(View view){
        if (this.adu == 2) {
            this.f0 = 0;
        } else {
            DialogClear(getString(R.string.Clear2), 3);
        }
    }

    public void reset3(View view){
        if (this.adu != 3) {
            DialogClear(getString(R.string.Clear), 1);
        } else {
            DialogClear(getString(R.string.Clear21), 1);
        }
    }

    public void sendSet(View view){
        send(this.adu, 49, 0, 0, 0);
    }

    public void sendMinus(View view){
        send(this.adu, 52, 0, 0, 0);
    }

    public void sendPlus(View view){
        send(this.adu, 51, 0, 0, 0);
    }

    public void sendOk(View view){
        send(this.adu, 50, 0, 0, 0);
    }

    public class value extends BroadcastReceiver {
        public value() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:12:0x002f  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x009d  */
        /* JADX WARNING: Removed duplicated region for block: B:41:? A[RETURN, SYNTHETIC] */
        public void onReceive(Context context, Intent intent) {
            Log.i("DEBUG", "Received Data");
            char c = 65535;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            Log.i("DEBUG", "hashCode: "+hashCode);
            Log.i("DEBUG","action: "+action);
            if (hashCode != -678816493) {
                if (hashCode == 513534204 && action.equals(BLEService.CONTENT_DEVICE)) {
                    c = 1;
                }
            } else if (action.equals(BLEService.ALL_VALUE)) {
                c = 0;
            }
            switch (c) {
                case 0:
                    byte[] byteArray = intent.getExtras().getByteArray(BLEService.ALL_VALUE);
                    if (byteArray.length >= 3) {
                        if (byteArray[2] == 2) {
                            Log.i("Return", UUIDs.bytesToHexString(byteArray));
                            return;
                        }
                        if ((byteArray[0] & 255) == 255 && byteArray[2] == 1) {
                            MainActivity.this.mValue = byteArray;
                        }
                        if ((byteArray[0] & 255) != 255 && MainActivity.this.mValue.length >= 3) {
                            MainActivity.this.mValue = UUIDs.concat(MainActivity.this.mValue, byteArray);
                            Log.i("合并", UUIDs.bytesToHexString(MainActivity.this.mValue));
                            if (MainActivity.this.mValue.length == 36) {
                                MainActivity.this.textValue(MainActivity.this.mValue);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case 1:
                    if (intent.getExtras().getBoolean(BLEService.CONTENT_DEVICE)) {
                        //MainActivity.this.imageScan.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.lj));
                        MainActivity.this.textName.setText(BLEService.mBluetoothGatt.getDevice().getName());
                        //MainActivity.this.lineChart.getLineData().clearValues();
                        //MainActivity.this.chartManager = new ChartManager(MainActivity.this.lineChart, MainActivity.names, MainActivity.colour);
                        return;
                    }
                    //MainActivity.this.imageScan.setImageDrawable(MainActivity.this.getResources().getDrawable(R.drawable.dk));
                    MainActivity.this.textName.setText("");
                    return;
                default:
                    return;
            }
        }
    }
    private String ReservedInt(int i, String str) {
        int intValue = Integer.valueOf(str).intValue();
        String str2 = intValue + "";
        switch (i) {
            case 2:
                if (intValue >= 10) {
                    return str2;
                }
                return "0" + intValue;
            case 3:
                if (intValue < 10) {
                    return "00" + intValue;
                } else if (intValue >= 100) {
                    return str2;
                } else {
                    return "0" + intValue;
                }
            case 4:
                if (intValue < 10) {
                    return "000" + intValue;
                } else if (intValue < 100) {
                    return "00" + intValue;
                } else if (intValue >= 1000) {
                    return str2;
                } else {
                    return "0" + intValue;
                }
            case 5:
                if (intValue < 10) {
                    return "0000" + intValue;
                } else if (intValue < 100) {
                    return "000" + intValue;
                } else if (intValue < 1000) {
                    return "00" + intValue;
                } else if (intValue >= 10000) {
                    return str2;
                } else {
                    return "0" + intValue;
                }
            default:
                return str2;
        }
    }
}