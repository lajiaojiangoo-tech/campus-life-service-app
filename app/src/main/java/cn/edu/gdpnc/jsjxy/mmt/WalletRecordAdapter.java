package cn.edu.gdpnc.jsjxy.mmt;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WalletRecordAdapter extends RecyclerView.Adapter<WalletRecordAdapter.VH> {

    private final List<WalletRecordModel> data = new ArrayList<>();

    public void setData(List<WalletRecordModel> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wallet_record, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        WalletRecordModel item = data.get(position);
        holder.tvType.setText(item.type);
        holder.tvDesc.setText(item.desc);
        holder.tvTime.setText(item.time);
        holder.tvBalance.setText("余额：¥" + String.format("%.2f", item.balance));

        if ("充值".equals(item.type)) {
            holder.tvAmount.setText("+¥" + String.format("%.2f", item.amount));
            holder.tvAmount.setTextColor(Color.parseColor("#FF27AE60"));
        } else {
            holder.tvAmount.setText("-¥" + String.format("%.2f", item.amount));
            holder.tvAmount.setTextColor(Color.parseColor("#FFE74C3C"));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvDesc, tvAmount, tvTime, tvBalance;

        VH(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBalance = itemView.findViewById(R.id.tvBalance);
        }
    }
}
