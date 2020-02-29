package com.sih.seizureglove.PatientHistory;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sih.seizureglove.R;

public class PatientHistoryRecyclerAdapter extends RecyclerView.Adapter<PatientHistoryRecyclerAdapter.PatientHistoryViewolder> {


    @NonNull
    @Override
    public PatientHistoryViewolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PatientHistoryViewolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class PatientHistoryViewolder extends RecyclerView.ViewHolder {


        TextView patientname,desc;
        public PatientHistoryViewolder(@NonNull View itemView) {
            super(itemView);
            patientname = itemView.findViewById(R.id.patientname);
            desc = itemView.findViewById(R.id.description);
        }
    }
}
