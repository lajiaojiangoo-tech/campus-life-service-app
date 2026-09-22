package cn.edu.gdpnc.jsjxy.mmt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

/**
 * 打印服务界面 - 对接Web服务端打印API
 * 功能：提交打印任务到服务器、从服务器获取打印记录
 */
public class PrintActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_FILE = 1001;

    // 演示文件列表
    private static final String[] DEMO_FILES = {
            "高等数学期末复习笔记.pdf",
            "毕业设计开题报告.docx",
            "社团活动海报.png",
            "英语四级真题.pdf",
            "数据结构课程作业.docx"
    };

    private TextView tvSelectedFile;
    private TextView tvCopies;
    private RadioGroup rgPaper, rgColor, rgSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        MaterialButton btnChooseFile = findViewById(R.id.btnChooseFile);
        MaterialButton btnSubmitPrint = findViewById(R.id.btnSubmitPrint);
        MaterialButton btnPrintHistory = findViewById(R.id.btnPrintHistory);
        MaterialButton btnMinus = findViewById(R.id.btnMinus);
        MaterialButton btnPlus = findViewById(R.id.btnPlus);

        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        tvCopies = findViewById(R.id.tvCopies);
        rgPaper = findViewById(R.id.rgPaper);
        rgColor = findViewById(R.id.rgColor);
        rgSide = findViewById(R.id.rgSide);

        btnChooseFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
        });

        // 生成演示文件按钮
        LinearLayout demoContainer = findViewById(R.id.demoFilesContainer);
        for (String fileName : DEMO_FILES) {
            MaterialButton btn = new MaterialButton(this, null, com.google.android.material.R.attr.borderlessButtonStyle);
            btn.setText(fileName);
            btn.setTextSize(13);
            btn.setAllCaps(false);
            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btn.setPadding(8, 0, 8, 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 2;
            btn.setLayoutParams(lp);
            btn.setOnClickListener(v -> {
                tvSelectedFile.setText("已选择：" + fileName);
                Toast.makeText(this, "已选择演示文件", Toast.LENGTH_SHORT).show();
            });
            demoContainer.addView(btn);
        }

        btnMinus.setOnClickListener(v -> {
            int copies = getCopies();
            if (copies > 1) {
                copies--;
                tvCopies.setText(String.valueOf(copies));
            } else {
                Toast.makeText(this, "最少 1 份", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            int copies = getCopies();
            copies++;
            tvCopies.setText(String.valueOf(copies));
        });

        // 提交打印到Web服务
        btnSubmitPrint.setOnClickListener(v -> submitPrintToServer());

        btnPrintHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecordsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 提交打印任务到Web服务
     */
    private void submitPrintToServer() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileText = tvSelectedFile.getText().toString();
        if (fileText == null || fileText.trim().isEmpty() || fileText.contains("未选择")) {
            Toast.makeText(this, "请先选择文件", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = fileText.replace("已选择：", "").trim();
        String copies = tvCopies.getText().toString();

        // 获取打印参数
        String paper = ((RadioButton) findViewById(rgPaper.getCheckedRadioButtonId())).getText().toString();
        String colorType = ((RadioButton) findViewById(rgColor.getCheckedRadioButtonId())).getText().toString();
        String side = ((RadioButton) findViewById(rgSide.getCheckedRadioButtonId())).getText().toString();

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("fileName", fileName);
        params.put("paper", paper);
        params.put("colorType", colorType);
        params.put("side", side);
        params.put("copies", Integer.parseInt(copies));

        HttpUtil.post("/api/print/submit", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");

                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        double price = ((Number) data.get("price")).doubleValue();
                        double walletBalance = ((Number) data.get("walletBalance")).doubleValue();
                        Toast.makeText(PrintActivity.this,
                                "提交打印成功！预计费用：¥" + price + "\n钱包余额：¥" + String.format("%.2f", walletBalance),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PrintActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PrintActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PrintActivity.this, "提交失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getCopies() {
        int copies = 1;
        try {
            copies = Integer.parseInt(tvCopies.getText().toString());
        } catch (Exception ignored) {}
        return copies;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                tvSelectedFile.setText("已选择：" + uri.getLastPathSegment());
            }
        }
    }
}
