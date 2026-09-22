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
 * 热水记录标签页
 */
public class HotWaterRecordFragment extends Fragment {

    private HotWaterRecordAdapter adapter;
    private TextView tvEmpty;

    public HotWaterRecordFragment() {
        super(R.layout.fragment_record_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvRecords);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HotWaterRecordAdapter();
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

        HttpUtil.get("/api/hotwater/records/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<HotWaterRecordModel> list = new ArrayList<>();

                        if (data != null) {
                            for (Map<String, Object> item : data) {
                                String type = (String) item.get("type");
                                String time = (String) item.get("time");
                                double balance = item.get("balance") != null
                                        ? ((Number) item.get("balance")).doubleValue() : 0;

                                String detail;
                                double amount;
                                if ("使用".equals(type)) {
                                    int liters = item.get("liters") != null
                                            ? ((Number) item.get("liters")).intValue() : 0;
                                    String location = item.get("location") != null
                                            ? (String) item.get("location") : "";
                                    amount = item.get("cost") != null
                                            ? ((Number) item.get("cost")).doubleValue() : 0;
                                    detail = liters + "L · " + location;
                                } else {
                                    amount = item.get("amount") != null
                                            ? ((Number) item.get("amount")).doubleValue() : 0;
                                    detail = "热水充值";
                                }

                                list.add(new HotWaterRecordModel(type, detail, amount, balance, time));
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
