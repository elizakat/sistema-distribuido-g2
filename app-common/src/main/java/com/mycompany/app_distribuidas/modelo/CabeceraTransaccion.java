package com.mycompany.app_distribuidas.modelo;

import java.util.Date;

public class CabeceraTransaccion {
    // El ID generado automaticamente
    private int idTransaccion;
    private Date fecha;
    // Clave foranea hacia CuentaBancaria
    private int numeroCuenta;
    private String descripcion;

    public CabeceraTransaccion() {
    }

    public CabeceraTransaccion(int idTransaccion, Date fecha, int numeroCuenta, String descripcion) {
        this.idTransaccion = idTransaccion;
        this.fecha = fecha;
        this.numeroCuenta = numeroCuenta;
        this.descripcion = descripcion;
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

    public int getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(int numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
