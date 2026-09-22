package cn.edu.gdpnc.jsjxy.mmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.VH> {

    public interface OnItemClickListener {
        void onClick(MenuItemModel item);
    }

    private final List<MenuItemModel> data;
    private final OnItemClickListener listener;

    public MenuAdapter(List<MenuItemModel> data, OnItemClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MenuItemModel item = data.get(position);
        holder.ivIcon.setImageResource(item.iconRes);
        holder.ivIcon.setBackgroundResource(item.bgRes);
        holder.tvTitle.setText(item.title);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIconBg);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
