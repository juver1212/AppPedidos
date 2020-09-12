package com.trackiinn.apptrack;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

import dmax.dialog.SpotsDialog;

public class ListaDocumentosFragment extends Fragment {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    String nombre_chofer;
    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private ListView lv1;
    ListaGuiaAdapterNew adapter;
    //String url = "http://www.jahesa.com/trackinn/index.php/hojaruta/";
    String url = "https://www.jahesa.com/trackinn/index.php/hojaruta/";
    //extraer_imei
    ArrayList<ListaGuiaCampos> datos=new ArrayList<ListaGuiaCampos>();
    private AlertDialog alertDialog;
    String imei = "";
    Context c;
    JSONArray json;
    String guias[];

    public ListaDocumentosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container2,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_lista_documentos, container2, false);

        lv1 = (ListView) view.findViewById(R.id.lista);
        imei = getImei(getActivity());
        boolean flg_activate=false;

        adapter = new ListaGuiaAdapterNew(getActivity(),R.layout.listview_item_row, datos);
        new asyniniciosession().execute(imei);
        return view;
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    private class asyniniciosession extends AsyncTask<String, Integer, String> {
        HttpURLConnection conn;
        URL url_new = null;
        int progress = 0;
        Notification notification;
        NotificationManager notificationManager;
        int id = 10;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setTheme(R.style.DialogWaiting).build();
            alertDialog.setTitle("Un momento");
            alertDialog.setMessage("Cargando lista de documentos");
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                url_new = new URL(url + "/extraer_imei");
                Log.e("url a consultar ", url+ "/extraer_imei");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "exception";
            }
            try {
                //Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url_new.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_imei", params[0]);
                Log.e("el imei es ", params[0]);
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

                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line, linea = "";

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                        linea = linea + " " + line;
                    }

                    json = new JSONArray(linea);
                    String s = "";
                    if (json.length() != 0) {
                        for (int i = 0; i < json.length(); i++) {
                            s = json.get(i).toString();
                            JSONObject last = new JSONObject(s);
                            last = json.getJSONObject(i);
                            Log.e("objeto json ", last.toString());
                            nombre_chofer = last.getString("Nom_Chofer");
                            ListaGuiaCampos objguia = new ListaGuiaCampos();
                            objguia.guia = last.getString("Serie_Ref") + "-" + last.getString("Num_Ref");
                            objguia.destino = "";
                            if (Integer.parseInt(last.getString("flgvalidado")) != 0) {
                                if (last.getString("latitud") != "") {
                                    objguia.destino = last.getString("latitud") + "," + last.getString("longitud");
                                }
                            }
                            objguia.cliente = last.getString("Nom_Destino");
                            datos.add(objguia);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }
            return ("unexception");
        }


        @Override
        protected void onPostExecute(String result) {

            alertDialog.dismiss();

            if (result.equals("exception")) {
                Toast.makeText(getActivity(), "Problemas al cargar documentos", Toast.LENGTH_LONG).show();
            }

            if (datos.size() > 0) {
                adapter.UpdateData(datos);
                lv1.setAdapter(adapter);
            } else {
                Toast.makeText(getActivity(), "No existen documentos pendientes", Toast.LENGTH_LONG).show();
                Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(600);
            }

        }
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

    public class ListaGuiaAdapterNew extends ArrayAdapter<ListaGuiaCampos> {
        Context context;
        int layoutResourceId;
        ArrayList<ListaGuiaCampos> data=null;

        public ListaGuiaAdapterNew(Context context, int layoutResourceId, ArrayList<ListaGuiaCampos> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            if(data.size()>0) {
                this.data = data;
            }
        }
        public void UpdateData(ArrayList<ListaGuiaCampos> data) {
            if(data.size()>0) {
                this.data = data;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            WeatherHolder holder = null;

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new WeatherHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.imgIcon_mensaje = (ImageView)row.findViewById(R.id.imgIcon_mensaje);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDestino = (TextView)row.findViewById(R.id.txtDestino);


            /*if(row == null)
            {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                holder = new WeatherHolder();
                holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
                holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
                holder.txtDestino = (TextView)row.findViewById(R.id.txtDestino);
                //row.setTag(holder);
            }
            else
            {
                holder = (WeatherHolder)row.getTag();
            }*/

            final ListaGuiaCampos weather = data.get(position);
            holder.txtTitle.setText(weather.guia);
            holder.txtDestino.setText(weather.destino);

            if(weather.destino==""){
                holder.imgIcon.setVisibility(View.INVISIBLE);
            }

            holder.imgIcon.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(weather.destino!=""){
                        Context c=getContext();
                        Toast.makeText(c, "Cargando ruta" , Toast.LENGTH_LONG).show();

                        Vibrator vi = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vi.vibrate(600);

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        String[] datos_envio=weather.destino.split(",");
                        i.setData(Uri.parse("waze://?ll=" + datos_envio[0] + ", " + datos_envio[1] + "&navigate=yes"));
                        c.startActivity(i);
                    }
                }
            });

            holder.imgIcon_mensaje.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                        Context c=getContext();
                        Toast.makeText(c, "Enviando mensaje a cliente" , Toast.LENGTH_LONG).show();
                        Vibrator vi = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vi.vibrate(600);

                }
            });

            holder.txtTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (GPSActivado(getActivity())) {
                        Context c = getContext();
                        Bundle args = new Bundle();
                        args.putString("documento", weather.guia);
                        Fragment nuevoFragmento = new DetalleDocumentoFragment();
                        nuevoFragmento.setArguments(args);
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.container2, nuevoFragmento);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                    else
                    {
                        showAlert();
                    }
                }
            });

            return row;
        }

        public class WeatherHolder
        {
            ImageView imgIcon;
            ImageView imgIcon_mensaje;
            TextView txtTitle;
            TextView txtDestino;
        }
    }

}
