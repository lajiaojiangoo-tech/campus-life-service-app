package cn.edu.gdpnc.jsjxy.mmt;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
 * 饮食订餐界面 - 对接Web服务端饮食API
 * 功能：从服务器获取菜品列表、提交订单到服务器
 */
public class FoodActivity extends AppCompatActivity {

    private TextView tvTotal;
    private List<FoodItemModel> list;
    private FoodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        setTitle("饮食");

        tvTotal = findViewById(R.id.tvTotal);
        MaterialButton btnSubmitOrder = findViewById(R.id.btnSubmitOrder);

        RecyclerView rvFood = findViewById(R.id.rvFood);
        rvFood.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new FoodAdapter(list, this::refreshTotal);
        rvFood.setAdapter(adapter);

        // 从Web服务获取菜品列表
        loadFoodListFromServer();

        btnSubmitOrder.setOnClickListener(v -> submitOrderToServer());

        MaterialButton btnMyOrders = findViewById(R.id.btnMyOrders);
        btnMyOrders.setOnClickListener(v -> showMyOrders());
    }

    /**
     * 从Web服务获取菜品列表
     */
    private void loadFoodListFromServer() {
        HttpUtil.get("/api/food/list", new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        list.clear();
                        for (Map<String, Object> item : data) {
                            String id = String.valueOf(item.get("id"));
                            String name = (String) item.get("name");
                            String desc = (String) item.get("desc");
                            double price = ((Number) item.get("price")).doubleValue();
                            list.add(new FoodItemModel(id, name, desc, (int) price));
                        }
                        adapter.notifyDataSetChanged();
                        refreshTotal();
                    }
                } catch (Exception e) {
                    Toast.makeText(FoodActivity.this, "解析菜品数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FoodActivity.this, "获取菜品列表失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 提交订单到Web服务
     */
    private void submitOrderToServer() {
        int total = calcTotal();
        if (total <= 0) {
            Toast.makeText(this, "你还没选菜哦~", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建订单项
        List<Map<String, Object>> items = new ArrayList<>();
        for (FoodItemModel f : list) {
            if (f.count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("foodId", f.id);
                item.put("count", f.count);
                items.add(item);
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("items", items);

        HttpUtil.post("/api/order/submit", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");

                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double orderTotal = ((Number) data.get("total")).doubleValue();
                        double walletBalance = ((Number) data.get("walletBalance")).doubleValue();
                        Toast.makeText(FoodActivity.this,
                                "下单成功！合计：¥" + (int) orderTotal + "\n钱包余额：¥" + String.format("%.2f", walletBalance),
                                Toast.LENGTH_LONG).show();

                        // 清空选择
                        for (FoodItemModel f : list) {
                            f.count = 0;
                        }
                        adapter.notifyDataSetChanged();
                        refreshTotal();
                    } else {
                        Toast.makeText(FoodActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(FoodActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FoodActivity.this, "提交订单失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshTotal() {
        tvTotal.setText("合计：¥" + calcTotal());
    }

    private int calcTotal() {
        int total = 0;
        for (FoodItemModel f : list) {
            total += f.price * f.count;
        }
        return total;
    }

    /**
     * 显示我的订单（BottomSheet）
     */
    private void showMyOrders() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_record_list, null);
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText("我的订单");

        RecyclerView rv = sheetView.findViewById(R.id.rvDialogRecords);
        TextView tvEmpty = sheetView.findViewById(R.id.tvDialogEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        OrderRecordAdapter adapter = new OrderRecordAdapter();
        rv.setAdapter(adapter);

        HttpUtil.get("/api/order/list/" + userId, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
                        List<OrderRecordModel> orderList = new ArrayList<>();

                        if (data != null) {
                            for (Map<String, Object> item : data) {
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
                                orderList.add(new OrderRecordModel(id, sb.toString(), total, status, time));
                            }
                        }

                        adapter.setData(orderList);
                        tvEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    Toast.makeText(FoodActivity.this, "解析订单数据失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FoodActivity.this, "获取订单失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}
