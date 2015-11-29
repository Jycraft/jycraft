package jycraft.plugin.servers;

import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.interpreter.PyInterpreter;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import wshttpserver.HttpWebSocketServerListener;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tuna on 11/25/2015.
 *
 * class required by HttpServer
 * this handles the socket behavior of the server !
 * I don't know how to implement the methods properly
 *
 */

// TODO: 11/25/2015 implement class properly in the StaticFilesServer instance classes
public class StaticFilesServerListener implements HttpWebSocketServerListener {
    private JyCraftPlugin plugin;
    private String password;
    private Map<WebSocket, PyInterpreter> connections;
    private Map<WebSocket, String> buffers;
    private Map<WebSocket, Boolean> authorized;


    public StaticFilesServerListener (JyCraftPlugin caller, String password){
        this.plugin = caller;
        this.password = password;
        this.connections = new HashMap<WebSocket, PyInterpreter>();
        this.buffers = new HashMap<WebSocket, String>();
        this.authorized = new HashMap<WebSocket, Boolean>();
    }

    public String getPassword(){
        return this.password;
    }

    public JyCraftPlugin getPlugin() {
        return this.plugin;
    }

    public void close(WebSocket webSocket){
        connections.get(webSocket).close();
        connections.remove(webSocket);
        buffers.remove(webSocket);
        authorized.remove(webSocket);
    }

    @Override
    public boolean wssConnect(SelectionKey selectionKey) {
        /**
         * TODO: 11/28/2015 Determine which is the role of this method.
         * Returns whether a new connection shall be accepted or not.<br> Therefore method is well suited to implement
         * some kind of connection limitation.<br>
         */
        return true;
    }

    @Override
    public void wssOpen(WebSocket webSocket, ClientHandshake chs) {
        plugin.log("New websocket connection");
        PyInterpreter interpreter = new PyInterpreter();
        OutputStream os = new SFLOutputStream(webSocket);
        interpreter.setOut(os);
        interpreter.setErr(os);
        connections.put(webSocket,interpreter);
        buffers.put(webSocket, "");
        authorized.put(webSocket, password == null || "".equals(password));
        webSocket.send("Login by sending 'login!<PASSWORD>'\n");
    }

    @Override
    public void wssClose(WebSocket webSocket, int i, String s, boolean b) {
        close(webSocket);
    }

    @Override
    public void wssMessage(WebSocket webSocket, String message) {
        boolean auth = authorized.get(webSocket);

        if (message.startsWith("login!")){
            String p = message.split("!")[1];
            if(!this.password.equals(p)){
                webSocket.send("Incorrect password!\n");
            }
            else {
                this.authorized.put(webSocket, true);
                webSocket.send("Welcome!\n");
                webSocket.send(">>> ");
            }
            return;
        }

        if (message.equals("exit!")){
            webSocket.close(CloseFrame.NORMAL);
            return;
        }
        if (!auth){
            webSocket.send("Not authorized, login first by sending 'login!<PASSWORD>'\n");
            return;
        }

        final PyInterpreter interpreter = connections.get(webSocket);
        boolean more = false;

        try{
            if (message.contains("\n")){
                more = getPlugin().parse(interpreter, message, true);
            }
            else {
                buffers.put(webSocket, buffers.get(webSocket) + "\n" + message);
                more = getPlugin().parse(interpreter, buffers.get(webSocket), false);
            }
        }
        catch (Exception e){
            plugin.log("[Python] " + e.toString());
            webSocket.send(e.toString() + "\n");
        }
        if (!more){
            buffers.put(webSocket, "");
        }
        if (more){
            webSocket.send("... ");
        }
        else {
            webSocket.send(">>> ");
        }

    }

    @Override
    public void wssMessage(WebSocket webSocket, ByteBuffer message) {
        boolean auth = this.authorized.get(webSocket);

        if (!auth){
            webSocket.send("Not authorized, login first by sending 'login!<PASSWORD>'\n");
            return;
        }
        else
        {
            webSocket.send("ByteBuffers not implemented yet");
            plugin.log("ByteBuffer message not implemented");
            return;
        }
    }


    @Override
    public void wssError(WebSocket webSocket, Exception e) {
        close(webSocket);
    }

    public class SFLOutputStream extends OutputStream {
        private WebSocket ws;
        String buffer;

        public SFLOutputStream(WebSocket ws){
            this.ws = ws;
            this.buffer = "";
        }

        @Override
        public void write(int b) {
            int[] bytes = { b };
            write(bytes, 0, bytes.length);
        }
        public void write(int[] bytes, int offset, int length) {
            String s = new String(bytes, offset, length);
            this.buffer += s;
            if (this.buffer.endsWith("\n")) {
                this.ws.send(this.buffer);
                plugin.log("[Python] "+this.buffer.substring(0, this.buffer.length()-1));
                buffer = "";
            }
        }
    }

}
