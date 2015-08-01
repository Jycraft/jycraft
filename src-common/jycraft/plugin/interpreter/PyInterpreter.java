package jycraft.plugin.interpreter;

import java.io.File;

import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.InteractiveInterpreter;

public class PyInterpreter extends InteractiveInterpreter {
	
	public PyInterpreter() {
		super(null, getPythonSystemState());
	}

	public static PySystemState getPythonSystemState() {
		PySystemState sys = new PySystemState();
		addPathToPySystemState(sys, "./");
		addPathToPySystemState(sys, "./python/");
		addPathToPySystemState(sys, "./python-plugins/");
		addPathToPySystemState(sys, "./lib-canary/");
		addPathToPySystemState(sys, "./lib-spigot/");
		return sys;
	}
	
	public static void addPathToPySystemState(PySystemState sys, String path) {
		try {
			sys.path.append(new PyString(path));
			File dependencyDirectory = new File(path);
			File[] files = dependencyDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
			    if (files[i].getName().endsWith(".jar")) {
			    	sys.path.append(new PyString(
			    			new File(path+files[i].getName()).getAbsolutePath()));
			    }
			}
		} catch (Exception e){}
	}
}
