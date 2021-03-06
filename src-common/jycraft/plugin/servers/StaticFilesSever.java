package jycraft.plugin.servers;

import wshttpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;


public class StaticFilesSever extends Thread {
    // class attributes
    private InetSocketAddress socketAddress;
    private ServerSocketChannel ssChannel;
    private File rootDir;
    private PySFListener webSocketListener;
    private HttpServer server;

    public StaticFilesSever(int websocketport, String rootdir, String staticdir, PySFListener serverlistener){
        super("HttpServer");
        // grab the socket address
        this.socketAddress = new InetSocketAddress("0.0.0.0", websocketport);
        // specify which is the root directory of the httpserver
        this.rootDir = new File(rootdir);
        this.webSocketListener = serverlistener;
        try {
            // open a channel to create an instance of the server
            this.ssChannel = HttpServer.openServerChannel(socketAddress);
            this.server = new HttpServer(this.ssChannel, this.rootDir, this.webSocketListener);
        } catch (IOException e) {
            System.out.printf("Error while initializing the server %s.\n", e.getMessage());
        }

        try {
            // add a static route on the httpserver
            this.server.addRouteStatic(staticdir, new File(staticdir));
        } catch (IOException e) {
            System.out.printf("Error while adding static route %s.\n", e.getMessage());
        }

    }

    public void setupServer(){
        // just calling the server.setup() method
        try {
            this.server.setup();
        } catch (IOException e) {
            System.out.printf("Error while setting up the server %s.\n", e.getMessage());
        }
    }

    public void loopServer() throws InterruptedException {
        // just calling the server.loop() method
        while (!Thread.interrupted())
        {
            // Accept new connections (non blocking)
            // You could run this in your main loop, or as a seperate thread
            this.server.loop();

            // ArrayBlockingQueue.poll(1, TimeUnit.MILLISECONDS); would also work,
            // for example when you have an event loop with worker threads.

            // Use a simple sleep in this example

            Thread.sleep(1);
        }
    }

    public void stopServer(){
        // just calling the server.stop() method
        this.server.stop();
    }

    @Override
    public void run() {
        // call HttpServer.setup() in a wrapper method
        setupServer();
        // loop to serve the files
        try {
            loopServer();
        }
        catch (InterruptedException e) {
            System.out.printf("Static files server thread interrupted %s.\n", e.getMessage());
        }
    }
}
