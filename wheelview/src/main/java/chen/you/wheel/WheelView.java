package chen.you.wheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 真正的3D WheelView
 * Created by you on 2017/3/20.
 * 作QQ:86207610
 */
public final class WheelView extends ViewGroup {
    //无效的位置
    public static final int IDLE_POSITION = -1;
    //没有指定宽或高时的默认大小
    private static final int DEF_SIZE = WheelParams.dp2px(128);
    //WheelView相关参数
    private WheelParams mWheelParams;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    //item绘制器
    private DrawManager mDrawManager;
    //RecyclerView adapter并item绘制画笔
    private WheelViewAdapter mWheelViewAdapter;
    //WheelView Adapter
    private Adapter mAdapter;
    private WheelViewObserver mObserver;
    //滑动监听用于selectedIndex回调
    private OnScrollListener mScrollListener;

    //当前选中的项
    private int mSelectedPosition = IDLE_POSITION;
    //itemSelected
    private List<OnItemSelectedListener> mSelectedListeners;
    //WheelView是否已经附着到窗体中
    private boolean hasAttachedToWindow = false;

    public WheelView(Context context) {
        super(context);
        initialize(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        mWheelParams = new WheelParams.Builder(context, attrs).build();
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setId(ViewCompat.generateViewId());
        mRecyclerView.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(context, mWheelParams.getLayoutOrientation(), false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //让滑动结束时都能定到中心位置
        new LinearSnapHelper().attachToRecyclerView(mRecyclerView);

        mWheelViewAdapter = new WheelViewAdapter(mWheelParams);
        mRecyclerView.setAdapter(mWheelViewAdapter);
        mDrawManager = new WheelDrawManager();
        mDrawManager.setWheelParams(mWheelParams);

        mScrollListener = new OnScrollListener();
        super.addView(mRecyclerView, -1, createLayoutParams());
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View...) is not supported in WheelView");
    }

    @Override
    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported in WheelView");
    }

    @Override
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in WheelView");
    }

    @Override
    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in WheelView");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingLeftRight = getPaddingLeft() + getPaddingRight();
        int paddingTopBottom = getPaddingTop() + getPaddingBottom();
        int childState = mRecyclerView.getMeasuredState();
        LayoutParams childParams = mRecyclerView.getLayoutParams();
        LayoutParams layoutParams = getLayoutParams();
        if (mWheelParams.isVertical()) {
            //非精准测量给默认值且不是linearlayout的layout_weight或者ConstraintLayout等之类的权重布局
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
                    && (layoutParams != null && layoutParams.width != 0)) {
                childParams.width = Math.max(DEF_SIZE, getSuggestedMinimumWidth() - paddingLeftRight);
            }
        } else {
            if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
                    && (layoutParams != null && layoutParams.height != 0)) {
                childParams.height = Math.max(DEF_SIZE, getSuggestedMinimumHeight() - paddingTopBottom);
            }
        }
        measureChild(mRecyclerView, widthMeasureSpec, heightMeasureSpec);
        int width = mRecyclerView.getMeasuredWidth() + paddingLeftRight;
        int height = mRecyclerView.getMeasuredHeight() + paddingTopBottom;
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec, childState));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        mRecyclerView.layout(left, top, left + width, top + height);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hasAttachedToWindow = true;
        mRecyclerView.addItemDecoration(mDrawManager);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hasAttachedToWindow = false;
        mRecyclerView.removeOnScrollListener(mScrollListener);
        mRecyclerView.removeItemDecoration(mDrawManager);
    }

    /**
     *  创建WheelView的LayoutParams
     */
    private LayoutParams createLayoutParams() {
        if (mWheelParams.isVertical())
            return new LayoutParams(LayoutParams.MATCH_PARENT, mWheelParams.getTotalItemSize());
        return new LayoutParams(mWheelParams.getTotalItemSize(), LayoutParams.MATCH_PARENT);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onDataSetChanged() {
        mWheelViewAdapter.notifyDataSetChanged();
    }

    /**
     * 分发selected事件监听
     */
    private void dispatchOnSelectIndexChanged(int index) {
        if (mSelectedPosition == index) return;
        mSelectedPosition = index;
        if (mSelectedListeners != null) {
            for (OnItemSelectedListener listener : mSelectedListeners) {
                listener.onItemSelected(this, index);
            }
        }
    }

    /**
     * 设置适配器, 有实现的通用的 {@link WheelAdapter } , {@link Adapter}
     * @param adapter
     */
    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.setWheelViewObserver(null);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            if (mObserver == null) {
                mObserver = new WheelViewObserver();
            }
            mAdapter.setWheelViewObserver(mObserver);
            this.mSelectedPosition = IDLE_POSITION;
            this.mWheelViewAdapter.adapter = adapter;
            onDataSetChanged();
            mLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    /**
     * 设置params, 用于代码生成WheelView时
     * @param wheelParams 新的参数 详见{@link WheelParams.Builder}
     */
    public void setWheelParams(WheelParams wheelParams) {
        if (mWheelParams == wheelParams || mWheelParams == null) return;
        mRecyclerView.removeItemDecoration(mDrawManager);
        mWheelParams = wheelParams;
        mDrawManager.setWheelParams(mWheelParams);
        mWheelViewAdapter = new WheelViewAdapter(mWheelParams);
        mWheelViewAdapter.adapter = mAdapter;
        mLayoutManager.setOrientation(mWheelParams.getLayoutOrientation());
        mRecyclerView.setAdapter(mWheelViewAdapter);
        if (hasAttachedToWindow) {
            mRecyclerView.addItemDecoration(mDrawManager);
        }
        mRecyclerView.setLayoutParams(createLayoutParams());
    }

    /**
     * 设置DrawManager
     */
    public void setDrawManager(DrawManager drawManager) {
        if (drawManager == null) return;
        mRecyclerView.removeItemDecoration(mDrawManager);
        mDrawManager = drawManager;
        mDrawManager.setWheelParams(mWheelParams);
        if (hasAttachedToWindow) {
            mRecyclerView.addItemDecoration(mDrawManager);
        }
        mRecyclerView.setLayoutParams(createLayoutParams());
    }

    /**
     * 设置当前item位置
     */
    public void setCurrentItem(int position) {
        mLayoutManager.scrollToPositionWithOffset(position, 0);
    }

    public void addOnItemSelectedListener(OnItemSelectedListener listener) {
        if (mSelectedListeners == null) mSelectedListeners = new ArrayList<>();
        mSelectedListeners.add(listener);
    }

    public void removeOnItemSelectedListener(OnItemSelectedListener listener) {
        if (mSelectedListeners != null) {
            mSelectedListeners.remove(listener);
        }
    }

    public int getCurrentItem() {
        return mDrawManager.centerItemPosition;
    }

    public WheelParams getWheelParams() {
        return mWheelParams;
    }

    public DrawManager getDrawManager() {
        return mDrawManager;
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    /** -------------------------- inner class --------------------------*/

    /**
     * Item selected
     */
    public interface OnItemSelectedListener {

        void onItemSelected(WheelView wheelView, int index);
    }

    /**
     * open WheelView Adapter
     */
    public static abstract class Adapter {

        private DataSetObserver wheelObserver;

        void setWheelViewObserver(DataSetObserver observer) {
            synchronized (this) {
                wheelObserver = observer;
            }
        }

        /**
         * 可以根据实际需求, 刷新后{@link #setCurrentItem(int) 0}
         */
        public final void notifyDataSetChanged() {
            synchronized (this) {
                if (wheelObserver != null) {
                    wheelObserver.onChanged();
                }
            }
        }

        protected abstract int getItemCount();

        /**
         * 绘制内容区域
         * @param c Canvas
         * @param p TextPaint
         * @param cf 居中时根据中心Y坐标 - textHeight
         * @param itemRect 绘制内容区域
         * @param position adapter index
         * @param params WheelParams
         */
        protected abstract void drawItem(Canvas c, Paint p, float cf, Rect itemRect, int position, WheelParams params);

        /**
         * 绘制分割线
         * @param c Canvas
         * @param p Paint
         * @param wvRect Wheel Rect
         * @param params WheelParams
         */
        protected void drawDivider(Canvas c, Paint p, Rect wvRect, WheelParams params) {
            if (params.isVertical()) {
                float dividerOff = (wvRect.height() - params.itemSize) / 2.0f;
                float firstY = wvRect.top + dividerOff - params.dividerPadding;
                c.drawLine(wvRect.left, firstY, wvRect.right, firstY, p);
                float secondY = wvRect.bottom - dividerOff + params.dividerPadding;
                c.drawLine(wvRect.left, secondY, wvRect.right, secondY, p);
            } else {
                float dividerOff = (wvRect.width() - params.itemSize) / 2.0f;
                float firstX = wvRect.left + dividerOff - params.dividerPadding;
                c.drawLine(firstX, wvRect.top, firstX, wvRect.bottom, p);
                float secondX = wvRect.right - dividerOff + params.dividerPadding;
                c.drawLine(secondX, wvRect.top, secondX, wvRect.bottom, p);
            }
        }
    }

    /**
     * 直接画文本适配器
     */
    public static abstract class WheelAdapter extends Adapter {

        // item toString
        protected abstract String getItemString(int position);

        @Override
        protected void drawItem(Canvas c, Paint p, float cf, Rect itemRect, int position, WheelParams params) {
            String text = getItemString(position);
            if (text == null) text = "";
            c.drawText(text, itemRect.exactCenterX(), itemRect.exactCenterY() - cf, p);
        }

    }

    /**
     * Adapter观察者
     */
    private class WheelViewObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            onDataSetChanged();
        }
    }

    /**
     * WheelView滑动监听
     */
    private class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
            if (mDrawManager.centerItemPosition == IDLE_POSITION) return;
            dispatchOnSelectIndexChanged(mDrawManager.centerItemPosition);
        }
    }

    /**
     * Wheel Item绘制管理
     */
    public static abstract class DrawManager extends RecyclerView.ItemDecoration {
        //Wheel相关参数
        WheelParams wheelParams;
        //中心位置
        int centerItemPosition = IDLE_POSITION;
        //整个WheelView的显示区域
        final Rect wvRect = new Rect();
        //item显示区域
        final Rect itemRect = new Rect();
        //中心偏移值即为itemSize / 2
        float centerItemScrollOff;

        void setWheelParams(WheelParams params) {
            params.setItemShowOrder(getShowOrder());
            this.wheelParams = params;
            centerItemScrollOff = params.itemSize / 2.f;
        }

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if (wheelParams == null) return;
            if (!(parent.getLayoutManager() instanceof LinearLayoutManager)) return;
            if (!(parent.getAdapter() instanceof WheelItemPainter)) return;
            LinearLayoutManager llm = (LinearLayoutManager) parent.getLayoutManager();
            WheelItemPainter painter = (WheelItemPainter) parent.getAdapter();
            wvRect.set(parent.getPaddingLeft(), parent.getPaddingTop(),
                    parent.getWidth() - parent.getPaddingRight(),
                    parent.getHeight() - parent.getPaddingBottom());
            int startPosition = llm.findFirstVisibleItemPosition();
            if (startPosition < 0) return;
            int endPosition = llm.findLastVisibleItemPosition();
            centerItemPosition = IDLE_POSITION;
            for (int itemPosition = startPosition; itemPosition <= endPosition; itemPosition++) {
                View itemView = llm.findViewByPosition(itemPosition);
                if (itemView == null) continue;
                int adapterPosition = parent.getChildAdapterPosition(itemView);
                if (adapterPosition < wheelParams.getShowItemCount()) continue;//itemCount为空白项,不考虑
                if (adapterPosition >= llm.getItemCount() - wheelParams.getShowItemCount()) break;//超过列表的也是空白项

                itemRect.set(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                drawItem(painter, c, adapterPosition);
            }
            painter.drawDivider(c, wvRect);
        }

        //画item
        abstract void drawItem(WheelItemPainter painter, Canvas c, int adapterPosition);

        //item显示规则
        abstract WheelParams.ItemShowOrder getShowOrder();
    }

    /**
     * Wheel Item绘制器
     */
    interface WheelItemPainter {

        //画item
        void drawItem(Canvas c, Rect itemRect, int alpha, int position);

        //画中心item
        void drawCenterItem(Canvas c, Rect itemRect, int alpha, int position);

        //画分割线
        void drawDivider(Canvas c, Rect wvRect);
    }

    /**
     * WheelView适配器代理
     */
    static class WheelViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements WheelItemPainter {
        //wheel params
        final WheelParams wheelParams;
        //wheel adapter
        Adapter adapter = null;

        //text画笔
        private final Paint textPaint;
        //分割线Paint
        private final Paint dividerPaint;
        //画文本居中时文本画笔的中心位置, 画居中文字时
        private final float textFontCenter;

        WheelViewAdapter(WheelParams wheelParams) {
            this.wheelParams = wheelParams;

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(wheelParams.textSize);
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            textFontCenter = (fm.bottom + fm.top) / 2.0f;

            dividerPaint = new Paint();
            dividerPaint.setAntiAlias(true);
            dividerPaint.setStrokeWidth(wheelParams.dividerSize);
            dividerPaint.setColor(wheelParams.dividerColor);
        }

        @Override
        public int getItemCount() {
            return wheelParams.getShowItemCount() * 2 + (adapter == null ? 0 : adapter.getItemCount());
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new View(parent.getContext());
            if (wheelParams.isVertical()) {
                view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, wheelParams.itemSize));
            } else {
                view.setLayoutParams(new LayoutParams(wheelParams.itemSize, LayoutParams.MATCH_PARENT));
            }
            view.setVisibility(View.INVISIBLE); //不显示只留测量性能更忧
            return new RecyclerView.ViewHolder(view) {};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        }

        @Override
        public void drawItem(Canvas c, Rect itemRect, int alpha, int position) {
            if (adapter != null) {
                textPaint.setColor(wheelParams.textColor);
                textPaint.setAlpha(alpha);
                adapter.drawItem(c, textPaint, textFontCenter, itemRect, position, wheelParams);
            }
        }

        @Override
        public void drawCenterItem(Canvas c, Rect itemRect, int alpha, int position) {
            if (adapter != null) {
                textPaint.setColor(wheelParams.textCenterColor);
                textPaint.setAlpha(alpha);
                adapter.drawItem(c, textPaint, textFontCenter, itemRect, position, wheelParams);
            }
        }

        @Override
        public void drawDivider(Canvas c, Rect wvRect) {
            if (adapter != null) {
                adapter.drawDivider(c, dividerPaint, wvRect, wheelParams);
            }
        }
    }
}
