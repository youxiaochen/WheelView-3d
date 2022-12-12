package chen.you.wheel;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * 3D旋转绘制管理类
 * Created by you on 2017/3/20.
 * 作QQ:86207610
 */
public class WheelDrawManager extends WheelView.DrawManager {
    /**
     * 保留2个相素让RecyclerView的顶部和底部可以多预画一个item, {@link WheelItemShowOrder}
     */
    private static final int SHOW_ORDER_OFFSET = 2;

    //此参数影响左右旋转对齐时的效果,系数越大,越明显(0-1之间)
    private static final float DEF_SCALE = 0.75F;
    //3D旋转
    final Camera camera;
    final Matrix matrix;
    //每个item平均下来后对应的旋转角度 根据中间分割线上下item和中间总数量计算每个item对应的旋转角度
    float itemDegree;
    //滑动轴的半径,旋转的偏移需要通过此参数和itemDegree来计算
    float wheelRadio;

    public WheelDrawManager() {
        camera = new Camera();
        matrix = new Matrix();
    }

    @Override
    void setWheelParams(WheelParams params) {
        super.setWheelParams(params);
        this.itemDegree = 180.f / (params.itemCount * 2 + 1);
        this.wheelRadio = (float) ((params.itemSize / 2.f) / Math.tan(Math.toRadians(itemDegree / 2.f)));
    }

    @Override
    void drawItem(WheelView.WheelItemPainter painter, Canvas c, int adapterPosition) {
        if (wheelParams.isVertical()) {
            drawVerticalItem(painter, c, adapterPosition);
        } else {
            drawHorizontalItem(painter, c, adapterPosition);
        }
    }

    private void drawVerticalItem(WheelView.WheelItemPainter painter, Canvas c, int adapterPosition) {
        int position = adapterPosition - wheelParams.getShowItemCount();//数据中的实际位置
        float itemCenterY = itemRect.exactCenterY();
        float scrollOffY = itemCenterY - wvRect.exactCenterY();
        if (Math.abs(scrollOffY) <= SHOW_ORDER_OFFSET) { //正中心
            centerItemPosition = position;
            painter.drawCenterItem(c, itemRect, 255, position);
            return;
        }
        float rotateDegreeX = scrollOffY * itemDegree / wheelParams.itemSize;//垂直布局时要以X轴为中心旋转

        //旋转后的渐变处理
        int alpha = 255;
        if (wheelParams.gradient) {
            alpha = degreeAlpha(rotateDegreeX);
            if (alpha <= 0) return;
        }

        float rotateSinX = (float) Math.sin(Math.toRadians(rotateDegreeX));
        float rotateOffY = scrollOffY - wheelRadio * rotateSinX;//因旋转导致界面视角的偏移
        //Log.d("you", "drawVerticalItem degree " + rotateDegreeX);
        //计算中心item, 优先最靠近中心区域的为中心点
        boolean isCenterItem = false;
        if (centerItemPosition == WheelView.IDLE_POSITION) {
            isCenterItem = Math.abs(scrollOffY) <= centerItemScrollOff;
            if (isCenterItem) {
                centerItemPosition = position;
            }
        }

        c.save();
        c.translate(0.0f, -rotateOffY);//因旋转导致界面视角的偏移
        camera.save();

        //旋转时离视角的z轴方向也会变化,先移动Z轴再旋转
        float z = (float) (wheelRadio * (1 - Math.abs(Math.cos(Math.toRadians(rotateDegreeX)))));
        camera.translate(0, 0, z);

        camera.rotateX(-rotateDegreeX);
        camera.getMatrix(matrix);
        camera.restore();

        //根据对齐方式,计算出垂直布局时X轴移动的位置
        float translateX = wvRect.exactCenterX();
        if (wheelParams.gravity == WheelParams.LEFT) {
            translateX *= 1 + DEF_SCALE;
        } else if (wheelParams.gravity == WheelParams.RIGHT) {
            translateX *= 1 - DEF_SCALE;
        }
        matrix.preTranslate(-translateX, -itemCenterY);
        matrix.postTranslate(translateX, itemCenterY);
        c.concat(matrix);
        if (isCenterItem) {
            painter.drawCenterItem(c, itemRect, alpha, position);
        } else {
            painter.drawItem(c, itemRect, alpha, position);
        }
        c.restore();
    }

    private void drawHorizontalItem(WheelView.WheelItemPainter painter, Canvas c, int adapterPosition) {
        int position = adapterPosition - wheelParams.getShowItemCount();
        float itemCenterX = itemRect.exactCenterX();
        float scrollOffX = itemCenterX - wvRect.exactCenterX();
        if (Math.abs(scrollOffX) <= SHOW_ORDER_OFFSET) { //正中心
            centerItemPosition = position;
            painter.drawCenterItem(c, itemRect, 255, position);
            return;
        }
        float rotateDegreeY = scrollOffX * itemDegree / wheelParams.itemSize;//垂直布局时要以Y轴为中心旋转

        //旋转后的渐变处理
        int alpha = 255;
        if (wheelParams.gradient) {
            alpha = degreeAlpha(rotateDegreeY);
            if (alpha <= 0) return;
        }

        float rotateSinY = (float) Math.sin(Math.toRadians(rotateDegreeY));
        float rotateOffX = scrollOffX - wheelRadio * rotateSinY;//因旋转导致界面视角的偏移
        //Log.d("you", "drawHorizontalItem degree " + rotateDegreeY);

        boolean isCenterItem = false;
        if (centerItemPosition == WheelView.IDLE_POSITION) {
            isCenterItem = Math.abs(scrollOffX) <= centerItemScrollOff;
            if (isCenterItem) {
                centerItemPosition = position;
            }
        }

        c.save();
        c.translate(-rotateOffX, 0.0f);
        camera.save();

        float z = (float) (wheelRadio * (1 - Math.abs(Math.cos(Math.toRadians(rotateDegreeY)))));
        camera.translate(0, 0, z);
        camera.rotateY(rotateDegreeY);
        camera.getMatrix(matrix);
        camera.restore();
        float parentCenterY = wvRect.exactCenterY();
        matrix.preTranslate(-itemCenterX, -parentCenterY);
        matrix.postTranslate(itemCenterX, parentCenterY);
        c.concat(matrix);
        if (isCenterItem) {
            painter.drawCenterItem(c, itemRect, alpha, position);
        } else {
            painter.drawItem(c, itemRect, alpha, position);
        }
        c.restore();
    }

    /**
     * 旋转大于90度时,完全透明
     * @param degree 旋转角度
     * @return 旋转后的透明度
     */
    private int degreeAlpha(float degree) {
        degree = Math.abs(degree);
        if (degree >= 90) return 0;
        float al = (90 - degree) / 90;
        return (int) (255 * al);
    }

    @Override
    WheelParams.ItemShowOrder getShowOrder() {
        return new WheelItemShowOrder();
    }

    static class WheelItemShowOrder implements WheelParams.ItemShowOrder {
        //通过计算,在item3个或者以上的时候,旋转后的WheelView高度会比实际高度小一个item的大小
        //计算方式可以详见博客中的原理图,计算出三角形的腰长即为半径
        //item在6个或者以上时,旋转后的高度会相差大于2, 因此3-5个时的效果最好,不会留过多的空白区域
        //此时防止WheelView旋转后的空白区域过多,可以适当修改大小, 适配器中头和尾添加的itemCount - 1
        //showItemCout 只能 -1, ItemDecoration超出屏幕外的不会draw
        @Override
        public int getShowItemCount(int itemCount) {
            return itemCount > 2 ? itemCount - 1 : itemCount;
        }

        //RecyclerView高度或者水平时的宽度添加2个像素是为了在减掉显示的一个item时,
        // 超出RecyclerView显示区域刚好可以再显示头部一个和尾部一个item
        @Override
        public int getTotalItemSize(int showItemCount, int itemSize) {
            return (showItemCount * 2 + 1) * itemSize + SHOW_ORDER_OFFSET;
        }
    }
}
