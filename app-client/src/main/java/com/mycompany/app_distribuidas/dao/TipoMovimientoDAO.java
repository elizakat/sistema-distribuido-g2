package com.mycompany.app_distribuidas.dao;

import com.google.gson.*;
import com.mycompany.app_distribuidas.modelo.TipoMovimiento;
import com.mycompany.app_distribuidas.protocolo.Mensajes;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import java.util.*;

public class TipoMovimientoDAO {

    public boolean guardar(
            TipoMovimiento tipo) {

        return ClienteWebSocket.get()
                .solicitar(
                        "inventario.tipoMovimiento.crear",
                        tipo
                ) != null;
    }

    public List<TipoMovimiento> listar() {

        JsonArray arr =
                ClienteWebSocket.get()
                .solicitar(
                        "inventario.tipoMovimiento.listar",
                        Map.of()
                )
                .getAsJsonObject()
                .getAsJsonArray(
                        "items"
                );

        List<TipoMovimiento> lista =
                new ArrayList<>();

        arr.forEach(
                x -> lista.add(
                        Mensajes.GSON.fromJson(
                                x,
                                TipoMovimiento.class
                        )
                )
        );

        return lista;
    }
    public boolean modificar(
        TipoMovimiento tipo) {

    return ClienteWebSocket.get()
            .solicitar(
                    "inventario.tipoMovimiento.actualizar",
                    tipo
            ) != null;
}
    public boolean eliminar(
        int id) {

    return ClienteWebSocket.get()
            .solicitar(
                    "inventario.tipoMovimiento.eliminar",
                    Map.of(
                            "idTipoMovimiento",
                            id
                    )
            ) != null;
}
    public TipoMovimiento buscar(
        int id) {

    JsonObject obj =
            ClienteWebSocket.get()
            .solicitar(
                    "inventario.tipoMovimiento.obtener",
                    Map.of(
                            "idTipoMovimiento",
                            id
                    )
            )
            .getAsJsonObject();

    return Mensajes.GSON.fromJson(
            obj,
            TipoMovimiento.class
    );
}
}