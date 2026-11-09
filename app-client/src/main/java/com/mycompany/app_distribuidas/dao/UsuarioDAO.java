package com.mycompany.app_distribuidas.dao;

import com.google.gson.*;
import com.mycompany.app_distribuidas.modelo.Usuario;
import com.mycompany.app_distribuidas.websocket.ClienteWebSocket;
import java.util.*;
public class UsuarioDAO {
 private JsonElement call(String op,Object d){return ClienteWebSocket.get().solicitar("seguridad.usuario."+op,d);}
 public boolean validarLogin(String u,String clave){return obtenerUsuario(u,clave)!=null;}
 public Usuario obtenerUsuario(String u,String clave){try{return MensajeUsuario(call("login",Map.of("username",u,"clave",clave)).getAsJsonObject());}catch(Exception e){return null;}}
 private Usuario MensajeUsuario(JsonObject x){if(x==null)return null;return com.mycompany.app_distribuidas.protocolo.Mensajes.GSON.fromJson(x,Usuario.class);}
 public boolean guardarUsuario(Usuario u){return call("crear",u)!=null;}
 public List<Usuario> listarUsuarios(){JsonArray arr=call("listar",Map.of()).getAsJsonObject().getAsJsonArray("items");List<Usuario> result=new ArrayList<>();arr.forEach(x->result.add(MensajeUsuario(x.getAsJsonObject())));return result;}
 public boolean modificarUsuario(Usuario u){return call("actualizar",u)!=null;}
 public boolean eliminarUsuario(int id){return call("eliminar",Map.of("idUsuario",id))!=null;}
 public Usuario buscarUsuario(int id){return MensajeUsuario(call("obtener",Map.of("idUsuario",id)).getAsJsonObject());}
 public boolean existeUsuario(String username){return listarUsuarios().stream().anyMatch(u->u.getUsername().equalsIgnoreCase(username));}
}