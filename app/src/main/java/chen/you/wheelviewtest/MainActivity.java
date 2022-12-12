package chen.you.wheelviewtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chen.you.wheel.WheelView;

public class MainActivity extends AppCompatActivity {

    private WheelView wv_city, wv_county, wv_name;

    private CityAdapter cityAdapter;
    private CountyAdapter countyAdapter;

    private TextView tv_city, tv_county, tv_number;

    private WheelView wv_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("游小陈的博客");
        wv_city = findViewById(R.id.wv_city);
        wv_county = findViewById(R.id.wv_county);
        wv_number = findViewById(R.id.wv_number);
        wv_name = findViewById(R.id.wv_name);
        tv_city = findViewById(R.id.tv_city);
        tv_county = findViewById(R.id.tv_county);
        tv_number = findViewById(R.id.tv_number);


        /* 市滑轮控件 */

        cityAdapter = new CityAdapter();
        wv_city.setAdapter(cityAdapter);
        wv_city.addOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, int index) {
                tv_city.setText("市: "+cityAdapter.getItemString(index));
                List<String> strs = Arrays.asList(TestDatas.AREAS[index]);
                countyAdapter.strs.clear();
                countyAdapter.strs.addAll(strs);
                countyAdapter.notifyDataSetChanged();
                wv_county.setCurrentItem(0);
                tv_county.setText("县: "+countyAdapter.getItemString(0));
            }
        });

        /* 区滑轮控件 */

        wv_county.addOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, int index) {
                tv_county.setText("县: "+countyAdapter.getItemString(index));
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
            protected String getItemString(int position) {
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
            protected String getItemString(int position) {
                return String.valueOf(position);
            }
        });
        wv_number.addOnItemSelectedListener(new WheelView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelView wheelView, int index) {
                tv_number.setText("水平布局"+index);
            }
        });
        wv_number.setCurrentItem(88);

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TwoActivity.class));
            }
        });
    }



    private class CityAdapter extends WheelView.WheelAdapter {
        @Override
        protected int getItemCount() {
            return TestDatas.NAMES.length;
        }

        @Override
        protected String getItemString(int position) {
            return TestDatas.NAMES[position];
        }
    }

    private class CountyAdapter extends WheelView.WheelAdapter {

        private List<String> strs;

        CountyAdapter() {
            strs = new ArrayList<>();
        }

        @Override
        protected int getItemCount() {
            return strs.size();
        }

        @Override
        protected String getItemString(int position) {
            return strs.get(position);
        }
    }

}
