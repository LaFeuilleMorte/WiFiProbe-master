package com.example.administrator.myapplication;

import java.util.HashMap;
import java.util.Map;

public class Option_Detail {
    private static Option_Detail optionDetail=null;
    private boolean yuyin=false;//语音
    private boolean zhendong=false;//震动
    private String zhanghu="";
    private String load_txt="no";
    private String password="";//账户
    private boolean tanchuang=false;//弹窗
    private String juli_min="20";
    private String checkhome="false";//多近报警
    private Map<String,String> map_txt=new HashMap<>();
    public static Option_Detail getSingleInstance(){
        if(optionDetail==null){
            optionDetail=new Option_Detail();
        }
        return optionDetail;
    }
      private Option_Detail(){ //私有构造方法，单例模式
               }

     public boolean getYuyin(){
         return  yuyin;
    }
    public void setYuyin(boolean yuyin){
        this.yuyin=yuyin;
    }
    public boolean getZhendong(){
        return zhendong;
    }
    public void setZhendong(boolean zhendong){
        this.zhendong=zhendong;
    }
    public String getZhanghu(){
        return zhanghu;
    }
    public void setZhanghu(String zhanghu){
        this.zhanghu=zhanghu;
    }
    public boolean getTanchuang(){
        return tanchuang;
    }
    public void setTanchuang(boolean tanchuang){
        this.tanchuang=tanchuang;
    }
    public String getJuli_min(){
        return juli_min;
    }
    public void setJuli_min(String juli_min){
        this.juli_min=juli_min;
    }
    public String getPassword(){return  password;}
    public void setPassword(String password){this.password=password;}
    public String getCheckhome(){return  checkhome;}
    public void setCheckhome(String checkhome){ this.checkhome=checkhome;}

}
