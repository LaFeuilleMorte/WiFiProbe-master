package com.example.administrator.myapplication;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.example.administrator.myapplication.service.LocationService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class My_Location_Service extends Service {
        LocationService locationService;
        Handler handler;
        Runnable   runnable_blt;
        Runnable  runnable_random_data;
        String[] my_names=new String[]{"狗剩子","二狗子","艾丽范宁","王大锤","孙尚香","貂蝉","奥黛丽赫本","麦肯娜格蕾丝","艾玛沃森"};
        ArrayList<Map<String,Object>>  random_list=new ArrayList<>();
        Handler handler_blt=new Handler();
   public  My_Location_Service(){

   }
    public void onCreate(){
       super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @SuppressLint("HandlerLeak")
    public int  onStartCommand(Intent intent, int flags, int startId){
        locationService = ((SysApplication) getApplication()).locationService;
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();
        runnable_blt=new Runnable() {
            @Override
            public void run() {
                if(SysApplication.globalBlueSocket==null){
                    Toast.makeText(getApplicationContext(),"蓝牙已断开，请重新连接",Toast.LENGTH_SHORT).show();}
                else{
                     if(!SysApplication.globalBlueSocket.isConnected()){
                        Toast.makeText(getApplicationContext(),"蓝牙已断开，请重新连接",Toast.LENGTH_SHORT).show();
                        try{ SysApplication.globalBlueSocket.close();}catch (Exception e){ }
                        SysApplication.globalBlueSocket=null;
                    }
                }
             handler_blt.postDelayed(runnable_blt,10000);
            }
        };  handler_blt.postDelayed(runnable_blt,10000);
      /*
      *  生成随机的数据
      * */
      /*Random random=new Random();
      runnable_random_data=new Runnable() {
          int count=0;
          @Override
          public void run() {
              Map<String,Object> map=new HashMap<>();
              map.put("range",random.nextFloat()%70+10);
              map.put("name",my_names[random.nextInt(my_names.length)%(my_names.length+1)]);
              map.put("mac",random.nextInt());
              Log.d("随机信息是map",map.toString());
              random_list.add(map);
              if(count++>20){
                  Intent intent = new Intent("random_data");
                  intent.putExtra("my_random_data",(Serializable)random_list );
                  LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                  count=0;//发出去一包数据, 清空重新来
                  Log.d("随机信息是20",random_list.toString());
               }
           handler_blt.postDelayed(runnable_random_data,5000);
          }
      };   handler_blt.postDelayed(runnable_random_data,5000);*/
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        handler_blt.removeCallbacks(runnable_blt);//注销掉蓝牙监听
        super.onDestroy();
    }
    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */

   public void logMsg(String str) {
       Intent intent = new Intent("location_serve");
       intent.putExtra("location_result", str);
       LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
       Log.d("广播", str);
   }

    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
                sb.append("\n纬度：");// 纬度
                sb.append(location.getLatitude());
                sb.append("\n经度：");// 经度
                sb.append(location.getLongitude());
                sb.append("\n详细地址：");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\n是否在室内：");// *****返回用户室内外判断结果*****
                if(location.getUserIndoorState()==1) {sb.append("是");} else{ sb.append("否");}
                sb.append("\n周边地点: ");// POI信息
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < 2; i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                  //  sb.append("\nspeed : ");
                   // sb.append(location.getSpeed());// 速度 单位：km/h
                 //   sb.append("\nsatellite : ");
                   // sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                  //  sb.append("\ngps status : ");
                    //sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                   // sb.append("\ndescribe : ");
                   // sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    //sb.append("\noperationers : ");// 运营商信息
                   // sb.append(location.getOperators());
                   // sb.append("\ndescribe : ");
                  //  sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                   // sb.append("\ndescribe : ");
                   // sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                  //  sb.append("\ndescribe : ");
                  //  sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                 //   sb.append("\ndescribe : ");
                  //  sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                  //  sb.append("\ndescribe : ");
                   // sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                logMsg(sb.toString());
            }
        }

    };
}
