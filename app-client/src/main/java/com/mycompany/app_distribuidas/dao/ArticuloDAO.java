package com.mycompany.app_distribuidas.dao;

import com.google.gson.*;import com.mycompany.app_distribuidas.modelo.Articulo;import com.mycompany.app_distribuidas.protocolo.Mensajes;import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;import java.util.*;
public class ArticuloDAO {
 public boolean guardarArticulo(Articulo a){return ClienteWebSocket.get().solicitar("inventario.articulo.crear",a)!=null;}
 public List<Articulo> listarArticulos(){JsonArray arr=ClienteWebSocket.get().solicitar("inventario.articulo.listar",Map.of()).getAsJsonObject().getAsJsonArray("items");List<Articulo> out=new ArrayList<>();arr.forEach(x->out.add(Mensajes.GSON.fromJson(x,Articulo.class)));return out;}
}