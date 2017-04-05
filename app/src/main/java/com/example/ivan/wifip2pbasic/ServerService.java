package com.example.ivan.wifip2pbasic;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by ivan on 07/07/16.
 */
public class ServerService extends IntentService {

    private Boolean serviceEnabled;

    private int port;
    private ResultReceiver serverResult;
    //private byte[] streamData;
    private byte[] pictureDataOut;
    private byte[] audioDataOut;
    private boolean imageProcessing = false;

    //Conversion of video data received
    //int width = 320;
    //int height = 240;
    int previewFormat = 17;
    //int minBufSize = 1408;
    //int minBufSize = 1024;
    int minBufSize;

    public ServerService() {
        super("ServerService");

        serviceEnabled=true;
        Log.d("NEUTRAL", "Server Service Class: Class called");
    }

    @Override
    protected void onHandleIntent(Intent intent){

        Log.d("NEUTRAL","Server Service Class: Intent received");
        port= ((Integer) intent.getExtras().get("port")).intValue();
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");
        minBufSize = ((Integer) intent.getExtras().get("audiobuf")).intValue();
        Log.d("NEUTRAL", "Audio buffer size of: " + minBufSize + " declared");

        ServerSocket serverSocket = null;
        Socket socket = null;

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
        }catch(IOException e) {
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }

        while(serviceEnabled){

            //Log.d("NEUTRAL", "Server Service: Package Received");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            byte[] picture_length_buffer = new byte[4];
            int read = 0;
            int picture_length;
            int marker = 0;

            try{
                InputStream is = socket.getInputStream();

                is.read(picture_length_buffer, 0, 4);
                picture_length = byteArrayToInt(picture_length_buffer);
                //Log.d("NEUTRAL","Length Received: " + picture_length);

                while (is.available() < picture_length){

                }

                while (marker < picture_length){

                    if (picture_length - marker >= 1024){
                        read = 1024;
                    }else{
                        read = picture_length - marker;
                    }
                    marker = marker + read;

                    is.read(buffer,0,read);
                    baos.write(buffer,0,read);
                    //Log.d("NEUTRAL", "Marker position: " + marker);
                }


            }catch(IOException e) {
                Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
            }

            byte[] buffer2 = baos.toByteArray();
            Log.d("NEUTRAL","Length of data received: " + buffer2.length);

            //byte[] audioData = Arrays.copyOfRange(buffer2, 0, minBufSize);
            //byte[] bytes = Arrays.copyOfRange(buffer2, minBufSize, buffer2.length);

            /*
            YuvImage yuv = new YuvImage(buffer2, 17, 640, 480, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, 640, 480), 100, out);
            byte[] image_buffer = out.toByteArray();
            */

            imageProcessing= (Boolean) intent.getExtras().get("imageProcessing");

            if (imageProcessing==false){
                Log.d("NEUTRAL", "Server Service: Package Sent to Main Activity");
                //Log.d("NEUTRAL","Length of audio: " + audioData.length);
                //Log.d("NEUTRAL","Length of picture: " + bytes.length);
                //streamData = buffer2;
                //pictureDataOut = bytes;
                //pictureDataOut = image_buffer;
                pictureDataOut = buffer2;
                //audioDataOut = audioData;
                signalActivity();
            }
        }

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

    public void signalActivity(){
        Bundle b = new Bundle();
        //b.putByteArray("streamData", streamData);
        //b.putByteArray("audioData", audioDataOut);
        b.putByteArray("pictureData", pictureDataOut);
        serverResult.send(port,b);
    }

    public void onDestroy(){
        serviceEnabled=false;
        stopSelf();
    }

}
