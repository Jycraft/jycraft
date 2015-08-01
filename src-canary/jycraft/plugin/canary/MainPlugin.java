package jycraft.plugin.canary;

import java.io.IOException;

import jycraft.plugin.ConsolePlugin;
import jycraft.plugin.utils.SetupUtils;
import net.canarymod.plugin.Plugin;

public class MainPlugin extends Plugin {
	
	public MainPlugin() {
		super();
	}
	
    @Override
    public boolean enable() {
    	getLogman().info("Loading Python Console");
    	try {
    		int tcpsocketserverport = getConfig().getInt("pythonconsole.serverconsole.telnetport", 44444);
    		int websocketserverport = getConfig().getInt("pythonconsole.serverconsole.websocketport", 44445);
    		String serverpass = getConfig().getString("pythonconsole.serverconsole.password", "swordfish");
    		SetupUtils.setup();
    		ConsolePlugin.start(this, tcpsocketserverport, websocketserverport, serverpass);
		} catch (IOException e) {
			getLogman().error(e.getMessage());
			return false;
		}
    	return true;
    }

	@Override
	public void disable() {}
    
}


