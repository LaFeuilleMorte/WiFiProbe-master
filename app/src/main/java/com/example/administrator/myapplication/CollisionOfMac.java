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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CollisionOfMac extends Activity {
    private ArrayList<String> date_list=new ArrayList<>();
    private ArrayList<String> adr_list=new ArrayList<>();
    private ArrayList<String> hotspot=new ArrayList<>();
    private MyDBAssistant myDBAssistant;
    private Map<String,String> my_info=new HashMap<>();
    private ArrayList<String> my_info_list=new ArrayList<>();
    ArrayList<String>  my_target=new ArrayList<>();
    private MyAdapter myAdapter;
    private ListView listView;
    private Handler handler;
    private RadioButton rb1,rb2,rb3;
    private RadioGroup radioGroup;
    private Button cacul;
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.collision_of_mac_layout);
        listView = (ListView) findViewById(R.id.list_collector_mac);
        radioGroup = (RadioGroup) findViewById(R.id.rgroup_coll);
        rb1=(RadioButton)findViewById(R.id.ch1_coll);
        rb2=(RadioButton)findViewById(R.id.ch2_coll);
        cacul=(Button)findViewById(R.id.cacul);
        cacul.setEnabled(false);
        myDBAssistant = new MyDBAssistant(this);
        myAdapter = new MyAdapter(this);
        listView.setAdapter(myAdapter);

        cacul.setOnClickListener((v)->{
            if(my_target.size()!=0){
                Intent intent=new Intent(CollisionOfMac.this,Result_Of_Cacul.class);
                intent.putExtra("target_cal",(Serializable)my_target);
                startActivity(intent);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.ch1_coll:
                        my_info = myDBAssistant.getDateGroup();
                        handler.sendEmptyMessage(0); break;
                    case R.id.ch2_coll:
                        my_info=myDBAssistant.getHotspotGroup();
                        handler.sendEmptyMessage(0); break;
              }
          }
        });
   handler=new Handler(){
       public void handleMessage(Message msg){
           my_info_list.clear();
           Set<String> info_set=my_info.keySet();
           for(String key:info_set){
              // String content = "<font color=\"#b80190\">" +key+ "</font>"
                //       +"<br>"+"<font color=\"#000000\">"+my_info.get(key)+"条记录"+"</font>";
               my_info_list.add(key+"\n"+my_info.get(key));
           }
       myAdapter.notifyDataSetChanged();
       }
   };   my_info=myDBAssistant.getDateGroup();handler.sendEmptyMessage(0);
}
    class MyAdapter extends BaseAdapter {
        Activity activity;
        Map<Integer,Boolean> map_of_check=new HashMap<>();
        public MyAdapter(Activity a) {
            activity = a;
        }

        // 设置每一页的长度，默认的是View_Count的值。
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            // return data.length;

            return my_info_list.size();
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
            ViewHolder holder;
            if(convertView==null){
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.collision_of_mac,null);
                holder=new ViewHolder();
                holder.tv_coll=(TextView) convertView.findViewById(R.id.text_coll);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.add_ch);
                convertView.setTag(holder);
            }else { holder=(ViewHolder)convertView.getTag();}

            String temp=my_info_list.get(position);
            String str1="";
            String str2="";
            String str3="";
            String content="";
            if(rb1.isChecked()){
                str1=temp.substring(0,temp.indexOf("\n"));
                str2=temp.substring(temp.indexOf("\n")+1,temp.length());
                 content = "<font color=\"#b80190\">" + str1+ "</font>"
                    +"<br>"+"<font color=\"#6959CD\">"+str2+"条数据记录"+"</font>"; }
             if(rb2.isChecked()){
                str1=temp.substring(0,temp.indexOf(":")-2);
                str2=temp.substring(temp.indexOf(":")-2,temp.indexOf("\n"));
                str3=temp.substring(temp.indexOf("\n"),temp.length());
                content="<font color=\"#b80190\">" + str1+ "</font>"
                        +"<br>"+"<font color=\"#0000CD\">" + str2+ "</font>"
                 +"<br>"+ "<font color=\"#6959CD\">"+str3+"条数据记录"+"</font>";
             }
             holder.tv_coll.setText(Html.fromHtml(content));
            holder.tv_coll.setOnClickListener((v)->{
                AlertDialog.Builder ab=new AlertDialog.Builder(CollisionOfMac.this);
                ab.setTitle("查看详细信息？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(CollisionOfMac.this,Result_Of_Collision.class);
                        String temp=holder.tv_coll.getText().toString();
                        if(rb1.isChecked()){
                        intent.putExtra("detail_coll",temp.substring(0,temp.indexOf("\n")));}
                        if(rb2.isChecked()){
                            intent.putExtra("detail_coll",temp.substring(0,temp.indexOf(":")+15));
                        }
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
            /*
            * 下面来处理交集运算
            * */
           holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   if(isChecked){
                       map_of_check.put(position,true);
                       my_target.add(holder.tv_coll.getText().toString());
                   }else {
                       map_of_check.remove(position);
                       my_target.remove(holder.tv_coll.getText().toString()); }
                   Log.d("计算目标是",my_target.toString());

                   if(myAdapter.map_of_check.size()<2){
                       cacul.setEnabled(false);cacul.setText("最低两项");
                   }else{
                       cacul.setText("选择"+myAdapter.map_of_check.size()+"项");
                       cacul.setEnabled(true);
                   }
               }
           });
            if(map_of_check!=null&&map_of_check.containsKey(position)){
                holder.checkBox.setChecked(true);
            }else {
                holder.checkBox.setChecked(false);
            }
            return convertView;
        }
    }
    static  class ViewHolder{
        CheckBox checkBox;
        TextView tv_coll;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK ){
            Intent intent=new Intent(CollisionOfMac.this,BTClient.class);
            intent.putExtra("socket","connected");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

