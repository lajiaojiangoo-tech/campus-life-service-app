package cn.edu.gdpnc.jsjxy.mmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {

    public interface OnCountChangeListener {
        void onChanged();
    }

    private final List<FoodItemModel> data;
    private final OnCountChangeListener listener;

    public FoodAdapter(List<FoodItemModel> data, OnCountChangeListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FoodItemModel item = data.get(position);

        h.tvName.setText(item.name);
        h.tvDesc.setText(item.desc);
        h.tvPrice.setText("¥" + item.price);
        h.tvCount.setText(String.valueOf(item.count));

        h.btnMinus.setOnClickListener(v -> {
            if (item.count > 0) {
                item.count--;
                h.tvCount.setText(String.valueOf(item.count));
                if (listener != null) listener.onChanged();
            }
        });

        h.btnPlus.setOnClickListener(v -> {
            item.count++;
            h.tvCount.setText(String.valueOf(item.count));
            if (listener != null) listener.onChanged();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvCount;
        Button btnMinus, btnPlus;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvCount = itemView.findViewById(R.id.tvCount);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
