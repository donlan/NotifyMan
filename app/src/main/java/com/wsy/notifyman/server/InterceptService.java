package com.wsy.notifyman.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wsy.notifyman.common.IssuePublish;
import com.wsy.notifyman.model.master.Issue;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by 思远 on 2017/5/12.
 * 底层生成的故障信息会发送到这里，这里做拦截分发处理
 */

public class InterceptService extends Service implements Observer {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof IssuePublish){
            Issue issue = (Issue) arg;
            if(needDistribute(issue)){

            }
        }
    }


    /**
     * 该为题是否发送给客户端
     * @param issue
     * @return
     */
    private boolean needDistribute(Issue issue){


        return true;
    }
}