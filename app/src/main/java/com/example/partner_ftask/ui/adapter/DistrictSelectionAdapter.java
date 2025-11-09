package com.example.partner_ftask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.District;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistrictSelectionAdapter extends RecyclerView.Adapter<DistrictSelectionAdapter.ViewHolder> {

    private List<District> districts;
    private Set<Long> selectedIds;

    public DistrictSelectionAdapter(List<District> districts) {
        this.districts = districts != null ? districts : new ArrayList<>();
        this.selectedIds = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_district_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        District district = districts.get(position);
        holder.tvDistrictName.setText(district.getName());
        holder.checkboxDistrict.setChecked(selectedIds.contains(district.getId()));

        holder.itemView.setOnClickListener(v -> {
            boolean isChecked = !holder.checkboxDistrict.isChecked();
            holder.checkboxDistrict.setChecked(isChecked);
            
            if (isChecked) {
                selectedIds.add(district.getId());
            } else {
                selectedIds.remove(district.getId());
            }
        });

        holder.checkboxDistrict.setOnClickListener(v -> {
            boolean isChecked = holder.checkboxDistrict.isChecked();
            if (isChecked) {
                selectedIds.add(district.getId());
            } else {
                selectedIds.remove(district.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return districts.size();
    }

    public List<Long> getSelectedDistrictIds() {
        return new ArrayList<>(selectedIds);
    }

    public void setSelectedDistricts(Set<Long> districtIds) {
        selectedIds.clear();
        if (districtIds != null) {
            selectedIds.addAll(districtIds);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxDistrict;
        TextView tvDistrictName;

        ViewHolder(View itemView) {
            super(itemView);
            checkboxDistrict = itemView.findViewById(R.id.checkbox_district);
            tvDistrictName = itemView.findViewById(R.id.tv_district_name);
        }
    }
}

