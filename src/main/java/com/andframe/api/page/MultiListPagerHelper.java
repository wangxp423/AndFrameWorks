package com.andframe.api.page;

import com.andframe.api.view.ItemsRefreshableViewer;
import com.andframe.api.view.ItemsViewer;
import com.andframe.task.AfListViewTask;

import java.util.Date;
import java.util.List;

/**
 * 多功能列表页帮助类接口
 * Created by SCWANG on 2016/9/7.
 */
public interface MultiListPagerHelper<T> extends ListPagerHelper<T> {

    /**
     * 显示数据页面
     */
    void showData();

    /**
     * 正在加载数据提示
     */
    void showLoading();

    /**
     * 处理空数据
     */
    void showNodata();

    /**
     * 错误信息处理
     *
     * @param ex 异常对象
     */
    void showLoadError(Throwable ex);

    /**
     * 加载数据（缓存优先）
     */
    void onLoad();

    /**
     * 用户加载分页通知事件
     *
     * @return 是否处理（影响列表控件响应状态）
     */
    boolean onMore();

    /**
     * 用户刷新数据通知事件
     *
     * @return 是否处理（影响列表控件响应状态）
     */
    boolean onRefresh();

    /**
     * 更新缓存
     */
    void putCache();

    /**
     * 清空缓存
     */
    void clearCache();

    /**
     * 更新缓存
     *
     * @param list 要缓存的数据
     */
    void putCache(List<T> list);

    /**
     * 获取缓存时间
     *
     * @return 如果没有缓存 返回 null
     */
    Date getCacheTime();

    /**
     * 任务准备开始 （在UI线程中）
     *
     * @return 返回true 表示准备完毕 否则 false 任务将被取消
     */
    boolean onTaskPrepare(int task);

    /**
     * 缓存加载结束处理时间（框架默认调用onRefreshed事件处理）
     *
     * @param task      任务执行对象
     * @param isfinish  任务是否完成（未捕捉到异常）
     * @param ltdata    完成加载数据
     * @param cachetime 缓存时间
     * @return 返回true 已经做好错误页面显示 返回false 框架会做好默认错误反馈
     */
    boolean onLoaded(AfListViewTask task, boolean isfinish, List<T> ltdata, Date cachetime);

    /**
     * 任务刷新结束处理事件
     *
     * @param task     任务执行对象
     * @param isfinish 任务是否完成（未捕捉到异常）
     * @param ltdata   完成加载数据
     * @return 返回true 已经做好错误页面显示 返回false 框架会做好默认错误反馈
     */
    boolean onRefreshed(AfListViewTask task, boolean isfinish, List<T> ltdata);

    /**
     * 任务加载更多结束处理事件
     *
     * @param task     任务执行对象
     * @param isfinish 任务是否完成（未捕捉到异常）
     * @param ltdata   完成加载数据
     * @return 返回true 已经做好错误页面显示 返回false 框架会做好默认错误反馈
     */
    boolean onMored(AfListViewTask task, boolean isfinish, List<T> ltdata);

    /**
     * 设置是否现实加载更多功能
     *
     * @param task   完成的任务
     * @param ltdata 任务加载的数据
     * @return true 显示更多功能 false 数据加载结束
     */
    boolean setMoreShow(AfListViewTask task, List<T> ltdata);


    /**
     * 加载缓存列表（不分页，在异步线程中执行，不可以更改页面操作）
     *
     * @param isCheckExpired 是否检测缓存过期（刷新失败时候可以加载缓存）
     * @return 返回 null 可以使用框架内置缓存
     */
    List<T> onTaskLoad(boolean isCheckExpired);

    void onTaskPushCache(List<T> list);

    void onTaskPutCache(List<T> list);

    /**
     * 由postTask(int task)触发
     * 除了与刷新、翻页、加载缓存有关的其他任务工作（异步线程、留给子类任务扩展用）
     *
     * @param task 任务标识
     * @return 是否成功执行
     */
    boolean onTaskWorking(int task) throws Exception;

    /**
     * 与onTaskWorking相对应的结束（UI线程）
     *
     * @param abListViewTask 任务执行对象
     * @param isfinish       任务是否完成（未捕捉到异常）
     * @param ltdata         完成加载数据
     * @return 返回true 已经做好错误页面显示 返回false 框架会做好默认错误反馈
     */
    boolean onTaskWorked(AfListViewTask abListViewTask, boolean isfinish, List<T> ltdata);

    AfListViewTask postTask(int task);

    /**
     * 创建新的AfListView
     *
     * @param listView ListView对象
     * @return 可刷新的ListView
     */
    ItemsRefreshableViewer<?> newAfListView(ItemsViewer listView);

    void addData(T value);

    void onDestroy();
}
