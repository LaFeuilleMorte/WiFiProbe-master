package com.example.administrator.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.utils.pachong;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router_Track extends Activity {
    private ArrayList list=new ArrayList();
    private Map<String,Object> map=new HashMap<String, Object>();
    private ArrayList<Map<String,Object>> myRouter;
    private Data_Receiver dataReceiver;
    private Handler handler;
    private MyAdapter myAdapter;
    private Button vip_item;
    private RatingBar star_track;
    private Kernel_Receiver kernelReceiver;
    private Handler handler_of_result;
    private String result="";
    private int i=0;
    private Handler blt_handler=new Handler();
    private Runnable blt_runable;
    private int count_result=0;
    public static String web_res="";
    String[] router_info={"路由Mac：","路由名称：","信号强度：","距离：","出现次数：","来源信息：","是否匿名："};
    String[] router_key={"mac","router","rssi","range","show","source","hidden"};//子表中显示路由信息列表
    private float[] range=new float[20];
    private float[] ref=new float[20];
    private float[] range_MAC=new float[20];
    private int count=0;
    private String last_show="";
    private String last_range="";
    private float aver=0;
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.router_track_layout);
        SysApplication.getInstance().addActivity(this);
        myAdapter=new MyAdapter(this);
        ListView listView=(ListView)findViewById(R.id.list_router_track);
        listView.setAdapter(myAdapter);
        vip_item=(Button)findViewById(R.id.vip_item);
        star_track=(RatingBar)findViewById(R.id.star_track_router);
        dataReceiver = new Data_Receiver();
        IntentFilter intentFilter_data = new IntentFilter("data_analysed");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);
        kernelReceiver=new Kernel_Receiver();
        IntentFilter intentFilter_kernel = new IntentFilter("kernel_result");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(kernelReceiver, intentFilter_kernel);
        Button my_button=(Button)findViewById(R.id.dynamic_track);

       Thread delay=new Thread(){
           public void run(){
              try{ Thread.currentThread().sleep(4000);

                      String my_mac=Serial_Port_Display.trackMac;
                      if(!my_mac.equals("quit_track")&&my_mac.contains(":"))
                      { web_res=pachong.getLat_Lon(my_mac);Log.d("dada","ddqdq"); }
                      Log.d("dada",web_res);

              }catch (Exception e){}
           }
       };delay.start();

        BarChart mBarChart = (BarChart) findViewById(R.id.line_router);
        BarChart Bar_Bottom=(BarChart)findViewById(R.id.bar_bottom);
            //显示边界
            mBarChart.setDrawBorders(true);
            Bar_Bottom.setDrawBorders(true);
            mBarChart.getAxisRight().setEnabled(false);
            Bar_Bottom.getAxisRight().setEnabled(false);
            mBarChart.getAxisLeft().setAxisMaximum(100);
            Bar_Bottom.getAxisLeft().setAxisMaximum(100);
       //设置数据
        List<BarEntry> entries_range = new ArrayList<>();
        List<BarEntry> entries_ref = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            entries_range.add(new BarEntry(i, 0));
           entries_ref.add(new BarEntry(i, 0)) ;
        }
       BarDataSet barset_range = new BarDataSet(entries_range, "参考距离(m)");
        barset_range.setColor(Color.GREEN);
        BarDataSet barset_ref=new BarDataSet(entries_ref,"校正距离(m)");
        barset_ref.setColor(Color.RED);

        BarData data = new BarData(barset_range);
        BarData data_ref=new BarData(barset_ref);

        data.setBarWidth(0.2f);
        data_ref.setBarWidth(0.2f);

        data.setValueTextSize(12f);
        data_ref.setValueTextSize(12f);

        mBarChart.setData(data);
        Bar_Bottom.setData(data_ref);
        my_button.setOnClickListener((v)->{

            Intent intent=new Intent(Router_Track.this,Dynamic_Dis.class);
            startActivity(intent);
        });

        handler=new Handler(){
            public void handleMessage(Message msg){
                if(msg.what==0){
                    myAdapter.notifyDataSetChanged();
                }

                if(msg.what==1){

                    for(i=0;i<myRouter.size();i++){
                        if(myRouter.get(i).get("mac").toString().equals(map.get("mac").toString())){
                            break;
                        }
                    }if(i==myRouter.size()){
                        return;// 如果在这里没有找到目标数据包直接退出去
                    }
                    try {
                    for(Map<String,Object> mapRouter:myRouter){

                          if (map.get("mac").toString().equals(mapRouter.get("mac").toString())) {
                           /*
                            if(mapRouter.get("show").toString().equals(last_show)){
                               return; //说明这一包数据是一样的，没有新数据，直接退出去
                            }*/
                              list.clear();
                              for (int i = 0; i < router_key.length; i++) {
                                  list.add(mapRouter.get(router_key[i]));
                              }
                              //last_show= mapRouter.get("show").toString();
                          }
                      }
                    }catch (Exception e){}
                    if(count<15){
                    range[count]=Float.parseFloat(list.get(3).toString());
                    count++;  }

                    if(count>=15){
                        for(int i=0;i<14;i++){
                            range[i]=range[i+1];
                        }
                        range[14]=Float.parseFloat(list.get(3).toString());//最后一个值直接付给最后一位数
                    }
                    entries_range.clear();
                    ArrayList<Float> al_1=new ArrayList();
                    float rs=0;
                    for(int n=0;n<15;n++){
                        if(Math.floor(range[n])!=0)
                        al_1.add(range[n]);
                    }
                    Collections.sort(al_1);
                    if(al_1.size()%2!=0)
                    {
                        rs=al_1.get(al_1.size()/2);}
                    else  if(al_1.size()%2==0){
                      try{  rs=(al_1.get(al_1.size()/2)+al_1.get(al_1.size()/2-1))/2;}catch (Exception e){}
                    }
                    Log.d("排序结果",al_1.toString()+" \n"+rs); al_1.clear();
                    TextView textView=(TextView)findViewById(R.id.mid_num);
                  //textView.setText(rs+"米");
                    String packs=Serial_Port_Display.builder.toString();
                    textView.setText(packs);
                    float max=range[0];
                    for (int i = 0; i < 15; i++) {
                    entries_range.add(new BarEntry(i,  range[i]));
                          if(range[i]>max){ max=range[i]; }
                    }
                    mBarChart.getAxisLeft().setAxisMaximum(max*6/5);
                    mBarChart.getAxisLeft().setAxisMinimum(0);
                    mBarChart.getBarData().setBarWidth(0.2f);
                    mBarChart.getBarData().setValueTextSize(12f);
                    mBarChart.notifyDataSetChanged();
                    mBarChart.invalidate();

                   myAdapter.notifyDataSetChanged();
                }
            }
        };
     /*
        Button button_dyn=(Button)findViewById(R.id.dynamic_dis);
        button_dyn.setOnClickListener((v)->{
            Intent intent=new Intent(Router_Track.this,Dynamic_Dis.class);
            startActivity(intent);
        });*/

        handler_of_result=new Handler(){
            public void handleMessage(Message msg){

                if(count_result<15){
                    ref[count_result]=Float.parseFloat(String.valueOf(result));
                    count_result++;  }
                if(count_result>=15){

                    for(int i=0;i<14;i++){
                        ref[i]=ref[i+1];
                    }
                    ref[14]=Float.parseFloat(result);
                    //最后一个值直接付给最后一位数
                }
                double max_ref=ref[0];
                entries_ref.clear();//先清理掉
                for(int i=0;i<15;i++){
                    if(ref[i]>max_ref){ max_ref=ref[i]; }
                    entries_ref.add(new BarEntry(i,(float)ref[i]));
                }
                Bar_Bottom.getAxisLeft().setAxisMaximum((float) max_ref*6/5);
                Bar_Bottom.getAxisLeft().setAxisMinimum(0);
                Bar_Bottom.getBarData().setBarWidth(0.2f);
                Bar_Bottom.getBarData().setValueTextSize(12f);
                Bar_Bottom.notifyDataSetChanged();
                Bar_Bottom.invalidate();
            }
        };

/**
 * 以下处理从其他界面跳转过来的玩意
 * */
        Intent intent=getIntent();
        if(intent!=null){
          //从列表界面跳转而来
            if((Map<String, Object>) intent.getSerializableExtra("track")!=null){
            map=(Map<String, Object>) intent.getSerializableExtra("track");

            }

         //从收藏界面跳转而来
            if(intent.getExtras().getString("router_mac_target")!=null){
                String str=intent.getExtras().getString("router_mac_target");
                map.put("mac",str.substring(0,17));
                map.put("router",str.substring(18,str.length()));
                Log.d("接收到",map.toString());
                for(int i=2;i<router_key.length;i++){
                    map.put(router_key[i],"正在加载...");
                }
            }

           //从通知界面跳转而来
            if(intent.getSerializableExtra("from_note_to_router_track")!=null){
                map = (Map<String, Object>) intent.getSerializableExtra("from_note_to_router_track"); }

            Intent my_intent=new Intent("track_mode");
            my_intent.putExtra("mac_to_track",map.get("mac").toString());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(my_intent);//通知开启追踪单个Mac模式

            SharedPreferences read = getSharedPreferences("mac_labeled", MODE_PRIVATE);
            if(read.contains(map.get("mac").toString())){
                vip_item.setText("取消收藏");
                star_track.setRating(1);
            }else {
                vip_item.setText("收藏");
                star_track.setRating(0);
            }
            vip_item.setOnClickListener((v)->{
                if(vip_item.getText().toString().equals("收藏")){
                    String labeled=map.get("mac").toString();
                    String value=map.get("router").toString();
                    SharedPreferences.Editor editor=getSharedPreferences("mac_labeled",MODE_PRIVATE).edit();
                    editor.putString(labeled,value);  Log.d("增加了","是的");
                    editor.commit();
                    vip_item.setText("取消收藏");
                    star_track.setRating(1);
                }else {
                    String labeled=map.get("mac").toString();
                    SharedPreferences.Editor editor=getSharedPreferences("mac_labeled",MODE_PRIVATE).edit();
                    editor.remove(labeled);Log.d("去掉了","是的");
                    editor.commit();
                    vip_item.setText("收藏");
                    star_track.setRating(0);
                }
            });

            if(map!=null&&map.size()!=0){
                for(int i=0;i<router_key.length;i++){
                   try{ list.add(map.get(router_key[i]));}catch (Exception e){}

                }  handler.sendEmptyMessage(0);
            }
        }
    }
    public void onNewIntent(Intent intent){

        intent=getIntent();
        if(intent!=null){
            //从列表界面跳转而来
            if((Map<String, Object>) intent.getSerializableExtra("track")!=null){
                map=(Map<String, Object>) intent.getSerializableExtra("track");
            }

            //从收藏界面跳转而来
            if(intent.getExtras().getString("router_mac_target")!=null){
                String str=intent.getExtras().getString("router_mac_target");
                map.put("mac",str.substring(0,17));
                map.put("router",str.substring(18,str.length()));
                Log.d("接收到",map.toString());
                for(int i=2;i<router_key.length;i++){
                    map.put(router_key[i],"正在加载...");
                }
            }

            //从通知界面跳转而来
            if(intent.getSerializableExtra("from_note_to_router_track")!=null){
                map = (Map<String, Object>) intent.getSerializableExtra("from_note_to_router_track"); }

            Intent my_intent=new Intent("track_mode");
            my_intent.putExtra("mac_to_track",map.get("mac").toString());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(my_intent);//通知开启追踪单个Mac模式

            SharedPreferences read = getSharedPreferences("mac_labeled", MODE_PRIVATE);
            if(read.contains(map.get("mac").toString())){
                vip_item.setText("取消收藏");
                star_track.setRating(1);
            }else {
                vip_item.setText("收藏");
                star_track.setRating(0);
            }
            vip_item.setOnClickListener((v)->{
                if(vip_item.getText().toString().equals("收藏")){
                    String labeled=map.get("mac").toString();
                    String value=map.get("router").toString();
                    SharedPreferences.Editor editor=getSharedPreferences("mac_labeled",MODE_PRIVATE).edit();
                    editor.putString(labeled,value);  Log.d("增加了","是的");
                    editor.commit();
                    vip_item.setText("取消收藏");
                    star_track.setRating(1);
                }else {
                    String labeled=map.get("mac").toString();
                    SharedPreferences.Editor editor=getSharedPreferences("mac_labeled",MODE_PRIVATE).edit();
                    editor.remove(labeled);Log.d("去掉了","是的");
                    editor.commit();
                    vip_item.setText("收藏");
                    star_track.setRating(0);
                }
            });

            if(map!=null&&map.size()!=0){
                for(int i=0;i<router_key.length;i++){
                    try{ list.add(map.get(router_key[i]));}catch (Exception e){}

                }  handler.sendEmptyMessage(0);
            }
        }
    }

    class MyAdapter extends BaseAdapter {
        Activity activity;

        public MyAdapter(Activity a) {
            activity = a;
        }

        // 设置每一页的长度，默认的是View_Count的值。
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            // return data.length;
            return  list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        //重点是getView方法
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            // return addTestView(position);
            String str1="";
            String str2="";
            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.router_track_item,null);
            TextView ylfn_did_view = (TextView)convertView.findViewById(R.id.tv_tags);
            TextView ylfn_second=(TextView)convertView.findViewById(R.id.tv_contents);
            str1=router_info[position]+" ";
            if(list.get(position)!=null) {str2=list.get(position).toString();}
            ylfn_did_view.setText(str1);
            ylfn_second.setText(str2);Log.d("改变","已改变");
            return convertView;
        }
    }
    private class Data_Receiver extends BroadcastReceiver {
        boolean first=true;
        @Override
        public void onReceive(Context context, Intent intent){
            if("data_analysed".equals(intent.getAction())){
                //  handler_read.getLooper().quit();//只要收到广播就不再读取了
                if(intent.getExtras()!=null){
                    try{
                      myRouter =(ArrayList<Map<String,Object>>)intent.getSerializableExtra("extra_data_router");
                        if(myRouter!=null&&myRouter.size()!=0) {
                            handler.sendEmptyMessage(1);
                        }
                    }catch (ClassCastException e){
                        Log.d("错误:","类型转化错误");}
                }
            }
        }
    }

    public void onStop(){
       super.onStop();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            AlertDialog.Builder ab=new AlertDialog.Builder(Router_Track.this);
            ab.setTitle("确定退出监听模式？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent my_intent=new Intent("track_mode");
                    my_intent.putExtra("mac_to_track","quit_track");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(my_intent);
                    Intent intent=new Intent(Router_Track.this,Any_kind.class);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(intent);
                    //这里直接跳转到全部路由界面，不往别的地方跳
                    dialog.dismiss();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class Kernel_Receiver extends BroadcastReceiver{
        public void onReceive(Context context,Intent intent){
            if("kernel_result".equals(intent.getAction())) {
                if (intent.getExtras() != null) {
                    if(intent.getExtras().getString("kernel_of_user")!=null){
                        result=intent.getExtras().getString("kernel_of_user");
                        if (!result .equals( "")) {
                            Log.d("收到result",result);   handler_of_result.sendEmptyMessage(0);
                        }
                    }
                }
            }
        }
    }
}
