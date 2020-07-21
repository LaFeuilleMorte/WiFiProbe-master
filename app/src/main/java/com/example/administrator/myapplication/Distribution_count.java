package com.example.administrator.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Distribution_count extends Activity {
    Data_Receiver dataReceiver;
    Handler handler;
    Handler timer_handler;
   DataSource dataSource=DataSource.getInsDs();
    String str;
    long current_tm;
    List<BarEntry> entries_range = new ArrayList<>();
    Map<Integer,Integer > map=new HashMap<>();
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.distribute_layout);
        TextView tv1=(TextView)findViewById(R.id.max_num);
        BarChart mBarChart=(BarChart)findViewById(R.id.bar_distribute) ;
        mBarChart.setDrawBorders(true);
        mBarChart.getAxisRight().setEnabled(false);
        mBarChart.getAxisLeft().setAxisMaximum(100);
        mBarChart.getXAxis().setXOffset(-90);
        for (int i = 0; i < 10; i++) {
            entries_range.add(new BarEntry(i*6-90, 0));
        }
        BarDataSet barset_range = new BarDataSet(entries_range, "统计结果(个)");
        barset_range.setColor(Color.GREEN);
        BarData data = new BarData(barset_range);
        data.setBarWidth(0.8f);
        data.setValueTextSize(12f);
        mBarChart.setData(data);

        for(int i=0;i<10;i++){
            map.put(i,0);
        }
        dataReceiver = new Data_Receiver();
        IntentFilter intentFilter_data = new IntentFilter("to_distribution");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);

        //设置数据
      handler=new Handler(){
          public void handleMessage(Message msg){

              int num=Integer.parseInt(str);Log.d("接收到",str);
              for(int i=0;i<10;i++){

                  if(-90+i*6<num&&num<-84+i*6){
                          map.put(i,map.get(i)+1);
                  }
              }
              Log.d("统计结果是",map.toString());
              entries_range.clear();
              for(int i=0;i<10;i++){
                  entries_range.add(new BarEntry(-90+i*6,map.get(i)));
              }
              float max=0;

              for(Integer i:map.keySet()){
                  if(max<map.get(i))
                  max=map.get(i);
              }
              mBarChart.getAxisLeft().setAxisMaximum(max*6/5);
              mBarChart.getAxisLeft().setAxisMinimum(0);
              mBarChart.getXAxis().setXOffset(-90);
              mBarChart.getBarData().setBarWidth(0.8f);
              mBarChart.getBarData().setValueTextSize(12f);
              mBarChart.notifyDataSetChanged();
              mBarChart.invalidate();
          }
      };

      Handler timer=new Handler();
      Runnable timer_thread=new Runnable() {
          @Override
          public void run() {
              int index=0;
              for(Integer i:map.keySet()){
                  if(map.get(i)>map.get(index)){
                      index=i;
                  }
              }

             // dataSource.set(cal(index*6-90));Log.d("value为",dataSource.getValue()+"");
              timer.postDelayed(this,60*1000);
          }
      };
      timer.postDelayed(timer_thread,0);
      timer_handler=new Handler(){
          LinkedList<Integer>  lst=new LinkedList<>();
          int count_tm=0;
          public void handleMessage(Message msg){
                //重置之后，起始时间也变成起始时间
                 if(count_tm==0) {
                     current_tm=System.currentTimeMillis();
                     count_tm++;
                 }
                 if(current_tm<System.currentTimeMillis()&&System.currentTimeMillis()<current_tm+60*1000){
                     count_tm++;
                 }

                 if(current_tm+60*1000<System.currentTimeMillis()){
                     if(lst.size()<=15){
                     lst.add(count_tm); }else {
                         lst.removeFirst(); lst.add(count_tm);
                     }
                    count_tm=0;
                 }//超过一分钟,拼接到链表，count_tm重置为0
              tv1.setText(lst.toString().replace('[',' ').replace(']',' '));
              Log.d("输出结果是",lst.toString());
          }
      };

    }
    private class Data_Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if("to_distribution".equals(intent.getAction())){
                //  handler_read.getLooper().quit();//只要收到广播就不再读取了
                if(intent.getExtras()!=null){
                 str=intent.getStringExtra("result");
                 handler.sendEmptyMessage(0);
                 timer_handler.sendEmptyMessage(0);
                }
            }
        }
    }
    public double cal(int input){
        double result=0f;
        Log.d("input的值是",input+" ");
        result=3*Math.pow(10,-5)*Math.pow(input,4)+0.0062*Math.pow(input,3)+0.5161*input*input+19.343*input+273.18;
        return result;
    }
}
