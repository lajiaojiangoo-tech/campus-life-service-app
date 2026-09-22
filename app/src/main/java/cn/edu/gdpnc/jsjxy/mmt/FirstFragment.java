package cn.edu.gdpnc.jsjxy.mmt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private TextView tvWelcome;

    public FirstFragment() {
        super(R.layout.fragment_first);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 显示欢迎文字
        tvWelcome = view.findViewById(R.id.tvWelcome);
        String name = HttpUtil.getUserName(requireContext());
        if (!name.isEmpty() && !name.equals("未登录")) {
            tvWelcome.setText("你好，" + name);
        }

        androidx.recyclerview.widget.RecyclerView rv = view.findViewById(R.id.rvMenu);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        List<MenuItemModel> list = new ArrayList<>();
        list.add(new MenuItemModel(R.drawable.ic_menu_express, R.drawable.bg_icon_express, "快递"));
        list.add(new MenuItemModel(R.drawable.ic_menu_print, R.drawable.bg_icon_print, "打印"));
        list.add(new MenuItemModel(R.drawable.ic_menu_food, R.drawable.bg_icon_food, "饮食"));
        list.add(new MenuItemModel(R.drawable.ic_menu_hotwater, R.drawable.bg_icon_hotwater, "热水"));
        list.add(new MenuItemModel(R.drawable.ic_menu_internet, R.drawable.bg_icon_internet, "上网"));

        MenuAdapter adapter = new MenuAdapter(list, item -> {
               Intent intent = null;
            switch (item.title) {
                case "快递":
                    intent = new Intent(requireContext(), ExpressActivity.class);
                    break;
                case "打印":
                    intent = new Intent(requireContext(), PrintActivity.class);
                    break;
                case "饮食":
                    intent = new Intent(requireContext(), FoodActivity.class);
                    break;
                case "热水":
                    intent = new Intent(requireContext(), HotWaterActivity.class);
                    break;
                case "上网":
                    intent = new Intent(requireContext(), InternetActivity.class);
                    break;
            }
            if (intent != null) startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 更新欢迎文字
        if (tvWelcome != null) {
            String name = HttpUtil.getUserName(requireContext());
            if (!name.isEmpty() && !name.equals("未登录")) {
                tvWelcome.setText("你好，" + name);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvWelcome = null;
    }
}
