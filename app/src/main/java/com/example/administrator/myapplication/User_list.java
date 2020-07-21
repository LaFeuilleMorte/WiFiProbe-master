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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class User_list extends Activity {

        private ArrayList<Map<String,Object>> myUser=new ArrayList<>();
        private Handler handler;
        private ExpandableListView expandlist;
        private ArrayList<Map<String, Object>>groupList = new ArrayList<Map<String,Object>>();
        private Data_Receiver dataReceiver;
        private SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
        private String[] user_info={"设备正在连接的路由Mac：","设备连接路由器名称：","信号强度：","距离：","第一次探测到时间：","最近探测到时间：","累计在线时长：","手机或电脑厂商：","出现次数："};
        private String[] user_key={"mac","router","rssi","range","start","latest","duration","source","show"};//子表中显示路由信息列表
       private MacTextWatcher macTextWatcher;
       private Button button_filter;
       private Handler blt_handler=new Handler();
       private Runnable blt_runable;
       private int classify=1;
       private String [] areas={"按名称排序","按MAC排序","按时刻排序","按距离排序"};
       private RadioOnClick OnClick=new RadioOnClick(0);
       private ListView areaListView;
       private ListView listView;
       private Button sort_filter;
       private Button clr_his;
       private MyAdapter myAdapter;
       private Button flag;
       private String str="";
       private String[] sort_string={"source","tmc","start","range"};
        Context context=User_list.this;
    @SuppressLint("HandlerLeak")
        @Override
       public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            SysApplication.getInstance().addActivity(this);
            setContentView(R.layout.uer_list_layout);
            macTextWatcher=new MacTextWatcher();
         button_filter=(Button)findViewById(R.id.button_filter_user);
        sort_filter=(Button)findViewById(R.id.sort_filter_button_user);
        clr_his=(Button)findViewById(R.id.clr_his);
        clr_his.setOnClickListener((v)->{
            Toast.makeText(User_list.this,"长按按钮清空列表内容",Toast.LENGTH_SHORT).show();
        });
        clr_his.setOnLongClickListener((v)->{
            AlertDialog.Builder myBuilder=new AlertDialog.Builder(User_list.this);
            myBuilder.setTitle("确定清空列表，清空后无法恢复?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.sendEmptyMessage(13);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int which){
                                dialog.dismiss();
                            }
                    }).show();
            return  true;
        });
        sort_filter.setOnClickListener(new RadioClickListener());
        flag=(Button)findViewById(R.id.flag_user);
        listView=(ListView)findViewById(R.id.list_user_list);
        myAdapter=new MyAdapter(this);
        listView.setAdapter(myAdapter);
        flag.setOnClickListener((v)->{
            if(flag.getText().toString().equals("升序")){
                flag.setText("降序");handler.sendEmptyMessage(0);
            }else {
                flag.setText("升序");handler.sendEmptyMessage(0);
            }
        });
            try{
                myUser=(ArrayList<Map<String,Object>>) read_info(); handler.sendEmptyMessage(1);Log.d("存储读取","true_for_now");}catch (Exception e){Log.d("cannot","find saved");}


            dataReceiver = new Data_Receiver();
            IntentFilter intentFilter_data = new IntentFilter("data_analysed");
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);




            handler = new Handler() {
                String temp_user;
                int m;

                public void handleMessage(Message msg) {

                    groupList.clear();
                    Log.d("状态用户", "已刷新");
                    //路由信息列表
                if(msg.what==13){
                    myAdapter.notifyDataSetChanged();Log.d("list_group","已清空");
                    myUser.clear();
                    write_info(groupList);
                    return;
                }//清空列表

                  if (myUser != null && myUser.size() != 0) {


                      if(classify==0){

                          str=str.replace(" ","");//去掉空格
                          if(!str.equals("")){
                        for (int n = 0; n < myUser.size(); n++) {
                            for (m = 0; m < groupList.size(); m++) {
                                if (myUser.get(n).get("tmc")!=null&&myUser.get(n).get("tmc").equals(groupList.get(m).get("tmc"))) {
                                   //如果列表中已有该项信息，则只是刷新该信息，而不添加新的条目
                                   for(int i=0;i<user_key.length;i++){
                                       if(i!=7&&i!=6){ //source和duration不刷新
                                       groupList.get(m).put(user_key[i],myUser.get(n).get(user_key[i]));}
                                   }
                                    break;
                                }
                            }
                              if (m == groupList.size()) {
                                      if(myUser.get(n).get("tmc").toString().contains(str)
                                              ||myUser.get(n).get("source").toString()
                                              .toLowerCase()
                                              .contains(str.toLowerCase())){
                                      groupList.add(myUser.get(n));
                                      groupList.get(groupList.size()-1).put("duration","0时00分00秒");
                                   }
                               }
                             }
                          }
                      }//过滤

                      if(classify==1){
                          for (int n = 0; n < myUser.size(); n++) {
                              for (m = 0; m < groupList.size(); m++) {
                                  if (myUser.get(n).get("tmc")!=null&&myUser.get(n).get("tmc").equals(groupList.get(m).get("tmc"))) {
                                      //如果列表中已有该项信息，则只是刷新该信息，而不添加新的条目
                                      for(int i=0;i<user_key.length;i++){
                                          if(i!=7&&i!=6){
                                              groupList.get(m).put(user_key[i],myUser.get(n).get(user_key[i]));}
                                      }
                                      break;
                                  }
                              }
                              if (m == groupList.size()) {
                                  groupList.add(myUser.get(n));
                                  groupList.get(groupList.size()-1).put("duration","0时00分00秒");
                              }
                          }
                      }//不过滤
                    } //上面的代码对多次接收到的相同的信息进行去重
                    //给每组的子元素赋值
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
                      }
                      //计算时差并放入数组
                    groupList=compareList(groupList,sort_filter.getText().toString(),flag.getText().toString());//排序算法
                    SharedPreferences read = getSharedPreferences("user_mac_labeled", MODE_PRIVATE);

                    for(Map<String,Object> map:groupList){
                        map.put("flag","0");//加入一个判断它是否是收藏的项
                        if(read.contains(map.get("tmc").toString())){
                            map.put("flag","1");Log.d("是收藏项",map.get("tmc").toString());
                        }else {map.put("flag","0");  }
                    }
                    myAdapter.notifyDataSetChanged();
            }
        };
        button_filter.setOnClickListener((v)->{

            AlertDialog.Builder builder = new AlertDialog.Builder(User_list.this);
            builder.setTitle("请输入要过滤的Mac");
            View view = LayoutInflater.from(User_list.this).inflate(R.layout.dialog_filter, null);
            builder.setView(view);
            final EditText editText=(EditText) view.findViewById(R.id.filter_user_mac);
             editText.addTextChangedListener(macTextWatcher);//默认初始为Mac搜索
            final RadioGroup ch=(RadioGroup) view.findViewById(R.id.rgroup);

            ch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(RadioGroup rg,int checkedId)
                {
                    switch(checkedId){
                        case R.id.ch1:editText.setText("");  editText.addTextChangedListener(macTextWatcher); break;
                        case R.id.ch2: editText.setText(""); editText.removeTextChangedListener(macTextWatcher);break;
                    }
                }
            });  builder.setCancelable(false);
            builder.setPositiveButton("确定过滤", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    classify=0;
                    str=editText.getText().toString();
                    handler.sendEmptyMessage(0);

                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(User_list.this,"过滤成功",Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("取消过滤", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    classify=1;handler.sendEmptyMessage(0);

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

        //注册监听广播
        private class Data_Receiver extends BroadcastReceiver {
            boolean first=true;
            @Override
            public void onReceive(Context context, Intent intent){
                if("data_analysed".equals(intent.getAction())){
                    if(intent.getExtras()!=null){
                        try{
                          myUser=(ArrayList<Map<String,Object>>)intent.getSerializableExtra("extra_data_user");
                            if(myUser!=null&&myUser.size()!=0) {
                                if(first){handler.sendEmptyMessage(1);}
                                 else{handler.sendEmptyMessageDelayed(1,2*1000);Log.d("收到","刷新");}
                                 first=false;
                            }
                        }catch (ClassCastException e){
                            Log.d("错误:","类型转化错误");}

                    }
                }
            }
        }

    private  ArrayList<Map<String,Object>> read_info() {

        //要读出多个ArrayList，所以用一个数组来存储它
       ArrayList<Map<String,Object>> user_=new ArrayList<Map<String,Object>>();

        try{
            ObjectInputStream in_1= new ObjectInputStream(getApplicationContext().openFileInput("user_info"));

            user_=( ArrayList<Map<String,Object>>)in_1.readObject();
            in_1.close();
        }
        catch (FileNotFoundException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}catch (ClassNotFoundException e){e.printStackTrace();}
        return user_;
    }
    private void write_info(ArrayList<Map<String,Object>> user_info_temp) {

        try{
            ObjectOutputStream out_2= new ObjectOutputStream(getApplicationContext().openFileOutput("user_info",MODE_PRIVATE));
            out_2.writeObject(user_info_temp);
            out_2.close();
        }
        catch (FileNotFoundException ee){ee.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}

    }

    private ArrayList<Map<String,Object>> compareList(ArrayList<Map<String,Object>> result_list,String sort_filter_string,String flag){

        Collections.sort(result_list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                String temp="";
                for(int i=0;i<4;i++){
                    if(sort_filter_string.equals(areas[i])){temp=sort_string[i];}
                }
                String str1="";
                String str2="";
                if(map1.get(temp)!=null && map2.get(temp)!=null){

                    if(temp.equals("range")){
                        if(flag.equals("升序")){
                           return   num_compare(map1.get(temp).toString(),map2.get(temp).toString());
                        }
                        if(flag.equals("降序")){
                            return  num_compare(map2.get(temp).toString(),map1.get(temp).toString());
                            //str1=map2.get(temp).toString().replace(".","");
                           // str2=map1.get(temp).toString().replace(".","");
                        }
                    }
                 else {
                        if (flag.equals("升序")) {
                            str1 = map1.get(temp).toString();
                            str2 = map2.get(temp).toString();
                        }
                        if (flag.equals("降序")) {
                            str1 = map2.get(temp).toString();
                            str2 = map1.get(temp).toString();
                        }
                    }
                }
                return str1.compareToIgnoreCase(str2);
            }
        });
        return result_list;
    }

   private  int num_compare(String num1,String num2){
        double dg1=Double.parseDouble(num1);
        double dg2=Double.parseDouble(num2);
        int result=0;
        if(dg1>dg2){
            result=1;
        }else if(dg1==dg2){
            result=0;
        }else  if(dg1<dg2){result=-1;}
        return result;
    }
    @Override
    public void onStart(){
        super.onStart();
        try{
        myUser=(ArrayList<Map<String,Object>>) read_info();handler.sendEmptyMessage(1);}catch (Exception e){Log.d("cannot","find saved");}
    }
    @Override
    public void onStop(){
        super.onStop();
       if(groupList!=null&& groupList.size()!=0){
        write_info(groupList);   }
        Log.d("Stop","stoped invoked"); // this.unregisterReceiver(dataReceiver);
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

    class RadioClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            AlertDialog ad =new AlertDialog.Builder(User_list.this).setTitle("选择排序方式"+"   按名称(默认)")

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
            sort_filter.setText(areas[index]);
            handler.sendEmptyMessage(0);
            dialog.dismiss();

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

            return  groupList.size();
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
            String str3="";
            ViewHolder holder;
            if(convertView==null){
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.user_list_item,null);
                holder=new ViewHolder();
                holder.my_stars=(RatingBar) convertView.findViewById(R.id.my_stars_user);
                holder.ylfn_did_view = (TextView)convertView.findViewById(R.id.text_left_user);
                holder.ylfn_second=(TextView)convertView.findViewById(R.id.text_right_user);
                holder.tv_mid=(TextView)convertView.findViewById(R.id.text_mid_user);
                convertView.setTag(holder);
            }else { holder=(ViewHolder)convertView.getTag();}
         if(groupList.get(position).get("source")!=null&& !groupList.get(position).get("source").toString().equals("")){
            str1=groupList.get(position).get("source").toString(); }
            String content = "<font color=\"#b80190\">" + str1+ "</font>"
                    +"<br>"+"<font color=\"#000000\">"+groupList.get(position).get("tmc").toString()+"</font>";
            holder.ylfn_did_view.setText(Html.fromHtml(content));

            if(groupList.get(position).get("router")!=null){
                str2=groupList.get(position).get("router").toString();
            }
            float num=0;
           // num=Float.parseFloat(groupList.get(position).get("range").toString());
           // if(num<=50&&num>=30)
            //{num*=1.5; } else if(num>50){num*=2; }
           // str3=num+"米";
            str3=groupList.get(position).get("range").toString()+"米";
            String content_mid="<font color=\"#b80190\">" + "距离: "+ "</font>"
                    +"<br>"+"<font color=\"#000000\">"+str3+"</font>";

            String content_second = "<font color=\"#b80190\">" + str2+ "</font>"
                    +"<br>"+"<font color=\"#000000\">"+groupList.get(position).get("mac").toString()+"</font>";

            holder.ylfn_second.setText(Html.fromHtml(content_second));
            holder.tv_mid.setText(Html.fromHtml(content_mid));
            holder.ylfn_did_view.setOnClickListener((v)->{

                        AlertDialog.Builder ab=new AlertDialog.Builder(User_list.this);
                        ab.setTitle("确定进入监听模式？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(User_list.this,Tracking_Mac.class);
                                intent.putExtra("track",(Serializable) groupList.get(position));
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

            });
            if(groupList.get(position).get("flag").toString().equals("0")){
                holder.my_stars.setRating(0); Log.d("星星数量","0");
            }if(groupList.get(position).get("flag").toString().equals("1")){
                holder.my_stars.setRating(1);Log.d("星星数量","1");
            }

            return convertView;
        }
    }
    static  class ViewHolder{
        RatingBar my_stars;
        TextView ylfn_did_view;
        TextView ylfn_second;
        TextView tv_mid;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent=new Intent(User_list.this,BTClient.class);
            intent.putExtra("socket","connected");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
