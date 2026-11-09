package com.mycompany.app_distribuidas.websocket;
import com.google.gson.*;
import com.mycompany.app_distribuidas.protocolo.Mensajes;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import java.util.concurrent.*;
public class ClienteWebSocket implements WebSocket.Listener {
    private static final ClienteWebSocket INSTANCE=new ClienteWebSocket();
    private final ConcurrentHashMap<String,CompletableFuture<JsonObject>> pendientes=new ConcurrentHashMap<>();
    private volatile WebSocket socket;
    private final StringBuilder buffer=new StringBuilder();
    public static ClienteWebSocket get(){
        return INSTANCE;
    }
    public synchronized void conectar(){
        if(socket!=null)return;
        String url=System.getProperty("app.ws.url",System.getenv().getOrDefault("APP_WS_URL","ws://localhost:8081"));
        try{
            socket=HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).buildAsync(URI.create(url),this).get(7,TimeUnit.SECONDS);
        }catch(Exception e){
            throw new IllegalStateException("No se puede conectar al servidor WebSocket: "+e.getMessage(),e);
        }
    }
    public JsonElement solicitar(String action,Object payload){
        conectar();
        String id=UUID.randomUUID().toString();
        CompletableFuture<JsonObject> f=new CompletableFuture<>();
        pendientes.put(id,f);
        try{
            socket.sendText(Mensajes.solicitud(id,action,Mensajes.GSON.toJsonTree(payload)).toString(),true).join();
            JsonObject resp=f.get(15,TimeUnit.SECONDS);
            if(!resp.get("success").getAsBoolean())throw new IllegalStateException(resp.getAsJsonObject("error").get("message").getAsString());
            return resp.get("data");
        }catch(Exception e){
            throw new IllegalStateException("Error al solicitar "+action+": "+e.getMessage(),e);
        }finally{
            pendientes.remove(id);
        }
    }
    @Override public CompletionStage<?> onText(WebSocket ws,CharSequence data,boolean last){
        synchronized(buffer){
            buffer.append(data);
            if(last){
                try{
                    JsonObject x=JsonParser.parseString(buffer.toString()).getAsJsonObject();
                    String id=Mensajes.str(x,"requestId");
                    CompletableFuture<JsonObject> f=pendientes.get(id);
                    if(f!=null)f.complete(x);
                }catch(Exception ex){
                    System.err.println("Respuesta inválida: "+ex.getMessage());
                }finally{
                    buffer.setLength(0);
                }
            }
        }ws.request(1);
        return null;
    }
    @Override public void onOpen(WebSocket ws){
        ws.request(1);
    }
    @Override public CompletionStage<?> onClose(WebSocket ws,int code,String reason){
        socket=null;
        pendientes.values().forEach(f->f.completeExceptionally(new IllegalStateException("Desconectado")));
        return null;
    }
    @Override public void onError(WebSocket ws,Throwable error){
        socket=null;
        pendientes.values().forEach(f->f.completeExceptionally(error));
    }
}
