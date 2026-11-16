package com.mycompany.app_distribuidas.modelo;

public class TipoMovimiento {

    private int idTipoMovimiento;
    private String descripcion;
    private int signo;

    public TipoMovimiento() {
    }

    public int getIdTipoMovimiento() {
        return idTipoMovimiento;
    }

    public void setIdTipoMovimiento(
            int idTipoMovimiento) {

        this.idTipoMovimiento =
                idTipoMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion) {

        this.descripcion =
                descripcion;
    }

    public int getSigno() {
        return signo;
    }

    public void setSigno(
            int signo) {

        this.signo =
                signo;
    }
}