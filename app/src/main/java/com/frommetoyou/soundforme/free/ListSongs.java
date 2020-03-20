package com.frommetoyou.soundforme.free;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.frommetoyou.soundforme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.frommetoyou.soundforme.free.MainActivity.QUITAR_PUBLICIDAD;
import static com.frommetoyou.soundforme.free.MainActivity.TIPO_DELAY_SAVE;

public class ListSongs extends AppCompatActivity {
    final static int COLOR_ACTIVO=0xff32ab9f;
    final static int COLOR_DEFAULT=0xff008688;
    Context context;
    int numero_par_contador=6;
    public static final int RUNTIME_PERMISSION_CODE = 7;
    public static final int REPRODUCIR_ENTERA = -1;
    public static final int REPRODUCIR_SEGUNDOS = 3;


    Button btnEntera,btnSegs,aux;
    String[] ListElements = new String[] { };

    ListView listView;

    List<String> ListElementsArrayList ;
    ArrayList<Long> SongID;
    ArrayAdapter<String> adapter ;

    ContentResolver contentResolver;

    Cursor cursor;
    Uri uri;
    Handler handler;
    Button button;
    AdView ListSongsBannerBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_songs);
        ListSongsBannerBottom=findViewById(R.id.ListSongsBannerBottom);
        listView = (ListView) findViewById(R.id.list_view1);
        button=findViewById(R.id.button);
        context = getApplicationContext();
        btnEntera=findViewById(R.id.btnEntera);
        btnSegs=findViewById(R.id.btnSegs);
        SharedPreferences sharedPreferences= context.getSharedPreferences("CustomPreferences",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        if(sharedPreferences.getInt(TIPO_DELAY_SAVE,REPRODUCIR_SEGUNDOS)==REPRODUCIR_SEGUNDOS) aux=btnSegs;
        else aux=btnEntera;
        if(!sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false)){
            AdRequest bannerAdMainBottomRequest=new AdRequest.Builder()
                    .addTestDevice("F4DB34CB0D84AC20866F711F5A128688")
                    .build();
            ListSongsBannerBottom.loadAd(bannerAdMainBottomRequest);
        }
        handler=new Handler();
        handler.postDelayed(titilar,500);
        ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));
        SongID=new ArrayList<>();
        adapter = new ArrayAdapter<String>
                (ListSongs.this, android.R.layout.simple_list_item_1, ListElementsArrayList);

        GetAllMediaMp3Files();
        listView.setAdapter(adapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListSongs.this,MainActivity.class);
                intent.putExtra("music_path",("android.resource://"+getPackageName()+"/raw/sound_file_1").toString());
                ListSongs.this.startActivity(intent);
            }
        });
        btnEntera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnEntera.setBackgroundColor(COLOR_ACTIVO);
                btnSegs.setBackgroundColor(COLOR_DEFAULT);
                editor.putInt(TIPO_DELAY_SAVE, REPRODUCIR_ENTERA);
                editor.apply();
                Toast.makeText(ListSongs.this,getResources().getString(R.string.duracion_entera),Toast.LENGTH_SHORT).show();
            }
        });
        btnSegs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSegs.setBackgroundColor(COLOR_ACTIVO);
                btnEntera.setBackgroundColor(COLOR_DEFAULT);
                editor.putInt(TIPO_DELAY_SAVE, REPRODUCIR_SEGUNDOS);
                editor.apply();
                Toast.makeText(ListSongs.this,getResources().getString(R.string.duracion_segs),Toast.LENGTH_SHORT).show();
            }
        });
        // ListView on item selected listener.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // TODO Auto-generated method stub
                // Showing ListView Item Click Value using Toast.
                Uri contentUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, SongID.get(position));
                Intent intent = new Intent(ListSongs.this,MainActivity.class);
                intent.putExtra("music_path",contentUri.toString());
                ListSongs.this.startActivity(intent);
                Toast.makeText(ListSongs.this,parent.getAdapter().getItem(position).toString(),Toast.LENGTH_LONG).show();

            }
        });
        //Toast.makeText(this,getResources().getString(R.string.duracion_entera),Toast.LENGTH_LONG).show();
    }
    Runnable titilar=new Runnable() {
        @Override
        public void run() {
            if(numero_par_contador%2==0)
            {
                aux.setBackgroundColor(COLOR_ACTIVO);
            }else aux.setBackgroundColor(COLOR_DEFAULT);
            if((numero_par_contador-=1)>=0)
            handler.postDelayed(this,300);
        }
    };

    public void GetAllMediaMp3Files(){

        contentResolver = context.getContentResolver();

        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
       // System.out.println("primer metodo: "+uri);
        //System.out.println("segundo metodo: "+ Environment.getExternalStorageDirectory());
        cursor = contentResolver.query(
                uri, // Uri
                null,
                null,
                null,
                MediaStore.MediaColumns.DISPLAY_NAME+""
        );

        if (cursor == null) {

            Toast.makeText(ListSongs.this,"Error", Toast.LENGTH_LONG).show();

        } else if (!cursor.moveToFirst()) {

            Toast.makeText(ListSongs.this,getResources().getString(R.string.no_hay_musica), Toast.LENGTH_LONG).show();

        }
        else {

            int Title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);

            //Getting Song ID From Cursor.
            int id = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

            do {

                // You can also get the Song ID using cursor.getLong(id).
                SongID.add(cursor.getLong(id));

                String SongTitle = cursor.getString(Title);

                // Adding Media File Names to ListElementsArrayList.
                ListElementsArrayList.add(SongTitle);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        switch(requestCode){

            case RUNTIME_PERMISSION_CODE:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {

                }
            }
        }
    }
}