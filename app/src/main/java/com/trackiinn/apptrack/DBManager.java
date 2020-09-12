package com.trackiinn.apptrack;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBManager {
    static private DatabaseHelper dbHelper;
    static private SQLiteDatabase database;
    private Context context;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public ArrayList<Integer> getData(String sql) {
        Cursor cursor;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();

        this.open();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.rawQuery(sql, null);
        while (cursor.moveToNext()){
            arrayList.add(cursor.getInt(0));
        }
        this.close();
        return arrayList;

    }

    public void insert(String imei, Double latitud, Double longitud, String fec_reg, String nivel_bateria, String velocidad, String datos_moviles, String estado_gps ) {
        this.open();
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.IMEI, imei);
        contentValue.put(DatabaseHelper.LATITUD, latitud);
        contentValue.put(DatabaseHelper.LONGITUD, longitud);
        contentValue.put(DatabaseHelper.FEC_REG, fec_reg);
        contentValue.put(DatabaseHelper.NIVEL_BATERIA, nivel_bateria);
        contentValue.put(DatabaseHelper.VELOCIDAD, velocidad);
        contentValue.put(DatabaseHelper.DATOS_MOVILES, datos_moviles);
        contentValue.put(DatabaseHelper.ESTADO_GPS, estado_gps);
        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
        this.close();
    }

    public ArrayList<DataLocation> select() {
        this.open();
        ArrayList<DataLocation> arrayList = new ArrayList<>();
    String[] columns = new String[]{DatabaseHelper.IMEI, DatabaseHelper.LATITUD, DatabaseHelper.LONGITUD, DatabaseHelper.FEC_REG,
            DatabaseHelper.NIVEL_BATERIA, DatabaseHelper.VELOCIDAD, DatabaseHelper.DATOS_MOVILES, DatabaseHelper.ESTADO_GPS, };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns
                , null, null, null, null, null);
        while (cursor.moveToNext()) {
            String imei = cursor.getString(0);
            Double latitud = cursor.getDouble(1);
            Double longitud = cursor.getDouble(2);
            String fec_reg = cursor.getString(3);
            String nivel_bateria = cursor.getString(4);
            String velocidad = cursor.getString(5);
            String datos_moviles = cursor.getString(6);
            String estado_gps = cursor.getString(7);



            arrayList.add(new DataLocation(imei,latitud,longitud,fec_reg,nivel_bateria,velocidad,datos_moviles,estado_gps));
        }
        this.close();
        return arrayList;
    }

   /* public void update(long codigo, String nombre, Double precio, byte[] imagen) {
        this.open();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.NOMBRE, nombre);
        contentValues.put(DatabaseHelper.PRECIO, precio);
        contentValues.put(DatabaseHelper.IMAGEN, imagen);
        int i = database.update(DatabaseHelper.TABLE_NAME
                , contentValues
                , DatabaseHelper.CODIGO + " = " + codigo
                , null);
        this.close();
    }
*/
    public void delete() {
        this.open();
        /*database.delete(DatabaseHelper.TABLE_NAME
                , DatabaseHelper.CODIGO + "=" + codigo
                , null);*/
        database.delete(DatabaseHelper.TABLE_NAME, null,null);
        this.close();
    }
}
