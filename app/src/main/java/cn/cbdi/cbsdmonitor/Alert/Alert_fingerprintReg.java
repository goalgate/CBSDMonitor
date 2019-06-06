package cn.cbdi.cbsdmonitor.Alert;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnDismissListener;
import com.bigkoo.alertview.OnItemClickListener;

import cn.cbdi.cbsdmonitor.AppInit;
import cn.cbdi.cbsdmonitor.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.cbsdmonitor.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.cbsdmonitor.R;


public class Alert_fingerprintReg {

    private Context context;

    private AlertView fingerprintRegView;

    TextView tv_fingerInfo;

    ImageView iv_fingerprint;

    private static Alert_fingerprintReg instance = null;

    public static Alert_fingerprintReg getInstance(Context context) {
        if (instance == null) {
            instance = new Alert_fingerprintReg(context);
        }
        return instance;
    }

    private Alert_fingerprintReg(Context context) {
        this.context = context;
        fingerRegInit();
    }

    private void fingerRegInit() {
        ViewGroup extView1 = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.fingerreg_form, null);
        tv_fingerInfo = (TextView) extView1.findViewById(R.id.tv_fingerInfo);
        iv_fingerprint = (ImageView) extView1.findViewById(R.id.iv_fingerprint) ;
        fingerprintRegView = new AlertView("指纹登记", null, "关闭", null, null, this.context, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {

            }
        });
        fingerprintRegView.addExtView(extView1);
        fingerprintRegView.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(Object o) {
                IDCardPresenter.getInstance().readCard();
                FingerPrintPresenter.getInstance().fpCancel(true);
                FingerPrintPresenter.getInstance().fpIdentify();

            }
        });
    }

    public void show() {
        iv_fingerprint.setImageBitmap(BitmapFactory.decodeResource(AppInit.getContext().getResources(), R.drawable.zw_icon));
        fingerprintRegView.show();
    }


    public TextView getTv_fingerInfo() {
        return tv_fingerInfo;
    }

    public ImageView getIv_fingerprint() {
        return iv_fingerprint;
    }

    public boolean isShowing(){
        return fingerprintRegView.isShowing();
    }
}
