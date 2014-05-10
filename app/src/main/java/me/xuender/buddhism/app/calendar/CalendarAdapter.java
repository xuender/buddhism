package me.xuender.buddhism.app.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

/**
 * Created by ender on 14-5-10.
 */
public class CalendarAdapter extends FragmentPagerAdapter {
    private Fragment[] fragments = new Fragment[12];
    private int year;

    public CalendarAdapter(FragmentManager fm) {
        super(fm);
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
    }

    private Fragment createFragment(int year, int month) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("y", year);
        bundle.putInt("m", month);
        fragment.setArguments(bundle);
        return fragment;
    }

    private Fragment getFragment(int p) {
        if (fragments[p] == null) {
            fragments[p] = createFragment(year, p);
        }
        return fragments[p];
    }

    @Override
    public Fragment getItem(int position) {
        return getFragment(position);
    }

    @Override
    public int getCount() {
        return 12;
    }
}