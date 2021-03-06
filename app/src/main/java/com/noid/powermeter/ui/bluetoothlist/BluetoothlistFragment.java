package com.noid.powermeter.ui.bluetoothlist;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
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

    private ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private FragmentBluetoothlistBinding binding;
    private RecyclerViewAdapter adapter;
    private BLEService mBleService;
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothlistFragment.this.mBleService = ((BLEService.MyBinder) iBinder).getService();
            BluetoothlistFragment.this.mBleService.scan(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private RecyclerView rv_device;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BluetoothlistViewModel bluetoothlistViewModel = new BluetoothlistViewModel();
        binding = FragmentBluetoothlistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        this.rv_device = binding.ListDevice;
        this.rv_device.setAdapter(new RecyclerViewAdapter(devices));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        this.rv_device.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(this.rv_device.getContext(), mLayoutManager.getOrientation());
        this.rv_device.addItemDecoration(mDividerItemDecoration);
        scanDevice();
        requireActivity().bindService(new Intent(getActivity(), BLEService.class), this.conn, Context.BIND_AUTO_CREATE);
        final Observer<ArrayList<BluetoothDevice>> bluetoothObserver = newData -> {
            devices = newData;
            adapter.notifyDataSetChanged();
        };
        bluetoothlistViewModel.getBluetoothDevices().observe(getViewLifecycleOwner(), bluetoothObserver);
        return root;
    }

    private void scanDevice() {
        this.adapter = new RecyclerViewAdapter(devices) {
            private final RecyclerView.OnClickListener mOnClickListener = new RecyclerView.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int i = rv_device.getChildLayoutPosition(view);
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
                if (BluetoothlistFragment.this.devices.get(position).getName() != null)
                    holder.deviceName.setText(BluetoothlistFragment.this.devices.get(position).getName());
                else
                    holder.deviceName.setText("<unknown>");
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
        this.rv_device.setAdapter(this.adapter);
    }

    @Override
    public void onDestroyView() {
        requireActivity().unbindService(this.conn);
        this.mBleService.scan(false);
        super.onDestroyView();
        binding = null;
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

        @Override
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