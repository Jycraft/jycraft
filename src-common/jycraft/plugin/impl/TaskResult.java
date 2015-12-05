package jycraft.plugin.impl;

import org.python.core.PyException;

/**
 * Holds information about the result of running a task.
 *
 * This is used to communicate between the websocket thread and the main server thread the python code is executing
 * on.
 */
class TaskResult {
    boolean more = false;
    PyException exception;
    boolean done = false;
}
