package com.example.administrator.myapplication;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class LocalDataBase extends AppCompatActivity implements View.OnClickListener,TimePicker.OnTimeChangedListener ,DatePicker.OnDateChangedListener {
    Handler handler;
    Handler handler_refresh;
    ArrayList<Map<String, Object>> groupList=new ArrayList<Map<String,Object>>();
    MyDBAssistant myDBAssistant;
    ArrayList<ArrayList<Map<String, Object>>> childList=new ArrayList<ArrayList<Map<String,Object>>>();
    ArrayList<Map<String, Object>> child;
    ArrayList<Map<String,Object>> myRouter=null;
    EditText editText;
    TextView textView_1;
    String string_result=null;
    private final int RESULT=5;
    private final  int DATE_CHANGED=17;
    private final  int TIME_CHANGED=18;
    private Context context;
    private LinearLayout llDate, llTime;
    private TextView tvDate, tvTime;
    private int year, month, day, hour, minute;
    private  ArrayList<Map<String,Object>> data=new ArrayList<Map<String,Object>>();
    private StringBuffer date, time;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日--HH时mm分ss秒");
    private  ExpandableListView expandlist;
    double cost;
    private String[] user_info={"设备正在连接的路由Mac：","设备连接路由器名称：","信号强度：","距离：","第一次探测到时间：","最近探测到时间：","累计在线时长：","手机或电脑厂商：","出现次数："};
    private String[] user_key={"mac","router","rssi","range","start","latest","duration","source","show"};
    private String[] areas = new String[]{"MAC地址","路由名称", "日期时间"};
   private String[] sort=new String[] {"名称","时间","频率","距离"};
    private SortSelect sortSelect=new SortSelect(0);
    private RadioOnClick OnClick = new RadioOnClick(0);//初始值第一个
    private ListView sortList;
    private ListView areaListView;
    private Button Button;
    private Button sortButton;
    MacTextWatcher macTextWatcher;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;
    @SuppressLint("HandlerLeak")
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.local_db);
        SysApplication.getInstance().addActivity(this);

        Button=(Button)findViewById(R.id.Button);
        Button.setOnClickListener(new RadioClickListener());//复选框
        sortButton=(Button)findViewById(R.id.Button_sort);
        sortButton.setOnClickListener(new SortClickListener());


        expandlist = (ExpandableListView) findViewById(R.id.list_db);
        context = this;
        date = new StringBuffer();
        time = new StringBuffer();
        initView();
        initDateTime();
        textView_1=(TextView)findViewById(R.id.textView1);
        Button button = (Button) findViewById(R.id.button_query);
        macTextWatcher=MacTextWatcher.getIns();
        editText=(EditText)findViewById(R.id.edit_query);
        editText.addTextChangedListener(macTextWatcher);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {//EditorInfo.IME_ACTION_SEARCH、EditorInfo.IME_ACTION_SEND等分别对应EditText的imeOptions属性

                    String str=tvTime.getText().toString();
                    if(str!="") {
                        str=str.substring(0,str.indexOf("时")+1);
                        if(str.indexOf("时")==1){
                            str="0"+str;
                        }
                    }

                    String str_date=tvDate.getText().toString();
                    if(str_date!="") {
                        String  temp1=str_date.substring(0,str_date.indexOf("年")+1);
                        String temp2=str_date.substring(str_date.indexOf("年")+1,str_date.indexOf("月")+1);
                        String temp3=str_date.substring(str_date.indexOf("月")+1,str_date.indexOf("日")+1);
                        if(temp2.length()==2){temp2="0"+temp2;}
                        if(temp3.length()==2){temp3="0"+temp3;}

                        str_date=temp1+temp2+temp3;
                    }

                    String str1=str_date+"--"+str; Log.d("查询时间",str1);
                    String str2=editText.getText().toString();
                    String str3=Button.getText().toString();
                    String str4=sortButton.getText().toString();
                    if(!str1.contains("日")&&Button.getText().toString().equals("日期时间")){
                        textView_1.setText("请选择您要查询的日期和时间");
                    } else{ show_on_search(str1,str2,str3,str4); textView_1.setText("正在查询，请稍等...");}

                    //TODO回车键按下时要执行的操作
                }
                return false;
            }
        });

       myDBAssistant=new MyDBAssistant(this);
      button.setOnClickListener((v) -> {

          String str=tvTime.getText().toString();
          if(str!="") {
            str=str.substring(0,str.indexOf("时")+1);
            if(str.indexOf("时")==1){
                str="0"+str;
            }
          }

          String str_date=tvDate.getText().toString();
          if(str_date!="") {
              String  temp1=str_date.substring(0,str_date.indexOf("年")+1);
              String temp2=str_date.substring(str_date.indexOf("年")+1,str_date.indexOf("月")+1);
              String temp3=str_date.substring(str_date.indexOf("月")+1,str_date.indexOf("日")+1);
              if(temp2.length()==2){temp2="0"+temp2;}
              if(temp3.length()==2){temp3="0"+temp3;}

              str_date=temp1+temp2+temp3;
          }

          String str1=str_date+"--"+str; Log.d("查询时间",str1);
          String str2=editText.getText().toString();
          String str3=Button.getText().toString();
          String str4=sortButton.getText().toString();
                        if(!str1.contains("日")&&Button.getText().toString().equals("日期时间")){
                            textView_1.setText("请选择您要查询的日期和时间");
                        } else{ show_on_search(str1,str2,str3,str4); textView_1.setText("正在查询，请稍等...");}
         });


        handler = new Handler() {

            public void handleMessage(Message msg) {

                if (msg.what == RESULT) {

                    if(data.size()!=0){
                        Log.d("查询结果收到",data.toString());
                        textView_1.setText("查询到"+ data.size()+"条"+"！耗时"+cost+"秒");
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
                        MyExpandableListAdapter adapter = new MyExpandableListAdapter(LocalDataBase.this,groupList, childList);
                        expandlist.setAdapter(adapter);

                     }else{
                        textView_1.setText("没有查询到任何信息,请更改选择条件");
                        groupList.clear();
                        childList.clear();
                        MyExpandableListAdapter adapter = new MyExpandableListAdapter(LocalDataBase.this,groupList, childList);
                        expandlist.setAdapter(adapter);
                         }
                }
            }
        };

       onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final long packedPosition = expandlist.getExpandableListPosition(position);
                final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
                final int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
                //长按的是group的时候，childPosition = -1
                if (childPosition == -1) {
                    AlertDialog.Builder builder= new AlertDialog.Builder(context);
                    builder.setTitle("警告");
                    builder.setMessage("确定展开详细信息");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String info=groupList.get(groupPosition).get("group").toString();
                            Intent intent=new Intent(LocalDataBase.this,DetailedInfo.class);
                            intent.putExtra("group_info",info);
                            startActivity(intent);
                            }
                    });
                    builder.setNegativeButton("取消",null);
                    builder.show();
                }
                return true;
            }
        };
         expandlist.setOnItemLongClickListener(onItemLongClickListener);
    }
    public void show_on_search(String date_time,String keyword,String type,String sort){
        Date date=new Date();
        long start=date.getTime();
        Thread db_thread=new Thread(()->{
                data=myDBAssistant.getObject(date_time,keyword,type,sort);
                Date date1=new Date();
                long temp=date1.getTime()-start;
                cost=temp/1000.0;
                handler.sendEmptyMessage(RESULT);
        }); db_thread.start();
    }

//下面是时间选项控件
    private void initView() {
        llDate = (LinearLayout) findViewById(R.id.ll_date);
        tvDate = (TextView) findViewById(R.id.tv_date);
       llTime = (LinearLayout) findViewById(R.id.ll_time);
       tvTime = (TextView) findViewById(R.id.tv_time);
        llDate.setOnClickListener(this);
        llTime.setOnClickListener(this);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_date:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (date.length() > 0) { //清除上次记录的日期
                            date.delete(0, date.length());
                        }
                        tvDate.setText(date.append(String.valueOf(year)).append("年").append(String.valueOf(month)).append("月").append(day).append("日"));
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("清空", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tvDate.setText("");dialog.dismiss();
                    }
                });
                final AlertDialog dialog = builder.create();
                View dialogView = View.inflate(context, R.layout.dialog_date, null);
                final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.datePicker);

                dialog.setTitle("设置日期");
                dialog.setView(dialogView);
                dialog.show();
                //初始化日期监听事件
                datePicker.init(year, month-1 , day, this);
                break;
            case R.id.ll_time:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                builder2.setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (time.length() > 0) { //清除上次记录的日期
                            time.delete(0, time.length());
                        }
                        tvTime.setText(time.append(String.valueOf(hour)).append("时").append(String.valueOf(minute)).append("分"));
                        dialog.dismiss();
                    }
                });

                builder2.setNegativeButton("清空", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tvTime.setText("");dialog.dismiss();
                }
            });
                AlertDialog dialog2 = builder2.create();
                View dialogView2 = View.inflate(context, R.layout.dialog_time, null);
                TimePicker timePicker = (TimePicker) dialogView2.findViewById(R.id.timePicker);
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);
                timePicker.setIs24HourView(true); //设置24小时制
                timePicker.setOnTimeChangedListener(this);
                dialog2.setTitle("设置时间");
                dialog2.setView(dialogView2);
                dialog2.show();
                break;
        }
    }

    private void initDateTime() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
       minute = calendar.get(Calendar.MINUTE);
    }

    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear+1;//不然会少一个月
        this.day = dayOfMonth;
    }

    public void onTimeChanged(TimePicker view, int hourOfDay,int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
    }




    public void onStart(){
        super.onStart();

    }
    public void onStop(){
        super.onStop();

    }
    class RadioClickListener implements View.OnClickListener {

            @Override

            public void onClick(View v) {

                AlertDialog ad =new AlertDialog.Builder(LocalDataBase.this).setTitle("选择检索方式"+"   MAC搜索(默认)")

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
                Button.setText(areas[index]);

                if(index==0){
                    editText.removeTextChangedListener(macTextWatcher);
                    editText.addTextChangedListener(macTextWatcher);
                } else {editText.removeTextChangedListener(macTextWatcher);}
                dialog.dismiss();

            }

        }



    class SortClickListener implements View.OnClickListener {

    @Override

    public void onClick(View v) {

        AlertDialog ad =new AlertDialog.Builder(LocalDataBase.this).setTitle("选择排序方式"+"   名称(默认)")

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

        setIndex(whichButton);
         sortButton.setText(sort[index]);
        //    Toast.makeText(LocalDataBase.this, "您已经选择了 " +  ":" + areas[index], Toast.LENGTH_LONG).show();
        dialog.dismiss();

    }

}

}
class MacTextWatcher implements TextWatcher {
    private static boolean mWasEdited = false;
    public static MacTextWatcher macTextWatcher;
    public  static MacTextWatcher getIns(){
        if(macTextWatcher==null ){
            macTextWatcher=new MacTextWatcher();
        }
        return  macTextWatcher;
    }
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
            builder.append(mac.charAt(i));
            if(i%2!=0&&i!=mac.length()-1&&i!=11){
                builder.append(":");
            }
        }
        s.replace(0,s.length(),builder.toString());Log.d("index","always 0");

    }
}