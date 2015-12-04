package jycraft.plugin.sponge;

import com.google.inject.Inject;
import jycraft.plugin.ConsolePlugin;
import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.impl.TaskRunnable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.python.util.InteractiveInterpreter;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;
import java.io.IOException;

@Plugin(id="jycraft", name="Jython Console", version="1.0.0")
public class SpongePlugin implements JyCraftPlugin {

    private static final String[] TELNET_PATH = {"pythonconsole", "serverconsole", "telnetport"};
    private static final String[] WEBSOCKET_PATH = {"pythonconsole", "serverconsole", "websocketport"};
    private static final String[] PASSWORD_PATH = {"pythonconsole", "serverconsole", "password"};
    private static final String[] STATIC_SERVE_DIR = {"pythonconsole", "staticserve", "staticdir"};
    private static final String[] STATIC_SERVE_ROOT_DIR = {"pythonconsole", "staticserve", "rootdir"};

    @Inject
    public Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    public File confFile;

    @Inject
    @DefaultConfig(sharedRoot = true)
    public ConfigurationLoader<CommentedConfigurationNode> confLoader;

    @Inject
    public Game game;

    private volatile Boolean state = null;

    @Listener
    public void onStart(GameInitializationEvent event) {
        logger.info("Launching python console");
        try {
            ConfigurationNode config;
            if (!confFile.exists()) {
                confFile.createNewFile();
                config = confLoader.load();
                config.getNode((Object[])TELNET_PATH).setValue(44444);
                config.getNode((Object[])WEBSOCKET_PATH).setValue(44445);
                config.getNode((Object[])PASSWORD_PATH).setValue("swordfish");
                config.getNode((Object[])STATIC_SERVE_DIR).setValue(System.getProperty("user.dir"));
                config.getNode((Object[])STATIC_SERVE_ROOT_DIR).setValue(System.getProperty("user.dir"));
                confLoader.save(config);
            } else {
                config = confLoader.load();
            }
            int telnet = config.getNode((Object[])TELNET_PATH).getInt();
            int websocket = config.getNode((Object[])WEBSOCKET_PATH).getInt();
            String password = config.getNode((Object[])PASSWORD_PATH).getString();
            String staticservedir = config.getNode((Object[])STATIC_SERVE_DIR).getString();
            String staticserverootdir = config.getNode((Object[])STATIC_SERVE_ROOT_DIR).getString();
            ConsolePlugin.start(this, telnet, websocket, password, staticserverootdir, staticservedir);
        } catch (IOException e) {
            logger.error("Failed to load config");
        }
    }

    @Override
    public void log(String message) {
        logger.info(message);
    }

    @Override
    public boolean parse(InteractiveInterpreter interpreter, String code, boolean exec) throws Exception {
        final TaskRunnable runnable = new TaskRunnable(this, interpreter, code, exec);
        game.getScheduler().createTaskBuilder()
                .name("Python script runner")
                .execute(t -> runnable.run())
                .submit(this);
        return runnable.more();
    }
}
