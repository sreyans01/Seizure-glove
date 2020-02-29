package com.sih.seizureglove.OtherFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sih.seizureglove.R;

public class HomeFragment extends Fragment {
    View fragmentView;
    Button firstaid_btn,patienthistory_btn,notifications_btn,ambulance_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_home,container,false);

        return super.onCreateView(inflater, container, savedInstanceState);

    }
}
