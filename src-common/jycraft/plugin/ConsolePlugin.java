package jycraft.plugin;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.servers.PyWebSocketServer;
import jycraft.plugin.servers.SocketServer;
import jycraft.plugin.servers.StaticFilesServerListener;
import jycraft.plugin.servers.StaticFilesSever;
import wshttpserver.HttpServer;

public class ConsolePlugin {
	private ConsolePlugin() {}
		
	public static void start(JyCraftPlugin mainPlugin, int telnetport, int websocketport, String serverpass) {
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
        // declare StaticFilesServer variable with x name for testing prupposes
		StaticFilesSever x;
        try{
            // HttpServer from wshttpserver.HttpServer throws IOException
            x = new StaticFilesSever(new HttpServer(// httpserver that will serve the files
                                                    HttpServer.openServerChannel(
                                                            // using the websocketport as a reference
                                                        new InetSocketAddress("0.0.0.0", websocketport+1)),
                                     // hardcoded directory, will change for a variable containing
                                     // the wished directory or current working directory
                                     new File("C:\\Users\\scyth\\workspace\\WSHttpServerExample\\htdocs"),
                                     // class that implements HttpWebSocketServerListener
                                     // required by httpserver constructor
                                     new StaticFilesServerListener()));

            // inside startServer() there's a thread.sleep(1); throws InterruptedException
            x.startServer();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
