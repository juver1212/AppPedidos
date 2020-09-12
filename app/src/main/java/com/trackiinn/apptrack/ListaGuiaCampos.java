package com.trackiinn.apptrack;

/**
 * Created by desarrollo on 18/08/2017.
 */
public class ListaGuiaCampos {
    public String guia;
    public String cliente;
    public String destino;
    public ListaGuiaCampos(){
        super();
    }

    public ListaGuiaCampos(String guia,String cliente,String destino) {
        super();
        this.guia = guia;
        this.cliente = cliente;
        this.destino = destino;
    }
}