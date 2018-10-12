package cn.huse.prepare;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.service.autofill.Dataset;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;
import cn.com.newland.nle_sdk.responseEntity.ProjectInfo;
import cn.com.newland.nle_sdk.responseEntity.base.BasePager;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import cn.com.newland.nle_sdk.util.Tools;
import cn.huse.myapplication.R;
import cn.huse.prepare.base.BaseActivity;
import cn.huse.prepare.util.Constans;
import cn.huse.prepare.util.DataCache;
import cn.huse.prepare.util.Device;
import cn.huse.prepare.util.Utils;
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
import org.xutils.view.annotation.ViewInject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

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
    Context context;
    private boolean isRecode = false;

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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LineDataSet lineDataSet;
            //画图
            if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
                lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
                lineDataSet.addEntry((Entry) msg.obj);
                lineChart.getData().notifyDataChanged();
                lineChart.notifyDataSetChanged();
                System.out.println(lineDataSet.getYMax());
            } else {
                List<Entry> values=new ArrayList<>();
                values.add((Entry) msg.obj);
                lineDataSet = new LineDataSet(values, "");
                LineData data = new LineData(lineDataSet);
                lineChart.setData(data);
            }
            lineChart.invalidate();
        }
    };

    private void initBtn() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始记录
                isRecode = true;

                new Thread() {
                    int x = 0;

                    @Override
                    public void run() {

                        while (isRecode) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            x++;
                            Message msg = Message.obtain();
                            msg.obj = new Entry(x, x * x);
                            handler.sendMessage(msg);
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
        Description description = new Description();
        description.setText("温度记录");
        description.setTextColor(Color.BLUE);
        description.setTextSize(10);
        lineChart.setDescription(description);//设置图表描述信息
        lineChart.setNoDataText("暂无数据");
        lineChart.setScaleEnabled(false);
        lineChart.setVisibleXRangeMaximum(10);
        //绘制动画
        lineChart.animateX(2500);
    }
}
