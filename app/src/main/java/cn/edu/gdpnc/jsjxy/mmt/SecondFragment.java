package cn.edu.gdpnc.jsjxy.mmt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

/**
 * "我的"页面 - 从Web服务获取用户信息，支持退出登录、钱包充值
 */
public class SecondFragment extends Fragment {

    private TextView tvName, tvStudentId, tvClass, tvWalletBalance;

    public SecondFragment() {
        super(R.layout.fragment_second);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tvName);
        tvStudentId = view.findViewById(R.id.tvStudentId);
        tvClass = view.findViewById(R.id.tvClass);
        tvWalletBalance = view.findViewById(R.id.tvWalletBalance);

        MaterialButton btnWalletRecharge = view.findViewById(R.id.btnWalletRecharge);
        MaterialButton btnProfile = view.findViewById(R.id.btnProfile);
        MaterialButton btnPrintHistory = view.findViewById(R.id.btnPrintHistory);
        MaterialButton btnSettings = view.findViewById(R.id.btnSettings);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // 钱包充值
        btnWalletRecharge.setOnClickListener(v -> showRechargeDialog());

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });

        btnPrintHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RecordsActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });

        // 退出登录
        btnLogout.setOnClickListener(v -> {
            HttpUtil.logout(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshProfile();
        loadWalletBalance();
    }

    /**
     * 钱包充值弹窗
     */
    private void showRechargeDialog() {
        EditText et = new EditText(requireContext());
        et.setHint("请输入充值金额（如 50）");
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setSingleLine(true);

        new AlertDialog.Builder(requireContext())
                .setTitle("钱包充值")
                .setMessage("请输入金额：")
                .setView(et)
                .setPositiveButton("充值", (dialog, which) -> {
                    String text = et.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(requireContext(), "金额不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount = Double.parseDouble(text);
                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "金额必须大于0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    rechargeWallet(amount);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 调用服务端钱包充值API
     */
    private void rechargeWallet(double amount) {
        String userId = HttpUtil.getUserId(requireContext());
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("amount", amount);

        HttpUtil.post("/api/wallet/recharge", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");

                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double balance = ((Number) data.get("balance")).doubleValue();
                        tvWalletBalance.setText(String.format("余额：¥%.2f", balance));
                        Toast.makeText(requireContext(), "充值成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "充值失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), "充值失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 从服务端加载钱包余额
     */
    private void loadWalletBalance() {
        String userId = HttpUtil.getUserId(requireContext());
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
                        tvWalletBalance.setText(String.format("余额：¥%.2f", balance));
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

    private void refreshProfile() {
        // 先从本地缓存显示
        String name = HttpUtil.getUserName(requireContext());
        String sid = HttpUtil.getStudentId(requireContext());
        String dorm = HttpUtil.getDorm(requireContext());

        tvName.setText(name);
        tvStudentId.setText("学号：" + sid);
        tvClass.setText("宿舍：" + dorm);

        // 从Web服务获取最新用户信息
        String userId = HttpUtil.getUserId(requireContext());
        if (userId.isEmpty()) return;

        HttpUtil.get("/api/user/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        String n = (String) data.get("name");
                        String s = (String) data.get("studentId");
                        String d = (String) data.get("dorm");

                        if (n != null) tvName.setText(n);
                        if (s != null) tvStudentId.setText("学号：" + s);
                        if (d != null) tvClass.setText("宿舍：" + d);
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
