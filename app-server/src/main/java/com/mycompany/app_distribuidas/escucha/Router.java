package com.mycompany.app_distribuidas.escucha;

import com.google.gson.JsonObject;
import com.mycompany.app_distribuidas.negocio.AppException;
import com.mycompany.app_distribuidas.negocio.CatalogosService;
import com.mycompany.app_distribuidas.negocio.FacturaService;
import com.mycompany.app_distribuidas.negocio.ReportesService;
import com.mycompany.app_distribuidas.negocio.UsuariosService;

/** Traduce las acciones SADP/1 a los servicios de negocio correspondientes. */
public class Router {

    private final CatalogosService catalogos = new CatalogosService();
    private final UsuariosService usuarios = new UsuariosService();
    private final FacturaService facturas = new FacturaService();
    private final ReportesService reportes = new ReportesService();

    public Object ejecutar(String action, JsonObject payload) throws Exception {
        String[] partes = action.split("\\.");
        if (partes.length != 3) {
            throw new AppException("UNKNOWN_ACTION", "Formato esperado modulo.recurso.operacion");
        }

        String recurso = partes[0] + "." + partes[1];
        String operacion = partes[2];

        return switch (recurso) {
            case "seguridad.usuario" -> usuarios.ejecutar(operacion, payload);
            case "inventario.articulo" -> catalogos.ejecutar("articulo", operacion, payload);
            case "facturacion.cliente" -> catalogos.ejecutar("cliente", operacion, payload);
            case "facturacion.ciudad" -> catalogos.ejecutar("ciudad", operacion, payload);
            case "facturacion.factura" -> facturas.ejecutar(operacion, payload);
            case "facturacion.reporte" -> reportes.ejecutar(operacion, payload);
            default -> throw new AppException("UNKNOWN_ACTION", "Acción desconocida: " + action);
        };
    }
}
