package com.andframe.impl.pager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.andframe.activity.AfActivity;
import com.andframe.activity.AfFragmentActivity;
import com.andframe.api.pager.PagerManager;
import com.andframe.application.AfApp;
import com.andframe.exception.AfException;
import com.andframe.feature.AfIntent;
import com.andframe.fragment.AfFragment;

import java.util.Stack;

/**
 * 页面堆栈管理器
 * Created by SCWANG on 2016/11/29.
 */

public class AfPagerManager implements PagerManager {

    //<editor-fold desc="功能实现">
    // 当前主页面
    private Stack<AfActivity> mStackActivity = new Stack<>();

    public AfPagerManager() {
        System.out.println(this + " new - size = " + mStackActivity.size());
    }

    @Override
    public void onActivityCreated(AfActivity activity) {
        if (!mStackActivity.contains(activity)) {
            mStackActivity.push(activity);
        }
        System.out.println(this + " onActivityCreated - " + activity + " size = " + mStackActivity.size());
    }

    @Override
    public void onActivityDestroy(AfActivity activity) {
        if (mStackActivity.contains(activity)) {
            mStackActivity.remove(activity);
        }
        System.out.println(this + " onActivityDestroy - " + activity + " size = " + mStackActivity.size());
    }

    @Override
    public void onActivityResume(AfActivity activity) {

    }

    @Override
    public void onActivityPause(AfActivity activity) {

    }

    @Override
    public void onFragmentAttach(AfFragment fragment, Context context) {

    }

    @Override
    public void onFragmentDetach(AfFragment fragment) {

    }

    @Override
    public void onFragmentResume(AfFragment fragment) {

    }

    @Override
    public void onFragmentPause(AfFragment fragment) {

    }

    @Override
    public boolean hasActivityRuning() {
        return !mStackActivity.isEmpty();
    }

    @Override
    public boolean hasActivity(Class<? extends AfActivity> clazz) {
        for (Activity activity : mStackActivity) {
            if (activity.getClass().equals(clazz)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AfActivity currentActivity() {
        System.out.println(this + " currentActivity - size = " + mStackActivity.size());
        if (mStackActivity.isEmpty()) {
            return null;
        }
        return mStackActivity.peek();
    }

    @Override
    public AfActivity getActivity(Class<? extends AfActivity> clazz) {
        for (AfActivity activity : mStackActivity) {
            if (activity.getClass().equals(clazz)) {
                return activity;
            }
        }
        return null;
    }

    @Override
    public void finishCurrentActivity() {
        AfActivity activity = currentActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    @Override
    public void finishActivity(AfActivity activity) {
        if (activity != null && mStackActivity.contains(activity)) {
            activity.finish();
        }
    }

    @Override
    public void finishAllActivity() {
        for (int i = 0, size = mStackActivity.size(); i < size; i++) {
            if (null != mStackActivity.get(i)) {
                mStackActivity.get(i).finish();
            }
        }
    }

    @Override
    public void startForeground() {
        throw new AfException("如要使用startForeground功能，请自行继承AfPagerManager并实现startForeground");
    }

    @Override
    public void startActivity(Class<? extends Activity> clazz, Object... args) {
        AfActivity activity = currentActivity();
        if (activity != null && activity.isRecycled()) {
            activity.startActivity(clazz, args);
        } else {
            AfApp app = AfApp.get();
            AfIntent intent = new AfIntent(app, clazz);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putKeyVaules(args);
            app.startActivity(intent);
        }
    }

    @Override
    public void startFragment(Class<? extends Fragment> clazz, Object... args) {
        AfActivity activity = currentActivity();
        if (activity != null && activity.isRecycled()) {
            activity.startFragment(clazz, args);
        } else {
            AfFragmentActivity.start(clazz, args);
        }
    }

    @Override
    public void startActivityForResult(Class<? extends Activity> clazz, int request, Object... args) {
        AfActivity activity = currentActivity();
        if (activity != null && activity.isRecycled()) {
            activity.startActivityForResult(clazz, request, args);
        }
    }

    @Override
    public void startFragmentForResult(Class<? extends Fragment> clazz, int request, Object... args) {
        AfActivity activity = currentActivity();
        if (activity != null && activity.isRecycled()) {
            activity.startFragment(clazz, request, args);
        }
    }

    //</editor-fold>
}