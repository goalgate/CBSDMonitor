package cn.cbdi.cbsdmonitor;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.baidu.aip.manager.FaceSDKManager;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.SPUtils;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.RxActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.cbdi.cbsdmonitor.Function.Fun_FingerPrint.mvp.presenter.FingerPrintPresenter;
import cn.cbdi.cbsdmonitor.Function.Func_Face.mvp.presenter.FacePresenter;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class SplashActivity extends RxActivity {

    String TAG = SplashActivity.class.getSimpleName();

    public FingerPrintPresenter fpp = FingerPrintPresenter.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_splash);
        try {
            File key = new File(Environment.getExternalStorageDirectory() + File.separator + "key.txt");
            copyToClipboard(AppInit.getContext(), FileIOUtils.readFile2String(key));
        } catch (Exception e) {
            e.printStackTrace();
        }

        FacePresenter.getInstance().FaceInit(this,new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
                Log.e(TAG,"sdk init start");
            }

            @Override
            public void initSuccess() {
                Log.e(TAG,"sdk init success");
                Observable.timer(3, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(SplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Observer<Long>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull Long aLong) {
                                fpp.fpInit();
                                fpp.fpOpen();

                                Observable.timer(3, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .compose(SplashActivity.this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                                        .subscribe(new Observer<Long>() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {

                                            }

                                            @Override
                                            public void onNext(@NonNull Long aLong) {
                                                ActivityUtils.startActivity(getPackageName(),getPackageName()+".MainActivity");
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {

                                            }

                                            @Override
                                            public void onComplete() {


                                            }
                                        });

                            }

                            @Override
                            public void onError(@NonNull Throwable e) {

                            }

                            @Override
                            public void onComplete() {


                            }
                        });
            }

            @Override
            public void initFail(int errorCode, String msg) {
                Log.e(TAG,"sdk init fail:" + msg);
            }
        });


    }
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager systemService = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        systemService.setPrimaryClip(ClipData.newPlainText("text", text));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}