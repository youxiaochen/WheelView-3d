package chen.you.wheel;

import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.NonNull;

/**
 * 线性Canvas装饰类, 不处理旋转, 可只处理alpha
 * Created by you on 2017/3/20.
 * 作QQ:86207610
 */
public class LinearDrawManager extends WheelView.DrawManager {

    //用于计算item偏移值对应的alpha值
    private float maxCenterScrollOff;
    //中心偏移值即为itemSize / 2
    float centerItemScrollOff;

    @Override
    protected void setWheelParams(@NonNull WheelParams params) {
        super.setWheelParams(params);
        maxCenterScrollOff = (params.getShowItemCount() + 1) * params.itemSize;
        centerItemScrollOff = params.itemSize / 2.f;
    }

    @Override
    protected void decorationItem(@NonNull Canvas c, @NonNull Rect itemRect, int position, @NonNull String item) {
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
        boolean isCenterItem = false;
        if (centerItemPosition == WheelView.IDLE_POSITION) {
            isCenterItem = Math.abs(scrollOff) <= centerItemScrollOff;
            if (isCenterItem) {
                centerItemPosition = position;
            }
        }
        if (isCenterItem) {
            getItemPainter().drawCenterItem(c, itemRect, alpha, item);
        } else {
            getItemPainter().drawItem(c, itemRect, alpha, item);
        }
    }
}
