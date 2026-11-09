package com.mycompany.app_distribuidas.modelo;

public class FacturaLinea {
 private int idArticulo;
 private java.math.BigDecimal cantidad;
 private java.math.BigDecimal precioVenta;
 public int getIdArticulo(){return idArticulo;}
 public void setIdArticulo(int v){this.idArticulo=v;}
 public java.math.BigDecimal getCantidad(){return cantidad;}
 public void setCantidad(java.math.BigDecimal v){this.cantidad=v;}
 public java.math.BigDecimal getPrecioVenta(){return precioVenta;}
 public void setPrecioVenta(java.math.BigDecimal v){this.precioVenta=v;}
}