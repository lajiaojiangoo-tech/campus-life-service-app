package cn.edu.gdpnc.jsjxy.mmt;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrintHistoryStore {

    private static final String SP_NAME = "print_history_sp";
    private static final String KEY_LIST = "history_list";

    public static List<PrintHistoryModel> load(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_LIST, "[]");

        List<PrintHistoryModel> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String fileName = o.optString("fileName", "");
                String config = o.optString("config", "");
                String time = o.optString("time", "");
                String status = o.optString("status", "");
                list.add(new PrintHistoryModel(fileName, config, time, status));
            }
        } catch (JSONException e) {
        }
        return list;
    }


    public static void add(Context context, PrintHistoryModel item) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_LIST, "[]");

        try {
            JSONArray arr = new JSONArray(json);

            JSONObject o = new JSONObject();
            o.put("fileName", item.fileName);
            o.put("config", item.config);
            o.put("time", item.time);
            o.put("status", item.status);

            JSONArray newArr = new JSONArray();
            newArr.put(o);
            for (int i = 0; i < arr.length(); i++) {
                newArr.put(arr.get(i));
            }

            sp.edit().putString(KEY_LIST, newArr.toString()).apply();
        } catch (JSONException e) {

            JSONArray newArr = new JSONArray();
            try {
                JSONObject o = new JSONObject();
                o.put("fileName", item.fileName);
                o.put("config", item.config);
                o.put("time", item.time);
                o.put("status", item.status);
                newArr.put(o);
            } catch (JSONException ignored) {}
            sp.edit().putString(KEY_LIST, newArr.toString()).apply();
        }
    }

    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_LIST).apply();
    }
}
