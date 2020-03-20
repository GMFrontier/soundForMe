package com.frommetoyou.soundforme.free;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.frommetoyou.soundforme.R;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static com.frommetoyou.soundforme.free.ListSongs.REPRODUCIR_ENTERA;
import static com.frommetoyou.soundforme.free.ListSongs.REPRODUCIR_SEGUNDOS;
import static com.frommetoyou.soundforme.free.MainActivity.ACTION_MODO_SILBIDO;
import static com.frommetoyou.soundforme.free.MainActivity.ACTION_STOP_SERVICE;
import static com.frommetoyou.soundforme.free.MainActivity.CANT_APLAUSOS;
import static com.frommetoyou.soundforme.free.MainActivity.CANT_SILBIDOS;
import static com.frommetoyou.soundforme.free.MainActivity.FLASH;
import static com.frommetoyou.soundforme.free.MainActivity.FUNCIONAMIENTO;
import static com.frommetoyou.soundforme.free.MainActivity.TIPO_DELAY_SAVE;
import static com.frommetoyou.soundforme.free.MainActivity.VIBRAR;

public class DetectorService extends Service {
    private Intent detenerMusicaDeteccion;
    private Context context;
    private String modoDeteccion,stringFuncionamiento,modoDeteccionLanguage;
    private NotificationHelper helper;
    private PendingIntent pendingDetener,pendingCambiar,pendingMain;
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;
    private boolean flashCamara;
    private boolean vibracion;
    private boolean funcionamiento_solo_durante_bloqueo;
    private boolean duracion_completa;
    private int cantidadSilbido=0;//5 es medio segundo
    private int cantidadAplausos=0;//2 como umbral minimo
    Resources strings;
    IntentFilter filter;
    Handler handler;
    BroadcastReceiver screen_off_receiver,screen_unlocked_receiver;
    Uri music_path;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        Intent detenerServicio = new Intent(getApplicationContext(), DetectorService.class);
        Intent abrirMain = new Intent(getApplicationContext(), MainActivity.class);
        detenerServicio.setAction(ACTION_STOP_SERVICE);
        pendingDetener=PendingIntent.getService(getApplicationContext(),0, detenerServicio,PendingIntent.FLAG_CANCEL_CURRENT);
        pendingMain=PendingIntent.getActivity(getApplicationContext(),0,abrirMain,PendingIntent.FLAG_CANCEL_CURRENT);
        detenerMusicaDeteccion=new Intent(getApplicationContext(),DetectorService.class);
        strings=getResources();
        screen_unlocked_receiver=new PhoneUnlockedReceiver();
        filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        screen_off_receiver = new ScreenOffReceiver();
        handler=new Handler();
        registerReceiver(screen_unlocked_receiver,new IntentFilter("android.intent.action.USER_PRESENT"));
        registerReceiver(screen_off_receiver, filter);
        sharedPreferences= context.getSharedPreferences("CustomPreferences",MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction()))
        {
            pararThreads();
            EventBus.getDefault().postSticky(new MessageEvent(true, false,"null"));
            stopForeground(true);
            stopSelf();
        }else
        {
            manejarExtras();
            if(modoDeteccion.equals(ACTION_MODO_SILBIDO)) modoDeteccionLanguage=strings.getString(R.string.silbido);
            else modoDeteccionLanguage=strings.getString(R.string.aplauso);
            activarForegroundService();
                iniciarThreads();
                detectorThread.setStopResources();
                detectorThread.habilitarVibracion(vibracion);
                detectorThread.cambioModo(modoDeteccion);
                detectorThread.habilitarFlash(flashCamara);
                detectorThread.setCantSilbido(cantidadSilbido);
                detectorThread.setCantAplausos(cantidadAplausos);
                detectorThread.setMusicPath(music_path);
                detectorThread.setTipoDelay(duracion_completa);
                if(funcionamiento_solo_durante_bloqueo) pararThreads();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(screen_off_receiver);
            unregisterReceiver(screen_unlocked_receiver);
        }catch (Throwable t){
            //Crashlytics.log(Log.WARN,"DetectorThread","Error message");
        }
    }
    Runnable pararThreads=new Runnable() {
        @Override
        public void run() {
            pararThreads();
        }
    };
    private void pararThreads()
    {
        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread = null;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void iniciarThreads()
    {
        if (recorderThread == null) {
            recorderThread = new RecorderThread();
            recorderThread.startRecording();
        }
        if (detectorThread==null)
        {
            detectorThread = new DetectorThread(recorderThread);
            detectorThread.contexto(context);
            detectorThread.habilitarVibracion(vibracion);
            detectorThread.cambioModo(modoDeteccion);
            detectorThread.habilitarFlash(flashCamara);
            detectorThread.setCantSilbido(cantidadSilbido);
            detectorThread.setCantAplausos(cantidadAplausos);
            detectorThread.setMusicPath(music_path);
            detectorThread.setTipoDelay(duracion_completa);
            detectorThread.start();
        }
    }
    //para saber qué hacer con la cantidad de silbidos y aplausos y demás enviado por el menu
    private void manejarExtras()
    {
        stringFuncionamiento=sharedPreferences.getString("FUNCIONAMIENTO_STRING",strings.getString(R.string.funcionamiento_bloqueo));
        vibracion=sharedPreferences.getBoolean(VIBRAR,false);
        flashCamara=sharedPreferences.getBoolean(FLASH,false);

        modoDeteccion=sharedPreferences.getString("MODO",ACTION_MODO_SILBIDO);

        int mensajeCantSilbidos = sharedPreferences.getInt(CANT_SILBIDOS, R.id.sbCorto);
        int mensajeCantAplausos = sharedPreferences.getInt(CANT_APLAUSOS, R.id.apUno);
        int mensajeFuncionamiento = sharedPreferences.getInt(FUNCIONAMIENTO, R.id.al_apagar_pantalla);
        int mensajeTipoDelay = sharedPreferences.getInt(TIPO_DELAY_SAVE, REPRODUCIR_SEGUNDOS);
        String music_aux=sharedPreferences.getString("music_path","android.resource://"+getPackageName()+"/raw/sound_file_1");
        music_path= Uri.parse(music_aux);
        if(mensajeCantSilbidos ==R.id.sbCorto)
            cantidadSilbido=3; //era 5,12,18
        else if(mensajeCantSilbidos ==R.id.sbMedio)
            cantidadSilbido=8;
        else if(mensajeCantSilbidos ==R.id.sbLargo)
            cantidadSilbido=12;
        if(mensajeCantAplausos ==R.id.apUno)
            cantidadAplausos=1;//2,3,4
        else if(mensajeCantAplausos==R.id.apDos)
            cantidadAplausos=2;
        else if(mensajeCantAplausos==R.id.apTres)
            cantidadAplausos=3;
        if(mensajeFuncionamiento ==R.id.al_apagar_pantalla)
            funcionamiento_solo_durante_bloqueo=true;
        else if(mensajeFuncionamiento ==R.id.pantalla_activa)
            funcionamiento_solo_durante_bloqueo=false;
        if(mensajeTipoDelay ==REPRODUCIR_ENTERA)
            duracion_completa=true;
        else if(mensajeTipoDelay ==REPRODUCIR_SEGUNDOS)
            duracion_completa=false;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void activarForegroundService()
    {
        helper= new NotificationHelper(context);
        pendingCambiar=PendingIntent.getService(getApplicationContext(),0, detenerMusicaDeteccion,PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) helper.createChannels();
        NotificationCompat.Builder builder = helper.getMyNotification(strings.getString(R.string.notifTitulo) + modoDeteccionLanguage, stringFuncionamiento);
        builder.setContentIntent(pendingMain);
        RemoteViews collapsedNotif=new RemoteViews(getPackageName(),R.layout.notification_collapsed);
        collapsedNotif.setTextViewText(R.id.notif_titulo,strings.getString(R.string.notifTitulo)+modoDeteccionLanguage);
        collapsedNotif.setTextViewText(R.id.notif_cuerpo,stringFuncionamiento);
        collapsedNotif.setTextViewText(R.id.tv_parar_musica,getResources().getString(R.string.notifBtnDetenerMusica));
        collapsedNotif.setTextViewText(R.id.tv_salir,getResources().getString(R.string.notifBtnSalir));
        builder.setContent(collapsedNotif);
        builder.setCustomContentView(collapsedNotif);
        collapsedNotif.setOnClickPendingIntent(R.id.btn_parar_musica,pendingCambiar);
        collapsedNotif.setOnClickPendingIntent(R.id.btn_salir,pendingDetener);
        startForeground(-1, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class PhoneUnlockedReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {

            KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
            if (funcionamiento_solo_durante_bloqueo) {
                pararThreads();
                handler.postDelayed(pararThreads,420);
                funcionamiento_solo_durante_bloqueo=true;
                System.out.println("SE HA RESUMIDO LA ACTIVIDAD");
            }
        }
    }
    public class ScreenOffReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("MI INTENT ES: "+intent);
            if (funcionamiento_solo_durante_bloqueo) {
                iniciarThreads();
                funcionamiento_solo_durante_bloqueo=true;
                System.out.println("SE HA PARADO LA ACTIVIDAD");
            }
        }
    }
}
