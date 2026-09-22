package cn.edu.gdpnc.jsjxy.mmt;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RecordsPagerAdapter extends FragmentStateAdapter {

    private static final String[] TITLES = {"打印", "订餐", "热水", "上网", "钱包"};

    public RecordsPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PrintRecordFragment();
            case 1: return new OrderRecordFragment();
            case 2: return new HotWaterRecordFragment();
            case 3: return new InternetRecordFragment();
            case 4: return new WalletRecordFragment();
            default: return new PrintRecordFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TITLES.length;
    }

    public static String getTitle(int position) {
        return TITLES[position];
    }
}
