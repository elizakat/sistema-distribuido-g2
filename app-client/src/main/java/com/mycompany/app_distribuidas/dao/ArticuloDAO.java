package com.mycompany.app_distribuidas.dao;

import com.google.gson.*;import com.mycompany.app_distribuidas.modelo.Articulo;import com.mycompany.app_distribuidas.protocolo.Mensajes;import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;import java.util.*;
public class ArticuloDAO {
 public boolean guardarArticulo(Articulo a){return ClienteWebSocket.get().solicitar("inventario.articulo.crear",a)!=null;}
 public List<Articulo> listarArticulos(){JsonArray arr=ClienteWebSocket.get().solicitar("inventario.articulo.listar",Map.of()).getAsJsonObject().getAsJsonArray("items");List<Articulo> out=new ArrayList<>();arr.forEach(x->out.add(Mensajes.GSON.fromJson(x,Articulo.class)));return out;}
public boolean modificarArticulo(
        Articulo articulo) {

    return ClienteWebSocket.get()
            .solicitar(
                    "inventario.articulo.actualizar",
                    articulo
            ) != null;
}
public Articulo buscarArticulo(int id) {

    JsonObject obj =
            ClienteWebSocket.get()
                    .solicitar(
                            "inventario.articulo.obtener",
                            Map.of(
                                    "idArticulo",
                                    id
                            )
                    )
                    .getAsJsonObject();

    return Mensajes.GSON.fromJson(
            obj,
            Articulo.class
    );
}
public boolean eliminarArticulo(
        int idArticulo) {

    return ClienteWebSocket.get()
            .solicitar(
                    "inventario.articulo.eliminar",
                    Map.of(
                            "idArticulo",
                            idArticulo
                    )
            ) != null;
}
}