package com.example.primecals;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<CalculationHistory> list = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CalculationHistory item);
    }

    public HistoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<CalculationHistory> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalculationHistory item = list.get(position);
        holder.tvExpr.setText(item.getExpression());
        holder.tvRes.setText(item.getResult());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpr, tvRes;
        ViewHolder(View itemView) {
            super(itemView);
            tvExpr = itemView.findViewById(R.id.tvHistoryExpression);
            tvRes = itemView.findViewById(R.id.tvHistoryResult);
        }
    }
}