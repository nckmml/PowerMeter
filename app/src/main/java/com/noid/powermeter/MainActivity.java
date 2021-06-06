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
import android.net.Uri;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public int adu;
    public boolean mBound = false;
    public ActivityResultLauncher<Intent> bluetoothActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        initView();
                    }
                }
            });
    BLEService mService;
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
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    private ArrayList<ArrayList<String>> recordList;

    public ActivityResultLauncher<Intent> saveActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
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
                                ArrayList<ArrayList<String>> templist = new ArrayList<>(getRecordData());
                                for (int i = 0; i < templist.size(); i++) {
                                    bw.write(templist.get(i).get(0).toString() + "," + templist.get(i).get(1).toString() + "," + templist.get(i).get(2).toString() + "," + templist.get(i).get(3).toString() + "\n");
                                }
                                Log.i("File writer", "Finished writing to file");
                                bw.flush();
                                bw.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    private ArrayList<ArrayList<Entry>> rawRecordList;
    private int f0 = 0;
    private ActivityMainBinding binding;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (BluetoothAdapter.getDefaultAdapter().isEnabled())
                        initView();
                    else
                        initBluetooth();
                } else {
                    System.exit(1);
                }
            });
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BLEService.MyBinder binder = (BLEService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
            getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getPrimaryNavigationFragment().onResume();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.exit:
                Intent intent = new Intent(this, BLEService.class);
                intent.setAction(BLEService.ACTION_STOP_NOTIFICATION_SERVICE);
                startService(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
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
        if (BLEService.mBluetoothGatt != null) {
            createFile("text/csv", BLEService.mBluetoothGatt.getDevice().getName() + "_" + java.time.LocalDateTime.now() + ".csv");
        } else
            createFile("text/csv", "PowerRecording_" + java.time.LocalDateTime.now() + ".csv");
    }

    public ArrayList<ArrayList<String>> getRecordData() {
        this.recordList = new ArrayList<>();
        Log.i("getRecordData", "started getting record data");
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

    public ArrayList<String> getTimeRecordData() {
        return new ArrayList<String>(mService.returnList(4));
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

            private DialogInterface.OnClickListener init(int var) {
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
        if (this.adu == 2) {
            this.f0 = 0;
        } else {
            DialogClear(getString(R.string.Clear2), 3);
        }
    }

    public void reset3(View view) {
        if (this.adu != 3) {
            DialogClear(getString(R.string.Clear), 1);
        } else {
            DialogClear(getString(R.string.Clear21), 1);
        }
    }

    public void sendSet(View view) {
        send(this.adu, 49, 0, 0, 0);
    }

    public void sendMinus(View view) {
        send(this.adu, 52, 0, 0, 0);
    }

    public void sendPlus(View view) {
        send(this.adu, 51, 0, 0, 0);
    }

    public void sendOk(View view) {
        send(this.adu, 50, 0, 0, 0);
    }
}