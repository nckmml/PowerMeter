package com.noid.powermeter.Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.noid.powermeter.MainActivity;
import com.noid.powermeter.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BLEService extends Service {
    public static final String ALL_VALUE = "ALL_VALUE";
    public static final String BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    public static final String CONTENT_DEVICE = "CONTENT_DEVICE";
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    public static final String ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE";
    public static final String ACTION_STOP_NOTIFICATION_SERVICE = "ACTION_STOP_NOTIFICATION_SERVICE";
    private final static String default_notification_channel_id = "default";
    private static final String TAG_NOTIFICATION_SERVICE = "NOTIFICATION_SERVICE";
    public static boolean CONTENT_STATUS = false;
    public static String bluetooth_device_address = "";
    public static BluetoothGatt mBluetoothGatt;
    private static BluetoothAdapter mAdapter;
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
    public ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("Scanning ", result.getDevice().toString() + " rssi " + result.getRssi());
            if (result.getDevice().toString() != null) {
                if (result.getDevice().getAddress().equals(BLEService.bluetooth_device_address)) {
                    BLEService.this.scan(false);
                    BLEService.this.connect(BLEService.bluetooth_device_address);
                }
                BLEService.this.broadcastUpdate(BLEService.BLUETOOTH_DEVICE, result.getDevice());
            }
        }
    };
    private MyBinder binder = new MyBinder();
    private byte[] mValue;
    private List<Float> list0 = new ArrayList();
    private List<Float> list1 = new ArrayList();
    private List<Float> list2 = new ArrayList();
    private List<Float> listData = new ArrayList();
    private List<String> timeList = new ArrayList();
    private int f0 = 0;
    private SharedPreferences mSharedPreferences;
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
            byte[] bArr = bluetoothGattCharacteristic.getValue();
            Float f;
            Float f2;
            Float valueOf = Float.valueOf(0.0f);
            Float valueOf2 = Float.valueOf(0.0f);
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
            DecimalFormat decimalFormat6 = new DecimalFormat("000.0");
            DecimalFormat decimalFormat7 = new DecimalFormat("000.00");
            DecimalFormat decimalFormat8 = new DecimalFormat("000.000000");
            DecimalFormat decimalFormat9 = new DecimalFormat("0000.0");
            DecimalFormat decimalFormat10 = new DecimalFormat("0000.00");
            DecimalFormat decimalFormat11 = new DecimalFormat("0000.000000");
            Float valueOf3 = Float.valueOf(0.0f);
            HashMap<String, String> datamap = new HashMap<String, String>();
            if (bArr.length >= 3) {
                if ((bArr[0] & 255) == 255 && bArr[2] == 1) {
                    mValue = bArr;
                }
                if ((bArr[0] & 255) != 255 && bArr.length >= 3) {
                    mValue = UUIDs.concat(mValue, bArr);
                    if (mValue != null) {
                        if (mValue.length == 36) {
                            StringBuilder dataBuilder = new StringBuilder();
                            bArr = mValue;
                            switch (bArr[3]) {
                                case 1:
                                    datamap.put("device", "1");
                                    f = Float.valueOf((float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 10.0d));
                                    datamap.put("voltage", (decimalFormat6.format(f) + "V"));
                                    f2 = Float.valueOf((float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 1000.0d));
                                    datamap.put("current", (decimalFormat3.format(f2) + "A"));
                                    String format = decimalFormat8.format((((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) * 0.997d);
                                    String substring = format.substring(0, format.length() + -4);
                                    datamap.put("co2", (substring + "kg"));
                                    String format2 = decimalFormat4.format((((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d) * (((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d));
                                    String substring2 = format2.substring(0, format2.length() + -4);
                                    datamap.put("echarges", substring2);
                                    valueOf3 = Float.valueOf((float) (((double) ((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256)) + (bArr[12] & 255))) / 10.0d));
                                    datamap.put("power", (decimalFormat9.format(valueOf3) + "W"));
                                    datamap.put("powerfactor", (decimalFormat2.format(((double) (((bArr[22] & 255) * 256) + (bArr[23] & 255))) / 1000.0d) + "PF"));
                                    datamap.put("electricity", (decimalFormat7.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "kWh"));
                                    datamap.put("acfreq", ((((double) (((bArr[20] & 255) * 256) + (bArr[21] & 255))) / 10.0d) + "Hz"));
                                    int i = ((bArr[24] & 255) * 256) + (bArr[25] & 255);
                                    datamap.put("temperature", (i + "℃/" + decimalFormat.format((((double) i) * 1.8d) + 32.0d) + "℉"));
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(decimalFormat2.format(((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d));
                                    datamap.put("eprice", (sb.toString()));
                                    if (bArr[30] == 0) {
                                        datamap.put("backlight", (getString(R.string.Long_black)));
                                    } else if (bArr[30] == 60) {
                                        datamap.put("backlight", (getString(R.string.Long_bright)));
                                    } else {
                                        datamap.put("backlight", (((int) bArr[30]) + getString(R.string.second)));
                                    }
                                    dataBuilder.append("Voltage: ");
                                    dataBuilder.append(datamap.get("voltage") + "\n");
                                    dataBuilder.append("Current: ");
                                    dataBuilder.append(datamap.get("current") + "\n");
                                    dataBuilder.append("Power: ");
                                    dataBuilder.append(datamap.get("power") + "\n");
                                    dataBuilder.append("Power Factor: ");
                                    dataBuilder.append(datamap.get("powerfactor") + "\n");
                                    dataBuilder.append("Electricity: ");
                                    dataBuilder.append(datamap.get("electricity") + "\n");
                                    dataBuilder.append("CO2: ");
                                    dataBuilder.append(datamap.get("co2") + "\n");
                                    dataBuilder.append("Electricity charges: ");
                                    dataBuilder.append(datamap.get("echarges") + "\n");
                                    dataBuilder.append("AC freq: ");
                                    dataBuilder.append(datamap.get("acfreq") + "\n");
                                    dataBuilder.append("Internal Temperature: ");
                                    dataBuilder.append(datamap.get("temperature") + "\n");
                                    dataBuilder.append("Elec. price setting: ");
                                    dataBuilder.append(datamap.get("eprice"));
                                    break;
                                case 2:
                                    datamap.put("device", "2");
                                    f0++;
                                    f = Float.valueOf((float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 10.0d));
                                    datamap.put("voltage", (decimalFormat6.format(f) + "V"));
                                    f2 = Float.valueOf((float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 1000.0d));
                                    datamap.put("current", (decimalFormat3.format(f2) + "A"));
                                    if (f0 / 3600 < 10) {
                                        str = "00" + (f0 / 3600);
                                    } else if (f0 / 3600 < 100) {
                                        str = "0" + (f0 / 3600);
                                    } else {
                                        str = "" + (f0 / 3600);
                                    }
                                    if (f0 / 60 < 10) {
                                        str2 = "0" + (f0 / 60);
                                    } else {
                                        str2 = "" + (0 / 60);
                                    }
                                    if (f0 % 60 < 10) {
                                        str3 = "0" + (f0 % 60);
                                    } else {
                                        str3 = "" + (f0 % 60);
                                    }
                                    datamap.put("time", (str + ":" + str2 + ":" + str3));
                                    valueOf3 = Float.valueOf(f.floatValue() * f2.floatValue());
                                    String format3 = decimalFormat11.format(valueOf3);
                                    String substring3 = format3.substring(0, format3.length() + -5);
                                    datamap.put("power", (substring3 + "W"));
                                    String format4 = decimalFormat8.format((((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) * 0.997d);
                                    String substring4 = format4.substring(0, format4.length() + -4);
                                    datamap.put("co2", (substring4 + "kg"));
                                    String format5 = decimalFormat4.format((((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d) * (((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d));
                                    String substring5 = format5.substring(0, format5.length() - 4);
                                    datamap.put("echarges", substring5);
                                    datamap.put("capacity", (decimalFormat7.format(((double) ((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256)) + (bArr[12] & 255))) / 100.0d) + "Ah"));
                                    datamap.put("electricity", (decimalFormat7.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "kWh"));
                                    StringBuilder sb2 = new StringBuilder();
                                    sb2.append(decimalFormat2.format(((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d));
                                    datamap.put("eprice", sb2.toString());
                                    if (bArr[30] == 0) {
                                        datamap.put("backlight", (getString(R.string.Long_black)));
                                    } else if (bArr[30] == 60) {
                                        datamap.put("backlight", (getString(R.string.Long_bright)));
                                    } else {
                                        datamap.put("backlight", (((int) bArr[30]) + getString(R.string.second)));
                                    }
                                    dataBuilder.append("Voltage: ");
                                    dataBuilder.append(datamap.get("Voltage") + "\n");
                                    dataBuilder.append("Current: ");
                                    dataBuilder.append(datamap.get("current") + "\n");
                                    dataBuilder.append("Power: ");
                                    dataBuilder.append(datamap.get("power") + "\n");
                                    dataBuilder.append("Capacity: ");
                                    dataBuilder.append(datamap.get("capacity") + "\n");
                                    dataBuilder.append("Electricity: ");
                                    dataBuilder.append(datamap.get("electricity") + "\n");
                                    dataBuilder.append("CO2: ");
                                    dataBuilder.append(datamap.get("co2") + "\n");
                                    dataBuilder.append("Electricity charges: ");
                                    dataBuilder.append(datamap.get("echarges") + "\n");
                                    dataBuilder.append("Time record: ");
                                    dataBuilder.append(datamap.get("time") + "\n");
                                    dataBuilder.append("Elec. price setting: ");
                                    dataBuilder.append(datamap.get("eprice"));
                                    break;
                                case 3:
                                    datamap.put("device", "3");
                                    f = Float.valueOf((float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 100.0d));
                                    datamap.put("voltage", (decimalFormat5.format(f) + "V"));
                                    Float valueOf4 = Float.valueOf((float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 100.0d));
                                    datamap.put("current", (decimalFormat5.format(valueOf4) + "A"));
                                    valueOf3 = Float.valueOf(f.floatValue() * valueOf4.floatValue());
                                    String format6 = decimalFormat11.format(valueOf3);
                                    String substring6 = format6.substring(0, format6.length() + -4);
                                    datamap.put("power", (substring6 + "W"));
                                    StringBuilder sb3 = new StringBuilder();
                                    sb3.append(ReservedInt(5, (((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256) + (bArr[12] & 255)) + ""));
                                    sb3.append("mAh");
                                    datamap.put("capacity", (sb3.toString()));
                                    datamap.put("electricity", (decimalFormat10.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "Wh"));
                                    datamap.put("dplus", (decimalFormat2.format(((double) (((bArr[19] & 255) * 256) + (bArr[20] & 255))) / 100.0d) + "V"));
                                    datamap.put("dminus", (decimalFormat2.format(((double) (((bArr[17] & 255) * 256) + (bArr[18] & 255))) / 100.0d) + "V"));
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
                                    datamap.put("time", (str4 + ":" + str5 + ":" + str6));
                                    int i3 = ((bArr[21] & 255) * 256) + (bArr[22] & 255);
                                    datamap.put("temperature", (i3 + "℃/" + decimalFormat.format((((double) i3) * 1.8d) + 32.0d) + "℉"));
                                    f2 = valueOf4;
                                    if (bArr[27] == 0) {
                                        datamap.put("backlight", (getString(R.string.Long_black)));
                                    } else if (bArr[27] == 60) {
                                        datamap.put("backlight", (getString(R.string.Long_bright)));
                                    } else {
                                        datamap.put("backlight", (((int) bArr[27]) + getString(R.string.second)));
                                    }
                                    dataBuilder.append("Voltage: ");
                                    dataBuilder.append(datamap.get("voltage") + "\n");
                                    dataBuilder.append("Current: ");
                                    dataBuilder.append(datamap.get("current") + "\n");
                                    dataBuilder.append("Power: ");
                                    dataBuilder.append(datamap.get("power") + "\n");
                                    dataBuilder.append("Capacity: ");
                                    dataBuilder.append(datamap.get("capacity") + "\n");
                                    dataBuilder.append("Electricity: ");
                                    dataBuilder.append(datamap.get("electricity") + "\n");
                                    dataBuilder.append("USB D+: ");
                                    dataBuilder.append(datamap.get("dplus") + "\n");
                                    dataBuilder.append("USB D-: ");
                                    dataBuilder.append(datamap.get("dminus") + "\n");
                                    dataBuilder.append("Time record: ");
                                    dataBuilder.append(datamap.get("time") + "\n");
                                    dataBuilder.append("Internal Temperature: ");
                                    dataBuilder.append(datamap.get("temperature"));
                                    break;
                                default:
                                    f = valueOf;
                                    f2 = valueOf2;
                                    break;
                            }
                            updateNotification(dataBuilder.toString());
                            list0.add(f);
                            list1.add(f2);
                            list2.add(valueOf3);
                            listData.add(f);
                            listData.add(Float.valueOf(f2.floatValue() * 5.0f));
                            listData.add(Float.valueOf(valueOf3.floatValue() / 6.0f));
                            timeList.add(df.format(Long.valueOf(System.currentTimeMillis())));
                            listData.clear();
                        }
                    }
                }
            }
            if (bluetoothGattCharacteristic != null) {
                bluetoothGattCharacteristic.getUuid().toString();
                BLEService.this.broadcastMap(BLEService.ALL_VALUE, datamap);
            }
        }
    };

    public BLEService() {
    }

    public static BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    public static void send(byte[] bArr) {
        BluetoothGattService service;
        if (CONTENT_STATUS && (service = mBluetoothGatt.getService(UUID.fromString(UUIDs.UUID_SERVICE))) != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(UUIDs.UUID_NOTIFY));
            characteristic.setValue(bArr);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public List returnList(int listi) {
        switch (listi) {
            case 0:
                return list0;
            case 1:
                return list1;
            case 2:
                return list2;
            case 3:
                return listData;
            case 4:
                return timeList;
            default:
                return new ArrayList();
        }
    }

    public void importList(ArrayList<ArrayList<String>> records) {
        List<Float> templist0 = new ArrayList<>();
        List<Float> templist1 = new ArrayList<>();
        List<Float> templist2 = new ArrayList<>();
        List<String> temptimeList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            templist0.add(Float.parseFloat(records.get(i).get(1)));
            templist1.add(Float.parseFloat(records.get(i).get(2)));
            templist2.add(Float.parseFloat(records.get(i).get(3)));
            temptimeList.add(records.get(i).get(0));
        }
        if (timeList.size() != 0) {
            templist0.addAll(list0);
            templist1.addAll(list1);
            templist2.addAll(list2);
            temptimeList.addAll(timeList);
        }
        list0 = new ArrayList<>(templist0);
        list1 = new ArrayList<>(templist1);
        list2 = new ArrayList<>(templist2);
        timeList = new ArrayList<>(temptimeList);
    }

    public void onCreate() {
        super.onCreate();
        initBluetooth();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        bluetooth_device_address = this.mSharedPreferences.getString("DEVICE_ADDRESS", null);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_START_NOTIFICATION_SERVICE:
                    startForegroundService();
                    Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                    if (mBluetoothGatt == null)
                        scan(true);
                    break;
                case ACTION_STOP_NOTIFICATION_SERVICE:
                    Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    BLEService.this.scan(false);
                    stopForegroundService();
                    break;
            }
        }
        Log.i("Kathy", "onStartCommand - startId = " + i2 + ", Thread ID = " + Thread.currentThread().getId());
        return super.onStartCommand(intent, i, i2);
    }

    private void startForegroundService() {
        startForeground(1, getNotification(""));
    }

    private void updateNotification(String text) {

        Notification notification = getNotification(text);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    private Notification getNotification(String text) {

        // The PendingIntent to launch our activity if the user selects
        // this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class), 0);

        // Create notification default intent.
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(BLEService.this,
                default_notification_channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("PowerMeter")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel notificationChannel = new
                NotificationChannel(NOTIFICATION_CHANNEL_ID, "Bluetooth Service", importance);
        mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(notificationChannel);
        return mBuilder.build();
    }

    private void stopForegroundService() {
        Log.d(TAG_NOTIFICATION_SERVICE, "Stop foreground service.");

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("test", "ServiceDestroy");
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

    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void scan(boolean z) {
        BluetoothLeScanner bluetoothLeScanner = mAdapter.getBluetoothLeScanner();
        if (z) {
            Log.i("test", "Scan started");
            bluetoothLeScanner.startScan(this.mLeScanCallback);
            return;
        }
        Log.i("test", "Scan stopped");
        bluetoothLeScanner.stopScan(this.mLeScanCallback);
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

    public void broadcastMap(String str, HashMap map) {
        Intent intent = new Intent(str);
        intent.putExtra(str, map);
        sendBroadcast(intent);
    }

    public void broadcastConnect(String str, boolean z) {
        Intent intent = new Intent(str);
        intent.putExtra(str, z);
        sendBroadcast(intent);
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

    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public BLEService getService() {
            return BLEService.this;
        }
    }
}
