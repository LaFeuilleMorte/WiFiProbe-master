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
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.example.administrator.myapplication.utils.pachong;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Tracking_Mac extends Activity {
    private String[] user_key={"tmc","mac","router","rssi","range","latest","duration","show","source"};
    private String[] userInfo={"设备Mac:","连接路由Mac:","连接路由名称:","信号强度:","参考距离:",
            "探测时间:","在线时间:","出现次数:","制造商:"};
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
    private ArrayList listItem=new ArrayList();
    private ArrayList<Map<String,Object>> handle_range;
    private ListView list_ylfn;
    private MyAdapter myAdapter;
    private int i=0;
    private Handler handler;
    private Handler handler_closest;
    private Handler handler_of_result;
    private Data_Receiver dataReceiver;
    private Kernel_Receiver kernelReceiver;
    private Location_Receiver location_receive;
    private  ArrayList<Map<String,Object> > myUser=new ArrayList<Map<String, Object>>();
    private String loc="";
    private String result="";
    private Handler blt_handler=new Handler();
    private  Runnable blt_runable;
    private int count_result=0;
    private Map<String,Object> map=new HashMap<String, Object>();
    private Button vip_item;
    private RatingBar star_track;
    private String last_show="";
    private String last_rssi="";
    private ProgressBar progressBar;
    private double[] range=new double[20];
    private double[] ref=new double[20];
    private int count=0;
    public  static  String web_res="";
    String lat_lon="";
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.tracking_mac);
        SysApplication.getInstance().addActivity(this);
         vip_item=(Button)findViewById(R.id.vip_item_user);
         star_track=(RatingBar) findViewById(R.id.star_track_user);
        BarChart mBarChart = (BarChart) findViewById(R.id.barchart_above);
        BarChart Bar_Bottom=(BarChart)findViewById(R.id.barchart_bottom);

      /*
      * 创建一个子线程检测蓝牙连接状况，如果断开直接重新连接
      * */
    /*  blt_runable=new Runnable() {
          @Override
          public void run() {
              if(SysApplication.globalBlueSocket!=null&&SysApplication.globalBlueSocket.isConnected()){
                  return;//什么也不做
              }else {  Intent intent=new Intent(Tracking_Mac.this,BTClient.class);
                      intent.putExtra("socket","disconnected");
                      startActivity(intent);
                  Toast.makeText(getApplicationContext(),"蓝牙已断开，请重新连接",Toast.LENGTH_SHORT).show();
              }
            Log.d("正在扫描蓝牙状态","yes");
             blt_handler.postDelayed(blt_runable,20*1000);
          }
      };  blt_handler.postDelayed(blt_runable,5*1000);*/


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

         dataReceiver = new Data_Receiver();
        location_receive = new Location_Receiver();
        kernelReceiver=new Kernel_Receiver();
        IntentFilter intentFilter_data = new IntentFilter("data_analysed");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);
        IntentFilter intentFilter_loc = new IntentFilter("location_serve");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(location_receive, intentFilter_loc);
        IntentFilter intentFilter_kernel = new IntentFilter("kernel_result");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(kernelReceiver, intentFilter_kernel);
        TextView tv_lat=(TextView)findViewById(R.id.long_lat) ;
         list_ylfn = (ListView) findViewById(R.id.list_track_user);
        myAdapter = new MyAdapter(Tracking_Mac.this);
        list_ylfn.setAdapter(myAdapter);

     /**
        Thread thread_pa=new Thread(){
            public void run(){
                 lat_lon=pachong.getLat_Lon();
                handler.sendEmptyMessage(12);
            }
        }; thread_pa.start();*/


        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what==12){
                    tv_lat.setText(lat_lon);
                    return;
                }

                if(msg.what==0){
                    myAdapter.notifyDataSetChanged();Log.d("更新","异常");
                }

                if (msg.what == 1) {

                  try{
                      Log.d("追踪信息上",myUser.toString());
                    for(i=0;i<myUser.size();i++){
                        if(myUser.get(i).get("tmc").toString().equals(map.get("tmc").toString())){
                            break;
                        }
                    }if(i==myUser.size()){
                        return; // 如果在这里没有找到目标数据包直接退出去
                    }   }catch (Exception e){}

                    /*这里会出现交叉修改的错误*/
                    try{
                    for (Map<String, Object> mapUser: myUser) {
                        if (map.get("tmc").toString().equals(mapUser.get("tmc"))) {
                          /*
                            if(mapUser.get("show").toString().equals(last_show)){
                                return; //下一包数据
                            }*/
                         //  Log.d("map的值是us",mapUser.get("show").toString());
                            listItem.clear();
                            for(int i=0;i<user_key.length;i++){
                              if(i==7){
                                  try{
                                      long duration= sdf.parse(mapUser.get("latest").toString()).getTime()
                                              -sdf.parse(mapUser.get("start").toString()).getTime();
                                      long dure=duration/1000;
                                      long sec=dure%60;
                                      long min=(dure-sec)/60%60;
                                      long hour=(dure-sec)/60/60;
                                      mapUser.put("duration",hour+"时"+min+"分"+sec+"秒");
                                  }catch (ParseException e){e.printStackTrace();
                                  }catch (NullPointerException e){e.printStackTrace();}
                              }
                                listItem.add(mapUser.get(user_key[i])); }
                         //   last_show=mapUser.get("show").toString();
                            }
                        }
                    }catch (Exception e){}
                    entries_range.clear();
                    if(count<15){
                        range[count]=Float.parseFloat(listItem.get(4).toString());
                        count++;  }
                    if(count>=15){

                        for(int i=0;i<14;i++){
                            range[i]=range[i+1];
                        }
                        range[14]=Double.parseDouble(listItem.get(4).toString());
                      //最后一个值直接付给最后一位数
                    }

                    double max=range[0];//double max_ref=ref[0];
                    for (int i = 0; i < 15; i++) {
                        entries_range.add(new BarEntry(i,  (float) range[i]));

                        if(range[i]>max){ max=range[i]; }

                    }

                    mBarChart.getAxisLeft().setAxisMaximum((float)max*6/5);
                    mBarChart.getAxisLeft().setAxisMinimum(0);
                    mBarChart.getBarData().setBarWidth(0.2f);
                    mBarChart.getBarData().setValueTextSize(12f);
                    mBarChart.notifyDataSetChanged();
                    mBarChart.invalidate();
                    myAdapter.notifyDataSetChanged();

                }
            }
        };
     handler_of_result=new Handler(){
         public void handleMessage(Message msg){

             if(count_result<15){
                 ref[count_result]=Double.parseDouble(String.valueOf(result));
                 count_result++;    }

                 if(count_result>=15){

                 for(int i=0;i<14;i++){
                     ref[i]=ref[i+1];
                 }
                 ref[14]=Double.parseDouble(result);
                 //最后一个值直接付给最后一位数
             }
             double max_ref=ref[0];
             entries_ref.clear();//先清理掉
             double aver1=0;
             double aver2=0;
            for(int i=0;i<15;i++){
             if(ref[i]>max_ref){ max_ref=ref[i]; }
                if(i<8){
                   aver1+=ref[i];
                 if(i==7){
                    aver1=aver1/8;
                 }
             }
             if(i>=8){
                 aver2+=ref[i];
                 if(i==14){
                     aver2=aver2/8;
                 }
             }
                entries_ref.add(new BarEntry(i,(float)ref[i]));
            }
            Log.d("两者相比",aver1-aver2+"");
             Bar_Bottom.getAxisLeft().setAxisMaximum((float) max_ref*6/5);
             Bar_Bottom.getAxisLeft().setAxisMinimum(0);
             Bar_Bottom.getBarData().setBarWidth(0.2f);
             Bar_Bottom.getBarData().setValueTextSize(12f);
             Bar_Bottom.notifyDataSetChanged();
             Bar_Bottom.invalidate();
         }
     };

        Intent intent = getIntent();
        if (intent != null) {

           if((Map<String, Object>) intent.getSerializableExtra("track")!=null){
            map = (Map<String, Object>) intent.getSerializableExtra("track");}

          /*
          * 这下面一段是从收藏界面跳转得来的
          * */
            if(intent.getExtras().getString("user_mac_target")!=null){

                String str=intent.getExtras().getString("user_mac_target");
                map.put("tmc",str.substring(0,17));
                map.put("mac",str.substring(18,str.length()));
                Log.d("接收到",map.toString());
                for(int i=2;i<user_key.length;i++){
                    map.put(user_key[i],"正在加载...");
                }
            }
            /*
            * 这下面一段是从通知栏跳转而来
            * */
                if(intent.getSerializableExtra("from_note_to_tracking_mac")!=null){
                    map = (Map<String, Object>) intent.getSerializableExtra("from_note_to_tracking_mac"); }

            SharedPreferences read = getSharedPreferences("user_mac_labeled", MODE_PRIVATE);
            if (read.contains(map.get("tmc").toString())) {
                vip_item.setText("取消收藏");
                star_track.setRating(1);
            } else {
                vip_item.setText("收藏");
                star_track.setRating(0);
            }
            vip_item.setOnClickListener((v) -> {
                if (vip_item.getText().toString().equals("收藏")) {
                    String labeled = map.get("tmc").toString();
                    String value = map.get("mac").toString();
                    SharedPreferences.Editor editor = getSharedPreferences("user_mac_labeled", MODE_PRIVATE).edit();
                    editor.putString(labeled, value);

                    editor.commit();
                    vip_item.setText("取消收藏");
                    star_track.setRating(1);
                } else {
                    String labeled = map.get("tmc").toString();
                    SharedPreferences.Editor editor = getSharedPreferences("user_mac_labeled", MODE_PRIVATE).edit();
                    editor.remove(labeled);

                    editor.commit();
                    vip_item.setText("收藏");
                    star_track.setRating(0);
                }
            });

            if(map!=null&&map.size()!=0){

                try{
                    long duration= sdf.parse(map.get("latest").toString()).getTime()-sdf.parse(map.get("start").toString()).getTime();
                    long dure=duration/1000;
                    long sec=dure%60;
                    long min=(dure-sec)/60%60;
                    long hour=(dure-sec)/60/60;
                    map.put("duration",hour+"时"+min+"分"+sec+"秒");
                }catch (ParseException e){e.printStackTrace();
                }catch (NullPointerException e){e.printStackTrace();}
            }//把时间差放进去

                for(int i=0;i<user_key.length;i++){
              try{
               listItem.add(map.get(user_key[i]).toString()); }catch (Exception e){}//这里可能会有空指针
                handler.sendEmptyMessage(0);
            }
        }

        Intent my_intent=new Intent("track_mode");
        my_intent.putExtra("mac_to_track",map.get("tmc").toString());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(my_intent);//通知开启追踪单个Mac模式
    }

    public  void onNewIntent(Intent intent){
        intent = getIntent();
        if (intent != null) {
            try{

            }catch (Exception e){}
            if((Map<String, Object>) intent.getSerializableExtra("track")!=null){
                map = (Map<String, Object>) intent.getSerializableExtra("track");

            }

            /*
             * 这下面一段是从收藏界面跳转得来的
             * */
            if(intent.getExtras().getString("user_mac_target")!=null){

                String str=intent.getExtras().getString("user_mac_target");
                map.put("tmc",str.substring(0,17));
                map.put("mac",str.substring(18,str.length()));
                Log.d("接收到",map.toString());
                for(int i=2;i<user_key.length;i++){
                    map.put(user_key[i],"正在加载...");
                }
            }
            /*
             * 这下面一段是从通知栏跳转而来
             * */
            if(intent.getSerializableExtra("from_note_to_tracking_mac")!=null){
                map = (Map<String, Object>) intent.getSerializableExtra("from_note_to_tracking_mac"); }

            SharedPreferences read = getSharedPreferences("user_mac_labeled", MODE_PRIVATE);
            if (read.contains(map.get("tmc").toString())) {
                vip_item.setText("取消收藏");
                star_track.setRating(1);
            } else {
                vip_item.setText("收藏");
                star_track.setRating(0);
            }
            vip_item.setOnClickListener((v) -> {
                if (vip_item.getText().toString().equals("收藏")) {
                    String labeled = map.get("tmc").toString();
                    String value = map.get("mac").toString();
                    SharedPreferences.Editor editor = getSharedPreferences("user_mac_labeled", MODE_PRIVATE).edit();
                    editor.putString(labeled, value);

                    editor.commit();
                    vip_item.setText("取消收藏");
                    star_track.setRating(1);
                } else {
                    String labeled = map.get("tmc").toString();
                    SharedPreferences.Editor editor = getSharedPreferences("user_mac_labeled", MODE_PRIVATE).edit();
                    editor.remove(labeled);

                    editor.commit();
                    vip_item.setText("收藏");
                    star_track.setRating(0);
                }
            });

            if(map!=null&&map.size()!=0){

                try{
                    long duration= sdf.parse(map.get("latest").toString()).getTime()-sdf.parse(map.get("start").toString()).getTime();
                    long dure=duration/1000;
                    long sec=dure%60;
                    long min=(dure-sec)/60%60;
                    long hour=(dure-sec)/60/60;
                    map.put("duration",hour+"时"+min+"分"+sec+"秒");
                }catch (ParseException e){e.printStackTrace();
                }catch (NullPointerException e){e.printStackTrace();}
            }//把时间差放进去

            for(int i=0;i<user_key.length;i++){
                try{
                    listItem.add(map.get(user_key[i]).toString()); }catch (Exception e){}//这里可能会有空指针
                    handler.sendEmptyMessage(0);
            }
        }

        Intent my_intent=new Intent("track_mode");
        my_intent.putExtra("mac_to_track",map.get("tmc").toString());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(my_intent);//通知开启追踪单个Mac模式

    }

    class MyAdapter extends BaseAdapter {
        Activity activity;

        public MyAdapter(Activity a) {
            activity = a;
        }

        // 设置每一页的长度，默认的是View_Count的值。
        @Override
        public int getCount() {
                return listItem.size();
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
            str1=userInfo[position]+" ";
       if(listItem.get(position)!=null){
        str2=listItem.get(position).toString(); }
            ylfn_did_view.setText(str1);
            ylfn_second.setText(str2);
            return convertView;
        }
    }
    private class Data_Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if("data_analysed".equals(intent.getAction())){
                if(intent.getExtras()!=null){
                    try{
                        myUser=(ArrayList<Map<String,Object>>)intent.getSerializableExtra("extra_data_user");
                        if(myUser!=null&&myUser.size()!=0) {
                          handler.sendEmptyMessage(1);     }
                    }catch (ClassCastException e){
                        Log.d("错误:","类型转化错误");}
                }
            }


        }
    }

    private  class Location_Receiver extends  BroadcastReceiver{
        public void onReceive(Context context,Intent intent){
            if("location_serve".equals(intent.getAction())) {
                if (intent.getExtras() != null) {
                    loc=intent.getExtras().getString("location_result");
                    if (!loc .equals( "")) {
                        handler.sendEmptyMessage(2);
                    }
                }
            }
        }
    }
    public void onStop(){
        super.onStop();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            AlertDialog.Builder ab=new AlertDialog.Builder(Tracking_Mac.this);
            ab.setTitle("确定退出监听模式？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent my_intent=new Intent("track_mode");
                    my_intent.putExtra("mac_to_track","quit_track");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(my_intent);
                      Intent intent=new Intent(Tracking_Mac.this,User_list.class);
                      intent.setAction(Intent.ACTION_MAIN);
                      intent.addCategory(Intent.CATEGORY_LAUNCHER);
                      startActivity(intent);
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
