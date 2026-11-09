package com.mycompany.app_distribuidas.negocio;
import com.google.gson.*;
import com.mycompany.app_distribuidas.persistencia.Database;
import com.mycompany.app_distribuidas.protocolo.Mensajes;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
public class CatalogosService {
    private record Tabla(String name,String id,String[] cols,String seq){
    }
    private Tabla tabla(String tipo){
        return switch(tipo){
            case "cliente"->new Tabla("CLIENTE","ID_CLIENTE",new String[]{
                "RUC","NOMBRE","DIRECCION"
            },"SEQ_CLIENTE");
            case "ciudad"->new Tabla("CIUDAD_ENTREGA","ID_CIUDAD",new String[]{
                "NOMBRE_CIUDAD"
            },"SEQ_CIUDAD");
            case "articulo"->new Tabla("ARTICULO","ID_ARTICULO",new String[]{
                "NOMBRE","PRECIO","STOCK_ACTUAL"
            },"SEQ_ARTICULO");
            default->throw new AppException("UNKNOWN_ACTION","Catálogo desconocido");
        };
    }
    private String key(String col){
        return switch(col){
            case "ID_CLIENTE"->"idCliente";
            case "ID_CIUDAD"->"idCiudad";
            case "ID_ARTICULO"->"idArticulo";
            case "NOMBRE_CIUDAD"->"nombreCiudad";
            case "STOCK_ACTUAL"->"stockActual";
            default->col.toLowerCase();
        };
    }
    private JsonObject row(ResultSet r,Tabla t)throws SQLException{
        JsonObject x=new JsonObject();
        x.addProperty(key(t.id),r.getLong(t.id));
        for(String col:t.cols){
            if(col.equals("PRECIO")||col.equals("STOCK_ACTUAL")){
                BigDecimal n=r.getBigDecimal(col);
                x.addProperty(key(col),n);
            }else x.addProperty(key(col),r.getString(col));
        }return x;
    }
    public Object ejecutar(String tipo,String op,JsonObject p)throws SQLException{
        Tabla t=tabla(tipo);
        try(Connection c=Database.abrir()){
            if(op.equals("listar")){
                JsonArray arr=new JsonArray();
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM "+t.name+" ORDER BY "+t.id);
                ResultSet r=s.executeQuery()){
                    while(r.next())arr.add(row(r,t));
                }JsonObject result=new JsonObject();
                result.add("items",arr);
                return result;
            }
            if(op.equals("obtener")){
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM "+t.name+" WHERE "+t.id+"=?")){
                    s.setLong(1,p.get(key(t.id)).getAsLong());
                    try(ResultSet r=s.executeQuery()){
                        if(!r.next())throw new AppException("NOT_FOUND","Registro no encontrado");
                        return row(r,t);
                    }
                }
            }
            if(op.equals("crear")||op.equals("actualizar")){
                for(String col:t.cols){
                    if(!p.has(key(col))||p.get(key(col)).isJsonNull())throw new AppException("VALIDATION_ERROR","Campo requerido: "+key(col));
                }StringBuilder sql=new StringBuilder();
                long id=0;
                if(op.equals("crear")){
                    id=Database.next(c,t.seq);
                    sql.append("INSERT INTO "+t.name+" ("+t.id+","+String.join(",",t.cols)+") VALUES (?"+",?".repeat(t.cols.length)+")");
                }
                else{
                    if(!p.has(key(t.id)))throw new AppException("VALIDATION_ERROR","ID requerido");
                    id=p.get(key(t.id)).getAsLong();
                    sql.append("UPDATE "+t.name+" SET ");
                    for(int i=0;
                    i<t.cols.length;
                    i++){
                        if(i>0)sql.append(",");
                        sql.append(t.cols[i]).append("=?");
                    }sql.append(" WHERE ").append(t.id).append("=?");
                }
                try(PreparedStatement s=c.prepareStatement(sql.toString())){
                    int index=1;
                    if(op.equals("crear"))s.setLong(index++,id);
                    for(String col:t.cols){
                        String k=key(col);
                        if(col.equals("PRECIO")||col.equals("STOCK_ACTUAL"))s.setBigDecimal(index++,p.get(k).getAsBigDecimal());
                        else s.setString(index++,p.get(k).getAsString());
                    }if(op.equals("actualizar"))s.setLong(index,id);
                    int count=s.executeUpdate();
                    if(count==0)throw new AppException("NOT_FOUND","Registro no encontrado");
                }return Map.of(key(t.id),id);
            }
            if(op.equals("eliminar")){
                long id=p.get(key(t.id)).getAsLong();
                try(PreparedStatement s=c.prepareStatement("DELETE FROM "+t.name+" WHERE "+t.id+"=?")){
                    s.setLong(1,id);
                    if(s.executeUpdate()==0)throw new AppException("NOT_FOUND","Registro no encontrado");
                }return Map.of(key(t.id),id);
            }
            throw new AppException("UNKNOWN_ACTION","Operación no disponible: "+op);
        }
    }
}
