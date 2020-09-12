package com.trackiinn.apptrack;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
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

/**
 * Un {@link IntentService} que simula un proceso en primer plano
 * <p>
 */
public class ProgressIntentService extends IntentService {
    private static final String TAG = ProgressIntentService.class.getSimpleName();

    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Transporte/";
    private File file = new File(ruta_fotos);
    Uri uri;
    ImageView cuadro;
    TextView texto;
    Button boton, boton2;
    CheckBox check;
    ListView lv1;
    EditText edit1, edit2;
    Spinner spi;
    ArrayAdapter<String> adapter;
    ArrayList<String> datos;
    private ProgressDialog pDialog;
    String  status="",myBase64Image="",num_guia = "", imei = "", url = "https://www.jahesa.com/transporte/index.php/", hora = "", lat_extraida = "", lon_extraida = "";
    Context c;
    JSONArray json;
    String guias[];
    double latitude;
    double longitud;
    Bitmap bitmap;
    String guia="",foto="";
    public Uri url_URI;
    public ProgressIntentService() {
        super("ProgressIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_RUN_ISERVICE.equals(action)) {
                handleActionRun();
            }
        }
    }
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        guia = intent.getExtras().getString("guia");
        foto = intent.getExtras().getString("foto");
    }

    /**
     * Maneja la acción de ejecución del servicio
     */
    private void handleActionRun() {
      try {
            // Se construye la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle("Enviando Imagen")
                    .setContentText("Subiendo...");

            for (int i = 1; i <= 20; i++) {

                // Poner en primer plano
                builder.setProgress(20, i, true);
                startForeground(1, builder.build());

                //TODO EL CODIGO VA AQUI
                new Subir_foto().execute(num_guia,foto);

                Thread.sleep(1000);
            }
            // Quitar de primer plano
            stopForeground(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Imagen Enviada...", Toast.LENGTH_SHORT).show();
        // Emisión para avisar que se terminó el servicio
        Intent localIntent = new Intent(Constants.ACTION_PROGRESS_EXIT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    private class Subir_foto extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... params) {
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
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("vp_guia", params[0])
                        .appendQueryParameter("vp_foto", params[1]);
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
                        status = last.getString("STATUS");
                        datos.add(status);
                    }

                }

                if (datos.size() > 0) {
                    Toast.makeText(getApplicationContext(), "Guardado Correctamente", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error al guardar foto", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /*class Subir_foto extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
                HttpConnectionParams.setSoTimeout(httpParams, 6000);
                Log.e("insertar", "insertando variable" + params[0]);
                HttpParams p = new BasicHttpParams();
                p.setParameter("vp_guia", params[0]);
                p.setParameter("vp_foto", params[1]);

                HttpClient httpclient = new DefaultHttpClient(p);
                HttpPost httppost = new HttpPost(url + "hojaruta/Subir_Foto");
                Log.e("url a consultar", url + "hojaruta");
                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("vp_guia", params[0]));
                    nameValuePairs.add(new BasicNameValuePair("vp_foto", params[1]));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    //Log.e("respuesta json desc","despues   poner set identity");
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    Log.e("respuesta json login", "despues de poner responsehandler-------" + httppost.toString());

                    String responseBody = httpclient.execute(httppost, responseHandler);
                    Log.e("respuesta json login", "despues de poner responsehandler");
                    // Parse
                    Log.e("respuesta json login", responseBody);

                    json = new JSONArray(responseBody);
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

                    Log.e("respuesta json desc", s);

                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e("mensaje sin json", e.getMessage());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("mensaje con json", e.getMessage());
                }
            } catch (Throwable t) {
                Log.e("mensaje", t.toString());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (datos.size() > 0) {
                Toast.makeText(getApplicationContext(), "Guardado Correctamente", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Error al guardar foto", Toast.LENGTH_LONG).show();
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(600);
            }
        }
    }*/

}
