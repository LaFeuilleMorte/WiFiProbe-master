package com.example.administrator.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CollectorOfMac extends Activity {
    TextView tv_tag;
    Button button_switch;
    Button button_remove;
    ListView listView;
    MyAdapter myAdapter;
    ArrayList list=new ArrayList();
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.collector_of_mac_layout);
        SysApplication.getInstance().addActivity(this);
        tv_tag=(TextView)findViewById(R.id.tv_collector_tags);
        button_switch=(Button)findViewById(R.id.button_switch);
        button_remove=(Button)findViewById(R.id.button_delete_mac);
        listView=(ListView)findViewById(R.id.list_collector_mac);
        myAdapter=new MyAdapter(this);
        listView.setAdapter(myAdapter);

        SharedPreferences read_ = getSharedPreferences("mac_labeled", MODE_PRIVATE);
        Map<String,?> map_all_=read_.getAll();
        Set<String> set__=map_all_.keySet();
        for(String key:set__){
            String temp=key+"\n"+map_all_.get(key).toString(); Log.d("key是",map_all_.get(key).toString());
            list.add(temp);
        }
        button_switch.setOnClickListener((v)->{
            if(tv_tag.getText().toString().equals("收藏的热点")){
                tv_tag.setText("收藏的终端");
                SharedPreferences read = getSharedPreferences("user_mac_labeled", MODE_PRIVATE);
               list.clear();
               Map<String,?> map_all=read.getAll();
                Set<String> set=map_all.keySet();
               for(String key:set){
                   String temp=key+"\n"+map_all.get(key).toString();// Log.d("key是",map_all.get(key).toString());
                   list.add(temp);
               }
               myAdapter.notifyDataSetChanged();
            }else {
                tv_tag.setText("收藏的热点");
                SharedPreferences read = getSharedPreferences("mac_labeled", MODE_PRIVATE);
                list.clear();
                Map<String,?> map_all=read.getAll();
                Set<String> set=map_all.keySet();
                for(String key:set){
                    String temp=key+"\n"+map_all.get(key).toString();// Log.d("key是",map_all.get(key).toString());
                    list.add(temp);
                }
                myAdapter.notifyDataSetChanged();
            }
        });
        button_remove.setEnabled(false);
        button_remove.setOnClickListener((v)->{
            SharedPreferences.Editor editor_user = getSharedPreferences("user_mac_labeled", MODE_PRIVATE).edit();
            SharedPreferences.Editor editor_router = getSharedPreferences("mac_labeled", MODE_PRIVATE).edit();
          Set<Integer> set=myAdapter.my_map.keySet();
          for(Integer position:set){
           try{
               if(tv_tag.getText().toString().equals("收藏的终端")){
               String labeled = list.get(position.intValue()).toString();
               labeled=labeled.substring(0,labeled.indexOf("\n")); Log.d("user_label是",labeled);
               editor_user.remove(labeled);
               Log.d("去掉了", "是的");
               editor_user.commit();
               Log.d("剩下",list.size()+""); list.remove(position.intValue());}

               if(tv_tag.getText().toString().equals("收藏的热点")){
                   String labeled = list.get(position.intValue()).toString();
                   labeled=labeled.substring(0,labeled.indexOf("\n")); Log.d("label是",labeled);
                   editor_router.remove(labeled);
                   Log.d("去掉了", "是的");
                   editor_router.commit();
                   Log.d("剩下",list.size()+""); list.remove(position.intValue());}

           }catch (Exception e){}

          }
            myAdapter.my_map.clear();
            myAdapter.notifyDataSetChanged();
      });
    }

    class MyAdapter extends BaseAdapter {
        Activity activity;
        public Map<Integer,Boolean> my_map=new HashMap<>();
        public MyAdapter(Activity a) {
            activity = a;
        }

        // 设置每一页的长度，默认的是View_Count的值。
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            // return data.length;

            return   list.size();
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
             ViewHolder holder;
            if(convertView==null){
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.collector_of_mac_item,null);
                holder=new ViewHolder();
                holder.my_stars=(RatingBar) convertView.findViewById(R.id.collector_stars);
                holder.myMac= (TextView)convertView.findViewById(R.id.tv_collector_left);
                holder.myBox=(CheckBox)convertView.findViewById(R.id.box1);
                convertView.setTag(holder);
            }else { holder=(ViewHolder)convertView.getTag();}
            holder.myMac.setText(list.get(position).toString());
            holder.myMac.setOnClickListener((v)->{
                AlertDialog.Builder builder=new AlertDialog.Builder(CollectorOfMac.this);
                builder.setPositiveButton("历史信息", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if(tv_tag.getText().toString().equals("收藏的终端")){
                        Intent intent =new Intent(CollectorOfMac.this,DetailedInfo.class);//到用户数据库里搜索
                        intent.putExtra("mac_target",holder.myMac.getText().toString());
                        startActivity(intent);
                        dialog.dismiss();  }
                        else {
                          Intent intent=new Intent(CollectorOfMac.this,History_Of_Router.class);
                        intent.putExtra("router_localDB",holder.myMac.getText().toString());
                        startActivity(intent);
                        dialog.dismiss();
                       }
                    }
                }).setNegativeButton("实时监测", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(SysApplication.globalBlueSocket!=null&&SysApplication.globalBlueSocket.isConnected()){
                                if(tv_tag.getText().toString().equals("收藏的热点")){
                                Intent intent=new Intent(CollectorOfMac.this,Router_Track.class);
                                intent.putExtra("router_mac_target",holder.myMac.getText().toString());
                                 startActivity(intent);dialog.dismiss();
                           }else{
                                    Intent intent=new Intent(CollectorOfMac.this,Tracking_Mac.class);
                                    intent.putExtra("user_mac_target",holder.myMac.getText().toString());
                                    startActivity(intent);dialog.dismiss();
                                }

                        }
                        else{
                                Toast.makeText(CollectorOfMac.this,"请先连接蓝牙",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                    }
                }).show();

            });
            holder.myBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked==true){
                        my_map.put(position,true);
                    }else {
                        my_map.remove(position);
                    }

                    if(myAdapter.my_map.size()==0){
                        button_remove.setEnabled(false);
                    }else{
                        button_remove.setText("移除选中的"+myAdapter.my_map.size()+"项");
                        button_remove.setEnabled(true);
                    }
                }
            });
            if(my_map!=null&&my_map.containsKey(position)){
                holder.myBox.setChecked(true);
            }else {
                holder.myBox.setChecked(false);
            }

            return convertView;
        }
    }
    static  class ViewHolder{
        RatingBar my_stars;
        TextView myMac;
        CheckBox myBox;
    }
}
