package com.noid.powermeter.ui.graph;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;
import com.noid.powermeter.MainActivity;
import com.noid.powermeter.Model.BLEService;
import com.noid.powermeter.R;
import com.noid.powermeter.databinding.FragmentGraphBinding;
import com.noid.powermeter.ui.textdisplay.TextdisplayFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphFragment extends Fragment {

    private LineChart chart;
    private FragmentGraphBinding binding;

    value receiver = new value();

    public GraphFragment() {
    }

    public static GraphFragment newInstance(String param1, String param2) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(receiver);
        binding = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chart = binding.chart1;
        chart.getDescription().setEnabled(false);
        LineDataSet set1, set2, set3;
        MainActivity main = (MainActivity) getActivity();
        if (main.mBound == true) {
            set1 = new LineDataSet(main.getRawRecordData().get(0), "Voltage");
            set2 = new LineDataSet(main.getRawRecordData().get(1), "Current");
            set3 = new LineDataSet(main.getRawRecordData().get(2), "Power");
            set1.setColor(MaterialColors.getColor(view, R.attr.colorPrimary));
            set1.setLineWidth(1.5f);
            set1.setDrawValues(false);
            set1.setDrawCircles(false);
            set1.setMode(LineDataSet.Mode.LINEAR);
            set1.setDrawFilled(false);
            set2.setColor(MaterialColors.getColor(view, R.attr.colorSecondary));
            set2.setLineWidth(1.5f);
            set2.setDrawValues(false);
            set2.setDrawCircles(false);
            set2.setMode(LineDataSet.Mode.LINEAR);
            set2.setDrawFilled(false);
            set3.setColor(MaterialColors.getColor(view, R.attr.colorError));
            set3.setLineWidth(1.5f);
            set3.setDrawValues(false);
            set3.setDrawCircles(false);
            set3.setMode(LineDataSet.Mode.LINEAR);
            set3.setDrawFilled(false);
            LineData data = new LineData(set1, set2, set3);
            chart.setData(data);
            Legend l = chart.getLegend();
            l.setForm(Legend.LegendForm.LINE);
            l.setTextSize(11f);
            l.setTextColor(MaterialColors.getColor(view, R.attr.colorOnSurface));
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            chart.getAxisLeft().setEnabled(false);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setTextColor(MaterialColors.getColor(view, R.attr.colorOnSurface));
            rightAxis.setDrawGridLines(true);
            rightAxis.setGranularityEnabled(true);

            XAxis xAxis = chart.getXAxis();
            xAxis.setTextColor(MaterialColors.getColor(view, R.attr.colorOnSurface));
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    ArrayList<ArrayList<String>> templist = new ArrayList<>(main.getRecordData());
                    return templist.get((int) value).get(0);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ALL_VALUE);
        intentFilter.addAction(BLEService.CONTENT_DEVICE);
        getActivity().registerReceiver(receiver, intentFilter);
        return root;
    }

    public void redrawChart(){
        LineDataSet set1, set2, set3;
        MainActivity main = (MainActivity) getActivity();
        View view = binding.getRoot();
        if (main.mBound == true) {
            set1 = new LineDataSet(main.getRawRecordData().get(0), "Voltage");
            set2 = new LineDataSet(main.getRawRecordData().get(1), "Current");
            set3 = new LineDataSet(main.getRawRecordData().get(2), "Power");
            set1.setColor(MaterialColors.getColor(view, R.attr.colorPrimary));
            set1.setLineWidth(1.5f);
            set1.setDrawValues(false);
            set1.setDrawCircles(false);
            set1.setMode(LineDataSet.Mode.LINEAR);
            set1.setDrawFilled(false);
            set2.setColor(MaterialColors.getColor(view, R.attr.colorSecondary));
            set2.setLineWidth(1.5f);
            set2.setDrawValues(false);
            set2.setDrawCircles(false);
            set2.setMode(LineDataSet.Mode.LINEAR);
            set2.setDrawFilled(false);
            set3.setColor(MaterialColors.getColor(view, R.attr.colorError));
            set3.setLineWidth(1.5f);
            set3.setDrawValues(false);
            set3.setDrawCircles(false);
            set3.setMode(LineDataSet.Mode.LINEAR);
            set3.setDrawFilled(false);
            LineData data = new LineData(set1, set2, set3);
            chart.setData(data);
            Legend l = chart.getLegend();
            l.setForm(Legend.LegendForm.LINE);
            l.setTextSize(11f);
            l.setTextColor(MaterialColors.getColor(view, R.attr.colorOnSurface));
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            chart.getAxisLeft().setEnabled(false);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setTextColor(MaterialColors.getColor(view, R.attr.colorOnSurface));
            rightAxis.setDrawGridLines(true);
            rightAxis.setGranularityEnabled(true);

            XAxis xAxis = chart.getXAxis();
            xAxis.setTextColor(MaterialColors.getColor(view, R.attr.colorOnSurface));
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    ArrayList<String> templist = new ArrayList<>(main.getTimeRecordData());
                    return templist.get((int) value);
                }
            });
        }
        chart.invalidate();
    }

    public class value extends BroadcastReceiver {
        public value() {
        }
        public void onReceive(Context context, Intent intent) {
            char c = 65535;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode == -678816493 && action.equals(BLEService.ALL_VALUE)) {
                    HashMap<String, String> datamap = (HashMap<String, String>)intent.getSerializableExtra(BLEService.ALL_VALUE);
                    if (datamap.size() > 0)
                        redrawChart();
            }
        }
    }
}