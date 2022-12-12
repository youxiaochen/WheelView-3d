package chen.you.wheel;

import android.graphics.Canvas;

/**
 * 普通绘制管理类,不处理旋转
 * Created by you on 2017/3/20.
 * 作QQ:86207610
 */
public class SimpleDrawManager extends WheelView.DrawManager {

    //用于计算item偏移值对应的alpha值
    private float maxCenterScrollOff;

    @Override
    void setWheelParams(WheelParams params) {
        super.setWheelParams(params);
        maxCenterScrollOff = (params.getShowItemCount() + 1) * params.itemSize;
    }

    @Override
    void drawItem(WheelView.WheelItemPainter painter, Canvas c, int adapterPosition) {
        float scrollOff; //相对中心的滑动偏移, 根据itemSize和偏移即可计算中离中心的比例和是否为中心item
        if (wheelParams.isVertical()) {
            scrollOff = wvRect.exactCenterY() - itemRect.exactCenterY();
        } else {
            scrollOff = wvRect.exactCenterX() - itemRect.exactCenterX();
        }

        //渐变处理
        int alpha = 255;
        if (wheelParams.gradient) {
            alpha = Math.max(255 - (int) (Math.abs(scrollOff) * 255 / maxCenterScrollOff), 0);
            if (alpha <= 0) return;
        }

        //中心计算
        int position = adapterPosition - wheelParams.getShowItemCount();
        boolean isCenterItem = false;
        if (centerItemPosition == WheelView.IDLE_POSITION) {
            isCenterItem = Math.abs(scrollOff) <= centerItemScrollOff;
            if (isCenterItem) {
                centerItemPosition = position;
            }
        }
        if (isCenterItem) {
            painter.drawCenterItem(c, itemRect, alpha, position);
        } else {
            painter.drawItem(c, itemRect, alpha, position);
        }
    }

    //不处理
    @Override
    WheelParams.ItemShowOrder getShowOrder() {
        return null;
    }
}
