package newjohn.com.dzcj.aty;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import newjohn.com.dzcj.Constant;


import newjohn.com.dzcj.Global;
import newjohn.com.dzcj.R;
import newjohn.com.dzcj.bean.DeviceDataBean;
import newjohn.com.dzcj.listviewscroll.AutoListView;
import newjohn.com.dzcj.listviewscroll.CHScrollView;
import newjohn.com.dzcj.listviewscroll.ListViewScrollAdapter;
import newjohn.com.dzcj.ui.CustomDatePicker;
import newjohn.com.dzcj.ui.SpinerPopWindow;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HistoryActivity extends BaseActivity implements  AutoListView.OnRefreshListener, AutoListView.OnLoadListener, AdapterView.OnItemClickListener {
    String TAG=HistoryActivity.class.getName();
    private AutoListView lstv;
    private CHScrollView headerScroll;
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();//
    private ListViewScrollAdapter adapter; //表格的适配器
    private CustomDatePicker customDatePicker1, customDatePicker2;


    private OkHttpClient okHttpClient;
    private String start_time="2017-11-14 12:00:00";
    private String end_time="";
    private int page=1;
    private int pageSize=20;
    private String deviceNum="null";
    private  String area="null";

    private String[] devicesList=null;
    ArrayList<String> dl;

    private Gson gson=new Gson();
    DeviceDataBean deviceDataBean;


    private Toolbar toolbar;
    @BindView(R.id.start)
    Button start;
    @BindView(R.id.start1)
    Button end;



    @BindView(R.id.search)
    ImageView searchButton;

    LinearLayout linearLayoutwait;
    private SpinerPopWindow<String> mSpinerPopWindow;
    private SpinerPopWindow<String> mSpinerPopWindow1;

    private TextView deviceTv;
    private TextView projectTv;

    private String deviceCode="";
    private String projectId="";


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            linearLayoutwait.setVisibility(View.GONE);
            List<Map<String, String>> result = (List<Map<String, String>>) msg.obj;
            switch (msg.what) {
                case AutoListView.REFRESH:
                    lstv.onRefreshComplete();
                    list.clear();
                    list.addAll(result);
                    break;
                case AutoListView.LOAD:
                    lstv.onLoadComplete();
                    list.addAll(result);
                    break;
            }
            lstv.setResultSize(result.size());
            adapter.notifyDataSetChanged();
        };
    };





    /**
     * 监听popupwindow取消
     */
    private PopupWindow.OnDismissListener dismissListener=new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            setTextImage(R.drawable.x,deviceTv);
            setTextImage(R.drawable.x,projectTv);
        }
    };

    /**
     * popupwindow显示的ListView的item点击事件
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            mSpinerPopWindow.dismiss();
            deviceTv.setText(Global.deviceNames.get(position));
            deviceCode=Global.deviceCodes.get(position);
            Toast.makeText(HistoryActivity.this, "点击了:" + Global.deviceNames.get(position),Toast.LENGTH_LONG).show();
        }
    };

    private AdapterView.OnItemClickListener itemClickListener1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            mSpinerPopWindow1.dismiss();
            projectTv.setText(Global.projectNames.get(position));
            projectId=Global.projectIds.get(position);
            Toast.makeText(HistoryActivity.this, "点击了g:" + Global.projectNames.get(position),Toast.LENGTH_LONG).show();
        }
    };
    /**
     * 显示PopupWindow
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_value:
                    mSpinerPopWindow.setWidth(deviceTv.getWidth());
                    mSpinerPopWindow.showAsDropDown(deviceTv);
                    setTextImage(R.drawable.s,deviceTv);
                    break;
                case R.id.tv_value1:
                    mSpinerPopWindow1.setWidth(projectTv.getWidth());
                    mSpinerPopWindow1.showAsDropDown(projectTv);
                    setTextImage(R.drawable.s,projectTv);
            }
        }
    };

    /**
     * 给TextView右边设置图片
     * @param resId
     */
    private void setTextImage(int resId,TextView textView) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
        textView.setCompoundDrawables(null, null, drawable, null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        linearLayoutwait=findViewById(R.id.hwait);
        okHttpClient=new OkHttpClient();
        ButterKnife.bind(this);
        initView();
        initData();
        setTime();
        search();




    }


    private void initView() {
        toolbar = findViewById(R.id.toolbar_h);


        //设置导航图标要在setSupportActionBar方法之后
        setSupportActionBar(toolbar);


        headerScroll = (CHScrollView) findViewById(R.id.item_scroll_title);
        CHScrollView.CHScrollViewHelper.mHScrollViews.clear();

        CHScrollView.CHScrollViewHelper.mHScrollViews.add(headerScroll);
        lstv = (AutoListView) findViewById(R.id.scroll_list);
        adapter = new ListViewScrollAdapter(this, list, R.layout.auto_listview_item,
                new String[]{"deviceName", "dateTime", "realHeight", "heightDifference", "temperature", "pressure"},
                new int[]{R.id.item_title, R.id.item_data1, R.id.item_data2, R.id.item_data3, R.id.item_data4, R.id.item_data5},
                R.id.item_scroll, lstv);

        lstv.setAdapter(adapter);
        lstv.setOnRefreshListener(this);
        lstv.setOnLoadListener(this);
        lstv.setOnItemClickListener(this);


        deviceTv = (TextView) findViewById(R.id.tv_value);
        deviceTv.setOnClickListener(clickListener);
        projectTv=findViewById(R.id.tv_value1);
        projectTv.setOnClickListener(clickListener);
        mSpinerPopWindow = new SpinerPopWindow<String>(this, Global.deviceNames,itemClickListener);
        mSpinerPopWindow.setOnDismissListener(dismissListener);
        mSpinerPopWindow1 = new SpinerPopWindow<String>(this, Global.projectNames,itemClickListener1);
        mSpinerPopWindow1.setOnDismissListener(dismissListener);


    }
    private void initData() {
        loadData(AutoListView.REFRESH);
    }

    private void loadData(final int what) {
        // 这里模拟从服务器获取数据
        new Thread(new Runnable() {

            @Override
            public void run() {

                switch (what){
                    case AutoListView.REFRESH:
                        page=1;
                        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date=new Date();
                        String str=sdf.format(date);
                        Log.i(TAG, "onCreate: "+"time"+str);
                        end_time=str;

                        RequestBody requestBody=new FormBody.Builder()
                                .add("projectId", projectId)
                                .add("deviceCode",deviceCode)
                                .add("beginDate",start_time)
                                .add("endDate",end_time)
                                .add("pageNo",page+"")
                                .add("pageSize",pageSize+"")
                                .build();
                        Log.i(TAG, "onClick-refresh: "+projectId+"-"+deviceCode+"-"+start_time+"-"+end_time+"-"+page);

                        final Request request=new Request.Builder()
                                .url("http://183.66.64.47:8090/api/common/deviceDataList?")
                                .post(requestBody)
                                .build();
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i(TAG, "onFailure: "+e.toString());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lstv.onRefreshComplete();
                                        lstv.setResultSize(0);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(HistoryActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();


                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String str=response.body().string();

                                Log.i(TAG, "onResponse: "+str);


                                deviceDataBean=gson.fromJson(str,DeviceDataBean.class);
                                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                                Map<String, String> data = null;

                                List<DeviceDataBean.List> devices=deviceDataBean.getResult().getList();
//
                                for (int i = 0; i < devices.size(); i++) {

                                    data = new HashMap<String, String>();
                                    data.put("deviceName", devices.get(i).getDevice().getName());
                                    data.put("dateTime", devices.get(i).getDateTime());
                                    data.put("realHeight", devices.get(i).getRealHeight());
                                    data.put("heightDifference",devices.get(i).getHeightDifference());
                                    data.put("temperature",devices.get(i).getTemperature());
                                    data.put("pressure",devices.get(i).getPressure());

                                    result.add(data);
                                }

                                Message msg = handler.obtainMessage();
                                msg.what = what;
                                msg.obj = result;
                                handler.sendMessage(msg);
                            }
                        });

                        break;
                    case AutoListView.LOAD:
                        page++;
                        RequestBody requestBody1=new FormBody.Builder()
                                .add("projectId", projectId)
                                .add("deviceCode",deviceCode)
                                .add("beginDate",start_time)
                                .add("endDate",end_time)
                                .add("pageNo",page+"")
                                .add("pageSize",pageSize+"")
                                .build();
                        Log.i(TAG, "load: "+projectId+"-"+deviceCode+"-"+start_time+"-"+end_time+"-"+page);

                        final Request request1=new Request.Builder()
                                .url("http://183.66.64.47:8090/api/common/deviceDataList?")
                                .post(requestBody1)
                                .build();

                        okHttpClient.newCall(request1).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i(TAG, "onFailure1: "+e.toString());
                                page--;
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       lstv.onRefreshComplete();
                                       lstv.setResultSize(0);
                                       adapter.notifyDataSetChanged();
                                       Toast.makeText(HistoryActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();
                                   }
                               });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                String str=response.body().string();
                                Log.i(TAG, "onResponse1: "+str);

                                deviceDataBean=gson.fromJson(str,DeviceDataBean.class);
                                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                                Map<String, String> data = null;

                                List<DeviceDataBean.List> devices=deviceDataBean.getResult().getList();
//
                                for (int i = 0; i < devices.size(); i++) {

                                    data = new HashMap<String, String>();
                                    data.put("deviceName", devices.get(i).getDevice().getName());
                                    data.put("dateTime", devices.get(i).getDateTime());
                                    data.put("realHeight", devices.get(i).getRealHeight());
                                    data.put("heightDifference",devices.get(i).getHeightDifference());
                                    data.put("temperature",devices.get(i).getTemperature());
                                    data.put("pressure",devices.get(i).getPressure());

                                    result.add(data);
                                }

                                Message msg = handler.obtainMessage();
                                msg.what = what;
                                msg.obj = result;
                                handler.sendMessage(msg);


                            }
                        });
                        break;
                }





            }
        }).start();
    }

    /**
     * 重写AutoListView.OnRefreshListener, AutoListView.OnLoadListener, AdapterView.OnItemClickListener的
     *  onRefresh()、onLoad()、onItemClick()方法，实现下拉刷新，加载更多，表格item点击事件
     */

    @Override
    public void onRefresh() {
        loadData(AutoListView.REFRESH);
    }

    @Override
    public void onLoad() {
        loadData(AutoListView.LOAD);
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            TextView textView = (TextView) adapterView.findViewById(R.id.item_data2);

            Toast.makeText(this, "你点击了：" + textView.getText(), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {

        }
    }


    public void setTime(){
        Calendar cal= Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);       //获取年月日时分秒
        Log.i("wxy","year"+year);
        final int month = cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        final int day = cal.get(Calendar.DAY_OF_MONTH);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());



                customDatePicker1 = new CustomDatePicker(HistoryActivity.this, new CustomDatePicker.ResultHandler() {
                    @Override
                    public void handle(String time) { // 回调接口，获得选中的时间
                        start.setText(time);
                        start_time=time+":00";
                    }
                }, "2010-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
                customDatePicker1.showSpecificTime(true); // 不显示时和分
                customDatePicker1.setIsLoop(false); // 不允许循环滚动
                customDatePicker1.show(now);
            }
        });


        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                Toast.makeText(HistoryActivity.this, "lllllll", Toast.LENGTH_SHORT).show();


                customDatePicker2 = new CustomDatePicker(HistoryActivity.this, new CustomDatePicker.ResultHandler() {
                    @Override
                    public void handle(String time) { // 回调接口，获得选中的时间
                      end_time=time+":00";
                        end.setText(time);
                    }
                }, "2010-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
                customDatePicker2.showSpecificTime(true); // 显示时和分
                customDatePicker2.setIsLoop(false); // 不允许循环滚动
                customDatePicker2.show(now);
            }
        });
    }




    public void search(){
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutwait.setVisibility(View.VISIBLE);
                page=1;


                RequestBody requestBody=new FormBody.Builder()
                     .add("projectId", projectId)
                     .add("deviceCode",deviceCode)
                        .add("beginDate",start_time)
                        .add("endDate",end_time)
                        .add("pageNo",page+"")
                        .add("pageSize",pageSize+"")
                        .build();
                Log.i(TAG, "onClick-search: "+projectId+"-"+deviceCode+"-"+start_time+"-"+end_time+"-"+page);

                final Request request=new Request.Builder()
                        .url("http://183.66.64.47:8090/api/common/deviceDataList?")
                        .post(requestBody)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure: "+e.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lstv.onRefreshComplete();
                                lstv.setResultSize(0);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(HistoryActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();


                            }
                        });

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str=response.body().string();

                        Log.i(TAG, "onResponse: "+str);


                        deviceDataBean=gson.fromJson(str,DeviceDataBean.class);
                        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                        Map<String, String> data = null;

                        List<DeviceDataBean.List> devices=deviceDataBean.getResult().getList();
//
                        for (int i = 0; i < devices.size(); i++) {

                            data = new HashMap<String, String>();
                            data.put("deviceName", devices.get(i).getDevice().getName());
                            data.put("dateTime", devices.get(i).getDateTime());
                            data.put("realHeight", devices.get(i).getRealHeight());
                            data.put("heightDifference",devices.get(i).getHeightDifference());
                            data.put("temperature",devices.get(i).getTemperature());
                            data.put("pressure",devices.get(i).getPressure());

                            result.add(data);
                        }

                        Message msg = handler.obtainMessage();
                        msg.what = 0;
                        msg.obj = result;
                        handler.sendMessage(msg);
                    }
                });

            }
        });


    }

//
//
//    public void search(){
//
//
//
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                linearLayoutwait.setVisibility(View.VISIBLE);
//
//
//
//                page=1;
//
//                RequestBody requestBody=new FormBody.Builder()
//                        .add("projectId", "188b1a2306b34956ac8d57c93dad24f0")
//                        .add("deviceCode",start_time)
//                        .add("beginDate",end_time)
//                        .add("endDate","")
//                        .add("pageNo",page+"")
//                        .add("pageSize",pageSize+"")
//                        .build();
//
//                final Request request=new Request.Builder()
//                        .url(Constant.URL+"FeedWebProject/historySearch")
//                        .post(requestBody)
//                        .build();
//                okHttpClient.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        Log.i(TAG, "onFailure: "+e.toString());
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                linearLayoutwait.setVisibility(View.GONE);
//                                lstv.setResultSize(0);
//                                adapter.notifyDataSetChanged();
//                                Toast.makeText(HistoryActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();
//
//
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        String str=response.body().string();
//
//                        Log.i(TAG, "onResponse: "+str);
//
//
//                        pigpenHisDatas=gson.fromJson(str,new TypeToken<ArrayList<PigpenHisData>>() {}.getType());
//
//                        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
//                        Map<String, String> data = null;
//                        pigpenHisDataToDBDao.deleteAll();
//                        for (int i = 0; i < pigpenHisDatas.size(); i++) {
//                            data = new HashMap<String, String>();
//                            data.put("area", pigpenHisDatas.get(i).getArea());
//                            data.put("deviceNum", pigpenHisDatas.get(i).getDeviceNum());
//                            data.put("value" , pigpenHisDatas.get(i).getValue()+sensorType(pigpenHisDatas.get(i).getDeviceNum()));
//                            data.put("dateTime" ,pigpenHisDatas.get(i).getDateTime());
//                            PigpenHisDataToDB pigpenHisDataToDB=new PigpenHisDataToDB(null,pigpenHisDatas.get(i).getArea(),pigpenHisDatas.get(i).getDeviceNum(),pigpenHisDatas.get(i).getValue(),pigpenHisDatas.get(i).getDateTime());
//
//                           pigpenHisDataToDBDao.insert(pigpenHisDataToDB);
//                            result.add(data);
//                        }
//
//                        Message msg = handler.obtainMessage();
//                        msg.what = 0;
//                        msg.obj = result;
//                        handler.sendMessage(msg);
//                    }
//                });
//
//            }
//        });
//
//
//
//    }



}
