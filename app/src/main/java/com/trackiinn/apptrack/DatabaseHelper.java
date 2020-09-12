package com.trackiinn.apptrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre de la tabla
    public static final String TABLE_NAME = "LOCATION_IMEI";


    // Columnas de la tabla
    public static final String IMEI = "imei";
    public static final String LATITUD = "latitud";
    public static final String LONGITUD = "longitud";
    public static final String FEC_REG = "fec_reg";
    public static final String NIVEL_BATERIA = "nivel_bateria";
    public static final String VELOCIDAD = "velocidad";
    public static final String DATOS_MOVILES = "datos_moviles";
    public static final String ESTADO_GPS = "estado_gps";

    // Nombre de la base de datos
    static final String DB_NAME = "TRANSPORTE.DB";

    // Versión de la base de datos(importante)
    static final int DB_VERSION = 1;

    // Script para la creación de la tabla
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "("
            + IMEI + " TEXT  NOT NULL, "
            + LATITUD + " REAL NOT NULL, "
            + LONGITUD + " REAL NOT NULL, "
            + FEC_REG + " TEXT NOT NULL, "
            + NIVEL_BATERIA + " TEXT NOT NULL, "
            + VELOCIDAD + " REAL NOT NULL, "
            + DATOS_MOVILES + " TEXT NOT NULL, "
            + ESTADO_GPS + " TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }
}
