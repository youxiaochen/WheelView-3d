package you.xiaochen.wheel.widget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import you.xiaochen.wheel.R;

/**
 * Created by you on 2017/10/11.
 */

public class WheelRecyclerView extends RecyclerView {
    /**
     * 显示的item数量
     */
    int itemCount;
    /**
     * 每个item大小,  垂直布局时为item的高度, 水平布局时为item的宽度
     */
    int itemSize;
    /**
     * 每个item平均下来后对应的旋转角度
     * 根据中间分割线上下item和中间总数量计算每个item对应的旋转角度
     */
    float itemDegree;
    /**
     * 滑动轴的半径
     */
    float wheelRadio;
    /**
     * 3D旋转
     */
    Camera camera;
    Matrix matrix;


    Rect parentRect;

    public WheelRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public WheelRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WheelRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     *
     */
    private void init(Context context) {
        camera = new Camera();
        matrix = new Matrix();
        parentRect = new Rect();
        itemCount = 3;
        itemSize = context.getResources().getDimensionPixelOffset(R.dimen.size60);
        this.itemDegree = 180.f / (itemCount * 2 + 1);
        wheelRadio = (float) WheelUtils.radianToRadio(itemSize, itemDegree);

        camera.setLocation(0, 0, -32);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        parentRect.set(l, t, r, b);
    }

    @Override
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {


        float parentCenterX = parentRect.exactCenterX();
        float parentCenterY = parentRect.exactCenterY();

        Rect rect = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());

        float itemCenterX = rect.exactCenterX();
        float scrollOffX = itemCenterX - parentCenterX;
        float rotateDegreeY = scrollOffX * itemDegree / itemSize;//垂直布局时要以Y轴为中心旋转
        if (Math.abs(rotateDegreeY) >= 90.f) rotateDegreeY = 90;
        float rotateSinY = (float) Math.sin(Math.toRadians(rotateDegreeY));
        float rotateOffX = scrollOffX - wheelRadio * rotateSinY;//因旋转导致界面视角的偏移
        //Log.i("you", "drawHorizontalItem degree " + rotateDegreeY);

        canvas.save();
        canvas.translate(-rotateOffX, 0.0f);
        camera.save();

        float z = (float) (wheelRadio * (1 - Math.abs(Math.cos(Math.toRadians(rotateDegreeY)))));
        camera.translate(0, 0, z);

        camera.rotateY(rotateDegreeY);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-itemCenterX, -parentCenterY);
        matrix.postTranslate(itemCenterX, parentCenterY);
        canvas.concat(matrix);

        boolean drawResult = super.drawChild(canvas, child, drawingTime);
        canvas.restore();
        return drawResult;
    }
}
