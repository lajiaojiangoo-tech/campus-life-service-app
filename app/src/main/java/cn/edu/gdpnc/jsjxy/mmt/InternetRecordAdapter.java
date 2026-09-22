package cn.edu.gdpnc.jsjxy.mmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InternetRecordAdapter extends RecyclerView.Adapter<InternetRecordAdapter.VH> {

    private final List<InternetRecordModel> data = new ArrayList<>();

    public void setData(List<InternetRecordModel> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_internet_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        InternetRecordModel item = data.get(position);
        holder.tvDesc.setText(item.desc);
        holder.tvAmount.setText("-¥" + String.format("%.2f", item.amount));
        holder.tvTime.setText(item.time);
        holder.tvBalance.setText("余额：¥" + String.format("%.2f", item.balance));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDesc, tvAmount, tvTime, tvBalance;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}
