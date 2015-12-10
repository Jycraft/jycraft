package jycraft.plugin.servers;

import com.google.gson.*;
import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.json.*;
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
 * Handles the Websocket behavior of the StaticFilesServer
 * It receives information from the coding environment on the same port the HttpServer is.
 * */

public class PySFListener implements HttpWebSocketServerListener {
    private JyCraftPlugin plugin;
    private String password;
    private Map<WebSocket, PyInterpreter> connections;
    private Map<WebSocket, String> buffers;
    private Map<WebSocket, Boolean> authorized;
    private JsonParser parser = new JsonParser();
    private Gson gson;


    public PySFListener(JyCraftPlugin caller, String password){
        this.plugin = caller;
        this.password = password;
        this.connections = new HashMap<WebSocket, PyInterpreter>();
        this.buffers = new HashMap<WebSocket, String>();
        this.authorized = new HashMap<WebSocket, Boolean>();
        this.gson = new Gson();
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
        // accept incoming connections
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
        MessageType messageType;
        JsonElement jsonmessage = null;
        Status status;
        LoginMessage loginMessage;
        LogoutMessage logoutMessage;
        try {
            jsonmessage = parser.parse(message);
        } catch(JsonParseException jpe){
            status = new Status(503, "Client's Json failed to parse");
            loginMessage = new LoginMessage("login", status);
            webSocket.send(this.gson.toJson(loginMessage));
            return;
        }

        messageType = MessageType.valueOf(jsonmessage.getAsJsonObject().get("type").getAsString());

        switch (messageType) {
            case login:
                if (!this.password.equals(jsonmessage.getAsJsonObject().get("password").getAsString())) {
                    status = new Status(500, "Login failed");
                    loginMessage = new LoginMessage("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                } else {
                    this.authorized.put(webSocket, true);
                    status = new Status(100, "Login successful");
                    loginMessage = new LoginMessage("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                }
                return;
            case execute:
                if (!auth) {
                    status = new Status(501, "Not authenticated");
                    loginMessage = new LoginMessage("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                    return;
                }
                final PyInterpreter interpreter = connections.get(webSocket);
                boolean more = false;
                String command = jsonmessage.getAsJsonObject().get("command").getAsString();
                ExecuteMessage exmessage;
                try{
                    if (command.contains("\n")){
                        more = getPlugin().parse(interpreter, command, true);
                    }
                    else {
                        buffers.put(webSocket, buffers.get(webSocket) + "\n" + command);
                        more = getPlugin().parse(interpreter, buffers.get(webSocket), false);
                    }
                }
                catch (Exception e){
                    plugin.log("[Python] " + e.toString());
                    status = new Status(3, "Python Exception");
                    exmessage = new ExecuteMessage("execute", new ExecuteException(e.getMessage(), e.getStackTrace().toString()), status);
                    webSocket.send(this.gson.toJson(exmessage));
                }
                if (!more){
                    buffers.put(webSocket, "");
                }
                if (more){
                    status = new Status(101, "More input expected");
                    exmessage = new ExecuteMessage("execute", status, "... ");
                    webSocket.send(this.gson.toJson(exmessage));
                }
                else {
                    status = new Status(102, "Expecting input");
                    exmessage = new ExecuteMessage("execute", status, ">>> ");
                    webSocket.send(this.gson.toJson(exmessage));
                }
                break;
            case logout:
                status = new Status(100, "Logout successful");
                logoutMessage = new LogoutMessage("login", status);
                webSocket.send(this.gson.toJson(logoutMessage));
                webSocket.close(CloseFrame.NORMAL);
                break;
            default:
                break;
        }

    }

    @Override
    public void wssMessage(WebSocket webSocket, ByteBuffer message) {
        boolean auth = this.authorized.get(webSocket);
        Status status;
        LoginMessage loginMessage;
        ExecuteMessage exmessage;
        if (!auth){
            status = new Status(501, "Not authenticated");
            loginMessage = new LoginMessage("login", status);
            webSocket.send(this.gson.toJson(loginMessage));
            return;
        }
        else
        {
            status = new Status(4, "ByteBuffers not implemented yet");
            exmessage = new ExecuteMessage("execute", status, "ByteBuffers not implemented");
            webSocket.send(this.gson.toJson(exmessage));
            plugin.log("ByteBuffer message not implemented");
            return;
        }
    }


    @Override
    public void wssError(WebSocket webSocket, Exception e) {
        close(webSocket);
    }

    private class SFLOutputStream extends OutputStream {
        private WebSocket ws;
        private String buffer;
        private Gson gson;

        public SFLOutputStream(WebSocket ws){
            this.ws = ws;
            this.buffer = "";
            this.gson = new Gson();
        }

        @Override
        public void write(int b) {
            int[] bytes = { b };
            write(bytes, 0, bytes.length);
        }
        public void write(int[] bytes, int offset, int length) {
            Status status;
            ExecuteMessage exmessage;
            String s = new String(bytes, offset, length);
            this.buffer += s;
            if (this.buffer.endsWith("\n")) {
                status = new Status(105, "Sending Result");
                exmessage = new ExecuteMessage("execute", status, this.buffer);
                ws.send(this.gson.toJson(exmessage));
                plugin.log("[Python] "+this.buffer.substring(0, this.buffer.length()-1));
                buffer = "";
            }
        }
    }

}
