package com.noid.powermeter.ui.export;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.noid.powermeter.databinding.FragmentExportBinding;

public class ExportFragment extends Fragment {

    private FragmentExportBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExportViewModel exportViewModel = new ViewModelProvider(this).get(ExportViewModel.class);

        binding = FragmentExportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext());
        final TextView textView = binding.textExport;
        final Button exportButton = binding.buttonExport;
        final CheckBox cb_recTemp = binding.checkboxRecordTemp;
        final CheckBox cb_recPercentage = binding.checkboxRecordPercentage;
        final CheckBox cb_useIntTemp = binding.checkboxUseInternalTemp;
        final CheckBox cb_useIntPercentage = binding.checkboxUseInternalPercentage;
        if (mSharedPreferences.getString("RECORD_TEMP", null).equals("TRUE"))
            cb_recTemp.setChecked(true);
        if (mSharedPreferences.getString("RECORD_PERCENTAGE", null).equals("TRUE"))
            cb_recPercentage.setChecked(true);
        if (mSharedPreferences.getString("INTERNAL_TEMP", null).equals("TRUE"))
            cb_useIntTemp.setChecked(true);
        if (mSharedPreferences.getString("INTERNAL_PERCENTAGE", null).equals("TRUE"))
            cb_useIntPercentage.setChecked(true);
        exportViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        exportViewModel.getData().observe(getViewLifecycleOwner(), s -> exportButton.setEnabled(true));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}