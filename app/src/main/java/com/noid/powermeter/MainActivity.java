package com.noid.powermeter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.noid.powermeter.Model.BLEService;

import com.noid.powermeter.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private byte[] mValue;

    private int adu;

    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

    private ArrayList<ArrayList<String>> recordList;
    private ArrayList<ArrayList<Entry>> rawRecordList;

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

    BLEService mService;
    boolean mBound = false;

    private static final int WRITE_REQUEST_CODE = 43;
    private static final int READ_REQUEST_CODE = 45;

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
                R.id.navigation_textdisplay, R.id.navigation_graph, R.id.navigation_bluetoothlist, R.id.navigation_export)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        initBluetooth();
        initView();
    }

    private void initView() {
        Intent intent = new Intent(this, BLEService.class);
        intent.setAction(BLEService.ACTION_START_NOTIFICATION_SERVICE);
        startForegroundService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
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
                    triggerRebirth(getApplicationContext());
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

    public void openFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null && data.getData() != null) {
                        ArrayList<ArrayList<String>> records = new ArrayList<>();
                        try (InputStream inputStream =
                                     getContentResolver().openInputStream(data.getData());
                             BufferedReader reader = new BufferedReader(
                                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (!line.equals("Time,Voltage,Current,Power")){
                                    String[] values = line.split(",");
                                    if (values.length == 4)
                                        records.add(new ArrayList<>(Arrays.asList(values)));
                                }
                            }
                            mService.importList(records);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
        if (requestCode == WRITE_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data != null
                            && data.getData() != null) {
                        OutputStream outputStream;
                        try {
                            outputStream = getContentResolver().openOutputStream(data.getData());
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                            Log.i("File writer", "started writing to file");
                            bw.write("Time,Voltage,Current,Power\n");
                            ArrayList<ArrayList<String>> templist = new ArrayList<>(getRecordData());
                            for (int i=0; i<templist.size(); i++){
                                bw.write(templist.get(i).get(0).toString()+","+templist.get(i).get(1).toString()+","+templist.get(i).get(2).toString()+","+templist.get(i).get(3).toString()+"\n");
                            }
                            Log.i("File writer", "Finished writing to file");
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

    public ArrayList<ArrayList<String>> getRecordData() {
        this.recordList = new ArrayList<>();
        Log.i("getRecordData","started getting record data");
        for (int i = 0; i < mService.returnList(4).size(); i++) {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(String.valueOf(mService.returnList(4).get(i)));
            arrayList.add(String.valueOf(mService.returnList(0).get(i)));
            arrayList.add(String.valueOf(mService.returnList(1).get(i)));
            arrayList.add(String.valueOf(mService.returnList(2).get(i)));
            this.recordList.add(arrayList);
        }
        Log.i("getRecordData", "finished getting record data");
        return this.recordList;
    }

    public ArrayList<ArrayList<Entry>> getRawRecordData() {
        this.rawRecordList = new ArrayList<ArrayList<Entry>>();
        for (int j = 0; j <= 2; j++) {
            ArrayList<Entry> arrayList = new ArrayList<>();
            for (int i = 0; i < mService.returnList(4).size(); i++) {
                arrayList.add(new Entry(i, (float) mService.returnList(j).get(i)));
            }
            this.rawRecordList.add(arrayList);
        }
        return this.rawRecordList;
    }

    private void updateText(HashMap<String, String> datamap){
        Log.d("updateText()", "Received data to update");
        this.adu = Integer.parseInt(datamap.get("device"));
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

    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    private void send(int i, int i2, int i3, int i4, int i5) {
        byte[] bArr = new byte[10];
        bArr[0] = -1;
        bArr[1] = 85;
        bArr[2] = 17;
        bArr[3] = (byte) i;
        bArr[4] = (byte) i2;
        bArr[6] = (byte) i3;
        bArr[7] = (byte) i4;
        bArr[8] = (byte) i5;
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
                    HashMap<String, String> datamap = (HashMap<String, String>)intent.getSerializableExtra(BLEService.ALL_VALUE);
                    if (datamap.size() > 0)
                        updateText(datamap);
                    return;
                case 1:
                    if (intent.getExtras().getBoolean(BLEService.CONTENT_DEVICE)) {
                        MainActivity.this.textName.setText(BLEService.mBluetoothGatt.getDevice().getName());
                        return;
                    }
                    MainActivity.this.textName.setText("");
                    return;
                default:
                    return;
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BLEService.MyBinder binder = (BLEService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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