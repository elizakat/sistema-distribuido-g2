package com.mycompany.app_distribuidas.persistencia;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mycompany.app_distribuidas.modelo.CuentaBancaria;
import com.mycompany.app_distribuidas.modelo.CabeceraTransaccion;
import com.mycompany.app_distribuidas.modelo.DetalleTransaccion;
import com.mycompany.app_distribuidas.modelo.TipoTransaccion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BancosDAO {

    // Guarda una nueva cuenta bancaria en Oracle otorgando un numero de cuenta real de 10 digitos
    public static void insertarCuenta(CuentaBancaria cuenta) throws SQLException {
        String sql = "INSERT INTO CUENTA_BANCARIA (ID_CUENTA, NUMERO_CUENTA, BANCO, DESCRIPCION) "
                   + "VALUES ((SELECT GREATEST(NVL(MAX(ID_CUENTA), 0) + 1, 2100100001) FROM CUENTA_BANCARIA), "
                   + "(SELECT TO_CHAR(GREATEST(NVL(MAX(ID_CUENTA), 0) + 1, 2100100001)) FROM CUENTA_BANCARIA), ?, ?)";
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cuenta.getCliente());
            ps.setString(2, cuenta.getDescripcion());
            ps.executeUpdate();
        }
    }

    // Devuelve la lista de cuentas bancarias
    public static List<CuentaBancaria> listarCuentas() throws SQLException {
        List<CuentaBancaria> lista = new ArrayList<>();
        try (Connection c = Database.abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT ID_CUENTA, BANCO, DESCRIPCION FROM CUENTA_BANCARIA ORDER BY ID_CUENTA")) {
            while (rs.next()) {
                lista.add(new CuentaBancaria(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return lista;
    }

    // Devuelve los nombres de clientes para llenar el ComboBox de cuentas bancarias
    public static List<String> listarClientes() throws SQLException {
        List<String> lista = new ArrayList<>();
        try (Connection c = Database.abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT NOMBRE FROM CLIENTE ORDER BY NOMBRE")) {
            while (rs.next()) lista.add(rs.getString(1));
        }
        if (lista.isEmpty()) {
            lista.add("Juan Perez");
            lista.add("Maria Gomez");
            lista.add("Empresa Comercial S.A.");
        }
        return lista;
    }

    // Modifica los datos de una cuenta bancaria
    public static void actualizarCuenta(CuentaBancaria cuenta) throws SQLException {
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE CUENTA_BANCARIA SET BANCO=?, DESCRIPCION=? WHERE ID_CUENTA=?")) {
            ps.setString(1, cuenta.getCliente());
            ps.setString(2, cuenta.getDescripcion());
            ps.setInt(3, cuenta.getNumeroCuenta());
            ps.executeUpdate();
        }
    }

    // Elimina una cuenta bancaria por su ID
    public static void eliminarCuenta(int idCuenta) throws SQLException {
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM CUENTA_BANCARIA WHERE ID_CUENTA=?")) {
            ps.setInt(1, idCuenta);
            ps.executeUpdate();
        }
    }

    // Guarda un nuevo tipo de transaccion con codigo automatico
    public static void insertarTipoTransaccion(TipoTransaccion tipo) throws SQLException {
        String sql = "INSERT INTO TIPO_TRANSACCION (ID_TIPO_TRANS, NOMBRE_TRANS, NATURALEZA) "
                   + "VALUES ((SELECT NVL(MAX(TO_NUMBER(ID_TIPO_TRANS)), 0) + 1 FROM TIPO_TRANSACCION), ?, ?)";
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tipo.getNombre());
            ps.setString(2, "+".equals(tipo.getSigno()) ? "C" : "D");
            ps.executeUpdate();
        }
    }

    // Devuelve la lista de tipos de transaccion
    public static List<TipoTransaccion> listarTiposTransaccion() throws SQLException {
        List<TipoTransaccion> lista = new ArrayList<>();
        try (Connection c = Database.abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT ID_TIPO_TRANS, NOMBRE_TRANS, NATURALEZA FROM TIPO_TRANSACCION ORDER BY TO_NUMBER(ID_TIPO_TRANS)")) {
            while (rs.next()) {
                lista.add(new TipoTransaccion(rs.getInt(1), rs.getString(2), "C".equals(rs.getString(3)) ? "+" : "-"));
            }
        }
        return lista;
    }

    // Modifica un tipo de transaccion existente
    public static void actualizarTipoTransaccion(TipoTransaccion tipo) throws SQLException {
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE TIPO_TRANSACCION SET NOMBRE_TRANS=?, NATURALEZA=? WHERE ID_TIPO_TRANS=?")) {
            ps.setString(1, tipo.getNombre());
            ps.setString(2, "+".equals(tipo.getSigno()) ? "C" : "D");
            ps.setString(3, String.valueOf(tipo.getIdTipoTransaccion()));
            ps.executeUpdate();
        }
    }

    // Elimina un tipo de transaccion por su codigo
    public static void eliminarTipoTransaccion(int idTipo) throws SQLException {
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM TIPO_TRANSACCION WHERE ID_TIPO_TRANS=?")) {
            ps.setString(1, String.valueOf(idTipo));
            ps.executeUpdate();
        }
    }

    // Guarda una transaccion completa con su cabecera y lista de detalles
    public static void insertarTransaccionCompleta(CabeceraTransaccion cabecera, List<DetalleTransaccion> detalles) throws SQLException {
        try (Connection c = Database.abrir()) {
            c.setAutoCommit(false);
            try {
                long idCab = Database.next(c, "SEQ_TRANSACCION");
                try (PreparedStatement psCab = c.prepareStatement("INSERT INTO TRANSACCION_BANCARIA (ID_TRANSACCION, FECHA_REGISTRO, ID_CUENTA, REFERENCIA) VALUES (?,?,?,?)")) {
                    psCab.setLong(1, idCab);
                    psCab.setDate(2, new java.sql.Date(cabecera.getFecha().getTime()));
                    psCab.setInt(3, cabecera.getNumeroCuenta());
                    psCab.setString(4, cabecera.getDescripcion());
                    psCab.executeUpdate();
                }
                guardarDetalles(c, idCab, detalles);
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    // Modifica una transaccion existente y reemplaza sus detalles
    public static void actualizarTransaccionCompleta(CabeceraTransaccion cabecera, List<DetalleTransaccion> detalles) throws SQLException {
        try (Connection c = Database.abrir()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement psCab = c.prepareStatement("UPDATE TRANSACCION_BANCARIA SET FECHA_REGISTRO=?, ID_CUENTA=?, REFERENCIA=? WHERE ID_TRANSACCION=?");
                     PreparedStatement psDel = c.prepareStatement("DELETE FROM DETALLE_TRANSACCION WHERE ID_TRANSACCION=?")) {
                    psCab.setDate(1, new java.sql.Date(cabecera.getFecha().getTime()));
                    psCab.setInt(2, cabecera.getNumeroCuenta());
                    psCab.setString(3, cabecera.getDescripcion());
                    psCab.setInt(4, cabecera.getIdTransaccion());
                    psCab.executeUpdate();
                    psDel.setInt(1, cabecera.getIdTransaccion());
                    psDel.executeUpdate();
                }
                guardarDetalles(c, cabecera.getIdTransaccion(), detalles);
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    // Elimina los detalles y la cabecera de una transaccion
    public static void eliminarTransaccionCompleta(int idTrans) throws SQLException {
        try (Connection c = Database.abrir()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps1 = c.prepareStatement("DELETE FROM DETALLE_TRANSACCION WHERE ID_TRANSACCION=?");
                 PreparedStatement ps2 = c.prepareStatement("DELETE FROM TRANSACCION_BANCARIA WHERE ID_TRANSACCION=?")) {
                ps1.setInt(1, idTrans); ps1.executeUpdate();
                ps2.setInt(1, idTrans); ps2.executeUpdate();
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    // Inserta las lineas del detalle en lote
    private static void guardarDetalles(Connection c, long idCabecera, List<DetalleTransaccion> detalles) throws SQLException {
        long idDet = 0;
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT NVL(MAX(ID_DETALLE_TRANS), 0) FROM DETALLE_TRANSACCION")) {
            if (rs.next()) idDet = rs.getLong(1);
        }
        try (PreparedStatement psDet = c.prepareStatement("INSERT INTO DETALLE_TRANSACCION (ID_DETALLE_TRANS, ID_TRANSACCION, ID_TIPO_TRANS, MONTO) VALUES (?,?,?,?)")) {
            for (DetalleTransaccion det : detalles) {
                psDet.setLong(1, ++idDet);
                psDet.setLong(2, idCabecera);
                psDet.setString(3, String.valueOf(det.getIdTipoTransaccion()));
                psDet.setDouble(4, det.getValor());
                psDet.addBatch();
            }
            psDet.executeBatch();
        }
    }

    // Lista todas las cabeceras de transaccion
    public static List<CabeceraTransaccion> listarCabeceras() throws SQLException {
        List<CabeceraTransaccion> lista = new ArrayList<>();
        try (Connection c = Database.abrir(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT ID_TRANSACCION, FECHA_REGISTRO, ID_CUENTA, REFERENCIA FROM TRANSACCION_BANCARIA ORDER BY ID_TRANSACCION DESC")) {
            while (rs.next()) lista.add(new CabeceraTransaccion(rs.getInt(1), rs.getDate(2), rs.getInt(3), rs.getString(4)));
        }
        return lista;
    }

    // Lista los detalles filtrados por la cabecera seleccionada
    public static List<DetalleTransaccion> listarDetallesPorCabecera(int idTrans) throws SQLException {
        List<DetalleTransaccion> lista = new ArrayList<>();
        String sql = "SELECT d.ID_DETALLE_TRANS, d.ID_TRANSACCION, t.FECHA_REGISTRO, d.ID_TIPO_TRANS, d.MONTO FROM DETALLE_TRANSACCION d JOIN TRANSACCION_BANCARIA t ON t.ID_TRANSACCION=d.ID_TRANSACCION WHERE d.ID_TRANSACCION=?";
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idTrans);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new DetalleTransaccion(rs.getInt(1), rs.getInt(2), rs.getDate(3), rs.getInt(4), rs.getDouble(5)));
            }
        }
        return lista;
    }

    // Consulta para el Reporte 1 de saldos en dolares por cuenta bancaria
    public static JsonArray reporteSaldos(String desde, String hasta) throws SQLException {
        JsonArray items = new JsonArray();
        String sql = "SELECT cb.ID_CUENTA, cb.BANCO, cb.DESCRIPCION, NVL(SUM(CASE WHEN tt.NATURALEZA='C' THEN d.MONTO ELSE -d.MONTO END), 0) "
                   + "FROM CUENTA_BANCARIA cb LEFT JOIN TRANSACCION_BANCARIA t ON t.ID_CUENTA=cb.ID_CUENTA AND t.FECHA_REGISTRO BETWEEN ? AND ? "
                   + "LEFT JOIN DETALLE_TRANSACCION d ON d.ID_TRANSACCION=t.ID_TRANSACCION LEFT JOIN TIPO_TRANSACCION tt ON tt.ID_TIPO_TRANS=d.ID_TIPO_TRANS "
                   + "GROUP BY cb.ID_CUENTA, cb.BANCO, cb.DESCRIPCION ORDER BY cb.ID_CUENTA";
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde)); ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject x = new JsonObject();
                    x.addProperty("numeroCuenta", rs.getInt(1)); x.addProperty("cliente", rs.getString(2));
                    x.addProperty("descripcion", rs.getString(3)); x.addProperty("saldo", rs.getDouble(4));
                    items.add(x);
                }
            }
        }
        return items;
    }

    // Consulta para el Reporte 2 cruzado de transacciones por cuenta bancaria
    public static JsonArray reporteCruzado(String desde, String hasta) throws SQLException {
        JsonArray items = new JsonArray();
        String sql = "SELECT cb.ID_CUENTA || ' - ' || cb.BANCO, tt.NOMBRE_TRANS, SUM(d.MONTO) FROM DETALLE_TRANSACCION d "
                   + "JOIN TRANSACCION_BANCARIA t ON t.ID_TRANSACCION=d.ID_TRANSACCION JOIN CUENTA_BANCARIA cb ON cb.ID_CUENTA=t.ID_CUENTA "
                   + "JOIN TIPO_TRANSACCION tt ON tt.ID_TIPO_TRANS=d.ID_TIPO_TRANS WHERE t.FECHA_REGISTRO BETWEEN ? AND ? GROUP BY cb.ID_CUENTA, cb.BANCO, tt.NOMBRE_TRANS";
        try (Connection c = Database.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde)); ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject x = new JsonObject();
                    x.addProperty("cuenta", rs.getString(1)); x.addProperty("transaccion", rs.getString(2)); x.addProperty("total", rs.getDouble(3));
                    items.add(x);
                }
            }
        }
        return items;
    }
}
