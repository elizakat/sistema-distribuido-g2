package com.mycompany.app_distribuidas.negocio;
import com.google.gson.*;
import com.mycompany.app_distribuidas.persistencia.Database;
import com.mycompany.app_distribuidas.protocolo.Mensajes;
import java.sql.*;
import java.math.*;
import java.util.*;
public class FacturaService {
    private BigDecimal decimal(JsonObject x,String key){
        if(!x.has(key)||x.get(key).isJsonNull())throw new AppException("VALIDATION_ERROR","Falta "+key);
        return x.get(key).getAsBigDecimal();
    }
    private int entero(JsonObject x,String k){
        if(!x.has(k))throw new AppException("VALIDATION_ERROR","Falta "+k);
        return x.get(k).getAsInt();
    }
    private String estado(Connection c,long id,boolean lock)throws SQLException{
        String sql="SELECT ESTADO FROM FACTURA WHERE ID_FACTURA=?"+(lock?" FOR UPDATE":"");
        try(PreparedStatement s=c.prepareStatement(sql)){
            s.setLong(1,id);
            try(ResultSet r=s.executeQuery()){
                if(!r.next())throw new AppException("NOT_FOUND","Factura no encontrada");
                return r.getString(1);
            }
        }
    }
    private void borrador(Connection c,long id)throws SQLException{
        if(!estado(c,id,true).equals("BORRADOR"))throw new AppException("CONFLICT","Solo se permite modificar facturas en borrador");
    }
    private JsonObject cabecera(ResultSet r)throws SQLException{
        JsonObject o=new JsonObject();
        o.addProperty("idFactura",r.getLong("ID_FACTURA"));
        o.addProperty("numeroFactura",r.getString("NUMERO_FACTURA"));
        o.addProperty("idCliente",r.getInt("ID_CLIENTE"));
        o.addProperty("idCiudad",r.getInt("ID_CIUDAD"));
        o.addProperty("fechaEmision",r.getDate("FECHA_EMISION").toString());
        o.addProperty("subtotal",r.getBigDecimal("SUBTOTAL"));
        o.addProperty("impuesto",r.getBigDecimal("IMPUESTO"));
        o.addProperty("total",r.getBigDecimal("TOTAL"));
        o.addProperty("estado",r.getString("ESTADO"));
        return o;
    }
    private Object obtener(Connection c,long id)throws SQLException{
        JsonObject result=new JsonObject();
        try(PreparedStatement s=c.prepareStatement("SELECT * FROM FACTURA WHERE ID_FACTURA=?")){
            s.setLong(1,id);
            try(ResultSet r=s.executeQuery()){
                if(!r.next())throw new AppException("NOT_FOUND","Factura no encontrada");
                result.add("factura",cabecera(r));
            }
        }JsonArray d=new JsonArray();
        try(PreparedStatement s=c.prepareStatement("SELECT d.*,a.NOMBRE FROM DETALLE_FACTURA d JOIN ARTICULO a ON a.ID_ARTICULO=d.ID_ARTICULO WHERE ID_FACTURA=? ORDER BY ID_DETALLE_FAC")){
            s.setLong(1,id);
            try(ResultSet r=s.executeQuery()){
                while(r.next()){
                    JsonObject x=new JsonObject();
                    x.addProperty("idDetalleFac",r.getLong("ID_DETALLE_FAC"));
                    x.addProperty("idArticulo",r.getLong("ID_ARTICULO"));
                    x.addProperty("nombreArticulo",r.getString("NOMBRE"));
                    x.addProperty("cantidad",r.getBigDecimal("CANTIDAD"));
                    x.addProperty("precioVenta",r.getBigDecimal("PRECIO_VENTA"));
                    d.add(x);
                }
            }
        }result.add("detalles",d);
        return result;
    }
    private void lineas(Connection c,long id,JsonArray lista)throws SQLException{
        if(lista==null||lista.isEmpty())throw new AppException("VALIDATION_ERROR","Ingrese al menos un artículo");
        BigDecimal total=BigDecimal.ZERO;
        Set<Integer> repetidos=new HashSet<>();
        for(JsonElement el:lista){
            JsonObject d=el.getAsJsonObject();
            int art=entero(d,"idArticulo");
            BigDecimal cant=decimal(d,"cantidad");
            if(cant.signum()<=0)throw new AppException("VALIDATION_ERROR","Cantidad debe ser positiva");
            if(!repetidos.add(art))throw new AppException("VALIDATION_ERROR","No repita artículos: "+art);
            BigDecimal precio;
            try(PreparedStatement s=c.prepareStatement("SELECT PRECIO FROM ARTICULO WHERE ID_ARTICULO=?")){
                s.setInt(1,art);
                try(ResultSet r=s.executeQuery()){
                    if(!r.next())throw new AppException("NOT_FOUND","Artículo no existe: "+art);
                    precio=r.getBigDecimal(1);
                }
            }long detalleId=Database.next(c,"SEQ_DETALLE_FAC");
            try(PreparedStatement s=c.prepareStatement("INSERT INTO DETALLE_FACTURA(ID_DETALLE_FAC,ID_FACTURA,ID_ARTICULO,CANTIDAD,PRECIO_VENTA) VALUES(?,?,?,?,?)")){
                s.setLong(1,detalleId);
                s.setLong(2,id);
                s.setInt(3,art);
                s.setBigDecimal(4,cant);
                s.setBigDecimal(5,precio);
                s.executeUpdate();
            }total=total.add(precio.multiply(cant));
        }total=total.setScale(2,RoundingMode.HALF_UP);
        try(PreparedStatement s=c.prepareStatement("UPDATE FACTURA SET SUBTOTAL=?, IMPUESTO=0, TOTAL=? WHERE ID_FACTURA=?")){
            s.setBigDecimal(1,total);
            s.setBigDecimal(2,total);
            s.setLong(3,id);
            s.executeUpdate();
        }
    }
    private Object transaccion(String op,JsonObject p)throws SQLException{
        try(Connection c=Database.abrir()){
            c.setAutoCommit(false);
            try{
                long id;
                Object result;
                switch(op){
                    case "crear"->{
                        id=Database.next(c,"SEQ_FACTURA");
                        String fecha=p.has("fechaEmision")?Mensajes.str(p,"fechaEmision"):java.time.LocalDate.now().toString();
                        try(PreparedStatement s=c.prepareStatement("INSERT INTO FACTURA(ID_FACTURA,ID_CLIENTE,ID_CIUDAD,NUMERO_FACTURA,FECHA_EMISION,SUBTOTAL,IMPUESTO,TOTAL,ESTADO) VALUES(?,?,?, ?,?,0,0,0,'BORRADOR')")){
                            s.setLong(1,id);
                            s.setInt(2,entero(p,"idCliente"));
                            s.setInt(3,entero(p,"idCiudad"));
                            s.setString(4,"FAC-"+String.format("%06d",id));
                            s.setDate(5,java.sql.Date.valueOf(fecha));
                            s.executeUpdate();
                        }lineas(c,id,p.getAsJsonArray("detalles"));
                        result=obtener(c,id);
                    }
                    case "actualizar"->{
                        id=entero(p,"idFactura");
                        borrador(c,id);
                        try(PreparedStatement s=c.prepareStatement("UPDATE FACTURA SET ID_CLIENTE=?,ID_CIUDAD=? WHERE ID_FACTURA=?")){
                            s.setInt(1,entero(p,"idCliente"));
                            s.setInt(2,entero(p,"idCiudad"));
                            s.setLong(3,id);
                            s.executeUpdate();
                        }try(PreparedStatement s=c.prepareStatement("DELETE FROM DETALLE_FACTURA WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            s.executeUpdate();
                        }lineas(c,id,p.getAsJsonArray("detalles"));
                        result=obtener(c,id);
                    }
                    case "eliminar"->{
                        id=entero(p,"idFactura");
                        borrador(c,id);
                        try(PreparedStatement s=c.prepareStatement("DELETE FROM DETALLE_FACTURA WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            s.executeUpdate();
                        }try(PreparedStatement s=c.prepareStatement("DELETE FROM FACTURA WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            s.executeUpdate();
                        }result=Map.of("idFactura",id);
                    }
                    case "confirmar"->{
                        id=entero(p,"idFactura");
                        borrador(c,id);
                        List<Integer> ids=new ArrayList<>();
                        try(PreparedStatement s=c.prepareStatement("SELECT ID_ARTICULO FROM DETALLE_FACTURA WHERE ID_FACTURA=? ORDER BY ID_ARTICULO")){
                            s.setLong(1,id);
                            try(ResultSet r=s.executeQuery()){
                                while(r.next())ids.add(r.getInt(1));
                            }
                        }if(ids.isEmpty())throw new AppException("VALIDATION_ERROR","Factura sin detalles");
                        Map<Integer,BigDecimal> stocks=new HashMap<>();
                        for(int art:ids){
                            try(PreparedStatement s=c.prepareStatement("SELECT STOCK_ACTUAL FROM ARTICULO WHERE ID_ARTICULO=? FOR UPDATE")){
                                s.setInt(1,art);
                                try(ResultSet r=s.executeQuery()){
                                    if(!r.next())throw new AppException("NOT_FOUND","Artículo no encontrado");
                                    stocks.put(art,r.getBigDecimal(1));
                                }
                            }
                        }int tipo=tipoMovimiento(c,-1);
                        long movimiento=Database.next(c,"SEQ_MOVIMIENTO");
                        try(PreparedStatement s=c.prepareStatement("INSERT INTO MOVIMIENTO_INVENTARIO(ID_MOVIMIENTO,ID_TIPO_MOV,FECHA,OBSERVACION) VALUES(?,?,SYSDATE,?)")){
                            s.setLong(1,movimiento);
                            s.setInt(2,tipo);
                            s.setString(3,"Factura "+id);
                            s.executeUpdate();
                        }try(PreparedStatement s=c.prepareStatement("SELECT ID_ARTICULO,CANTIDAD,PRECIO_VENTA FROM DETALLE_FACTURA WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            try(ResultSet r=s.executeQuery()){
                                while(r.next()){
                                    int art=r.getInt(1);
                                    BigDecimal cantidad=r.getBigDecimal(2);
                                    if(stocks.get(art).compareTo(cantidad)<0)throw new AppException("INSUFFICIENT_STOCK","Stock insuficiente para artículo "+art);
                                    stocks.put(art,stocks.get(art).subtract(cantidad));
                                    try(PreparedStatement update=c.prepareStatement("UPDATE ARTICULO SET STOCK_ACTUAL=STOCK_ACTUAL-? WHERE ID_ARTICULO=?")){
                                        update.setBigDecimal(1,cantidad);
                                        update.setInt(2,art);
                                        update.executeUpdate();
                                    }try(PreparedStatement ins=c.prepareStatement("INSERT INTO DETALLE_MOVIMIENTO(ID_DETALLE_MOV,ID_MOVIMIENTO,ID_ARTICULO,CANTIDAD,PRECIO) VALUES(?,?,?,?,?)")){
                                        ins.setLong(1,Database.next(c,"SEQ_DETALLE_MOV"));
                                        ins.setLong(2,movimiento);
                                        ins.setInt(3,art);
                                        ins.setBigDecimal(4,cantidad);
                                        ins.setBigDecimal(5,r.getBigDecimal(3));
                                        ins.executeUpdate();
                                    }
                                }
                            }
                        }try(PreparedStatement s=c.prepareStatement("INSERT INTO FACTURA_MOVIMIENTO(ID_FACTURA,ID_MOVIMIENTO) VALUES(?,?)")){
                            s.setLong(1,id);
                            s.setLong(2,movimiento);
                            s.executeUpdate();
                        }try(PreparedStatement s=c.prepareStatement("UPDATE FACTURA SET ESTADO='CONFIRMADA' WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            s.executeUpdate();
                        }result=Map.of("idFactura",id,"idMovimiento",movimiento,"estado","CONFIRMADA");
                    }
                    case "anular"->{
                        id=entero(p,"idFactura");
                        if(!estado(c,id,true).equals("CONFIRMADA"))throw new AppException("CONFLICT","Solo puede anular factura confirmada");
                        try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM DETALLE_COBRO WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            try(ResultSet r=s.executeQuery()){
                                r.next();
                                if(r.getInt(1)>0)throw new AppException("CONFLICT","La factura tiene cobros registrados");
                            }
                        }List<Integer> ids=new ArrayList<>();
                        try(PreparedStatement s=c.prepareStatement("SELECT ID_ARTICULO FROM DETALLE_FACTURA WHERE ID_FACTURA=? ORDER BY ID_ARTICULO")){
                            s.setLong(1,id);
                            try(ResultSet r=s.executeQuery()){
                                while(r.next())ids.add(r.getInt(1));
                            }
                        }for(int art:ids){
                            try(PreparedStatement s=c.prepareStatement("SELECT STOCK_ACTUAL FROM ARTICULO WHERE ID_ARTICULO=? FOR UPDATE")){
                                s.setInt(1,art);
                                s.executeQuery().close();
                            }
                        }long movimiento=Database.next(c,"SEQ_MOVIMIENTO");
                        int tipo=tipoMovimiento(c,1);
                        try(PreparedStatement s=c.prepareStatement("INSERT INTO MOVIMIENTO_INVENTARIO(ID_MOVIMIENTO,ID_TIPO_MOV,FECHA,OBSERVACION) VALUES(?,?,SYSDATE,?)")){
                            s.setLong(1,movimiento);
                            s.setInt(2,tipo);
                            s.setString(3,"Reversión factura "+id);
                            s.executeUpdate();
                        }try(PreparedStatement s=c.prepareStatement("SELECT ID_ARTICULO,CANTIDAD,PRECIO_VENTA FROM DETALLE_FACTURA WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            try(ResultSet r=s.executeQuery()){
                                while(r.next()){
                                    int art=r.getInt(1);
                                    BigDecimal cant=r.getBigDecimal(2);
                                    try(PreparedStatement u=c.prepareStatement("UPDATE ARTICULO SET STOCK_ACTUAL=STOCK_ACTUAL+? WHERE ID_ARTICULO=?")){
                                        u.setBigDecimal(1,cant);
                                        u.setInt(2,art);
                                        u.executeUpdate();
                                    }try(PreparedStatement d=c.prepareStatement("INSERT INTO DETALLE_MOVIMIENTO(ID_DETALLE_MOV,ID_MOVIMIENTO,ID_ARTICULO,CANTIDAD,PRECIO) VALUES(?,?,?,?,?)")){
                                        d.setLong(1,Database.next(c,"SEQ_DETALLE_MOV"));
                                        d.setLong(2,movimiento);
                                        d.setInt(3,art);
                                        d.setBigDecimal(4,cant);
                                        d.setBigDecimal(5,r.getBigDecimal(3));
                                        d.executeUpdate();
                                    }
                                }
                            }
                        }try(PreparedStatement s=c.prepareStatement("UPDATE FACTURA_MOVIMIENTO SET ID_MOVIMIENTO_REVERSA=? WHERE ID_FACTURA=? AND ID_MOVIMIENTO_REVERSA IS NULL")){
                            s.setLong(1,movimiento);
                            s.setLong(2,id);
                            if(s.executeUpdate()!=1)throw new AppException("CONFLICT","No existe movimiento original");
                        }try(PreparedStatement s=c.prepareStatement("UPDATE FACTURA SET ESTADO='ANULADA' WHERE ID_FACTURA=?")){
                            s.setLong(1,id);
                            s.executeUpdate();
                        }result=Map.of("idFactura",id,"estado","ANULADA");
                    }
                    default->throw new AppException("UNKNOWN_ACTION","Operación no implementada");
                }c.commit();
                return result;
            }catch(Exception e){
                c.rollback();
                if(e instanceof SQLException se)throw se;
                if(e instanceof RuntimeException re)throw re;
                throw new IllegalStateException(e);
            }
        }
    }
    private int tipoMovimiento(Connection c,int signo)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("SELECT ID_TIPO_MOV FROM TIPO_MOVIMIENTO WHERE SIGNO=? ORDER BY ID_TIPO_MOV FETCH FIRST 1 ROWS ONLY")){
            s.setInt(1,signo);
            try(ResultSet r=s.executeQuery()){
                if(!r.next())throw new AppException("VALIDATION_ERROR","Configure un tipo de movimiento con SIGNO "+signo);
                return r.getInt(1);
            }
        }
    }
    public Object ejecutar(String op,JsonObject p)throws SQLException{
        if(Set.of("crear","actualizar","eliminar","confirmar","anular").contains(op))return transaccion(op,p);
        try(Connection c=Database.abrir()){
            if(op.equals("obtener"))return obtener(c,entero(p,"idFactura"));
            if(op.equals("listar")){
                JsonArray out=new JsonArray();
                String sql="SELECT * FROM FACTURA WHERE (? IS NULL OR TRUNC(FECHA_EMISION)>=?) AND (? IS NULL OR TRUNC(FECHA_EMISION)<=?) ORDER BY ID_FACTURA DESC";
                try(PreparedStatement s=c.prepareStatement(sql)){
                    java.sql.Date desde=p.has("desde")?java.sql.Date.valueOf(p.get("desde").getAsString()):null;
                    java.sql.Date hasta=p.has("hasta")?java.sql.Date.valueOf(p.get("hasta").getAsString()):null;
                    s.setDate(1,desde);
                    s.setDate(2,desde);
                    s.setDate(3,hasta);
                    s.setDate(4,hasta);
                    try(ResultSet r=s.executeQuery()){
                        while(r.next())out.add(cabecera(r));
                    }
                }JsonObject x=new JsonObject();
                x.add("items",out);
                return x;
            }
            if(op.equals("saldo")){
                try(PreparedStatement s=c.prepareStatement("SELECT TOTAL,TOTAL_PAGADO,SALDO_PENDIENTE FROM V_SALDO_FACTURA WHERE ID_FACTURA=?")){
                    s.setInt(1,entero(p,"idFactura"));
                    try(ResultSet r=s.executeQuery()){
                        if(!r.next())throw new AppException("NOT_FOUND","Factura no encontrada");
                        return Map.of("total",r.getBigDecimal(1),"pagado",r.getBigDecimal(2),"saldoPendiente",r.getBigDecimal(3));
                    }
                }
            }
            throw new AppException("UNKNOWN_ACTION","Acción no implementada");
        }
    }
}
