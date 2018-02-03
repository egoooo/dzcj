package newjohn.com.dzcj.aty;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import newjohn.com.dzcj.Global;
import newjohn.com.dzcj.R;
import newjohn.com.dzcj.bean.DeBean;
import newjohn.com.dzcj.bean.LoginBean;
import newjohn.com.dzcj.bean.ProjectBean;
import newjohn.com.dzcj.login.LoginActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG="MainActivity";


    OkHttpClient okHttpClient;
    Gson gson;
    DeBean deBean;
    ProjectBean projectBean;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String reply= (String) msg.obj;
                    deBean=gson.fromJson(reply,DeBean.class);
                    if (deBean.getStatus()==1){
                        List<DeBean.List> deviceNameList=deBean.getResult().getList();
                        Global.deviceCodes.clear();
                        Global.deviceNames.clear();
                        Global.deviceIds.clear();
                        Global.deviceCodes.add("");
                        Global.deviceNames.add("all");
                        Global.deviceIds.add("");
                        for (int i=0;i<deviceNameList.size();i++){
                            Global.deviceCodes.add(deviceNameList.get(i).getDeviceCode());

                            Global.deviceIds.add(deviceNameList.get(i).getId());
                            Global.deviceNames.add(deviceNameList.get(i).getName());
                        }
                    }
                    break;
                case 2:
                    String reply1= (String) msg.obj;
                    projectBean=gson.fromJson(reply1,ProjectBean.class);
                    if (projectBean.getStatus()==1){
                        List<ProjectBean.List> projectList=projectBean.getResult().getList();
                        Global.projectIds.clear();
                        Global.projectNames.clear();
                        Global.projectNames.add("all");
                        Global.projectIds.add("");
                        for (int i=0;i<projectList.size();i++){
                            Global.projectIds.add(projectList.get(i).getId());
                            Global.projectNames.add(projectList.get(i).getProjectName());
                        }
                    }
                    break;


            }
        }
    };
    CardView history_card;
    private CardView online_card;
    private CardView alert_card;
    private CardView config_card;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: "+ Global.uid+Global.token);
        history_card=findViewById(R.id.histoty_card);
        online_card=findViewById(R.id.online_card);
        alert_card=findViewById(R.id.alert_card);
        config_card=findViewById(R.id.config_card);
       history_card.setOnClickListener(this);
       online_card.setOnClickListener(this);
        alert_card.setOnClickListener(this);
        config_card.setOnClickListener(this);
        gson=new Gson();
        getDeviceListAndProjectList();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.histoty_card:
                startActivity(new Intent(MainActivity.this,HistoryActivity.class));
                break;
            case R.id.alert_card:
                startActivity(new Intent(MainActivity.this,AlertActivity.class));
                break;
            case R.id.config_card:
                startActivity(new Intent(MainActivity.this,ConfigActivity.class));
                break;
            case R.id.online_card:
                startActivity(new Intent(MainActivity.this,DeviceActivity.class));


        }

    }


    public void getDeviceListAndProjectList(){
        okHttpClient=new OkHttpClient();

        RequestBody requestBody=new FormBody.Builder()
                .add("uid",Global.uid)
                .add("token",Global.token)
                .build();
        Request request=new Request.Builder()
                .url("http://183.66.64.47:8090/api/user/deviceList")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: "+e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(TAG, "onResponse: " + str);
                Message msg = handler.obtainMessage();
                msg.what = 1;
                msg.obj = str;
                handler.sendMessage(msg);

            }
        });

        RequestBody requestBody1=new FormBody.Builder()

                .build();
        Request request1=new Request.Builder()
                .url("http://183.66.64.47:8090/api/common/projectList")
                .post(requestBody1)
                .build();
        okHttpClient.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "onFailure: "+e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i(TAG, "onResponse: " + str);
                Message msg = handler.obtainMessage();
                msg.what = 2;
                msg.obj = str;
                handler.sendMessage(msg);

            }
        });

    }




}
