package cn.edu.gdpnc.jsjxy.mmt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
 * 上网服务界面 - 对接Web服务端上网API
 * 功能：校园网登录/登出、查询流量、充值
 */
public class InternetActivity extends AppCompatActivity {

    private TextView tvNetStatus, tvBalanceNet, tvFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet);

        setTitle("上网");

        tvNetStatus = findViewById(R.id.tvNetStatus);
        tvBalanceNet = findViewById(R.id.tvBalanceNet);
        tvFlow = findViewById(R.id.tvFlow);

        MaterialButton btnNetLogin = findViewById(R.id.btnNetLogin);
        MaterialButton btnQueryFlow = findViewById(R.id.btnQueryFlow);
        MaterialButton btnRecharge = findViewById(R.id.btnRecharge);

        // 从Web服务获取上网状态
        loadInternetStatus();

        // 校园网登录
        btnNetLogin.setOnClickListener(v -> internetLogin());

        // 查询流量
        btnQueryFlow.setOnClickListener(v -> queryFlow());

        // 充值上网费
        btnRecharge.setOnClickListener(v -> {
            EditText et = new EditText(this);
            et.setHint("请输入充值金额（如 20）");
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            et.setSingleLine(true);

            new AlertDialog.Builder(this)
                    .setTitle("充值上网费")
                    .setMessage("请输入金额：")
                    .setView(et)
                    .setPositiveButton("充值", (dialog, which) -> {
                        String text = et.getText().toString().trim();
                        if (text.isEmpty()) {
                            Toast.makeText(this, "金额不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int money = Integer.parseInt(text);
                        if (money <= 0) {
                            Toast.makeText(this, "金额必须大于 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        rechargeInternet(money);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        MaterialButton btnMyRecords = findViewById(R.id.btnMyRecords);
        btnMyRecords.setOnClickListener(v -> showMyRecords());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInternetStatus();
    }

    /**
     * 从Web服务获取上网状态
     */
    private void loadInternetStatus() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) return;

        HttpUtil.get("/api/internet/status/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        boolean isLogin = Boolean.TRUE.equals(data.get("isLogin"));
                        double balance = ((Number) data.get("balance")).doubleValue();
                        double flowGB = ((Number) data.get("flowGB")).doubleValue();

                        tvNetStatus.setText("状态：" + (isLogin ? "已登录" : "未登录"));
                        tvBalanceNet.setText(String.format("余额：¥%.2f", balance));
                        tvFlow.setText(String.format("本月剩余流量：%.1f GB", flowGB));
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
     * 校园网登录/登出
     */
    private void internetLogin() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断当前状态，切换登录/登出
        String statusText = tvNetStatus.getText().toString();
        boolean isLogin = statusText.contains("已登录");

        String path = isLogin ? "/api/internet/logout" : "/api/internet/login";
        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);

        HttpUtil.post(path, params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");

                    if (code == 200) {
                        Toast.makeText(InternetActivity.this, msg, Toast.LENGTH_SHORT).show();
                        loadInternetStatus();
                    } else {
                        Toast.makeText(InternetActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InternetActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(InternetActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 查询流量
     */
    private void queryFlow() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", userId);

        HttpUtil.post("/api/internet/query_flow", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double flowGB = ((Number) data.get("flowGB")).doubleValue();
                        double balance = ((Number) data.get("balance")).doubleValue();

                        tvFlow.setText(String.format("本月剩余流量：%.1f GB", flowGB));
                        tvBalanceNet.setText(String.format("余额：¥%.2f", balance));
                        Toast.makeText(InternetActivity.this, "查询成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InternetActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(InternetActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 充值上网费
     */
    private void rechargeInternet(int amount) {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("amount", amount);

        HttpUtil.post("/api/internet/recharge", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");

                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double netBalance = ((Number) data.get("balance")).doubleValue();
                        double walletBalance = ((Number) data.get("walletBalance")).doubleValue();
                        Toast.makeText(InternetActivity.this,
                                "充值成功：¥" + amount + "\n钱包余额：¥" + String.format("%.2f", walletBalance),
                                Toast.LENGTH_LONG).show();
                        loadInternetStatus();
                    } else {
                        Toast.makeText(InternetActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(InternetActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(InternetActivity.this, error, Toast.LENGTH_SHORT).show();
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
        tvTitle.setText("上网记录");

        RecyclerView rv = sheetView.findViewById(R.id.rvDialogRecords);
        TextView tvEmpty = sheetView.findViewById(R.id.tvDialogEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        InternetRecordAdapter adapter = new InternetRecordAdapter();
        rv.setAdapter(adapter);

        HttpUtil.get("/api/internet/records/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<InternetRecordModel> recordList = new ArrayList<>();

                        if (data != null) {
                            for (Map<String, Object> item : data) {
                                String desc = (String) item.get("desc");
                                double amount = item.get("amount") != null
                                        ? ((Number) item.get("amount")).doubleValue() : 0;
                                double balance = item.get("balance") != null
                                        ? ((Number) item.get("balance")).doubleValue() : 0;
                                String time = (String) item.get("time");
                                recordList.add(new InternetRecordModel(desc, amount, balance, time));
                            }
                        }

                        adapter.setData(recordList);
                        tvEmpty.setVisibility(recordList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    Toast.makeText(InternetActivity.this, "解析记录数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(InternetActivity.this, "获取记录失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
