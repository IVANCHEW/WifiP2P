package com.example.ivan.wifip2pbasic;

import android.app.IntentService;
import android.content.Intent;
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

/**
 * Created by ivan on 07/07/16.
 */
public class ServerService extends IntentService {

    private Boolean serviceEnabled;

    private int port;
    private ResultReceiver serverResult;
    private byte[] pictureData;

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

            while(true && serviceEnabled){

                socket = welcomeSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                Log.d("NEUTRAL","Server Service Class: About to start Handhskae");

                //Receive Data
                Integer length = is.available();
                //Log.d("NEUTRAL", "Data received: " + length);
                byte[] buffer = new byte[length];
                is.read(buffer);
                pictureData=buffer;

                //String test = new String(buffer, "UTF-8");
                //Complete method
                signalActivity();
                socket.close();
                Log.d("NEUTRAL","Data Transfer Completed: ");
            }

        }catch(IOException e){
            //signalActivity(e.getMessage());
            Log.d("NEUTRAL", e.getMessage());
        }catch(Exception e){
            //signalActivity(e.getMessage());
            Log.d("NEUTRAL", e.getMessage());
        }

        serverResult.send(port,null);


    }

    public void signalActivity(){
        Log.d("NEUTRAL", "Signal Activity");
        Bundle b = new Bundle();
        //b.putString("message",message);
        b.putByteArray("pictureData", pictureData);
        serverResult.send(port,b);
    }

    public void onDestroy(){
        serviceEnabled=false;
        stopSelf();
    }
}
