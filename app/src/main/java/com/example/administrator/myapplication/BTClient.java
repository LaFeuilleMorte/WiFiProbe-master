package com.example.administrator.myapplication;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
public class BTClient extends Activity {
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    public String filename = ""; //用来保存存储的文件名
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备
    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);   //设置画面为主画面 main.xml
        Intent my_intent=getIntent();
        SysApplication.getInstance().addActivity(this);
        init_service();
   //信息列表按键
        Button button1=(Button)findViewById(R.id.button_user);
        Button button3=(Button) findViewById(R.id.button_router);
        Button button4=(Button)findViewById(R.id.home);
        Button button5=(Button)findViewById(R.id.Button_local);
        Button button6=(Button)findViewById(R.id.Button_cloud);
        Button buttonCollect=(Button)findViewById(R.id.Button_collector);
        Button buttonCollision=(Button)findViewById(R.id.Button_collision);
        Button button_distri=(Button)findViewById(R.id.distribute);
        button_distri.setOnClickListener((v)->{
            Intent intent=new Intent(this,Distribution_count.class);
            startActivity(intent);
        });

        button1.setOnClickListener((v)->{
            Intent intent=new Intent(BTClient.this,User_list.class);
            startActivity(intent);
        });

        button3.setOnClickListener((v)->{
            Intent intent=new Intent(BTClient.this,Any_kind.class);
            startActivity(intent);
        });
        button4.setOnClickListener((v)->{
            Intent intent=new Intent(BTClient.this,MainActivity.class);
            startActivity(intent);
        });
        button5.setOnClickListener((v)->{
            Intent intent=new Intent(BTClient.this,LocalDataBase.class);
            startActivity(intent);
        });
        button6.setOnClickListener((v)->{
            Intent intent=new Intent(BTClient.this,Radar_Activity.class);
            startActivity(intent);
        });
        buttonCollect.setOnClickListener((v)->{
            Intent intent=new Intent(BTClient.this,CollectorOfMac.class);
            startActivity(intent);
        });
        buttonCollision.setOnClickListener((v)->{

          Intent intent=new Intent(BTClient.this,CollisionOfMac.class);
            startActivity(intent);
        });
    }

    //服务开起来

    public void init_service() {
        Intent intent_1 = new Intent();
        intent_1.setClass(BTClient.this, MyIntentService.class);
        startService(intent_1);
        Log.i("start service", "successfully");
    }
   //禁用返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return false;
    }

}