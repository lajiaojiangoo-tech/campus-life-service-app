package cn.edu.gdpnc.jsjxy.mmt;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订餐记录标签页
 */
public class OrderRecordFragment extends Fragment {

    private OrderRecordAdapter adapter;
    private TextView tvEmpty;

    public OrderRecordFragment() {
        super(R.layout.fragment_record_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvRecords);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderRecordAdapter();
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        String userId = HttpUtil.getUserId(requireContext());
        if (userId.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        HttpUtil.get("/api/order/list/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<OrderRecordModel> list = new ArrayList<>();

                        if (data != null) {
                            for (Map<String, Object> item : data) {
                                // 拼接菜品列表
                                List<Map<String, Object>> items = (List<Map<String, Object>>) item.get("items");
                                StringBuilder sb = new StringBuilder();
                                if (items != null) {
                                    for (int i = 0; i < items.size(); i++) {
                                        if (i > 0) sb.append("、");
                                        sb.append(items.get(i).get("name"))
                                                .append("×").append(items.get(i).get("count"));
                                    }
                                }
                                double total = ((Number) item.get("total")).doubleValue();
                                String status = (String) item.get("status");
                                String time = (String) item.get("time");
                                String id = String.valueOf(item.get("id"));
                                list.add(new OrderRecordModel(id, sb.toString(), total, status, time));
                            }
                        }

                        adapter.setData(list);
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    // 静默处理
                }
            }

            @Override
            public void onFailure(String error) {
                // 静默处理
            }
        });
    }
}
