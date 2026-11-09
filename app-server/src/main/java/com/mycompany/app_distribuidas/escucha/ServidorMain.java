package com.mycompany.app_distribuidas.escucha;
import com.google.gson.*;
import com.mycompany.app_distribuidas.protocolo.Mensajes;
import com.mycompany.app_distribuidas.negocio.AppException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
public class ServidorMain extends WebSocketServer {
    private final java.util.Set<WebSocket> autenticados=java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Router router = new Router();
    private final ExecutorService workers=Executors.newFixedThreadPool(12);
    public ServidorMain(int puerto){
        super(new InetSocketAddress(puerto));
        setReuseAddr(true);
    }
    @Override public void onOpen(WebSocket socket,ClientHandshake req){
        System.out.println("Cliente conectado: "+socket.getRemoteSocketAddress());
    }
    @Override public void onClose(WebSocket socket,int code,String reason,boolean remote){
        autenticados.remove(socket);
        System.out.println("Cliente desconectado: "+code);
    }
    @Override public void onMessage(WebSocket socket,String msg){
        if(msg.length()>65536){
            socket.send(Mensajes.error("","INVALID_MESSAGE","Mensaje demasiado grande").toString());
            return;
        }workers.execute(()->{
            String id="";
            try{
                JsonObject req=JsonParser.parseString(msg).getAsJsonObject();
                id=Mensajes.str(req,"requestId");
                if(id.isBlank()||!req.has("action")||!req.has("payload")||!req.get("payload").isJsonObject())throw new AppException("INVALID_MESSAGE","Solicitud incompleta");
                if(!"1.0".equals(Mensajes.str(req,"version")))throw new AppException("UNSUPPORTED_VERSION","Se requiere protocolo 1.0");
                String action=Mensajes.str(req,"action");
                if(!action.equals("seguridad.usuario.login")&&!autenticados.contains(socket))throw new AppException("UNAUTHORIZED","Debe iniciar sesión");
                Object data=router.ejecutar(action,req.getAsJsonObject("payload"));
                if(action.equals("seguridad.usuario.login"))autenticados.add(socket);
                socket.send(Mensajes.ok(id,data).toString());
            }catch(AppException e){
                socket.send(Mensajes.error(id,e.getCode(),e.getMessage()).toString());
            }catch(Exception e){
                System.err.println("Error servidor: "+e);
                socket.send(Mensajes.error(id,"INTERNAL_ERROR","No se pudo procesar la solicitud").toString());
            }
        });
    }
    @Override public void onError(WebSocket socket,Exception ex){
        System.err.println("WebSocket: "+ex);
    }
    @Override public void onStart(){
        System.out.println("SADP/1 escuchando en ws://localhost:"+getPort());
    }
    public static void main(String[] args){
        int puerto=Integer.parseInt(System.getProperty("app.port",System.getenv().getOrDefault("APP_PORT","8081")));
        new ServidorMain(puerto).start();
    }
}
