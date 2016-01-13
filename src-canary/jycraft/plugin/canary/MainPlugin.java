package jycraft.plugin.canary;

import jycraft.plugin.ConsolePlugin;
import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.interpreter.PyContext;
import net.canarymod.plugin.Plugin;
import org.python.util.InteractiveInterpreter;

import java.io.File;

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
			PyContext.setPlugin(this);
			if (exec) {
				interpreter.exec(code);
				return false;
			}
			return interpreter.runsource(code);
		} catch (Throwable e) {
			throw new Exception(e);
		} finally {
			PyContext.setPlugin(null);
		}
	}
	public boolean parse(InteractiveInterpreter interpreter, File script, boolean exec) throws Exception {
		try {
			PyContext.setPlugin(this);
			if (exec){
				interpreter.execfile(script.getAbsolutePath());
				return false;
			}
			return interpreter.runsource(script.getAbsolutePath());
		} catch (Throwable e) {
			throw new Exception(e);
		} finally {
			PyContext.setPlugin(null);
		}
	}
}


