package cn.edu.gdpnc.jsjxy.mmt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PrintHistoryAdapter extends RecyclerView.Adapter<PrintHistoryAdapter.VH> {

    private final List<PrintHistoryModel> data;

    public PrintHistoryAdapter(List<PrintHistoryModel> data) {
        this.data = data;
    }

    public void setData(List<PrintHistoryModel> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_print_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        PrintHistoryModel item = data.get(position);

        holder.tvFileName.setText("文件：" + item.fileName);
        holder.tvConfig.setText(item.config);
        holder.tvTime.setText("时间：" + item.time);
        holder.tvStatus.setText("状态：" + item.status);

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(),
                        "点击了记录：" + item.fileName,
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFileName, tvConfig, tvTime, tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvConfig = itemView.findViewById(R.id.tvConfig);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
