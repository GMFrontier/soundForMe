package com.frommetoyou.soundforme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;

import com.frommetoyou.soundforme.R;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    private static final String CHANNEL_ID="com.example.soundforme";//+new Random().nextInt()
    private static final String CHANNEL_NAME="Servicio de deteccion";
    private NotificationManager manager;

    public NotificationHelper(Context base){
        super(base);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels(){
        NotificationChannel myChannel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_MIN);
        //myChannel.enableVibration(true);
        //myChannel.enableLights(true);
        myChannel.setLightColor(Color.GREEN);
        myChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        myChannel.setDescription("DescripcionCanal1");
        myChannel.setShowBadge(false);
        getManager().createNotificationChannel(myChannel);
    }
    public NotificationManager getManager(){
        if(manager==null)
        {
            manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationCompat.Builder getMyNotification(String titulo,String cuerpo)
    {

        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
               // .setSubText(titulo)
                //.setContentTitle(titulo)
                //.setContentText(cuerpo)
                .setChannelId(CHANNEL_ID)
                .setOngoing(true)
                .setAutoCancel(true);

    }
}
