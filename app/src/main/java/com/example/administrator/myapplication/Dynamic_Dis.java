package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class Dynamic_Dis extends Activity {
    String value_str = "";
    Handler handler;
    String pre="";
    private DecimalFormat df=new DecimalFormat();
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Data_Receiver dataReceiver=new Data_Receiver();
        setContentView(R.layout.dynamic_layout);
        DataSource dataSource = DataSource.getInsDs();
        IntentFilter intentFilter_data = new IntentFilter("data_analysed");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);
        TextView tv1=(TextView)findViewById(R.id.tv_dyn);
        TextView tv2=(TextView)findViewById(R.id.tv_trend);
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        Handler handler1=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
             /*
               if(!value_str.equals("")&&!pre.equals("")) {
                   if (Double.parseDouble(pre) < Double.parseDouble(value_str)) {
                       tv2.setText("预测趋势：" + "远离");
                   }
                   if (Double.parseDouble(pre) > Double.parseDouble(value_str)) {
                       tv2.setText("预测趋势：" + "接近");
                   }
               }*/
                pre=value_str;
                handler1.postDelayed(this,7*1000);
            }
        };
        handler1.post(runnable);
        handler=new Handler(){
            public void handleMessage(Message msg){
              Log.d("res_mid的值是：", value_str + "");

              String[] raw=value_str.split("&");
 try{             Double lon=Double.parseDouble(raw[0]);
              Double lat=Double.parseDouble(raw[1]);
              Log.d("decimal",lon+"");
              Log.d("decimal",lat+"");
              String distance=df.format(110000*Math.pow(lon*lon+lat*lat,0.5));
             tv1.setText("估算距离："+distance);
                BullsView bullsView=(BullsView)findViewById(R.id.bulls);
                bullsView.invalidate();}catch (Exception e){}
                //刷新界面
            }
        };
    }

    private class Data_Receiver extends BroadcastReceiver {
        boolean first = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("data_analysed".equals(intent.getAction())) {
                //  handler_read.getLooper().quit();//只要收到广播就不再读取了
                if (intent.getExtras() != null) {
                          value_str=intent.getStringExtra("value_mid");
                          Log.d("距离值是：",value_str);
                          handler.sendEmptyMessage(0);
                }
            }
        }
    }
}
