package jycraft.plugin.servers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.json.*;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import wshttpserver.HttpWebSocketServerListener;

import java.io.File;
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
        this.gson = GsonUtils.getGson();
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
    }

    @Override
    public void wssClose(WebSocket webSocket, int i, String s, boolean b) {
        close(webSocket);
    }

    @Override
    public void wssMessage(WebSocket webSocket, String message) {
        boolean auth = authorized.get(webSocket);
        MessageType messageType;
        final TypeToken<Message> messageTypeToken = new TypeToken<Message>(){};
        final Message jsonMessage = gson.fromJson(message, messageTypeToken.getType());
        Status status;
        Message loginMessage;
        Message logoutMessage;

        messageType = MessageType.valueOf(jsonMessage.getType().toUpperCase());
        plugin.log(messageType.name());

        switch (messageType) {
            case LOGIN:
                if (!this.password.equals(jsonMessage.getPassword())) {
                    status = new Status(500, "Login failed");
                    loginMessage = new Message("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                } else {
                    this.authorized.put(webSocket, true);
                    status = new Status(100, "Login successful");
                    loginMessage = new Message("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                }
                return;
            case INTERACTIVE:
                if (!auth) {
                    status = new Status(501, "Not authenticated");
                    loginMessage = new Message("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                    return;
                }
                boolean more = false;
                final PyInterpreter interpreter = connections.get(webSocket);
                String command = jsonMessage.getCommand();
                Message exmessage;
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
                    exmessage = new Message("interactive", status);
                    webSocket.send(this.gson.toJson(exmessage));
                }
                if (!more){
                    buffers.put(webSocket, "");
                }
                if (more){
                    status = new Status(101, "More input expected");
                    exmessage = new Message("interactive", status);
                    exmessage.setResult("... ");
                    webSocket.send(this.gson.toJson(exmessage));
                }
                else {
                    status = new Status(102, "Expecting input");
                    exmessage = new Message("interactive", status);
                    exmessage.setResult(">>> ");
                    webSocket.send(this.gson.toJson(exmessage));
                }
                break;
            case FILE:
                if (!auth) {
                    status = new Status(501, "Not authenticated");
                    loginMessage = new Message("login", status);
                    webSocket.send(this.gson.toJson(loginMessage));
                    return;
                }
                final PyInterpreter fileInterpreter = connections.get(webSocket);
                String filePath = jsonMessage.getFilePath();
                Message fileExmessage;
                boolean success = true;
                final File script;

                try{
                   script = new File(System.getProperty("user.dir").concat("/" + filePath));
                    if (!script.exists() || !script.isDirectory())
                        return;
                   getPlugin().parse(fileInterpreter, script, true);
                }
                catch (Exception e){
                    success = false;
                    plugin.log("[Python] " + e.toString());
                    status = new Status(3, "Python Exception");
                    fileExmessage = new Message("file", status);
                    webSocket.send(this.gson.toJson(fileExmessage));

                }
                finally {
                    status = new Status(102, "File Executed");
                    fileExmessage = new Message("file", status);
                    fileExmessage.setResult("Execution: " + (success ? "successful": "unsuccessful"));
                    webSocket.send(this.gson.toJson(fileExmessage));
                }


                break;
            case LOGOUT:
                status = new Status(100, "Logout successful");
                logoutMessage = new Message("login", status);
                webSocket.send(this.gson.toJson(logoutMessage));
                webSocket.close(CloseFrame.NORMAL);
                break;
            default:
                status = new Status(500, "Unidentified action type");
                logoutMessage = new Message("undefined", status);
                webSocket.send(this.gson.toJson(logoutMessage));
                break;
        }

    }

    @Override
    public void wssMessage(WebSocket webSocket, ByteBuffer message) {
        boolean auth = this.authorized.get(webSocket);
        Status status;
        Message loginMessage;
        Message exmessage;
        if (!auth){
            status = new Status(501, "Not authenticated");
            loginMessage = new Message("login", status);
            webSocket.send(this.gson.toJson(loginMessage));
            return;
        }
        else
        {
            status = new Status(4, "ByteBuffers not implemented yet");
            exmessage = new Message("execute", status);
            exmessage.setResult("ByteBuffer message not implemented");
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
        private Status status;
        private Message OSMessage;
        public SFLOutputStream(WebSocket ws){
            this.ws = ws;
            this.buffer = "";
            this.gson = GsonUtils.getGson();
            status = new Status(100, "Sending result");
            OSMessage = new Message("result", status);
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
                plugin.log("[Python] "+this.buffer.substring(0, this.buffer.length()-1));
                OSMessage.setResult(this.buffer);
                ws.send(this.gson.toJson(OSMessage));
                // FIXME: Exception while registering a new class in type adapter types and labels must be unique
                buffer = "";
            }
        }
    }

}
