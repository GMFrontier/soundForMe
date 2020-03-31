package com.frommetoyou.soundforme;

import com.musicg.api.DetectionApi;
import com.musicg.wave.WaveHeader;

/**
 * Api for detecting clap
 *
 * @author Jacquet Wong
 *
 */
public class ClapApi2 extends DetectionApi {

    public ClapApi2(WaveHeader waveHeader) {
        super(waveHeader);
    }

    protected void init(){
        // settings for detecting a clap
        minFrequency = 90.0f;
        maxFrequency = 3000.0f;

        // get the decay part of a clap
        minIntensity = 10000.0f;
        maxIntensity = 100000.0f;

        minStandardDeviation = 0.0f;
        maxStandardDeviation = 0.05f;

        highPass = 100;
        lowPass = 10000;

        minNumZeroCross = 100;
        maxNumZeroCross = 500;

        numRobust = 4;
    }

    public boolean isClap(byte[] audioBytes){
        return isSpecificSound(audioBytes);
    }
}