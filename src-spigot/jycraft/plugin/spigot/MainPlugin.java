package jycraft.plugin.spigot;

import jycraft.plugin.ConsolePlugin;
import jycraft.plugin.JyCraftPlugin;

import jycraft.plugin.impl.TaskRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.python.util.InteractiveInterpreter;

public class MainPlugin extends JavaPlugin implements JyCraftPlugin {

	@Override
	public void onEnable() {
		getLogger().info("Loading Python Console");
		int tcpsocketserverport = getConfig().getInt("pythonconsole.serverconsole.telnetport", 44444);
        int staticserveport = getConfig().getInt("pythonconsole.staticserve.staticserveport", 44446);
		String serverpass = getConfig().getString("pythonconsole.serverconsole.password", "swordfish");
		String staticserverootdir = getConfig().getString("pythonconsole.staticserve.rootdir", System.getProperty("user.dir").concat("/static"));
        String staticservedir = getConfig().getString("pythonconsole.staticserve.staticdir", System.getProperty("user.dir").concat("/static"));
		ConsolePlugin.start(this, tcpsocketserverport, staticserveport, serverpass, staticserverootdir, staticservedir);
	}

	@Override
	public void log(String message) {
		getLogger().info(message);
	}

	@Override
	public boolean parse(final InteractiveInterpreter interpreter, final String code, final boolean exec) throws Exception {
		final TaskRunnable runnable = new TaskRunnable(this, interpreter, code, exec);
		BukkitRunnable r = new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		} ;
		// run the python code on main thread
		r.runTask(this);
		return runnable.more();
	}
}
