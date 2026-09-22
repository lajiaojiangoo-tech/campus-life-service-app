package cn.edu.gdpnc.jsjxy.mmt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 网络请求工具类
 * 封装OkHttp，提供GET/POST/PUT异步请求方法
 */
public class HttpUtil {

    // Web服务端地址，模拟器用10.0.2.2，真机改为实际IP
    public static String BASE_URL = "http://10.0.2.2:5000";

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    private static final Gson gson = new Gson();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface HttpCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }

    /**
     * GET请求
     */
    public static void get(String path, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure("网络请求失败：" + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                mainHandler.post(() -> callback.onSuccess(body));
            }
        });
    }

    /**
     * POST请求
     */
    public static void post(String path, Object bodyObj, HttpCallback callback) {
        String json = gson.toJson(bodyObj);
        RequestBody body = RequestBody.create(json, JSON_TYPE);

        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure("网络请求失败：" + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                mainHandler.post(() -> callback.onSuccess(respBody));
            }
        });
    }

    /**
     * PUT请求
     */
    public static void put(String path, Object bodyObj, HttpCallback callback) {
        String json = gson.toJson(bodyObj);
        RequestBody body = RequestBody.create(json, JSON_TYPE);

        Request request = new Request.Builder()
                .url(BASE_URL + path)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onFailure("网络请求失败：" + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                mainHandler.post(() -> callback.onSuccess(respBody));
            }
        });
    }

    /**
     * 解析JSON响应
     */
    public static <T> T parseResponse(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T parseResponse(String json, Type type) {
        return gson.fromJson(json, type);
    }

    // ========== 用户登录状态管理 ==========

    private static final String SP_USER = "user_sp";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAME = "name";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_DORM = "dorm";

    /**
     * 保存登录用户信息
     */
    public static void saveLoginUser(Context context, String userId, String username,
                                      String name, String studentId, String dorm) {
        SharedPreferences sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_NAME, name)
                .putString(KEY_STUDENT_ID, studentId)
                .putString(KEY_DORM, dorm)
                .apply();
    }

    /**
     * 获取当前登录用户ID
     */
    public static String getUserId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        return sp.getString(KEY_USER_ID, "");
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUserName(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        return sp.getString(KEY_NAME, "未登录");
    }

    /**
     * 获取当前登录用户学号
     */
    public static String getStudentId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        return sp.getString(KEY_STUDENT_ID, "");
    }

    /**
     * 获取当前登录用户宿舍
     */
    public static String getDorm(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        return sp.getString(KEY_DORM, "");
    }

    /**
     * 是否已登录
     */
    public static boolean isLoggedIn(Context context) {
        return !getUserId(context).isEmpty();
    }

    /**
     * 退出登录
     */
    public static void logout(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
