package jycraft.plugin.impl;

import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.interpreter.PyContext;
import org.python.core.PyException;
import org.python.util.InteractiveInterpreter;

import java.io.File;

public class FileRunnable implements Runnable {
    private final JyCraftPlugin plugin;
    private final boolean exec;
    private final InteractiveInterpreter interpreter;
    private final File script;
    private final TaskResult result;


    public FileRunnable(JyCraftPlugin plugin, InteractiveInterpreter interpreter, File script, boolean exec) {
        this.plugin = plugin;
        this.exec = exec;
        this.script = script;
        this.result = new TaskResult();
        this.interpreter = interpreter;
    }


    @Override
    public void run() {
        try {
            PyContext.setPlugin(this.plugin);
            if (exec) {
                interpreter.execfile(this.script.getAbsolutePath());
            }
        } catch (PyException e){
            result.exception = e;
        } finally {
            PyContext.setPlugin(null);
            synchronized (result){
                result.done = true;
                result.notifyAll();
            }
        }
    }

    public boolean more() throws InterruptedException, PyException{
        synchronized (result) {
            while (!result.done) {
                result.wait();
            }
        }
        // if an exception occurs in the main thread throw it on the web-socket thread
        if(result.exception != null)
            throw result.exception;
        return result.more;
    }
}
