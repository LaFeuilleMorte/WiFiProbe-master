package com.example.administrator.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class My_Options extends Activity {
    CheckBox yuyin, zhendong, tanchuang;
    TextView zhanghu;
    ListView areaListView;
    String[] areas;
    EditText juli;
    int index=0;
    ArrayList<String> my_arrays=new ArrayList();
    Option_Detail optionDetail;
    RadioOnClick OnClick;
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.my_options);
        SysApplication.getInstance().addActivity(this);
        optionDetail = Option_Detail.getSingleInstance();
        yuyin = (CheckBox) findViewById(R.id.yuyin);
        zhendong = (CheckBox) findViewById(R.id.zhendong);
        tanchuang = (CheckBox) findViewById(R.id.tanchuang);
        zhanghu = (TextView) findViewById(R.id.zhanghu);
        juli = (EditText) findViewById(R.id.juli_min);
        juli.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        SharedPreferences preferences=getSharedPreferences("account_reg",MODE_PRIVATE);//去找存的账户
        SharedPreferences read_last=getSharedPreferences("yonghu",MODE_PRIVATE);//找上次的账户
        Map<String,?> map_all_=preferences.getAll();
        Set<String> set__=map_all_.keySet();
        my_arrays.clear();//先清空
        for(String key:set__){
            my_arrays.add(key);  Log.d("key是",key);//先全部加载进去
            }

            for(int i=0;i<my_arrays.size();i++){
                if(read_last.getAll().get("user")!=null){
                     if(read_last.getAll().get("user").toString().equals(my_arrays.get(i))){
                         Collections.swap(my_arrays,0,i);//交换位置
                    }
                }
            }//调换上次登录的位置，放到第一个
            for(int i=0;i<my_arrays.size();i++){
              Log.d("areas是"+i+" ",my_arrays.get(i));
            }

        areas=(String[])my_arrays.toArray(new String[my_arrays.size()]);//变成数组
        //先加载设置
        zhendong.setChecked(optionDetail.getZhendong());
        yuyin.setChecked(optionDetail.getYuyin());
        tanchuang.setChecked(optionDetail.getTanchuang());
        juli.setText(optionDetail.getJuli_min());
        zhanghu.setText(optionDetail.getZhanghu());

        //账户监听者
        OnClick=new RadioOnClick(0);
        zhanghu.setOnClickListener(new RadioClickListener());//设置点击监听
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(My_Options.this);
            builder.setTitle("是否保存设置?");builder.setCancelable(false);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    zhendong.setChecked(optionDetail.getZhendong());
                    yuyin.setChecked(optionDetail.getYuyin());
                    tanchuang.setChecked(optionDetail.getTanchuang());
                    juli.setText(optionDetail.getJuli_min());
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor_check_inner = getSharedPreferences("check_my_options", MODE_PRIVATE).edit();
                    SharedPreferences.Editor my_zhanghu=getSharedPreferences("yonghu",MODE_PRIVATE).edit();
                    SharedPreferences read_acc=getSharedPreferences("account_reg",MODE_PRIVATE);
                    if (tanchuang.isChecked()) {
                        editor_check_inner.putString("tanchuang", "true");
                        optionDetail.setTanchuang(true);
                    } else {
                        editor_check_inner.putString("tanchuang", "false");
                        optionDetail.setTanchuang(false);
                    }

                    if (yuyin.isChecked()) {
                        editor_check_inner.putString("yuyin", "true");
                        optionDetail.setYuyin(true);
                    } else {
                        editor_check_inner.putString("yuyin", "false");
                        optionDetail.setYuyin(false);
                    }

                    if (zhendong.isChecked()) {
                        editor_check_inner.putString("zhendong", "true");
                        optionDetail.setZhendong(true);
                    } else {
                        editor_check_inner.putString("zhendong", "false");
                        optionDetail.setZhendong(false);
                    }
                    editor_check_inner.putString("juli_min", juli.getText().toString());
                    optionDetail.setJuli_min(juli.getText().toString());

                   my_zhanghu.putString("user",zhanghu.getText().toString());
                   my_zhanghu.putString("password",read_acc.getAll().get(zhanghu.getText().toString()).toString());

                   optionDetail.setZhanghu(zhanghu.getText().toString());
                   my_zhanghu.commit();
                   editor_check_inner.commit();


                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //回到主界面
                    Intent intent = new Intent(My_Options.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            builder.show();
            return true;
        }
            return super.onKeyDown(keyCode, event);
        }

        class RadioClickListener implements View.OnClickListener {

        @Override

        public void onClick(View v) {

            AlertDialog ad =new AlertDialog.Builder(My_Options.this).setTitle("选择账户")

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
            zhanghu.setText(my_arrays.get(index));
            dialog.dismiss();

        }

    }
}
