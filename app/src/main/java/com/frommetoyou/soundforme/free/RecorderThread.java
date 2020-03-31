package com.frommetoyou.soundforme;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
//import com.crashlytics.android.Crashlytics;

public class RecorderThread extends Thread {

    private AudioRecord audioRecord;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 44100;
    private int frameByteSize = 2048; // for 1024 fft size (16bit sample size)
    int totalAbsValue;
    short sample;
    float averageAbsValue;

    byte[] buffer;

    public RecorderThread(){
        int recBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding); // need to be larger than size of a frame
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfiguration, audioEncoding, recBufSize);
        buffer = new byte[frameByteSize];
    }

    public AudioRecord getAudioRecord(){
        return audioRecord;
    }

    public void startRecording(){
        try{
            audioRecord.startRecording();
        } catch (Exception e) {
            //Crashlytics.log(Log.WARN,"DetectorThread","Error message");

            e.printStackTrace();
        }
    }

    public void stopRecording(){
        try{
            if(audioRecord!=null){
                audioRecord.stop();
                audioRecord.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Crashlytics.log(Log.WARN,"DetectorThread","Error message");
        }
    }

    public byte[] getFrameBytes(){
        audioRecord.read(buffer, 0, frameByteSize);
        // analyze sound
        totalAbsValue = 0;
        sample = 0;
        averageAbsValue = 0.0f;

        for (int i = 0; i < frameByteSize; i += 2) {
            sample = (short)((buffer[i]) | buffer[i + 1] << 8);
            totalAbsValue += Math.abs(sample);
        }
        averageAbsValue = totalAbsValue / frameByteSize / 2;
        // no input
        if (averageAbsValue < 30){
            return null;
        }
        return buffer;
    }

    public void run() {
        startRecording();
    }
}