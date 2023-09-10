import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;


import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;


@WebSocket
public class MapWebSocketHandler {

    private Session session;
    private static final ConcurrentHashMap<Session, String> sessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected");
        this.session = session;
        sessions.put(session, "");
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        sessions.remove(this.session);
    }

    public static void broadcastData(String[][] Map,
                                     Queue<PackageTask> packageTaskQueue) {
        sessions.keySet().forEach(session -> {
            try {
                String jsonData = ToJason.toJson(Map, packageTaskQueue);
                session.getRemote().sendString(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}