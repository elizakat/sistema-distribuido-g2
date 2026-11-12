package com.mycompany.app_distribuidas.modelo;

public class CuentaBancaria {
    // El ID de la cuenta
    private int numeroCuenta;
    private String cliente;
    private String descripcion;

    public CuentaBancaria() {
    }

    public CuentaBancaria(int numeroCuenta, String cliente, String descripcion) {
        this.numeroCuenta = numeroCuenta;
        this.cliente = cliente;
        this.descripcion = descripcion;
    }

    public int getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(int numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
