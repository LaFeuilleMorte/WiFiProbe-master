package com.example.administrator.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailedInfo extends Activity {
    ArrayList<Map<String,Object>> result_detail=new ArrayList<Map<String, Object>>();
     MyDBAssistant myDBAssistant=new MyDBAssistant(this);
    private String[] user_key={"tmc","mac","source","router","start","latest","show","adr"};
    private String[] userInfo={"连接路由Mac:","连接路由名称:","信号强度:","参考距离:","上线时间:",
            "下线时间:","在线时间:","出现次数:",""};
    private ArrayList<Map<String, Object>> listItem;
    private String[] areas = new String[]{"连接过的路由","连接时间", "地址信息"};
    private ListView list_ylfn;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
    MyAdapter myAdapter;
    Button btnPre, btnNext,button_classify;
    View.OnClickListener clickListener;
    TextView tv_result;
    TextView tv_left;
    TextView tv_right;
    private ListView areaListView;
    RadioOnClick OnClick=new RadioOnClick(0);
    // 用于显示每列5个Item项。
    int VIEW_COUNT = 10;

    // 用于显示页号的索引
    int index = 0;
    int classify=0;
    public  void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.list_ylfn);
        SysApplication.getInstance().addActivity(this);

        list_ylfn = (ListView) findViewById(R.id.listYlfn);
        btnPre = (Button) findViewById(R.id.btnPre);
        btnNext = (Button) findViewById(R.id.btnNext);
        button_classify=(Button)findViewById(R.id.button_classify);
        tv_result=(TextView)findViewById(R.id.title);
        tv_left=(TextView)findViewById(R.id.tv_left);
        tv_right=(TextView)findViewById(R.id.tv_right);
        listItem = new ArrayList<Map<String, Object>>();
        Intent intent=getIntent();
        String str="";
        if(intent.getExtras().getString("group_info")!=null){
          str=intent.getExtras().getString("group_info");}

        if(intent.getExtras().getString("mac_target")!=null){
            String str_temp=intent.getExtras().getString("mac_target");
            str=str_temp.substring(0,17);
            Map<String,Object> map =new HashMap<String, Object>();
            map.put("tmc",str_temp.substring(0,17));
            map.put("mac",str_temp.substring(18,str_temp.length()));
            for (int i=2;i<user_key.length;i++){
                map.put(user_key[i],"没有任何信息");
            }
            result_detail.add(map);//初始化，防止没有查到消息时出现错误
        }

        if(intent.getExtras().getString("mac_result_detail")!=null){
            String str_temp=intent.getExtras().getString("mac_result_detail");
            Map<String,Object> map =new HashMap<String, Object>();
            map.put("tmc",str_temp);
            for (int i=1;i<user_key.length;i++){
                map.put(user_key[i],"没有任何信息");
            }
            result_detail.add(map);//初始化，防止没有查到消息时出现错误
        }

          if(myDBAssistant.getLocalDetail(str)!=null&&myDBAssistant.getLocalDetail(str).size()!=0){
                 result_detail=myDBAssistant.getLocalDetail(str);
        }

        button_classify.setOnClickListener(new RadioClickListener());

       for(Map<String,Object> map:result_detail){
        try{
            long duration= sdf.parse(map.get("latest").toString()).getTime()-sdf.parse(map.get("start").toString()).getTime();
            long dure=duration/1000;
            long sec=dure%60;
            long min=(dure-sec)/60%60;
            long hour=(dure-sec)/60/60;
            map.put("duration",hour+"时"+min+"分"+sec+"秒");
        }catch (ParseException e){e.printStackTrace();
        }catch (NullPointerException e){e.printStackTrace();}
       }
        listItem.addAll(result_detail);
        tv_result.setText("用户Mac:"+listItem.get(0).get("tmc").toString()+"\n"+"来源信息:"+listItem.get(0).get("source").toString());
         myAdapter=new MyAdapter(this);
         list_ylfn.setAdapter(myAdapter);

            clickListener = new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    switch (v.getId()) {
                        case R.id.btnPre:
                            preView();
                            break;
                        case R.id.btnNext:
                            nextView();
                            break;
                    }
                }

            };
            // 添加2个Button的监听事件。
            btnPre.setOnClickListener(clickListener);
            btnNext.setOnClickListener(clickListener);
            // 检查2个Button是否是可用的
            checkButton();

    }




    // 点击左边的Button，表示向前翻页，索引值要减1.
    public void preView() {
        index--;

        // 刷新ListView里面的数值。
        myAdapter.notifyDataSetChanged();

        // 检查Button是否可用。
        checkButton();
    }

    // 点击右边的Button，表示向后翻页，索引值要加1.
    public void nextView() {
        index++;

        // 刷新ListView里面的数值。
        myAdapter.notifyDataSetChanged();

        // 检查Button是否可用。
        checkButton();
    }

    public void checkButton() {
        // 索引值小于等于0，表示不能向前翻页了，以经到了第一页了。
        // 将向前翻页的按钮设为不可用。
        if (index <= 0) {
            btnPre.setEnabled(false);
        }else{
            btnPre.setEnabled(true);
        }
        // 值的长度减去前几页的长度，剩下的就是这一页的长度，如果这一页的长度比View_Count小，表示这是最后的一页了，后面在没有了。
        // 将向后翻页的按钮设为不可用。
        if (listItem.size() - index * VIEW_COUNT <= VIEW_COUNT) {
            btnNext.setEnabled(false);
        }
        // 否则将2个按钮都设为可用的。
        else {
            btnNext.setEnabled(true);
        }

    }

    // ListView的Adapter，这个是关键的导致可以分页的根本原因。
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

            // ori表示到目前为止的前几页的总共的个数。
            int ori = VIEW_COUNT * index;

            // 值的总个数-前几页的个数就是这一页要显示的个数，如果比默认的值小，说明这是最后一页，只需显示这么多就可以了
            if (listItem.size() - ori < VIEW_COUNT) {
                return listItem.size() - ori;
            }
            // 如果比默认的值还要大，说明一页显示不完，还要用换一页显示，这一页用默认的值显示满就可以了。
            else {
                return VIEW_COUNT;
            }

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
            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.ylfn,null);
            TextView ylfn_did_view = (TextView)convertView.findViewById(R.id.ylfn_did);
            TextView ylfn_second=(TextView)convertView.findViewById(R.id.ylfn_second);
            StringBuffer buffer=new StringBuffer(256);
            int n=position + index * VIEW_COUNT;
            if(classify==0) {
                 str1= listItem.get(n).get("mac").toString();
                 str2 = listItem.get(n).get("router").toString();
            }if(classify==1){
                 str1 = listItem.get(n).get("start").toString();
                 str1=str1.substring(0,str1.indexOf("-")+2)
                         +"\n"+str1.substring(str1.indexOf("-")+2,str1.length());
                 str2 = listItem.get(n).get("latest").toString();
                str2=str2.substring(0,str2.indexOf("-")+2)
                        +"\n"+str2.substring(str2.indexOf("-")+2,str2.length());
            }if(classify==2){
                 str1 = listItem.get(n).get("adr").toString();
                 str2 = listItem.get(n).get("show").toString();
            }
            ylfn_did_view.setText(str1);
            ylfn_second.setText(str2);
            return convertView;
        }
    }

    class RadioClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            AlertDialog ad =new AlertDialog.Builder(DetailedInfo.this).setTitle("选择查看信息的类型")

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
           button_classify.setText(areas[index]);
           if(button_classify.getText().toString().equals(areas[0])){
               tv_left.setText("连接的路由Mac");
               tv_right.setText("连接的路由名称");
               classify=0;myAdapter.notifyDataSetChanged();
           }
           if(button_classify.getText().toString().equals(areas[1])){
                tv_left.setText("连接起始时间");
                tv_right.setText("连接中断时间");
               classify=1;myAdapter.notifyDataSetChanged();
            }
            if(button_classify.getText().toString().equals(areas[2])){
                tv_left.setText("出现地址");
                tv_right.setText("出现次数");
                classify=2;myAdapter.notifyDataSetChanged();
            }
            dialog.dismiss();

        }

    }

}
