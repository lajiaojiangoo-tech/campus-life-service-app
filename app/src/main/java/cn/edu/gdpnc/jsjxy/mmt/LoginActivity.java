package cn.edu.gdpnc.jsjxy.mmt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录/注册界面
 * 对接Web服务端用户认证API
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private TextInputEditText etRegUsername, etRegPassword, etRegName, etRegStudentId, etRegDorm;
    private View cardRegister;
    private MaterialButton btnLogin, btnRegister, btnGoRegister, btnGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 如果已登录，直接跳转主页
        if (HttpUtil.isLoggedIn(this)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        // 登录表单
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        // 注册表单
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegName = findViewById(R.id.etRegName);
        etRegStudentId = findViewById(R.id.etRegStudentId);
        etRegDorm = findViewById(R.id.etRegDorm);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        cardRegister = findViewById(R.id.cardRegister);

        // 登录按钮
        btnLogin.setOnClickListener(v -> doLogin());

        // 切换到注册
        btnGoRegister.setOnClickListener(v -> {
            findViewById(R.id.cardRegister).getParent().requestLayout();
            cardRegister.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            btnGoRegister.setVisibility(View.GONE);
            // 隐藏登录卡片的父CardView
            ((View) btnLogin.getParent().getParent()).setVisibility(View.GONE);
        });

        // 切换到登录
        btnGoLogin.setOnClickListener(v -> {
            cardRegister.setVisibility(View.GONE);
            ((View) btnLogin.getParent().getParent()).setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnGoRegister.setVisibility(View.VISIBLE);
        });

        // 注册按钮
        btnRegister.setOnClickListener(v -> doRegister());

        // 演示账号点击填入
        findViewById(R.id.tvDemoHint).setOnClickListener(v -> {
            etUsername.setText("admin");
            etPassword.setText("123456");
        });
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("请输入用户名");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("请输入密码");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("登录中...");

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        HttpUtil.post("/api/login", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("登 录");

                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    if (code == 200) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        String userId = (String) data.get("userId");
                        String name = (String) data.get("name");
                        String studentId = (String) data.get("studentId");
                        String dorm = (String) data.get("dorm");

                        HttpUtil.saveLoginUser(LoginActivity.this, userId, username, name, studentId, dorm);
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        String msg = (String) result.get("msg");
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                btnLogin.setEnabled(true);
                btnLogin.setText("登 录");
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doRegister() {
        String username = etRegUsername.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String name = etRegName.getText().toString().trim();
        String studentId = etRegStudentId.getText().toString().trim();
        String dorm = etRegDorm.getText().toString().trim();

        if (username.isEmpty()) {
            etRegUsername.setError("请输入用户名");
            return;
        }
        if (password.isEmpty()) {
            etRegPassword.setError("请输入密码");
            return;
        }
        if (name.isEmpty()) {
            etRegName.setError("请输入姓名");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("注册中...");

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("name", name);
        params.put("studentId", studentId);
        params.put("dorm", dorm);

        HttpUtil.post("/api/register", params, new HttpUtil.HttpCallback() {
            @Override
            public void onSuccess(String response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("注 册");

                try {
                    Map<String, Object> result = HttpUtil.parseResponse(response,
                            new com.google.gson.reflect.TypeToken<Map<String, Object>>() {}.getType());

                    double code = ((Number) result.get("code")).doubleValue();
                    String msg = (String) result.get("msg");
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();

                    if (code == 200) {
                        // 注册成功，切换到登录
                        cardRegister.setVisibility(View.GONE);
                        ((View) btnLogin.getParent().getParent()).setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                        btnGoRegister.setVisibility(View.VISIBLE);
                        etUsername.setText(username);
                        etPassword.setText("");
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                btnRegister.setEnabled(true);
                btnRegister.setText("注 册");
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
