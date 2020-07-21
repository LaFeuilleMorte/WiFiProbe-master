package com.example.administrator.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.utils.pachong;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class MyIntentService extends Service {

    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    private StringBuilder range_all=new StringBuilder();
    private StringBuilder range_result=new StringBuilder();
    private InputStream is;    //输入流，用来接收蓝牙数据
    private TextView text0;    //提示栏解句柄
    private EditText edit0;    //发送数据输入句柄
    private TextView dis;       //接收数据显示句柄
    private ScrollView sv;      //翻页句柄
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    private String useful;
    public String web_res="";

    private String my_track_target="";
    private String temp_init_checker="";
    private ArrayList list_router=new ArrayList();
    Thread collector_thread;
    private ArrayList list_user=new ArrayList();
    public String filename=""; //用来保存存储的文件名
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;
    String my_mac="";
    private Option_Detail optionDetail;
    boolean bRun = true;
    int count_show=0;
    boolean first_init_kernel=true;
    private Vibrator vibrator;
    boolean bThread = false;
    boolean analyseflag;
    boolean readflag;
    double average=0;
    private int num=0;
    private LinkedList<Double> range_MAC=new LinkedList<>();
    private  LinkedList<String> json_queue=new LinkedList<>();
    private double[] array_mac=new double[35];
    private double key=0;
    private static int write_read=1;//先进行写操作，然后再进行读操作，避免两个线程同时操作一个文件
    public static int HCIstate = 0;
    public static int cnt = 0;
    private int index=0;
    private double Pre_Range=0;
    public ArrayList<Map<String,Object>> myRouter = new ArrayList<>();
    public ArrayList<Map<String,Object>> myUser = new ArrayList<>();
    public ArrayList<Map<String,Object>> myUnconnected = new ArrayList<>();
    public  Data_Receiver dataReceiver;
    public  String str="";
    double last_value=0;
    int ccnt=0;
    private MyDBAssistant myDBAssistant;
    private DataSource dataSource;
    private ArrayList<Map<String,Object>> my_track_router=new ArrayList<>();
    private ArrayList<Map<String,Object>> my_track_user=new ArrayList<>();
    private ArrayList<Map<String,Object>> connected_user_track=new ArrayList<>();
    private ArrayList<Map<String,Object>> my_collect_router=new ArrayList<>();
    private ArrayList<Map<String,Object>> my_collect_user=new ArrayList<>();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备
    private Handler handler;
    private Track_Mode_Activate trackModeActivate;
    private  MediaPlayer mediaPlayer;
    public  Serial_Port_Display serial_port;
   //通知类

    NotificationManager mNotificationManager;
    public MyIntentService()
    {

    }

        @Override
        public IBinder onBind(Intent intent) {
            return new Binder();
        }


        @Override
        public void onCreate() {
            Log.d("Service:", "成功创建");
            SharedPreferences read=getSharedPreferences("account_reg",MODE_PRIVATE);
            try{ str=read.getAll().get("location").toString(); }catch (Exception e){}
            super.onCreate();
            analyseflag = true;
            readflag = true;
            myDBAssistant = new MyDBAssistant(MyIntentService.this);
            dataReceiver = new Data_Receiver();
            IntentFilter intentFilter_data = new IntentFilter("location_serve");
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);
            trackModeActivate = new Track_Mode_Activate();
            IntentFilter track_mode = new IntentFilter("track_mode");
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(trackModeActivate, track_mode);
            optionDetail=Option_Detail.getSingleInstance();
           //初始化音乐播放器
            vibrator= (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
            try{mediaPlayer.prepare();mediaPlayer.setLooping(true);}catch (Exception e){}
            /*初始化通知
            * */
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            new Serial_Port_Display();
            db_thread.start();
        }
                        //读之前先加载收藏项，每二十次加载一次收藏项
                       /*
                        if (my_count % 500 == 0) {
                            SharedPreferences read_ = getSharedPreferences("mac_labeled", MODE_PRIVATE);
                            Map<String, ?> map_all_ = read_.getAll();
                            Set<String> set__ = map_all_.keySet();
                            list_user.clear();
                            list_router.clear();
                            //用之前一定要清空
                            for (String key : set__) {
                                String temp = key;
                                Log.d("热点key是", map_all_.get(key).toString());
                                list_router.add(temp);
                            }
                            SharedPreferences read_2 = getSharedPreferences("user_mac_labeled", MODE_PRIVATE);
                            Map<String, ?> map_all_2 = read_2.getAll();
                            Set<String> set__2 = map_all_2.keySet();
                            for (String key : set__2) {
                                String temp = key;
                                Log.d("用户key是", map_all_2.get(key).toString());
                                list_user.add(temp);
                            }
                        }*/
                       //先读第一次再每二十次增加
                        //下面的代码易出现异常，所以捕捉异常


                    /*
                        for (Map<String, Object> map : info.data) {

                            //发送追踪结果
                            if (map.get("mac").equals("ec:3d:fd:ef:d5:92")
                                    || (map.containsKey("tmc") && map.get("tmc").equals("ec:3d:fd:ef:d5:92"))) {
                                Intent intent = new Intent("to_distribution");
                                intent.putExtra("result", map.get("rssi").toString());
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            }


                            int Mac_flag = 0;
                            Log.d("当前模式", my_track_target);*/


                          /*  for (Map<String, Object> mapRouter : myRouter) {
                                Log.d("路由器资料包", mapRouter.toString());
                            }*/

                         /*   for (Map<String, Object> mapUser : connected_user_track) {
                                Log.d("连接追踪热点结果是", mapUser.toString());
                            }*/

                         /*
                            if (my_track_target.equals("") || my_track_target.equals("quit_track")) {
                                Intent intent = new Intent("data_analysed");
                                intent.putExtra("extra_data_user", (Serializable) myUser);
                                intent.putExtra("extra_data_router", (Serializable) myRouter);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                Log.d("广播", "发送成功");

                                if (first) {
                                    handler.sendEmptyMessage(1);
                                } else {
                                    handler.sendEmptyMessageDelayed(1, 20 * 1000);
                                }
                                first = false;
                            }//非追踪模式下存数据，发送myUser，myRouter 每过二十秒存一次数据,如果是追踪模式就不存
                            else {
                                Intent intent = new Intent("data_analysed");
                                intent.putExtra("extra_data_user_tracker", (Serializable) my_track_user);
                                intent.putExtra("extra_data_router_tracker", (Serializable) my_track_router);
                                intent.putExtra("extra_data_connected", (Serializable) connected_user_track);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                Log.d("追踪结果广播", "发送成功");
                            }//追踪模式下只用追踪一部分的值.*/

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            read_info();
            timer();
            return super.onStartCommand(intent, flags, startId);
        }
public void read_info(){
            Thread read_thread=new Thread(){
                public void run(){
                    String distance="";
                    while(true){
                        if(Serial_Port_Display.flag==Serial_Port_Display.FINISH_PACK){
                            Intent intent = new Intent("data_analysed");
                            intent.putExtra("extra_data_user", (Serializable) Serial_Port_Display.userList);
                            intent.putExtra("extra_data_router", (Serializable)Serial_Port_Display.routerList);
                            intent.putExtra("track_range", (Serializable)Serial_Port_Display.trackList);
                            intent.putExtra("freq_lst",(Serializable)Serial_Port_Display.freqList);
                            Log.d("track_list",Serial_Port_Display.trackList.toString());
                            web_res=Router_Track.web_res;
                            Log.d("web_res是",web_res);
                            Log.d("freq_list",Serial_Port_Display.freqList.toString());
                            if(web_res.contains("@")&&Serial_Port_Display.closest_ll.contains("@")){
                                String[] s=web_res.split("@");
                                String[] r=Serial_Port_Display.closest_ll.split("@");
                                double x1=Double.parseDouble(s[0]);
                                double y1=Double.parseDouble(s[1]);
                                double x2=Double.parseDouble(r[0]);
                                double y2=Double.parseDouble(r[1]);
                                Log.d("原始数据",x1+","+x2+"\n"+y1+","+y2);
                                double d1=x1-x2;
                                double d2=y1-y2;
                                d1=0.0001;
                                        d2=0.0002;
                                distance=String.valueOf(d1)+"&"+String.valueOf(d2);
                                       //df.format(110000*Math.pow((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2),0.5));
                            }
                            Log.d("结果是",distance);
                            intent.putExtra("value_mid",distance);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                            handler.sendEmptyMessage(1);
                            Log.d("广播", "发送成功");
                        }else{
                            continue;
                          }
                        try{
                          currentThread().sleep(2000);}catch (Exception e){}
                    }
                }
            }; read_thread.start();
}
public String crawler_delay(Map<String,Object> map,String src) {

    long start=System.currentTimeMillis();
    String target = "";
    String temp=map.get(src).toString();
    String h2="";
    for(int i = 0;i < 8;i++)
    {
        if(i != 2 && i != 5 )
        {
            h2 = h2 + temp.charAt(i);
        }
        else
        {
            h2 = h2 + "-";
        }
    }
     h2=h2.toUpperCase();
     //target=optionDetail.getMap_txt(h2);


   Log.d("耗时","结果是"+target+"耗时"+(System.currentTimeMillis()-start)/1000.0+"秒");
    return  target;
}
    public void onDestroy(){
                    if(_socket!=null)
                        try{
                            _socket.close();
                            _socket=null;
                            try{
                           analyseflag=false;readflag=false; //中断线程
                                 }catch (Exception e){}
                        }catch(IOException e){Log.d("socket","disconnected unsuccessfully");
                    }
                }

    @SuppressLint("HandlerLeak")
    Thread db_thread=new Thread(()->{
        Looper.prepare();
        handler=new Handler(){
            public  void handleMessage(Message msg){
             try{

                if(msg.what==1){  myDBAssistant.saveObject(Serial_Port_Display.routerList,Serial_Port_Display.userList,SysApplication.location); }

              }catch (Exception e){}
            }
        };

        Looper.loop();
    });


    private class Data_Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if("location_serve".equals(intent.getAction())) {
                //  handler_read.getLooper().quit();//只要收到广播就不再读取了
                if (intent.getExtras() != null) {
                    //str=intent.getExtras().getString("location_result");
                }
            }
        }
    }

    private class Track_Mode_Activate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if("track_mode".equals(intent.getAction())) {
                if (intent.getExtras() != null) {
                   Serial_Port_Display.trackMac=intent.getExtras().getString("mac_to_track");
                }
            }
        }
    }
        private LinkedList<Double> series(int start,int end,double step)
        {
            LinkedList<Double> x = new LinkedList<>();
            int total = (int)((end-start)/step);
            for(int i = 0;i < total;i++)
            {
                x.add(start+i*step);
            }
            return x;
        }

        public double GuassKernel(double x,double xi,double h)
        {
            double u = (x-xi)/h;
            return 0.4*Math.exp(-u*u/2);
        }

        public double KernelEstimate(LinkedList<Double> data)
        {
            LinkedList<Double> x = new LinkedList<>();
            LinkedList<Double> result = new LinkedList<>();
            double step = 1;
            x = series(0,100,step);
            for(int i = 0;i < x.size();i++)
            {
                double sum = 0;
                for(int j = 0;j < data.size();j++)
                {
                    sum = sum + GuassKernel(x.get(i),data.get(j),0.2);
                }
                result.add(sum);
            }
            double maxValue = -1;
            double optDistance = -1;
            for(int i = 0;i < result.size();i++)
            {
                if(result.get(i) > maxValue)
                {
                    maxValue = result.get(i);
                    optDistance = i;
                }
            }
            return optDistance*step;
        }

    private  void init_note_user(int flag,Map<String,Object> map){

        if(!optionDetail.getTanchuang()){return; }//如果不显示弹窗，就直接return出去

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)//左部图标
                .setContentTitle("关注的终端信息")//上部标题
                .setContentText(map.get("source").toString()+"\n"+map.get("tmc"))//中部通知内容
                .setAutoCancel(true);//点击通知后自动消失

        if(optionDetail.getZhendong()){builder.setDefaults(Notification.DEFAULT_VIBRATE);}
        if(optionDetail.getYuyin()) {builder.setDefaults(Notification.DEFAULT_SOUND);}

        Intent resultIntent = new Intent(this, Tracking_Mac.class);//点击通知后进入的活动
        //这两句非常重要，使之前的活动不出栈
        resultIntent.putExtra("from_note_to_tracking_mac",(Serializable)map);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);//允许更新

        builder.setContentIntent(resultPendingIntent);
        //如果没有就创建，如果有就更新，
        //第一个参数是设置创建通知的id或者需要更新通知的id
        Log.d("终端id是",flag+"  "+map.get("source").toString());
        mNotificationManager.notify(flag, builder.build());

    }

    public void timer(){

        Timer timer=new Timer();


        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try{

                     if(Serial_Port_Display.filter_range==0.0){
                         return;
                    }
                    Intent intent=new Intent("kernel_result");
                    intent.putExtra("kernel_of_user",String.valueOf(Serial_Port_Display.filter_range));
                    LocalBroadcastManager.getInstance(MyIntentService.this).sendBroadcast(intent);
                   Log.d("距离滤波",Serial_Port_Display.filter_range+"米");
                }catch (Exception e){}
            }
        },1000,30*1000);
    }
    private  void init_note_router(int flag,Map<String,Object> map){

       if(!optionDetail.getTanchuang()){return; }//如果不显示弹窗，就直接return出去

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)//左部图标
                .setContentTitle("关注的WiFi热点")//上部标题
                .setContentText(map.get("router").toString()+"\n"+map.get("mac")) //中部通知内容
                .setAutoCancel(true);//点击通知后自动消失
        if(optionDetail.getZhendong()){builder.setDefaults(Notification.DEFAULT_VIBRATE);}
        if(optionDetail.getYuyin()) {builder.setDefaults(Notification.DEFAULT_SOUND);}
        Intent resultIntent = new Intent(this, Router_Track.class);//点击通知后进入的活动
        //这两句非常重要，使之前的活动不出栈
        resultIntent.putExtra("from_note_to_router_track",(Serializable)map);
      //  resultIntent.setAction(Intent.ACTION_MAIN);
       // resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);//允许更新

        builder.setContentIntent(resultPendingIntent);
        //如果没有就创建，如果有就更新，
        //第一个参数是设置创建通知的id或者需要更新通知的id
      Log.d("热点id是",flag+"  "+map.get("router").toString());
        mNotificationManager.notify(flag+100, builder.build()); //这样可以让用户和热点分开

    }

    private void init_kernel(Map<String,Object> map,String tracking_mac,String flag){
      double result=0;
      if(first_init_kernel){ temp_init_checker=tracking_mac; first_init_kernel=false; }//把第一次的值给一个全局变量
        // ，然后看这个tracking_mac有没有变化。
      if(temp_init_checker.equals(tracking_mac)){
      //temp_init_checker=tracking_mac;
       if(flag.equals("found")){
           //if(Double.parseDouble(map.get("range").toString())<Pre_Range){
         ccnt=0;
         Pre_Range=Double.parseDouble(map.get("range").toString());
     range_MAC.add(Pre_Range);
       }
            //有包的时候flag就是found,把包读进去

       if(flag.equals("missed")&&Math.floor(Pre_Range)!=0){  range_MAC.add(Pre_Range);}//没有包补数据

          Log.d("低于15次",range_MAC.toString());
         if(range_MAC.size()==30){
          result=KernelEstimate(range_MAC);
           //save_log(range_MAC,result);
          Log.d("待处理数据",range_MAC.toString()+"\n"+result);
             range_MAC.clear(); }//30个计算一次，清空
        }

      else {
          temp_init_checker=tracking_mac;
          Pre_Range=0;
          range_MAC.clear();//如果发生了变化，那就把目标赋给全局变量，然后在把range_Mac清零,也要把Pre_Range清零
      }
       if(result>0.0){
          Intent intent=new Intent("kernel_result");
          intent.putExtra("kernel_of_user",String.valueOf(result));
          LocalBroadcastManager.getInstance(MyIntentService.this).sendBroadcast(intent);
          ring_ing_and_vibrate(result);Log.d("报警阈值",result+"");
      }
        last_value=result;
    }

    public void ring_ing_and_vibrate(double result) {
        Log.d("测试播放结果", " " + result);
        long[] patter = {1000, 1000, 2000, 50};
        if (result < Double.parseDouble(optionDetail.getJuli_min())) {
            if(optionDetail.getYuyin()){
                mediaPlayer.start();   } else { mediaPlayer.pause(); }
            if(optionDetail.getZhendong()){
                vibrator.vibrate(patter, -1);
            }else { vibrator.cancel(); }

            Log.d("一播放", "是的");
        }
        if (result > Double.parseDouble(optionDetail.getJuli_min())) {
            vibrator.cancel();
            mediaPlayer.pause();
            Log.d("停止播放", "是的");
        }
    }

    public void save_json(String str){
        FileOutputStream fos;
        File file=new File(getExternalFilesDir("数据"),"datas.txt");
        try{
            fos=new FileOutputStream(file,true);
            fos.write(str.getBytes());
            fos.close();
        }catch (Exception e){}
    }
    public void save_log(LinkedList<Double> range, double result){

        if(num%10==0){range_all.append(range.toString()).append("\n");
        range_result.append(result).append("\n");
            FileOutputStream fos1,fos2;
            //要写入的数据
            String str1 =range_all.toString();
            String str2=range_result.toString();
            //设置文件路径 ，第一个参数是文件保存的路径，null放在根目录下，第二个参数是文件名
            File file1 = new File(getExternalFilesDir("kernel_filter"), "/data_raw.txt");
            File file2= new File(getExternalFilesDir("kernel_filter"), "/data_result.txt");

            try {
                fos1 = new FileOutputStream(file1);
                fos1.write(str1.getBytes());
                fos2=new FileOutputStream(file2);
                fos2.write(str2.getBytes());
                Log.d("写入成功","确认");
                fos1.close();fos2.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            num=0;
        }
        num++;
    }


}


