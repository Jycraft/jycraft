package jycraft.plugin;

import java.io.File;
import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.servers.PyWebSocketServer;
import jycraft.plugin.servers.SocketServer;
import jycraft.plugin.servers.StaticFilesSever;

public class ConsolePlugin {
    private ConsolePlugin() {}

    public static void start(JyCraftPlugin mainPlugin, int telnetport, int websocketport, int staticserveport, String serverpass, String staticserverootdir, String staticservedir) {
        if (telnetport > -1) {
            SocketServer server = new SocketServer(mainPlugin, telnetport, serverpass);
            Thread t = new Thread(server);
            t.start();
        }
        if (websocketport > -1) {
            PyWebSocketServer webserver = new PyWebSocketServer(mainPlugin, websocketport, serverpass);
            webserver.start();
        }
        loadPythonPlugins("./python-plugins");
        // if a port has been specified, it has to be greater than 0
        if (staticserveport > -1){
            // new file server instance
            StaticFilesSever filesServer = new StaticFilesSever(staticserveport,staticserverootdir, staticservedir);
            filesServer.start();
        }
    }

    private static void loadPythonPlugins(String path) {
        File pluginsDir = new File(path);
        if (!pluginsDir.exists() || !pluginsDir.isDirectory())
            return;
        final File[] files = pluginsDir.listFiles();
        for (final File file : files) {
            if (!file.getName().endsWith(".py")) continue;
            Thread pyPlugin = new Thread() {
                @Override
                public void run() {
                    PyInterpreter interpreter = new PyInterpreter();
                    interpreter.execfile(file.getAbsolutePath());
                    interpreter.close();
                }
            };
            pyPlugin.start();
        }
    }

}
