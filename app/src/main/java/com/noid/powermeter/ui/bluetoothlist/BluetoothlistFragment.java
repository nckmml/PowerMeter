package com.noid.powermeter.ui.bluetoothlist;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentBluetoothlistBinding;

import java.util.ArrayList;
import java.util.List;

public class BluetoothlistFragment extends Fragment implements View.OnClickListener{

    private BluetoothlistViewModel bluetoothlistViewModel;
    private FragmentBluetoothlistBinding binding;
    private value receiver;
    private List<BluetoothDevice> devices = new ArrayList();
    private BaseAdapter adapter;
    private BLEService mBleService;
    private ListView lv_device;
    private TextView deviceAddress;
    private TextView deviceName;
    private Button back;

    private ServiceConnection conn = new ServiceConnection() {
        /* class com.tang.etest.e_test.Model.ScanActivity.AnonymousClass3 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothlistFragment.this.mBleService = ((BLEService.MyBinder) iBinder).getService();
            Log.i("Kathy", "ActivityA - onServiceConnected");
            BluetoothlistFragment.this.mBleService.scan(true);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("Kathy", "ActivityA - onServiceDisconnected");
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        /* class com.tang.etest.e_test.Model.ScanActivity.AnonymousClass1 */

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) BluetoothlistFragment.this.devices.get(i);
            if (bluetoothDevice != null) {
                BluetoothlistFragment.this.mBleService.scan(false);
                BluetoothlistFragment.this.mBleService.connect(bluetoothDevice.getAddress());
                //BluetoothlistFragment.this.finish();
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bluetoothlistViewModel =
                new ViewModelProvider(this).get(BluetoothlistViewModel.class);

        binding = FragmentBluetoothlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*
        final TextView textView = binding.textBluetoothlist;
        bluetoothlistViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
         */
        this.lv_device = binding.ListDevice;
        scanDevice();
        this.lv_device.setOnItemClickListener(this.onItemClickListener);
        this.receiver = new value();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.BLUETOOTH_DEVICE);
        getActivity().registerReceiver(this.receiver, intentFilter);
        getActivity().bindService(new Intent(getActivity(), BLEService.class), this.conn, Context.BIND_AUTO_CREATE);
        return root;
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
                return BluetoothlistFragment.this.devices.size();
            }

            public View getView(int i, View view, ViewGroup viewGroup) {
                LayoutInflater layoutInflater = BluetoothlistFragment.this.getLayoutInflater();
                if (view == null) {
                    view = layoutInflater.inflate(R.layout.listitem_device, (ViewGroup) null);
                } else {
                    Log.i("info", "Regenerating Cache" + i);
                }
                BluetoothlistFragment.this.deviceName = (TextView) view.findViewById(R.id.device_name);
                BluetoothlistFragment.this.deviceName.setText(((BluetoothDevice) BluetoothlistFragment.this.devices.get(i)).getName());
                BluetoothlistFragment.this.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                BluetoothlistFragment.this.deviceAddress.setText(((BluetoothDevice) BluetoothlistFragment.this.devices.get(i)).getAddress());
                return view;
            }
        };
        this.lv_device.setAdapter((ListAdapter) this.adapter);
    }

    @Override
    public void onDestroyView() {
        getActivity().unbindService(this.conn);
        getActivity().unregisterReceiver(this.receiver);
        this.mBleService.scan(false);
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {

    }

    public class value extends BroadcastReceiver {
        public value() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Install:", intent.getExtras().get(BLEService.BLUETOOTH_DEVICE) + "");
            if (((action.hashCode() == -277749465 && action.equals(BLEService.BLUETOOTH_DEVICE)) ? (char) 0 : 65535) == 0) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getExtras().get(BLEService.BLUETOOTH_DEVICE);
                if (!BluetoothlistFragment.this.devices.contains(bluetoothDevice)) {
                    BluetoothlistFragment.this.devices.add(bluetoothDevice);
                    BluetoothlistFragment.this.adapter.notifyDataSetChanged();
                }
            }
        }
    }
}