package com.example.genelin.chatroomtest;


        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.os.Handler;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;
//发布板块
public class FabuActivity extends AppCompatActivity implements OnClickListener{
    private final static String LOGIN_URL = "";
    private EditText editName, editUser, editPhone, editPlace;
    private Button btnsubmit_post;
    private String result = "";

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Toast.makeText(FabuActivity.this, result, Toast.LENGTH_SHORT).show();
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fabu);

        initView();
        setView();
    }
    private void initView() {
        editName = (EditText) findViewById(R.id.Name);
        editUser = (EditText) findViewById(R.id.User);
        editPhone = (EditText) findViewById(R.id.Phone);
        editPlace = (EditText) findViewById(R.id.Place);
        btnsubmit_post = (Button) findViewById(R.id.submit_post);
    }

    private void setView() {
        btnsubmit_post.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        new Thread() {
            public void run() {
                result = PostUtils.LoginByPost(editName.getText().toString(), editUser.getText().toString(),editPlace.getText().toString(),editPhone.getText().toString());
                handler.sendEmptyMessage(0x123);
            };
        }.start();
    };


}
