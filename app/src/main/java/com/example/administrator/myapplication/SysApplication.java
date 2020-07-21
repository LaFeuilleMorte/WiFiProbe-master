package com.example.administrator.myapplication;
import android.app.Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import com.baidu.mapapi.SDKInitializer;
import com.example.administrator.myapplication.service.LocationService;
public class SysApplication extends Application {
    private List<Activity> mList = new LinkedList<Activity>();
    private static SysApplication instance;
    private Option_Detail optionDetail;
    private String[] opts=new String[]{"yuyin","zhendong","tanchuang"};
    static BluetoothSocket globalBlueSocket = null;
    public LocationService locationService;
    private Map<String,String> map_txt=new HashMap<>();
    Vibrator mVibrator=null;
    public static Map<Integer,String> macMap=new HashMap<Integer, String>();
    public static String location="";
    public SysApplication() {
    }

    public void onCreate(){
        super.onCreate();


        locationService = new LocationService(getApplicationContext());
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
       optionDetail=Option_Detail.getSingleInstance();
        SharedPreferences read=getSharedPreferences("check_my_options",MODE_PRIVATE);

        SharedPreferences read_loc=getSharedPreferences("account_reg",MODE_PRIVATE);
        try{
            location= read_loc.getAll().get("location").toString();}catch (Exception e){}
       /*
       把TXT文件加载到内存
       * */
       load_file();
        if(read.contains("juli_min")){
            optionDetail.setJuli_min( read.getAll().get("juli_min").toString());
        }
        for(int i=0;i<opts.length;i++){
            if(read.contains(opts[i])){
                if(read.getAll().get(opts[i]).toString().equals("true")){
                    if(i==0){
                        optionDetail.setYuyin(true);
                    }if(i==1){
                        optionDetail.setZhendong(true);
                    }if(i==2){
                        optionDetail.setTanchuang(true);
                    }
                }
                if(read.getAll().get(opts[i]).toString().equals("false")){
                    if(i==0){
                        optionDetail.setYuyin(false);
                    }if(i==1){
                        optionDetail.setZhendong(false);
                    }if(i==2){
                        optionDetail.setTanchuang(false);
                    }
                }
            }
        }
     SharedPreferences read_zhanghu=getSharedPreferences("yonghu",MODE_PRIVATE);
        try{optionDetail.setZhanghu(read_zhanghu.getAll().get("user").toString());
        }catch (Exception e){}//加载上一次登录的账户

        /*创建application时先加载设置
        * */

   }

    public  static void setGlobalBlueSocket(BluetoothSocket __globalBlueSocket){
        globalBlueSocket = __globalBlueSocket;
    }

    public synchronized static SysApplication getInstance() {
        if (null == instance) {
            instance = new SysApplication();
        }
        return instance;
    }
    // add Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
        Log.d("当前活动的数量",mList.size()+"");
    }

    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
             System.exit(0);
        }
    }
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    public  void load_file() {


        Thread thread_load=new Thread() {
            public void run() {
                try

                {
                    InputStream is=getAssets().open("dic_mac_company.txt");
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(reader);

                    String line = "";
                    line = br.readLine();
                    while (line != null) {
                        String[] macinfo = line.split("\t");
                        String macNum = macinfo[1];
                        String macManuf = macinfo[2];
                        int macIdx = to_hash(macNum);
                        macMap.put(macIdx, macManuf);
                        line = br.readLine();
                    }
                } catch (Exception e)

                {
                    Log.d("error", "wed");
                    e.printStackTrace();
                }
                Log.d("whole map",macMap.toString());
            }

        };thread_load.start();
    }

    public  static int to_hash(String data)
    {
        int macIdx = 0;
        for(int i = 0;i < data.length();i++)
        {
            if(data.charAt(i) != '-' && data.charAt(i) != ':')
            {
                int numDict = 0;
                switch(data.charAt(i))
                {
                    case '0':numDict = 0;break;
                    case '1':numDict = 1;break;
                    case '2':numDict = 2;break;
                    case '3':numDict = 3;break;
                    case '4':numDict = 4;break;
                    case '5':numDict = 5;break;
                    case '6':numDict = 6;break;
                    case '7':numDict = 7;break;
                    case '8':numDict = 8;break;
                    case '9':numDict = 9;break;
                    case 'A':numDict = 10;break;
                    case 'B':numDict = 11;break;
                    case 'C':numDict = 12;break;
                    case 'D':numDict = 13;break;
                    case 'E':numDict = 14;break;
                    case 'F':numDict = 15;break;
                }
                macIdx = macIdx * 16 + numDict;
            }
        }
        return macIdx;
    }
}
