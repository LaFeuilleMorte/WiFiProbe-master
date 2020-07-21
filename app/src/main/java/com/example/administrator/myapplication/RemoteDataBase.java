package com.example.administrator.myapplication;

import android.app.Activity;
import android.os.Bundle;

public class RemoteDataBase extends Activity {
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.local_db);
        SysApplication.getInstance().addActivity(this);
    }
}
