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

        //  SERVER SOCKET CONFIGURATION
        try{
            serverSocket = new ServerSocket(port);
            serverSocket.setPerformancePreferences(0, 1, 1);
            serverSocket.setReceiveBufferSize(1024*1024);
            Log.d("NEUTRAL","Server Socket Buffer Size Set: " + serverSocket.getReceiveBufferSize());
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }

        // SERVER SOCKET INITIALISATION
        try {
            socket = serverSocket.accept();
            is = socket.getInputStream();
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }

        // BEGIN RECEIVER LOOP
        int audioBufSize = dm.getAudioBufSize();

        while(serviceEnabled){

            //Log.d("NEUTRAL", "Server Service: Package Received");
            int picture_length;

            //Log.d(TAG,"Waiting for first packet");
            ensureAvailable(4);

            //Log.d(TAG,"Reading First Packet");
            picture_length = readPictureLength();
            //Log.d(TAG,"Target Length: " + picture_length);

            //Log.d(TAG,"Waiting for second packet");
            ensureAvailable(picture_length);

            //waitForImageMemory();

            readImageData(picture_length);
            ensureAvailable(audioBufSize);
            readAudioData(audioBufSize);

        }

        // EXIT LOOP - CLOSE SOCKET
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

    private void ensureAvailable(int l){

        boolean available = false;
        int available_bytes = 0;

        while(!available){
            try{
                available_bytes = is.available();
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }
            Log.d(TAG,"Available Bytes: " + available_bytes);
            if (available_bytes>=l){
                available = true;
                Log.d(TAG,"Inputstream bytes ready");
            }else{
                try{
                    Thread.sleep(100);
                    Log.d(TAG,"Sleeping Thread");
                }catch (InterruptedException e) {
                    Log.d(TAG,"Error sleeping thread: " + e.toString());
                }
            }
        }
    }

    private int readPictureLength(){
        byte[] picture_length_buffer = new byte[4];
        int picture_length;

        try{
            is.read(picture_length_buffer, 0, 4);
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }

        picture_length = byteArrayToInt(picture_length_buffer);

        return picture_length;
    }

    private void readImageData(int picture_length){
        int marker = 0;
        int read;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!dm.getImageLoadStatus()){
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
                    //is.skip(1);
                }catch(IOException e) {
                    Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
                }
                baos.write(buffer,0,read);
                //Log.d("NEUTRAL", "Marker position: " + marker);
            }

            byte[] buffer2 = baos.toByteArray();
            Log.d("NEUTRAL","Length of data received: " + buffer2.length);

            Log.d(TAG,"Loading Image");
            dm.loadImage(buffer2);
        }

        else{
            try{
                is.skip(picture_length);
                Log.d(TAG,"Skipped Frame");
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }
        }
    }

    private void readAudioData(int audioBufSize){
        if (!dm.getAudioLoadStatus()){
            byte[] buffer3 = new byte[audioBufSize];
            boolean audio_read = false;
            try {
                if (is.available() >= audioBufSize) {
                    Log.d(TAG,"Reading Audio");
                    is.read(buffer3, 0, audioBufSize);
                    audio_read = true;
                }
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }

            if(audio_read){
                Log.d(TAG,"Loading Audio");
                dm.loadAudio(buffer3);
            }
        }
        else{
            try{
                is.skip(audioBufSize);
                Log.d(TAG,"Skipped Audio Frame");
            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }
        }
    }

    private void waitForImageMemory(){
        while(dm.getImageLoadStatus()){
            try{
                Thread.sleep(100);
                Log.d(TAG,"Sleeping Thread: Image Loading");
            }catch (InterruptedException e) {
                Log.d(TAG,"Error sleeping thread: " + e.toString());
            }
        }
    }
}
