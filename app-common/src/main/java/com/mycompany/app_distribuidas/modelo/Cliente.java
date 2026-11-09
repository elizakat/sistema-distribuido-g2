package com.mycompany.app_distribuidas.modelo;

public class Cliente {
 private int idCliente;
 private String ruc;
 private String nombre;
 private String direccion;
 public int getIdCliente(){return idCliente;}
 public void setIdCliente(int v){this.idCliente=v;}
 public String getRuc(){return ruc;}
 public void setRuc(String v){this.ruc=v;}
 public String getNombre(){return nombre;}
 public void setNombre(String v){this.nombre=v;}
 public String getDireccion(){return direccion;}
 public void setDireccion(String v){this.direccion=v;}
}