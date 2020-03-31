package com.frommetoyou.soundforme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.frommetoyou.soundforme.MainActivity.ACEPTA_POLITICAS;

public class AceptarPoliticas extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button aceptaPoliticas;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aceptar_politicas);
        sharedPreferences=getSharedPreferences("CustomPreferences",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        editor.putBoolean(ACEPTA_POLITICAS,false).apply();
        aceptaPoliticas=findViewById(R.id.aceptaPoliticas);
        textView=findViewById(R.id.textoPoliticas);
        textView.setText(Html.fromHtml(getString(R.string.texto_politicas)));
     //   textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        aceptaPoliticas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean(ACEPTA_POLITICAS,false);
                editor.putBoolean(ACEPTA_POLITICAS,true).apply();
                AceptarPoliticas.this.startActivity(new Intent(AceptarPoliticas.this,MainActivity.class));
            }
        });
    }
    @Override
    public void onBackPressed() {
        //no permitir al usuario retroceder la pantalla
    }
}
