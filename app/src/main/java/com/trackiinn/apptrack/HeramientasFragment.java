package com.trackiinn.apptrack;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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


public class HeramientasFragment extends Fragment {

    JSONArray json;
    String url = "https://www.jahesa.com/trackinn/index.php/",status = "";
    NotificationManager mNotifyManager;
    NotificationCompat.Builder builder;
    public int indicador = 0;
    private static List<DataLocation> ListaSQLite;
    GridView grid = null;
    private DBManager dbManager;
    String[] letters = new String[] {
            "A", "B", "C", "D", "E",
            "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O",
            "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"};

    public HeramientasFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_heramientas, container, false);

        grid = (GridView) view.findViewById(R.id.gridView);
        Button button= (Button) view.findViewById(R.id.button);
        dbManager = new DBManager(getActivity());


        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(getActivity(),
                        ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
            }
        });



        List<DataLocation> ListaSQLite = new ArrayList<>(dbManager.select());
        String[] arr = toArray(ListaSQLite);


        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, arr);
        grid.setAdapter(adapter);


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Sqlite_Envio();
            }
        });


        return  view;
    }

    public static String[] toArray(List<DataLocation> a) {
        String[] arr = new String[a.size()*3];
        int i = 0;

            for (DataLocation data : a) {
                if(String.valueOf(data.getLongitud()).length() > 10) {
                    arr[i] = String.valueOf(data.getLongitud()).substring(0, 10);
                }
                else
                {
                    arr[i] = String.valueOf(data.getLongitud());
                }

                if(String.valueOf(data.getLatitud()).length() > 10) {
                    arr[i+1] = String.valueOf(data.getLatitud()).substring(0, 10);
                }
                else
                {
                    arr[i+1] = String.valueOf(data.getLatitud());
                }


                arr[i+2] = String.valueOf(data.getFec_reg()).replace(".-2019","");
                i = i + 3;
            }
        return arr;
    }

    private void Sqlite_Envio() {

        try {

            dbManager = new DBManager(getActivity());
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
        int progress = 0;
        Notification notification;
        NotificationManager notificationManager;
        Random random = new Random();
        int id = random.nextInt(10) + 1;
        ArrayList<String> datos;

        protected void onPreExecute() {
            IniciarProgressBar("Enviando registros","Enviando al servidor",id);
        }

        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+ "android/Registrar_Datos_SQlite");

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
                        .appendQueryParameter("vp_datos", params[0]);
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
                TerminarProgressBar("Operacion cancelada","Error al enviar datos",id);
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
                TerminarProgressBar("Operacion cancelada","Error al enviar datos",id);
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
                    Toast.makeText(getActivity(), "Liberando Memoria", Toast.LENGTH_LONG).show();

                    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
                    toolbar.setTitle("Registro de ubicación");
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    HeramientasFragment nuevoFragmento4 = new HeramientasFragment();
                    manager.beginTransaction().replace(R.id.container2, nuevoFragmento4).commit();

                    TerminarProgressBar("Datos enviados","Operación exitosa",id);
                    /*Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);*/
                }
                else
                {
                    TerminarProgressBar("Operacion cancelada","Error al enviar datos",id);
                }

            }  catch (Exception e) {
                TerminarProgressBar("Operacion cancelada","Error al enviar datos",id);
                e.printStackTrace();
            }
        }
    }

    private void TerminarProgressBar(String title, String subtitle, int id){
        builder.setContentText(subtitle).setContentTitle(title).setProgress(0,0,false);
        mNotifyManager.notify(id, builder.build());
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

    private void EliminaDatosSQLite() {
        dbManager = new DBManager(getActivity());

        try {
            dbManager.delete();
        }
        catch(Exception e){
            e.getMessage();
        }
    }
}
