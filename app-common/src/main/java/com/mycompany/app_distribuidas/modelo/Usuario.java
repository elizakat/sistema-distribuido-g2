package com.mycompany.app_distribuidas.modelo;

public class Usuario {

    private int idUsuario;
    private String username;
    private String clave;
    private String estado;
    private String tipoUsuario;

    public Usuario() {
    }

    public Usuario(int idUsuario,
                   String username,
                   String clave,
                   String estado,
                   String tipoUsuario) {

        this.idUsuario = idUsuario;
        this.username = username;
        this.clave = clave;
        this.estado = estado;
        this.tipoUsuario = tipoUsuario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoUsuario() {
    return tipoUsuario;
}

public void setTipoUsuario(String tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
}
    
}