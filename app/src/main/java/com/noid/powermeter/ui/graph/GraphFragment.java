package com.noid.powermeter.ui.graph;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;

import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentGraphBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class GraphFragment extends Fragment {

    private LineChart chart;
    private FragmentGraphBinding binding;
    private LineDataSet set1, set2, set3;
    private ArrayList<String> timeRecordData;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        GraphViewModel graphViewModel = new ViewModelProvider(this).get(GraphViewModel.class);
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        chart = binding.chart1;
        chart.getAxisLeft().setEnabled(false);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(MaterialColors.getColor(container.getRootView(), R.attr.colorOnSurface));
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularityEnabled(true);
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(MaterialColors.getColor(container.getRootView(), R.attr.colorOnSurface));
        chart.getDescription().setEnabled(false);
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(MaterialColors.getColor(container.getRootView(), R.attr.colorOnSurface));
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        final Observer<ArrayList<Entry>> voltageObserver = newData -> {
            set1 = new LineDataSet(newData, "Voltage");
            set1.setColor(MaterialColors.getColor(root.getRootView(), R.attr.colorPrimary));
            set1.setLineWidth(1.5f);
            set1.setDrawValues(false);
            set1.setDrawCircles(false);
            set1.setMode(LineDataSet.Mode.LINEAR);
            set1.setDrawFilled(false);
        };
        final Observer<ArrayList<Entry>> currentObserver = newData -> {
            set2 = new LineDataSet(newData, "Current");
            set2.setColor(MaterialColors.getColor(root.getRootView(), R.attr.colorSecondary));
            set2.setLineWidth(1.5f);
            set2.setDrawValues(false);
            set2.setDrawCircles(false);
            set2.setMode(LineDataSet.Mode.LINEAR);
            set2.setDrawFilled(false);
        };
        final Observer<ArrayList<Entry>> powerObserver = newData -> {
            set3 = new LineDataSet(newData, "Power");
            set3.setColor(MaterialColors.getColor(root.getRootView(), R.attr.colorError));
            set3.setLineWidth(1.5f);
            set3.setDrawValues(false);
            set3.setDrawCircles(false);
            set3.setMode(LineDataSet.Mode.LINEAR);
            set3.setDrawFilled(false);
        };
        final Observer<ArrayList<String>> timeRecordObserver = newData -> {
            timeRecordData = new ArrayList<>(newData);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return timeRecordData.get((int) value);
                }
            });
            redrawChart();
        };
        graphViewModel.getVoltageData().observe(getViewLifecycleOwner(), voltageObserver);
        graphViewModel.getCurrentData().observe(getViewLifecycleOwner(), currentObserver);
        graphViewModel.getPowerData().observe(getViewLifecycleOwner(), powerObserver);
        graphViewModel.getTimeRecordData().observe(getViewLifecycleOwner(), timeRecordObserver);
        return root;
    }

    public void redrawChart(){
        chart = binding.chart1;
        LineData data = new LineData(set1, set2, set3);
        chart.setData(data);
        chart.invalidate();
    }
}