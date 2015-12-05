package jycraft.plugin.interpreter;

import jycraft.plugin.JyCraftPlugin;

/**
 * Thread context
 */
public class PyContext {

    public static final ThreadLocal<PyContext> context = new ThreadLocal<PyContext>();

    public static JyCraftPlugin getPlugin() {
        PyContext context = PyContext.context.get();
        return context == null ? null : context.plugin;
    }

    public static void setPlugin(JyCraftPlugin plugin) {
        context.set(new PyContext(plugin));
    }

    public JyCraftPlugin plugin;

    public PyContext(JyCraftPlugin plugin) {
        this.plugin = plugin;
    }
}
