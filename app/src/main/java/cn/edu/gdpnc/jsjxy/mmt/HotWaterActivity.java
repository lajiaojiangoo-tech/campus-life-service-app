package cn.edu.gdpnc.jsjxy.mmt;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热水服务界面 - 对接Web服务端热水API
 * 功能：从服务器查询余额、使用热水、充值
 */
public class HotWaterActivity extends AppCompatActivity {

    private TextView tvBalance;
    private Spinner spinnerLocation;
    private RadioGroup rgWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_water);

        spinnerLocation = findViewById(R.id.spinnerLocation);
        tvBalance = findViewById(R.id.tvBalance);
        rgWater = findViewById(R.id.rgWater);
        MaterialButton btnStartWater = findViewById(R.id.btnStartWater);

        String[] locations = {
                "1号宿舍热水房",
                "2号宿舍热水房",
                "3号宿舍热水房"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);

        // 从Web服务查询余额
        loadBalanceFromServer();

        btnStartWater.setOnClickListener(v -> useHotWater());

        MaterialButton btnMyRecords = findViewById(R.id.btnMyRecords);
        btnMyRecords.setOnClickListener(v -> showMyRecords());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBalanceFromServer();
    }

    /**
     * 从Web服务查询钱包余额
     */
    private void loadBalanceFromServer() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) return;

        HttpUtil.get("/api/wallet/balance/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double balance = ((Number) data.get("balance")).doubleValue();
                        tvBalance.setText(String.format("钱包余额：¥%.2f", balance));
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

    /**
     * 使用热水 - 调用Web服务
     */
    private void useHotWater() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgWater.getCheckedRadioButtonId();
        RadioButton rb = findViewById(checkedId);
        String text = rb.getText().toString();
        int liter = Integer.parseInt(text.replace("L", ""));
        String location = spinnerLocation.getSelectedItem().toString();

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("liters", liter);
        params.put("location", location);

        HttpUtil.post("/api/hotwater/use", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");

                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double cost = ((Number) data.get("cost")).doubleValue();
                        double balance = ((Number) data.get("balance")).doubleValue();
                        String loc = (String) data.get("location");
                        int liters = ((Number) data.get("liters")).intValue();

                        tvBalance.setText(String.format("钱包余额：¥%.2f", balance));
                        Toast.makeText(HotWaterActivity.this,
                                "已在 " + loc + " 出水 " + liters + "L\n花费 ¥" + (int) cost + "\n钱包余额：¥" + String.format("%.2f", balance),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(HotWaterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(HotWaterActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HotWaterActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示我的记录（BottomSheet）
     */
    private void showMyRecords() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_record_list, null);
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("热水记录");

        RecyclerView rv = sheetView.findViewById(R.id.rvDialogRecords);
        TextView tvEmpty = sheetView.findViewById(R.id.tvDialogEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        HotWaterRecordAdapter adapter = new HotWaterRecordAdapter();
        rv.setAdapter(adapter);

        HttpUtil.get("/api/hotwater/records/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<HotWaterRecordModel> recordList = new ArrayList<>();

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

                                recordList.add(new HotWaterRecordModel(type, detail, amount, balance, time));
                            }
                        }

                        adapter.setData(recordList);
                        tvEmpty.setVisibility(recordList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    Toast.makeText(HotWaterActivity.this, "解析记录数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(HotWaterActivity.this, "获取记录失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
