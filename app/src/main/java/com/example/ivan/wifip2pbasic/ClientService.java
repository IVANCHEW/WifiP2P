package com.example.ivan.wifip2pbasic;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by ivan on 07/07/16.
 */
public class ClientService extends IntentService {

    private Boolean serviceEnabled=false;
    private int port;
    private String sendData;
    private ResultReceiver clientResult;
    private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiP2pInfo;
    private byte[] pictureData;


    public ClientService(){
        super("ClientService");
        serviceEnabled=true;
        //Log.d("NEUTRAL","Client Service Class: Called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("NEUTRAL","Client Service: Intent Received");
        port = ((Integer) intent.getExtras().get("port")).intValue();
        clientResult = (ResultReceiver) intent.getExtras().get("clientResult");
        wifiP2pInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");
        pictureData =(byte[])intent.getExtras().get("pictureData");

        if(!wifiP2pInfo.isGroupOwner){

            InetAddress targetIP = wifiP2pInfo.groupOwnerAddress;
            Socket clientSocket=null;
            OutputStream os=null;

            try{
                clientSocket = new Socket(targetIP,port);
                os = clientSocket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                InputStream is = clientSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

               //Send Data
                os.write(pictureData,0,pictureData.length);
                os.flush();
                /*
                while(true){

                    int byteRead = bis.read(buffer,0,buffer.length);

                    if (byteRead==-1){
                        break;
                    }

                    os.write(buffer,0,byteRead);
                    os.flush();
                }
                 */


                //Close Socket after send complete
                br.close();
                isr.close();
                is.close();

                pw.close();
                os.close();

                clientSocket.close();

                //signalActivity("Data transfer complete, close socket");
                Log.d("NEUTRAL","Client Service: Data Transfer Complete");

            }catch (IOException e){
                signalActivity(e.getMessage());
            }catch (Exception e){
                signalActivity(e.getMessage());
            }
        }else{
            signalActivity("Target device is a group owner");
            Log.d("NEUTRAL","Target device is a group owner");
        }

        clientResult.send(port,null);

    }

    public void signalActivity(String message){

        Bundle b = new Bundle();
        b.putString("message",message);
        clientResult.send(port,b);
        Log.d("NEUTRAL","Client Service: Signaled Activity");

    }

    public void onDestoy(){

        serviceEnabled=false;
        Log.d("NEUTRAL","Client Service Destroyed");
        stopSelf();

    }

}

