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
package com.blade.web.http;

/**
 * 
 * <p>
 * Http异常
 * </p>
 *
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HttpException() {
		super();
	}

	public HttpException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable throwable) {
		super(throwable);
	}

}
