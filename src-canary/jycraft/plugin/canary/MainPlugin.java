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
		int staticserveport = getConfig().getInt("pythonconsole.staticserve.staticserveport", 44446);
		String serverpass = getConfig().getString("pythonconsole.serverconsole.password", "swordfish");
		String staticserverootdir = getConfig().getString("pythonconsole.staticserve.rootdir", System.getProperty("user.dir").concat("/static"));
		String staticservedir = getConfig().getString("pythonconsole.staticserve.staticdir", System.getProperty("user.dir").concat("/static"));
		ConsolePlugin.start(this, tcpsocketserverport, staticserveport, serverpass, staticserverootdir, staticservedir);
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


