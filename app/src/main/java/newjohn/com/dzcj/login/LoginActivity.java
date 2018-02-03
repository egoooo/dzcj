package newjohn.com.dzcj.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.gson.Gson;

import java.io.IOException;

import newjohn.com.dzcj.Global;
import newjohn.com.dzcj.aty.MainActivity;
import newjohn.com.dzcj.R;
import newjohn.com.dzcj.bean.LoginBean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserName;
    private EditText etUserPassword;
    private ImageView unameClear;
    private ImageView pwdClear;
    private Button btnLogin;
    private String userName;
    private String userPassword;
    CheckBox rememberPass;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    LinearLayout linearLayoutWait;
    private String TAG="LoginActivity";
    Gson  gson;
    LoginBean loginBean;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String reply= (String) msg.obj;
                    loginBean=gson.fromJson(reply,LoginBean.class);


                    if (loginBean.getStatus()==0){
                        linearLayoutWait.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this,loginBean.getInfo(),Toast.LENGTH_SHORT).show();
                    }

                   else {
                        Toast.makeText(LoginActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                        Global.uid=loginBean.getResult().getId();
                        Global.token=loginBean.getResult().getToken();


                        editor=sharedPreferences.edit();
                        if (rememberPass.isChecked()){
                            editor.putBoolean("remember_password",true);
                            editor.putString("userName",etUserName.getText().toString());
                            editor.putString("password",etUserPassword.getText().toString());

                        }
                        else {
                            editor.clear();
                        }
                        editor.commit();

                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    break;

                case 2:
                    Toast.makeText(LoginActivity.this,"检查网络！",Toast.LENGTH_SHORT).show();
                    linearLayoutWait.setVisibility(View.GONE);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_activty);
        init();
    }

    private void init(){
        etUserName = (EditText) findViewById(R.id.et_userName);
        etUserPassword = (EditText) findViewById(R.id.et_password);
        unameClear = (ImageView) findViewById(R.id.iv_unameClear);
        pwdClear = (ImageView) findViewById(R.id.iv_pwdClear);
        btnLogin=findViewById(R.id.btn_login);
        linearLayoutWait=findViewById(R.id.loginwait);
        rememberPass=findViewById(R.id.cb_checkbox);


        EditTextClearTools.addClearListener(etUserName,unameClear);
        EditTextClearTools.addClearListener(etUserPassword,pwdClear);

        linearLayoutWait=findViewById(R.id.loginwait);
        final OkHttpClient okHttpClient=new OkHttpClient();
        gson=new Gson();
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember=sharedPreferences.getBoolean("remember_password",false);
        if (isRemember){
            String userName=sharedPreferences.getString("userName","");
            String password=sharedPreferences.getString("password","");
            etUserName.setText(userName);
            etUserPassword.setText(password);
            rememberPass.setChecked(true);
        }

        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName=etUserName.getText().toString();
                String password=etUserPassword.getText().toString();
                Log.i(TAG, "u: "+userName);
                Log.i(TAG, "p: "+password);
                if (userName.equals("")){
                    Toast.makeText(LoginActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
                }
                else if(password.equals("")){
                    Toast.makeText(LoginActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                }
                else {
                    linearLayoutWait.setVisibility(View.VISIBLE);
                    RequestBody requestBody=new FormBody.Builder()
                        .add("loginName",userName)
                        .add("password",password)
                        .build();
                    Request request=new Request.Builder()
                        .url("http://183.66.64.47:8090/api/common/login")
                        .post(requestBody)
                        .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i(TAG, "onFailure: "+e.toString());
                            Message msg = handler.obtainMessage();
                            msg.what = 2;
                            handler.sendMessage(msg);
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

                }
            }
        });
    }

}
