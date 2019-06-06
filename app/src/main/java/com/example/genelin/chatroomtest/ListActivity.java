package com.example.genelin.chatroomtest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//import com.wangj.baselibrary.http.bean.CommonRequest;
//import com.wangj.baselibrary.http.bean.CommonResponse;
//import com.wangj.baselibrary.http.interf.ResponseHandler;
//import com.wangj.baselibrary.util.DialogUtil;
//import com.wangj.baselibrary.util.LoadingDialogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//排行榜板块
public class ListActivity extends BaseActivity {
    private String URL_PRODUCT = "http://172.16.92.204:8080/STUforest/ProductServlet";
    ListView lvProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        lvProduct = (ListView) findViewById(R.id.lv);

        getListData();
    }

    private void getListData() {
        CommonRequest request = new CommonRequest();
        sendHttpPostRequest(URL_PRODUCT, request, new ResponseHandler() {
            @Override
            public void success(CommonResponse response) {
                LoadingDialogUtil.cancelLoading();

                if (response.getDataList().size() > 0) {
                    ProductAdapter adapter = new ProductAdapter(ListActivity.this, response.getDataList());
                    lvProduct.setAdapter(adapter);
                } else {
                    DialogUtil.showHintDialog(ListActivity.this, "列表数据为空", true);
                }
            }

            @Override
            public void fail(String failCode, String failMsg) {
                LoadingDialogUtil.cancelLoading();
            }
        }, true);
    }

    private static class ProductAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String, String>> list;

        ProductAdapter(Context context, ArrayList<HashMap<String, String>> list) {
            this.context = context;
            this.list = list;
        }

        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
                holder = new ViewHolder();
                holder.tvRank = (TextView) convertView.findViewById(R.id.tv_rank);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
                holder.tvLikes = (TextView) convertView.findViewById(R.id.tv_likes);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> map = list.get(position);
            holder.tvRank.setText(map.get("rank"));
            holder.tvName.setText(map.get("name"));
            holder.tvTime.setText(map.get("time"));
            holder.tvLikes.setText(map.get("likes"));

            return convertView;
        }

        private static class ViewHolder {
            private TextView tvRank;
            private TextView tvName;
            private TextView tvTime;
            private TextView tvLikes;
        }
    }
}

class HttpPostTask extends AsyncTask<String, String, String> {

    /** BaseActivity 中基础问题的处理 handler */
    public Handler mHandler;

    /** 返回信息处理回调接口 */
    private ResponseHandler rHandler;

    /** 请求类对象 */
    private CommonRequest request;

    public HttpPostTask(CommonRequest request,
                        Handler mHandler,
                        ResponseHandler rHandler) {
        this.request = request;
        this.mHandler = mHandler;
        this.rHandler = rHandler;
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder resultBuf = new StringBuilder();
        try {
            URL url = new URL(params[0]);

            // 第一步：使用URL打开一个HttpURLConnection连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 第二步：设置HttpURLConnection连接相关属性
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestMethod("POST"); // 设置请求方法，“POST或GET”
            connection.setConnectTimeout(8000); // 设置连接建立的超时时间
            connection.setReadTimeout(8000); // 设置网络报文收发超时时间
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // 如果是POST方法，需要在第3步获取输入流之前向连接写入POST参数
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(request.getJsonStr());
            out.flush();

            // 第三步：打开连接输入流读取返回报文 -> *注意*在此步骤才真正开始网络请求
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 通过连接的输入流获取下发报文，然后就是Java的流处理
                InputStream in = connection.getInputStream();
                BufferedReader read = new BufferedReader(new InputStreamReader(in));
                String line;
                while((line = read.readLine()) != null) {
                    resultBuf.append(line);
                }
                return resultBuf.toString();
            } else {
                // 异常情况，如404/500...
                mHandler.obtainMessage(Constant.HANDLER_HTTP_RECEIVE_FAIL,
                        "[" + responseCode + "]" + connection.getResponseMessage()).sendToTarget();
            }
        } catch (IOException e) {
            // 网络请求过程中发生IO异常
            mHandler.obtainMessage(Constant.HANDLER_HTTP_SEND_FAIL,
                    e.getClass().getName() + " : " + e.getMessage()).sendToTarget();
        }
        return resultBuf.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        if (rHandler != null) {
            if (!"".equals(result)) {
				/* 交易成功时需要在处理返回结果时手动关闭Loading对话框，可以灵活处理连续请求多个接口时Loading框不断弹出、关闭的情况 */

                CommonResponse response = new CommonResponse(result);
                // 这里response.getResCode()为多少表示业务完成也是和服务器约定好的
                if ("0".equals(response.getResCode())) { // 正确
                    rHandler.success(response);
                } else {
                    rHandler.fail(response.getResCode(), response.getResMsg());
                }
            }
        }
    }

}



class CommonRequest {
    /**
     * 请求码，类似于接口号（在本文中用Servlet做服务器时暂时用不到）
     */
    private String requestCode;
    /**
     * 请求参数
     * （说明：这里只用一个简单map类封装请求参数，对于请求报文需要上送一个数组的复杂情况需要自己再加一个ArrayList类型的成员变量来实现）
     */
    private HashMap<String, String> requestParam;

    public CommonRequest() {
        requestCode = "";
        requestParam = new HashMap<>();
    }

    /**
     * 设置请求代码，即接口号，在本例中暂时未用到
     */
    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    /**
     * 为请求报文设置参数
     * @param paramKey 参数名
     * @param paramValue 参数值
     */
    public void addRequestParam(String paramKey, String paramValue) {
        requestParam.put(paramKey, paramValue);
    }

    /**
     * 将请求报文体组装成json形式的字符串，以便进行网络发送
     * @return 请求报文的json字符串
     */
    public String getJsonStr() {
        // 由于Android源码自带的JSon功能不够强大（没有直接从Bean转到JSonObject的API），为了不引入第三方资源这里我们只能手动拼装一下啦
        JSONObject object = new JSONObject();
        JSONObject param = new JSONObject(requestParam);
        try {
            // 下边2个"requestCode"、"requestParam"是和服务器约定好的请求体字段名称，在本文接下来的服务端代码会说到
            object.put("requestCode", requestCode);
            object.put("requestParam", param);
        } catch (JSONException e) {
            LogUtil.logErr("请求报文组装异常：" + e.getMessage());
        }
        // 打印原始请求报文
        LogUtil.logRequest(object.toString());
        return object.toString();
    }
}

class CommonResponse {

    /**
     * 交易状态代码
     */
    private String resCode = "";

    /**
     * 交易失败说明
     */
    private String resMsg = "";

    /**
     * 简单信息
     */
    private HashMap<String, String> propertyMap;

    /**
     * 列表类信息
     */
    private ArrayList<HashMap<String, String>> mapList;

    /**
     * 通用报文返回构造函数
     *
     * @param responseString Json格式的返回字符串
     */
    public CommonResponse(String responseString) {

        // 日志输出原始应答报文
        LogUtil.logResponse(responseString);

        propertyMap = new HashMap<>();
        mapList = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(responseString);

            /* 说明：
                以下名称"resCode"、"resMsg"、"property"、"list"
                和请求体中提到的字段名称一样，都是和服务器程序开发者约定好的字段名字，在本文接下来的服务端代码会说到
             */
            resCode = root.getString("resCode");
            resMsg = root.optString("resMsg");

            JSONObject property = root.optJSONObject("property");
            if (property != null) {
                parseProperty(property, propertyMap);
            }

            JSONArray list = root.optJSONArray("list");
            if (list != null) {
                parseList(list);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 简单信息部分的解析到{@link CommonResponse#propertyMap}
     *
     * @param property  信息部分
     * @param targetMap 解析后保存目标
     */
    private void parseProperty(JSONObject property, HashMap<String, String> targetMap) {
        Iterator<?> it = property.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            Object value = property.opt(key);
            targetMap.put(key, value.toString());
        }
    }

    /**
     * 解析列表部分信息到{@link CommonResponse#mapList}
     *
     * @param list 列表信息部分
     */
    private void parseList(JSONArray list) {
        int i = 0;
        while (i < list.length()) {
            HashMap<String, String> map = new HashMap<>();
            try {
                parseProperty(list.getJSONObject(i++), map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mapList.add(map);
        }
    }

    public String getResCode() {
        return resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public HashMap<String, String> getPropertyMap() {
        return propertyMap;
    }

    public ArrayList<HashMap<String, String>> getDataList() {
        return mapList;
    }
}

class TimerThread extends Thread {

    private int seconds;
    private Handler mHandler;

    private String contralStr = "";
    private boolean pauseState;

    /**
     * 倒计时后台线程
     * @param mHandler 页面处理handler(msg.what=101-倒计时减一秒，arg1-剩余秒数；102-倒计时暂停；1001-倒计时结束)
     * @param seconds 总秒数，单位为秒
     */
    public TimerThread(Handler mHandler, int seconds) {
        this.seconds = seconds;
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        super.run();
        Message message;
        try {
            for (int i = seconds; i >= 0; i--) {
                synchronized (contralStr) {
                    if(pauseState){
                        i = i + 1;
                        contralStr.wait();
                    } else {
                        message = new Message();
                        message.what = 101;
                        message.arg1 = i;
                        mHandler.sendMessage(message); // 倒计时减一秒
                        Thread.sleep(995);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessage(1001); // 倒计时完毕
    }

    /**
     * 倒计时线程暂停
     */
    public void pause(){
        if(!pauseState){
            mHandler.sendEmptyMessage(102);
            pauseState = true;
        }
    }

    /**
     * 倒计时线程继续
     */
    public void continueGo(){
        if(pauseState){
            synchronized (contralStr) {
                pauseState = false;
                contralStr.notifyAll();
            }
        }
    }

    /**
     * 检查当前线程暂停状态
     * @return
     */
    public boolean isPauesed(){
        return pauseState;
    }

}

class DialogUtil {

    private static Dialog dialog; // 只使用一个Dialog实例
    private static View hintView;
    private static View decideView;

    /**
     * 提示框（无标题，一个确认键）
     *
     * @param context             上下文
     * @param msgStr              提示内容文字
     * @param exitCurrentActivity 点击确认按钮是否退出当前Activity
     */
    public static void showHintDialog(Context context,
                                      String msgStr,
                                      boolean exitCurrentActivity) {
        hintView = prepareHintView(context, false, null, msgStr, "确定", exitCurrentActivity);
        dialog = createDialog(context, hintView, false);
        dialog.show();
    }

    /**
     * 提示框（无标题，一个确认键）
     *
     * @param context             上下文
     * @param msgStrId            提示内容文字资源ID
     * @param exitCurrentActivity 点击确认按钮是否退出当前Activity
     */
    public static void showHintDialog(Context context,
                                      int msgStrId,
                                      boolean exitCurrentActivity) {
        showHintDialog(context, context.getResources().getString(msgStrId), exitCurrentActivity);
    }

    /**
     * 提示框（无标题，一个确认键）
     *
     * @param context  上下文
     * @param msgStr   提示内容文字
     * @param listener 确认按钮响应事件
     */
    public static void showHintDialog(Context context,
                                      String msgStr,
                                      View.OnClickListener listener) {
        showHintDialog(context, false, null, msgStr, "确定", listener);
    }

    /**
     * 提示框（无标题，一个确认键）
     *
     * @param context  上下文
     * @param msgStrId 提示内容文字资源ID
     * @param listener 确认按钮响应事件
     */
    public static void showHintDialog(Context context,
                                      int msgStrId,
                                      View.OnClickListener listener) {
        showHintDialog(context, context.getResources().getString(msgStrId), listener);
    }

    /**
     * 提示框（带标题，一个确认按钮）
     *
     * @param context             上下文
     * @param titleStr            标题文字
     * @param msgStr              内容显示文字
     * @param exitCurrentActivity 点击确认按钮是否退出当前Activity
     */
    public static void showHintDialogWithTitle(Context context,
                                               String titleStr,
                                               String msgStr,
                                               boolean exitCurrentActivity) {
        hintView = prepareHintView(context, true, titleStr, msgStr, "确定",
                exitCurrentActivity);
        dialog = createDialog(context, hintView, false);
        dialog.show();
    }

    /**
     * 提示框（带标题，一个确认按钮）
     *
     * @param context             上下文
     * @param titleStrId          标题文字资源ID
     * @param msgStrId            内容显示文字资源ID
     * @param exitCurrentActivity 点击确认按钮是否退出当前Activity
     */
    public static void showHintDialogWithTitle(Context context,
                                               int titleStrId,
                                               int msgStrId,
                                               boolean exitCurrentActivity) {
        showHintDialogWithTitle(context, context.getResources().getString(titleStrId),
                context.getResources().getString(msgStrId), exitCurrentActivity);
    }

    /**
     * 提示框（带标题，一个确认按钮）
     *
     * @param context  上下文
     * @param titleStr 标题文字
     * @param msgStr   内容显示文字
     * @param listener 点击确认按钮响应事件
     */
    public static void showHintDialogWithTitle(Context context,
                                               String titleStr,
                                               String msgStr,
                                               View.OnClickListener listener) {
        showHintDialog(context, true, titleStr, msgStr, "确定", listener);
    }

    /**
     * 提示框（带标题，一个确认按钮）
     *
     * @param context    上下文
     * @param titleStrId 标题文字资源ID
     * @param msgStrId   内容显示文字资源ID
     * @param listener   点击确认按钮响应事件
     */
    public static void showHintDialogWithTitle(Context context,
                                               int titleStrId,
                                               int msgStrId,
                                               View.OnClickListener listener) {
        showHintDialog(context, true, context.getResources().getString(titleStrId),
                context.getResources().getString(msgStrId), "确定", listener);
    }

    /**
     * 提示框（全属性：标题、内容、按钮文字、按钮响应）
     *
     * @param context   上下文
     * @param showTitle 是否显示标题
     * @param titleStr  标题文字
     * @param msgStr    内容文字
     * @param btnStr    按钮文字
     * @param listener  按钮响应监听
     */
    public static void showHintDialog(Context context,
                                      boolean showTitle,
                                      String titleStr,
                                      String msgStr,
                                      String btnStr,
                                      View.OnClickListener listener) {
        hintView = prepareHintView(context, showTitle, titleStr, msgStr, btnStr, listener);
        dialog = createDialog(context, hintView, false);
        dialog.show();
    }

    /**
     * 准备提示框View（无按钮响应监听）
     */
    private static View prepareHintView(final Context context,
                                        boolean showTitle,
                                        String title,
                                        String content,
                                        String btnString,
                                        final boolean exitCurrentActivity) {
        hintView = LayoutInflater.from(context).inflate(R.layout.dialog_hint, null);
        TextView tvTitle = (TextView) hintView.findViewById(R.id.tv_title);
        tvTitle.setText(title);

        TextView tvContent = (TextView) hintView.findViewById(R.id.tv_content);
        tvContent.setText(content);

        Button btnIKnow = (Button) hintView.findViewById(R.id.btn_iknow);
        btnIKnow.setText(btnString);
        btnIKnow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                if (exitCurrentActivity) {
                    dismissDialog();
                    ((Activity) context).finish();
                }
            }
        });

        hintView.findViewById(R.id.tv_title).setVisibility(
                showTitle ? View.VISIBLE : View.GONE);

        return hintView;
    }

    /**
     * 准备提示框View（带按钮响应监听）
     */
    private static View prepareHintView(final Context context,
                                        boolean showTitle,
                                        String title,
                                        String content,
                                        String btnString,
                                        View.OnClickListener listener) {
        hintView = LayoutInflater.from(context).inflate(R.layout.dialog_hint, null);

        TextView tvTitle = (TextView) hintView.findViewById(R.id.tv_title);
        tvTitle.setText(title);

        TextView tvContent = (TextView) hintView.findViewById(R.id.tv_content);
        tvContent.setText(content);

        Button btnIKnow = (Button) hintView.findViewById(R.id.btn_iknow);
        btnIKnow.setText(btnString);
        if (listener != null) {
            btnIKnow.setOnClickListener(listener);
        }

        hintView.findViewById(R.id.tv_title).setVisibility(
                showTitle ? View.VISIBLE : View.GONE);

        return hintView;
    }

    /**
     * 无标题选择对话框
     *
     * @param context         上下文
     * @param content         内容文字
     * @param cancelListener  左侧按钮监听
     * @param confirmListener 右侧按钮监听
     */
    public static void showDecideDialogNoTitle(Context context,
                                               String content,
                                               View.OnClickListener cancelListener,
                                               View.OnClickListener confirmListener) {
        decideView = prepareDecideView(context, false, null, content,
                "取消", cancelListener, "确定", confirmListener);
        dialog = createDialog(context, decideView, false);
        dialog.show();
    }

    /**
     * 无标题选择对话框
     *
     * @param context         上下文
     * @param content         内容文字
     * @param cancelStr       左侧按钮文字
     * @param cancelListener  左侧按钮监听
     * @param confirmStr      右侧按钮文字
     * @param confirmListener 右侧按钮监听
     */
    public static void showDecideDialogNoTitle(Context context,
                                               String content,
                                               String cancelStr,
                                               View.OnClickListener cancelListener,
                                               String confirmStr,
                                               View.OnClickListener confirmListener) {
        decideView = prepareDecideView(context, false, null, content,
                cancelStr, cancelListener, confirmStr, confirmListener);
        dialog = createDialog(context, decideView, false);
        dialog.show();
    }

    /**
     * 带标题的选择对话框
     *
     * @param context         上下文
     * @param title           标题文字
     * @param content         内容文字
     * @param cancelListener  左侧取消按钮监听
     * @param confirmListener 右侧确定按钮监听
     */
    public static void showDecideDialogWithTitle(Context context,
                                                 String title,
                                                 String content,
                                                 View.OnClickListener cancelListener,
                                                 View.OnClickListener confirmListener) {
        decideView = prepareDecideView(context, true, title, content,
                "取消", cancelListener, "确定", confirmListener);
        dialog = createDialog(context, decideView, false);
        dialog.show();
    }

    /**
     * 带标题的选择对话框
     *
     * @param context         上下文
     * @param title           标题文字
     * @param content         内容文字
     * @param cancelStr       左侧按钮文字
     * @param cancelListener  左侧按钮监听
     * @param confirmStr      右侧按钮文字
     * @param confirmListener 右侧按钮监听
     */
    public static void showDecideDialogWithTitle(Context context,
                                                 String title,
                                                 String content,
                                                 String cancelStr,
                                                 View.OnClickListener cancelListener,
                                                 String confirmStr,
                                                 View.OnClickListener confirmListener) {
        decideView = prepareDecideView(context, true, title, content,
                cancelStr, cancelListener, confirmStr, confirmListener);
        dialog = createDialog(context, decideView, false);
        dialog.show();
    }

    /**
     * 准备选择对话框View
     */
    private static View prepareDecideView(Context context,
                                          boolean showTitle,
                                          String title,
                                          String content,
                                          String cancelStr,
                                          View.OnClickListener cancelListener,
                                          String confirmStr,
                                          View.OnClickListener confirmListener) {
        decideView = LayoutInflater.from(context).inflate(
                R.layout.dialog_decide, null);
        TextView tvTitle = (TextView) decideView.findViewById(R.id.tv_title);
        tvTitle.setText(title);
        TextView tvContent = (TextView) decideView
                .findViewById(R.id.tv_content);
        tvContent.setText(content);
        Button btnCancel = (Button) decideView.findViewById(R.id.btn_cancel);
        btnCancel.setText(cancelStr);
        btnCancel.setOnClickListener(cancelListener);
        Button btnConfirm = (Button) decideView.findViewById(R.id.btn_confirm);
        btnConfirm.setText(confirmStr);
        btnConfirm.setOnClickListener(confirmListener);
        decideView.findViewById(R.id.tv_title)
                .setVisibility(showTitle ? View.VISIBLE : View.GONE);
        return decideView;
    }

    /**
     * 显示自定义对话框
     *
     * @param context    上下文
     * @param view       自定义对话框显示的View
     * @param cancelable 点击Back或对话框以外时候取消对话框
     */
    public static void showCustomDialog(Context context, View view, boolean cancelable) {
        if (dialog == null) {
            dialog = new Dialog(context, R.style.MyDialogStyle);
        }
        dialog.setContentView(view);
        dialog.setCancelable(cancelable);
        dialog.show();
    }

    /**
     * 创建对话框
     */
    private static Dialog createDialog(Context context, View view,
                                       boolean cancelable) {
        if (dialog == null) {
            dialog = new Dialog(context, R.style.MyDialogStyle);
        }
        dialog.setContentView(view);
        dialog.setCancelable(cancelable);
        return dialog;
    }

    /**
     * 关闭对话框
     */
    public static void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}

class LoadingDialogUtil {

    private static Dialog loadDialog;
    private static View loadingView;

    public static void showLoadingDialog(Context context) {
        if(loadDialog == null) {
            prepareLoadingView(context, true, "正在为您加载，请稍候...");
            loadDialog = new Dialog(context, R.style.MyDialogStyle);
            loadDialog.setContentView(loadingView);
            loadDialog.setCancelable(false);
            loadDialog.show();
        }
    }

    public static void cancelLoading() {
        if(loadDialog != null
                && loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
    }

    /**
     * 准备进度对话框
     */
    private static void prepareLoadingView(final Context context,
                                           boolean showLoadingStr, String loadingStr) {
        loadingView = LayoutInflater.from(context).inflate(
                R.layout.dialog_loading, null);
        ImageView img = (ImageView) loadingView.findViewById(R.id.img_loading);
        AnimationDrawable anim = (AnimationDrawable) img.getDrawable();
        anim.start();

        TextView tvTitle = (TextView) loadingView.findViewById(R.id.tv_loading);
        tvTitle.setText(loadingStr);
        tvTitle.setVisibility(showLoadingStr ? View.VISIBLE : View.INVISIBLE);
    }

}

class LogUtil {

    public static boolean showRunningLog;

    public static boolean showHttpDataLog;

    private static String TAG = "WangJ";

    /**
     * 日志输出单个字符串
     *
     * @param value 日志内容
     */
    public static void log(String value){
        if(showRunningLog){
            Log.i(TAG, value);
        }
    }

    /**
     * 日志输出单个字符串
     *
     * @param key 待输出字符串含义的标注
     * @param value 日志内容
     */
    public static void log(String key, String value){
        if(showRunningLog){
            Log.i(TAG, key + ": " + value);
        }
    }

    /**
     * 日志输出字符串数组
     *
     * @param key 待输出字符串含义的标注
     * @param strArray 要查看的数组
     */
    public static void log(String key, String[] strArray){
        if(showRunningLog){
            Log.i(TAG, key + ":");
            for (int i = 0; i < strArray.length; i++) {
                Log.i(TAG, "item[" + i + "] : " + strArray[i]);
            }
        }
    }

    /**
     * 日志输出HashMap
     *
     * @param key 待输出字符串含义的标注
     * @param map 要查看的map
     */
    public static void log(String key, HashMap<String, String> map){
        if(showRunningLog){
            Log.i(TAG, key + ":");
            for (HashMap.Entry<String, String> entry : map.entrySet()) {
                Log.i(TAG, entry.getKey() + " : " + entry.getValue());
            }
        }
    }

    /**
     * 打印请求参数
     * @param requestStr 请求内容
     */
    public static void logRequest(String requestStr){
        if(showHttpDataLog){
            Log.i(TAG, "The RequestStr：\n" + requestStr);
        }
    }

    /**
     * 打印服务器返回结果字符串
     * @param responseStr 报文返回字符串
     */
    public static void logResponse(String responseStr){
        if(showHttpDataLog){
            Log.i(TAG, "The ResponseStr：\n" + responseStr);
        }
    }

    /**
     * Error 错误记录
     * @param errMsg 错误信息
     */
    public static void logErr(String errMsg){
        Log.e(TAG, errMsg);
    }
}

interface ResponseHandler {

    /**
     * 交易成功的处理
     * @param response 格式化报文
     */
    void success(CommonResponse response);

    /**
     * 报文通信正常，但交易内容失败的处理
     * @param failCode 返回的交易状态码
     * @param failMsg 返回的交易失败说明
     */
    void fail(String failCode, String failMsg);
}

class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void sendHttpPostRequest(String url, CommonRequest request, ResponseHandler responseHandler, boolean showLoadingDialog) {
        new HttpPostTask(request, mHandler, responseHandler).execute(url);
        if(showLoadingDialog) {
            LoadingDialogUtil.showLoadingDialog(BaseActivity.this);
        }
    }

    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == Constant.HANDLER_HTTP_SEND_FAIL) {
                LogUtil.logErr(msg.obj.toString());

                LoadingDialogUtil.cancelLoading();
                DialogUtil.showHintDialog(BaseActivity.this, "请求发送失败，请重试", true);
            } else if (msg.what == Constant.HANDLER_HTTP_RECEIVE_FAIL) {
                LogUtil.logErr(msg.obj.toString());

                LoadingDialogUtil.cancelLoading();
                DialogUtil.showHintDialog(BaseActivity.this, "请求接受失败，请重试", true);
            }
        }
    };
}


