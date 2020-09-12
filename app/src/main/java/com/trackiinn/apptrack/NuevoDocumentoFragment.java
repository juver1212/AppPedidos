package com.trackiinn.apptrack;


import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Spinner;
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
import java.util.List;

import dmax.dialog.SpotsDialog;

public class NuevoDocumentoFragment extends Fragment {

    Spinner cboDocumento, cboDireccion;
    Button btnguardar;
    String url = "https://www.jahesa.com/trackinn/index.php/android/";
    JSONArray json;
    String guias[];
    ArrayList<Clientes> clientes=new ArrayList<Clientes>();
    ArrayList<Direcciones> direcciones=new ArrayList<Direcciones>();
    private ClientesAdapter adapter;
    private DireccionesAdapter adapterDireccion;
    private AlertDialog alertDialog;
    View vista;
    AutoCompleteTextView txtclientes;
    ArrayList<String> datos;
    String id_cliente, id_direccion;
    EditText serie_doc, num_doc, cliente, observacion;

    String documentos_pendientes = "", inicio_ruta ="", inicio_ruta_fecha ="", documentos_atendidos ="", conductor ="",
            cod_hoja="", num_hoja="", cod_empresa="", placa="";

    public NuevoDocumentoFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nuevo_documento, container, false);

        vista=view;

        btnguardar = (Button) view.findViewById(R.id.boton2);
        serie_doc = (EditText) view.findViewById(R.id.txtSerie);
        num_doc = (EditText) view.findViewById(R.id.txtNumero);
        observacion = (EditText) view.findViewById(R.id.txtObservacion);
        cboDocumento = (Spinner) view.findViewById(R.id.cbotipdoc);
        cboDireccion = (Spinner) view.findViewById(R.id.cboDireccion);
        txtclientes = (AutoCompleteTextView) view.findViewById(R.id.txtCliente) ;

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
            placa = bundle.getString("placa");
        }

        String[] Tipdocumentos = new String[] {"Guia de remision","Factura","Pedido","Cotizacion","Otros"};
        ArrayAdapter<String> adapterdocumentos = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice, Tipdocumentos);
        adapterdocumentos.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        cboDocumento.setAdapter(adapterdocumentos);

        String[] Tipdirecciones = new String[] {"Sin dirección"};
        ArrayAdapter<String> adapterdireccion = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_singlechoice, Tipdirecciones);
        adapterdireccion.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        cboDireccion.setAdapter(adapterdireccion);

        cboDireccion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position != 0) {
                    Direcciones direccion = adapterDireccion.getItem(position);
                /*Toast.makeText(getActivity(), "ID: " + direccion.getId() + "\nName: " + direccion.getDireccion(),
                        Toast.LENGTH_SHORT).show();*/
                    id_direccion = String.valueOf(direccion.getId());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {  }
        });

        txtclientes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
               Clientes user = adapter.getItem(position);
               /*Toast.makeText(getActivity(), "ID: " + user.getId() + "\nName: " + user.getNombre(),
                       Toast.LENGTH_SHORT).show();*/
               id_cliente = user.getId();
               new ListarDireccion().execute(String.valueOf(user.getId()));
           }
       });


        btnguardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (cod_hoja.equals("") || cod_hoja.equals("")) {
                    Toast.makeText(getActivity(), "No tiene hoja de ruta asiganada", Toast.LENGTH_LONG).show();
                    Vibrator v1 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v1.vibrate(600);
                } else {
                    String documento = cboDocumento.getSelectedItem().toString();
                    String serie = serie_doc.getText().toString();
                    String numero = num_doc.getText().toString();
                    String cliente = txtclientes.getText().toString();
                    String idcliente = id_cliente;
                    String direccion = cboDireccion.getSelectedItem().toString();
                    String iddireccion = id_direccion;
                    String observaciones = observacion.getText().toString();
                    String imei = getImei(getActivity());

                    new Guardar_Documento().execute(imei, documento, serie, numero, cliente, idcliente, direccion, iddireccion, observaciones, cod_hoja, placa);
                }
            }
        });

        new ListarClientes().execute(cod_empresa);
        return view;
    }

    public static String getImei(Context c) {
        TelephonyManager telephonyManager = (TelephonyManager) c
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    private class ListarClientes extends AsyncTask<String, String, String>
    {
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
                url_new = new URL(url+"/ListarClientes_Combo");

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
                        .appendQueryParameter("vp_empresa", params[0]);
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

                        Clientes cliente = new Clientes();
                        cliente.setId(last.getString("nro_doc"));
                        cliente.setNombre(last.getString("nombre"));
                        clientes.add(cliente);

                    }
                }

                if (clientes.size() > 0) {
                    adapter = new ClientesAdapter(getActivity(), android.R.layout.select_dialog_singlechoice, clientes);
                    txtclientes.setAdapter(adapter);

                } else {
                    Toast.makeText(getActivity(), "No existen clientes en la lista", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            }  catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private class ListarDireccion extends AsyncTask<String, String, String>
    {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            alertDialog= new SpotsDialog.Builder().setContext(getActivity()).build();
            alertDialog.setTitle("Un momento");
            alertDialog.setMessage("Cargando direcciones del cliente");
            alertDialog.show();

        }
        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url_new = new URL(url+"/ListarDirecciones_Combo");

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
                        .appendQueryParameter("vp_documento", params[0]);
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

                        Direcciones direccion = new Direcciones();
                        direccion.setId(last.getInt("id_direccion"));
                        direccion.setDireccion(last.getString("direccion"));
                        direcciones.add(direccion);

                    }
                }
                alertDialog.dismiss();
                if (direcciones.size() > 0) {
                    adapterDireccion = new DireccionesAdapter(getActivity(), android.R.layout.select_dialog_singlechoice, direcciones);
                    cboDireccion.setAdapter(adapterDireccion);

                } else {
                    Toast.makeText(getActivity(), "Cliente no cuenta con direccion registrada", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            }  catch (JSONException e) {
                Toast.makeText(getActivity(), "Cliente no cuenta con direccion registrada", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
    public class ClientesAdapter extends ArrayAdapter<Clientes> {

        Context context;
        int  textViewResourceId;
        List<Clientes> items, tempItems, suggestions;

        public ClientesAdapter(Context context, int textViewResourceId, List<Clientes> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
            tempItems = new ArrayList<Clientes>(items);
            suggestions = new ArrayList<Clientes>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Clientes accnum = items.get(position);
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setText(accnum.getNombre());
            label.setTextColor(getResources().getColor(R.color.colorBlack));
            return label;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        Filter nameFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                String str = ((Clientes) resultValue).getNombre();
                return str;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {

                    List<Clientes> suggestions = new ArrayList<Clientes>();
                    FilterResults filterResults = new FilterResults();

                    for (Clientes accno : tempItems) {
                        if (accno.getNombre().toLowerCase().contains(constraint.toString().toLowerCase())) {

                            suggestions.add(accno);
                        }
                    }

                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();


                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults
                    results) {
                List<Clientes> filterList = (ArrayList<Clientes>)
                        results.values;
                if (results != null && results.count > 0) {
                    clear();
                    for (Clientes accnum : filterList) {
                        add(accnum);
                        notifyDataSetChanged();
                    }
                }
            }
        };
    }

    public class DireccionesAdapter extends ArrayAdapter<Direcciones> {

        Context context;
        int resource, textViewResourceId;
        List<Direcciones> items, tempItems, suggestions;

        public DireccionesAdapter(Context context, int textViewResourceId, List<Direcciones> items) {
            super(context, textViewResourceId, items);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            this.items = items;
            tempItems = new ArrayList<Direcciones>(items); // this makes the difference.
            suggestions = new ArrayList<Direcciones>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Direcciones accnum = items.get(position);
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setTextColor(getResources().getColor(R.color.colorBlack));
            label.setText(accnum.getDireccion());
            return label;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }

        Filter nameFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                String str = ((Clientes) resultValue).getNombre();
                return str;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {

                    List<Direcciones> suggestions = new ArrayList<Direcciones>();
                    FilterResults filterResults = new FilterResults();

                    for (Direcciones accno : tempItems) {
                        if (accno.getDireccion().toLowerCase().contains(constraint.toString().toLowerCase())) {

                            suggestions.add(accno);
                        }
                    }

                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();


                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults
                    results) {
                List<Direcciones> filterList = (ArrayList<Direcciones>)
                        results.values;
                if (results != null && results.count > 0) {
                    clear();
                    for (Direcciones accnum : filterList) {
                        add(accnum);
                        notifyDataSetChanged();
                    }
                }
            }
        };
    }

    private class Guardar_Documento extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url_new = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setTheme(R.style.DialogWaiting).build();
            alertDialog.setMessage("Guardando documento");
            alertDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            datos = new ArrayList<String>();
            try {
                // Enter URL address where your php file resides
                url_new = new URL(url+ "Guardar_Documento");

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
                        .appendQueryParameter("vp_tip_doc", params[1])
                        .appendQueryParameter("vp_serie", params[2])
                        .appendQueryParameter("vp_numero", params[3])
                        .appendQueryParameter("vp_cliente_descripcion", params[4])
                        .appendQueryParameter("vp_cliente", params[5])
                        .appendQueryParameter("vp_direccion_descripcion", params[6])
                        .appendQueryParameter("vp_direccion", params[7])
                        .appendQueryParameter("vp_tip_doc_ref", "")
                        .appendQueryParameter("vp_serie_ref", "")
                        .appendQueryParameter("vp_numero_ref", "")
                        .appendQueryParameter("vp_peso", "")
                        .appendQueryParameter("vp_observacion", params[8])
                        .appendQueryParameter("vp_cod_hoja", params[9])
                        .appendQueryParameter("vp_placa", params[10]);
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
                    //return(result.toString());
                        return("succes");

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

                if (result.equals("succes")) {
                    Toast.makeText(getActivity(), "Guardado Correctamente", Toast.LENGTH_LONG).show();

                    serie_doc.setText("");
                    num_doc.setText("");
                    observacion.setText("");
                    cboDocumento.setSelection(0);
                    cboDireccion.setSelection(0);
                    txtclientes.setText("");

                    FragmentManager manager1 = getActivity().getSupportFragmentManager();
                    ListaDocumentosFragment nuevoFragmento1 = new ListaDocumentosFragment();
                    manager1.beginTransaction().replace(R.id.container2, nuevoFragmento1).commit();

                } else {
                    Toast.makeText(getActivity(), "Error al Guardar", Toast.LENGTH_LONG).show();
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(600);
                }

            /*}  catch ( e) {
                e.printStackTrace();
            }*/
        }
    }

}
