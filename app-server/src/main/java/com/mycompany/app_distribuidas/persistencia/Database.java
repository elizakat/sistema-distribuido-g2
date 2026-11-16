package com.mycompany.app_distribuidas.persistencia;

import java.sql.*;

public final class Database {

    public static Connection abrir() throws SQLException {
        String url = System.getenv().getOrDefault("ORACLE_URL", "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String user = System.getenv().getOrDefault("ORACLE_USER", "APP_G2");
        String pass = System.getenv().getOrDefault("ORACLE_PASSWORD", "14070212");

        return DriverManager.getConnection(url, user, pass);
    }

    public static long next(Connection c, String sequence) throws SQLException {
        String sql = switch (sequence) {
            case "SEQ_CLIENTE", "SEQ_FACTURA", "SEQ_MOVIMIENTO", "SEQ_ARTICULO",
                 "SEQ_USUARIO", "SEQ_TIPO_TRANSACCION", "SEQ_TRANSACCION", "SEQ_COBRO" ->
                "SELECT " + sequence + ".NEXTVAL FROM DUAL";
            case "SEQ_CIUDAD" -> "SELECT NVL(MAX(ID_CIUDAD), 0) + 1 FROM CIUDAD_ENTREGA";
            case "SEQ_DETALLE_FAC" -> "SELECT NVL(MAX(ID_DETALLE_FAC), 0) + 1 FROM DETALLE_FACTURA";
            case "SEQ_DETALLE_MOV" -> "SELECT NVL(MAX(ID_DETALLE_MOV), 0) + 1 FROM DETALLE_MOVIMIENTO";
            case "SEQ_CUENTA" -> "SELECT GREATEST(NVL(MAX(ID_CUENTA), 0) + 1, 2100100001) FROM CUENTA_BANCARIA";
            case "SEQ_DETALLE_TRANS" -> "SELECT NVL(MAX(ID_DETALLE_TRANS), 0) + 1 FROM DETALLE_TRANSACCION";
            case "SEQ_DETALLE_COBRO" -> "SELECT NVL(MAX(ID_DETALLE_COBRO), 0) + 1 FROM DETALLE_COBRO";
            default -> throw new IllegalArgumentException("Secuencia inválida: " + sequence);
        };
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            r.next();
            return r.getLong(1);
        }
    }

    private Database() {
    }
}
