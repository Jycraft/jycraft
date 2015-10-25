package jycraft.plugin;

import org.python.util.InteractiveInterpreter;

public interface JyCraftPlugin {
    void log(String message);

    boolean parse(InteractiveInterpreter interpreter, String code, boolean exec) throws Exception;
}
