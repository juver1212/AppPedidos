package com.trackiinn.apptrack;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import dmax.dialog.SpotsDialog;

import android.app.NotificationManager;

public class DetalleDocumentoFragment extends Fragment {

    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Transporte/";
    private File file = new File(ruta_fotos);
    Uri uri;
    ImageView cuadro;
    TextView texto;
    Button boton, boton2, boton_despacho;
    CheckBox check, check_atendido;
    ListView lv1;
    EditText edit1, edit2;
    Spinner spi;
    ArrayAdapter<String> adapter;
    ArrayList<String> datos;

    private AlertDialog alertDialog;
    String status = "", myBase64Image = "", num_guia = "", imei = "", url = "https://www.jahesa.com/trackinn/index.php/", transportista = "", hora = "", lat_extraida = "", lon_extraida = "", hora_atencion = "", lat_ext_aten = "", lon_ext_aten = "";
    int cantidad_fotos = 0;
    Context c;
    JSONArray json;
    double latitude = 0;
    double longitud = 0;
    Bitmap bitmap;
    LocationManager locationManager;
    MyLocationListener mlocListener;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder builder;


    public DetalleDocumentoFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container2,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_documento, container2, false);
        imei=getImei(getActivity());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocListener.setMainActivity(this);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) mlocListener);


        boton_despacho = (Button) view.findViewById(R.id.button);
        boton = (Button) view.findViewById(R.id.boton);
        texto = (TextView) view.findViewById(R.id.txtHeader);
        check = (CheckBox) view.findViewById(R.id.chk1);
        check_atendido = (CheckBox) view.findViewById(R.id.chk2);
        edit1 = (EditText) view.findViewById(R.id.campo_mensaje);
        edit2 = (EditText) view.findViewById(R.id.editText);
        spi = (Spinner) view.findViewById(R.id.spinner);


        //LLENADO DE SPINNER OBSERVACION
        Spinner spinner_animales = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter spinner_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.Observacion, android.R.layout.select_dialog_singlechoice);
        spinner_adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner_animales.setAdapter(spinner_adapter);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            num_guia = bundle.getString("documento");
        }

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Doc. N°" + num_guia);

        new Validar_Checkbox().execute(num_guia);
        new Validar_Foto().execute(num_guia);

        Button button= (Button) view.findViewById(R.id.boton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                file.mkdirs();
                String file = ruta_fotos + getCode() + ".jpg";
                File mi_foto = new File(file);
                try {
                    mi_foto.createNewFile();
                } catch (IOException ex) {
                    Log.e("ERROR ", "Error:" + ex);
                }
                uri = Uri.fromFile(mi_foto);
                //Abre la camara para tomar la foto
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //Guarda imagen
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                //Retorna a la actividad
                startActivityForResult(cameraIntent, 0);
            }
        });

        boton_despacho.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (GPSActivado(getActivity())) {
                    String det_obs = edit1.getText().toString();
                    String obs = spi.getSelectedItem().toString();
                    String km = edit2.getText().toString();
                    String lat = String.valueOf(latitude);
                    String lon = String.valueOf(longitud);

                    //SI ES DESPACHADO --------------------------

                    if (obs.equals("Despacho normal") || obs.equals("Otros despachos")) {

                        if (!det_obs.equals("") && !km.equals("") && check.isChecked() == true) {
                            if (!transportista.equals("")) {
                                if (cantidad_fotos != 0) {
                                    new Despachar_Guia().execute(num_guia, det_obs, obs, km, lat, lon);
                                } else {
                                    Toast.makeText(getActivity(), "No se ha realizado ninguna foto", Toast.LENGTH_LONG).show();
                                    Vibrator d = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                    d.vibrate(600);
                                }
                            } else {
                                new Despachar_Guia().execute(num_guia, det_obs, obs, km, lat, lon);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Campos incorrectos", Toast.LENGTH_LONG).show();
                            Vibrator d = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                            d.vibrate(600);
                        }
                    }

                    //SI NO ES DESPACHADO --------------------------

                    else if (!obs.equals("Despacho normal") && !obs.equals("Otros despachos") && !obs.equals("")) {

                        if (!det_obs.equals("") && !km.equals("") && check.isChecked() == true) {
                            new Despachar_Guia().execute(num_guia, det_obs, obs, km, lat, lon);
                        } else {
                            Toast.makeText(getActivity(), "No se ha realizado ninguna foto", Toast.LENGTH_LONG).show();
                            Vibrator d = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                            d.vibrate(600);
                        }
                    }

                    //SI NO SE SELECCIONA NADA  --------------------------

                    else {
                        Toast.makeText(getActivity(), "Seleccione Observacion ", Toast.LENGTH_LONG).show();
                        Vibrator d = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        d.vibrate(600);
                    }
                } else {
                    showAlert();
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (GPSActivado(getActivity())) {
                    boolean isChecked = ((CheckBox) view).isChecked();

                    if (isChecked) {
                        check.setChecked(true);
                        new MarcadoCheck().execute(num_guia, String.valueOf(latitude), String.valueOf(longitud));
                    } else {
                        //cbMarcame.setText("Checkbox desmarcado!");
                    }
                }
                else
                {
                    showAlert();
                }
            }
        });

        check_atendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GPSActivado(getActivity())) {
                    boolean isChecked = ((CheckBox) view).isChecked();

                    if (isChecked) {
                        check_atendido.setChecked(true);
                        new MarcadoCheck_Atencion_Cliente().execute(num_guia, String.valueOf(latitude), String.valueOf(longitud));
                    } else {
                        //cbMarcame.setText("Checkbox desmarcado!");
                    }
                }
                else
                {
                    showAlert();
                }
            }
        });

        return  view;
    }

    public static boolean GPSActivado(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showAlert() {
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(getActivity());
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

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public class MyLocationListener implements LocationListener {
        DetalleDocumentoFragment mainActivity;

        public DetalleDocumentoFragment getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(DetalleDocumentoFragment mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();
            String Text = "Mi ubicaci—n actual es: " + "\n Lat = "
                    + loc.getLatitude() + "\n Long = " + loc.getLongitude();
            this.mainActivity.setLocation(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            edit1.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            edit1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    public void setLocation(Location loc) {
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address address = list.get(0);
                    //edit1.setText("Mi direcci—n es: \n" + address.getAddressLine(0));
                    latitude = loc.getLatitude();
                    longitud = loc.getLongitude();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Subir_foto extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        int progress = 0;
        Notification notification;
        NotificationManager notificationManager;
        Random random = new Random();
        int id = random.nextInt(10) + 1;

        protected void onPreExecute() {
            IniciarProgressBar("Cargando foto","Enviando al servidor",id);
        }

        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "hojaruta/Subir_Foto");

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
                        .appendQueryParameter("vp_guia", params[0])
                        .appendQueryParameter("vp_foto", params[1])
                        .appendQueryParameter("vp_imei", imei);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                TerminarProgressBar("Operacion cancelada","Error al enviando al servidor",id);
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
                    // Pass data to onPostExecute method
                    return(result.toString());

                }else{

                    return("unsuccessful");
                }

            } catch (IOException e) {
                TerminarProgressBar("Operacion cancelada","Error al enviando al servidor",id);
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
                        status = last.getString("STATUS");
                        datos.add(status);
                    }

                }

                if (datos.size() > 0) {
                    TerminarProgressBar("Foto guardada","Operación exitosa",id);
                    new Validar_Foto().execute(num_guia);
                } else {
                    TerminarProgressBar("Operacion cancelada","Error al enviando al servidor",id);
                    //Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    //v.vibrate(600);
                }

            }  catch (JSONException e) {
                TerminarProgressBar("Operacion cancelada","Error al enviando al servidor",id);
                e.printStackTrace();
            }
        }
    }

    private class MarcadoCheck_Atencion_Cliente extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... params) {

            datos = new ArrayList<String>();

            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "hojaruta/Grabar_Hora_Atencion");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_guia", params[0])
                        .appendQueryParameter("vp_latitud", params[1])
                        .appendQueryParameter("vp_longitud", params[2]);
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
                    // Pass data to onPostExecute method
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
                        String documento = last.getString("status");
                        datos.add(documento);
                    }

                }

                if (datos.size() > 0) {
                    //Toast.makeText(getActivity(), "Preparando despacho", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Error de llegada", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class MarcadoCheck extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "hojaruta/Grabar_Hora_Llegada");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_guia", params[0])
                        .appendQueryParameter("vp_latitud", params[1])
                        .appendQueryParameter("vp_longitud", params[2]);
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
                    // Pass data to onPostExecute method
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
                        String documento = last.getString("status");
                        datos.add(documento);
                    }

                }

                if (datos.size() > 0) {
                   // Toast.makeText(getActivity(), "Preparando despacho", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Error de llegada", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class Validar_Checkbox extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setTheme(R.style.DialogWaiting).build();
            alertDialog.setTitle("Un momento");
            alertDialog.setMessage("Validando informacion");
            alertDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "hojaruta/Validar_Checkbox");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_guia", params[0]);
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
                    // Pass data to onPostExecute method
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
                        hora = last.getString("Hor_LLeg");
                        lat_extraida = last.getString("latitud");
                        lon_extraida = last.getString("longitud");
                        //CHECKBOX DE ATENCION ----------------
                        hora_atencion = last.getString("cliente_atencion");
                        lat_ext_aten = last.getString("latitud_atencion");
                        lon_ext_aten = last.getString("longitud_atencion");
                        transportista = last.getString("serie_guia_trasporte");
                        datos.add(hora);
                    }

                }

                alertDialog.dismiss();
                if (!hora.equals("")) {
                    check.setChecked(true);
                    check.setEnabled(false);
                }

                if(!hora_atencion.equals("")){
                    check_atendido.setChecked(true);
                    check_atendido.setEnabled(false);
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class Despachar_Guia extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setTheme(R.style.DialogWaiting).build();
            alertDialog.setMessage("Guardando información");
            alertDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "hojaruta/Actualizar_Guia");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_guia", params[0])
                        .appendQueryParameter("vp_det_obs", params[1])
                        .appendQueryParameter("vp_obs", params[2])
                        .appendQueryParameter("vp_km", params[3])
                        .appendQueryParameter("vp_lat", params[4])
                        .appendQueryParameter("vp_lon", params[5])
                        .appendQueryParameter("vp_imei", imei);
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
                    // Pass data to onPostExecute method
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
                        String estado = last.getString("status");
                        datos.add(estado);
                    }

                }

                alertDialog.dismiss();
                if (datos.size() > 0) {
                    Toast.makeText(getActivity(), "Guardado Correctamente", Toast.LENGTH_LONG).show();
                    Fragment nuevoFragmento = new ListaDocumentosFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container2, nuevoFragmento);
                    transaction.addToBackStack(null);
                    transaction.commit();

                } else {
                    Toast.makeText(getActivity(), "Errro al Guardar", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class Validar_Foto extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "hojaruta/Validar_Foto");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_guia", params[0])
                        .appendQueryParameter("vp_imei", imei);
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
                    // Pass data to onPostExecute method
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
                        cantidad_fotos = last.getInt("cantidad");
                    }
                }
            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @SuppressLint("SimpleDateFormat")
    private String getCode() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoCode = "img_" + date;
        return photoCode;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            Uri imageUri = uri;

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);

                final long maxBytes = 1024 * 1024;
                final Bitmap scaledBitmap = ReducirImagen2(bitmap,maxBytes);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                new Subir_foto().execute(num_guia,encodedImage);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Bitmap ReducirImagen(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap ReducirImagen2(final Bitmap input, final long maxBytes) {
        final int currentWidth = input.getWidth();
        final int currentHeight = input.getHeight();
        final int currentPixels = currentWidth * currentHeight;
        // Get the amount of max pixels:
        // 1 pixel = 4 bytes (R, G, B, A)
        final long maxPixels = maxBytes / 4; // Floored
        if (currentPixels <= maxPixels) {
            // Already correct size:
            return input;
        }
        // Scaling factor when maintaining aspect ratio is the square root since x and y have a relation:
        final double scaleFactor = Math.sqrt(maxPixels / (double) currentPixels);
        final int newWidthPx = (int) Math.floor(currentWidth * scaleFactor);
        final int newHeightPx = (int) Math.floor(currentHeight * scaleFactor);
        final Bitmap output = Bitmap.createScaledBitmap(input, newWidthPx, newHeightPx, true);
        return output;
    }

    public void onClick2(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("waze://?ll=" + lat_extraida + ", " + lon_extraida + "&navigate=yes"));
        this.startActivity(i);
    }

    private void IniciarProgressBar(String title, String subtitle, int id) {
        builder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.mipmap.ic_laucher_ja)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setProgress(100, 0, true);

        Intent notificationIntent = new Intent(String.valueOf(getActivity()));
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Add as notification
        mNotifyManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel nChannel = new NotificationChannel("Channel"+String.valueOf(id), "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            nChannel.enableLights(true);
            assert mNotifyManager != null;
            builder.setChannelId("Channel"+String.valueOf(id));
            mNotifyManager.createNotificationChannel(nChannel);
        }
        assert mNotifyManager != null;
        mNotifyManager.notify(id, builder.build());

    }

    private void TerminarProgressBar(String title, String subtitle, int id){
        builder.setContentText(subtitle).setContentTitle(title).setProgress(0,0,false);
        mNotifyManager.notify(id, builder.build());
    }

}
