package com.example.administrator.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Result_Of_Collision extends Activity {
    private ExpandableListView expandlist;
    private MyExpandableListAdapter adapter;
    private Button flag_btn;
    private Button option_btn;
    private final int RESULT=5;
    private  ArrayList<Map<String,Object>> data=new ArrayList<Map<String,Object>>();
    Handler handler;
    private Button button_filter;
    int classify=1;
    String str="";
    Handler handler_refresh;
    private MacTextWatcher macTextWatcher;
    ArrayList<Map<String, Object>> groupList=new ArrayList<Map<String,Object>>();
    MyDBAssistant myDBAssistant=new MyDBAssistant(this);
    ArrayList<ArrayList<Map<String, Object>>> childList=new ArrayList<ArrayList<Map<String,Object>>>();
    ArrayList<Map<String, Object>> child;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
    private String[] user_info={"设备Mac：","设备正在连接的路由Mac：","设备连接路由器名称：","信号强度：","距离：","第一次探测到时间：","最近探测到时间：","累计在线时长：","出现次数："};
    private String[] user_key={"tmc","mac","router","rssi","range","start","latest","duration","show"};
    private TextView tv_tag;
    private ListView areaListView;
    private ListView sortList;
    private String[] areas={"按Mac","按名称","按时间","按频率"};
    private String[] areas_options={"tmc","source","start","show"};
    private String[] sort={"升序","降序"};
    private RadioOnClick OnClick;
    String src="";
    private SortSelect sortSelect;
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.history_of_router);
        SysApplication.getInstance().addActivity(this);
        expandlist=(ExpandableListView)findViewById(R.id.list_history_router);
        tv_tag=(TextView)findViewById(R.id.tv_history_tag) ;
        ArrayList<String> target=new ArrayList<>();
        flag_btn=(Button)findViewById(R.id.flag_btn);
        option_btn=(Button)findViewById(R.id.options_btn);
        button_filter=(Button)findViewById(R.id.button_of_filter) ;
        macTextWatcher=new MacTextWatcher();
        Intent intent=getIntent();
        if(intent!=null){
            String content="";

         /*
         *这一段处理详细信息
         * */
          if(intent.getExtras().getString("detail_coll")!=null
                  &&!intent.getExtras().getString("detail_coll").equals(""))
            {
              src=intent.getExtras().getString("detail_coll");
            if(src.contains(":")&&!src.contains("年")){
                content = "<font color=\"#b80190\">" + src.substring(0,src.indexOf(":")-3)+ "</font>"
                        +"<br>"+"<font color=\"#000000\">"+src.substring(src.indexOf(":")-2,src.indexOf(":")+15)+"</font>";
            }else {content= "<font color=\"#b80190\">"+src+"的全部记录"+"</font>"; }

            Log.d("收到内容是",src);
            tv_tag.setText(Html.fromHtml(content));
           }
        }
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
                       if(classify==0){
                           for(Map<String,Object> map:data){
                               if(map.get("tmc").toString().contains(str)
                                       ||map.get("source").toString().toLowerCase().contains(str.toLowerCase())){
                                  groupList.add(map);
                               }
                           }
                          String content = "<font color=\"#b80190\">" + "查询到含有“"+str+"”的结果"+"</font>"
                                   +"<br>"+"<font color=\"#000000\">"+"一共"+groupList.size()+"条"+"</font>";
                           tv_tag.setText(Html.fromHtml(content));
                       }//上面过滤
                        else if(classify==1){ groupList.addAll(data);}//下面不过滤

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

                            user_.put("group",user_.get("source"));
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
                        MyExpandableListAdapter adapter = new MyExpandableListAdapter(Result_Of_Collision.this,groupList, childList);
                        expandlist.setAdapter(adapter);
                        String content ="";
                       if(classify==0){return; }//如果是过滤情况，直接退出去，下面不执行
                        if(src.contains(":")&&!src.contains("年")){
                            content = "<font color=\"#b80190\">" + src.substring(0,src.indexOf(":")-3)+ "</font>"
                                    +"<br>"+"<font color=\"#000000\">"+src.substring(src.indexOf(":")-2,src.indexOf(":")+15)+"</font>"
                                   +"<font color=\"#08088A\">"+"   一共"+data.size()+"条"+"</font>";
                        }else { content= "<font color=\"#b80190\">"+src+"的全部记录"+"</font>"
                             +"<font color=\"#08088A\">"+"   一共"+data.size()+"条"+"</font>"; }

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
        data=myDBAssistant.getLocalDb_Collision(src,temp,flag_btn.getText().toString());
        handler.sendEmptyMessage(RESULT);
      /*
      * 过滤代码
      * */
        button_filter.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Result_Of_Collision.this);
            builder.setTitle("请输入要过滤的Mac");
            View view = LayoutInflater.from(Result_Of_Collision.this).inflate(R.layout.dialog_filter, null);
            builder.setView(view);
            final EditText editText=(EditText) view.findViewById(R.id.filter_user_mac);
            final RadioGroup ch=(RadioGroup) view.findViewById(R.id.rgroup);
            editText.addTextChangedListener(macTextWatcher);//默认初始为Mac搜索
            ch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(RadioGroup rg,int checkedId)
                {
                    switch(checkedId){
                        case R.id.ch1:editText.setText(""); editText.addTextChangedListener(macTextWatcher); break;
                        case R.id.ch2: editText.setText(""); editText.removeTextChangedListener(macTextWatcher);break;
                    }
                }
            }); builder.setCancelable(false);
            builder.setPositiveButton("确定过滤", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    classify=0;
                    str=editText.getText().toString();
                    handler.sendEmptyMessage(RESULT);
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Result_Of_Collision.this,"过滤成功",Toast.LENGTH_SHORT).show();
                }

            });
            builder.setNegativeButton("取消过滤", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    classify=1;handler.sendEmptyMessage(RESULT);
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.show();
        });

    }

        class RadioClickListener implements View.OnClickListener {

            @Override

            public void onClick(View v) {

                AlertDialog ad =new AlertDialog.Builder(Result_Of_Collision.this).setTitle("选择排序字段"+"   按Mac(默认)")

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
                data=myDBAssistant.getLocalDb_Collision(src,areas_options[index],flag_btn.getText().toString());
                handler.sendEmptyMessage(RESULT);
                dialog.dismiss();

            }

        }


        class SortClickListener implements View.OnClickListener {

            @Override

            public void onClick(View v) {

                AlertDialog ad =new AlertDialog.Builder(Result_Of_Collision.this).setTitle("选择排序方式"+"   升序(默认)")

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

                data= myDBAssistant.getLocalDb_Collision(src,options,sort[index]);
                handler.sendEmptyMessage(RESULT);
                dialog.dismiss();

            }

        }

    class MacTextWatcher implements TextWatcher {
        private  boolean mWasEdited = false;
        private MacTextWatcher(){

        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if(mWasEdited){
                mWasEdited = false;
                return;
            }
            mWasEdited = true;
            String mac = s.toString();
            mac = mac.replace(":","");
            StringBuffer builder = new StringBuffer();
            for(int i=0;i<mac.length()&&i<12;i++){
                if(('a'<=mac.toLowerCase().charAt(i)
                        &&mac.toLowerCase().charAt(i)<='f')
                        || ('0'<=mac.charAt(i)
                        &&mac.charAt(i)<='9')){
                    builder.append(mac.charAt(i));
                }
                if(i%2!=0&&i!=mac.length()-1&&i!=11){
                    builder.append(":");
                }
            }
            s.replace(0,s.length(),builder.toString());Log.d("index","always 0");

        }
    }
}

