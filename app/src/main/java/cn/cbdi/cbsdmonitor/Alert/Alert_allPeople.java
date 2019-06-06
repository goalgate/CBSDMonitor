package cn.cbdi.cbsdmonitor.Alert;

import android.content.Context;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import cn.cbdi.cbsdmonitor.AppInit;
import cn.cbdi.cbsdmonitor.Bean.Employees;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class Alert_allPeople {

    AlertView alertView;

    private static Alert_allPeople instance = null;

    public static Alert_allPeople getInstance(Context context) {
        if (instance == null) {
            instance = new Alert_allPeople(context);
        }
        return instance;
    }

    private Alert_allPeople(final Context context) {
        final List<Employees> employeesList = AppInit.getInstance().getDaoSession().loadAll(Employees.class);
        String[] employees = new String[employeesList.size()];
        for (int i = 0; i < employeesList.size(); i++) {
            employees[i] = employeesList.get(i).getName();
        }
        alertView = new AlertView("请挑选要删除的人员", null, "取消",
                employees, null,
                context, AlertView.Style.ActionSheet, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, final int position) {
                if (position != -1) {
                    Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(@NonNull Long aLong) throws Exception {
                                    new AlertView("是否确定删除" + employeesList.get(position).getName(), null, "取消", new String[]{"确定"}, null, context, AlertView.Style.Alert, new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (position == 0) {
                                                AppInit.getInstance().getDaoSession().delete(employeesList.get(position));
                                            }
                                        }
                                    }).show();
                                }
                            });
                }
            }
        });
    }

    public void show() {
        Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        alertView.show();
                    }
                });
    }
}
