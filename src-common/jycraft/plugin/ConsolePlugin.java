package jycraft.plugin;

import java.io.File;

import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.servers.PyWebSocketServer;
import jycraft.plugin.servers.SocketServer;

public class ConsolePlugin {
	private ConsolePlugin() {}
		
	public static void start(Object mainPlugin, int telnetport, int websocketport, String serverpass) {
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
