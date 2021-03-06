package com.maxcion.dragview;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 创建DragView的建造累
 * 在这里必传的参数有 activity， contentView maxHeight
 */
public class DragViewBuilder {

    private Activity activity;
    //遮罩的色值
    private int shadeColor;
    private int width;
    private int height;
    //用来显示的内容的view
    private View contentView;

    //监听窗口划出和划入的回调
    private IOnDragViewStateChangeListener listener;

    //拖拽View 最高高度， 因为DragView 是一个ViewGroup，如果里面有RecyclerView， RV内容高度不确定的时候，
    //这个maxHeight 主要是为了避免 当RV数据过多超过屏幕时， 整个DragView高度也是一个屏幕的情况
    private int maxHeight;
    private DragView dragView;

    private boolean canCancelOutside = false;
    private boolean canDrag;

    private DragViewBuilder() {
    }

    public static DragViewBuilder create() {
        return new DragViewBuilder();
    }

    public DragViewBuilder setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public DragViewBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public DragViewBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public DragViewBuilder setShadeColor(int shadeColor) {
        this.shadeColor = shadeColor;
        return this;
    }

    public DragViewBuilder setContentView(View contentView) {
        this.contentView = contentView;
        return this;
    }

    public DragViewBuilder setDragViewStateChangeListener(IOnDragViewStateChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public DragViewBuilder setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    public DragViewBuilder setCanCancelOutside(boolean canCancelOutside) {
        this.canCancelOutside = canCancelOutside;
        return this;
    }

    public DragViewBuilder setCanDrag(boolean canDrag) {
        this.canDrag = canDrag;
        return this;
    }

    public Activity getActivity() {
        return activity;
    }

    public int getShadeColor() {
        return shadeColor;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public View getContentView() {
        return contentView;
    }

    public IOnDragViewStateChangeListener getListener() {
        return listener;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public boolean isCanCancelOutside() {
        return canCancelOutside;
    }

    public boolean isCanDrag(){
        return canDrag;
    }

    public boolean isDismiss() {
        if (dragView == null) {
            return true;
        }
        return dragView.isDismiss();
    }

    public void show() {
        if (activity == null) {
            throw new RuntimeException("DragView : activity can not be null");
        }

        if (contentView == null) {
            throw new RuntimeException("DragView : contentView can not be null");
        }

        //创建DragView（包裹需要显示的View）
        dragView = new DragView(this);

        //从当前的Activity中找到R.id.content 的View（是一个FrameLayout）因为是一个FrameLayout才能把最新添加进的View
        //放在最上层，后期如果有 新手引导 也不会有问题
        FrameLayout rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);

        //把DragView 添加到Activity中
        rootView.addView(dragView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    public void dismiss() {
        if (null != dragView) {
            dragView.dismiss();
        }
    }

}
