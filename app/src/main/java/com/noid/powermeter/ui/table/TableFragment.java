package com.noid.powermeter.ui.table;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentTableBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TableFragment extends Fragment {

    private RecyclerView rv_records;
    private RecyclerViewAdapter adapter;

    private final ArrayList<ArrayList> records = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        TableViewModel mViewModel = new ViewModelProvider(this).get(TableViewModel.class);
        com.noid.powermeter.databinding.FragmentTableBinding binding = FragmentTableBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        rv_records = binding.ListRecord;
        rv_records.setAdapter(new RecyclerViewAdapter(records));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rv_records.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(rv_records.getContext(), mLayoutManager.getOrientation());
        rv_records.addItemDecoration(mDividerItemDecoration);
        adapter = new RecyclerViewAdapter(records) {
            @Override
            public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
                holder.recordTime.setText((String)records.get(0).get(position));
                holder.recordVoltage.setText(((Entry) records.get(1).get(position)).getY() +"V");
                holder.recordCurrent.setText(((Entry) records.get(2).get(position)).getY() +"A");
                holder.recordPower.setText(((Entry) records.get(3).get(position)).getY() +"W");
            }
            @NotNull
            @Override
            public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.listitem_recordentry, parent, false);
                return new ViewHolder(view);
            }

            public long getItemId(int i) {
                return 0;
            }

            public int getItemCount() {
                return records.get(0).size();
            }
        };
        rv_records.setAdapter(adapter);
        records.add(new ArrayList<>());
        records.add(new ArrayList<>());
        records.add(new ArrayList<>());
        records.add(new ArrayList<>());
        final Observer<ArrayList<String>> timeRecordObserver = newData -> {
            records.set(0, newData);
        };
        mViewModel.getTimeRecordData().observe(getViewLifecycleOwner(), timeRecordObserver);
        final Observer<ArrayList<Entry>> voltageObserver = newData -> {
            records.set(1, newData);
        };
        mViewModel.getVoltageData().observe(getViewLifecycleOwner(), voltageObserver);
        final Observer<ArrayList<Entry>> currentObserver = newData -> {
            records.set(2, newData);
        };
        mViewModel.getCurrentData().observe(getViewLifecycleOwner(), currentObserver);
        final Observer<ArrayList<Entry>> powerObserver = newData -> {
            records.set(3, newData);
            adapter.notifyDataSetChanged();
            rv_records.scrollToPosition(records.get(0).size()-1);
        };
        mViewModel.getPowerData().observe(getViewLifecycleOwner(), powerObserver);

        if (records.size() != 0)
            Log.d("TableFragment", "record size "+records.get(0).size());
        return root;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        ArrayList<ArrayList> records;

        public RecyclerViewAdapter(ArrayList<ArrayList> records) {
            this.records = records;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutInflater
                            .from(requireContext())
                            .inflate(R.layout.listitem_recordentry, parent, false)
            );
        }

        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView recordTime;
            TextView recordVoltage;
            TextView recordCurrent;
            TextView recordPower;

            public ViewHolder(@NonNull View view) {
                super(view);
                recordTime = view.findViewById(R.id.record_time);
                recordVoltage = view.findViewById(R.id.record_voltage);
                recordCurrent = view.findViewById(R.id.record_current);
                recordPower = view.findViewById(R.id.record_power);
            }
        }
    }
}