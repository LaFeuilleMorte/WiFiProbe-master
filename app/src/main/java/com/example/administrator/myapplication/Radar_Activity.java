package com.example.administrator.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.myapplication.been.Info;
import com.example.administrator.myapplication.custom.CustomViewPager;
import com.example.administrator.myapplication.custom.RadarViewGroup;
import com.example.administrator.myapplication.utils.FixedSpeedScroller;
import com.example.administrator.myapplication.utils.LogUtil;
import com.example.administrator.myapplication.utils.ZoomOutPageTransformer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Radar_Activity extends Activity implements ViewPager.OnPageChangeListener, RadarViewGroup.IRadarClickListener {

    private CustomViewPager viewPager;
    private RelativeLayout ryContainer;
    private RadarViewGroup radarViewGroup;
    private ArrayList<Map<String,Object>>  terminals=new ArrayList<>();
    private ArrayList<Map<String,Object>>  hotspot=new ArrayList<>();
    private ArrayList<Map<String,Object>>  myUser=new ArrayList<>();
    private ArrayList<Map<String,Object>> myRouter=new ArrayList<>();
     private Handler handler;
     private  ViewpagerAdapter mAdapter;
    private int mPosition;
    private Map<String,Object> map;
    private FixedSpeedScroller scroller;
    private SparseArray<Info> mDatas = new SparseArray<>();
    private Data_Receiver dataReceiver;
    //private ArrayList<Map<String,Object>> random_list=new ArrayList<>();
    @SuppressLint({"HandlerLeak", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radar_view);
        SysApplication.getInstance().addActivity(this);
        initView();
        dataReceiver = new Data_Receiver();
        IntentFilter intentFilter_data = new IntentFilter("data_analysed");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);
        /**
         * 将Viewpager所在容器的事件分发交给ViewPager
         */
        ryContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return viewPager.dispatchTouchEvent(event);
            }
        });
         mAdapter = new ViewpagerAdapter();
        viewPager.setAdapter(mAdapter);
        //设置缓存数为展示的数目
        viewPager.setOffscreenPageLimit(mDatas.size());
        viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        //设置切换动画
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.addOnPageChangeListener(this);
        setViewPagerSpeed(250);
        radarViewGroup.setDatas(mDatas);
        radarViewGroup.setiRadarClickListener(this);
        handler=new Handler(){
            int i=0;
            public void handleMessage(Message msg){

                  terminals.clear();
                  hotspot.clear(); //这里terminals表示全部的
                  //把他们全部放在一块，然后再一起处理

                for(Map<String,Object> mapUser:myRouter){

                    for(i=0;i<hotspot.size();i++){
                        if( hotspot.get(i).containsKey("mac")//含有tmc情况,即用户
                                &&hotspot.get(i).get("mac").toString().equals(mapUser.get("mac").toString())
                                ){
                            hotspot.get(i).put("range",mapUser.get("range"));break;
                        }
                    }
                    if(i==hotspot.size()){
                        hotspot.add(mapUser);   }
                }



                  for(Map<String,Object> mapUser:myUser){

                        for(i=0;i<terminals.size();i++){
                            if( terminals.get(i).containsKey("tmc")//含有tmc情况,即用户
                                      &&terminals.get(i).get("tmc").toString().equals(mapUser.get("tmc").toString())
                                    ){
                                terminals.get(i).put("range",mapUser.get("range"));break;
                            }
                        }
                        if(i==terminals.size()){
                            terminals.add(mapUser);   }
                    }

                terminals.addAll(hotspot);
                 initData();
                  Log.d("terminals是",terminals.toString());
            }
        };


    }

    private void initData() {
        mDatas.clear();
        for (int i = 0; i < terminals.size(); i++) {
            Info info = new Info();
           // info.setName(terminals.get(i).get("source").toString());//terminals.get(i).get("source").toString());
            info.setDistance(Float.parseFloat(terminals.get(i).get("range").toString()));
            if(terminals.get(i).containsKey("tmc")){
                info.setName(terminals.get(i).get("source").toString());
                info.setKind("user");
            }else {
                info.setName(terminals.get(i).get("router").toString());
                info.setKind("router");
            }
            mDatas.put(i, info);
        }
            //更新数据
         radarViewGroup.setDatas(mDatas);
         mAdapter.notifyDataSetChanged();
    }
    private void initView() {
        viewPager = (CustomViewPager) findViewById(R.id.vp);
        radarViewGroup = (RadarViewGroup) findViewById(R.id.radar);
        ryContainer = (RelativeLayout) findViewById(R.id.ry_container);
    }

    /**
     * 设置ViewPager切换速度
     *
     * @param duration
     */
    private void setViewPagerSpeed(int duration) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            scroller = new FixedSpeedScroller(Radar_Activity.this, new AccelerateInterpolator());
            field.set(viewPager, scroller);
            scroller.setmDuration(duration);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPosition = position;
    }

    @Override
    public void onPageSelected(int position) {
        radarViewGroup.setCurrentShowItem(position);
        LogUtil.m("当前位置 " + mPosition);
        LogUtil.m("速度 " + viewPager.getSpeed());
        //当手指左滑速度大于2000时viewpager右滑（注意是item+2）
        if (viewPager.getSpeed() < -1800) {

            viewPager.setCurrentItem(mPosition + 2);
            LogUtil.m("位置 " + mPosition);
            viewPager.setSpeed(0);
        } else if (viewPager.getSpeed() > 1800 && mPosition > 0) {
            //当手指右滑速度大于2000时viewpager左滑（注意item-1即可）
            viewPager.setCurrentItem(mPosition - 1);
            LogUtil.m("位置 " + mPosition);
            viewPager.setSpeed(0);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRadarItemClick(int position) {
        viewPager.setCurrentItem(position);
    }


    class ViewpagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final Info info = mDatas.get(position);
            //设置一大堆演示用的数据，麻里麻烦~~
           View view = LayoutInflater.from(Radar_Activity.this).inflate(R.layout.viewpager_layout, null);
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            TextView tvDistance = (TextView) view.findViewById(R.id.tv_distance);
            tvName.setText(info.getName());
            tvDistance.setText(info.getDistance() + "m");
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

    }

    private class Data_Receiver extends BroadcastReceiver {
        boolean first=true;
        @Override
        public void onReceive(Context context, Intent intent){
            if("data_analysed".equals(intent.getAction())){

                if(intent.getExtras()!=null){
                    try{
                        myUser =(ArrayList<Map<String,Object>>)intent.getSerializableExtra("extra_data_user");
                        myRouter=(ArrayList<Map<String,Object>>)intent.getSerializableExtra("extra_data_router");

                        if (myRouter!=null&&myUser!=null){
                            handler.sendEmptyMessageDelayed(0,2*1000);
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
}
