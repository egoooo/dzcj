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
import newjohn.com.dzcj.bean.DeBean;
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


public class DeviceActivity extends BaseActivity implements  AutoListView.OnRefreshListener, AutoListView.OnLoadListener, AdapterView.OnItemClickListener {
    String TAG=DeviceActivity.class.getName();
    private AutoListView lstv1;
    private CHScrollView headerScroll1;
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();//
    private ListViewScrollAdapter adapter1; //表格的适配器



    private OkHttpClient okHttpClient;

    private int page=1;
    private int pageSize=20;


    private Gson gson=new Gson();

    DeBean deBean;


    private Toolbar toolbar;




    @BindView(R.id.search_d)
    ImageView searchButton;

    LinearLayout linearLayoutwait;
    private SpinerPopWindow<String> mSpinerPopWindow;


    private TextView deviceTv;


    private String deviceCode="";



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
            setTextImage(R.drawable.x,deviceTv);

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
            Toast.makeText(DeviceActivity.this, "点击了:" + Global.deviceNames.get(position),Toast.LENGTH_LONG).show();
        }
    };


    /**
     * 显示PopupWindow
     */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.device_d:
                    mSpinerPopWindow.setWidth(deviceTv.getWidth());
                    mSpinerPopWindow.showAsDropDown(deviceTv);
                    setTextImage(R.drawable.s,deviceTv);
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
        setContentView(R.layout.activity_device);
        linearLayoutwait=findViewById(R.id.hwait_d);
        okHttpClient=new OkHttpClient();
        ButterKnife.bind(this);
        initView();
        initData();

        search();




    }


    private void initView() {
        toolbar = findViewById(R.id.toolbar_d);


        //设置导航图标要在setSupportActionBar方法之后
        setSupportActionBar(toolbar);


        headerScroll1 = (CHScrollView) findViewById(R.id.item_scroll_title_d);
        CHScrollView.CHScrollViewHelper.mHScrollViews.clear();


        CHScrollView.CHScrollViewHelper.mHScrollViews.add(headerScroll1);
        lstv1 = (AutoListView) findViewById(R.id.scroll_list_d);
        adapter1 = new ListViewScrollAdapter(this, list, R.layout.auto_listview_item_device,
                new String[]{
                        "deviceName",
                        "deviceType",
                        "deviceCode",
                        "company",
                        "office"},
                new int[]{R.id.item_title_d, R.id.item_data1_d, R.id.item_data2_d, R.id.item_data3_d, R.id.item_data4_d},
                R.id.item_scroll_d, lstv1);

        lstv1.setAdapter(adapter1);
        lstv1.setOnRefreshListener(this);
        lstv1.setOnLoadListener(this);
        lstv1.setOnItemClickListener(this);


        deviceTv = (TextView) findViewById(R.id.device_d);
        deviceTv.setOnClickListener(clickListener);

        mSpinerPopWindow = new SpinerPopWindow<String>(this, Global.deviceNames,itemClickListener);
        mSpinerPopWindow.setOnDismissListener(dismissListener);



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
                              .add("deviceCode",deviceCode)
//                                .add("beginDate",start_time)
//                                .add("endDate",end_time)
                                .add("uid",Global.uid)
                                .add("token",Global.token)
                                .add("pageNo",page+"")
                                .add("pageSize",pageSize+"")

                                .build();
                        Log.i(TAG, "onClick-refresh: "+deviceCode+"-"+page);

                        final Request request=new Request.Builder()
                                .url("http://183.66.64.47:8090/api/user/deviceList")
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
                                        Toast.makeText(DeviceActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();


                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String str=response.body().string();

                                Log.i(TAG, "onResponse: "+str);


                                deBean=gson.fromJson(str,DeBean.class);
                                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                                Map<String, String> data = null;

                                List<DeBean.List> devices=deBean.getResult().getList();
                                if (deBean.getStatus()==1){
                                    for (int i = 0; i < devices.size(); i++) {
                                        data = new HashMap<String, String>();
                                        data.put("deviceName", devices.get(i).getName());
                                        data.put("deviceType", devices.get(i).getType());
                                        data.put("deviceCode", devices.get(i).getDeviceCode());
                                        data.put("company",devices.get(i).getCompany().getName());
                                        data.put("office",devices.get(i).getOffice().getName());



                                        result.add(data);
                                    }
                                }
//


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
                               .add("deviceCode",deviceCode)
                                .add("uid",Global.uid)
                                .add("token",Global.token)
//                                .add("beginDate",start_time)
//                                .add("endDate",end_time)
                                .add("pageNo",page+"")
                                .add("pageSize",pageSize+"")
                                .build();
                        Log.i(TAG, "onClick-load: "+deviceCode+"-"+page);

                        final Request request1=new Request.Builder()
                                .url("http://183.66.64.47:8090/api/user/deviceList")
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
                                       Toast.makeText(DeviceActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();
                                   }
                               });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String str=response.body().string();

                                Log.i(TAG, "onResponse: "+str);


                                deBean=gson.fromJson(str,DeBean.class);
                                List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                                Map<String, String> data = null;

                                List<DeBean.List> devices=deBean.getResult().getList();
//
                                if (deBean.getStatus()==1){
                                    for (int i = 0; i < devices.size(); i++) {
                                        data = new HashMap<String, String>();
                                        data.put("deviceName", devices.get(i).getName());
                                        data.put("deviceType", devices.get(i).getType());
                                        data.put("deviceCode", devices.get(i).getDeviceCode());
                                        data.put("company",devices.get(i).getCompany().getName());
                                        data.put("office",devices.get(i).getOffice().getName());



                                        result.add(data);
                                    }
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
                                .add("deviceCode",deviceCode)
//                                .add("beginDate",start_time)
//                                .add("endDate",end_time)
                        .add("pageNo",page+"")
                        .add("pageSize",pageSize+"")
                        .add("uid",Global.uid)
                        .add("token",Global.token)
                        .build();
                Log.i(TAG, "onClick-search: "+"-"+deviceCode+"-"+page);

                final Request request=new Request.Builder()
                        .url("http://183.66.64.47:8090/api/user/deviceList")
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
                                Toast.makeText(DeviceActivity.this, "检查网络！", Toast.LENGTH_SHORT).show();


                            }
                        });

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str=response.body().string();

                        Log.i(TAG, "onResponse: "+str);


                        deBean=gson.fromJson(str,DeBean.class);
                        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
                        Map<String, String> data = null;

                        List<DeBean.List> devices=deBean.getResult().getList();
//
                        if (deBean.getStatus()==1){
                            for (int i = 0; i < devices.size(); i++) {
                                data = new HashMap<String, String>();
                                data.put("deviceName", devices.get(i).getName());
                                data.put("deviceType", devices.get(i).getType());
                                data.put("deviceCode", devices.get(i).getDeviceCode());
                                data.put("company",devices.get(i).getCompany().getName());
                                data.put("office",devices.get(i).getOffice().getName());



                                result.add(data);
                            }
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
