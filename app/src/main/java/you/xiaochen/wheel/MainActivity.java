package you.xiaochen.wheel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import you.xiaochen.wheel.widget.DatePickerDialog;
import you.xiaochen.wheel.widget.WheelView;

public class MainActivity extends AppCompatActivity {
    private WheelView wv_city, wv_county, wv_name;
    private WheelView wv_number;

    private CityAdapter cityAdapter;
    private CountyAdapter countyAdapter;

    private TextView tv_city, tv_county, tv_number, tvDatePicker;

    private DatePickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("游小陈的博客");

        wv_city = (WheelView) findViewById(R.id.wv_city);
        wv_county = (WheelView) findViewById(R.id.wv_county);
        wv_number = (WheelView) findViewById(R.id.wv_number);
        wv_name = (WheelView) findViewById(R.id.wv_name);
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_county = (TextView) findViewById(R.id.tv_county);
        tv_number = (TextView) findViewById(R.id.tv_number);

        tvDatePicker = (TextView) findViewById(R.id.datePicker);

        /* 市滑轮控件 */

        cityAdapter = new CityAdapter();
        wv_city.setAdapter(cityAdapter);
        wv_city.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, int index) {
                tv_city.setText("市: "+cityAdapter.getItem(index));
                List<String> strs = Arrays.asList(TestDatas.AREAS[index]);
                countyAdapter.strs.clear();
                countyAdapter.strs.addAll(strs);
                countyAdapter.notifyDataSetChanged();
                wv_county.setCurrentItem(0);
                tv_county.setText("县: "+countyAdapter.getItem(0));
            }
        });



        /* 区滑轮控件 */
        wv_county.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, int index) {
                tv_county.setText("县: "+countyAdapter.getItem(index));
            }
        });

        countyAdapter = new CountyAdapter();
        wv_county.setAdapter(countyAdapter);

        /*  名字适配  */
        wv_name.setAdapter(new WheelView.WheelAdapter() {
            @Override
            protected int getItemCount() {
                return 20;
            }

            @Override
            protected String getItem(int index) {
                return "游小陈";
            }
        });

        /* 水平滑轮控件 */
        wv_number.setAdapter(new WheelView.WheelAdapter() {
            @Override
            protected int getItemCount() {
                return 100;
            }

            @Override
            protected String getItem(int index) {
                return String.valueOf(index);
            }
        });
        wv_number.setOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, int index) {
                tv_number.setText("水平布局"+index);
            }
        });
        wv_number.setCurrentItem(88);


        dialog = new DatePickerDialog(this);
        dialog.setDate(Calendar.getInstance(), false);
        dialog.setOnDateSelectedListener(new DatePickerDialog.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int dayOfMonth) {
                tvDatePicker.setText(String.format("%s年%s月%s日", year, month + 1, dayOfMonth));
            }
        });

        //Activity2.lanuch(this);
    }

    public void viewClick(View view) {
        String tag = (String) view.getTag();
        switch (tag) {
            case "WheelRecyclerView":
                Activity2.lanuch(this);
                break;
            default:
                dialog.show();
        }
    }

    private static class CityAdapter extends WheelView.WheelAdapter {
        @Override
        protected int getItemCount() {
            return TestDatas.NAMES.length;
        }

        @Override
        protected String getItem(int index) {
            return TestDatas.NAMES[index];
        }
    }

    private static class CountyAdapter extends WheelView.WheelAdapter {
        private List<String> strs;

        CountyAdapter() {
            strs = new ArrayList<>();
        }

        @Override
        protected int getItemCount() {
            return strs.size();
        }

        @Override
        protected String getItem(int index) {
            return strs.get(index);
        }
    }

}
