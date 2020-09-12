package com.trackiinn.apptrack;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

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
import java.util.Locale;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;


public class OcurrenciaFragment extends Fragment {


    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView descripcion, titulo;
    private ImageButton mSpeakBtn;
    private Button btnguardar;
    String url = "https://www.jahesa.com/trackinn/index.php/", imei = "";
    JSONArray json;
    ArrayList<String> datos;
    private AlertDialog alertDialog;
    double latitude = 0;
    double longitud = 0;
    LocationManager locationManager;

    public OcurrenciaFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container2,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ocurrencia, container2, false);


        descripcion = (TextView) view.findViewById(R.id.txtDescripcion);
        //titulo = (TextView) view.findViewById(R.id.txtTitulo);
        mSpeakBtn = (ImageButton) view.findViewById(R.id.btnSpeak);
        btnguardar = (Button) view.findViewById(R.id.boton2);

        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();


        mSpeakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GPSActivado(getActivity())) {

                    String title = "";// titulo.getText().toString();
                    String description = descripcion.getText().toString();
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
                    dateformat.setTimeZone(TimeZone.getTimeZone("America/Lima"));
                    String fecha = dateformat.format(c.getTime()) + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);


                    LocationManager locationManager = (LocationManager)
                            getActivity().getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                    double latitude = location.getLatitude();
                    double longitud = location.getLongitude();

                    String lat = String.valueOf(latitude);
                    String lon = String.valueOf(longitud);

                    new GuardarOcurrencia().execute(imei, title, description, fecha, "N", lat, lon);
                }
                else
                {
                    showAlert();
                }
            }
        });

        return view;
    }


    public static boolean AccesoIntenet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
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

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hola, estamos para ayudarte. Indicanos tu ocurrencia");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    descripcion.setText(result.get(0));
                }
                break;
            }

        }
    }

    public class GuardarOcurrencia extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setTheme(R.style.DialogWaiting).build();
            alertDialog.setTitle("Un momento");
            alertDialog.setMessage("Enviando información");
            alertDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "android/save_ocurrencia");

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
                        .appendQueryParameter("vp_imei", params[0])
                        .appendQueryParameter("vp_titulo", params[1])
                        .appendQueryParameter("vp_descripcion", params[2])
                        .appendQueryParameter("vp_fecha", params[3])
                        .appendQueryParameter("vp_sqlite", params[4])
                        .appendQueryParameter("vp_latitud", params[5])
                        .appendQueryParameter("vp_longitud", params[6]);
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
                Toast.makeText(getActivity(), "Problemas al enviar ocurrencia", Toast.LENGTH_LONG).show();
                return "exception";
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            alertDialog.dismiss();

            if(result.equals("exception"))
            {
                Toast.makeText(getActivity(), "Problemas al enviar ocurrencia", Toast.LENGTH_LONG).show();
            }
            else {
                descripcion.setText("");
                //titulo.setText("");
                Toast.makeText(getActivity(), "Ocurrencia registrada", Toast.LENGTH_LONG).show();
            }
        }

    }

}
