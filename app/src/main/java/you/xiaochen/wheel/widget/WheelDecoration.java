package you.xiaochen.wheel.widget;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by you on 2017/9/26.
 * 作QQ: 86207610
 */

abstract class WheelDecoration extends RecyclerView.ItemDecoration {
    /**
     * 垂直布局时的靠左,居中,靠右立体效果
     */
    static final int GRAVITY_LEFT = 1;
    static final int GRAVITY_CENTER = 2;
    static final int GRAVITY_RIGHT = 3;
    /**
     * 此参数影响左右旋转对齐时的效果,系数越大,越明显,自己体会......(0-1之间)
     */
    static final float DEF_SCALE = 0.75F;
    /**
     * 无效的位置
     */
    public static final int IDLE_POSITION = -1;
    /**
     * 显示的item数量
     */
    final int itemCount;
    /**
     * 每个item大小,  垂直布局时为item的高度, 水平布局时为item的宽度
     */
    final int itemSize;
    /**
     * 每个item平均下来后对应的旋转角度
     * 根据中间分割线上下item和中间总数量计算每个item对应的旋转角度
     */
    final float itemDegree;
    /**
     * 滑动轴的半径
     */
    final float wheelRadio;
    /**
     * 对齐方式
     */
    final int gravity;
    /**
     * 3D旋转
     */
    final Camera camera;
    final Matrix matrix;

    /**
     * 判断是否为中间item
     */
    boolean hasCenterItem;
    float halfItemHeight;

    int centerItemPosition = IDLE_POSITION;

    WheelDecoration(int itemCount, int itemSize, int gravity) {
        this.itemCount = itemCount;
        this.itemSize = itemSize;
        this.halfItemHeight = itemSize / 2.0f;
        this.itemDegree = 180.f / (itemCount * 2 + 1);
        this.gravity = gravity;
        wheelRadio = (float) WheelUtils.radianToRadio(itemSize, itemDegree);

        camera = new Camera();
        matrix = new Matrix();
    }

    @Override
    public final void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        centerItemPosition = IDLE_POSITION;
        if (!(parent.getLayoutManager() instanceof LinearLayoutManager)) return;
        LinearLayoutManager llm = (LinearLayoutManager) parent.getLayoutManager();
        boolean isVertical = llm.getOrientation() == LinearLayoutManager.VERTICAL;//垂直与水平布局方式
        Rect parentRect = new Rect(parent.getLeft(), parent.getTop(), parent.getRight(), parent.getBottom());
        int startPosition = llm.findFirstVisibleItemPosition();
        if (startPosition < 0) return;
        int endPosition = llm.findLastVisibleItemPosition();
        hasCenterItem = false;
        for (int itemPosition = startPosition; itemPosition <= endPosition; itemPosition++) {
            if (itemPosition < itemCount) continue;//itemCount为空白项,不考虑
            if (itemPosition >= llm.getItemCount() - itemCount) break;//超过列表的也是空白项
            //Log.i("you", "onDraw currentItem... " + itemPosition);
            View itemView = llm.findViewByPosition(itemPosition);
            Rect itemRect = new Rect(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            if (isVertical) {//垂直布局, 还需要靠对齐方式
                drawVerticalItem(c, itemRect, itemPosition, translateX(parentRect), parentRect.exactCenterY());
            } else {//水平布局
                drawHorizontalItem(c, itemRect, itemPosition, parentRect.exactCenterX(), parentRect.exactCenterY());
            }
        }
        drawDivider(c, parentRect, isVertical);
    }

    /**
     * 根据对齐方式,计算出垂直布局时X轴移动的位置
     * @return
     */
    private float translateX(Rect parentRect) {
        float parentCenterX = parentRect.exactCenterX();
        switch (gravity) {
            case GRAVITY_LEFT:
                return parentCenterX * (1 + DEF_SCALE);
            case GRAVITY_RIGHT:
                return parentCenterX * (1 - DEF_SCALE);
        }
        return parentCenterX;
    }

    /**
     * 画垂直布局时的item
     * @param c
     * @param rect
     * @param position
     * @param translateX X轴旋转时的平移点
     * @param parentCenterY RecyclerView的中心Y点
     */
    void drawVerticalItem(Canvas c, Rect rect, int position, float translateX, float parentCenterY) {
        int realPosition = position - itemCount;//数据中的实际位置
        float itemCenterY = rect.exactCenterY();
        float scrollOffY = itemCenterY - parentCenterY;
        float rotateDegreeX = scrollOffY * itemDegree / itemSize;//垂直布局时要以X轴为中心旋转
        int alpha = degreeAlpha(rotateDegreeX);
        if (alpha <= 0) return;
        float rotateSinX = (float) Math.sin(Math.toRadians(rotateDegreeX));
        float rotateOffY = scrollOffY - wheelRadio * rotateSinX;//因旋转导致界面视角的偏移
        //Log.i("you", "drawVerticalItem degree " + rotateDegreeX);
        //计算中心item, 优先最靠近中心区域的为中心点
        boolean isCenterItem = false;
        if (!hasCenterItem) {
            isCenterItem = Math.abs(scrollOffY) <= halfItemHeight;
            if (isCenterItem) {
                centerItemPosition = realPosition;
                hasCenterItem = true;
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
        matrix.preTranslate(-translateX, -itemCenterY);
        matrix.postTranslate(translateX, itemCenterY);
        c.concat(matrix);
        drawItem(c, rect, realPosition, alpha, isCenterItem, true);
        c.restore();
    }

    /**
     * 画水平布局时的item
     * @param c
     * @param rect
     * @param position
     * @param parentCenterX RecyclerView的中心X点
     * @param parentCenterY RecyclerView的中心Y点
     */
    void drawHorizontalItem(Canvas c, Rect rect, int position, float parentCenterX, float parentCenterY) {
        int realPosition = position - itemCount;
        float itemCenterX = rect.exactCenterX();
        float scrollOffX = itemCenterX - parentCenterX;
        float rotateDegreeY = scrollOffX * itemDegree / itemSize;//垂直布局时要以Y轴为中心旋转
        int alpha = degreeAlpha(rotateDegreeY);
        if (alpha <= 0) return;
        float rotateSinY = (float) Math.sin(Math.toRadians(rotateDegreeY));
        float rotateOffX = scrollOffX - wheelRadio * rotateSinY;//因旋转导致界面视角的偏移
        //Log.i("you", "drawHorizontalItem degree " + rotateDegreeY);

        boolean isCenterItem = false;
        if (!hasCenterItem) {
            isCenterItem = Math.abs(scrollOffX) <= halfItemHeight;
            if (isCenterItem) {
                centerItemPosition = realPosition;
                hasCenterItem = true;
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
        matrix.preTranslate(-itemCenterX, -parentCenterY);
        matrix.postTranslate(itemCenterX, parentCenterY);
        c.concat(matrix);
        drawItem(c, rect, realPosition, alpha, isCenterItem, false);
        c.restore();
    }

    /**
     * 旋转大于90度时,完全透明
     * @param degree
     * @return
     */
    int degreeAlpha(float degree) {
        degree = Math.abs(degree);
        if (degree >= 90) return 0;
        float al = (90 - degree) / 90;
        return (int) (255 * al);
    }

    /**
     * 画item,  如何画法可以在此方法中实现
     * @param c
     * @param rect  item的区域
     * @param position item index
     * @param alpha 已经计算好的item所在位置的透明度,也可以不考虑设置此参数
     * @param isCenterItem 是否为中心点
     * @param isVertical 是否为垂直布局, false 为水平布局
     */
    abstract void drawItem(Canvas c, Rect rect, int position, int alpha, boolean isCenterItem, boolean isVertical);

    /**
     * 画分割线 如何画法可以在此方法中实现
     * @param c
     * @param rect 整个内容区域
     * @param isVertical
     */
    abstract void drawDivider(Canvas c, Rect rect, boolean isVertical);
}
