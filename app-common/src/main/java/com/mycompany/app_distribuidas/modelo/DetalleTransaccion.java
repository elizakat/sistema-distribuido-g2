package com.mycompany.app_distribuidas.modelo;

import java.util.Date;

public class DetalleTransaccion {
    // ID automatico del detalle
    private int idDetalle;
    // Clave foranea hacia CabeceraTransaccion
    private int idTransaccion;
    private Date fecha;
    // Clave foranea hacia TipoTransaccion
    private int idTipoTransaccion;
    private double valor;

    public DetalleTransaccion() {
    }

    public DetalleTransaccion(int idDetalle, int idTransaccion, Date fecha, int idTipoTransaccion, double valor) {
        this.idDetalle = idDetalle;
        this.idTransaccion = idTransaccion;
        this.fecha = fecha;
        this.idTipoTransaccion = idTipoTransaccion;
        this.valor = valor;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(int idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdTipoTransaccion() {
        return idTipoTransaccion;
    }

    public void setIdTipoTransaccion(int idTipoTransaccion) {
        this.idTipoTransaccion = idTipoTransaccion;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
