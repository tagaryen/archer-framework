package com.archer.framework.base.component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.archer.framework.base.conf.Conf;
import com.archer.framework.base.logger.LoggerInitliazer;
import com.archer.framework.base.timer.Timer;
import com.archer.tools.java.PathUtil;

public class ClassContainer {
	
	private List<Class<?>> classes;
	private Conf conf;
	private ComponentContainer components;
	private Timer timer;
	private ArcherClassLoader checker;
	
	public ClassContainer(Conf conf, String mainCls) {
		this.checker = new ArcherClassLoader(mainCls);
		this.timer = new Timer();
		this.classes = listAllClasses();
		this.conf = conf;
		this.components = new ComponentContainer(classes, this.conf, new LoggerInitliazer(conf));
	}
	
	public ComponentContainer components() {
		return components;
	}
	
	public void loadComponents() {
		try {
			components.loadForwardComponents();
			components.loadAllComponents();
			components.logger().info("Archer Application started in {}ms", timer.calculateCost());
		} catch(Throwable e) {
			components.logger().error("{}", e.getLocalizedMessage());
			e.printStackTrace();
			components.logger().warn("Archer Application start failed");
			System.exit(0);
		}
	}
	
	private List<Class<?>> listAllClasses() {
		PathUtil.getCurrentWorkDir();
		List<Class<?>> classes = new ArrayList<>();
		URL[] urls = getClassPathURL();
        for (URL url : urls) {
        	String path = url.getPath();
        	try {
            	if(new File(path).isDirectory()) {
            		classes.addAll(getClassesFromPath(path, null));
            	} else if(path.endsWith(".jar")) {
            		classes.addAll(getClassesFromJar(path));
            	}
        	} catch(Exception ignore) {}
        }
        
        return classes;
	}

	private URL[] getClassPathURL() {
        String cp = System.getProperty("java.class.path");
		ArrayList<URL> path = new ArrayList<>();
        int off = 0, next = -1;
        do {
        	try {
                next = cp.indexOf(File.pathSeparator, off);
                String element = (next == -1)
                    ? cp.substring(off)
                    : cp.substring(off, next);
                if (!element.isEmpty()) {
                	File file = new File(element).getCanonicalFile();
                    String filepath = file.getAbsolutePath();
                    if (!filepath.startsWith("/")) {
                    	filepath = "/" + filepath;
                    }
                    if (!filepath.endsWith("/") && file.isDirectory()) {
                    	filepath = filepath + "/";
                    }
                    URL url = new URL("file", "", filepath);
                    if (url != null) {
                    	path.add(url);
                    }
                }
                off = next + 1;
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        } while (next != -1);

        int size = path.size();
        ArrayDeque<URL> unopenedUrls = new ArrayDeque<>(size);
        for (int i = 0; i < size; i++) {
            unopenedUrls.add(path.get(i));
        }
        return path.toArray(new URL[0]);
	}
	
	private List<Class<?>> getClassesFromPath(String path, String parentPkg) {
        List<Class<?>> classes = new ArrayList<>();
        File directory = new File(path);
        if(parentPkg == null) {
        	parentPkg = "";
        } else {
        	parentPkg += ".";
        }
        File[] files = directory.listFiles();
        if(files == null) {
        	return classes;
        }
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = parentPkg + file.getName().substring(0, file.getName().length() - 6);
            	Class<?> clazz = checker.checkAndLoadClass(className);
            	if(clazz != null) {
                    classes.add(clazz);
            	}
            } else if(file.isDirectory()) {
            	List<Class<?>> subClasses = getClassesFromPath(file.getAbsolutePath(), parentPkg + file.getName());
            	classes.addAll(subClasses);
            }
        }
        return classes;
    }
	
	private List<Class<?>> getClassesFromJar(String jarFilePath) throws IOException {
    	List<Class<?>> classes = new ArrayList<>();
        try(JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                	String className = null;
                    try {
                    	className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                    	Class<?> clazz = checker.checkAndLoadClass(className);
                    	if(clazz != null) {
                            classes.add(clazz);
                    	}
                    } catch (Throwable ignore) {}
                }
            }
        }
        return classes;
    }
}
