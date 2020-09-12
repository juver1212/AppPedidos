package com.trackiinn.apptrack;

import android.os.Parcel;
import android.os.Parcelable;

public class Clientes implements Parcelable {

    private String _id;
    private String _nombre;
    //private String _direccion;

    public Clientes(Parcel source){
        this._id = "0";
        this._nombre = "";
    }

    public Clientes() {

    }

    public void setId(String id){
        this._id = id;
    }

    public String getId(){
        return this._id;
    }

    public void setNombre(String nombre){
        this._nombre = nombre;
    }

    public String getNombre(){
        return this._nombre;
    }

   /* public void setDireccion(String direccion){
        this._direccion = direccion;
    }

    public String getDireccion(){
        return this._direccion;
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(_nombre);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Clientes createFromParcel(Parcel source) {
            return new Clientes(source);
        }

        @Override
        public Clientes[] newArray(int size) {
            return new Clientes[size];
        }

    };

    @Override
    public String toString() {
        return this._nombre;
    }

}