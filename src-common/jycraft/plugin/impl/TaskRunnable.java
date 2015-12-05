package jycraft.plugin.impl;

import jycraft.plugin.JyCraftPlugin;
import jycraft.plugin.interpreter.PyContext;
import org.python.core.PyException;
import org.python.util.InteractiveInterpreter;

/**
 * Allows Runs the python interpreter in a different thread
 */
public class TaskRunnable implements Runnable{
	private final JyCraftPlugin plugin;
    private final boolean exec;
    private final InteractiveInterpreter interpreter;
    private final String code;
    private final TaskResult result;

    public TaskRunnable(JyCraftPlugin plugin, InteractiveInterpreter interpreter, String code, boolean exec) {
        this.plugin = plugin;
        this.exec = exec;
        this.interpreter = interpreter;
        this.code = code;
        this.result = new TaskResult();
    }

    /**
     * Runs the task
     */
    public void run() {
		try {
			PyContext.setPlugin(this.plugin);
			if (exec) {
				interpreter.exec(code);
			} else {
				result.more = interpreter.runsource(code);
			}
		} catch (PyException e) {
			result.exception = e;
		} finally {
			PyContext.setPlugin(null);
			// notify other call
			synchronized (result) {
				result.done = true;
				result.notifyAll();
			}
		}
	}

    /**
     * Blocks until the task is completed and returns where the interpreter needs more to complete the statement
     */
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
