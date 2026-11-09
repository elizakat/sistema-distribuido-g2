package com.mycompany.app_distribuidas.persistencia;
import java.sql.*;
public final class Database {
    public static Connection abrir() throws SQLException {
        String url=System.getenv().getOrDefault("ORACLE_URL","jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String user=System.getenv().getOrDefault("ORACLE_USER","APP_G2");
        String pass=System.getenv("ORACLE_PASSWORD");
        if(pass==null)throw new SQLException("Configure ORACLE_PASSWORD");
        return DriverManager.getConnection(url,user,pass);
    }
    public static long next(Connection c,String sequence)throws SQLException{
        try(PreparedStatement ps=c.prepareStatement("SELECT "+switch(sequence){
            case "SEQ_CLIENTE","SEQ_CIUDAD","SEQ_FACTURA","SEQ_DETALLE_FAC","SEQ_MOVIMIENTO","SEQ_DETALLE_MOV","SEQ_ARTICULO","SEQ_USUARIO"->sequence;
            default->throw new IllegalArgumentException("Secuencia inválida");
        }+".NEXTVAL FROM DUAL");
        ResultSet r=ps.executeQuery()){
            r.next();
            return r.getLong(1);
        }
    }
    private Database(){
    }
}
