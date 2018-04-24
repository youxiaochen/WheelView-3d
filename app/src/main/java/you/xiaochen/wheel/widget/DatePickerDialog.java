package you.xiaochen.wheel.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import you.xiaochen.wheel.R;

public class DatePickerDialog extends Dialog implements View.OnClickListener, WheelView.OnItemSelectedListener {

    WheelView yearWheelView;
    WheelView monthWheelView;
    WheelView dayWheelView;
    View doneView;

    WheelView.SimpleAdapter yearAdapter = new WheelView.SimpleAdapter();
    WheelView.SimpleAdapter monthAdapter = new WheelView.SimpleAdapter();
    WheelView.SimpleAdapter dayAdapter = new WheelView.SimpleAdapter();

    List<Integer> years = new ArrayList<>();

    Calendar selectedCalendar;

    int selectedYearIndex, selectedMonthIndex, selectedDayIndex;

    OnDateSelectedListener listener;

    int initYear, initMonth, initDay;

    boolean mOnlyHistory;

    public DatePickerDialog(@NonNull Context context) {
        this(context, R.style.disFloatingDialog);
    }

    public DatePickerDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams attr = getWindow().getAttributes();
        attr.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attr.gravity = Gravity.BOTTOM;
//        attr.windowAnimations = R.style.bottomWindowAnim;

        //setCanceledOnTouchOutside(true);

        setContentView(R.layout.dialog_date_picker);
        View root = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        if (root != null) {
            yearWheelView = root.findViewWithTag("yearWheel");
            monthWheelView = root.findViewWithTag("monthWheel");
            dayWheelView = root.findViewWithTag("dayWheel");
            doneView = root.findViewWithTag("done");
            doneView.setOnClickListener(this);
        }
    }

    public void setOnDateSelectedListener(OnDateSelectedListener l) {
        listener = l;
    }

    public void setDate(Calendar calendar) {
        setDate(calendar, true);
    }

    public void setDate(Calendar calendar, boolean onlyHistory) {
        mOnlyHistory = onlyHistory;
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        selectedCalendar = calendar;
        int year, month, x;
        year = calendar.get(Calendar.YEAR);
        initMonth = selectedMonthIndex = calendar.get(Calendar.MONTH);
        month = calendar.get(Calendar.DAY_OF_MONTH);
        initDay = selectedDayIndex =  month - 1;
        for (int i = 10; i > 0; i--) {
            x = year - i;
            years.add(x);
            yearAdapter.add(String.format("%s年", x));
        }
        years.add(year);
        yearAdapter.add(String.format("%s年", year));
        initYear = selectedYearIndex = years.size() - 1;
        if (!onlyHistory) {
            for (int i = 1; i < 11; i++) {
                x = year + i;
                years.add(x);
                yearAdapter.add(String.format("%s年", x));
            }
        }
        updateMonths();
        updateDays();
    }

    private void updateMonths() {
        selectedCalendar.set(Calendar.YEAR, years.get(selectedYearIndex));
        int months = selectedCalendar.getActualMaximum(Calendar.MONTH) + 1;
        int x;
        monthAdapter.clear();
        for (int i = 0; i < months; i++) {
            if (mOnlyHistory && selectedYearIndex == initYear) {
                if (i > initMonth) continue;
                x = i + 1;
                monthAdapter.add(String.format("%s月", x));
            } else {
                x = i + 1;
                monthAdapter.add(String.format("%s月", x));
            }
        }
        monthAdapter.notifyDataSetChanged();
        if (monthWheelView != null) {
            if (selectedMonthIndex >= monthAdapter.size()) {
                selectedMonthIndex = monthAdapter.size() - 1;
            }
            monthWheelView.setCurrentItem(selectedMonthIndex);
        }
    }

    private void updateDays() {
        selectedCalendar.set(Calendar.YEAR, years.get(selectedYearIndex));
        selectedCalendar.set(Calendar.MONTH, selectedMonthIndex);
        int daysOfMonth = selectedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int x;
        dayAdapter.clear();
        for (int i = 0; i < daysOfMonth; i++) {
            if (mOnlyHistory && selectedYearIndex == initYear
                    && selectedMonthIndex == initMonth) {
                if (i > initDay) continue;
                x = i + 1;
                dayAdapter.add(String.format("%s日", x));
            } else {
                x = i + 1;
                dayAdapter.add(String.format("%s日", x));
            }
        }
        dayAdapter.notifyDataSetChanged();
        if (dayWheelView != null) {
            if (selectedDayIndex >= dayAdapter.size()) {
                selectedDayIndex = dayAdapter.size() - 1;
            }
            dayWheelView.setCurrentItem(selectedDayIndex);
        }
    }

    @Override
    public void onItemSelected(WheelView wheelView, int index) {
        String tag = wheelView.getTag().toString();
        switch (tag) {
            case "yearWheel":
                selectedYearIndex = index;
                updateMonths();
                updateDays();
                break;
            case "monthWheel":
                selectedMonthIndex = index;
                updateDays();
                break;
            case "dayWheel":
                selectedDayIndex = index;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        yearWheelView.setOnItemSelectedListener(this);
        yearWheelView.setAdapter(yearAdapter);
        yearWheelView.setCurrentItem(selectedYearIndex);
        monthWheelView.setOnItemSelectedListener(this);
        monthWheelView.setAdapter(monthAdapter);
        monthWheelView.setCurrentItem(selectedMonthIndex);
        dayWheelView.setOnItemSelectedListener(this);
        dayWheelView.setAdapter(dayAdapter);
        dayWheelView.setCurrentItem(selectedDayIndex);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onDateSelected(years.get(selectedYearIndex), selectedMonthIndex, selectedDayIndex + 1);
        }
        dismiss();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int dayOfMonth);
    }
}
