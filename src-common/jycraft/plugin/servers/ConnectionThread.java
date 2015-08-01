package jycraft.plugin.servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import jycraft.plugin.interpreter.PyInterpreter;
import jycraft.plugin.utils.PluginUtils;

import org.python.util.InteractiveInterpreter;

public class ConnectionThread implements Runnable {
	protected Socket socket;
	protected SocketServer server;
	protected InteractiveInterpreter interpreter;
	protected String line;
	protected String buffer;
	protected PrintStream out;
	protected BufferedReader in;
	
	public ConnectionThread(Socket socket, SocketServer socketServer) {
		this.socket = socket;
		this.server = socketServer;
		this.buffer = "";
		this.interpreter = new PyInterpreter();
		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			this.out = new PrintStream(this.socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.interpreter.setOut(this.out);
		this.interpreter.setErr(this.out);
		PluginUtils.log(this.server.getPlugin(), "New telnet connection");
	}

	public void run() {
		try {
			if (server.getPassword() != null && !"".equals(server.getPassword())) {
				out.print("PASSWORD: ");
				line = in.readLine();
				if (!server.getPassword().equals(line)) {
					out.println("Incorrect password!");
					socket.close();
					return;
				}
			}
			
			out.println("Welcome! Don't forget to type 'exit!' when you want to logout");
			
			out.print(">>> ");
			while ((line = in.readLine()) != null && !line.equals("exit!")) {
				boolean more = false;
				try {
					if (line.contains("\n")) {
						// As we are using readLine() above, this branch
						// will never occur. The telnet interface is thus
						// a pure REPL
						more = PluginUtils.parse(server.getPlugin(), interpreter, line, true);
						interpreter.exec(line);
					} else {
						buffer += "\n"+line;
						more = PluginUtils.parse(server.getPlugin(), interpreter, buffer, false);
					}
				} catch (Exception e) {
					out.print(e.toString()+"\n");
				}
				if (!more) buffer = "";
				if (more) out.print("... ");
				else out.print(">>> ");
			}			
			socket.close();
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		} finally {
			try {
				this.out.close();
				this.in.close();
			} catch (IOException e) {}
			interpreter.close();
		}
	}

}
