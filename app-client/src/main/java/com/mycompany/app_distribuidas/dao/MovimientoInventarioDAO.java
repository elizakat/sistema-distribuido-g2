package com.mycompany.app_distribuidas.dao;

import com.google.gson.JsonObject;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;

public class MovimientoInventarioDAO {

    public boolean guardarMovimiento(
            JsonObject movimiento) {

        return ClienteWebSocket.get()
                .solicitar(
                        "inventario.movimiento.crear",
                        movimiento
                ) != null;
    }
}