package com.archer.framework.web.filter;

import java.lang.annotation.Annotation;

import com.archer.net.http.HttpRequest;
import com.archer.net.http.HttpResponse;

public interface AnnotationRequestFilter<T extends Annotation> {

	FilterState onRequest(HttpRequest req, HttpResponse res);
	
	Class<T> getAnnotationType();
	
	int priority();
}
