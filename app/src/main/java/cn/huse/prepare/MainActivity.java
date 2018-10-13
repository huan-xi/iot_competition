package cn.huse.prepare;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import cn.com.newland.nle_sdk.responseEntity.ListItemOfDevice;
import cn.com.newland.nle_sdk.responseEntity.ListItemOfNewestData;
import cn.com.newland.nle_sdk.responseEntity.ProjectInfo;
import cn.com.newland.nle_sdk.responseEntity.base.BasePager;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import cn.com.newland.nle_sdk.util.Tools;
import cn.huse.myapplication.R;
import cn.huse.prepare.base.BaseActivity;
import cn.huse.prepare.util.*;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import org.xutils.DbManager;
import org.xutils.db.Selector;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.xml.datatype.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity {
    @ViewInject(R.id.up_down)
    SeekBar up_down;
    private NetWorkBusiness netWorkBusiness;
    @ViewInject(R.id.line_chart)
    LineChart lineChart;
    @ViewInject(R.id.groupListView)
    QMUIGroupListView groupListView;
    @ViewInject(R.id.start)
    QMUIRoundButton btn_start;
    @ViewInject(R.id.end)
    QMUIRoundButton btn_end;
    @ViewInject(R.id.save)
    QMUIRoundButton btn_save;
    @ViewInject(R.id.read)
    QMUIRoundButton btn_read;

    Context context;
    private boolean isRecode = false;
    DynamicLineChartManager dynamicLineChartManager;

    DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
            .setDbName("data.db")
            .setDbVersion(2)
            .setDbOpenListener(new DbManager.DbOpenListener() {
                @Override
                public void onDbOpened(DbManager db) {
                    // 开启WAL, 对写入加速提升巨大
                    db.getDatabase().enableWriteAheadLogging();
                }
            })
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        context = this;
        initChar();
        initList();
        initBtn();
        netWorkBusiness = new NetWorkBusiness(DataCache.getToken(), Constans.getApiUrl());
        up_down.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(seekBar.getProgress() + 6);
                netWorkBusiness.control(Device.getDEVICE(), Device.getUp_down(), seekBar.getProgress() + 6 + "", new Callback<BaseResponseEntity>() {
                    @Override
                    public void onResponse(Call<BaseResponseEntity> call, Response<BaseResponseEntity> response) {
                    }

                    @Override
                    public void onFailure(Call<BaseResponseEntity> call, Throwable t) {

                    }
                });
            }
        });
    }


    private void initBtn() {
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DbManager dbManager = x.getDb(daoConfig);
                try {
                   List<POJO> pojos=dbManager.selector(POJO.class).findAll();

                    System.out.println("test");
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始记录
                isRecode = true;
                new Thread() {
                    @Override
                    public void run() {

                        while (isRecode) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            netWorkBusiness.getSensorData(Device.getDEVICE(), Device.getGuanzhao(), null, null, null, null, null, "1", null, new Callback<BaseResponseEntity>() {
                                @Override
                                public void onResponse(Call<BaseResponseEntity> call, Response<BaseResponseEntity> response) {
                                    BaseResponseEntity<List<ListItemOfDevice>> baseResponseEntity = response.body();

                                    if (baseResponseEntity != null) {
                                        Map<String, Object> d = (Map) baseResponseEntity.getResultObj();
                                        List datas = (List) d.get("DataPoints");
                                        Map<String, Object> data = (Map<String, Object>) datas.get(0);
                                        List<Map> list = (List<Map>) data.get("PointDTO");
                                        double value = (double) list.get(0).get("Value");
                                        String time = (String) list.get(0).get("RecordTime");
                                        Utils.saveEntity(daoConfig,value ,time );
                                        time = time.substring(11);
                                        dynamicLineChartManager.addEntry((int) value, time);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "请求出错 : 请求参数不合法或者服务出错", Toast.LENGTH_SHORT);
                                    }
                                }

                                @Override
                                public void onFailure(Call<BaseResponseEntity> call, Throwable t) {

                                }
                            });
                        }
                    }
                }.start();

            }
        });
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecode = false;
            }
        });

    }

    private void initList() {
        QMUICommonListItemView itemWithSwitch = groupListView.createItemView("灯光控制");
        itemWithSwitch.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
        itemWithSwitch.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                int cmd = isChecked ? 1 : 0;
                netWorkBusiness.control(Device.getDEVICE(), Device.getLIGNT(), cmd, new Callback<BaseResponseEntity>() {
                    @Override
                    public void onResponse(Call<BaseResponseEntity> call, Response<BaseResponseEntity> response) {
                        BaseResponseEntity<BasePager<ProjectInfo>> baseResponseEntity = response.body();
                        if (baseResponseEntity != null) {
                            String string = isChecked ? "开启" : "关闭";
                            Utils.showSuceess(context, "灯光已" + string);
                        } else {
                            Toast.makeText(getApplicationContext(), "请求出错 : 请求参数不合法或者服务出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseEntity> call, Throwable t) {

                    }
                });
            }
        });
        QMUICommonListItemView fin = groupListView.createItemView("风扇控制");
        fin.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_SWITCH);
        fin.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                int cmd = isChecked ? 1 : 0;
                netWorkBusiness.control(Device.getDEVICE(), Device.getFAN(), cmd, new Callback<BaseResponseEntity>() {
                    @Override
                    public void onResponse(Call<BaseResponseEntity> call, Response<BaseResponseEntity> response) {
                        BaseResponseEntity<BasePager<ProjectInfo>> baseResponseEntity = response.body();
                        if (baseResponseEntity != null) {
                            String string = isChecked ? "开启" : "关闭";
                            Utils.showSuceess(context, "风扇已" + string);
                        } else {
                            Toast.makeText(getApplicationContext(), "请求出错 : 请求参数不合法或者服务出错", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseEntity> call, Throwable t) {

                    }
                });
            }
        });
        QMUIGroupListView.newSection(this)
                .setTitle("硬件控制").addItemView(itemWithSwitch, null)
                .setTitle("硬件控制").addItemView(fin, null)
                .addTo(groupListView);
    }

    private void initChar() {
        dynamicLineChartManager = new DynamicLineChartManager(lineChart, "记录", Color.BLUE);
        dynamicLineChartManager.setDescription("温度记录");
    }

}
