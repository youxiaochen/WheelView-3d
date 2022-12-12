package chen.you.wheel;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * WheelView 相关参数类属性
 * Created by you on 2017/3/20.
 * 作QQ:86207610
 */
public final class WheelParams {
    /**
     * 垂直与水平布局两种状态
     */
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 0;
    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }
    /**
     * 垂直布局时的靠左,居中,靠右立体效果
     */
    public static final int CENTER = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    @IntDef({CENTER, LEFT, RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {
    }
    /**
     * 一些默认参数大小
     */
    public static final int DEF_ITEM_COUNT = 3;
    public static final int DEF_ITEM_SIZE = dp2px(40);
    public static final int DEF_TEXT_SIZE = sp2px(18);
    public static final int DEF_DIVIDER_SIZE = dp2px(1);

    static int dp2px(float dp) {
        final float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    static int sp2px(float sp) {
        final float fontDensity = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontDensity + 0.5f);
    }

    //布局方向
    public final @Orientation int orientation;
    //item数量
    public final int itemCount;
    //item大小
    public final int itemSize;
    //对齐方式
    public final @Gravity int gravity;
    //item文字大小, 如果中心文字大小需要调整可以用Canvas缩放
    public final float textSize;
    //item文字颜色
    public final @ColorInt int textColor;
    //中心文字颜色
    public final @ColorInt int textCenterColor;
    //分割线大小
    public final int dividerSize;
    //分割线颜色
    public final @ColorInt int dividerColor;
    //透明度渐变
    public final boolean gradient;
    //分割线填充值默认为0, 分割线矩阵大小为itemSize + dividerPadding
    public final int dividerPadding;
    //实际显示在界面上的itemCount
    private int showItemCount;
    //ItemShowOrder
    private ItemShowOrder itemShowOrder;

    private WheelParams(Builder builder) {
        this.orientation = builder.orientation;
        this.itemCount = builder.itemCount;
        this.itemSize = builder.itemSize;
        this.gravity = builder.gravity;
        this.textSize = builder.textSize;
        this.textColor = builder.textColor;
        this.textCenterColor = builder.textCenterColor;
        this.gradient = builder.gradient;
        this.dividerSize = builder.dividerSize;
        this.dividerColor = builder.dividerColor;
        this.dividerPadding = builder.dividerPadding;
        this.showItemCount = itemCount;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public int getShowItemCount() {
        return showItemCount;
    }

    @RecyclerView.Orientation public int getLayoutOrientation() {
        return orientation == VERTICAL ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL;
    }

    boolean isVertical() {
        return orientation == VERTICAL;
    }

    int getTotalItemSize() {
        if (itemShowOrder != null) return itemShowOrder.getTotalItemSize(showItemCount, itemSize);
        //中间项 + 上下itemCount
        return (showItemCount * 2 + 1) * itemSize;
    }

    void setItemShowOrder(ItemShowOrder itemShowOrder) {
        this.itemShowOrder = itemShowOrder;
        this.showItemCount = itemShowOrder == null ? itemCount : itemShowOrder.getShowItemCount(itemCount);
    }

    /**
     * 界面显示规则, 3D旋转后由于圆形直径相比非旋转时的大小相差很大, 因此需要对显示数量和大小进行修改
     */
    interface ItemShowOrder {

        int getShowItemCount(int itemCount);

        int getTotalItemSize(int showItemCount, int itemSize);
    }

    public final static class Builder {
        //布局方向
        @Orientation int orientation = VERTICAL;
        //item数量
        int itemCount = DEF_ITEM_COUNT;
        //item大小
        int itemSize = DEF_ITEM_SIZE;
        //对齐方式
        @Gravity int gravity = CENTER;
        //item文字大小, 如果中心文字大小需要调整可以用Canvas缩放
        float textSize = DEF_TEXT_SIZE;
        //item文字颜色
        @ColorInt int textColor = Color.BLACK;
        //中心文字颜色
        @ColorInt int textCenterColor = Color.RED;
        //透明度渐变
        boolean gradient = true;
        //分割线大小
        int dividerSize = DEF_DIVIDER_SIZE;
        //分割线颜色
        @ColorInt int dividerColor = Color.RED;
        //分割线填充值默认为0, 分割线矩阵大小为itemSize + dividerPadding
        int dividerPadding = 0;

        public Builder() {
        }

        private Builder(WheelParams params) {
            this.orientation = params.orientation;
            this.itemCount = params.itemCount;
            this.itemSize = params.itemSize;
            this.gravity = params.gravity;
            this.textSize = params.textSize;
            this.textColor = params.textColor;
            this.textCenterColor = params.textCenterColor;
            this.dividerSize = params.dividerSize;
            this.dividerColor = params.dividerColor;
            this.gradient = params.gradient;
            this.dividerPadding = params.dividerPadding;
        }

        Builder(Context context, AttributeSet attrs) {
            if (attrs != null) {
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
                orientation = a.getInt(R.styleable.WheelView_wheelOrientation, orientation);
                itemCount = a.getInt(R.styleable.WheelView_wheelItemCount, itemCount);
                itemSize = a.getDimensionPixelOffset(R.styleable.WheelView_wheelItemSize, itemSize);
                gravity = a.getInt(R.styleable.WheelView_wheelGravity, gravity);
                textSize = a.getDimension(R.styleable.WheelView_wheelTextSize, textSize);
                textColor = a.getColor(R.styleable.WheelView_wheelTextColor, textColor);
                textCenterColor = a.getColor(R.styleable.WheelView_wheelTextCenterColor, textCenterColor);
                gradient = a.getBoolean(R.styleable.WheelView_wheelGradient, gradient);
                dividerColor = a.getColor(R.styleable.WheelView_wheelDividerColor, dividerColor);
                dividerSize = a.getDimensionPixelOffset(R.styleable.WheelView_wheelDividerSize, dividerSize);
                dividerPadding = a.getDimensionPixelOffset(R.styleable.WheelView_wheelDividerPadding, dividerPadding);
                a.recycle();
            }
        }

        public Builder setOrientation(@Orientation int orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setItemCount(int itemCount) {
            this.itemCount = itemCount;
            return this;
        }

        public Builder setItemSize(int itemSize) {
            this.itemSize = itemSize;
            return this;
        }

        public Builder setGravity(@Gravity int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setTextColor(@ColorInt int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setTextCenterColor(@ColorInt int textCenterColor) {
            this.textCenterColor = textCenterColor;
            return this;
        }

        public Builder setGradient(boolean gradient) {
            this.gradient = gradient;
            return this;
        }

        public Builder setDividerSize(int dividerSize) {
            this.dividerSize = dividerSize;
            return this;
        }

        public Builder setDividerColor(@ColorInt int dividerColor) {
            this.dividerColor = dividerColor;
            return this;
        }

        public Builder setDividerPadding(int dividerPadding) {
            this.dividerPadding = dividerPadding;
            return this;
        }

        public WheelParams build() {
            if (itemCount <= 0) itemCount = DEF_ITEM_COUNT;
            if (itemSize <= 0) itemSize = DEF_ITEM_SIZE;
            if (textSize <= 0) textSize = DEF_TEXT_SIZE;
            if (dividerSize <= 0) dividerSize = DEF_DIVIDER_SIZE;
            return new WheelParams(this);
        }
    }

}
