package jycraft.plugin.utils;

import jycraft.plugin.canary.CanaryParser;
import jycraft.plugin.spigot.SpigotParser;
import net.canarymod.plugin.Plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.python.util.InteractiveInterpreter;

public class PluginUtils {
	public static void log(Object plugin, String message) {
		if (isCanary(plugin)) {
			((Plugin) plugin).getLogman().info(message);
		} else {
			((JavaPlugin) plugin).getLogger().info(message);
		}
	}
	
	public static boolean isCanary(Object plugin) {
		return plugin instanceof jycraft.plugin.canary.MainPlugin;
	}
	
	public static boolean isSpigot(Object plugin) {
		return !isCanary(plugin);
	}
	
	public static boolean parse(Object caller, InteractiveInterpreter interpreter, String code, boolean exec) throws Exception {
		if (PluginUtils.isCanary(caller))
			return CanaryParser.parse(interpreter, code, exec);
		else
			return SpigotParser.parse(interpreter, code, exec, caller);
	}

	
}
