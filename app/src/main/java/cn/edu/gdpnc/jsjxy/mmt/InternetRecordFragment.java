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
 * 上网充值记录标签页
 */
public class InternetRecordFragment extends Fragment {

    private InternetRecordAdapter adapter;
    private TextView tvEmpty;

    public InternetRecordFragment() {
        super(R.layout.fragment_record_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvRecords);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InternetRecordAdapter();
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

        HttpUtil.get("/api/internet/records/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<InternetRecordModel> list = new ArrayList<>();

                        if (data != null) {
                            for (Map<String, Object> item : data) {
                                String desc = item.get("desc") != null ? (String) item.get("desc") : "上网充值";
                                double amount = item.get("amount") != null
                                        ? ((Number) item.get("amount")).doubleValue() : 0;
                                double balance = item.get("balance") != null
                                        ? ((Number) item.get("balance")).doubleValue() : 0;
                                String time = item.get("time") != null ? (String) item.get("time") : "";
                                list.add(new InternetRecordModel(desc, amount, balance, time));
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
