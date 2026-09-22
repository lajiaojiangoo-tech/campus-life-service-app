package cn.edu.gdpnc.jsjxy.mmt;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 快递服务界面 - 对接Web服务端快递API
 * 功能：取件码查询、快递列表（分组）、取件确认、寄件
 */
public class ExpressActivity extends AppCompatActivity implements ExpressAdapter.OnPickupClickListener {

    private RecyclerView rvMyExpress;
    private TextView tvEmptyExpress;
    private ProgressBar pbExpress;
    private ExpressAdapter expressAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_express);

        MaterialButton btnPickupCode = findViewById(R.id.btnPickupCode);
        MaterialButton btnSendExpress = findViewById(R.id.btnSendExpress);
        MaterialButton btnStation = findViewById(R.id.btnStation);

        rvMyExpress = findViewById(R.id.rvMyExpress);
        tvEmptyExpress = findViewById(R.id.tvEmptyExpress);
        pbExpress = findViewById(R.id.pbExpress);

        rvMyExpress.setLayoutManager(new LinearLayoutManager(this));
        expressAdapter = new ExpressAdapter(this);
        rvMyExpress.setAdapter(expressAdapter);

        // 取件码查询
        btnPickupCode.setOnClickListener(v -> showPickupCodeDialog());

        // 寄快递 - 系统内表单
        btnSendExpress.setOnClickListener(v -> showSendExpressDialog());

        // 驿站信息
        btnStation.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("驿站信息")
                    .setMessage("驿站位置：茂名幼儿师范学校五区菜鸟驿站\n" +
                            "联系电话：123456789\n" +
                            "营业时间：10:00-19:00")
                    .setPositiveButton("知道了", null)
                    .show();
        });

        // 页面加载时自动获取快递列表
        refreshList();
    }

    /**
     * 刷新快递列表
     */
    private void refreshList() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            tvEmptyExpress.setVisibility(View.VISIBLE);
            tvEmptyExpress.setText("请先登录");
            return;
        }
        loadMyExpressList(userId);
    }

    /**
     * 取件确认回调
     */
    @Override
    public void onPickupClick(ExpressModel item) {
        new AlertDialog.Builder(this)
                .setTitle("确认取件")
                .setMessage("确认已取件？\n\n单号：" + item.trackingNo +
                        "\n快递公司：" + item.company +
                        "\n取件码：" + item.pickupCode)
                .setPositiveButton("确认", (dialog, which) -> pickupExpress(item.id))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 取件码查询弹窗
     */
    private void showPickupCodeDialog() {
        final EditText et = new EditText(this);
        et.setHint("请输入快递单号");
        et.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("取件码查询")
                .setMessage("请输入快递单号：")
                .setView(et)
                .setPositiveButton("查询", (dialog, which) -> {
                    String trackingNo = et.getText().toString().trim();
                    if (trackingNo.isEmpty()) {
                        Toast.makeText(this, "单号不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    queryExpressFromServer(trackingNo);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 寄快递表单弹窗
     */
    private void showSendExpressDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        TextInputEditText etReceiver = new TextInputEditText(this);
        etReceiver.setHint("收件人姓名");
        layout.addView(etReceiver);

        TextInputEditText etPhone = new TextInputEditText(this);
        etPhone.setHint("联系电话");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        TextInputEditText etAddress = new TextInputEditText(this);
        etAddress.setHint("收件地址");
        layout.addView(etAddress);

        TextInputEditText etItem = new TextInputEditText(this);
        etItem.setHint("物品类型（如：文件、衣物）");
        layout.addView(etItem);

        new AlertDialog.Builder(this)
                .setTitle("寄快递")
                .setView(layout)
                .setPositiveButton("提交", (dialog, which) -> {
                    String receiver = etReceiver.getText() != null ? etReceiver.getText().toString().trim() : "";
                    String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                    String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
                    String itemType = etItem.getText() != null ? etItem.getText().toString().trim() : "";

                    if (receiver.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                        Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitSendExpress(receiver, phone, address, itemType);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 提交寄件请求
     */
    private void submitSendExpress(String receiver, String phone, String address, String itemType) {
        String userId = HttpUtil.getUserId(this);
        Map<String, String> body = new HashMap<>();
        body.put("userId", userId);
        body.put("receiver", receiver);
        body.put("phone", phone);
        body.put("address", address);
        body.put("itemType", itemType);

        HttpUtil.post("/api/express/send", body, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Toast.makeText(ExpressActivity.this, "寄件提交成功", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = (String) result.get("msg");
                        Toast.makeText(ExpressActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ExpressActivity.this, "提交失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ExpressActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 调用服务端取件确认API
     */
    private void pickupExpress(String expressId) {
        String userId = HttpUtil.getUserId(this);
        Map<String, String> body = new HashMap<>();
        body.put("expressId", expressId);
        body.put("userId", userId);

        HttpUtil.post("/api/express/pickup", body, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Toast.makeText(ExpressActivity.this, "取件成功", Toast.LENGTH_SHORT).show();
                        refreshList();
                    } else {
                        String msg = (String) result.get("msg");
                        Toast.makeText(ExpressActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ExpressActivity.this, "取件失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ExpressActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 从Web服务查询快递信息 - 结果弹窗带操作按钮
     */
    private void queryExpressFromServer(String trackingNo) {
        HttpUtil.get("/api/express/query?trackingNo=" + trackingNo, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        String status = (String) data.get("status");
                        String expressId = String.valueOf(data.get("id"));
                        String info = "单号：" + data.get("trackingNo") +
                                "\n快递公司：" + data.get("company") +
                                "\n状态：" + status +
                                "\n取件码：" + data.get("pickupCode");

                        AlertDialog.Builder builder = new AlertDialog.Builder(ExpressActivity.this)
                                .setTitle("查询结果")
                                .setMessage(info);

                        // 待取件状态增加取件按钮
                        if ("待取件".equals(status)) {
                            builder.setPositiveButton("确认取件", (d, w) -> pickupExpress(expressId));
                            builder.setNeutralButton("复制单号", (d, w) -> {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(ClipData.newPlainText("快递单号", trackingNo));
                                Toast.makeText(ExpressActivity.this, "已复制单号", Toast.LENGTH_SHORT).show();
                            });
                            builder.setNegativeButton("关闭", null);
                        } else {
                            builder.setPositiveButton("复制单号", (d, w) -> {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(ClipData.newPlainText("快递单号", trackingNo));
                                Toast.makeText(ExpressActivity.this, "已复制单号", Toast.LENGTH_SHORT).show();
                            });
                            builder.setNegativeButton("关闭", null);
                        }

                        builder.show();
                    } else {
                        String msg = (String) result.get("msg");
                        new AlertDialog.Builder(ExpressActivity.this)
                                .setTitle("查询结果")
                                .setMessage(msg)
                                .setPositiveButton("确定", null)
                                .show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ExpressActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ExpressActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 从Web服务加载快递列表
     */
    private void loadMyExpressList(String userId) {
        pbExpress.setVisibility(View.VISIBLE);
        tvEmptyExpress.setVisibility(View.GONE);

        HttpUtil.get("/api/express/list/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                pbExpress.setVisibility(View.GONE);
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<ExpressModel> list = new ArrayList<>();

                        if (data != null) {
                            for (Map<String, Object> item : data) {
                                String id = String.valueOf(item.get("id"));
                                String trackingNo = (String) item.get("trackingNo");
                                String company = (String) item.get("company");
                                String status = (String) item.get("status");
                                String pickupCode = (String) item.get("pickupCode");
                                String arrivalTime = item.get("arrivalTime") != null
                                        ? String.valueOf(item.get("arrivalTime")) : "";
                                list.add(new ExpressModel(id, trackingNo, company, status, pickupCode, arrivalTime));
                            }
                        }

                        expressAdapter.setData(list);

                        if (list.isEmpty()) {
                            tvEmptyExpress.setVisibility(View.VISIBLE);
                            tvEmptyExpress.setText("暂无快递信息");
                        } else {
                            tvEmptyExpress.setVisibility(View.GONE);
                        }
                    } else {
                        tvEmptyExpress.setVisibility(View.VISIBLE);
                        tvEmptyExpress.setText("获取快递列表失败");
                    }
                } catch (Exception e) {
                    tvEmptyExpress.setVisibility(View.VISIBLE);
                    tvEmptyExpress.setText("解析数据失败");
                }
            }

            @Override
            public void onFailure(String error) {
                pbExpress.setVisibility(View.GONE);
                tvEmptyExpress.setVisibility(View.VISIBLE);
                tvEmptyExpress.setText("网络请求失败");
                Toast.makeText(ExpressActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
