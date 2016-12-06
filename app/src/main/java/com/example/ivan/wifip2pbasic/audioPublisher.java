package com.example.ivan.wifip2pbasic;

import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by ivan on 20/10/16.
 */
public class audioPublisher implements Runnable {

    AudioTrack speaker;
    int minBuffSize;
    byte[] data;

    public audioPublisher (AudioTrack s, int m, byte[] b){
        Log.d("NEUTRAL", "Audio Class: Initialised Method called");
        speaker = s;
        minBuffSize = m;
        data = b;
    }

    public void updateParam (byte[] b){
        Log.d("NEUTRAL", "Audio Class: Update Method called");
        data = b;
    }

    @Override
    public void run() {
        speaker.write(data, 0, minBuffSize);
        Log.d("NEUTRAL", "Audio Class: Finished Running");
    }

}
