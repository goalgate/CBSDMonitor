package cn.cbdi.cbsdmonitor;

import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.Feature;
import com.baidu.aip.entity.User;
import com.baidu.aip.face.AutoTexturePreviewView;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.cbdi.cbsdmonitor.Bean.Employees;
import cn.cbdi.cbsdmonitor.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.cbsdmonitor.Function.Fun_FingerPrint.mvp.view.IFingerPrintView;
import cn.cbdi.cbsdmonitor.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.cbsdmonitor.Function.Func_Face.mvp.view.IFaceView;
import cn.cbdi.cbsdmonitor.Function.Func_IDCard.mvp.presenter.IDCardPresenter;
import cn.cbdi.cbsdmonitor.Function.Func_IDCard.mvp.view.IIDCardView;
import cn.cbdi.cbsdmonitor.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static cn.cbdi.cbsdmonitor.Function.Func_Face.mvp.Module.FaceImpl2.FEATURE_DATAS_UNREADY;

public abstract class FunctionActivity extends RxActivity implements IFaceView, IFingerPrintView, IIDCardView {

    private String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.preview_view)
    AutoTexturePreviewView previewView;

    @BindView(R.id.texture_view)
    TextureView textureView;

    SwitchPresenter sp = SwitchPresenter.getInstance();

    FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    FacePresenter fp = FacePresenter.getInstance();

    IDCardPresenter idp = IDCardPresenter.getInstance();

    String ver = "sync1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        idp.idCardOpen();
        sp.switch_Open();
        Log.e(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        idp.IDCardPresenterSetView(this);
        idp.readCard();
        fpp.FingerPrintPresenterSetView(this);
        fp.FacePresenterSetView(this);
        fp.FaceIdentifyReady();
        fp.FaceIdentify();
        Observable.timer(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
//                        fpp.fpIdentify();
                        SyncData();

                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        fpp.fpCancel(true);
        fpp.FingerPrintPresenterSetView(null);
        fp.PreviewCease();
        fp.FacePresenterSetView(null);
        idp.IDCardPresenterSetView(null);
        idp.stopReadCard();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        fp.FaceSetNoAction();
        fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fpp.fpClose();
        idp.idCardClose();
    }

    @Override
    public void onBackPressed() {

    }

    private SPUtils config = SPUtils.getInstance("config");

    private void SyncData() {
        if (config.getBoolean(ver, true)) {
            List<Employees> employeesList = AppInit.getInstance().getDaoSession().loadAll(Employees.class);
            Observable.just(employeesList)
                    .subscribeOn(Schedulers.computation())
                    .unsubscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Employees>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(List<Employees> employees) {
                            for (Employees employee : employees) {
                                fpp.fpDownTemplate(employee.getFpID(), employee.getFpTemp());
                                User user = new User();
                                user.setUserId(employee.getCardID());
                                user.setUserInfo(employee.getName());
                                user.setGroupId("1");
                                Feature feature = new Feature();
                                feature.setGroupId("1");
                                feature.setUserId(employee.getCardID());
                                feature.setFeature(employee.getFaseBytes());
                                user.getFeatureList().add(feature);
                                FaceApi.getInstance().userAdd(user);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            fpp.fpIdentify();
                            config.put(ver, false);
                        }
                    });


        } else {
            fpp.fpIdentify();
        }

    }
}
