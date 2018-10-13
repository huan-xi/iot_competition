package cn.huse.prepare;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.com.newland.nle_sdk.responseEntity.ListItemOfDevice;
import cn.com.newland.nle_sdk.responseEntity.ListItemOfNewestData;
import cn.com.newland.nle_sdk.responseEntity.ProjectInfo;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BasePager;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import cn.huse.myapplication.R;
import cn.huse.prepare.adapter.ContentPage;
import cn.huse.prepare.base.BaseActivity;
import cn.huse.prepare.util.*;
import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.table.TableData;
import com.github.mikephil.charting.charts.LineChart;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import org.xutils.DbManager;
import org.xutils.db.Selector;
import org.xutils.db.annotation.Table;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.xml.datatype.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    @ViewInject(R.id.read)
    QMUIRoundButton btn_read;
    @ViewInject(R.id.table)
    SmartTable table;
    @ViewInject(R.id.topBar)
    QMUITopBar topBar;
    Context context;
    @ViewInject(R.id.tabSegment)
    QMUITabSegment mTabSegment;
    @ViewInject(R.id.contentViewPager)
    ViewPager mContentViewPager;
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
    private Map<ContentPage, View> mPageMap = new HashMap<>();
    private ContentPage mDestPage = ContentPage.Item1;

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return ContentPage.SIZE;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            ContentPage page = ContentPage.getPage(position);
            View view = getPageView(page);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    };



    private View getPageView(ContentPage page) {
        View view = mPageMap.get(page);
        if (view == null) {
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setTextColor(ContextCompat.getColor(this, R.color.bar_divider));

            if (page == ContentPage.Item1) {
                textView.setText("test1");
            } else if (page == ContentPage.Item2) {
                textView.setText("test2");
            }

            view = textView;
            mPageMap.put(page, view);
        }
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        context = this;
        initChar();
        initList();
        initBtn();
        initTable();
        initBar();
        topBar.setTitle("首页");
        netWorkBusiness = new NetWorkBusiness(DataCache.getToken(), Constans.getApiUrl());
        initSeekBar();
    }

    private void initBar() {
        mContentViewPager.setAdapter(mPagerAdapter);
        mContentViewPager.setCurrentItem(mDestPage.getPosition(), false);
        mTabSegment.addTab(new QMUITabSegment.Tab("折线图"));
        mTabSegment.addTab(new QMUITabSegment.Tab("记录表"));
        mTabSegment.setupWithViewPager(mContentViewPager, false);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initSeekBar() {

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

    private void initTable() {

    }


    private void initBtn() {
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DbManager dbManager = x.getDb(daoConfig);
                try {
                    List<POJO> pojos = dbManager.selector(POJO.class).findAll();
                    Column<String> column1 = new Column<>("时间", "time");
                    Column<Integer> column2 = new Column<>("数值", "value");
                    final TableData tableData = new TableData("表格名", pojos, column1, column2);
                    table.setTableData(tableData);
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
                                        Utils.saveEntity(daoConfig, value, time);
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
