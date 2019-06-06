package com.example.genelin.chatroomtest;

/**
 * Created by GeneLin on 2019/4/12.
 */

//伪全局变量
public class Constant {

    public static String URL = "http://172.16.92.204:8080/STUforest/";

    public static String URL_Chat = URL + "chat";

    public static String URL_Timer = URL+"MyServlet";

    public static String URL_Fabu = URL+"Fabu";

    static final int HANDLER_HTTP_SEND_FAIL = 1001;
    static final int HANDLER_HTTP_RECEIVE_FAIL = 1002;

    /*
     * Activity跳转用RequestCode
     */
    public static int REQUEST_CODE_ = 0;

    /*
     * Activity跳转用ResponseCode
     */
    public static int RESPONSE_CODE_SUCCESS = 100;
    public static int RESPONSE_CODE_FAIL = 101;
    public static int RESPONSE_CODE_NO_RESULT = 102;
}

