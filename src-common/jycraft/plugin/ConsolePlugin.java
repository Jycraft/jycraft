package jycraft.plugin;

import java.io.File;
import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.servers.SocketServer;
import jycraft.plugin.servers.PySFListener;
import jycraft.plugin.servers.StaticFilesSever;

public class ConsolePlugin {
    private ConsolePlugin() {}

    public static void start(JyCraftPlugin mainPlugin, int telnetport, int staticserveport, String serverpass, String staticserverootdir, String staticservedir) {
        if (telnetport > -1) {
            SocketServer server = new SocketServer(mainPlugin, telnetport, serverpass);
            Thread t = new Thread(server);
            t.start();
        }
        if (staticserveport > -1){ // if a port has been specified, it has to be greater than 0
            // new file server instance
            PySFListener WsServerListener = new PySFListener(mainPlugin, serverpass);
            StaticFilesSever filesServer = new StaticFilesSever(staticserveport,staticserverootdir, staticservedir, WsServerListener);
            filesServer.start();
        }
        loadPythonPlugins(mainPlugin, "./python-plugins");
    }

    private static void loadPythonPlugins(final JyCraftPlugin mainPlugin, String path) {
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
                    try {
                        mainPlugin.parse(interpreter, file, true);
                    } catch (Exception e) {
                        mainPlugin.log("[Python] Exception while loading plugins");
                        mainPlugin.log("[Python] " + e.toString());
                    }
                }
            };
            pyPlugin.start();
        }
    }

}
