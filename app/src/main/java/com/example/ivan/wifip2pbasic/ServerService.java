package com.example.ivan.wifip2pbasic;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private byte[] pictureData;
    private boolean imageProcessing = false;

    public ServerService() {
        super("ServerService");
        serviceEnabled=true;
        Log.d("NEUTRAL","Server Service Class: Class called");
    }

    @Override
    protected void onHandleIntent(Intent intent){

        Log.d("NEUTRAL","Server Service Class: Intent received");
        port= ((Integer) intent.getExtras().get("port")).intValue();
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");


        ServerSocket welcomeSocket = null;
        Socket socket = null;

        try{
            welcomeSocket = new ServerSocket(port);
            while(serviceEnabled){

                //STANDARD OPENING CODES
                socket = welcomeSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                //DIRECT READ METHOD
                //Receive Data
                Integer length = is.available();
                byte[] buffer = new byte[length];

                if (is.available()>=1026) {
                    is.read(buffer,0,1026);
                }



                imageProcessing= (Boolean) intent.getExtras().get("imageProcessing");

                if (imageProcessing==false){
                    pictureData = buffer;
                    signalActivity();
                }

                socket.close();

                //Audio Test
                /*
                pictureData = buffer;
                signalActivity();

                socket.close();
                */


                //METHOD 2 - Fixed length
                /*
                //imageProcessing= (Boolean) intent.getExtras().get("imageProcessing");
                //Log.d("NEUTRAL","Looping");
                //if (imageProcessing==false){
                //Log.d("NEUTRAL","Image Processing = False");
                int byteSize = 4052;
                byte[] buffer = new byte[byteSize];
                //Log.d("NEUTRAL","AVailable bytes: " + is.available());

                //if (is.available()>=byteSize){

                is.read(buffer,0,byteSize);

                byte [] pLen = new byte[4];
                for (int i=0; i < 4; i++){
                    pLen[i]=buffer[i];
                }
                int picLen = byteArrayToInt(pLen);
                Log.d("NEUTRAL","Server: Picture Length Received: " + picLen);
                if (picLen>0){
                    Log.d("NEUTRAL","Server: Process Input stream");
                    pictureData = new byte[picLen];
                    for (int i=4; i < picLen + 4; i++){
                        pictureData[i-4] = buffer[i];
                    }
                    signalActivity();
                }
                //Log.d("NEUTRAL","Retrieved Picture Length: " + picLen);
                //}

                //}
                socket.close();
                */
            }


        }catch(IOException e){
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }catch(Exception e){
            Log.d("NEUTRAL", "Server Service Errror: " + e.getMessage());
        }
    }

    public void signalActivity(){
        Bundle b = new Bundle();
        b.putByteArray("pictureData", pictureData);
        serverResult.send(port,b);
    }

    public void onDestroy(){
        serviceEnabled=false;
        stopSelf();
    }

    public static byte[][] divideArray(byte[] source, int chunksize) {


        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];

        int start = 0;

        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
            start += chunksize ;
        }

        return ret;
    }

    public static int byteArrayToInt(byte[] b)
    {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

}
