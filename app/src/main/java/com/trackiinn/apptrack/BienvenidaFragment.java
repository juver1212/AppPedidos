package com.trackiinn.apptrack;


import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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


public class BienvenidaFragment extends Fragment {

    private Button btninicio;
    private AlertDialog alertDialog;
    String imei = "";
    Context c;
    JSONArray json;
    String url = "https://www.jahesa.com/trackinn/index.php/hojaruta/";
    public static final int CONNECTION_TIMEOUT=50000;
    public static final int READ_TIMEOUT=50000;
    private TextView titulo1, subtitulo1, titulo2, subtitulo2, titulo3, subtitulo3, txtconductor;
    String documentos_pendientes = "", inicio_ruta ="", inicio_ruta_fecha ="", documentos_atendidos ="", conductor ="",
            cod_hoja="", num_hoja="", cod_empresa="";

    public BienvenidaFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_bienvenida, container, false);


        btninicio = (Button) v.findViewById(R.id.boton2);
        titulo1 = (TextView) v.findViewById(R.id.titulo1);
        subtitulo1 = (TextView) v.findViewById(R.id.subtitulo1);
        titulo2 = (TextView) v.findViewById(R.id.titulo2);
        subtitulo2 = (TextView) v.findViewById(R.id.subtitulo2);
        titulo3 = (TextView) v.findViewById(R.id.titulo3);
        subtitulo3 = (TextView) v.findViewById(R.id.subtitulo3);
        txtconductor = (TextView) v.findViewById(R.id.txtconductor);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            cod_hoja = bundle.getString("cod_hoja");
            num_hoja = bundle.getString("num_hoja");
            cod_empresa = bundle.getString("cod_empresa");
            documentos_pendientes = bundle.getString("documentos_pendientes");
            inicio_ruta = bundle.getString("inicio_ruta");
            inicio_ruta_fecha = bundle.getString("inicio_ruta_fecha");
            documentos_atendidos = bundle.getString("documentos_atendidos");
            conductor = bundle.getString("conductor");

            txtconductor.setText(conductor);

            if (inicio_ruta.equals("") || inicio_ruta.equals("null")) {
                subtitulo1.setText("Tienes "+documentos_pendientes+" entregas pendientes");
                subtitulo2.setText("No tienes ruta asignada");
                subtitulo3.setText("Tienes "+documentos_pendientes+" entregas pendientes");
            } else {
                subtitulo1.setText("Tienes "+documentos_pendientes+" entregas pendientes");
                subtitulo2.setText("La ruta inicio el "+ inicio_ruta_fecha+" a las "+inicio_ruta+"");
                subtitulo3.setText(documentos_atendidos+" entregas realizadas");
            }

        }



        btninicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment nuevoFragmento = new ListaDocumentosFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container2, nuevoFragmento);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        imei = getImei(getActivity());

        //new asyniniciosession().execute(imei);

        return v;
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /*
    private class asyniniciosession extends AsyncTask<String, Integer, String>
    {
        HttpURLConnection conn;
        URL url_new = null;
        int progress = 0;
        Notification notification;
        NotificationManager notificationManager;
        int id = 10;
        String documentos_pendientes = "", inicio_ruta ="", inicio_ruta_fecha ="", documentos_atendidos ="", conductor ="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }



        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+"/extraer_datos_bienvenida");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url_new.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_imei", params[0]);
                Log.e("el imei es ",params[0]);
                //.appendQueryParameter("password", params[1]);
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
                    Log.e("la respuesta es  ",result.toString());

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
                        Log.e("objeto json ", last.toString());
                        documentos_atendidos = last.getString("documentos_entregados");
                        documentos_pendientes = last.getString("documentos_pendientes");
                        inicio_ruta = last.getString("inicio_ruta");
                        inicio_ruta_fecha = last.getString("inicio_ruta_fecha");
                        conductor = last.getString("conductor");
                    }
                }

                //alertDialog.dismiss();
                txtconductor.setText(conductor);

                if (inicio_ruta.equals("") || inicio_ruta.equals("null")) {
                    subtitulo1.setText("Tienes "+documentos_pendientes+" entregas pendientes");
                    subtitulo2.setText("No tienes ruta asignada");
                    subtitulo3.setText("Tienes "+documentos_pendientes+" entregas pendientes");
                } else {
                    subtitulo1.setText("Tienes "+documentos_pendientes+" entregas pendientes");
                    subtitulo2.setText("La ruta inicio el "+ inicio_ruta_fecha+" a las "+inicio_ruta+"");
                    subtitulo3.setText(documentos_atendidos+" entregas realizadas");
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    */
}
