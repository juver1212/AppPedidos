package com.trackiinn.apptrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
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

import dmax.dialog.SpotsDialog;


public class MenuLateralActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    LocationManager locationManager;
    private DBManager dbManager;
    private android.app.AlertDialog alertDialog;
    String documentos_pendientes = "", inicio_ruta ="", inicio_ruta_fecha ="", documentos_atendidos ="", conductor ="",
            cod_hoja="", num_hoja="", cod_empresa="", placa="";
    JSONArray json;
    Bundle args;
    protected PowerManager.WakeLock wakelock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);

        String imei = getImei(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        boolean flg_activate=false;
        boolean flg_activate_envio=false;

        Context c=this.getApplication();
        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo services : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(services.service.getClassName())) {
                flg_activate=true;
            }
        }
        if(!flg_activate){
            Intent service = new Intent(c, LocationService.class);
            c.startService(service);
        }

        /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyApp::MyWakelockTag");
        wakeLock.acquire();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        KeyguardManager.KeyguardLock lock = ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE)).newKeyguardLock(KEYGUARD_SERVICE);
        powerManager = ((PowerManager) getSystemService(Context.POWER_SERVICE));
        PowerManager.WakeLock wake = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakelockTag");

        lock.disableKeyguard();
        wake.acquire();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);*/

        /*final PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wakelock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakelock.acquire();*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setTitle("TRACKINN");
        new DatosInicio().execute(imei);


        /*Intent myIntent = new Intent();
        myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivity(myIntent);

        Intent intent = new Intent();
        String packageName = this.getPackageName();
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            this.startActivity(intent);
        }*/

        //IniciaServicioSQLite();
        //IniciaServicioEnvioDatos();

        //this.finish();
        //return;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lateral, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Context c=this.getApplication();
            locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            Intent service = new Intent(c, LocationService.class);
            c.stopService(service);

            //TerminarServicioSQLite();
            //TerminarServicioEnvioDatos();
        }
        if(id == R.id.action_reiniciar)
        {
            Context c=this.getApplication();
            locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
            Intent service = new Intent(c, LocationService.class);
            c.stopService(service);
            c.startService(service);

            //ReiniciarServicioSQLite();
            //ReiniciarServicioEnvioDatos();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (id == R.id.nav_camera){
            if(checkLocation()) {
                toolbar.setTitle("Lista de documentos");
                FragmentManager manager = getSupportFragmentManager();
                ListaDocumentosFragment nuevoFragmento1 = new ListaDocumentosFragment();
                manager.beginTransaction().replace(R.id.container2, nuevoFragmento1).commit();
            }
        } else if (id == R.id.nav_gallery) {
            if(checkLocation()) {
                toolbar.setTitle("Nuevo documento");
                FragmentManager manager = getSupportFragmentManager();
                NuevoDocumentoFragment nuevoFragmento2 = new NuevoDocumentoFragment();
                nuevoFragmento2.setArguments(args);
                manager.beginTransaction().replace(R.id.container2, nuevoFragmento2).commit();
            }
        } else if (id == R.id.nav_slideshow) {
            if(checkLocation())
            {
                toolbar.setTitle("Ocurrencias");
                FragmentManager manager = getSupportFragmentManager();
                OcurrenciaFragment nuevoFragmento3 = new OcurrenciaFragment();
                manager.beginTransaction().replace(R.id.container2, nuevoFragmento3).commit();
            }
        } else if (id == R.id.nav_manage) {
            if(checkLocation())
            {
                toolbar.setTitle("Registro de ubicación");
                FragmentManager manager = getSupportFragmentManager();
                HeramientasFragment nuevoFragmento4 = new HeramientasFragment();
                manager.beginTransaction().replace(R.id.container2, nuevoFragmento4).commit();
            }

        } else if (id == R.id.nav_share) {

            toolbar.setTitle("Validar datos moviles");
            FragmentManager manager = getSupportFragmentManager();
            ValidarDatosFragment nuevoFragmento = new ValidarDatosFragment();
            manager.beginTransaction().replace(R.id.container2, nuevoFragmento).commit();

        } else if (id == R.id.nav_send) {
            toolbar.setTitle("Validar datos moviles");
            FragmentManager manager = getSupportFragmentManager();
            ValidarDatosFragment nuevoFragmento = new ValidarDatosFragment();
            manager.beginTransaction().replace(R.id.container2, nuevoFragmento).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    private void Enviar_Mensaje(String numero, String mensaje){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(numero, null, mensaje, null, null);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("gps desactivado")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "para seguir usando esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private void Mensajesqlite(){
        final String[] result = new String[1];

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Porfavor ingresar contraseña para poder continuar.");
            final EditText input = new EditText(this);

            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());

            b.setView(input);
            b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    result[0] = input.getText().toString();
                    if(result[0].equals("Innovation"))
                    {
                        EliminaDatosSQLite();
                        Toast.makeText(getApplicationContext(), "Liberando Memoria", Toast.LENGTH_LONG).show();

                        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle("Registro de ubicación");
                        FragmentManager manager = getSupportFragmentManager();
                        HeramientasFragment nuevoFragmento4 = new HeramientasFragment();
                        manager.beginTransaction().replace(R.id.container2, nuevoFragmento4).commit();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Contraseña incorrecta", Toast.LENGTH_LONG).show();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(600);
                    }
                }
            });
            b.setNegativeButton("CANCEL", null);
            b.show();

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

    private class DatosInicio extends AsyncTask<String, Integer, String>
    {
        HttpURLConnection conn;
        URL url_new = null;
        int id = 10;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }



        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url_new = new URL("https://www.jahesa.com/trackinn/index.php/hojaruta/extraer_datos_bienvenida");

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

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_imei", params[0]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
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
                json = new JSONArray(result);
                String s = "";
                if (json.length() != 0) {
                    for (int i = 0; i < json.length(); i++) {
                        s = json.get(i).toString();
                        JSONObject last = new JSONObject(s);
                        last = json.getJSONObject(i);
                        documentos_atendidos = last.getString("documentos_entregados");
                        documentos_pendientes = last.getString("documentos_pendientes");
                        inicio_ruta = last.getString("inicio_ruta");
                        inicio_ruta_fecha = last.getString("inicio_ruta_fecha");
                        conductor = last.getString("conductor");
                        num_hoja = last.getString("num_hoja");
                        cod_hoja = last.getString("cod_hoja");
                        cod_empresa = last.getString("cod_empresa");
                        placa = last.getString("placa");
                    }
                }

                args = new Bundle();
                args.putString("cod_hoja", cod_hoja);
                args.putString("num_hoja", num_hoja);
                args.putString("cod_empresa", cod_empresa);
                args.putString("documentos_pendientes", documentos_pendientes);
                args.putString("inicio_ruta", inicio_ruta);
                args.putString("inicio_ruta_fecha", inicio_ruta_fecha);
                args.putString("documentos_atendidos", documentos_atendidos);
                args.putString("conductor", conductor);
                args.putString("placa", placa);

                FragmentManager manager1 = getSupportFragmentManager();
                BienvenidaFragment nuevoFragmento1 = new BienvenidaFragment();
                nuevoFragmento1.setArguments(args);
                manager1.beginTransaction().replace(R.id.container2, nuevoFragmento1).commit();

            }  catch (JSONException e) {
                Toast.makeText(MenuLateralActivity.this, "Problemas al cargar informacion", Toast.LENGTH_LONG).show();
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(600);
                e.printStackTrace();
            }
        }
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
