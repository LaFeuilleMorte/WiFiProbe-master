package com.example.administrator.myapplication;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.os.Looper;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class Any_kind extends Activity {
    private ArrayList<Map<String,Object>> myRouter=new ArrayList<Map<String,Object>>();
    private ArrayList<Map<String,Object>> myUser=new ArrayList<>();
    private Handler handler;
    private Data_Receiver dataReceiver;
    private ExpandableListView expandlist;
    private ArrayList<Map<String, Object>>groupList = new ArrayList<Map<String,Object>>();
    String[] router_info={"路由名称：","信号强度：","距离：","出现次数：","来源信息：","是否匿名："};
    String[] router_key={"router","rssi","range","show","source","hidden"};//子表中显示路由信息列表
    private Context context=this;
    private MacTextWatcher macTextWatcher;
    private Button button_filter;
    private Handler blt_handler=new Handler();
    private Runnable blt_runable;
    int nums=0;
    private int classify=1;
    private String str="";
    private String [] areas={"按MAC排序","按名称排序","按距离排序"};
    private RadioOnClick OnClick=new RadioOnClick(0);
    private ListView areaListView;
    private Button sort_filter;
    private Button clr_his;
    private Button flag;
    private Button save_his;
    private ListView listView;
    private MyAdapter myAdapter;
    private String my_str="";
    private String[] sort_string={"mac","router","range"};
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);
        setContentView(R.layout.any_kind_layout);
        listView=(ListView)findViewById(R.id.list_any_kind);
        myAdapter=new MyAdapter(this);
        listView.setAdapter(myAdapter);
        macTextWatcher = new MacTextWatcher();
        button_filter = (Button) findViewById(R.id.button_filter);
        sort_filter = (Button) findViewById(R.id.sort_filter_button);
        clr_his=(Button)findViewById(R.id.clr_his_hot);
        sort_filter.setOnClickListener(new RadioClickListener());

      /*save_his=(Button)findViewById(R.id.save_history);
        save_his.setOnClickListener((v)->{
            if(groupList!=null&&groupList.size()!=0){
                try{
                    ArrayList<Map<String,Object>> my_list=new ArrayList<>();
                    String str1="";
                    my_list.addAll(compareList(groupList,"按距离排序","升序"));
                    for(int i=0;i<15;i++){
                        str1=str1+groupList.get(i).get("mac")+" | "+
                        groupList.get(i).get("range")+" | "+groupList.get(i).get("rssi")+"\n";
                    }
                    //save_my_log(str1,nums);
                    nums++;
                }catch (Exception e){}
            }
        });*/

        clr_his.setOnClickListener((v)->{
            Toast.makeText(Any_kind.this,"长按按钮清空列表内容",Toast.LENGTH_SHORT).show();
        });
        clr_his.setOnLongClickListener((v)->{
            AlertDialog.Builder myBuilder=new AlertDialog.Builder(Any_kind.this);
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

        flag = (Button) findViewById(R.id.flag);
        flag.setOnClickListener((v) -> {
            if (flag.getText().toString().equals("升序")) {
                flag.setText("降序");
                handler.sendEmptyMessage(0);
            } else {
                flag.setText("升序");
                handler.sendEmptyMessage(0);
            }
        });
        try {
            myRouter = (ArrayList<Map<String, Object>>) read_info();
            handler.sendEmptyMessage(0);
            Log.d("存储读取", "true_for_now");
        } catch (Exception e) {
            Log.d("cannot", "find saved");
        }
        dataReceiver = new Data_Receiver();
        IntentFilter intentFilter_data = new IntentFilter("data_analysed");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver, intentFilter_data);

/*
* 监测蓝牙状态
* */
      /*  blt_runable=new Runnable() {
            @Override
            public void run() {
                if(SysApplication.globalBlueSocket!=null&&SysApplication.globalBlueSocket.isConnected()){
                    return;//什么也不做
                }else {  Intent intent=new Intent(Any_kind.this,BTClient.class);
                    intent.putExtra("socket","disconnected");
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(),"蓝牙已断开，请重新连接",Toast.LENGTH_SHORT).show();
                }
                Log.d("正在扫描蓝牙状态","yes");
                blt_handler.postDelayed(blt_runable,20*1000);
            }
        };  blt_handler.postDelayed(blt_runable,5*1000);*/

        handler = new Handler() {
            String temp_router;
            int m;

            public void handleMessage(Message msg) {

                Log.d("状态", "已刷新");
                groupList.clear();

                if(msg.what==13){
                    myAdapter.notifyDataSetChanged();
                    myRouter.clear();
                    write_info(groupList);
                    return;
                }

                if (myRouter != null && myRouter.size() != 0) {

                    if (classify == 0) {
                        str=str.replace(" ", "");//去掉空格
                        if (!str.equals("")) {

                            for (int n = 0; n < myRouter.size(); n++) {
                                for (m = 0; m < groupList.size(); m++) {
                                    if (myRouter.get(n).get("mac") != null && myRouter.get(n).get("mac").equals(groupList.get(m).get("mac"))) {
                                        //如果列表中已有该项信息，则只是刷新该信息，而不添加新的条目
                                        for (int i = 0; i < router_key.length; i++) {
                                            groupList.get(m).put(router_key[i], myRouter.get(n).get(router_key[i]));
                                        }

                                        int count=0;

                                        if(myUser.size()!=0){

                                             for(Map<String,Object> mapUser:myUser){
                                                 if(mapUser.get("mac").toString().equals(groupList.get(m).get("mac"))){
                                                 count++;
                                            }
                                          }
                                        }

                                        groupList.get(m).put("linked",count);
                                        break;
                                    }
                                }
                                if (m == groupList.size()) {
                                    if (myRouter.get(n).get("mac").toString().contains(str)
                                          || myRouter.get(n).get("router").toString()
                                            .toLowerCase() //统一小写
                                            .contains(str.toLowerCase())) {
                                        groupList.add(myRouter.get(n));
                                        int count=0;

                                        if(myUser!=null&&myUser.size()!=0){
                                      try{
                                        for(Map<String,Object> mapUser:myUser){
                                            if(mapUser.get("mac").toString().equals(groupList.get(m).get("mac"))){
                                                count++;
                                            }
                                        }
                                                   }catch (Exception e){}
                                      }

                                        groupList.get(m).put("linked",count);

                                    }
                                }
                            }
                        }
                    }
                    //如果不过滤
                    if (classify == 1) {
                        for (int n = 0; n < myRouter.size(); n++) {
                            for (m = 0; m < groupList.size(); m++) {
                                if (myRouter.get(n).get("mac") != null && myRouter.get(n).get("mac").equals(groupList.get(m).get("mac"))) {
                                    //如果列表中已有该项信息，则只是刷新该信息，而不添加新的条目
                                    for (int i = 0; i < router_key.length; i++) {
                                        groupList.get(m).put(router_key[i], myRouter.get(n).get(router_key[i]));
                                    }

                                    int count=0;
                                 try{  if(myUser!=null&&myUser.size()!=0){
                                    for(Map<String,Object> mapUser:myUser){
                                        if(mapUser.get("mac").toString().equals(groupList.get(m).get("mac"))){
                                            count++;
                                        }
                                    }
                                   }
                                 }catch (Exception e){}
                                    groupList.get(m).put("linked",count);

                                    break;
                                }
                            }
                            if (m == groupList.size()) {

                                groupList.add(myRouter.get(n));//size增加1
                                int count=0;
                               if(myUser!=null&&myUser.size()!=0){
                               try{
                                for(Map<String,Object> mapUser:myUser){
                                    if(mapUser.get("mac").toString().equals(groupList.get(m).get("mac"))){
                                        count++;
                                    }
                                }      }catch (Exception e){}//此处有异常
                               }
                                groupList.get(m).put("linked",count);

                            }
                        }
                    }
                }//上面的代码对多次接收到的相同的信息进行去重
                groupList = compareList(groupList, sort_filter.getText().toString(), flag.getText().toString());//排序
                SharedPreferences read = getSharedPreferences("mac_labeled", MODE_PRIVATE);
                for(Map<String,Object> map:groupList){
                    map.put("flag","0");//加入一个判断它是否是收藏的项
                    if(read.contains(map.get("mac").toString())){
                        map.put("flag","1");Log.d("是收藏项",map.get("mac").toString());
                    }else {map.put("flag","0");  }
                }
                myAdapter.notifyDataSetChanged();Log.d("列表已更新","confirmed");//通知数值发生变化
            }
        };

        button_filter.setOnClickListener((v) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(Any_kind.this);
                builder.setTitle("请输入要过滤的Mac");
                View view = LayoutInflater.from(Any_kind.this).inflate(R.layout.dialog_filter, null);
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
                        handler.sendEmptyMessage(0);
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(Any_kind.this,"过滤成功",Toast.LENGTH_SHORT).show();
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
        @Override
        public void onReceive(Context context, Intent intent){
            if("data_analysed".equals(intent.getAction())){
                            //  handler_read.getLooper().quit();//只要收到广播就不再读取了
                            if(intent.getExtras()!=null){
                                try{
                                    myRouter=(ArrayList<Map<String,Object>>)intent.getSerializableExtra("extra_data_router");
                                    myUser=(ArrayList<Map<String, Object>>)intent.getSerializableExtra("extra_data_user");
                                    if(myRouter!=null&&myRouter.size()!=0) {
                                        Log.d("已收到","准备刷新");
                                      handler.sendEmptyMessage(1);
                                    }

                    }catch (ClassCastException e){
                        Log.d("错误:","类型转化错误");}
                }
            }
        }
    }
    //排序算法
    private ArrayList<Map<String,Object>> compareList(ArrayList<Map<String,Object>> result_list,String sort_filter_string,String flag){

         Collections.sort(result_list, new Comparator<Map<String, Object>>() {
             @Override
             public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                 String temp="";
             for(int i=0;i<3;i++){
                 if(sort_filter_string.equals(areas[i])){temp=sort_string[i];}
             }
                 String str1="";
                 String str2="";
               if(map1.get(temp)!=null && map2.get(temp)!=null){
                if(temp.equals("range")){
                    if(flag.equals("升序")){
                        return   num_compare(map1.get(temp).toString(),map2.get(temp).toString()); }
                    if(flag.equals("降序")){
                        return   num_compare(map2.get(temp).toString(),map1.get(temp).toString());
                    }
                }
               else{
                    if(flag.equals("升序")){
                      str1=map1.get(temp).toString();
                       str2=map2.get(temp).toString();
                     }
                    if(flag.equals("降序")){
                       str1=map2.get(temp).toString();
                        str2=map1.get(temp).toString();
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

    private  ArrayList<Map<String,Object>> read_info() {

        //要读出多个ArrayList，所以用一个数组来存储它
        ArrayList<Map<String,Object>> _router=new ArrayList<Map<String,Object>>();

            try{
                ObjectInputStream in_2= new ObjectInputStream(getApplicationContext().openFileInput("router_info"));
                _router=( ArrayList<Map<String,Object>>)in_2.readObject();
                in_2.close();
            }
            catch (FileNotFoundException e){e.printStackTrace();}
            catch (IOException e){e.printStackTrace();}catch (ClassNotFoundException e){e.printStackTrace();}
        return _router;
    }

    private void write_info(ArrayList<Map<String,Object>> router_info) {

        try{
            ObjectOutputStream out_2= new ObjectOutputStream(getApplicationContext().openFileOutput("router_info",MODE_PRIVATE));
            out_2.writeObject(router_info);
            out_2.close();
        }
        catch (FileNotFoundException ee){ee.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}

    }
 @Override
      public void onStart(){
        super.onStart();
     try{
         myRouter=(ArrayList<Map<String,Object>>) read_info();handler.sendEmptyMessage(1); Log.d("存储读取","true_for_now_start");}catch (Exception e){Log.d("cannot","find saved");}
 }

    @Override
    public void onStop(){
        super.onStop();
        if(groupList.size()!=0){
        write_info(groupList);}
        Log.d("stop","stop invoked");
        // this.unregisterReceiver(dataReceiver);
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

            AlertDialog ad =new AlertDialog.Builder(Any_kind.this).setTitle("选择排序方式"+"   按MAC(默认)")

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
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.any_kind_list,null);
                holder=new ViewHolder();
                holder.my_stars=(RatingBar) convertView.findViewById(R.id.my_stars);
                holder.ylfn_did_view = (TextView)convertView.findViewById(R.id.text_left);
                holder.ylfn_second=(TextView)convertView.findViewById(R.id.text_right);
                holder.tv_mid=(TextView)convertView.findViewById(R.id.text_mid);
                convertView.setTag(holder);
            }else { holder=(ViewHolder)convertView.getTag();}

            String content = "<font color=\"#b80190\">" + groupList.get(position).get("router").toString()+ "</font>"
                    +"<br>"+"<font color=\"#000000\">"+groupList.get(position).get("mac").toString()+"</font>";

            holder.ylfn_did_view.setText(Html.fromHtml(content));

            if(groupList.get(position).get("linked")!=null){
            str2=groupList.get(position).get("linked").toString();}

            float num=0;

            //参数修正
            //num=Float.parseFloat(groupList.get(position).get("range").toString());
          //  if(num<=50&&num>=30)
            //{num*=1.5; }else if(num>50){num*=2; }
           // str3=num+"米";

            str3=groupList.get(position).get("range").toString()+"米";

            String content_mid="<font color=\"#b80190\">" + "距离: "+ "</font>"
                    +"<br>"+"<font color=\"#000000\">"+str3+"</font>";
            String content_right = "<font color=\"#b80190\">" + "终端数量: "+ "</font>"
                    +"<br>"+"<font color=\"#000000\">"+str2+"</font>";

            holder.tv_mid.setText(Html.fromHtml(content_mid));
            holder.ylfn_second.setText(Html.fromHtml(content_right));

            holder.ylfn_did_view.setOnClickListener((v)->{
                AlertDialog.Builder ab=new AlertDialog.Builder(Any_kind.this);
                ab.setTitle("确定进入监听模式？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent(Any_kind.this,Router_Track.class);
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
            Intent intent=new Intent(Any_kind.this,BTClient.class);
            intent.putExtra("socket","connected");
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

   /* public void save_my_log(String str1,int nums){

        FileOutputStream fos1;
        File file1 = new File(getExternalFilesDir("my_log_res"), "/my_log_group.txt");
        try {
            fos1 = new FileOutputStream(file1);
            my_str=my_str+"第"+nums+"组：\n"+str1;
            fos1.write(my_str.getBytes());
            Log.d("写入成功","确认");
            fos1.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }*/

}



