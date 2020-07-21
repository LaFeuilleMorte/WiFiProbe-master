package com.example.administrator.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class History_Of_Router extends Activity {
    private ExpandableListView expandlist;
    private MyExpandableListAdapter adapter;
    private Button flag_btn;
    private Button option_btn;
    private final int RESULT=5;
    private  ArrayList<Map<String,Object>> data=new ArrayList<Map<String,Object>>();
    Handler handler;
    Handler handler_refresh;
    ArrayList<Map<String, Object>> groupList=new ArrayList<Map<String,Object>>();
    MyDBAssistant myDBAssistant=new MyDBAssistant(this);
    ArrayList<ArrayList<Map<String, Object>>> childList=new ArrayList<ArrayList<Map<String,Object>>>();
    ArrayList<Map<String, Object>> child;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
    private String[] user_info={"设备正在连接的路由Mac：","设备连接路由器名称：","信号强度：","距离：","第一次探测到时间：","最近探测到时间：","累计在线时长：","手机或电脑厂商：","出现次数："};
    private String[] user_key={"mac","router","rssi","range","start","latest","duration","source","show"};
    private TextView tv_tag;
    private ListView areaListView;
    private ListView sortList;
    private String[] areas={"按Mac","按名称","按时间","按频率"};
    private String[] areas_options={"tmc","source","start","show"};
    private String[] sort={"升序","降序"};
    private RadioOnClick OnClick;
    private SortSelect sortSelect;
    private String src;
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.history_of_router);
        SysApplication.getInstance().addActivity(this);
        expandlist=(ExpandableListView)findViewById(R.id.list_history_router);
        tv_tag=(TextView)findViewById(R.id.tv_history_tag) ;
        flag_btn=(Button)findViewById(R.id.flag_btn);
        option_btn=(Button)findViewById(R.id.options_btn);
        Intent intent=getIntent();
        src=intent.getExtras().getString("router_localDB");
        String content = "<font color=\"#b80190\">" + src.substring(0,17)+ "</font>"
                +"<br>"+"<font color=\"#000000\">"+src.substring(18,src.length())+"</font>";
        tv_tag.setText(Html.fromHtml(content));
        OnClick=new RadioOnClick(0);
        sortSelect=new SortSelect(0);
        option_btn.setOnClickListener(new RadioClickListener());
        flag_btn.setOnClickListener(new SortClickListener());

        handler = new Handler() {

            public void handleMessage(Message msg) {

                if (msg.what == RESULT) {

                    if(data.size()!=0){
                        groupList.clear();
                        childList.clear();
                        groupList.addAll(data);

                        for(Map<String,Object> user_:groupList){
                            try{
                                long duration= sdf.parse(user_.get("latest").toString()).getTime()-sdf.parse(user_.get("start").toString()).getTime();
                                long dure=duration/1000;
                                long sec=dure%60;
                                long min=(dure-sec)/60%60;
                                long hour=(dure-sec)/60/60;
                                user_.put("duration",hour+"时"+min+"分"+sec+"秒");
                            }catch (ParseException e){e.printStackTrace();
                            }catch (NullPointerException e){e.printStackTrace();}

                            user_.put("group",user_.get("tmc"));
                        }//计算时差duration
                        for (int j = 0; j < groupList.size(); j++) {
                            child = new ArrayList<Map<String, Object>>();
                            for(int i=0;i<user_info.length;i++) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                try{
                                    map.put("child",user_info[i]+groupList.get(j).get(user_key[i]));
                                }catch (NullPointerException e){Log.i("异常","空指针异常");
                                }
                                child.add(map);
                            }
                            childList.add(child);
                        }
                        //新建一个数据源，避免原来的数据源修改时报错
                        MyExpandableListAdapter adapter = new MyExpandableListAdapter(History_Of_Router.this,groupList, childList);
                        expandlist.setAdapter(adapter);
                        String content = "<font color=\"#b80190\">" + src.substring(0,17)+ "</font>"
                                +"<br>"+"<font color=\"#000000\">"+src.substring(18,src.length())+"</font>"
                                +"<font color=\"#08088A\">"+"   一共"+data.size()+"条"+"</font>";
                        tv_tag.setText(Html.fromHtml(content));
                    }else{
                        groupList.clear();
                        childList.clear();
                        MyExpandableListAdapter adapter = new MyExpandableListAdapter(History_Of_Router.this,groupList, childList);
                        expandlist.setAdapter(adapter);
                        String content = "<font color=\"#b80190\">" + src.substring(0,17)+ "</font>"
                                +"<br>"+"<font color=\"#000000\">"+src.substring(18,src.length())+"</font>"
                                +"<font color=\"#08088A\">"+"   没有任何记录"+"</font>";
                        tv_tag.setText(Html.fromHtml(content));
                    }
                }
            }
        };

        String temp="";
        for(int i=0;i<areas.length;i++){
            if(areas[i].equals(option_btn.getText().toString())){
                temp=areas_options[i];
            }
        }
        data=myDBAssistant.getLocalDb_Router_To_User(src.substring(0,17),temp,flag_btn.getText().toString());
        handler.sendEmptyMessage(RESULT);
    }
    class RadioClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            AlertDialog ad =new AlertDialog.Builder(History_Of_Router.this).setTitle("选择排序字段"+"   按Mac(默认)")

                    .setSingleChoiceItems(areas,OnClick.getIndex(),OnClick).create();

            areaListView=ad.getListView();

            ad.show();

        }

    }

    class RadioOnClick implements DialogInterface.OnClickListener{

        private int index;



        public RadioOnClick(int index){

            this.index = index;

        }

        public void setIndex(int index){

            this.index=index;

        }

        public int getIndex(){

            return index;

        }



        public void onClick(DialogInterface dialog, int whichButton){

            setIndex(whichButton);
            option_btn.setText(areas[index]);
            data=myDBAssistant.getLocalDb_Router_To_User(src.substring(0,17),areas_options[index],flag_btn.getText().toString());
            handler.sendEmptyMessage(RESULT);
            dialog.dismiss();

        }

    }


    class SortClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            AlertDialog ad =new AlertDialog.Builder(History_Of_Router.this).setTitle("选择排序方式"+"   升序(默认)")

                    .setSingleChoiceItems(sort,sortSelect.getIndex(),sortSelect).create();

            sortList=ad.getListView();

            ad.show();

        }

    }

    class SortSelect implements DialogInterface.OnClickListener{

        private int index;



        public SortSelect(int index){

            this.index = index;

        }

        public void setIndex(int index){

            this.index=index;

        }

        public int getIndex(){

            return index;

        }



        public void onClick(DialogInterface dialog, int whichButton){
            String options="";
            setIndex(whichButton);
           flag_btn.setText(sort[index]);
           for(int i=0;i<areas.length;i++){
               if(areas[i].equals(option_btn.getText().toString())){
                   options=areas_options[i];
               }
           }
          data= myDBAssistant.getLocalDb_Router_To_User(src.substring(0,17),options,sort[index]);
            handler.sendEmptyMessage(RESULT);
           dialog.dismiss();

        }

    }

}
