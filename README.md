# WheelView-3d
### Camera, Matrix 实现WheelView 3d效果


![纵向排列](https://github.com/youxiaochen/WheelView-3d/blob/master/imgs/GIF111.gif)
![水平排列](https://github.com/youxiaochen/WheelView-3d/blob/master/imgs/GIF222.gif)



```
<declare-styleable name="WheelView">
        <!-- 中间分割线外的item数量,整个滑动数量就为 wheelItemCount * 2 + 1  -->
        <attr name="wheelItemCount" format="integer"/>
        <!-- 滑轮item高度 -->
        <attr name="wheelItemSize" format="dimension"/>
        <!-- 滑轮字体大小 -->
        <attr name="wheelTextSize" format="dimension"/>
        <!-- 滑轮字体颜色 -->
        <attr name="wheelTextColor" format="color"/>
        <!-- 滑轮中心字体颜色 -->
        <attr name="wheelTextColorCenter" format="color"/>
        <!-- 分割线颜色 -->
        <attr name="dividerColor" format="color"/>
        <!-- 布局方向 -->
        <attr name="wheelOrientation">
            <enum name="vertical" value="1"/>
            <enum name="horizontal" value="2"/>
        </attr>
        <!-- 两根分割线的距离 -->
        <attr name="wheelDividerSize" format="dimension"/>
    </declare-styleable>

```






