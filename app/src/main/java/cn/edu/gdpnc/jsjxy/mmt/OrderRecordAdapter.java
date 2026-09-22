package cn.edu.gdpnc.jsjxy.mmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderRecordAdapter extends RecyclerView.Adapter<OrderRecordAdapter.VH> {

    private final List<OrderRecordModel> data = new ArrayList<>();

    public void setData(List<OrderRecordModel> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderRecordModel item = data.get(position);
        holder.tvItems.setText(item.items);
        holder.tvTotal.setText("合计：¥" + (int) item.total);
        holder.tvStatus.setText(item.status);
        holder.tvTime.setText(item.time);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvItems, tvTotal, tvStatus, tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
