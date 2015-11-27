package jycraft.plugin.servers;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import wshttpserver.HttpWebSocketServerListener;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Created by Tuna on 11/25/2015.
 *
 * class required by HttpServer
 * this handles the socket behavior of the server !
 * I don't know how to implement the methods properly
 *
 */
// TODO: 11/25/2015 Identify which is the appropriate behavior for this class

// TODO: 11/25/2015 implement class properly in the StaticFilesServer instance classes
public class StaticFilesServerListener implements HttpWebSocketServerListener {


    public boolean wssConnect(SelectionKey selectionKey) {
        return false;
    }


    public void wssOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

    }


    public void wssClose(WebSocket webSocket, int i, String s, boolean b) {

    }


    public void wssMessage(WebSocket webSocket, String s) {

    }


    public void wssMessage(WebSocket webSocket, ByteBuffer byteBuffer) {

    }


    public void wssError(WebSocket webSocket, Exception e) {

    }
}
