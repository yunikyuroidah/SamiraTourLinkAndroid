package com.example.samiratravelmobile.fragments;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samiratravelmobile.R;
import java.util.List;

public class KeunggulanAdapter extends RecyclerView.Adapter<KeunggulanAdapter.ViewHolder> {
    private final List<String[]> list;

    public KeunggulanAdapter(List<String[]> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keunggulan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] data = list.get(position);
        holder.txtIcon.setText(data[0]);
        holder.txtTitle.setText(data[1]);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtIcon, txtTitle;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardKeunggulan);
            txtIcon = itemView.findViewById(R.id.txtIcon);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}
