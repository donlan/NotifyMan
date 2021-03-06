package com.wsy.notifyman.server.views;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wsy.notifyman.Config;
import com.wsy.notifyman.R;
import com.wsy.notifyman.common.Group;
import com.wsy.notifyman.common.SPHelper;
import com.wsy.notifyman.model.Apply;
import com.wsy.notifyman.server.adapter.ApplyAdapter;

import java.util.Collections;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import dong.lan.base.ui.BaseFragment;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 */

public class AffairsFragment extends BaseFragment implements ApplyAdapter.ApplyCallBack {

    private ApplyAdapter applyAdapter;

    public static AffairsFragment newInstance(String tittle) {
        AffairsFragment fragment = new AffairsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("tittle", tittle);
        fragment.setArguments(bundle);
        return fragment;
    }


    private RecyclerView applyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(content == null){
            content = inflater.inflate(R.layout.fragment_affairs,container,false);
            applyList = (RecyclerView) content.findViewById(R.id.list);
            applyList.setLayoutManager(new GridLayoutManager(getContext(),1));
            start(null);
        }
        return content;
    }

    @Override
    public void start(Object data) {
        if(isAdded() && isStart){
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Apply> applies = realm.where(Apply.class).findAllSortedAsync("applyTime", Sort.DESCENDING);
            applies.addChangeListener(new RealmChangeListener<RealmResults<Apply>>() {
                @Override
                public void onChange(RealmResults<Apply> element) {
                    applyAdapter.notifyDataSetChanged();
                }
            });
            applyAdapter = new ApplyAdapter(applies);
            applyAdapter.setApplyCallBack(this);
            applyList.setAdapter(applyAdapter);
        }
        super.start(data);
    }

    @Override
    public void apply(final Apply apply) {
        final String user = apply.applyUser;
        new AlertDialog.Builder(getContext())
                .setTitle("绑定申请")
                .setMessage(apply.applyReason)
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactManager.acceptInvitation(apply.applyUser, "", new BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                                if(i == 0){
                                    addToGroup(apply.applyUser);
                                    Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            RealmResults<Apply> applies = realm.where(Apply.class).equalTo("applyUser",user).findAll();
                                            if(applies!=null && !applies.isEmpty())
                                            for(Apply a : applies){
                                                a.status = Config.APPLY_AGREE;
                                            }
                                        }
                                    });
                                }else{
                                    toast("同意请求失败，"+s);
                                }
                            }
                        });
                    }
                }).show();
    }

    private void addToGroup(final String user) {
        long groupId = SPHelper.get().getLong("groupId");
        if(groupId!=0 && !TextUtils.isEmpty(user)){
            JMessageClient.addGroupMembers(groupId, Collections.singletonList(user), new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    if(i == 0){
                        toast("入群成功");
                        JMessageClient.getUserInfo(user, new GetUserInfoCallback() {
                            @Override
                            public void gotResult(int i, String s, UserInfo userInfo) {
                                if(i == 0)
                                    Group.get().getUsers().add(userInfo);
                            }
                        });
                    }else{
                        toast("入群失败："+s);
                    }
                }
            });
        }
    }
}
