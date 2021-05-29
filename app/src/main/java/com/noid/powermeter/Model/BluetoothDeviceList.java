package com.noid.powermeter.Model;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.noid.powermeter.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceList extends AppCompatActivity implements View.OnClickListener {
    private BaseAdapter adapter;
    private Button back;
    private ServiceConnection conn = new ServiceConnection() {
        /* class com.tang.etest.e_test.Model.ScanActivity.AnonymousClass3 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothDeviceList.this.mBleService = ((BLEService.MyBinder) iBinder).getService();
            Log.i("Kathy", "ActivityA - onServiceConnected");
            BluetoothDeviceList.this.mBleService.scan(true);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("Kathy", "ActivityA - onServiceDisconnected");
        }
    };
    private TextView deviceAddress;
    private TextView deviceName;
    private List<BluetoothDevice> devices = new ArrayList();
    private ListView lv_device;
    private BLEService mBleService;
    private Button next;
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        /* class com.tang.etest.e_test.Model.ScanActivity.AnonymousClass1 */

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) BluetoothDeviceList.this.devices.get(i);
            if (bluetoothDevice != null) {
                BluetoothDeviceList.this.mBleService.scan(false);
                BluetoothDeviceList.this.mBleService.connect(bluetoothDevice.getAddress());
                BluetoothDeviceList.this.finish();
            }
        }
    };
    private value receiver;

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.SupportActivity, android.support.v4.app.FragmentActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_bluetooth_device_list);
        initView();
        this.receiver = new value();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.BLUETOOTH_DEVICE);
        registerReceiver(this.receiver, intentFilter);
        bindService(new Intent(this, BLEService.class), this.conn, Context.BIND_AUTO_CREATE);
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity
    public void onDestroy() {
        unbindService(this.conn);
        unregisterReceiver(this.receiver);
        this.mBleService.scan(false);
        super.onDestroy();
    }

    private void initView() {
        this.lv_device = (ListView) findViewById(R.id.List_device);
        scanDevice();
        this.lv_device.setOnItemClickListener(this.onItemClickListener);
        this.back = (Button) findViewById(R.id.back);
        this.back.setOnClickListener(this);
    }

    private void scanDevice() {
        Log.i("DEBUG", "Scan eingeleitet");
        this.adapter = new BaseAdapter() {
            /* class com.tang.etest.e_test.Model.ScanActivity.AnonymousClass2 */

            public Object getItem(int i) {
                return null;
            }

            public long getItemId(int i) {
                return 0;
            }

            public int getCount() {
                return BluetoothDeviceList.this.devices.size();
            }

            public View getView(int i, View view, ViewGroup viewGroup) {
                LayoutInflater layoutInflater = BluetoothDeviceList.this.getLayoutInflater();
                if (view == null) {
                    view = layoutInflater.inflate(R.layout.listitem_device, (ViewGroup) null);
                } else {
                    Log.i("info", "Regenerating Cache" + i);
                }
                BluetoothDeviceList.this.deviceName = (TextView) view.findViewById(R.id.device_name);
                BluetoothDeviceList.this.deviceName.setText(((BluetoothDevice) BluetoothDeviceList.this.devices.get(i)).getName());
                BluetoothDeviceList.this.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                BluetoothDeviceList.this.deviceAddress.setText(((BluetoothDevice) BluetoothDeviceList.this.devices.get(i)).getAddress());
                return view;
            }
        };
        this.lv_device.setAdapter((ListAdapter) this.adapter);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            finish();
        }
    }

    public class value extends BroadcastReceiver {
        public value() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Install:", intent.getExtras().get(BLEService.BLUETOOTH_DEVICE) + "");
            if (((action.hashCode() == -277749465 && action.equals(BLEService.BLUETOOTH_DEVICE)) ? (char) 0 : 65535) == 0) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getExtras().get(BLEService.BLUETOOTH_DEVICE);
                if (!BluetoothDeviceList.this.devices.contains(bluetoothDevice)) {
                    BluetoothDeviceList.this.devices.add(bluetoothDevice);
                    BluetoothDeviceList.this.adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
