package com.frommetoyou.soundforme;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.hardware.Camera.Parameters.FLASH_MODE_AUTO;
import static android.hardware.Camera.Parameters.FLASH_MODE_ON;
import static android.hardware.Camera.Parameters.FLASH_MODE_TORCH;
import static com.frommetoyou.soundforme.MainActivity.ACTION_MODO_APLAUSO;
import static com.frommetoyou.soundforme.MainActivity.ACTION_MODO_SILBIDO;

//import com.crashlytics.android.Crashlytics;


public class DetectorThread extends Thread {
    private int PAUSA_DELAY = 1600;//5600 era
    private final static int SLEEP_ACTIVO = 28;//250
    private final static int SLEEP_INACTIVO = 33;//300
    private final static int SLEEP_MUY_ACTIVO = 18; //15
    private final static int AHORRO_TEMPORIZADOR = 1000*60;
    private final static int INTERMITENCIA_ACTIVA=350;
    private final static int INTERMITENCIA_INACTIVA=1500;
    private final static int PASAR_MODO_ACTIVO_TEMPORIZADOR=10000;
    private int SLEEP_TIME = SLEEP_ACTIVO;//5600 era
    private RecorderThread recorder;
    private volatile Thread _thread;
    private LinkedList<Boolean> whistleResultList = new LinkedList<>();
    private int totalWhistlesDetected = 0, cantidadSilbido=0,cantidadAplausos=0,deteccionesPiso;
    private int numWhistles;
    private Context context;
    private Uri music_path;
    private Handler h,handler;
    private String modoDeteccion=ACTION_MODO_SILBIDO;
    private WhistleApi whistleApi;
    private ClapApi2 clapApi;
    private boolean continuarDeteccion = true, isWhistle,flashCamara=false,vibracion=false,duracion_completa, flash_activo=true,bluetooth_mode=false,speaker_mode=true; //espera_bien_activa boolean
    private MediaPlayer mediaPlayer;
    private static Camera camera = null;// has to be static, otherwise onDestroy() destroys it
    private Vibrator v;
    private Thread thisThread;
    private AudioManager audio;
    private int origionalVolume,originalMode;
    public DetectorThread(RecorderThread recorder) {
        this.recorder = recorder;
        AudioRecord audioRecord = recorder.getAudioRecord();

        int bitsPerSample = 0;
        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bitsPerSample = 16;
        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
            bitsPerSample = 8;
        }

        int channel = 0;
        // whistle detection only supports mono channel
        if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
            channel = 1;
        }
        h = new Handler();
        handler=new Handler();
        WaveHeader waveHeader = new WaveHeader();
        waveHeader.setChannels(channel);
        waveHeader.setBitsPerSample(bitsPerSample);
        waveHeader.setSampleRate(audioRecord.getSampleRate());
        whistleApi = new WhistleApi(waveHeader);
        clapApi = new ClapApi2(waveHeader);
    }

    private void initBuffer() {
        numWhistles = 0;
        whistleResultList.clear();

        // init the first frames
        int whistleCheckLength = 3;
        for (int i = 0; i < whistleCheckLength; i++) {
            whistleResultList.add(false);
        }
        // end init the first frames
    }

    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void stopDetection() {
        h.post(stopResources);
        h.removeCallbacks(null);
        handler.removeCallbacks(null);
        _thread = null;
    }

    private void pararDeteccion() {
        continuarDeteccion = false;
    }
    private void continuarDeteccion() {
        continuarDeteccion = true;
    }

    public void run() {
        mediaPlayer = MediaPlayer.create(context, R.raw.sound_file_1);

        try {
            h.postDelayed(ahorro_bateria,AHORRO_TEMPORIZADOR);
            initBuffer();
             thisThread = Thread.currentThread();
              handler.post(detectar);

        } catch (Exception e) {
         //   Crashlytics.log(Log.WARN, "DetectorThread", "Error message");
            e.printStackTrace();
        }
    }
    private Runnable ahorro_bateria=new Runnable() {
        @Override
        public void run() {
            SLEEP_TIME=SLEEP_INACTIVO;
        }
    };
    Runnable detectar=new Runnable() {
        @Override
        public void run() {
            if(_thread==thisThread){
                if(continuarDeteccion){
                    byte[] buffer = recorder.getFrameBytes();
                    // audio analyst
                    if (buffer != null) {
                        // sound detected
                        if (modoDeteccion.equals(ACTION_MODO_SILBIDO))
                        {
                            deteccionesPiso=cantidadSilbido;
                            isWhistle = whistleApi.isWhistle(buffer);
                        }
                        if (modoDeteccion.equals(ACTION_MODO_APLAUSO))
                        {
                            deteccionesPiso=cantidadAplausos;
                            isWhistle = clapApi.isClap(buffer);
                        }
                        if (whistleResultList.getFirst()) {
                            numWhistles--;
                        }

                        whistleResultList.removeFirst();
                        whistleResultList.add(isWhistle);

                        if (isWhistle)
                        {
                            h.removeCallbacks(pasar_modo_activo,ahorro_bateria);
                            SLEEP_TIME=SLEEP_MUY_ACTIVO;
                            h.postDelayed(pasar_modo_activo,PASAR_MODO_ACTIVO_TEMPORIZADOR);
                            h.postDelayed(ahorro_bateria,AHORRO_TEMPORIZADOR);
                            numWhistles++;
                        }

                        int whistlePassScore = 3;
                        if (numWhistles >= whistlePassScore) {
                            // clear buffer
                            initBuffer();

                            totalWhistlesDetected++;
                            if (totalWhistlesDetected >= deteccionesPiso) {
                                totalWhistlesDetected = 0;
                                //espera_bien_activa=0;
                                if(vibracion)manejadorVibracion();
                                if(flashCamara) h.post(iniciarFlash);
                                audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                originalMode=audio.getMode();
                                origionalVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                                audio.setMode(AudioManager.STREAM_MUSIC);
                                audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                                //origionalVolume = audio.getStreamVolume(originalMode);

                                    bluetooth_mode=audio.isBluetoothScoOn();
                                    speaker_mode=audio.isSpeakerphoneOn();
                                    audio.stopBluetoothSco();
                                    audio.setBluetoothScoOn(false);
                                    audio.setSpeakerphoneOn(true);

                                manejadorMusica();
                                h.postDelayed(stopResources, PAUSA_DELAY);
                                pararDeteccion();
                                h.postDelayed(start, PAUSA_DELAY);
                                h.removeCallbacks(pasar_modo_activo,ahorro_bateria);
                                SLEEP_TIME=SLEEP_MUY_ACTIVO;
                                h.postDelayed(pasar_modo_activo,PASAR_MODO_ACTIVO_TEMPORIZADOR);
                                h.postDelayed(ahorro_bateria,AHORRO_TEMPORIZADOR);
                            }
                        }
                        // end whistle detection
                    } else {
                        // no sound detected
                        if (whistleResultList.getFirst()) {
                            numWhistles--;
                            if (modoDeteccion.equals(ACTION_MODO_SILBIDO)) totalWhistlesDetected = 0;
                        }
                        whistleResultList.removeFirst();
                        whistleResultList.add(false);
                    }
                    // end audio analyst
                }
            }
            handler.postDelayed(this,SLEEP_TIME);
        }
    };

    private Runnable pasar_modo_activo=new Runnable() {
        @Override
        public void run() {
            SLEEP_TIME=SLEEP_ACTIVO;
        }
    };
    public void setStopResources()
    {
        h.removeCallbacks(stopResources);
        h.post(stopResources);
        continuarDeteccion();
    }
    private Runnable stopResources = new Runnable() {
        public void run() {
            try {
                detenerMedia();
                if (audio!=null){
                    audio.setMode(originalMode);
                    System.out.println(originalMode+" y el otro es "+AudioManager.STREAM_MUSIC);
                    //audio.setStreamVolume(originalMode, origionalVolume, 0);
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC,origionalVolume,0);

                    if(bluetooth_mode){
                        audio.startBluetoothSco();
                        audio.setBluetoothScoOn(true);
                    }else if(!speaker_mode){
                        audio.setSpeakerphoneOn(false);
                    }
                }

                detenerFlash();
                    h.removeCallbacks(iniciarFlash);
                    h.removeCallbacks(vibrador);
            } catch (Exception e) {
                //Crashlytics.log(Log.WARN, "DetectorThread: "+e, "Error recursos media");
                e.printStackTrace();
            }
        }
    };

    public void detenerMedia(){
        try{
            if (mediaPlayer != null) {
               // if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                //mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }catch (IllegalStateException e){
            e.printStackTrace();
        }

    }
    public void manejadorVibracion()
    {
        v = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        h.post(vibrador);
    }

    private Runnable vibrador=new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //MAL cuidado con older devices
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(INTERMITENCIA_ACTIVA);
            }
            h.postDelayed(this,INTERMITENCIA_INACTIVA);
        }
    };



    private Runnable iniciarFlash=new Runnable() {
        @Override
        public void run() {
            if(flash_activo){
                flash_activo=!flash_activo;
                manejadorFlash();
                h.postDelayed(this,INTERMITENCIA_ACTIVA);
            }
            else {
                try {
                    flash_activo=!flash_activo;
                    detenerFlash();
                    h.postDelayed(this,INTERMITENCIA_ACTIVA);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    };
    public void manejadorFlash() {
        try {
            camera=Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(getFlashOnParameter() );
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (IOException e) {
            System.out.println("camara error!: "+e);
        }
    }
    //un coso que saque de internet para que funcione en varias camaras
    private String getFlashOnParameter() {
        List<String> flashModes = camera.getParameters().getSupportedFlashModes();

        if (flashModes.contains(FLASH_MODE_TORCH)) {
            return FLASH_MODE_TORCH;
        } else if (flashModes.contains(FLASH_MODE_ON)) {
            return FLASH_MODE_ON;
        } else if (flashModes.contains(FLASH_MODE_AUTO)) {
            return FLASH_MODE_AUTO;
        }
        throw new RuntimeException();
    }
    private void detenerFlash() throws CameraAccessException {
        if(camera != null ) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
    private void manejadorMusica()
    {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context,music_path);
            mediaPlayer.setVolume(1.0f,1.0f);
            mediaPlayer.prepare();
            if(duracion_completa) PAUSA_DELAY=mediaPlayer.getDuration();
            else PAUSA_DELAY=3000;
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public Runnable start = new Runnable() {
        @Override
        public void run() {
            continuarDeteccion();
        }
    };
    public void contexto(Context contexto)
    {
        context=contexto;
    }
    public void cambioModo(String modo)
    {
        modoDeteccion=modo;
    }
    public void habilitarFlash(boolean flash)
    {
        flashCamara=flash;
    }
    public void habilitarVibracion(boolean vibrate)
    {
        vibracion=vibrate;
    }
    public void setCantSilbido(int cantidadSilbido)
    {
        this.cantidadSilbido=cantidadSilbido;
    }
    public void setCantAplausos(int cantidadAplausos)
    {
        this.cantidadAplausos=cantidadAplausos;
    }
    public void setMusicPath(Uri music_path)
    {
        this.music_path=music_path;
    }
    public void setTipoDelay(boolean duracion_completa)
    {
        this.duracion_completa=duracion_completa;
    }
}