package you.xiaochen.wheel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import you.xiaochen.wheel.R;

/**
 * Created by you on 2017/9/25.
 * 作QQ:86207610
 */

public class WheelView extends ViewGroup {
    /**
     * 无效的位置
     */
    public static final int IDLE_POSITION = -1;
    /**
     * 垂直与水平布局两种状态
     */
    public static final int WHEEL_VERTICAL = 1;
    public static final int WHEEL_HORIZONTAL = 2;
    /**
     * item color
     */
    private int textColor = Color.BLACK;
    /**
     * 中心item颜色
     */
    private int textColorCenter = Color.RED;
    /**
     * 分割线颜色
     */
    private int dividerColor = Color.BLACK;
    /**
     * 文本大小
     */
    private float textSize = 36.f;
    /**
     * item数量
     */
    private int itemCount = 3;
    /**
     * item大小
     */
    private int itemSize = 90;
    /**
     * 分割线之间距离
     */
    private int dividerSize = 90;
    /**
     * 布局方向
     */
    private int orientation = WHEEL_VERTICAL;
    /**
     * 对齐方式
     */
    private int gravity = WheelDecoration.GRAVITY_CENTER;

    /**
     * recyclerView
     */
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    /**
     * wheel 3d
     */
    private WheelDecoration wheelDecoration;
    private WheelViewAdapter wheelAdapter;
    /**
     * adapter
     */
    private WheelAdapter adapter;

    private int lastSelectedPosition = IDLE_POSITION;
    private int selectedPosition = IDLE_POSITION;

    public WheelView(Context context) {
        super(context);
        init(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
            itemCount = a.getInt(R.styleable.WheelView_wheelItemCount, itemCount);
            textColor = a.getColor(R.styleable.WheelView_wheelTextColor, textColor);
            textColorCenter = a.getColor(R.styleable.WheelView_wheelTextColorCenter, textColorCenter);
            dividerColor = a.getColor(R.styleable.WheelView_dividerColor, dividerColor);
            textSize = a.getDimension(R.styleable.WheelView_wheelTextSize, textSize);
            itemSize = a.getDimensionPixelOffset(R.styleable.WheelView_wheelItemSize, itemSize);
            dividerSize = a.getDimensionPixelOffset(R.styleable.WheelView_wheelDividerSize, dividerSize);
            orientation = a.getInt(R.styleable.WheelView_wheelOrientation, orientation);
            gravity = a.getInt(R.styleable.WheelView_wheelGravity, gravity);
            a.recycle();
        }
        initRecyclerView(context);
    }

    private void initRecyclerView(Context context) {
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        int totolItemSize = (itemCount * 2 + 1) * itemSize;
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(orientation == WHEEL_VERTICAL ?
                LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);
        //让滑动结束时都能定到中心位置
        new LinearSnapHelper().attachToRecyclerView(mRecyclerView);
        this.addView(mRecyclerView, WheelUtils.createLayoutParams(orientation, totolItemSize));

        wheelAdapter = new WheelViewAdapter(orientation, itemSize, itemCount);
        wheelDecoration = new SimpleWheelDecoration(wheelAdapter, gravity, textColor, textColorCenter, textSize, dividerColor, dividerSize);
        mRecyclerView.addItemDecoration(wheelDecoration);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (listener == null || wheelDecoration == null) return;
                if (wheelDecoration.centerItemPosition == IDLE_POSITION || newState != RecyclerView.SCROLL_STATE_IDLE) return;
                selectedPosition = wheelDecoration.centerItemPosition;
                if (selectedPosition != lastSelectedPosition) {
                    listener.onItemSelected(wheelDecoration.centerItemPosition);
                    lastSelectedPosition = selectedPosition;
                }
            }
        });
        mRecyclerView.setAdapter(wheelAdapter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        if (orientation == WHEEL_HORIZONTAL) {//水平布局时,最好固定高度,垂直布局时最好固定宽度
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            View child = getChildAt(0);
            width = child.getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = itemSize + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    private void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            View child = getChildAt(0);
            height = child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        }
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = itemSize + getPaddingLeft() + getPaddingRight();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() <= 0) {
            return;
        }
        View child = getChildAt(0);
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        int left, top;
        if (orientation == WHEEL_HORIZONTAL) {//水平布局时,最好固定高度,垂直布局时最好固定宽度
            int centerWidth = (getWidth() - getPaddingLeft() - getPaddingRight() - childWidth) >> 1;
            left = getPaddingLeft() + centerWidth;
            top = getPaddingTop();
        } else {
            int centerHeight = (getHeight() - getPaddingTop() - getPaddingBottom() - childHeight) >> 1;
            left = getPaddingLeft();
            top = getPaddingTop() + centerHeight;
        }
        child.layout(left, top, left + childWidth, top + childHeight);
    }

    public void setAdapter(WheelAdapter adapter) {
        this.selectedPosition = -1;
        this.lastSelectedPosition = -1;
        this.wheelAdapter.adapter = adapter;
        adapter.wheelViewAdapter = wheelAdapter;
        this.wheelAdapter.notifyDataSetChanged();
    }

    public WheelAdapter getAdapter() {
        return adapter;
    }

    public void setCurrentItem(int position) {
        layoutManager.scrollToPositionWithOffset(position, 0);
    }

    public int getCurrentItem() {
        return wheelDecoration.centerItemPosition;
    }

    private OnItemSelectedListener listener;

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * item selected
     */
    public interface OnItemSelectedListener {
        void onItemSelected(int index);
    }

    /**
     * wheel adapter
     * by you
     */
    public static abstract class WheelAdapter {

        WheelViewAdapter wheelViewAdapter;

        protected abstract int getItemCount();

        protected abstract String getItem(int index);

        public final void notifyDataSetChanged() {
            if (wheelViewAdapter != null) {
                wheelViewAdapter.notifyDataSetChanged();
            }
        }
    }

}
