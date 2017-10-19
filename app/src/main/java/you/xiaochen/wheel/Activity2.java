package you.xiaochen.wheel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by you on 2017/10/11.
 */

public class Activity2 extends AppCompatActivity {

    RecyclerView mRecyclerView;

    public static void lanuch(Context context) {
        context.startActivity(new Intent(context, Activity2.class));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new MyAdapter());

    }


    class MyAdapter extends RecyclerView.Adapter<ViewHolderTest> {

        @Override
        public int getItemCount() {
            return 20;
        }

        @Override
        public ViewHolderTest onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.wheel_item, parent, false);
            return new ViewHolderTest(item);
        }

        @Override
        public void onBindViewHolder(ViewHolderTest holder, int position) {

        }
    }

    class ViewHolderTest extends RecyclerView.ViewHolder {
        public ViewHolderTest(View itemView) {
            super(itemView);
        }
    }


}
