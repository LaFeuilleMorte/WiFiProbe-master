package com.example.administrator.myapplication;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.Inflater;

public class MainActivity extends Activity{
    Button Connect_BLT;
    TextView result;
    Data_Receiver dataReceiver;
    TextView text_reg;
    EditText edit_acc;
    CheckBox checkBox;
    EditText edit_psd;
    Button log_blt;
    String str="";
    Handler handler;
    TextView tv_paoma;
    private MyDBAssistant myDBAssistant;
    TextView help_me;
    Handler handler_color;
    Option_Detail optionDetail;
    boolean isStop=true;
    String[] colors=new String[]{"#0000FF","#800080","#FF1493","#00BFFF","#8A2BE2","#4B0082","#00FFFF","#00FF7F","#FFFF00"};
    MacTextWatcher macTextWatcher=new MacTextWatcher();

    Button serial;
    @SuppressLint("HandlerLeak")
    public  void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        SysApplication.getInstance().addActivity(this);
        optionDetail=Option_Detail.getSingleInstance();
        text_reg=(TextView)findViewById(R.id.reg_name);
        edit_acc=(EditText)findViewById(R.id.account_home);
        edit_psd=(EditText)findViewById(R.id.password_home);
        checkBox=(CheckBox)findViewById(R.id.remember_pwd);
        log_blt=(Button)findViewById(R.id.log_in);
        tv_paoma=(TextView)findViewById(R.id.paomadeng);
        help_me=(TextView)findViewById(R.id.help_me);

        serial=(Button)findViewById(R.id.test_serial);
        serial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Serial_Port_Display.class);;
                startActivity(intent);
            }
        });

        //只做一次
        Random random = new Random();
        handler_color=new Handler(){
          public void handleMessage(Message msg){
              int s = random.nextInt(colors.length)%(colors.length+1);
              tv_paoma.setTextColor(Color.parseColor(colors[s]));
              handler_color.sendEmptyMessageDelayed(0,500);
          }
        };handler_color.sendEmptyMessage(0);

      //输入当前地址，进入此界面时触发
        AlertDialog.Builder builder_loc=new AlertDialog.Builder(MainActivity.this);
        builder_loc.setTitle("请输入当前地址：");
        View view_loc = LayoutInflater.from(MainActivity.this).inflate(R.layout.loc_text_inflater, null);
        builder_loc.setView(view_loc);
        final EditText editText_loc=(EditText)view_loc.findViewById(R.id.edit_loc);
        final TextView textView_loc=(TextView)view_loc.findViewById(R.id.tishi_loc);
        editText_loc.addTextChangedListener(macTextWatcher);
        SharedPreferences read_loc=getSharedPreferences("account_reg",MODE_PRIVATE);
        try{
        editText_loc.setText(read_loc.getAll().get("location").toString());}catch (Exception e){}
      //  editText_pwd.addTextChangedListener(macTextWatcher);
        builder_loc.setCancelable(false);
        builder_loc.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(!editText_loc.getText().toString().equals("")){
                  //  if(editText_pwd.getText().toString().length()>=8){
                        SharedPreferences.Editor editor = getSharedPreferences("account_reg", MODE_PRIVATE).edit();
                        String str1=editText_loc.getText().toString();
                       // String str2=editText_pwd.getText().toString();
                        editor.putString("location",str1);
                        editor.commit();

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                    }
                else{
                     textView_loc.setText("地址不能为空");
                    try {
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        builder_loc.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        builder_loc.show();
        //输入地址确认窗口
        help_me.setOnClickListener((v)-> {
                   AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                   builder.setTitle("");
                   View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.help_items, null);
                   builder.setView(view);
                   builder.show();//显示帮助下的选项
                   TextView opt = (TextView) view.findViewById(R.id.opt_home);
                   TextView about = (TextView) view.findViewById(R.id.about_home);
                   opt.setOnClickListener((view1) -> {
                       Intent intent_opt = new Intent(MainActivity.this, My_Options.class);
                       startActivity(intent_opt);
                   });
                   about.setOnClickListener((view2) -> {
                       AlertDialog.Builder about_builder = new AlertDialog.Builder(MainActivity.this);
                       about_builder.setTitle("关于本软件");
                       View myView = LayoutInflater.from(MainActivity.this).inflate(R.layout.about_app, null);
                       about_builder.setView(myView);
                       about_builder.show();//显示软件介绍下的对话框
                   });
               });
     SharedPreferences read_check=getSharedPreferences("check_home",MODE_PRIVATE);
      if(!read_check.contains("id")){     checkBox.setChecked(false);       }else {
            if (read_check.getAll().get("id").toString().equals("false")) {
                checkBox.setChecked(false);
            }
            if (read_check.getAll().get("id").toString().equals("true")) {
                checkBox.setChecked(true);
            }
        }
         checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                 SharedPreferences.Editor editor_check_inner=getSharedPreferences("check_home",MODE_PRIVATE).edit();
                if(isChecked){
                 editor_check_inner.putString("id","true");
                }else {
                  editor_check_inner.putString("id","false"); //设为不勾选时，直接删掉密码
                  SharedPreferences.Editor editor_pass=getSharedPreferences("yonghu",MODE_PRIVATE).edit();
                  editor_pass.remove("password");
                  editor_pass.commit();
                }
             editor_check_inner.commit();
            }
        });
        edit_acc.addTextChangedListener(macTextWatcher);
        edit_psd.addTextChangedListener(macTextWatcher);
        SharedPreferences read_init=getSharedPreferences("yonghu",MODE_PRIVATE);
       try{edit_acc.setText(read_init.getAll().get("user").toString());}catch (Exception e){ }
       try{edit_psd.setText(read_init.getAll().get("password").toString());}catch (Exception e){ }
        log_blt.setOnClickListener((v)->{
            SharedPreferences read=getSharedPreferences("account_reg",MODE_PRIVATE);
            if(read.contains(edit_acc.getText().toString())){
               String temp=read.getAll().get(edit_acc.getText().toString()).toString();
               if(temp.equals(edit_psd.getText().toString())){

                   Intent intent = new Intent();
                   if(SysApplication.globalBlueSocket!=null&&SysApplication.globalBlueSocket.isConnected()){
                       intent.putExtra("socket","connected");}    else{
                       intent.putExtra("socket","disconnected");
                   }
                   intent.setClass(getApplicationContext(), BTClient.class);
                   startActivity(intent);
                   SharedPreferences.Editor editor=getSharedPreferences("yonghu",MODE_PRIVATE).edit();
                   editor.putString("user",edit_acc.getText().toString());
                   if(checkBox.isChecked()){
                       editor.putString("password",edit_psd.getText().toString());
                   } else{
                       editor.remove("password"); }//记住密码就记住，没有记住密码就直接删除
                   editor.commit();
               }else {Toast.makeText(MainActivity.this,"密码错误，请重新输入",Toast.LENGTH_SHORT).show();
                      edit_psd.setText("");//清空密码
                         }
            }else {Toast.makeText(MainActivity.this,"用户名错误，请重新输入",Toast.LENGTH_SHORT).show();
                       edit_acc.setText("");
                         }
        });

        text_reg.setOnClickListener((v) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("请输入注册名和密码");
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.reg_name_and_pwd, null);
            builder.setView(view);
            final EditText editText_acc=(EditText) view.findViewById(R.id.account_reg);
            final EditText editText_pwd=(EditText)view.findViewById(R.id.password_reg);
            final TextView textView_reg=(TextView)view.findViewById(R.id.tishi);
            editText_acc.addTextChangedListener(macTextWatcher);
            editText_pwd.addTextChangedListener(macTextWatcher);
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if(!editText_acc.getText().toString().equals("")&&!editText_pwd.getText().toString().equals("")){
                            if(editText_pwd.getText().toString().length()>=8){
                                SharedPreferences.Editor editor = getSharedPreferences("account_reg", MODE_PRIVATE).edit();
                                String str1=editText_acc.getText().toString();
                                String str2=editText_pwd.getText().toString();
                                editor.putString(str1,str2);
                                editor.commit();

                                try {
                                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                    field.setAccessible(true);
                                    field.set(dialog, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                            }else {    textView_reg.setText("密码不能少于8位");

                              try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                                  }
                            }
                    }else{
                             textView_reg.setText("用户名和密码不能为空");
                             try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
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

       handler=new Handler(){
           public  void handleMessage(Message msg){
          ////
           }

       };
        Intent intent=new Intent(MainActivity.this,My_Location_Service.class);
        startService(intent);
        dataReceiver=new Data_Receiver();
        IntentFilter intentFilter_data=new IntentFilter("location_serve");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(dataReceiver,intentFilter_data);
    }
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this,MyIntentService.class));
        stopService(new Intent(MainActivity.this,My_Location_Service.class));
    }

    private long mExitTime = 0;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if ((System.currentTimeMillis() - mExitTime) > 2000 ) {

                Toast.makeText(this, "再按一次退出应用程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }else{
                SysApplication.getInstance().exit();
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private class Data_Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if("location_serve".equals(intent.getAction())) {
                //  handler_read.getLooper().quit();//只要收到广播就不再读取了
                if (intent.getExtras() != null) {
                       str=intent.getExtras().getString("location_result");
                       handler.sendEmptyMessage(1);
                }
            }
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
            String temp = s.toString();
            temp = temp.replace(" ","")
                       .replace("\n","");
            s.replace(0,s.length(),temp);
        }
    }

}