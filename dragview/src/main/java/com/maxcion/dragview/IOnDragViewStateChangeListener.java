package com.maxcion.dragview;

public interface IOnDragViewStateChangeListener {

    /**
     * 当dragView 滑动到顶部或者底部的回调
     * @param dismiss true 代表 滑动到底部， false 代表滑动到顶部
     */
    void onChange(boolean dismiss);
}
