package com.archer.framework.base.component;

import java.lang.reflect.Modifier;

import com.archer.framework.base.annotation.Component;
import com.archer.framework.base.annotation.Config;
import com.archer.framework.base.annotation.Controller;
import com.archer.framework.base.annotation.Service;
import com.archer.framework.base.exceptions.ArcherApplicationException;
import com.archer.framework.base.exceptions.TypeException;
import com.archer.tools.java.StringUtil;

class ArcherClassLoader {
	
	private final String basePkg = "com.archer.framework.";
	private final String pkg;
	
	public ArcherClassLoader(String clsName) {
		this.pkg = getPackageName(clsName);
	}
	
    private String getPackageName(String name) {
    	if(StringUtil.isEmpty(name)) {
    		throw new TypeException("Invalid class name " + name);
    	}
    	int lastDot = name.lastIndexOf('.');
    	if(lastDot < 2 || lastDot == name.length() - 1) {
    		throw new TypeException("Invalid class name " + name);
    	}
    	return name.substring(0, lastDot + 1);
    }
	
    private boolean isAllowed(String name) {
		return name == null ? false : (name.startsWith(pkg) || name.startsWith(basePkg));
	}
	
	
    protected Class<?> checkAndLoadClass(String name) {
		if(!isAllowed(name)) {
			return null;
		}
		
		Class<?> cls = null;
		try {
			cls = Class.forName(name);
		} catch(Throwable ignore) {
			return null;
		}
		
		boolean isNormalClass = (!cls.isInterface() && !Modifier.isAbstract(cls.getModifiers()));
		
		if(ForwardComponent.class.isAssignableFrom(cls) && isNormalClass) {
			return cls;
		}

		boolean annotationed = false;
		Config config = cls.getAnnotation(Config.class);
		if(config != null) {
			annotationed = true;
			if(isNormalClass) {
				return cls;
			}
		}
		
		Controller controller = cls.getAnnotation(Controller.class);
		if(controller != null) {
			annotationed = true;
			if(isNormalClass) {
				return cls;
			}
		}
		
		Component component = cls.getAnnotation(Component.class);
		if(component != null) {
			annotationed = true;
			if(isNormalClass) {
				return cls;
			}
		}

		Service service = cls.getAnnotation(Service.class);
		if(service != null) {
			annotationed = true;
			if(isNormalClass) {
				return cls;
			}
		}
		
		if(annotationed) {
			throw new ArcherApplicationException("class '" + cls.getName() + "' must not be interface or abstract");
		}
		return cls;
	}

}
