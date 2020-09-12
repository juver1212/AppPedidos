package com.trackiinn.apptrack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

public class EnvioService extends Service {

    private static final long INTERVALO_ACTUALIZACION = 120000; // En ms
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    JSONArray json;
    String url="https://www.jahesa.com/trackinn/index.php/";
    String imei="";
    Handler hand = new Handler();
    NotificationCompat.Builder builder;
    private DBManager dbManager;
    public int indicador = 0;
    private static List<DataLocation> ListaSQLite;


    public EnvioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("servicio envio", "Servicio iniciado");
        hand.postDelayed(runnable,INTERVALO_ACTUALIZACION);
        return START_STICKY;
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(AccesoIntenet())
            {
                new Enviar_datos().execute();
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(2, new Notification());
        Log.e("Servicio Envio","Oncreate--------------");
    }
    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            mLocationManager.removeUpdates(mLocationListener);
            Log.e("Servicio Envio","Ondestroy Try--------------");
            Enviar_Mensaje("977743233","Ondestroy Try--------------");

        } catch (SecurityException e) {
            Log.e("Servicio Envio","Ondestroy Cath--------------");
            Enviar_Mensaje("977743233","Ondestroy Cath--------------");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.jahesa.apppedidos";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(3, notification);
    }

    public Boolean AccesoIntenet() {

        ConnectivityManager cm;
        NetworkInfo ni;
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        ni = cm.getActiveNetworkInfo();

        if (ni != null) {
            return true;
        }
        else {
            return false;
        }
    }

    private class Enviar_datos extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;
        ArrayList<String> datos;
        ArrayList<JSONObject> lista;

        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                dbManager = new DBManager(EnvioService.this);
                ListaSQLite = new ArrayList<>(dbManager.select());
                lista = new ArrayList<>();

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
            } catch (JSONException e) {
                Enviar_Mensaje("977743233","Error al extraer datos del Sqlite");
                e.printStackTrace();
            }
            try {
                url_new = new URL(url+ "android/Registrar_Datos_SQlite");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Enviar_Mensaje("977743233","Error al conectarse a url");
                return "exception";
            }
            try {
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(50000);
                conn.setConnectTimeout(50000);
                conn.setRequestMethod("POST");

                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_datos", lista.toString());
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                Enviar_Mensaje("977743233","Error en respuesta del url");
                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                if (response_code == HttpURLConnection.HTTP_OK) {

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
                Enviar_Mensaje("977743233","Error en parsear respuesta del servidor");
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
                    Log.e("Servicio Envio","Enviado correctamente");
                    hand.postDelayed(runnable, INTERVALO_ACTUALIZACION);
                    Enviar_Mensaje("977743233","Registros enviados correctamente: "+lista.size());
                    EliminaDatosSQLite();
                }
                else
                {
                    Enviar_Mensaje("977743233","Error al enviar al los registros del Sqlite");
                }

            }  catch (Exception e) {
                e.printStackTrace();
                Enviar_Mensaje("977743233","Error en OnPostExecute");
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

    private void Enviar_Mensaje(String numero, String mensaje){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(numero, null, mensaje, null, null);
    }
}
