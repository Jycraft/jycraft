package jycraft.plugin.canary;

import jycraft.plugin.ConsolePlugin;
import jycraft.plugin.JyCraftPlugin;
import net.canarymod.plugin.Plugin;
import org.python.util.InteractiveInterpreter;

public class MainPlugin extends Plugin implements JyCraftPlugin{
	
	public MainPlugin() {
		super();
	}
	
    @Override
    public boolean enable() {
    	log("Loading Python Console");
    	int tcpsocketserverport = getConfig().getInt("pythonconsole.serverconsole.telnetport", 44444);
		int websocketserverport = getConfig().getInt("pythonconsole.serverconsole.websocketport", 44445);
		String serverpass = getConfig().getString("pythonconsole.serverconsole.password", "swordfish");
		ConsolePlugin.start(this, tcpsocketserverport, websocketserverport, serverpass);
    	return true;
    }

	@Override
	public void disable() {}

	@Override
	public void log(String message) {
		getLogman().info(message);
	}

	@Override
	public boolean parse(InteractiveInterpreter interpreter, String code, boolean exec) throws Exception {
		try {
			if (exec) {
				interpreter.exec(code);
				return false;
			}
			return interpreter.runsource(code);
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}
}


