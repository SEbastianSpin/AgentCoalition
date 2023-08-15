package agents;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static agents.ToJason.toJson;


@WebSocket
public class MapWebSocketHandler {


    private Session session;
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();


    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        sessions.put(session, "");
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        sessions.remove(this.session);
    }

    public static void broadcastMap(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> map) {
        sessions.keySet().forEach(session -> {
            try {
                // Convert your map to JSON string here. This assumes you have a toJson() method
                String jsonMap = toJson(map);
                session.getRemote().sendString(jsonMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
