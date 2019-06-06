package com.example.genelin.chatroomtest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
//主界面
public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button timer=(Button)findViewById(R.id.button4);

        Button chatroom=(Button)findViewById(R.id.button5);

        Button fabu=(Button)findViewById(R.id.button3);

        Button list=(Button)findViewById(R.id.button2);

        chatroom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i =new Intent (MainActivity.this,Chatroom.class);
                startActivity(i);
            }
        });

        timer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i =new Intent (MainActivity.this,TimerActivity.class);
                startActivity(i);
            }
        });

        fabu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i =new Intent (MainActivity.this,FabuActivity.class);
                startActivity(i);
            }
        });

        list.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i =new Intent (MainActivity.this,ListActivity.class);
                startActivity(i);
            }
        });

    }

}
