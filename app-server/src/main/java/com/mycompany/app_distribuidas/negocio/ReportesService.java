package com.mycompany.app_distribuidas.negocio;
import com.google.gson.*;
import com.mycompany.app_distribuidas.persistencia.Database;
import java.sql.*;
public class ReportesService {
    public Object ejecutar(String tipo,JsonObject p)throws SQLException{
        if(!p.has("desde")||!p.has("hasta"))throw new AppException("VALIDATION_ERROR","Fechas desde y hasta obligatorias");
        java.sql.Date desde=java.sql.Date.valueOf(p.get("desde").getAsString()),hasta=java.sql.Date.valueOf(p.get("hasta").getAsString());
        if(desde.after(hasta))throw new AppException("VALIDATION_ERROR","Rango de fechas incorrecto");
        String sql=switch(tipo){
            case "ventasPorCiudad"->"SELECT c.NOMBRE_CIUDAD AS ETIQUETA,SUM(f.TOTAL) AS TOTAL FROM FACTURA f JOIN CIUDAD_ENTREGA c ON c.ID_CIUDAD=f.ID_CIUDAD WHERE f.ESTADO='CONFIRMADA' AND f.FECHA_EMISION>=? AND f.FECHA_EMISION<? GROUP BY c.NOMBRE_CIUDAD ORDER BY c.NOMBRE_CIUDAD";
            case "ventasClienteArticulo"->"SELECT a.NOMBRE AS ARTICULO,c.NOMBRE AS CLIENTE,SUM(d.CANTIDAD*d.PRECIO_VENTA) AS TOTAL FROM FACTURA f JOIN DETALLE_FACTURA d ON d.ID_FACTURA=f.ID_FACTURA JOIN ARTICULO a ON a.ID_ARTICULO=d.ID_ARTICULO JOIN CLIENTE c ON c.ID_CLIENTE=f.ID_CLIENTE WHERE f.ESTADO='CONFIRMADA' AND f.FECHA_EMISION>=? AND f.FECHA_EMISION<? GROUP BY a.NOMBRE,c.NOMBRE ORDER BY a.NOMBRE,c.NOMBRE";
            default->throw new AppException("UNKNOWN_ACTION","Reporte desconocido");
        };
        JsonArray out=new JsonArray();
        try(Connection c=Database.abrir();
        PreparedStatement s=c.prepareStatement(sql)){
            s.setDate(1,desde);
            s.setDate(2,java.sql.Date.valueOf(hasta.toLocalDate().plusDays(1)));
            try(ResultSet r=s.executeQuery()){
                while(r.next()){
                    JsonObject x=new JsonObject();
                    if(tipo.equals("ventasPorCiudad")){
                        x.addProperty("ciudad",r.getString(1));
                        x.addProperty("total",r.getBigDecimal(2));
                    }else{
                        x.addProperty("articulo",r.getString(1));
                        x.addProperty("cliente",r.getString(2));
                        x.addProperty("total",r.getBigDecimal(3));
                    }out.add(x);
                }
            }
        }JsonObject result=new JsonObject();
        result.add("items",out);
        return result;
    }
}
