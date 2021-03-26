package com.neocoretechs.bootstrapper;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.neocoretechs.relatrix.server.HandlerClassLoader;
/**
 * This class is a proxy to the HandlerClassLoader that will retrieve bytecodes from the Relatrix server specified as arg 0
 * on the command line, and run the main method specified as arg 1, and use the remaining arguments on the command line to pass to
 * the initialized main method.<p/>
 * Functions in the same manner an app server does to load bytecode from a centralized location to bring up a container, 
 * but in this case the container is just the JVM and the app is any old app for which bytecode is made available.<p/>
 * Due to the strange nature of recursion and the delegation model, specifying loading bytecode from a command line supplied
 * variable, like -Djava.system.class.loader=com.neocoretechs.relatrix.server.HandlerClassLoader is a problem since all sorts of
 * egg and chicken scenarios present themselves.<p/>
 * The caveat here is that the bytecode for ALL supporting classes has to be loaded into the Relatrix bytecode server which
 * runs on default port 9999 from a node accessible to all nodes requiring class loading. Methods within HandlerClassLoader
 * support the loading of JARs, classes, directories, etc, and for overwriting and deleting old bytecode.<p/>
 * The use case here is a cluster or robot or network where provisioning only has to be supplied to the server rather than directories
 * on each node, thus centralizing deployment and operation of the system.<p/>
 * java -cp /lib/BigSack.jar:/lib/Relatrix.jar:/lib/bootstrapper.jar. -DBigSack.properties="/lib/BigSack.properties" com.neocoretechs.bootstrapper.bootstrap <remote node> <main method> <arg 0> <arg 1> ...
 * @author Jonathan Groff (C) NeoCoreTechs 2021
 *
 */
public class bootstrap {
    public static final HandlerClassLoader hcl = new HandlerClassLoader();

	/**
	 * Bootstrap bytecode from Relatrix server running at arg[0] host using 
	 * the main method of arg[1], and passing to that main method the remaining arguments on the command line.<p/>
	 * The primary purpose to is to load from remote bytecode repository those classes needed to initialize and run the
	 * Java application from a centralized bytecode server instead of a collection of Jars or a directory, much as an app server do.
	 * @param args [0] - remote or local hostname [1] class with main method to exec [2-...] arguments to loaded main
	 */
	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("USAGE: java com.neocoretechs.bootstrapper.bootstrap <bytecode server node> <main method of class to execute> [arg 0 to main] [arg 1 to main] ...");
			return;
		}
      	int count = 0;
    	Class[] types = null;
    	String[] sargs = null;
    	Object[] oargs = new Object[1];
        try {
        	hcl.connectToRemoteRepository(args[0]);
        	Class targetClass = hcl.loadClass(args[1]);
        	final Method method = targetClass.getMethod("main", String[].class);
        	count = method.getParameterCount();
        	types = method.getParameterTypes();
            sargs = new String[args.length-2];
            for(int i = 0; i < args.length-2; i++) {
            	sargs[i] = args[i+2];
            }
            oargs[0] = sargs;
            method.invoke(null, oargs);
            //
        	//Constructor c = target.getConstructor(new Class[] {Class.forName(args[1])});
			//Object o =  c.newInstance(new Object[] {}); //default constructor call
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			System.out.println("Failed to invoke main method of "+args[1]+". Check bytecode server "+args[0]+" provisioning.");
	        for(int i = 0; i < oargs.length; i++) {
	            	System.out.println("Argument to main "+i+" - "+oargs[i]);
	        }
	        System.out.println("Should be:"+count+" of types:");
	        if(types == null)
	        	System.out.println("Wasn't able to get the parameter types, sorry");
	        else {
	        	if(types.length != oargs.length) {
	        		System.out.println("Required length of paramters is "+types.length+" but the proposed parameter array is of length "+oargs.length);
	        	} else
	        		for(int i = 0; i < types.length; i++) {
	        			System.out.println(i+" required:"+types[i]+" proposed:"+oargs[i].getClass());
	        		}
	        }
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Failed to connect to remote bytecode server, check status of server process on node "+args[0]);
			e.printStackTrace();
		}
	}

}
