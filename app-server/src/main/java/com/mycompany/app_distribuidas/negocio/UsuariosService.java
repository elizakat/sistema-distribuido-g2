package com.mycompany.app_distribuidas.negocio;
import com.google.gson.*;
import com.mycompany.app_distribuidas.persistencia.Database;
import com.mycompany.app_distribuidas.protocolo.Mensajes;
import java.sql.*;
import java.util.*;
public class UsuariosService {
    private JsonObject fila(ResultSet r)throws SQLException{
        JsonObject x=new JsonObject();
        x.addProperty("idUsuario",r.getInt("ID_USUARIO"));
        x.addProperty("username",r.getString("USERNAME"));
        x.addProperty("estado",r.getString("ESTADO"));
        x.addProperty("tipoUsuario", "ADMIN");
        return x;
    }
    public Object ejecutar(String op,JsonObject p)throws SQLException{
        try(Connection c=Database.abrir()){
            if(op.equals("login")){
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM USUARIO WHERE USERNAME=? AND CLAVE=? AND ESTADO='A'")){
                    s.setString(1,Mensajes.str(p,"username"));
                    s.setString(2,Mensajes.str(p,"clave"));
                    try(ResultSet r=s.executeQuery()){
                        if(!r.next())throw new AppException("UNAUTHORIZED","Usuario o contraseña incorrectos");
                        return fila(r);
                    }
                }
            }
            if(op.equals("listar")){
                JsonArray a=new JsonArray();
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM USUARIO ORDER BY ID_USUARIO");
                ResultSet r=s.executeQuery()){
                    while(r.next())a.add(fila(r));
                }JsonObject x=new JsonObject();
                x.add("items",a);
                return x;
            }
            if(op.equals("obtener")){
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM USUARIO WHERE ID_USUARIO=?")){
                    s.setInt(1,p.get("idUsuario").getAsInt());
                    try(ResultSet r=s.executeQuery()){
                        if(!r.next())throw new AppException("NOT_FOUND","Usuario no encontrado");
                        return fila(r);
                    }
                }
            }
            if(op.equals("crear")){
                long id=Database.next(c,"SEQ_USUARIO");
                try(PreparedStatement s=c.prepareStatement("INSERT INTO USUARIO(ID_USUARIO,USERNAME,CLAVE,ESTADO,TIPO_USUARIO) VALUES(?,?,?,?,?)")){
                    s.setLong(1,id);
                    s.setString(2,Mensajes.str(p,"username"));
                    s.setString(3,Mensajes.str(p,"clave"));
                    s.setString(4,p.has("estado")?Mensajes.str(p,"estado"):"A");
                    s.setString(5,Mensajes.str(p,"tipoUsuario"));
                    s.executeUpdate();
                }return Map.of("idUsuario",id);
            }
            if(op.equals("actualizar")){
                try(PreparedStatement s=c.prepareStatement("UPDATE USUARIO SET USERNAME=?,ESTADO=?,TIPO_USUARIO=? "+(p.has("clave")&&!Mensajes.str(p,"clave").isBlank()?",CLAVE=?":"")+" WHERE ID_USUARIO=?")){
                    s.setString(1,Mensajes.str(p,"username"));
                    s.setString(2,Mensajes.str(p,"estado"));
                    s.setString(3,Mensajes.str(p,"tipoUsuario"));
                    int index=4;
                    if(p.has("clave")&&!Mensajes.str(p,"clave").isBlank())s.setString(index++,Mensajes.str(p,"clave"));
                    s.setInt(index,p.get("idUsuario").getAsInt());
                    if(s.executeUpdate()==0)throw new AppException("NOT_FOUND","Usuario no encontrado");
                }return Map.of("idUsuario",p.get("idUsuario").getAsInt());
            }
            if(op.equals("eliminar")){
                try(PreparedStatement s=c.prepareStatement("DELETE FROM USUARIO WHERE ID_USUARIO=?")){
                    s.setInt(1,p.get("idUsuario").getAsInt());
                    if(s.executeUpdate()==0)throw new AppException("NOT_FOUND","Usuario no encontrado");
                }return Map.of("idUsuario",p.get("idUsuario").getAsInt());
            }
            throw new AppException("UNKNOWN_ACTION","Acción desconocida");
        }
    }
}
