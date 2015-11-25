package jycraft.plugin.servers;

import wshttpserver.HttpServer;

import java.io.IOException;

/**
 * Created by Tuna on 11/25/2015.
 * this is What I feel to be a dummy class just calling a HttpServer instance
 * using just two basic methods
 * server.setup()
 * server.stop()
 * my guess is that this class should manage httpserver's behavior
 */
public class StaticFilesSever {
    HttpServer server;


    public StaticFilesSever(HttpServer server) {
        this.server = server;
    }

    // wrapper method for starting server not well implemented
    public void startServer() throws IOException, InterruptedException{

        server.setup();

        while(!Thread.interrupted()){
            // Accept new connections (non blocking)
            // You could run this in your main loop, or as a seperate thread
            server.loop();

            // ArrayBlockingQueue.poll(1, TimeUnit.MILLISECONDS); would also work,
            // for example when you have an event loop with worker threads.

            // Use a simple sleep in this example
            Thread.sleep(1);
        }
        server.stop();
    }
}
