package com.example.administrator.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ScrollView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;

//数据库建立操作类
public  class MyDbHelper extends SQLiteOpenHelper {
    private static MyDbHelper dbhelper = null;
    public static MyDbHelper getInstens(Context context) {
        if (dbhelper == null) {
            dbhelper = new MyDbHelper(context);
        }
        return dbhelper;
    }

    private MyDbHelper(Context context) {
        super(context, "info_all.db", null, 1);
        Log.d("数据库————","成功创建");
        // TODO Auto-generated constructor stub
    }

    public MyDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql_class_table="create table if not exists info_all(id integer primary key autoincrement," +
                "tmc TEXT,mac TEXT,start TEXT,latest TEXT,source TEXT,router TEXT,adr TEXT,range TEXT,rssi TEXT,show TEXT)";
        sqLiteDatabase.execSQL(sql_class_table); Log.d("数据库","成功创建");

      /*String sql_router="create table if not exists info_router(id integer primary key autoincrement," +"mac TEXT, " +
                " router TEXT, rssi TEXT, range TEXT, hidden TEXT ,source TEXT,adr TEXT)";
        sqLiteDatabase.execSQL(sql_router);*/

        String sql_txt="create table if not exists txt(id integer primary key autoincrement," +"my_key TEXT, " +
                " my_value TEXT)";
        sqLiteDatabase.execSQL(sql_txt);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}


//对象存储查询类
class MyDBAssistant {
    Context context;
    public MyDbHelper dbhelper;

    public MyDBAssistant(Context context) {

        this.context = context;
    }

    //存储对象方法
    public void saveObject(ArrayList<Map<String,Object>> router_,ArrayList<Map<String, Object>> user_,String loc) {
        try {
            dbhelper = MyDbHelper.getInstens(context);
            SQLiteDatabase database = dbhelper.getWritableDatabase();
           /*
            for(Map<String,Object> map:router_)
            {
                ContentValues values=new ContentValues();
                values.put("mac",map.get("mac").toString());
                values.put("source",map.get("source").toString());
                values.put("router",map.get("router").toString());
                values.put("adr",loc);
                values.put("hidden",map.get("hidden").toString());
                values.put("range",map.get("range").toString());
                values.put("rssi",map.get("rssi").toString());
                database.insert("info_router",null,values);
            }*/

            for(Map<String,Object> map:user_)
            {
                ContentValues values=new ContentValues();
                values.put("tmc",map.get("tmc").toString());
                values.put("mac",map.get("mac").toString());
                values.put("start",map.get("start").toString());
                values.put("latest",map.get("latest").toString());
                values.put("source",map.get("source").toString());
                values.put("router",map.get("router").toString());
                values.put("adr",loc);
                values.put("range",map.get("range").toString());
                values.put("rssi",map.get("rssi").toString());
                values.put("show",map.get("show").toString());
                database.insert("info_all",null,values);
            }
            String sql_del=  "delete from info_all where info_all.id not in (select MAX(info_all.id) from info_all group by  tmc, start)";
         /*
            String sql_router="delete from info_router where " +
                    "info_router.id not in (select MAX(info_router.id) from info_router group by mac)";*/
            database.execSQL(sql_del); // database.execSQL(sql_router);
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLocal_Router(String src){
        String str="";
        dbhelper=MyDbHelper.getInstens(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        String sql="select * from info_router where "+" mac like '"+src+"%'";
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                str=cursor.getString(cursor.getColumnIndex("source"));
            }
            cursor.close();
        }
        Log.d("数据库路由是",str);
        return str;
    }

    public ArrayList<Map<String,Object>> getLocalDb_Router_To_User(String src,String options,String flag){
        ArrayList<Map<String,Object>> result_detail=new ArrayList<Map<String, Object>>();
       dbhelper=MyDbHelper.getInstens(context);
       SQLiteDatabase database = dbhelper.getReadableDatabase();
       String sql="select * from info_all where "+" mac like '"+src+"%'";
       String sql_options=" order by "+options;
       String sql_flag="";
       if(flag.equals("升序")){
           sql_flag=" asc";
       }else {sql_flag=" desc"; }
       Cursor cursor=database.rawQuery(sql+sql_options+sql_flag,null);
       if(cursor!=null) {
           while (cursor.moveToNext()) {
               Map<String, Object> map = new HashMap<String, Object>();
               map.put("start", cursor.getString(cursor.getColumnIndex("start")));
               map.put("latest", cursor.getString(cursor.getColumnIndex("latest")));
               map.put("tmc", cursor.getString(cursor.getColumnIndex("tmc")));
               map.put("mac", cursor.getString(cursor.getColumnIndex("mac")));
               map.put("router", cursor.getString(cursor.getColumnIndex("router")));
               map.put("source", cursor.getString(cursor.getColumnIndex("source")));
               map.put("adr", cursor.getString(cursor.getColumnIndex("adr")));
               map.put("range", cursor.getString(cursor.getColumnIndex("range")));
               map.put("rssi", cursor.getString(cursor.getColumnIndex("rssi")));
               map.put("show", cursor.getString(cursor.getColumnIndex("show")));
               Log.d("query_result", "查询结束");
               result_detail.add(map);
           }
        cursor.close();
       }
           return result_detail;
       }
    public ArrayList<Map<String,Object>> getLocalDetail(String src){

        ArrayList<Map<String,Object>> result_detail=new ArrayList<Map<String, Object>>();
        dbhelper=MyDbHelper.getInstens(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        String sql="select * from info_all where "+" tmc like '"+src+"%'";
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor!=null){
            while (cursor.moveToNext()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("start", cursor.getString(cursor.getColumnIndex("start")));
                map.put("latest", cursor.getString(cursor.getColumnIndex("latest")));
                map.put("tmc", cursor.getString(cursor.getColumnIndex("tmc")));
                map.put("mac", cursor.getString(cursor.getColumnIndex("mac")));
                map.put("router", cursor.getString(cursor.getColumnIndex("router")));
                map.put("source", cursor.getString(cursor.getColumnIndex("source")));
                map.put("adr",cursor.getString(cursor.getColumnIndex("adr")));
                map.put("range", cursor.getString(cursor.getColumnIndex("range")));
                map.put("rssi", cursor.getString(cursor.getColumnIndex("rssi")));
                map.put("show", cursor.getString(cursor.getColumnIndex("show")));
                Log.d("query_result", "查询结束");
                result_detail.add(map);
            }
            cursor.close();  }
        return result_detail;
    }

    public ArrayList<Map<String,Object>> getObject(String date_time,String keyword,String flag,String sort) {
        ArrayList<Map<String,Object>> result_user=new ArrayList<Map<String, Object>>();
        dbhelper=MyDbHelper.getInstens(context); String mysql ="";String str1="";String str2="";

        SQLiteDatabase database = dbhelper.getReadableDatabase();
              if(flag.equals("MAC地址")){
                  if (!date_time .equals("--")) {

                      str1 = "select * from info_all where "
                              + "(start" + " like '" + date_time + "%') and "
                              + "(tmc" + " like '" + keyword + "%')";
                  }else{   str1 = "select * from info_all where "
                          + " tmc" + " like '" + keyword + "%'";}
              }
              else if(flag.equals("路由名称")){
                  if (!date_time .equals("--")) {

                      str1 = "select * from info_all where "
                              + "(start" + " like '" + date_time + "%') and "
                              + "(router" + " like '" + keyword + "%')";
                  }else{   str1 = "select * from info_all where "
                          + " router" + " like '" + keyword + "%'";}

               }else {   str1="select * from info_all where "
                      +"((start" + " like '"+date_time+"%') and"
                      + "(router" + " like '"+keyword+"%')) or "

                      + "((start" + " like '"+date_time+"%') and "
                      +"(tmc" + " like '"+keyword+"%'))";}

                     if(sort.equals("名称")){
                            str2=" order by tmc asc";  }else  if(sort.equals("时间")){
                             str2="order by start desc";
                     }else if(sort.equals("频率")){
                              str2="order by show desc";
                     }else { str2="order by range asc ";}
            mysql=str1+str2;
         Cursor cursor = database.rawQuery(mysql,null);

           if (cursor != null) {

               while (cursor.moveToNext()) {
                   Map<String,Object> map=new HashMap<String,Object>();
                   map.put("start",cursor.getString(cursor.getColumnIndex("start")));
                   map.put("latest",cursor.getString(cursor.getColumnIndex("latest")));
                   map.put("tmc",cursor.getString(cursor.getColumnIndex("tmc")));
                   map.put("mac",cursor.getString(cursor.getColumnIndex("mac")));
                   map.put("router",cursor.getString(cursor.getColumnIndex("router")));
                   map.put("source",cursor.getString(cursor.getColumnIndex("source")));
                //   map.put("adr",cursor.getString(cursor.getColumnIndex("adr")));
                   map.put("range",cursor.getString(cursor.getColumnIndex("range")));
                   map.put("rssi",cursor.getString(cursor.getColumnIndex("rssi")));
                   map.put("show",cursor.getString(cursor.getColumnIndex("show")));
                   Log.d("query_result",  "查询结束" );
                   result_user.add(map);
               }
               cursor.close();
           }
        return result_user;
    }

    public Map<String,String> getDateGroup(){
      // 把分类信息打印出来
        Map<String,String> result_detail=new HashMap<>();
        dbhelper=MyDbHelper.getInstens(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        String sql="select start from info_all ";
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor!=null){
            while (cursor.moveToNext()) {
                 String temp=cursor.getString(cursor.getColumnIndex("start"));
                 temp=temp.substring(0,temp.indexOf("-"));
                 int count=1;
                    if(result_detail.containsKey(temp)){
                         result_detail.put(temp, String.valueOf(Integer.parseInt(result_detail.get(temp))+1) );
                         continue; //如果已经有了则下一次循环,并增1
                      }
                result_detail.put(temp,String.valueOf(count));
            }
            cursor.close();
        }Log.d("结果是",result_detail.toString());
        return result_detail;
    }

    public Map<String,String> getHotspotGroup(){
        // 把分类信息打印出来
        Map<String,String> result_detail=new HashMap<>();
        dbhelper=MyDbHelper.getInstens(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        String sql="select * from info_all ";
        Cursor cursor=database.rawQuery(sql,null);
        if(cursor!=null){
            while (cursor.moveToNext()) {
                String temp=cursor.getString(cursor.getColumnIndex("mac"));
                String another=cursor.getString(cursor.getColumnIndex("router"));
                int count=1;
                if(result_detail.containsKey(another+temp)){
                    result_detail.put(another+temp , String.valueOf(Integer.parseInt(result_detail.get(another+temp))+1) );
                    continue; //如果已经有了则下一次循环,并增1
                }
                result_detail.put(another+temp,String.valueOf(count));
            }
            cursor.close();
        }Log.d("结果是",result_detail.toString());
        return result_detail;
    }

    public ArrayList<Map<String,Object>> getLocalDb_Collision(String src,String options,String flag){
        ArrayList<Map<String,Object>> result_detail=new ArrayList<Map<String, Object>>();
        dbhelper=MyDbHelper.getInstens(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        String sql="";
        if(src.contains(":")&&!src.contains("年")){
        sql="select * from info_all where "+" mac like '"+src.substring(src.indexOf(":")-2,src.indexOf(":")+15)+"%'" ;}
       else{ sql="select * from info_all where "+" start like '"+src+"%'"; }
         String sql_options=" order by "+options;
        String sql_flag="";
        if(flag.equals("升序")){
            sql_flag=" asc";
        }else {sql_flag=" desc"; }
        Cursor cursor=database.rawQuery(sql+sql_options+sql_flag,null);
        if(cursor!=null) {
            while (cursor.moveToNext()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("start", cursor.getString(cursor.getColumnIndex("start")));
                map.put("latest", cursor.getString(cursor.getColumnIndex("latest")));
                map.put("tmc", cursor.getString(cursor.getColumnIndex("tmc")));
                map.put("mac", cursor.getString(cursor.getColumnIndex("mac")));
                map.put("router", cursor.getString(cursor.getColumnIndex("router")));
                map.put("source", cursor.getString(cursor.getColumnIndex("source")));
                map.put("adr", cursor.getString(cursor.getColumnIndex("adr")));
                map.put("range", cursor.getString(cursor.getColumnIndex("range")));
                map.put("rssi", cursor.getString(cursor.getColumnIndex("rssi")));
                map.put("show", cursor.getString(cursor.getColumnIndex("show")));
                Log.d("query_result", "查询结束");
                result_detail.add(map);
            }
            cursor.close();
        }
        return result_detail;
    }
    public ArrayList<Map<String,Object>> getCalcul(ArrayList<String> arrayList){
     ArrayList<Map<String,Object>>  result_arraylist=new ArrayList<Map<String,Object>>();
       ArrayList<Map<String,Object>> result_detail=new ArrayList<>();
        ArrayList<Map<String,Object>> temp_list=new ArrayList<>();
       dbhelper=MyDbHelper.getInstens(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();
        String sql="";
      int num=arrayList.size();
       for(String src:arrayList){
      //循环查询符合条件的结果
        result_detail.clear();
        if(src.contains(":")&&!src.contains("年")){
            sql="select * from info_all where "
                    +" mac like '"+src.substring(src.indexOf(":")-2,src.indexOf(":")+15)+"%'";}
                   // +" and ( id in (select max(id) from info_all group by tmc))"; }

                    else{ sql="select * from info_all where "+" start like '"+src.substring(0,src.indexOf("\n"))+"%'";}
                // +" and  (id in (select max(id) from info_all group by tmc))"; }

        Cursor cursor=database.rawQuery(sql,null);
        if(cursor!=null) {
            while (cursor.moveToNext()) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("start", cursor.getString(cursor.getColumnIndex("start")));
                map.put("latest", cursor.getString(cursor.getColumnIndex("latest")));
                map.put("tmc", cursor.getString(cursor.getColumnIndex("tmc")));
                map.put("mac", cursor.getString(cursor.getColumnIndex("mac")));
                map.put("router", cursor.getString(cursor.getColumnIndex("router")));
                map.put("source", cursor.getString(cursor.getColumnIndex("source")));
                map.put("range", cursor.getString(cursor.getColumnIndex("range")));
                map.put("rssi", cursor.getString(cursor.getColumnIndex("rssi")));
                map.put("show", cursor.getString(cursor.getColumnIndex("show")));
                result_detail.add(map);
            }
            cursor.close();
         }

         temp_list.clear();
        for(Map<String,Object> map:result_detail){
            int flag=0;
            if(temp_list.size()>0){
                for(Map<String,Object> map_dis:temp_list){
                       if(map.get("tmc").toString().equals(map_dis.get("tmc").toString())){
                          flag=1;
                          break;//如果在表里找到相同的就不add了
                    }
                }
            }
            if(flag==0){
                temp_list.add(map);
            }
        }
         result_arraylist.addAll(temp_list);
       }

       result_detail.clear();

       for(Map<String,Object> map:result_arraylist){
           int flag=0;
           if(result_detail.size()>0){

           for(Map<String,Object> map_result:result_detail){
               if(map.get("tmc").toString()
                       .equals(map_result.get("tmc").toString())){
                   int temp=Integer.parseInt(map_result.get("count").toString());
                   flag=1;
                   temp++;
                   map_result.put("count",String.valueOf(temp));
               }
            }
           }
           if(flag==0){
               int count=1;
               map.put("count",String.valueOf(count));
               result_detail.add(map) ;
           }
         //  Log.d("计算重复次数",map.get("count").toString());
       }//就是去重算法

       result_arraylist.clear();int m;
       result_arraylist.addAll(result_detail);
       result_detail.clear(); //把它清理掉,以便再添加元素作为结果
       for(m=0;m<result_arraylist.size();m++){
           if(Integer.parseInt(result_arraylist.get(m).get("count").toString())==num){
               result_detail.add(result_arraylist.get(m));//如果重复次数为1就去掉
         }
       }
        return result_detail;
    }
}