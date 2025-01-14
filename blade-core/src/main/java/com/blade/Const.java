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

/**
 * 常量接口
 * 
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public interface Const {

	/**
	 * 当前最新版本
	 */
	String BLADE_VERSION = "1.5.0-beta";
	
	/**
     * 服务器500错误时返回的HTML
     */
	String INTERNAL_ERROR = "<html><head><title>500 Internal Error</title></head><body bgcolor=\"white\"><center><h1>500 Internal Error</h1></center><hr><center>blade "
			+ BLADE_VERSION +"</center></body></html>";
    
	/**
	 * 服务器404错误HTML
	 */
	String VIEW_NOTFOUND = "<html><head><title>404 Not Found</title></head><body bgcolor=\"white\"><center><h1>[ %s ] Not Found</h1></center><hr><center>blade "
			+ BLADE_VERSION +"</center></body></html>";
	
	/**
	 * jetty默认端口
	 */
	int DEFAULT_PORT = 9000;
	
	/**
	 * 请求线程池context key
	 */
	String BLADE_EXECUTOR = "blade-req-executor";
}
