package com.example.ivan.wifip2pbasic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements WifiP2pManager.PeerListListener{

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<WifiP2pDevice> peersConnect = new ArrayList<WifiP2pDevice>();
    List<String> peerNames = new ArrayList<String>();

    WifiP2pConfig config = new WifiP2pConfig();

    IntentFilter mIntentFilter;

    Button button1, button2, button3, button4, button5;
    FrameLayout frame1;
    Spinner spinner1;
    TextView text1, text2;
    Boolean validPeers = false;

    WifiP2pInfo wifiP2pInfo;
    WifiP2pDevice targetDevice;
    Boolean transferReadyState=false;
    Boolean activeTransfer =false;

    private Intent clientServiceIntent;

    public final int port = 7950;

    Boolean serverStatus=false;
    private Intent serverServiceIntent;

    ImageView imageView1;
    Camera mainCamera;
    Preview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);
        button5= (Button)findViewById(R.id.button5);
        text1 = (TextView)findViewById(R.id.textView1);
        spinner1 = (Spinner)findViewById(R.id.spinner1);
        frame1= (FrameLayout)findViewById(R.id.previewFrame);

        //INITIATE THE CAMERA
        try{

            mainCamera= Camera.open();
        }catch(Exception e){
            Log.d("NEUTRAL","Error opening camera");

        }
        mPreview = new Preview(this, mainCamera);

        //START THE PREVIEW
        try{

            //Initiate the preview
            frame1.addView(mPreview);

            Log.d("NEUTRAL", "Camera Preview Class Added");

        }catch(RuntimeException e){

            Log.d("NEUTRAL","Error in OnCreate");
            System.err.println(e);
            return;

        }

        //DISCOVER PEERS
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener(){

                    @Override
                    public void onSuccess() {
                        text1.setText("Peers Discovered");
                    }

                    @Override
                    public void onFailure(int reason) {
                        text1.setText("Peer Discovery Unsuccessful");
                    }
                });

            }
        });

        //CONNECT TO THE FIRST PEER DETECTED
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validPeers==true){
                    config.deviceAddress = peers.get(0).deviceAddress;
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("NEUTRAL","Connection successful");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("NEUTRAL","Connection failed");
                        }
                    });
                }


            }
        });

        //SEND DATA
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendData();
            }
        });

        //START SERVER
        button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startServer();
            }
        });

        //Start Preview
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList){
        Log.d("NEUTRAL","Main Activity Listener");

        peers.clear();
        peers.addAll(peerList.getDeviceList());
        validPeers = true;
        //Get Device Names
        peerNames.clear();

        int i = 0;
        while(i<peers.size()){
            peerNames.add(peers.get(i).deviceName);

            Log.d("NEUTRAL","Device name: " + peerNames.get(i));
            i++;
        }

    }

    public void setClientStatus(String message){
        text1.setText(message);
    }

    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device){
        wifiP2pInfo=info;
        targetDevice=device;
        transferReadyState=status;
    }

    public void setNetworkToPendingState(boolean status){
        transferReadyState=status;
    }

    public void sendData(){

        if(activeTransfer==false){

            if(!transferReadyState){
                Log.d("NEUTRAL","Error - Connection not ready");
                text1.setText("Error - Connection not ready");
            }else if(wifiP2pInfo==null){
                Log.d("NEUTRAL","Error - Missing Wifi P2P Information");
                text1.setText("Error - Missing Wifi P2P Information");
            }
            //No errors found, begin transmission of data
            else
            {
                //Launch Client Service
                Log.d("NEUTRAL","Main Activity: No errors found, launching client service");
                clientServiceIntent = new Intent(this, ClientService.class);
                clientServiceIntent.putExtra("port",new Integer(port));
                clientServiceIntent.putExtra("wifiInfo",wifiP2pInfo);
                clientServiceIntent.putExtra("clientResult", new ResultReceiver(null){

                    @Override
                    protected void onReceiveResult(int resultCode, final Bundle resultData){
                        if(resultCode == port){

                            if(resultData==null){

                                Log.d("NEUTRAL","Transfer Status: False");
                                activeTransfer=false;

                            }else{

                                //Receives updates from the Service class and provides status on the UI
                                final TextView client_status_text= (TextView) findViewById(R.id.textView2);
                                client_status_text.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        client_status_text.setText((String)resultData.get("message"));
                                    }
                                });

                            }

                        }
                    }


                });

                activeTransfer = true;
                this.startService(clientServiceIntent);
            }
        }else
        {
            Log.d("NEUTRAL","Cannot Send Data");
        }
    }

    public void startServer(){

        if(serverStatus==false){

            serverServiceIntent = new Intent(this, ServerService.class);
            serverServiceIntent.putExtra("port", new Integer(port));
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null){
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData){

                    if(resultCode==port){

                        if(resultData==null){
                            serverStatus=false;
                            Log.d("NEUTRAL", "Main Activity: Server Stopped");
                        }else{

                        }

                    }

                }
            });

            serverStatus=true;
            startService(serverServiceIntent);

            Log.d("NEUTRAL","Main Activity: Server Running");


        }else
        {
            Log.d("NEUTRAL","Server Already Running");
        }

    }

}
