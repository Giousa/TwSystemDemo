package com.zmm.twsystemdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zmm.twsystemdemo.client.Client;
import com.zmm.twsystemdemo.utils.ToastUtils;
import com.zmm.twsystemdemo.utils.UIUtils;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Message;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Client.ClientListener {

    private Client mClient;
    private String hostIp = "192.168.43.1";
    private TextView mTvContent;
    private String mSimulateID;

    //计时器
    private Timer mTimer = null;
    private MyTimerTask mTimerTask = null;
    private static int count = 0;
    private boolean isPause = false;
    private static int delay = 1000;  //1s
    private static int period = 1000;  //1s
    private static final int UPDATE_TEXTVIEW = 0;
    private int mSpeed = 10;
    private Button mConnect;
    private Button mSend;
    private Button mStop;
    private Button mClear;

    private boolean isOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnect = findViewById(R.id.btn_connect);
        mSend = findViewById(R.id.btn_send);
        mStop = findViewById(R.id.btn_stop);
        mClear = findViewById(R.id.btn_clear);
        mTvContent = findViewById(R.id.tv_content);

    }

    @Override
    protected void onResume() {
        super.onResume();

        initView();
    }

    private void initView() {


//        //模拟json
//        mSimulateID = "requestData={\"loginId\":\"TW2018-TEST-ID\",\"s_id\":\"TW2018-TEST-ID\",\"s_name\":\"TW2018\",\"curSpeed\":\"25\",\"curResistance\":\"3\",\"curDirection\":\"1\",\"calories\":\".000\",\"passiveMileage\":\".000\",\"spasmLevel\":\"6\",\"spasmTimes\":\"0\"}";

        mConnect.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mStop.setOnClickListener(this);

        //初始化客户端
        mClient = new Client(hostIp);
        mClient.setClientListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.btn_connect:
                connectToDevice();
                break;

            case R.id.btn_send:

                if(isOk){
                    startTimer();
                }else {
                    ToastUtils.SimpleToast("当前未连接");
                }
                break;

            case R.id.btn_stop:
                stopData();
                break;

            case R.id.btn_clear:
                mTvContent.setText("");
                break;
        }


    }

    /**
     * 连接机顶盒
     */
    private void connectToDevice() {

        mConnect.setClickable(false);
        mConnect.setText("正在连接中...");

        //连接
        new Thread(new Runnable() {
            @Override
            public void run() {
                mClient.start();
            }
        }).start();

    }


    private void stopData() {
        mSpeed = 0;
        //模拟json
        mSimulateID = "requestData={\"loginId\":\"TW2018-TEST-ID\",\"s_id\":\"TW2018-TEST-ID\",\"s_name\":\"TW2018\",\"curSpeed\":\""+mSpeed+"\",\"curResistance\":\"3\",\"curDirection\":\"1\",\"calories\":\".000\",\"passiveMileage\":\".000\",\"spasmLevel\":\"6\",\"spasmTimes\":\"0\"}";


        try {
            mClient.sendData(mSimulateID);
            mTvContent.append("发送圈数："+mSpeed+" rpm\n");
        } catch (Exception e) {
            mTvContent.append("数据发送失败\n");
            e.printStackTrace();
        }

        stopTimer();
    }


    @Override
    public void onClientListener(final String msg, final int flag) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvContent.append(msg+"\n");
                switch (flag){
                    case 0:
                        isOk = false;
                        mConnect.setClickable(true);
                        mConnect.setText("连接失败");
                        break;

                    case 1:
                        isOk = true;
                        mConnect.setClickable(false);
                        mConnect.setText("连接成功");
                        break;

                    case 2:
                        isOk = false;
                        mConnect.setClickable(false);
                        mConnect.setText("继续连接...");
                        break;

                    case 3:
                        isOk = false;
                        mConnect.setClickable(true);
                        mConnect.setText("连接异常");
                        break;
                }

            }
        });
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXTVIEW:
                    updateTextView();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 发送数据到机顶盒
     */
    private void updateTextView(){

        mSpeed++;
        if(mSpeed >= 60){
            mSpeed = 5;
        }

        //模拟json
        mSimulateID = "requestData={\"loginId\":\"TW2018-TEST-ID\",\"s_id\":\"TW2018-TEST-ID\",\"s_name\":\"TW2018\",\"curSpeed\":\""+mSpeed+"\",\"curResistance\":\"3\",\"curDirection\":\"1\",\"calories\":\".000\",\"passiveMileage\":\".000\",\"spasmLevel\":\"6\",\"spasmTimes\":\"0\"}";


        try {
            mClient.sendData(mSimulateID);
            mTvContent.append("发送圈数："+mSpeed+" rpm\n");
        } catch (Exception e) {
            mTvContent.append("数据发送失败\n");
            e.printStackTrace();
        }

    }

    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = new MyTimerTask();
        }
        if (mTimerTask == null) {
            mTimerTask = new MyTimerTask();
        }
        if(mTimer != null ){
            mTimer.schedule(mTimerTask, delay, period);
        }
    }


    class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            sendMessage(UPDATE_TEXTVIEW);
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } while (isPause);
            count ++;
        }
    }

    private void stopTimer(){
        cancelTime();
        count = 0;
    }

    public void sendMessage(int id){
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }

    private void cancelTime(){
        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTime();
    }

}
