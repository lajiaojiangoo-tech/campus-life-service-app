package cn.edu.gdpnc.jsjxy.mmt;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

/**
 * 个人信息界面 - 对接Web服务端用户API
 * 功能：从服务器获取用户信息、更新用户信息到服务器
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etStudentId, etDorm;

    public static final String SP_NAME = "profile_sp";
    public static final String KEY_NAME = "name";
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_DORM = "dorm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle("个人信息");

        etName = findViewById(R.id.etName);
        etStudentId = findViewById(R.id.etStudentId);
        etDorm = findViewById(R.id.etDorm);

        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        // 从本地缓存填充
        etName.setText(HttpUtil.getUserName(this));
        etStudentId.setText(HttpUtil.getStudentId(this));
        etDorm.setText(HttpUtil.getDorm(this));

        // 从Web服务获取最新信息
        loadProfileFromServer();

        btnSave.setOnClickListener(v -> saveProfileToServer());

        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * 从Web服务获取用户信息
     */
    private void loadProfileFromServer() {
        String userId = HttpUtil.getUserId(this);
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
                        String name = (String) data.get("name");
                        String studentId = (String) data.get("studentId");
                        String dorm = (String) data.get("dorm");

                        if (name != null) etName.setText(name);
                        if (studentId != null) etStudentId.setText(studentId);
                        if (dorm != null) etDorm.setText(dorm);
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
     * 保存用户信息到Web服务
     */
    private void saveProfileToServer() {
        String userId = HttpUtil.getUserId(this);
        if (userId.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String sid = etStudentId.getText().toString().trim();
        String dorm = etDorm.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sid.isEmpty()) {
            Toast.makeText(this, "学号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("studentId", sid);
        params.put("dorm", dorm);

        HttpUtil.put("/api/user/" + userId, params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        // 更新本地缓存
                        HttpUtil.saveLoginUser(ProfileActivity.this, userId,
                                HttpUtil.parseResponse(response,
                                        new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType())
                                        .toString(), name, sid, dorm);

                        Toast.makeText(ProfileActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfileActivity.this, "保存失败：" + error, Toast.LENGTH_SHORT).show();
            }
        });

        // 同时更新本地缓存
        HttpUtil.saveLoginUser(this, userId, "", name, sid, dorm);
    }
}
