package com.archer.framework.web.filter;

import java.lang.annotation.Annotation;

import com.archer.net.http.HttpRequest;
import com.archer.net.http.HttpResponse;

public interface AnnotationResponseFilter<T extends Annotation> {

	FilterState onResponse(HttpRequest req, HttpResponse res, Object ret);
	
	Class<T> getAnnotationType();
	
	int priority();
}
