package com.example.ivan.wifip2pbasic;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by ivan on 23/3/17.
 */
public class DataManager {
    private static final String TAG = "NEUTRAL";

    //DEBUGGING
    int                 test_count = 0;
    int                 load_count = 0;

    //SYSTEM DATA
    byte[]              image_holder;

    //SYSTEM MANAGEMENT
    boolean             image_loaded = false;
    boolean             wifi_connected = false;
    private             MainActivity mActivity;

    public DataManager(MainActivity activity){
        Log.d(TAG, "Data Manager Called");
        this.mActivity = activity;
    }

    public void loadImage(byte[] b){
        if (!image_loaded){
            image_holder = Arrays.copyOf(b, b.length);;
            image_loaded = true;
            Log.d(TAG, "Data Manager, Image loaded: " + load_count);
            load_count = load_count + 1;
            mActivity.updateDisplayImage();
        }else{
            Log.d(TAG,"Image not loaded, holder full");
        }
    }

    public byte[] getImage(){
        if(image_loaded){
            /*
            YuvImage yuv = new YuvImage(image_holder, 17, 640, 480, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(100, 100, 540, 380), 50, out);
            byte[] image_buffer = out.toByteArray();
            return  image_buffer;
            */
            return image_holder;
        }else{
            Log.d(TAG,"No Image Loaded");
            return null;
        }
    }

    public void unloadImage(){
        image_loaded = false;
    }

    public boolean getImageLoadStatus(){
        return image_loaded;
    }

    public boolean getConnectionStatus(){
        return wifi_connected;
    }

    public void setConnectionStatus(boolean b){
        wifi_connected = b;
    }

}
