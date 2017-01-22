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
    //private byte[] pictureDataOut;
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

        ServerSocket welcomeSocket = null;
        Socket socket = null;

        try{
            welcomeSocket = new ServerSocket(port);
            while(serviceEnabled){
                Log.d("NEUTRAL", "Server Service: Package Received");
                //STANDARD OPENING CODES
                socket = welcomeSocket.accept();
                InputStream is = socket.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = is.read(buffer,0,buffer.length)) != - 1){
                    baos.write(buffer,0,read);
                }

                baos.flush();

                byte[] buffer2 = baos.toByteArray();
                Log.d("NEUTRAL","Length of data received: " + buffer2.length);

                imageProcessing= (Boolean) intent.getExtras().get("imageProcessing");

                if (imageProcessing==false){
                    Log.d("NEUTRAL", "Server Service: Package Sent to Main Activity");
                    audioDataOut = buffer2;
                    signalActivity();
                }

                socket.close();

            }


        }catch(IOException e){
            Log.d("NEUTRAL", "Sever Service IO Exception Error: " + e.getMessage());
        }catch(Exception e){
            Log.d("NEUTRAL", "Server Service Errror: " + e.getMessage());
        }
    }

    public void signalActivity(){
        Bundle b = new Bundle();
        //b.putByteArray("streamData", streamData);
        b.putByteArray("audioData", audioDataOut);
        //b.putByteArray("pictureData", pictureDataOut);
        serverResult.send(port,b);
    }

    public void onDestroy(){
        serviceEnabled=false;
        stopSelf();
    }

}
