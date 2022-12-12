package chen.you.wheelviewtest;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import chen.you.wheel.SimpleDrawManager;
import chen.you.wheel.WheelDrawManager;
import chen.you.wheel.WheelParams;
import chen.you.wheel.WheelView;

/**
 * author: you : 2022/12/8
 */
public final class TwoActivity extends AppCompatActivity implements View.OnClickListener {

    private WheelView wv;

    private TestAdapter adapter;

    private List<String> tests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
        wv = findViewById(R.id.wv_city);
        for (int i = 0; i < 100; i++) {
            tests.add(String.valueOf(i));
        }
        adapter = new TestAdapter(tests);
        wv.setAdapter(adapter);

        findViewById(R.id.bt0).setOnClickListener(this);
        findViewById(R.id.bt1).setOnClickListener(this);
        findViewById(R.id.bt2).setOnClickListener(this);
        findViewById(R.id.bt3).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt0:
                tests.clear();
                for (int i = 0; i < 100; i++) {
                    tests.add("new test " + i);
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.bt1:
//                WheelParams params0 = new WheelParams.Builder().setOrientation(WheelParams.HORIZONTAL).setTextCenterColor(Color.RED).build();
                WheelParams params = wv.getWheelParams().newBuilder().setOrientation(WheelParams.HORIZONTAL).setTextCenterColor(Color.RED).build();
                wv.setWheelParams(params);
                break;
            case R.id.bt2:
                if (wv.getDrawManager() instanceof SimpleDrawManager) {
                    wv.setDrawManager(new WheelDrawManager());
                } else {
                    wv.setDrawManager(new SimpleDrawManager());
                }
                break;
            case R.id.bt3:
                wv.setAdapter(new WheelView.WheelAdapter() {
                    @Override
                    protected String getItemString(int position) {
                        return "position " + position;
                    }

                    @Override
                    protected int getItemCount() {
                        return 100;
                    }
                });
                break;
        }
    }

    static class TestAdapter extends WheelView.WheelAdapter {

        List<String> strs;

        public TestAdapter(List<String> strs) {
            this.strs = strs;
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
