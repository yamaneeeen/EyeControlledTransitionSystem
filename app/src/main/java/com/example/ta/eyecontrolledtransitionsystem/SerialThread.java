package com.example.ta.eyecontrolledtransitionsystem;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by ta on 2015/09/28.
 */
public class SerialThread extends Thread{

    private boolean thread_flag = false;
    private BluetoothSocket bluetoothSocket;
    private WebViewActivity webViewActivity;

    private final static String TAG = "SerialThread";

    public SerialThread(BluetoothSocket bluetoothSocket,WebViewActivity webViewActivity){
        this.bluetoothSocket = bluetoothSocket;
        this.webViewActivity = webViewActivity;
        thread_flag = true;
    }

    public void run(){

        InputStream inputStream = null;

        try{
            inputStream = bluetoothSocket.getInputStream();

            int receiveDataLength;
            byte[] receiveData = new byte[1];
            for(int i=0; i<1; i++){
                receiveData[i] = 0x00;
            }
            Log.d(TAG, "thread start");

            while(thread_flag){

				/*---これ以降が通信の処理になる---------------------------------------------------*/

                receiveDataLength = inputStream.read(receiveData);
                Log.d(TAG, "" + receiveData[0]);

                Message message = webViewActivity.handler.obtainMessage(receiveData[0]);
                webViewActivity.handler.sendMessage(message);
                //webViewActivity.checkData(receiveData,receiveDataLength);

                /*--------------------------------------------------------------------------------*/
            }
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
            thread_flag = false;
            try{
                bluetoothSocket.close();
            }catch(Exception ee){
            }
        }

        Log.d(TAG,"Thread finished");

    }

    void quit() {
        thread_flag = false;
    }

}
