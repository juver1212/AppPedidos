package com.trackiinn.apptrack;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;



public class ValidarDatosFragment extends Fragment {

    private Button btnvalidar, btnvalidar_gps;
    private ImageView image_exito, image_exito_gps;

    public ValidarDatosFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_validar_datos, container, false);

        btnvalidar = (Button) view.findViewById(R.id.boton_validar_datos);
        btnvalidar_gps = (Button) view.findViewById(R.id.boton_validar_gps);
        image_exito = (ImageView) view.findViewById(R.id.datos_con_senal);
        image_exito_gps = (ImageView) view.findViewById(R.id.gps_con_senal);


        btnvalidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AccesoIntenet(getActivity())) {
                    image_exito.setImageResource(R.drawable.ic_datos_valida_3);
                }
                else
                {
                    image_exito.setImageResource(R.drawable.ic_datos_valida_2);
                }
            }
        });

        btnvalidar_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (GPSActivado(getActivity())) {
                    image_exito_gps.setImageResource(R.drawable.ic_gps_valida_1);
                }
                else
                {
                    image_exito_gps.setImageResource(R.drawable.ic_gps_valida_2);
                }
            }
        });

        return view;
    }

    public static boolean GPSActivado(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean AccesoIntenet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

}
