package com.archer.framework.base.component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.archer.framework.base.annotation.ConstructorParam;
import com.archer.framework.base.exceptions.ArcherApplicationException;

public class ContainerInstance {
	
	private Object instance;
	
	private Class<?> cls;
	
	private Parameter[] params;
	
	private Constructor<?> constructor;
	
	private String name;
	
	private boolean proxy;

	private Class<?> proxyClass;
	
	private Field[] fields;

	public ContainerInstance(Object instance) {
		this(instance.getClass(), instance, null);
	}
	
	public ContainerInstance(Class<?> cls, Object instance, Class<?> proxyClass) {
		this(cls, null, null, proxyClass);
		this.instance = instance;
	}

	
	public ContainerInstance(Class<?> cls, Constructor<?> constructor, Parameter[] params) {
		this(cls, constructor, params, null);
	}
	
	public ContainerInstance(Class<?> cls, Constructor<?> constructor, Parameter[] params, Class<?> proxyClass) {
		this.cls = cls;
		this.constructor = constructor;
		this.params = params;
		this.proxyClass = proxyClass;
		this.proxy = proxyClass != null;
		this.fields = loadFields(cls);
	}
	
	public Object newInstance(Map<String, ContainerInstance> components) {
		Object[] paramVals = new Object[params.length];
		for(int i = 0; i < params.length; i++) {
			Parameter p = params[i];
			ConstructorParam anp = p.getAnnotation(ConstructorParam.class);
			if(anp == null) {
				throw new ArcherApplicationException("class '"+p.getClass().getName()+"' constructor params is not a 'ConstructorParam' ");
			}
			String name = anp.name();
			if(name.isEmpty()) {
				name = p.getClass().getName();
			}
			ContainerInstance pobj = components.getOrDefault(name, null);
			if(pobj == null) {
				throw new ArcherApplicationException("can not found component (or whitch has no argurment constructor) '" + name + "' to construct " + cls.getName());
			}
			paramVals[i] = pobj.getInstance();
		}
		try {
			instance = constructor.newInstance(paramVals);
			return instance;
		} catch (Exception e) {
			throw new ArcherApplicationException("can not construct instance of '" + cls.getName() + "'", e);
		}
	}
	
	private Field[] loadFields(Class<?> cls) {
		Field[] selfFs = cls.getDeclaredFields();
		if(cls.getSuperclass().equals(Object.class)) {
			return selfFs;
		} else {
			Field[] superFs = loadFields(cls.getSuperclass());
			Field[] newfs = new Field[selfFs.length + superFs.length];
			System.arraycopy(selfFs, 0, newfs, 0, selfFs.length);
			System.arraycopy(superFs, 0, newfs, selfFs.length, superFs.length);
			selfFs = newfs;
		}
		return selfFs;
	}
	
	public Object getInstance() {
		return instance;
	}

	public Class<?> getCls() {
		return cls;
	}

	public Parameter[] getParams() {
		return params;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isProxy() {
		return proxy;
	}

	public Class<?> getProxyClass() {
		return proxyClass;
	}

	public Field[] getFields() {
		return fields;
	}
}
