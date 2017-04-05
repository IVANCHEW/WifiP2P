package com.example.ivan.wifip2pbasic;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by ivan on 20/10/16.
 */
public class audioPublisher implements Runnable {

    private static final String TAG = "NEUTRAL";

    byte[] data;
    DataManager dm;

    AudioTrack speaker;
    int audioBuffSize = 1408*2;
    private int sampleRate = 8000;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    public audioPublisher (DataManager d){
        Log.d(TAG, "Audio Class: Initialised Method called");
        dm = d;
        speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,audioBuffSize, AudioTrack.MODE_STREAM);
        speaker.play();
        dm.setAudioBufSize(audioBuffSize);
    }

    public void writeAudio (){
        data = dm.getAudio();
        speaker.write(data, 0, audioBuffSize);
        Log.d(TAG, "Audio Class: Writing Complete");
        dm.unloadAudio();
    }

    @Override
    public void run() {
        data = dm.getAudio();
        speaker.write(data, 0, audioBuffSize);
        Log.d(TAG, "Audio Class: Finished Running");
        dm.unloadAudio();
        Log.d(TAG,"Audio unloaded from Data Manager");
    }

    public void updateBuffSize(int i){
        audioBuffSize = i;
        speaker.stop();
        speaker = new AudioTrack(AudioManager.STREAM_MUSIC,sampleRate,channelConfig,audioFormat,audioBuffSize, AudioTrack.MODE_STREAM);
        speaker.play();
    }

}
