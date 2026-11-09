package com.mycompany.app_distribuidas.modelo;

public class Articulo {

    private int idArticulo;
    private String nombre;
    private double precio;
    private double stockActual;

    public Articulo() {
    }

    public Articulo(int idArticulo,
                    String nombre,
                    double precio,
                    double stockActual) {

        this.idArticulo = idArticulo;
        this.nombre = nombre;
        this.precio = precio;
        this.stockActual = stockActual;
    }

    public int getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }
}