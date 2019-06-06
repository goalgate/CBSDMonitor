package cn.cbdi.cbsdmonitor.Alert;

import android.content.Context;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ToastUtils;

import cn.cbdi.cbsdmonitor.Status.OperationStatus;

public class ManagerAlert {

    AlertView alertView;

    private static ManagerAlert instance = null;

    public static ManagerAlert getInstance(Context context, ManagerAlertCallBack callBack) {
        if (instance == null) {
            instance = new ManagerAlert(context, callBack);
        }
        return instance;
    }

    private ManagerAlert(Context context, final ManagerAlertCallBack listener) {
        alertView = new AlertView("管理员，请选择以下操作", null, null, new String[]{"添加新的人员", "删除已有人员", "正常使用"}, null,
                context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    listener.adding();
                } else if (position == 1) {
                    listener.removing();
                } else if (position == 2) {
                    listener.working();
                }
            }
        });
    }

    public void show() {
        alertView.show();
    }

    public interface ManagerAlertCallBack {
        void adding();

        void removing();

        void working();
    }
}
