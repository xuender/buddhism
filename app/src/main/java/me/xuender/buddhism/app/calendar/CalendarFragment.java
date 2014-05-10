package me.xuender.buddhism.app.calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.xuender.buddhism.app.R;

/**
 * Created by ender on 14-5-10.
 */
public class CalendarFragment extends Fragment {
    private View rootView;
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
            context = rootView.getContext();
            final LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.layout);
            int start = 0;
            List<Day> days = getDays();
            LinearLayout row = null;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, getArguments().getInt("y", calendar.get(Calendar.YEAR)));
            calendar.set(Calendar.MONTH, getArguments().getInt("m", calendar.get(Calendar.MONTH)));
            calendar.set(Calendar.DATE, 1);
            if (row != null) {
                while (row.getChildCount() < 7) {
                    addView(row, true);
                }
            }
            int endDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int d = 0; d < endDay; d++) {
                if (d == 0 || calendar.get(Calendar.DAY_OF_WEEK) == 2) {
                    if (row != null) {
                        while (row.getChildCount() < 7) {
                            addView(row, false);
                        }
                    }
                    row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setWeightSum(7);
                    row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    row.setPadding(0, 3, 0, 3);
                    layout.addView(row);
                }
//                final LinearLayout dl = new LinearLayout(context);
//                dl.setPadding(0, 3, 0, 3);
//                dl.setOrientation(LinearLayout.VERTICAL);
//                dl.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                Day day = days.remove(0);
//                if (day.isNo()) {
//                    dl.setBackgroundColor(getResources().getColor(R.color.火));
//                }
                //dl.addView(getDayView(String.valueOf(calendar.get(Calendar.DATE)), day, R.style.day));
//                dl.addView(getDayView(day.getTitle(), day, R.style.fo));
                row.addView(getDayView(day.getTitle(), day, R.style.fo));
                calendar.add(Calendar.DATE, 1);
            }
        }
        return rootView;
    }

    private TextView getDayView(String title, Day day, int style) {
        final TextView dayView = new TextView(context);
        dayView.setPadding(0, 3, 0, 3);
        if (day.isNo()) {
            dayView.setBackgroundColor(getResources().getColor(R.color.火));
        }
        dayView.setTextAppearance(context, style);
        dayView.setText(title);
        dayView.setGravity(Gravity.CENTER);
//        dayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
        dayView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        if (day.isNow()) {
            dayView.setTextColor(getResources().getColor(R.color.木));
        }
        return dayView;
    }

    private void addView(LinearLayout row, boolean end) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, android.R.style.TextAppearance_Small);
        textView.setText("");
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        if (end) {
            row.addView(textView);
        } else {
            row.addView(textView, 0);
        }
    }

    private List<Day> getDays() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd");
        try {
            SharedPreferences notes = context.getSharedPreferences("calendar", Context.MODE_PRIVATE);
            JSONArray array = new JSONArray(notes.getString("day", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Date date = new Date(obj.getLong("c"));
                ChineseCalendar.addS(sdf.format(date), getString(R.string.yang) + obj.getString("t"));
                ChineseCalendar chineseCalendar = new ChineseCalendar(date);
                //chineseCalendar.computeChineseFields();
                ChineseCalendar.addL(getKey(chineseCalendar.get(ChineseCalendar.CHINESE_MONTH),
                                chineseCalendar.get(ChineseCalendar.CHINESE_DATE)),
                        getString(R.string.yin) + obj.getString("t")
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Day> days = new ArrayList<Day>();
        Calendar calendar = Calendar.getInstance();
        Calendar now = (Calendar) calendar.clone();
        calendar.set(Calendar.YEAR, getArguments().getInt("y", calendar.get(Calendar.YEAR)));
        calendar.set(Calendar.MONTH, getArguments().getInt("m", calendar.get(Calendar.MONTH)));
        calendar.set(Calendar.DATE, 1);
        int endDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int d = 0; d < endDay; d++) {
            days.add(getDay(calendar, calendar.equals(now)));
            calendar.add(Calendar.DATE, 1);
        }
        return days;
    }

    private Day getDay(Calendar calendar, boolean now) {
        ChineseCalendar chineseCalendar = new ChineseCalendar(calendar);
        Day day = new Day(calendar.get(Calendar.DATE),
                chineseCalendar.getChinese(ChineseCalendar.CHINESE_TERM_OR_DATE),
                chineseCalendar.getChinese(ChineseCalendar.LUNAR_FESTIVAL),
                chineseCalendar.getChinese(ChineseCalendar.FO_FESTIVAL));
        if (now) {
            day.now();
        }
//        day.setName(chineseCalendar.getChinese(ChineseCalendar.CHINESE_DATE));
        return day;
    }

    private String getKey(int m, int d) {
        StringBuilder sb = new StringBuilder();
        if (m < 10) {
            sb.append('0');
        }
        sb.append(m);
        if (d < 10) {
            sb.append('0');
        }
        sb.append(d);
        return sb.toString();
    }
}
