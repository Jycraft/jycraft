package jycraft.plugin;

import org.python.util.InteractiveInterpreter;

import java.io.File;

public interface JyCraftPlugin {
    void log(String message);

    boolean parse(InteractiveInterpreter interpreter, String code, boolean exec) throws Exception;
    boolean parse(InteractiveInterpreter interpreter, File script, boolean exec) throws Exception;
}
