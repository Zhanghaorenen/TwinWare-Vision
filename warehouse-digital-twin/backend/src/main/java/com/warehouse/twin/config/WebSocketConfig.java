package com.warehouse.twin.config;
import com.warehouse.twin.websocket.WarehouseWebSocketHandler; import org.springframework.context.annotation.Configuration; import org.springframework.web.socket.config.annotation.*;
@Configuration @EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WarehouseWebSocketHandler handler;public WebSocketConfig(WarehouseWebSocketHandler h){handler=h;}
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r){r.addHandler(handler,"/ws/warehouse").setAllowedOriginPatterns("*");}
}
