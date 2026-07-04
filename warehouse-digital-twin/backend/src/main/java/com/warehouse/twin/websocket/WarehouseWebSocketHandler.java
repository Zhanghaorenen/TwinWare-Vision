package com.warehouse.twin.websocket;
import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.stereotype.Component; import org.springframework.web.socket.*; import org.springframework.web.socket.handler.TextWebSocketHandler; import java.util.Set; import java.util.concurrent.CopyOnWriteArraySet;
@Component
public class WarehouseWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions=new CopyOnWriteArraySet<>();private final ObjectMapper json;
    public WarehouseWebSocketHandler(ObjectMapper json){this.json=json;}
    public void afterConnectionEstablished(WebSocketSession s){sessions.add(s);}
    public void afterConnectionClosed(WebSocketSession s,CloseStatus status){sessions.remove(s);}
    public void broadcast(Object value){try{TextMessage m=new TextMessage(json.writeValueAsString(value));for(var s:sessions)if(s.isOpen())try{s.sendMessage(m);}catch(Exception ignored){sessions.remove(s);}}catch(Exception ignored){}}
}
