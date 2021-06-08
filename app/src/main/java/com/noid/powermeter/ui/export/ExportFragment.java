package com.noid.powermeter.ui.export;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.noid.powermeter.databinding.FragmentExportBinding;

public class ExportFragment extends Fragment {

    private FragmentExportBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExportViewModel exportViewModel = new ViewModelProvider(this).get(ExportViewModel.class);

        binding = FragmentExportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textExport;
        final Button exportButton = binding.buttonExport;
        exportViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        exportViewModel.getData().observe(getViewLifecycleOwner(), s -> exportButton.setEnabled(true));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}