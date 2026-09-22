package cn.edu.gdpnc.jsjxy.mmt;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ExpressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> items = new ArrayList<>();
    private final OnPickupClickListener listener;

    public interface OnPickupClickListener {
        void onPickupClick(ExpressModel item);
    }

    public ExpressAdapter(OnPickupClickListener listener) {
        this.listener = listener;
    }

    /**
     * 设置快递数据，自动按状态分组排序
     */
    public void setData(List<ExpressModel> data) {
        items.clear();

        // 按状态分组：待取件 → 运输中 → 已签收
        String[] statusOrder = {"待取件", "运输中", "已签收"};

        for (String status : statusOrder) {
            List<ExpressModel> group = new ArrayList<>();
            for (ExpressModel item : data) {
                if (status.equals(item.status)) {
                    group.add(item);
                }
            }
            if (!group.isEmpty()) {
                items.add(status + "(" + group.size() + ")");
                items.addAll(group);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_express_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_express, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvHeader.setText((String) items.get(position));
        } else {
            bindItem((ItemVH) holder, (ExpressModel) items.get(position));
        }
    }

    private void bindItem(ItemVH holder, ExpressModel item) {
        holder.tvCompany.setText(item.company);
        holder.tvTrackingNo.setText(item.trackingNo);
        holder.tvCode.setText("取件码：" + item.pickupCode);

        // 状态颜色
        holder.tvStatus.setText(item.status);
        switch (item.status) {
            case "待取件":
                holder.tvStatus.setTextColor(0xFFFF9800);
                break;
            case "运输中":
                holder.tvStatus.setTextColor(0xFF2196F3);
                break;
            case "已签收":
                holder.tvStatus.setTextColor(0xFF4CAF50);
                break;
            default:
                holder.tvStatus.setTextColor(0xFF999999);
                break;
        }

        // 到站时间
        if (item.arrivalTime != null && !item.arrivalTime.isEmpty()) {
            holder.tvArrivalTime.setText(item.arrivalTime);
            holder.tvArrivalTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvArrivalTime.setVisibility(View.GONE);
        }

        // 复制单号
        holder.btnCopyNo.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("快递单号", item.trackingNo));
            Toast.makeText(v.getContext(), "已复制单号：" + item.trackingNo, Toast.LENGTH_SHORT).show();
        });

        // 取件按钮：仅"待取件"状态显示
        if ("待取件".equals(item.status)) {
            holder.btnPickup.setVisibility(View.VISIBLE);
            holder.btnPickup.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPickupClick(item);
                }
            });
        } else {
            holder.btnPickup.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvCompany, tvStatus, tvTrackingNo, tvCode, tvArrivalTime;
        MaterialButton btnPickup, btnCopyNo;

        ItemVH(@NonNull View itemView) {
            super(itemView);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTrackingNo = itemView.findViewById(R.id.tvTrackingNo);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvArrivalTime = itemView.findViewById(R.id.tvArrivalTime);
            btnPickup = itemView.findViewById(R.id.btnPickup);
            btnCopyNo = itemView.findViewById(R.id.btnCopyNo);
        }
    }
}
