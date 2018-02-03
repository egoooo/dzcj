package newjohn.com.dzcj.aty;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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
import newjohn.com.dzcj.Global;
import newjohn.com.dzcj.R;
import newjohn.com.dzcj.bean.AlertBean;
import newjohn.com.dzcj.bean.DeviceDataBean;
import newjohn.com.dzcj.bean.ProjectConfigBean;
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


public class ConfigActivity extends BaseActivity implements  AutoListView.OnRefreshListener, AutoListView.OnLoadListener, AdapterView.OnItemClickListener {
    String TAG=ConfigActivity.class.getName();
    private AutoListView lstv1;
    private CHScrollView headerScroll1;
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();//
    private ListViewScrollAdapter adapter1; //表格的适配器
    private CustomDatePicker customDatePicker1, customDatePicker2;


    private OkHttpClient okHttpClient;

    private int page=1;
    private int pageSize=20;


    private Gson gson=new Gson();


    ProjectConfigBean projectConfigBean;


    private Toolbar toolbar;




    @BindView(R.id.search_c)
    ImageView searchButton;

    LinearLayout linearLayoutwait;

    private SpinerPopWindow<String> mSpinerPopWindow1;


    private TextView projectTv;


    private String projectId="";


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            linearLayoutwait.setVisibility(View.GONE);
            List<Map<String, String>> result = (List<Map<String, String>>) msg.obj;
            switch (msg.what) {
                case AutoListView.REFRESH:
                    lstv1.onRefreshComplete();
                    list.clear();
                    list.addAll(result);
                    break;
                case AutoListView.LOAD:
                    lstv1.onLoadComplete();
                    list.addAll(result);
                    break;
            }
            lstv1.setResultSize(result.size());
            adapter1.notifyDataSetChanged();
        };
    };





    /**
     * 监听popupwindow取消
     */
    private PopupWindow.OnDismissListener dismissListener=new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {

            setTextImage(R.drawable.x,projectTv);
        }
    };

    /**
     * popupwindow显示的ListView的item点击事件
     */


    private AdapterView.OnItemClickListener itemClickListener1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            mSpinerPopWindow1.dismiss();
            projectTv.setText(Global.projectNames.get(position));
            projectId=Global.projectIds.get(position);
            Toast.makeText(ConfigActivity.this, "点击了g:" + Global.projectNames.get(position),Toast.LENGTH_LONG).show();
        }
    };
    /**
     * 显示PopupWindow
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.project_c:
                    mSpinerPopWindow1.setWidth(projectTv.getWidth());
                    mSpinerPopWindow1.showAsDropDown(projectTv);
                    setTextImage(R.drawable.s,projectTv);
                    break;
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
        setContentView(R.layout.activity_config);
        linearLayoutwait=findViewById(R.id.hwait_c);
        okHttpClient=new OkHttpClient();
        ButterKnife.bind(this);
        initView();
        initData();

        search();




    }


    private void initView() {
        toolbar = findViewById(R.id.toolbar_c);


        //设置导航图标要在setSupportActionBar方法之后
        setSupportActionBar(toolbar);


        headerScroll1 = (CHScrollView) findViewById(R.id.item_scroll_title_c);
        CHScrollView.CHScrollViewHelper.mHScrollViews.clear();


        CHScrollView.CHScrollViewHelper.mHScrollViews.add(headerScroll1);
        lstv1 = (AutoListView) findViewById(R.id.scroll_list_c);
        adapter1 = new ListViewScrollAdapter(this, list, R.layout.auto_listview_item_config,
                new String[]{
                "projectName",
                "density",
                "pressureCoe",
                "specificationsCoe",
                "instrument",
                "alarmHigh",
                "alarmLow",
                "dataPoints",
                "deviation",
                "filterParam",
                "intervalTime",
                "onOff"},
                new int[]{R.id.item_title_c, R.id.item_data1_c, R.id.item_data2_c, R.id.item_data3_c, R.id.item_data4_c, R.id.item_data5_c,R.id.item_data6_c, R.id.item_data7_c, R.id.item_data8_c, R.id.item_data9_c, R.id.item_data10_c,R.id.item_data11_c},
                R.id.item_scroll_c, lstv1);

        lstv1.setAdapter(adapter1);
        lstv1.setOnRefreshListener(this);
        lstv1.setOnLoadListener(this);
        lstv1.setOnItemClickListener(this);



        projectTv=findViewById(R.id.project_c);
        projectTv.setOnClickListener(clickListener);

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


                        RequestBody requestBody=new FormBody.Builder()
//                                .add("projectId", projectId)
//                                .add("deviceCode",deviceCode)
//                                .add("beginDate",start_time)
//                                .add("endDate",end_time)
                                .add("pageNo",page+"")
                                .add("pageSize",pageSize+"")
                                .build();
                        Log.i(TAG, "onClick-refresh: "+projectId+"-"+page);

                        final Request request=new Request.Builder()
                                .url("http://183.66.64.47:8090/api/common/deviceConfig")
                                .post(requestBody)
                                .build();
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.i(TAG, "onFailure: "+e.toString());

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lstv1.onRefreshComplete();
                                        lstv1.setResultSize(0);
                                        adapter1.notifyDataSetChanged();
                                        Toast.makeText(ConfigActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();


                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String str=response.body().string();

                                Log.i(TAG, "onResponse: "+str);


                                projectConfigBean=gson.fromJson(str,ProjectConfigBean.class);
                                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                                Map<String, String> data = null;

                                List<ProjectConfigBean.List> prjectConfigs= projectConfigBean.getResult().getList();
//
                                for (int i = 0; i < prjectConfigs.size(); i++) {


                                    data = new HashMap<String, String>();
                                    data.put("projectName",prjectConfigs.get(i).getProject().getProjectName());
                                    data.put("pressureCoe",prjectConfigs.get(i).getPressureCoe());
                                    data.put("specificationsCoe",prjectConfigs.get(i).getSpecificationsCoe());
                                    data.put("instrument",prjectConfigs.get(i).getInstrument());
                                    data.put("alarmHigh",prjectConfigs.get(i).getAlarmHigh());
                                    data.put("alarmLow",prjectConfigs.get(i).getAlarmLow());
                                    data.put("dataPoints",prjectConfigs.get(i).getDataPoints());
                                    data.put("deviation",prjectConfigs.get(i).getDeviation());
                                    data.put("filterParam",prjectConfigs.get(i).getFilterParam());
                                    data.put("intervalTime",prjectConfigs.get(i).getIntervalTime());
                                    data.put("onOff",prjectConfigs.get(i).getOnOff());
                                    data.put("density",prjectConfigs.get(i).getDensity());



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
//                                .add("projectId", projectId)
//                                .add("deviceCode",deviceCode)
//                                .add("beginDate",start_time)
//                                .add("endDate",end_time)
                                .add("pageNo",page+"")
                                .add("pageSize",pageSize+"")
                                .build();
                        Log.i(TAG, "onClick-load: "+projectId+""+page);

                        final Request request1=new Request.Builder()
                                .url("http://183.66.64.47:8090/api/common/deviceConfig")
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
                                       lstv1.onRefreshComplete();
                                       lstv1.setResultSize(0);
                                       adapter1.notifyDataSetChanged();
                                       Toast.makeText(ConfigActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();
                                   }
                               });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String str=response.body().string();

                                Log.i(TAG, "onResponse: "+str);


                                projectConfigBean=gson.fromJson(str,ProjectConfigBean.class);
                                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                                Map<String, String> data = null;

                                List<ProjectConfigBean.List> prjectConfigs= projectConfigBean.getResult().getList();
//
                                for (int i = 0; i < prjectConfigs.size(); i++) {


                                    data = new HashMap<String, String>();
                                    data.put("projectName",prjectConfigs.get(i).getProject().getProjectName());
                                    data.put("pressureCoe",prjectConfigs.get(i).getPressureCoe());
                                    data.put("specificationsCoe",prjectConfigs.get(i).getSpecificationsCoe());
                                    data.put("instrument",prjectConfigs.get(i).getInstrument());
                                    data.put("alarmHigh",prjectConfigs.get(i).getAlarmHigh());
                                    data.put("alarmLow",prjectConfigs.get(i).getAlarmLow());
                                    data.put("dataPoints",prjectConfigs.get(i).getDataPoints());
                                    data.put("deviation",prjectConfigs.get(i).getDeviation());
                                    data.put("filterParam",prjectConfigs.get(i).getFilterParam());
                                    data.put("intervalTime",prjectConfigs.get(i).getIntervalTime());
                                    data.put("onOff",prjectConfigs.get(i).getOnOff());
                                    data.put("density",prjectConfigs.get(i).getDensity());



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






    public void search(){
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutwait.setVisibility(View.VISIBLE);
                page=1;

                RequestBody requestBody=new FormBody.Builder()
//                                .add("projectId", projectId)
//                                .add("deviceCode",deviceCode)
//                                .add("beginDate",start_time)
//                                .add("endDate",end_time)
                        .add("pageNo",page+"")
                        .add("pageSize",pageSize+"")
                        .build();
                Log.i(TAG, "onClick-search: "+projectId+"-"+page);

                final Request request=new Request.Builder()
                        .url("http://183.66.64.47:8090/api/common/deviceConfig")
                        .post(requestBody)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure: "+e.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lstv1.onRefreshComplete();
                                lstv1.setResultSize(0);
                                adapter1.notifyDataSetChanged();
                                Toast.makeText(ConfigActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();


                            }
                        });

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str=response.body().string();

                        Log.i(TAG, "onResponse: "+str);


                        projectConfigBean=gson.fromJson(str,ProjectConfigBean.class);
                        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                        Map<String, String> data = null;

                        List<ProjectConfigBean.List> prjectConfigs= projectConfigBean.getResult().getList();
//
                        for (int i = 0; i < prjectConfigs.size(); i++) {


                            data = new HashMap<String, String>();
                            data.put("projectName",prjectConfigs.get(i).getProject().getProjectName());
                            data.put("pressureCoe",prjectConfigs.get(i).getPressureCoe());
                            data.put("specificationsCoe",prjectConfigs.get(i).getSpecificationsCoe());
                            data.put("instrument",prjectConfigs.get(i).getInstrument());
                            data.put("alarmHigh",prjectConfigs.get(i).getAlarmHigh());
                            data.put("alarmLow",prjectConfigs.get(i).getAlarmLow());
                            data.put("dataPoints",prjectConfigs.get(i).getDataPoints());
                            data.put("deviation",prjectConfigs.get(i).getDeviation());
                            data.put("filterParam",prjectConfigs.get(i).getFilterParam());
                            data.put("intervalTime",prjectConfigs.get(i).getIntervalTime());
                            data.put("onOff",prjectConfigs.get(i).getOnOff());
                            data.put("density",prjectConfigs.get(i).getDensity());



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







}
