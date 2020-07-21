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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class Result_Of_Cacul extends Activity {

    private ArrayList<Map<String, Object>> data= new ArrayList<Map<String, Object>>();
    private Handler handler;
    private ExpandableListView expandlist;
    private ArrayList<Map<String, Object>> groupList = new ArrayList<Map<String, Object>>();
    String[] router_info = {"路由名称：", "信号强度：", "距离：", "出现次数：", "来源信息：", "是否匿名："};
    String[] router_key = {"router", "rssi", "range", "show", "source", "hidden"};//子表中显示路由信息列表
    private Context context = this;
    private MacTextWatcher macTextWatcher;
    private MyDBAssistant myDBAssistant;
    private Button button_filter;
    private Handler blt_handler = new Handler();
    private Runnable blt_runable;
    private int classify = 1;
    private String str = "";
    private String[] areas = {"按MAC", "按名称", "按距离"};
    private RadioOnClick OnClick = new RadioOnClick(0);
    private ListView areaListView;
    private Button sort_filter;
    private Button flag;
    private ListView listView;
    private MyAdapter myAdapter;
    private ArrayList<Map<String,Object>> result_detail=new ArrayList<>();
    private String[] sort_string = {"tmc", "source", "range"};

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);
        setContentView(R.layout.any_kind_layout);
        listView = (ListView) findViewById(R.id.list_any_kind);
        myAdapter = new MyAdapter(this);
        myDBAssistant=new MyDBAssistant(this);
        listView.setAdapter(myAdapter);
        macTextWatcher = new MacTextWatcher();
        button_filter = (Button) findViewById(R.id.button_filter);
        sort_filter = (Button) findViewById(R.id.sort_filter_button);
        sort_filter.setOnClickListener(new RadioClickListener());
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

        handler = new Handler() {

            public void handleMessage(Message msg) {


                groupList.clear();
                if (result_detail != null && result_detail.size() != 0) {

                    if (classify == 0) {
                        str = str.replace(" ", "");//去掉空格
                        if (!str.equals("")) {

                             for(Map<String,Object> map:result_detail){
                                    if (map.get("tmc").toString().contains(str)
                                            ||map.get("source").toString()
                                            .toLowerCase() //统一小写
                                            .contains(str.toLowerCase())) {
                                        groupList.add(map);
                                    }
                                    }
                                }
                            }
                    //如果不过滤
                    if (classify == 1) {
                        groupList.addAll(result_detail);
                    }
                }
                groupList = compareList(groupList, sort_filter.getText().toString(), flag.getText().toString());//排序
                myAdapter.notifyDataSetChanged();
                Log.d("列表已更新", "confirmed");//通知数值发生变化
            }
        };
        //在这里取intent，不会报空指针
        Intent intent=getIntent();
        if(intent!=null){
            if(intent.getSerializableExtra("target_cal")!=null){
                ArrayList<String> arrayList=(ArrayList<String>) intent.getSerializableExtra("target_cal");
                result_detail=myDBAssistant.getCalcul(arrayList);
                if(result_detail!=null&&result_detail.size()!=0){
                    handler.sendEmptyMessage(0);
                }
            }
        }

        button_filter.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Result_Of_Cacul.this);
            builder.setTitle("请输入要过滤的Mac");
            View view = LayoutInflater.from(Result_Of_Cacul.this).inflate(R.layout.dialog_filter, null);
            builder.setView(view);
            final EditText editText = (EditText) view.findViewById(R.id.filter_user_mac);
            final RadioGroup ch = (RadioGroup) view.findViewById(R.id.rgroup);
            editText.addTextChangedListener(macTextWatcher);//默认初始为Mac搜索
            ch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup rg, int checkedId) {
                    switch (checkedId) {
                        case R.id.ch1:
                            editText.setText("");
                            editText.addTextChangedListener(macTextWatcher);
                            break;
                        case R.id.ch2:
                            editText.setText("");
                            editText.removeTextChangedListener(macTextWatcher);
                            break;
                    }
                }
            });
            builder.setCancelable(false);
            builder.setPositiveButton("确定过滤", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    classify = 0;
                    str = editText.getText().toString();
                    handler.sendEmptyMessage(0);
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Result_Of_Cacul.this, "过滤成功", Toast.LENGTH_SHORT).show();
                }

            });
            builder.setNegativeButton("取消过滤", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    classify = 1;
                    handler.sendEmptyMessage(0);
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


    //排序算法
    private ArrayList<Map<String, Object>> compareList(ArrayList<Map<String, Object>> result_list, String sort_filter_string, String flag) {

        Collections.sort(result_list, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                String temp = "";
                for (int i = 0; i < 3; i++) {
                    if (sort_filter_string.equals(areas[i])) {
                        temp = sort_string[i];
                    }
                }
                String str1 = "";
                String str2 = "";
                if (map1.get(temp) != null && map2.get(temp) != null) {
                    if (flag.equals("升序")) {
                        str1 = map1.get(temp).toString();
                        str2 = map2.get(temp).toString();
                    }
                    if (flag.equals("降序")) {
                        str1 = map2.get(temp).toString();
                        str2 = map1.get(temp).toString();
                    }

                }
                return str1.compareToIgnoreCase(str2);
            }
        });
        return result_list;
    }



    class MacTextWatcher implements TextWatcher {
        private boolean mWasEdited = false;

        private MacTextWatcher() {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mWasEdited) {
                mWasEdited = false;
                return;
            }
            mWasEdited = true;
            String mac = s.toString();
            mac = mac.replace(":", "");
            StringBuffer builder = new StringBuffer();
            for (int i = 0; i < mac.length() && i < 12; i++) {
                if (('a' <= mac.toLowerCase().charAt(i)
                        && mac.toLowerCase().charAt(i) <= 'f')
                        || ('0' <= mac.charAt(i)
                        && mac.charAt(i) <= '9')) {
                    builder.append(mac.charAt(i));
                }
                if (i % 2 != 0 && i != mac.length() - 1 && i != 11) {
                    builder.append(":");
                }
            }
            s.replace(0, s.length(), builder.toString());
            Log.d("index", "always 0");

        }
    }

    class RadioClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            AlertDialog ad = new AlertDialog.Builder(Result_Of_Cacul.this).setTitle("选择排序方式" + "   按MAC(默认)")

                    .setSingleChoiceItems(areas, OnClick.getIndex(), OnClick).create();

            areaListView = ad.getListView();

            ad.show();

        }

    }

    class RadioOnClick implements DialogInterface.OnClickListener {

        private int index;


        public RadioOnClick(int index) {

            this.index = index;

        }

        public void setIndex(int index) {

            this.index = index;

        }

        public int getIndex() {

            return index;

        }


        public void onClick(DialogInterface dialog, int whichButton) {

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

            return groupList.size();
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

            convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.result_of_cal_item, null);
            TextView tv=convertView.findViewById(R.id.result_cal);
            String content = "<font color=\"#b80190\">" + groupList.get(position).get("source").toString() + "</font>"
                    + "<br>" + "<font color=\"#000000\">" + groupList.get(position).get("tmc").toString() + "</font>";
            tv.setText(Html.fromHtml(content));
           tv.setOnClickListener((v) -> {
                AlertDialog.Builder ab = new AlertDialog.Builder(Result_Of_Cacul.this);
                ab.setTitle("点击展开详细信息").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String temp=tv.getText().toString();
                        Intent intent = new Intent(Result_Of_Cacul.this,DetailedInfo.class);
                        intent.putExtra("mac_result_detail", temp.substring(temp.indexOf(":")-2,temp.indexOf(":")+15));
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

            return convertView;
        }
    }
}

