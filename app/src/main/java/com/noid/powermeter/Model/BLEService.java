package com.noid.powermeter.Model;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.preference.PreferenceManager;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BLEService extends Service {
    public static final String ALL_VALUE = "ALL_VALUE";
    public static final String BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    public static final String CONTENT_DEVICE = "CONTENT_DEVICE";
    public static boolean CONTENT_STATUS = false;
    public static String bluetooth_device_address = "";
    private static BluetoothAdapter mAdapter;
    public static BluetoothGatt mBluetoothGatt;
    private MyBinder binder = new MyBinder();
    private Context context;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /* class com.tang.etest.e_test.Model.BLEService.AnonymousClass2 */

        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            Log.i("onConnectionStateChange", "Connection" + i);
            if (i2 == BluetoothProfile.STATE_DISCONNECTED) {
                BLEService.this.scan(true);
                BLEService.CONTENT_STATUS = false;
                BLEService.this.broadcastConnect(BLEService.CONTENT_DEVICE, false);
            } else if (i2 == BluetoothProfile.STATE_CONNECTED) {
                BLEService.mBluetoothGatt.discoverServices();
                BLEService.CONTENT_STATUS = true;
                BLEService.this.broadcastConnect(BLEService.CONTENT_DEVICE, true);
                SharedPreferences.Editor edit = BLEService.this.mSharedPreferences.edit();
                BLEService.bluetooth_device_address = bluetoothGatt.getDevice().getAddress();
                edit.putString("DEVICE_ADDRESS", BLEService.bluetooth_device_address);
                edit.commit();
            }
        }

        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.i("onServicesDiscovered", "Service Callback " + i);
            for (BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()) {
                for (final BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                    if (bluetoothGattCharacteristic.getUuid().equals(UUID.fromString(UUIDs.UUID_NOTIFY))) {
                        new Thread(new Runnable() {
                            /* class com.tang.etest.e_test.Model.BLEService.AnonymousClass2.AnonymousClass1 */

                            public void run() {
                                new Timer().schedule(new TimerTask() {
                                    /* class com.tang.etest.e_test.Model.BLEService.AnonymousClass2.AnonymousClass1.AnonymousClass1 */

                                    public void run() {
                                        BLEService.this.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                                    }
                                }, 1000);
                            }
                        }).start();
                    }
                }
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                /* class com.tang.etest.e_test.Model.BLEService.AnonymousClass2.AnonymousClass2 */

                public void run() {
                    Toast.makeText(BLEService.this.getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.i("onCharacteristicRead", "Read " + bluetoothGattCharacteristic.getValue());
        }

        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            Log.i("写入", UUIDs.bytesToHexString(bluetoothGattCharacteristic.getValue()));
        }

        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            if (bluetoothGattCharacteristic != null) {
                bluetoothGattCharacteristic.getUuid().toString();
                BLEService.this.broadcastByte(BLEService.ALL_VALUE, bluetoothGattCharacteristic.getValue());
            }
        }
    };
    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        /* class com.tang.etest.e_test.Model.BLEService.AnonymousClass1 */

        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            Log.i("Scanning ", bluetoothDevice.getName() + " rssi " + i);
            if (bluetoothDevice.getName() != null) {
                if (bluetoothDevice.getAddress().equals(BLEService.bluetooth_device_address)) {
                    BLEService.this.scan(false);
                    BLEService.this.connect(BLEService.bluetooth_device_address);
                }
                BLEService.this.broadcastUpdate(BLEService.BLUETOOTH_DEVICE, bluetoothDevice);
            }
        }
    };
    private SharedPreferences mSharedPreferences;
    String valueStr = "";

    public void onCreate() {
        super.onCreate();
        initBluetooth();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        bluetooth_device_address = this.mSharedPreferences.getString("DEVICE_ADDRESS", null);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Log.i("test", "ServiceStart");
        scan(true);
        Log.i("Kathy", "onStartCommand - startId = " + i2 + ", Thread ID = " + Thread.currentThread().getId());
        return super.onStartCommand(intent, i, i2);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("test", "ServiceDestroy");
    }

    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public BLEService getService() {
            return BLEService.this;
        }
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.i("test", "Bind");
        return this.binder;
    }

    public boolean onUnbind(Intent intent) {
        Log.i("test", "Unbind");
        return super.onUnbind(intent);
    }

    public void initBluetooth() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void scan(boolean z) {
        if (z) {
            Log.i("test", "Scan started");
            mAdapter.startLeScan(this.mLeScanCallback);
            return;
        }
        Log.i("test", "Scan stopped");
        mAdapter.stopLeScan(this.mLeScanCallback);
    }

    public void connect(String str) {
        Log.i("Connecting ", str);
        Context applicationContext = getApplicationContext();
        Toast.makeText(applicationContext, "Connecting: " + str, Toast.LENGTH_SHORT).show();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = mAdapter.getRemoteDevice(str).connectGatt(this, false, this.mGattCallback);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean z) {
        mBluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, z);
        List<BluetoothGattDescriptor> descriptors = bluetoothGattCharacteristic.getDescriptors();
        if (descriptors != null) {
            for (BluetoothGattDescriptor bluetoothGattDescriptor : descriptors) {
                bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                synchronized (this) {
                    mBluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void broadcastUpdate(String str, BluetoothDevice bluetoothDevice) {
        Intent intent = new Intent(str);
        intent.putExtra(str, bluetoothDevice);
        sendBroadcast(intent);
    }

    public void broadcastValueUpdate(String str, String str2) {
        Intent intent = new Intent(str);
        intent.putExtra(str, str2);
        sendBroadcast(intent);
    }

    public void broadcastByte(String str, byte[] bArr) {
        Intent intent = new Intent(str);
        intent.putExtra(str, bArr);
        sendBroadcast(intent);
    }

    public void broadcastConnect(String str, boolean z) {
        Intent intent = new Intent(str);
        intent.putExtra(str, z);
        sendBroadcast(intent);
    }

    public static void send(byte[] bArr) {
        BluetoothGattService service;
        if (CONTENT_STATUS && (service = mBluetoothGatt.getService(UUID.fromString(UUIDs.UUID_SERVICE))) != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(UUIDs.UUID_NOTIFY));
            characteristic.setValue(bArr);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }
}
