package com.example.administrator.myapplication;

import android.app.Activity;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.administrator.myapplication.utils.pachong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import android_serialport_api.SerialPort;

    public class Serial_Port_Display {
        protected SerialPort mSerialPort;
        protected InputStream mInputStream;
        protected OutputStream mOutputStream;
        private TextView text;
        public static String closest_ll="";
        private String prot = "ttyUSB0";//串口号（具体的根据自己的串口号来配置）
        private int baudrate = 115200;//波特率（可自行设定）
        private static int i = 0;
        private StringBuilder sb;
        private static Map<Integer, String> macMap = new HashMap<Integer, String>();
        private Thread receiveThread;
        private Thread sendThread;
        public static Serial_Port_Display portDisplay = null;
        public static int tmc_idx = 0;
        public static int mac_idx = 1;
        public static int count;
        public static int channel_idx = 4;
        public static int rssi_idx = 5;
        public static  Double filter_range = 0.0;
        public static  Integer freq=0;
        public static int name_idx = 6;
        public static ArrayList<Map<String, Object>> routerList = new ArrayList();
        public static ArrayList<Map<String, Object>> userList = new ArrayList();
        public static ArrayList<Double> freqList = new ArrayList();
        public static ArrayList<Double> trackList = new ArrayList();
        public static ArrayList<Map<String,Object>> sort_range=new ArrayList<>();
        public static final int FINISH_PACK = 0;
        public static final int PACKING = 1;
        public static int flag = PACKING;
        public static int obj_idx = 8;
        public static int timer_count=0;
        public static String my_loc="";
        public static int pre=0;
        public static String trackMac = "quit_track";
        public static  StringBuilder builder=new StringBuilder(1024);
        private static DecimalFormat df = new DecimalFormat();
        static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
        public Serial_Port_Display() {
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(2);
            sb = new StringBuilder();
            // 配置并打开串口
            try {
                mSerialPort = new SerialPort(new File("/dev/" + prot), baudrate,
                        0);
                mInputStream = mSerialPort.getInputStream();
                mOutputStream = mSerialPort.getOutputStream();
                receiveThread();
                send_Indication();//打开串口就发指令
                timer(); //定时器打开
                find_closest_timer();//找最近的mac
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("test", "打开失败");
                e.printStackTrace();
            }
            if (SysApplication.macMap.size() != 0) {
                macMap = SysApplication.macMap;
            }
        }

        private void receiveThread() {
            // 接收串口信息
            receiveThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        int size = 0;
                        int ch;
                        try {
                            byte[] buffer = new byte[1024];
                            if (mInputStream == null)
                                return;
                            while ((ch = mInputStream.read()) != '\n') {
                                if (ch != -1) {
                                    buffer[size] = (byte) ch;
                                    size++;
                                }
                            }
                            buffer[size] = (byte) '\n';
                            size++;

                            if (size > 0) {
                                String recinfo = new String(buffer, 0,
                                        size);
                                try {
                                    Log.d("xiaoxi", recinfo);
                                    infoPack(recinfo, trackMac);
                                    flag = FINISH_PACK;
                                    currentThread().sleep(300);//等待一会以让服务遍历
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            receiveThread.start();
        }

        public void send_Indication() {
            // 发送串口信息
            sendThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            i++;
                            mOutputStream.write(("AT+ALL=1").getBytes());
                            Log.d("track_mac",trackMac+"\nvoila");
                           // mOutputStream.write(("AT+FREQUENCY=2").getBytes());
                           if(!trackMac.equals("quit_track")&&trackMac.contains(":")){
                               //mOutputStream.write(("AT+FREQUENCY=2").getBytes());
                           }

                            Log.i("test", "发送成功:1" + i);
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            Log.i("test", "发送失败");
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            };
            sendThread.start();
        }


        /**
         * 关闭串口
         */
        public void closeSerialPort() {

            if (mSerialPort != null) {
                mSerialPort.close();
            }
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


        public static String freq_check(String data) {
            Double dt = Double.parseDouble(data);
            if (dt > 14) {
                return "5G";
            } else {
                return "2.4G";
            }
        }

        public static Double get_distance(String data) {
            Double dt = Double.parseDouble(data);
            Double result=0.0;
            result=0.037*Math.exp(-0.086*dt);
            /*
           if(dt<=-65){
              result=-0.0032*dt*dt*dt-0.6293*dt*dt-42.384*dt-964.9;
           }*/
           /*if(dt>-65){
               result=0.00001*dt*dt*dt*dt+0.0019*dt*dt*dt+0.1292*dt*dt+4.0551*dt+48.326;
            }*/
           // result = 0.00003 * dt * dt * dt * dt + 0.0062 * dt * dt * dt + 0.5161 * dt * dt + 19.343 * dt + 273.18;
            return Double.parseDouble(df.format(result));
        }

        public static int routerUpdate(ArrayList<Map<String, Object>> rList, String mac) {
            for (int i = 0; i < rList.size(); i++) {
                Map<String, Object> temp = rList.get(i);
                if (temp.containsKey("mac")) {
                    if (temp.get("mac").toString().equals(mac)) {
                        return i;
                    }
                }
            }
            return -1;
        }

        public static int userUpdate(ArrayList<Map<String, Object>> rList, String rmac, String umac) {
            for (int i = 0; i < rList.size(); i++) {
                Map<String, Object> temp = rList.get(i);
                if (temp.containsKey("mac") && temp.containsKey("tmc")) {
                    if (temp.get("mac").toString().equals(rmac) && temp.get("tmc").toString().equals(umac)) {
                        return i;
                    }
                }
            }
            return -1;
        }

        public static Map<String, Object> routerPack(String mac,
                                                     String maunf,
                                                     String channel,
                                                     String freq,
                                                     String rssi,
                                                     Double range,
                                                     String name) {
            Map<String, Object> rList = new HashMap<String, Object>();
            rList.put("mac", mac);
            rList.put("source", maunf);
            rList.put("channel", channel);
            rList.put("freq", freq);
            rList.put("rssi", rssi);
            rList.put("range", range);
            rList.put("router", name);
            rList.put("show",1);//Log.d("hanshu",rssi+"@"+range);
            return rList;
        }

        public static Map<String, Object> userPack(String r_mac,
                                                   String u_mac,
                                                   String maunf,
                                                   String channel,
                                                   String freq,
                                                   String rssi,
                                                   Double range,
                                                   String name) {
            Map<String, Object> rList = new HashMap<String, Object>();
            rList.put("mac", r_mac);
            rList.put("tmc", u_mac);
            rList.put("source", maunf);
            rList.put("channel", channel);
            rList.put("freq", freq);
            rList.put("rssi", rssi);
            rList.put("range", range);
            rList.put("router", name);
            rList.put("show",1);
            String dateStringParse = "";
            try {
                Date date = new Date();
                dateStringParse = sdf.format(date);

            } catch (Exception e) {
                e.printStackTrace();
            }
            rList.put("start",dateStringParse);
            rList.put("latest", dateStringParse);
            if (r_mac.equals("FF:FF:FF:FF:FF:FF")) {
                rList.put("status", "offline");
            } else {
                rList.put("status", "normal");
            }
            return rList;
        }

       public static void timer(){

            Timer timer=new Timer();
           timer.schedule(new TimerTask() {

               @Override
               public void run() {
                  try{
                     gather();

                  }catch (Exception e){}
               }
           },1000,60*1000);
       }
        public static void find_closest_timer(){

            Timer timer=new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try{
                        if(timer_count>0){
                            Log.d("经纬度是：",my_loc);
                            return;
                        }
                        my_loc=find_closest();
                        timer_count=1;
                    }catch (Exception e){}
                }
            },1000,30*1000);
        }
        public static void gather() {
    /*
    打开定时器: 定时时长set_time.
    在每次定时器完成的时候，
     */

            if(trackMac.equals("quit_track")&&trackMac.contains(":")){
                Log.d("当前不是追踪模式","是的");
                return;
            }


            freq = freqList.size();
            Collections.sort(trackList);
            if (trackList.size() % 2 == 1) {
                filter_range = trackList.get((trackList.size() - 1) / 2);
            } else {
                filter_range = (trackList.get(trackList.size() / 2 - 1) + trackList.get(trackList.size() / 2) + 0.0) / 2;
            }
           trackList.clear();
            freqList.clear();
            Log.d("滤波值",filter_range+"米");Log.d("计数",freq+"包");

            builder.append(Serial_Port_Display.freq).append("包").append(",");
        }

        //用来追踪的函数
        public static void infoPack(String readin, String trackMac) {

            String[] macinfo = readin.split("\\|");
            String user_mac = macinfo[tmc_idx];
            String router_mac = macinfo[mac_idx];
            String channel = macinfo[channel_idx];
            String rssi = macinfo[rssi_idx];
            String nickname = macinfo[name_idx];
            String owner = macinfo[obj_idx];

            int user_hash = SysApplication.to_hash(user_mac.substring(0, 8));
            int router_hash = SysApplication.to_hash(router_mac.substring(0, 8));
            String freqency = freq_check(channel);
            Double range = get_distance(rssi);

            if (macMap.containsKey(user_hash) && (macMap.containsKey(router_hash) || router_mac.equals("FF:FF:FF:FF:FF:FF"))) {
                if (!router_mac.equals("FF:FF:FF:FF:FF:FF")) {
                    int router_idx = routerUpdate(routerList, router_mac);
                    String maunf = macMap.get(router_hash).toString();
                    Map<String, Object> rList = routerPack(router_mac, maunf, channel, freqency, rssi, range, nickname);
                    if (router_idx != -1) {
                        routerList.remove(router_idx);
                    }
                    routerList.add(rList);
                }
                int user_idx = userUpdate(userList, router_mac, user_mac);
                String maunf = macMap.get(user_hash).toString();
                Map<String, Object> uList = userPack(router_mac, user_mac, maunf, channel, freqency, rssi, range, nickname);
                if (user_idx != -1) {
                    userList.remove(user_idx);
                }
                userList.add(uList);

                if (router_mac.equals(trackMac)) {
                    freqList.add(range);trackList.add(range);Log.d("wq",range+"米");
                    Log.d("owner",owner);
                   // if (owner.equals("0")) {
                    //    trackList.add(range);Log.d("wq",range+"米");
                  //  }
                }

            }

        }

        public static String find_closest(){


               String mac="";

               double min=Double.parseDouble(routerList.get(0).get("range").toString());
            for(int i=0;i<routerList.size();i++){
                double value=Double.parseDouble(routerList.get(i).get("range").toString());
                if(min>value){
                    min=value;
                    mac=routerList.get(i).get("mac").toString();
                }
            }
         closest_ll= pachong.getLat_Lon(mac);
         return min+"米"+"\n"+"目标mac:"+mac+"\n"+closest_ll;

        }
    }



