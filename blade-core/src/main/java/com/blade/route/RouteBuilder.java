/**
 * Copyright (c) 2015, biezhi 王爵 (biezhi.me@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.route;

import java.lang.reflect.Method;
import java.util.Set;

import blade.kit.StringKit;
import blade.kit.resource.ClassPathClassReader;
import blade.kit.resource.ClassReader;

import com.blade.Aop;
import com.blade.Blade;
import com.blade.annotation.After;
import com.blade.annotation.Before;
import com.blade.annotation.Interceptor;
import com.blade.annotation.Path;
import com.blade.annotation.Route;
import com.blade.ioc.Container;
import com.blade.web.http.HttpMethod;

/**
 * 
 * <p>
 * 路由构造器
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class RouteBuilder {
    
    /**
	 * 默认路由后缀包，用户扫描路由所在位置，默认为route，用户可自定义
	 */
    private String pkgRoute = "route";
	
	/**
	 * 默认拦截器后缀包，用户扫描拦截器所在位置，默认为interceptor，用户可自定义
	 */
    private String pkgInterceptor = "interceptor";
	
    /**
     * 类读取器,用于在指定规则中扫描类
     */
    private ClassReader classReader = new ClassPathClassReader();
    
    /**
     * IOC容器，存储路由到ioc中
     */
    private Container container = null;
    
    private Blade blade;
    
    private Routers routers;
    
    public RouteBuilder(Blade blade) {
    	this.blade = blade;
    	this.routers = blade.routers();
    	this.container = blade.container();
    }
    
    /**
     * 开始构建路由
     */
    public void building() {
        String basePackage = blade.basePackage();
        
        if(StringKit.isNotBlank(basePackage)){
        	
        	// 处理如：com.xxx.* 表示递归扫描包
        	String suffix = basePackage.endsWith(".*") ? ".*" : "";
        	basePackage = basePackage.endsWith(".*") ? basePackage.substring(0, basePackage.length() - 2) : basePackage;
        	
			String routePackage = basePackage + "." + pkgRoute + suffix;
			String interceptorPackage = basePackage + "." + pkgInterceptor + suffix;
			
        	buildRoute(routePackage);
        	buildInterceptor(interceptorPackage);
        	
        } else {
        	// 路由
        	String[] routePackages = blade.routePackages();
        	if(null != routePackages && routePackages.length > 0){
        		buildRoute(routePackages);
        	}
        	
    		// 拦截器
        	String interceptorPackage = blade.interceptorPackage();
        	if(StringKit.isNotBlank(interceptorPackage)){
        		buildInterceptor(interceptorPackage);
        	}
		}
    }
    
    /**
     * 构建拦截器
     * 
     * @param interceptorPackages	要添加的拦截器包
     */
    private void buildInterceptor(String... interceptorPackages){
    	
    	// 扫描所有的Interceptor
		Set<Class<?>> classes = null;
		
    	// 拦截器
		for(String packageName : interceptorPackages){
			
			boolean recursive = false;
			
			if (packageName.endsWith(".*")) {
				packageName = packageName.substring(0, packageName.length() - 2);
				recursive = true;
			}
			
    		// 扫描所有的Interceptor
    		classes = classReader.getClassByAnnotation(packageName, Interceptor.class, recursive);
    		
    		if(null != classes && classes.size() > 0){
    			for(Class<?> interceptorClazz : classes){
    				parseInterceptor(interceptorClazz);
    			}
    		}
    	}
    }
    
    /**
     * 构建路由
     * 
     * @param routePackages		要添加的路由包
     */
    private void buildRoute(String... routePackages){
    	Set<Class<?>> classes = null;
    	// 路由
		for(String packageName : routePackages){
			
			boolean recursive = false;
			
			if (packageName.endsWith(".*")) {
				packageName = packageName.substring(0, packageName.length() - 2);
				recursive = true;
			}
			
    		// 扫描所有的Controoler
    		classes = classReader.getClassByAnnotation(packageName, Path.class, recursive);
    		
    		if(null != classes && classes.size() > 0){
    			for(Class<?> pathClazz : classes){
    				parseRouter(pathClazz);
    			}
    		}
    	}
    	
    }
    
    /**
     * 解析拦截器
     * 
     * @param interceptor		要解析的拦截器class
     */
    private void parseInterceptor(final Class<?> interceptor){
    	
    	Method[] methods = interceptor.getMethods();
    	if(null == methods || methods.length == 0){
    		return;
    	}
    	
    	container.registerBean(Aop.create(interceptor));
    	
    	for (Method method : methods) {
			
			Before before = method.getAnnotation(Before.class);
			After after = method.getAnnotation(After.class);
			
			if (null != before) {
				
				String suffix = before.suffix();
				
				String path = getRoutePath(before.value(), "", suffix);
				
				buildInterceptor(path, interceptor, method, HttpMethod.BEFORE);
				
				String[] paths = before.values();
				if(null != paths && paths.length > 0){
					for(String value : paths){
						String pathV = getRoutePath(value, "", suffix);
						buildInterceptor(pathV, interceptor, method, HttpMethod.BEFORE);
					}
				}
			}
			
			if (null != after) {
				
				String suffix = after.suffix();
				
				String path = getRoutePath(after.value(), "", suffix);
				
				buildInterceptor(path, interceptor, method, HttpMethod.AFTER);
				
				String[] paths = after.values();
				if(null != paths && paths.length > 0){
					for(String value : paths){
						String pathV = getRoutePath(value, "", suffix);
						buildInterceptor(pathV, interceptor, method, HttpMethod.AFTER);
					}
				}
			}
		}
    }
    
    /**
     * 解析一个控制器中的所有路由
     * 
     * @param controller		要解析的路由class
     */
    private void parseRouter(final Class<?> router){
    	
    	Method[] methods = router.getMethods();
    	if(null == methods || methods.length == 0){
    		return;
    	}
    	
    	container.registerBean(Aop.create(router));
    	
		final String nameSpace = router.getAnnotation(Path.class).value();
		
		final String suffix = router.getAnnotation(Path.class).suffix();
		
		for (Method method : methods) {
			
			Route mapping = method.getAnnotation(Route.class);
			
			//route方法
			if (null != mapping) {
				
				////构建路由
				String path = getRoutePath(mapping.value(), nameSpace, suffix);
				
				HttpMethod methodType = mapping.method();
				
				buildRoute(router, method, path, methodType);
				
				// 构建多个路由
				String[] paths = mapping.values();
				if(null != paths && paths.length > 0){
					for(String value : paths){
						String pathV = getRoutePath(value, nameSpace, suffix);
						buildRoute(router, method, pathV, methodType);
					}
				}
			}
		}
    }
    
    private String getRoutePath(String value, String nameSpace, String suffix){
    	String path = value.startsWith("/") ? value : "/" + value;
		path = nameSpace + path;
		path = path.replaceAll("[/]+", "/");
		
		path = path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		
		path = path + suffix;
		
		return path;
    }
    
    /**
     * 构建一个路由
     * 
     * @param target		路由目标执行的class
     * @param execMethod	路由执行方法
     * @param path			路由url
     * @param method		路由http方法
     */
    private void buildRoute(Class<?> clazz, Method execMethod, String path, HttpMethod method){
    	this.routers.route(path, clazz, execMethod, method);
    }
    
    /**
     * 构建一个路由
     * 
     * @param path			路由url
     * @param target		路由目标执行的class
     * @param execMethod	路由执行方法
     * @param method		路由http方法
     */
    private void buildInterceptor(String path, Class<?> clazz, Method execMethod, HttpMethod method){
    	this.routers.route(path, clazz, execMethod, method);
    }
    
}