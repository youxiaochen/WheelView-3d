package you.xiaochen.wheel.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

/**
 * Created by you on 2017/9/26.
 * 作QQ: 86207610
 */

class SimpleWheelDecoration extends WheelDecoration {
    /**
     * wheel item颜色与中心选中时的颜色
     */
    private final int textColor, textColorCenter;

    private final int dividerSize;
    /**
     * 画文本居中时文本画笔的高度
     */
    private final float textHeight;
    /**
     * wheel paint, dividerPaint
     */
    private final Paint paint, dividerPaint;

    private final WheelViewAdapter adapter;

    SimpleWheelDecoration(WheelViewAdapter adapter, int gravity, int textColor, int textColorCenter, float textSize, int dividerColor, int dividerSize) {
        super(adapter.itemCount, adapter.itemSize, gravity);
        this.textColor = textColor;
        this.textColorCenter = textColorCenter;
        this.dividerSize = dividerSize;
        this.adapter = adapter;

        this.paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        textHeight = (fm.bottom + fm.top) / 2.0f;

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setColor(dividerColor);
    }

    @Override
    void drawItem(Canvas c, Rect rect, int position, int alpha, boolean isCenterItem, boolean isVertical) {
        String s = adapter.getItemString(position);
        paint.setColor(isCenterItem ? textColorCenter : textColor);
        paint.setAlpha(alpha);
        //在rect区域内画居中文字
        c.drawText(s, rect.exactCenterX(), rect.exactCenterY() - textHeight, paint);
    }

    @Override
    void drawDivider(Canvas c, Rect rect, boolean isVertical) {
        if (isVertical) {
            float dividerOff = (rect.height() - dividerSize) / 2.0f;
            float firstY = rect.top + dividerOff;
            c.drawLine(rect.left, firstY, rect.right, firstY, dividerPaint);
            float secondY = rect.bottom - dividerOff;
            c.drawLine(rect.left, secondY, rect.right, secondY, dividerPaint);
        } else {
            float dividerOff = (rect.width() - dividerSize) / 2.0f;
            float firstX = rect.left + dividerOff;
            c.drawLine(firstX, rect.top, firstX, rect.bottom, dividerPaint);
            float secondX = rect.right - dividerOff;
            c.drawLine(secondX, rect.top, secondX, rect.bottom, dividerPaint);
        }
    }

}
