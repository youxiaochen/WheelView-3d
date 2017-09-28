package you.xiaochen.wheel.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by you on 2017/9/25.
 * 作QQ:86207610
 */

final class WheelUtils {

    private WheelUtils() {}

    /**
     * 根据item的大小(弧的长度),和item对应的旋转角度,计算出滑轮轴的半径
     * @param radian
     * @param degree
     * @return
     */
    static double radianToRadio(int radian, float degree) {
        return radian * 180d / (degree * Math.PI);
    }

    /**
     * 根据方向代码创建view layoutparams
     *
     * 如果水平布局时,最好指定高度大小,  垂直布局时最定宽度大小
     *
     * @param orientation
     * @param size
     * @return
     */
    static ViewGroup.LayoutParams createLayoutParams(int orientation, int size) {
        if (orientation == WheelView.WHEEL_VERTICAL) {
            return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        } else {
            return new ViewGroup.LayoutParams(size, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    /**
     * 获取控件的中心x位置
     * @param view
     * @return
     */
    static int getViewCenterX(View view) {
        return (view.getLeft() + view.getRight()) >> 1;
    }

    /**
     * 获取控件的中心y位置
     * @param view
     * @return
     */
    static int getViewCenterY(View view) {
        return (view.getTop() + view.getBottom()) >> 1;
    }

}
