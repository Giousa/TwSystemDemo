package com.zmm.twsystemdemo.utils;

import android.app.Application;
import android.content.Context;

/**
 * Description:
 * Author:zhangmengmeng
 * Date:2017/3/20
 * Time:上午9:42
 */

public class SGBApplication extends Application {


    private static SGBApplication application;
    //全局上下文环境
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        mContext = getApplicationContext();

    }

    public static Context getContext() {
        return mContext;
    }


}
