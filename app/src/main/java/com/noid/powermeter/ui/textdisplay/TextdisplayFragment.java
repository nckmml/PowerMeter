package com.noid.powermeter.ui.textdisplay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.noid.powermeter.MainActivity;
import com.noid.powermeter.databinding.FragmentTextdisplayBinding;

public class TextdisplayFragment extends Fragment {

    private TextdisplayViewModel textdisplayViewModel;
    private FragmentTextdisplayBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        textdisplayViewModel =
                new ViewModelProvider(this).get(TextdisplayViewModel.class);

        binding = FragmentTextdisplayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}