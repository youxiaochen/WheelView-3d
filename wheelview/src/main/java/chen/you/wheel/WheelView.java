package chen.you.wheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 真正的3D WheelView, 内部核心类 {@link DrawManager}, {@link WheelParams}
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

    //绘制器管理
    private DrawManager mDrawManager;
    //RecyclerView adapter
    private WheelAdapter mWheelAdapter;
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

    //当前ViewGroup测量的矩阵与子控件要显示的矩阵
    private final Rect mContainerRect = new Rect();
    private final Rect mChildRect = new Rect();

    public WheelView(@NonNull Context context) {
        super(context);
        initialize(context, null);
    }

    public WheelView(@NonNull Context context, @NonNull WheelParams params) {
        this(context, params, new WheelDrawManager(), new SimpleItemPainter());
    }

    //用代码生成控件
    public WheelView(@NonNull Context context, @NonNull WheelParams params,
                     @NonNull DrawManager drawManager, @NonNull ItemPainter painter) {
        super(context);
        initialize(context, params, drawManager, painter);
    }

    public WheelView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public WheelView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(@NonNull Context context, AttributeSet attrs) {
        WheelParams params = new WheelParams.Builder(context, attrs).build();
        initialize(context, params, new WheelDrawManager(), new SimpleItemPainter());
    }

    private void initialize(Context context, WheelParams params, DrawManager drawManager, ItemPainter painter) {
        this.mWheelParams = params;
        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setId(ViewCompat.generateViewId());
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(context, mWheelParams.getLayoutOrientation(), false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //让滑动结束时都能定到中心位置
        new LinearSnapHelper().attachToRecyclerView(mRecyclerView);

        mWheelAdapter = new WheelAdapter(mWheelParams);
        mRecyclerView.setAdapter(mWheelAdapter);
        this.mDrawManager = drawManager;
        mDrawManager.setWheelParams(mWheelParams);
        mDrawManager.setItemPainter(painter);

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
        mContainerRect.set(getPaddingLeft(), getPaddingTop(), r - l - getPaddingRight(), b - t - getPaddingBottom());
        Gravity.apply(Gravity.TOP | Gravity.START, width, height, mContainerRect, mChildRect);
        mRecyclerView.layout(mChildRect.left, mChildRect.top, mChildRect.right, mChildRect.bottom);
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
        mRecyclerView.removeOnScrollListener(mScrollListener);
        mRecyclerView.removeItemDecoration(mDrawManager);
        hasAttachedToWindow = false;
    }

    /**
     * 创建WheelView的LayoutParams
     */
    private LayoutParams createLayoutParams() {
        if (mWheelParams.isVertical())
            return new LayoutParams(LayoutParams.MATCH_PARENT, mWheelParams.getTotalItemSize());
        return new LayoutParams(mWheelParams.getTotalItemSize(), LayoutParams.MATCH_PARENT);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onDataSetChanged() {
        mWheelAdapter.refreshDataCounts();
        mWheelAdapter.notifyDataSetChanged();
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
     */
    public void setAdapter(@Nullable Adapter adapter) {
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
            this.mWheelAdapter.adapter = adapter;
            onDataSetChanged();
            mLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    /**
     * 设置params, 用于代码生成WheelView时
     *
     * @param wheelParams 新的参数 详见{@link WheelParams.Builder}
     */
    public void setWheelParams(@NonNull WheelParams wheelParams) {
        mRecyclerView.removeItemDecoration(mDrawManager);
        mWheelParams = wheelParams;
        mDrawManager.setWheelParams(mWheelParams);
        mWheelAdapter = new WheelAdapter(mWheelParams);
        mWheelAdapter.adapter = mAdapter;
        mLayoutManager.setOrientation(mWheelParams.getLayoutOrientation());
        mRecyclerView.setAdapter(mWheelAdapter);
        if (hasAttachedToWindow) {
            mRecyclerView.addItemDecoration(mDrawManager);
        }
        mRecyclerView.setLayoutParams(createLayoutParams());
    }

    /**
     * 设置DrawManager
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setDrawManager(@NonNull DrawManager drawManager) {
        drawManager.setItemPainter(mDrawManager.itemPainter);
        mRecyclerView.removeItemDecoration(mDrawManager);
        mDrawManager = drawManager;
        mDrawManager.setWheelParams(mWheelParams);
        if (hasAttachedToWindow) {
            mRecyclerView.addItemDecoration(mDrawManager);
        }
        mRecyclerView.setLayoutParams(createLayoutParams());
        mWheelAdapter.notifyDataSetChanged();
    }

    //设置item绘制器
    public void setItemPainter(@NonNull ItemPainter painter) {
        mDrawManager.setItemPainter(painter);
        invalidate();
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

    @NonNull
    public WheelParams getWheelParams() {
        return mWheelParams;
    }

    @NonNull
    public DrawManager getDrawManager() {
        return mDrawManager;
    }

    @NonNull
    public ItemPainter getItemPainter() {
        return mDrawManager.itemPainter;
    }

    @Nullable
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

        public abstract int getItemCount();

        @NonNull public abstract String getItem(int position);
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
     * Item绘制器
     */
    public static class ItemPainter {
        WheelParams wheelParams;

        @CallSuper
        protected void setWheelParams(@NonNull WheelParams params) {
            this.wheelParams = params;
        }

        protected final WheelParams getWheelParams() {
            return wheelParams;
        }

        //画item
        protected void drawItem(@NonNull Canvas c, @NonNull Rect itemRect, int alpha, @NonNull String item) {
        }

        //画中心item
        protected void drawCenterItem(@NonNull Canvas c, @NonNull Rect itemRect, int alpha, @NonNull String item) {
        }

        //画分割线
        protected void drawDivider(@NonNull Canvas c, @NonNull Rect parentRect) {
        }
    }

    /**
     * 绘制器管理, 亦可重写此类实现想要的效果
     * 不旋转处理的管理类{@link LinearDrawManager}, Wheel效果的{@link WheelDrawManager}
     */
    public static abstract class DrawManager extends RecyclerView.ItemDecoration {
        //Wheel相关参数
        WheelParams wheelParams;
        //Item绘制器
        ItemPainter itemPainter;
        //整个WheelView的显示区域
        final Rect wvRect = new Rect();
        //item显示区域
        final Rect itemRect = new Rect();
        //中心位置
        int centerItemPosition = IDLE_POSITION;

        @CallSuper
        protected void setWheelParams(@NonNull WheelParams params) {
            params.setItemShowOrder(getShowOrder());
            this.wheelParams = params;
            if (itemPainter != null) {
                itemPainter.setWheelParams(wheelParams);
            }
        }

        @CallSuper
        void setItemPainter(@NonNull ItemPainter itemPainter) {
            if (this.wheelParams != null) {
                itemPainter.setWheelParams(wheelParams);
            }
            this.itemPainter = itemPainter;
        }

        public final WheelParams getWheelParams() {
            return wheelParams;
        }

        public final ItemPainter getItemPainter() {
            return itemPainter;
        }

        public final int getCenterItemPosition() {
            return centerItemPosition;
        }

        protected void setCenterItemPosition(int centerItemPosition) {
            this.centerItemPosition = centerItemPosition;
        }

        @Override
        public final void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        }

        @Override
        public final void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        }

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.State state) {
            if (wheelParams == null || itemPainter == null) return;
            if (rv.getLayoutManager() == null || !(rv.getAdapter() instanceof WheelAdapter)) return;
            int wheelCount = rv.getLayoutManager().getItemCount() - wheelParams.getShowItemCount() * 2;
            WheelAdapter adapter = (WheelAdapter) rv.getAdapter();
            wvRect.set(rv.getPaddingLeft(), rv.getPaddingTop(),
                    rv.getWidth() - rv.getPaddingRight(), rv.getHeight() - rv.getPaddingBottom());
            preDecoration(c, wvRect);
            for (int i = 0; i < rv.getChildCount(); i++) {
                View itemView = rv.getChildAt(i);
                int adapterPosition = rv.getChildAdapterPosition(itemView) - wheelParams.getShowItemCount();
                if (adapterPosition < 0 || adapterPosition >= wheelCount) {
                    continue; //itemCount为空白项,不考虑 || 超过列表的也是空白项
                }
                itemRect.set(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                decorationItem(c, itemRect, adapterPosition, adapter.getItem(adapterPosition));
            }
            decorationOver(c, wvRect);
        }

        //不处理
        protected WheelParams.ItemShowOrder getShowOrder() {
            return null;
        }

        //Canvas预装饰
        protected void preDecoration(@NonNull Canvas c, @NonNull Rect parentRect) {
            centerItemPosition = IDLE_POSITION;
        }

        //画item时的画笔装饰
        protected abstract void decorationItem(@NonNull Canvas c, @NonNull Rect itemRect, int position, @NonNull String item);

        //画完item时的画笔装饰
        protected void decorationOver(@NonNull Canvas c, @NonNull Rect parentRect) {
            itemPainter.drawDivider(c, parentRect);
        }
    }

    //RecyclerView实际显示的adapter
    static class WheelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        //wheel params
        final WheelParams wheelParams;
        //wheel adapter
        Adapter adapter = null;
        //ItemCount为null时重新计算,亦防止重复计算
        Integer itemCounts;

        public WheelAdapter(WheelParams wheelParams) {
            this.wheelParams = wheelParams;
        }

        void refreshDataCounts() {
            itemCounts = null;
        }

        @Override
        public int getItemCount() {
            if (itemCounts == null) {
                itemCounts = adapter == null ? 0 : adapter.getItemCount() + wheelParams.getShowItemCount() * 2;
            }
            return itemCounts;
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
            view.setVisibility(View.INVISIBLE);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        }

        @NonNull private String getItem(int position) {
            if (adapter != null) return adapter.getItem(position);
            return "";
        }
    }

    /**
     * 常用绘制器, 亦可重写此类
     */
    public static class SimpleItemPainter extends ItemPainter {
        //text画笔
        private final Paint textPaint = new TextPaint();
        //分割线Paint
        private final Paint dividerPaint = new Paint();
        //画文本居中时文本画笔的中心位置, 画居中文字时
        private float textFontCenter;

        @Override
        protected void setWheelParams(@NonNull WheelParams params) {
            super.setWheelParams(params);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(params.textSize);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            textFontCenter = (fm.bottom + fm.top) / 2.0f;

            dividerPaint.setAntiAlias(true);
            dividerPaint.setStrokeWidth(params.dividerSize);
            dividerPaint.setColor(params.dividerColor);
        }

        @Override
        protected void drawItem(@NonNull Canvas c, @NonNull Rect itemRect, int alpha, @NonNull String item) {
            textPaint.setColor(wheelParams.textColor);
            textPaint.setAlpha(alpha);
            c.drawText(item, itemRect.exactCenterX(), itemRect.exactCenterY() - textFontCenter, textPaint);
        }

        @Override
        protected void drawCenterItem(@NonNull Canvas c, @NonNull Rect itemRect, int alpha, @NonNull String item) {
            textPaint.setColor(wheelParams.textCenterColor);
            textPaint.setAlpha(alpha);
            c.drawText(item, itemRect.exactCenterX(), itemRect.exactCenterY() - textFontCenter, textPaint);
        }

        @Override
        protected void drawDivider(@NonNull Canvas c, @NonNull Rect parentRect) {
            if (wheelParams.isVertical()) {
                float dividerOff = (parentRect.height() - wheelParams.itemSize) / 2.0f;
                float firstY = parentRect.top + dividerOff - wheelParams.dividerPadding;
                c.drawLine(parentRect.left, firstY, parentRect.right, firstY, dividerPaint);
                float secondY = parentRect.bottom - dividerOff + wheelParams.dividerPadding;
                c.drawLine(parentRect.left, secondY, parentRect.right, secondY, dividerPaint);
            } else {
                float dividerOff = (parentRect.width() - wheelParams.itemSize) / 2.0f;
                float firstX = parentRect.left + dividerOff - wheelParams.dividerPadding;
                c.drawLine(firstX, parentRect.top, firstX, parentRect.bottom, dividerPaint);
                float secondX = parentRect.right - dividerOff + wheelParams.dividerPadding;
                c.drawLine(secondX, parentRect.top, secondX, parentRect.bottom, dividerPaint);
            }
        }
    }
}
