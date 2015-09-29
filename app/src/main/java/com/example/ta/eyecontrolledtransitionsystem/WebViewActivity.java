package com.example.ta.eyecontrolledtransitionsystem;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class WebViewActivity extends AppCompatActivity implements TabHost.OnTabChangeListener{

    private boolean findFlag;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private SerialThread serialThread;
    private MyWebView myWebView;

    private final int SCROLL_DOWN = 65;
    private final int SCROLL_UP = 66;
    private final int MUSIC_PLAY_OR_PAUSE = 2;
    private final int MUSIC_PREVIOUS_SONG = 3;
    private final int MUSIC_NEXT_SONG = 4;
    private final String DEVICE_NAME = "gig";
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public final String TAG = "WebViewAcytivity";
    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1");
        tab1.setIndicator("Yahoo!");
        tab1.setContent(R.id.first_content);
        tabHost.addTab(tab1);


        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2");
        tab2.setIndicator("YouTube");
        tab2.setContent(R.id.second_content);
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("tab3");
        tab3.setIndicator("niconico");
        tab3.setContent(R.id.third_content);
        tabHost.addTab(tab3);

        TabHost.TabSpec tab4 = tabHost.newTabSpec("tab4");
        tab4.setIndicator("google");
        tab4.setContent(R.id.forth_content);
        tabHost.addTab(tab4);


        tabHost.setOnTabChangedListener(this);

        myWebView = (MyWebView)findViewById(R.id.webView1);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl("http://www.yahoo.co.jp/");

        myWebView.setOnTouchEventCallback(new MyWebView.OnTouchEventCallback() {

            public void onTouchStateChanged(int state) {
                switch (state) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP");
                        break;
                }
            }
        });

        handler = new Handler(){
            public void handleMessage(Message message) {
                Log.d(TAG,"message received");
                switch(message.what){
                    case SCROLL_DOWN:
                        scrollDown();
                        break;
                    case SCROLL_UP:
                        scrollUp();
                        break;
                    case MUSIC_PLAY_OR_PAUSE:
                        Log.d(TAG,"MUSIC_PLAY_OR_PAUSE");
                        break;
                    case MUSIC_PREVIOUS_SONG:
                        Log.d(TAG, "MUSIC_PREVIOUS_SONG");
                        break;
                    case MUSIC_NEXT_SONG:
                        Log.d(TAG, "MUSIC_NEXT_SONG");
                        break;
                }
            }
        };

        /*---Bluetoothデバイスを検索してbluetoothDeviceに代入-------------------------------------*/

        findFlag = false;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : devices){
            Log.d(TAG,"device name:" + device.getName());
            if(device.getName().equals(DEVICE_NAME)){
                Log.d(TAG,"find bluetooth device:" + DEVICE_NAME);
                Toast.makeText(this, "デバイスを発見できました", Toast.LENGTH_SHORT).show();
                bluetoothDevice = device;
                findFlag = true;
            }
        }

        /*----------------------------------------------------------------------------------------*/

        /*---デバイスと接続し通信を始める---------------------------------------------------------*/

        if(findFlag){
            try{
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                Log.d(TAG, "complete connecting");
                Toast.makeText(this,"デバイスとの接続が完了しました",Toast.LENGTH_SHORT).show();

                serialThread = new SerialThread(bluetoothSocket,this);
                serialThread.start();
                Log.d(TAG, "start communicating");
                Toast.makeText(this, "通信を開始します", Toast.LENGTH_LONG).show();
            }catch(Exception e){
                Toast.makeText(this, "デバイスと接続できませんでした", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"デバイスを見つけられませんでした",Toast.LENGTH_LONG).show();
        }

        /*----------------------------------------------------------------------------------------*/
    }

    public void checkData(byte[] receiveData,int receiveDataLength){
        Log.d(TAG,"receiveDataLength:" + receiveDataLength);
        for(int i=0; i<receiveDataLength; i++){
            Log.d(TAG,"receiveData(" + i + "):" + receiveData[i]);
            switch (receiveData[0]){
                case SCROLL_DOWN:
                    Log.d(TAG, "SCROLL_DOWN");
                    scrollUp();
                    break;
                case SCROLL_UP:
                    Log.d(TAG, "SCROLL_UP");
                    scrollDown();
                    break;
                case MUSIC_PLAY_OR_PAUSE:
                    Log.d(TAG,"MUSIC_PLAY_OR_PAUSE");
                    musicPlayOrPause();
                    break;
                case MUSIC_PREVIOUS_SONG:
                    Log.d(TAG,"MUSIC_PREVIOUS_SONG");
                    musicPreviousSong();
                    break;
                case MUSIC_NEXT_SONG:
                    Log.d(TAG,"MUSIC_NEXT_SONG");
                    musicNextSong();
                    break;
            }
        }
    }

    public void onTabChanged(String tabId){
        switch (tabId){
            case "tab1":
                Log.d(TAG,"changed tab1");
                myWebView = (MyWebView) findViewById(R.id.webView1);
                myWebView.setWebViewClient(new WebViewClient());
                myWebView.loadUrl("http://www.yahoo.co.jp/");
                break;
            case "tab2":
                Log.d(TAG,"changed tab2");
                myWebView = (MyWebView) findViewById(R.id.webView2);
                myWebView.setWebViewClient(new WebViewClient());
                myWebView.getSettings().setJavaScriptEnabled(true);
                myWebView.loadUrl("https://www.youtube.com/?gl=JP&tab=w1");
                break;
            case "tab3":
                Log.d(TAG,"changed tab3");
                myWebView = (MyWebView) findViewById(R.id.webView3);
                myWebView.setWebViewClient(new WebViewClient());
                myWebView.loadUrl("http://www.nicovideo.jp/");
                break;
            case "tab4":
                myWebView = (MyWebView) findViewById(R.id.webView4);
                myWebView.setWebViewClient(new WebViewClient());
                myWebView.loadUrl("https://www.google.co.jp/webhp?source=search_app/");
                break;
        }
    }

    private void musicPlayOrPause(){
        try{
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            KeyEvent downEvent = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
            sendOrderedBroadcast(downIntent, null);
            Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            KeyEvent upEvent = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
            sendOrderedBroadcast(upIntent, null);
        }catch (Exception e){
            Log.d(TAG,"IOException");
        }
    }

    private void musicPreviousSong(){
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
        KeyEvent downEvent = new KeyEvent(downTime,eventTime,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PREVIOUS,0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT,downEvent);
        sendOrderedBroadcast(downIntent,null);
        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
        KeyEvent upEvent = new KeyEvent(downTime,eventTime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PREVIOUS,0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT,upEvent);
        sendOrderedBroadcast(upIntent,null);
    }

    private void musicNextSong(){
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
        KeyEvent downEvent = new KeyEvent(downTime,eventTime,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_NEXT,0);
        downIntent.putExtra(Intent.EXTRA_KEY_EVENT,downEvent);
        sendOrderedBroadcast(downIntent,null);
        Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
        KeyEvent upEvent = new KeyEvent(downTime,eventTime,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_NEXT,0);
        upIntent.putExtra(Intent.EXTRA_KEY_EVENT,upEvent);
        sendOrderedBroadcast(upIntent,null);
    }

    private void scrollUp() {
        long downTime;
        long eventTime;
        MotionEvent motionEvent;
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis() + 100;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 300, 300, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 300, 250, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 300, 200, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 300, 200, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 300, 100, 0);
        myWebView.dispatchTouchEvent(motionEvent);
    }

    private void scrollDown() {
        long downTime;
        long eventTime;
        MotionEvent motionEvent;
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis() + 100;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 300, 100, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 300, 150, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 300, 200, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 300, 200, 0);
        myWebView.dispatchTouchEvent(motionEvent);
        downTime += 5;
        eventTime += 5;
        motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, 300, 300, 0);
        myWebView.dispatchTouchEvent(motionEvent);
    }

    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        serialThread.quit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
