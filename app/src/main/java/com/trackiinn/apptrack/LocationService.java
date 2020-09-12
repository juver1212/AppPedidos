package com.trackiinn.apptrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class LocationService extends Service {
    private static final long INTERVALO_ACTUALIZACION = 10000; // En ms
    double latitude=0;
    double longitud=0;
    float velocidad=0;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    String imei="";
    Handler hand = new Handler();
    public String nivel_bateria="";

    NotificationCompat.Builder builder;
    private DBManager dbManager;
    public int indicador = 0;
    PowerManager pm;
    PowerManager.WakeLock wl;

    private static List<DataLocation> ListaSQLite;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        indicador = 0;
        Log.i("servicio transporte", "Servicio iniciado");
        TelephonyManager telephonyManager = (TelephonyManager) this.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        imei= telephonyManager.getDeviceId();
        hand.postDelayed(runnable,INTERVALO_ACTUALIZACION);
        return START_STICKY;
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            indicador++;

            if(indicador % 30 == 0) {
               EncenderPantalla();
               Sqlite_Envio();
            }else {
                ApagarPantalla();
            }

            if (!AccesoGPS()) {
                IniciarServicioSinGPS();
            } else {
                IniciarServicioConGPS();
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
            Log.e("Servicio transporte","Oncreate--------------");
    }
    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            mLocationManager.removeUpdates(mLocationListener);
            Log.e("Servicio transporte","Ondestroy Try--------------");
            Enviar_Mensaje("977743233","Ondestroy Try--------------");

        } catch (SecurityException e) {
            Log.e("Servicio transporte","Ondestroy Cath--------------");
            Enviar_Mensaje("977743233","Ondestroy Cath--------------");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.jahesa.apppedidos";//"com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                //.setSmallIcon(R.drawable.icon_1)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void IniciarServicioSinGPS(){
        /*if(AccesoIntenet2()) {

            Log.e("Servicio transporte","Servicio sin GPS--------------");
            new EnviarDatosMysql().execute(imei, "0", "0", "0", "0", "N","S","N","fecha");
        }
        else
        {*/
            EnviarDatosSQLite(imei, String.valueOf(latitude), String.valueOf(longitud), nivel_bateria, String.valueOf(velocidad), "S", "N");
        //}

        hand.postDelayed(runnable, INTERVALO_ACTUALIZACION);
    }

    private void IniciarServicioConGPS() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); //primero
        Log.i("servicio transporte", "LLEGOOOOOO::!!!!");
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) { //tercero
                latitude = location.getLatitude();
                longitud = location.getLongitude();
                velocidad=location.getSpeed()* 3.6f;
                Log.i("servicio transporte", "LLEGOOOOOO333333::!!!!");
                BroadcastReceiver BateriaRecivier=new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        context.unregisterReceiver(this);
                        int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
                        int level = -1;
                        if (currentLevel >=0 && scale > 0){
                            level= (currentLevel * 100)/scale;
                        }
                        nivel_bateria=String.valueOf(level);
                    }
                };
                IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(BateriaRecivier,batteryFilter);
                //verificamos la red
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                String estado_red="";

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HHmmss", Locale.getDefault());
                Date date = new Date();

                /*if(AccesoIntenet2()) {
                    Log.e("Servicio transporte","Servicio en ejecucion(1)--------------");
                    Log.e("Servicio transporte","Fecha: "+dateFormat.format(date));

                    new EnviarDatosMysql().execute(imei, String.valueOf(latitude), String.valueOf(longitud), nivel_bateria, String.valueOf(velocidad), "S", "S","N","fecha");
                }
                else
                {*/
                    EnviarDatosSQLite(imei, String.valueOf(latitude), String.valueOf(longitud), nivel_bateria, String.valueOf(velocidad), "S", "S");
                    Log.e("Servicio transporte","Servicio en ejecucion(2)--------------");
                    Log.e("Servicio transporte","Fecha2: "+dateFormat.format(date));
                //}

                hand.postDelayed(runnable, INTERVALO_ACTUALIZACION);

                try{
                    mLocationManager.removeUpdates(mLocationListener); //ultimo
                } catch (SecurityException e) {
                    Log.e("Servicio transporte","Error al ejecutar el servicio(1)--------------");
                    Log.e("Servicio transporte",e.getMessage());
                    Enviar_Mensaje("977743233","servicio(1)--"+e.getMessage());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                try {
                    Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(lastKnownLocation!=null){
                        latitude = lastKnownLocation.getLatitude();
                        longitud = lastKnownLocation.getLongitude();

                    }
                } catch (SecurityException e) {
                    Log.e("Servicio transporte","Error al ejecutar el servicio(2)--------------");
                    Log.e("Servicio transporte",e.getMessage());
                    Enviar_Mensaje("977743233","servicio(2)--"+e.getMessage());
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        try {
            Log.i("servicio transporte", "LLEGOOOOOO2222222::!!!!");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, mLocationListener); //segundo

        } catch (SecurityException e) {
            Log.e("Servicio transporte","Error al ejecutar el servicio(3)--------------");
            Log.e("Servicio transporte",e.getMessage());
            Enviar_Mensaje("977743233","servicio(3)--"+e.getMessage());
        }
    }

    private boolean AccesoGPS() {
        Boolean status = false;
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        System.out.println("Provider contains=> " + provider);
        if (provider.contains("gps") || provider.contains("network")){
            status = true;
        }
        return status;
    }

    private void EnviarDatosSQLite(String imei, String latitud, String longitud, String nivel_bateria, String velocidad, String datos_moviles, String estado_gps ) {

        dbManager = new DBManager(this);

        Calendar c = Calendar.getInstance();
        //c.set(Calendar.HOUR_OF_DAY, 24);
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        dateformat.setTimeZone(TimeZone.getTimeZone("America/Lima"));
        String fecha = dateformat.format(c.getTime())+" "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND);

        dbManager.insert(imei, Double.parseDouble(latitud), Double.parseDouble(longitud), fecha, nivel_bateria, velocidad,datos_moviles,estado_gps);

        //Enviar_Mensaje("977743233","Datos enviados");
    }

    private void Enviar_Mensaje(String numero, String mensaje) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(numero, null, mensaje, null, null);
    }

    public void EncenderPantalla(){
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakelockTag");
        wl.acquire();
    }

    public void ApagarPantalla(){
        if(wl != null)
        {
            if (wl.isHeld()) {
                wl.release();
            }
        }
    }

    private void EliminaDatosSQLite() {
        dbManager = new DBManager(this);

        try {
            dbManager.delete();
        }
        catch(Exception e){
            e.getMessage();
        }
    }

    private void Sqlite_Envio() {

        try {

            dbManager = new DBManager(this);
            ListaSQLite = new ArrayList<>(dbManager.select());

            ArrayList<JSONObject> lista = new ArrayList<>();


            for (DataLocation data : ListaSQLite) {
                JSONObject jObject = new JSONObject();
                jObject.put("vp_imei", data.getImei());
                jObject.put("vp_latitud", String.valueOf(data.getLatitud()));
                jObject.put("vp_longitud", String.valueOf(data.getLongitud()));
                jObject.put("vp_nivel_bateria", data.getNivel_bateria());
                jObject.put("vp_velocidad", data.getVelocidad());
                jObject.put("vp_gps", data.getEstado_gps());
                jObject.put("vp_internet", data.getDatos_moviles());
                jObject.put("vp_sqlite", "S");
                jObject.put("vp_fecha", data.getFec_reg());
                lista.add(jObject);
                indicador++;
            }

            new Enviar_datos().execute(lista.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class Enviar_datos extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;
        Random random = new Random();
        int id = random.nextInt(10) + 1;
        ArrayList<String> datos;


        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                url_new = new URL("https://www.jahesa.com/trackinn/index.php/android/Registrar_Datos_SQlite");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(50000);
                conn.setConnectTimeout(50000);
                conn.setRequestMethod("POST");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_datos", params[0]);
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            try {

                if(result.equals("1"))
                {
                    EliminaDatosSQLite();
                    Toast.makeText(LocationService.this.getApplicationContext(),"Liberando Memoria",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LocationService.this.getApplicationContext(),"Error al enviar datos",Toast.LENGTH_SHORT).show();
                }

            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
