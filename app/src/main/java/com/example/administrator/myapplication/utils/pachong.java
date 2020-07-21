package com.example.administrator.myapplication.utils;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
public class pachong {

        public static String doit_webpage(String mac)
        {
            StringBuffer url = new StringBuffer("http://dingwei.doit.am/d.php?mac=");
            url.append(mac);
            String result = url.toString();
            return result;
        }

        public static String doit_spy(String url)
        {
            try
            {
                URL sniffurl = new URL(url);
                URLConnection conn = sniffurl.openConnection();
                InputStream is = conn.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                String line = br.readLine();
                return line;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public static String doit_longitude(String info)
        {
            String longitude = "-1";
            Integer longitude_idx = info.indexOf("longitude");
            Integer latitude_idx = info.indexOf("latitude");
            if(longitude_idx != -1)
            {
                longitude = info.substring(longitude_idx + 11, latitude_idx - 2);
            }
            return longitude;
        }

        public static String doit_latitude(String info)
        {
            String latitude = "-1";
            Integer latitude_idx = info.indexOf("latitude");
            Integer accuracy_idx = info.indexOf("accuracy");
            if(latitude_idx != -1)
            {
                latitude = info.substring(latitude_idx + 10, accuracy_idx - 2);
            }
            return latitude;
        }

        public static String getLat_Lon(String mac)
        {
            Gson gson = new Gson();
            String weburl;
            //String mac = "ff:ff:ff:ff:ff:ff";
            String state;
            weburl = doit_webpage(mac);
            state = doit_spy(weburl);
            String longitude = doit_longitude(state);
            String latitude = doit_latitude(state);
            return  longitude+"@"+latitude ;

        }
}
