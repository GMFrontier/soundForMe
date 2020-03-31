package com.frommetoyou.soundforme;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

public class Instrucciones extends AppCompatActivity {
    private static final String politicas="https://franciscogmontero.wixsite.com/soundforme";
    private ImageButton playAplauso,playSilbido;
    Button btnPrivacy;
    private MediaPlayer aplauso,silbido;
    private boolean reproducir=true;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instrucciones);
        playAplauso=findViewById(R.id.btnAplausoPlay);
        playSilbido=findViewById(R.id.btnSilbidoPlay);
        final String sonido_silbido="android.resource://"+getPackageName()+"/raw/silbido";
        final String sonido_aplauso="android.resource://"+getPackageName()+"/raw/aplauso";
        handler=new Handler();
        btnPrivacy=findViewById(R.id.btnPrivacy);
        btnPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Instrucciones.this.startActivity(new Intent(Instrucciones.this, AceptarPoliticas.class));

                // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(politicas));
               // startActivity(browserIntent);
            }
        });
        playAplauso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reproducir)
                try {
                    reproducir=false;
                    aplauso = new MediaPlayer();
                    aplauso.setDataSource(getApplicationContext(), Uri.parse(sonido_aplauso));
                    aplauso.setVolume(1.0f,1.0f);
                    aplauso.prepare();
                    handler.postDelayed(setTrueReproducir,aplauso.getDuration()-1000);
                    aplauso.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        playSilbido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(reproducir)
                try {
                    reproducir=false;
                    silbido = new MediaPlayer();
                    silbido.setDataSource(getApplicationContext(),Uri.parse(sonido_silbido));
                    silbido.setVolume(1.0f,1.0f);
                    silbido.prepare();
                    handler.postDelayed(setTrueReproducir,silbido.getDuration());
                    silbido.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    Runnable setTrueReproducir=new Runnable() {
        @Override
        public void run() {
            reproducir=true;
        }
    };
}
