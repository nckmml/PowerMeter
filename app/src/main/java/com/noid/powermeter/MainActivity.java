package com.noid.powermeter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.mikephil.charting.data.Entry;
import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private MainActivityViewModel mainActivityViewModel;
    private int adu() {
        if (mainActivityViewModel.getData().getValue() != null)
            return Integer.parseInt(mainActivityViewModel.getData().getValue().get(0));
        return 0;
    }
    private boolean mBound = false;
    public ActivityResultLauncher<Intent> bluetoothActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    initView();
                }
            });
    private BLEService mService;
    public ActivityResultLauncher<Intent> openActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            ArrayList<ArrayList<String>> records = new ArrayList<>();
                            try (InputStream inputStream =
                                         getContentResolver().openInputStream(data.getData());
                                 BufferedReader reader = new BufferedReader(
                                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {

                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (!line.equals("Time,Voltage,Current,Power")) {
                                        String[] values = line.split(",");
                                        if (values.length == 4)
                                            records.add(new ArrayList<>(Arrays.asList(values)));
                                    }
                                }
                                mService.importList(records);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
    public ActivityResultLauncher<Intent> saveActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null
                            && data.getData() != null) {
                        OutputStream outputStream;
                        try {
                            outputStream = getContentResolver().openOutputStream(data.getData());
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                            Log.i("File writer", "started writing to file");
                            bw.write("Time,Voltage,Current,Power\n");
                            ArrayList<Entry> voltagelist = new ArrayList(getVoltageData());
                            ArrayList<Entry> currentlist = new ArrayList(getCurrentData());
                            ArrayList<Entry> powerlist = new ArrayList(getPowerData());
                            ArrayList<String> timerecordlist = new ArrayList(getTimeRecordData());

                            for (int i = 0; i < timerecordlist.size(); i++) {
                                bw.write(timerecordlist.get(i) + "," + voltagelist.get(i).getY() + "," + currentlist.get(i).getY() + "," + powerlist.get(i).getY() + "\n");
                            }
                            Log.i("File writer", "Finished writing to file");
                            bw.flush();
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    private final ServiceConnection connection = new ServiceConnection() {

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
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (BluetoothAdapter.getDefaultAdapter().isEnabled())
                        initView();
                    else
                        initBluetooth();
                } else {
                    exit();
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.exit) {
            exit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Entry> getVoltageData(){
        return mainActivityViewModel.getVoltageData().getValue();
    }

    private ArrayList<Entry> getCurrentData(){
        return mainActivityViewModel.getCurrentData().getValue();
    }

    private ArrayList<Entry> getPowerData(){
        return mainActivityViewModel.getPowerData().getValue();
    }

    private ArrayList<String> getTimeRecordData(){
        return mainActivityViewModel.getTimeRecordData().getValue();
    }

    private void exit() {
        if(mBound) {
            Intent intent = new Intent(this, BLEService.class);
            intent.setAction(BLEService.ACTION_STOP_NOTIFICATION_SERVICE);
            startService(intent);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        com.noid.powermeter.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_textdisplay, R.id.navigation_graph, R.id.navigation_table, R.id.navigation_bluetoothlist, R.id.navigation_export)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        initLocationPermission();
    }

    private void initView() {
        Intent intent = new Intent(this, BLEService.class);
        intent.setAction(BLEService.ACTION_START_NOTIFICATION_SERVICE);
        startForegroundService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void initLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            DialogLocation(getString(R.string.locationInfo));
        } else if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            initBluetooth();
    }

    public void initBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            System.exit(1);
        }
        assert bluetoothAdapter != null;
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothActivityResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else
            initView();
    }

    private void createFile(String mimeType, String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        saveActivityResultLauncher.launch(intent);
    }

    public void openFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        openActivityResultLauncher.launch(intent);
    }

    public void exportExcel(View view) {
        /*
        this.file = new File(getSDPath() + "/Etest");
        makeDir(this.file);
        ExcelUtils.initExcel(this.file.toString() + "/Etest.xls", new String[]{"时间", "电压", "电流", "功率"});
        this.fileName = getSDPath() + "/Etest/Etest.xls";
        ExcelUtils.writeObjListToExcel(getRecordData(), this.fileName, this);
         */
        if (BLEService.mBluetoothGatt != null) {
            createFile("text/csv", BLEService.mBluetoothGatt.getDevice().getName() + "_" + java.time.LocalDateTime.now() + ".csv");
        } else
            createFile("text/csv", "PowerRecording_" + java.time.LocalDateTime.now() + ".csv");
    }

    private void DialogClear(String str, final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage(str);
        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            private int anonVar;

            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.send(adu(), anonVar, 0, 0, 0);
            }

            private DialogInterface.OnClickListener init(int var) {
                anonVar = var;
                return this;
            }
        }.init(i)).setNegativeButton(getString(R.string.cancel), (dialogInterface, i1) -> {
        }).show();
    }

    private void DialogLocation(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Info));
        builder.setMessage(str);
        builder
                .setPositiveButton(getString(R.string.confirm), (dialogInterface, i12) -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i1) -> exit()).show();
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

    public void reset1(View view) {
        DialogClear(getString(R.string.Clear1), 2);
    }

    public void reset2(View view) {
        if (adu() != 2) {
            DialogClear(getString(R.string.Clear2), 3);
        }
    }

    public void reset3(View view) {
        if (adu() != 3) {
            DialogClear(getString(R.string.Clear), 1);
        } else {
            DialogClear(getString(R.string.Clear21), 1);
        }
    }

    public void sendSet(View view) {
        send(adu(), 49, 0, 0, 0);
    }

    public void sendMinus(View view) {
        send(adu(), 52, 0, 0, 0);
    }

    public void sendPlus(View view) {
        send(adu(), 51, 0, 0, 0);
    }

    public void sendOk(View view) {
        send(adu(), 50, 0, 0, 0);
    }
}