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

    private TableViewModel mViewModel;
    private RecyclerView rv_records;
    private RecyclerViewAdapter adapter;
    private final ArrayList<ArrayList> records = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(TableViewModel.class);
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
        final Observer<ArrayList<String>> timeObserver = newData -> {};
        mViewModel.getTimeRecordData().observe(getViewLifecycleOwner(), timeObserver);
        if (mViewModel.getTimeRecordData().getValue() != null) {
            Log.d("TableFragment", "getData.getValue != null");
            records.add(getTimeRecordData());
            records.add(getVoltageData());
            records.add(getCurrentData());
            records.add(getPowerData());
        } else {
            Log.d("TableFragment", "getData.getValue = null");
            records.add(new ArrayList());
            records.add(new ArrayList());
            records.add(new ArrayList());
            records.add(new ArrayList());
        }
        if (records.size() != 0)
            Log.d("TableFragment", "record size "+records.get(0).size());
        final Observer<ArrayList<String>> dataObserver = newData -> {
            adapter.notifyDataSetChanged();
            rv_records.scrollToPosition(records.get(0).size()-1);
        };
        final Observer<ArrayList<Entry>> entryObserver = entries -> {};
        mViewModel.getVoltageData().observe(getViewLifecycleOwner(), entryObserver);
        mViewModel.getCurrentData().observe(getViewLifecycleOwner(), entryObserver);
        mViewModel.getPowerData().observe(getViewLifecycleOwner(), entryObserver);
        mViewModel.getData().observe(getViewLifecycleOwner(), dataObserver);
        return root;
    }

    private ArrayList<Entry> getVoltageData(){
        return mViewModel.getVoltageData().getValue();
    }

    private ArrayList<Entry> getCurrentData(){
        return mViewModel.getCurrentData().getValue();
    }

    private ArrayList<Entry> getPowerData(){
        return mViewModel.getPowerData().getValue();
    }

    private ArrayList<String> getTimeRecordData(){
        return mViewModel.getTimeRecordData().getValue();
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