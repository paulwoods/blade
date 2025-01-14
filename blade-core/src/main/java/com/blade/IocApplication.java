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
package com.blade;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import blade.kit.log.Logger;
import blade.kit.resource.ClassPathClassReader;
import blade.kit.resource.ClassReader;

import com.blade.ioc.Container;
import com.blade.ioc.Scope;
import com.blade.plugin.Plugin;

/**
 * IOC容器初始化类
 * <p>
 * 用于初始化ioc对象
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class IocApplication {

	private static final Logger LOGGER = Logger.getLogger(IocApplication.class);
	
	/**
	 * IOC容器，单例获取默认的容器实现
	 */
	private Container container = null;
	
	/**
	 * 类读取对象，加载class
	 */
	private ClassReader classReader = null;
	
	/**
	 * 插件列表
	 */
	private List<Plugin> plugins = null;
	
	public IocApplication(Container container) {
		this.classReader = new ClassPathClassReader();
		this.plugins = new ArrayList<Plugin>();
		this.container = container;
	}
	
	/**
	 * 初始化IOC
	 * @param iocs		IOC包
	 * @param bootstrap	启动对象
	 */
	public void init(String[] iocs, Bootstrap bootstrap){
		
		// 初始化全局配置类
		if(null == container.getBean(Bootstrap.class, Scope.SINGLE)){
			container.registerBean(bootstrap);
		}
		
		// 初始化ioc容器，加载ioc包的对象 要配置符合ioc的注解的类才会被加载
		if(null != iocs && iocs.length > 0){
			for(String packageName : iocs){
				registerBean(packageName);
			}
		}
		
		// 初始化注入
		container.initWired();
		
		Set<String> names = container.getBeanNames();
		for(String name : names){
			LOGGER.info("Add Object：" + name + "=" + container.getBean(name, null));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Plugin> T registerPlugin(Class<T> plugin){
		Object object = container.registerBean(Aop.create(plugin));
		T t = (T) object;
		plugins.add(t);
		return t;
	}

	public <T extends Plugin> T getPlugin(Class<T> plugin){
		if(null != plugin && null != container){
			return container.getBean(plugin, null);
		}
		return null;
	}
	
	/**
	 * 注册一个包下的所有对象
	 * 
	 * @param packageName 包名称
	 */
	private void registerBean(String packageName) {
		
		// 是否递归扫描
		boolean recursive = false; 
		if (packageName.endsWith(".*")) {
			packageName = packageName.substring(0, packageName.length() - 2);
			recursive = true;
		}
		
		// 扫描包下所有class
		Set<Class<?>> classes = classReader.getClass(packageName, recursive);
		for (Class<?> clazz : classes) {
			// 注册带有Component和Service注解的类
			if (container.isRegister(clazz.getAnnotations())) {
				container.registerBean(Aop.create(clazz));
			}
		}
	}
	
	public List<Plugin> getPlugins() {
		return plugins;
	}

	/**
	 * 销毁
	 */
	public void destroy() {
		// 清空ioc容器
		container.removeAll();
		for(Plugin plugin : plugins){
			plugin.destroy();
		}
	}
}