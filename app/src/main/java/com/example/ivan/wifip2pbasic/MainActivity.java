package com.example.ivan.wifip2pbasic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements OnItemSelectedListener, WifiP2pManager.PeerListListener{

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<WifiP2pDevice> peersConnect = new ArrayList<WifiP2pDevice>();
    List<String> peerNames = new ArrayList<String>();
    Spinner spinner1;
    ArrayAdapter<String> spinnerArrayAdapter;
    int peerSelected;

    WifiP2pConfig config = new WifiP2pConfig();

    IntentFilter mIntentFilter;

    Button button1, button2, button3, button4, button5, button6, button7;
    EditText editText1;
    FrameLayout frame1;
    TextView text1, text2;
    Boolean validPeers = false;
    Boolean streamAlternate = false;

    WifiP2pInfo wifiP2pInfo;
    WifiP2pDevice targetDevice;
    Boolean transferReadyState=false;
    Boolean activeTransfer =false;

    private Intent clientServiceIntent;

    public final int port = 7950;

    Boolean serverStatus=false;
    private Intent serverServiceIntent;

    Camera mainCamera;
    Preview mPreview;
    public byte[] receivePData, receiveAData;
    Bitmap bmpout, bmpout2;
    Matrix matrix = new Matrix();
    ImageView imageview;
    Boolean imageProcessing=false;

    int count =0 ;
    int count2 = 0;
    int nserver=0;

    //Conversion of video data received
    /*
    int width = 320;
    int height = 240;
    int previewFormat = 17;
    */
    //int minBufSize = 2048;
    int minBufSize = 1408;
    //int minBufSize = 4410;

    //Audio Test
    private AudioTrack speaker;
    private int sampleRate = 8000;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int audioCount=  0;
    Runnable AP;
    Boolean audioRunning = false;
    Handler handleAudio = new Handler(){
        @Override
        public void handleMessage(Message msg){
            audioRunning=false;
        }
    };

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
        button6 = (Button)findViewById(R.id.button6);
        button7 = (Button)findViewById(R.id.button7);
        text1 = (TextView)findViewById(R.id.textView1);
        frame1= (FrameLayout)findViewById(R.id.previewFrame);
        editText1 = (EditText)findViewById(R.id.editText);
        imageview=(ImageView)findViewById(R.id.imageView2);
        imageview.setScaleType(ImageView.ScaleType.FIT_XY);

        spinner1 = (Spinner)findViewById(R.id.spinner);
        spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, peerNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(spinnerArrayAdapter);
        spinner1.setOnItemSelectedListener(this);
        matrix.postRotate(180);

        //====================================INITIATE WIFI DIRECT====================================
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener(){

            @Override
            public void onSuccess() {
                text1.setText("Wifi Direct Initiation: Peer Discovery Conducted");
            }

            @Override
            public void onFailure(int reason) {
                text1.setText("Wifi Direct Initiation: Peer Discovery Unsuccessful");
            }
        });

        //====================================INITATE BUTTONS====================================

        //UPDATE PEERS ON UI
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("NEUTRAL", "Initiate Detect Peers");

                mManager.requestPeers(mChannel,null);

                peerNames.clear();

                if (peers.size()>0){
                    Log.d("NEUTRAL","Peers Discovered");
                    text1.setText("Peers Discovered");
                    int i = 0;
                    while(i<peers.size()){
                        peerNames.add(peers.get(i).deviceName);
                        i++;
                    }
                    spinner1.setAdapter(spinnerArrayAdapter);
                    validPeers=true;
                }
                else
                {
                    Log.d("NEUTRAL","No Peers Available");
                    text1.setText("No Peers Available");
                    validPeers=false;
                }
            }
        });

        //CONNECT TO PEER
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (validPeers==true){

                try {
                    config.wps.setup = WpsInfo.PBC;
                    config.groupOwnerIntent = 15;
                    config.deviceAddress = peers.get(peerSelected).deviceAddress;
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("NEUTRAL", "Connection successful");
                            try{
                                startServer();
                                Log.d("NEUTRAL","Server Started");
                                speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize, AudioTrack.MODE_STREAM);
                                speaker.play();
                                Log.d("NEUTRAL", "Speaker initialized");
                                text1.setText("Server Started");
                            }catch(Exception e){
                                Log.d("NEUTRAL","Server and audio start error");
                                Log.d("NEUTRAL",e.toString());
                            }
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("NEUTRAL", "Connection failed");
                        }
                    });
                }catch(Exception e){
                    Log.d("NEUTRAL","Peer connection attempted and failed");
                    Log.d("NEUTRAL",e.toString());
                }
            }else{
                Log.d("NEUTRAL", "No valid peers selected");
            }
            }
        });

        //=============STOP CONNECT
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("NEUTRAL","Stop Connection intiated");
                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener(){
                    @Override
                    public void onSuccess() {
                        try{
                            //Undeclare available peers
                            peerNames.clear();
                            validPeers=false;
                            Log.d("NEUTRAL","Peers cleared");

                            //Uninitialise Services
                            serverStatus=false;
                            speaker.release();
                        }catch(Exception e){
                            Log.d("NEUTRAL",e.toString());
                        }
                        Log.d("NEUTRAL","Stop Connection successful");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("NEUTRAL","Stop Connection failed");
                    }
                });
            }
        });

        //============START SERVER - NOT IN USE
        button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            }
        });

        //============START THE PREVIEW - NOT IN USE
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }

        });

        //================START SERVER WITH AUDIO FEATURES - NOT IN USE
        button6.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                text1.setText("Server Started");
                startServer();
                Log.d("NEUTRAL","Server Started");
                speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,minBufSize, AudioTrack.MODE_STREAM);
                speaker.play();
                Log.d("NEUTRAL", "Speaker initialized");
                //Log.d("NEUTRAL", "Audio class initialised");

            }
        });

        button7.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                speaker.release();
                Log.d("NEUTRAL","Speakers Released");
            }
        });

    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinner1.setSelection(position);
        peerSelected = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

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
        //Log.d("NEUTRAL","Main Activity: Listener");
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        validPeers = true;
    }

    public void setClientStatus(String message){
        text1.setText(message);
    }

    public void setServerStatus(String message){
        text1.setText("Server Status" + nserver + message);
        nserver = nserver + 1;
    }

    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device){
        wifiP2pInfo=info;
        targetDevice=device;
        transferReadyState=status;
    }

    public boolean writeAudio(){
        AP = new audioPublisher(speaker, minBufSize, receivePData);
        new Thread(AP).start();

        return false;
    }

    public void setNetworkToPendingState(boolean status){
        transferReadyState=status;
    }

    public void startServer(){

        if(!serverStatus){

            serverServiceIntent = new Intent(this, ServerService.class);
            serverServiceIntent.putExtra("port", new Integer(port));
            serverServiceIntent.putExtra("audiobuf", new Integer(minBufSize));
            serverServiceIntent.putExtra("imageProcessing", imageProcessing);
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null){
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData){

                    if(resultCode==port){
                    //Log.d("NEUTRAL","Received server results");

                        if(resultData==null){
                            serverStatus=false;
                            Log.d("NEUTRAL", "Main Activity: Server Stopped");
                        }else if(imageProcessing==false){

                            imageProcessing = true;
                            serverServiceIntent.putExtra("imageProcessing", imageProcessing);
                            bmpout = null;
                            receivePData = (byte[])resultData.get("pictureData");
                            receiveAData = (byte[])resultData.get("audioData");
                            //Log.d("NEUTRAL","Steaming, Received byte array of length: " + receivePData.length);

                            //Reading audio data
                            startAudioWrite();

                            //Changed
                            imageview.post(new Runnable() {
                                @Override
                                public void run() {

                                    bmpout = BitmapFactory.decodeByteArray(receivePData,0,receivePData.length);
                                    bmpout2 = Bitmap.createBitmap(bmpout, 0, 0, bmpout.getWidth(), bmpout.getHeight(), matrix, false);

                                    count2 = count2 + 1;
                                    Log.d("NEUTRAL", "Frame Count = " + count2);

                                    if (bmpout==null){
                                        count = count + 1;
                                        Log.d("NEUTRAL","image: Bitmap null : " + count);
                                    }else{
                                        imageview.setImageBitmap(bmpout2);
                                    }
                                    imageProcessing = false;
                                    serverServiceIntent.putExtra("imageProcessing", imageProcessing);
                                }
                            });

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

    public void startAudioWrite(){
        final Thread writeThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    Log.d("NEUTRAL","Main Activity: Started Audio Write Thread");
                    speaker.flush();
                    speaker.write(receiveAData,0,receiveAData.length);

                } catch (Exception e) {
                    Log.d("NEUTRAL", "IOException: " + e.getMessage());
                }

            }

        });
        writeThread.start();
    }

}
