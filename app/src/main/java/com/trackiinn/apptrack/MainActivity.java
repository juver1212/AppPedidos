package com.trackiinn.apptrack;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    public boolean indica = true;

    private static final int TIME = 4 * 1000;// 4 seconds
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StartAnimations();



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                verifyPermission();
            }
        }, TIME);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, TIME);

    }

    public static boolean GPSActivado(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showAlert() {
        final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
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

    private void verifyPermission() {
        int permsRequestCode = 100;
        String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,  Manifest.permission.RECORD_AUDIO,  Manifest.permission.SEND_SMS};

        int telefono = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int localizacion = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int camara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int escritura = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int mensaje = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);


        if (telefono == PackageManager.PERMISSION_GRANTED && localizacion == PackageManager.PERMISSION_GRANTED && camara == PackageManager.PERMISSION_GRANTED && escritura == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED && mensaje == PackageManager.PERMISSION_GRANTED)
        {
            if (GPSActivado(this)) {
                Intent inten = new Intent(this, MenuLateralActivity.class);
                startActivity(inten);
                MainActivity.this.finish();
            }
            else
            {
                showAlert();
            }

        } else {
            ActivityCompat.requestPermissions(this, perms, permsRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 100:
                for(int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] != 0){
                        indica = false;
                    }
                }
                if(indica)
                {
                    if (GPSActivado(this)) {
                        Intent inten = new Intent(this, MenuLateralActivity.class);
                        startActivity(inten);
                        MainActivity.this.finish();
                    }
                    else
                    {
                        showAlert();
                    }

                }
                else
                {
                    mostrarMensaje();
                }

                break;

        }
    }

    private void mostrarMensaje() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos incompletos")
                .setMessage("Se necesita la aprobación de los permisos para poder continuar.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                })
                .show();
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        RelativeLayout l=(RelativeLayout) findViewById(R.id.relative);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.logo);
        iv.clearAnimation();
        iv.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate2);
        anim.reset();
        ImageView iv2 = (ImageView) findViewById(R.id.logo2);
        iv2.clearAnimation();
        iv2.startAnimation(anim);
    }

}
