package com.mycompany.app_distribuidas.negocio;

import com.mycompany.app_distribuidas.modelo.CuentaBancaria;
import com.mycompany.app_distribuidas.modelo.CabeceraTransaccion;
import com.mycompany.app_distribuidas.modelo.DetalleTransaccion;
import com.mycompany.app_distribuidas.modelo.TipoTransaccion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mycompany.app_distribuidas.persistencia.BancosDAO;
import java.sql.SQLException;
import java.util.List;

public class BancosService {

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private static final Gson gsonEntrada = new Gson();

    // Recibe el JSON del Router, lo convierte a objetos Java y llama al metodo correcto
    public Object ejecutar(String recurso, String operacion, JsonObject payload) throws AppException {

        try {
            // Enrutamiento para cuentas bancarias
            if (recurso.equals("cuenta")) {
                if (operacion.equals("insertar")) {
                    CuentaBancaria cuenta = gson.fromJson(payload, CuentaBancaria.class);
                    registrarCuentaBancaria(cuenta);
                    return "Cuenta registrada";
                } else if (operacion.equals("listar")) {
                    JsonObject respuesta = new JsonObject();
                    respuesta.add("items", gson.toJsonTree(BancosDAO.listarCuentas()));
                    return respuesta;
                } else if (operacion.equals("clientes")) {
                    JsonObject respuesta = new JsonObject();
                    respuesta.add("items", gson.toJsonTree(BancosDAO.listarClientes()));
                    return respuesta;
                } else if (operacion.equals("actualizar")) {
                    CuentaBancaria cuenta = gson.fromJson(payload, CuentaBancaria.class);
                    BancosDAO.actualizarCuenta(cuenta);
                    return "Cuenta actualizada";
                } else if (operacion.equals("eliminar")) {
                    BancosDAO.eliminarCuenta(payload.get("numeroCuenta").getAsInt());
                    return "Cuenta eliminada";
                }
            }

            // Enrutamiento para tipos de transaccion
            else if (recurso.equals("tipotransaccion")) {
                if (operacion.equals("insertar")) {
                    TipoTransaccion tipo = gson.fromJson(payload, TipoTransaccion.class);
                    BancosDAO.insertarTipoTransaccion(tipo);
                    return "Tipo de transacción registrado";
                } else if (operacion.equals("listar")) {
                    JsonObject respuesta = new JsonObject();
                    respuesta.add("items", gson.toJsonTree(BancosDAO.listarTiposTransaccion()));
                    return respuesta;
                } else if (operacion.equals("actualizar")) {
                    TipoTransaccion tipo = gson.fromJson(payload, TipoTransaccion.class);
                    BancosDAO.actualizarTipoTransaccion(tipo);
                    return "Tipo de transacción actualizado";
                } else if (operacion.equals("eliminar")) {
                    BancosDAO.eliminarTipoTransaccion(payload.get("idTipoTransaccion").getAsInt());
                    return "Tipo de transacción eliminado";
                }
            }

            // Enrutamiento para transacciones complejas de cabecera y detalle
            else if (recurso.equals("transaccion")) {
                if (operacion.equals("insertar")) {
                    CabeceraTransaccion cabecera = gsonEntrada.fromJson(payload.get("cabecera"), CabeceraTransaccion.class);
                    List<DetalleTransaccion> detalles = gsonEntrada.fromJson(payload.get("detalles"), new TypeToken<List<DetalleTransaccion>>(){}.getType());
                    procesarTransaccion(cabecera, detalles);
                    return "Transacción procesada correctamente";
                } else if (operacion.equals("listar")) {
                    JsonObject respuesta = new JsonObject();
                    respuesta.add("items", gson.toJsonTree(BancosDAO.listarCabeceras()));
                    return respuesta;
                } else if (operacion.equals("detalles")) {
                    int idTrans = payload.get("idTransaccion").getAsInt();
                    JsonObject respuesta = new JsonObject();
                    respuesta.add("items", gson.toJsonTree(BancosDAO.listarDetallesPorCabecera(idTrans)));
                    return respuesta;
                } else if (operacion.equals("actualizar")) {
                    CabeceraTransaccion cabecera = gsonEntrada.fromJson(payload.get("cabecera"), CabeceraTransaccion.class);
                    List<DetalleTransaccion> detalles = gsonEntrada.fromJson(payload.get("detalles"), new TypeToken<List<DetalleTransaccion>>(){}.getType());
                    BancosDAO.actualizarTransaccionCompleta(cabecera, detalles);
                    return "Transacción actualizada correctamente";
                } else if (operacion.equals("eliminar")) {
                    BancosDAO.eliminarTransaccionCompleta(payload.get("idTransaccion").getAsInt());
                    return "Transacción eliminada correctamente";
                }
            }

            // Enrutamiento para reportes de bancos
            else if (recurso.equals("reporte")) {
                String desde = payload.get("desde").getAsString();
                String hasta = payload.get("hasta").getAsString();
                JsonObject respuesta = new JsonObject();
                if (operacion.equals("saldos")) {
                    respuesta.add("items", BancosDAO.reporteSaldos(desde, hasta));
                    return respuesta;
                } else if (operacion.equals("cruzado")) {
                    respuesta.add("items", BancosDAO.reporteCruzado(desde, hasta));
                    return respuesta;
                }
            }

            throw new AppException("UNKNOWN_OPERATION", "Operación no soportada en bancos: " + operacion);

        } catch (SQLException ex) {
            throw new AppException("DB_ERROR", "Error de base de datos: " + ex.getMessage());
        }
    }

    // Valida y registra una cuenta bancaria
    public static void registrarCuentaBancaria(CuentaBancaria cuenta) throws AppException {
        if (cuenta.getCliente() == null || cuenta.getCliente().trim().isEmpty()) {
            throw new AppException("INVALID_DATA", "El nombre del cliente es obligatorio.");
        }
        try {
            BancosDAO.insertarCuenta(cuenta);
        } catch (SQLException ex) {
            throw new AppException("DB_ERROR", "Error al guardar la cuenta bancaria: " + ex.getMessage());
        }
    }

    // Valida y procesa una transaccion completa
    public static void procesarTransaccion(CabeceraTransaccion cabecera, List<DetalleTransaccion> detalles) throws AppException {
        if (detalles == null || detalles.isEmpty()) {
            throw new AppException("INVALID_TX", "La transacción debe tener al menos un detalle.");
        }
        for (DetalleTransaccion det : detalles) {
            if (det.getValor() <= 0) {
                throw new AppException("INVALID_VAL", "Los valores deben ser mayores a cero.");
            }
        }
        try {
            BancosDAO.insertarTransaccionCompleta(cabecera, detalles);
        } catch (SQLException ex) {
            throw new AppException("DB_ERROR", "Error al procesar la transacción: " + ex.getMessage());
        }
    }
}
