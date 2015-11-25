package jycraft.plugin.spigot;

import jycraft.plugin.ConsolePlugin;
import jycraft.plugin.JyCraftPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.python.util.InteractiveInterpreter;

import java.io.File;

public class MainPlugin extends JavaPlugin implements JyCraftPlugin {

	@Override
	public void onEnable() {
		getLogger().info("Loading Python Console");
		int tcpsocketserverport = getConfig().getInt("pythonconsole.serverconsole.telnetport", 44444);
		int websocketserverport = getConfig().getInt("pythonconsole.serverconsole.websocketport", 44445);
        int staticserveport = getConfig().getInt("pythonconsole.staticserve.staticserveport", 44446);
		String serverpass = getConfig().getString("pythonconsole.serverconsole.password", "swordfish");
		String staticserverootdir = getConfig().getString("pythonconsole.staticserve.rootdir", System.getProperty("user.dir"));
        String staticservedir = getConfig().getString("pythonconsole.staticserve.staticdir", System.getProperty("user.dir").concat("/static"));
		ConsolePlugin.start(this, tcpsocketserverport, websocketserverport, staticserveport, serverpass, staticserverootdir, staticservedir);
	}

	@Override
	public void log(String message) {
		getLogger().info(message);
	}

	@Override
	public boolean parse(final InteractiveInterpreter interpreter, final String code, final boolean exec) throws Exception {
		try{
			final boolean[] morea = new boolean[]{false};
			BukkitRunnable r = new BukkitRunnable() {
				public void run() {
					if (exec) {
						interpreter.exec(code);
					} else {
						morea[0] = interpreter.runsource(code);
					}
				}
			};
			r.runTask(this);
			return morea[0];
		} catch (Throwable e) {
			throw new Exception(e);
		}
	}
}
