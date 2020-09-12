package com.trackiinn.apptrack;

import android.os.Parcel;
import android.os.Parcelable;

public class Direcciones implements Parcelable {

    private int _id;
    private String _direccion;

    public Direcciones(Parcel source){
        this._id = 0;
        this._direccion = "";
    }

    public Direcciones() {

    }

    public void setId(int id){
        this._id = id;
    }

    public int getId(){
        return this._id;
    }


    public void setDireccion(String direccion){
        this._direccion = direccion;
    }

    public String getDireccion(){
        return this._direccion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(_direccion);
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
        return this._direccion;
    }

}