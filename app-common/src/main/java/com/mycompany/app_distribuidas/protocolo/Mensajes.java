package com.mycompany.app_distribuidas.protocolo;

import com.google.gson.*;
public final class Mensajes {
 public static final Gson GSON=new GsonBuilder().serializeNulls().create();
 public static JsonObject solicitud(String id,String accion,JsonElement datos){JsonObject x=new JsonObject();x.addProperty("version","1.0");x.addProperty("requestId",id);x.addProperty("action",accion);x.add("payload",datos==null?new JsonObject():datos);return x;}
 public static JsonObject ok(String id,Object datos){JsonObject x=new JsonObject();x.addProperty("version","1.0");x.addProperty("requestId",id);x.addProperty("success",true);x.add("data",GSON.toJsonTree(datos));x.add("error",JsonNull.INSTANCE);return x;}
 public static JsonObject error(String id,String codigo,String mensaje){JsonObject x=new JsonObject();x.addProperty("version","1.0");x.addProperty("requestId",id);x.addProperty("success",false);x.add("data",JsonNull.INSTANCE);JsonObject e=new JsonObject();e.addProperty("code",codigo);e.addProperty("message",mensaje);x.add("error",e);return x;}
 public static String str(JsonObject x,String k){return x.has(k)&&!x.get(k).isJsonNull()?x.get(k).getAsString():"";}
 public static int entero(JsonObject x,String k){return x.get(k).getAsInt();}
 private Mensajes(){}
}