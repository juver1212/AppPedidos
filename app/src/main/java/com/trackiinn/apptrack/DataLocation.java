package com.trackiinn.apptrack;

public class DataLocation {
    private String imei;
    private Double latitud;
    private Double longitud;
    private String fec_reg;
    private String nivel_bateria;
    private String velocidad;
    private String datos_moviles;
    private String estado_gps;


    public DataLocation(String imei, Double latitud, Double longitud, String fec_reg, String nivel_bateria, String velocidad, String datos_moviles, String estado_gps) {
        this.imei = imei;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fec_reg = fec_reg;
        this.nivel_bateria = nivel_bateria;
        this.velocidad = velocidad;
        this.datos_moviles = datos_moviles;
        this.estado_gps = estado_gps;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getFec_reg() {
        return fec_reg;
    }

    public void setFec_reg(String fec_reg) {
        this.fec_reg = fec_reg;
    }

    public String getNivel_bateria() {
        return nivel_bateria;
    }

    public void setNivel_bateria(String nivel_bateria) {
        this.nivel_bateria = nivel_bateria;
    }

    public String getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(String velocidad) {
        this.velocidad = velocidad;
    }

    public String getDatos_moviles() {
        return datos_moviles;
    }

    public void setDatos_moviles(String datos_moviles) {
        this.datos_moviles = datos_moviles;
    }

    public String getEstado_gps() {
        return estado_gps;
    }

    public void setEstado_gps(String estado_gps) {
        this.estado_gps = estado_gps;
    }

}
