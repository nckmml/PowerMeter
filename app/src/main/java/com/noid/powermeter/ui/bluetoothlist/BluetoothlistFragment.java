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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentBluetoothlistBinding;
import com.noid.powermeter.ui.textdisplay.TextdisplayFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BluetoothlistFragment extends Fragment {

    private final ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private FragmentBluetoothlistBinding binding;
    private value receiver;
    private RecyclerViewAdapter adapter;
    private BLEService mBleService;
    private final ServiceConnection conn = new ServiceConnection() {
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
    private RecyclerView lv_device;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBluetoothlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        this.lv_device = binding.ListDevice;
        this.lv_device.setAdapter(new RecyclerViewAdapter(devices));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        this.lv_device.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this.lv_device.getContext(), mLayoutManager.getOrientation());
        this.lv_device.addItemDecoration(mDividerItemDecoration);
        scanDevice();
        this.receiver = new value();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.BLUETOOTH_DEVICE);
        requireActivity().registerReceiver(this.receiver, intentFilter);
        requireActivity().bindService(new Intent(getActivity(), BLEService.class), this.conn, Context.BIND_AUTO_CREATE);
        return root;
    }

    private void scanDevice() {
        Log.i("DEBUG", "Scan eingeleitet");
        this.adapter = new RecyclerViewAdapter(devices) {
            private final RecyclerView.OnClickListener mOnClickListener = new RecyclerView.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int i = lv_device.getChildLayoutPosition(view);
                    BluetoothDevice bluetoothDevice = BluetoothlistFragment.this.devices.get(i);
                    if (bluetoothDevice != null) {
                        BluetoothlistFragment.this.mBleService.scan(false);
                        BluetoothlistFragment.this.mBleService.connect(bluetoothDevice.getAddress());
                        devices.clear();
                        FragmentManager fragmentManager = getParentFragmentManager();
                        Fragment textFragment = new TextdisplayFragment();
                        fragmentManager.beginTransaction().replace(R.id.fragment_bluetoothlist, textFragment, textFragment.getTag()).commit();
                    }
                }
            };

            @Override
            public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
                holder.deviceName.setText(BluetoothlistFragment.this.devices.get(position).getName());
                holder.deviceAddress.setText(BluetoothlistFragment.this.devices.get(position).getAddress());
            }

            @NotNull
            @Override
            public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.listitem_device, parent, false);
                view.setOnClickListener(mOnClickListener);
                return new ViewHolder(view);
            }

            public long getItemId(int i) {
                return 0;
            }

            public int getItemCount() {
                return BluetoothlistFragment.this.devices.size();
            }
        };
        this.lv_device.setAdapter(this.adapter);
    }

    @Override
    public void onDestroyView() {
        requireActivity().unbindService(this.conn);
        requireActivity().unregisterReceiver(this.receiver);
        this.mBleService.scan(false);
        super.onDestroyView();
        binding = null;
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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        ArrayList<BluetoothDevice> devices;

        public RecyclerViewAdapter(ArrayList<BluetoothDevice> devices) {
            this.devices = devices;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater
                            .from(requireContext())
                            .inflate(R.layout.listitem_device, parent, false)
            );
        }

        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView deviceName;
            TextView deviceAddress;

            public ViewHolder(@NonNull View view) {
                super(view);
                deviceName = view.findViewById(R.id.device_name);
                deviceAddress = view.findViewById(R.id.device_address);
            }
        }
    }
}