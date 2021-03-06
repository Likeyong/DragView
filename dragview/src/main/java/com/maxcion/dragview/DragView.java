package com.maxcion.dragview;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

/**
 * DragView 的拖动主要是通过ViewDragHelper 来实现拖动定位的
 * 整个DragView的设计思路是： DragView是FrameLayout
 * 然后给这个FrameLayout 添加一个底部对齐的RelativeLayout（MaxHeightRelativeLayout 控制最高高度）
 * MaxHeightRelativeLayout 就是直接包裹你传进来的contentView
 */
public class DragView extends FrameLayout {
    //在设置DragView时设置的所有参数
    private DragViewBuilder mBuilder;
    private ViewDragHelper mViewDragHelper;
    private View mDragView;
    private View mContentViewLayout;

    private boolean mIsDismiss = true;
    private boolean mContentViewContainPointDown;

    public boolean isDismiss() {
        return mIsDismiss;
    }

    public DragView(DragViewBuilder mBuilder) {
        super(mBuilder.getActivity());
        this.mBuilder = mBuilder;
        init();
    }

    /**
     * 在这里创建 一个底部对齐的MaxHeightRelativeLayout 用来承载contentView
     */
    private void init() {
        mIsDismiss = false;
        mViewDragHelper = ViewDragHelper.create(this, new DragCallback());
        setBackgroundColor(mBuilder.getShadeColor());
        mContentViewLayout = mBuilder.getContentView();

        int measureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE / 2, MeasureSpec.AT_MOST);
        mContentViewLayout.measure(measureSpec, measureSpec);

        int measuredHeight = Math.min(mContentViewLayout.getMeasuredHeight(), mBuilder.getMaxHeight());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, measuredHeight);
        params.gravity = Gravity.BOTTOM;
        addView(mContentViewLayout, params);
        if (mBuilder.isCanCancelOutside()) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }


    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //判断 down的时候  手指是否点在 contentView 的外部,如果在contentView 外部 就走原生的分发流程
        // (给根布局设置了点击事件,点击就dismiss). 如果在contentView内部,就走mViewDragHelper的分发流程
        if (event.getAction() == MotionEvent.ACTION_DOWN && mContentViewLayout != null) {
            int[] location = new int[2];
            mContentViewLayout.getLocationOnScreen(location);
            Rect rect = new Rect(location[0], location[1], location[0] + mContentViewLayout.getWidth(), location[1] + mContentViewLayout.getHeight());
            mContentViewContainPointDown = rect.contains((int) event.getRawX(), (int) event.getRawY());

        }
        //down 事件 在contentView 内部  走mViewDragHelper 逻辑
        if (mContentViewContainPointDown) {
            mViewDragHelper.processTouchEvent(event);
        } else {
            //down事件 不在contentView 内部,将事件交给rootView 的click事件处理
            super.onTouchEvent(event);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private class DragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (child == mContentViewLayout) {
                mDragView = child;
            }
            return child == mContentViewLayout && mBuilder.isCanDrag();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //top 代表当前按下的手指在的位置，
            // return 的值 是 你要把view 放在哪个位置
            //在这里对滑动的上限进行限制
            return Math.max(getHeight() - mDragView.getHeight(), top);
        }


        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //手指抬起释放时回调
            if (mDragView != null) {

                int maxDy = mDragView.getHeight();
                int startY = getHeight() - maxDy;
                int curY = releasedChild.getTop();
                int finalTop = startY;
                if (curY - startY > maxDy / 2) {
                    finalTop = getHeight();
                }
                mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), finalTop);
                invalidate();

            }

        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE && mDragView != null) {
                IOnDragViewStateChangeListener listener = mBuilder.getListener();
                if (listener != null) {
                    listener.onChange(mDragView.getTop() >= getHeight());

                }
                if (mDragView.getTop() >= getHeight()) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    });
                }
            }
        }
    }

    public void dismiss() {
        mIsDismiss = true;
        View contentView = mContentViewLayout;
        ViewParent parent = contentView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(contentView);
        }
        //因为可能不止一次调用shoe，也就代表不止一次进行addView， 所以要在DragView 划出的时候将View  remove掉
        removeView(mContentViewLayout);
        FrameLayout rootView = mBuilder.getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.removeView(DragView.this);
    }


}
