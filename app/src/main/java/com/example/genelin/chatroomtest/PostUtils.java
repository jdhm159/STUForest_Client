package com.example.genelin.chatroomtest;

/**
 * Created by GeneLin on 2019/5/10.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
//post请求方法
public class PostUtils {

    public static String LOGIN_URL = "http://192.168.2.225:8080/STUforest/Fabu";

    public static String LoginByPost(String Name,String User,String Phone, String Place)
    {
        String msg = "";
        try{
            HttpURLConnection conn = (HttpURLConnection) new URL(LOGIN_URL).openConnection();
            //设置请求方式,请求超时信息
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            //设置运行输入,输出:
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //Post方式不能缓存,需手动设置为false
            conn.setUseCaches(false);
            //我们请求的数据:
            String data = "Name="+ URLEncoder.encode(Name, "UTF-8")+
                    "&User="+ URLEncoder.encode(User, "UTF-8")+
                    "&Phone="+ URLEncoder.encode(Phone, "UTF-8")+
                    "&Place="+ URLEncoder.encode(Place, "UTF-8");
            //这里可以写一些请求头的东东...
            //获取输出流
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
                String lines;
                while ((lines = reader.readLine()) != null){
                    System.out.println(lines);
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

}
