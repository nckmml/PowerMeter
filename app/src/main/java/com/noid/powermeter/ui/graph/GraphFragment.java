package com.noid.powermeter.ui.graph;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.noid.powermeter.MainActivity;
import com.noid.powermeter.databinding.FragmentGraphBinding;

public class GraphFragment extends Fragment {

    private LineChart chart;
    private FragmentGraphBinding binding;

    public GraphFragment() {
    }

    public static GraphFragment newInstance(String param1, String param2) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        chart = binding.chart1;
        chart.getDescription().setEnabled(false);
        LineDataSet set1, set2, set3;
        MainActivity main = (MainActivity) getActivity();
        assert main != null;
        set1 = new LineDataSet(main.getRawRecordData().get(0), "Voltage");
        set2 = new LineDataSet(main.getRawRecordData().get(1), "Current");
        set3 = new LineDataSet(main.getRawRecordData().get(2), "Power");
        set1.setColor(Color.RED);
        set1.setLineWidth(0.5f);
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);
        set2.setColor(Color.GREEN);
        set2.setLineWidth(0.5f);
        set2.setDrawValues(false);
        set2.setDrawCircles(false);
        set2.setMode(LineDataSet.Mode.LINEAR);
        set2.setDrawFilled(false);
        set3.setColor(Color.BLUE);
        set3.setLineWidth(0.5f);
        set3.setDrawValues(false);
        set3.setDrawCircles(false);
        set3.setMode(LineDataSet.Mode.LINEAR);
        set3.setDrawFilled(false);
        LineData data = new LineData(set1, set2, set3);
        chart.setData(data);
        chart.animateX(1500);
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        chart.getAxisLeft().setEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(ColorTemplate.getHoloBlue());
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularityEnabled(true);
        return root;
    }
}