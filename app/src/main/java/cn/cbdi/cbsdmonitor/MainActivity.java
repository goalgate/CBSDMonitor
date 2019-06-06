package cn.cbdi.cbsdmonitor;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.api.FaceApi;
import com.baidu.aip.entity.User;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnDismissListener;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.cbdi.cbsdmonitor.Alert.Alert_fingerprintReg;
import cn.cbdi.cbsdmonitor.Bean.SingleEmployee;
import cn.cbdi.cbsdmonitor.Bean.Employees;
import cn.cbdi.cbsdmonitor.Function.Func_Face.mvp.presenter.FacePresenter;
import cn.cbdi.cbsdmonitor.Function.Func_Switch.mvp.module.SwitchImpl;
import cn.cbdi.cbsdmonitor.Function.Func_Switch.mvp.presenter.SwitchPresenter;
import cn.cbdi.cbsdmonitor.Status.OperationStatus;
import cn.cbdi.cbsdmonitor.greendao.DaoSession;
import cn.cbdi.drv.card.CardInfoBean;
import cn.cbdi.drv.card.ICardInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static cn.cbdi.cbsdmonitor.Function.Func_Face.mvp.Module.FaceImpl2.FEATURE_DATAS_UNREADY;

public class MainActivity extends FunctionActivity {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    DaoSession mdaoSession = AppInit.getInstance().getDaoSession();

    Disposable disposableTips;

    @BindView(R.id.iv_setting)
    ImageView iv_setting;

    @BindView(R.id.tv_time)
    TextView tv_time;

    @BindView(R.id.tv_info)
    TextView tv_info;

    @OnClick(R.id.lay_setting)
    void option() {

    }

    ViewGroup addView;

    EditText et_IDCard;

    EditText et_name;

    AlertView addAlert;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        ButterKnife.bind(this);
        updateManager();
        UIInit();
        reboot();
    }




    private void UIInit() {
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(20, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws Exception {
                        if (OperationStatus.getInstance().getStatus() == OperationStatus.adding) {
                            return Observable.just("当前正处于添加人员模式");
                        } else if (OperationStatus.getInstance().getStatus() == OperationStatus.removing) {
                            return Observable.just("当前正处于删除人员模式");
                        } else {
                            return Observable.just("等待用户操作...");
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tv_info.setText(s);
                    }
                });

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        tv_time.setText(formatter.format(new Date(System.currentTimeMillis())));
                    }
                });
        addView = (ViewGroup) LayoutInflater.from(MainActivity.this).inflate(R.layout.add_person_form, null);
        et_IDCard = (EditText) addView.findViewById(R.id.et_IDCard);
        et_name = (EditText) addView.findViewById(R.id.et_name);
        addAlert = new AlertView("增加人员信息", null, "取消", new String[]{"确定"}, null, MainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (position == 0) {
                    if (TextUtils.isEmpty(et_IDCard.getText().toString()) || TextUtils.isEmpty(et_name.getText().toString())) {
                        ToastUtils.showLong("您的输入为空请重试");
                    } else {
                        ICardInfo MycardInfo = new CardInfoBean(et_IDCard.getText().toString(), et_name.getText().toString());
                        fp.FaceReg(MycardInfo, null);
                    }
                }
            }
        }).addExtView(addView);
        setGestures();
        Alert_fingerprintReg.getInstance(this).getIv_fingerprint().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fpp.fpCancel(true);
                Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                fpp.fpEnroll(fp_id);
                            }
                        });
            }
        });
    }

    private void updateManager() {
        File manager = new File(Environment.getExternalStorageDirectory() + File.separator + "manager.txt");
        String managerString = FileIOUtils.readFile2String(manager);
        String[] managers = managerString.split("\\|");
        for (String s : managers) {
            Employees employees;
            try {
                employees = mdaoSession.queryRaw(Employees.class, "where CARD_ID = " + s).get(0);
            } catch (IndexOutOfBoundsException e) {
                employees = new Employees();
            }
            employees.setCardID(s);
            employees.setType(1);
            mdaoSession.insertOrReplace(employees);
        }
    }

    @BindView(R.id.gestures_overlay)
    GestureOverlayView gestures;
    GestureLibrary mGestureLib;

    private void setGestures() {
        gestures.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
        gestures.setGestureVisible(false);
        gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay,
                                           Gesture gesture) {
                ArrayList<Prediction> predictions = mGestureLib.recognize(gesture);
                if (predictions.size() > 0) {
                    Prediction prediction = (Prediction) predictions.get(0);
                    // 匹配的手势
                    if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
                        if (prediction.name.equals("setting")) {
                            NetworkUtils.openWirelessSettings();
                        }
                    }
                }
            }
        });
        if (mGestureLib == null) {
            mGestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
            mGestureLib.load();
        }
    }

    private void reboot() {
        long daySpan = 24 * 60 * 60 * 1000;
        // 规定的每天时间，某时刻运行
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd '03:00:00'");
        // 首次运行时间
        try {
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sdf.format(new Date()));
            if (System.currentTimeMillis() > startTime.getTime())
                startTime = new Date(startTime.getTime() + daySpan);
            final Timer t = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // 要执行的代码
                    AppInit.getMyManager().reboot();
                }
            };
            t.scheduleAtFixedRate(task, startTime, daySpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OperationStatus.getInstance().setStatus(OperationStatus.working);
    }

    @Override
    public void onStart() {
        super.onStart();
        fp.CameraPreview(this, previewView, textureView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppInit.getMyManager().unBindAIDLService(AppInit.getContext());
        disposableTips.dispose();
    }

    @Override
    public void onsetCardImg(Bitmap bmp) {

    }

    @Override
    public void onsetCardInfo(ICardInfo cardInfo) {
        try {
            Employees employees = mdaoSession.queryRaw(Employees.class, "where CARD_ID = " + cardInfo.cardId()).get(0);
            if (employees.getType() == 1) {
                if (employees.getFaseBytes() != null) {
                    new AlertView("管理员，请选择以下操作", null, null, new String[]{"添加新的人员", "弹出身份证人名输入框", "删除已有人员", "正常使用"}, null,
                            this, AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 0) {
                                tv_info.setText("当前正处于添加人员模式");
                                OperationStatus.getInstance().setStatus(OperationStatus.adding);
                                fpp.fpCancel(true);
                                fp.FaceSetNoAction();
                                fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
                            } else if (position == 1) {
                                tv_info.setText("当前正处于添加人员模式");
                                OperationStatus.getInstance().setStatus(OperationStatus.adding);
                                fpp.fpCancel(true);
                                fp.FaceSetNoAction();
                                fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
                                Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(@NonNull Long aLong) throws Exception {
                                                addAlert.show();
                                            }
                                        });
                            } else if (position == 2) {
                                tv_info.setText("当前正处于删除人员模式");
                                OperationStatus.getInstance().setStatus(OperationStatus.removing);
                                fpp.fpCancel(true);
                                fp.FaceSetNoAction();
                                fp.setIdentifyStatus(FEATURE_DATAS_UNREADY);
                                Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(@NonNull Long aLong) throws Exception {
                                                final List<Employees> employeesList = AppInit.getInstance().getDaoSession().loadAll(Employees.class);
                                                String[] employees = new String[employeesList.size()];
                                                for (int i = 0; i < employeesList.size(); i++) {
                                                    employees[i] = employeesList.get(i).getName();
                                                }
                                                new AlertView("请挑选要删除的人员", null, "取消",
                                                        employees, null,
                                                        MainActivity.this, AlertView.Style.ActionSheet, new OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(Object o, final int position) {
                                                        if (position != -1) {
                                                            final int del_position = position;
                                                            Observable.timer(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                                                                    .subscribe(new Consumer<Long>() {
                                                                        @Override
                                                                        public void accept(@NonNull Long aLong) throws Exception {
                                                                            new AlertView("是否确定删除" + employeesList.get(position).getName(), null, "取消", new String[]{"确定"}, null, MainActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                                                                @Override
                                                                                public void onItemClick(Object o, int position) {
                                                                                    if (position == 0) {
                                                                                        FaceApi.getInstance().userDelete(employeesList.get(del_position).getCardID(), "1");
                                                                                        fpp.fpRemoveTmpl(employeesList.get(del_position).getFpID());
                                                                                        mdaoSession.delete(employeesList.get(del_position));

                                                                                    }
                                                                                }
                                                                            }).show();
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }).show();
                                            }
                                        });
                            } else if (position == 3) {
                                tv_info.setText("等待用户操作");
                                OperationStatus.getInstance().setStatus(OperationStatus.working);
                                fp.FaceIdentifyReady();
                                fp.FaceIdentify();
                                fpp.fpIdentify();
                            }
                        }
                    }).show();
                    return;
                } else {
                    fpp.fpCancel(true);
                    fp.FaceReg(cardInfo, null);
                    return;
                }
            } else {
                if (OperationStatus.getInstance().getStatus() == OperationStatus.adding) {
                    fp.FaceReg(cardInfo, null);
                } else {
                    openDoor(employees);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            if (OperationStatus.getInstance().getStatus() == OperationStatus.adding) {
                fp.FaceReg(cardInfo, null);
            } else {
                tv_info.setText("系统查无此人");
            }
        }

    }

    @Override
    public void onSetText(String Msg) {


    }

    @Override
    public void onSetImg(Bitmap bmp) {
        Alert_fingerprintReg.getInstance(this).getIv_fingerprint().setImageBitmap(bmp);

    }

    @Override
    public void onFpSucc(String msg) {
        try {
            Employees employees = mdaoSession.queryRaw(Employees.class, "where FP_ID = " + msg.substring(3, msg.length())).get(0);
            openDoor(employees);
        } catch (IndexOutOfBoundsException e) {

        }

    }

    @Override
    public void onText(String msg) {
        if (Alert_fingerprintReg.getInstance(this).isShowing()) {
            Alert_fingerprintReg.getInstance(this).getTv_fingerInfo().setText(msg);
            if (msg.contains("录入成功")) {
                Employees employees = SingleEmployee.getInstance().getEm();
                employees.setFpID(fp_id);
                employees.setFpTemp(fpp.fpUpTemlate(fp_id));
                try {
                    employees.setType(mdaoSession.queryRaw(Employees.class, "where CARD_ID = " + employees.getCardID()).get(0).getType());
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                fp_id = "0";
                mdaoSession.insertOrReplace(employees);
                FaceApi.getInstance().userAdd(global_User);
                SingleEmployee.getInstance().clear();
            }
        } else {
            if ("请确认指纹是否已登记".equals(msg)) {
                tv_info.setText("请确认指纹是否已登记,再重试");
            } else if ("松开手指".equals(msg)) {
                tv_info.setText(msg);
            }
        }

    }

    String fp_id = "0";

    @Override
    public void onText(FacePresenter.FaceResultType resultType, String text) {
        if (resultType.equals(FacePresenter.FaceResultType.Reg)) {
            if (text.equals("success")) {
                sp.buzz(SwitchImpl.Hex.H2);
                tv_info.setText("人脸数据获取成功");
                idp.stopReadCard();
                Alert_fingerprintReg.getInstance(MainActivity.this).show();
                fp_id = String.valueOf(fpp.fpGetEmptyID());
                fpp.fpEnroll(fp_id);
            } else {
                sp.buzz(SwitchImpl.Hex.H2);
                tv_info.setText("人脸数据获取失败,请重试");
                SingleEmployee.getInstance().clear();
            }
        }else if (resultType.equals(FacePresenter.FaceResultType.Identify_non)) {
                tv_info.setText("系统查无此人");

        }
    }

    @Override
    public void onBitmap(FacePresenter.FaceResultType resultType, Bitmap bitmap) {

    }


    User global_User;

    @Override
    public void onUser(FacePresenter.FaceResultType resultType, User user) {
        try {
            if (resultType.equals(FacePresenter.FaceResultType.Identify)) {
                Employees employees = mdaoSession.queryRaw(Employees.class, "where CARD_ID = " + user.getUserId()).get(0);
                openDoor(employees);
            } else if (resultType.equals(FacePresenter.FaceResultType.Reg)) {
                global_User = user;
            }
        } catch (IndexOutOfBoundsException e) {

        }

    }


    private void openDoor(Employees employees) {
        tv_info.setText(employees.getName() + "开门");
        sp.OutD9(true);
        Observable.timer(2,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        sp.OutD9(false);
                    }
                });

    }
}
