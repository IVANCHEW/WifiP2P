package com.example.ivan.wifip2pbasic;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ivan on 23/3/17.
 */
public class DataReceiver implements Runnable {

    private static final String TAG = "NEUTRAL";

    DataManager dm;
    private MainActivity mActivity;

    //Server Component
    private int port;
    WifiP2pInfo wifiP2pInfo;
    int previewFormat = 17;
    ServerSocket serverSocket = null;
    Socket socket = null;
    InputStream is;

    //Receiver Components
    Boolean serviceEnabled = false;

    public DataReceiver(MainActivity activity, int p, WifiP2pInfo info, DataManager d){
        Log.d(TAG,"Data Receiver Class Called");
        this.mActivity = activity;
        this.port = p;
        this.wifiP2pInfo = info;
        this.dm = d;
    }

    public void updateInitialisationData(WifiP2pInfo info){
        this.wifiP2pInfo = info;
    }

    public void run(){
        Log.d(TAG,"Initialising Data Receiver Class");
        serviceEnabled = true;
        try{
            serverSocket = new ServerSocket(port);
            serverSocket.setPerformancePreferences(0, 1, 1);
            serverSocket.setReceiveBufferSize(1024*1024);
            Log.d("NEUTRAL","Server Socket Buffer Size Set: " + serverSocket.getReceiveBufferSize());
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }

        //STANDARD OPENING CODES
        try {
            socket = serverSocket.accept();
            is = socket.getInputStream();
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }

        //Receiver Loop
        while(serviceEnabled){

            //Log.d("NEUTRAL", "Server Service: Package Received");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            byte[] picture_length_buffer = new byte[4];
            int read = 0;
            int picture_length;
            int marker = 0;

            //Log.d(TAG,"Waiting for first packet");
            try{
                while (is.available() < 4){
                }
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }

            //Log.d(TAG,"Reading First Packet");
            try{
                is.read(picture_length_buffer, 0, 4);
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }

            picture_length = byteArrayToInt(picture_length_buffer);
            //Log.d("NEUTRAL","Length Received: " + picture_length);

            //Log.d(TAG,"Waiting for second packet");
            try{
                while (is.available() < picture_length){
                }
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }

            //Log.d(TAG,"Reading second packet");
            while (marker < picture_length){

                if (picture_length - marker >= 1024){
                    read = 1024;
                }else{
                    read = picture_length - marker;
                }
                marker = marker + read;
                try{
                    is.read(buffer,0,read);
                }catch(IOException e) {
                    Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
                }
                baos.write(buffer,0,read);
                //Log.d("NEUTRAL", "Marker position: " + marker);
            }

            byte[] buffer2 = baos.toByteArray();
            Log.d("NEUTRAL","Length of data received: " + buffer2.length);

            if (!dm.getImageLoadStatus()){
                Log.d(TAG,"Loading Image");
                dm.loadImage(buffer2);
            }
        }

        Log.d(TAG,"Closing Socket");
        try{
            socket.close();
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }


    }

    public static int byteArrayToInt(byte[] b)
    {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

    public void stopReceiver(){
        serviceEnabled = false;
    }

}
