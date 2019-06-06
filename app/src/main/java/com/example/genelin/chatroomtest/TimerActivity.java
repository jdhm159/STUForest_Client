package com.example.genelin.chatroomtest;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Collections;




import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//计时板块
public class TimerActivity extends Activity implements View.OnClickListener{

    private EditText inputet;//输入时间
    private Button getTime;//得到输入的时间
    private Button startTime;//点击开始自动减少时间的按钮
    private Button stopTime;//点击开始停止自动减少时间的按钮
    private TextView time;//当前时间
    private int i = 0;
    private int s = 0;
    private Timer timer = null;//计时器
    private TimerTask timerTask = null;
    private String studytime;
    private Button signup;
    public static final int SHOW_RESPONSE=1;
    public Handler handler=new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what){
                case SHOW_RESPONSE:
                    String response=(String)msg.obj;
                    Toast.makeText(TimerActivity.this, response, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_layout);

        initviews();//初始化视图
        setOnclick();//设置视图的点击监听
    }

    private void setOnclick() {
        getTime.setOnClickListener(this);
        startTime.setOnClickListener(this);
        stopTime.setOnClickListener(this);
    }

    private void initviews() {
        inputet = (EditText) findViewById(R.id.inputtime);
        getTime = (Button) findViewById(R.id.gettime);
        startTime = (Button) findViewById(R.id.starttime);
        stopTime = (Button) findViewById(R.id.stoptime);
        time = (TextView) findViewById(R.id.time);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gettime:
                time.setText(inputet.getText().toString());//当前时间
                i = Integer.parseInt(inputet.getText().toString());//当前输入时间
                break;
            case R.id.starttime://开始自动减时
                startTime();
                break;
            case R.id.stoptime://停止自动减时
                stopTime();

                studytime = String.valueOf(s);
                SendByHttpClient(studytime);

                break;
        }
    }
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            time.setText(msg.arg1+"");
            startTime();
        };
    };
    /**
     * 开始自动减时
     */
    private void startTime() {
        if(timer==null){
            timer = new Timer();
        }

        timerTask = new TimerTask() {

            @Override
            public void run() {
                i--;//自动减1
                s++;
                Message message = Message.obtain();
                message.arg1=i;
                mHandler.sendMessage(message);//发送消息
            }
        };
        timer.schedule(timerTask, 1000);//1000ms执行一次
    }
    /**
     * 停止自动减时
     */
    private void stopTime() {
        if(timer!=null)
            timer.cancel();
    }

    public void SendByHttpClient(final String studyti ){
        final String timerUrlStr=Constant.URL_Timer+"?studytime="+studyti;
        System.out.print(timerUrlStr);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(timerUrlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    //设置请求方法
                    connection.setRequestMethod("GET");
                    //设置连接超时时间（毫秒）
                    connection.setConnectTimeout(5000);
                    //设置读取超时时间（毫秒）
                    connection.setReadTimeout(5000);

                    //返回输入流
                    InputStream in = connection.getInputStream();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {//关闭连接
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }



}




















