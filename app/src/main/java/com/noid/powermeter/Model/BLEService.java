package com.noid.powermeter.Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.data.Entry;
import com.noid.powermeter.R;
import com.noid.powermeter.ui.Repository;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BLEService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private static final String default_notification_channel_id = "default";
    private static final String TAG_NOTIFICATION_SERVICE = "NOTIFICATION_SERVICE";
    private static final int DEVICE_AC = 1;
    private static final int DEVICE_DC = 2;
    private static final int DEVICE_USB = 3;

    private static boolean CONTENT_STATUS = false;
    private static String bluetooth_device_address = "";
    private static BluetoothAdapter mAdapter;
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final MyBinder binder = new MyBinder();

    private final MutableLiveData<ArrayList<String>> mData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Entry>> mVoltageData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Entry>> mCurrentData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Entry>> mPowerData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> mTimeRecordData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<BluetoothDevice>> mBluetoothDevices = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Entry>> mTemperatureData = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Entry>> mPercentageData = new MutableLiveData<>();

    private ArrayList<Entry> VoltageData = new ArrayList<>();
    private ArrayList<Entry> CurrentData = new ArrayList<>();
    private ArrayList<Entry> PowerData = new ArrayList<>();
    private ArrayList<String> TimeRecordData = new ArrayList<>();
    private final ArrayList<BluetoothDevice> BluetoothDevices = new ArrayList<>();
    private ArrayList<Entry> TemperatureData = new ArrayList<>();
    private ArrayList<Entry> PercentageData = new ArrayList<>();

    private byte[] mValue;
    private int recordedSeconds = 0;
    private int entryCount = 0;
    private SharedPreferences mSharedPreferences;
    private BatInfoReceiver mBatInfoReceiver;

    public static final String ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE";
    public static final String ACTION_STOP_NOTIFICATION_SERVICE = "ACTION_STOP_NOTIFICATION_SERVICE";
    public static BluetoothGatt mBluetoothGatt;

    public LiveData<ArrayList<String>> getData(){
        return mData;
    }
    public LiveData<ArrayList<Entry>> getVoltageData() {
        return mVoltageData;
    }
    public LiveData<ArrayList<Entry>> getCurrentData() {
        return mCurrentData;
    }
    public LiveData<ArrayList<Entry>> getPowerData() {
        return mPowerData;
    }
    public LiveData<ArrayList<String>> getTimeRecordData() {
        return mTimeRecordData;
    }
    public LiveData<ArrayList<BluetoothDevice>> getBluetoothDevices(){
        return mBluetoothDevices;
    }
    public LiveData<ArrayList<Entry>> getTemperatureData(){
        return mTemperatureData;
    }
    public LiveData<ArrayList<Entry>> getPercentageData(){
        return mPercentageData;
    }

    private final ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("Scanning ", result.getDevice().toString() + " rssi " + result.getRssi());
            if (result.getDevice().getAddress().equals(BLEService.bluetooth_device_address)) {
                BLEService.this.scan(false);
                BLEService.this.connect(BLEService.bluetooth_device_address);
            }
            if (!BluetoothDevices.contains(result.getDevice())) {
                BluetoothDevices.add(result.getDevice());
                mBluetoothDevices.postValue(BluetoothDevices);
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (i2 == BluetoothProfile.STATE_DISCONNECTED) {
                BLEService.this.scan(true);
                BLEService.CONTENT_STATUS = false;
            } else if (i2 == BluetoothProfile.STATE_CONNECTED) {
                BLEService.mBluetoothGatt.discoverServices();
                BLEService.CONTENT_STATUS = true;
                SharedPreferences.Editor edit = BLEService.this.mSharedPreferences.edit();
                BLEService.bluetooth_device_address = bluetoothGatt.getDevice().getAddress();
                edit.putString("DEVICE_ADDRESS", BLEService.bluetooth_device_address);
                edit.apply();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            for (BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()) {
                for (final BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                    if (bluetoothGattCharacteristic.getUuid().equals(UUID.fromString(UUIDs.UUID_NOTIFY))) {
                        new Thread(() -> new Timer().schedule(new TimerTask() {
                            /* class com.tang.etest.e_test.Model.BLEService.AnonymousClass2.AnonymousClass1.AnonymousClass1 */

                            public void run() {
                                BLEService.this.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                            }
                        }, 1000)).start();
                    }
                }
            }
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(BLEService.this.getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            byte[] bArr = bluetoothGattCharacteristic.getValue();
            if (bArr.length >= 3) {
                if ((bArr[0] & 255) == 255 && bArr[2] == 1) {
                    mValue = bArr;
                }
                if ((bArr[0] & 255) != 255) {
                    mValue = UUIDs.concat(mValue, bArr);
                    if (mValue != null) {
                        if (mValue.length == 36) {
                            bArr = mValue;
                            ArrayList<String> data = new ArrayList<>();
                            StringBuilder dataBuilder = new StringBuilder();
                            String capacity;
                            String electricity;
                            String carbon;
                            String echarges;
                            String time;
                            String temperature;
                            String eprice;
                            float voltage = 0.0f;
                            float current = 0.0f;
                            float power = 0.0f;
                            int tempRawC = 0;
                            String timeHours;
                            String timeMinutes;
                            String timeSeconds;
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                            symbols.setDecimalSeparator('.');
                            DecimalFormat decimalFormat = new DecimalFormat("0.0");
                            decimalFormat.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat2 = new DecimalFormat("0.00");
                            decimalFormat2.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat3 = new DecimalFormat("0.000");
                            decimalFormat3.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat4 = new DecimalFormat("0.000000");
                            decimalFormat4.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat5 = new DecimalFormat("00.00");
                            decimalFormat5.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat6 = new DecimalFormat("000.0");
                            decimalFormat6.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat7 = new DecimalFormat("000.00");
                            decimalFormat7.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat8 = new DecimalFormat("000.000000");
                            decimalFormat8.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat9 = new DecimalFormat("0000.0");
                            decimalFormat9.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat10 = new DecimalFormat("0000.00");
                            decimalFormat10.setDecimalFormatSymbols(symbols);
                            DecimalFormat decimalFormat11 = new DecimalFormat("0000.000000");
                            decimalFormat11.setDecimalFormatSymbols(symbols);
                            data.add(String.valueOf(bArr[3]));
                            switch (bArr[3]) {
                                case DEVICE_AC:
                                    voltage = (float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 10.0d);
                                    data.add((decimalFormat6.format(voltage)));
                                    current = (float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 1000.0d);
                                    data.add((decimalFormat3.format(current)));
                                    power = (float) (((double) ((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256)) + (bArr[12] & 255))) / 10.0d);
                                    data.add((decimalFormat9.format(power)));
                                    String powerfactor = (decimalFormat2.format(((double) (((bArr[22] & 255) * 256) + (bArr[23] & 255))) / 1000.0d) + "PF");
                                    data.add(powerfactor);
                                    electricity = (decimalFormat7.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "kWh");
                                    data.add(electricity);
                                    String format = decimalFormat8.format((((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) * 0.997d);
                                    carbon = format.substring(0, format.length() + -4);
                                    data.add(carbon + "kg");
                                    String format2 = decimalFormat4.format((((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d) * (((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d));
                                    echarges = format2.substring(0, format2.length() + -4);
                                    data.add(echarges);
                                    String acfreq = ((((double) (((bArr[20] & 255) * 256) + (bArr[21] & 255))) / 10.0d) + "Hz");
                                    data.add(acfreq);
                                    tempRawC = ((bArr[24] & 255) * 256) + (bArr[25] & 255);
                                    temperature = (tempRawC + "℃/" + decimalFormat.format((((double) tempRawC) * 1.8d) + 32.0d) + "℉");
                                    data.add(temperature);
                                    eprice = (decimalFormat2.format(((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d));
                                    data.add(eprice);
                                    if (bArr[30] == 0) {
                                        data.add(getString(R.string.Long_black));
                                    } else if (bArr[30] == 60) {
                                        data.add(getString(R.string.Long_bright));
                                    } else {
                                        data.add(((int) bArr[30]) + getString(R.string.second));
                                    }
                                    dataBuilder.append("Voltage: ");
                                    dataBuilder.append(decimalFormat6.format(voltage)).append("V").append("\n");
                                    dataBuilder.append("Current: ");
                                    dataBuilder.append(decimalFormat3.format(current)).append("A").append("\n");
                                    dataBuilder.append("Power: ");
                                    dataBuilder.append(decimalFormat9.format(power)).append("W").append("\n");
                                    dataBuilder.append("Power Factor: ");
                                    dataBuilder.append(powerfactor).append("\n");
                                    dataBuilder.append("Electricity: ");
                                    dataBuilder.append(electricity).append("\n");
                                    dataBuilder.append("CO2: ");
                                    dataBuilder.append(carbon).append("kg").append("\n");
                                    dataBuilder.append("Electricity charges: ");
                                    dataBuilder.append(echarges).append("\n");
                                    dataBuilder.append("AC freq: ");
                                    dataBuilder.append(acfreq).append("\n");
                                    dataBuilder.append("Internal Temperature: ");
                                    dataBuilder.append(temperature).append("\n");
                                    dataBuilder.append("Elec. price setting: ");
                                    dataBuilder.append(eprice);
                                    break;
                                case DEVICE_DC:
                                    recordedSeconds++;
                                    voltage = (float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 10.0d);
                                    data.add((decimalFormat6.format(voltage)));
                                    current = (float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 1000.0d);
                                    data.add(decimalFormat3.format(current));
                                    power = voltage * current;
                                    String format3 = decimalFormat11.format(power);
                                    String substring3 = format3.substring(0, format3.length() + -5);
                                    data.add((substring3));
                                    capacity = (decimalFormat7.format(((double) ((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256)) + (bArr[12] & 255))) / 100.0d) + "Ah");
                                    data.add(capacity);
                                    electricity = (decimalFormat7.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "kWh");
                                    data.add(electricity);
                                    String format4 = decimalFormat8.format((((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) * 0.997d);
                                    carbon = format4.substring(0, format4.length() + -4);
                                    data.add(carbon + "kg");
                                    String format5 = decimalFormat4.format((((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d) * (((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d));
                                    echarges = format5.substring(0, format5.length() - 4);
                                    data.add(echarges);
                                    if (recordedSeconds / 3600 < 10) {
                                        timeHours = "00" + (recordedSeconds / 3600);
                                    } else if (recordedSeconds / 3600 < 100) {
                                        timeHours = "0" + (recordedSeconds / 3600);
                                    } else {
                                        timeHours = "" + (recordedSeconds / 3600);
                                    }
                                    if (recordedSeconds / 60 < 10) {
                                        timeMinutes = "0" + (recordedSeconds / 60);
                                    } else {
                                        timeMinutes = "" + (0 / 60);
                                    }
                                    if (recordedSeconds % 60 < 10) {
                                        timeSeconds = "0" + (recordedSeconds % 60);
                                    } else {
                                        timeSeconds = "" + (recordedSeconds % 60);
                                    }
                                    time = (timeHours + ":" + timeMinutes + ":" + timeSeconds);
                                    data.add(time);
                                    tempRawC = ((bArr[24] & 255) * 256) + (bArr[25] & 255);
                                    temperature = (tempRawC + "℃/" + decimalFormat.format((((double) tempRawC) * 1.8d) + 32.0d) + "℉");
                                    data.add(temperature);
                                    eprice = decimalFormat2.format(((double) ((((bArr[17] & 255) * 65536) + ((bArr[18] & 255) * 256)) + (bArr[19] & 255))) / 100.0d);
                                    data.add(eprice);
                                    if (bArr[30] == 0) {
                                        data.add(getString(R.string.Long_black));
                                    } else if (bArr[30] == 60) {
                                        data.add(getString(R.string.Long_bright));
                                    } else {
                                        data.add(((int) bArr[30]) + getString(R.string.second));
                                    }
                                    dataBuilder.append("Voltage: ");
                                    dataBuilder.append(decimalFormat6.format(voltage)).append("V").append("\n");
                                    dataBuilder.append("Current: ");
                                    dataBuilder.append(decimalFormat3.format(current)).append("A").append("\n");
                                    dataBuilder.append("Power: ");
                                    dataBuilder.append(substring3).append("W").append("\n");
                                    dataBuilder.append("Capacity: ");
                                    dataBuilder.append(capacity).append("\n");
                                    dataBuilder.append("Electricity: ");
                                    dataBuilder.append(electricity).append("\n");
                                    dataBuilder.append("CO2: ");
                                    dataBuilder.append(carbon).append("kg").append("\n");
                                    dataBuilder.append("Electricity charges: ");
                                    dataBuilder.append(echarges).append("\n");
                                    dataBuilder.append("Time record: ");
                                    dataBuilder.append(time).append("\n");
                                    dataBuilder.append("Internal Temperature: ");
                                    dataBuilder.append(temperature).append("\n");
                                    dataBuilder.append("Elec. price setting: ");
                                    dataBuilder.append(eprice);
                                    break;
                                case DEVICE_USB:
                                    voltage = (float) (((double) ((((bArr[4] & 255) * 65536) + ((bArr[5] & 255) * 256)) + (bArr[6] & 255))) / 100.0d);
                                    data.add((decimalFormat5.format(voltage)));
                                    float valueOf4 = (float) (((double) ((((bArr[7] & 255) * 65536) + ((bArr[8] & 255) * 256)) + (bArr[9] & 255))) / 100.0d);
                                    data.add(decimalFormat5.format(valueOf4));
                                    power = voltage * valueOf4;
                                    String format6 = decimalFormat11.format(power);
                                    String substring6 = format6.substring(0, format6.length() + -4);
                                    data.add((substring6));
                                    capacity = ReservedInt((((bArr[10] & 255) * 65536) + ((bArr[11] & 255) * 256) + (bArr[12] & 255)) + "") +
                                            "mAh";
                                    data.add(capacity);
                                    electricity = (decimalFormat10.format(((double) (((((bArr[13] & 255) * 16777216) + ((bArr[14] & 255) * 65536)) + ((bArr[15] & 255) * 256)) + (bArr[16] & 255))) / 100.0d) + "Wh");
                                    data.add(electricity);
                                    String dplus = (decimalFormat2.format(((double) (((bArr[19] & 255) * 256) + (bArr[20] & 255))) / 100.0d) + "V");
                                    data.add(dplus);
                                    String dminus = (decimalFormat2.format(((double) (((bArr[17] & 255) * 256) + (bArr[18] & 255))) / 100.0d) + "V");
                                    data.add(dminus);
                                    if (((bArr[23] & 255) * 256) + (bArr[24] & 255) < 10) {
                                        timeHours = "00" + (((bArr[23] & 255) * 256) + (bArr[24] & 255));
                                    } else if (((bArr[23] & 255) * 256) + (bArr[24] & 255) < 100) {
                                        timeHours = "0" + (((bArr[23] & 255) * 256) + (bArr[24] & 255));
                                    } else {
                                        timeHours = "" + (((bArr[23] & 255) * 256) + (bArr[24] & 255));
                                    }
                                    if (bArr[25] < 10) {
                                        timeMinutes = "0" + ((int) bArr[25]);
                                    } else {
                                        timeMinutes = "" + ((int) bArr[25]);
                                    }
                                    if (bArr[26] < 10) {
                                        timeSeconds = "0" + ((int) bArr[26]);
                                    } else {
                                        timeSeconds = "" + ((int) bArr[26]);
                                    }
                                    time = (timeHours + ":" + timeMinutes + ":" + timeSeconds);
                                    data.add(time);
                                    tempRawC = ((bArr[21] & 255) * 256) + (bArr[22] & 255);
                                    temperature = (tempRawC + "℃/" + decimalFormat.format((((double) tempRawC) * 1.8d) + 32.0d) + "℉");
                                    data.add(temperature);
                                    current = valueOf4;
                                    if (bArr[27] == 0) {
                                        data.add(getString(R.string.Long_black));
                                    } else if (bArr[27] == 60) {
                                        data.add(getString(R.string.Long_bright));
                                    } else {
                                        data.add(((int) bArr[27]) + getString(R.string.second));
                                    }
                                    dataBuilder.append("Voltage: ");
                                    dataBuilder.append(decimalFormat5.format(voltage)).append("V").append("\n");
                                    dataBuilder.append("Current: ");
                                    dataBuilder.append(decimalFormat5.format(valueOf4)).append("A").append("\n");
                                    dataBuilder.append("Power: ");
                                    dataBuilder.append(substring6).append("W").append("\n");
                                    dataBuilder.append("Capacity: ");
                                    dataBuilder.append(capacity).append("\n");
                                    dataBuilder.append("Electricity: ");
                                    dataBuilder.append(electricity).append("\n");
                                    dataBuilder.append("USB D+: ");
                                    dataBuilder.append(dplus).append("\n");
                                    dataBuilder.append("USB D-: ");
                                    dataBuilder.append(dminus).append("\n");
                                    dataBuilder.append("Time record: ");
                                    dataBuilder.append(time).append("\n");
                                    dataBuilder.append("Internal Temperature: ");
                                    dataBuilder.append(temperature);
                                    break;
                            }
                            updateNotification(dataBuilder.toString());
                            if (mSharedPreferences.getString("RECORD_TEMP", null).equals("TRUE")){
                                if (mSharedPreferences.getString("INTERNAL_TEMP", null).equals("TRUE")){
                                    TemperatureData.add(new Entry(entryCount, mBatInfoReceiver.get_temp()));
                                    Log.d("BLEService", "Adding temperature: "+mBatInfoReceiver.get_temp());
                                } else {
                                    TemperatureData.add(new Entry(entryCount, tempRawC));
                                    Log.d("BLEService", "Adding temperature: "+tempRawC);
                                }
                                mTemperatureData.postValue(TemperatureData);
                            }
                            if (mSharedPreferences.getString("RECORD_PERCENTAGE", null).equals("TRUE")){
                                if (mSharedPreferences.getString("INTERNAL_PERCENTAGE", null).equals("TRUE")){
                                    PercentageData.add(new Entry(entryCount, mBatInfoReceiver.get_percentage()));
                                } else {
                                    PercentageData.add(new Entry(entryCount, 0));
                                }
                                mPercentageData.postValue(PercentageData);
                            }
                            mData.postValue(data);
                            VoltageData.add(new Entry(entryCount, voltage));
                            CurrentData.add(new Entry(entryCount, current));
                            PowerData.add(new Entry(entryCount, power));
                            TimeRecordData.add(df.format(System.currentTimeMillis()));
                            mVoltageData.postValue(VoltageData);
                            mCurrentData.postValue(CurrentData);
                            mPowerData.postValue(PowerData);
                            mTimeRecordData.postValue(TimeRecordData);
                            entryCount++;
                        }
                    }
                }
            }
        }
    };

    public BLEService() {
    }

    public static void send(byte[] bArr) {
        BluetoothGattService service;
        if (CONTENT_STATUS && (service = mBluetoothGatt.getService(UUID.fromString(UUIDs.UUID_SERVICE))) != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(UUIDs.UUID_NOTIFY));
            characteristic.setValue(bArr);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void importList(ArrayList<ArrayList<String>> records) {
        int tempcount = 0;
        ArrayList<Entry> templist0 = new ArrayList<>();
        ArrayList<Entry> templist1 = new ArrayList<>();
        ArrayList<Entry> templist2 = new ArrayList<>();
        ArrayList<String> temptimeList = new ArrayList<>();
        for (int i = 0; i < records.size(); i++){
            templist0.add(new Entry(tempcount, Float.parseFloat(records.get(i).get(1))));
            templist1.add(new Entry(tempcount, Float.parseFloat(records.get(i).get(2))));
            templist2.add(new Entry(tempcount, Float.parseFloat(records.get(i).get(3))));
            temptimeList.add(records.get(i).get(0));
            tempcount++;
        }
        if (entryCount != 0) {
            for (int i = 0; i < entryCount; i++){
                templist0.add(new Entry(tempcount, VoltageData.get(i).getY()));
                templist1.add(new Entry(tempcount, CurrentData.get(i).getY()));
                templist2.add(new Entry(tempcount, PowerData.get(i).getY()));
                temptimeList.add(TimeRecordData.get(i));
                tempcount++;
            }
        }
        VoltageData = new ArrayList<>(templist0);
        CurrentData = new ArrayList<>(templist1);
        PowerData = new ArrayList<>(templist2);
        TimeRecordData = new ArrayList<>(temptimeList);
        entryCount = tempcount;
        mVoltageData.postValue(VoltageData);
        mCurrentData.postValue(CurrentData);
        mPowerData.postValue(PowerData);
        mTimeRecordData.postValue(TimeRecordData);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBluetooth();
        Repository.instance().addData(getData());
        Repository.instance().addVoltageData(getVoltageData());
        Repository.instance().addCurrentData(getCurrentData());
        Repository.instance().addPowerData(getPowerData());
        Repository.instance().addTimeRecordData(getTimeRecordData());
        Repository.instance().addBluetoothDevice(getBluetoothDevices());
        Repository.instance().addTemperatureData(getTemperatureData());
        Repository.instance().addPercentageData(getPercentageData());
        mBatInfoReceiver = new BatInfoReceiver();
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        bluetooth_device_address = this.mSharedPreferences.getString("DEVICE_ADDRESS", null);
    }

    @Override
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
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt.close();
                    }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("test", "ServiceDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("test", "Bind");
        return this.binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("test", "Unbind");
        return super.onUnbind(intent);
    }

    private void initBluetooth() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void scan(boolean z) {
        if (mAdapter.isEnabled()) {
            BluetoothLeScanner bluetoothLeScanner = mAdapter.getBluetoothLeScanner();
            if (z) {
                Log.i("test", "Scan started");
                bluetoothLeScanner.startScan(this.mLeScanCallback);
                return;
            }
            Log.i("test", "Scan stopped");
            bluetoothLeScanner.stopScan(this.mLeScanCallback);
        }
    }

    public void connect(String addr) {
        Log.i("Connecting ", addr);
        Context applicationContext = getApplicationContext();
        Toast.makeText(applicationContext, "Connecting: " + addr, Toast.LENGTH_SHORT).show();
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = mAdapter.getRemoteDevice(addr).connectGatt(this, false, this.mGattCallback);
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

    private String ReservedInt(String str) {
        int intValue = Integer.parseInt(str);
        String str2 = intValue + "";
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
    }

    public class MyBinder extends Binder {
        public MyBinder() {
        }

        public BLEService getService() {
            return BLEService.this;
        }
    }

    public static class BatInfoReceiver extends BroadcastReceiver {
        int temp = 0;
        float percentage = 0;

        float get_temp() {
            return (float) (temp / 10);
        }

        float get_percentage() {
            return percentage;
        }
        @Override
        public void onReceive(Context arg0, Intent intent){
            temp = (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0));
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            percentage = level * 100 / (float)scale;
        }
    }
}
