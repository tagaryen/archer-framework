package com.archer.framework.datasource.mysql;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.archer.framework.base.component.ForwardComponent;
import com.archer.tools.java.ClassUtil;

public class RepositoryForward implements ForwardComponent {

	@Override
	public List<Object> listForwardComponents(List<Class<?>> classes) {
		List<Object> repos = new ArrayList<>(64);
		for(Class<?> cls: classes) {
			if(Repository.class.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
				repos.add(ClassUtil.newInstance(cls));
			}
		}
		return repos;
	}

}
