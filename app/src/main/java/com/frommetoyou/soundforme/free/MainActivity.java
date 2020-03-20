package com.frommetoyou.soundforme.free;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.frommetoyou.soundforme.BuildConfig;
import com.frommetoyou.soundforme.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.onesignal.OneSignal;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PurchasesUpdatedListener {
    public final static String CUSTOM_PREFERENCES="CustomPreferences";
    public final static String ACTION_STOP_SERVICE="STOP_FG_SERVICE";
    public final static String ACTION_MODO_APLAUSO="Aplauso";
    public final static String ACTION_MODO_SILBIDO="Silbido";
    public final static String CANT_APLAUSOS="cantAplausos";
    public final static String CANT_SILBIDOS="cantSilbidos";
    public final static String FUNCIONAMIENTO="pantalla";
    public final static String SERVICIO_INICIADO="servicio_iniciado";
    public final static String VIBRAR="vibrate";
    public final static String MODO="modo";
    public final static String FLASH="flash";
    public final static String TIPO_DELAY_SAVE="tipo_delay_save";
    public final static String ACEPTA_POLITICAS="acepta_politicas";
    public final static int REQUEST_MIC=2222,REQUEST_CAM=3333,REQUEST_STORAGE=4444;//es solo un codigo para ver que permisos fueron aceptados mas adelante... algo asi ni lei todavia jaja
    private final static String MI_APP_PACKAGE ="com.frommetoyou.soundforme.free";
    public final static String QUITAR_PUBLICIDAD="quitar_publicidad";
    private final static String TIEMPO ="tiempo";
    private final static String UNICA_PRUEBA_CONSUMIDA="unica_prueba";
    private final static String PRUEBA_YA_CONSUMIDA="prueba_ya_fue_consumida";
    private final static String ITEM_SKU_ADROMOVAL="test_free_ad_purchase";
    /*
    Banner	ca-app-pub-3940256099942544/6300978111
    Intersticial	ca-app-pub-3940256099942544/1033173712
    Vídeo bonificado	ca-app-pub-3940256099942544/5224354917
    Nativo avanzado	ca-app-pub-3940256099942544/2247696110
     */
    BillingClient billingClient;
    boolean servicioIniciado;
    public String modoDeteccion=ACTION_MODO_SILBIDO,modoDeteccionLanguage;
    ToggleButton tgActivar,tgModo, tgFlashlight, tgVibrate;
    Button btnPanel,btnInstrucciones,btnPrivacy,btnCalifica;
    ImageButton cambiarMusica;
    TextView titulo,tvModo;
    Context contexto;
    private Intent detectorService;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    MenuItem prevGrupoSilbido, prevGrupoAplauso, prevGrupoFuncionamiento;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    AdView bannerAdMainBottom;
    private InterstitialAd interstitialAdMainConfig,interstitialAdMainMusic;
    SkuDetailsParams.Builder params;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences= getSharedPreferences(CUSTOM_PREFERENCES,MODE_PRIVATE);
        editor=sharedPreferences.edit();
        checkFirstRun();
        bannerAdMainBottom=findViewById(R.id.bannerAdMainBottom);
        interstitialAdMainConfig=new InterstitialAd(this);
        interstitialAdMainMusic=new InterstitialAd(this);
        editor.putBoolean(QUITAR_PUBLICIDAD,false).apply();
        //---------------------------------------------------------------------------------------
        billingClient=BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    List skuList=new ArrayList();
                    skuList.add(ITEM_SKU_ADROMOVAL);
                    params=SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);

                    for ( Purchase purchase : purchasesResult.getPurchasesList()){
                        if (!purchase.isAcknowledged()) {
                            AcknowledgePurchaseParams acknowledgePurchaseParams =
                                    AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(purchase.getPurchaseToken())
                                            .build();
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                                @Override
                                public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

                                }
                            });
                        }
                        if((purchase.getPurchaseState()==Purchase.PurchaseState.PURCHASED||purchase.getPurchaseState()==Purchase.PurchaseState.PENDING)&&purchase.getSku().equals(ITEM_SKU_ADROMOVAL)){
                           // bannerAdMainBottom.removeAllViews();
                            editor.putBoolean(QUITAR_PUBLICIDAD,true).apply();
                            bannerAdMainBottom.removeAllViews();
                        }else if (purchase.getPurchaseState()==Purchase.PurchaseState.UNSPECIFIED_STATE&&purchase.getSku().equals(ITEM_SKU_ADROMOVAL))
                        {
                            ConsumeParams consumeParams=ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).setDeveloperPayload(purchase.getDeveloperPayload()).build();
                            billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                                @Override
                                public void onConsumeResponse(BillingResult billingResult, String s) {
                                    //TODO
                                }
                            });
                            editor.putBoolean(QUITAR_PUBLICIDAD,false).apply();
                        }
                    }
                }else
                    //Toast.makeText(MainActivity.this,"No se inicio correctamente al servicio PAY",Toast.LENGTH_LONG).show();
                    System.out.println("1 EL VALOR ES DESCONECTADO 1: "+sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false));
                   //  navigationView.getMenu().findItem(R.id.quitar_toda_publicidad).setEnabled(false);

            }

            @Override
            public void onBillingServiceDisconnected() {
                //nada, implementar un aviso al usuario?
                navigationView.getMenu().findItem(R.id.quitar_toda_publicidad).setChecked(false);
                System.out.println("1 EL VALOR ES DESCONECTADO: "+sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false));
                // Toast.makeText(MainActivity.this,"No se encuentra a disposicion de realizar compras",Toast.LENGTH_LONG).show();
            }
        });
        //----------------------------------------------------------------------------------------
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //System.out.println("2 EL VALOR ES: "+sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false));

        if(!sharedPreferences.getBoolean(PRUEBA_YA_CONSUMIDA,false)){
            long savedMillis=sharedPreferences.getLong(TIEMPO,-1);
            if (System.currentTimeMillis() >= savedMillis + 24 * 60 * 60 * 1000) { //un dia entero
                editor.putBoolean(QUITAR_PUBLICIDAD,false).apply();
                editor.putBoolean(PRUEBA_YA_CONSUMIDA,true).apply();
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////////
        if(!sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false))
        {
            System.out.println("3 EL VALOR ES: "+sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false));

            //bannerAdMainBottom.setAdUnitId("ca-app-pub-9206493690157601/4395850245");//EL DE VERDAD
            AdRequest bannerAdMainBottomRequest=new AdRequest.Builder()
                    .addTestDevice("F4DB34CB0D84AC20866F711F5A128688")
                    .build();
            //interstitialAdMainConfig.setAdUnitId("ca-app-pub-3940256099942544/1033173712");//TEST AD
            interstitialAdMainConfig.setAdUnitId("ca-app-pub-9206493690157601/6024147755");//EL DE VERDAD

            //interstitialAdMainMusic.setAdUnitId("ca-app-pub-3940256099942544/1033173712");//TEST AD
            interstitialAdMainMusic.setAdUnitId("ca-app-pub-9206493690157601/3972699484");//EL DE VERDAD
            bannerAdMainBottom.loadAd(bannerAdMainBottomRequest);
            interstitialAdMainConfig.loadAd(new AdRequest.Builder()
                    .addTestDevice("F4DB34CB0D84AC20866F711F5A128688")
                    .build());
            interstitialAdMainMusic.loadAd(new AdRequest.Builder()
                    .addTestDevice("F4DB34CB0D84AC20866F711F5A128688")
                    .build());
        }
        /////////////////////////////////////////////////////////////////////////////////////////
        interstitialAdMainConfig.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAdMainConfig.loadAd(new AdRequest.Builder().build());
            }
        });
        interstitialAdMainMusic.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAdMainMusic.loadAd(new AdRequest.Builder().build());
            }
        });

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        btnCalifica=findViewById(R.id.btnCalifica);
        tgActivar = findViewById(R.id.tg_activar);
        tgModo=findViewById(R.id.tg_modo);
        titulo= findViewById(R.id.titulo);
        tvModo=findViewById(R.id.tv_modo);
        tgFlashlight=findViewById(R.id.btn_flaslight);
        tgVibrate=findViewById(R.id.btn_vibrate);
        btnPanel=findViewById(R.id.btn_config);
        drawerLayout=findViewById(R.id.drawer);
        navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        cambiarMusica=findViewById(R.id.btn_cambiar_musica);
        btnInstrucciones=findViewById(R.id.btn_instrucciones);
        servicioIniciado=sharedPreferences.getBoolean(SERVICIO_INICIADO,false);
        contexto=this;
        detectorService=new Intent(contexto,DetectorService.class);
        EventBus.getDefault().register(this);
        solicitarMic();

        if(!sharedPreferences.getBoolean(ACEPTA_POLITICAS,true)) {
            pararServicio();
            MainActivity.this.startActivity(new Intent(MainActivity.this, AceptarPoliticas.class));
        }
        if(modoDeteccion.equals(ACTION_MODO_SILBIDO)) modoDeteccionLanguage=getResources().getString(R.string.silbido);
        else modoDeteccionLanguage=getResources().getString(R.string.aplauso);
        if(servicioIniciado)
        {
            int funcionamiento=sharedPreferences.getInt(FUNCIONAMIENTO,R.id.al_apagar_pantalla);
            try {
                prevGrupoFuncionamiento=navigationView.getMenu().findItem(funcionamiento);
                prevGrupoFuncionamiento.setChecked(true);
            }catch (Exception e)
            {
                e.printStackTrace();
                prevGrupoFuncionamiento=navigationView.getMenu().findItem(R.id.al_apagar_pantalla);
                prevGrupoFuncionamiento.setChecked(true);
            }
            detectorService.setAction("");
            detectorService.putExtra("modo",modoDeteccion);
            contexto.startService(detectorService);
                servicioIniciado=true;
                editor.putBoolean(SERVICIO_INICIADO,servicioIniciado);
                editor.apply();
            tgActivar.setBackgroundResource(R.drawable.button_borders);
            tgActivar.setChecked(true);
        }

        btnInstrucciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this,Instrucciones.class));
            }
        });

        cambiarMusica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(solicitarAlmacenamiento()) {
                    if(interstitialAdMainConfig.isLoaded()&&!sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false)) interstitialAdMainMusic.show();
                    Intent intent = new Intent(MainActivity.this, ListSongs.class);
                    MainActivity.this.startActivity(intent); //ListSongs activity (listado de temas)
                    //System.out.println("ACCEDIO AL ALMACENAMIENTO");
                }
            }
        });
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.abrir,R.string.cerrar)
        {
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                if(servicioIniciado)iniciarServicio(false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(interstitialAdMainConfig.isLoaded()&&!sharedPreferences.getBoolean(QUITAR_PUBLICIDAD,false)) interstitialAdMainConfig.show();
            }
        };
        btnPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(interstitialAdMainConfig.isLoaded()) interstitialAdMainConfig.show();
                if(!drawerLayout.isDrawerOpen(GravityCompat.START))drawerLayout.openDrawer(GravityCompat.START);
                else drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        tgVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    tgVibrate.setBackgroundResource(R.drawable.vibrate_on);
                }else
                {
                    tgVibrate.setBackgroundResource(R.drawable.vibrate_off);
                }
                editor.putBoolean(VIBRAR,tgVibrate.isChecked());
                editor.apply();
                if (servicioIniciado)  iniciarServicio(false);


            }
        });
        tgFlashlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                solicitarCamara();
                if(b){
                    tgFlashlight.setBackgroundResource(R.drawable.real_flash_on);
                }else
                {
                    tgFlashlight.setBackgroundResource(R.drawable.real_flash_off);
                }
                editor.putBoolean(FLASH,tgFlashlight.isChecked());
                editor.apply();
                if (servicioIniciado) iniciarServicio(false);


            }
        });


        tgModo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked)
                    {
                        modoDeteccion=ACTION_MODO_APLAUSO;

                    }else
                    {
                        modoDeteccion=ACTION_MODO_SILBIDO;
                    }
                        editor.putString("MODO",modoDeteccion);
                        editor.putBoolean(MODO,tgModo.isChecked());
                        editor.apply();
                    if(modoDeteccion.equals(ACTION_MODO_SILBIDO)) modoDeteccionLanguage=getResources().getString(R.string.silbido);
                    else modoDeteccionLanguage=getResources().getString(R.string.aplauso);
                        if (servicioIniciado) iniciarServicio(false);
            }
        });

        tgActivar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tgActivar.setBackgroundResource(R.drawable.button_borders);
                    iniciarServicio(false);
                } else {
                    tgActivar.setBackgroundResource(R.drawable.button_borders_off);
                    pararServicio();
                }
                editor.putBoolean(SERVICIO_INICIADO,tgActivar.isChecked());
                editor.apply();
            }
        });
    }
    void prepararBotonesInicio(){
        tgFlashlight.setChecked(sharedPreferences.getBoolean(FLASH,false));
        tgVibrate.setChecked(sharedPreferences.getBoolean(VIBRAR,false));
        tgModo.setChecked(sharedPreferences.getBoolean(MODO,false));

        int cant_silbidos=sharedPreferences.getInt(CANT_SILBIDOS,R.id.sbCorto);
        int cant_aplausos=sharedPreferences.getInt(CANT_APLAUSOS,R.id.apUno);
        int funcionamiento=sharedPreferences.getInt(FUNCIONAMIENTO,R.id.al_apagar_pantalla);

        try {
            prevGrupoSilbido=navigationView.getMenu().findItem(cant_silbidos);
            prevGrupoSilbido.setChecked(true);
        }catch (Exception e)
        {
            e.printStackTrace();
            prevGrupoSilbido=navigationView.getMenu().findItem(R.id.sbCorto);
            prevGrupoSilbido.setChecked(true);
        }
        try {
            prevGrupoAplauso=navigationView.getMenu().findItem(cant_aplausos);
            prevGrupoAplauso.setChecked(true);
        }catch (Exception e)
        {
            e.printStackTrace();
            prevGrupoAplauso=navigationView.getMenu().findItem(R.id.apUno);
            prevGrupoAplauso.setChecked(true);
        }
        try {
            prevGrupoFuncionamiento=navigationView.getMenu().findItem(funcionamiento);
            prevGrupoFuncionamiento.setChecked(true);
        }catch (Exception e)
        {
            e.printStackTrace();
            prevGrupoFuncionamiento=navigationView.getMenu().findItem(R.id.al_apagar_pantalla);
            prevGrupoFuncionamiento.setChecked(true);
        }
    }
//pasar estas cosas a onCreate, ya que en onStart parece que actualza a cada rato
    @Override
    protected void onStart() {
        super.onStart();
        prepararBotonesInicio();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            String music= extras.getString("music_path");
            System.out.println("mi path es: "+music);
            editor.putString("music_path",music);
            editor.apply();
        }
        if(servicioIniciado)iniciarServicio(true);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!servicioIniciado)
        {
            this.stopService(detectorService);
           // android.os.Process.killProcess(android.os.Process.myPid());
        }
        EventBus.getDefault().unregister(this);
    }

    private void iniciarServicio(boolean cancelar_toast)
    {
        detectorService.setAction("");
        detectorService.putExtra("modo",modoDeteccion);
        contexto.startService(detectorService);
        servicioIniciado=true;
        editor.putBoolean(SERVICIO_INICIADO,servicioIniciado);
        editor.apply();
        //mejorar mas adelante
        if(!cancelar_toast)Toast.makeText(this,getResources().getString(R.string.modo)+modoDeteccionLanguage+". "+ prevGrupoFuncionamiento,Toast.LENGTH_LONG).show();
    }
    private void pararServicio()
    {
        detectorService.setAction(ACTION_STOP_SERVICE);
        contexto.startService(detectorService);
        servicioIniciado=false;
        editor.putBoolean(SERVICIO_INICIADO,servicioIniciado);
        editor.apply();
    }

    public void solicitarMic()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_MIC);
        }
    }
    private void solicitarCamara()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQUEST_CAM);
    }
    public boolean solicitarAlmacenamiento()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_STORAGE);
        }
        return ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent mensaje) {
        if(mensaje.getServiciDetenido())
        {
            editor.putBoolean(SERVICIO_INICIADO, false);
            editor.apply();
            tgActivar.setChecked(false);
        }
    }

    //los nombres de las keys deben ser iguales para mantener cierto orden
    //no puedo usar las mismas porque al ser las keys xml, tienen id unico y genera conflictos
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int groupId = menuItem.getGroupId();

        if (groupId == R.id.grupo_funcionamiento) {
            if (prevGrupoFuncionamiento != null) {
                prevGrupoFuncionamiento.setChecked(false);
            }
            prevGrupoFuncionamiento = menuItem;
            editor.putString("FUNCIONAMIENTO_STRING", String.valueOf(prevGrupoFuncionamiento));
            editor.putInt(FUNCIONAMIENTO,prevGrupoFuncionamiento.getItemId());
            editor.apply();

        }else if (groupId == R.id.grupo_silbido) {
            if (prevGrupoSilbido != null) {
                prevGrupoSilbido.setChecked(false);
            }

            prevGrupoSilbido = menuItem;
            editor.putInt(CANT_SILBIDOS, prevGrupoSilbido.getItemId());
            editor.apply();


        } else if (groupId == R.id.grupo_aplauso) {
            if (prevGrupoAplauso != null) {
                prevGrupoAplauso.setChecked(false);
            }
            prevGrupoAplauso = menuItem;
            editor.putInt(CANT_APLAUSOS, prevGrupoAplauso.getItemId());
            editor.apply();
        }else if (groupId == R.id.grupo_share){
            List<Intent> targetShareIntents=new ArrayList<>();
            Intent shareIntent=new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            List<ResolveInfo> resInfos=getPackageManager().queryIntentActivities(shareIntent, 0);
            if(!resInfos.isEmpty()){
                for(ResolveInfo resInfo : resInfos){
                    String packageName=resInfo.activityInfo.packageName;
                    if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                            || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.apps.plus")
                            || packageName.contains("com.google.android.talk") || packageName.contains("com.slack")
                            || packageName.contains("com.google.android.gm") || packageName.contains("com.facebook.orca")
                            || packageName.contains("com.yahoo.mobile") || packageName.contains("com.skype.raider")
                            || packageName.contains("com.android.mms")|| packageName.contains("com.linkedin.android")
                            || packageName.contains("com.google.android.apps.messaging")){
                        Intent intent=new Intent();
                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.cuerpo_mensaje)+"\n\n"+"https://play.google.com/store/apps/details?id=" + MI_APP_PACKAGE);
                        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.asunto_mensaje));
                        intent.setPackage(packageName);
                        targetShareIntents.add(intent);
                    }
                }
                if(!targetShareIntents.isEmpty()){
                    Intent chooserIntent=Intent.createChooser(targetShareIntents.remove(0), getResources().getString(R.string.selecciona_app_compartir));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                    startActivityForResult(chooserIntent,3131);
                }else{
                    Toast.makeText(this, getResources().getString(R.string.no_hay_apps_compartir), Toast.LENGTH_LONG).show();
                }
            }
        } else if (groupId == R.id.grupo_purchase) {
            try {
                billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> SkuDetailsList) {
                        if(SkuDetailsList != null &&
                                billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                            for(SkuDetails skuDetails:SkuDetailsList){
                                if(skuDetails.getSku().equals(ITEM_SKU_ADROMOVAL)){
                                    BillingFlowParams paramso= BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
                                    billingClient.launchBillingFlow(MainActivity.this,paramso);
                                }
                            }
                        }
                    }
                });
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        menuItem.setChecked(true);
        return false; // IMPORTANT! NOT TRUE!
    }
    @Override
    protected void onActivityResult(
            int callbackIdentifier, int resultCode, Intent intent) {
        // Is this the expected sendSMS callback ?
        if (callbackIdentifier== 3131) {
            if (resultCode == RESULT_OK) {
                String mensaje=getResources().getString(R.string.gracias);
                if(!sharedPreferences.getBoolean(UNICA_PRUEBA_CONSUMIDA,false)) {
                    bannerAdMainBottom.removeAllViews();
                    editor.putBoolean(QUITAR_PUBLICIDAD,true).apply();
                    editor.putBoolean(UNICA_PRUEBA_CONSUMIDA,true).apply();
                    long savedMillis = System.currentTimeMillis();
                    editor.putLong(TIEMPO,savedMillis).apply();
                    mensaje+=". "+getResources().getString(R.string.publicidad_quitada);
                }
                Toast.makeText(this,mensaje,Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this,getResources().getString(R.string.error_al_compartir),Toast.LENGTH_LONG).show();
            }
        }
        // Support inherited callback functions
        super.onActivityResult(callbackIdentifier,resultCode,intent);
    }
    public void calificaApp(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="+MI_APP_PACKAGE)));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + MI_APP_PACKAGE)));
        }
    }
    private void checkFirstRun() {
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        int savedVersionCode = sharedPreferences.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) { //los mantengo iguales porque mas adelante puedo llegar a utilizar este primer "else if"
            editor.clear().apply();
            editor.putBoolean(ACEPTA_POLITICAS,false).apply();
        } else if (currentVersionCode > savedVersionCode) {
          //  cuidado con el UNICA_PRUEBA_CONSUMIDA eh
        }

        // Update the shared preferences with the current version code
        editor.putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> compras) {
        if(compras!=null && billingResult.getResponseCode()== BillingClient.BillingResponseCode.OK){
            for(Purchase compra:compras){
                manejarCompras(compra);
            }
        }else if( billingResult.getResponseCode()== BillingClient.BillingResponseCode.USER_CANCELED){
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.compra_cancelada),
                    Toast.LENGTH_LONG).show();
        }else if( billingResult.getResponseCode()==BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            bannerAdMainBottom.removeAllViews();
            editor.putBoolean(QUITAR_PUBLICIDAD,true).apply();
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.compra_habia_realizada),
                    Toast.LENGTH_LONG).show();
            //navigationView.getMenu().findItem(R.id.quitar_toda_publicidad).setChecked(false);
        }
    }

    private void manejarCompras(Purchase purchase) {
        // Grant entitlement to the user.
            if (!purchase.isAcknowledged()) {

                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                       // Toast.makeText(MainActivity.this,"La compra ha sido reconocida. Disfrute de la misma!",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            bannerAdMainBottom.removeAllViews();
            editor.putBoolean(QUITAR_PUBLICIDAD,true).apply();
            Toast.makeText(MainActivity.this,
                    getResources().getString(R.string.compra_agradecimiento),
                    Toast.LENGTH_LONG).show();
            navigationView.getMenu().findItem(R.id.quitar_toda_publicidad).setEnabled(false);
        }
}
